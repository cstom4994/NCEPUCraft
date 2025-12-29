package net.kaoruxun.ncepucore.scoreboard;

/**
 * @author RukiaOvO
 * @date 2025/12/29
 * @description PlayerScore class
 */

public class PlayerScore {
    private final String playerName;
    private final int score;

    public PlayerScore(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }
}
