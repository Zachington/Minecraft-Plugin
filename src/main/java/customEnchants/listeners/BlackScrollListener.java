package customEnchants.listeners;

import customEnchants.TestEnchants;
import customEnchants.utils.EnchantmentData;
import customEnchants.utils.GuiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;

public class BlackScrollListener implements Listener {
    
    private final Set<UUID> programmaticClosures = new HashSet<>();
    private final JavaPlugin plugin;

    public BlackScrollListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;

    Inventory clickedInventory = event.getClickedInventory();
    if (clickedInventory == null) return;

    InventoryView view = event.getView();
    String title = view.getTitle();
    boolean isBlackScrollGui = title.equals("§8Black Scroll");

    ItemStack clickedItem = event.getCurrentItem();
    Material clickedType = (clickedItem == null) ? Material.AIR : clickedItem.getType();
    boolean isToolOrArmor = isToolOrArmor(clickedType);
    boolean isGlassPane = clickedType.name().endsWith("GLASS_PANE");

    ItemStack cursor = event.getCursor();
    boolean isHoldingBlackScroll = false;

    if (cursor != null && cursor.hasItemMeta()) {
        ItemMeta meta = cursor.getItemMeta();
        if (meta.hasDisplayName() && meta.getDisplayName().equals("§8Black Scroll")) {
            isHoldingBlackScroll = true;
        }
    }
    
    if (event.getView().getTitle().equals("§8Black Scroll")) {
        if (clickedInventory == null || !clickedInventory.equals(event.getView().getTopInventory())) {
            return; // Click happened in player's inventory — ignore it
        }
        }

    // ========== BLACK SCROLL GUI PROTECTION ==========
    if (isBlackScrollGui) {
        // Cancel unsafe interactions
        if (clickedInventory.equals(view.getTopInventory())) {
            if (isGlassPane || isToolOrArmor || event.getClick().isShiftClick() || event.getClick().isKeyboardClick() || isHoldingBlackScroll) {
                event.setCancelled(true);
                return;
            }
            if (isHoldingBlackScroll && clickedItem != null && clickedItem.getType() == Material.ENCHANTED_BOOK) {
                event.setCancelled(true);
                return;
            }
        }

        if (isHoldingBlackScroll && clickedInventory == view.getBottomInventory() && isToolOrArmor) {
            event.setCancelled(true);
            return;
        }


        // === Handle clicking an enchanted book in GUI ===
        if (clickedItem != null && clickedItem.getType() == Material.ENCHANTED_BOOK) {
            // Give a copy of the book
            ItemStack bookCopy = clickedItem.clone();
            HashMap<Integer, ItemStack> leftoverBook = player.getInventory().addItem(bookCopy);
            if (!leftoverBook.isEmpty()) {
                leftoverBook.values().forEach(book -> player.getWorld().dropItemNaturally(player.getLocation(), book));
            }
            event.setCancelled(true); // Prevent default pickup
            event.setCurrentItem(null); // Remove the clicked book from the GUI

            // Modify the tool in slot 13
            ItemStack toolInSlot = view.getTopInventory().getItem(13);
            if (toolInSlot != null && toolInSlot.getType() != Material.AIR) {
                ItemStack modifiedTool = toolInSlot.clone();
                ItemMeta toolMeta = modifiedTool.getItemMeta();
                ItemMeta bookMeta = clickedItem.getItemMeta();

                if (toolMeta != null && bookMeta != null && toolMeta.hasLore() && bookMeta.hasDisplayName()) {
                    List<String> toolLore = new ArrayList<>(toolMeta.getLore());
                    String strippedBookName = ChatColor.stripColor(bookMeta.getDisplayName());
                    toolLore.removeIf(line -> ChatColor.stripColor(line).contains(strippedBookName));
                    toolMeta.setLore(toolLore);
                    modifiedTool.setItemMeta(toolMeta);
                }

                HashMap<Integer, ItemStack> leftoverTool = player.getInventory().addItem(modifiedTool);
                if (!leftoverTool.isEmpty()) {
                    leftoverTool.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
                }
            }

            view.getTopInventory().setItem(13, null);
            programmaticClosures.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    player.closeInventory();
                }
            }, 1L);
        }
        return; // End Black Scroll GUI logic
    }

    // ========== OUTSIDE BLACK SCROLL GUI ==========
    if (cursor == null || clickedItem == null || clickedInventory == null) return;

    ItemMeta cursorMeta = cursor.getItemMeta();
    if (cursorMeta == null || !cursorMeta.hasDisplayName() || !cursorMeta.getDisplayName().equals("§8Black Scroll")) return;

    List<String> lore = cursorMeta.getLore();
    if (lore == null || lore.isEmpty() || !lore.get(0).contains("Drag and drop on a tool to remove an enchant")) return;

    if (!isCustomEnchantedTool(clickedItem)) return;

    // Remove tool from inventory
    event.setCurrentItem(null);

    // Consume 1 Black Scroll from cursor
    int totalAmount = cursor.getAmount();
    player.setItemOnCursor(null);
    if (totalAmount > 1) {
        ItemStack leftoverScrolls = cursor.clone();
        leftoverScrolls.setAmount(totalAmount - 1);
        HashMap<Integer, ItemStack> leftoverMap = player.getInventory().addItem(leftoverScrolls);
        if (!leftoverMap.isEmpty()) {
            leftoverMap.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
    }

    // Build GUI
    Inventory gui = GuiUtil.blackScrollInventory(player);
    gui.setItem(13, clickedItem.clone());

    // Fill enchanted book slots
    int[] bookSlots = {28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    int bookIndex = 0;

    ItemMeta toolMeta = clickedItem.getItemMeta();
    if (toolMeta != null && toolMeta.hasLore()) {
        List<String> toolLore = toolMeta.getLore();
        for (String line : toolLore) {
            for (EnchantmentData.EnchantmentInfo info : EnchantmentData.ENCHANTMENTS) {
    if (line.contains(info.name)) {

        // Prevent Prestige and P+ rarity enchants
        String rarity = info.rarity;
        if (rarity.equalsIgnoreCase("PRESTIGE") || rarity.equalsIgnoreCase("P+")) {
            continue; // Skip this enchant, don't add to black scroll GUI
        }

        int level = extractLevelFromLore(line);
        ItemStack book = EnchantmentData.createEnchantedBook(info, level, 100, false);
        if (bookIndex < bookSlots.length) {
            gui.setItem(bookSlots[bookIndex], book);
            bookIndex++;
        }
        break;
    }
}
        }
    }

    player.openInventory(gui);
}



    @EventHandler
    public void onBlackScrollGuiClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player player)) return;
    if (!event.getView().getTitle().equals("§8Black Scroll")) return;

    // If closed programmatically, skip
    if (programmaticClosures.remove(player.getUniqueId())) {
        // This was a programmatic close, so skip the logic
        return;
    }

    // Manual close: return the tool in slot 13
    Inventory inv = event.getInventory();
    ItemStack toolInSlot13 = inv.getItem(13);

    if (toolInSlot13 != null && toolInSlot13.getType() != Material.AIR) {
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(toolInSlot13);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
        inv.setItem(13, null);
    }
    new BukkitRunnable() {
        @Override
        public void run() {
            player.updateInventory();
        }
    }.runTaskLater(TestEnchants.getInstance(), 2L);
}

    private int extractLevelFromLore(String line) {
        String[] parts = line.split(" ");
        String last = parts[parts.length - 1];
        return romanToInt(last);
    }

    private static int romanToInt(String levelStr) {
        switch (levelStr) {
            case "I": return 1;
            case "II": return 2;
            case "III": return 3;
            case "IV": return 4;
            case "V": return 5;
        }
        try {
            int num = Integer.parseInt(levelStr);
            if (num >= 6) return num;
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    private boolean isCustomEnchantedTool(ItemStack item) {
        if (item == null) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;

        List<String> lore = meta.getLore();
        if (lore == null) return false;

        for (String loreLine : lore) {
            for (String enchantName : EnchantmentData.ENCHANT_NAMES) {
                if (loreLine.contains(enchantName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isToolOrArmor(Material mat) {
        // Tools
        if (mat == Material.WOODEN_PICKAXE || mat == Material.STONE_PICKAXE || mat == Material.IRON_PICKAXE
            || mat == Material.GOLDEN_PICKAXE || mat == Material.DIAMOND_PICKAXE || mat == Material.NETHERITE_PICKAXE)
            return true;
        if (mat == Material.WOODEN_AXE || mat == Material.STONE_AXE || mat == Material.IRON_AXE
            || mat == Material.GOLDEN_AXE || mat == Material.DIAMOND_AXE || mat == Material.NETHERITE_AXE)
            return true;
        if (mat == Material.WOODEN_SHOVEL || mat == Material.STONE_SHOVEL || mat == Material.IRON_SHOVEL
            || mat == Material.GOLDEN_SHOVEL || mat == Material.DIAMOND_SHOVEL || mat == Material.NETHERITE_SHOVEL)
            return true;
        if (mat == Material.FISHING_ROD) return true;
        if (mat == Material.BOW) return true;
        if (mat == Material.CROSSBOW) return true;
        if (mat == Material.WOODEN_HOE || mat == Material.STONE_HOE || mat == Material.IRON_HOE
            || mat == Material.GOLDEN_HOE || mat == Material.DIAMOND_HOE || mat == Material.NETHERITE_HOE)
            return true;
        if (mat == Material.WOODEN_SWORD || mat == Material.STONE_SWORD || mat == Material.IRON_SWORD
            || mat == Material.GOLDEN_SWORD || mat == Material.DIAMOND_SWORD || mat == Material.NETHERITE_SWORD)
            return true;
        // Armor
        if (mat == Material.LEATHER_HELMET || mat == Material.LEATHER_CHESTPLATE || mat == Material.LEATHER_LEGGINGS || mat == Material.LEATHER_BOOTS)
            return true;
        if (mat == Material.CHAINMAIL_HELMET || mat == Material.CHAINMAIL_CHESTPLATE || mat == Material.CHAINMAIL_LEGGINGS || mat == Material.CHAINMAIL_BOOTS)
            return true;
        if (mat == Material.IRON_HELMET || mat == Material.IRON_CHESTPLATE || mat == Material.IRON_LEGGINGS || mat == Material.IRON_BOOTS)
            return true;
        if (mat == Material.GOLDEN_HELMET || mat == Material.GOLDEN_CHESTPLATE || mat == Material.GOLDEN_LEGGINGS || mat == Material.GOLDEN_BOOTS)
            return true;
        if (mat == Material.DIAMOND_HELMET || mat == Material.DIAMOND_CHESTPLATE || mat == Material.DIAMOND_LEGGINGS || mat == Material.DIAMOND_BOOTS)
            return true;
        if (mat == Material.NETHERITE_HELMET || mat == Material.NETHERITE_CHESTPLATE || mat == Material.NETHERITE_LEGGINGS || mat == Material.NETHERITE_BOOTS)
            return true;

        return false;
    }
}
