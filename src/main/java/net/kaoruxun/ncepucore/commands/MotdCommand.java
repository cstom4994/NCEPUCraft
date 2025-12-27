package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.command.CommandSender;

@CommandName("motd")
public final class MotdCommand extends BasicCommand {
    public MotdCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ncepu.motd")) {
            sender.sendMessage("§c你没有权限来执行这个指令!");
            return true;
        }

        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            instance.reloadConfig();
            sender.sendMessage("§aMOTD 配置已重载!");
            return true;
        }

        sender.sendMessage("§e用法: /motd reload");
        return true;
    }
}


