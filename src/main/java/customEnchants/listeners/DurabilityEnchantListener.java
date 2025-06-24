package customEnchants.listeners;

import customEnchants.utils.EnchantFunctionUtil;
import customEnchants.utils.EnchantmentData;
import customEnchants.utils.HeldToolInfo;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class DurabilityEnchantListener implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
    ItemStack tool = event.getItem();
    if (tool == null || !tool.hasItemMeta()) return;
    Player player = event.getPlayer();
    

    HeldToolInfo info = HeldToolInfo.fromItem(tool);
    ItemMeta meta = tool.getItemMeta();
    if (!(meta instanceof Damageable damageable)) return;

    int currentDamage = damageable.getDamage();
    boolean modified = false;

    if (EnchantFunctionUtil.pureGreedActive(player)) {
        return;
    }

    // Unbreakable
    int unbreakableLevel = info.getLevel("Unbreakable");
    if (unbreakableLevel > 0) {
        int index = EnchantmentData.getEnchantmentIndex("Unbreakable");
        if (index >= 0 && index < EnchantmentData.ENCHANT_PROC_CHANCE.length) {
            double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[index];
            if (random.nextDouble() < baseChance * unbreakableLevel) {
                damageable.setDamage(Math.max(0, currentDamage - 100));
                modified = true;
            }
        }
    }

    // Preservation
    int preservationLevel = info.getLevel("Preservation");
    if (preservationLevel > 0) {
        int index = EnchantmentData.getEnchantmentIndex("Preservation");
        if (index >= 0 && index < EnchantmentData.ENCHANT_PROC_CHANCE.length) {
            double chance = EnchantmentData.ENCHANT_PROC_CHANCE[index];
            if (random.nextDouble() < chance) {
                damageable.setDamage(Math.max(0, currentDamage - (3 * preservationLevel)));
                modified = true;
            }
        }
    }

    // Miner’s Instinct – reduce durability loss when tool is under 20% durability
    int instinctLevel = info.getLevel("Miners Instinct");
    if (instinctLevel > 0) {
        int maxDurability = tool.getType().getMaxDurability();
        int threshold = (int) (maxDurability * 0.2);

        if (currentDamage >= maxDurability - threshold) {
            int index = EnchantmentData.getEnchantmentIndex("Miners Instinct");
            if (index >= 0 && index < EnchantmentData.ENCHANT_PROC_CHANCE.length) {
                double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[index];
                if (random.nextDouble() < baseChance * instinctLevel) {
                    damageable.setDamage(Math.max(0, currentDamage - 1));
                    modified = true;
                }
            }
        }
    }

    if (modified) {
        tool.setItemMeta(meta);
        event.setCancelled(true); // cancel the damage event if durability was restored
    }
}
}