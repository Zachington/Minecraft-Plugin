package customEnchants.listeners;

import customEnchants.TestEnchants;
import customEnchants.managers.QuestManager;
import customEnchants.utils.EssenceManager;
import customEnchants.utils.RankQuest;
import customEnchants.utils.StatTracker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

public class QuestListener implements Listener {

    private final QuestManager questManager;
    private final EssenceManager essenceManager;
    private final StatTracker statTracker;
    

    public QuestListener(QuestManager questManager, EssenceManager essenceManager, StatTracker statTracker) {
        this.questManager = questManager;
        this.essenceManager = essenceManager;
        this.statTracker = statTracker;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();

    Set<String> activeQuests = questManager.getActiveQuests(player);
    int currentBlocks = TestEnchants.getInstance().statTracker.getPlayerStat(uuid, "blocks_broken", false);
    int currentEssence = TestEnchants.getInstance().statTracker.getPlayerStat(uuid, "earned_essence", false);

    for (String questKey : activeQuests) {
        RankQuest quest = questManager.get(questKey);
        if (quest == null) continue;

        // Extract base rank key from questKey (like "a-b" from "a-b-quest1")
        String[] parts = questKey.split("-quest");
        if (parts.length == 0) continue;
        String baseRankKey = parts[0];

        // Check blocks broken requirement if applicable
        if (quest.blocksRequired > 0) {
            int blocksAtStart = TestEnchants.getInstance().statTracker.getPlayerStat(uuid, "blocks_broken_at_rank_start." + baseRankKey, false);
            int blocksSinceStart = currentBlocks - blocksAtStart;

            if (blocksSinceStart >= quest.blocksRequired) {
                questManager.completeQuest(player, questKey);
                player.sendMessage("§aQuest complete: Mine " + quest.blocksRequired + " blocks!");
                continue;  // Quest done, no need to check essence for this questKey
            }
        }

        // Check essence earned requirement if the extraObjective contains essence
        if (quest.extraObjective != null && quest.extraObjective.startsWith("earn_essence:")) {
            int essenceNeeded = Integer.parseInt(quest.extraObjective.split(":")[1]);
            int essenceAtStart = TestEnchants.getInstance().statTracker.getPlayerStat(uuid, "earned_essence_at_rank_start." + baseRankKey, false);
            int essenceSinceStart = currentEssence - essenceAtStart;

            if (essenceSinceStart >= essenceNeeded) {
                questManager.completeQuest(player, questKey);
                player.sendMessage("§aQuest complete: Earn " + essenceNeeded + " essence!");
            }
        }
    }
}

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();

    if (QuestManager.hasQuest(player, "a-b-quest2")) {
        ItemStack[] storedExtractorItems = essenceManager.getStoredExtractorContents(player);

        boolean hasAnyItem = false;
        for (ItemStack item : storedExtractorItems) {
            if (item != null) {
                hasAnyItem = true;
                break;
            }
        }

        if (hasAnyItem) {
            questManager.completeQuest(player, "a-b-quest2");
            player.sendMessage("§aQuest complete: Equip Extractor!"); 
        }
    }
}

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;

    // Check GUI title exactly (including color codes)
    if (!event.getView().getTitle().equals("§2Active Quests")) return;

    // Check if player is viewing quests (make sure this metadata is set when GUI opens)
    if (!player.hasMetadata("viewing_quests")) return;

    // Cancel any interaction in the top inventory (the quest GUI)
    if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
        event.setCancelled(true);
    }

    // Optionally cancel shift-clicks anywhere (to prevent item moving into GUI)
    if (event.isShiftClick()) {
        event.setCancelled(true);
    }
}

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
    Player player = (Player) event.getPlayer();
    if (player.hasMetadata("viewing_quests")) {
        player.removeMetadata("viewing_quests", TestEnchants.getInstance());
    }
}







}
