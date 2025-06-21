package customEnchants.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import customEnchants.TestEnchants;
import customEnchants.utils.crateTableUtil.LootEntry;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiUtil {

    // Regex pattern to extract level and chance from lore
    private static final Pattern LEVEL_PATTERN = Pattern.compile("(.+) ((?:[IV]{1,3}|IV|V)|\\d+)$");
    private static final Pattern CHANCE_PATTERN = Pattern.compile("Success Rate: (\\d+(?:\\.\\d+)?)%");

    //Use EnchantmentData createEnchantedBook this is only for anvils 
    public static ItemStack createCustomEnchantBook(String enchantName, int level, double chance) {
        int index = EnchantmentData.getEnchantmentIndex(enchantName);
        if (index == -1) return null;

        EnchantmentData.EnchantmentInfo info = EnchantmentData.getEnchantmentInfo(index);
        String rarityColor = EnchantmentData.getRarityColor(info.rarity);
        String romanLevel = toRoman(level);

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return null;

        meta.setDisplayName(rarityColor + info.name + " " + romanLevel);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + info.lore);
        lore.add("");
        lore.add(ChatColor.GREEN + "Success Rate: " + (int) Math.round(chance * 100) + "%");
        lore.add(EnchantmentData.getColoredRarity(info.rarity));
        lore.add("");
        lore.add(ChatColor.GRAY + "Applicable to: " + formatToolTypes(info.toolTypes));
        lore.add(ChatColor.GRAY + "Drag and drop onto item to apply");

        meta.setLore(lore);
        book.setItemMeta(meta);
        return book;
    }

    //Parses an enchanted book and returns enchantment info.
    public static EnchantParseResult parseCustomEnchantBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.hasLore()) return null;

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        List<String> lore = meta.getLore();
        if (lore == null || lore.size() < 3) return null;

        // Extract enchant name and level from display name
        Matcher levelMatcher = LEVEL_PATTERN.matcher(displayName);
        if (!levelMatcher.matches()) return null;

        String name = levelMatcher.group(1).trim();
        int level = fromRoman(levelMatcher.group(2).trim());

        // Extract chance from lore
        double chance = -1;
        for (String line : lore) {
            Matcher chanceMatcher = CHANCE_PATTERN.matcher(ChatColor.stripColor(line));
            if (chanceMatcher.find()) {
                chance = Double.parseDouble(chanceMatcher.group(1)) / 100.0;
                break;
            }
        }

        if (chance < 0) return null;
        return new EnchantParseResult(name, level, chance);
    }
    public static class RarityInfo {
        public final String colorCode;
        public final String rarityName;

        public RarityInfo(String colorCode, String rarityName) {
            this.colorCode = colorCode;
            this.rarityName = rarityName;
        }
    }

    public static RarityInfo getRarityInfoFromBook(ItemStack book) {
    if (book == null) return new RarityInfo("§f", "COMMON");

    ItemMeta meta = book.getItemMeta();
    if (meta == null || !meta.hasDisplayName()) return new RarityInfo("§f", "COMMON");

    String displayName = meta.getDisplayName();

    if (displayName.length() >= 2 && displayName.charAt(0) == '§') {
        String colorCode = displayName.substring(0, 2);

        for (int i = 0; i < EnchantmentData.RARITY_COLORS.length; i++) {
            if (EnchantmentData.RARITY_COLORS[i].equals(colorCode)) {
                return new RarityInfo(EnchantmentData.RARITY_COLORS[i], EnchantmentData.RARITY_NAMES[i]);
            }
        }
    }

    return new RarityInfo("§f", "COMMON"); // default fallback
}

    private static void fillSlots(Inventory inv, ItemStack item, int[]... ranges) {
    for (int[] range : ranges) {
        for (int i = range[0]; i < range[1]; i++) {
            inv.setItem(i, item);
        }
    }
}

    private static int[] range(int start, int end) {
        return new int[]{start, end};
    }

    private static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> Integer.toString(number);
        };
    }

    private static int fromRoman(String roman) {
        return switch (roman) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            default -> -1;
        };
    }

    private static String formatToolTypes(String toolTypes) {
        return "ALL".equals(toolTypes) ? "All Tools" : toolTypes.replace(",", ", ");
    }

    private static ItemStack keyItemWithLore(Player player, String keyType) {
    ItemStack item = customItemUtil.createCustomItem(keyType);
    if (item == null || !item.hasItemMeta()) return item;

    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
    lore.add(ChatColor.GRAY + "Keys: " + ClaimStorage.getKeyCount(player, keyType));
    meta.setLore(lore);
    item.setItemMeta(meta);

    return item;
}
    public static class EnchantParseResult {
        public final String name;
        public final int level;
        public final double chance;

        public EnchantParseResult(String name, int level, double chance) {
            this.name = name;
            this.level = level;
            this.chance = chance;
        }
    }

    public static Inventory createScrapGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8Tinkerer");

        for (int i = 0; i < 9; i++) {
            gui.setItem(i, createPane(Material.BLACK_STAINED_GLASS_PANE));
        }
        for (int i = 9; i < 18; i++) {
            gui.setItem(i, createPane(Material.GRAY_STAINED_GLASS_PANE));
        }
        for (int i = 18; i < 27; i++) {
            gui.setItem(i, createPane(Material.WHITE_STAINED_GLASS_PANE));
        }

        return gui;
    }

    public static ItemStack createPane(Material color) {
        ItemStack pane = new ItemStack(color);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" "); // Blank name prevents hover text
            pane.setItemMeta(meta);
        }
        return pane;
    }

    public static Inventory blackScrollInventory(Player player) {
        Inventory blackScrollGui = Bukkit.createInventory(null, 54, "§8Black Scroll");

        for (int i = 0; i < 9; i++) {
            blackScrollGui.setItem(i, createPane(Material.GRAY_STAINED_GLASS_PANE));
        }
        for (int i = 9; i < 13; i++) {
            blackScrollGui.setItem(i, createPane(Material.GRAY_STAINED_GLASS_PANE));
        }
        for (int i = 14; i < 28; i++) {
            blackScrollGui.setItem(i, createPane(Material.GRAY_STAINED_GLASS_PANE));
        }
        for (int i = 35; i < 37; i++) {
            blackScrollGui.setItem(i, createPane(Material.GRAY_STAINED_GLASS_PANE));
        }
        for (int i = 44; i < 54; i++) {
            blackScrollGui.setItem(i, createPane(Material.GRAY_STAINED_GLASS_PANE));
        }
        return blackScrollGui;
    }

    public static Inventory enchantKeyInventory(Player player) {
    Inventory gui = Bukkit.createInventory(null, 54, "§5Enchanter Crate");

    fillSlots(gui, createPane(Material.GRAY_STAINED_GLASS_PANE),
        range(0, 10), range(17, 19), range(26, 28), range(35, 37), range(44, 54));

    List<crateTableUtil.LootEntry> lootList = crateTableUtil.LOOT_TABLES.get("Enchant Key");

    if (lootList != null) {
        int slotIndex = 10;

        for (crateTableUtil.LootEntry loot : lootList) {
            while (slotIndex < gui.getSize() && gui.getItem(slotIndex) != null) {
                slotIndex++;
            }
            if (slotIndex >= gui.getSize()) break;

            String name = ChatColor.stripColor(loot.item.getItemMeta().getDisplayName());
            ItemStack customItem = customItemUtil.createCustomItem(name);

            if (customItem != null) { 
    ItemMeta meta = customItem.getItemMeta();
    if (meta != null) {
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        String chanceLine = ChatColor.YELLOW + "Chance: " + loot.chance + "%";
        if (!lore.contains(chanceLine)) {
            lore.add(chanceLine);
        }
        meta.setLore(lore);
        customItem.setItemMeta(meta);
    }
    gui.setItem(slotIndex++, customItem);
} else {
    // Handle non-custom (vanilla) items
    ItemStack vanillaItem = loot.item; // <-- however you're storing the item
    if (vanillaItem != null) {
        ItemMeta meta = vanillaItem.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            String chanceLine = ChatColor.YELLOW + "Chance: " + loot.chance + "%";
            if (!lore.contains(chanceLine)) {
                lore.add(chanceLine);
            }
            meta.setLore(lore);
            vanillaItem.setItemMeta(meta);
        }
        gui.setItem(slotIndex++, vanillaItem);
    }
}

        }
    }

    return gui;
}

    public static Inventory miningKeyInventory(Player player) {
    Inventory gui = Bukkit.createInventory(null, 54, "§3Mining Crate");

    // Fill background slots with gray panes
    fillSlots(gui, createPane(Material.GRAY_STAINED_GLASS_PANE), 
        range(0, 10), range(17, 19), range(26, 28), range(35, 37), range(44, 54));

    List<LootEntry> lootList = crateTableUtil.LOOT_TABLES.get("Mining Key");
    if (lootList != null) {
        int slotIndex = 10; // choose your starting slot (avoid background slots)
        for (LootEntry loot : lootList) {
            while (slotIndex < gui.getSize() && gui.getItem(slotIndex) != null) {
                slotIndex++;
            }
            if (slotIndex >= gui.getSize()) break;
            String name = ChatColor.stripColor(loot.item.getItemMeta().getDisplayName());
            ItemStack customItem = customItemUtil.createCustomItem(name);

            if (customItem != null) { 
    ItemMeta meta = customItem.getItemMeta();
    if (meta != null) {
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        String chanceLine = ChatColor.YELLOW + "Chance: " + loot.chance + "%";
        if (!lore.contains(chanceLine)) {
            lore.add(chanceLine);
        }
        meta.setLore(lore);
        customItem.setItemMeta(meta);
    }
    gui.setItem(slotIndex++, customItem);
} else {
    // Handle non-custom (vanilla) items
    ItemStack vanillaItem = loot.item; // <-- however you're storing the item
    if (vanillaItem != null) {
        ItemMeta meta = vanillaItem.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            String chanceLine = ChatColor.YELLOW + "Chance: " + loot.chance + "%";
            if (!lore.contains(chanceLine)) {
                lore.add(chanceLine);
            }
            meta.setLore(lore);
            vanillaItem.setItemMeta(meta);
        }
        gui.setItem(slotIndex++, vanillaItem);
    }
}

        }
    }

    return gui;
}

    public static Inventory prisonKeyInventory(Player player) {
    Inventory gui = Bukkit.createInventory(null, 54, "§1Prison Crate");

    // Fill background slots with gray panes
    fillSlots(gui, createPane(Material.GRAY_STAINED_GLASS_PANE), 
        range(0, 10), range(17, 19), range(26, 28), range(35, 37), range(44, 54));

    List<LootEntry> lootList = crateTableUtil.LOOT_TABLES.get("Prison Key");
    if (lootList != null) {
        int slotIndex = 10; // choose your starting slot (avoid background slots)
        for (LootEntry loot : lootList) {
            while (slotIndex < gui.getSize() && gui.getItem(slotIndex) != null) {
                slotIndex++;
            }
            if (slotIndex >= gui.getSize()) break; // safety check
            String name = ChatColor.stripColor(loot.item.getItemMeta().getDisplayName());
            ItemStack customItem = customItemUtil.createCustomItem(name);

            if (customItem != null) { 
    ItemMeta meta = customItem.getItemMeta();
    if (meta != null) {
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        String chanceLine = ChatColor.YELLOW + "Chance: " + loot.chance + "%";
        if (!lore.contains(chanceLine)) {
            lore.add(chanceLine);
        }
        meta.setLore(lore);
        customItem.setItemMeta(meta);
    }
    gui.setItem(slotIndex++, customItem);
} else {
    // Handle non-custom (vanilla) items
    ItemStack vanillaItem = loot.item; // <-- however you're storing the item
    if (vanillaItem != null) {
        ItemMeta meta = vanillaItem.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            String chanceLine = ChatColor.YELLOW + "Chance: " + loot.chance + "%";
            if (!lore.contains(chanceLine)) {
                lore.add(chanceLine);
            }
            meta.setLore(lore);
            vanillaItem.setItemMeta(meta);
        }
        gui.setItem(slotIndex++, vanillaItem);
    }
}

        }
    }

    return gui;
}

    public static Inventory divineKeyInventory(Player player) {
    Inventory gui = Bukkit.createInventory(null, 54, "§cDivine Crate");

    // Fill background slots with gray panes
    fillSlots(gui, createPane(Material.GRAY_STAINED_GLASS_PANE), 
        range(0, 10), range(17, 19), range(26, 28), range(35, 37), range(44, 54));

    List<LootEntry> lootList = crateTableUtil.LOOT_TABLES.get("Divine Key");
    if (lootList != null) {
        int slotIndex = 10; // choose your starting slot (avoid background slots)
        for (LootEntry loot : lootList) {
            while (slotIndex < gui.getSize() && gui.getItem(slotIndex) != null) {
                slotIndex++;
            }
            if (slotIndex >= gui.getSize()) break; // safety check
            String name = ChatColor.stripColor(loot.item.getItemMeta().getDisplayName());
            ItemStack customItem = customItemUtil.createCustomItem(name);

            if (customItem != null) { 
    ItemMeta meta = customItem.getItemMeta();
    if (meta != null) {
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        String chanceLine = ChatColor.YELLOW + "Chance: " + loot.chance + "%";
        if (!lore.contains(chanceLine)) {
            lore.add(chanceLine);
        }
        meta.setLore(lore);
        customItem.setItemMeta(meta);
    }
    gui.setItem(slotIndex++, customItem);
} else {
    // Handle non-custom (vanilla) items
    ItemStack vanillaItem = loot.item; // <-- however you're storing the item
    if (vanillaItem != null) {
        ItemMeta meta = vanillaItem.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            String chanceLine = ChatColor.YELLOW + "Chance: " + loot.chance + "%";
            if (!lore.contains(chanceLine)) {
                lore.add(chanceLine);
            }
            meta.setLore(lore);
            vanillaItem.setItemMeta(meta);
        }
        gui.setItem(slotIndex++, vanillaItem);
    }
}

        }
    }

    return gui;
}

    public static Inventory durabilityKeyInventory(Player player) {
    Inventory gui = Bukkit.createInventory(null, 54, "§8Durability Crate");

    // Fill background slots with gray panes
    fillSlots(gui, createPane(Material.GRAY_STAINED_GLASS_PANE), 
        range(0, 10), range(17, 19), range(26, 28), range(35, 37), range(44, 54));

    List<LootEntry> lootList = crateTableUtil.LOOT_TABLES.get("Durability Key");
    if (lootList != null) {
        int slotIndex = 10; // choose your starting slot (avoid background slots)
        for (LootEntry loot : lootList) {
            while (slotIndex < gui.getSize() && gui.getItem(slotIndex) != null) {
                slotIndex++;
            }
            if (slotIndex >= gui.getSize()) break; // safety check
            String name = ChatColor.stripColor(loot.item.getItemMeta().getDisplayName());
            ItemStack customItem = customItemUtil.createCustomItem(name);

            if (customItem != null) { 
    ItemMeta meta = customItem.getItemMeta();
    if (meta != null) {
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        String chanceLine = ChatColor.YELLOW + "Chance: " + loot.chance + "%";
        if (!lore.contains(chanceLine)) {
            lore.add(chanceLine);
        }
        meta.setLore(lore);
        customItem.setItemMeta(meta);
    }
    gui.setItem(slotIndex++, customItem);
} else {
    // Handle non-custom (vanilla) items
    ItemStack vanillaItem = loot.item; // <-- however you're storing the item
    if (vanillaItem != null) {
        ItemMeta meta = vanillaItem.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            String chanceLine = ChatColor.YELLOW + "Chance: " + loot.chance + "%";
            if (!lore.contains(chanceLine)) {
                lore.add(chanceLine);
            }
            meta.setLore(lore);
            vanillaItem.setItemMeta(meta);
        }
        gui.setItem(slotIndex++, vanillaItem);
    }
}

        }
    }

    return gui;
}

    public static Inventory decorVoucher(Player player, List<ItemStack> items) {
    Inventory gui = Bukkit.createInventory(null, 54, "Decoration Voucher");

    fillSlots(gui, createPane(Material.GRAY_STAINED_GLASS_PANE),
        range(0, 10), range(17, 19), range(26, 28), range(35, 37), range(44, 54));

    int slotIndex = 10;
    for (ItemStack item : items) {
        if (slotIndex >= gui.getSize()) break;
        gui.setItem(slotIndex, item.clone());
        slotIndex++;
    }

    return gui;
}

    public static Inventory claimInventory(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "Claim");

        fillSlots(gui, createPane(Material.GRAY_STAINED_GLASS_PANE),
        range(0,10), range(11, 13), range(14, 16), range(17, 27));
        
        ItemStack trialKey = new ItemStack(Material.TRIAL_KEY);
        ItemMeta trialKeyMeta = trialKey.getItemMeta();
        trialKeyMeta.setDisplayName(ChatColor.GOLD + "Key Claim");
        trialKey.setItemMeta(trialKeyMeta);

        ItemStack prestige = new ItemStack(Material.END_CRYSTAL);
        ItemMeta prestigeMeta = prestige.getItemMeta();
        prestigeMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Prestige Claim");
        prestige.setItemMeta(prestigeMeta);

        ItemStack other = new ItemStack(Material.CHEST);
        ItemMeta otherMeta = other.getItemMeta();
        otherMeta.setDisplayName(ChatColor.WHITE + "Other Claims");
        other.setItemMeta(otherMeta);


        gui.setItem(10, trialKey);
        gui.setItem(13, prestige);
        gui.setItem(16, other);
        
        
        
        return gui;
    }

    public static Inventory guiKeyClaim(Player player) {
    Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Key Claim");

    fillSlots(gui, createPane(Material.GRAY_STAINED_GLASS_PANE), 
        range(0, 10), range(17, 19), range(26, 28), range(35, 37), range(44, 54));

    gui.setItem(10, keyItemWithLore(player, "Mining Key"));
    gui.setItem(11, keyItemWithLore(player, "Prison Key"));
    gui.setItem(12, keyItemWithLore(player, "Enchant Key"));
    gui.setItem(13, keyItemWithLore(player, "Divine Key"));
    gui.setItem(14, keyItemWithLore(player, "Durability Key"));

    return gui;
}

    public static Inventory createExtractorStorageGUI(Player player, EssenceManager essenceManager) {
    Inventory gui = Bukkit.createInventory(null, 45, ChatColor.DARK_GRAY + "Extractor Storage");
    ItemStack filler = createPane(Material.GRAY_STAINED_GLASS_PANE);

    // Fill all slots with gray panes
    for (int i = 0; i < 45; i++) {
        gui.setItem(i, filler);
    }

    int[] centerSlots = {12, 13, 14, 21, 22, 23, 30, 31, 32};
    // Clear the center 3x3 grid
    for (int slot : centerSlots) {
        gui.setItem(slot, null);
    }

    // Load stored extractors from file and set them in center slots
    essenceManager.loadExtractorInventory(player, gui);

    return gui;
}

    public static Inventory essenceInventory(Player player) {
    Inventory gui = Bukkit.createInventory(null, 27, "Essence Menu");

    fillSlots(gui, createPane(Material.GRAY_STAINED_GLASS_PANE),
        range(0,10), range(11, 12), range(13, 14), range(15, 16), range(17, 27));

    // Put items directly in the specific slots:
    ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
    ItemMeta meta = pickaxe.getItemMeta();
    if (meta != null) {
        meta.setDisplayName(ChatColor.AQUA + "Extractors");
        pickaxe.setItemMeta(meta);
    }
    gui.setItem(10, pickaxe);

    ItemStack hoe = new ItemStack(Material.DIAMOND_HOE);
    meta = hoe.getItemMeta();
    if (meta != null) {
        meta.setDisplayName(ChatColor.AQUA + "Plows");
        hoe.setItemMeta(meta);
    }
    gui.setItem(12, hoe);

    ItemStack fishingRod = new ItemStack(Material.FISHING_ROD);
    meta = fishingRod.getItemMeta();
    if (meta != null) {
        meta.setDisplayName(ChatColor.AQUA + "Fishing Nets");
        fishingRod.setItemMeta(meta);
    }
    gui.setItem(14, fishingRod);

    ItemStack emeraldBlock = new ItemStack(Material.EMERALD_BLOCK);
    meta = emeraldBlock.getItemMeta();
    if (meta != null) {
        meta.setDisplayName(ChatColor.AQUA + "Sell Essence");
        emeraldBlock.setItemMeta(meta);
    }
    gui.setItem(16, emeraldBlock);

    return gui;
}

    public static Inventory essenceSellInventory(Player player, EssenceManager essenceManager) {
    Inventory gui = Bukkit.createInventory(null, 36, "Sell Essence");

    fillSlots(gui, createPane(Material.GRAY_STAINED_GLASS_PANE),
        range(0,11), range(13, 14), range(16, 20), range(22, 23), range(25, 36));

    // Helper method to create dye item
    BiConsumer<Integer, Material> createTierItem = (tier, material) -> {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String display = switch (tier) {
                case 1 -> ChatColor.RED + "Tier 1";
                case 2 -> ChatColor.GOLD + "Tier 2";
                case 3 -> ChatColor.YELLOW + "Tier 3";
                case 4 -> ChatColor.GREEN + "Tier 4";
                case 5 -> ChatColor.DARK_GREEN + "Tier 5";
                case 6 -> ChatColor.BLUE + "Tier 6";
                case 7 -> ChatColor.LIGHT_PURPLE + "Tier 7";
                case 8 -> ChatColor.DARK_PURPLE + "Tier 8";
                default -> "Unknown";
            };
            meta.setDisplayName(display);

            int amount = essenceManager.getEssence(player, tier);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to sell");
            lore.add(ChatColor.DARK_GRAY + "Amount: " + ChatColor.WHITE + amount);
            meta.setLore(lore);

            item.setItemMeta(meta);
            gui.setItem(getSlotForTier(tier), item);
        }
    };

    createTierItem.accept(1, Material.RED_DYE);
    createTierItem.accept(2, Material.ORANGE_DYE);
    createTierItem.accept(3, Material.YELLOW_DYE);
    createTierItem.accept(4, Material.LIME_DYE);
    createTierItem.accept(5, Material.GREEN_DYE);
    createTierItem.accept(6, Material.BLUE_DYE);
    createTierItem.accept(7, Material.MAGENTA_DYE);
    createTierItem.accept(8, Material.PURPLE_DYE);

    return gui;
}

    private static int getSlotForTier(int tier) {
    return switch (tier) {
        case 1 -> 11;
        case 2 -> 12;
        case 3 -> 14;
        case 4 -> 15;
        case 5 -> 20;
        case 6 -> 21;
        case 7 -> 23;
        case 8 -> 24;
        default -> 0;
    };
}

    public static void openQuestGUI(Player player) {
    Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Active Quests");

    // Add border
    ItemStack border = createPane(Material.GRAY_STAINED_GLASS_PANE);
    for (int i = 0; i < 54; i++) {
        if (isBorderSlot(i)) gui.setItem(i, border);
    }

    // Get active quests
    Set<String> activeQuests = TestEnchants.getInstance().getQuestManager().getActiveQuests(player);

    List<ItemStack> questItems = new ArrayList<>();
    for (String questKey : activeQuests) {
        RankQuest quest = TestEnchants.getInstance().getQuestManager().get(questKey);
        if (quest == null) continue;

        ItemStack questItem = createQuestDisplayItem(player, questKey, quest);
        questItems.add(questItem);
    }

    // Center slots (non-border)
    int[] centerSlots = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    for (int i = 0; i < questItems.size() && i < centerSlots.length; i++) {
        gui.setItem(centerSlots[i], questItems.get(i));
    }

    player.openInventory(gui);
}

    private static boolean isBorderSlot(int slot) {
    return slot < 9 || slot >= 45 || slot % 9 == 0 || slot % 9 == 8;
}

    public static ItemStack createQuestDisplayItem(Player player, String questKey, RankQuest quest) {
    Material material;
    String displayName;
    String progressLine = "";
    UUID uuid = player.getUniqueId();
    StatTracker statTracker = TestEnchants.getInstance().statTracker;

    // Default material fallback
    material = Material.BARRIER;
    displayName = ChatColor.RED + "Unknown Quest";

    // Parse quest type
    if (questKey.endsWith("quest1")) {
        // Block break quest
        material = Material.STONE;
        displayName = ChatColor.AQUA + "Mine " + quest.blocksRequired + " blocks";
        String baseRank = questKey.split("-quest")[0];
        int current = statTracker.getPlayerStat(uuid, "blocks_broken", false);
        int atStart = statTracker.getPlayerStat(uuid, "blocks_broken_at_rank_start." + baseRank, false);
        int progress = current - atStart;
        progressLine = ChatColor.GRAY + "Progress: " + Math.min(progress, quest.blocksRequired) + "/" + quest.blocksRequired;
    } else if (quest.extraObjective != null) {
        String[] parts = quest.extraObjective.split(":");
        String type = parts[0];
        String param = parts.length > 1 ? parts[1] : "";

        switch (type) {
            case "apply_enchant":
                material = Material.ENCHANTED_BOOK;
                displayName = ChatColor.LIGHT_PURPLE + "Apply " + param + " enchant";
                int current = statTracker.getPlayerStat(uuid, "enchants_applied_" + param, false);
                int atStart = statTracker.getPlayerStat(uuid, "enchants_applied_" + param + "_at_rank_start." + questKey.split("-quest")[0], false);
                progressLine = ChatColor.GRAY + "Progress: " + (current - atStart) + "/1";
                break;

            case "equip_extractor":
                material = Material.COAL_ORE;
                displayName = ChatColor.DARK_AQUA + "Equip an Extractor";
                progressLine = ChatColor.GRAY + "Progress: Equip an Extractor to complete";
                break;

            case "earn_essence":
                material = Material.EMERALD;
                displayName = ChatColor.DARK_GREEN + "Earn Essence";
                current = statTracker.getPlayerStat(uuid, "earned_essence", false);
                atStart = statTracker.getPlayerStat(uuid, "earned_essence_at_rank_start." + questKey.split("-quest")[0], false);
                progressLine = ChatColor.GRAY + "Progress: " + (current - atStart) + "/" + param;
                break;

            case "sell_filler":
                material = Material.EMERALD_BLOCK;
                displayName = ChatColor.GREEN + "Sell " + param + " filler";
                current = statTracker.getPlayerStat(uuid, "filler_sold", false);
                atStart = statTracker.getPlayerStat(uuid, "filler_sold_at_rank_start." + questKey.split("-quest")[0], false);
                progressLine = ChatColor.GRAY + "Progress: " + (current - atStart) + "/" + param;
                break;

            case "craft_shard":
                material = Material.ECHO_SHARD;
                displayName = ChatColor.AQUA + "Craft " + param + " Essence Shard";
                progressLine = ChatColor.GRAY + "Progress: Not tracked";
                break;

            case "scrap_enchant":
                material = Material.ANVIL;
                displayName = ChatColor.GOLD + "Scrap an Enchant";
                progressLine = ChatColor.GRAY + "Progress: Not tracked";
                break;

            case "open_crates":
                material = Material.TRIAL_KEY;
                displayName = ChatColor.YELLOW + "Open " + param + " Crates";
                current = statTracker.getPlayerStat(uuid, "crate_total", false);
                atStart = statTracker.getPlayerStat(uuid, "total_crates_at_rank_start." + questKey.split("-quest")[0], false);
                progressLine = ChatColor.GRAY + "Progress: " + (current - atStart) + "/" + param;
                break;

            case "use_black_scroll":
                material = Material.BLACK_CANDLE;
                displayName = ChatColor.DARK_PURPLE + "Use a Black Scroll";
                progressLine = ChatColor.GRAY + "Progress: Not tracked";
                break;

            case "rent_cell":
                material = Material.IRON_BARS;
                displayName = ChatColor.GRAY + "Rent a Cell";
                progressLine = ChatColor.GRAY + "Progress: Not tracked";
                break;

            case "craft_extractor":
                material = Material.CRAFTING_TABLE;
                displayName = ChatColor.GOLD + "Craft an Extractor";
                progressLine = ChatColor.GRAY + "Progress: Not tracked";
                break;
        }
    }

    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
        meta.setDisplayName(displayName);
        meta.setLore(Collections.singletonList(progressLine));
        item.setItemMeta(meta);
    }

    return item;
}

    public static void openEssenceShardGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "Essence Shards");

        // Define each shard with material, name, lore
        inv.setItem(10, createShard(Material.COAL, "Coal Essence Shard", 
                Arrays.asList(
                    ChatColor.GRAY + "Crafting Recipe:",
                    ChatColor.GRAY + "576 Coal"
                )));
        inv.setItem(11, createShard(Material.COPPER_INGOT, "Copper Essence Shard",
                Arrays.asList(
                    ChatColor.GRAY + "Crafting Recipe:",
                    ChatColor.GRAY + "576 Copper"
                )));
        inv.setItem(19, createShard(Material.IRON_INGOT, "Iron Essence Shard",
                Arrays.asList(
                    ChatColor.GRAY + "Crafting Recipe:",
                    ChatColor.GRAY + "576 Iron"
                )));
        inv.setItem(20, createShard(Material.REDSTONE, "Redstone Essence Shard",
                Arrays.asList(
                    ChatColor.GRAY + "Crafting Recipe:",
                    ChatColor.GRAY + "288 Coal",
                    ChatColor.GRAY + "288 Copper"
                )));

        inv.setItem(15, createShard(Material.LAPIS_LAZULI, "Lapis Essence Shard",
                Arrays.asList(
                    ChatColor.GRAY + "Crafting Recipe:",
                    ChatColor.GRAY + "1 Redstone Essence Shard",
                    ChatColor.GRAY + "288 Iron"
                )));
        inv.setItem(16, createShard(Material.GOLD_INGOT, "Gold Essence Shard",
                Arrays.asList(
                    ChatColor.GRAY + "Crafting Recipe:",
                    ChatColor.GRAY + "576 Gold"
                )));
        inv.setItem(24, createShard(Material.DIAMOND, "Diamond Essence Shard",
                Arrays.asList(
                    ChatColor.GRAY + "Crafting Recipe:",
                    ChatColor.GRAY + "1 Lapis Essence Shard",
                    ChatColor.GRAY + "288 Emerald"
                )));
        inv.setItem(25, createShard(Material.EMERALD, "Emerald Essence Shard",
                Arrays.asList(
                    ChatColor.GRAY + "Crafting Recipe:",
                    ChatColor.GRAY + "576 Emerald"
                )));
            fillEmptySlotsWithGrayPane(inv);

        player.openInventory(inv);
    }

    public static void openDeepEssenceShardGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "Deep Essence Shards");

        inv.setItem(10, createDeepShard(Material.COAL_BLOCK, "Coal", "Coal Essence Shard"));
        inv.setItem(11, createDeepShard(Material.COPPER_BLOCK, "Copper", "Copper Essence Shard"));
        inv.setItem(19, createDeepShard(Material.IRON_BLOCK, "Iron", "Iron Essence Shard"));
        inv.setItem(20, createDeepShard(Material.REDSTONE_BLOCK, "Redstone", "Redstone Essence Shard"));
        inv.setItem(13, createAmethystShard());

        inv.setItem(15, createDeepShard(Material.LAPIS_BLOCK, "Lapis", "Lapis Essence Shard"));
        inv.setItem(16, createDeepShard(Material.GOLD_BLOCK, "Gold", "Gold Essence Shard"));
        inv.setItem(24, createDeepShard(Material.DIAMOND_BLOCK, "Diamond", "Diamond Essence Shard"));
        inv.setItem(25, createDeepShard(Material.EMERALD_BLOCK, "Emerald", "Emerald Essence Shard"));
        fillEmptySlotsWithGrayPane(inv);

        player.openInventory(inv);
    }

    private static ItemStack createDeepShard(Material blockMaterial, String materialName, String correspondingShardName) {
        ItemStack item = new ItemStack(blockMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "Deep " + materialName + " Essence Shard");

            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "10 " + correspondingShardName,
                ChatColor.GRAY + "128 Amethyst Blocks"
            ));

            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createShard(Material material, String name, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createAmethystShard() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Amethyst Essence Shard");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Crafting Recipe:",
            ChatColor.GRAY + "16 Deep Emerald Essence Shard",
            ChatColor.GRAY + "1024 Amethyst Blocks"
        ));
        item.setItemMeta(meta);
    }
    return item;
}

    public static void fillEmptySlotsWithGrayPane(Inventory inv) {
    ItemStack grayPane = createPane(Material.GRAY_STAINED_GLASS_PANE);
    for (int slot = 0; slot < inv.getSize(); slot++) {
        ItemStack current = inv.getItem(slot);
        if (current == null || current.getType() == Material.AIR) {
            inv.setItem(slot, grayPane);
        }
    }
}

    public static void openExtractorCraftingGUI(Player player) {
    Inventory inv = Bukkit.createInventory(null, 36, "Extractor Crafting");

    inv.setItem(10, createExtractor(Material.COAL_ORE, "Coal Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "1 Coal Essence Shard",
                ChatColor.GRAY + "1 Extractor Core"
            )));

    inv.setItem(11, createExtractor(Material.COPPER_ORE, "Copper Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "2 Copper Essence Shard",
                ChatColor.GRAY + "1 Extractor Core"
            )));

    inv.setItem(20, createExtractor(Material.REDSTONE_ORE, "Redstone Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "10 Redstone Essence Shard",
                ChatColor.GRAY + "1 Extractor Core"
            )));

    inv.setItem(15, createExtractor(Material.LAPIS_ORE, "Lapis Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "15 Lapis Essence Shard",
                ChatColor.GRAY + "1 Extractor Core"
            )));

    inv.setItem(19, createExtractor(Material.IRON_ORE, "Iron Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "5 Iron Essence Shard",
                ChatColor.GRAY + "1 Extractor Core"
            )));

    inv.setItem(16, createExtractor(Material.GOLD_ORE, "Gold Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "20 Gold Essence Shard",
                ChatColor.GRAY + "1 Extractor Core"
            )));

    inv.setItem(24, createExtractor(Material.DIAMOND_ORE, "Diamond Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "15 Diamond Essence Shard",
                ChatColor.GRAY + "1 Extractor Core"
            )));

    inv.setItem(25, createExtractor(Material.EMERALD_ORE, "Emerald Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "54 Emerald Essence Shard",
                ChatColor.GRAY + "1 Extractor Core"
            )));

    fillEmptySlotsWithGrayPane(inv);
    player.openInventory(inv);
}

    public static void openDeepExtractorCraftingGUI(Player player) {
    Inventory inv = Bukkit.createInventory(null, 36, "Deep Extractor Crafting");

    inv.setItem(10, createExtractor(Material.DEEPSLATE_COAL_ORE, "Deep Coal Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "6 Deep Coal Essence Shard",
                ChatColor.GRAY + "1 Extractor Core"
            )));

    inv.setItem(11, createExtractor(Material.DEEPSLATE_COPPER_ORE, "Deep Copper Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "18 Deep Copper Essence Shard",
                ChatColor.GRAY + "2 Extractor Core"
            )));

    inv.setItem(20, createExtractor(Material.DEEPSLATE_REDSTONE_ORE, "Deep Redstone Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "54 Deep Redstone Essence Shard",
                ChatColor.GRAY + "10 Extractor Core"
            )));

    inv.setItem(15, createExtractor(Material.DEEPSLATE_LAPIS_ORE, "Deep Lapis Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "36 Deep Lapis Essence Shard",
                ChatColor.GRAY + "15 Extractor Core"
            )));

    inv.setItem(19, createExtractor(Material.DEEPSLATE_IRON_ORE, "Deep Iron Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "30 Deep Iron Essence Shard",
                ChatColor.GRAY + "5 Extractor Core"
            )));

    inv.setItem(16, createExtractor(Material.DEEPSLATE_GOLD_ORE, "Deep Gold Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "90 Deep Gold Essence Shard",
                ChatColor.GRAY + "20 Extractor Core"
            )));

    inv.setItem(24, createExtractor(Material.DEEPSLATE_DIAMOND_ORE, "Deep Diamond Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "48 Deep Diamond Essence Shard",
                ChatColor.GRAY + "25 Extractor Core"
            )));

    inv.setItem(25, createExtractor(Material.DEEPSLATE_EMERALD_ORE, "Deep Emerald Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "90 Deep Emerald Essence Shard",
                ChatColor.GRAY + "30 Extractor Core"
            )));

    inv.setItem(13, createExtractor(Material.NETHER_GOLD_ORE, "Nether Gold Extractor",
            Arrays.asList(
                ChatColor.GRAY + "Crafting Recipe:",
                ChatColor.GRAY + "10 Amethyst Shards",
                ChatColor.GRAY + "50 Extractor Core"
            )));

    fillEmptySlotsWithGrayPane(inv);
    player.openInventory(inv);
}

    private static ItemStack createExtractor(Material material, String name, List<String> lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
        meta.setDisplayName(ChatColor.AQUA + name);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    return item;
}

    public static Inventory getFurnaceUpgradeGUI(Player player) {
    Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Furnace Upgrades");

    // Fill top and bottom rows with mats
    ItemStack[] mats = {
        new ItemStack(Material.COAL),
        new ItemStack(Material.COPPER_INGOT),
        new ItemStack(Material.IRON_INGOT),
        new ItemStack(Material.GOLD_INGOT),
        new ItemStack(Material.DIAMOND),
        new ItemStack(Material.EMERALD),
        new ItemStack(Material.AMETHYST_SHARD),
    };
    for (int i = 0; i < 7; i++) {
    gui.setItem(i + 1, mats[i]);  // slots 1 to 7 get mats[0] to mats[6]
    }
    for (int i = 0; i < 7; i++) {
    gui.setItem(i + 19, mats[i]);  // slots 19 to 24 get mats[1] to mats[6]
    }   

    for (int tier = 1; tier <= 7; tier++) {
        Material furnaceMat = (tier == 7) ? Material.BLAST_FURNACE : Material.FURNACE;
        ItemStack furnace = new ItemStack(furnaceMat);
        ItemMeta meta = furnace.getItemMeta();

        String name = (tier == 7) ? "Blast Furnace" : "Tier " + tier + " Furnace";
        int smeltBonus = switch (tier) {
            case 1 -> 10;
            case 2 -> 20;
            case 3 -> 30;
            case 4 -> 45;
            case 5 -> 60;
            case 6 -> 80;
            default -> 100;
        };

        // Calculate cost
        double[] tierCosts = {
            50_000,     // Tier 1
            100_000,    // Tier 2
            150_000,    // Tier 3
            250_000,    // Tier 4
            300_000,    // Tier 5
            650_000,    // Tier 6
            1_000_000   // Tier 7 (Blast Furnace)
        };

        double moneyCost = tierCosts[tier - 1];

        // Set lore
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Double Smelt Chance: " + smeltBonus + "%");
        lore.add(ChatColor.YELLOW + "Crafting Cost:");
        if (tier == 1) {
            lore.add(ChatColor.GRAY + "- Furnace");
        } else {
            String prev = (tier == 7) ? "Tier 6 Furnace" : "Tier " + (tier - 1) + " Furnace";
            lore.add(ChatColor.GRAY + "- " + prev);
        }
        lore.add(ChatColor.GRAY + "- $" + String.format("%,d", (int) moneyCost));

        meta.setDisplayName(ChatColor.GREEN + name);
        meta.setLore(lore);
        furnace.setItemMeta(meta);
        gui.setItem(9 + tier, furnace);
    }

    fillEmptySlotsWithGrayPane(gui);
    return gui;
}





}