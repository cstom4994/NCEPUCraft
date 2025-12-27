package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.DatabaseSingleton;
import net.kaoruxun.ncepucore.utils.Serializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
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
                // 如果玩家头上有乘客(比如搬运功能) 先放下 避免利用观察者位移带走实体
                if (!p.getPassengers().isEmpty()) {
                    for (final Entity it : p.getPassengers()) it.leaveVehicle();
                }
                DatabaseSingleton.INSTANCE.setPlayerData(p, "toggleLocation", Serializer.serializeLocation(p.getLocation()));
                p.setGameMode(GameMode.SPECTATOR);
                break;
            case SPECTATOR:
                final byte[] bytes = DatabaseSingleton.INSTANCE.getPlayerData(p, "toggleLocation");
                final Location dest = bytes == null ? null : Serializer.deserializeLocation(bytes);
                if (bytes != null) {
                    DatabaseSingleton.INSTANCE.deletePlayerData(p, "toggleLocation");
                }

                // 先切回生存 再传送 并且强制放下乘客 避免 teleport 失败导致卡在当前位置
                if (!p.getPassengers().isEmpty()) {
                    for (final Entity it : p.getPassengers()) it.leaveVehicle();
                }
                p.setGameMode(GameMode.SURVIVAL);
                if (dest != null && !p.teleport(dest)) {
                    // 极少数情况下同步teleport仍可能失败 下一tick再试一次
                    Bukkit.getScheduler().runTask(instance, () -> p.teleport(dest));
                }
                break;
        }
        p.sendActionBar("§a模式切换成功!");
        return true;
    }
}
