package customEnchants.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Location;


import customEnchants.utils.EnchantmentData.EnchantmentInfo;
import customEnchants.utils.GiveItem.EnchantmentDropData;
import customEnchants.utils.GiveItem.ItemDropEntry;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.List;


public class EnchantFunctionUtil {

    private static final Random random = new Random();


    public static boolean handleDelayedDynamite(BlockBreakEvent event, HeldToolInfo tool, JavaPlugin plugin) {
        Block block = event.getBlock();
        PersistentDataContainer blockData = block.getChunk().getPersistentDataContainer();

        String keyStr = getBlockKey(block);
        NamespacedKey hitsKey = new NamespacedKey(plugin, keyStr + "_hits");
        NamespacedKey levelKey = new NamespacedKey(plugin, keyStr + "_level");

        // Check if this block is a delayed dynamite coal block by looking for your keys
        if (blockData.has(hitsKey, PersistentDataType.INTEGER) && blockData.has(levelKey, PersistentDataType.INTEGER)) {
            int hits = blockData.getOrDefault(hitsKey, PersistentDataType.INTEGER, 0);
            int level = blockData.getOrDefault(levelKey, PersistentDataType.INTEGER, 1);

            hits++;
            if (hits >= 10) {
                block.setType(Material.AIR);
                float explosionPower = 4.0F + (level - 1);
                block.getWorld().createExplosion(block.getLocation(), explosionPower, false, true); // no fire, no block damage
                blockData.remove(hitsKey);
                blockData.remove(levelKey);
            } else {
                blockData.set(hitsKey, PersistentDataType.INTEGER, hits);
                block.setType(Material.COAL_BLOCK);  // Reset it back to coal after each hit
            }
            event.setCancelled(true); // Cancel to prevent normal drops
            return true;
        }

        // Existing delayed dynamite enchant proc check
        if (tool != null && tool.customEnchants.containsKey("Delayed Dynamite")) {
            EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Delayed Dynamite");
            int level = tool.getLevel("Delayed Dynamite");
            double procChance = info.procChance * level;

            if (event.isCancelled()) {
                return false;  // Don't do anything if another plugin blocked this break
            }

            if (random.nextDouble() < procChance) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Block newBlock = block.getLocation().getBlock();
                    if (newBlock.getType() == Material.AIR) { // ensure block is still broken
                        newBlock.setType(Material.COAL_BLOCK);
                        PersistentDataContainer delayedBlockData = newBlock.getChunk().getPersistentDataContainer();
                        delayedBlockData.set(hitsKey, PersistentDataType.INTEGER, 0);
                        delayedBlockData.set(levelKey, PersistentDataType.INTEGER, level);
                    }
                }, 1L);
            }
        }
        return false;
    }

    public static boolean handleRegenerate(BlockBreakEvent event, HeldToolInfo tool, JavaPlugin plugin) {
    Block block = event.getBlock();
    Material type = block.getType();

    // Only apply to ores
    if (!isOre(type)) return false;

    if (tool != null && tool.customEnchants.containsKey("Regenerate")) {
        EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Regenerate");
        int level = tool.getLevel("Regenerate");
        double procChance = info.procChance * level;

        if (random.nextDouble() < procChance) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Block regenBlock = block.getLocation().getBlock();
                if (regenBlock.getType() == Material.AIR) {
                    regenBlock.setType(type);  // regenerate original ore
                }
            }, 20L); // 1 second delay (20 ticks) to simulate natural regen
        }
    }
    return false;
}

    public static boolean handleConjure(BlockBreakEvent event, HeldToolInfo tool, JavaPlugin plugin) {
    Block block = event.getBlock();
    Material type = block.getType();

    if (!isOre(type)) return false;

    if (tool != null && tool.customEnchants.containsKey("Conjure")) {
        // Always cancel drops before proc check
        event.setDropItems(false);

        EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Conjure");
        int level = tool.getLevel("Conjure");
        double procChance = info.procChance * level;

        if (random.nextDouble() < procChance) {
            Material upgraded = getUpgradedBlock(type);
            if (upgraded != null) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    block.setType(upgraded);
                }, 1L);
            }
            return true;
        } else {
            // If proc fails, manually drop original block drops
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(type));
        }
    }
    return false;
}

    public static void handleXpSyphon(BlockBreakEvent event, HeldToolInfo tool, JavaPlugin plugin) {
    if (tool == null || !tool.customEnchants.containsKey("Xp Syphon")) {
        return;
    }

    EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Xp Syphon");
    int level = tool.getLevel("Xp Syphon");
    double procChance = info.procChance;  

    if (event.isCancelled()) {
        return;  
    }

    if (random.nextDouble() < procChance) {
        Player player = event.getPlayer();
        int xpAmount = 1 * level;
        player.giveExp(xpAmount);
    }
}

    public static boolean handleLightWeight(BlockBreakEvent event, HeldToolInfo tool, JavaPlugin plugin) {
    if (tool == null || !tool.customEnchants.containsKey("Light Weight")) {
        return false;
    }

    EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Light Weight");
    int level = tool.getLevel("Light Weight");
    double procChance = info.procChance * level;

    if (event.isCancelled()) {
        return false;
    }

    if (random.nextDouble() < procChance) {
        Player player = event.getPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 10, 0)); // Haste I for 10 seconds
        player.sendMessage(ChatColor.GREEN + "Light Weight activated! You feel faster.");
        return true;
    }
    return false;
}

    public static boolean handleSpeedBreaker(BlockBreakEvent event, HeldToolInfo tool, JavaPlugin plugin) {
    if (tool == null || !tool.customEnchants.containsKey("Speed Breaker")) {
        return false;
    }

    EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Speed Breaker");
    int level = tool.getLevel("Speed Breaker");
    double procChance = info.procChance * level;
    Player player = event.getPlayer();

    if (event.isCancelled()) {
        return false;
    }

    if (player.hasPotionEffect(PotionEffectType.HASTE)) {
        return false; // Do nothing if haste is already active
    }

    if (random.nextDouble() < procChance) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 10, 0)); // Haste I for 10 sec
        return true;
    }
    return false;
}

    public static void handleSprinter(BlockBreakEvent event, HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Sprinter")) {
        return;
    }

    EnchantmentData.EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Sprinter");
    if (info == null) return; // safety check

    int level = tool.getLevel("Sprinter");
    double procChance = info.procChance * level;

    if (random.nextDouble() < procChance) {
        Player player = event.getPlayer();
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.SPEED,
            100, // 5 seconds (20 ticks = 1 second)
            0,  // Speed I
            false, false, false
        ));
    }
}

    public static boolean handleBounder(BlockBreakEvent event, HeldToolInfo tool) {
        if (tool == null || !tool.customEnchants.containsKey("Bounder")) {
            return false;
        }

        Player player = event.getPlayer();
        int level = tool.getLevel("Bounder");
        EnchantmentData.EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Bounder");
        if (info == null) return false;

        double procChance = info.procChance * level;
        if (random.nextDouble() < procChance) {
            // Apply Jump Boost 1 for 2 seconds (40 ticks)
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 0, true, false, false));
            return true;
        }

        return false;
    }

    public static void handleKeyMiner(BlockBreakEvent event, HeldToolInfo tool) {
        Player player = event.getPlayer();
        int level = tool.getLevel("Key Miner");
        if (level <= 0) return;

        int enchantIndex = EnchantmentData.getEnchantmentIndex("Key Miner");
        if (enchantIndex < 0 || enchantIndex >= EnchantmentData.ENCHANT_PROC_CHANCE.length) return;

        double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex];
        double amplifyBonus = getAmplifyLevel(player.getInventory().getItemInMainHand()) * 0.05;
        double chance = Math.min(baseChance * level + amplifyBonus, 1.0);

        if (random.nextDouble() >= chance) return;

        List<ItemDropEntry> drops = EnchantmentDropData.ENCHANT_DROP_MAP.get("Key Miner");
        if (drops == null || drops.isEmpty()) return;

        // Weighted random drop selection
        double totalWeight = 0;
        for (ItemDropEntry entry : drops) {
            totalWeight += Math.min(entry.baseChancePerLevel * level, 1.0);
        }

        double r = random.nextDouble() * totalWeight;
        double cumulative = 0;
        for (ItemDropEntry entry : drops) {
            double weight = Math.min(entry.baseChancePerLevel * level, 1.0);
            cumulative += weight;
            if (r <= cumulative) {
                String keyName = ChatColor.stripColor(entry.item.getItemMeta().getDisplayName());
                ClaimStorage.addKeys(player, keyName, 1);
                player.sendMessage(ChatColor.GREEN + "You received a " + keyName + "!");
                break;
            }
        }
    }

    public static void handleGoldDigger(BlockBreakEvent event, HeldToolInfo tool) {
    Player player = event.getPlayer();
    int level = tool.getLevel("Gold Digger");
    if (level <= 0) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Gold Digger");
    if (enchantIndex < 0 || enchantIndex >= EnchantmentData.ENCHANT_PROC_CHANCE.length) return;

    double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex];
    double chance = Math.min(baseChance * level, 1.0);

    if (random.nextDouble() >= chance) return;

    String playerRank = RankUtils.getRank(player);
    int prestige = RankUtils.getPrestigeFromRank(playerRank);
    
    double base = 500 + (1000 * prestige);
    double variance = Math.random() * 1000;
    double money = base + variance;

    VaultUtil.giveMoney(player, money);
    player.sendMessage(ChatColor.GOLD + "You found $" + String.format("%.2f", money) + " from Gold Digger!");
}


    public static void handleVeinMiner(BlockBreakEvent event, HeldToolInfo tool) {
    Player player = event.getPlayer();
    ItemStack toolItem = player.getInventory().getItemInMainHand();

    int level = tool.getLevel("Vein Miner");
    if (level <= 0) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Vein Miner");
    if (enchantIndex < 0 || enchantIndex >= EnchantmentData.ENCHANT_PROC_CHANCE.length) return;

    double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex];
    double chance = Math.min(baseChance * level, 1.0);

    if (Math.random() > chance) return;

    Block origin = event.getBlock();
    Material targetType = origin.getType();

    // Only allow ores (adjust as needed)
    if (!isOre(targetType)) return;

    // Cancel default drop to avoid double drop
    event.setDropItems(false);

    Set<Block> blocksToBreak = new HashSet<>();
    findConnectedOres(origin, targetType, blocksToBreak, origin.getLocation(), 3);

    // Break all found blocks naturally
    for (Block block : blocksToBreak) {
        block.breakNaturally(toolItem);
    }
}











    private static void findConnectedOres(Block block, Material targetType, Set<Block> found, Location origin, int radius) {
    if (found.contains(block)) return;
    if (block.getType() != targetType) return;
    if (block.getLocation().distance(origin) > radius) return;

    found.add(block);

    // Check all adjacent blocks (6 directions)
    for (BlockFace face : List.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
        Block relative = block.getRelative(face);
        findConnectedOres(relative, targetType, found, origin, radius);
    }
}

    private static int getAmplifyLevel(ItemStack tool) {
        HeldToolInfo heldInfo = HeldToolInfo.fromItem(tool);
        return heldInfo != null ? heldInfo.getLevel("Amplify") : 0;
    }

    private static String getBlockKey(Block block) {
        // Example: return block location as string key or something unique per block
        return block.getWorld().getName() + "_" + block.getX() + "_" + block.getY() + "_" + block.getZ();
    }

    private static boolean isOre(Material type) {
    return switch (type) {
        case COAL_ORE, IRON_ORE, GOLD_ORE, DIAMOND_ORE, EMERALD_ORE, COPPER_ORE, REDSTONE_ORE, LAPIS_ORE,
            DEEPSLATE_COAL_ORE, DEEPSLATE_IRON_ORE, DEEPSLATE_GOLD_ORE, DEEPSLATE_DIAMOND_ORE,
            DEEPSLATE_EMERALD_ORE, DEEPSLATE_COPPER_ORE, DEEPSLATE_REDSTONE_ORE, DEEPSLATE_LAPIS_ORE -> true;
        default -> false;
    };
}

    private static Material getUpgradedBlock(Material ore) {
    return switch (ore) {
        case COAL_ORE, DEEPSLATE_COAL_ORE -> Material.COAL_BLOCK;
        case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.RAW_IRON_BLOCK;
        case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.RAW_COPPER_BLOCK;
        case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.RAW_GOLD_BLOCK;
        case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> Material.DIAMOND_BLOCK;
        case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> Material.EMERALD_BLOCK;
        case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> Material.REDSTONE_BLOCK;
        case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> Material.LAPIS_BLOCK;
        default -> null;
    };
}


}
