package net.kaoruxun.ncepucore.shulkerboxpreview;

import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author RukiaOvO
 * @date 2025/12/30
 * @description allow players to preview shulker box without opening them
 */

public class ShulkerBoxPreviewListener implements Listener {

    private final static String PREVIEW_TITLE = "§6§l潜影盒预览";
    private final ShulkerBoxService shulkerBoxService;

    public ShulkerBoxPreviewListener(ShulkerBoxService shulkerBoxService) {
        this.shulkerBoxService = shulkerBoxService;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        // 检查是否在潜影盒预览界面中
        if (e.getView().getTitle().contains(PREVIEW_TITLE)) {
            e.setCancelled(true);
        }

        ItemStack itemStack = e.getCurrentItem();
        ClickType clickType = e.getClick();
        ShulkerBox shulkerBox = ShulkerBoxPreviewUtils.toShulkerBox(itemStack);
        if (shulkerBox != null && (clickType == ClickType.RIGHT || clickType == ClickType.CREATIVE)) {
            openShulkerPreview(player, shulkerBox);
        }

        clearCursorItem(player);
    }

    private void openShulkerPreview(Player player, ShulkerBox shulkerBox) {
        Inventory previewInventory = shulkerBoxService.initPreviewInventory(shulkerBox, PREVIEW_TITLE);
        if (previewInventory != null) {
            player.openInventory(previewInventory);
        }
    }

    // 清理光标预留物品
    private void clearCursorItem(Player player) {
        if (player.getItemOnCursor() != null) {
            player.setItemOnCursor(null);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        clearCursorItem(p);
    }
}
