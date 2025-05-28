package customEnchants.listeners;

import customEnchants.utils.ItemVoucherUtil;
import customEnchants.utils.ItemVoucherUtil.VoucherType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class VoucherListener implements Listener {

    @EventHandler
    public void onVoucherUse(PlayerInteractEvent event) {
        if (!event.getAction().toString().contains("RIGHT_CLICK")) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());

        VoucherType type = switch (name) {
            case "Transmutation Voucher" -> VoucherType.TRANSMUTATION;
            case "Decoration Voucher" -> VoucherType.DECORATION;
            case "$1500 Voucher" -> VoucherType.MONEY_1500;
            case "Key All Voucher" -> VoucherType.KEY_ALL;
            default -> null;
        };

        if (type != null) {
            event.setCancelled(true); // prevent block interaction
            item.setAmount(item.getAmount() - 1); // consume one
            ItemVoucherUtil.redeemVoucher(event.getPlayer(), type);
        }
    }

    @EventHandler
    public void onVoucherGuiClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || !event.getView().getTitle().equals("Decoration Voucher")) return;

        event.setCancelled(true); // block all interaction by default

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        Player player = (Player) event.getWhoClicked();
        player.getInventory().addItem(clicked.clone());
        player.sendMessage(ChatColor.GREEN + "You claimed: " + clicked.getType().name());
        player.closeInventory();
    }

}
