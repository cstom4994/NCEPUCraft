package net.kaoruxun.ncepucore;


import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class SomeItems {

    public static final String ITEM_QRCODE_NAME = "§e服务器规则二维码";

    public static ItemStack getNoobFood(){
        ItemStack item = new ItemStack(Material.COOKED_BEEF, 64);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName("§6[ §e§l新人牛排耶 §6]");
        im.setLore(Arrays.asList("§f香喷喷的样子", "§f或许是从" + ChatColor.YELLOW + "奶思" +"§f家偷的"));
        item.setItemMeta(im);
        return item;
    }

    public static ItemStack getMap(String filename, String locname, String name){

        ItemStack is = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) is.getItemMeta();

        try {
            Render render = new Render(filename);
            MapView map = Bukkit.createMap(Objects.requireNonNull(Bukkit.getWorld("world")));

            map.getRenderers().forEach(map::removeRenderer);
            map.addRenderer(render);
            map.setLocked(true);

            meta.setMapView(map);
            meta.setDisplayName(name);
            meta.setColor(Color.YELLOW);
            meta.setLocationName(locname);
            meta.setUnbreakable(true);
            meta.setLore(Lists.newArrayList(name));
            is.setItemMeta(meta);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return is;
    }


    private final static class Render extends MapRenderer {
        private BufferedImage buffer;

        public Render(String name)
        {
            try {
                buffer = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(name)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void render(MapView map, MapCanvas canvas, Player player) {
            canvas.drawImage(0, 0, buffer);
        }
    }
}
