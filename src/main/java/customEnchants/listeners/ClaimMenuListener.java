package customEnchants.listeners;

import customEnchants.utils.customItemUtil;
import customEnchants.TestEnchants;
import customEnchants.utils.ClaimStorage;
import customEnchants.utils.GuiUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ClaimMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;

    InventoryView view = event.getView();
    String title = view.getTitle();

    // Check if this is one of the GUIs to protect
    if (title.equals("Claim") || title.equals(ChatColor.GOLD + "Key Claim") || title.equalsIgnoreCase("Sell Essence")) {

        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;

        // Cancel *all* clicks inside the top inventory (the GUI)
        if (clickedInv.equals(view.getTopInventory())) {
            event.setCancelled(true);

            if (event.getClick().isKeyboardClick()) return;

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

            // Your existing GUI interaction code here...
            if (title.equals("Claim")) {
                String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

                switch (itemName.toLowerCase()) {
                    case "key claim" -> player.openInventory(GuiUtil.guiKeyClaim(player));
                    case "prestige claim" -> player.sendMessage(ChatColor.LIGHT_PURPLE + "Prestige Claim GUI not implemented yet!");
                    case "other claims" -> player.sendMessage(ChatColor.WHITE + "Other Claims GUI not implemented yet!");
                }
                return;
            }

            if (title.equals(ChatColor.GOLD + "Key Claim")) {
                String keyName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                if (keyName == null || keyName.isEmpty()) return;

                int currentAmount = ClaimStorage.getKeyCount(player, keyName);
                if (currentAmount <= 0) {
                    if (clicked.getType() != Material.TRIAL_KEY) return;
                    player.sendMessage(ChatColor.RED + "You don't have any " + keyName + "s!");
                return;
                }

                int amountToGive = event.isShiftClick() ? Math.min(64, currentAmount) : 1;
                ItemStack keyItem = customItemUtil.createCustomItem(keyName);
                keyItem.setAmount(amountToGive);

                HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(keyItem);
                int failed = leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
                int given = amountToGive - failed;

                if (given > 0) {
                    ClaimStorage.removeKeys(player, keyName, given);
                    player.sendMessage(ChatColor.GREEN + "You claimed " + given + " " + keyName + (given > 1 ? "s!" : "!"));
                } else {
                    player.sendMessage(ChatColor.RED + "Your inventory is full!");
                }

                Bukkit.getScheduler().runTaskLater(TestEnchants.getPlugin(TestEnchants.class), () ->
                    player.openInventory(GuiUtil.guiKeyClaim(player)), 1L);
            }
        }
        else if (clickedInv.equals(view.getBottomInventory())) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
            }
        }
    }
}



    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
    InventoryView view = event.getView();
    String title = ChatColor.stripColor(view.getTitle());

    if (title.equals("Claim") || title.equals("Key Claim") || title.equalsIgnoreCase("Sell Essence")) {
        // Only cancel if any of the dragged slots are in the GUI (top inventory)
        int guiSize = view.getTopInventory().getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < guiSize) {
                event.setCancelled(true);
                break;
            }
        }
    }
}
}
