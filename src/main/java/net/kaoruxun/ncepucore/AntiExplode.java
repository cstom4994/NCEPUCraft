package net.kaoruxun.ncepucore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

final class AntiExplode implements CommandExecutor, Listener {
    private boolean flag = false;
    @SuppressWarnings({"NullableProblems", "deprecation"})
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (flag) {
            flag = false;
            if (sender.isOp()) sender.sendMessage("§a当前服务器的TNT爆炸已开启!");
            else Bukkit.broadcastMessage("§a当前服务器的TNT爆炸已开启!");
        } else {
            flag = true;
            if (sender.isOp()) sender.sendMessage("§e当前服务器的TNT爆炸已关闭!");
            else Bukkit.broadcastMessage("§e当前服务器的TNT爆炸已关闭!");
        }
        return true;
    }

    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent e) {
        if (flag) e.blockList().clear();
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onEntitySpawn(final EntitySpawnEvent e) {
        if (flag && (e.getEntityType() == EntityType.WITHER || e.getEntityType() == EntityType.ENDER_DRAGON)) {
            e.setCancelled(true);
            Location loc = e.getLocation();
            Bukkit.broadcastMessage("§c" + loc.getNearbyPlayers(16).stream().findFirst()
                    .map(HumanEntity::getName).orElse("有人") + "尝试在 (" + loc.getWorld().getName() + ", " +
                    loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ") 生成凋零或末影龙!");
        }
    }
}
