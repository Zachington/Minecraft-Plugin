package customEnchants.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import customEnchants.utils.EnchantmentData.EnchantmentInfo;

public class crateTableUtil {

    public static final Map<String, List<LootEntry>> LOOT_TABLES = new HashMap<>();
    public static final List<EnchantmentInfo> ENCHANTMENTS = new ArrayList<>();

    static {
        // Fill ENCHANTMENTS list
        for (int i = 0; i < EnchantmentData.ENCHANT_NAMES.length; i++) {
            ENCHANTMENTS.add(EnchantmentData.getEnchantmentInfo(i));
        }

        // Mining Key loot
        LOOT_TABLES.put("Mining Key", Arrays.asList(
            new LootEntry(new ItemStack(Material.DIAMOND, 16), 40),
            new LootEntry(new ItemStack(Material.EMERALD, 8), 30),
            new LootEntry(new ItemStack(Material.IRON_INGOT, 32), 30)
        ));



        // Enchant Key loot
        LOOT_TABLES.put("Enchant Key", Arrays.asList(
            new LootEntry(customItemUtil.createCustomItem("Common Enchant"), 40),
            new LootEntry(customItemUtil.createCustomItem("Uncommon Enchant"), 25),
            new LootEntry(customItemUtil.createCustomItem("Rare Enchant"), 20),
            new LootEntry(customItemUtil.createCustomItem("Epic Enchant"), 10),
            new LootEntry(customItemUtil.createCustomItem("Legendary Enchant"), 5)
        ));
    }



    public static EnchantmentInfo getEnchantmentInfoByName(String name) {
        for (EnchantmentInfo info : ENCHANTMENTS) {
            if (info.name.equalsIgnoreCase(name)) return info;
        }
        return null;
    }

    public static class LootEntry {
    public final ItemStack item;
    public final double chance;
    public final EnchantmentInfo enchantmentInfo; // <-- new field

    // Constructor for entries without enchantment info
    public LootEntry(ItemStack item, double chance) {
        this(item, chance, null); // delegate to full constructor
    }

    // Constructor with enchantment info
    public LootEntry(ItemStack item, double chance, EnchantmentInfo enchantmentInfo) {
        this.item = item;
        this.chance = chance;
        this.enchantmentInfo = enchantmentInfo;
    }
}
}
