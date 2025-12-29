package net.kaoruxun.ncepucore.scoreboard;

/**
 * @author RukiaOvO
 * @date 2025/12/29
 * @description BoardType Enum
 */

public enum BoardType {
    LEVEL_RANK("§a§l等级排行"),
    FISHING_RANK("§b§l钓鱼数"),
    MINING_RANK("§7§l挖掘数"),
    KILLING_RANK("§4§l击杀实体数"),
    DEATH_RANK("§0§l死亡次数");

    private final String titleName;

    BoardType(String t) {
        this.titleName = t;
    }

    public String getTitleName() {
        return titleName;
    }
}
