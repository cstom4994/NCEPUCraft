package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("tpaall")
public final class TpaAllCommand extends BasicCommand {
    public TpaAllCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return false;
        final Player sp = (Player) sender;
        final String str = "  §d玩家 §f" + sp.getName() + " §d希望你传送到他那里:";
        instance.getServer().getOnlinePlayers().forEach(it -> instance.requestTeleport(it, str,
                () -> instance.delayTeleport(it, sp.getLocation())));
        sender.sendMessage("§a成功向玩家发送传送请求!");
        return true;
    }
}
