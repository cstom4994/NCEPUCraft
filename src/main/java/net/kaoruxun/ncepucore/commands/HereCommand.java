package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("here")
public final class HereCommand extends BasicCommand {

    public static final Component TEXT_C = Component.text(" 分享了一个位置: ").color(NamedTextColor.GRAY);

    public HereCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length > 1) return false;
        final Player p = (Player) sender;

        var c = Component.text("[")
                .append(Component.text(p.getWorld().getName()))
                .append(Component.text("]"))
                .append(Component.text(String.format(" x:%.1f y:%.1f z:%.1f", p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ())));

        if (args.length == 0) {
            Bukkit.broadcast(p.displayName().color(NamedTextColor.WHITE).append(TEXT_C).append(c.color(NamedTextColor.AQUA)));
        } else {
            final Player hp = instance.getServer().getPlayerExact(args[0]);
            if (hp == null) {
                p.sendMessage(Component.text(ChatColor.RED + "未知玩家"));
                return true;
            }
            hp.sendMessage(p.displayName().color(NamedTextColor.WHITE).append(Component.text(" 悄悄地向你")).append(TEXT_C).append(c.color(NamedTextColor.AQUA)));
            p.sendMessage(Component.text("你悄悄地向 ").append(hp.displayName()).append(TEXT_C).append(c.color(NamedTextColor.AQUA)));
        }
        return true;
    }
}

