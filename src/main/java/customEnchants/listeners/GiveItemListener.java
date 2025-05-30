package customEnchants.listeners;

import customEnchants.utils.GiveItem.EnchantmentDropData;
import customEnchants.utils.GiveItem.ItemDropEntry;
import customEnchants.utils.ClaimStorage;
import customEnchants.utils.EnchantmentData;
import customEnchants.utils.HeldToolInfo;
import customEnchants.utils.VaultUtil;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Random;

public class GiveItemListener implements Listener {


    @EventHandler
public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    ItemStack tool = player.getInventory().getItemInMainHand();
    HeldToolInfo heldToolInfo = HeldToolInfo.fromItem(tool);
    Random random = new Random();

    // --- Key Miner ---
    int keyMinerLevel = heldToolInfo.getLevel("Key Miner");
    if (keyMinerLevel > 0) {
        int keyMinerIndex = EnchantmentData.getEnchantmentIndex("Key Miner");
        if (keyMinerIndex >= 0 && keyMinerIndex < EnchantmentData.ENCHANT_PROC_CHANCE.length) {
            double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[keyMinerIndex];
            double amplifyBonus = getAmplifyLevel(tool) * 0.05;
            double chance = Math.min(baseChance * keyMinerLevel + amplifyBonus, 1.0);

            if (random.nextDouble() < chance) {
            List<ItemDropEntry> drops = EnchantmentDropData.ENCHANT_DROP_MAP.get("Key Miner");
            if (drops != null && !drops.isEmpty()) {
        // Calculate total weight (sum of baseChancePerLevel * level)
        double totalWeight = 0;
        for (ItemDropEntry entry : drops) {
            totalWeight += Math.min(entry.baseChancePerLevel * keyMinerLevel, 1.0);
        }

        // Random pick weighted by drop chances
        double r = random.nextDouble() * totalWeight;
        double cumulative = 0;
        for (ItemDropEntry entry : drops) {
            double weight = Math.min(entry.baseChancePerLevel * keyMinerLevel, 1.0);
            cumulative += weight;
            if (r <= cumulative) {
                //Send to claim menu
                String keyName = ChatColor.stripColor(entry.item.getItemMeta().getDisplayName());
                ClaimStorage.addKeys(player, keyName, 1);
                player.sendMessage(ChatColor.GREEN + "You received a " + keyName + "!");
                break;
            }
        }
    }
}

        }
    }

    // --- Gold Digger ---
    int goldDiggerLevel = heldToolInfo.getLevel("Gold Digger");
if (goldDiggerLevel > 0) {
    int goldDiggerIndex = EnchantmentData.getEnchantmentIndex("Gold Digger");
    if (goldDiggerIndex >= 0 && goldDiggerIndex < EnchantmentData.ENCHANT_PROC_CHANCE.length) {
        double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[goldDiggerIndex];
        double chance = Math.min(baseChance * goldDiggerLevel, 1.0);

        if (random.nextDouble() < chance) {
            double money = 500 + Math.random() * 1000; // 500–1500, add * prestige later
            VaultUtil.giveMoney(player, money);
            player.sendMessage(ChatColor.GOLD + "You found $" + String.format("%.2f", money) + " from Gold Digger!");
        }
    }
}
}


    private int getAmplifyLevel(ItemStack tool) {
        HeldToolInfo heldInfo = HeldToolInfo.fromItem(tool);
        return heldInfo.getLevel("Amplify"); // or refine this logic if Amplify is stored differently
    }
}
