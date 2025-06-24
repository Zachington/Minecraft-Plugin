package customEnchants.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import customEnchants.TestEnchants;

public class EnchantTableListener implements Listener {

    private static final ItemStack LAPIS = new ItemStack(Material.LAPIS_LAZULI, 64);

    @EventHandler
    public void onEnchantTableOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Inventory inv = event.getInventory();
        if (inv.getType() == InventoryType.ENCHANTING) {
            Bukkit.getScheduler().runTaskLater(TestEnchants.getInstance(), () -> {
                inv.setItem(1, LAPIS.clone()); // Slot 1 is the lapis slot
            }, 1L); // Delay 1 tick to make sure GUI is fully initialized
        }
    }

    @EventHandler
    public void onEnchantTableClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Inventory inv = event.getInventory();
        if (inv.getType() == InventoryType.ENCHANTING && event.getRawSlot() == 1) {
            event.setCancelled(true); // Prevent lapis interaction
        }
    }
}
