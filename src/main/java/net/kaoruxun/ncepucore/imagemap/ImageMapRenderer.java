package net.kaoruxun.ncepucore.imagemap;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

// 绘制预渲染的128x128图像到地图
public final class ImageMapRenderer extends MapRenderer {
    private final BufferedImage image128;
    private final Set<UUID> renderedFor = Collections.newSetFromMap(new WeakHashMap<>());

    public ImageMapRenderer(BufferedImage image128) {
        super(true);
        this.image128 = image128;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if (player == null) return;
        final UUID uuid = player.getUniqueId();
        if (renderedFor.contains(uuid)) return;
        renderedFor.add(uuid);
        canvas.drawImage(0, 0, image128);
    }
}


