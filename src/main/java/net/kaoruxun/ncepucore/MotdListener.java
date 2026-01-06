package net.kaoruxun.ncepucore;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.List;

public final class MotdListener implements Listener {
    private final Main instance;

    // 支持占位符: {online} {max} {tps} {mspt} {version}
    private static final List<String> MOTD_LINES = List.of(
            "&aBIGT &7| &f大唐盛世",
            "&7在线: &b{online}&7/&b{max} &7| &aTPS: &f{tps} &7MSPT: &f{mspt}"
    );

    public MotdListener(Main instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPing(ServerListPingEvent event) {
        String motd = String.join("\n", MOTD_LINES);

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


