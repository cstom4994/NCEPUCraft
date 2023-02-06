package net.kaoruxun.ncepucore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class EndPlatform implements CommandExecutor, Listener {
    public boolean flag = false;
    @SuppressWarnings({"NullableProblems", "deprecation"})
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (flag) {
            flag = false;
            if (sender.isOp()) sender.sendMessage("§a当前服务器的末地黑曜石平台刷新已开启!");
            else Bukkit.broadcastMessage("§a当前服务器的末地黑曜石平台刷新已开启!");
        } else {
            flag = true;
            if (sender.isOp()) sender.sendMessage("§e当前服务器的末地黑曜石平台刷新已关闭!");
            else Bukkit.broadcastMessage("§e当前服务器的末地黑曜石平台刷新已关闭!");
        }
        return true;
    }
}
