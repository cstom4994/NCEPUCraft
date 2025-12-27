package net.kaoruxun.ncepucore;

import net.kaoruxun.ncepucore.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 玩家空手下蹲右键实体即可把实体搬起来(作为玩家乘客) 再空手下蹲右键方块/地面放下
@SuppressWarnings("unused")
public final class CarryEntityListener implements Listener {
    private static final double PLACE_RAY_TRACE_DISTANCE = 5.0;

    // player -> carried entity
    private final Map<UUID, UUID> carried = new HashMap<>();

    private boolean isEmptyHands(final Player p) {
        return p.getInventory().getItemInMainHand().getType().isAir()
                && p.getInventory().getItemInOffHand().getType().isAir();
    }

    @Nullable
    private Entity getCarriedEntity(final Player p) {
        final UUID id = carried.get(p.getUniqueId());
        if (id == null) return null;
        final Entity e = Bukkit.getEntity(id);
        if (e == null || !e.isValid() || e.isDead() || e.getVehicle() != p) {
            carried.remove(p.getUniqueId());
            return null;
        }
        return e;
    }

    private void dropHere(final Player p) {
        final Entity e = getCarriedEntity(p);
        if (e == null) return;

        // 先让实体离开玩家 再传送到玩家脚下附近的安全位置
        e.leaveVehicle();

        final Location loc = p.getLocation().clone().add(0.0, 0.1, 0.0);
        final Location safe = Utils.findSafeLocation(loc);
        if (safe != null) {
            e.teleport(safe.add(0.5, 0.0, 0.5));
        } else {
            e.teleport(p.getLocation());
        }
        e.setFallDistance(0);
        carried.remove(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        final Player p = e.getPlayer();
        if (!p.isSneaking()) return;
        if (!isEmptyHands(p)) return;
        if (!p.hasPermission("ncepu.carry")) return;
        if (p.isInsideVehicle()) return;

        // 已经抱着了 就不再抱第二个
        if (getCarriedEntity(p) != null) {
            e.setCancelled(true);
            return;
        }

        final Entity target = e.getRightClicked();
        if (!(target instanceof LivingEntity)) return;
        if (target instanceof Player) return;
        // boss(末影龙/凋灵等) 禁止搬运
        if (target instanceof Boss) return;
        if (!target.isValid() || target.isDead()) return;
        if (target.getVehicle() != null) return;

        // 允许搬起常见生物/动物 让目标成为玩家乘客(坐在头上/肩上效果取决于客户端渲染)
        final boolean ok = p.addPassenger(target);
        if (!ok) return;

        carried.put(p.getUniqueId(), target.getUniqueId());
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        final Player p = e.getPlayer();
        if (!p.isSneaking()) return;
        if (!isEmptyHands(p)) return;
        if (!p.hasPermission("ncepu.carry")) return;

        final Entity carriedEntity = getCarriedEntity(p);
        if (carriedEntity == null) return;

        Block placeBlock = null;
        if (e.getClickedBlock() != null && e.getBlockFace() != null) {
            placeBlock = e.getClickedBlock().getRelative(e.getBlockFace());
        } else {
            final RayTraceResult hit = p.rayTraceBlocks(PLACE_RAY_TRACE_DISTANCE, FluidCollisionMode.NEVER);
            if (hit != null && hit.getHitBlock() != null && hit.getHitBlockFace() != null) {
                placeBlock = hit.getHitBlock().getRelative(hit.getHitBlockFace());
            }
        }

        if (placeBlock == null) {
            e.setCancelled(true);
            return;
        }

        // 必须可穿过 并且上方也要可穿过(避免塞进方块里/空间不足)
        if (!placeBlock.isPassable() || !placeBlock.getRelative(BlockFace.UP).isPassable()) {
            e.setCancelled(true);
            return;
        }

        final Location base = placeBlock.getLocation().add(0.5, 0.0, 0.5);
        final Location safe = Utils.findSafeLocation(base.clone());
        if (safe == null) {
            e.setCancelled(true);
            return;
        }

        carriedEntity.leaveVehicle();
        carriedEntity.teleport(safe.add(0.0, 0.0, 0.0));
        carriedEntity.setFallDistance(0);
        carried.remove(p.getUniqueId());

        e.setCancelled(true);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        dropHere(e.getPlayer());
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent e) {
        dropHere(e.getEntity());
    }
}


