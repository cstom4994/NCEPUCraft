package net.kaoruxun.ncepucore.shulkerboxpreview;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author RukiaOvO
 * @date 2025/12/31
 * @description allow players to preview shulker box without opening them
 */
public class ShulkerBoxService {

    public Inventory initPreviewInventory(ShulkerBox shulkerBox, String titleName) {
        // 若在背包中右键潜影盒，则创建一个新的库存界面
        Inventory previewInventory = Bukkit.createInventory(
                null,
                InventoryType.SHULKER_BOX,
                titleName
        );

        // 复制潜影盒内容到预览界面
        ItemStack[] contents = shulkerBox.getInventory().getContents();
        for (int i = 0; i < Math.min(contents.length, 27); i++) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                previewInventory.setItem(i, contents[i].clone());
            }
        }

        return previewInventory;
    }
}
