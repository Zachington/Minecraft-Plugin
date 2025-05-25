package customEnchants.listeners;

import customEnchants.utils.EnchantmentData;
import customEnchants.utils.GuiUtil;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;

public class EnchantScrapListener implements Listener {

    private final String GUI_TITLE = "§8Tinkerer";
    private final Plugin plugin;
    private static final Set<Material> BLOCKED_MATERIALS = Set.of(
        Material.BLACK_STAINED_GLASS_PANE,
        Material.GRAY_STAINED_GLASS_PANE,
        Material.WHITE_STAINED_GLASS_PANE
    );

    public EnchantScrapListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;
    if (!event.getView().getTitle().equals(GUI_TITLE)) return;

    event.setCancelled(true); // Block everything by default

    Inventory clickedInv = event.getClickedInventory();
    ItemStack clickedItem = event.getCurrentItem();
    ItemStack cursor = event.getCursor();
    int slot = event.getRawSlot();

    // Allow placing custom books into black glass panes
    if (slot < 27 && clickedItem != null && clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
        if (cursor != null && cursor.getType() == Material.ENCHANTED_BOOK &&
            GuiUtil.parseCustomEnchantBook(cursor) != null) {
            event.setCancelled(false); // allow placing
        }
        return;
    }

    // Handle clicking the sugar to destroy the book
    if (slot >= 18 && slot <= 26 && clickedItem != null && clickedItem.getType() == Material.SUGAR) {
        int bookSlot = slot - 9;
        ItemStack book = clickedInv.getItem(bookSlot);
        if (book != null && GuiUtil.parseCustomEnchantBook(book) != null) {
            clickedInv.setItem(slot, new ItemStack(Material.WHITE_STAINED_GLASS_PANE)); // Reset sugar
            clickedInv.setItem(bookSlot, null); // Remove book
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
        }
        return;
    }

    // Handle shift-clicking books from player inventory into GUI
    if (event.isShiftClick() && clickedInv != null && clickedInv.equals(player.getInventory())) {
        if (clickedItem != null && clickedItem.getType() == Material.ENCHANTED_BOOK &&
            GuiUtil.parseCustomEnchantBook(clickedItem) != null) {

            Inventory topInv = event.getView().getTopInventory();
            for (int i = 0; i < 9; i++) {
                ItemStack target = topInv.getItem(i);
                if (target != null && target.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                    topInv.setItem(i, clickedItem.clone());
                    player.getInventory().setItem(event.getSlot(), null);
                    break;
                }
            }
        }
    }
}

    // Call this method whenever you need to update the sugar below custom enchant books
    public void updateSugarBelowCustomEnchant(Inventory gui) {
    // Create plain black glass pane without any name or lore
    ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta blackMeta = blackGlass.getItemMeta();
    if (blackMeta != null) {
        blackMeta.setDisplayName(null);
        blackMeta.setLore(null);
        blackGlass.setItemMeta(blackMeta);
    }

    // Create plain white glass pane without any name or lore
    ItemStack whiteGlass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
    ItemMeta whiteMeta = whiteGlass.getItemMeta();
    if (whiteMeta != null) {
        whiteMeta.setDisplayName(null);
        whiteMeta.setLore(null);
        whiteGlass.setItemMeta(whiteMeta);
    }

    for (int i = 0; i < 9; i++) { // slots 0-8 are for custom enchant books
        ItemStack item = gui.getItem(i);
        int belowSlot = i + 18; // slots 18-26 are below books, for sugar or white glass

        if (item != null && GuiUtil.parseCustomEnchantBook(item) != null) {
            GuiUtil.RarityInfo rarityInfo = GuiUtil.getRarityInfoFromBook(item);
            gui.setItem(belowSlot, createSugar(rarityInfo.colorCode, rarityInfo.rarityName));
        } else {
            // Reset slot i to plain black glass pane (no name/lore)
            gui.setItem(i, blackGlass);
            // Reset slot belowSlot to plain white glass pane (no name/lore)
            gui.setItem(belowSlot, whiteGlass);
        }
    }
}

// Modified createSugar to accept a color code string like "§6" or "§a"
private ItemStack createSugar(String colorCode, String rarityName) {
    ItemStack sugar = new ItemStack(Material.SUGAR);
    ItemMeta meta = sugar.getItemMeta();
    if (meta != null) {
        meta.setDisplayName(colorCode + rarityName + " Dust");
        sugar.setItemMeta(meta);
    }
    return sugar;
}

}
