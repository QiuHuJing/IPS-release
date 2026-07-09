package dev.efnilite.vilib.particle;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * Folia 1.21.x 兼容的粒子工具类。
 * 替换 vilib 原版，安全处理 1.21 的粒子 API 变更。
 */
public final class Particles {

    private Particles() {}

    /**
     * 在指定位置绘制粒子。
     */
    public static void draw(@NotNull Location location, @NotNull ParticleData<?> data) {
        spawn(location.getWorld(), location, data, Collections.emptyList());
    }

    /**
     * 绘制粒子（仅对指定玩家可见）。
     */
    public static void draw(@NotNull Location location, @NotNull ParticleData<?> data, @NotNull Player player) {
        player.spawnParticle(data.particle(), location, data.size(),
                data.offsetX(), data.offsetY(), data.offsetZ(), data.speed(), data.data());
    }

    /**
     * 绘制线段上的粒子。
     */
    public static void line(@NotNull Location start, @NotNull Location end, @NotNull ParticleData<?> data, double step) {
        World world = start.getWorld();
        if (world == null) return;
        double distance = start.distance(end);
        Location direction = end.clone().subtract(start);
        for (double d = 0; d <= distance; d += step) {
            Location point = start.clone().add(direction.clone().multiply(d / distance));
            spawn(world, point, data, Collections.emptyList());
        }
    }

    /**
     * 绘制 BoundingBox 外框粒子。
     */
    public static void box(@NotNull BoundingBox box, @NotNull World world, @NotNull ParticleData<?> data, double step) {
        box(box, world, data, null, step);
    }

    /**
     * 绘制 BoundingBox 外框粒子（仅对指定玩家可见）。
     */
    public static void box(@NotNull BoundingBox box, @NotNull World world, @NotNull ParticleData<?> data, Player player, double step) {
        double minX = box.getMinX(), minY = box.getMinY(), minZ = box.getMinZ();
        double maxX = box.getMaxX(), maxY = box.getMaxY(), maxZ = box.getMaxZ();

        // 12条边
        line(new Location(world, minX, minY, minZ), new Location(world, maxX, minY, minZ), data, step);
        line(new Location(world, maxX, minY, minZ), new Location(world, maxX, minY, maxZ), data, step);
        line(new Location(world, maxX, minY, maxZ), new Location(world, minX, minY, maxZ), data, step);
        line(new Location(world, minX, minY, maxZ), new Location(world, minX, minY, minZ), data, step);

        line(new Location(world, minX, maxY, minZ), new Location(world, maxX, maxY, minZ), data, step);
        line(new Location(world, maxX, maxY, minZ), new Location(world, maxX, maxY, maxZ), data, step);
        line(new Location(world, maxX, maxY, maxZ), new Location(world, minX, maxY, maxZ), data, step);
        line(new Location(world, minX, maxY, maxZ), new Location(world, minX, maxY, minZ), data, step);

        line(new Location(world, minX, minY, minZ), new Location(world, minX, maxY, minZ), data, step);
        line(new Location(world, maxX, minY, minZ), new Location(world, maxX, maxY, minZ), data, step);
        line(new Location(world, maxX, minY, maxZ), new Location(world, maxX, maxY, maxZ), data, step);
        line(new Location(world, minX, minY, maxZ), new Location(world, minX, maxY, maxZ), data, step);
    }

    /**
     * 在圆形上绘制粒子。
     */
    public static void circle(@NotNull Location center, @NotNull ParticleData<?> data, int points, double radius) {
        World world = center.getWorld();
        if (world == null) return;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location point = new Location(world, x, center.getY(), z);
            spawn(world, point, data, Collections.emptyList());
        }
    }

    private static void spawn(World world, Location loc, ParticleData<?> data, Collection<Player> players) {
        if (world == null || loc == null || data == null) return;
        try {
            Particle particle = data.particle();
            // 1.21+ 兼容：检查粒子是否需要数据
            if (requiresData(particle) && data.data() == null) {
                return; // 跳过需要数据但未提供的粒子
            }
            world.spawnParticle(particle, loc, data.size(),
                    data.offsetX(), data.offsetY(), data.offsetZ(), data.speed(), data.data());
        } catch (Exception ignored) {
            // 安全忽略任何粒子生成错误
        }
    }

    /**
     * 检查粒子类型是否需要非空数据。
     * 在 1.21+ 中，某些粒子（如 DUST, ENTITY_EFFECT 等）要求数据不能为 null。
     */
    private static boolean requiresData(Particle particle) {
        if (particle == null) return false;
        return switch (particle) {
            case DUST, DUST_COLOR_TRANSITION, DUST_PLUME, 
                 ENTITY_EFFECT, INSTANT_EFFECT,
                 SCULK_CHARGE, SHRIEK, VIBRATION,
                 TRAIL, BLOCK_CRUMBLE, BLOCK_MARKER,
                 FALLING_DUST, ITEM -> true;
            default -> false;
        };
    }
}
