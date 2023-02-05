package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.DatabaseSingleton;
import net.kaoruxun.ncepucore.utils.Serializer;
import net.kaoruxun.ncepucore.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandName("warp")
public final class WarpCommand extends BasicCommand {
    public WarpCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length < 1) return false;
        final byte[] bytes = DatabaseSingleton.INSTANCE.get("warp." + args[0]);
        if (bytes == null) sender.sendMessage("§c该地标不存在!");
        else {
            Utils.teleportPlayer((Player) sender, Serializer.deserializeLocation(bytes));
            sender.sendMessage("§a传送成功!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length < 1 ? new ArrayList<>(instance.warps) : null;
    }
}
