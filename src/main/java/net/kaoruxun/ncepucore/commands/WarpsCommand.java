package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CommandName("warps")
public final class WarpsCommand extends BasicCommand {
    private static final int PAGE_SIZE = 10;

    public WarpsCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        final ArrayList<String> list = new ArrayList<>(instance.warps);
        if (list.isEmpty()) {
            sender.sendMessage("§e当前没有任何地标 可使用 §b/setwarp <名字> §e创建");
            return true;
        }

        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);

        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                return false;
            }
        }

        final int total = list.size();
        final int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page < 1 || page > pages) {
            sender.sendMessage("§c页码范围: 1-" + pages);
            return true;
        }

        final int start = (page - 1) * PAGE_SIZE;
        final int end = Math.min(start + PAGE_SIZE, total);

        sender.sendMessage("§a地标列表 §7(" + total + "个) §f| §e第 §b" + page + "§e/§b" + pages + "§e页");
        for (int i = start; i < end; i++) {
            sender.sendMessage("§b- §f" + list.get(i));
        }
        sender.sendMessage("§7用法: §f/warp <名字> §7| §f/warps <页码>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        return args.length == 1 ? List.of("1", "2", "3") : null;
    }
}

