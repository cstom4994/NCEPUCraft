package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.imagemap.ImageMapService;
import net.kaoruxun.ncepucore.imagemap.UrlImageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@CommandName("imgmap")
public final class ImageMapCommand extends BasicCommand {
    // hard limits to protect server
    private static final int MAX_IMAGE_BYTES = 15 * 1024 * 1024; // 15MB
    private static final int CONNECT_TIMEOUT_MS = 8_000;
    private static final int READ_TIMEOUT_MS = 12_000;

    public ImageMapCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return false;
        final Player p = (Player) sender;
        if (args.length < 1) return false;

        final String url = args[0];
        int cols = 1;
        int rows = 1;
        int titleStartIdx = 1;
        if (args.length >= 3) {
            final Integer c = tryParseInt(args[1]);
            final Integer r = tryParseInt(args[2]);
            if (c != null && r != null) {
                cols = clamp(c, 1, 10);
                rows = clamp(r, 1, 10);
                titleStartIdx = 3;
            }
        }
        final String title = (args.length > titleStartIdx)
                ? String.join(" ", Arrays.copyOfRange(args, titleStartIdx, args.length))
                : (cols == 1 && rows == 1 ? "§eURL 图片地图" : ("§eURL 图片地图 §7(" + cols + "x" + rows + ")"));
        final int fCols = cols;
        final int fRows = rows;
        final String fTitle = title;

        try {
            UrlImageUtils.validateHttpUrl(url);
        } catch (IllegalArgumentException e) {
            p.sendMessage("§c" + e.getMessage());
            return true;
        }

        p.sendMessage("§7正在下载图片并生成地图 请稍候... §8(" + fCols + "x" + fRows + ")");

        // Async download + decode + resize
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                UrlImageUtils.rejectPrivateAddress(url);
                final UrlImageUtils.DownloadResult downloaded = UrlImageUtils.downloadImageBytes(url, MAX_IMAGE_BYTES, CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
                // 快速判断 URL 实际返回了网页/不支持格式
                if (downloaded.contentType() != null && !downloaded.contentType().toLowerCase().startsWith("image/")) {
                    throw new IOException("URL 返回的不是图片 content-type=" + downloaded.contentType());
                }
                if (UrlImageUtils.looksLikeHtml(downloaded.bytes())) {
                    throw new IOException("URL 返回了网页内容（可能被防盗链/403/重定向拦截）");
                }
                if (UrlImageUtils.looksLikeWebp(downloaded.bytes())) {
                    throw new IOException("图片为WebP请换PNG/JPG");
                }

                final BufferedImage decoded = UrlImageUtils.decodeImage(downloaded.bytes());
                final int targetW = fCols * UrlImageUtils.MAP_SIZE;
                final int targetH = fRows * UrlImageUtils.MAP_SIZE;
                final BufferedImage scaled = UrlImageUtils.resizeContain(decoded, targetW, targetH, java.awt.Color.BLACK);

                // Back to main thread: create map + renderer + give item + persist
                Bukkit.getScheduler().runTask(instance, () -> {
                    if (!p.isOnline()) return;
                    try {
                        final ImageMapService service = getService();
                        int created = 0;
                        for (int y = 0; y < fRows; y++) {
                            for (int x = 0; x < fCols; x++) {
                                final BufferedImage tile = new BufferedImage(UrlImageUtils.MAP_SIZE, UrlImageUtils.MAP_SIZE, BufferedImage.TYPE_INT_ARGB);
                                final java.awt.Graphics2D g = tile.createGraphics();
                                try {
                                    g.drawImage(
                                            scaled,
                                            0, 0, UrlImageUtils.MAP_SIZE, UrlImageUtils.MAP_SIZE,
                                            x * UrlImageUtils.MAP_SIZE, y * UrlImageUtils.MAP_SIZE,
                                            (x + 1) * UrlImageUtils.MAP_SIZE, (y + 1) * UrlImageUtils.MAP_SIZE,
                                            null
                                    );
                                } finally {
                                    g.dispose();
                                }

                                final byte[] png128 = UrlImageUtils.encodePng(tile);
                                final String tileSha = UrlImageUtils.sha256Hex(png128);

                                final MapView map = ImageMapService.createNewMap(p.getWorld());
                                ImageMapService.attachRenderer(map, tile);

                                final ItemStack item = new ItemStack(Material.FILLED_MAP, 1);
                                final MapMeta meta = (MapMeta) item.getItemMeta();
                                Objects.requireNonNull(meta);
                                meta.setMapView(map);
                                meta.setDisplayName(fCols == 1 && fRows == 1 ? fTitle : (fTitle + " §7[" + (x + 1) + "," + (y + 1) + "]"));
                                meta.setColor(Color.YELLOW);
                                item.setItemMeta(meta);

                                final var leftover = p.getInventory().addItem(item);
                                if (!leftover.isEmpty()) {
                                    p.getWorld().dropItemNaturally(p.getLocation(), item);
                                    p.sendMessage("§e背包已满 地图已掉落在脚下。");
                                }

                                // persist (each tile is its own cached png)
                                try {
                                    service.registerNew(
                                            map.getId(),
                                            new ImageMapService.Entry(url, tileSha, meta.getDisplayName(), p.getUniqueId().toString(), System.currentTimeMillis()),
                                            png128
                                    );
                                } catch (IOException ex) {
                                    instance.getLogger().warning("[imgmap] 持久化失败 mapId=" + map.getId() + " err=" + ex.getMessage());
                                    p.sendMessage("§e已生成地图 mapId=" + map.getId() + " 但持久化失败（重启后可能丢失） 请查看日志。");
                                }

                                created++;
                            }
                        }

                        p.sendMessage("§a生成成功! §f共生成 " + created + " 张地图。");
                    } catch (Throwable t) {
                        instance.getLogger().warning("[imgmap] 创建地图失败: " + t.getMessage());
                        t.printStackTrace();
                        p.sendMessage("§c创建地图失败 请查看服务器日志。");
                    }
                });
            } catch (IOException ex) {
                Bukkit.getScheduler().runTask(instance, () -> {
                    if (!p.isOnline()) return;
                    p.sendMessage("§c下载/解析图片失败: §f" + ex.getMessage());
                });
            } catch (Throwable t) {
                Bukkit.getScheduler().runTask(instance, () -> {
                    if (!p.isOnline()) return;
                    p.sendMessage("§c生成失败 请查看服务器日志。");
                });
                instance.getLogger().warning("[imgmap] 未知错误: " + t.getMessage());
                t.printStackTrace();
            }
        });

        return true;
    }

    private ImageMapService getService() {
        final ImageMapService service = instance.getImageMapService();
        if (service == null) throw new IllegalStateException("ImageMapService 未初始化");
        return service;
    }

    private static Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}


