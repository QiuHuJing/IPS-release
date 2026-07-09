package dev.efnilite.ip.hook;

import dev.efnilite.ip.IP;
import dev.efnilite.ip.api.Registry;
import dev.efnilite.ip.generator.ParkourGenerator;
import dev.efnilite.ip.leaderboard.Leaderboard;
import dev.efnilite.ip.leaderboard.Score;
import dev.efnilite.ip.mode.Mode;
import dev.efnilite.ip.mode.Modes;
import dev.efnilite.ip.player.ParkourPlayer;
import dev.efnilite.ip.player.ParkourSpectator;
import dev.efnilite.ip.player.ParkourUser;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PlaceholderAPI 扩展 — 提供丰富的跑酷变量。
 * 变量前缀: %witp_<变量名>%
 */
public class PAPIHook extends PlaceholderExpansion {

    // 匹配: <mode>_rank_<n>_<type>  例如: default_rank_1_name
    // 匹配: <mode>_player_<type>    例如: default_player_score
    private static final Pattern MODE_RANK = Pattern.compile("^(\\w+)_rank_(\\d+)_(\\w+)$");
    private static final Pattern MODE_PLAYER = Pattern.compile("^(\\w+)_player_(\\w+)$");

    @Override
    public @NotNull String getIdentifier() {
        return "witp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Efnilite & 叶小光";
    }

    @Override
    public boolean canRegister() { return true; }

    @Override
    public boolean persist() { return true; }

    @Override
    public @NotNull String getVersion() {
        return IP.getPlugin().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        // ===== 无玩家变量 =====
        String noPlayerResult = handleNoPlayerPlaceholder(params);
        if (noPlayerResult != null) return noPlayerResult;

        // ===== 模式特定排行榜变量 =====
        String modeRankResult = handleModeRankPlaceholder(params);
        if (modeRankResult != null) return modeRankResult;

        // ===== 模式特定玩家变量 =====
        String modePlayerResult = handleModePlayerPlaceholder(params, player);
        if (modePlayerResult != null) return modePlayerResult;

        // ===== 需要玩家的变量 =====
        if (player == null) return null;

        // 玩家通用变量
        String playerResult = handlePlayerPlaceholder(player, params);
        if (playerResult != null) return playerResult;

        // 玩家跑酷变量
        ParkourUser user = ParkourUser.getUser(player);
        ParkourPlayer pp = null;
        if (user instanceof ParkourPlayer p) pp = p;
        else if (user instanceof ParkourSpectator s) pp = s.closest;

        if (pp != null && pp.session != null && pp.session.generator != null) {
            return handlePlayingPlaceholder(pp, params);
        }

        return null;
    }

    // ==================== 无玩家变量 ====================

    private String handleNoPlayerPlaceholder(String params) {
        return switch (params) {
            case "version", "ver" -> IP.getPlugin().getDescription().getVersion();
            case "record_player", "top_player" -> {
                Score s = Modes.DEFAULT.getLeaderboard().getScoreAtRank(1);
                yield s != null ? s.name() : "无";
            }
            case "record_score", "top_score" -> {
                Score s = Modes.DEFAULT.getLeaderboard().getScoreAtRank(1);
                yield s != null ? Integer.toString(s.score()) : "0";
            }
            case "record_time", "top_time" -> {
                Score s = Modes.DEFAULT.getLeaderboard().getScoreAtRank(1);
                yield s != null ? s.time() : "--:--";
            }
            case "playing", "players_online" ->
                Integer.toString(ParkourPlayer.getPlayers().size());
            case "total_players" ->
                Integer.toString(Modes.DEFAULT.getLeaderboard().scores.size());
            default -> null;
        };
    }

    // ==================== 模式特定排行榜 ====================
    // 格式: <mode>_rank_<n>_<type>  如 default_rank_1_name

    private String handleModeRankPlaceholder(String params) {
        Matcher m = MODE_RANK.matcher(params);
        if (!m.matches()) return null;

        String modeName = m.group(1);
        int rank = Integer.parseInt(m.group(2));
        String type = m.group(3);

        Mode mode = Registry.getMode(modeName);
        Leaderboard lb = mode != null ? mode.getLeaderboard() : Modes.DEFAULT.getLeaderboard();
        if (lb == null) return "?";

        Score score = lb.getScoreAtRank(rank);
        if (score == null) return "?";

        return switch (type) {
            case "name", "player" -> score.name();
            case "score" -> Integer.toString(score.score());
            case "time" -> score.time();
            case "difficulty" -> score.difficulty();
            case "difficulty_string" -> parseDifficultyStr(score.difficulty());
            default -> "?";
        };
    }

    // ==================== 模式特定玩家变量 ====================
    // 格式: <mode>_player_<type>  如 default_player_score

    private String handleModePlayerPlaceholder(String params, Player player) {
        Matcher m = MODE_PLAYER.matcher(params);
        if (!m.matches()) return null;
        if (player == null) return "?";

        String modeName = m.group(1);
        String type = m.group(2);

        Mode mode = Registry.getMode(modeName);
        Leaderboard lb = mode != null ? mode.getLeaderboard() : Modes.DEFAULT.getLeaderboard();
        if (lb == null) return "?";

        Score score = lb.get(player.getUniqueId());
        if (score == null) return "?";

        return switch (type) {
            case "rank" -> Integer.toString(lb.getRank(player.getUniqueId()));
            case "score" -> Integer.toString(score.score());
            case "time" -> score.time();
            case "difficulty" -> score.difficulty();
            case "difficulty_string" -> parseDifficultyStr(score.difficulty());
            default -> "?";
        };
    }

    // ==================== 玩家通用变量 ====================

    private String handlePlayerPlaceholder(Player player, String params) {
        ParkourUser user = ParkourUser.getUser(player);
        return switch (params) {
            case "rank" ->
                Integer.toString(Modes.DEFAULT.getLeaderboard().getRank(player.getUniqueId()));
            case "highscore", "high_score" ->
                Integer.toString(Modes.DEFAULT.getLeaderboard().get(player.getUniqueId()).score());
            case "high_score_time" ->
                Modes.DEFAULT.getLeaderboard().get(player.getUniqueId()).time();
            case "is_playing" ->
                user != null ? "true" : "false";
            case "locale" -> {
                if (user != null) yield user.locale;
                yield "zh_cn";
            }
            default -> null;
        };
    }

    // ==================== 游戏中玩家变量 ====================

    private String handlePlayingPlaceholder(ParkourPlayer pp, String params) {
        ParkourGenerator gen = pp.session.generator;
        return switch (params) {
            case "score", "current_score" -> Integer.toString(gen.score);
            case "total_score" -> Integer.toString(gen.totalScore);
            case "time", "current_time" -> gen.getFormattedTime();
            case "blocklead", "lead" -> Integer.toString(pp.blockLead);
            case "style" -> pp.style;
            case "time_pref", "time_preference" -> Integer.toString(pp.selectedTime);
            case "scoreboard_enabled" -> pp.showScoreboard.toString();
            case "difficulty" -> Double.toString(pp.schematicDifficulty);
            case "difficulty_string" -> parseDifficulty(pp.schematicDifficulty);
            case "particles_enabled" -> pp.particles.toString();
            case "sound_enabled" -> pp.sound.toString();
            case "special_enabled" -> pp.useSpecialBlocks.toString();
            case "fall_message_enabled" -> pp.showFallMessage.toString();
            case "session_players" -> Integer.toString(pp.session.getPlayers().size());
            case "session_spectators" -> Integer.toString(pp.session.getSpectators().size());
            default -> {
                // score_until_X: 距离下一个间隔分数的剩余分数
                if (params.startsWith("score_until_")) {
                    String val = params.replace("score_until_", "");
                    try {
                        int interval = Integer.parseInt(val);
                        if (interval > 0) {
                            yield Integer.toString(interval - (gen.totalScore % interval));
                        }
                    } catch (NumberFormatException ignored) {}
                    yield "0";
                }
                yield null;
            }
        };
    }

    // ==================== 工具 ====================

    private String parseDifficulty(double d) {
        if (d <= 0.25) return "简单";
        else if (d <= 0.5) return "中等";
        else if (d <= 0.75) return "困难";
        else if (d <= 1.0) return "地狱";
        return "?";
    }

    private String parseDifficultyStr(String s) {
        try {
            if (s.contains("?")) return "?";
            return parseDifficulty(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return "?";
        }
    }
}
