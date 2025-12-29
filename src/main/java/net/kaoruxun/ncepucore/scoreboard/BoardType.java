package net.kaoruxun.ncepucore.scoreboard;

/**
 * @author RukiaOvO
 * @date 2025/12/29
 * @description BoardType Enum
 */

public enum BoardType {
    LEVEL_RANK("§a§l等级排行榜"),
    FISHING_RANK("§b§l钓鱼排行榜"),
    MINING_RANK("§7§l挖掘排行榜"),
    KILLING_RANK("§4§l杀生排行榜");

    private final String titleName;

    BoardType(String t) {
        this.titleName = t;
    }

    public String getTitleName() {
        return titleName;
    }
}
