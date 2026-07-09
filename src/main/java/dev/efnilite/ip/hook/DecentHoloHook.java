package dev.efnilite.ip.hook;

import dev.efnilite.ip.IP;
import dev.efnilite.ip.api.Registry;
import dev.efnilite.ip.config.Config;
import dev.efnilite.ip.leaderboard.Leaderboard;
import dev.efnilite.ip.leaderboard.Score;
import dev.efnilite.ip.mode.Mode;
import dev.efnilite.ip.mode.Modes;
import dev.efnilite.ip.util.FoliaTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DecentHolograms 全息排行榜支持（纯反射，零编译依赖）。
 */
public class DecentHoloHook {

    private static final Map<String, Object> activeHolograms = new ConcurrentHashMap<>();
    private static Object dhAPI; // DHAPI static class

    public static void init() {
        if (!Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            return;
        }
        IP.logging().info("已注册 DecentHolograms 钩子（反射模式）");

        // 延迟初始化，等 DecentHolograms 完全加载
        FoliaTask.create(IP.getPlugin()).delay(40).execute(() -> {
            createHologramsByReflection();
            // 定期刷新（每 30 秒）
            FoliaTask.create(IP.getPlugin()).async().delay(600).repeat(600)
                    .execute(DecentHoloHook::refreshAll).run();
        }).run();
    }

    @SuppressWarnings("unchecked")
    private static void createHologramsByReflection() {
        if (!Config.CONFIG.fileConfiguration.getBoolean("holograms.decentholograms.enabled", false)) return;

        List<Map<?, ?>> rawList = Config.CONFIG.fileConfiguration.getMapList("holograms.decentholograms.leaderboards");
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

                List<String> textLines = buildLeaderboardLines(mode, lines);
                String holoId = "ip_lb_" + modeName + "_" + UUID.randomUUID().toString().substring(0, 8);

                // 反射调用 DHAPI.createHologram(id, location, lines)
                Object holo = createDHHologram(holoId, loc, textLines);
                if (holo != null) {
                    activeHolograms.put(holoId, holo);
                    IP.logging().info("DecentHolograms: 已创建排行榜全息 '" + modeName + "' 于 " + locStr);
                }
            } catch (Exception e) {
                IP.logging().stack("DecentHolograms: 创建全息失败", e);
            }
        }
    }

    private static Object createDHHologram(String id, Location loc, List<String> lines) {
        try {
            // DHAPI.createHologram(String id, Location location, List<String> lines)
            Class<?> dhapiClass = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            Method createMethod = dhapiClass.getMethod("createHologram", String.class, Location.class, List.class);
            return createMethod.invoke(null, id, loc, lines);
        } catch (Exception e) {
            IP.logging().warn("DecentHolograms API 调用失败: " + e.getMessage());
            return null;
        }
    }

    private static void refreshAll() {
        for (Map.Entry<String, Object> entry : new HashMap<>(activeHolograms).entrySet()) {
            try {
                refreshDHHologram(entry.getValue());
            } catch (Exception ignored) {}
        }
    }

    private static void refreshDHHologram(Object holo) {
        try {
            // DHAPI.setHologramLines(Hologram holo, List<String> lines)
            Class<?> dhapiClass = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            Class<?> holoClass = Class.forName("eu.decentsoftware.holograms.api.holograms.Hologram");
            Method sizeMethod = holoClass.getMethod("size");
            Method getLineMethod = holoClass.getMethod("getLine", int.class);
            Method setLineMethod = dhapiClass.getMethod("setHologramLine", holoClass, int.class, String.class);

            // 暂时不重建所有行以避免闪烁，仅作标记
        } catch (Exception ignored) {}
    }

    public static List<String> buildLeaderboardLines(Mode mode, int maxLines) {
        List<String> lines = new ArrayList<>();
        Leaderboard lb = mode.getLeaderboard();
        if (lb == null) {
            lines.add("§c§l暂无数据");
            return lines;
        }
        lines.add("§6§l无尽跑酷 §e排行榜");
        lines.add("§7§m--------------------");
        for (int i = 1; i <= maxLines; i++) {
            Score score = lb.getScoreAtRank(i);
            if (score == null) break;
            String prefix = i == 1 ? "§6① " : i == 2 ? "§7② " : i == 3 ? "§c③ " : "§8" + i + ". ";
            lines.add(prefix + "§f" + score.name() + " §7- §a" + score.score() + "分");
        }
        if (lb.getScoreAtRank(1) == null) lines.add("§7  虚位以待...");
        lines.add("§7§m--------------------");
        lines.add("§8更新: " + java.time.LocalTime.now().toString().substring(0, 8));
        return lines;
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
