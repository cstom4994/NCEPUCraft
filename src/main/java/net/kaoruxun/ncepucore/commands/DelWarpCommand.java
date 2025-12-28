package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.DatabaseSingleton;
import net.kaoruxun.ncepucore.utils.Serializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CommandName("delwarp")
public final class DelWarpCommand extends BasicCommand {
    public static final String WARPS = "warps";

    public DelWarpCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length < 1) return false;
        final Player p = (Player) sender;
        if (DatabaseSingleton.INSTANCE.get("warp." + args[0]) == null) {
            p.sendMessage("§c该地标不存在或已被删除!");
            return false;
        }
        DatabaseSingleton.INSTANCE.delete("warp." + args[0]);
        instance.warps.remove(args[0]);
        try {
            DatabaseSingleton.INSTANCE.set(WARPS, Serializer.serializeObject(instance.warps));
        } catch (IOException e) {
            e.printStackTrace();
            p.sendMessage("§c未知错误，请查看服务器日志!");
        }
        p.sendMessage("§a成功删除地标!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return Collections.emptyList();
        final ArrayList<String> candidates = new ArrayList<>(instance.warps);
        Collections.sort(candidates, String.CASE_INSENSITIVE_ORDER);
        final ArrayList<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(args[0], candidates, result);
        return result;
    }
}
