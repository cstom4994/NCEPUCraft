package net.kaoruxun.ncepucore;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;


// 砍树连锁 玩家用斧头砍天然树(周围有非持久树叶 且树干中不包含玩家放置的原木)时 自动砍掉整棵树并自然掉落
// 同时记录本次连锁砍树破坏的方块快照 支持 /treeback 回溯一次
final class TreeChopperListener implements Listener {

    // 记录玩家放置的原木位置(不持久化 重启后会清空)
    private record BlockPos(UUID worldId, int x, int y, int z) {}

    private record BlockSnapshot(BlockPos pos, Material type, String blockDataString) {}

    private record ChopOperation(long createdAtMs, Map<BlockPos, BlockSnapshot> snapshots) {}

    private final Set<BlockPos> playerPlacedLogs = new HashSet<>();

    // 记录每个玩家最近一次连锁砍树操作(只保留 1 次)
    private final Map<UUID, ChopOperation> lastChopByPlayer = new HashMap<>();

    // 防止极端结构导致搜索过大/卡顿
    private static final int MAX_LOGS = 512;

    // 防止树干很分散时 bounding box 体积过大导致卡顿(仅用于清理树叶阶段)
    private static final int MAX_LEAF_BOX_VOLUME = 200_000;

    // 回溯有效期(避免玩家隔很久回滚导致误操作/大范围刷方块)
    private static final long ROLLBACK_TTL_MS = 10L * 60L * 1000L;

    // 防重入 避免 breakNaturally 引发的连锁触发(不同实现/版本下可能存在差异)
    private final Set<UUID> choppingPlayers = new HashSet<>();

    private static BlockPos posOf(Block b) {
        return new BlockPos(b.getWorld().getUID(), b.getX(), b.getY(), b.getZ());
    }

    private static BlockSnapshot snapshotOf(Block b) {
        BlockData data = b.getBlockData();
        return new BlockSnapshot(posOf(b), b.getType(), data.getAsString(true));
    }


    // 回溯玩家最近一次连锁砍树操作
    // 注意 不会自动回收掉落物
    boolean rollbackLastChop(Player player) {
        ChopOperation op = lastChopByPlayer.remove(player.getUniqueId());
        if (op == null) return false;
        if (System.currentTimeMillis() - op.createdAtMs > ROLLBACK_TTL_MS) return false;

        // 顺序不强制
        op.snapshots.values().stream()
                .sorted((a, b) -> {
                    boolean aLog = a.type.toString().endsWith("_LOG");
                    boolean bLog = b.type.toString().endsWith("_LOG");
                    return Boolean.compare(!aLog, !bLog);
                })
                .forEach(this::restoreSnapshot);

        return true;
    }

    private void restoreSnapshot(BlockSnapshot snapshot) {
        BlockPos pos = snapshot.pos;
        World world = Bukkit.getWorld(pos.worldId);
        if (world == null) return;
        Block block = world.getBlockAt(pos.x, pos.y, pos.z);
        try {
            BlockData data = Bukkit.createBlockData(snapshot.blockDataString);
            block.setType(snapshot.type, false);
            block.setBlockData(data, false);
        } catch (IllegalArgumentException ignored) {
            // 回滚字符串无法解析时 至少恢复方块类型
            block.setType(snapshot.type, false);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (isNaturalLog(block)) {
            playerPlacedLogs.add(posOf(block));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTreeBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (!choppingPlayers.add(player.getUniqueId())) return;

        // 用 LinkedHashMap 保持一个稳定顺序 同时以坐标去重(避免重复记录)
        Map<BlockPos, BlockSnapshot> snapshots = new LinkedHashMap<>();

        try {
            Block block = event.getBlock();
            if (!isNaturalLog(block)) return;

            ItemStack tool = player.getInventory().getItemInMainHand();
            if (!isAxe(tool)) return;

            ItemMeta meta = tool.getItemMeta();
            if (!(meta instanceof Damageable damageable)) return;

            int currentDamage = damageable.getDamage();
            int maxDurability = tool.getType().getMaxDurability();
            if (maxDurability <= 0) return;

            int baseBreaks = maxDurability - currentDamage;
            if (baseBreaks <= 0) return;

            int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
            int effectiveMaxBreaks = unbreakingLevel > 0 ? baseBreaks * (unbreakingLevel + 1) : baseBreaks;

            // 如果是玩家放置的原木 只砍这一个(并从记录里移除)
            if (isPlayerPlaced(block)) {
                playerPlacedLogs.remove(posOf(block));
                return;
            }

            TreeSearchResult result = findTree(block, Math.min(effectiveMaxBreaks, MAX_LOGS));

            // 没找到天然树叶 或者 树干里混入了玩家放置的原木 视为建筑/结构 只砍单块
            if (!result.foundLeaves || result.foundPlayerPlaced) return;

            // 取消默认破坏 避免双倍掉落
            event.setCancelled(true);

            // 按参考实现模拟耐久损耗(考虑耐久附魔概率)
            Random rand = new Random();
            for (Block log : result.treeBlocks) {
                if (currentDamage >= maxDurability) break;

                snapshots.putIfAbsent(posOf(log), snapshotOf(log));
                log.breakNaturally(tool);

                if (unbreakingLevel == 0 || rand.nextDouble() < (1.0 / (unbreakingLevel + 1))) {
                    currentDamage++;
                }
            }

            damageable.setDamage(currentDamage);
            tool.setItemMeta(meta);

            if (currentDamage >= maxDurability) {
                player.getInventory().setItemInMainHand(null);
            }

            // 清理树叶以获得自然掉落(跳过玩家放置的持久叶子)
            breakLeavesInBoundingBox(result.treeBlocks, snapshots);

            // 仅当本次确实有方块被插件破坏时 写入可回溯记录
            if (!snapshots.isEmpty()) {
                lastChopByPlayer.put(player.getUniqueId(), new ChopOperation(System.currentTimeMillis(), snapshots));
                // player.sendMessage("§a已记录本次砍树操作 可使用 §f/treeback §a回溯一次(仅限短时间内)");
            }
        } finally {
            choppingPlayers.remove(player.getUniqueId());
        }
    }

    // 是否为天然原木非去皮
    private boolean isNaturalLog(Block block) {
        Material type = block.getType();
        String name = type.toString();
        return name.endsWith("_LOG") && !name.startsWith("STRIPPED_");
    }

    // 是否为树叶 并且不是玩家放置的持久叶子 
    private boolean isNaturalLeaf(Block block) {
        if (!block.getType().toString().endsWith("_LEAVES")) return false;
        BlockData data = block.getBlockData();
        if (data instanceof Leaves leaves) {
            return !leaves.isPersistent();
        }
        return true;
    }

    private boolean isAxe(ItemStack item) {
        if (item == null) return false;
        return item.getType().toString().endsWith("_AXE");
    }

    private boolean isPlayerPlaced(Block block) {
        return playerPlacedLogs.contains(posOf(block));
    }

    private TreeSearchResult findTree(Block start, int maxBreaks) {
        Set<Block> treeBlocks = new HashSet<>();
        Queue<Block> queue = new ArrayDeque<>();
        queue.add(start);

        boolean foundLeaves = false;
        boolean foundPlayerPlaced = false;

        while (!queue.isEmpty() && treeBlocks.size() < maxBreaks && treeBlocks.size() < MAX_LOGS) {
            Block current = queue.poll();
            if (!isNaturalLog(current)) continue;
            if (!treeBlocks.add(current)) continue;

            if (isPlayerPlaced(current)) foundPlayerPlaced = true;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (treeBlocks.contains(neighbor)) continue;
                        if (isNaturalLog(neighbor)) {
                            queue.add(neighbor);
                        } else if (isNaturalLeaf(neighbor)) {
                            foundLeaves = true;
                        }
                    }
                }
            }
        }

        return new TreeSearchResult(treeBlocks, foundLeaves, foundPlayerPlaced);
    }

    private void breakLeavesInBoundingBox(Set<Block> treeLogs, Map<BlockPos, BlockSnapshot> snapshots) {
        if (treeLogs.isEmpty()) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (Block log : treeLogs) {
            int x = log.getX();
            int y = log.getY();
            int z = log.getZ();
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }

        int margin = 2;
        minX -= margin; maxX += margin;
        minY -= margin; maxY += margin;
        minZ -= margin; maxZ += margin;

        long volume = (long) (maxX - minX + 1) * (long) (maxY - minY + 1) * (long) (maxZ - minZ + 1);
        if (volume > MAX_LEAF_BOX_VOLUME) return;

        Block sample = treeLogs.iterator().next();
        var world = sample.getWorld();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (isNaturalLeaf(b)) {
                        snapshots.putIfAbsent(posOf(b), snapshotOf(b));
                        b.breakNaturally();
                    }
                }
            }
        }
    }

    private static final class TreeSearchResult {
        final Set<Block> treeBlocks;
        final boolean foundLeaves;
        final boolean foundPlayerPlaced;

        private TreeSearchResult(Set<Block> treeBlocks, boolean foundLeaves, boolean foundPlayerPlaced) {
            this.treeBlocks = treeBlocks;
            this.foundLeaves = foundLeaves;
            this.foundPlayerPlaced = foundPlayerPlaced;
        }
    }
}


