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

public final class ScoreBoardService {

    private final JavaPlugin plugin;
    private final Map<UUID, Boolean> playerScoreBoardToggles = new ConcurrentHashMap<>();
    private final Map<UUID, Scoreboard> playerScoreBoards = new ConcurrentHashMap<>();
    private BoardType currentBoard = BoardType.LEVEL_RANK; //默认显示等级排行
    // Paper/Bukkit 调度器周期单位是 tick 20 tick = 1 秒
    private int timeIntervalSeconds = 10; //排行榜更新以及切换间隔10s

    private BukkitRunnable updateTask;
    private BukkitRunnable switchTask;

    public ScoreBoardService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startTasks() {
        long periodTicks = Math.max(1L, timeIntervalSeconds * 20L);

        // 使用单一任务 每隔 N 秒切换一次榜单并刷新 避免重复刷新
        switchTask = new BukkitRunnable() {
            @Override
            public void run() {
                switchToNextBoard();
                updateAllScoreboards();
            }
        };
        // 启动时先刷新一次 之后按间隔循环
        updateAllScoreboards();
        switchTask.runTaskTimer(this.plugin, periodTicks, periodTicks);
    }

    public void stopTasks() {
        if (updateTask != null) updateTask.cancel();
        if (switchTask != null) switchTask.cancel();

        // 清理所有计分板
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (playerScoreBoards.containsKey(player.getUniqueId())) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
    }

    private void switchToNextBoard() {
        // 切换到下一个排行榜类型
        BoardType[] values = BoardType.values();
        int nextOrdinal = (currentBoard.ordinal() + 1) % values.length;
        currentBoard = values[nextOrdinal];
    }

    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isScoreBoardEnabled(player)) {
                updateScoreboard(player);
            }
        }
    }

    private void updateScoreboard(Player player) {
        Scoreboard scoreboard = getPlayerScoreBoard(player);
        Objective objective = scoreboard.getObjective("scoreboard");

        if (objective == null) {
            objective = scoreboard.registerNewObjective("scoreboard", "dummy", currentBoard.getTitleName());
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            objective.setDisplayName(currentBoard.getTitleName());
        }

        for (String playerScore : scoreboard.getEntries()) {
            scoreboard.resetScores(playerScore);
        }

        List<PlayerScore> playerScores = getScoreBoardData(currentBoard);
        int maxEntries = Math.min(playerScores.size(), 10); //最多显示10行数据
        for (int i = 0; i < maxEntries; i++) {
            PlayerScore playerScore = playerScores.get(i);
            String displayText = formatDisplay(playerScore, i + 1);
            Score score = objective.getScore(displayText);
            score.setScore(maxEntries - i);
        }
    }

    private List<PlayerScore> getScoreBoardData(BoardType type) {
        List<PlayerScore> entries = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerName = player.getName();
            int score = switch (type) {
                case LEVEL_RANK -> player.getLevel();
                case FISHING_RANK -> player.getStatistic(Statistic.FISH_CAUGHT);
                case MINING_RANK -> calculateMiningBlocks(player);
                case KILLING_RANK -> calculateKillingEntities(player);
            };

            entries.add(new PlayerScore(playerName, score));
        }

        //降序排序
        entries.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return entries;
    }

    private int calculateMiningBlocks(Player player) {
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
        // Bukkit/Paper 的 KILL_ENTITY 统计需要 EntityType 维度 不能传 Material
        // 这里用更稳定且开销更小的聚合统计 击杀生物+击杀玩家
        int mobKills = 0;
        int playerKills = 0;
        try {
            mobKills = player.getStatistic(Statistic.MOB_KILLS);
        } catch (Exception ignored) {}
        try {
            playerKills = player.getStatistic(Statistic.PLAYER_KILLS);
        } catch (Exception ignored) {}
        return mobKills + playerKills;
    }

    private String formatDisplay(PlayerScore playerScore, int rank) {
        String rankColor;
        if (rank == 1) {
            rankColor = ChatColor.GOLD.toString();
        } else if (rank == 2) {
            rankColor = ChatColor.GRAY.toString();
        } else if (rank == 3) {
            rankColor = ChatColor.YELLOW.toString();
        } else {
            rankColor = ChatColor.WHITE.toString();
        }

        return String.format("%s#%d %s§f: %d",
                rankColor, rank, playerScore.getPlayerName(), playerScore.getScore());
    }

    private Scoreboard getPlayerScoreBoard(Player player) {
        return playerScoreBoards.computeIfAbsent(player.getUniqueId(),
                id -> Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void toggleScoreBoard(Player player) {
        UUID uuid = player.getUniqueId();
        boolean enabled = !playerScoreBoardToggles.getOrDefault(uuid, true);
        playerScoreBoardToggles.put(uuid, enabled);

        //切换计分板状态
        if (enabled) {
            Scoreboard scoreboard = getPlayerScoreBoard(player);
            player.setScoreboard(scoreboard);
            updateScoreboard(player);
            player.sendMessage(ChatColor.GREEN + "排行榜已开启!");
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            playerScoreBoards.remove(uuid);
            player.sendMessage(ChatColor.RED + "排行榜已关闭!");
        }
    }

    public boolean isScoreBoardEnabled(Player player) {
        return playerScoreBoardToggles.getOrDefault(player.getUniqueId(), true);
    }
}