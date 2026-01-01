package net.kaoruxun.ncepucore.shulkerboxpreview;

import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;


/**
 * @author RukiaOvO
 * @date 2025/12/31
 * @description
 */
public class ShulkerBoxPreviewUtils {
    public static ShulkerBox toShulkerBox(ItemStack clickItem) {
        if (clickItem == null) return null;

        ItemMeta meta = clickItem.getItemMeta();
        if (meta instanceof BlockStateMeta blockStateMeta &&
                blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox) {
            return shulkerBox;
        }
        return null;
    }
}
