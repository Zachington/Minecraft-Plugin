package customEnchants.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.entity.PlayerDeathEvent;

import org.bukkit.entity.Zombie;

import customEnchants.utils.GoblinUtil;
import customEnchants.utils.StatTracker;
import customEnchants.utils.customItemUtil;

public class RngBlockBreak implements Listener {

    private final StatTracker stats;  
    private final JavaPlugin plugin;

    // Constructor that accepts your StatTracker and plugin instance
    public RngBlockBreak(JavaPlugin plugin, StatTracker stats) {
        this.plugin = plugin;
        this.stats = stats;
    }

    private static final Map<UUID, Integer> lastGoblinSpawnMap = new HashMap<>();
    private final Map<UUID, List<Zombie>> playerGoblins = new HashMap<>();
    private final Random random = new Random();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String worldName = event.getBlock().getWorld().getName();
        if (!worldName.equals("world") && !worldName.equals("mine_world")) return;

        UUID uuid = player.getUniqueId();

        if (random.nextInt(50000) == 0) {
        ItemStack extractorCore = customItemUtil.createCustomItem("Extractor Core");
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(extractorCore);
        if (!leftover.isEmpty()) {
            // Drop leftover on the ground near player if inventory full
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }
        player.sendMessage(ChatColor.GREEN + "You found an Extractor Core!");
    }

        int blocksBroken = stats.getPlayerStat(uuid, "blocks_broken", false); // total count
        int lastSpawn = getLastGoblinSpawnBlocks(uuid);
        int blocksSinceLastSpawn = blocksBroken - lastSpawn;

        for (GoblinUtil.LootGoblinType goblin : GoblinUtil.goblins.values()) {
            if (!GoblinUtil.canSpawn(player, goblin)) continue;

            boolean guaranteedSpawn = blocksSinceLastSpawn >= goblin.guaranteedInterval;
            boolean rngSpawn = (random.nextInt(goblin.spawnChance) == 0);

            if (guaranteedSpawn || rngSpawn) {
                spawnGoblin(event.getBlock().getLocation(), player, goblin);
                player.sendTitle(ChatColor.GOLD + goblin.name + " has Spawned!","",10, 70, 20);
                setLastGoblinSpawnBlocks(uuid, blocksBroken);  // Reset counter
                String typeKey = goblin.name.replaceAll("§.", "").toLowerCase().replace(" ", "_"); // e.g., "common_goblin"
                String statKey = "goblins_spawned." + typeKey;
                int prev = stats.getPlayerStat(uuid, statKey, false);
                stats.setPlayerStat(uuid, statKey, prev + 1, false);
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
    public void onPlayerDeath(PlayerDeathEvent event) {
    UUID uuid = event.getEntity().getUniqueId();
    List<Zombie> goblins = playerGoblins.remove(uuid);

    if (goblins != null) {
        for (Zombie goblin : goblins) {
            if (!goblin.isDead()) {
                goblin.remove();
            }
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

    if (goblin.equipment != null) {
    if (goblin.equipment.length > 0) goblinEntity.getEquipment().setItemInMainHand(goblin.equipment[0]);
    if (goblin.equipment.length > 1) goblinEntity.getEquipment().setHelmet(goblin.equipment[1]);
    if (goblin.equipment.length > 2) goblinEntity.getEquipment().setChestplate(goblin.equipment[2]);
    if (goblin.equipment.length > 3) goblinEntity.getEquipment().setLeggings(goblin.equipment[3]);
    if (goblin.equipment.length > 4) goblinEntity.getEquipment().setBoots(goblin.equipment[4]);
}

    goblinEntity.setMetadata("customDrops", new FixedMetadataValue(plugin, goblin.drops));
    goblinEntity.setMetadata("goblinHits", new FixedMetadataValue(plugin, 0)); 
    goblinEntity.setMetadata("goblinOwner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
    playerGoblins.computeIfAbsent(player.getUniqueId(), k -> new java.util.ArrayList<>()).add(goblinEntity);
}

    private int getLastGoblinSpawnBlocks(UUID uuid) {
        return lastGoblinSpawnMap.getOrDefault(uuid, 0);
    }

    private void setLastGoblinSpawnBlocks(UUID uuid, int blocks) {
        lastGoblinSpawnMap.put(uuid, blocks);
    }


}
