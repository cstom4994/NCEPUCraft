package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("tpcancel")
public final class CancelCommand extends BasicCommand {
    public CancelCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return false;
        sender.sendMessage(instance.cancelTeleport((Player) sender) ? "§e已取消传送!" : "§c当前没有待处理的传送请求!");
        return true;
    }
}
