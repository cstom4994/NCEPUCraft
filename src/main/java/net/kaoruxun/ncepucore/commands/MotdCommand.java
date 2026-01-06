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
        // if (!sender.hasPermission("ncepu.motd")) {
        //     sender.sendMessage("§c你没有权限来执行这个指令!");
        //     return true;
        // }
        // if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
        //     sender.sendMessage("§eMOTD 已写死在代码中，不再读取配置文件 无需 reload");
        //     return true;
        // }
        // sender.sendMessage("§e用法: /motd reload");
        return true;
    }
}


