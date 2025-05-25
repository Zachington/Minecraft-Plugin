package customEnchants.listeners;


import customEnchants.utils.GuiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;

public class EnchantScrapListener implements Listener {

    private final String GUI_TITLE = "§8Tinkerer";
    private final Plugin plugin;
    
    public EnchantScrapListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;
    if (!event.getView().getTitle().equals(GUI_TITLE)) return;
    //var
    Inventory clickedInv = event.getClickedInventory();
    ItemStack clickedItem = event.getCurrentItem();
    ItemStack cursor = event.getCursor();
    int slot = event.getRawSlot();
    Inventory topInv = event.getView().getTopInventory();


    //Cancels interaction with gui unless drag and dropping enchanted book
    if (event.getClickedInventory() == event.getView().getTopInventory()) {
    // Default cancel behavior for GUI
    event.setCancelled(true);

    // Handle drag-and-drop of custom enchant book onto glass
    if (slot >= 0 && slot <= 8 &&
        cursor != null && cursor.getType() == Material.ENCHANTED_BOOK &&
        GuiUtil.parseCustomEnchantBook(cursor) != null &&
        (clickedItem == null || clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE)) {

        topInv.setItem(slot, cursor.clone());
        player.setItemOnCursor(null);
        Bukkit.getScheduler().runTask(plugin, () -> updateSugarBelowCustomEnchant(topInv));
    }
    // Detect shift-clicking a custom enchanted book OUT of the Scrap GUI
if (event.isShiftClick() && clickedInv != null && clickedInv.equals(topInv) && slot >= 0 && slot <= 8) {
    if (clickedItem != null && clickedItem.getType() == Material.ENCHANTED_BOOK &&
        GuiUtil.parseCustomEnchantBook(clickedItem) != null) {

        // Wait 1 tick so the item is removed before updating
        Bukkit.getScheduler().runTask(plugin, () -> updateSugarBelowCustomEnchant(topInv));
    }
}
}





    //Update on click book

    if (clickedItem != null 
    && clickedItem.getType() == Material.ENCHANTED_BOOK 
    && GuiUtil.parseCustomEnchantBook(clickedItem) != null
    && (event.getAction() == InventoryAction.PICKUP_ALL 
        || event.getAction() == InventoryAction.PICKUP_ONE 
        || event.getAction() == InventoryAction.PICKUP_HALF 
        || event.getAction() == InventoryAction.PICKUP_SOME)) {      
    Bukkit.getScheduler().runTask(plugin, () -> updateSugarBelowCustomEnchant(topInv));
    }


    // Allow interacting with custom enchant book slots (0-8)
if (slot >= 0 && slot <= 8) {
    if (clickedItem != null && clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
        event.setCancelled(true);
        return;
    }

    if (clickedItem != null && clickedItem.getType() == Material.ENCHANTED_BOOK) {
        
        event.setCancelled(false);
        return;
    }

    if (clickedItem == null && cursor != null && cursor.getType() == Material.ENCHANTED_BOOK &&
        GuiUtil.parseCustomEnchantBook(cursor) != null) {
        event.setCancelled(false);
        return;
    }

    event.setCancelled(true);
    Bukkit.getScheduler().runTask(plugin, () -> updateSugarBelowCustomEnchant(topInv));
    return;
}

    // Handle clicking the sugar to destroy the book below (slots 18-26)
if (slot >= 18 && slot <= 26 && clickedItem != null && clickedItem.getType() == Material.SUGAR) {
    if (clickedInv == null) return;
    int bookSlot = slot - 18; // book above sugar
    ItemStack book = clickedInv.getItem(bookSlot);
    if (book != null && GuiUtil.parseCustomEnchantBook(book) != null) {
        boolean keepSugar = ThreadLocalRandom.current().nextBoolean(); // 50/50 chance

        if (keepSugar) {
            // Player keeps the sugar: 
            // Remove the book
            clickedInv.setItem(bookSlot, null); 

            // Remove sugar from GUI slot
            clickedInv.setItem(slot, new ItemStack(Material.WHITE_STAINED_GLASS_PANE)); 

            // Give sugar item to player inventory
            GuiUtil.RarityInfo rarityInfo = GuiUtil.getRarityInfoFromBook(book);
            ItemStack sugarToGive = createSugar(rarityInfo.colorCode, rarityInfo.rarityName);

            // Update lore to show 1–10% instead of §kxx
            ItemMeta meta = sugarToGive.getItemMeta();
            if (meta != null && meta.hasLore()) {
                List<String> lore = meta.getLore();
                if (lore != null && !lore.isEmpty()) {
                    int chance = ThreadLocalRandom.current().nextInt(1, 11); // 1 to 10
                for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).contains("§kxx")) {
                lore.set(i, "§7Success Booster: §a" + chance + "§a%");
                break;
            }
        }
        meta.setLore(lore);
        sugarToGive.setItemMeta(meta);
    }
}
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(sugarToGive);

            // If inventory full, drop on ground near player
            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), sugarToGive);
            }

            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_PLACE, 1, 1);

        } else {
            // Destroy both sugar and book (reset sugar to white glass)
            clickedInv.setItem(slot, new ItemStack(Material.WHITE_STAINED_GLASS_PANE)); 
            clickedInv.setItem(bookSlot, null); 
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
        }
    }
    event.setCancelled(true); // prevent default pickup behavior
    Bukkit.getScheduler().runTask(plugin, () -> updateSugarBelowCustomEnchant(event.getView().getTopInventory()));

    return;
}




    // Block clicking black or white glass panes anywhere else
    if (clickedItem != null) {
        Material type = clickedItem.getType();
        if (type == Material.BLACK_STAINED_GLASS_PANE || type == Material.WHITE_STAINED_GLASS_PANE) {
            return; // already cancelled
        }
    }

    // Handle shift-clicking books from player inventory into GUI (slots 0-8)
    if (event.isShiftClick() && clickedInv != null && clickedInv.equals(player.getInventory())) {
        if (clickedItem != null && clickedItem.getType() == Material.ENCHANTED_BOOK &&
            GuiUtil.parseCustomEnchantBook(clickedItem) != null) {

            for (int i = 0; i < 9; i++) {
                ItemStack target = topInv.getItem(i);
                if (target != null && target.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                    topInv.setItem(i, clickedItem.clone());
                    player.getInventory().setItem(event.getSlot(), null);
                    Bukkit.getScheduler().runTask(plugin, () -> updateSugarBelowCustomEnchant(topInv));
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
        blackMeta.setDisplayName(" ");
        blackMeta.setLore(null);
        blackGlass.setItemMeta(blackMeta);
    }

    // Create plain white glass pane without any name or lore
    ItemStack whiteGlass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
    ItemMeta whiteMeta = whiteGlass.getItemMeta();
    if (whiteMeta != null) {
        whiteMeta.setDisplayName(" ");
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

        // Add lore line with "Success Booster: " in green + obfuscated "xx" + "%" in green
        List<String> lore = new ArrayList<>();
        lore.add("§7Success Booster: §a§kxx§a%");
        lore.add(" ");
        lore.add("§7Drag and Drop onto a custom enchant book of ");
        lore.add("§7the same rarity to boost the success chance");

        meta.setLore(lore);
        sugar.setItemMeta(meta);
    }
    return sugar;
}

@EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        Inventory gui = event.getInventory();
        Player player = (Player) event.getPlayer();

        // Check the slots for any custom enchant books
        for (int i = 0; i < 9; i++) {  // assuming 0-8 are book slots
            ItemStack item = gui.getItem(i);
            if (item != null && item.getType() == Material.ENCHANTED_BOOK) {
                // Check if it's a custom enchant book (use your parser)
                if (GuiUtil.parseCustomEnchantBook(item) != null) {
                    // Try adding back to player's inventory
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                    if (!leftover.isEmpty()) {
                        // If player's inventory full, drop at player's location
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                        player.sendMessage(ChatColor.YELLOW + "Your custom enchant book was dropped because your inventory is full.");
                    }
                    // Remove from GUI so it's not duplicated
                    gui.setItem(i, null);
                }
            }
        }
    }
}

