package net.kaoruxun.ncepucore.imagemap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("deprecation")
public final class ImageMapService {
    private static final int STORE_VERSION = 1;

    private final JavaPlugin plugin;
    private final File storeFile;
    private final File cacheDir;

    private final Map<Integer, Entry> entries = new HashMap<>();

    public ImageMapService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.storeFile = new File(plugin.getDataFolder(), "imagemaps.yml");
        this.cacheDir = new File(plugin.getDataFolder(), "imagemap-cache");
        if (!cacheDir.exists()) cacheDir.mkdirs();
    }

    public void loadAndRegisterAll() {
        load();

        int ok = 0;
        final Iterator<Map.Entry<Integer, Entry>> it = entries.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<Integer, Entry> pair = it.next();
            final int mapId = pair.getKey();
            final Entry e = pair.getValue();

            final MapView view = Bukkit.getMap(mapId);
            if (view == null) {
                plugin.getLogger().warning("[imgmap] 地图不存在 mapId=" + mapId + "，已从持久化记录移除");
                it.remove();
                continue;
            }

            final File cached = cacheFile(e.sha256);
            if (!cached.exists()) {
                plugin.getLogger().warning("[imgmap] 缓存缺失 mapId=" + mapId + " sha256=" + e.sha256 + " url=" + e.url);
                continue;
            }
            try {
                final BufferedImage img = ImageIO.read(cached);
                if (img == null) {
                    plugin.getLogger().warning("[imgmap] 缓存图片无法读取 mapId=" + mapId + " file=" + cached.getName());
                    continue;
                }
                attachRenderer(view, img);
                ok++;
            } catch (IOException ex) {
                plugin.getLogger().log(Level.WARNING, "[imgmap] 读取缓存失败 mapId=" + mapId + " file=" + cached.getName(), ex);
            }
        }

        // 如果移除了缺失的地图 持久化清理
        save();
        plugin.getLogger().info("[imgmap] 已恢复 " + ok + " 张 URL 地图");
    }

    public synchronized void registerNew(int mapId, Entry entry, byte[] png128) throws IOException {
        final File f = cacheFile(entry.sha256);
        if (!f.exists()) {
            Files.write(f.toPath(), png128);
        }
        entries.put(mapId, entry);
        save();
    }

    public synchronized Entry getEntry(int mapId) {
        return entries.get(mapId);
    }

    public void shutdown() {
        save();
    }

    private File cacheFile(String sha256) {
        return new File(cacheDir, sha256 + ".png");
    }

    private synchronized void load() {
        entries.clear();
        if (!storeFile.exists()) return;
        final FileConfiguration cfg = YamlConfiguration.loadConfiguration(storeFile);
        final int ver = cfg.getInt("version", 0);
        if (ver != STORE_VERSION) {
            plugin.getLogger().warning("[imgmap] imagemaps.yml 版本不匹配 ver=" + ver + "，仍尝试读取");
        }
        final Object section = cfg.get("maps");
        if (!(section instanceof org.bukkit.configuration.ConfigurationSection mapsSec)) return;

        for (String key : mapsSec.getKeys(false)) {
            try {
                final int mapId = Integer.parseInt(key);
                final String url = mapsSec.getString(key + ".url", "");
                final String sha = mapsSec.getString(key + ".sha256", "");
                final String title = mapsSec.getString(key + ".title", "");
                final String createdBy = mapsSec.getString(key + ".createdBy", "");
                final long createdAt = mapsSec.getLong(key + ".createdAt", 0L);
                if (url.isBlank() || sha.isBlank()) continue;
                entries.put(mapId, new Entry(url, sha, title, createdBy, createdAt));
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "[imgmap] 读取记录失败 key=" + key, t);
            }
        }
    }

    private synchronized void save() {
        final YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("version", STORE_VERSION);
        for (Map.Entry<Integer, Entry> it : entries.entrySet()) {
            final String k = String.valueOf(it.getKey());
            final Entry e = it.getValue();
            cfg.set("maps." + k + ".url", e.url);
            cfg.set("maps." + k + ".sha256", e.sha256);
            cfg.set("maps." + k + ".title", e.title);
            cfg.set("maps." + k + ".createdBy", e.createdBy);
            cfg.set("maps." + k + ".createdAt", e.createdAt);
        }
        try {
            cfg.save(storeFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "[imgmap] 保存 imagemaps.yml 失败", e);
        }
    }

    public static void attachRenderer(MapView map, BufferedImage image128) {
        // 擦除现有的渲染器 避免vanilla更新
        for (MapRenderer r : new ArrayList<>(map.getRenderers())) {
            map.removeRenderer(r);
        }
        map.addRenderer(new ImageMapRenderer(image128));
        map.setLocked(true);
    }

    public static MapView createNewMap(World world) {
        return Bukkit.createMap(Objects.requireNonNull(world, "world"));
    }

    public record Entry(String url, String sha256, String title, String createdBy, long createdAt) {}
}


