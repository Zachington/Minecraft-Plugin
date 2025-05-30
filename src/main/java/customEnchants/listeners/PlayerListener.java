package customEnchants.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import customEnchants.TestEnchants;
import customEnchants.utils.EssenceManager;
import customEnchants.utils.RankUtils;

public class PlayerListener implements Listener {

    private final EssenceManager essenceManager;

    public PlayerListener(EssenceManager essenceManager) {
        this.essenceManager = essenceManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if rank is already set in NBT
        PersistentDataContainer container = player.getPersistentDataContainer();
    if (!container.has(RankUtils.rankKey, PersistentDataType.STRING)) {
        RankUtils.setRank(player, "a");  // Set initial rank to 'a' only if no rank is set
        System.out.println("[Join] Rank not found, setting to 'a' for " + player.getName());
    } else {
        String currentRank = RankUtils.getRank(player);
        System.out.println("[Join] Player " + player.getName() + " rank on join: " + currentRank);
    }

        // Schedule scoreboard update
        Bukkit.getScheduler().runTaskLater(TestEnchants.getInstance(), () -> {
    TestEnchants.getInstance().getScoreboardUtil().updateScoreboard(player);
}, 20L);

        // Preload extractor inventory into memory
        essenceManager.preloadExtractorInventory(player);
    }
}
