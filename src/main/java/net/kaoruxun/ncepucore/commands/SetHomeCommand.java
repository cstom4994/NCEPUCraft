package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.DatabaseSingleton;
import net.kaoruxun.ncepucore.utils.Serializer;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("sethome")
public final class SetHomeCommand extends BasicCommand {
    public SetHomeCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return false;
        final Player p = (Player) sender;
        if (p.getGameMode() == GameMode.SPECTATOR) {
            p.sendMessage("§c你不能在旁观模式设置家!");
        } else {
            DatabaseSingleton.INSTANCE.setPlayerData(p, "home", Serializer.serializeLocation(p.getLocation()));
            p.sendMessage("§a成功设置家!");
        }
        return true;
    }
}
