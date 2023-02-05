package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.Pair;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("tpaccept")
public final class TpAcceptCommand extends BasicCommand {
    public TpAcceptCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return false;
        final Pair<Long, Runnable> p = instance.playerTasks.get(sender);
        if (p != null) {
            instance.playerTasks.remove(sender);
            if (p.left > System.currentTimeMillis()) {
                p.right.run();
                return true;
            }
        }
        sender.sendMessage("§c当前没有待处理的传送请求!");
        return true;
    }
}
