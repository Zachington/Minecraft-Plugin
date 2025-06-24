package customEnchants.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.inventory.ItemStack;

import customEnchants.managers.CellManager;
import customEnchants.utils.CellUtil;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class FarmUpgradeListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onCropGrow(BlockGrowEvent event) {  
    Block block = event.getBlock();
    Material type = block.getType();

    if (!isCrop(type)) {
        return;
    }

    UUID ownerId = CellManager.getCellOwnerForLocation(block.getLocation());
    if (ownerId == null) {
        return;
    }

    int farmLevel = CellUtil.getFarmUpgradeLevel(ownerId);
    if (farmLevel <= 0) {
        return;
    }

    double multiplier = 1 + 0.25 * farmLevel;

    BlockState state = block.getState();
    if (!(state.getBlockData() instanceof Ageable ageable)) {
        return;
    }

    int currentAge = ageable.getAge();
    int maxAge = ageable.getMaximumAge();

    if (currentAge >= maxAge) {
        return;
    }

    // Calculate how many stages to grow this tick
    int guaranteedStages = (int) multiplier;                // guaranteed growth stages
    double fractionalChance = multiplier - guaranteedStages; // chance for one more stage

    int stagesToGrow = guaranteedStages;
    if (random.nextDouble() < fractionalChance) {
        stagesToGrow++;
    }

    int newAge = Math.min(currentAge + stagesToGrow, maxAge);

    if (newAge > currentAge) {
        ageable.setAge(newAge);
        state.setBlockData(ageable);
        state.update(true);
    }
}

    @EventHandler
public void onCropBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    Material type = block.getType();

    if (!isCrop(type)) {
        return;
    }

    BlockState state = block.getState();
    if (!(state.getBlockData() instanceof Ageable ageable)) {
        return;
    }

    int currentAge = ageable.getAge();
    int maxAge = ageable.getMaximumAge();

    // Only multiply drops if fully grown
    if (currentAge < maxAge) {
        return;
    }

    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();

    UUID ownerId = CellManager.getCellOwnerForLocation(block.getLocation());
    if (ownerId == null) return;

    List<UUID> members = CellUtil.getMembers(ownerId);
    if (!ownerId.equals(playerId) && !members.contains(playerId)) {
        return;
    }

    int farmLevel = CellUtil.getFarmUpgradeLevel(ownerId);
    if (farmLevel <= 0) return;

    double multiplier = 1.0 + 0.25 * farmLevel;

    event.setDropItems(false);

    Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());

    for (ItemStack drop : drops) {
        int originalAmount = drop.getAmount();

        int extraAmount = (int) Math.floor(originalAmount * (multiplier - 1));
        double fractional = (originalAmount * (multiplier - 1)) - extraAmount;
        if (random.nextDouble() < fractional) {
            extraAmount++;
        }

        drop.setAmount(originalAmount + extraAmount);
        block.getWorld().dropItemNaturally(block.getLocation(), drop);
    }
}

    private boolean isCrop(Material mat) {
        // Add all crops you want to support here
        return switch (mat) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART, SWEET_BERRY_BUSH, COCOA, GLOW_BERRIES -> true;
            default -> false;
        };
    }
}
