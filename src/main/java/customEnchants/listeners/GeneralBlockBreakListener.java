package customEnchants.listeners;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import customEnchants.utils.EnchantmentData;
import customEnchants.utils.RankUtils;
import customEnchants.utils.StatTracker;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import customEnchants.utils.HeldToolInfo;

public class GeneralBlockBreakListener implements Listener {
    private final StatTracker stats;
    private final JavaPlugin plugin;
    private final Random random = new Random();

    public GeneralBlockBreakListener(StatTracker stats, JavaPlugin plugin) {
        this.stats = stats;
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String worldName = block.getWorld().getName();

        if (!worldName.equals("world") && !worldName.equals("mine_world")) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        HeldToolInfo tool = HeldToolInfo.fromItem(item);
        for (String enchant : tool.customEnchants.keySet()) {
            String rarity = EnchantmentData.getRarity(enchant);
            if ("PRESTIGE".equalsIgnoreCase(rarity) && !RankUtils.isAtLeastP1a(player)) {
                player.sendMessage(ChatColor.RED + "You must be Prestige I (p1a) to use tools with " + enchant + "!");
                event.setCancelled(true);
                return;
            }
            if ("PRESTIGE+".equalsIgnoreCase(rarity) && !RankUtils.isAtLeastP10a(player)) {
                player.sendMessage(ChatColor.RED + "You must be Prestige X (p10a) to use tools with " + enchant + "!");
                event.setCancelled(true);
                return;
            }
        }

        // ---- Delayed Dynamite Logic ----
        if (handleDelayedDynamite(event, tool)) return;

        // Normal block break stat tracking
        UUID uuid = player.getUniqueId();
        stats.incrementPlayerStat(uuid, "blocks_broken");
        stats.incrementServerStat("blocks_broken");
        stats.save();
    }

    @EventHandler
    public void onExplosionDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
            event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            if (event.getEntity() instanceof Player) {
                event.setCancelled(true);
            }
        }
    }

    private boolean handleDelayedDynamite(BlockBreakEvent event, HeldToolInfo tool) {
    Block block = event.getBlock();
    PersistentDataContainer blockData = block.getChunk().getPersistentDataContainer();

    String keyStr = getBlockKey(block);
    NamespacedKey hitsKey = new NamespacedKey(plugin, keyStr + "_hits");
    NamespacedKey levelKey = new NamespacedKey(plugin, keyStr + "_level");

    // Handle if it's already a coal block
    if (block.getType() == Material.COAL_BLOCK) {
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
        event.setCancelled(true); // prevent dropping coal
        return true;
    }

    // Handle normal block breaking
    if (tool.customEnchants.containsKey("Delayed Dynamite")) {
        EnchantmentData.EnchantmentInfo info = EnchantmentData.getEnchantmentInfoByName("Delayed Dynamite");
        int level = tool.getLevel("Delayed Dynamite");
        double procChance = info.procChance * level;

        if (event.isCancelled()) {
            return false;  // Don't do anything if another plugin (like WorldGuard) blocked this break
        }

        if (random.nextDouble() < procChance) {
            // Delay placing coal block by 1 tick to allow block break to fully complete
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



    private String getBlockKey(Block block) {
        return "coalblock_" + block.getX() + "_" + block.getY() + "_" + block.getZ();
    }
}
