package customEnchants.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.Listener;

import customEnchants.utils.StatTracker;

public class GeneralBlockBreakListener implements Listener {
    private final StatTracker stats;

    public GeneralBlockBreakListener(StatTracker stats) {
        this.stats = stats;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        String worldName = event.getBlock().getWorld().getName();
    if (!worldName.equals("world") && !worldName.equals("mine_world")) {
        return; // Ignore breaks outside these worlds
    }

        UUID uuid = event.getPlayer().getUniqueId();

        stats.incrementPlayerStat(uuid, "blocks_broken");
        stats.incrementServerStat("blocks_broken");
        stats.save(); // Optional: or call this periodically instead
    }
}
