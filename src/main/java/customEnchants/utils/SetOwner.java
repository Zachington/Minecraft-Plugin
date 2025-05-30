package customEnchants.utils;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class SetOwner {

    private final JavaPlugin plugin;

    public SetOwner(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setOwner(ItemStack item, Player player) {
    if (item == null || !item.hasItemMeta()) return;

    ItemMeta meta = item.getItemMeta();
    if (meta == null) return;

    // Update or insert owner line
    List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

    lore.removeIf(line -> ChatColor.stripColor(line).startsWith("Owner:"));
    lore.add(ChatColor.GRAY + "Owner: " + player.getName());

    meta.setLore(lore);

    NamespacedKey key = new NamespacedKey(plugin, "owner_uuid");
    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, player.getUniqueId().toString());

    item.setItemMeta(meta);
}

}
