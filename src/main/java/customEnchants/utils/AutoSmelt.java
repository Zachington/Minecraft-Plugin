package customEnchants.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class AutoSmelt {

    private static final Map<Material, Material> smeltMap = new HashMap<>();
    private static final Random random = new Random();

    public static boolean canSmelt(Material material) {
        return smeltMap.containsKey(material);
    }

    static {
        smeltMap.put(Material.IRON_ORE, Material.IRON_INGOT);
        smeltMap.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        smeltMap.put(Material.COBBLESTONE, Material.STONE);
        smeltMap.put(Material.SAND, Material.GLASS);
        smeltMap.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        // Add more as needed
    }

    public static final NamespacedKey FORTUNE_KEY = NamespacedKey.minecraft("fortune");
    public static void tryAutoSmelt(Block block, ItemStack tool, int autoSmeltLevel) {
    Material smeltedMaterial = smeltMap.get(block.getType());
    if (smeltedMaterial == null) {
        Bukkit.getLogger().info("Smelted material not found for block: " + block.getType());
        return;
    }

    Bukkit.getLogger().info("Smelting block " + block.getType() + " into " + smeltedMaterial);

    block.setType(Material.AIR);

    Enchantment fortune = Enchantment.getByKey(NamespacedKey.minecraft("fortune"));
    int fortuneLevel = (fortune != null) ? tool.getEnchantmentLevel(fortune) : 0;
    int amount = smeltedMaterial == Material.COPPER_INGOT
        ? 3 + random.nextInt(fortuneLevel + 1)  // COPPER special case
        : 1 + random.nextInt(fortuneLevel + 1);

    Bukkit.getLogger().info("Dropping " + amount + " of " + smeltedMaterial);
    ItemStack smeltedDrop = new ItemStack(smeltedMaterial, amount);
    block.getWorld().dropItemNaturally(block.getLocation(), smeltedDrop);
}

}
