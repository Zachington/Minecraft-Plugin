package customEnchants.listeners;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.event.block.Action;

import customEnchants.TestEnchants;
import customEnchants.managers.QuestManager;
import customEnchants.utils.GuiUtil;
import customEnchants.utils.RankQuest;
import customEnchants.utils.StatTracker;
import customEnchants.utils.crateTableUtil;
import customEnchants.utils.customItemUtil;
import customEnchants.utils.crateTableUtil.LootEntry;

public class CrateListener implements Listener {


    private final World world;
    private final int xMin, xMax, yMin, yMax, zMin, zMax;
    private final Map<Player, String> guiOpenMethod = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final Map<Player, Long> shiftRightCooldown = new HashMap<>();
    private final StatTracker statTracker;

    public CrateListener(World world, int xMin, int xMax, int yMin, int yMax, int zMin, int zMax, StatTracker statTracker) {
        this.world = world;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.zMin = zMin;
        this.zMax = zMax;
        this.statTracker = statTracker;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Location loc = event.getClickedBlock().getLocation();

        if (!loc.getWorld().equals(world)) return;
        if (loc.getBlockX() < xMin || loc.getBlockX() > xMax) return;
        if (loc.getBlockY() < yMin || loc.getBlockY() > yMax) return;
        if (loc.getBlockZ() < zMin || loc.getBlockZ() > zMax) return;

        Player player = event.getPlayer();
        Material clickedType = event.getClickedBlock().getType();
        boolean isSneaking = player.isSneaking();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.ENCHANTING_TABLE) {
            event.setCancelled(true);
        }
        
        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK:
                if (handleLeftClick(player, clickedType)) {
                    event.setCancelled(true);
                }
                break;

            case RIGHT_CLICK_BLOCK:
                if (isSneaking) {
                    long now = System.currentTimeMillis();
                    Long lastUse = shiftRightCooldown.getOrDefault(player, 0L);
                    if (now - lastUse < 300) return;
                    shiftRightCooldown.put(player, now);
                    if (handleShiftRightClick(player, clickedType, loc)) {
                        event.setCancelled(true);
                    }
            } else {
                if (handleRightClick(player, clickedType, loc)) {
                    event.setCancelled(true);
                    UUID uuid = player.getUniqueId();
                    TestEnchants.getInstance().statTracker.incrementPlayerStat(uuid, "crate_total", 1);

                    // Check for active crate quests
                    QuestManager questManager = TestEnchants.getInstance().getQuestManager();
                    Set<String> activeQuests = questManager.getActiveQuests(player);
                    int currentCrates = TestEnchants.getInstance().statTracker.getPlayerStat(uuid, "crate_total", false);

                    for (String questKey : activeQuests) {
                        RankQuest quest = questManager.get(questKey);
                        if (quest == null || quest.extraObjective == null || !quest.extraObjective.startsWith("open_crates:")) continue;

                        String[] parts = quest.extraObjective.split(":");
                        int required = Integer.parseInt(parts[1]);
                        String baseRank = questKey.split("-quest")[0];

                        int cratesAtStart = TestEnchants.getInstance().statTracker.getPlayerStat(uuid, "total_crates_at_rank_start." + baseRank, false);
                        int cratesSinceStart = currentCrates - cratesAtStart;

                    if (cratesSinceStart >= required) {
                        questManager.completeQuest(player, questKey);
                        player.sendMessage("§aQuest complete: Opened " + required + " crates!");
                    }
                    }
                }
            }
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) return;

    InventoryView view = event.getView();
    String title = view.getTitle();

    if (title.equals("§5Enchanter Crate") 
    || title.equals("§3Mining Crate") 
    || title.equals("§1Prison Crate") 
    || title.equals("§cDivine Crate") 
    || title.equals("§8Durability Crate")) {

        int rawSlot = event.getRawSlot();
        int topSize = view.getTopInventory().getSize();

        // Cancel any click inside the GUI slots
        if (rawSlot < topSize) {
            event.setCancelled(true);
            return;
        }

        // Also cancel shift-clicks that try to put items into the GUI
        if (event.isShiftClick()) {
            ItemStack cursorItem = event.getCurrentItem();
            if (cursorItem != null) {
                // Prevent shift-click if it would place items into the GUI
                event.setCancelled(true);
            }
        }
    }
}

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
    InventoryView view = event.getView();
    Player player = (Player) event.getPlayer();

    String title = view.getTitle();

    if (title.equals("§5Enchanter Crate")) {
        String method = guiOpenMethod.remove(player);
        if (!"RIGHT_CLICK".equals(method)) return;
        handleLootDistribution(player, view.getTopInventory(), "Enchant Key");
    }
    else if (title.equals("§3Mining Crate")) {
        String method = guiOpenMethod.remove(player);
        if (!"RIGHT_CLICK".equals(method)) return;
        handleLootDistribution(player, view.getTopInventory(), "Mining Key");
    }
    else if (title.equals("§8Durability Crate")) {
        String method = guiOpenMethod.remove(player);
        if (!"RIGHT_CLICK".equals(method)) return;
        handleLootDistribution(player, view.getTopInventory(), "Durability Key");
    }
    else if (title.equals("§cDivine Crate")) {
        String method = guiOpenMethod.remove(player);
        if (!"RIGHT_CLICK".equals(method)) return;
        handleLootDistribution(player, view.getTopInventory(), "Divine Key");
    }
    else if (title.equals("§1Prison Crate")) {
        String method = guiOpenMethod.remove(player);
        if (!"RIGHT_CLICK".equals(method)) return;
        handleLootDistribution(player, view.getTopInventory(), "Prison Key");
    }
}

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
    String title = event.getView().getTitle();
    String strippedTitle = ChatColor.stripColor(title);

    if (strippedTitle.equals("Mining Crate") 
            || strippedTitle.equals("Enchanter Crate")) {
        int guiSize = event.getView().getTopInventory().getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < guiSize) {
                event.setCancelled(true);
                break;
            }
        }
    }
}

    private void handleLootDistribution(Player player, Inventory topInv, String lootTableName) {
    ItemStack chosenItem = getRandomLoot(lootTableName);
    if (chosenItem == null) {
        player.sendMessage("No loot available.");
        return;
    }

    if (chosenItem.hasItemMeta()) {
        ItemMeta meta = chosenItem.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>(meta.getLore());
            int randomChance = 1 + new Random().nextInt(100);
            for (int i = 0; i < lore.size(); i++) {
                String line = ChatColor.stripColor(lore.get(i));
                if (line.startsWith("Success Rate:")) {
                    lore.set(i, ChatColor.GREEN + "Success Rate: " + randomChance + "%");
                    break;
                }
            }
            meta.setLore(lore);
            chosenItem.setItemMeta(meta);
        }
    }

    int foundSlot = -1;
    ItemMeta chosenMeta = chosenItem.hasItemMeta() ? chosenItem.getItemMeta() : null;
    String chosenName = (chosenMeta != null && chosenMeta.hasDisplayName())
        ? ChatColor.stripColor(chosenMeta.getDisplayName())
        : null;

    for (int i = 0; i < topInv.getSize(); i++) {
    ItemStack item = topInv.getItem(i);
    if (item == null) continue;

    // Match by display name if available
    if (chosenItem.hasItemMeta() && chosenItem.getItemMeta().hasDisplayName()) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) continue;

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (chosenName != null && displayName.equalsIgnoreCase(chosenName)) {
            foundSlot = i;
            break;
        }
    } else {
    // Fallback to matching by Material and amount
    if (item.getType() == chosenItem.getType() && item.getAmount() == chosenItem.getAmount()) {
        // Strip lore line containing "Chance:" or "Success Rate:" from the chosenItem before placing
        if (chosenItem.hasItemMeta()) {
            ItemMeta meta = chosenItem.getItemMeta();
            if (meta.hasLore()) {
                List<String> lore = new ArrayList<>(meta.getLore());
                lore.removeIf(line -> {
                    String stripped = ChatColor.stripColor(line).toLowerCase();
                    return stripped.contains("chance:") || stripped.contains("success rate:");
                });
                meta.setLore(lore);
                chosenItem.setItemMeta(meta);
            }
        }

        foundSlot = i;
        break;
    }
}
}


    if (foundSlot == -1) {
        return;
    }

    final int chosenSlot = foundSlot;
    List<Integer> slotsToChange = new ArrayList<>();
    for (int i = 0; i < topInv.getSize(); i++) {
        if (i != chosenSlot) {
            ItemStack item = topInv.getItem(i);
            if (item != null && item.getType() == Material.GRAY_STAINED_GLASS_PANE) continue;
            slotsToChange.add(i);
        }
    }
    Collections.shuffle(slotsToChange);

    new BukkitRunnable() {
        int index = 0;

        @Override
    public void run() {
        if (index >= slotsToChange.size()) {
            this.cancel();

            // Clean "Success Rate" lore line before giving
            if (chosenItem.hasItemMeta()) {
                ItemMeta meta = chosenItem.getItemMeta();
                if (meta.hasLore()) {
                    List<String> lore = new ArrayList<>(meta.getLore());
                    lore.removeIf(line -> ChatColor.stripColor(line).startsWith("Chance"));
                    meta.setLore(lore);
                    chosenItem.setItemMeta(meta);
                }
            }

            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(chosenItem.clone());
            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), chosenItem.clone());
            }

            topInv.setItem(chosenSlot, null);
            ItemMeta meta = chosenItem.getItemMeta();
            String itemName;

            if (meta != null && meta.hasDisplayName()) {
                itemName = ChatColor.stripColor(meta.getDisplayName());
            } else {
                itemName = chosenItem.getType().name().replace('_', ' ').toLowerCase();
                itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
            }
            player.sendMessage("§aYou received: " + itemName);
            player.closeInventory();
            return;
        }
        int slot = slotsToChange.get(index);
        topInv.setItem(slot, GuiUtil.createPane(Material.GRAY_STAINED_GLASS_PANE));
        index++;
    }
}.runTaskTimer(TestEnchants.getInstance(), 10L, 2L);
}

    public static ItemStack getRandomLoot(String lootTableName) {
        List<LootEntry> lootEntries = crateTableUtil.LOOT_TABLES.get(lootTableName);
        if (lootEntries == null || lootEntries.isEmpty()) return null;

        double totalWeight = lootEntries.stream().mapToDouble(e -> e.chance).sum();
        double randomValue = Math.random() * totalWeight;

        double currentWeight = 0;
        for (LootEntry entry : lootEntries) {
            currentWeight += entry.chance;
            if (randomValue <= currentWeight) {
                return entry.item.clone();
            }
        }
        return null;
    }

    private boolean handleLeftClick(Player player, Material clickedType) {
    if (clickedType == Material.ENCHANTING_TABLE) {
        player.openInventory(GuiUtil.enchantKeyInventory(player));
        guiOpenMethod.put(player, "LEFT_CLICK");
        return true;
    } else if (clickedType == Material.REDSTONE_BLOCK) {
        player.openInventory(GuiUtil.divineKeyInventory(player));
        guiOpenMethod.put(player, "LEFT_CLICK");
        return true;
    } else if (clickedType == Material.BEDROCK) {
        player.openInventory(GuiUtil.durabilityKeyInventory(player));
        guiOpenMethod.put(player, "LEFT_CLICK");
        return true;
    } else if (clickedType == Material.CREAKING_HEART) {
        player.openInventory(GuiUtil.miningKeyInventory(player));
        guiOpenMethod.put(player, "LEFT_CLICK");
        return true;
    } else if (clickedType == Material.CRYING_OBSIDIAN) {
        player.openInventory(GuiUtil.prisonKeyInventory(player));
        guiOpenMethod.put(player, "LEFT_CLICK");
        return true;
    }
    return false;
}

    private boolean handleRightClick(Player player, Material clickedType, Location clickedBlockLoc) {
    ItemStack handItem = player.getInventory().getItemInMainHand();

    switch (clickedType) {
        case ENCHANTING_TABLE:
            return processKey(player, handItem, "Enchant Key", "crate_enchant", GuiUtil.enchantKeyInventory(player), clickedBlockLoc);
        case REDSTONE_BLOCK:
            return processKey(player, handItem, "Divine Key", "crate_divine", GuiUtil.divineKeyInventory(player), clickedBlockLoc);
        case BEDROCK:
            return processKey(player, handItem, "Durability Key", "crate_durability", GuiUtil.durabilityKeyInventory(player), clickedBlockLoc);
        case CREAKING_HEART:  
            return processKey(player, handItem, "Mining Key", "crate_mining", GuiUtil.miningKeyInventory(player), clickedBlockLoc);
        case CRYING_OBSIDIAN:
            return processKey(player, handItem, "Prison Key", "crate_prison", GuiUtil.prisonKeyInventory(player), clickedBlockLoc);
        default:
            return false;
    }
}

    private boolean handleShiftRightClick(Player player, Material clickedType, Location clickedBlockLoc) {
    ItemStack handItem = player.getInventory().getItemInMainHand();

    // Allowed crate types
    if (clickedType != Material.ENCHANTING_TABLE &&
        clickedType != Material.REDSTONE_BLOCK &&
        clickedType != Material.BEDROCK &&
        clickedType != Material.CREAKING_HEART &&
        clickedType != Material.CRYING_OBSIDIAN) {
        return false;
    }

    if (!isValidCustomItem(handItem)) {
        player.sendMessage("You must hold a valid crate key to open this.");
        pushPlayerBack(player, clickedBlockLoc);
        return false;
    }

    String plainName = stripColorCodes(handItem.getItemMeta().getDisplayName());
    String crateType = null;

    if (clickedType == Material.ENCHANTING_TABLE && plainName.equalsIgnoreCase("Enchant Key")) {
        crateType = "Enchant Key";
    } else if (clickedType == Material.REDSTONE_BLOCK && plainName.equalsIgnoreCase("Divine Key")) {
        crateType = "Divine Key";
    } else if (clickedType == Material.BEDROCK && plainName.equalsIgnoreCase("Durability Key")) {
        crateType = "Durability Key";
    } else if (clickedType == Material.CREAKING_HEART && plainName.equalsIgnoreCase("Mining Key")) {
        crateType = "Mining Key";
    } else if (clickedType == Material.CRYING_OBSIDIAN && plainName.equalsIgnoreCase("Prison Key")) {
        crateType = "Prison Key";
    }

    if (crateType == null) {
        player.sendMessage("You must hold the correct key to open this crate.");
        pushPlayerBack(player, clickedBlockLoc);
        return false;
    }

    // Process the key usage, increment stats, and check quest progress
    return processShiftRightClickCrate(player, handItem, crateType, clickedBlockLoc);
}

    private void pushPlayerBack(Player player, Location blockLocation) {
        Location playerLoc = player.getLocation();
        double dx = playerLoc.getX() - blockLocation.getX();
        double dy = playerLoc.getY() - blockLocation.getY();
        double dz = playerLoc.getZ() - blockLocation.getZ();
        Vector pushVector = new Vector(dx, dy, dz).normalize().multiply(1);
        Vector currentVelocity = player.getVelocity();
        player.setVelocity(new Vector(pushVector.getX(), currentVelocity.getY(), pushVector.getZ()));
    }

    private String stripColorCodes(String input) {
        if (input == null) return null;
        return ChatColor.stripColor(input);
    }

    private boolean isValidCustomItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String name = item.getItemMeta().getDisplayName();
        if (name == null) return false;

        String plainName = stripColorCodes(name);
        for (int i = 0; i < customItemUtil.CUSTOM_ITEM.length; i++) {
            if (plainName.equalsIgnoreCase(stripColorCodes(customItemUtil.CUSTOM_ITEM[i]))) {
                return item.getType() == customItemUtil.CUSTOM_ITEM_MATERIAL[i];
            }
        }
        return false;
    }

    private boolean processKey(Player player, ItemStack handItem, String keyName, String statKey, Inventory gui, Location clickedBlockLoc) {
    if (!isValidCustomItem(handItem)) {
        player.sendMessage("You must hold a " + keyName + " to open this.");
        pushPlayerBack(player, clickedBlockLoc);
        return false;
    }

    String plainName = stripColorCodes(handItem.getItemMeta().getDisplayName());
    if (!plainName.equalsIgnoreCase(keyName)) {
        player.sendMessage("You must hold a " + keyName + " to open this.");
        pushPlayerBack(player, clickedBlockLoc);
        return false;
    }

    // Decrement the key item count
    if (handItem.getAmount() <= 1) {
        player.getInventory().setItemInMainHand(null);
    } else {
        handItem.setAmount(handItem.getAmount() - 1);
        player.getInventory().setItemInMainHand(handItem);
    }

    // Open the GUI
    guiOpenMethod.put(player, "RIGHT_CLICK");
    player.openInventory(gui);

    UUID uuid = player.getUniqueId();
    StatTracker statTracker = TestEnchants.getInstance().getStatTracker();

    // Increment stats
    statTracker.incrementPlayerStat(uuid, statKey);
    statTracker.incrementPlayerStat(uuid, "crate_total");

    // Now immediately check crate quests progress
    QuestManager questManager = TestEnchants.getInstance().getQuestManager();
    Set<String> activeQuests = new HashSet<>(questManager.getActiveQuests(player));
    int currentCrates = statTracker.getPlayerStat(uuid, "crate_total", false);

    for (String questKey : activeQuests) {
        RankQuest quest = questManager.get(questKey);
        if (quest == null || quest.extraObjective == null || !quest.extraObjective.startsWith("open_crates:"))
            continue;

        String[] parts = quest.extraObjective.split(":");
        int required = Integer.parseInt(parts[1]);
        String baseRank = questKey.split("-quest")[0];

        int cratesAtStart = statTracker.getPlayerStat(uuid, "total_crates_at_rank_start." + baseRank, false);
        int cratesSinceStart = currentCrates - cratesAtStart;

        if (cratesSinceStart >= required) {
            questManager.completeQuest(player, questKey);
            player.sendMessage("§aQuest complete: Opened " + required + " crates!");
        }
    }

    return true;
}

    private boolean processShiftRightClickCrate(Player player, ItemStack handItem, String crateType, Location clickedBlockLoc) {
    int amount = Math.min(handItem.getAmount(), 32);
    // Decrement key amount
    handItem.setAmount(handItem.getAmount() - amount);
    if (handItem.getAmount() <= 0) {
        player.getInventory().setItemInMainHand(null);
    } else {
        player.getInventory().setItemInMainHand(handItem);
    }

    String statKey = switch (crateType) {
        case "Enchant Key" -> "crate_enchant";
        case "Divine Key" -> "crate_divine";
        case "Durability Key" -> "crate_durability";
        case "Mining Key" -> "crate_mining";
        case "Prison Key" -> "crate_prison";
        default -> null;
    };

    StatTracker statTracker = TestEnchants.getInstance().getStatTracker();
    UUID uuid = player.getUniqueId();

    if (statKey != null) {
        statTracker.incrementPlayerStat(uuid, statKey, amount);
        statTracker.incrementPlayerStat(uuid, "crate_total", amount); // optional total counter
    }

    Random random = new Random();

    // Give loot for each key opened
    for (int i = 0; i < amount; i++) {
        ItemStack loot = getRandomLoot(crateType);
        if (loot == null) {
            player.sendMessage("No loot available.");
            break;
        }

        if (loot.hasItemMeta()) {
            ItemMeta meta = loot.getItemMeta();
            if (meta.hasLore()) {
                List<String> lore = new ArrayList<>(meta.getLore());
                int randomChance = 1 + random.nextInt(100);
                for (int j = 0; j < lore.size(); j++) {
                    String line = ChatColor.stripColor(lore.get(j));
                    if (line.startsWith("Success Rate:")) {
                        lore.set(j, ChatColor.GREEN + "Success Rate: " + randomChance + "%");
                        break;
                    }
                }
                meta.setLore(lore);
                loot.setItemMeta(meta);
            }
        }

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(loot);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), loot);
        }
    }

    // Now immediately check crate quests progress
    QuestManager questManager = TestEnchants.getInstance().getQuestManager();
    Set<String> activeQuests = questManager.getActiveQuests(player);
    int currentCrates = statTracker.getPlayerStat(uuid, "crate_total", false);

    for (String questKey : activeQuests) {
        RankQuest quest = questManager.get(questKey);
        if (quest == null || quest.extraObjective == null || !quest.extraObjective.startsWith("open_crates:"))
            continue;

        String[] parts = quest.extraObjective.split(":");
        int required = Integer.parseInt(parts[1]);
        String baseRank = questKey.split("-quest")[0];

        int cratesAtStart = statTracker.getPlayerStat(uuid, "total_crates_at_rank_start." + baseRank, false);
        int cratesSinceStart = currentCrates - cratesAtStart;

        if (cratesSinceStart >= required) {
            questManager.completeQuest(player, questKey);
            player.sendMessage("§aQuest complete: Opened " + required + " crates!");
        }
    }

    player.sendMessage("You received " + amount + " item" + (amount > 1 ? "s" : "") + " from the " + crateType + " crate.");
    guiOpenMethod.remove(player);
    return true;
}

}