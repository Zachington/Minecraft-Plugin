package customEnchants.listeners;

import customEnchants.utils.HeldToolInfo;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class MagnetListener implements Listener {

    private static final double MAGNET_RANGE = 5.0; // base range in blocks
    private static final double MAGNET_PULL_SPEED = 0.1; // velocity multiplier


    // Pull items when they spawn near players holding Magnet
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();

        List<Player> nearbyPlayers = item.getNearbyEntities(MAGNET_RANGE, MAGNET_RANGE, MAGNET_RANGE).stream()
            .filter(e -> e instanceof Player)
            .map(e -> (Player) e)
            .collect(Collectors.toList());

        for (Player player : nearbyPlayers) {
            ItemStack tool = player.getInventory().getItemInMainHand();
            if (tool == null) continue;

            HeldToolInfo info = HeldToolInfo.fromItem(tool);
            int level = info.getLevel("Magnet");
            if (level > 0) {
                pullItemToPlayer(item, player, level);
                break; // only pull to the nearest one player
            }
        }
    }

    // Periodically pull items when player moves
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null) return;

        HeldToolInfo info = HeldToolInfo.fromItem(tool);
        int level = info.getLevel("Magnet");
        if (level <= 0) return;

        double range = MAGNET_RANGE + level; // range scales with level

        List<Item> nearbyItems = player.getNearbyEntities(range, range, range).stream()
            .filter(e -> e instanceof Item)
            .map(e -> (Item) e)
            .collect(Collectors.toList());

        for (Item item : nearbyItems) {
            pullItemToPlayer(item, player, level);
        }
    }

    private void pullItemToPlayer(Item item, Player player, int level) {
        Vector toPlayer = player.getLocation().toVector().subtract(item.getLocation().toVector());
        double distance = toPlayer.length();
        if (distance < 1) return; // close enough, no need to pull

        toPlayer.normalize();
        double speed = MAGNET_PULL_SPEED * level;
        Vector velocity = toPlayer.multiply(speed);

        item.setVelocity(velocity);
    }
}
