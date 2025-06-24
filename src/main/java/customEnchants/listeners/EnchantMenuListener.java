package customEnchants.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import customEnchants.TestEnchants;
import customEnchants.utils.GuiUtil;

public class EnchantMenuListener implements Listener {

    private final TestEnchants plugin; // or whatever class you need

    public EnchantMenuListener(TestEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;
    String title = event.getView().getTitle();

    if (title == null) return;

    String strippedTitle = ChatColor.stripColor(title);
    if (!strippedTitle.startsWith("Custom Enchants - Page ")) return;

    if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
        event.setCancelled(true);
    }

    ItemStack clicked = event.getCurrentItem();
    if (clicked == null || clicked.getType() == Material.AIR) return;

    int currentPage = 1;
    try {
        String pagePart = strippedTitle.substring(strippedTitle.indexOf("Page ") + 5);
        currentPage = Integer.parseInt(pagePart.trim());
    } catch (Exception ignored) {}

    if (clicked.getType() == Material.ARROW) {
        String itemName = clicked.getItemMeta() != null ? ChatColor.stripColor(clicked.getItemMeta().getDisplayName()) : "";

        if (itemName.equalsIgnoreCase("Next Page")) {
            int nextPage = currentPage + 1;
            if (nextPage <= GuiUtil.getMaxPages()) {
                Inventory nextInv = GuiUtil.getEnchantInfoGUI(nextPage);
                player.openInventory(nextInv);
            }
        } else if (itemName.equalsIgnoreCase("Previous Page")) {
            int prevPage = currentPage - 1;
            if (prevPage < 1) prevPage = 1;
            Inventory prevInv = GuiUtil.getEnchantInfoGUI(prevPage);
            player.openInventory(prevInv);
        }
    }
}
}
