package net.kaoruxun.ncepucore;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MotdListener implements Listener {
    private final Main instance;

    public MotdListener(Main instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPing(ServerListPingEvent event) {
        if (!instance.getConfig().getBoolean("motd.enabled", true)) return;

        final boolean random = instance.getConfig().getBoolean("motd.random", false);
        String motd;

        if (random) {
            final List<String> messages = instance.getConfig().getStringList("motd.messages");
            if (messages == null || messages.isEmpty()) return;
            motd = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
        } else {
            final List<String> lines = instance.getConfig().getStringList("motd.lines");
            if (lines == null || lines.isEmpty()) return;
            motd = String.join("\n", lines);
        }

        motd = applyPlaceholders(motd, event);
        motd = ChatColor.translateAlternateColorCodes('&', motd);
        event.setMotd(motd);
    }

    private String applyPlaceholders(String motd, ServerListPingEvent event) {
        final int online = Bukkit.getOnlinePlayers().size();
        final int max = event.getMaxPlayers();

        final String tps = String.format("%.1f", instance.getLastTps());
        final String mspt = String.format("%.1f", instance.getLastMspt());

        return motd
                .replace("\\n", "\n")
                .replace("{online}", String.valueOf(online))
                .replace("{max}", String.valueOf(max))
                .replace("{tps}", tps)
                .replace("{mspt}", mspt)
                .replace("{version}", Bukkit.getVersion());
    }
}


