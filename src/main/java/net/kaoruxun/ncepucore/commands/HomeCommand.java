package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.*;
import net.kaoruxun.ncepucore.utils.DatabaseSingleton;
import net.kaoruxun.ncepucore.utils.Serializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("home")
public final class HomeCommand extends TeleportCommand {
    public HomeCommand(Main main) {
        super(main);
    }

    @Override
    public void doAction(CommandSender sender, Player p, boolean now) {
        final byte[] bytes = DatabaseSingleton.INSTANCE.getPlayerData(p, "home");
        if (bytes == null) sender.sendMessage("§c你还没有设置家!");
        else instance.delayTeleport(p, Serializer.deserializeLocation(bytes), now);
    }
}
