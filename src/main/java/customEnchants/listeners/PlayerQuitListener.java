package customEnchants.listeners;

import customEnchants.managers.VaultManager;
import customEnchants.utils.StatTracker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final StatTracker stats;
    private final VaultManager vaultManager;

    public PlayerQuitListener(StatTracker stats, VaultManager vaultManager) {
    this.stats = stats;
    this.vaultManager = vaultManager;
}


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stats.save();
        vaultManager.saveVault(event.getPlayer().getUniqueId());
    }
}