package net.kaoruxun.ncepucore.utils;

import io.netty.buffer.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class Serializer {
    private Serializer() {
    }

    public static ByteBuf createByteBuf() {
        return Unpooled.buffer();
    }

    public static byte[] byteBufToByteArray(ByteBuf buf) {
        if (buf.hasArray()) return buf.array();
        final byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(0, bytes);
        return bytes;
    }

    public static byte[] serializeObject(Serializable obj) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
             ObjectOutputStream oo = new ObjectOutputStream(os)) {
            oo.writeObject(obj);
            oo.flush();
            return os.toByteArray();
        }
    }

    public static Serializable deserializeObject(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
             ObjectInputStream oi = new ObjectInputStream(bi)) {
            return (Serializable) oi.readObject();
        }
    }

    public static void writeString(ByteBuf buf, CharSequence str) {
        buf.writeInt(ByteBufUtil.utf8Bytes(str));
        ByteBufUtil.writeUtf8(buf, str);
    }

    public static String readString(ByteBuf buf) {
        return buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8).toString();
    }

    public static byte[] serializeLocation(Location loc) {
        final ByteBuf buf = createByteBuf();
        writeString(buf, loc.getWorld().getName());
        buf.writeInt(loc.getBlockX());
        buf.writeInt(loc.getBlockY());
        buf.writeInt(loc.getBlockZ());
        buf.writeFloat(loc.getYaw());
        buf.writeFloat(loc.getPitch());
        return byteBufToByteArray(buf);
    }

    public static Location deserializeLocation(byte[] bytes) {
        final ByteBuf buf = Unpooled.wrappedBuffer(bytes);
        return new Location(
                Bukkit.getWorld(readString(buf)),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readFloat(),
                buf.readFloat()
        );
    }
}
