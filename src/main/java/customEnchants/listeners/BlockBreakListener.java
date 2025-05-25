package customEnchants.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import customEnchants.utils.BlockBreaker;
import customEnchants.utils.EnchantmentData;
import customEnchants.utils.HeldToolInfo;
import customEnchants.utils.AutoSmelt;
import customEnchants.utils.StructureUtil;

public class BlockBreakListener implements Listener {

    private static final Set<Material> ORES = EnumSet.of(
        Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.COPPER_ORE,
        Material.REDSTONE_ORE, Material.LAPIS_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE,
        Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_GOLD_ORE,
        Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.DEEPSLATE_LAPIS_ORE,
        Material.DEEPSLATE_DIAMOND_ORE, Material.DEEPSLATE_EMERALD_ORE
    );

    // Map enchant names to handlers that return whether they handled the block (true if blocks broken)
    private static final Map<String, BiFunction<BlockBreakEvent, Integer, Boolean>> ENCHANT_HANDLERS = Map.of(
        "Blast", (event, level) -> {
            Player player = event.getPlayer();
            ItemStack tool = player.getInventory().getItemInMainHand();
            int autoSmelt = HeldToolInfo.fromItem(tool).getLevel("Auto Smelt");
            int brokenCount = BlockBreaker.breakBlastArea(event.getBlock(), player, tool, level, autoSmelt);
            return brokenCount > 0;
        },
        "Wall Breaker", (event, level) -> {
            Player player = event.getPlayer();
            ItemStack tool = player.getInventory().getItemInMainHand();
            int autoSmelt = HeldToolInfo.fromItem(tool).getLevel("Auto Smelt");
            int brokenCount = BlockBreaker.breakWallBreakerArea(event.getBlock(), player, tool, level, autoSmelt);
            return brokenCount > 0;
        },
        "Ore Scavenger", (event, level) -> {
            Block broken = event.getBlock();
            Player player = event.getPlayer();

            // Only proceed if broken block is an ore
            if (!ORES.contains(broken.getType())) return false;

            double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[4]; // Ore Scavenger proc chance base
            double finalChance = procChance * level;

            if (Math.random() > finalChance) {
                return false; // Did not activate
            }

            // Activate: replace nearby blocks
            int maxReplacements = 3 + level;

            StructureUtil.replaceNearbyBlocksRandomized(
            player.getLocation(),
            5,
            (block) -> !block.getType().isAir() && block.getType() != Material.BEDROCK,
            broken.getType(),
            maxReplacements
            );

            return true; // Successfully activated and replaced blocks
        },
        "Frost Touch", (event, level) -> {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        String enchantName = "Frost Touch";

        double procChance = EnchantmentData.ENCHANT_PROC_CHANCE[9];
        int amplifyLevel = HeldToolInfo.getAmplifyLevel(tool, enchantName);
        double amplifyBonus = amplifyLevel * 0.01;
        double finalChance = procChance * level + amplifyBonus;

        if (Math.random() <= finalChance) {
            StructureUtil.spawnPackedIceClump(player.getLocation(), level);
            return true;
        } else {
            return false;
    }
}
    );


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null) {
            return;
        }

        HeldToolInfo enchantInfo = HeldToolInfo.fromItem(tool);
        if (enchantInfo == null || enchantInfo.customEnchants.isEmpty()) {
            return;
        }

        boolean handledByAnyEnchant = false;

        for (Map.Entry<String, Integer> entry : enchantInfo.customEnchants.entrySet()) {
            String enchantName = entry.getKey();
            int level = entry.getValue();

            BiFunction<BlockBreakEvent, Integer, Boolean> handler = ENCHANT_HANDLERS.get(enchantName);
            if (handler != null) {
                boolean didHandle = handler.apply(event, level);
                if (didHandle) {
                    handledByAnyEnchant = true;
                }
            }
        }

        int autoSmeltLevel = enchantInfo.getLevel("Auto Smelt");
        Block block = event.getBlock();
        Material blockType = block.getType();

        if (!handledByAnyEnchant) {
            if (autoSmeltLevel > 0 && AutoSmelt.canSmelt(blockType)) {
                event.setDropItems(false);
                AutoSmelt.tryAutoSmelt(block, tool, autoSmeltLevel);
            }
        }
    }
}
