package customEnchants.utils;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


import customEnchants.utils.EnchantmentData.EnchantmentInfo;

import java.util.*;

public class ItemVoucherUtil {

    public enum VoucherType {
    TRANSMUTATION,
    DECORATION,
    MONEY_1500,
    KEY_ALL,
    PRESERVATION
}

// Loot tables for the vouchers (example items)
public static final Map<VoucherType, List<ItemStack>> LOOT_TABLES = new HashMap<>();
public static final Map<VoucherType, List<crateTableUtil.LootEntry>> WEIGHTED_LOOT_TABLES = new HashMap<>();
private static final Random random = new Random();

static {
    LOOT_TABLES.put(VoucherType.TRANSMUTATION, List.of(
        new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1),
        new ItemStack(Material.NETHERITE_INGOT, 1)
    ));

    LOOT_TABLES.put(VoucherType.DECORATION, List.of(
        new ItemStack(Material.BLACKSTONE, 32),
        new ItemStack(Material.GLOWSTONE, 5)
    ));

    EnchantmentInfo preservation = EnchantmentData.getEnchantmentInfoByName("Preservation");

    if (preservation != null) {
    Map<Integer, Double> levelWeights = Map.of(
        1, 10.0,
        2, 10.0,
        3, 10.0,
        4, 10.0,
        5, 10.0,
        6, 10.0,
        7, 10.0,
        8, 10.0,
        9, 10.0,
        10, 10.0
    );

    List<crateTableUtil.LootEntry> preservationLoot = new ArrayList<>();
    for (Map.Entry<Integer, Double> entry : levelWeights.entrySet()) {
        int level = entry.getKey();
        double weight = entry.getValue();
        ItemStack book = EnchantmentData.createEnchantedBook(preservation, level, 50, false);
        preservationLoot.add(new crateTableUtil.LootEntry(book, weight));
    }

    WEIGHTED_LOOT_TABLES.put(VoucherType.PRESERVATION, preservationLoot);
    } else {
    WEIGHTED_LOOT_TABLES.put(VoucherType.PRESERVATION, Collections.emptyList());
}

    LOOT_TABLES.put(VoucherType.MONEY_1500, Collections.emptyList());
}

    

    public static void redeemVoucher(Player player, VoucherType type) {
        switch (type) {
            case TRANSMUTATION -> {
                List<ItemStack> lootTable = LOOT_TABLES.get(type);
                if (lootTable == null || lootTable.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No loot configured for this voucher!");
                return;
                }
                for (ItemStack reward : lootTable) {
                    player.getInventory().addItem(reward.clone());
                }
                player.sendMessage(ChatColor.GREEN + "You redeemed a Transmutation voucher!");
            }
            case DECORATION -> {    
                List<ItemStack> lootTable = LOOT_TABLES.get(type);
                if (lootTable == null || lootTable.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No loot configured for this voucher!");
                return;
                }
                    Inventory gui = GuiUtil.decorVoucher(player, lootTable); // pass loot to GUI
                    player.openInventory(gui); // open it for the player
                }
            case MONEY_1500 -> {
                double amount = 1500.0;
                VaultUtil.giveMoney(player, amount);
                player.sendMessage(ChatColor.GOLD + "You received $1500!");
            }
            case KEY_ALL -> {
                List<ItemStack> loot = getKeyAllLoot();
                if (loot.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No loot configured for this voucher!");
                return;
                }

                List<String> keyNames = new ArrayList<>();

                for (ItemStack key : loot) {
                    String keyName = ChatColor.stripColor(key.getItemMeta().getDisplayName());
                    ClaimStorage.addKeys(player, keyName, 1); // add key count to claim storage
                    keyNames.add(keyName);
                }

                String messageKeys = String.join(", ", keyNames);
                player.sendMessage(ChatColor.GREEN + "Your keys (" + messageKeys + ") were added to the claim menu!");
            }
            case PRESERVATION -> {
                List<crateTableUtil.LootEntry> lootTable = WEIGHTED_LOOT_TABLES.get(type);
                if (lootTable == null || lootTable.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No loot configured for this voucher!");
                return;
                }

                ItemStack reward = getWeightedRandomItem(lootTable);
                player.getInventory().addItem(reward);
                player.sendMessage(ChatColor.GREEN + "You redeemed a Preservation voucher!");
                }


            default -> player.sendMessage(ChatColor.RED + "Unknown voucher type.");
        }
    }

    public static List<ItemStack> getKeyAllLoot() {
        List<ItemStack> loot = new ArrayList<>();

        // Always add Mining Key and Prison Key
        ItemStack miningKey = customItemUtil.createCustomItem("Mining Key");
        ItemStack prisonKey = customItemUtil.createCustomItem("Prison Key");

        if (miningKey != null) loot.add(miningKey);
        if (prisonKey != null) loot.add(prisonKey);

        // 50/50 chance for another Prison Key or Enchant Key
        if (random.nextBoolean()) {
            ItemStack anotherPrisonKey = customItemUtil.createCustomItem("Prison Key");
            if (anotherPrisonKey != null) loot.add(anotherPrisonKey);
        } else {
            ItemStack enchantKey = customItemUtil.createCustomItem("Enchant Key");
            if (enchantKey != null) loot.add(enchantKey);
        }

        return loot;
    }

    public static ItemStack getRandomEnchantedBookFromRarity(String rarity) {
        List<EnchantmentData.EnchantmentInfo> matches = new ArrayList<>();

        // Filter enchantments matching the rarity
        for (EnchantmentData.EnchantmentInfo enchant : EnchantmentData.ENCHANTMENTS) {
            if (enchant.rarity.equalsIgnoreCase(rarity)) {
                matches.add(enchant);
            }
        }

        // If none found, return null
        if (matches.isEmpty()) return null;

        // Pick random enchant from the list
        EnchantmentData.EnchantmentInfo chosen = matches.get(random.nextInt(matches.size()));

        // Random level between 1 and max level
        int level = 1;

        // Random success chance between 40% and 100%
        int chance = 40 + random.nextInt(61); // 40 to 100 inclusive

        // Create and return the book
        return EnchantmentData.createEnchantedBook(chosen, level, chance, false);
    }

    public static ItemStack getWeightedRandomItem(List<crateTableUtil.LootEntry> entries) {
    double totalWeight = entries.stream().mapToDouble(e -> e.chance).sum();
    double r = Math.random() * totalWeight;

    double cumulative = 0.0;
    for (crateTableUtil.LootEntry entry : entries) {
        cumulative += entry.chance;
        if (r <= cumulative) {
            return entry.item.clone();
        }
    }

    return entries.get(entries.size() - 1).item.clone(); // Fallback
}

}
