package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.Pair;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

@CommandName("afk")
public final class AfkCommand extends TargetCommand implements Listener {
    private final int TIME = 5 * 60 * 1000;

    public AfkCommand(Main main) {
        super(main);
        main.getServer().getScheduler().runTaskTimer(main, (Runnable) this::syncStatus, 100, 100);
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    @Override
    public void doAction(CommandSender sender, Player p, boolean now) {
        Pair<Location, Long> it = instance.afkPlayers.get(p);
        final long time = System.currentTimeMillis();
        if (it == null) {
            it = new Pair<>(p.getLocation(), time - TIME);
            instance.afkPlayers.put(p, it);
        } else it.right = it.right > time ? time - TIME : time + TIME;
        syncStatus(p, time);
        sender.sendMessage("§a设置成功!");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getPlayer().getInventory().getItemInMainHand().getType() != Material.FISHING_ROD)
            renewStatus(e.getPlayer());
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e) {
        renewStatus(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerPostRespawnEvent e) {
        renewStatus(e.getPlayer());
    }

    @EventHandler
    public void onHeld(PlayerItemHeldEvent e) {
        renewStatus(e.getPlayer());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        renewStatus(e.getPlayer());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        renewStatus(e.getPlayer());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        renewStatus(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerFishEvent e) {
        renewStatus(e.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        renewStatus(e.getPlayer());
        syncStatus(e.getPlayer());
    }

    private void renewStatus(Player p) {
        Pair<Location, Long> it = instance.afkPlayers.get(p);
        if (it == null) {
            it = new Pair<>(p.getLocation(), System.currentTimeMillis() + TIME);
            instance.afkPlayers.put(p, it);
        } else it.right = System.currentTimeMillis() + TIME;
    }

    private void syncStatus(final Player p) {
        syncStatus(p, System.currentTimeMillis());
    }

    private void syncStatus(final Player p, final long time) {
        Pair<Location, Long> it = instance.afkPlayers.get(p);
        final Location loc = p.getLocation();
        if (it == null) {
            it = new Pair<>(loc, time + TIME);
            instance.afkPlayers.put(p, it);
        } else if (loc.getWorld() != it.left.getWorld() || loc.distance(it.left) > 0.0001) {
            it.left = loc;
            it.right = time + TIME;
        }

        if (it.right > time) {
            if (p.getPlayerListName().startsWith("§7")) {
                p.setPlayerListName(null);
                p.sendMessage("§7你回来了.");
            }
        } else {
            if (!p.getPlayerListName().startsWith("§7")) {
                p.setPlayerListName("§7" + p.getName());
                p.sendMessage("§7你因为长时间没有移动而进入了离开模式.");
            }
        }
    }

    private void syncStatus() {
        final long time = System.currentTimeMillis();
        instance.getServer().getOnlinePlayers().forEach(p -> syncStatus(p, time));
    }
}
