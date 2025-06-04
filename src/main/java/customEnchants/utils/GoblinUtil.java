package customEnchants.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;



public class GoblinUtil {

    public static class LootGoblinType {
    public final String name;
    public final String minRank;
    public final int spawnChance;
    public final int guaranteedInterval;
    public final List<ItemStack> drops;
    public final List<PotionEffect> potionEffects;

    public LootGoblinType(String name, String minRank, int spawnChance, int guaranteedInterval,
                        List<ItemStack> drops, List<PotionEffect> potionEffects) {
        this.name = name;
        this.minRank = minRank;
        this.spawnChance = spawnChance;
        this.guaranteedInterval = guaranteedInterval;
        this.drops = drops;
        this.potionEffects = potionEffects;
    }
}
    List<ItemStack> drops = Arrays.asList(customItemUtil.createCustomItem("Prestige Key"), customItemUtil.createCustomItem("Prestige+ Key"));

    // Register your goblin types here
    public static final Map<String, LootGoblinType> goblins = new HashMap<>();
static {
    goblins.put("PrestigeGoblin", new LootGoblinType(
        "Prestige Goblin",
        "p1a",
        75000,
        10,
        Arrays.asList(customItemUtil.createCustomItem("Prestige Key")), 
        Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 1)) // speed effect, 60 seconds
    ));

    goblins.put("Prestige+Goblin", new LootGoblinType(
        "Prestige+ Goblin",
        "p10a",
        100000,
        10,
        Arrays.asList(customItemUtil.createCustomItem("Prestige+ Key")),
        Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 1)) // invisibility, 30 seconds
    ));

    // Add more goblins here in the same way...
}

    // Utility: check if player meets min rank
    public static boolean canSpawn(Player player, LootGoblinType goblin) {
        // Use your RankUtils to check rank
        switch (goblin.minRank) {
            case "p1a":
                return RankUtils.isAtLeastP1a(player);
            case "p10a":
                return RankUtils.isAtLeastP10a(player);
            default:
                return true;
        }
    }

    
}
