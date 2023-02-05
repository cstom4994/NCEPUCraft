package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@CommandName("disrobe")
public final class DisrobeCommand extends TargetCommand {
    public DisrobeCommand(final Main main) {
        super(main);
    }

    @Override
    public void doAction(final CommandSender sender, final Player p, boolean now) {
        final PlayerInventory pi = p.getInventory();
        final World world = p.getWorld();
        final Location loc = p.getLocation();
        for (final ItemStack it : pi.getArmorContents()) if (it != null) world.dropItem(loc, it);
        pi.setArmorContents(new ItemStack[4]);
        p.updateInventory();
        sender.sendMessage("§a操作成功!");
    }
}
