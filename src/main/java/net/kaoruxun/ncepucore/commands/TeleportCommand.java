package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class TeleportCommand extends TargetCommand {
    public TeleportCommand(Main main) { super(main); }

    public abstract void doAction(CommandSender sender, Player p, boolean now);
}
