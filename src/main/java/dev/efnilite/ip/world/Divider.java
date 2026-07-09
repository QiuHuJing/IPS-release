package dev.efnilite.ip.world;

import dev.efnilite.ip.IP;
import dev.efnilite.ip.config.Option;
import dev.efnilite.ip.session.Session;
import dev.efnilite.vilib.util.Locations;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * <p>将跑酷世界划分为多个区域，每个区域对应一个活跃的会话。</p>
 * <p>Divides the parkour world in sections, each with an active session.</p>
 * <p>Folia 适配：使用 ConcurrentHashMap 保证线程安全。</p>
 * <p>Iteration 2.1 - Folia compatible.</p>
 *
 * @author Efnilite
 * @since 5.0.0
 */
public class Divider {

    /**
     * 所有会话与其区域编号的映射（线程安全）。
     * Map with all session ids mapped to the session instances (thread-safe).
     */
    public static final Map<Session, Integer> sections = new ConcurrentHashMap<>();

    /**
     * 将指定会话关联到一个区域。
     * Associates a session to a specific section.
     *
     * @param session 会话实例
     */
    public static Location add(Session session) {
        // 尝试获取离中心最近的可用区域
        // attempts to get the closest available section to the center
        var missing = IntStream.range(0, sections.size() + 1)
                .filter(i -> !sections.containsValue(i))
                .findFirst()
                .orElseThrow();

        sections.put(session, missing);

        var location = toLocation(session);

        IP.log("Added session at %s".formatted(Locations.toString(location, true)));

        return location;
    }

    /**
     * 将会话从区域中移除。
     * Disassociates a session from a specific section.
     *
     * @param session 会话实例
     */
    public static void remove(Session session) {
        IP.log("Removed session at %s".formatted(Locations.toString(toLocation(session), true)));

        sections.remove(session);
    }

    /**
     * @param session 会话实例
     * @return 区域 n 中心位置的坐标
     */
    private static Location toLocation(Session session) {
        int[] xz = spiralAt(sections.get(session));

        return new Location(World.getWorld(),
                xz[0] * Option.BORDER_SIZE,
                (Option.MAX_Y + Option.MIN_Y) / 2.0,
                xz[1] * Option.BORDER_SIZE);
    }

    /**
     * @param session 会话实例
     * @return 长度为2的数组，第一个元素为最小坐标，第二个为最大坐标
     */
    public static Location[] toSelection(Session session) {
        Location center = toLocation(session);

        // 获取最小和最大坐标
        Location max = center.clone().add(Option.BORDER_SIZE / 2, 0, Option.BORDER_SIZE / 2);
        Location min = center.clone().subtract(Option.BORDER_SIZE / 2, 0, Option.BORDER_SIZE / 2);

        max.setY(Option.MAX_Y);
        min.setY(Option.MIN_Y);

        return new Location[]{min, max};
    }

    /**
     * 计算螺旋坐标。
     * Gets a spiral coordinate.
     *
     * @param n 索引值
     * @return 该索引对应的坐标
     */
    // https://math.stackexchange.com/a/163101
    private static int[] spiralAt(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Invalid n bound: %d".formatted(n));
        }

        n++; // one-index
        int k = (int) Math.ceil((Math.sqrt(n) - 1) / 2);
        int t = 2 * k + 1;
        int m = t * t;
        t--;

        if (n > m - t) {
            return new int[]{k - (m - n), -k};
        } else {
            m -= t;
        }

        if (n > m - t) {
            return new int[]{-k, -k + (m - n)};
        } else {
            m -= t;
        }

        if (n > m - t) {
            return new int[]{-k + (m - n), k};
        } else {
            return new int[]{k, k - (m - n - t)};
        }
    }
}
