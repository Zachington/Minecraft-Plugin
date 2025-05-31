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

        LOOT_TABLES.put("Divine Key", Arrays.asList(
            new LootEntry(new ItemStack(Material.DIAMOND, 5), 20),
            new LootEntry(new ItemStack(Material.DIAMOND, 10), 20),
            new LootEntry(customItemUtil.createCustomItem("Deepslate Coal Extractor"), 5),
            new LootEntry(customItemUtil.createCustomItem("Diamond Extractor"), 10),
            ((java.util.function.Supplier<LootEntry>) () -> {
                ItemStack key = customItemUtil.createCustomItem("Enchant Key");
                key.setAmount(2);
                return new LootEntry(key, 20);
            }).get(),
            new LootEntry(customItemUtil.createCustomItem("Durability Key"), 20),
            new LootEntry(customItemUtil.createCustomItem("Black Scroll"), 25)
        ));

        LOOT_TABLES.put("Durability Key", Arrays.asList(
            new LootEntry(customItemUtil.createDurabilityShard(1), 10),
            new LootEntry(customItemUtil.createDurabilityShard(2), 10),
            new LootEntry(customItemUtil.createDurabilityShard(3), 10),
            new LootEntry(customItemUtil.createDurabilityShard(4), 10),
            new LootEntry(customItemUtil.createDurabilityShard(5), 10),
            new LootEntry(customItemUtil.createDurabilityShard(6), 10),
            new LootEntry(customItemUtil.createDurabilityShard(7), 10),
            new LootEntry(customItemUtil.createCustomItem("Preservation Voucher"), 30)
        ));

        LOOT_TABLES.put("Prison Key", Arrays.asList(
            new LootEntry(customItemUtil.createCustomItem("Divine Key"), 5),
            new LootEntry(customItemUtil.createCustomItem("Enchant Key"), 15),
            ((java.util.function.Supplier<LootEntry>) () -> {
                ItemStack key = customItemUtil.createCustomItem("Mining Key");
                key.setAmount(2);
                return new LootEntry(key, 30);
            }).get(),
            new LootEntry(customItemUtil.createCustomItem("Copper Extractor"), 5),
            new LootEntry(customItemUtil.createCustomItem("Decoration Voucher"), 45)
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
    public final EnchantmentInfo enchantmentInfo; 

    // Constructor for entries without enchantment info
    public LootEntry(ItemStack item, double chance) {
        this(item, chance, null); 
    }

    // Constructor with enchantment info
    public LootEntry(ItemStack item, double chance, EnchantmentInfo enchantmentInfo) {
        this.item = item;
        this.chance = chance;
        this.enchantmentInfo = enchantmentInfo;
    }
}
}
