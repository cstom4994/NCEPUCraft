package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("tpa")
public final class TpaCommand extends BasicCommand {
    public TpaCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length != 1) return false;
        final Player sp = (Player) sender;
        final Player p = instance.getPlayer(sender, args[0]);
        if (p == null) return true;
        instance.requestTeleport(p, "  §d玩家 §f" + sp.getName() + " §d希望传送到你这里:",
                () -> instance.delayTeleport(sp, p.getLocation()));
        sender.sendMessage("§a成功向玩家发送传送请求!");
        return true;
    }
}
