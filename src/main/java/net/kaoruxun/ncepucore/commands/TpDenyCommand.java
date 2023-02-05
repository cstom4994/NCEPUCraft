package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.Pair;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("tpdeny")
public final class TpDenyCommand extends BasicCommand {
    public TpDenyCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return false;
        final Pair<Long, Runnable> p = instance.playerTasks.get(sender);
        if (p != null) {
            instance.playerTasks.remove(sender);
            if (p.left > System.currentTimeMillis()) {
                sender.sendMessage("§e你拒绝了传送!");
                return true;
            }
        }
        sender.sendMessage("§c当前没有待处理的传送请求!");
        return true;
    }
}
