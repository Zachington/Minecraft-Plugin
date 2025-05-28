package customEnchants.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import customEnchants.utils.crateTableUtil.LootEntry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiUtil {

    // Regex pattern to extract level and chance from lore
    private static final Pattern LEVEL_PATTERN = Pattern.compile("(.+) ([IVXLCDM]+)$");
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
                    List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                    lore.add(ChatColor.YELLOW + "Chance: " + loot.chance + "%");
                    meta.setLore(lore);
                    customItem.setItemMeta(meta);
                }

                gui.setItem(slotIndex++, customItem);
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
            if (slotIndex >= gui.getSize()) break; // safety check
            gui.setItem(slotIndex, loot.item.clone()); // place the exact item
            slotIndex++;
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


}