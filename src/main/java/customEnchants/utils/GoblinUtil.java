package customEnchants.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


import org.bukkit.Material;
import org.bukkit.entity.Player;



public class GoblinUtil {

    public static class LootGoblinType {
    public final String name;
    public final String minRank;
    public final int spawnChance;
    public final int guaranteedInterval;
    public final List<ItemStack> drops;
    public final List<PotionEffect> potionEffects;
    public final ItemStack[] equipment;

    public LootGoblinType(String name, String minRank, int spawnChance, int guaranteedInterval,
                        List<ItemStack> drops, List<PotionEffect> potionEffects, ItemStack[] equipment) {
        this.name = name;
        this.minRank = minRank;
        this.spawnChance = spawnChance;
        this.guaranteedInterval = guaranteedInterval;
        this.drops = drops;
        this.potionEffects = potionEffects;
        this.equipment = equipment;
    }
}
    List<ItemStack> drops = Arrays.asList(customItemUtil.createCustomItem("Prestige Key"), customItemUtil.createCustomItem("Prestige+ Key"));

    // Register your goblin types here
    public static final Map<String, LootGoblinType> goblins = new HashMap<>();
static {
    goblins.put("CommonGoblin", new LootGoblinType(
        "§7Common Goblin",
        "a",
        10000,
        1000000000,
        Arrays.asList(
            new ItemStack(Material.COAL_BLOCK, 32),
            new ItemStack(Material.RAW_COPPER_BLOCK, 16),
            new ItemStack(Material.RAW_IRON_BLOCK, 16)
        ),
        Arrays.asList(
            new PotionEffect(PotionEffectType.SPEED, 20 * 30, 0) // speed effect, 30 seconds
        ),
    new ItemStack[] {
        new ItemStack(Material.WOODEN_SWORD),  // Main hand
        new ItemStack(Material.LEATHER_HELMET),
        new ItemStack(Material.LEATHER_CHESTPLATE),
        new ItemStack(Material.LEATHER_LEGGINGS),
        new ItemStack(Material.LEATHER_BOOTS)
    }
    ));

    goblins.put("RareGoblin", new LootGoblinType(
    "§9Rare Goblin",
    "a",
    50000,
    1000000000,
    Arrays.asList(
        customItemUtil.createCustomItem("Mining Key"),
        customItemUtil.createCustomItem("Prison Key"),
        customItemUtil.createCustomItem("Enchant Key")
    ),
    Arrays.asList(
        new PotionEffect(PotionEffectType.SPEED, 20 * 30, 0) // speed effect, 30 seconds
    ),
    new ItemStack[] {
        new ItemStack(Material.STONE_SWORD),  // Main hand
        new ItemStack(Material.CHAINMAIL_HELMET),
        new ItemStack(Material.CHAINMAIL_CHESTPLATE),
        new ItemStack(Material.CHAINMAIL_LEGGINGS),
        new ItemStack(Material.CHAINMAIL_BOOTS)
    }
));

    goblins.put("LegendaryGoblin", new LootGoblinType(
    "§eLegendary Goblin",
    "a",
    75000,
    1000000000,
    Arrays.asList(
        customItemUtil.createCustomItem("Divine Key"),
        customItemUtil.createCustomItem("§6Legendary Enchant")
    ),
    Arrays.asList(
        new PotionEffect(PotionEffectType.SPEED, 20 * 30, 0) // speed effect, 30 seconds
    ),
    new ItemStack[] {
        new ItemStack(Material.IRON_SWORD),  // Main hand
        new ItemStack(Material.IRON_HELMET),
        new ItemStack(Material.IRON_CHESTPLATE),
        new ItemStack(Material.IRON_LEGGINGS),
        new ItemStack(Material.IRON_BOOTS)
    }
));

    goblins.put("PrestigeGoblin", new LootGoblinType(
        "§dPrestige Goblin",
        "p1a",
        750000,
        350000,
        Arrays.asList(customItemUtil.createCustomItem("Prestige Key")), 
        Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 1)),
        new ItemStack[] {
        new ItemStack(Material.DIAMOND_SWORD),  // Main hand
        new ItemStack(Material.DIAMOND_HELMET),
        new ItemStack(Material.DIAMOND_CHESTPLATE),
        new ItemStack(Material.DIAMOND_LEGGINGS),
        new ItemStack(Material.DIAMOND_BOOTS)
    } 
    ));

    goblins.put("Prestige+Goblin", new LootGoblinType(
        "§4Prestige+ Goblin",
        "p10a",
        1000000,
        100000,
        Arrays.asList(customItemUtil.createCustomItem("Prestige+ Key")),
        Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 1)),
        new ItemStack[] {
        new ItemStack(Material.NETHERITE_SWORD),  // Main hand
        new ItemStack(Material.NETHERITE_HELMET),
        new ItemStack(Material.NETHERITE_CHESTPLATE),
        new ItemStack(Material.NETHERITE_LEGGINGS),
        new ItemStack(Material.NETHERITE_BOOTS)
    } 
    ));

    // Add more goblins here in the same way...
}

    // Utility: check if player meets min rank
    public static boolean canSpawn(Player player, LootGoblinType goblin) {
    String playerRank = RankUtils.getRank(player);
    String requiredRank = goblin.minRank;

    if (requiredRank == null || requiredRank.isEmpty()) {
        return true; // No rank requirement
    }

    return RankUtils.compareRanks(playerRank, requiredRank) >= 0;
}

    
}
