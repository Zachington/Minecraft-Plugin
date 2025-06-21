package customEnchants.listeners;

import customEnchants.utils.customItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class FurnaceListener implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey tierKey;
    private final Random random = new Random();

    public FurnaceListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.tierKey = new NamespacedKey(plugin, "furnaceTier");
    }

    // Store tier on place
    @EventHandler
    public void onFurnacePlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());
        int tier = getTierFromName(name);
        if (tier <= 0) return;

        Block placedBlock = event.getBlockPlaced();

        // Only store if placed block is furnace or blast furnace
        if (placedBlock.getType() == Material.FURNACE || placedBlock.getType() == Material.BLAST_FURNACE) {
            if (placedBlock.getState() instanceof Furnace furnace) {
                furnace.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, tier);
                furnace.update();
            }
        }
    }

    // Give custom furnace item back on break, prevent default drops
    @EventHandler
    public void onFurnaceBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.FURNACE && block.getType() != Material.BLAST_FURNACE) return;

        if (!(block.getState() instanceof Furnace furnace)) return;

        int tier = getTierFromFurnaceBlock(furnace);
        if (tier <= 0) return;

        event.setDropItems(false); // Cancel normal drops

        // Drop custom furnace item of the correct tier
        String furnaceName = tier == 7 ? "Blast Furnace" : "Tier " + tier + " Furnace";
        ItemStack drop = customItemUtil.createCustomItem(furnaceName);
        if (drop != null) {
            block.getWorld().dropItemNaturally(block.getLocation(), drop);
        }
    }

    // Handle double smelt chance and add extra smelted item in furnace output slot
    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
    Block block = event.getBlock();
    Furnace furnace = (Furnace) block.getState();
    Bukkit.getLogger().info("Smelting in special furnace");

    int tier = getTierFromFurnaceBlock(furnace);
    if (tier <= 0) return;

    int chance = getDoubleSmeltChance(tier);
    if (random.nextInt(100) >= chance) return;

    // Get the original result
    ItemStack result = event.getResult();
    if (result == null || result.getType() == Material.AIR) return;

    // Double the result
    ItemStack doubled = result.clone();
    doubled.setAmount(Math.min(result.getAmount() * 2, result.getMaxStackSize()));

    event.setResult(doubled); // Use event's method, not inventory
}


    // Helper: get tier from block PersistentDataContainer
    
    private int getTierFromFurnaceBlock(Furnace furnace) {
        if (furnace == null) return 0;
        return furnace.getPersistentDataContainer().getOrDefault(tierKey, PersistentDataType.INTEGER, 0);
    }

    // Helper: parse tier from furnace display name string
    private int getTierFromName(String name) {
        if (name.equalsIgnoreCase("Blast Furnace")) return 7;
        if (name.startsWith("Tier ")) {
            try {
                return Integer.parseInt(name.split(" ")[1]);
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    // Helper: returns the double smelt chance for each tier
    private int getDoubleSmeltChance(int tier) {
        return switch (tier) {
            case 1 -> 10;
            case 2 -> 20;
            case 3 -> 30;
            case 4 -> 45;
            case 5 -> 60;
            case 6 -> 80;
            case 7 -> 100;
            default -> 0;
        };
    }
}
