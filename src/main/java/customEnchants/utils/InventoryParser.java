package customEnchants.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;



public class InventoryParser {

    //item rarity
    public static int itemRarity(ItemStack item) {
        if (item == null) return -1;  // No item
        if (!item.hasItemMeta()) return -1;  // No meta
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return -1;  // No custom name

        String name = meta.getDisplayName();
        if (name.length() < 2) return -1;  // At least two chars for color code

        // Check if the first two characters match any rarity color
        String prefix = name.substring(0, 2); // e.g. "§f"

        for (int i = 0; i < EnchantmentData.RARITY_COLORS.length; i++) {
            if (prefix.equals(EnchantmentData.RARITY_COLORS[i])) {
                return i; // Return the index of the rarity
            }
        }
        return -1; // Not found
    }


    //Find success chance in lore line
    public static int clickedSuccess(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return -1;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return -1;

        List<String> lore = meta.getLore();
        if (lore == null) return -1;

        Pattern pattern = Pattern.compile("(?i)success rate[:\\s]*([0-9]+)");

        for (String line : lore) {
            String cleanLine = stripColor(line).trim();
            Matcher matcher = pattern.matcher(cleanLine);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1)); // e.g. 75
            }
        }

        return -1; // Not found
    }

    // Helper method to remove Minecraft color codes (like §a, §f, etc)
    private static String stripColor(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }

    //Success Booster parser 
    public static int getSuccessBoosterValue(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return 0;

        List<String> lore = meta.getLore();
        if (lore == null) return 0;

        Pattern pattern = Pattern.compile("(?i)success booster.*?(\\d+)");

        for (String line : lore) {
            String clean = stripColor(line);
            Matcher matcher = pattern.matcher(clean);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1)); // Return the first number found
            }
        }

        return 0; // No booster found
    }







}
