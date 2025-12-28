package net.kaoruxun.ncepucore.inspect;

import net.kaoruxun.ncepucore.utils.DatabaseSingleton;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.iq80.leveldb.DBIterator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class InspectService {
    private static final String KEY_PREFIX = "inspect.";
    private static final int MAX_RECORDS_PER_BLOCK = 20;
    private static final byte[] KEY_PREFIX_BYTES = KEY_PREFIX.getBytes(StandardCharsets.UTF_8);

    private InspectService() {}

    public static String key(World world, int x, int y, int z) {
        final UUID wid = world.getUID();
        return KEY_PREFIX + wid + "." + x + "." + y + "." + z;
    }

    public static String key(Location loc) {
        final World w = loc.getWorld();
        if (w == null) throw new IllegalArgumentException("location world is null");
        return key(w, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static void append(World world, int x, int y, int z, Player actor, InspectAction action, Material material) {
        append(world, x, y, z, actor, action, material, "");
    }

    public static void append(World world, int x, int y, int z, Player actor, InspectAction action, Material material, String extra) {
        append(world, x, y, z, actor.getUniqueId(), actor.getName(), action, material, extra);
    }

    public static void append(World world, int x, int y, int z, UUID actorId, String actorName, InspectAction action, Material material, String extra) {
        final String k = key(world, x, y, z);
        final byte[] existing = DatabaseSingleton.INSTANCE.get(k);
        final List<InspectRecord> list = InspectRecord.decodeList(existing, MAX_RECORDS_PER_BLOCK);

        // 新记录放在最前面
        final ArrayList<InspectRecord> out = new ArrayList<>(Math.min(MAX_RECORDS_PER_BLOCK, list.size() + 1));
        out.add(new InspectRecord(System.currentTimeMillis(), actorId, actorName, action, material, extra));
        for (InspectRecord r : list) {
            if (out.size() >= MAX_RECORDS_PER_BLOCK) break;
            out.add(r);
        }

        DatabaseSingleton.INSTANCE.set(k, InspectRecord.encodeList(out));
    }

    public static List<InspectRecord> query(Location loc, int limit) {
        final String k = key(loc);
        final byte[] bytes = DatabaseSingleton.INSTANCE.get(k);
        return InspectRecord.decodeList(bytes, limit);
    }

    public static final class CleanupResult {
        public long scannedKeys;
        public long updatedKeys;
        public long deletedKeys;
        public long removedRecords;
        public long keptRecords;
    }

    // 清理所有 inspect.* 记录 移除早于 cutoffMillis 的记录 只会裁剪每个方块的记录列表 不会影响其他数据
    public static CleanupResult cleanupOlderThan(long cutoffMillis) {
        final CleanupResult result = new CleanupResult();
        DBIterator it = null;
        try {
            it = DatabaseSingleton.INSTANCE.iterator();
            for (it.seek(KEY_PREFIX_BYTES); it.hasNext(); it.next()) {
                final Map.Entry<byte[], byte[]> e = it.peekNext();
                final byte[] k = e.getKey();
                if (!startsWith(k, KEY_PREFIX_BYTES)) break;
                result.scannedKeys++;

                final byte[] v = e.getValue();
                final List<InspectRecord> list = InspectRecord.decodeList(v, 0);
                if (list.isEmpty()) continue;

                final ArrayList<InspectRecord> kept = new ArrayList<>(list.size());
                for (InspectRecord r : list) {
                    if (r == null) continue;
                    if (r.tsMillis >= cutoffMillis) kept.add(r);
                    else result.removedRecords++;
                }
                result.keptRecords += kept.size();

                if (kept.size() == list.size()) continue; // 无变化

                if (kept.isEmpty()) {
                    DatabaseSingleton.INSTANCE.delete(k);
                    result.deletedKeys++;
                } else {
                    DatabaseSingleton.INSTANCE.set(new String(k, StandardCharsets.UTF_8), InspectRecord.encodeList(kept));
                    result.updatedKeys++;
                }
            }
        } finally {
            if (it != null) {
                try {
                    it.close();
                } catch (IOException ignored) {
                }
            }
        }
        return result;
    }

    private static boolean startsWith(byte[] a, byte[] prefix) {
        if (a == null || prefix == null) return false;
        if (a.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (a[i] != prefix[i]) return false;
        }
        return true;
    }
}


