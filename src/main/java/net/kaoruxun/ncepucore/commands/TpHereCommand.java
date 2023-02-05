package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("tphere")
public final class TpHereCommand extends BasicCommand {
    public TpHereCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length != 1) return false;
        final Player sp = (Player) sender;
        final Player p = instance.getPlayer(sender, args[0]);
        if (p == null) return true;
        instance.requestTeleport(p, "  §d玩家 §f" + sp.getName() + " §d希望你传送到他那里:",
                () -> instance.delayTeleport(p, sp.getLocation()));
        sender.sendMessage("§a成功向玩家发送传送请求!");
        return true;
    }
}
