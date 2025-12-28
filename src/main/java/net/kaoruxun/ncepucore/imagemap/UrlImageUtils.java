package net.kaoruxun.ncepucore.imagemap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.GZIPInputStream;

public final class UrlImageUtils {
    private UrlImageUtils() {}

    public static final int MAP_SIZE = 128;

    public static void validateHttpUrl(String url) throws IllegalArgumentException {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("URL 不能为空");
        if (url.length() > 2048) throw new IllegalArgumentException("URL 过长");
        final URI uri = URI.create(url);
        final String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("只允许 http/https URL");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("URL host 无效");
        }
    }

    // 基本SSRF保护 禁止访问内网/本机地址
    public static void rejectPrivateAddress(String url) throws IOException {
        final URI uri = URI.create(url);
        final String host = uri.getHost();
        if (host == null) throw new IOException("无法解析 host");
        final InetAddress addr = InetAddress.getByName(host);
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress() || addr.isLinkLocalAddress() || addr.isSiteLocalAddress()) {
            throw new IOException("禁止访问内网/本机地址");
        }
    }

    public static DownloadResult downloadImageBytes(String url, int maxBytes, int connectTimeoutMs, int readTimeoutMs) throws IOException {
        final URL u = URI.create(url).toURL();
        final HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(connectTimeoutMs);
        conn.setReadTimeout(readTimeoutMs);
        conn.setRequestProperty("User-Agent", "NCEPUCraft-ImageMap/1.0");
        conn.setRequestProperty("Accept", "image/*,*/*;q=0.8");
        // 避免拿到 br 压缩导致 ImageIO 无法识别
        conn.setRequestProperty("Accept-Encoding", "identity");

        final int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("HTTP " + code);
        }

        final String contentType = conn.getContentType();
        final String contentEncoding = conn.getContentEncoding();
        final int contentLength = conn.getContentLength();

        if (contentLength > maxBytes && contentLength > 0) {
            throw new IOException("图片过大(>" + maxBytes + " bytes)");
        }

        InputStream raw = conn.getInputStream();
        if (contentEncoding != null) {
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                raw = new GZIPInputStream(raw);
            } else if ("br".equalsIgnoreCase(contentEncoding)) {
                throw new IOException("服务器返回 br 压缩 当前不支持");
            }
        }

        try (InputStream in = raw;
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final byte[] buf = new byte[8192];
            int n;
            int total = 0;
            while ((n = in.read(buf)) >= 0) {
                total += n;
                if (total > maxBytes) throw new IOException("图片过大(>" + maxBytes + " bytes)");
                out.write(buf, 0, n);
            }
            final byte[] bytes = out.toByteArray();
            return new DownloadResult(bytes, sha256Hex(bytes), contentType, contentEncoding);
        } finally {
            conn.disconnect();
        }
    }

    public static BufferedImage decodeImage(byte[] bytes) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            final BufferedImage img = ImageIO.read(in);
            if (img == null) throw new IOException("无法识别的图片格式");
            return img;
        }
    }

    // 调整图像大小为128x128 保持宽高比 并使用黑色背景填充
    public static BufferedImage resizeToMap(BufferedImage src) {
        return resizeContain(src, MAP_SIZE, MAP_SIZE, Color.BLACK);
    }

    // 调整图像大小 保持宽高比 并使用黑色背景填充
    public static BufferedImage resizeContain(BufferedImage src, int targetW, int targetH, Color background) {
        final BufferedImage dst = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = dst.createGraphics();
        try {
            g.setComposite(AlphaComposite.SrcOver);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(background);
            g.fillRect(0, 0, targetW, targetH);

            final int sw = src.getWidth();
            final int sh = src.getHeight();
            if (sw <= 0 || sh <= 0) return dst;

            final double scale = Math.min((double) targetW / (double) sw, (double) targetH / (double) sh);
            final int tw = Math.max(1, (int) Math.round(sw * scale));
            final int th = Math.max(1, (int) Math.round(sh * scale));
            final int x = (targetW - tw) / 2;
            final int y = (targetH - th) / 2;
            g.drawImage(src, x, y, tw, th, null);
            return dst;
        } finally {
            g.dispose();
        }
    }

    public static byte[] encodePng(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", out)) throw new IOException("PNG 编码失败");
            return out.toByteArray();
        }
    }

    public static String sha256Hex(byte[] bytes) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] digest = md.digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // should never happen on Java 21
            throw new RuntimeException(e);
        }
    }

    public static boolean looksLikeWebp(byte[] bytes) {
        if (bytes == null || bytes.length < 12) return false;
        // RIFF....WEBP
        return bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
    }

    public static boolean looksLikeHtml(byte[] bytes) {
        if (bytes == null) return false;
        final int n = Math.min(bytes.length, 64);
        final String head = new String(bytes, 0, n, java.nio.charset.StandardCharsets.UTF_8).trim().toLowerCase();
        return head.startsWith("<!doctype html") || head.startsWith("<html") || head.contains("<head") || head.contains("<body");
    }

    public record DownloadResult(byte[] bytes, String sha256, String contentType, String contentEncoding) {}
}


