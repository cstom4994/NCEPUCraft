package net.kaoruxun.ncepucore.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author RukiaOvO
 * @date 2025/12/31
 * @description scoreboard listener
 */
public class ScoreBoardListener implements Listener {

    private final ScoreBoardService scoreBoardService;

    public ScoreBoardListener(ScoreBoardService scoreBoardService) {
        this.scoreBoardService = scoreBoardService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if(scoreBoardService.isEnabled(p)) {
            scoreBoardService.createForJoinPlayer(p);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if(scoreBoardService.isEnabled(p)) {
            scoreBoardService.removeScoreboard(p);
        }
    }
}
