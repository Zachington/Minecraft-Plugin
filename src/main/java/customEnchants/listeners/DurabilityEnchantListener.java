package customEnchants.listeners;

import customEnchants.utils.EnchantmentData;
import customEnchants.utils.HeldToolInfo;
import org.bukkit.event.Listener;
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

        HeldToolInfo info = HeldToolInfo.fromItem(tool);
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return;

        int currentDamage = damageable.getDamage();
        boolean modified = false;

        // Unbreakable
        int unbreakableLevel = info.getLevel("Unbreakable");
        if (unbreakableLevel > 0) {
            int index = EnchantmentData.getEnchantmentIndex("Unbreakable");
            if (index >= 0 && index < EnchantmentData.ENCHANT_PROC_CHANCE.length) {
                double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[index];
                int amplifyLevel = info.getLevel("Amplify");
                double amplifyBonus = amplifyLevel * 0.05;
                double chance = Math.min(baseChance * unbreakableLevel + amplifyBonus, 1.0);

                if (random.nextDouble() < chance) {
                    damageable.setDamage(Math.max(0, currentDamage - 100));  //Durability number
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
                    damageable.setDamage(Math.max(0, damageable.getDamage() - (3 * preservationLevel)));    //Durability Number
                    modified = true;
                }
            }
        }

        if (modified) {
            tool.setItemMeta(meta);
            event.setCancelled(true); // cancel the damage event if durability is restored
        }
    }
}
