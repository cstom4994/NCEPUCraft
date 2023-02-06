package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("here")
public final class HereCommand extends BasicCommand {

    public static final Component TEXT_C = Component.text(" 向你分享了一个位置: ").color(NamedTextColor.GRAY);

    public HereCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length != 0) return false;
        final Player p = (Player) sender;
        var c = Component.text("[")
                .append(Component.text(p.getWorld().getName()))
                .append(Component.text("]"))
                .append(Component.text(String.format(" %.1f, %.1f, %.1f", p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ())));

        Bukkit.broadcast(p.displayName().color(NamedTextColor.WHITE).append(TEXT_C).append(c.color(NamedTextColor.AQUA)));
        return true;
    }
}

