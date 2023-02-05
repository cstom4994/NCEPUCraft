package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.DatabaseSingleton;
import net.kaoruxun.ncepucore.utils.Serializer;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("toggle")
public final class ToggleCommand extends BasicCommand {
    public ToggleCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return false;
        final Player p = (Player) sender;
        switch (p.getGameMode()) {
            case SURVIVAL:
                DatabaseSingleton.INSTANCE.setPlayerData(p, "toggleLocation", Serializer.serializeLocation(p.getLocation()));
                p.setGameMode(GameMode.SPECTATOR);
                break;
            case SPECTATOR:
                final byte[] bytes = DatabaseSingleton.INSTANCE.getPlayerData(p, "toggleLocation");
                if (bytes != null) {
                    DatabaseSingleton.INSTANCE.deletePlayerData(p, "toggleLocation");
                    p.teleport(Serializer.deserializeLocation(bytes));
                }
                p.setGameMode(GameMode.SURVIVAL);
                break;
        }
        p.sendActionBar("§a模式切换成功!");
        return true;
    }
}
