package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author RukiaOvO
 * @date 2025/12/29
 * @description ScoreBoard Command class
 */

@CommandName("scoreboard")
public final class ScoreBoardCommand extends BasicCommand{
    public ScoreBoardCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return false;
        instance.getScoreBoardService().toggleForPlayer(player);

        return true;
    }
}
