package dev.efnilite.ip.hook;

import dev.efnilite.ip.IP;
import dev.efnilite.ip.api.Registry;
import dev.efnilite.ip.config.Config;
import dev.efnilite.ip.mode.Mode;
import dev.efnilite.ip.mode.Modes;
import dev.efnilite.ip.util.FoliaTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Method;
import java.util.*;

/**
 * FancyHolograms 全息排行榜支持（纯反射，零编译依赖）。
 */
public class FancyHoloHook {

    private static final List<Object> activeHolograms = new ArrayList<>();

    public static void init() {
        if (!Bukkit.getPluginManager().isPluginEnabled("FancyHolograms")) {
            return;
        }
        IP.logging().info("已注册 FancyHolograms 钩子（反射模式）");

        FoliaTask.create(IP.getPlugin()).delay(60).execute(() -> {
            createHologramsByReflection();
            FoliaTask.create(IP.getPlugin()).async().delay(600).repeat(600)
                    .execute(FancyHoloHook::refreshAll).run();
        }).run();
    }

    @SuppressWarnings("unchecked")
    private static void createHologramsByReflection() {
        if (!Config.CONFIG.fileConfiguration.getBoolean("holograms.fancyholograms.enabled", false)) return;

        List<Map<?, ?>> rawList = Config.CONFIG.fileConfiguration.getMapList("holograms.fancyholograms.leaderboards");
        if (rawList == null || rawList.isEmpty()) return;

        for (Map<?, ?> rawEntry : rawList) {
            try {
                Map<String, Object> entry = (Map<String, Object>) (Map<?, ?>) rawEntry;
                String locStr = (String) entry.get("location");
                String modeName = (String) entry.getOrDefault("mode", "default");
                int lines = Integer.parseInt(String.valueOf(entry.getOrDefault("lines", 10)));

                Location loc = parseLocation(locStr);
                if (loc == null || loc.getWorld() == null) continue;

                Mode mode = Registry.getMode(modeName);
                if (mode == null) mode = Modes.DEFAULT;

                String holoName = "ip_lb_" + modeName + "_" + UUID.randomUUID().toString().substring(0, 8);
                List<String> textLines = DecentHoloHook.buildLeaderboardLines(mode, lines);

                Object holo = createFHHologram(holoName, loc, textLines);
                if (holo != null) {
                    activeHolograms.add(holo);
                    IP.logging().info("FancyHolograms: 已创建排行榜全息 '" + modeName + "' 于 " + locStr);
                }
            } catch (Exception e) {
                IP.logging().stack("FancyHolograms: 创建全息失败", e);
            }
        }
    }

    private static Object createFHHologram(String name, Location loc, List<String> lines) {
        try {
            // FancyHologramsPlugin.get().getHologramManager()
            Class<?> pluginClass = Class.forName("de.oliver.fancyholograms.api.FancyHologramsPlugin");
            Method getPlugin = pluginClass.getMethod("get");
            Object plugin = getPlugin.invoke(null);
            Method getManager = pluginClass.getMethod("getHologramManager");
            Object manager = getManager.invoke(plugin);

            // manager.create(name)
            Class<?> mgrClass = manager.getClass();
            Method createMethod = mgrClass.getMethod("create", String.class);
            Object holo = createMethod.invoke(manager, name);

            // holo.getData() → TextHologramData
            Class<?> holoClass = holo.getClass();
            Method getData = holoClass.getMethod("getData");
            Object data = getData.invoke(holo);

            // data.setText(lines), data.setLocation(loc)
            Class<?> dataClass = data.getClass();
            Method setText = dataClass.getMethod("setText", List.class);
            Method setLocation = dataClass.getMethod("setLocation", Location.class);
            setText.invoke(data, lines);
            setLocation.invoke(data, loc);

            // 配置完成，添加 hologram
            Method addMethod = mgrClass.getMethod("addHologram", holoClass);
            addMethod.invoke(manager, holo);

            return holo;
        } catch (Exception e) {
            IP.logging().warn("FancyHolograms API 调用失败: " + e.getMessage());
            return null;
        }
    }

    private static void refreshAll() {
        // FancyHolograms 全息投影更新策略：重建文本行
        for (Object holo : new ArrayList<>(activeHolograms)) {
            try {
                Class<?> holoClass = holo.getClass();
                Method getData = holoClass.getMethod("getData");
                Object data = getData.invoke(holo);
                // 简单地重新获取数据并刷新
                Method forceUpdate = holoClass.getMethod("forceUpdate");
                forceUpdate.invoke(holo);
            } catch (Exception ignored) {}
        }
    }

    private static Location parseLocation(String s) {
        String[] parts = s.split(",");
        if (parts.length < 4) return null;
        try {
            return new Location(Bukkit.getWorld(parts[0].trim()),
                    Double.parseDouble(parts[1].trim()), Double.parseDouble(parts[2].trim()),
                    Double.parseDouble(parts[3].trim()));
        } catch (Exception e) { return null; }
    }
}
