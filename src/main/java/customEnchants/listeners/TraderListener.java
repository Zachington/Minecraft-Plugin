package customEnchants.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import customEnchants.TestEnchants;
import customEnchants.managers.QuestManager;
import customEnchants.managers.RankManager;
import customEnchants.utils.GuiUtil;
import customEnchants.utils.RankQuest;
import customEnchants.utils.StatTracker;
import customEnchants.utils.customItemUtil;
import net.milkbowl.vault.economy.Economy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TraderListener implements Listener {

    private final StatTracker statTracker;
    private final QuestManager questManager;
    private final Economy economy;

    public TraderListener (StatTracker statTracker, QuestManager questManager, Economy economy) {
        this.statTracker = statTracker;
        this.questManager = questManager;
        this.economy = economy;
    };

    @EventHandler
    public void onVillagerInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (!(entity instanceof Villager villager)) return;

        String name = villager.getCustomName();
        if (name == null) return;

        name = ChatColor.stripColor(name);

        event.setCancelled(true); // Cancel default villager trading

        if (name.equalsIgnoreCase("Essence Trader")) {
            GuiUtil.openEssenceShardGUI(player);
        } else if (name.equalsIgnoreCase("Deep Essence Trader")) {
            GuiUtil.openDeepEssenceShardGUI(player);
        } else if (name.equalsIgnoreCase("Tinkerer")) {
            Inventory gui = GuiUtil.createScrapGui(player);
            player.openInventory(gui);
        } else if (name.equalsIgnoreCase("Deep Extractor Trader")) {
            GuiUtil.openDeepExtractorCraftingGUI(player);
        } else if (name.equalsIgnoreCase("Extractor Trader")) {
            GuiUtil.openExtractorCraftingGUI(player);
        } else if (name.equalsIgnoreCase("Furnace Upgrader")) {
            player.openInventory(GuiUtil.getFurnaceUpgradeGUI(player));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;

    ItemStack clicked = event.getCurrentItem();
    if (clicked == null || clicked.getType() == Material.AIR) return;

    String title = event.getView().getTitle();

    if (event.getClickedInventory() == player.getInventory()) return;

    if (title.equals(ChatColor.GRAY + "Furnace Upgrades")) {
    event.setCancelled(true);
    
    ItemMeta meta = clicked.getItemMeta();
    if (meta == null || !meta.hasDisplayName() || !meta.hasLore()) return;
    
    List<String> lore = meta.getLore();

    // Parse money cost from lore (assumes line with $ in it)
    double moneyCost = 0;
    for (String line : lore) {
        if (line.contains("$")) {
            String cleaned = ChatColor.stripColor(line).replace("-", "").replace("$", "").trim();
            try {
                moneyCost = Double.parseDouble(cleaned.replace(",", ""));
            } catch (NumberFormatException ignored) {}
        }
    }
    
    // Parse required materials from lore lines (skip first line Double Smelt Chance, start at line 2+)
    Map<Material, Integer> requiredMaterials = new HashMap<>();
    Map<String, Integer> requiredCustomItems = new HashMap<>();
    
    boolean inCostSection = false;
    for (String line : lore) {
        String clean = ChatColor.stripColor(line).trim();
        if (clean.equalsIgnoreCase("Crafting Cost:")) {
            inCostSection = true;
            continue;
        }
        if (inCostSection) {
            if (clean.startsWith("- ")) {
                String itemName = clean.substring(2).trim();
                if (itemName.startsWith("$") || itemName.matches("\\$?\\d+[kKmMbB]?.*")) continue;

                Material mat = getMaterialFromName(itemName);
                if (mat != null) {
                    requiredMaterials.put(mat, requiredMaterials.getOrDefault(mat, 0) + 1);
                } else {
                    requiredCustomItems.put(itemName, requiredCustomItems.getOrDefault(itemName, 0) + 1);
                }
            } else {
                // end of cost section if next line doesn't start with "-"
                break;
            }
        }
    }
    
    // Check if player has enough money
    if (!economy.has(player, moneyCost)) {
        player.sendMessage(ChatColor.RED + "You need $" + String.format("%,.0f", moneyCost) + " to upgrade this furnace.");
        return;
    }
    
    // Check if player has required materials (uses your existing helper)
    if (!hasEnoughItems(player, requiredMaterials, requiredCustomItems)) {
        player.sendMessage(ChatColor.RED + "You don't have the required materials to upgrade this furnace.");
        return;
    }

    String displayName = ChatColor.stripColor(meta.getDisplayName()); // e.g. "Tier 3 Furnace" or "Blast Furnace"

        int currentTier = 0;
        if (displayName.equalsIgnoreCase("Blast Furnace")) {
            currentTier = 7;
        } else if (displayName.startsWith("Tier ")) {
            try {
                String tierStr = displayName.split(" ")[1]; // "3" from "Tier 3 Furnace"
                currentTier = Integer.parseInt(tierStr);
        } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Invalid furnace tier!");
        return;
        }
        } else {
            player.sendMessage(ChatColor.RED + "Unknown furnace tier!");
        return;
        }

        String itemKey;
        if (currentTier == 7) {
            itemKey = "Blast Furnace"; // Or whatever key your customItemUtil expects
        } else {
            itemKey = "Tier " + currentTier + " Furnace";
        }

        ItemStack upgradedFurnace = customItemUtil.createCustomItem(itemKey);
        if (upgradedFurnace == null) {
            player.sendMessage(ChatColor.RED + "Failed to create the upgraded furnace item!");
        return;
        }
    
    // Remove money and items
    economy.withdrawPlayer(player, moneyCost);
    removeItems(player, requiredMaterials, requiredCustomItems);
    
    // Give the upgraded furnace
    player.getInventory().addItem(upgradedFurnace);
    player.sendMessage(ChatColor.GREEN + "You upgraded your furnace and paid $" + String.format("%,.0f", moneyCost));
}

    boolean isShardGUI = title.equals("Essence Shards") || title.equals("Deep Essence Shards");
    boolean isExtractorGUI = title.equals("Extractor Crafting") || title.equals("Deep Extractor Crafting");

    if (!isShardGUI && !isExtractorGUI) return;

    event.setCancelled(true);

    ItemMeta meta = clicked.getItemMeta();
    if (meta == null || !meta.hasLore() || !meta.hasDisplayName()) return;

    List<String> lore = meta.getLore();

    Map<Material, Integer> requiredMaterials = new HashMap<>();
    Map<String, Integer> requiredCustomItems = new HashMap<>();

    // Parse lore lines for required items
    for (String line : lore) {
        String stripped = ChatColor.stripColor(line).trim();
        if (!stripped.matches("\\d+ .+")) continue;

        String[] parts = stripped.split(" ", 2);
        int amount;
        try {
            amount = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            continue; // skip malformed line
        }

        String itemName = parts[1];

        Material material = getMaterialFromName(itemName);
        if (material != null) {
            requiredMaterials.merge(material, amount, Integer::sum);
        } else {
            requiredCustomItems.merge(itemName, amount, Integer::sum);
        }
    }

    // Check if player has enough items
    if (!hasEnoughItems(player, requiredMaterials, requiredCustomItems)) {
        player.sendMessage(ChatColor.RED + "You don't have the required materials.");
        
        return;
    }

    // Remove required items from inventory
    removeItems(player, requiredMaterials, requiredCustomItems);

    // Create result item
    String displayName = ChatColor.stripColor(meta.getDisplayName());
    ItemStack result = customItemUtil.createCustomItem(displayName); // shared for shard & extractor
    if (result == null) {
        player.sendMessage(ChatColor.RED + "That item is not registered.");
        return;
    }

    player.getInventory().addItem(result);
    player.sendMessage(ChatColor.GREEN + "You crafted a " + meta.getDisplayName() + ChatColor.GREEN + "!");

    UUID uuid = player.getUniqueId();
    StatTracker tracker = TestEnchants.getInstance().getStatTracker();

    // Update stats
    if (isShardGUI) {
        int current = tracker.getPlayerStat(uuid, "crafted_essence", false);
        tracker.setPlayerStat(uuid, "crafted_essence", current + 1, false);
    } else if (isExtractorGUI) {
        int current = tracker.getPlayerStat(uuid, "crafted_extractors", false);
        tracker.setPlayerStat(uuid, "crafted_extractors", current + 1, false);
    }

    // Quest check only for shard quests (you can add more later for extractors)
    if (isShardGUI || isExtractorGUI) {
    Set<String> activeQuests = questManager.getActiveQuests(player);

    for (String questKey : activeQuests) {
        RankQuest quest = questManager.get(questKey);
        if (quest == null || quest.extraObjective == null) continue;

        if (isShardGUI && quest.extraObjective.startsWith("craft_shard:")) {
            int shardsNeeded = Integer.parseInt(quest.extraObjective.split(":")[1]);

            String baseRankKey = RankManager.getRank(player); // e.g., "a" or "p1a"
            String rankedKey = "crafted_essence_at_rank_start." + baseRankKey;

            int craftedAtStart = tracker.getPlayerStat(uuid, rankedKey, false);
            int craftedNow = tracker.getPlayerStat(uuid, "crafted_essence", false);
            int craftedSinceRank = craftedNow - craftedAtStart;

            if (craftedSinceRank >= shardsNeeded) {
                questManager.completeQuest(player, questKey);
                player.sendMessage("§aQuest complete: Craft " + shardsNeeded + " essence shards!");
            }
        }

        if (isExtractorGUI && quest.extraObjective.startsWith("craft_extractor:")) {
            int extractorsNeeded = Integer.parseInt(quest.extraObjective.split(":")[1]);

            String baseRankKey = RankManager.getRank(player);
            String rankedKey = "crafted_extractors_at_rank_start." + baseRankKey;

            int craftedAtStart = tracker.getPlayerStat(uuid, rankedKey, false);
            int craftedNow = tracker.getPlayerStat(uuid, "crafted_extractors", false);
            int craftedSinceRank = craftedNow - craftedAtStart;

            if (craftedSinceRank >= extractorsNeeded) {
                questManager.completeQuest(player, questKey);
                player.sendMessage("§aQuest complete: Craft " + extractorsNeeded + " extractors!");
            }
        }
    }
}

}


    private Material getMaterialFromName(String name) {
    name = name.toLowerCase().trim();
    if (loreNameToMaterial.containsKey(name)) {
        return loreNameToMaterial.get(name);
    }
    // fallback: try Material enum names with uppercase and underscores
    try {
        return Material.valueOf(name.toUpperCase().replace(" ", "_"));
    } catch (IllegalArgumentException e) {
        return null;
    }
}

    private boolean hasEnoughItems(Player player, Map<Material, Integer> requiredMaterials, Map<String, Integer> requiredCustomItems) {
        // Check materials
        for (Map.Entry<Material, Integer> entry : requiredMaterials.entrySet()) {
            int needed = entry.getValue();
            int total = countMaterialIncludingBlocks(player, entry.getKey());
            if (total < needed) return false;
        }

        // Check custom items
        for (Map.Entry<String, Integer> entry : requiredCustomItems.entrySet()) {
            int needed = entry.getValue();
            int total = countCustomItems(player, entry.getKey());
            if (total < needed) return false;
        }
        return true;
    }

    private int countMaterialIncludingBlocks(Player player, Material material) {
    int total = 0;
    Material blockForm = Material.getMaterial(material.name() + "_BLOCK");

    for (ItemStack item : player.getInventory().getContents()) {
        if (item == null) continue;

        // Only count items that have NO custom display name or lore
        ItemMeta meta = item.getItemMeta();
        boolean isCustom = (meta != null && (meta.hasDisplayName() || meta.hasLore()));

        if (isCustom) continue; // skip custom items

        if (item.getType() == material) total += item.getAmount();
        if (blockForm != null && item.getType() == blockForm) total += item.getAmount() * 9;
    }
    return total;
}

    private int countCustomItems(Player player, String name) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;
            ItemMeta meta = item.getItemMeta();
            String display = ChatColor.stripColor(meta.getDisplayName()).trim();
            if (meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(name)) {
                Bukkit.getLogger().info("Looking for: " + name + ", found: " + display);
                total += item.getAmount();
            }
        }
        return total;
    }

    private void removeItems(Player player, Map<Material, Integer> requiredMaterials, Map<String, Integer> requiredCustomItems) {
    PlayerInventory inv = player.getInventory();

    // Remove materials prioritizing blocks first
    for (Map.Entry<Material, Integer> entry : requiredMaterials.entrySet()) {
        Material material = entry.getKey();
        int toRemove = entry.getValue();

        Material blockForm = Material.getMaterial(material.name() + "_BLOCK");

        // Remove block form first (each block counts as 9 units)
        for (int i = 0; i < inv.getSize(); i++) {
            if (toRemove <= 0) break;
            ItemStack item = inv.getItem(i);
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            boolean isCustom = (meta != null && (meta.hasDisplayName() || meta.hasLore()));
            if (isCustom) continue; // skip custom items

            if (blockForm != null && item.getType() == blockForm) {
                int blocksAvailable = item.getAmount();
                int unitsAvailable = blocksAvailable * 9;

                int unitsToRemove = Math.min(unitsAvailable, toRemove);
                int blocksToRemove = unitsToRemove / 9;
                if (unitsToRemove % 9 != 0) blocksToRemove++; // Remove an extra block if not exact multiple

                blocksToRemove = Math.min(blocksToRemove, blocksAvailable);

                item.setAmount(blocksAvailable - blocksToRemove);
                if (item.getAmount() <= 0) inv.clear(i);

                toRemove -= blocksToRemove * 9;
            }
        }

        // Then remove the material itself (units = 1 each)
        for (int i = 0; i < inv.getSize(); i++) {
            if (toRemove <= 0) break;
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() != material) continue;

            ItemMeta meta = item.getItemMeta();
            boolean isCustom = (meta != null && (meta.hasDisplayName() || meta.hasLore()));
            if (isCustom) continue; // skip custom items

            int amtAvailable = item.getAmount();
            int amtToRemove = Math.min(amtAvailable, toRemove);

            item.setAmount(amtAvailable - amtToRemove);
            if (item.getAmount() <= 0) inv.clear(i);

            toRemove -= amtToRemove;
        }
    }

    // Remove custom items (exact match by stripped display name)
    for (Map.Entry<String, Integer> entry : requiredCustomItems.entrySet()) {
        String name = entry.getKey();
        int toRemove = entry.getValue();

        for (int i = 0; i < inv.getSize(); i++) {
            if (toRemove <= 0) break;
            ItemStack item = inv.getItem(i);
            if (item == null || !item.hasItemMeta()) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(name)) {
                int amtAvailable = item.getAmount();
                int amtToRemove = Math.min(amtAvailable, toRemove);

                item.setAmount(amtAvailable - amtToRemove);
                if (item.getAmount() <= 0) inv.clear(i);

                toRemove -= amtToRemove;
            }
        }
    }
}

    private static final Map<String, Material> loreNameToMaterial = new HashMap<>();

    static {
    loreNameToMaterial.put("copper", Material.COPPER_INGOT);
    loreNameToMaterial.put("iron", Material.IRON_INGOT);
    loreNameToMaterial.put("gold", Material.GOLD_INGOT);
    loreNameToMaterial.put("lapis", Material.LAPIS_LAZULI);
    loreNameToMaterial.put("diamond", Material.DIAMOND);
    loreNameToMaterial.put("coal", Material.COAL);
    loreNameToMaterial.put("redstone", Material.REDSTONE);
    loreNameToMaterial.put("emerald", Material.EMERALD);
    loreNameToMaterial.put("amethyst", Material.AMETHYST_BLOCK);
    // Add more if needed
}


}
