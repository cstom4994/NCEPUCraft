package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Constants;
import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class TargetCommand extends BasicCommand {
    public TargetCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        switch (args.length) {
            case 0:
                if (!(sender instanceof Player)) return false;
                doAction(sender, (Player) sender, false);
                return true;
            case 1:
                if (!Utils.canTeleportOthers(sender)) return false;
                final Player p = instance.getServer().getPlayerExact(args[0]);
                if (p == null) sender.sendMessage(Constants.NO_SUCH_PLAYER);
                else doAction(sender, p, true);
                return true;
        }
        return true;
    }

    public abstract void doAction(CommandSender sender, Player p, boolean now);
}
