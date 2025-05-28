package customEnchants.listeners;

import customEnchants.utils.StatTracker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final StatTracker stats;

    public PlayerQuitListener(StatTracker stats) {
        this.stats = stats;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stats.save();
    }
}