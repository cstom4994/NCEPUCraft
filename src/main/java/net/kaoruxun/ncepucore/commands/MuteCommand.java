package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.DatabaseSingleton;
import net.kaoruxun.ncepucore.utils.Serializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.IOException;
import java.util.HashSet;

@CommandName("mute")
public final class MuteCommand extends BasicCommand implements Listener {
    private final static String MUTED_LIST = "mutedList";

    @SuppressWarnings("unchecked")
    public MuteCommand(Main main) {
        super(main);
        final byte[] bytes = DatabaseSingleton.INSTANCE.get(MUTED_LIST);
        if (bytes != null) try {
            main.mutedPlayers.addAll((HashSet<String>) Serializer.deserializeObject(bytes));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (instance.mutedPlayers.contains(e.getPlayer().getUniqueId().toString())) {
            e.setCancelled(true);
            e.getPlayer().sendActionBar("§c发送失败! 您已被禁言!");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        final OfflinePlayer o = instance.getServer().getOfflinePlayer(args[0]);
        if (!o.hasPlayedBefore()) {
            sender.sendMessage("§c该玩家还从未在服务器游玩过!");
            return true;
        }
        final String id = o.getUniqueId().toString();
        final Player p = o.getPlayer();
        if (instance.mutedPlayers.contains(id)) {
            instance.mutedPlayers.remove(id);
            if (p != null) p.sendMessage("§a您已被解除禁言!");
        } else {
            instance.mutedPlayers.add(id);
            if (p != null) p.sendMessage("§c您已被禁言!");
        }
        try {
            DatabaseSingleton.INSTANCE.set(MUTED_LIST, Serializer.serializeObject(instance.mutedPlayers));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
