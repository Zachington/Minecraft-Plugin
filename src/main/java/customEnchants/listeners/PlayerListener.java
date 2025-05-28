package customEnchants.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Bukkit;


import customEnchants.TestEnchants;
import customEnchants.utils.ScoreboardUtil;

public class PlayerListener implements Listener {
    
    @EventHandler
public void onJoin(PlayerJoinEvent event) {
    Bukkit.getScheduler().runTaskLater(TestEnchants.getInstance(), () -> {
        ScoreboardUtil.updateScoreboard(event.getPlayer());
    }, 20L); // wait 1 second for Vault to fully load
}
}