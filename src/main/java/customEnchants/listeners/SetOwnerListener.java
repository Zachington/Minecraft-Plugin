package customEnchants.listeners;


import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import customEnchants.utils.SetOwner;

public class SetOwnerListener implements Listener {

    private final JavaPlugin plugin;
    private final SetOwner setOwnerUtil;

    public SetOwnerListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.setOwnerUtil = new SetOwner(plugin);
    }

    // Utility method to check if material is a pickaxe
    private boolean isPickaxe(Material mat) {
        return mat == Material.WOODEN_PICKAXE ||
            mat == Material.STONE_PICKAXE ||
            mat == Material.IRON_PICKAXE ||
            mat == Material.GOLDEN_PICKAXE ||
            mat == Material.DIAMOND_PICKAXE ||
            mat == Material.NETHERITE_PICKAXE;
    }

    @EventHandler
public void onCraftItem(CraftItemEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;

    if (event.getCurrentItem() == null || !isPickaxe(event.getCurrentItem().getType())) return;

    // Delay by 1 tick to ensure the crafted item is in the inventory
    plugin.getServer().getScheduler().runTask(plugin, () -> {
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && isPickaxe(cursor.getType())) {
            setOwnerUtil.setOwner(cursor, player);
        }
    });
}

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result == null || !isPickaxe(result.getType())) return;

        HumanEntity human = event.getView().getPlayer();
        if (!(human instanceof Player player)) return;

        ItemStack modifiedResult = result.clone();
        setOwnerUtil.setOwner(modifiedResult, player);
        event.setResult(modifiedResult);
    }
}
