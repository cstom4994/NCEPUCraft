package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

/**
 * @author RukiaOvO
 * @date 2025/12/29
 * @description allow players to hat the items on hand
 */

@CommandName("hat")
public final class HatCommand extends BasicCommand {
    public HatCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        //限定非旁观者玩家可使用
        if (!(sender instanceof Player)) return false;
        final Player p = (Player) sender;
        if(p.getGameMode() == GameMode.SPECTATOR) {
            p.sendActionBar("§c请不要在旁观者模式穿戴!");
            return true;
        }

        PlayerInventory inventory = p.getInventory();
        ItemStack item = inventory.getItemInMainHand();
        ItemStack currentHat = inventory.getHelmet();
        if (item.getType() == Material.AIR) {
            p.sendMessage("§c你手中没有物品!");
            return true;
        }

        // 只取1个物品作为帽子 避免把整组物品戴在头上导致数量异常
        ItemStack newHat = item.clone();
        newHat.setAmount(1);
        inventory.setHelmet(newHat);

        // 先消耗手上物品 再处理旧帽子 手上只有1个时直接交换 避免背包满导致丢失
        if (item.getAmount() <= 1) {
            if (currentHat != null && currentHat.getType() != Material.AIR) {
                inventory.setItemInMainHand(currentHat);
            } else {
                inventory.setItemInMainHand(new ItemStack(Material.AIR));
            }
        } else {
            item.setAmount(item.getAmount() - 1);
            inventory.setItemInMainHand(item);

            // 旧帽子放背包 背包满则丢地上 这里必须放currentHat不能放item否则会复制
            if (currentHat != null && currentHat.getType() != Material.AIR) {
                Map<Integer, ItemStack> leftovers = inventory.addItem(currentHat);
                for (ItemStack leftover : leftovers.values()) {
                    p.getWorld().dropItemNaturally(p.getLocation(), leftover);
                }
            }
        }

        p.sendMessage("§a已将 " + newHat.getType().name() + " §a戴在头上!");
        return true;
    }
}

