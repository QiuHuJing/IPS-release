package dev.efnilite.vilib.fastboard;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Folia 1.21.x 兼容的空操作 FastBoard 替代。
 * 记分板在 1.21.x 上暂不可用（NMS 类名变更），此存根保证插件正常运行。
 */
public class FastBoardBase {

    protected final Player player;
    protected String title = "";
    protected List<String> lines = Collections.emptyList();
    private boolean deleted = false;

    public FastBoardBase(Player player) {
        this.player = player;
    }

    public String getTitle() { return title; }

    public void updateTitle(String title) { this.title = title; }

    public void updateLines(List<String> lines) { this.lines = lines; }

    public boolean isDeleted() { return deleted; }

    public void delete() { this.deleted = true; }

    // 内部分类枚举（vilib 其他代码可能引用）
    public enum ObjectiveMode { INTEGER, HEARTS }

    public enum TeamMode { CREATE, DESTROY }

    public enum VersionType { MODERN, LEGACY }

    /**
     * Scoreboard 操作动作 —— 1.21 兼容存根。
     * 原始版本通过 NMS 加载 ScoreboardServer$Action，在 1.21 已移除。
     */
    public enum ScoreboardAction {
        CHANGE_TITLE, UPDATE_LINES
    }
}
