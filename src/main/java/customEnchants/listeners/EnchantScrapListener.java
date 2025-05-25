package customEnchants.listeners;

import customEnchants.utils.GuiUtil;
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

public class EnchantScrapListener implements Listener {

    private final String GUI_TITLE = "§8Tinker Enchant Book";
    private final Plugin plugin;

    public EnchantScrapListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals(GUI_TITLE)) return;

        Inventory inv = e.getInventory();
        int slot = e.getRawSlot();

        // Only handle clicks inside the GUI inventory
        if (slot >= inv.getSize()) return;

        ItemStack clicked = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        // Prevent placing sugar into GUI slots (from cursor)
        if ((e.getAction().name().contains("PLACE") || e.getAction().name().contains("PICKUP"))
                && cursor != null && cursor.getType() == Material.SUGAR) {
            e.setCancelled(true);
            return;
        }

        // Prevent taking glass panes anywhere in the GUI
        if (clicked != null && isGlass(clicked.getType())) {
            e.setCancelled(true);
            return;
        }

        // Handle shift-click from player inventory into GUI
        if (e.isShiftClick() && e.getClickedInventory() != null && !e.getClickedInventory().equals(inv)) {
            ItemStack shiftItem = clicked;
            if (shiftItem != null && shiftItem.getType() == Material.ENCHANTED_BOOK && GuiUtil.parseCustomEnchantBook(shiftItem) != null) {
                int targetSlot = -1;
                for (int i = 0; i <= 8; i++) {
                    ItemStack slotItem = inv.getItem(i);
                    if (slotItem != null && slotItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                        targetSlot = i;
                        break;
                    }
                }
                if (targetSlot == -1) return; // No free slot

                e.setCancelled(true);

                // Place one enchanted book in target slot
                ItemStack toPlace = shiftItem.clone();
                toPlace.setAmount(1);
                inv.setItem(targetSlot, toPlace);

                // Remove one book from player's stack
                if (shiftItem.getAmount() > 1) {
                    shiftItem.setAmount(shiftItem.getAmount() - 1);
                } else {
                    e.getClickedInventory().removeItem(shiftItem);
                }

                // Place sugar below (slot + 18)
                int sugarSlot = targetSlot + 18;
                if (sugarSlot < inv.getSize()) {
                    ItemStack sugar = new ItemStack(Material.SUGAR);
                    ItemMeta meta = sugar.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName("§fScrap Book");
                        sugar.setItemMeta(meta);
                    }
                    inv.setItem(sugarSlot, sugar);
                }

                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
                return;
            }
        }
        // Handle normal drag-and-drop placing of enchanted book into GUI slots 0-8
        else if (!e.isShiftClick()) {
            if (slot >= 0 && slot <= 8) {
                if (cursor != null && cursor.getType() == Material.ENCHANTED_BOOK && GuiUtil.parseCustomEnchantBook(cursor) != null) {
                    if (clicked != null && clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                        e.setCancelled(true);
                        inv.setItem(slot, cursor.clone());
                        player.setItemOnCursor(null);

                        // Place sugar below
                        int sugarSlot = slot + 18;
                        if (sugarSlot < inv.getSize()) {
                            ItemStack sugar = new ItemStack(Material.SUGAR);
                            ItemMeta meta = sugar.getItemMeta();
                            if (meta != null) {
                                meta.setDisplayName("§fScrap Book");
                                sugar.setItemMeta(meta);
                            }
                            inv.setItem(sugarSlot, sugar);
                        }

                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTakeSugar(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals(GUI_TITLE)) return;

        Inventory inv = e.getInventory();
        int slot = e.getRawSlot();

        ItemStack clicked = e.getCurrentItem();

        // Allow taking sugar only if in bottom row (18-26) and it is sugar with correct name
        if (clicked != null && clicked.getType() == Material.SUGAR && slot >= 18 && slot <= 26) {
            if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;
            if (!clicked.getItemMeta().getDisplayName().equals("§fScrap Book")) return;

            // Cancel default pickup to manage manually
            e.setCancelled(true);

            int bookSlot = slot - 18;
            ItemStack book = inv.getItem(bookSlot);

            // Only proceed if bookSlot contains a valid custom enchant book
            if (book != null && GuiUtil.parseCustomEnchantBook(book) != null) {
                // Remove the book and reset GUI after short delay (to avoid concurrency issues)
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Clear book slot
                    inv.setItem(bookSlot, createPane(Material.BLACK_STAINED_GLASS_PANE));
                    // Reset bottom row panes
                    for (int i = 18; i < 27; i++) {
                        inv.setItem(i, createPane(Material.WHITE_STAINED_GLASS_PANE));
                    }
                    // Reset middle row panes (optional)
                    for (int i = 9; i < 18; i++) {
                        inv.setItem(i, createPane(Material.GRAY_STAINED_GLASS_PANE));
                    }
                }, 1L);

                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.5f);

                // Remove sugar from slot immediately (simulate taking sugar)
                inv.setItem(slot, null);
            }
        }
    }

    private static boolean isGlass(Material mat) {
        return mat != null && mat.name().endsWith("_STAINED_GLASS_PANE");
    }

    private ItemStack createPane(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            pane.setItemMeta(meta);
        }
        return pane;
    }
}
