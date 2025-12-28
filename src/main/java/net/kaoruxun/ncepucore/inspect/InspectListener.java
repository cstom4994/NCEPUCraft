package net.kaoruxun.ncepucore.inspect;

import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public final class InspectListener implements Listener {
    private static final int QUERY_LIMIT = 5;
    private static final int MAX_DIFF_ITEMS = 8;

    private final Map<UUID, ContainerSession> containerSessions = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void preventPlaceWhenInspecting(BlockPlaceEvent e) {
        if (InspectState.isInspecting(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        final Player p = e.getPlayer();
        if (InspectState.isInspecting(p.getUniqueId())) return; // 检查模式下不记录(且会在低优先级被取消)
        final Block b = e.getBlockPlaced();
        InspectAsyncWriter.append(p, InspectAction.PLACE, b);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void preventBreakWhenInspecting(BlockBreakEvent e) {
        if (InspectState.isInspecting(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        final Player p = e.getPlayer();
        if (InspectState.isInspecting(p.getUniqueId())) return; // 检查模式下不记录(且会在低优先级被取消)
        final Block b = e.getBlock();
        InspectAsyncWriter.append(p, InspectAction.BREAK, b);
    }

    // 检查模式 阻止破坏/放置/交互 改为点击查询
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != RIGHT_CLICK_BLOCK && e.getAction() != LEFT_CLICK_BLOCK) return;
        final Player p = e.getPlayer();
        if (!InspectState.isInspecting(p.getUniqueId())) return;
        final Block b = e.getClickedBlock();
        if (b == null) return;

        e.setCancelled(true);

        final Location loc = b.getLocation();
        final List<InspectRecord> records = InspectService.query(loc, QUERY_LIMIT);
        final String header = "§6[Inspect] §f" + b.getType().name() +
                " §7(" + loc.getWorld().getName() + " " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")";
        p.sendMessage(header);
        if (records.isEmpty()) {
            p.sendMessage("§7  没有记录");
            return;
        }
        final long now = System.currentTimeMillis();
        for (InspectRecord r : records) {
            final String extra = (r.extra == null || r.extra.isEmpty()) ? "" : (" §7[" + r.extra + "]");
            p.sendMessage("§7  - §e" + r.playerName + " §f" + r.action.display + " §a" + r.material.name() + extra + " §7(" + formatAge(now - r.tsMillis) + ")");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        InspectState.clear(e.getPlayer().getUniqueId());
        containerSessions.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onContainerOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        if (InspectState.isInspecting(p.getUniqueId())) return; // 检查模式下不会打开(交互会被取消) 这里也不记录

        final Inventory inv = e.getInventory();
        final Location loc = getHolderLocation(inv.getHolder());
        if (loc == null || loc.getWorld() == null) return;

        // 记录“打开”
        final Material containerType = loc.getBlock().getType();
        InspectAsyncWriter.append(p, InspectAction.CONTAINER_OPEN, loc.getBlock());

        // 建立会话快照（用于关闭时对比取放）
        final EnumMap<Material, Integer> before = countItems(inv);
        containerSessions.put(p.getUniqueId(), new ContainerSession(inv, loc, containerType, before));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onContainerClose(InventoryCloseEvent e) {
        final Player p = (Player) e.getPlayer();
        final ContainerSession session = containerSessions.remove(p.getUniqueId());
        if (session == null) return;
        if (session.inventory != e.getInventory()) return; // 避免串会话

        final EnumMap<Material, Integer> after = countItems(e.getInventory());
        final Diff diff = diff(session.before, after);
        if (diff.isEmpty()) return;

        final String extra = buildDiffSummary(diff);
        InspectAsyncWriter.append(p, InspectAction.CONTAINER_CHANGE, session.loc.getBlock(), extra);
    }

    private static String formatAge(long deltaMillis) {
        if (deltaMillis < 0) deltaMillis = 0;
        final long sec = deltaMillis / 1000;
        if (sec < 60) return sec + "秒前";
        final long min = sec / 60;
        if (min < 60) return min + "分钟前";
        final long hour = min / 60;
        if (hour < 24) return hour + "小时前";
        final long day = hour / 24;
        return day + "天前";
    }

    private static final class ContainerSession {
        final Inventory inventory;
        final Location loc;
        final Material containerType;
        final EnumMap<Material, Integer> before;

        ContainerSession(Inventory inventory, Location loc, Material containerType, EnumMap<Material, Integer> before) {
            this.inventory = inventory;
            this.loc = loc;
            this.containerType = containerType;
            this.before = before;
        }
    }

    private static Location getHolderLocation(InventoryHolder holder) {
        if (holder == null) return null;
        try {
            // 大多数方块容器 holder 都是 BlockState 带 getLocation()
            final var m = holder.getClass().getMethod("getLocation");
            final Object o = m.invoke(holder);
            return (o instanceof Location) ? (Location) o : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static EnumMap<Material, Integer> countItems(Inventory inv) {
        final EnumMap<Material, Integer> map = new EnumMap<>(Material.class);
        final ItemStack[] contents = inv.getStorageContents();
        for (ItemStack it : contents) {
            if (it == null) continue;
            final Material m = it.getType();
            if (m == Material.AIR) continue;
            map.merge(m, it.getAmount(), Integer::sum);
        }
        return map;
    }

    private static final class Diff {
        final EnumMap<Material, Integer> putIn = new EnumMap<>(Material.class);
        final EnumMap<Material, Integer> takeOut = new EnumMap<>(Material.class);

        boolean isEmpty() {
            return putIn.isEmpty() && takeOut.isEmpty();
        }
    }

    private static Diff diff(EnumMap<Material, Integer> before, EnumMap<Material, Integer> after) {
        final Diff d = new Diff();
        final EnumMap<Material, Integer> all = new EnumMap<>(Material.class);
        for (Map.Entry<Material, Integer> e : before.entrySet()) all.put(e.getKey(), 1);
        for (Map.Entry<Material, Integer> e : after.entrySet()) all.put(e.getKey(), 1);
        for (Material m : all.keySet()) {
            final int b = before.getOrDefault(m, 0);
            final int a = after.getOrDefault(m, 0);
            final int delta = a - b;
            if (delta > 0) d.putIn.put(m, delta);
            else if (delta < 0) d.takeOut.put(m, -delta);
        }
        return d;
    }

    private static String buildDiffSummary(Diff diff) {
        final StringBuilder sb = new StringBuilder();
        if (!diff.putIn.isEmpty()) {
            sb.append("放入:");
            appendTopItems(sb, diff.putIn, "+");
        }
        if (!diff.takeOut.isEmpty()) {
            if (!sb.isEmpty()) sb.append(" | ");
            sb.append("取出:");
            appendTopItems(sb, diff.takeOut, "-");
        }
        return sb.toString();
    }

    private static void appendTopItems(StringBuilder sb, EnumMap<Material, Integer> map, String sign) {
        int shown = 0;
        int totalTypes = map.size();
        for (Map.Entry<Material, Integer> e : map.entrySet()) {
            if (shown >= MAX_DIFF_ITEMS) break;
            if (shown > 0) sb.append(", ");
            sb.append(sign).append(e.getValue()).append(" ").append(e.getKey().name());
            shown++;
        }
        if (totalTypes > shown) sb.append(", ...");
    }
}


