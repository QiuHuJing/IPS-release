package dev.efnilite.ip.player.data;

import dev.efnilite.ip.IP;
import dev.efnilite.ip.config.Config;
import dev.efnilite.ip.config.Option;
import dev.efnilite.ip.reward.Reward;
import io.papermc.lib.PaperLib;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class for storing previous data of players
 */
public class PreviousData {

    private final InventoryData inventoryData;

    private final int hunger;
    private final boolean flying;
    private final boolean allowFlight;
    private final GameMode gamemode;
    private final Location location;
    private final Collection<PotionEffect> effects;

    /**
     * List of all {@link Reward} to execute on leave.
     */
    public List<Reward> onLeave = new ArrayList<>();

    public PreviousData(@NotNull Player player) {
        gamemode = player.getGameMode();
        location = player.getLocation();
        hunger = player.getFoodLevel();
        allowFlight = player.getAllowFlight();
        flying = player.isFlying();
        effects = player.getActivePotionEffects();

        for (PotionEffect effect : effects) {
            player.removePotionEffect(effect.getType());
        }

        if (Config.CONFIG.getBoolean("options.inventory-handling")) {
            inventoryData = new InventoryData(player);
            inventoryData.save(Config.CONFIG.getBoolean("options.inventory-saving"));
        } else {
            inventoryData = null;
        }
    }

    public void apply(Player player, boolean urgent) {
        var to = Config.CONFIG.getBoolean("bungeecord.go-back-enabled") ? Option.GO_BACK_LOC : location;

        if (!urgent) {
            // 正常退出：先异步传送回原位，再恢复数据
            PaperLib.teleportAsync(player, to).thenRun(() -> apply(player));
        } else {
            // 紧急退出（玩家断线/踢出）：直接恢复数据，跳过传送
            // Folia 不支持同步传送，且玩家已离线时异步传送无法完成回调
            apply(player);
        }
    }

    private void apply(Player player) {
        try {
            player.setFoodLevel(hunger);
            player.setGameMode(gamemode);
            player.setAllowFlight(allowFlight);
            player.setFlying(flying);

            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            for (PotionEffect effect : effects) {
                player.addPotionEffect(effect);
            }

            player.resetPlayerTime();
            player.resetPlayerWeather();
            player.setVelocity(new Vector(0, 0, 0));
            player.setFallDistance(0f);
        } catch (Exception ex) { // not the best way to do this... too bad!
            IP.logging().stack("Error while recovering stats of %s".formatted(player.getName()), ex);
        }

        if (inventoryData != null) {
            inventoryData.apply();
        }
    }
}