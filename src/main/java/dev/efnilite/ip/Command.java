package dev.efnilite.ip;

import dev.efnilite.ip.api.Registry;
import dev.efnilite.ip.config.Config;
import dev.efnilite.ip.config.Locales;
import dev.efnilite.ip.leaderboard.Leaderboard;
import dev.efnilite.ip.menu.Menus;
import dev.efnilite.ip.menu.ParkourOption;
import dev.efnilite.ip.mode.Mode;
import dev.efnilite.ip.mode.Modes;
import dev.efnilite.ip.mode.MultiMode;
import dev.efnilite.ip.player.ParkourPlayer;
import dev.efnilite.ip.player.ParkourUser;
import dev.efnilite.ip.player.data.InventoryData;
import dev.efnilite.ip.session.Session;
import dev.efnilite.vilib.command.ViCommand;
import dev.efnilite.vilib.inventory.item.Item;
import dev.efnilite.vilib.particle.ParticleData;
import dev.efnilite.vilib.particle.Particles;
import dev.efnilite.vilib.schematic.Schematic;
import dev.efnilite.vilib.schematic.Schematics;
import dev.efnilite.vilib.util.Locations;
import dev.efnilite.vilib.util.Strings;
import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

@SuppressWarnings("deprecation")
public class Command extends ViCommand {

    public static final HashMap<Player, Location[]> selections = new HashMap<>();

    private static final ItemStack WAND = new Item(Material.GOLDEN_AXE, "<red><bold>结构法杖 Schematic Wand")
            .lore("<gray>左键: 第一点 | 右键: 第二点")
            .build();

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        switch (args.length) {
            case 0 -> handle0Args(sender, player);
            case 1 -> handle1Args(args[0], sender, player);
            case 2 -> handle2Args(args[0], args[1], sender, player);
            case 3 -> handle3Args(args[0], args[1], args[2], sender, player);
            case 4 -> handle4Args(args[0], args[1], args[2], args[3], sender, player);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                if (ParkourOption.JOIN.mayPerform(sender)) {
                    completions.add("join");
                    completions.add("leave");
                }
                if (ParkourOption.MAIN.mayPerform(sender)) {
                    completions.add("menu");
                }
                if (ParkourOption.PLAY.mayPerform(sender)) {
                    completions.add("play");
                }
                if (ParkourOption.LEADERBOARDS.mayPerform(sender)) {
                    completions.add("leaderboard");
                }
                if (sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    completions.add("schematic");
                    completions.add("reload");
                    completions.add("forcejoin");
                    completions.add("forceleave");
                    completions.add("reset");
                    completions.add("recoverinventory");
                    completions.add("settings");
                }
                return completions(args[0], completions);
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("reset") && sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    completions.add("everyone");
                    for (ParkourPlayer pp : ParkourPlayer.getPlayers()) {
                        completions.add(pp.getName());
                    }
                } else if (args[0].equalsIgnoreCase("join") && sender.hasPermission(ParkourOption.JOIN.permission)) {
                    for (ParkourPlayer pp : ParkourPlayer.getPlayers()) {
                        completions.add(pp.getName());
                    }
                } else if (args[0].equalsIgnoreCase("schematic") && sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    completions.addAll(Arrays.asList("wand", "pos1", "pos2", "save", "paste"));
                } else if (args[0].equalsIgnoreCase("forcejoin") && sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    completions.add("nearest");
                    completions.add("everyone");
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        completions.add(pl.getName());
                    }
                } else if (args[0].equalsIgnoreCase("forceleave") && sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    completions.add("everyone");
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        completions.add(pl.getName());
                    }
                } else if (args[0].equalsIgnoreCase("recoverinventory") && sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        completions.add(pl.getName());
                    }
                } else if (args[0].equalsIgnoreCase("settings") && sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    completions.addAll(Arrays.asList("list", "get", "set", "reload", "gui"));
                }
                return completions(args[1], completions);
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("settings") && args[1].equalsIgnoreCase("list")
                        && sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    for (Config c : Config.values()) {
                        completions.add(c.name().toLowerCase());
                    }
                } else if (args[0].equalsIgnoreCase("settings") && (args[1].equalsIgnoreCase("get") || args[1].equalsIgnoreCase("set"))
                        && sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    completions.add("<配置名:路径>");
                    completions.add("config:debug");
                    completions.add("config:joining");
                    completions.add("config:world.name");
                    completions.add("generation:generation.settings.min-y");
                    completions.add("generation:advanced.border-size");
                }
                return completions(args[2], completions);
            }
            case 4 -> {
                if (args[0].equalsIgnoreCase("settings") && args[1].equalsIgnoreCase("set")
                        && sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    completions.add("<新值>");
                    completions.add("true");
                    completions.add("false");
                }
                return completions(args[3], completions);
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    private void handle0Args(@NotNull CommandSender sender, @Nullable Player player) {
        if (player != null && ParkourOption.MAIN.mayPerform(player)) {
            Menus.MAIN.open(player);
            return;
        }
        sendHelpMessages(sender);
    }

    private void sendHelpMessages(CommandSender sender) {
        send(sender, "");
        send(sender, "<dark_gray><strikethrough>---------------<reset> %s <dark_gray><strikethrough>---------------<reset>".formatted(IP.NAME));
        send(sender, "");
        send(sender, "<gray>/parkour <dark_gray>- 主菜单");
        if (sender.hasPermission(ParkourOption.JOIN.permission)) {
            send(sender, "<gray>/parkour join [模式/玩家] <dark_gray>- 加入默认模式或指定模式");
            send(sender, "<gray>/parkour leave <dark_gray>- 退出跑酷");
        }
        if (sender.hasPermission(ParkourOption.MAIN.permission)) {
            send(sender, "<gray>/parkour menu <dark_gray>- 打开菜单");
        }
        if (sender.hasPermission(ParkourOption.PLAY.permission)) {
            send(sender, "<gray>/parkour play <dark_gray>- 模式选择菜单");
        }
        if (sender.hasPermission(ParkourOption.LEADERBOARDS.permission)) {
            send(sender, "<gray>/parkour leaderboard [类型] <dark_gray>- 打开排行榜");
        }
        if (sender.hasPermission(ParkourOption.ADMIN.permission)) {
            send(sender, "<gray>/ip settings <dark_gray>- 管理配置文件");
            send(sender, "<gray>/ip reload <dark_gray>- 重载配置文件");
            send(sender, "<gray>/ip reset <everyone/玩家> <dark_gray>- 重置记录 <red>不可撤销!");
            send(sender, "<gray>/ip forcejoin <everyone/nearest/玩家> <dark_gray>- 强制加入");
            send(sender, "<gray>/ip forceleave <everyone/玩家> <dark_gray>- 强制退出");
            send(sender, "<gray>/ip recoverinventory <玩家> <dark_gray>- 恢复玩家背包");
        }
        send(sender, "");
    }

    private void handle1Args(@NotNull String arg, @NotNull CommandSender sender, @Nullable Player player) {
        switch (arg.toLowerCase()) {
            case "help" -> sendHelpMessages(sender);
            case "reload" -> {
                if (!cooldown(sender, "reload", 2500)) return;
                if (!sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    send(sender, noPermMsg());
                    return;
                }
                Config.reload(false);
                send(sender, "%s配置文件已重载。".formatted(IP.PREFIX));
            }
            case "settings" -> {
                if (!sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    send(sender, noPermMsg());
                    return;
                }
                sendSettingsHelp(sender);
            }
        }

        if (player == null) return;

        switch (arg.toLowerCase()) {
            case "join" -> {
                if (!cooldown(sender, "join", 2500)) return;
                if (!ParkourOption.JOIN.mayPerform(player)) {
                    send(sender, Locales.getString(player, "other.no_do"));
                    return;
                }
                ParkourUser user = ParkourUser.getUser(player);
                if (user != null) return;
                Modes.DEFAULT.create(player);
            }
            case "play" -> {
                if (ParkourOption.PLAY.mayPerform(player)) Menus.PLAY.open(player);
            }
            case "leave" -> {
                if (!cooldown(sender, "leave", 2500)) return;
                ParkourUser.leave(player);
            }
            case "menu", "main" -> {
                if (!ParkourOption.MAIN.mayPerform(player)) Menus.MAIN.open(player);
            }
            case "leaderboard" -> player.performCommand("ip leaderboard invalid");
            case "schematic" -> {
                if (!player.hasPermission(ParkourOption.ADMIN.permission)) {
                    send(sender, Locales.getString(player, "other.no_do"));
                    return;
                }
                send(player, "");
                send(player, "<red>/ip schematic wand <dark_gray>- <gray>获取结构法杖");
                send(player, "<red>/ip schematic pos1 <dark_gray>- <gray>设置第一点");
                send(player, "<red>/ip schematic pos2 <dark_gray>- <gray>设置第二点");
                send(player, "<red>/ip schematic save <dark_gray>- <gray>保存结构");
                send(player, "<red>/ip schematic paste <文件> <dark_gray>- <gray>粘贴结构");
                send(player, "");
            }
        }
    }

    private void handle2Args(@NotNull String arg1, @NotNull String arg2, @NotNull CommandSender sender, @Nullable Player player) {
        // ---- 管理员指令（无需玩家） ----
        switch (arg1.toLowerCase()) {
            case "forcejoin" -> {
                if (!sender.hasPermission(ParkourOption.ADMIN.permission)) return;
                if (arg2.equalsIgnoreCase("everyone")) {
                    Bukkit.getOnlinePlayers().forEach(other -> Modes.DEFAULT.create(other));
                    send(sender, IP.PREFIX + "已强制所有人加入！");
                    return;
                }
                if (arg2.equalsIgnoreCase("nearest")) {
                    Player closest = null;
                    double distance = Double.MAX_VALUE;
                    Location from = sender instanceof Player ? ((Player) sender).getLocation()
                            : (sender instanceof BlockCommandSender ? ((BlockCommandSender) sender).getBlock().getLocation() : null);
                    if (from == null || from.getWorld() == null) return;
                    for (Player p : from.getWorld().getPlayers()) {
                        double d = p.getLocation().distance(from);
                        if (d < distance) { distance = d; closest = p; }
                    }
                    if (closest == null) return;
                    send(sender, IP.PREFIX + "已强制 " + closest.getName() + " 加入！");
                    Modes.DEFAULT.create(closest);
                    return;
                }
                Player other = Bukkit.getPlayer(arg2);
                if (other == null) { send(sender, IP.PREFIX + "该玩家不在线！"); return; }
                Modes.DEFAULT.create(other);
            }
            case "forceleave" -> {
                if (!sender.hasPermission(ParkourOption.ADMIN.permission)) return;
                if (arg2.equalsIgnoreCase("everyone")) {
                    ParkourPlayer.getPlayers().forEach(ParkourUser::leave);
                    send(sender, IP.PREFIX + "已强制所有人退出！");
                    return;
                }
                Player other = Bukkit.getPlayer(arg2);
                if (other == null) { send(sender, IP.PREFIX + "该玩家不在线！"); return; }
                ParkourUser user = ParkourUser.getUser(other);
                if (user == null) { send(sender, IP.PREFIX + "该玩家不在跑酷中！"); return; }
                ParkourUser.leave(user);
            }
            case "reset" -> {
                if (!sender.hasPermission(ParkourOption.ADMIN.permission) || !cooldown(sender, "reset", 2500)) return;
                if (arg2.equalsIgnoreCase("everyone")) {
                    for (Mode mode : Registry.getModes()) {
                        Leaderboard leaderboard = mode.getLeaderboard();
                        if (leaderboard == null) continue;
                        leaderboard.resetAll();
                        leaderboard.write(true);
                    }
                    send(sender, IP.PREFIX + "已重置所有排行榜记录！");
                    return;
                }
                String name = null;
                UUID uuid = null;
                Player online = Bukkit.getPlayerExact(arg2);
                if (online != null) { name = online.getName(); uuid = online.getUniqueId(); }
                if (arg2.contains("-")) uuid = UUID.fromString(arg2);
                if (uuid == null) {
                    OfflinePlayer offline = Bukkit.getOfflinePlayer(arg2);
                    name = offline.getName(); uuid = offline.getUniqueId();
                }
                UUID finalUuid = uuid;
                String finalName = name;
                for (Mode mode : Registry.getModes()) {
                    Leaderboard leaderboard = mode.getLeaderboard();
                    if (leaderboard == null) continue;
                    leaderboard.remove(finalUuid);
                    leaderboard.write(true);
                }
                send(sender, IP.PREFIX + "已重置 " + finalName + " 的排行榜记录。");
            }
            case "recoverinventory" -> {
                if (!cooldown(sender, "recoverinventory", 2500) || !sender.hasPermission(ParkourOption.ADMIN.permission)) return;
                Player other = Bukkit.getPlayer(arg2);
                if (other == null) { send(sender, IP.PREFIX + "该玩家不在线！"); return; }
                new InventoryData(other).load(result -> {
                    if (result != null) {
                        send(sender, "%s已成功恢复 %s 的背包！".formatted(IP.PREFIX, other.getName()));
                    } else {
                        send(sender, "%s<red>恢复 %s 的背包时出错，请检查控制台。".formatted(IP.PREFIX, other.getName()));
                    }
                });
            }
            // ---- Settings 子命令 ----
            case "settings" -> handleSettings2(arg2, sender, player);
        }

        if (player == null) return;

        switch (arg1) {
            case "join" -> {
                if (!cooldown(sender, "join", 2500) || !ParkourOption.JOIN.mayPerform(player)) return;
                Mode mode = Registry.getMode(arg2);
                if (mode != null) { mode.create(player); return; }
                Player other = Bukkit.getPlayer(arg2);
                if (other == null) { send(sender, "%s未知玩家！".formatted(IP.PREFIX)); return; }
                ParkourPlayer parkourPlayer = ParkourPlayer.getPlayer(other);
                if (parkourPlayer == null) { send(sender, "%s未知玩家！".formatted(IP.PREFIX)); return; }
                ParkourUser user = ParkourUser.getUser(player);
                Session session = parkourPlayer.session;
                if (user != null && user.session == session) return;
                if (session.isAcceptingPlayers()) {
                    ((MultiMode) session.generator.getMode()).join(player, session);
                } else {
                    Modes.SPECTATOR.create(player, session);
                }
            }
            case "leaderboard" -> {
                if (!ParkourOption.LEADERBOARDS.mayPerform(player)) {
                    send(sender, Locales.getString(player, "other.no_do"));
                    return;
                }
                Mode mode = Registry.getMode(arg2.toLowerCase());
                if (mode == null) Menus.LEADERBOARDS.open(player);
                else Menus.SINGLE_LEADERBOARD.open(player, mode, Leaderboard.Sort.SCORE);
            }
            case "schematic" -> {
                if (!sender.hasPermission(ParkourOption.ADMIN.permission)) {
                    send(sender, Locales.getString(player, "other.no_do"));
                    return;
                }
                Location playerLocation = player.getLocation();
                Location[] existingSelection = selections.get(player);
                switch (arg2.toLowerCase()) {
                    case "wand" -> {
                        player.getInventory().addItem(WAND);
                        send(player, "<dark_gray>----------- <dark_red><bold>结构 Schematic<reset><dark_gray>-----------");
                        send(player, "<gray><red>左键<gray> -> 设置第一点 | <red>右键<gray> -> 设置第二点");
                    }
                    case "pos1" -> {
                        send(player, "%s第一点已设为 %s".formatted(IP.PREFIX, Locations.toString(playerLocation, true)));
                        if (existingSelection == null) selections.put(player, new Location[]{playerLocation, null});
                        else {
                            selections.put(player, new Location[]{playerLocation, existingSelection[1]});
                            Particles.box(BoundingBox.of(playerLocation, existingSelection[1]), player.getWorld(), new ParticleData<>(Particle.END_ROD, null, 2), player, 0.2);
                        }
                    }
                    case "pos2" -> {
                        send(player, "%s第二点已设为 %s".formatted(IP.PREFIX, Locations.toString(playerLocation, true)));
                        if (existingSelection == null) selections.put(player, new Location[]{null, playerLocation});
                        else {
                            selections.put(player, new Location[]{existingSelection[0], playerLocation});
                            Particles.box(BoundingBox.of(existingSelection[0], playerLocation), player.getWorld(), new ParticleData<>(Particle.END_ROD, null, 2), player, 0.2);
                        }
                    }
                    case "save" -> {
                        if (!cooldown(sender, "IP save schematic", 2500)) return;
                        if (existingSelection == null || existingSelection[0] == null || existingSelection[1] == null) {
                            send(player, "<dark_red><bold>结构 <reset><gray>请先设置两个坐标点。");
                            return;
                        }
                        String code = UUID.randomUUID().toString().split("-")[0];
                        send(player, "<dark_red><bold>结构 <reset><gray>正在保存，代码为 <red>'%s'<gray>。请记得添加到 schematics.yml。".formatted(code));
                        Schematic.save(IP.getInFolder("schematics/parkour-%s".formatted(code)), existingSelection[0], existingSelection[1], IP.getPlugin());
                    }
                }
            }
        }
    }

    // ---- Settings 子命令处理 ----

    private void sendSettingsHelp(CommandSender sender) {
        send(sender, "");
        send(sender, "<dark_gray><strikethrough>------<reset> <#FF6464><bold>配置管理 Settings<reset> <dark_gray><strikethrough>------");
        send(sender, "");
        send(sender, "<gray>/ip settings list [配置名] <dark_gray>- 列出配置项");
        send(sender, "<gray>  可用配置: config, generation, rewards, schematics");
        send(sender, "<gray>/ip settings get <配置:路径> <dark_gray>- 获取配置值");
        send(sender, "<gray>  例如: config:debug, config:world.name");
        send(sender, "<gray>/ip settings set <配置:路径> <值> <dark_gray>- 设置配置值");
        send(sender, "<gray>  例如: config:debug true");
        send(sender, "<gray>/ip settings reload <dark_gray>- 从磁盘重载所有配置");
        send(sender, "");
    }

    private void handleSettings2(@NotNull String arg2, @NotNull CommandSender sender, @Nullable Player player) {
        if (!sender.hasPermission(ParkourOption.ADMIN.permission)) {
            send(sender, noPermMsg());
            return;
        }
        switch (arg2.toLowerCase()) {
            case "list" -> {
                send(sender, "%s可用配置: config, generation, rewards, schematics".formatted(IP.PREFIX));
                send(sender, "%s使用 /ip settings list <配置名> 查看具体内容".formatted(IP.PREFIX));
            }
            case "reload" -> {
                Config.reload(false);
                send(sender, "%s所有配置文件已从磁盘重载!".formatted(IP.PREFIX));
            }
            case "gui" -> {
                if (player == null) { send(sender, "%sGUI 菜单需要玩家执行。".formatted(IP.PREFIX)); return; }
                openSettingsGui(player);
            }
            default -> sendSettingsHelp(sender);
        }
    }

    private void handle3Args(@NotNull String arg1, @NotNull String arg2, @NotNull String arg3,
                             @NotNull CommandSender sender, @Nullable Player player) {
        if (arg1.equalsIgnoreCase("settings")) {
            handleSettings3(arg2, arg3, sender, player);
            return;
        }

        if (player == null) return;

        if (arg1.equalsIgnoreCase("schematic") && arg2.equalsIgnoreCase("paste")) {
            if (!player.hasPermission(ParkourOption.ADMIN.permission)) return;
            Schematic schematic = Schematics.getSchematic(IP.getPlugin(), arg3);
            if (schematic == null) { send(sender, "%s找不到结构 %s".formatted(IP.PREFIX, arg3)); return; }
            schematic.paste(player.getLocation());
            send(sender, "%s已粘贴结构 %s".formatted(IP.PREFIX, arg3));
        }
    }

    private void handleSettings3(@NotNull String sub, @NotNull String arg3,
                                 @NotNull CommandSender sender, @Nullable Player player) {
        if (!sender.hasPermission(ParkourOption.ADMIN.permission)) {
            send(sender, noPermMsg());
            return;
        }
        switch (sub.toLowerCase()) {
            case "list" -> {
                Config config = parseConfigName(arg3);
                if (config == null) {
                    send(sender, "%s未知配置: %s。可用: config, generation, rewards, schematics".formatted(IP.PREFIX, arg3));
                    return;
                }
                FileConfiguration fc = config.fileConfiguration;
                send(sender, "");
                send(sender, "<#FF6464><bold>=== %s === ".formatted(config.name()) + config.fileName);
                for (String key : fc.getKeys(true)) {
                    Object val = fc.get(key);
                    if (val instanceof org.bukkit.configuration.ConfigurationSection) continue;
                    send(sender, "<gray>%s <dark_gray>= <white>%s".formatted(key, val));
                }
                send(sender, "");
            }
            case "get" -> {
                ConfigEntry entry = parseConfigPath(arg3);
                if (entry == null) {
                    send(sender, "%s格式错误。请使用: <配置名>:<路径>，例如 config:debug".formatted(IP.PREFIX));
                    return;
                }
                Object value = entry.config().fileConfiguration.get(entry.path());
                send(sender, "%s<gray>%s <dark_gray>= <white>%s".formatted(IP.PREFIX, arg3, value));
            }
            case "set" -> send(sender, "%s请使用: /ip settings set <配置:路径> <值>".formatted(IP.PREFIX));
            default -> sendSettingsHelp(sender);
        }
    }

    private void handle4Args(@NotNull String arg1, @NotNull String arg2, @NotNull String arg3,
                             @NotNull String arg4, @NotNull CommandSender sender, @Nullable Player player) {
        if (arg1.equalsIgnoreCase("settings") && arg2.equalsIgnoreCase("set")) {
            if (!sender.hasPermission(ParkourOption.ADMIN.permission)) {
                send(sender, noPermMsg());
                return;
            }
            ConfigEntry entry = parseConfigPath(arg3);
            if (entry == null) {
                send(sender, "%s格式错误。请使用: <配置名>:<路径>，例如 config:debug".formatted(IP.PREFIX));
                return;
            }
            FileConfiguration fc = entry.config().fileConfiguration;
            String path = entry.path();
            Object oldValue = fc.get(path);

            // 智能类型转换
            Object newValue = arg4;
            if (oldValue instanceof Boolean || "true".equalsIgnoreCase(arg4) || "false".equalsIgnoreCase(arg4)) {
                newValue = Boolean.parseBoolean(arg4);
            } else if (oldValue instanceof Integer) {
                try { newValue = Integer.parseInt(arg4); } catch (NumberFormatException ignored) {}
            } else if (oldValue instanceof Double) {
                try { newValue = Double.parseDouble(arg4); } catch (NumberFormatException ignored) {}
            } else if (oldValue instanceof Long) {
                try { newValue = Long.parseLong(arg4); } catch (NumberFormatException ignored) {}
            }

            fc.set(path, newValue);
            try {
                fc.save(entry.config().path);
                // 自动重载该配置文件
                entry.config().load();
                send(sender, "%s<green>已更新并重载! <gray>%s <dark_gray>= <white>%s (原值: %s)"
                        .formatted(IP.PREFIX, arg3, newValue, oldValue));
            } catch (IOException e) {
                send(sender, "%s<red>保存失败: %s".formatted(IP.PREFIX, e.getMessage()));
            }
        }
    }

    // ---- 工具方法 ----

    private String noPermMsg() {
        return "<gray>你没有权限使用此指令。";
    }

    private Config parseConfigName(String name) {
        return switch (name.toLowerCase()) {
            case "config", "config.yml" -> Config.CONFIG;
            case "generation", "generation.yml" -> Config.GENERATION;
            case "rewards", "rewards-v2.yml" -> Config.REWARDS;
            case "schematics", "schematics.yml" -> Config.SCHEMATICS;
            default -> null;
        };
    }

    private ConfigEntry parseConfigPath(String input) {
        int colonIdx = input.indexOf(':');
        if (colonIdx <= 0) return null;
        String configName = input.substring(0, colonIdx);
        String path = input.substring(colonIdx + 1);
        Config config = parseConfigName(configName);
        if (config == null) return null;
        return new ConfigEntry(config, path);
    }

    private record ConfigEntry(Config config, String path) {}

    // ---- 配置 GUI ----

    private void openSettingsGui(Player player) {
        dev.efnilite.vilib.inventory.Menu menu = new dev.efnilite.vilib.inventory.Menu(3,
                "<#FF6464><bold>配置管理器 Settings");

        int slot = 0;
        for (Config config : Config.values()) {
            String name = config.fileName;
            String desc = config.fileConfiguration.getKeys(false).size() + " 个顶级配置项";
            Item item = new Item(Material.BOOKSHELF, "<#6693E7><bold>" + name)
                    .lore("<gray>" + desc,
                          "<dark_gray>点击查看/修改配置项",
                          "",
                          "<#8DE5A3>点击进入");
            menu.item(slot++, item.click(event -> {
                if (!player.hasPermission(ParkourOption.ADMIN.permission)) return;
                openConfigDetailGui(player, config, "");
            }));
        }

        Item reloadItem = new Item(Material.CLOCK, "<#60EB76><bold>重载全部配置")
                .lore("<gray>从磁盘重新加载所有配置文件");
        menu.item(22, reloadItem.click(event -> {
            if (!player.hasPermission(ParkourOption.ADMIN.permission)) return;
            Config.reload(false);
            player.sendMessage(Strings.colour("%s<green>所有配置已重载!".formatted(IP.PREFIX)));
            player.closeInventory();
        }));

        Item closeItem = new Item(Material.BARRIER, "<#D71F1F><bold>关闭")
                .lore("<gray>返回上一页");
        menu.item(26, closeItem.click(event -> player.closeInventory()));

        menu.open(player);
    }

    private void openConfigDetailGui(Player player, Config config, String parentPath) {
        FileConfiguration fc = config.fileConfiguration;
        String title = parentPath.isEmpty() ? config.fileName : parentPath;
        if (title.length() > 28) title = title.substring(title.length() - 28);
        dev.efnilite.vilib.inventory.Menu menu = new dev.efnilite.vilib.inventory.Menu(6,
                "<#FF6464><bold>" + title);

        java.util.Set<String> keys;
        if (parentPath.isEmpty()) {
            keys = fc.getKeys(false);
        } else {
            org.bukkit.configuration.ConfigurationSection section = fc.getConfigurationSection(parentPath);
            keys = section != null ? section.getKeys(false) : new java.util.HashSet<>();
        }

        List<String> sortedKeys = new ArrayList<>(keys);
        java.util.Collections.sort(sortedKeys);

        int slot = 0;
        for (String key : sortedKeys) {
            if (slot >= 45) break;
            String fullPath = parentPath.isEmpty() ? key : parentPath + "." + key;
            Object value = fc.get(fullPath);

            if (value instanceof org.bukkit.configuration.ConfigurationSection) {
                Item item = new Item(Material.CHEST, "<#6693E7><bold>" + key)
                        .lore("<gray>类型: 配置节",
                              "<dark_gray>点击进入子配置");
                menu.item(slot++, item.click(event -> openConfigDetailGui(player, config, fullPath)));
            } else {
                String valStr = String.valueOf(value);
                String typeStr = value instanceof Boolean ? "布尔" : value instanceof Number ? "数字" : "字符串";
                Material mat = value instanceof Boolean ?
                        ((Boolean) value ? Material.LIME_WOOL : Material.RED_WOOL) :
                        Material.PAPER;
                Item item = new Item(mat, "<#60EB76><bold>" + key)
                        .lore("<gray>类型: " + typeStr,
                              "<gray>当前值: <white>" + valStr,
                              "",
                              "<#8DE5A3>左键 <dark_gray>切换/设置值",
                              "<#8DE5A3>右键 <dark_gray>查看指令用法");
                menu.item(slot++, item.click(event -> {
                    if (!player.hasPermission(ParkourOption.ADMIN.permission)) return;
                    if (value instanceof Boolean) {
                        boolean newVal = !(Boolean) value;
                        fc.set(fullPath, newVal);
                        saveAndReload(config, fc);
                        player.sendMessage(Strings.colour("%s<green>已切换! <gray>%s <dark_gray>= <white>%s"
                                .formatted(IP.PREFIX, fullPath, newVal)));
                        openConfigDetailGui(player, config, parentPath);
                    } else {
                        player.closeInventory();
                        player.sendMessage(Strings.colour("%s<gray>使用指令修改: <white>/ip settings set %s:%s <值>"
                                .formatted(IP.PREFIX, config.name().toLowerCase(), fullPath)));
                    }
                }));
            }
        }

        Item backItem = new Item(Material.ARROW, "<#F5A3A3><bold>返回")
                .lore("<gray>返回上级菜单");
        menu.item(49, backItem.click(event -> {
            if (parentPath.contains(".")) {
                String newParent = parentPath.substring(0, parentPath.lastIndexOf('.'));
                openConfigDetailGui(player, config, newParent);
            } else {
                openSettingsGui(player);
            }
        }));

        Item closeItem = new Item(Material.BARRIER, "<#D71F1F><bold>关闭").lore("<gray>关闭菜单");
        menu.item(53, closeItem.click(event -> player.closeInventory()));

        menu.open(player);
    }

    private void saveAndReload(Config config, FileConfiguration fc) {
        try {
            fc.save(config.path);
            config.load();
        } catch (IOException e) {
            IP.logging().error("Failed to save config: " + e.getMessage());
        }
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(Strings.colour(message));
    }
}
