package customEnchants.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiUtil {

    // Regex pattern to extract level and chance from lore
    private static final Pattern LEVEL_PATTERN = Pattern.compile("(.+) ([IVXLCDM]+)$");
    private static final Pattern CHANCE_PATTERN = Pattern.compile("Success Rate: (\\d+(?:\\.\\d+)?)%");

    /**
     * Creates a custom enchanted book with the specified name, level, and chance.
     */
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
        lore.add(ChatColor.GREEN + "Success Rate: " + String.format("%.1f", chance * 100) + "%");
        lore.add(EnchantmentData.getColoredRarity(info.rarity));
        lore.add("");
        lore.add(ChatColor.GRAY + "Applicable to: " + formatToolTypes(info.toolTypes));
        lore.add(ChatColor.GRAY + "Drag and drop onto item to apply");

        meta.setLore(lore);
        book.setItemMeta(meta);
        return book;
    }

    /**
     * Parses an enchanted book and returns enchantment info.
     */
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

    private static ItemStack createPane(Material color) {
        ItemStack pane = new ItemStack(color);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" "); // Blank name prevents hover text
            pane.setItemMeta(meta);
        }
        return pane;
    }
}
