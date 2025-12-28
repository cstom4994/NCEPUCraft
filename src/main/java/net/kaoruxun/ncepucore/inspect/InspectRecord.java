package net.kaoruxun.ncepucore.inspect;

import org.bukkit.Material;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 存储格式 每条记录一行 字段用 ';' 分隔
// ts;uuid;name;action;material;extra?
public final class InspectRecord {
    public final long tsMillis;
    public final UUID playerId;
    public final String playerName;
    public final InspectAction action;
    public final Material material;
    public final String extra; // 可选 容器变更详情等 不能包含 ';'

    public InspectRecord(long tsMillis, UUID playerId, String playerName, InspectAction action, Material material, String extra) {
        this.tsMillis = tsMillis;
        this.playerId = playerId;
        this.playerName = playerName;
        this.action = action;
        this.material = material;
        this.extra = extra;
    }

    public String encodeLine() {
        final String ex = extra == null ? "" : extra.replace(";", ",");
        return tsMillis + ";" + playerId + ";" + playerName + ";" + action.name() + ";" + material.name() + ";" + ex;
    }

    public static InspectRecord decodeLine(String line) {
        final String[] parts = line.split(";", -1);
        if (parts.length < 5) return null; // 兼容旧格式
        try {
            long ts = Long.parseLong(parts[0]);
            UUID uuid = UUID.fromString(parts[1]);
            String name = parts[2];
            InspectAction action = InspectAction.valueOf(parts[3]);
            Material mat = Material.valueOf(parts[4]);
            String extra = parts.length >= 6 ? parts[5] : "";
            return new InspectRecord(ts, uuid, name, action, mat, extra);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static byte[] encodeList(List<InspectRecord> records) {
        if (records == null || records.isEmpty()) return new byte[0];
        final StringBuilder sb = new StringBuilder();
        for (InspectRecord r : records) {
            if (r == null) continue;
            sb.append(r.encodeLine()).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static List<InspectRecord> decodeList(byte[] bytes, int limit) {
        final ArrayList<InspectRecord> out = new ArrayList<>();
        if (bytes == null || bytes.length == 0) return out;
        final String s = new String(bytes, StandardCharsets.UTF_8);
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (line == null || line.isEmpty()) continue;
            final InspectRecord r = decodeLine(line.trim());
            if (r != null) out.add(r);
            if (limit > 0 && out.size() >= limit) break;
        }
        return out;
    }
}


