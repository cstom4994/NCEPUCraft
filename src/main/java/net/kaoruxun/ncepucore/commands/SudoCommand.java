package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@CommandName("sudo")
public final class SudoCommand extends BasicCommand {
    public SudoCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length < 2) return false;
        final Player p = instance.getPlayer(sender, args[0]);
        if (p == null) return true;
        if (p.hasPermission("ncepu.sudo.avoid")) {
            sender.sendMessage("§c不能在目标玩家上执行当前指令!");
            return true;
        }
        sender.sendMessage(p.performCommand(String.join(" ", Arrays.copyOfRange(args, 1, args.length))) ? "§a执行成功!" : "§c执行失败!");
        return true;
    }
}
