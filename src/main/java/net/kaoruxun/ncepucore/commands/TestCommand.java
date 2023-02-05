package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandName("test")
public final class TestCommand extends BasicCommand {
    public TestCommand(Main main) {
        super(main);
    }

    @Override
    public boolean callback(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || args.length == 0) return false;
        final Player p = (Player) sender;
        if(p.isOp()){
            switch (args[0].toString()) {
                case "noobfood" -> p.getInventory().addItem(Main.SOMEITEMS.getNoobFood());
                case "map" -> p.getInventory().addItem(Main.SOMEITEMS.getMap(args[1].toString(), "图像", "MAP"));
                default -> p.sendMessage(ChatColor.GREEN + "Test::" + args[0]);
            }
        }else{

        }
        return true;
    }
}