package net.kaoruxun.ncepucore.inspect;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// 统一的异步写入器 所有 inspect 记录写入都走单线程队列 避免主线程卡顿
public final class InspectAsyncWriter {
    private InspectAsyncWriter() {}

    private static volatile ExecutorService executor;
    private static final AtomicBoolean started = new AtomicBoolean(false);

    public static void start(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        if (!started.compareAndSet(false, true)) return;
        final ThreadFactory tf = r -> {
            final Thread t = new Thread(r, "NCEPU-InspectWriter");
            t.setDaemon(true);
            return t;
        };
        executor = Executors.newSingleThreadExecutor(tf);
    }

    public static void shutdown() {
        started.set(false);
        final ExecutorService ex = executor;
        executor = null;
        if (ex == null) return;
        ex.shutdown();
        try {
            // 给一点时间把队列写完 超过就直接打断退出
            if (!ex.awaitTermination(3, TimeUnit.SECONDS)) {
                ex.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            ex.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void append(Player actor, InspectAction action, org.bukkit.block.Block block) {
        append(actor, action, block, "");
    }

    public static void append(Player actor, InspectAction action, org.bukkit.block.Block block, String extra) {
        if (actor == null || action == null || block == null || block.getWorld() == null) return;
        final ExecutorService ex = executor;
        if (ex == null) {
            // 兜底 如果未启动 直接同步写入
            InspectService.append(block.getWorld(), block.getX(), block.getY(), block.getZ(), actor, action, block.getType(), extra);
            return;
        }
        final var world = block.getWorld();
        final int x = block.getX(), y = block.getY(), z = block.getZ();
        final var mat = block.getType();
        final var uuid = actor.getUniqueId();
        final String name = actor.getName();
        final String exStr = extra == null ? "" : extra;
        ex.execute(() -> InspectService.append(world, x, y, z, uuid, name, action, mat, exStr));
    }
}


