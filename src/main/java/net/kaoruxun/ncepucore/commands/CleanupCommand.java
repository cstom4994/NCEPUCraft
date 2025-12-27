package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

@CommandName("cleanup")
public final class CleanupCommand extends BasicCommand {
    private BukkitTask running;

    private long startAtMs;
    private int threshold;
    private int maxChunksPerTick;

    private int totalChunks;
    private int scannedChunks;
    private int hotChunks;
    private int removedItems;
    private int removedFallingBlocks;

    public CleanupCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ncepu.cleanup")) {
            sender.sendMessage("§c你没有权限使用该指令.");
            return true;
        }

        if (args.length > 0) {
            final String sub = args[0].toLowerCase();
            if (sub.equals("cancel")) {
                if (running == null) {
                    sender.sendMessage("§e当前没有正在运行的 cleanup.");
                    return true;
                }
                running.cancel();
                running = null;
                sender.sendMessage("§a已取消 cleanup.");
                instance.getLogger().info("[cleanup] cancelled by " + sender.getName());
                return true;
            }
            if (sub.equals("status")) {
                if (running == null) {
                    sender.sendMessage("§e当前没有正在运行的 cleanup.");
                    return true;
                }
                sender.sendMessage("§acleanup 进度: §f" + scannedChunks + "/" + totalChunks +
                        " §7(热点区块=" + hotChunks + ", items=" + removedItems + ", fallingBlocks=" + removedFallingBlocks + ")");
                return true;
            }
        }

        if (running != null) {
            sender.sendMessage("§ecleanup 正在运行中. 使用 §f/cleanup status §e查看进度, 或 §f/cleanup cancel §e取消.");
            return true;
        }

        // /cleanup [threshold] [maxChunksPerTick]
        threshold = 500;
        maxChunksPerTick = 5;
        if (args.length >= 1 && isInt(args[0])) threshold = Math.max(1, Integer.parseInt(args[0]));
        if (args.length >= 2 && isInt(args[1])) maxChunksPerTick = Math.max(1, Integer.parseInt(args[1]));

        final List<Chunk> chunks = snapshotLoadedChunks();
        totalChunks = chunks.size();
        scannedChunks = 0;
        hotChunks = 0;
        removedItems = 0;
        removedFallingBlocks = 0;
        startAtMs = System.currentTimeMillis();

        sender.sendMessage("§a开始 cleanup: §floadedChunks=" + totalChunks +
                " §7threshold=" + threshold + " maxChunksPerTick=" + maxChunksPerTick);

        instance.getLogger().info("[cleanup] start by " + sender.getName() +
                " loadedChunks=" + totalChunks + " threshold=" + threshold + " maxChunksPerTick=" + maxChunksPerTick);

        // 分片执行，避免一次性扫完导致严重卡顿
        running = new BukkitRunnable() {
            int idx = 0;

            @Override
            public void run() {
                if (!instance.isEnabled()) {
                    cancel();
                    running = null;
                    return;
                }

                int processed = 0;
                while (processed < maxChunksPerTick && idx < chunks.size()) {
                    final Chunk c = chunks.get(idx++);
                    processed++;
                    scannedChunks++;

                    try {
                        if (c == null || !c.isLoaded()) continue;
                        final Entity[] entities = c.getEntities();
                        final int count = entities.length;
                        if (count < threshold) continue;

                        hotChunks++;
                        int rmItems = 0;
                        int rmFalling = 0;
                        for (final Entity e : entities) {
                            if (e instanceof Item) {
                                e.remove();
                                rmItems++;
                            } else if (e instanceof FallingBlock && !(e instanceof TNTPrimed)) {
                                e.remove();
                                rmFalling++;
                            }
                        }
                        removedItems += rmItems;
                        removedFallingBlocks += rmFalling;

                        final World w = c.getWorld();
                        instance.getLogger().info("[cleanup] hotChunk world=" + w.getName() +
                                " chunk=(" + c.getX() + "," + c.getZ() + ")" +
                                " entities=" + count +
                                " removed: items=" + rmItems + " fallingBlocks=" + rmFalling);
                    } catch (final Throwable t) {
                        instance.getLogger().warning("[cleanup] error scanning chunk=(" + c.getX() + "," + c.getZ() + "): " + t);
                    }
                }

                if (idx >= chunks.size()) {
                    final long cost = System.currentTimeMillis() - startAtMs;
                    instance.getLogger().info("[cleanup] done scannedChunks=" + scannedChunks + "/" + totalChunks +
                            " hotChunks=" + hotChunks +
                            " removed: items=" + removedItems + " fallingBlocks=" + removedFallingBlocks +
                            " costMs=" + cost);
                    sender.sendMessage("§acleanup 完成: §fscanned=" + scannedChunks + "/" + totalChunks +
                            " §7hotChunks=" + hotChunks +
                            " items=" + removedItems + " fallingBlocks=" + removedFallingBlocks +
                            " costMs=" + cost);
                    cancel();
                    running = null;
                }
            }
        }.runTaskTimer(instance, 1L, 1L);

        return true;
    }

    private List<Chunk> snapshotLoadedChunks() {
        final List<Chunk> out = new ArrayList<>();
        for (final World w : instance.getServer().getWorlds()) {
            try {
                final Chunk[] loaded = w.getLoadedChunks();
                for (final Chunk c : loaded) out.add(c);
            } catch (final Throwable ignored) {
            }
        }
        return out;
    }

    private static boolean isInt(String s) {
        if (s == null || s.isEmpty()) return false;
        int i = 0;
        if (s.charAt(0) == '-') {
            if (s.length() == 1) return false;
            i = 1;
        }
        for (; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }
}


