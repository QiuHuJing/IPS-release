package dev.efnilite.vilib.fastboard;

import org.bukkit.entity.Player;

/**
 * Folia 1.21.x 兼容的空操作 FastBoard 替代。
 * 记分板功能在 Folia 1.21.x 上暂不可用，但不影响跑酷核心功能。
 */
public class FastBoard extends FastBoardBase {

    public FastBoard(Player player) {
        super(player);
    }
}
