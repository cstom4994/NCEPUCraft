package net.kaoruxun.ncepucore;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

final class RedStoneDetection implements Listener, CommandExecutor {
    private final Main plugin;
    private BukkitTask task;
    private boolean started = false;
    private Player player;
    final private HashMap<Location, Integer> redStoneRecord = new HashMap<>();

    RedStoneDetection(Main plugin) {
        this.plugin = plugin;
    }

    private void start() {
        if (started || player == null) return;
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, redStoneRecord::clear, 5 * 20L, 5 * 20L);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        started = true;
    }

    private void stop() {
        if (!started) return;
        BlockRedstoneEvent.getHandlerList().unregister(this);
        task.cancel();
        started = false;
        player = null;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onBlockRedStone(final BlockRedstoneEvent e) {
        final Location loc = e.getBlock().getLocation();
        int i = 0;
        try {
            i = redStoneRecord.get(loc);
        } catch (Exception ignored) { }
        redStoneRecord.put(loc, i == 0 ? (i = 1) : ++i);
        if (i >= 30) {
            if (player != null) {
                final TextComponent c = new TextComponent("§c高频红石: §e(" +
                        loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," +
                        loc.getBlockZ() + ")");
                c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + loc.getBlockX() +
                        " " + loc.getBlockY() + " " + loc.getBlockZ()));
                player.sendMessage(c);
            }
            redStoneRecord.remove(loc);
        }
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        if (e.getPlayer() == player) stop();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) return true;
        if (started) {
            sender.sendMessage("§e高频红石检测§c已停止");
            if (player != null && player != sender) player.sendMessage("§e高频红石检测§c已停止");
            stop();
        } else {
            player = (Player) sender;
            sender.sendMessage("§e高频红石检测§a进行中");
            start();
        }
        return true;
    }
}
