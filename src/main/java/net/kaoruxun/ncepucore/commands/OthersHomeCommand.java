package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.DatabaseSingleton;
import net.kaoruxun.ncepucore.utils.Serializer;
import net.kaoruxun.ncepucore.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("othershome")
public final class OthersHomeCommand extends BasicCommand {
    public OthersHomeCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length != 1) return false;
        final Player p = instance.getPlayer(sender, args[0]);
        if (p == null) return true;
        final byte[] bytes = DatabaseSingleton.INSTANCE.getPlayerData(p, "home");
        if (bytes == null) sender.sendMessage("§c该玩家还没有设置家!");
        else {
            Utils.teleportPlayer((Player) sender, Serializer.deserializeLocation(bytes));
            sender.sendMessage("§a传送成功!");
        }
        return true;
    }
}
