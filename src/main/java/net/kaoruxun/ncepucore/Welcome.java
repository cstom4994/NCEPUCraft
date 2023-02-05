package net.kaoruxun.ncepucore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class Welcome implements CommandExecutor {
    @SuppressWarnings({"NullableProblems", "deprecation"})
    @Override
    public boolean onCommand(final CommandSender s, final Command command, final String label, final String[] args) {
        if (!(s instanceof Player)) return false;
        Bukkit.broadcastMessage("§f" + s.getName() + "§7: 欢迎新dalao!");
        return true;
    }
}
