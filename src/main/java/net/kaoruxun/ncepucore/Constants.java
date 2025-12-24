package net.kaoruxun.ncepucore;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.potion.PotionEffectType;

import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public final class Constants {
    private Constants() {}
    public static final String COMMAND_DESCRIPTION = "A NCEPUCraft provided command.";
    public static final String WRONG_USAGE = "§c错误的命令用法!";
    public static final String NO_PERMISSION = "§c你没有权限来执行这个指令!";

    public static final String PLAYER_HEADER = "§b§m          §r §a[§eNCEPUCraft§a] §b§m          \n§aQQ 群: §7760836917\n§r";

    public static final String JOIN_MESSAGE_HEADER = "§b§m                    §r §a[§eNCEPUCraft§a] §b§m                   §r";
    public static final String JOIN_MESSAGE1 = "  §c由于服务器没有领地插件, 请不要随意拿取他人物品, 否则会直接封禁!";
    public static final String JOIN_MESSAGE_FOOTER = "§b§m                                                         §r\n\n";
    public static final TextComponent[] JOIN_MESSAGES = new TextComponent[5];

    public static final TextComponent[] RULES = new TextComponent[6];
    public static final HoverEvent AT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{ new TextComponent("§f点击可以直接 §a@ §f该玩家") }),
        TPA = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{ new TextComponent("点击可以复制文本") });

    public static final Pattern[] DANGER_COMMANDS = {
            Pattern.compile("kill @e *$"),
            Pattern.compile("kill @e\\[(?!type=)"),
            Pattern.compile("tp @e *$"),
            Pattern.compile("tp @e\\[(?!type=)"),
            Pattern.compile("teleport @e *$"),
            Pattern.compile("teleport @e\\[(?!type=)")
    };

    public static final PotionEffectType[] EFFECTS = new PotionEffectType[] {
            PotionEffectType.SPEED,
            PotionEffectType.HEALTH_BOOST,
            // 1.21 API 中旧名称被移除 使用与 Mojang 同步的新名称
            PotionEffectType.STRENGTH,
            PotionEffectType.JUMP_BOOST,
            PotionEffectType.REGENERATION,
            PotionEffectType.RESISTANCE,
            PotionEffectType.ABSORPTION
    };

    static {
        (JOIN_MESSAGES[0] = new TextComponent("  QQ 群: ")).setColor(ChatColor.GREEN);

        TextComponent c = JOIN_MESSAGES[1] = new TextComponent("760836917");
        c.setColor(ChatColor.GRAY);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://jq.qq.com/?wv=1027&k=Wzlekp2K"));

        JOIN_MESSAGES[2] = new TextComponent("      ");

        JOIN_MESSAGES[3] = new TextComponent("  §b服务器最新公告可以点击 ");

        c = JOIN_MESSAGES[4] = new TextComponent("[查看]");
        c.setColor(ChatColor.BLUE);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://docs.qq.com/doc/DRHlBR2NIeERpdFB6"));


        RULES[0] = new TextComponent("  §b欢迎您来到 §eNCEPUCraft §a!\n  §e您需要扫描您手中的二维码或点击 ");

        c = RULES[1] = new TextComponent("[这里]");
        c.setColor(ChatColor.BLUE);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://docs.qq.com/doc/DRHlBR2NIeERpdFB6"));

        RULES[2] = new TextComponent("  §e来阅读服务器规定\n  §7点击确认后则默认您已阅读并遵守服务器规定!\n");

        c = RULES[3] = new TextComponent(" [我已阅读并遵守服务器规定] §7或输入指令/acceptrule");
        c.setColor(ChatColor.GREEN);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptrule"));

        RULES[4] = new TextComponent("      ");

        c = RULES[5] = new TextComponent("[拒绝]");
        c.setColor(ChatColor.RED);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/denyrule"));
    }

    public static final String NO_SUCH_PLAYER = "§c没有这样的一个玩家!";

    public static final String MESSAGE_HEADER = "§b§m                    §r §e[传送系统] §b§m                    ";
    public static final String MESSAGE_FOOTER = "§b§m                                                       ";
    public static final BaseComponent[] CANCEL_HUB = new BaseComponent[3];
    public static final BaseComponent[] REQUEST_HUB = new BaseComponent[4];

    static {
        CANCEL_HUB[0] = new TextComponent("  §d正在传送中...\n  §7传送中禁止移动, 如需取消请点击下面的文字:\n");
        CANCEL_HUB[1] = new TextComponent("                  ");
        TextComponent t = new TextComponent("[取消传送]");
        t.setColor(ChatColor.RED);
        t.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpcancel"));
        CANCEL_HUB[2] = t;

        REQUEST_HUB[0] = new TextComponent("            ");
        t = new TextComponent("[拒绝传送]");
        t.setColor(ChatColor.RED);
        t.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"));
        REQUEST_HUB[1] = t;
        REQUEST_HUB[2] = new TextComponent("    ");
        t = new TextComponent("[确认传送]");
        t.setColor(ChatColor.GREEN);
        t.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"));
        REQUEST_HUB[3] = t;
    }
}
