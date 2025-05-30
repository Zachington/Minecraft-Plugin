package customEnchants.listeners;

import customEnchants.TestEnchants;
import customEnchants.utils.GuiUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EssenceMenuListener implements Listener {

    private final TestEnchants plugin;

    public EssenceMenuListener(TestEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEssenceMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String rawTitle = event.getView().getTitle();
        String strippedTitle = ChatColor.stripColor(rawTitle);
        ItemStack clicked = event.getCurrentItem();

        // === Handle Essence Menu ===
        if (strippedTitle.equalsIgnoreCase("Essence Menu")) {
            event.setCancelled(true);
            if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

            String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

            switch (displayName) {
                case "Extractors" -> player.openInventory(GuiUtil.createExtractorStorageGUI(player, plugin.getEssenceManager()));
                case "Plows", "Fishing Nets" -> player.sendMessage(ChatColor.RED + "Not implemented yet.");
                case "Sell Essence" -> player.openInventory(GuiUtil.essenceSellInventory(player, plugin.getEssenceManager()));
            }
            return;
        }

        // === Handle Sell Essence GUI ===
        if (strippedTitle.equalsIgnoreCase("Sell Essence")) {
            event.setCancelled(true);
    
            if (clicked == null) {
                
                return;
            }

            if (!clicked.hasItemMeta()) {
                return;
            }

            Material type = clicked.getType();

            int tier = switch (type) {
                case RED_DYE -> 1;
                case ORANGE_DYE -> 2;
                case YELLOW_DYE -> 3;
                case LIME_DYE -> 4;
                case GREEN_DYE -> 5;
                case BLUE_DYE -> 6;
                case MAGENTA_DYE -> 7;
                case PURPLE_DYE -> 8;
                default -> -1;
            };

            
            if (tier == -1) {
                return;
            }

            int current = plugin.getEssenceManager().getEssence(player, tier);

            if (current <= 0) {
                player.sendMessage(ChatColor.RED + "You have no essence of this tier to sell!");
                return;
            }

            boolean isShiftClick = event.isShiftClick();

            int amountToSell = isShiftClick ? current : 1;

            plugin.getEssenceManager().removeEssence(player, tier, amountToSell);

            double rewardPer = plugin.getEssenceSellPrices().getOrDefault(tier, 10.0);
            double totalReward = rewardPer * amountToSell;
            plugin.getEconomy().depositPlayer(player, totalReward);

            player.sendMessage(ChatColor.GREEN + "Sold " + amountToSell + " Tier " + tier + " Essence for $" + totalReward);
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                player.openInventory(GuiUtil.essenceSellInventory(player, plugin.getEssenceManager())),
            1L);
        }
    }
}
