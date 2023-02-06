package net.kaoruxun.ncepucore;

import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.*;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import net.kaoruxun.ncepucore.commands.*;
import net.kaoruxun.ncepucore.utils.*;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

import static net.kaoruxun.ncepucore.utils.Utils.registerCommand;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

// Based on https://github.com/neko-craft/NekoCore by Shirasawa
@Plugin(name = "NCEPUCore", version = "1.0")
@Description("An basic plugin used in NCEPUCraft.")
@Author("KaoruXun")
@ApiVersion(ApiVersion.Target.v1_13)
@Permission(name = "ncepu.show", defaultValue = PermissionDefault.TRUE)
@Permission(name = "ncepu.explode")
@Permission(name = "ncepu.endplatform")
@Permission(name = "ncepu.rsd")
@Permission(name = "ncepu.notdeatheffect")
@Command(name = "show", permission = "ncepu.show")
@Command(name = "explode", permission = "ncepu.explode")
@Command(name = "endplatform", permission = "ncepu.endplatform")
@Command(name = "rsd", permission = "ncepu.rsd")
@Command(name = "welcome", aliases = "w")
@Command(name = "bedrock", aliases = "be")
@Command(name = "toggle")
@Command(name = "acceptrule")
@Command(name = "denyrule")

@Command(name = "afk", permission = "ncepu.afk")
@Command(name = "back", permission = "ncepu.back")
@Command(name = "db", permission = "ncepu.db")
@Command(name = "delwarp", permission = "ncepu.warp.del")
@Command(name = "disrobe", permission = "ncepu.disrobe")
@Command(name = "tpcancel")
@Command(name = "home", permission = "ncepu.home")
@Command(name = "warp", permission = "ncepu.warp")
@Command(name = "mute", permission = "ncepu.mute")
@Command(name = "othershome", permission = "ncepu.others")
@Command(name = "sethome", permission = "ncepu.home")
@Command(name = "setwarp", permission = "ncepu.warp.set")
@Command(name = "spawn", permission = "ncepu.spawn")
@Command(name = "status", permission = "ncepu.status")
@Command(name = "sudo", permission = "ncepu.sudo")
@Command(name = "toggle", permission = "ncepu.toggle")
@Command(name = "tpaall", permission = "ncepu.tpaall")
@Command(name = "tpaccept")
@Command(name = "tpa", permission = "ncepu.tpa")
@Command(name = "tpdeny")
@Command(name = "tphere", permission = "ncepu.tphere")
@Command(name = "freeze", permission = "ncepu.freeze")
@Command(name = "test", permission = "ncepu.test")
@Permission(name = "ncepu.afk", defaultValue = PermissionDefault.TRUE)
@Permission(name = "ncepu.spawn", defaultValue = PermissionDefault.TRUE)
@Permission(name = "ncepu.home", defaultValue = PermissionDefault.TRUE)
@Permission(name = "ncepu.tpa", defaultValue = PermissionDefault.TRUE)
@Permission(name = "ncepu.tphere", defaultValue = PermissionDefault.TRUE)
@Permission(name = "ncepu.back", defaultValue = PermissionDefault.TRUE)
@Permission(name = "ncepu.toggle", defaultValue = PermissionDefault.TRUE)
@Permission(name = "ncepu.tpaall", defaultValue = PermissionDefault.TRUE)
@Permission(name = "ncepu.status", defaultValue = PermissionDefault.TRUE)
@Permission(name = "ncepu.disrobe", defaultValue = PermissionDefault.TRUE)
@Permission(name = "ncepu.warp", defaultValue = PermissionDefault.TRUE)
@Permission(name = "ncepu.others")
@Permission(name = "ncepu.sudo.avoid")
@Permission(name = "ncepu.immediate")
@Permission(name = "ncepu.sudo")
@Permission(name = "ncepu.mute")
@Permission(name = "ncepu.db")
@Permission(name = "ncepu.freeze")
@Permission(name = "ncepu.test")

@SuppressWarnings({"unused", "deprecation"})
public final class Main extends JavaPlugin implements Listener {
    private int i = 0;
    private Thread thread;
    private static final HashMap<UUID, Object[]> deathRecords = new HashMap<>();
    private static final DecimalFormat df = new DecimalFormat("0.0");
    private static final Random RANDOM = new Random();
    private static final JsonParser PARSER = new JsonParser();

    public static final SomeItems SOMEITEMS = new SomeItems();
    private World nether, world, theEnd;
    private final Set<Player> beList = Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<Player> warning = Collections.newSetFromMap(new WeakHashMap<>());

    private final String CHAIRS_NAME = "$$Chairs$$";
    private final HashSet<ArmorStand> chair_list = new HashSet<>();

    private final Advancement DEATH = Bukkit.getAdvancement(new NamespacedKey("ncepucraft", "ncepucraft/death")),
            STRIKE = Bukkit.getAdvancement(new NamespacedKey("ncepucraft", "ncepucraft/death_strike")),
            HUNGRY = Bukkit.getAdvancement(new NamespacedKey("ncepucraft", "ncepucraft/death_hungry")),
            EXPLOSION = Bukkit.getAdvancement(new NamespacedKey("ncepucraft", "ncepucraft/death_explosion")),
            STABBED = Bukkit.getAdvancement(new NamespacedKey("ncepucraft", "ncepucraft/death_stabbed")),
            STONECUTTER = Bukkit.getAdvancement(new NamespacedKey("ncepucraft", "ncepucraft/death_stonecutter")),
            FIRST_STEP = Bukkit.getAdvancement(new NamespacedKey("ncepucraft", "ncepucraft/first_step")),
            CHAT = Bukkit.getAdvancement(new NamespacedKey("ncepucraft", "ncepucraft/chat")),
            QUESTION = Bukkit.getAdvancement(new NamespacedKey("ncepucraft", "ncepucraft/chat_question")),
            AT = Bukkit.getAdvancement(new NamespacedKey("ncepucraft", "ncepucraft/chat_at")),
            HOME = Bukkit.getAdvancement(new NamespacedKey("ncepucraft", "ncepucraft/home"));


    public final WeakHashMap<Player, Pair<Integer, Location>> countdowns = new WeakHashMap<>();
    public final WeakHashMap<Player, Pair<Long, Runnable>> playerTasks = new WeakHashMap<>();
    public final WeakHashMap<Player, Pair<Location, Long>> afkPlayers = new WeakHashMap<>();
    @SuppressWarnings("CanBeFinal")
    public static net.kaoruxun.ncepucore.Main INSTANCE;
    public final HashSet<String> mutedPlayers = new HashSet<>();
    public final HashSet<String> warps = new HashSet<>();
    private final WeakHashMap<Player, Long> delays = new WeakHashMap<>();
    private BukkitTask countdownTask;

    private final EndPlatform endPlatform = new EndPlatform();


    {
        INSTANCE = this;
    }

    @SuppressWarnings({"BusyWait", "ResultOfMethodCallIgnored", "ConstantConditions"})
    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        final Server s = getServer();
        final PluginManager m = s.getPluginManager();

        final AntiExplode antiExplode = new AntiExplode();
        final Rules rules = new Rules(this);

        m.registerEvents(antiExplode, this);
        m.registerEvents(rules, this);
        m.registerEvents(this, this);
        registerCommand("explode", antiExplode);
        registerCommand("endplatform", endPlatform);
        registerCommand("show", new ShowItem());
        registerCommand("rsd", new RedStoneDetection(this));
        registerCommand("acceptrule", rules);
        registerCommand("welcome", new Welcome());
        registerCommand("bedrock", this);
        registerCommand("toggle", (a, b, c, d) -> {
            a.sendMessage("§c模式切换命令已更改为 §e/gamemode§c. 也可使用 §eF3 + N §c进行切换.");
            return true;
        });

        world = s.getWorld("world");
        nether = s.getWorld("world_nether");
        theEnd = s.getWorld("world_the_end");
        final Location spawn = world.getSpawnLocation();

        // Chairs
        s.getScheduler().runTaskTimer(this, () ->
                        getServer().getWorlds().forEach(w -> w.getEntitiesByClasses(ArmorStand.class).forEach(this::check)),
                100, 100);
        s.getPluginManager().registerEvents(this, this);

        try {
            if (!getDataFolder().exists()) getDataFolder().mkdir();
            DatabaseSingleton.init(Iq80DBFactory.factory.open(new File(getDataFolder(), "database"),
                    new Options().createIfMissing(true)));
        } catch (IOException e) {
            e.printStackTrace();
            setEnabled(false);
            return;
        }
        getServer().getPluginManager().registerEvents(new Events(this), this);
        try {
            Utils.loadCommands(
                    this,
                    AfkCommand.class,
                    BackCommand.class,
                    CancelCommand.class,
                    DbCommand.class,
                    DelWarpCommand.class,
                    DisrobeCommand.class,
                    FreezeCommand.class,
                    HomeCommand.class,
                    MuteCommand.class,
                    OthersHomeCommand.class,
                    WarpCommand.class,
                    SetHomeCommand.class,
                    SetWarpCommand.class,
                    SpawnCommand.class,
                    StatusCommand.class,
                    SudoCommand.class,
                    ToggleCommand.class,
                    TpaAllCommand.class,
                    TpAcceptCommand.class,
                    TpaCommand.class,
                    TpDenyCommand.class,
                    TpHereCommand.class,
                    TestCommand.class
            );
        } catch (Exception e) {
            e.printStackTrace();
            setEnabled(false);
            return;
        }
        countdownTask = getServer().getScheduler().runTaskTimer(this, () -> {
            final Iterator<Map.Entry<Player, Pair<Integer, Location>>> iterator = countdowns.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<Player, Pair<Integer, Location>> it = iterator.next();
                final Player p = it.getKey();
                final Pair<Integer, Location> pair = it.getValue();
                if (--pair.left < 1) {
                    iterator.remove();
                    final Location dest = pair.right;
                    if (dest == null) continue;
                    Utils.teleportPlayer(p, dest);
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    p.sendActionBar("§a传送成功!");
                } else p.sendActionBar("§e将在 §b" + pair.left + "秒 §e后进行传送!");
            }
        }, 20, 20);


        thread = new Thread(() -> {
            try {
                while (true) {
                    final double tps = s.getTPS()[0];
                    if (tps < 4.5) i++;
                    else i = 0;
                    if (i > 20) {
                        getServer().broadcastMessage("§c服务器 TPS 低, 将在五秒后自动重启!");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getServer().shutdown();
                        return;
                    }
                    final String footer = "\n§aTPS: §7" + df.format(tps) + " §aMSPT: §7" +
                            df.format(s.getTickTimes()[0] / 1000000.0) + "\n§b§m                                      ";
                    final ArrayList<Player> list = new ArrayList<>();
                    s.getOnlinePlayers().forEach(it -> {
                        it.setPlayerListFooter(footer);
                        final Location loc = it.getLocation();
                        if (loc.getWorld() == world && loc.distanceSquared(spawn) > 400) list.add(it);
                    });
                    if (!list.isEmpty()) s.getScheduler().runTask(this, () -> list.forEach(it -> Utils.giveAdvancement(FIRST_STEP, it)));
                    try {
                        s.getWorlds().forEach(it -> {
                            final Chunk[] ch = it.getLoadedChunks();
                            for (final Chunk c : ch) if (c.getEntities().length > 500) {
                                s.getScheduler().runTask(this, () -> {
                                    final Entity[] es = c.getEntities();
                                    for (final Entity e : es) if (e instanceof Item || (e instanceof FallingBlock && !(e instanceof TNTPrimed)))
                                        e.remove();
                                    if (c.getEntities().length < 200) s.broadcastMessage("§c这个位置 §7(" + c.getWorld().getName() + ", " +
                                            (c.getX() << 4) + ", " + (c.getZ() << 4) + ") §c有一大堆实体, 已被清除.");
                                });
                            }
                        });
                    } catch (final Exception ignored) { }
                    if (world.isThundering() && world.hasStorm()) {
                        world.getPlayers().forEach(it -> {
                            if (it.getGameMode() != GameMode.SURVIVAL || RANDOM.nextInt(17) != 0) return;
                            final Location loc = it.getLocation();
                            if (it.isInRain() && RANDOM.nextInt(3) == 0) {
                                final PlayerInventory inv = it.getInventory();
                                if (Utils.isConductive(inv.getItemInMainHand()) ||
                                        Utils.isConductive(inv.getItemInOffHand()) ||
                                        Utils.isConductive(inv.getBoots()) ||
                                        Utils.isConductive(inv.getChestplate()) ||
                                        Utils.isConductive(inv.getLeggings()) ||
                                        Utils.isConductive(inv.getHelmet())) {
                                    s.getScheduler().runTask(this, () -> Utils.strikeLightning(loc));
                                    return;
                                }
                            }
                            Block block = loc.toHighestLocation().getBlock();
                            if (Utils.isLeaves(block.getType()) && block.getHumidity() > 0 && block.getTemperature() > 0) {
                                final Leaves data = (Leaves) block.getBlockData();
                                if (data.isPersistent()) return;
                                int y = block.getY(), endY = loc.getBlockY() + 3;
                                while (y-- > endY) {
                                    block = block.getRelative(0, -1, 0);
                                    final Material type = block.getType();
                                    if (!(type == Material.AIR || Utils.isLeaves(type) || Utils.isLog(type))) return;
                                }
                                s.getScheduler().runTask(this, () -> Utils.strikeLightning(loc));
                            }
                        });
                        getServer().getScheduler().runTask(this, () -> world.getEntities().forEach(it -> {
                            if (it.getType() == EntityType.DROPPED_ITEM) {
                                if (!Utils.isConductive(((Item) it).getItemStack().getType())) return;
                            } else if (it instanceof Monster) {
                                EntityEquipment inv = ((LivingEntity) it).getEquipment();
                                if (!(Utils.isConductive(inv.getItemInMainHand()) ||
                                        Utils.isConductive(inv.getItemInOffHand()) ||
                                        Utils.isConductive(inv.getBoots()) ||
                                        Utils.isConductive(inv.getChestplate()) ||
                                        Utils.isConductive(inv.getLeggings()) ||
                                        Utils.isConductive(inv.getHelmet()))) return;
                            } else return;
                            if (!it.isInRain() || RANDOM.nextInt(14) != 0) return;
                            Utils.strikeLightning(it.getLocation());
                        }));
                    }
                    Thread.sleep(2000);
                }
            } catch (final InterruptedException ignored) { } catch (final Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    @Override
    public void onDisable() {
        chair_list.forEach(it -> {
            it.getPassengers().forEach(Entity::leaveVehicle);
            it.remove();
        });
        chair_list.clear();


        if (countdownTask != null) countdownTask.cancel();
        countdowns.clear();
        playerTasks.clear();
        try {
            DatabaseSingleton.closeDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        countdownTask = null;

        deathRecords.clear();
        if (thread == null) return;
        thread.interrupt();
        thread = null;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command, final String label, final String[] args) {
        if (!command.getName().equalsIgnoreCase("bedrock") || !(sender instanceof Player)) return false;
        if (beList.contains(sender)) {
            beList.remove(sender);
            sender.sendMessage("§a您当前已离开了 Bedrock 模式!");
        } else {
            beList.add((Player) sender);
            sender.sendMessage("§a您已进入 Bedrock 模式!");
        }
        return true;
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        final Player player = e.getPlayer();

        final Entity t = e.getPlayer().getVehicle();
        if (t instanceof ArmorStand) {
            e.getPlayer().leaveVehicle();
            check(t);
        }

        e.setQuitMessage("§c- " + Utils.getDisplayName(player));
        beList.remove(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.setJoinMessage("§a+ " + Utils.getDisplayName(e.getPlayer()));
        final Player p = e.getPlayer();
        final Server s = getServer();
        p.setPlayerListHeader(Constants.PLAYER_HEADER);
        p.sendMessage(Constants.JOIN_MESSAGE_HEADER);
        p.sendMessage("  §a当前在线玩家: §7" + s.getOnlinePlayers().size() +
                "                     §a当前TPS: " + (int) s.getTPS()[0]);

        p.sendMessage(Constants.JOIN_MESSAGES);
        p.sendMessage(Constants.JOIN_MESSAGE1);
        p.sendMessage(Constants.JOIN_MESSAGE_FOOTER);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerPreLogin(final AsyncPlayerPreLoginEvent e) {
        boolean needKick = true;
        for (final ProfileProperty it : e.getPlayerProfile().getProperties()) if (it.getName().equals("textures")) try {
            JsonElement urlElement = PARSER.parse(new String(Base64.getDecoder().decode(it.getValue()))).getAsJsonObject()
                    .getAsJsonObject("textures").getAsJsonObject("SKIN").get("url");
            if (urlElement != null) {
                String url = urlElement.getAsString();
                if (!url.endsWith("1a4af718455d4aab528e7a61f86fa25e6a369d1768dcb13f7df319a713eb810b") &&
                        !url.endsWith("3b60a1f6d562f52aaebbf1434f1de147933a3affe0e764fa49ea057536623cd3")) needKick = false;
            }
        } catch (final Exception ignored) { }
        if (needKick) e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§c请先给您的游戏账户设置一个皮肤再尝试进入服务器!");
    }

    @EventHandler
    public void onSmithItem(final SmithItemEvent e) {
        ItemStack is = e.getInventory().getResult();
        if (is == null || is.getType() != Material.DIAMOND) return;
        e.getInventory().setResult(new ItemStack(Material.DIAMOND, is.getAmount()));
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent e) {
        final List<ItemStack> drops = e.getDrops();
        switch (e.getEntityType()) {
            case TURTLE -> {
                final Player killer = e.getEntity().getKiller();
                int count = 2;
                if (killer != null) {
                    int bound = Math.round(((float) killer
                            .getInventory()
                            .getItemInMainHand()
                            .getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)) / 2);
                    if (bound > 0) count += RANDOM.nextInt(bound);
                }
                drops.add(new ItemStack(Material.SCUTE, count));
            }
            case RAVAGER -> {
                drops.clear();
                drops.add(new ItemStack(Material.LEATHER, 4));
            }
            case VINDICATOR -> drops.removeIf(it -> it.getType() == Material.WHITE_BANNER || it.getType() == Material.IRON_AXE);
            case EVOKER -> drops.removeIf(is -> is.getType() == Material.WHITE_BANNER);
            case PILLAGER -> drops.removeIf(it -> it.getType() == Material.WHITE_BANNER || it.getType() == Material.CROSSBOW);
            case WITCH -> drops.removeIf(is -> is.getType() == Material.POTION);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTransform(final EntityTransformEvent e) {
        if (e.getEntityType() != EntityType.VILLAGER) return;
        for (final Entity it : e.getTransformedEntities()) if (it.getType() == EntityType.WITCH) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock() != null &&
                e.getClickedBlock().getType() == Material.FARMLAND) e.setCancelled(true);


        final Block b = e.getClickedBlock();
        final Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR || e.getItem() != null || e.getAction() != RIGHT_CLICK_BLOCK ||
                b == null || (!p.isOp() && p.hasPermission("ncepu.cannotsit")) ||
                b.getType().data != Stairs.class) return;
        final Stairs data = (Stairs) b.getBlockData();
        if (data.getHalf() == Bisected.Half.TOP) return;
        final Location l = b.getLocation().clone().add(0.5, -1.18, 0.5);
        final Collection<ArmorStand> entities = l.getNearbyEntitiesByType(ArmorStand.class, 0.5, 0.5, 0.5);
        int i = entities.size();
        if (i > 0) {
            for (ArmorStand it : entities) if (!check(it)) i--;
            if (i > 0) return;
        }
        switch (data.getFacing()) {
            case SOUTH: l.setYaw(180); break;
            case EAST: l.setYaw(90); break;
            case WEST: l.setYaw(270); break;
        }

        final ArmorStand a = (ArmorStand) b.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        a.setAI(false);
        a.setCustomName(CHAIRS_NAME);
        a.setCanMove(false);
        a.setBasePlate(false);
        a.setCanTick(false);
        a.setVisible(false);
        a.setCanPickupItems(false);
        a.setPassenger(p);
        chair_list.add(a);
    }


    @EventHandler
    void onEntityDismount(final EntityDismountEvent e) {
        final Entity l = e.getDismounted();
        final String name = l.getCustomName();
        if (l instanceof ArmorStand && name != null && name.equals(CHAIRS_NAME)) leaveChair(l, e.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobJump(final EntityInteractEvent e) {
        if (e.getEntityType() != EntityType.PLAYER &&
            e.getBlock().getType() == Material.FARMLAND) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLightingStrike(final LightningStrikeEvent e) {
        var block = e.getLightning().getLocation().getBlock();
        if (block.getY() < 2) return;
        block = block.getRelative(0, -1, 0);
        if (block.getType() != Material.LIGHTNING_ROD) return;
        var b2 = block.getRelative(0, -1, 0);
        if (Utils.isLog(b2.getType())) b2.setType(Material.COAL_BLOCK);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalCreate(final PortalCreateEvent e) {
        if (endPlatform.flag && e.getReason() == PortalCreateEvent.CreateReason.END_PLATFORM) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRaidTrigger(final RaidTriggerEvent e) {
        if (getServer().getTPS()[0] < 16.0) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntitySpawn(final EntitySpawnEvent e) {
        switch (e.getEntityType()) {
            case CREEPER: return;
            case WITHER:
            case PILLAGER:
            case RAVAGER:
            case EVOKER:
            case EVOKER_FANGS:
            case VINDICATOR:
            case ENDERMAN:
                if (getServer().getTPS()[0] >= 16.0) break;
            case BAT:
                e.setCancelled(true);
                return;
            case HUSK:
            case ZOMBIE:
            case DROWNED:
            case ZOMBIE_VILLAGER:
                if (RANDOM.nextBoolean()) ((Zombie) e.getEntity()).setShouldBurnInDay(false);
                break;
            case VILLAGER:
                CreatureSpawnEvent.SpawnReason reason = e.getEntity().getEntitySpawnReason();
                if (reason == CreatureSpawnEvent.SpawnReason.CUSTOM ||
                        reason == CreatureSpawnEvent.SpawnReason.COMMAND) return;
                final Location l = e.getLocation();
                if (l.getNearbyEntitiesByType(Villager.class, 48).size() > 50) {
                    Bukkit.broadcastMessage("§c有人在 §7" + l.getBlockX() + "," + l.getBlockY() + "," +
                            l.getBlockZ() + " §c大量繁殖村民.");
                }
                return;
        }
        if (!(e.getEntity() instanceof final Monster entity)) return;
        for (int i = 0; i < 2 && RANDOM.nextInt(10) >= 7; i++) {
            PotionEffectType type = Constants.EFFECTS[RANDOM.nextInt(Constants.EFFECTS.length - 1)];
            entity.addPotionEffect(new PotionEffect(type, 144000,
                            type == PotionEffectType.DAMAGE_RESISTANCE || RANDOM.nextBoolean() ? 1 : 2));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(final AsyncPlayerChatEvent e) {
        final StringBuilder sb = new StringBuilder();
        final String n = e.getPlayer().getName();
        boolean isAt = false;
        for (String s : e.getMessage().split(" ")) {
            if (s.startsWith("@")) s = s.replaceAll("^@+", "");
            if (n.equalsIgnoreCase(s)) {
                sb.append(s).append(' ');
                continue;
            }
            final Player p = getServer().getPlayerExact(s);
            if (p != null) {
                sb.append("§a@").append(s).append("§7");
                p.sendMessage("§a一位叫 §f" + n + " §a的小朋友@了你.");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                isAt = true;
            } else sb.append(s);
            sb.append(' ');
        }
        final String value = sb.toString();
        final String be = n + "§7: " + value;
        final TextComponent name = new TextComponent(n),
            text = new TextComponent(": " + value);
        name.setHoverEvent(Constants.AT);
        name.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, n + " "));
        text.setColor(ChatColor.GRAY);
        text.setHoverEvent(Constants.TPA);
        text.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value.trim()));
        e.getRecipients().forEach(it -> {
            if (beList.contains(it)) it.sendMessage(be);
            else it.sendMessage(name, text);
        });
        e.getRecipients().clear();
        final boolean flag = isAt;
        getServer().getScheduler().runTask(this, () -> {
            Utils.giveAdvancement(CHAT, e.getPlayer());
            if (flag) Utils.giveAdvancement(AT, e.getPlayer());
            if (value.contains("\u00BF")) Utils.giveAdvancement(QUESTION, e.getPlayer());
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerLoginEvent e) {
        if (e.getResult() == PlayerLoginEvent.Result.ALLOWED) warning.add(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent e) {
        final Player p = e.getEntity();
        Utils.giveAdvancement(DEATH, p);
        final EntityDamageEvent dmg = p.getLastDamageCause();
        if (dmg != null) {
            Advancement ad = null;
            switch (dmg.getCause()) {
                case MAGIC:
                    if (dmg instanceof EntityDamageByBlockEvent) {
                        final Block block = ((EntityDamageByBlockEvent) dmg).getDamager();
                        if (block != null && block.getType() == Material.STONECUTTER) {
                            e.setDeathMessage(p.getName() + "裂开了");
                            ad = STONECUTTER;
                        }
                    }
                    break;
                case ENTITY_EXPLOSION:
                case BLOCK_EXPLOSION:
                    ad = EXPLOSION;
                    break;
                case STARVATION:
                    ad = HUNGRY;
                    break;
                case LIGHTNING:
                    ad = STRIKE;
                    break;
                default: if (dmg instanceof EntityDamageByBlockEvent) {
                    final Block block = ((EntityDamageByBlockEvent) dmg).getDamager();
                    if (block != null) {
                        final Material type = block.getType();
                        if (type == Material.CACTUS || type == Material.SWEET_BERRY_BUSH) ad = STABBED;
                    }
                }
            }
            if (ad != null) Utils.giveAdvancement(ad, p);
        }
        if (p.hasPermission("ncepu.notdeatheffect") || p.getStatistic(Statistic.PLAY_ONE_MINUTE) < 20 * 60 * 40) return;
        deathRecords.put(p.getUniqueId(), new Object[] { p.getExhaustion(), p.getSaturation(), p.getFoodLevel() });
    }

    @EventHandler
    public void onPlayerPostRespawn(final PlayerPostRespawnEvent e) {
        final Player p = e.getPlayer();
        if (p.hasPermission("ncepu.notdeatheffect")) return;
        final UUID id = p.getUniqueId();
        final Object[] obj = deathRecords.get(id);
        if (obj != null) {
            p.setExhaustion((float) obj[0]);
            p.setSaturation((float) obj[1]);
            p.setFoodLevel((int) obj[2]);
            p.setHealth(Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue() / 2);
            p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * 30, 1, true, false));
            deathRecords.remove(id);
        }
    }

    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent e) {
        switch (e.getEntityType()) {
            case CREEPER:
            case FIREBALL:
            case SMALL_FIREBALL:
            case DRAGON_FIREBALL:
            case ENDER_DRAGON:
            case WITHER_SKULL:
                break;
            default:
                Location loc = e.getLocation();
                if (loc.getWorld() != world || loc.distanceSquared(world.getSpawnLocation()) > 12544) return;
        }
        e.blockList().clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        if (e.getPlayer().isOp()) return;
//        if (checkTrapChestExact(e.getBlock().getLocation())) {
//            Player player = e.getPlayer();
//            getServer().broadcastMessage("§c玩家 §f" + player.getName() + " §c正在尝试从出生点钻石箱中取出物品!!");
//            player.banPlayer("§c不要尝试偷盗! 解封请进入QQ群: 760836917");
//            e.setCancelled(true);
//        } else if (checkTrapChest(e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreakByEntity(final HangingBreakByEntityEvent e) {
        if (e.getRemover() != null && e.getRemover().getType() == EntityType.CREEPER) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getWhoClicked().isOp()) return;
        final InventoryHolder holder = e.getView().getTopInventory().getHolder();
//        if (holder instanceof Chest && e.getWhoClicked() instanceof Player) {
//            if (e.getClickedInventory() == e.getView().getTopInventory() && checkTrapChestExact(((Chest) holder).getLocation())) {
//                Player player = (Player) e.getWhoClicked();
//                getServer().broadcastMessage("§c玩家 §f" + player.getName() + " §c正在尝试从出生点钻石箱中取出物品!!");
//                player.banPlayer("§c不要尝试偷盗! 解封请进入QQ群: 760836917");
//            }
//        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(final EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Zombie) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
        if (e.getEntityType() == EntityType.VILLAGER && !((Villager) e.getEntity()).hasAI()) e.setCancelled(true);
        switch (e.getDamager().getType()) {
            case PLAYER:
                if (e.getEntityType() == EntityType.VILLAGER &&
                        ((Player) e.getDamager()).getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD) {
                    var villager = (Villager) e.getEntity();
                    villager.setAI(!villager.hasAI());
                    break;
                } else return;
            case CREEPER:
            case ENDER_CRYSTAL:
            case LIGHTNING:
            case FIREBALL:
                break;
            default: return;
        }
        if (e.getEntity() instanceof Monster || e.getEntityType() == EntityType.PLAYER) return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByBlock(final EntityDamageByBlockEvent e) {
        if (e.getEntityType() == EntityType.VILLAGER && ((e.getDamager() != null &&
                e.getDamager().getType() == Material.STONECUTTER) || !((Villager) e.getEntity()).hasAI())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(final BlockIgniteEvent e) {
        switch (e.getCause()) {
            case SPREAD, LAVA -> e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(final BlockBurnEvent e) {
        e.setCancelled(true);
    }

    private boolean isDangerCommand(final String cmd) {
        for (final Pattern c : Constants.DANGER_COMMANDS) if (c.matcher(cmd).matches()) return true;
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
        if (isDangerCommand(e.getMessage())) {
            e.getPlayer().sendMessage("§c危险的指令已被拒绝执行!");
            e.setCancelled(true);
        } else if (e.getMessage().startsWith("sethome") || e.getMessage().startsWith("/sethome")) Utils.giveAdvancement(HOME, e.getPlayer());
    }
    @EventHandler(ignoreCancelled = true)
    public void onServerCommand(final ServerCommandEvent e) {
        if (isDangerCommand(e.getCommand())) {
            e.getSender().sendMessage("§c危险的指令已被拒绝执行!");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(final BlockExplodeEvent e) {
        final Block b = e.getBlock();
        final Location loc = b.getLocation();
        if (b.getWorld() == nether && b.getType().isAir() &&
            (Math.pow(loc.getBlockX() + 36, 2) + Math.pow(loc.getBlockZ(), 2)) < 12544) {
            e.setCancelled(true);
            loc.getNearbyPlayers(6).forEach(it -> {
                it.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 120, 5, true, false));
                it.sendMessage("§c请不要在距离世界出生点7个区块以内玩爆炸物!");
            });
        }
    }

    private boolean checkTrapChest(final Location loc) {
//        return loc.getWorld() == world && (Math.pow(loc.getBlockX() + 202, 2) +
//                Math.pow(loc.getBlockY() - 65, 2) + Math.pow(loc.getBlockZ() - 219, 2) <= 4);
        return false;
    }

    private boolean checkTrapChestExact(final Location loc) {
//        return loc != null && loc.getWorld() == world && loc.getBlockX() == -202 && loc.getBlockY() == 65 &&loc.getBlockZ() == 219;
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockSpread(final BlockSpreadEvent e) {
        Block block = e.getSource();
        if (block.getType() != e.getNewState().getType()) return;
        final Material material;
        switch (block.getType()) {
            case KELP:
                material = Material.KELP_PLANT;
                break;
            case BAMBOO:
                material = Material.BAMBOO;
                break;
            default: return;
        }
        int height = 0;
        do {
            if (height++ >= 15) {
                e.setCancelled(true);
                return;
            }
            block = block.getRelative(BlockFace.DOWN);
        } while (block.getType() == material);
        if (height >= 2 + new Random(block.getLocation().add(0, 1, 0).toBlockKey()).nextInt(13)) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent e) {
        if (!e.getPlayer().isOp() && checkTrapChest(e.getBlock().getLocation())) e.setCancelled(true);
        else if (e.getBlock().getType() == Material.WET_SPONGE) Utils.absorbLava(e.getBlock(), null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpongeAbsorb(final SpongeAbsorbEvent e) {
        if (!e.getBlocks().isEmpty()) Utils.absorbLava(e.getBlock(), this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(final BlockPistonExtendEvent e) {
        for (final Block block : e.getBlocks()) if (checkTrapChest(block.getLocation())) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(final BlockPistonRetractEvent e) {
        for (final Block block : e.getBlocks()) if (checkTrapChest(block.getLocation())) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent e) {
        if (e.getEntityType() != EntityType.ENDERMAN) return;
        final Enderman entity = (Enderman) e.getEntity();
        if (entity.getWorld() == theEnd) switch (e.getBlock().getType()) {
            case MELON:
            case PUMPKIN:
                return;
        }
        e.setCancelled(true);
        entity.setCarriedBlock(null);
    }


    public void delayTeleport(final Player player, final Location loc) {
        delayTeleport(player, loc, false);
    }

    public void delayTeleport(final Player player, Location loc, final boolean now) {
        boolean isSafe = true;
        if (player.getGameMode() == GameMode.SURVIVAL) {
            final Location temp = Utils.findSafeLocation(loc);
            if (temp == null) isSafe = false;
            else loc = temp;
        }
        if (now || (!shouldPlayerBeDelayed(player) && isSafe)) {
            countdowns.put(player, new Pair<>(1, loc));
        } else {
            player.sendMessage(Constants.MESSAGE_HEADER);
            if (!isSafe) player.sendTitle(new Title("§c危!", "§e检测到目标位置可能不安全!"));
            player.sendMessage(Constants.CANCEL_HUB);
            player.sendMessage(Constants.MESSAGE_FOOTER);
            countdowns.put(player, new Pair<>(10, loc));
        }
        if (!now) delays.put(player, System.currentTimeMillis() + 2 * 1000 * 60);
    }

    public void requestTeleport(Player player, String message, Runnable fn) {
        playerTasks.put(player, new Pair<>(System.currentTimeMillis() + 2 * 1000 * 60, fn));
        player.sendMessage(Constants.MESSAGE_HEADER);
        player.sendMessage(message);
        player.sendMessage(Constants.REQUEST_HUB);
        player.sendMessage(Constants.MESSAGE_FOOTER);
    }

    public boolean cancelTeleport(final Player player) {
        return countdowns.remove(player) != null;
    }

    public boolean shouldPlayerBeDelayed(Player player) {
        if (player.hasPermission("ncepu.immediate")) return false;
        final Long time = delays.get(player);
        return time != null && time > System.currentTimeMillis();
    }

    public Player getPlayer(CommandSender sender, String name) {
        final Player p = getServer().getPlayerExact(name);
        if (p == null) {
            sender.sendMessage(Constants.NO_SUCH_PLAYER);
            return null;
        }
        if (p == sender) {
            sender.sendMessage("§c你不能传送你自己!");
            return null;
        }
        return p;
    }

    @SuppressWarnings("unused")
    public boolean isAfking(final Player player) {
        final Pair<Location, Long> pair = afkPlayers.get(player);
        return pair != null && pair.right < System.currentTimeMillis();
    }


    private void leaveChair(final Entity l, @Nullable final Entity p) {
        //noinspection SuspiciousMethodCalls
        chair_list.remove(l);
        l.remove();
        getServer().getScheduler().runTaskLater(this, () -> {
            final Entity p2 = p == null ? l.getPassenger() : p;
            if (p2 == null) return;
            p2.teleport(p2.getLocation().add(0.0, 0.5, 0.0));
        }, 1);
    }

    private boolean check(final Entity it) {
        final String name = it.getCustomName();
        final Entity p = it.getPassenger();
        if (name != null && name.equals(CHAIRS_NAME)) {
            if (p != null && p.getVehicle() == it && it.getLocation().clone().add(-0.5, 1.18, -0.5)
                    .getBlock().getType().data == Stairs.class) return true;
            it.remove();
            //noinspection SuspiciousMethodCalls
            chair_list.remove(it);
        }
        return false;
    }

    private ArrayList<ArmorStand> getChairsNearBy(final Location l) {
        final Collection<ArmorStand> entities = l.getNearbyEntitiesByType(ArmorStand.class, 0.5, 0.5, 0.5);
        final ArrayList<ArmorStand> list = new ArrayList<>();
        for (ArmorStand it : entities) if (check(it)) list.add(it);
        return list;
    }
}
