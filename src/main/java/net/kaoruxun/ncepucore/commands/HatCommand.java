package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
        inventory.setHelmet(item.clone());

        //旧帽子放背包,背包满则丢地上
        if (currentHat != null && currentHat.getType() != Material.AIR) {
            if(inventory.firstEmpty() != -1) {
                inventory.addItem(item);
            } else {
                p.getWorld().dropItemNaturally(p.getLocation(), currentHat);
            }
        }

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            inventory.setItemInMainHand(new ItemStack(Material.AIR));
        }

        p.sendMessage("§a已将 " + item.getType().name() + " §a戴在头上!");
        return true;
    }
}

