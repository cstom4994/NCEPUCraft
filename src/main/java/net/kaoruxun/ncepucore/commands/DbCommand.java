package net.kaoruxun.ncepucore.commands;

import net.kaoruxun.ncepucore.Main;
import net.kaoruxun.ncepucore.utils.DatabaseSingleton;
import net.kaoruxun.ncepucore.utils.Serializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;

@CommandName("db")
public final class DbCommand extends BasicCommand {
    public DbCommand(Main main) {
        super(main);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean callback(CommandSender sender, String[] args) {
        if (args.length < 1) return false;
        switch (args[0]) {
            case "getall": {
                DatabaseSingleton.INSTANCE.forEach(i -> {
                    String key = asString(i.getKey());
                    sender.sendMessage(key + " = " + (isLocation(key) ? Serializer.deserializeLocation(i.getValue()).toString() : asString(i.getValue())));
                });
            }
            return true;
            case "get": {
                if (args.length < 2) return false;
                byte[] bytes = DatabaseSingleton.INSTANCE.get(args[1].getBytes());
                if (bytes != null)
                    sender.sendMessage(isLocation(args[1]) ? Serializer.deserializeLocation(bytes).toString() : asString(bytes));
                else sender.sendMessage("§c" + args[1] + "is null!");
            }
            return true;
            case "player": {
                if (args.length < 2) return false;
                final OfflinePlayer offlinePlayer = instance.getServer().getOfflinePlayer(args[1]);
                if (!offlinePlayer.hasPlayedBefore()) {
                    sender.sendMessage("§c该玩家还从未在服务器游玩过!");
                    return true;
                }
                final Player player = offlinePlayer.getPlayer();
                assert player != null;
                if (args.length == 2) { // db player <player> // get all data of a player
                    sender.sendMessage(player.getName() + ":");
                    DatabaseSingleton.INSTANCE.filter(i -> asString(i.getKey()).startsWith(player.getUniqueId().toString())).forEach(it -> {
                        String key = asString(it.getKey()).split("\\.")[1];
                        byte[] bytes = it.getValue();
                        String value = isLocation(key)?Serializer.deserializeLocation(bytes).toString():asString(bytes);
                        sender.sendMessage("  " + key + " = " + value);
                    });
                    return true;
                }
                if (args.length > 3) { // db player <player> get <prop> // get <prop> of a player
                    if (!Objects.equals(args[2], "get")) return false;
                    for (int i = 3; i < args.length; i++) {
                        byte[] bytes = DatabaseSingleton.INSTANCE.getPlayerData(player, args[i]);
                        if (bytes != null) {
                            sender.sendMessage(player.getName() + "." + args[i] + ":");
                            sender.sendMessage("  " + (isLocation(args[i]) ?
                                    Serializer.deserializeLocation(bytes).toString() :
                                    asString(bytes)));
                        } else sender.sendMessage("§c" + player.getName() + "." + args[i] + " is null!");
                    }
                    return true;
                }
            }
        }
        return true;
    }

    boolean isLocation(String key) {
        return key.endsWith("home") || key.endsWith("Location");
    }
}
