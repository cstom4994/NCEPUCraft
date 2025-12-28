package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.EnderPearlChunkLoader;
import net.kaoruxun.ncepucore.Main;
import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.command.CommandSender;

import java.util.*;

@CommandName("enderpearl")
public final class EnderPearlCommand extends BasicCommand {
    private static final int PER_PAGE = 8;

    public EnderPearlCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ncepu.enderpearl")) {
            sender.sendMessage("§c你没有权限来执行这个指令!");
            return true;
        }

        // 需要读取 Bukkit 实体信息 放到主线程执行更稳妥
        instance.getServer().getScheduler().runTask(instance, () -> {
            final EnderPearlChunkLoader loader = instance.getEnderPearlChunkLoader();
            if (loader == null || !loader.isEnabled()) {
                sender.sendMessage("§6[EnderPearl] §c功能未启用");
                return;
            }

            // /enderpearl scan: 直接扫描当前已加载的末影珍珠数量(用于排查事件没追踪到还是票据没加上)
            if (args.length >= 1 && args[0].equalsIgnoreCase("scan")) {
                int found = 0;
                final Map<String, Integer> byWorld = new LinkedHashMap<>();
                for (World w : instance.getServer().getWorlds()) {
                    final int c = w.getEntitiesByClass(EnderPearl.class).size();
                    if (c > 0) byWorld.put(w.getName(), c);
                    found += c;
                }
                sender.sendMessage("§6[EnderPearl] §f扫描已加载实体: §e" + found + "§f 个末影珍珠  §7(追踪中=§e" + loader.getTrackedCount() + "§7)");
                if (byWorld.isEmpty()) {
                    sender.sendMessage("§6[EnderPearl] §7(当前所有世界都未发现已加载的末影珍珠实体)");
                } else {
                    byWorld.forEach((name, c) -> sender.sendMessage("§7- §b" + name + "§7: §e" + c));
                }
                sender.sendMessage("§6[EnderPearl] §7提示: 把珍珠丢进气泡柱后立刻执行 /enderpearl scan 再执行 /enderpearl 看票据区块信息");
                return;
            }

            int page = 1;
            if (args.length >= 1) {
                try {
                    page = Integer.parseInt(args[0]);
                } catch (Exception ignored) {
                    sender.sendMessage("§6[EnderPearl] §f用法: §e/enderpearl §7[页码]");
                    return;
                }
            }
            if (page < 1) page = 1;

            final List<EnderPearlChunkLoader.DebugEntry> list = loader.snapshotDebug();
            list.sort(Comparator.comparingLong(a -> a.trackedAtMs));

            final int total = list.size();
            final int maxPearls = loader.getMaxPearls();
            final int tickInterval = loader.getTickInterval();
            final int radius = loader.getRadius();

            // 统计票据区块数(按世界聚合 去重)
            final Map<UUID, Set<Long>> ticketChunksByWorld = new HashMap<>();
            for (EnderPearlChunkLoader.DebugEntry e : list) {
                final Set<Long> set = ticketChunksByWorld.computeIfAbsent(e.worldId, k -> new HashSet<>());
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        final int x = e.chunkX + dx;
                        final int z = e.chunkZ + dz;
                        set.add(packChunk(x, z));
                    }
                }
            }
            int totalTicketChunks = 0;
            for (Set<Long> s : ticketChunksByWorld.values()) totalTicketChunks += s.size();

            sender.sendMessage("§6[EnderPearl] §f追踪末影珍珠: §e" + total + "§f/§e" + maxPearls +
                    " §7tick间隔=§e" + tickInterval + "§7 半径=§e" + radius +
                    "§7 票据区块(去重)=§e" + totalTicketChunks);

            if (total == 0) {
                sender.sendMessage("§6[EnderPearl] §7(当前没有被追踪的末影珍珠)  §7可用: §e/enderpearl scan");
                return;
            }

            final int pages = (total + PER_PAGE - 1) / PER_PAGE;
            if (page > pages) page = pages;

            final int from = (page - 1) * PER_PAGE;
            final int to = Math.min(total, from + PER_PAGE);
            sender.sendMessage("§6[EnderPearl] §f列表 §7(" + page + "/" + pages + ")  §f显示: §e" + (from + 1) + "§f-§e" + to);

            final long now = System.currentTimeMillis();
            for (int i = from; i < to; i++) {
                final EnderPearlChunkLoader.DebugEntry e = list.get(i);
                final long ageSec = Math.max(0L, (now - e.trackedAtMs) / 1000L);
                final int chunkCount = (radius * 2 + 1) * (radius * 2 + 1);
                final String shooter = (e.shooterName == null) ? "unknown" : e.shooterName;
                sender.sendMessage("§7- §f#" + (i + 1) +
                        " §7世界=§b" + e.worldName +
                        " §7区块=§e(" + e.chunkX + "," + e.chunkZ + ")" +
                        " §7票据=§a" + chunkCount +
                        " §7坐标=§f(" + fmt(e.x) + "," + fmt(e.y) + "," + fmt(e.z) + ")" +
                        " §7玩家=§d" + shooter +
                        " §7存在时间=§e" + ageSec + "s" +
                        (e.entityValid ? "" : " §c(实体已无效, 等待清理)")
                );
                // 详细区块范围(只输出范围 不逐个列举 避免刷屏)
                sender.sendMessage("  §8chunks: §7[" + (e.chunkX - radius) + "," + (e.chunkZ - radius) + "] -> [" +
                        (e.chunkX + radius) + "," + (e.chunkZ + radius) + "]");
            }
        });

        return true;
    }

    private static long packChunk(int x, int z) {
        return (((long) x) << 32) ^ (z & 0xffffffffL);
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.ROOT, "%.2f", v);
    }
}


