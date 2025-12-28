package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.inspect.InspectState;
import net.kaoruxun.ncepucore.inspect.InspectService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("inspect")
public final class InspectCommand extends BasicCommand {
    public InspectCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        final Player p = (sender instanceof Player) ? (Player) sender : null;

        boolean enabled;
        // /inspect clean <day>
        if (args.length >= 1 && args[0].equalsIgnoreCase("clean")) {
            if (!sender.hasPermission("ncepu.inspect.clean")) {
                sender.sendMessage("§c你没有权限来执行这个指令!");
                return true;
            }
            // /inspect clean        -> 清空全部
            // /inspect clean 0      -> 清空全部
            // /inspect clean <day>  -> 清理早于 day 天的记录
            final int day;
            if (args.length == 1) {
                day = 0;
            } else if (args.length == 2) {
                try {
                    day = Integer.parseInt(args[1]);
                } catch (Exception ignored) {
                    return false;
                }
            } else {
                return false;
            }
            if (day < 0) return false;

            final long cutoff = (day == 0)
                    ? Long.MAX_VALUE // 全部记录都会被判定为过旧
                    : (System.currentTimeMillis() - day * 24L * 60L * 60L * 1000L);
            sender.sendMessage("§6[Inspect] §f开始清理数据库 " +
                    (day == 0 ? "§e清空全部记录" : ("删除早于 §e" + day + "§f 天的记录")));

            instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
                final InspectService.CleanupResult r = InspectService.cleanupOlderThan(cutoff);
                instance.getServer().getScheduler().runTask(instance, () -> {
                    sender.sendMessage("§6[Inspect] §f清理完成 扫描key=§e" + r.scannedKeys +
                            "§f 更新key=§e" + r.updatedKeys +
                            "§f 删除key=§e" + r.deletedKeys +
                            "§f 移除记录=§e" + r.removedRecords +
                            "§f 保留记录=§e" + r.keptRecords);
                });
            });
            return true;
        }

        if (p == null) return false;
        if (!p.hasPermission("ncepu.inspect")) {
            p.sendMessage("§c你没有权限来执行这个指令!");
            return true;
        }
        if (args.length == 0) {
            enabled = InspectState.toggle(p.getUniqueId());
        } else {
            switch (args[0].toLowerCase()) {
                case "on", "enable" -> {
                    InspectState.setInspecting(p.getUniqueId(), true);
                    enabled = true;
                }
                case "off", "disable" -> {
                    InspectState.setInspecting(p.getUniqueId(), false);
                    enabled = false;
                }
                case "status" -> {
                    enabled = InspectState.isInspecting(p.getUniqueId());
                }
                default -> {
                    return false;
                }
            }
        }

        p.sendMessage("§6[Inspect] §f检查模式: " + (enabled ? "§a开启" : "§c关闭") + "§f 点击方块以查询放置/破坏/容器变更记录 ");
        return true;
    }
}


