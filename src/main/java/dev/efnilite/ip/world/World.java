package dev.efnilite.ip.world;

import dev.efnilite.ip.IP;
import dev.efnilite.ip.config.Config;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * 跑酷世界管理 — Folia 兼容版。
 * 优先创建独立 void 世界；若 Folia 阻止创建，则自动回退到服务端已有世界。
 */
public class World {

    private static String name;
    private static org.bukkit.World world;
    private static boolean isFallbackWorld = false;

    public static void create() {
        name = Config.CONFIG.getString("world.name");
        org.bukkit.World existing = Bukkit.getWorld(name);

        if (!Config.CONFIG.getBoolean("joining")) return;

        if (existing != null) {
            IP.logging().warn("Parkour world already loaded.");
            world = existing;
            isFallbackWorld = false;
            setup();
            return;
        }

        if (Config.CONFIG.getBoolean("world.delete-on-reload")) {
            deleteWorld();
        }

        // 尝试1: 标准 API 创建独立世界
        if (tryStandardCreation()) return;

        // 尝试2: NMS 反射创建
        if (tryNmsCreation()) return;

        // 尝试3: 回退到已有世界（Folia 专用方案）
        if (tryFallbackWorld()) return;

        // 完全失败
        IP.logging().error("==============================================");
        IP.logging().error(" 所有世界创建/回退方案均失败！");
        IP.logging().error(" 请检查服务端配置或将 config.yml 中 joining 设为 false");
        IP.logging().error("==============================================");
    }

    /**
     * 尝试标准 Paper/Spigot API 创建世界。
     */
    private static boolean tryStandardCreation() {
        try {
            ChunkGenerator gen = dev.efnilite.vilib.util.VoidGenerator.getGenerator();
            if (gen == null) return false;

            org.bukkit.WorldCreator creator = new org.bukkit.WorldCreator(name)
                    .generateStructures(false)
                    .type(WorldType.NORMAL)
                    .generator(gen)
                    .environment(org.bukkit.World.Environment.NORMAL);

            // 先试 creator.createWorld()（部分 Folia fork 允许）
            try {
                world = creator.createWorld();
                if (world != null) {
                    IP.log("World '" + name + "' created via WorldCreator.createWorld()!");
                    setup();
                    return true;
                }
            } catch (UnsupportedOperationException e) {
                IP.log("WorldCreator.createWorld() blocked by Folia.");
            }

            // 再试 Bukkit.createWorld()
            try {
                world = Bukkit.createWorld(creator);
                if (world != null) {
                    IP.log("World '" + name + "' created via Bukkit.createWorld()!");
                    setup();
                    return true;
                }
            } catch (UnsupportedOperationException e) {
                IP.log("Bukkit.createWorld() blocked by Folia.");
            }

            // 试 Bukkit.getWorld（可能自动加载预创建的世界）
            world = Bukkit.getWorld(name);
            if (world != null) {
                IP.log("World '" + name + "' loaded via Bukkit.getWorld()!");
                setup();
                return true;
            }

        } catch (Exception e) {
            IP.logging().warn("Standard creation failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * 尝试通过 NMS 反射创建世界。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean tryNmsCreation() {
        try {
            // 准备世界文件夹
            File worldFolder = new File(name);
            worldFolder.mkdirs();
            for (String sub : new String[]{"region","data","entities","playerdata","stats","advancements","datapacks"})
                new File(worldFolder, sub).mkdirs();
            try { new File(worldFolder, "session.lock").createNewFile(); } catch (IOException ignored) {}
            writeLevelDatNBT(worldFolder);

            // 获取 MinecraftServer
            Object craftServer = Bukkit.getServer();
            Method getServer = craftServer.getClass().getDeclaredMethod("getServer");
            getServer.setAccessible(true);
            Object ms = getServer.invoke(craftServer);
            Class<?> msClass = ms.getClass();

            // 检查 levels map
            Field levelsField = findField(msClass, "levels");
            if (levelsField == null) return false;
            levelsField.setAccessible(true);
            java.util.Map levels = (java.util.Map) levelsField.get(ms);

            // 使用 WorldLoader
            Field wlField = findField(msClass, "worldLoader");
            if (wlField == null) return false;
            wlField.setAccessible(true);
            Object worldLoader = wlField.get(ms);

            // 用世界名建立 ResourceKey
            Class<?> rkClass = Class.forName("net.minecraft.resources.ResourceKey");
            Class<?> rlClass = Class.forName("net.minecraft.resources.ResourceLocation");
            Class<?> regClass = Class.forName("net.minecraft.core.registries.Registries");
            Object dimReg = regClass.getField("DIMENSION").get(null);
            Object location = rlClass.getConstructor(String.class, String.class).newInstance(name, name);
            Method rkCreate = rkClass.getDeclaredMethod("create",
                    Class.forName("net.minecraft.resources.ResourceKey"), rlClass);
            rkCreate.setAccessible(true);
            Object worldKey = rkCreate.invoke(null, dimReg, location);

            if (levels.containsKey(worldKey)) {
                // 已经注册，尝试获取
                world = wrapServerLevel(levels.get(worldKey));
                if (world != null) { setup(); return true; }
            }

            // 获取 overworld 的 ServerLevel 作为模板
            Object owLoc = rlClass.getConstructor(String.class, String.class).newInstance("overworld", "overworld");
            Object owKey = rkCreate.invoke(null, dimReg, owLoc);
            Object owLevel = levels.get(owKey);
            if (owLevel == null) return false;

            // 准备 levelAccess
            Field ssField = findField(msClass, "storageSource");
            if (ssField == null) return false;
            ssField.setAccessible(true);
            Object storageSource = ssField.get(ms);
            Method createAccess = storageSource.getClass().getDeclaredMethod("createAccess", String.class);
            createAccess.setAccessible(true);
            Object levelAccess = createAccess.invoke(storageSource, name);

            // 获取 DimensionType
            Field dtField = null;
            for (Field f : owLevel.getClass().getDeclaredFields()) {
                if (f.getType().getSimpleName().contains("DimensionType")) { dtField = f; break; }
            }
            Object dimType = dtField != null ? dtField.get(owLevel) : null;

            // 获取 BiomeSource
            Field bsField = null;
            for (Field f : owLevel.getClass().getDeclaredFields()) {
                if (f.getType().getSimpleName().contains("Biome")) { bsField = f; break; }
            }
            if (bsField != null) bsField.setAccessible(true);
            Object biomeSource = bsField != null ? bsField.get(owLevel) : null;

            // 获取 levelData
            Field ldField = findField(owLevel.getClass(), "serverLevelData");
            if (ldField == null) ldField = findField(owLevel.getClass(), "levelData");
            if (ldField == null) return false;
            ldField.setAccessible(true);
            Object levelData = ldField.get(owLevel);

            // 获取或创建 ChunkGenerator
            Object nmsGen = null;
            Field csField = findField(owLevel.getClass(), "chunkSource");
            if (csField != null) {
                csField.setAccessible(true);
                Object cs = csField.get(owLevel);
                Field cgField = findField(cs.getClass(), "generator");
                if (cgField != null) { cgField.setAccessible(true); nmsGen = cgField.get(cs); }
            }

            // 获取 randomState
            Field rsField = findField(owLevel.getClass(), "randomState");
            Object randomState = rsField != null ? rsField.get(owLevel) : null;

            // 获取 executor
            Field exField = findField(msClass, "executor");
            Object executor = exField != null ? exField.get(ms) : null;

            // 尝试构造 ServerLevel
            java.lang.reflect.Constructor<?>[] ctors = owLevel.getClass().getDeclaredConstructors();
            for (java.lang.reflect.Constructor<?> ctor : ctors) {
                ctor.setAccessible(true);
                Class<?>[] pts = ctor.getParameterTypes();
                Object[] args = new Object[pts.length];
                boolean allFilled = true;
                for (int i = 0; i < pts.length; i++) {
                    if (pts[i].isAssignableFrom(ms.getClass())) args[i] = ms;
                    else if (pts[i].getSimpleName().contains("LevelStorageAccess")) args[i] = levelAccess;
                    else if (pts[i].getSimpleName().contains("ServerLevelData") || pts[i].getSimpleName().contains("WorldData")) args[i] = levelData;
                    else if (pts[i] == rkClass) args[i] = worldKey;
                    else if (pts[i].getSimpleName().contains("DimensionType") || pts[i].getSimpleName().contains("LevelStem")) args[i] = dimType;
                    else if (pts[i].getSimpleName().contains("ChunkGenerator")) args[i] = nmsGen;
                    else if (pts[i].getSimpleName().contains("Biome")) args[i] = biomeSource;
                    else if (pts[i].getSimpleName().contains("Executor")) args[i] = executor;
                    else if (pts[i].getSimpleName().contains("RandomState")) args[i] = randomState;
                    else if (pts[i] == long.class) args[i] = 0L;
                    else if (pts[i] == int.class) args[i] = 0;
                    else if (pts[i] == boolean.class) args[i] = false;
                    else if (java.util.List.class.isAssignableFrom(pts[i])) args[i] = java.util.Collections.emptyList();
                    else { allFilled = false; break; }
                }
                if (!allFilled) continue;

                try {
                    Object newLevel = ctor.newInstance(args);
                    levels.put(worldKey, newLevel);
                    world = wrapServerLevel(newLevel);
                    if (world != null) {
                        IP.log("World '" + name + "' created via NMS reflection!");
                        setup();
                        return true;
                    }
                } catch (Exception ignored) {}
            }

        } catch (Exception e) {
            IP.logging().warn("NMS creation failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * 核心回退方案：使用服务端已有世界。
     * 不创建新世界，直接复用默认世界（如 "world"）。
     * 跑酷区域会被放置在远距离坐标处，不影响正常游戏。
     */
    private static boolean tryFallbackWorld() {
        // 优先使用 config 中配置的 fallback 世界
        String fallbackName = Config.CONFIG.getString("world.fall-back");
        if (fallbackName != null && !fallbackName.isEmpty()) {
            org.bukkit.World fb = Bukkit.getWorld(fallbackName);
            if (fb != null) {
                world = fb;
                name = fb.getName();
                isFallbackWorld = true;
                IP.log("==============================================");
                IP.log("  Folia 阻止创建新世界，已回退到配置的 fallback 世界:");
                IP.log("  '" + name + "' (config: world.fall-back)");
                IP.log("  跑酷区域将被放置在远距离坐标，不影响正常游戏。");
                IP.log("==============================================");
                setup();
                return true;
            }
        }

        // 自动选择一个非 parkour 的已有世界
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            if (!w.getName().equals(name)) {
                world = w;
                name = w.getName();
                isFallbackWorld = true;
                IP.log("==============================================");
                IP.log("  Folia 阻止创建新世界，自动回退到已有世界:");
                IP.log("  '" + name + "'");
                IP.log("  如果希望使用其他世界，请在 config.yml 中设置:");
                IP.log("  world.fall-back: <世界名称>");
                IP.log("  跑酷区域将被放置在远距离坐标，不影响正常游戏。");
                IP.log("==============================================");
                setup();
                return true;
            }
        }

        return false;
    }

    // ---- Helpers ----

    private static org.bukkit.World wrapServerLevel(Object serverLevel) {
        try {
            Class<?> cwClass = Class.forName("org.bukkit.craftbukkit.CraftWorld");
            for (java.lang.reflect.Constructor<?> c : cwClass.getDeclaredConstructors()) {
                c.setAccessible(true);
                try {
                    Object[] args = new Object[c.getParameterCount()];
                    Class<?>[] pts = c.getParameterTypes();
                    for (int i = 0; i < pts.length; i++) {
                        if (pts[i].isAssignableFrom(serverLevel.getClass())) args[i] = serverLevel;
                        else args[i] = null;
                    }
                    return (org.bukkit.World) c.newInstance(args);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try { return clazz.getDeclaredField(name); } catch (NoSuchFieldException e) {}
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void writeLevelDatNBT(File worldFolder) {
        File levelFile = new File(worldFolder, "level.dat");
        try {
            writeProperLevelDat(levelFile);
        } catch (Throwable ex) {
            try { writeMinimalLevelDat(levelFile); } catch (IOException ignored) {}
        }
    }

    private static void writeProperLevelDat(File levelFile) throws Throwable {
        Class<?> nbtIo = Class.forName("net.minecraft.nbt.NbtIo");
        Class<?> ct = Class.forName("net.minecraft.nbt.CompoundTag");
        Object data = ct.getDeclaredConstructor().newInstance();
        Method ps = ct.getMethod("putString", String.class, String.class);
        Method pi = ct.getMethod("putInt", String.class, int.class);
        Method pl = ct.getMethod("putLong", String.class, long.class);
        Method pb = ct.getMethod("putByte", String.class, byte.class);
        Method p = ct.getMethod("put", String.class, ct);
        ps.invoke(data, "LevelName", name);
        pi.invoke(data, "version", 19133);
        pi.invoke(data, "DataVersion", 3953);
        pl.invoke(data, "RandomSeed", (long)(Math.random()*Long.MAX_VALUE));
        pl.invoke(data, "LastPlayed", System.currentTimeMillis());
        pl.invoke(data, "Time", 0L); pl.invoke(data, "DayTime", 0L);
        pb.invoke(data, "allowCommands", (byte)0); pb.invoke(data, "Difficulty", (byte)0);
        pb.invoke(data, "DifficultyLocked", (byte)0); pb.invoke(data, "hardcore", (byte)0);
        Object wgs = ct.getDeclaredConstructor().newInstance();
        pl.invoke(wgs, "seed", 0L); pb.invoke(wgs, "generate_features", (byte)1);
        Object dims = ct.getDeclaredConstructor().newInstance();
        for (String dim : new String[]{"minecraft:overworld","minecraft:the_nether","minecraft:the_end"}) {
            Object d = ct.getDeclaredConstructor().newInstance(); ps.invoke(d, "type", dim);
            Object g = ct.getDeclaredConstructor().newInstance(); ps.invoke(g, "type", dim);
            ps.invoke(g, "biome_source", "minecraft:fixed"); p.invoke(d, "generator", g);
            p.invoke(dims, dim, d);
        }
        p.invoke(wgs, "dimensions", dims); p.invoke(data, "WorldGenSettings", wgs);
        p.invoke(data, "GameRules", ct.getDeclaredConstructor().newInstance());
        Object v = ct.getDeclaredConstructor().newInstance();
        pi.invoke(v, "Id", 3953); ps.invoke(v, "Name", "1.21"); pi.invoke(v, "Snapshot", 0);
        p.invoke(data, "Version", v);
        Object root = ct.getDeclaredConstructor().newInstance(); p.invoke(root, "Data", data);
        nbtIo.getDeclaredMethod("writeCompressed", ct, Path.class).invoke(null, root, levelFile.toPath());
    }

    private static void writeMinimalLevelDat(File levelFile) throws IOException {
        byte[] gz = { (byte)0x1f,(byte)0x8b,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x03,
            0x01,0x05,0x00,(byte)0xfb,(byte)0xff,0x0a,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00 };
        try (FileOutputStream fos = new FileOutputStream(levelFile)) { fos.write(gz); }
    }

    private static void setup() {
        if (world == null) return;
        final org.bukkit.World w = world;
        IP.log("Setting up world rules for '" + name + "'...");
        
        // Folia: setGameRule 必须在全局区域线程执行
        Bukkit.getGlobalRegionScheduler().run(IP.getPlugin(), task -> {
            try {
                w.setGameRule(GameRule.DO_FIRE_TICK, false);
                w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                w.setGameRule(GameRule.DO_TILE_DROPS, false);
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                w.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
                w.setGameRule(GameRule.KEEP_INVENTORY, true);
                w.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                w.getWorldBorder().setCenter(0, 0);
                w.getWorldBorder().setSize(10_000_000);
                w.setDifficulty(Difficulty.PEACEFUL);
                w.setClearWeatherDuration(1000000);
                w.setAutoSave(false);
                IP.log("World rules configured successfully.");
            } catch (Exception ex) {
                IP.logging().stack("Error setting up world rules", ex);
            }
        });
    }

    public static void delete() {
        if (isFallbackWorld) return; // 不删除回退世界
        if (!Config.CONFIG.getBoolean("world.delete-on-reload") || !Config.CONFIG.getBoolean("joining")) return;
        if (getWorld() != null) getWorld().getPlayers().forEach(p -> p.kickPlayer("Restarting"));
        deleteWorld();
    }

    private static void deleteWorld() {
        File file = new File(name);
        if (!file.exists()) return;
        if (world != null && !isFallbackWorld) {
            try { Bukkit.unloadWorld(world, false); } catch (Exception ignored) {}
            world = null;
        }
        try (Stream<Path> files = Files.walk(file.toPath())) {
            files.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception ignored) {}
    }

    public static String getName() { return name; }
    public static org.bukkit.World getWorld() { return world; }
}
