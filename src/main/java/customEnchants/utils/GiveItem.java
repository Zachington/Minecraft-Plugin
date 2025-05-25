package customEnchants.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
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
            ENCHANT_DROP_MAP.put("Gold Digger", List.of(
                new ItemDropEntry(new ItemStack(Material.DIAMOND), 0.1),
                new ItemDropEntry(new ItemStack(Material.EMERALD), 0.3),
                new ItemDropEntry(new ItemStack(Material.GOLD_INGOT), 0.5)
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