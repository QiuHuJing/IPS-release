package dev.efnilite.ip.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Folia 兼容的任务调度工具类。
 * 替代 vilib 的 Task.create()，使用 Folia 的 GlobalRegionScheduler 和 AsyncScheduler。
 *
 * @author Efnilite, adapted for Folia
 * @since 5.3.2-Folia
 */
public class FoliaTask {

    private final Plugin plugin;
    private Runnable runnable;
    private long delay = 0;
    private long period = -1;
    private boolean async = false;
    private Consumer<ScheduledTask> cancelCallback;

    private FoliaTask(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 创建一个新的 Folia 兼容任务构建器。
     *
     * @param plugin 插件实例
     * @return FoliaTask 构建器
     */
    @NotNull
    public static FoliaTask create(@NotNull Plugin plugin) {
        return new FoliaTask(plugin);
    }

    /**
     * 设置任务为异步执行。
     */
    @NotNull
    public FoliaTask async() {
        this.async = true;
        return this;
    }

    /**
     * 设置延迟（tick）。
     */
    @NotNull
    public FoliaTask delay(long ticks) {
        this.delay = ticks;
        return this;
    }

    /**
     * 设置重复执行间隔（tick）。
     * -1 表示不重复。
     */
    @NotNull
    public FoliaTask repeat(long ticks) {
        this.period = ticks;
        return this;
    }

    /**
     * 设置要执行的任务。
     */
    @NotNull
    public FoliaTask execute(@NotNull Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    /**
     * 设置任务取消时的回调。
     */
    @NotNull
    public FoliaTask onCancel(@NotNull Consumer<ScheduledTask> callback) {
        this.cancelCallback = callback;
        return this;
    }

    /**
     * 运行任务并返回 ScheduledTask 以便后续取消。
     *
     * @return ScheduledTask 实例
     */
    @NotNull
    public ScheduledTask run() {
        if (runnable == null) {
            throw new IllegalStateException("在调用 run() 之前必须调用 execute() 设置任务体！");
        }

        if (async) {
            return runAsync();
        } else {
            return runSync();
        }
    }

    @NotNull
    private ScheduledTask runSync() {
        var scheduler = Bukkit.getGlobalRegionScheduler();
        if (period > 0) {
            if (delay > 0) {
                return scheduler.runAtFixedRate(plugin, task -> executeSafely(task), delay, period);
            } else {
                return scheduler.runAtFixedRate(plugin, task -> executeSafely(task), 1, period);
            }
        } else if (delay > 0) {
            return scheduler.runDelayed(plugin, task -> executeSafely(task), delay);
        } else {
            return scheduler.run(plugin, task -> executeSafely(task));
        }
    }

    @NotNull
    private ScheduledTask runAsync() {
        var scheduler = Bukkit.getAsyncScheduler();
        if (period > 0) {
            if (delay > 0) {
                return scheduler.runAtFixedRate(plugin, task -> executeSafely(task), delay * 50, period * 50, TimeUnit.MILLISECONDS);
            } else {
                return scheduler.runAtFixedRate(plugin, task -> executeSafely(task), 50, period * 50, TimeUnit.MILLISECONDS);
            }
        } else if (delay > 0) {
            return scheduler.runDelayed(plugin, task -> executeSafely(task), delay * 50, TimeUnit.MILLISECONDS);
        } else {
            return scheduler.runNow(plugin, task -> executeSafely(task));
        }
    }

    private void executeSafely(ScheduledTask task) {
        try {
            runnable.run();
        } catch (Exception ex) {
            plugin.getSLF4JLogger().error("FoliaTask 执行异常", ex);
            ex.printStackTrace();
        }
    }
}
