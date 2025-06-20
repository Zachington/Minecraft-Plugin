package customEnchants.listeners;

import customEnchants.TestEnchants;
import customEnchants.managers.VaultManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.block.Action;



public class VaultListener implements Listener {
    private final VaultManager vaultManager;

    public VaultListener(VaultManager vaultManager) {
        this.vaultManager = vaultManager;
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENDER_CHEST) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        Inventory vault = vaultManager.getVault(player.getUniqueId(), 1);
        player.openInventory(vault);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        Player player = (Player) event.getPlayer();

        for (int i = 1; i <= 5; i++) {
            if (inv.equals(vaultManager.getVault(player.getUniqueId(), i))) {
                vaultManager.saveVault(player.getUniqueId());
                break;
            }
        }
    }

    @EventHandler
    public void onVaultItemMove(InventoryClickEvent event) {
    Inventory inventory = event.getInventory();
    if (inventory == null || !event.getView().getTitle().startsWith("Vault")) return;

    if (!(event.getWhoClicked() instanceof Player player)) return;

    if (event.getRawSlot() < inventory.getSize()) {
        Bukkit.getScheduler().runTaskLater(TestEnchants.getInstance(), () -> {
            vaultManager.saveVault(player.getUniqueId()); // Save after click
        }, 1L);
    }
}

}

