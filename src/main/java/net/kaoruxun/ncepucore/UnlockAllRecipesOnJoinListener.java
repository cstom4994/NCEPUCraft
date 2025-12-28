package net.kaoruxun.ncepucore;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// 玩家进入服务器时解锁全部可用配方
final class UnlockAllRecipesOnJoinListener implements Listener {
    private final List<NamespacedKey> allRecipeKeys;

    UnlockAllRecipesOnJoinListener() {
        this.allRecipeKeys = snapshotAllRecipeKeys();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerJoinUnlockAllRecipes(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        // 批量解锁
        player.discoverRecipes(allRecipeKeys);
    }

    private static List<NamespacedKey> snapshotAllRecipeKeys() {
        final ArrayList<NamespacedKey> keys = new ArrayList<>();
        final Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            final Recipe recipe = it.next();
            if (!(recipe instanceof Keyed keyed)) continue;
            final NamespacedKey key = keyed.getKey();
            if (key != null) keys.add(key);
        }
        return List.copyOf(keys);
    }
}


