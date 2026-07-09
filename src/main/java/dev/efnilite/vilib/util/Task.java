package dev.efnilite.vilib.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Folia 兼容的 Task 调度器 — 替代 vilib 原始的 Bukkit.getScheduler() 实现。
 * 此文件在构建时会被 shade 插件重定向到 dev.efnilite.ip.lib.vilib.util.Task，
 * 替换掉 vilib 库中不兼容 Folia 的原始版本。
 *
 * Folia-compatible Task scheduler — replaces the original Bukkit.getScheduler() implementation.
 *
 * @author Efnilite, adapted for Folia by 叶小光
 */
@SuppressWarnings("deprecation")
public class Task {

    private int delay;
    private int repeat;
    private boolean async;
    private final Plugin plugin;
    private Runnable defaultRunnable;
    private BukkitRunnable bukkitRunnable;
    private ScheduledTask scheduledTask;

    public Task(Plugin plugin) {
        this.plugin = plugin;
        this.delay = 0;
        this.repeat = 0;
        this.async = false;
    }

    @NotNull
    public static Task create(@NotNull Plugin plugin) {
        return new Task(plugin);
    }

    @NotNull
    public Task execute(@NotNull Runnable runnable) {
        this.defaultRunnable = runnable;
        return this;
    }

    @NotNull
    public Task execute(@NotNull BukkitRunnable runnable) {
        this.bukkitRunnable = runnable;
        return this;
    }

    @NotNull
    public Task async() {
        this.async = true;
        return this;
    }

    @NotNull
    public Task delay(int ticks) {
        this.delay = ticks;
        return this;
    }

    @NotNull
    public Task repeat(int ticks) {
        this.repeat = ticks;
        return this;
    }

    @NotNull
    public Task cancel() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
        return this;
    }

    public void cancelAndRunImmediately() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
        if (bukkitRunnable != null) {
            bukkitRunnable.run();
        }
        if (defaultRunnable != null) {
            defaultRunnable.run();
        }
    }

    @NotNull
    public BukkitTask run() {
        Runnable targetRunnable;
        if (bukkitRunnable != null) {
            targetRunnable = bukkitRunnable;
        } else if (defaultRunnable != null) {
            targetRunnable = defaultRunnable;
        } else {
            throw new IllegalStateException("Both runnable types are null! 请在调用 run() 之前调用 execute()");
        }

        if (async) {
            runAsync(targetRunnable);
        } else {
            runSync(targetRunnable);
        }

        return new FoliaBukkitTaskAdapter(scheduledTask);
    }

    private void runSync(Runnable runnable) {
        var scheduler = Bukkit.getGlobalRegionScheduler();
        if (repeat > 0) {
            if (delay > 0) {
                scheduledTask = scheduler.runAtFixedRate(plugin, task -> runSafely(runnable), delay, repeat);
            } else {
                scheduledTask = scheduler.runAtFixedRate(plugin, task -> runSafely(runnable), 1, repeat);
            }
        } else if (delay > 0) {
            scheduledTask = scheduler.runDelayed(plugin, task -> runSafely(runnable), delay);
        } else {
            scheduledTask = scheduler.run(plugin, task -> runSafely(runnable));
        }
    }

    private void runAsync(Runnable runnable) {
        var scheduler = Bukkit.getAsyncScheduler();
        if (repeat > 0) {
            if (delay > 0) {
                scheduledTask = scheduler.runAtFixedRate(plugin, task -> runSafely(runnable),
                        delay * 50L, repeat * 50L, TimeUnit.MILLISECONDS);
            } else {
                scheduledTask = scheduler.runAtFixedRate(plugin, task -> runSafely(runnable),
                        50L, repeat * 50L, TimeUnit.MILLISECONDS);
            }
        } else if (delay > 0) {
            scheduledTask = scheduler.runDelayed(plugin, task -> runSafely(runnable),
                    delay * 50L, TimeUnit.MILLISECONDS);
        } else {
            scheduledTask = scheduler.runNow(plugin, task -> runSafely(runnable));
        }
    }

    private void runSafely(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            plugin.getSLF4JLogger().error("Task 执行异常", ex);
            ex.printStackTrace(); // 确保打印到控制台
        }
    }

    /**
     * 适配器：将 Folia 的 ScheduledTask 包装为 BukkitTask 接口，
     * 以保持与 vilib 内部代码的兼容性。
     */
    private static class FoliaBukkitTaskAdapter implements BukkitTask {

        private final ScheduledTask task;

        FoliaBukkitTaskAdapter(ScheduledTask task) {
            this.task = task;
        }

        @Override
        public int getTaskId() {
            return task.hashCode();
        }

        @Override
        public @NotNull Plugin getOwner() {
            // Folia's ScheduledTask doesn't expose owner — return a stub
            return Bukkit.getPluginManager().getPlugins()[0];
        }

        @Override
        public boolean isSync() {
            return true; // best effort
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }

        @Override
        public void cancel() {
            task.cancel();
        }
    }
}
