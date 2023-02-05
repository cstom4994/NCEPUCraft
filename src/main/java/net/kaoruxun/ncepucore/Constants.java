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

    public static final String JOIN_MESSAGE_HEADER = "§b§m                       §r §a[§eNCEPUCraft§a] §b§m                      §r";
    public static final String JOIN_MESSAGE1 = "  §c由于服务器没有领地插件, 请不要随意拿取他人物品, 否则会直接封禁!";
    public static final String JOIN_MESSAGE_FOOTER = "§b§m                                                          §r\n\n";
    public static final TextComponent[] JOIN_MESSAGES = new TextComponent[3];

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
            PotionEffectType.INCREASE_DAMAGE,
            PotionEffectType.JUMP,
            PotionEffectType.REGENERATION,
            PotionEffectType.DAMAGE_RESISTANCE,
            PotionEffectType.ABSORPTION
    };

    static {
        (JOIN_MESSAGES[0] = new TextComponent("  QQ 群: ")).setColor(ChatColor.GREEN);

        TextComponent c = JOIN_MESSAGES[1] = new TextComponent("760836917");
        c.setColor(ChatColor.GRAY);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://jq.qq.com/?wv=1027&k=Wzlekp2K"));

        JOIN_MESSAGES[2] = new TextComponent("      ");


        RULES[0] = new TextComponent("  §b欢迎您来到 §eNCEPUCraft §a!\n  §e您需要扫描您手中的二维码或点击 ");

        c = RULES[1] = new TextComponent("[这里]");
        c.setColor(ChatColor.BLUE);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://docs.qq.com/doc/DRHlBR2NIeERpdFB6"));

        RULES[2] = new TextComponent("  §e来阅读服务器规定\n  §7点击确认后则默认您已阅读并遵守服务器规定!\n       ");

        c = RULES[3] = new TextComponent(" [我已阅读并遵守服务器规定] §7或输入指令/acceptrule");
        c.setColor(ChatColor.GREEN);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptrule"));

        RULES[4] = new TextComponent("      ");

        c = RULES[5] = new TextComponent("[拒绝]");
        c.setColor(ChatColor.RED);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/denyrule"));
    }
}
