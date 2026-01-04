package net.kaoruxun.ncepucore.shulker;

import net.kaoruxun.ncepucore.inspect.InspectState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.event.block.Action.RIGHT_CLICK_AIR;

// 手持潜影盒对空气右键打开 并且关闭时写回物品NBT
@SuppressWarnings("unused")
public final class ShulkerBoxPreviewListener implements Listener {
    private static final String PERMISSION = "ncepu.shulkerboxpreview";
    private static final int OFFHAND_SLOT = 40;

    private final Map<UUID, Session> sessions = new HashMap<>();

    public boolean openFor(Player player, EquipmentSlot hand) {
        if (!player.hasPermission(PERMISSION)) return false;
        if (sessions.containsKey(player.getUniqueId())) return false;

        final ItemStack inHand = (hand == EquipmentSlot.OFF_HAND)
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();

        if (!isShulkerBoxItem(inHand)) return false;
        // 正常生存模式下潜影盒不应可堆叠 为避免堆叠潜影盒写回导致复制/错写直接拒绝
        if (inHand.getAmount() != 1) return false;

        final BlockStateMeta meta = (BlockStateMeta) inHand.getItemMeta();
        if (meta == null) return false;
        final BlockState state = meta.getBlockState();
        if (!(state instanceof ShulkerBox shulkerBox)) return false;

        final Inventory ui = Bukkit.createInventory(player, 27, "潜影盒");
        ui.setContents(shulkerBox.getInventory().getContents());

        final int heldSlot = player.getInventory().getHeldItemSlot();
        sessions.put(player.getUniqueId(), new Session(player.getUniqueId(), ui, hand, heldSlot));
        player.openInventory(ui);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (InspectState.isInspecting(p.getUniqueId())) return; // 检查模式下不接管交互

        // 仅在对空气右键时打开 避免影响正常放置潜影盒(对方块右键)
        if (e.getAction() != RIGHT_CLICK_AIR) return;

        final ItemStack item = e.getItem();
        if (!isShulkerBoxItem(item)) return;
        if (!p.hasPermission(PERMISSION)) return;

        // 打开预览 同时取消事件防止尝试放置潜影盒/触发其它交互
        if (openFor(p, e.getHand() == null ? EquipmentSlot.HAND : e.getHand())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        final Session session = sessions.get(player.getUniqueId());
        if (session == null) return;
        if (e.getView().getTopInventory() != session.ui) return;

        // 禁止潜影盒套娃 不允许把任何潜影盒物品放进顶部 27 格
        final boolean clickedTop = e.getClickedInventory() == e.getView().getTopInventory();
        final boolean clickedBottom = e.getClickedInventory() == e.getView().getBottomInventory();

        // 在顶部格子里直接放置(鼠标光标为潜影盒)
        if (clickedTop && isShulkerBoxItem(e.getCursor())) {
            e.setCancelled(true);
            return;
        }

        // 从底部 Shift-Click 把潜影盒送入顶部
        if (clickedBottom && e.isShiftClick() && isShulkerBoxItem(e.getCurrentItem())) {
            e.setCancelled(true);
            return;
        }

        // 数字键把热键栏上的潜影盒换进顶部
        if (clickedTop && e.getClick() == ClickType.NUMBER_KEY) {
            final int btn = e.getHotbarButton();
            if (btn >= 0 && btn <= 8) {
                final ItemStack hotbar = player.getInventory().getItem(btn);
                if (isShulkerBoxItem(hotbar)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        // 防止用F键把正在使用的潜影盒换走(ClickType.SWAP_OFFHAND)
        if (e.getClick() == ClickType.SWAP_OFFHAND) {
            e.setCancelled(true);
            return;
        }

        // 防止用数字键把正在使用的潜影盒换走
        if (e.getClick() == ClickType.NUMBER_KEY &&
                session.hand == EquipmentSlot.HAND &&
                e.getHotbarButton() == session.heldHotbarSlot) {
            e.setCancelled(true);
            return;
        }

        // 防止直接点击/Shift 点击把正在使用的潜影盒移走
        if (e.getClickedInventory() == player.getInventory()) {
            final int slot = e.getSlot(); // PlayerInventory slot index
            if ((session.hand == EquipmentSlot.HAND && slot == session.heldHotbarSlot) ||
                    (session.hand == EquipmentSlot.OFF_HAND && slot == OFFHAND_SLOT)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        final Session session = sessions.get(player.getUniqueId());
        if (session == null) return;
        if (e.getView().getTopInventory() != session.ui) return;

        // 禁止拖拽把潜影盒放入顶部
        final int topSize = session.ui.getSize(); // 27
        if (e.getNewItems().values().stream().anyMatch(ShulkerBoxPreviewListener::isShulkerBoxItem)) {
            for (int raw : e.getRawSlots()) {
                if (raw >= 0 && raw < topSize) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        // Chest 风格界面 top(0..26) bottom(main 27..53, hotbar 54..62)
        if (session.hand == EquipmentSlot.HAND) {
            final int rawHeld = topSize + 27 + session.heldHotbarSlot;
            if (e.getRawSlots().contains(rawHeld)) e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHeldChange(PlayerItemHeldEvent e) {
        final Session session = sessions.get(e.getPlayer().getUniqueId());
        if (session == null) return;
        // 打开期间禁止切换手持栏位 避免关闭时写回到当前手上物品导致丢失/错写
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        final Session session = sessions.get(player.getUniqueId());
        if (session == null) return;
        if (e.getView().getTopInventory() != session.ui) return;

        try {
            writeBack(player, session);
        } finally {
            sessions.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent e) {
        final Session session = sessions.get(e.getPlayer().getUniqueId());
        if (session == null) return;
        // 打开界面时禁止交换主副手(否则可能把目标潜影盒换走)
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        final Player p = e.getPlayer();
        final Session session = sessions.get(p.getUniqueId());
        if (session == null) return;

        // 为避免各种边缘情况下把正在编辑的潜影盒丢出导致写回失败/复制 这里简单地在会话期间禁用丢物品
        e.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // 掉线时 Bukkit 不保证一定触发 InventoryCloseEvent 这里强制清理会话
        sessions.remove(e.getPlayer().getUniqueId());
    }

    private void writeBack(Player player, Session session) {
        final ItemStack target = (session.hand == EquipmentSlot.OFF_HAND)
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItem(session.heldHotbarSlot);
        if (!isShulkerBoxItem(target)) return;
        if (target.getAmount() != 1) return;

        if (!(target.getItemMeta() instanceof BlockStateMeta meta)) return;
        final BlockState state = meta.getBlockState();
        if (!(state instanceof ShulkerBox shulkerBox)) return;

        shulkerBox.getInventory().setContents(session.ui.getContents());
        meta.setBlockState(shulkerBox);
        target.setItemMeta(meta);

        if (session.hand == EquipmentSlot.OFF_HAND) player.getInventory().setItemInOffHand(target);
        else player.getInventory().setItem(session.heldHotbarSlot, target);
    }

    private static boolean isShulkerBoxItem(ItemStack item) {
        if (item == null) return false;
        final Material type = item.getType();
        return type != Material.AIR && Tag.SHULKER_BOXES.isTagged(type);
    }

    private record Session(UUID playerId, Inventory ui, EquipmentSlot hand, int heldHotbarSlot) { }
}


