package net.kaoruxun.ncepucore.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ScoreBoard {

    private final JavaPlugin plugin;
    private final Map<UUID, Scoreboard> playerScoreboards = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> scoreboardToggles = new ConcurrentHashMap<>();

    private BoardType currentBoard = BoardType.LEVEL_RANK;
    private BukkitRunnable updateTask;

    private final int updateInterval = 10;

    public ScoreBoard(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        initAllPlayers();
        startUpdateTask();
    }

    public void stop() {
        stopUpdateTask();
        clearAllScoreboards();
    }

    public void createForJoinPlayer(Player player) {
        if (isEnabled(player)) {
            createScoreboard(player);
        }
    }

    public void toggleForPlayer(Player player) {
        // 切换状态
        boolean enabled = !isEnabled(player);
        scoreboardToggles.put(player.getUniqueId(), enabled);

        if (enabled) {
            createScoreboard(player);
            player.sendMessage(ChatColor.GREEN + "排行榜已开启!");
        } else {
            removeScoreboard(player);
            player.sendMessage(ChatColor.RED + "排行榜已关闭!");
        }
    }

    public boolean isEnabled(Player player) {
        return scoreboardToggles.getOrDefault(player.getUniqueId(), true);
    }

    private void initAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isEnabled(player)) {
                createScoreboard(player);
            }
        }
    }

    private void startUpdateTask() {
        if (updateTask != null) return;

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                switchBoard();
                updateAll();
            }
        };

        long ticks = updateInterval * 20L;
        updateTask.runTaskTimer(plugin, ticks, ticks);
    }

    private void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    private void switchBoard() {
        BoardType[] values = BoardType.values();
        currentBoard = values[(currentBoard.ordinal() + 1) % values.length];
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isEnabled(player) && playerScoreboards.containsKey(player.getUniqueId())) {
                updateScoreboard(player);
            }
        }
    }

    private void createScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        playerScoreboards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);
        updateScoreboard(player);
    }

    private void updateScoreboard(Player player) {
        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) return;

        Objective objective = getOrCreateObjective(scoreboard);
        objective.setDisplayName(currentBoard.getTitleName());

        clearEntries(scoreboard);
        displayRankings(objective);
    }

    private Objective getOrCreateObjective(Scoreboard scoreboard) {
        Objective objective = scoreboard.getObjective("scoreboard");
        if (objective == null) {
            objective = scoreboard.registerNewObjective("scoreboard", "dummy", "");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        return objective;
    }

    private void clearEntries(Scoreboard scoreboard) {
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
    }

    private void displayRankings(Objective objective) {
        List<PlayerScore> rankings = getRankings(currentBoard);
        int displayCount = Math.min(rankings.size(), 10);

        for (int i = 0; i < displayCount; i++) {
            PlayerScore score = rankings.get(i);
            String entry = formatEntry(i + 1, score);
            objective.getScore(entry).setScore(score.getScore());
        }
    }

    private String formatEntry(int rank, PlayerScore playerScore) {
        return String.format("§6§l#%d §2%s",
                rank, playerScore.getPlayerName());
    }

    private List<PlayerScore> getRankings(BoardType type) {
        List<PlayerScore> rankings = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            int score = getPlayerStatistic(player, type);
            rankings.add(new PlayerScore(player.getName(), score));
        }

        // 倒序排序
        rankings.sort((a, b) -> b.getScore() - a.getScore());
        return rankings;
    }

    private int getPlayerStatistic(Player player, BoardType type) {
        return switch (type) {
            case LEVEL_RANK -> player.getLevel();
            case FISHING_RANK -> player.getStatistic(Statistic.FISH_CAUGHT);
            case MINING_RANK -> calculateMiningStatistic(player);
            case KILLING_RANK -> calculateKillingEntities(player);
            case DEATH_RANK -> player.getStatistic(Statistic.DEATHS);
        };
    }

    private int calculateMiningStatistic(Player player) {
        int total = 0;
        for (Material material : Material.values()) {
            if (material.isBlock()) {
                try {
                    total += player.getStatistic(Statistic.MINE_BLOCK, material);
                } catch (Exception ignored) {}
            }
        }
        return total;
    }

    private int calculateKillingEntities(Player player) {
        int mobKills = 0;
        int playerKills = 0;
        try {
            mobKills = player.getStatistic(Statistic.MOB_KILLS);
            playerKills = player.getStatistic(Statistic.PLAYER_KILLS);
        } catch (Exception ignored) {}
        return mobKills + playerKills;
    }

    private void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        playerScoreboards.remove(player.getUniqueId());
    }

    private void clearAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeScoreboard(player);
        }
        playerScoreboards.clear();
    }
}