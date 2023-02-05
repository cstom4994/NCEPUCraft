package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CommandName("freeze")
public final class FreezeCommand extends BasicCommand {
    final List<String> LIST = Arrays.asList("true", "false");
    public FreezeCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player)) return false;
        final boolean ai = args.length == 1 && "true".equals(args[0]);
        ((Player) sender).getNearbyEntities(10, 10, 10).forEach(it -> {
            if (it instanceof LivingEntity) ((LivingEntity) it).setAI(ai);
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return args.length == 1 && sender instanceof Player ? LIST : Collections.emptyList();
    }
}
