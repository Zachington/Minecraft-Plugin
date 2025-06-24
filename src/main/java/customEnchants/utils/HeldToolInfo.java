package customEnchants.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HeldToolInfo {

    // Map enchantment name -> level
    public Map<String, Integer> customEnchants;

    public HeldToolInfo() {
        customEnchants = new HashMap<>();
    }

    // Optionally, add helper methods

    public int getLevel(String enchantName) {
        return customEnchants.getOrDefault(enchantName, 0);
    }

    public void setLevel(String enchantName, int level) {
        customEnchants.put(enchantName, level);
    }
    public static HeldToolInfo fromItem(ItemStack tool) {
        HeldToolInfo info = new HeldToolInfo();
        if (tool == null || !tool.hasItemMeta()) return info;

        ItemMeta meta = tool.getItemMeta();
        if (meta == null || !meta.hasLore()) return info;

        List<String> lore = meta.getLore();
        for (String line : lore) {
            String cleanLine = ChatColor.stripColor(line).trim();
            if (cleanLine.isEmpty()) continue;

            for (int i = 0; i < EnchantmentData.getEnchantmentCount(); i++) {
                EnchantmentData.EnchantmentInfo enchantInfo = EnchantmentData.getEnchantmentInfo(i);
                if (enchantInfo == null) continue;

                String enchantName = enchantInfo.name;
                if (cleanLine.startsWith(enchantName + " ")) {
                    String levelPart = cleanLine.substring(enchantName.length()).trim();
                    int level = parseRomanNumeral(levelPart);
                    if (level != -1) {
                        info.customEnchants.put(enchantName, level);
                    }
                    break;
                }
            }
        }

        return info;
    }
    private static int parseRomanNumeral(String levelStr) {
    switch (levelStr) {
        case "I": return 1;
        case "II": return 2;
        case "III": return 3;
        case "IV": return 4;
        case "V": return 5;
    }
    // Try parsing as Arabic number for levels above 5
    try {
        int num = Integer.parseInt(levelStr);
        if (num >= 6) {
            return num;
        }
    } catch (NumberFormatException e) {
        // Ignore, will return -1 below
    }
    return -1;
}

    public boolean hasEcho() {
    return customEnchants.containsKey("Legends Echo") || customEnchants.containsKey("Final Echo");
    }

    public double getEchoMultiplier() {
        if (customEnchants.containsKey("Final Echo")) return 2.0; // +100%
        if (customEnchants.containsKey("Legends Echo")) return 1.5; // +50%
        return 1.0;
    }
}