package net.kaoruxun.ncepucore;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import net.kaoruxun.ncepucore.utils.Utils;
import net.kaoruxun.ncepucore.SomeItems;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Objects;

@SuppressWarnings("deprecation")
final class Rules implements Listener, CommandExecutor {
    private static final String ITEM_NAME = "§e服务器规则二维码", NOT_ACCEPTED = "§c你还没有打开聊天框点击§a[同意服务器规定]§c!";
    private static final Render render = new Render();
    private final Location spawn;
    private final MapView map = Bukkit.createMap(Objects.requireNonNull(Bukkit.getWorld("world")));

    private final HashSet<Player> notAccepts = new HashSet<>();
    private final File acceptsFile;
    private final Advancement ROOT = Bukkit.getAdvancement(new NamespacedKey("ncepucraft", "ncepucraft/root"));
    private String accepts = "";

    {
        map.getRenderers().forEach(map::removeRenderer);
        map.addRenderer(render);
        map.setLocked(true);
    }

    @SuppressWarnings("ConstantConditions")
    public Rules(final Main main) {
        spawn = main.getServer().getWorld("world").getSpawnLocation();
        acceptsFile = new File(main.getDataFolder(), "accepts.txt");

        try {
            if (acceptsFile.exists()) accepts = new String(Files.readAllBytes(acceptsFile.toPath()));
            else if (!acceptsFile.createNewFile()) throw new IOException("Failed to create new file");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(main.getServer().getPluginCommand("denyrule")).setExecutor((sender, c, l, a) -> {
            if (!(sender instanceof final Player p)) return false;
            if (notAccepts.contains(p)) p.kickPlayer("§e[NCEPUCraft] §c你拒绝遵守服务器规定.");
            else {
                removeMap(p);
                p.sendMessage("§c你已经同意遵守了服务器规定!");
            }
            return true;
        });
    }

    private void removeMap(final Player p) {
        final PlayerInventory i = p.getInventory();
        final ItemStack is = i.getItemInMainHand();
        final ItemMeta im = is.getItemMeta();
        if (im != null && im.hasDisplayName() && im.getDisplayName().equals(ITEM_NAME)) {
            i.remove(is);
            p.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (accepts.contains(p.getUniqueId().toString())) return;
        notAccepts.add(p);
        p.sendMessage(Constants.JOIN_MESSAGE_HEADER);
        p.sendMessage(Constants.RULES);
        p.sendMessage(Constants.JOIN_MESSAGE_FOOTER);
        if (p.getInventory().getItemInMainHand().getType() != Material.AIR) return;
        final ItemStack is = new ItemStack(Material.FILLED_MAP);
        final MapMeta meta = (MapMeta) is.getItemMeta();
        meta.setMapView(map);
        meta.setDisplayName(ITEM_NAME);
        meta.setColor(Color.YELLOW);
        meta.setLocationName("二维码");
        meta.setUnbreakable(true);
        meta.setLore(Lists.newArrayList(ITEM_NAME));
        is.setItemMeta(meta);
        p.getInventory().setItemInMainHand(is);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        notAccepts.remove(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (notAccepts.contains(e.getPlayer()) && spawn.distance(e.getTo()) > 10) e.setCancelled(true);
    }
    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (notAccepts.contains(e.getPlayer())) e.setCancelled(true);
    }
    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        if (notAccepts.contains(e.getPlayer()) && !("/denyrule".equals(e.getMessage()) ||
                "/acceptrule".equals(e.getMessage()))) {
            e.setCancelled(true);
            e.getPlayer().sendActionBar(NOT_ACCEPTED);
        }
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        if (notAccepts.contains(e.getPlayer())) {
            e.setCancelled(true);
            e.getPlayer().sendActionBar(NOT_ACCEPTED);
        }
    }
    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e) {
        if (notAccepts.contains(e.getPlayer())) e.setCancelled(true);
    }
    @EventHandler
    public void onHeld(PlayerItemHeldEvent e) {
        if (notAccepts.contains(e.getPlayer())) e.setCancelled(true);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof final Player p)) return false;
        final String uuid = p.getUniqueId().toString();
        removeMap(p);
        if (!accepts.contains(uuid)) {
            notAccepts.remove(p);
            String text = "," + uuid;
            accepts += text;
            try {
                FileWriter writer = new FileWriter(acceptsFile, true);
                writer.write(text);
                writer.close();
            } catch (IOException e) { e.printStackTrace(); }
            p.sendMessage("§a感谢您接受了服务器的规定, 同时也希望您能一直遵守规定!");
            p.getInventory().addItem(Main.SOMEITEMS.getNoobFood());
            Bukkit.broadcastMessage("§b欢迎新玩家 §7" + p.getDisplayName() + " §b加入了服务器!");
        }
        Utils.giveAdvancement(ROOT, p);
        return true;
    }

    private final static class Render extends MapRenderer {
        private BufferedImage buffer;

        {
            try {
                buffer = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("QRCode.png")));
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
