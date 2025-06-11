package customEnchants.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import org.bukkit.entity.Zombie;

import customEnchants.utils.GoblinUtil;
import customEnchants.utils.StatTracker;

public class RngBlockBreak implements Listener {

    private final StatTracker stats;  
    private final JavaPlugin plugin;

    // Constructor that accepts your StatTracker and plugin instance
    public RngBlockBreak(JavaPlugin plugin, StatTracker stats) {
        this.plugin = plugin;
        this.stats = stats;
    }

    private static final Map<UUID, Integer> lastGoblinSpawnMap = new HashMap<>();
    private final Random random = new Random();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String worldName = event.getBlock().getWorld().getName();
        if (!worldName.equals("world") && !worldName.equals("mine_world")) return;

        UUID uuid = player.getUniqueId();

        int blocksBroken = stats.getPlayerStat(uuid, "blocks_broken", false); // total count
        int lastSpawn = getLastGoblinSpawnBlocks(uuid);
        int blocksSinceLastSpawn = blocksBroken - lastSpawn;

        for (GoblinUtil.LootGoblinType goblin : GoblinUtil.goblins.values()) {
            if (!GoblinUtil.canSpawn(player, goblin)) continue;

            boolean guaranteedSpawn = blocksSinceLastSpawn >= goblin.guaranteedInterval;
            boolean rngSpawn = (random.nextInt(goblin.spawnChance) == 0);

            if (guaranteedSpawn || rngSpawn) {
                spawnGoblin(event.getBlock().getLocation(), player, goblin);
                setLastGoblinSpawnBlocks(uuid, blocksBroken);  // Reset counter
                break;  // Spawn only one per break
            }
        }
    }

    @SuppressWarnings("unchecked")
    @EventHandler
public void onGoblinDeath(EntityDeathEvent event) {
    if (!(event.getEntity() instanceof Zombie zombie) || !zombie.isBaby()) return;

    // Custom drops
    if (zombie.hasMetadata("customDrops")) {
        List<ItemStack> drops = (List<ItemStack>) zombie.getMetadata("customDrops").get(0).value();
        event.getDrops().clear(); // Remove default drops

        // Deliver to summoning player
        if (zombie.hasMetadata("goblinOwner")) {
            String uuidStr = zombie.getMetadata("goblinOwner").get(0).asString();
            Player owner = Bukkit.getPlayer(UUID.fromString(uuidStr));
            if (owner != null && owner.isOnline()) {
                for (ItemStack item : drops) {
                    HashMap<Integer, ItemStack> leftovers = owner.getInventory().addItem(item);
                    for (ItemStack leftover : leftovers.values()) {
                        owner.getWorld().dropItemNaturally(owner.getLocation(), leftover);
                    }
                }
            } else {
                // Fallback: drop at zombie location if player is offline
                event.getDrops().addAll(drops);
            }
        } else {
            // Fallback: drop at zombie location if metadata missing
            event.getDrops().addAll(drops);
        }
    }
}


    @EventHandler
public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Zombie zombie) || !zombie.isBaby()) return;
    if (!zombie.hasMetadata("goblinHits")) return;

    event.setDamage(0); // Cancel actual damage

    int hits = zombie.getMetadata("goblinHits").get(0).asInt();
    hits++;

    if (hits >= 5) {
        zombie.removeMetadata("goblinHits", plugin); // Clean up
        zombie.setHealth(0); // Kill after 3rd hit
    } else {
        zombie.setMetadata("goblinHits", new FixedMetadataValue(plugin, hits));
    }
}



    private void spawnGoblin(Location loc, Player player, GoblinUtil.LootGoblinType goblin) {
    World world = loc.getWorld();
    Zombie goblinEntity = (Zombie) world.spawnEntity(loc.add(0, 1, 0), EntityType.ZOMBIE);
    goblinEntity.setBaby(true);
    goblinEntity.setCustomName(goblin.name);
    goblinEntity.setCustomNameVisible(true);
    goblinEntity.setMaxHealth(100);
    goblinEntity.setHealth(100);
    goblinEntity.setRemoveWhenFarAway(false);
    goblinEntity.getEquipment().clear();

    // Potion effects
    if (goblin.potionEffects != null) {
        for (PotionEffect effect : goblin.potionEffects) {
            goblinEntity.addPotionEffect(effect);
        }
    }

    goblinEntity.setMetadata("customDrops", new FixedMetadataValue(plugin, goblin.drops));
    goblinEntity.setMetadata("goblinHits", new FixedMetadataValue(plugin, 0)); 
    goblinEntity.setMetadata("goblinOwner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));


}



    private int getLastGoblinSpawnBlocks(UUID uuid) {
        return lastGoblinSpawnMap.getOrDefault(uuid, 0);
    }

    private void setLastGoblinSpawnBlocks(UUID uuid, int blocks) {
        lastGoblinSpawnMap.put(uuid, blocks);
    }


}
