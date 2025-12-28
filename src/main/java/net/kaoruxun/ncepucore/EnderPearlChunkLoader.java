package net.kaoruxun.ncepucore;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;


// Paper 默认不会让飞行中的实体像原版那样长期保持区块加载
// 这里通过 plugin chunk ticket 的方式 让末影珍珠所在区块持续保持加载 
// 从而实现类似原版末影珍珠长期加载区块(例如气泡柱循环)的效果
public final class EnderPearlChunkLoader implements Listener {
    private final Plugin plugin;

    private static final boolean ENABLED = true;
    private static final int TICK_INTERVAL = 1; // 1=每 tick 跟随一次
    private static final int MAX_PEARLS = 512;  // 防刷上限 同时追踪的末影珍珠数量
    private static final int RADIUS = 0;        // 0=仅当前区块 1=3x3 2=5x5...
    private static final int RESCAN_INTERVAL_TICKS = 20; // 定时重扫 避免漏掉 launch 事件的末影珍珠

    private BukkitTask task;
    private final Map<UUID, TrackedPearl> tracked = new HashMap<>();
    private long tickCounter = 0;

    public static final class DebugEntry {
        public final UUID entityId;
        public final String worldName;
        public final UUID worldId;
        public final int chunkX;
        public final int chunkZ;
        public final double x;
        public final double y;
        public final double z;
        public final String shooterName;
        public final UUID shooterId;
        public final long trackedAtMs;
        public final boolean entityValid;

        DebugEntry(UUID entityId,
                   String worldName,
                   UUID worldId,
                   int chunkX,
                   int chunkZ,
                   double x,
                   double y,
                   double z,
                   String shooterName,
                   UUID shooterId,
                   long trackedAtMs,
                   boolean entityValid) {
            this.entityId = entityId;
            this.worldName = worldName;
            this.worldId = worldId;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.x = x;
            this.y = y;
            this.z = z;
            this.shooterName = shooterName;
            this.shooterId = shooterId;
            this.trackedAtMs = trackedAtMs;
            this.entityValid = entityValid;
        }
    }

    private static final class TrackedPearl {
        final UUID entityId;
        UUID worldId;
        int chunkX;
        int chunkZ;
        String shooterName;
        UUID shooterId;
        long trackedAtMs;

        TrackedPearl(UUID entityId, UUID worldId, int chunkX, int chunkZ) {
            this.entityId = entityId;
            this.worldId = worldId;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.trackedAtMs = System.currentTimeMillis();
        }
    }

    public EnderPearlChunkLoader(Plugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!ENABLED) return;
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, TICK_INTERVAL);
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        // 尽量在不强行加载区块的前提下移除票据
        for (TrackedPearl t : tracked.values()) {
            final World w = Bukkit.getWorld(t.worldId);
            if (w != null) {
                removeTicketsIfLoaded(w, t.chunkX, t.chunkZ);
            }
        }
        tracked.clear();
    }

    public boolean isEnabled() {
        return ENABLED;
    }

    public int getTickInterval() {
        return TICK_INTERVAL;
    }

    public int getMaxPearls() {
        return MAX_PEARLS;
    }

    public int getRadius() {
        return RADIUS;
    }

    public int getTrackedCount() {
        return tracked.size();
    }

    // 获取末影珍珠追踪信息快照(要求在主线程调用) 该方法会读取 Bukkit API(Bukkit.getEntity/World 等) 不适合异步线程
    public List<DebugEntry> snapshotDebug() {
        final List<DebugEntry> out = new ArrayList<>(tracked.size());
        for (TrackedPearl t : tracked.values()) {
            final Entity ent = Bukkit.getEntity(t.entityId);
            final boolean valid = ent instanceof EnderPearl && ent.isValid();
            final World w = Bukkit.getWorld(t.worldId);
            final String worldName = (w == null) ? "unknown" : w.getName();
            double x = 0, y = 0, z = 0;
            int cx = t.chunkX, cz = t.chunkZ;
            if (ent != null) {
                final var loc = ent.getLocation();
                x = loc.getX();
                y = loc.getY();
                z = loc.getZ();
                final var c = loc.getChunk();
                cx = c.getX();
                cz = c.getZ();
            }
            out.add(new DebugEntry(
                    t.entityId,
                    worldName,
                    t.worldId,
                    cx,
                    cz,
                    x, y, z,
                    t.shooterName,
                    t.shooterId,
                    t.trackedAtMs,
                    valid
            ));
        }
        return out;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLaunch(ProjectileLaunchEvent e) {
        if (!ENABLED) return;
        final Projectile p = e.getEntity();
        if (!(p instanceof EnderPearl)) return;
        track((EnderPearl) p);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHit(ProjectileHitEvent e) {
        if (!ENABLED) return;
        final Projectile p = e.getEntity();
        if (!(p instanceof EnderPearl)) return;
        // 命中后通常很快就会被移除 清理由 tick() 或 EntityRemoveFromWorldEvent 兜底处理
    }

    private void track(EnderPearl pearl) {
        if (!pearl.isValid()) return;
        if (tracked.size() >= MAX_PEARLS && !tracked.containsKey(pearl.getUniqueId())) {
            // 防刷保护 达到上限就不再追踪新的(避免恶意用珍珠常驻大量区块)
            return;
        }

        final World w = pearl.getWorld();
        final var c = pearl.getLocation().getChunk();
        final TrackedPearl old = tracked.get(pearl.getUniqueId());
        if (old == null) {
            final TrackedPearl tp = new TrackedPearl(pearl.getUniqueId(), w.getUID(), c.getX(), c.getZ());
            final ProjectileSource shooter = pearl.getShooter();
            if (shooter instanceof Entity) {
                final Entity se = (Entity) shooter;
                tp.shooterId = se.getUniqueId();
                tp.shooterName = se.getName();
            } else if (shooter != null) {
                tp.shooterName = shooter.toString();
            }
            tracked.put(pearl.getUniqueId(), tp);
            addTickets(w, c.getX(), c.getZ());
        } else {
            // 处理极端情况 UUID 复用/跨世界(理论上不会发生 但做一下兜底)
            old.worldId = w.getUID();
            old.chunkX = c.getX();
            old.chunkZ = c.getZ();
            addTickets(w, old.chunkX, old.chunkZ);
        }
    }

    private void untrack(UUID pearlId) {
        final TrackedPearl t = tracked.remove(pearlId);
        if (t == null) return;
        final World w = Bukkit.getWorld(t.worldId);
        if (w != null) {
            removeTicketsIfLoaded(w, t.chunkX, t.chunkZ);
        }
    }

    private void tick() {
        tickCounter++;

        // 定时重扫 把已经存在但未被追踪的末影珍珠补进 tracked(例如发射器/插件生成/事件漏掉等情况)
        if (tickCounter % RESCAN_INTERVAL_TICKS == 0) {
            for (World w : Bukkit.getWorlds()) {
                // 只扫描已加载实体(world 内部只会返回已加载区块中的实体)
                for (EnderPearl pearl : w.getEntitiesByClass(EnderPearl.class)) {
                    track(pearl);
                }
            }
        }

        if (tracked.isEmpty()) return;

        final Iterator<Map.Entry<UUID, TrackedPearl>> it = tracked.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<UUID, TrackedPearl> entry = it.next();
            final UUID id = entry.getKey();
            final TrackedPearl t = entry.getValue();

            final Entity ent = Bukkit.getEntity(id);
            if (!(ent instanceof EnderPearl) || !ent.isValid()) {
                // 珍珠已不存在/无效 释放对应区块票据
                final World w = Bukkit.getWorld(t.worldId);
                if (w != null) {
                    removeTicketsIfLoaded(w, t.chunkX, t.chunkZ);
                }
                it.remove();
                continue;
            }

            final EnderPearl pearl = (EnderPearl) ent;
            final World w = pearl.getWorld();
            final var c = pearl.getLocation().getChunk();

            final int newX = c.getX();
            final int newZ = c.getZ();
            final UUID newWorldId = w.getUID();

            if (t.worldId.equals(newWorldId) && t.chunkX == newX && t.chunkZ == newZ) {
                // still in same chunk
                continue;
            }

            // chunk/world changed: move tickets
            final World oldWorld = Bukkit.getWorld(t.worldId);
            if (oldWorld != null) {
                removeTicketsIfLoaded(oldWorld, t.chunkX, t.chunkZ);
            }

            t.worldId = newWorldId;
            t.chunkX = newX;
            t.chunkZ = newZ;
            addTickets(w, newX, newZ);
        }
    }

    private void addTickets(World w, int chunkX, int chunkZ) {
        // radius=0 只保持珍珠当前区块加载 半径>0 可保持周围区块(可选)
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                final int x = chunkX + dx;
                final int z = chunkZ + dz;
                w.getChunkAt(x, z); // 如未加载会加载 这正是我们要的
                w.addPluginChunkTicket(x, z, plugin);
            }
        }
    }

    private void removeTicketsIfLoaded(World w, int chunkX, int chunkZ) {
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                final int x = chunkX + dx;
                final int z = chunkZ + dz;
                if (!w.isChunkLoaded(x, z)) continue; // 不强制加载 仅在已加载时移除
                w.removePluginChunkTicket(x, z, plugin);
            }
        }
    }
}


