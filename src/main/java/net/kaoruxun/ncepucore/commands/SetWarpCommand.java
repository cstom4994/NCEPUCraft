package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.DatabaseSingleton;
import net.kaoruxun.ncepucore.utils.Serializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashSet;

@CommandName("setwarp")
public final class SetWarpCommand extends BasicCommand {
    public static final String WARPS = "warps";

    @SuppressWarnings("unchecked")
    public SetWarpCommand(Main main) {
        super(main);
        final byte[] bytes = DatabaseSingleton.INSTANCE.get(WARPS);
        if (bytes != null) try {
            main.warps.addAll((HashSet<String>) Serializer.deserializeObject(bytes));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length < 1) return false;
        final Player p = (Player) sender;
        DatabaseSingleton.INSTANCE.set("warp." + args[0], Serializer.serializeLocation(p.getLocation()));
        instance.warps.add(args[0]);
        try {
            DatabaseSingleton.INSTANCE.set(WARPS, Serializer.serializeObject(instance.warps));
        } catch (IOException e) {
            e.printStackTrace();
            p.sendMessage("§c未知错误，请查看服务器日志!");
        }
        p.sendMessage("§a成功设置地标!");
        return true;
    }
}
