package customEnchants.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import customEnchants.TestEnchants;
import customEnchants.managers.QuestManager;
import customEnchants.utils.EssenceManager;
import customEnchants.utils.RankUtils;

public class PlayerListener implements Listener {

    private final EssenceManager essenceManager;
    private final QuestManager questManager;
    private final TestEnchants testEnchants;

    public PlayerListener(EssenceManager essenceManager, QuestManager questManager, TestEnchants testEnchants) {
        this.essenceManager = essenceManager;
        this.questManager = questManager;
        this.testEnchants = testEnchants;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer container = player.getPersistentDataContainer();
        NamespacedKey nvkey = new NamespacedKey(testEnchants, "night_vision_enabled");

        if (container.has(nvkey, PersistentDataType.INTEGER) && container.get(nvkey, PersistentDataType.INTEGER) == 1) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
    }

    if (!container.has(RankUtils.rankKey, PersistentDataType.STRING)) {
        RankUtils.setRank(player, "a");  // Set initial rank to 'a' only if no rank is set
        System.out.println("[Join] Rank not found, setting to 'a' for " + player.getName());
    } else {
        String currentRank = RankUtils.getRank(player);
        System.out.println("[Join] Player " + player.getName() + " rank on join: " + currentRank);
    }

    if (!player.hasPlayedBefore()) {
        Location spawnA = new Location(Bukkit.getWorld("world"), -34, 0, -62);
        player.teleport(spawnA);
        if (questManager.getActiveQuests(player).isEmpty()) {
    String baseRank = "a";
    String nextRank = "b"; // hardcoded for first rank-up

    String quest1Key = baseRank + "-" + nextRank + "-quest1";
    String quest2Key = baseRank + "-" + nextRank + "-quest2";

    if (QuestManager.rankQuests.containsKey(quest1Key)) {
        questManager.addQuest(player, quest1Key);
    }
    if (QuestManager.rankQuests.containsKey(quest2Key)) {
        questManager.addQuest(player, quest2Key);
    }

    UUID uuid = player.getUniqueId();
    var statTracker = TestEnchants.getInstance().statTracker;

    // Save initial stat baselines for tracking
    statTracker.setPlayerStat(uuid, "blocks_broken_at_rank_start." + baseRank + "-" + nextRank,
        statTracker.getPlayerStat(uuid, "blocks_broken", false), false);

    statTracker.setPlayerStat(uuid, "earned_essence_at_rank_start." + baseRank + "-" + nextRank,
        statTracker.getPlayerStat(uuid, "earned_essence", false), false);

    statTracker.setPlayerStat(uuid, "filler_sold_at_rank_start." + baseRank + "-" + nextRank,
        statTracker.getPlayerStat(uuid, "filler_sold", false), false);

    statTracker.setPlayerStat(uuid, "total_crates_at_rank_start." + baseRank + "-" + nextRank,
        statTracker.getPlayerStat(uuid, "crate_total", false), false);

    for (String rarity : new String[]{"common", "uncommon", "rare", "epic", "legendary"}) {
        String key = "enchants_applied_" + rarity;
        statTracker.setPlayerStat(uuid, key + "_at_rank_start." + baseRank + "-" + nextRank,
            statTracker.getPlayerStat(uuid, key, false), false);
    }

    System.out.println("[Join] Assigned a-b quests to " + player.getName());
}
    }

        // Schedule scoreboard update
        Bukkit.getScheduler().runTaskLater(TestEnchants.getInstance(), () -> {
        TestEnchants.getInstance().getScoreboardUtil().updateScoreboard(player);
        }, 20L);

        // Preload extractor inventory into memory
        essenceManager.preloadExtractorInventory(player);

    
    }
}
