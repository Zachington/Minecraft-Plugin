package customEnchants.listeners;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import customEnchants.utils.EnchantmentData;
import customEnchants.utils.RankUtils;
import customEnchants.utils.StatTracker;
import customEnchants.utils.HeldToolInfo;
import customEnchants.utils.EnchantFunctionUtil;

public class GeneralBlockBreakListener implements Listener {
    private final StatTracker stats;
    private final JavaPlugin plugin;
    

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
            if (!RankUtils.canUseEnchants(tool, player)) {
            event.setCancelled(true);
            return;
        }

        RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(block.getWorld()));
        if (regions != null) {
        ApplicableRegionSet regionSet = regions.getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()));

            for (ProtectedRegion region : regionSet) {
                String regionId = region.getId();

                if (regionId.startsWith("mine_")) {
                    String requiredRank = regionId.substring(5);
                    String playerRank = RankUtils.getRank(player);

                    if (!RegionBlockBreakListener.hasRequiredRank(playerRank, requiredRank)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (event.isCancelled()) {
            return;
        }


    if (!RankUtils.canUseEnchants(tool, player)) {
        event.setCancelled(true);
        return;
    }

        
        boolean handled = EnchantFunctionUtil.handleDelayedDynamite(event, tool, plugin);
        if (handled) {      
            return; 
        }

        

        EnchantFunctionUtil.handleRegenerate(event, tool, plugin);
        EnchantFunctionUtil.handleConjure(event, tool, plugin);
        EnchantFunctionUtil.handleXpSyphon(event, tool, plugin);
        EnchantFunctionUtil.handleLightWeight(event, tool);
        EnchantFunctionUtil.handleSpeedBreaker(event, tool);
        EnchantFunctionUtil.handleSprinter(event, tool);
        EnchantFunctionUtil.handleBounder(event, tool);
        EnchantFunctionUtil.handleKeyMiner(event, tool, plugin);
        EnchantFunctionUtil.handleGoldDigger(event, tool);
        EnchantFunctionUtil.handleVeinMiner(event, tool);
        EnchantFunctionUtil.handleBlast(event, tool);
        EnchantFunctionUtil.handleWallBreaker(event, tool);
        EnchantFunctionUtil.handleFrostTouch(event, tool);
        EnchantFunctionUtil.handleOreScavenger(event, tool);
        EnchantFunctionUtil.handleTreasureHunter(event, tool);
        EnchantFunctionUtil.handleResistance(event, tool);
        EnchantFunctionUtil.handleJackpot(event, tool);
        EnchantFunctionUtil.handleClumsy(event, tool, plugin);
        EnchantFunctionUtil.handleDustCollector(event, tool);
        EnchantFunctionUtil.handleEfficientGrip(event, tool);
        EnchantFunctionUtil.handleTunneler(event, tool);
        EnchantFunctionUtil.handleGemPolish(event, tool);
        EnchantFunctionUtil.handleVeinFlicker(event, tool);
        EnchantFunctionUtil.handleAutoSell(event, tool);
        EnchantFunctionUtil.handleFortuneLink(event, tool);
        EnchantFunctionUtil.handleWealthPulse(event, tool);
        EnchantFunctionUtil.handleResistance(event, tool);
        EnchantFunctionUtil.handleOmniMiner(event, tool);
        EnchantFunctionUtil.handlePureGreed(player, tool, event);


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

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
    Player player = event.getPlayer();
    ItemStack item = player.getInventory().getItem(event.getNewSlot());

    HeldToolInfo tool = HeldToolInfo.fromItem(item);
    
    // If no tool or doesn't have Speed Breaker, handle potion removal safely
    if (tool == null || !tool.customEnchants.containsKey("Speed Breaker")) {
        PotionEffect active = player.getPotionEffect(PotionEffectType.HASTE);
        if (active != null && active.getDuration() >= 999999 - 100 && !active.isAmbient()) {
            player.removePotionEffect(PotionEffectType.HASTE);
        }
        return;
    }

    // --- Prestige check here ---
    String rarity = EnchantmentData.getRarity("Speed Breaker");
    String playerRank = RankUtils.getRank(player);

    if ("PRESTIGE".equalsIgnoreCase(rarity)) {
        if (RankUtils.compareRanks(playerRank, "p1a") < 0) {
            player.sendMessage(ChatColor.RED + "You must be Prestige 1 to use Speed Breaker!");
            PotionEffect active = player.getPotionEffect(PotionEffectType.HASTE);
            if (active != null && active.getDuration() >= 999999 - 100 && !active.isAmbient()) {
                player.removePotionEffect(PotionEffectType.HASTE);
            }
            return;
        }
    } 

    // Apply potion effect
    int level = tool.getLevel("Speed Breaker");
    if (level >= 3) {
        int amplifier = level - 2; // Level 3 = Haste 1
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.HASTE, 
            999999, 
            amplifier, 
            false, false, false
        ));
    } else {
        // Only remove our long-duration effect if present
        PotionEffect active = player.getPotionEffect(PotionEffectType.HASTE);
        if (active != null && active.getDuration() >= 999999 - 100 && !active.isAmbient()) {
            player.removePotionEffect(PotionEffectType.HASTE);
        }
    }
}

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (world != null && "cell_world".equals(world.getName())) {
            event.setCancelled(true);
        }
    }


}
