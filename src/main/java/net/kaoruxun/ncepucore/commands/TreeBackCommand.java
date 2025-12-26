package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("treeback")
public final class TreeBackCommand extends BasicCommand {
    public TreeBackCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return false;

        boolean ok = instance.rollbackLastTreeChop(player);
        if (ok) {
            player.sendMessage("§a已回溯你最近一次砍树连锁操作(不会自动回收掉落物)");
        } else {
            player.sendMessage("§c没有可回溯的砍树记录(可能已过期或你最近没有触发连锁砍树)");
        }
        return true;
    }
}


