package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

@CommandName("status")
public final class StatusCommand extends BasicCommand {
    private final static String TEMPLATE = "\n§f游戏时间§7: %s (%d刻)\n§f运行时间§7: %d天 %s\n§f开服时间§7: %d年 %d天 %s\n§f服务器版本§7: %s\n§f内存使用§7: %.2f §f/ §7%.2f GB\n§f每Tick用时§7: %.3f 毫秒\n§f实体数§7: 主世界:%d 下界:%d 末地:%d\n§f区块数§7: 主世界:%d 下界:%d 末地:%d\n";
    private final static SimpleDateFormat FORMAT0 = new SimpleDateFormat("HH:mm:ss");
    private final static SimpleDateFormat FORMAT1 = new SimpleDateFormat("H小时 m分钟 s秒");
    private final static float GB = 1024 * 1024 * 1024;
    private final static float MS = 1000 * 1000;
    private final static Runtime runtime = Runtime.getRuntime();
    private final static RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
    private final World WORLD = Objects.requireNonNull(instance.getServer().getWorld("world"));
    private final World NETHER = (World) ObjectUtils.defaultIfNull(instance.getServer().getWorld("world_nether"), WORLD);
    private final World END = (World) ObjectUtils.defaultIfNull(instance.getServer().getWorld("world_the_end"), WORLD);

    static {
        final TimeZone tz = TimeZone.getTimeZone("GMT+00:00");
        FORMAT0.setTimeZone(tz);
        FORMAT1.setTimeZone(tz);
    }

    public StatusCommand(final Main main) { super(main); }

    @SuppressWarnings("deprecation")
    @Override
    public boolean callback(CommandSender sender, String[] args) {
        final long tick = WORLD.getTime();
        instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
            final Date date = new Date(System.currentTimeMillis() - 1565798400000L);
            final Date uptime = new Date(bean.getUptime());
            final Calendar c = Calendar.getInstance();
            c.setTime(uptime);
            final int day1 = c.get(Calendar.DAY_OF_YEAR) - 1;
            c.setTime(date);
            sender.sendMessage(String.format(
                    TEMPLATE,
                    FORMAT0.format(new Date((tick + 6000) * 3600)),
                    tick,
                    day1,
                    FORMAT1.format(uptime),
                    date.getYear() - 70,
                    c.get(Calendar.DAY_OF_YEAR) - 1,
                    FORMAT1.format(date),
                    Bukkit.getVersion(),
                    runtime.totalMemory() / GB,
                    runtime.maxMemory() / GB,
                    Bukkit.getTickTimes()[0] / MS,
                    WORLD.getEntityCount(),
                    NETHER.getEntityCount(),
                    END.getEntityCount(),
                    WORLD.getChunkCount(),
                    NETHER.getChunkCount(),
                    END.getChunkCount()
            ));
        });
        return true;
    }
}
