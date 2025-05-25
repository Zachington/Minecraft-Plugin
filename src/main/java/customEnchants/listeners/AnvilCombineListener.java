package customEnchants.listeners;

import customEnchants.utils.EnchantmentData;
import customEnchants.utils.GuiUtil;
import customEnchants.utils.GuiUtil.EnchantParseResult;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilCombineListener implements Listener {

    @EventHandler
    public void onAnvilCombine(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);

        if (first == null || second == null) return;
        if (first.getType() != Material.ENCHANTED_BOOK || second.getType() != Material.ENCHANTED_BOOK) return;

        EnchantParseResult result1 = GuiUtil.parseCustomEnchantBook(first);
        EnchantParseResult result2 = GuiUtil.parseCustomEnchantBook(second);
        if (result1 == null || result2 == null) return;

        // Must be same name and level, and below max level
        if (!result1.name.equals(result2.name)) return;
        if (result1.level != result2.level) return;

        int enchantIndex = EnchantmentData.getEnchantmentIndex(result1.name);
        if (enchantIndex == -1) return;

        int maxLevel = EnchantmentData.getEnchantmentInfo(enchantIndex).maxLevel;
        if (result1.level >= maxLevel) return;

        // Combine to next level, average the success chance
        int newLevel = result1.level + 1;
        double newChance = (result1.chance + result2.chance) / 2.0;

        ItemStack combined = GuiUtil.createCustomEnchantBook(result1.name, newLevel, newChance);
        event.setResult(combined);
        inv.setRepairCost(1);
    }
}
