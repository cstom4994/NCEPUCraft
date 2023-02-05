package net.kaoruxun.ncepucore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

final class ShowItem implements CommandExecutor {
    private final static Component TEXT_C = Component.text(" 向你展示了一个物品: ").color(NamedTextColor.GRAY);

    @SuppressWarnings({ "NullableProblems" })
    @Override
    public boolean onCommand(final CommandSender s, final Command command, final String label, final String[] args) {
        if (!(s instanceof final Player p)) return false;
        final ItemStack i = p.getInventory().getItemInMainHand();
        if (i.getType() == Material.AIR) {
            s.sendMessage("§c你的手里没有物品.");
            return true;
        }

        final var im = i.getItemMeta();
        var displayName = im.displayName();
        var c = Component.text("[")
                .append(displayName == null ? Component.translatable((i.getType().isBlock() ? "block." : "item.") + i.getType().getKey()
                        .toString().replace(':', '.')) : displayName.style(Style.style(TextDecoration.ITALIC)))
                .append(Component.text("]"));
        if (i.getAmount() > 1) c = c.append(Component.text("x" + i.getAmount()));

        Bukkit.broadcast(p.displayName().color(NamedTextColor.WHITE).append(TEXT_C).append(c.color(NamedTextColor.AQUA))
                .hoverEvent(i.asHoverEvent()));
        return true;
    }
}
