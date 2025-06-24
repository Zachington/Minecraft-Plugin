package customEnchants.listeners;

import customEnchants.managers.CellManager;
import customEnchants.utils.CellUtil;
import customEnchants.utils.GuiUtil;
import customEnchants.utils.VaultUtil;
import net.milkbowl.vault.economy.Economy;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class CellUpgradeListener implements Listener {


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;
    Inventory inv = event.getInventory();

    if (!event.getView().getTitle().equals("Cell Upgrade Menu")) return;

    if (event.getRawSlot() < inv.getSize()) {
        event.setCancelled(true);

        // ====== Cell Size Upgrade (Slot 12) ======
        if (event.getRawSlot() == 12) {
            int currentLevel = CellUtil.getCellSizeLevel(player.getUniqueId());
            int maxLevel = 5;

            if (currentLevel >= maxLevel) {
                player.sendMessage(ChatColor.RED + "Your cell is already at maximum size.");
                return;
            }

            double upgradeCost = getCellSizeUpgradeCost(currentLevel + 1);

            Economy economy = VaultUtil.getEconomy();
            if (economy == null) {
                player.sendMessage(ChatColor.RED + "Economy system is not available.");
                return;
            }

            if (!economy.has(player, upgradeCost)) {
                player.sendMessage(ChatColor.RED + "You need $" + String.format("%,.0f", upgradeCost) + " to upgrade your cell size.");
                return;
            }

            economy.withdrawPlayer(player, upgradeCost);

            boolean success = CellManager.upgradeCellSize(player);

            if (success) {
                player.sendMessage(ChatColor.GREEN + "Cell size upgraded to level " + (currentLevel + 1) + "!");
                GuiUtil.openCellUpgradeGUI(player);
            } else {
                player.sendMessage(ChatColor.RED + "Failed to upgrade cell size.");
            }
            return;
        }

        // ====== Member Capacity Upgrade (Slot 10) ======
        if (event.getRawSlot() == 10) {
    if (!CellManager.hasCell(player.getUniqueId())) {
        player.sendMessage(ChatColor.RED + "You don't own a cell.");
        return;
    }

    CellUtil.PlayerCellData data = CellUtil.getPlayerCellData(player.getUniqueId());
    int level = data.getMemberCapacityLevel();

    if (level >= 3) {
        player.sendMessage(ChatColor.RED + "Member capacity is already maxed.");
        return;
    }

    double upgradeCost = switch (level) {
        case 0 -> 1_000_000;
        case 1 -> 5_000_000;
        case 2 -> 10_000_000;
        default -> 999_999_999;
    };

    Economy economy = VaultUtil.getEconomy();
    if (economy == null) {
        player.sendMessage(ChatColor.RED + "Economy system is not available.");
        return;
    }

    if (!economy.has(player, upgradeCost)) {
        player.sendMessage(ChatColor.RED + "You need $" + String.format("%,.0f", upgradeCost) + " to upgrade member capacity.");
        return;
    }

    economy.withdrawPlayer(player, upgradeCost);

    data.setMemberCapacityLevel(level + 1);
    player.sendMessage(ChatColor.GREEN + "Cell member limit upgraded to " +
            CellUtil.getMaxMembersForLevel(level + 1) + "!");
    GuiUtil.openCellUpgradeGUI(player);
}
    // ====== Hopper Limit Upgrade (Slot 14) ======
    if (event.getRawSlot() == 14) {
    if (!CellManager.hasCell(player.getUniqueId())) {
        player.sendMessage(ChatColor.RED + "You don't own a cell.");
        return;
    }

    CellUtil.PlayerCellData data = CellUtil.getPlayerCellData(player.getUniqueId());
    int level = data.getHopperLimitLevel();

    int maxLevel = 4;
    if (level >= maxLevel) {
        player.sendMessage(ChatColor.RED + "Hopper limit is already maxed.");
        return;
    }

    double upgradeCost = switch (level) {
        case 0 -> 500_000;
        case 1 -> 2_000_000;
        case 2 -> 5_000_000;
        case 3 -> 10_000_000;
        default -> 999_999_999;
    };

    Economy economy = VaultUtil.getEconomy();
    if (economy == null) {
        player.sendMessage(ChatColor.RED + "Economy system is not available.");
        return;
    }

    if (!economy.has(player, upgradeCost)) {
        player.sendMessage(ChatColor.RED + "You need $" + String.format("%,.0f", upgradeCost) + " to upgrade hopper limit.");
        return;
    }

    economy.withdrawPlayer(player, upgradeCost);

    data.setHopperLimitLevel(level + 1);
    player.sendMessage(ChatColor.GREEN + "Hopper limit upgraded to " + CellUtil.getHopperLimitForLevel(level + 1) + "!");
    GuiUtil.openCellUpgradeGUI(player);
    return;
}
    if (event.getRawSlot() == 16) { // Farm Upgrade slot
    if (!CellManager.hasCell(player.getUniqueId())) {
        player.sendMessage(ChatColor.RED + "You don't own a cell.");
        return;
    }

    CellUtil.PlayerCellData data = CellUtil.getPlayerCellData(player.getUniqueId());
    int level = data.getFarmUpgradeLevel();
    int maxLevel = 5;

    if (level >= maxLevel) {
        player.sendMessage(ChatColor.RED + "Farm upgrade is already maxed.");
        return;
    }

    double upgradeCost = getFarmUpgradeCost(level + 1);

    Economy economy = VaultUtil.getEconomy();
    if (economy == null) {
        player.sendMessage(ChatColor.RED + "Economy system is not available.");
        return;
    }

    if (!economy.has(player, upgradeCost)) {
        player.sendMessage(ChatColor.RED + "You need $" + String.format("%,.0f", upgradeCost) + " to upgrade your farm.");
        return;
    }

    economy.withdrawPlayer(player, upgradeCost);

    data.setFarmUpgradeLevel(level + 1);
    player.sendMessage(ChatColor.GREEN + "Farm growth and yield upgraded to " + String.format("%.2fx", 1.0 + 0.25 * (level + 1)) + "!");
    GuiUtil.openCellUpgradeGUI(player);
}
    }
}

    @EventHandler
    public void onHopperPlace(BlockPlaceEvent event) {
    Player player = event.getPlayer();
    Block block = event.getBlockPlaced();

    // Only care about hoppers
    if (block.getType() != Material.HOPPER) return;

    UUID playerId = player.getUniqueId();
    if (!CellManager.hasCell(playerId)) return;

    // Get player's cell region
    ProtectedRegion region = CellManager.getCellRegion(playerId);
    if (region == null) return;

    // Check if block placed inside the cell region
    if (!region.contains(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ())) {
        return;
    }

    // Count hoppers already in the cell region
    int hopperCount = countHoppersInRegion(region.getMinimumPoint(), region.getMaximumPoint(), block.getWorld());

    int hopperLimitLevel = CellUtil.getHopperLimitLevel(playerId);
    int hopperLimit = CellUtil.getHopperLimitForLevel(hopperLimitLevel);

    if (hopperCount >= hopperLimit) {
        player.sendMessage(ChatColor.RED + "Your cell hopper limit (" + hopperLimit + ") has been reached!");
        event.setCancelled(true);
    }
}

// Helper method to count hoppers in region
    private int countHoppersInRegion(BlockVector3 min, BlockVector3 max, World world) {
    int count = 0;
    for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
        for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                Block block = world.getBlockAt(x, y, z);
                if (block.getType() == Material.HOPPER) {
                    count++;
                }
            }
        }
    }
    return count;
}

    public static double getCellSizeUpgradeCost(int newLevel) {
        return switch (newLevel) {
            case 1 -> 1_000_000;
            case 2 -> 5_000_000;
            case 3 -> 10_000_000;
            case 4 -> 25_000_000;
            case 5 -> 50_000_000;
            default -> 1_000;
        };
    }
    
    public static double getFarmUpgradeCost(int level) {
    return switch (level) {
        case 1 -> 10_000_000;
        case 2 -> 25_000_000;
        case 3 -> 50_000_000;
        case 4 -> 100_000_000;
        case 5 -> 200_000_000;
        default -> Double.MAX_VALUE;
    };
}

}
