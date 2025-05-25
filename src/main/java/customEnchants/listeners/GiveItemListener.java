package customEnchants.listeners;

import customEnchants.utils.GiveItem.EnchantmentDropData;
import customEnchants.utils.GiveItem.ItemDropEntry;
import customEnchants.utils.EnchantmentData;
import customEnchants.utils.HeldToolInfo;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class GiveItemListener implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        HeldToolInfo heldToolInfo = HeldToolInfo.fromItem(tool);
        int level = heldToolInfo.getLevel("Gold Digger");
        if (level <= 0) return;

        int index = EnchantmentData.getEnchantmentIndex("Gold Digger");
        if (index < 0 || index >= EnchantmentData.ENCHANT_PROC_CHANCE.length) return;

        double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[index];
        int amplifyLevel = getAmplifyLevel(tool, "Gold Digger"); // You implement this
        double amplifyBonus = amplifyLevel * 0.05;

        double chance = Math.min(baseChance * level + amplifyBonus, 1.0);

    if (random.nextDouble() < chance) {
        List<ItemDropEntry> drops = EnchantmentDropData.ENCHANT_DROP_MAP.get("Gold Digger");
        if (drops != null) {
        for (ItemDropEntry entry : drops) {
            // roll drop chance per entry and drop items
            double dropChance = Math.min(entry.baseChancePerLevel * level, 1.0);
            if (random.nextDouble() < dropChance) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), entry.item.clone());
                }
            }
        }
    }
}

    private int getAmplifyLevel(ItemStack tool, String enchantName) {
        HeldToolInfo heldInfo = HeldToolInfo.fromItem(tool);
        return heldInfo.getLevel("Amplify"); // or refine this logic if Amplify is stored differently
    }
}
