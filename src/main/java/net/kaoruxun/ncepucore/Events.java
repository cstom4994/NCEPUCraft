package net.kaoruxun.ncepucore;

import net.kaoruxun.ncepucore.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

@SuppressWarnings("unused")
final class Events implements Listener {
    final private Main instance;

    Events(Main main) {
        instance = main;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        final Player p = e.getPlayer();
        if (instance.countdowns.containsKey(p) && e.getFrom().distance(e.getTo()) > 0) e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Utils.recordPlayerLocation(e.getEntity());
    }
}
