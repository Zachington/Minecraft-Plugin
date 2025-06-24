package customEnchants.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Consumer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import customEnchants.TestEnchants;
import customEnchants.managers.SellManager;
import customEnchants.utils.EnchantmentData.EnchantmentInfo;
import customEnchants.utils.GiveItem.EnchantmentDropData;
import customEnchants.utils.GiveItem.ItemDropEntry;

import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class EnchantFunctionUtil {

    private static final Random random = new Random();
    private static EssenceManager essenceManager = null;
    private static final Set<UUID> autoSellActive = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Double> wealthPulseEarnings = new HashMap<>();
    private static final Map<UUID, Double> autoSellEarnings = new HashMap<>();

    public EnchantFunctionUtil(EssenceManager essenceManager) {
        EnchantFunctionUtil.essenceManager = essenceManager;
    }


    public static boolean handleDelayedDynamite(BlockBreakEvent event, HeldToolInfo tool, JavaPlugin plugin) {
    Block block = event.getBlock();
    PersistentDataContainer blockData = block.getChunk().getPersistentDataContainer();

    String keyStr = getBlockKey(block);
    NamespacedKey hitsKey = new NamespacedKey(plugin, keyStr + "_hits");
    NamespacedKey levelKey = new NamespacedKey(plugin, keyStr + "_level");

    // Check if this is a ticking dynamite block
    if (blockData.has(hitsKey, PersistentDataType.INTEGER) && blockData.has(levelKey, PersistentDataType.INTEGER)) {
        int hits = blockData.getOrDefault(hitsKey, PersistentDataType.INTEGER, 0);
        int level = blockData.getOrDefault(levelKey, PersistentDataType.INTEGER, 1);

        hits++;
        if (hits >= 10) {
            block.setType(Material.AIR);
            float basePower = 4.0F + (level - 1);

            boolean echoed = tryEcho("Delayed Dynamite", event, tool, multiplier -> {
                float explosionPower = (float) (basePower * multiplier);
                block.getWorld().createExplosion(block.getLocation(), explosionPower, false, true);
            });

            if (!echoed) {
                block.getWorld().createExplosion(block.getLocation(), basePower, false, true);
            }

            blockData.remove(hitsKey);
            blockData.remove(levelKey);
        } else {
            blockData.set(hitsKey, PersistentDataType.INTEGER, hits);
            block.setType(Material.COAL_BLOCK);
        }

        event.setCancelled(true);
        return true;
    }

    // Base enchant trigger
    boolean baseProc = false;
    if (tool != null && tool.customEnchants.containsKey("Delayed Dynamite")) {
        if (!event.isCancelled()) {
            EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Delayed Dynamite");
            int level = tool.getLevel("Delayed Dynamite");
            double procChance = info.procChance * level;

            if (random.nextDouble() < procChance) {
                baseProc = true;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Block newBlock = block.getLocation().getBlock();
                    if (newBlock.getType() == Material.AIR) {
                        newBlock.setType(Material.COAL_BLOCK);
                        PersistentDataContainer delayedBlockData = newBlock.getChunk().getPersistentDataContainer();
                        delayedBlockData.set(hitsKey, PersistentDataType.INTEGER, 0);
                        delayedBlockData.set(levelKey, PersistentDataType.INTEGER, level);
                    }
                }, 1L);
            }
        }
    }

    // Independent echo proc (in case base failed)
    tryEcho("Delayed Dynamite", event, tool, multiplier -> {
        // Echo will trigger explosion instantly
        float power = (float) ((4.0F + tool.getLevel("Delayed Dynamite") - 1) * multiplier);
        block.getWorld().createExplosion(block.getLocation(), power, false, true);
    });

    return baseProc;
}

    public static boolean handleRegenerate(BlockBreakEvent event, HeldToolInfo tool, JavaPlugin plugin) {
    Block block = event.getBlock();
    Material type = block.getType();

    if (!isOre(type)) return false;
    if (tool == null || !tool.customEnchants.containsKey("Regenerate")) return false;

    EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Regenerate");
    int level = tool.getLevel("Regenerate");
    double procChance = info.procChance * level;

    boolean baseProc = random.nextDouble() < procChance;

    if (baseProc) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (block.getType() == Material.AIR) {
                block.setType(type);
            }
        }, 20L);
    }

    tryEcho("Regenerate", event, tool, multiplier -> {
        // Run effect either way, but scaled
        Location loc = block.getLocation();
        World world = loc.getWorld();
        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();

        int radius = 1;
        int maxBlocks = (int) Math.round(multiplier);
        int replaced = 0;

        outerLoop:
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block adj = world.getBlockAt(cx + x, cy + y, cz + z);
                    Material adjType = adj.getType();
                    if (adjType != Material.BEDROCK) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (adj.getType() != type) {
                                adj.setType(type);
                            }
                        }, 20L);
                        replaced++;
                        if (replaced >= maxBlocks) break outerLoop;
                    }
                }
            }
        }
    });

    return baseProc;
}

    public static boolean handleConjure(BlockBreakEvent event, HeldToolInfo tool, JavaPlugin plugin) {
    Block block = event.getBlock();
    Material type = block.getType();
    Player player = event.getPlayer();
    ItemStack toolItem = player.getInventory().getItemInMainHand();

    if (!isOre(type) || tool == null || !tool.customEnchants.containsKey("Conjure")) return false;

    event.setDropItems(false); // always cancel drops

    EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Conjure");
    int level = tool.getLevel("Conjure");
    double procChance = info.procChance * level;

    boolean baseProc = false;
    Material upgraded = getUpgradedBlock(type);

    if (upgraded != null && random.nextDouble() < procChance) {
        baseProc = true;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            block.setType(upgraded);
        }, 1L);

        applyConjureEcho(block, upgraded, plugin, level, event, tool);
    } else {
        block.breakNaturally(toolItem);
    }

    // Independent echo proc even if base failed
    if (!baseProc && upgraded != null) {
        tryEcho("Conjure", event, tool, multiplier -> {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                block.setType(upgraded);
            }, 1L);

            conjureNearbyBlocks(block, upgraded, plugin, multiplier);
        });
    }

    return baseProc;
}

    public static void handleXpSyphon(BlockBreakEvent event, HeldToolInfo tool, JavaPlugin plugin) {
    if (tool == null || !tool.customEnchants.containsKey("Xp Syphon") || event.isCancelled()) return;

    Player player = event.getPlayer();
    EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Xp Syphon");
    int level = tool.getLevel("Xp Syphon");
    double procChance = info.procChance;

    int xpAmount = 1 * level;
    boolean baseProc = random.nextDouble() < procChance;

    if (baseProc) {
        player.giveExp(xpAmount);

        tryEcho("Xp Syphon", event, tool, multiplier -> {
            int amplified = (int) Math.ceil(xpAmount * multiplier);
            player.giveExp(amplified);
        });
    } else {
        // Echo-only path
        tryEcho("Xp Syphon", event, tool, multiplier -> {
            int amplified = (int) Math.ceil(xpAmount * multiplier);
            player.giveExp(amplified);
        });
    }
}

    public static boolean handleLightWeight(BlockBreakEvent event, HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Light Weight")) return false;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Light Weight");
    if (enchantIndex < 0) return false;
    int level = tool.getLevel("Light Weight");
    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;

    if (event.isCancelled()) return false;

    Player player = event.getPlayer();

    boolean baseProc = random.nextDouble() < procChance;
    if (baseProc) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 10, 0));
    }

    tryEcho("Light Weight", event, tool, multiplier -> {
        int hasteLevel = (int) (multiplier - 1);
        if (hasteLevel < 0) hasteLevel = 0;
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 10, hasteLevel));
        player.sendMessage(ChatColor.GREEN + "Light Weight activated! You feel faster.");
    });

    return baseProc;
}

    public static boolean handleSpeedBreaker(BlockBreakEvent event, HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Speed Breaker")) return false;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Speed Breaker");
    if (enchantIndex < 0) return false;
    int level = tool.getLevel("Speed Breaker");
    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;

    Player player = event.getPlayer();

    if (event.isCancelled()) return false;
    if (player.hasPotionEffect(PotionEffectType.HASTE)) return false;

    boolean baseProc = random.nextDouble() < procChance;
    if (baseProc) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 10, 0));
    }

    tryEcho("Speed Breaker", event, tool, multiplier -> {
        int hasteLevel = (int) (multiplier - 1);
        if (hasteLevel < 0) hasteLevel = 0;
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 10, hasteLevel));
    });

    return baseProc;
}

    public static void handleSprinter(BlockBreakEvent event, HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Sprinter")) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Sprinter");
    if (enchantIndex < 0) return;
    int level = tool.getLevel("Sprinter");
    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;

    Player player = event.getPlayer();

    boolean baseProc = random.nextDouble() < procChance;
    if (baseProc) {
        // Base Speed I (amplifier 0)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0, false, false, false));
    }

    // Independent echo proc
    tryEcho("Sprinter", event, tool, multiplier -> {
        int amp = (int) (multiplier - 1);
        if (amp < 0) amp = 0;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, amp, false, false, false));
    });
}

    public static boolean handleBounder(BlockBreakEvent event, HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Bounder")) return false;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Bounder");
    if (enchantIndex < 0) return false;
    int level = tool.getLevel("Bounder");
    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;

    Player player = event.getPlayer();

    boolean baseProc = random.nextDouble() < procChance;
    if (baseProc) {
        // Base Jump Boost I
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 0, true, false, false));
    }

    tryEcho("Bounder", event, tool, multiplier -> {
        int amp = (int) (multiplier - 1);
        if (amp < 0) amp = 0;
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, amp, true, false, false));
    });

    return baseProc;
}

    public static void handleKeyMiner(BlockBreakEvent event, HeldToolInfo tool, JavaPlugin plugin) {
    Player player = event.getPlayer();
    int level = tool.getLevel("Key Miner");
    if (level <= 0) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Key Miner");
    if (enchantIndex < 0 || enchantIndex >= EnchantmentData.ENCHANT_PROC_CHANCE.length) return;

    double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex];

    double chance = Math.min(baseChance * level, 1.0);

    boolean baseProc = random.nextDouble() < chance;

    if (baseProc) {
        giveKeyDrop(player, level);
    }

    // Independent echo proc
    tryEcho("Key Miner", event, tool, multiplier -> {
        int echoLevel = (int) Math.max(1, level * multiplier);
        giveKeyDrop(player, echoLevel);
    });
}

    public static void handleGoldDigger(BlockBreakEvent event, HeldToolInfo tool) {
    Player player = event.getPlayer();
    int level = tool.getLevel("Gold Digger");
    if (level <= 0) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Gold Digger");
    if (enchantIndex < 0 || enchantIndex >= EnchantmentData.ENCHANT_PROC_CHANCE.length) return;

    double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex];
    double chance = Math.min(baseChance * level, 1.0);

    String playerRank = RankUtils.getRank(player);
    int prestige = RankUtils.getPrestigeFromRank(playerRank);
    double base = 500 + (1000 * prestige);
    double variance = Math.random() * 1000;
    double money = base + variance;

    boolean baseProc = random.nextDouble() < chance;

    if (baseProc) {
        VaultUtil.giveMoney(player, money);
        player.sendMessage(ChatColor.GOLD + "You found $" + String.format("%.2f", money) + " from Gold Digger!");
    }

    // Echo proc runs independently, even if baseProc failed
    tryEcho("Gold Digger", event, tool, multiplier -> {
        double amplified = money * multiplier;
        VaultUtil.giveMoney(player, amplified);
        player.sendMessage(ChatColor.GOLD + "Your Echo boosted Gold Digger! You found $" + String.format("%.2f", amplified));
    });
}

    public static void handleVeinMiner(BlockBreakEvent event, HeldToolInfo tool) {
    Player player = event.getPlayer();
    int level = tool.getLevel("Vein Miner");
    if (level <= 0) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Vein Miner");
    if (enchantIndex < 0) return;
    double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex];
    if (random.nextDouble() > baseChance) return;

    Block origin = event.getBlock();
    Material type = origin.getType();
    if (!isOre(type)) return;

    event.setDropItems(false);
    ItemStack toolItem = player.getInventory().getItemInMainHand();

    Set<Block> blocks = new HashSet<>();
    int baseRadius = 3;

    // Base radius vein mining
    findConnectedOres(origin, type, blocks, origin.getLocation(), baseRadius);

    // Echo effect: larger radius search added independently
    tryEcho("Vein Miner", event, tool, multiplier -> {
        int echoRadius = (int) Math.round(baseRadius * multiplier);
        findConnectedOres(origin, type, blocks, origin.getLocation(), echoRadius);
    });

    // Break all collected blocks
    blocks.forEach(b -> b.breakNaturally(toolItem));
}

    public static boolean handleBlast(BlockBreakEvent event, HeldToolInfo tool) {
    Player player = event.getPlayer();
    int level = tool.getLevel("Blast");
    if (level <= 0) return false;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Blast");
    if (enchantIndex < 0) return false;

    double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    if (random.nextDouble() > baseChance) return false;
    if (!RankUtils.canUseEnchants(tool, player)) return false;

    ItemStack toolItem = player.getInventory().getItemInMainHand();
    Block baseBlock = event.getBlock();
    BlockFace facing = getFacingDirection(player);
    Block center = baseBlock.getRelative(facing);

    int centerY = center.getY();
    int playerY = player.getLocation().getBlockY();
    if (centerY < playerY) center = center.getRelative(0, 1, 0);

    int minY = centerY < playerY ? -1 : (centerY == playerY ? 0 : -1);
    int maxY = centerY < playerY ? 1 : (centerY == playerY ? 2 : 1);

    // Base blast radius
    int radius = 1;

    // Echo can increase radius independently
    final int[] echoRadius = {0};
    tryEcho("Blast", event, tool, multiplier -> {
        if (multiplier == 2.0) echoRadius[0] = 2;
        else if (multiplier == 1.5) echoRadius[0] = 1;
    });

    int totalRadius = radius + echoRadius[0];
    int autoSmeltLevel = tool.getLevel("Auto Smelt");
    int broken = 0;

    for (int x = -totalRadius; x <= totalRadius; x++) {
        for (int y = minY; y <= maxY; y++) {
            for (int z = -totalRadius; z <= totalRadius; z++) {
                Block target = center.getRelative(x, y, z);
                if (target.getType().isAir() || target.getType() == Material.BEDROCK) continue;

                if (autoSmeltLevel > 0 && AutoSmelt.canSmelt(target.getType())) {
                    AutoSmelt.tryAutoSmelt(target, toolItem, autoSmeltLevel);
                } else {
                    target.breakNaturally(toolItem);
                }
                broken++;
            }
        }
    }

    return broken > 0;
}

    public static boolean handleWallBreaker(BlockBreakEvent event, HeldToolInfo tool) {
    Player player = event.getPlayer();
    int level = tool.getLevel("Wall Breaker");
    if (level <= 0) return false;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Wall Breaker");
    if (enchantIndex < 0) return false;

    double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    if (random.nextDouble() > baseChance) return false;
    if (!RankUtils.canUseEnchants(tool, player)) return false;

    ItemStack toolItem = player.getInventory().getItemInMainHand();
    Block baseBlock = event.getBlock();
    BlockFace facing = getFacingDirection(player);

    int centerY = baseBlock.getY();
    int playerY = player.getLocation().getBlockY();
    int minY = centerY < playerY ? -1 : (centerY == playerY ? 0 : -1);
    int maxY = centerY < playerY ? 1 : (centerY == playerY ? 2 : 1);

    final int[] echoRadius = {0};
    tryEcho("Wall Breaker", event, tool, multiplier -> {
        if (multiplier == 2.0) echoRadius[0] = 2;
        else if (multiplier == 1.5) echoRadius[0] = 1;
    });

    int autoSmeltLevel = tool.getLevel("Auto Smelt");
    int broken = 0;

    for (int dy = minY; dy <= maxY; dy++) {
        int y = centerY + dy;
        if (y < playerY - 1) continue;

        for (int dx = -1 - echoRadius[0]; dx <= 1 + echoRadius[0]; dx++) {
            Block target = switch (facing) {
                case NORTH, SOUTH -> baseBlock.getRelative(dx, dy, 0);
                case EAST, WEST -> baseBlock.getRelative(0, dy, dx);
                default -> baseBlock.getRelative(dx, dy, 0);
            };

            if (target.getType().isAir() || target.getType() == Material.BEDROCK) continue;

            if (autoSmeltLevel > 0 && AutoSmelt.canSmelt(target.getType())) {
                AutoSmelt.tryAutoSmelt(target, toolItem, autoSmeltLevel);
            } else {
                target.breakNaturally(toolItem);
            }
            broken++;
        }
    }

    return broken > 0;
}

    public static boolean handleOreScavenger(BlockBreakEvent event, HeldToolInfo tool) {
    Player player = event.getPlayer();
    Block broken = event.getBlock();

    if (!isOre(broken.getType())) return false;

    int level = tool.getLevel("Ore Scavenger");
    if (level <= 0) return false;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Ore Scavenger");
    if (enchantIndex < 0) return false;

    double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    if (random.nextDouble() > baseChance) return false;

    int baseMax = 3 + level;
    final int[] echoBonus = {0};

    tryEcho("Ore Scavenger", event, tool, multiplier -> {
        if (multiplier == 2.0) echoBonus[0] = baseMax;
        else if (multiplier == 1.5) echoBonus[0] = baseMax / 2;
    });

    int totalMax = baseMax + echoBonus[0];

    replaceNearbyBlocksRandomized(
        player.getLocation(),
        5,
        block -> !block.getType().isAir() && block.getType() != Material.BEDROCK,
        broken.getType(),
        totalMax
    );

    return true;
}

    public static boolean handleFrostTouch(BlockBreakEvent event, HeldToolInfo tool) {
    Player player = event.getPlayer();
    int level = tool.getLevel("Frost Touch");
    if (level <= 0) return false;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Frost Touch");
    if (enchantIndex < 0) return false;

    double finalChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;


    if (random.nextDouble() > finalChance) return false;

    int baseBlocks = 20 + 10 * level;
    final int[] echoBonus = {0};

    tryEcho("Frost Touch", event, tool, multiplier -> {
        if (multiplier == 2.0) echoBonus[0] = baseBlocks;
        else if (multiplier == 1.5) echoBonus[0] = baseBlocks / 2;
    });

    spawnPackedIceClump(player.getLocation(), baseBlocks + echoBonus[0]);
    return true;
}

    public static boolean handleTreasureHunter(BlockBreakEvent event, HeldToolInfo tool) {
    Player player = event.getPlayer();
    int level = tool.getLevel("Treasure Hunter");
    if (level <= 0) return false;

    int index = EnchantmentData.getEnchantmentIndex("Treasure Hunter");
    if (index < 0) return false;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[index] * level;
    if (random.nextDouble() > procChance) return false;

    boolean echoed = tryEcho("Treasure Hunter", event, tool, multiplier -> {
        int bonusAmount = (int) Math.round(multiplier);
        giveRandomLoot(player, bonusAmount);
        player.sendMessage(ChatColor.GOLD + "Treasure Hunter activated! You dug up some hidden loot!");
    });

    if (!echoed) {
        giveRandomLoot(player, 1);
        player.sendMessage(ChatColor.GOLD + "Treasure Hunter activated! You dug up some hidden loot!");
    }
    return true;
}

    public static double getEssenceLinkMultiplier(HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Essence Link")) return 1.0;

    int level = tool.getLevel("Essence Link");
    if (level <= 0) return 1.0;

    int index = EnchantmentData.getEnchantmentIndex("Essence Link");
    if (index < 0) return 1.0;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[index] * level;
    if (random.nextDouble() > procChance) return 1.0;

    final double[] multiplier = {1.0};

    tryEcho("Essence Link", null, tool, echoMultiplier -> {
        multiplier[0] = echoMultiplier;
    });

    // Base multiplier increases with level, amplified by echo multiplier
    return multiplier[0] * (1.0 + 0.1 * level);  // Example: 10% essence boost per level
}

    public static boolean handleJackpot(BlockBreakEvent event, HeldToolInfo tool) {
    Player player = event.getPlayer();
    int level = tool.getLevel("Jackpot");
    if (level <= 0) return false;

    int index = EnchantmentData.getEnchantmentIndex("Jackpot");
    if (index < 0) return false;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[index] * level;
    if (random.nextDouble() > procChance) return false;

    ItemStack toolItem = player.getInventory().getItemInMainHand();
    Block block = event.getBlock();

    boolean echoed = tryEcho("Jackpot", event, tool, multiplier -> {
        int multiplierAmount = 2 + random.nextInt(3); // 2 to 4
        multiplierAmount = (int) Math.round(multiplierAmount * multiplier);
        block.getWorld().dropItemNaturally(block.getLocation(), multiplyDrops(block, toolItem, multiplierAmount));
    });

    if (!echoed) {
        int multiplierAmount = 2 + random.nextInt(3);
        block.getWorld().dropItemNaturally(block.getLocation(), multiplyDrops(block, toolItem, multiplierAmount));
    }

    return true;
}

    public static boolean handleEfficientGrip(BlockBreakEvent event, HeldToolInfo tool) {
    Player player = event.getPlayer();
    int level = tool.getLevel("Efficient Grip");
    if (level <= 0) return false;

    int index = EnchantmentData.getEnchantmentIndex("Efficient Grip");
    if (index < 0) return false;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[index] * level;
    if (random.nextDouble() > procChance) return false;

    boolean echoed = tryEcho("Efficient Grip", event, tool, multiplier -> {
        int amplifier = (int) (multiplier - 1);
        // Reduce food exhaustion or increase saturation
        player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1 + amplifier));
    });

    if (!echoed) {
        player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
    }
    return true;
}

    public static boolean handleClumsy(BlockBreakEvent event, HeldToolInfo tool, JavaPlugin plugin) {
    if (tool == null || !tool.customEnchants.containsKey("Clumsy")) return false;

    Player player = event.getPlayer();
    Block origin = event.getBlock();
    ItemStack toolItem = player.getInventory().getItemInMainHand();

    int level = tool.getLevel("Clumsy");
    if (level <= 0) return false;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Clumsy");
    if (enchantIndex < 0) return false;

    double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    if (random.nextDouble() > baseChance) return false;

    boolean echoed = tryEcho("Clumsy", event, tool, multiplier -> {
        double echoChance = baseChance * multiplier;
        if (random.nextDouble() > echoChance) return;

        // Pick one random adjacent block to break
        Block randomAdjacent = getRandomAdjacentBlock(origin, player.getWorld());
        if (randomAdjacent != null && randomAdjacent.getType() != Material.AIR && randomAdjacent.getType() != Material.BEDROCK) {
            // Break naturally with the same tool
            Bukkit.getScheduler().runTask(plugin, () -> randomAdjacent.breakNaturally(toolItem));
        }
    });

    if (!echoed) {
        // Normal proc without echo amplification
        Block randomAdjacent = getRandomAdjacentBlock(origin, player.getWorld());
        if (randomAdjacent != null && randomAdjacent.getType() != Material.AIR && randomAdjacent.getType() != Material.BEDROCK) {
            Bukkit.getScheduler().runTask(plugin, () -> randomAdjacent.breakNaturally(toolItem));
        }
    }

    return true;
}

    public static boolean handleDustCollector(BlockBreakEvent event, HeldToolInfo tool) {
    Block broken = event.getBlock();

    if (tool == null || !tool.customEnchants.containsKey("Dust Collector")) return false;

    int level = tool.getLevel("Dust Collector");
    if (level <= 0) return false;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Dust Collector");
    if (enchantIndex < 0) return false;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    if (random.nextDouble() > procChance) return false;

    // Check if the broken block is a filler block
    if (!isFiller(broken.getType())) return false;

    // Cancel default drops and drop gravel or sand instead
    event.setDropItems(false);

    // Decide gravel or sand drop randomly
    Material dropMat = (random.nextBoolean()) ? Material.GRAVEL : Material.SAND;

    int dropAmount = 1; // can scale with level or echo if you want

    tryEcho("Dust Collector", event, tool, multiplier -> {
        int amplifiedAmount = (int) Math.ceil(dropAmount * multiplier);
        broken.getWorld().dropItemNaturally(broken.getLocation(), new ItemStack(dropMat, amplifiedAmount));
    });

    // If echo didn’t trigger, drop normal amount
    broken.getWorld().dropItemNaturally(broken.getLocation(), new ItemStack(dropMat, dropAmount));

    return true;
}

    public static boolean handleTunneler(BlockBreakEvent event, HeldToolInfo tool) {
    Player player = event.getPlayer();
    Block baseBlock = event.getBlock();

    if (tool == null || !tool.customEnchants.containsKey("Tunneler")) return false;

    int level = tool.getLevel("Tunneler");
    if (level <= 0) return false;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Tunneler");
    if (enchantIndex < 0) return false;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    if (random.nextDouble() > procChance) return false;

    ItemStack toolItem = player.getInventory().getItemInMainHand();

    BlockFace facing = getFacingDirection(player);

    // The two blocks forward from the base block in facing direction
    Block firstForward = baseBlock.getRelative(facing, 1);
    Block secondForward = baseBlock.getRelative(facing, 2);

    boolean minedAny = false;

    if (!firstForward.getType().isAir() && firstForward.getType() != Material.BEDROCK) {
        firstForward.breakNaturally(toolItem);
        minedAny = true;
    }

    if (!secondForward.getType().isAir() && secondForward.getType() != Material.BEDROCK) {
        secondForward.breakNaturally(toolItem);
        minedAny = true;
    }

    return minedAny;
}

    public static void handleGemPolish(BlockBreakEvent event, HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Gem Polish")) return;

    Player player = event.getPlayer();
    Block block = event.getBlock();
    Material type = block.getType();

    if (type != Material.EMERALD_ORE && type != Material.DEEPSLATE_EMERALD_ORE) return;

    int level = tool.getLevel("Gem Polish");
    if (level <= 0) return;

    // Base XP from emerald ore (adjust if needed)
    int baseXp = 3;

    int extraXp = (int) Math.ceil(baseXp * 0.10 * level);

    // Give the extra XP (just the bonus, player still gets base XP naturally)
    player.giveExp(extraXp);

}

    public static void handleVeinFlicker(BlockBreakEvent event, HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Vein Flicker")) return;

    Player player = event.getPlayer();
    int level = tool.getLevel("Vein Flicker");
    if (level <= 0) return;

    // Optional: You can add a proc chance like other enchants
    int enchantIndex = EnchantmentData.getEnchantmentIndex("Vein Flicker");
    if (enchantIndex < 0) return;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    if (random.nextDouble() > procChance) return;

    Block origin = event.getBlock();
    Material type = origin.getType();
    if (!isOre(type)) return;

    event.setDropItems(false);
    ItemStack toolItem = player.getInventory().getItemInMainHand();

    Set<Block> blocks = new HashSet<>();
    int baseRadius = 1; // smaller radius for flicker

    findConnectedOres(origin, type, blocks, origin.getLocation(), baseRadius);

    tryEcho("Vein Flicker", event, tool, multiplier -> {
        int radius = (int) Math.round(baseRadius * multiplier);
        findConnectedOres(origin, type, blocks, origin.getLocation(), radius);
    });

    blocks.forEach(b -> b.breakNaturally(toolItem));
}

    public static void handleAutoSell(BlockBreakEvent event, HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Auto Sell")) return;

    Player player = event.getPlayer();
    Block block = event.getBlock();
    UUID playerId = player.getUniqueId();

    // Active phase: auto-sell filler
    if (autoSellActive.contains(playerId)) {
        if (isFiller(block.getType())) {
            double value = SellManager.getPrice(block.getType()) * 0.25;
            VaultUtil.giveMoney(player, value);
            event.setDropItems(false);

            autoSellEarnings.put(playerId, autoSellEarnings.getOrDefault(playerId, 0.0) + value);
        }
        return;
    }

    int level = tool.getLevel("Auto Sell");
    if (level <= 0) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Auto Sell");
    if (enchantIndex < 0) return;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    if (Math.random() > procChance) return;

    // Activate auto sell window
    autoSellActive.add(playerId);
    autoSellEarnings.put(playerId, 0.0);

    player.sendMessage(ChatColor.GOLD + "Auto Sell activated for 5 seconds! Filler blocks will auto-sell at 25% value.");

    Bukkit.getScheduler().runTaskLater(TestEnchants.getInstance(), () -> {
        autoSellActive.remove(playerId);
        double earned = autoSellEarnings.getOrDefault(playerId, 0.0);
        autoSellEarnings.remove(playerId);

        player.sendMessage(ChatColor.GRAY + "Auto Sell expired. Total earned: $" + String.format("%.2f", earned));
    }, 20 * 5);
}

    public static void handleFortuneLink(BlockBreakEvent event, HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Fortune Link")) return;

    Player player = event.getPlayer();
    Block block = event.getBlock();
    Material type = block.getType();

    if (!isFiller(type)) return;

    int level = tool.getLevel("Fortune Link");
    if (level <= 0) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Fortune Link");
    if (enchantIndex < 0) return;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    if (Math.random() > procChance) return;

    event.setDropItems(false);

    int fortune = getFortuneLevel(player.getInventory().getItemInMainHand()); // use existing method to get vanilla fortune
    int multiplier = 1;

    if (fortune > 0) {
        int rand = random.nextInt(fortune + 2) - 1;
        if (rand < 0) rand = 0;
        multiplier += rand;
    }

    if (isAutoSellActive(player)) {
        double basePrice = SellManager.getPrice(type);
        double value = basePrice * 0.25 * multiplier;
        VaultUtil.giveMoney(player, value);
    } else {
        ItemStack drop = new ItemStack(type, multiplier);
        block.getWorld().dropItemNaturally(block.getLocation(), drop);
    }

}

    public static double getEssenceHoarderMultiplier(HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Essence Hoarder")) return 1.0;

    int level = tool.getLevel("Essence Hoarder");
    if (level <= 0) return 1.0;

    int index = EnchantmentData.getEnchantmentIndex("Essence Hoarder");
    if (index < 0) return 1.0;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[index] * level;
    if (random.nextDouble() > procChance) return 1.0;

    final double[] multiplier = {1.0};

    tryEcho("Essence Hoarder", null, tool, echoMultiplier -> {
        multiplier[0] = echoMultiplier;
    });

    // Stronger base multiplier than Essence Link
    return multiplier[0] * (1.0 + 0.1 * level);  // Example: 50% essence boost per level
}

    public static void handleWealthPulse(BlockBreakEvent event, HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Wealth Pulse")) return;

    Player player = event.getPlayer();
    Block block = event.getBlock();
    UUID playerId = player.getUniqueId();

    // If Wealth Pulse is already active, auto-sell filler blocks
    if (autoSellActive.contains(playerId)) {
        if (isFiller(block.getType())) {
            double value = SellManager.getPrice(block.getType()) * 0.50;
            VaultUtil.giveMoney(player, value);
            event.setDropItems(false);

            // Accumulate earnings
            wealthPulseEarnings.put(playerId, wealthPulseEarnings.getOrDefault(playerId, 0.0) + value);
        }
        return;
    }

    int level = tool.getLevel("Wealth Pulse");
    if (level <= 0) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Wealth Pulse");
    if (enchantIndex < 0) return;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    if (Math.random() > procChance) return;

    // Activate buff
    autoSellActive.add(playerId);
    wealthPulseEarnings.put(playerId, 0.0); // Reset tracking

    player.sendMessage(ChatColor.GOLD + "Wealth Pulse activated for 5 seconds! Filler blocks will auto-sell at 50% value.");

    Bukkit.getScheduler().runTaskLater(TestEnchants.getInstance(), () -> {
        autoSellActive.remove(playerId);
        double earned = wealthPulseEarnings.getOrDefault(playerId, 0.0);
        wealthPulseEarnings.remove(playerId);

        player.sendMessage(ChatColor.GRAY + "Wealth Pulse expired. Total earned: $" + String.format("%.2f", earned));
    }, 20 * 5);
}

    public static void handleResistance(BlockBreakEvent event, HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Resistance")) return;

    Player player = event.getPlayer();
    int level = tool.getLevel("Resistance");
    if (level <= 0) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Resistance");
    if (enchantIndex < 0) return;

    double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    boolean mainProc = random.nextDouble() <= baseChance;

    final boolean[] echoProc = {false};
    tryEcho("Resistance", event, tool, multiplier -> {
        echoProc[0] = true;
    });

    if (!mainProc && !echoProc[0]) return;

    // Give Resistance effect (duration and amplifier based on level, adjust as needed)
    int durationTicks = 20 * 5; // 5 seconds
    int amplifier = 0; // Resistance I

    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, amplifier));
}

    public static void handleOmniMiner(BlockBreakEvent event, HeldToolInfo tool) {
    if (tool == null || !tool.customEnchants.containsKey("Omni Miner")) return;

    Player player = event.getPlayer();
    int level = tool.getLevel("Omni Miner");
    if (level <= 0) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Omni Miner");
    if (enchantIndex < 0) return;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    if (random.nextDouble() > procChance) return;

    Block origin = event.getBlock();

    // Base radius, e.g. 3 blocks
    int baseRadius = 4;

    final int[] echoBonusRadius = {0};
    tryEcho("Omni Miner", event, tool, multiplier -> {
        if (multiplier == 2.0) echoBonusRadius[0] = baseRadius;         // double radius
        else if (multiplier == 1.5) echoBonusRadius[0] = baseRadius / 2; // +50% radius
    });

    int radius = baseRadius + echoBonusRadius[0];

    event.setDropItems(false);
    ItemStack toolItem = player.getInventory().getItemInMainHand();

    // Collect ores in radius of origin (not just same type)
    Set<Block> oreBlocks = new HashSet<>();

    // Scan within cube radius around origin
    Location originLoc = origin.getLocation();
    World world = origin.getWorld();

    for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
                Block block = world.getBlockAt(originLoc.clone().add(x, y, z));
                if (isOre(block.getType())) {
                    oreBlocks.add(block);
                }
            }
        }
    }

    // Break all ore blocks found
    for (Block b : oreBlocks) {
        b.breakNaturally(toolItem);
    }
}

    public static void handleAuraOfWealth(BlockBreakEvent event, HeldToolInfo tool, int essenceGained) {
    if (tool == null || !tool.customEnchants.containsKey("Aura of Wealth")) return;

    Player player = event.getPlayer();
    int level = tool.getLevel("Aura of Wealth");
    if (level <= 0) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex("Aura of Wealth");
    if (enchantIndex < 0) return;

    double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[enchantIndex] * level;
    if (random.nextDouble() > procChance) return;

    // Radius to affect others, e.g. 10 blocks
    int radius = 20;

    // Essence percentage shared, e.g. 10% per level
    double essenceSharePercent = 0.10 * level;

    // Get nearby players within radius (excluding the main player)
    for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
        if (!(entity instanceof Player nearbyPlayer)) continue;
        if (nearbyPlayer.equals(player)) continue;

        int essenceToGive = (int) Math.floor(essenceGained * essenceSharePercent);
        if (essenceToGive <= 0) continue;

        essenceManager.addEssence(nearbyPlayer, 1, essenceToGive); // Assuming tier 1 essence
        nearbyPlayer.sendMessage(ChatColor.GREEN + "You received " + essenceToGive + " essence from " + player.getName() + "'s Aura of Wealth!");
    }
}

    public static void handlePureGreed(Player player, HeldToolInfo tool, BlockBreakEvent event) {
    if (tool == null || !tool.customEnchants.containsKey("Pure Greed")) return;

    Material type = event.getBlock().getType();
    double price = SellManager.getPrice(type);
    if (price > 0) { // Only sell if price defined
        double value = price * 1.5;
        VaultUtil.giveMoney(player, value);
        event.setDropItems(false);
    }
}






    private static void applyConjureEcho(Block block, Material upgraded, JavaPlugin plugin, int level,
                                    BlockBreakEvent event, HeldToolInfo tool) {
    tryEcho("Conjure", event, tool, multiplier -> {
        conjureNearbyBlocks(block, upgraded, plugin, multiplier);
    });
}

    private static void conjureNearbyBlocks(Block center, Material upgraded, JavaPlugin plugin, double multiplier) {
    Location loc = center.getLocation();
    World world = loc.getWorld();
    int cx = loc.getBlockX();
    int cy = loc.getBlockY();
    int cz = loc.getBlockZ();

    int radius = 1;
    int maxBlocks = (int) Math.round(multiplier);
    int[] replaced = {0};

    outerLoop:
    for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == 0 && y == 0 && z == 0) continue;
                Block adj = world.getBlockAt(cx + x, cy + y, cz + z);
                if (adj.getType() != Material.BEDROCK) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (adj.getType() != upgraded) {
                            adj.setType(upgraded);
                        }
                    }, 1L);
                    replaced[0]++;
                    if (replaced[0] >= maxBlocks) break outerLoop;
                }
            }
        }
    }
}

    public static boolean tryEcho(
    String enchantName,
    BlockBreakEvent event,
    HeldToolInfo tool,
    Consumer<Double> amplifiedEffect
) {
    if (!tool.hasEcho()) return false;

    EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName(enchantName);
    if (info == null) return false;

    int level = tool.getLevel(enchantName);
    if (level <= 0) return false;

    double baseChance = info.procChance * level;
    if (Math.random() > baseChance) return false;

    double multiplier = getEchoMultiplier(tool);
    if (multiplier > 1.0) {
        amplifiedEffect.accept(multiplier);
        return true;
    }

    return false;
}

    public static double getEchoMultiplier(HeldToolInfo tool) {
    if (tool == null || !tool.hasEcho()) return 1.0;

    double baseChance = tool.customEnchants.containsKey("Final Echo") ? 0.3
                        : tool.customEnchants.containsKey("Legends Echo") ? 0.2
                        : 0.0;

    return Math.random() <= baseChance ? tool.getEchoMultiplier() : 1.0;
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

    public static BlockFace getFacingDirection(Player player) {
        float yaw = (player.getLocation().getYaw() + 360) % 360;
        if (yaw < 45 || yaw >= 315) return BlockFace.SOUTH;
        if (yaw < 135) return BlockFace.WEST;
        if (yaw < 225) return BlockFace.NORTH;
        return BlockFace.EAST;
    }

    private static boolean shouldProc(ItemStack tool, String enchantName, int level) {
        int index = EnchantmentData.getEnchantmentIndex(enchantName);
        if (index == -1) return false;

        

        double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[index];
        double chance = Math.min(baseChance * level, 1.0);

        return random.nextDouble() < chance;
    }

    public static void replaceNearbyBlocksRandomized(Location center, int radius, Predicate<Block> filter, Material replacement, int max) {
    World world = center.getWorld();
    int cx = center.getBlockX();
    int cy = center.getBlockY();
    int cz = center.getBlockZ();
    int playerY = center.getBlockY(); // Redundant with cy, but clear

    List<Block> candidates = new ArrayList<>();

    for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
                int bx = cx + x;
                int by = cy + y;
                int bz = cz + z;

                if (by < playerY - 1) continue;

                Block block = world.getBlockAt(bx, by, bz);
                if (filter.test(block) && block.getType() != replacement) {
                    candidates.add(block);
                }
            }
        }
    }

    Collections.shuffle(candidates);

    for (int i = 0; i < Math.min(max, candidates.size()); i++) {
        candidates.get(i).setType(replacement);
    }
}

    public static void spawnPackedIceClump(Location center, int maxBlocks) {
    World world = center.getWorld();
    int cx = center.getBlockX();
    int cy = center.getBlockY();
    int cz = center.getBlockZ();

    int radius = 5;
    List<BlockDistance> candidates = new ArrayList<>();

    for (int x = -radius; x <= radius; x++) {
        for (int y = -1; y <= 4; y++) {
            for (int z = -radius; z <= radius; z++) {
                int dx = cx + x;
                int dy = cy + y;
                int dz = cz + z;

                double distanceSq = x * x + y * y + z * z;
                if (distanceSq > radius * radius) continue;

                Block block = world.getBlockAt(dx, dy, dz);
                if (!block.getType().isAir() && block.getType() != Material.PACKED_ICE && block.getType() != Material.BEDROCK && block.getType() != Material.POLISHED_TUFF) {
                    candidates.add(new BlockDistance(block, distanceSq));
                }
            }
        }
    }

    // Sort by distance to ensure closer blocks are prioritized
        candidates.sort(Comparator.comparingDouble(b -> b.distanceSq));

        for (int i = 0; i < Math.min(maxBlocks, candidates.size()); i++) {
            candidates.get(i).block.setType(Material.PACKED_ICE);
        }
    }
    private static class BlockDistance {
        Block block;
        double distanceSq;

        BlockDistance(Block block, double distanceSq) {
            this.block = block;
            this.distanceSq = distanceSq;
        }
    }

    private static void giveKeyDrop(Player player, int level) {
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

    private static void giveRandomLoot(Player player, int amount) {
    // Example random loot pool: money, diamonds, ingots
    for (int i = 0; i < amount; i++) {
        double roll = random.nextDouble();
        if (roll < 0.4) {
            VaultUtil.giveMoney(player, 50 + random.nextInt(100)); // money
        } else if (roll < 0.7) {
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
        } else if (roll < 0.9) {
            player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 16));
        } else {
            player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 16));
        }
    }
}

    private static ItemStack multiplyDrops(Block block, ItemStack tool, int multiplier) {
    Collection<ItemStack> drops = block.getDrops(tool);
    // Simplify: Only return first drop multiplied for example
    if (drops.isEmpty()) return new ItemStack(Material.AIR);
    ItemStack base = drops.iterator().next();
    base.setAmount(base.getAmount() * multiplier);
    return base;
}

    private static Block getRandomAdjacentBlock(Block block, World world) {
    List<Block> adjacentBlocks = new ArrayList<>();

    // Check all blocks in 3x3x3 cube around block excluding center
    for (int dx = -1; dx <= 1; dx++) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dy == 0 && dz == 0) continue;
                Block adj = block.getRelative(dx, dy, dz);
                if (adj.getType() != Material.AIR && adj.getType() != Material.BEDROCK) {
                    adjacentBlocks.add(adj);
                }
            }
        }
    }

    if (adjacentBlocks.isEmpty()) return null;

    return adjacentBlocks.get(random.nextInt(adjacentBlocks.size()));
}

    private static final Set<Material> FILLER_BLOCKS = Set.of(
    Material.COBBLESTONE,
    Material.ANDESITE,
    Material.DIORITE,
    Material.MOSSY_COBBLESTONE,
    Material.BRICKS,
    Material.BLUE_TERRACOTTA,
    Material.RED_TERRACOTTA,
    Material.YELLOW_TERRACOTTA,
    Material.GREEN_TERRACOTTA,
    Material.GRAY_TERRACOTTA,
    Material.PURPUR_BLOCK,
    Material.LIGHT_GRAY_CONCRETE,
    Material.ORANGE_CONCRETE,
    Material.PINK_CONCRETE,
    Material.CYAN_CONCRETE,
    Material.LIME_CONCRETE,
    Material.END_STONE,
    Material.CALCITE,
    Material.WHITE_CONCRETE,
    Material.POLISHED_DIORITE,
    Material.RED_SANDSTONE,
    Material.BASALT,
    Material.BLACKSTONE,
    Material.QUARTZ_BLOCK,
    Material.LIGHT_BLUE_TERRACOTTA,
    Material.PRISMARINE,
    Material.PACKED_MUD,
    Material.TERRACOTTA,
    Material.BROWN_CONCRETE
);

    private static boolean isFiller(Material material) {
    return FILLER_BLOCKS.contains(material);
}

    public static boolean isAutoSellActive(Player player) {
    return autoSellActive.contains(player.getUniqueId());
}

    public static int getFortuneLevel(ItemStack item) {
    if (item == null || !item.hasItemMeta()) return 0;

    return item.getEnchantmentLevel(Enchantment.FORTUNE);
}

    public static boolean pureGreedActive(Player player) {
    ItemStack held = player.getInventory().getItemInMainHand();
    if (held == null || !held.hasItemMeta()) return false;

    HeldToolInfo info = HeldToolInfo.fromItem(held);
    if (info == null) return false;

    return info.customEnchants.containsKey("Pure Greed") && info.getLevel("Pure Greed") > 0;
}


}
