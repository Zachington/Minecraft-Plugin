package customEnchants.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import customEnchants.utils.customItemUtil;

public class AntiCheatListener implements Listener {

    // Cancel placing if item is custom
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null) return;

        Material type = item.getType();
        if (type == Material.FURNACE || type == Material.BLAST_FURNACE) return;

        if (customItemUtil.isCustomItem(item)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            player.sendMessage(ChatColor.RED + "You cannot place custom items!");
        }
    }

    // Cancel crafting if result is custom
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
    if (event.getWhoClicked() instanceof Player player) {
        // Check the result
        ItemStack result = event.getRecipe().getResult();
        if (customItemUtil.isCustomItem(result)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot craft custom items!");
            return;
        }

        // Check ingredients
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item != null && customItemUtil.isCustomItem(item)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot use custom items in crafting!");
                return;
            }
        }
    }
}

    // Optional: Prevent preview of custom items in crafting result slot
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
    ItemStack result = event.getInventory().getResult();
    if (result != null && customItemUtil.isCustomItem(result)) {
        event.getInventory().setResult(null);
        return;
    }

    // Check ingredients too
    for (ItemStack item : event.getInventory().getMatrix()) {
        if (item != null && customItemUtil.isCustomItem(item)) {
            event.getInventory().setResult(null);
            return;
        }
    }
}
}
