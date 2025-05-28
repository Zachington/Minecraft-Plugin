package customEnchants.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

//import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GiveItem {

    // Represents an item drop with a base chance per enchantment level
    public static class ItemDropEntry {
        public final ItemStack item;
        public final double baseChancePerLevel;

        public ItemDropEntry(ItemStack item, double baseChancePerLevel) {
            this.item = item;
            this.baseChancePerLevel = baseChancePerLevel;
        }
    }

    // Holds drop entries for each enchantment name
    public static class EnchantmentDropData {
        public static final Map<String, List<ItemDropEntry>> ENCHANT_DROP_MAP = new HashMap<>();
        static {
            ENCHANT_DROP_MAP.put("Key Miner", List.of(
                new ItemDropEntry(customItemUtil.createCustomItem("Mining Key"), 0.1),
                new ItemDropEntry(customItemUtil.createCustomItem("Prison Key"), 0.1),
                new ItemDropEntry(customItemUtil.createCustomItem("Enchant Key"), 0.05),
                new ItemDropEntry(customItemUtil.createCustomItem("Divine Key"), 0.03),
                new ItemDropEntry(customItemUtil.createCustomItem("Durability Key"), 0.02),
                new ItemDropEntry(customItemUtil.createCustomItem("Prestige Key"), 0.01),
                new ItemDropEntry(customItemUtil.createCustomItem("Prestige+ Key"), 0.01)
                ));
    
}

    // Utility method to check and roll for item drops
    public static void tryDropItems(String enchantName, int level, org.bukkit.Location location) {
        List<ItemDropEntry> drops = EnchantmentDropData.ENCHANT_DROP_MAP.get(enchantName);
        if (drops == null) return;

        Random random = new Random();
        for (ItemDropEntry entry : drops) {
            double chance = Math.min(entry.baseChancePerLevel * level, 1.0);
            if (random.nextDouble() < chance) {
                location.getWorld().dropItemNaturally(location, entry.item.clone());
            }
        }
    }
}
}