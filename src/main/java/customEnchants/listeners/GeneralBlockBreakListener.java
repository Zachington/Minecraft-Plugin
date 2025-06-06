package customEnchants.listeners;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import customEnchants.utils.EnchantmentData;
import customEnchants.utils.RankUtils;
import customEnchants.utils.StatTracker;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import customEnchants.utils.HeldToolInfo;

public class GeneralBlockBreakListener implements Listener {
    private final StatTracker stats;
    private final JavaPlugin plugin;

    public GeneralBlockBreakListener(StatTracker stats, JavaPlugin plugin) {
        this.stats = stats;
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String worldName = event.getBlock().getWorld().getName();
        if (!worldName.equals("world") && !worldName.equals("mine_world")) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                NamespacedKey key = new NamespacedKey(plugin, "owner_uuid");
                PersistentDataContainer container = meta.getPersistentDataContainer();

                if (container.has(key, PersistentDataType.STRING)) {
                    String ownerUUID = container.get(key, PersistentDataType.STRING);
                    if (!player.getUniqueId().toString().equals(ownerUUID)) {
                        player.sendMessage(ChatColor.RED + "You do not own this pickaxe!");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        HeldToolInfo tool = HeldToolInfo.fromItem(item);
        for (String enchant : tool.customEnchants.keySet()) {
            String rarity = EnchantmentData.getRarity(enchant);
    if ("PRESTIGE".equalsIgnoreCase(rarity) && !RankUtils.isAtLeastP1a(player)) {
        player.sendMessage(ChatColor.RED + "You must be Prestige I (p1a) to use tools with " + enchant + "!");
        event.setCancelled(true);
        return;
    }
    if ("PRESTIGE+".equalsIgnoreCase(rarity) && !RankUtils.isAtLeastP10a(player)) {
        player.sendMessage(ChatColor.RED + "You must be Prestige X (p10a) to use tools with " + enchant + "!");
        event.setCancelled(true);
        return;
    }
}



        UUID uuid = player.getUniqueId();
        stats.incrementPlayerStat(uuid, "blocks_broken");
        stats.incrementServerStat("blocks_broken");
        stats.save();
    }
}
