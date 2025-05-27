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
            new LootEntry(
                EnchantmentData.createEnchantedBook(getEnchantmentInfoByName("Blast"), 1, 100, true),9,getEnchantmentInfoByName("Blast")),
            new LootEntry(EnchantmentData.createEnchantedBook(
                getEnchantmentInfoByName("Ore Scavenger"), 1, 100, true), 9, getEnchantmentInfoByName("Ore Scavenger")),
            new LootEntry(EnchantmentData.createEnchantedBook(
                getEnchantmentInfoByName("Wall Breaker"), 1, 100, true), 9, getEnchantmentInfoByName("Wall Breaker")),
            new LootEntry(EnchantmentData.createEnchantedBook(
                getEnchantmentInfoByName("Unbreakable"), 1, 100, true), 9, getEnchantmentInfoByName("Unbreakable")),
            new LootEntry(EnchantmentData.createEnchantedBook(
                getEnchantmentInfoByName("Auto Smelt"), 1, 100, true), 9, getEnchantmentInfoByName("Auto Smelt")),
            new LootEntry(EnchantmentData.createEnchantedBook(
                getEnchantmentInfoByName("Magnet"), 1, 100, true), 9, getEnchantmentInfoByName("Magnet")),
            new LootEntry(EnchantmentData.createEnchantedBook(
                getEnchantmentInfoByName("Preservation"), 1, 100, true), 9, getEnchantmentInfoByName("Preservation")),
            new LootEntry(EnchantmentData.createEnchantedBook(
                getEnchantmentInfoByName("Gold Digger"), 1, 100, true), 9, getEnchantmentInfoByName("Gold Digger")),
            new LootEntry(EnchantmentData.createEnchantedBook(
                getEnchantmentInfoByName("Frost Touch"), 1, 100, true), 9, getEnchantmentInfoByName("Frost Touch")),
            new LootEntry(EnchantmentData.createEnchantedBook(
                getEnchantmentInfoByName("Amplify"), 1, 100, true), 9, getEnchantmentInfoByName("Amplify")),
            new LootEntry(new ItemStack(Material.EXPERIENCE_BOTTLE, 32), 10)
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
