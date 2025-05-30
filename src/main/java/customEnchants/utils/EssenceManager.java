package customEnchants.utils;


import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EssenceManager {
    private final NamespacedKey[] tierKeys = new NamespacedKey[8];
    private final Map<UUID, ItemStack[]> extractorStorage = new HashMap<>();
    private final int[] centerSlots = {12, 13, 14, 21, 22, 23, 30, 31, 32};
    private final JavaPlugin plugin;

    public EssenceManager(JavaPlugin plugin) {
        this.plugin = plugin;
        for (int i = 0; i < 8; i++) {
            tierKeys[i] = new NamespacedKey(plugin, "essence_tier_" + (i + 1));
        }
    }

    // --- Essence management ---

    public void addEssence(Player player, int tier, int amount) {
        if (tier < 1 || tier > 8 || amount <= 0) return;

        PersistentDataContainer data = player.getPersistentDataContainer();
        NamespacedKey key = tierKeys[tier - 1];
        int current = data.getOrDefault(key, PersistentDataType.INTEGER, 0);
        data.set(key, PersistentDataType.INTEGER, current + amount);
    }

    public int getEssence(Player player, int tier) {
        if (tier < 1 || tier > 8) return 0;

        PersistentDataContainer data = player.getPersistentDataContainer();
        NamespacedKey key = tierKeys[tier - 1];
        return data.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public void removeEssence(Player player, int tier, int amount) {
        if (tier < 1 || tier > 8 || amount <= 0) return;

        PersistentDataContainer data = player.getPersistentDataContainer();
        NamespacedKey key = tierKeys[tier - 1];
        int current = getEssence(player, tier);
        int newAmount = Math.max(0, current - amount);
        data.set(key, PersistentDataType.INTEGER, newAmount);
    }

    // --- GUI item storage per player ---

    public void saveExtractorInventory(Player player, Inventory gui) {
        ItemStack[] stored = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            stored[i] = gui.getItem(centerSlots[i]);
        }
        extractorStorage.put(player.getUniqueId(), stored);

        saveExtractorInventoryToFile(player, stored);
    }

    public void loadExtractorInventory(Player player) {
    ItemStack[] stored = extractorStorage.get(player.getUniqueId());
    if (stored == null) {
        stored = loadExtractorInventoryFromFile(player);
        if (stored != null) {
            extractorStorage.put(player.getUniqueId(), stored);
        }
    }
}

    public void loadExtractorInventory(Player player, Inventory gui) {
    loadExtractorInventory(player); // loads from memory or file to cache
    ItemStack[] stored = extractorStorage.get(player.getUniqueId());
    if (stored != null && gui != null) {
        for (int i = 0; i < 9; i++) {
            gui.setItem(centerSlots[i], stored[i]);
        }
    }
}

    public void clearExtractorInventory(Player player) {
        extractorStorage.remove(player.getUniqueId());

        // Also delete the player's file on clear
        File playerFile = getPlayerFile(player);
        if (playerFile.exists()) {
            playerFile.delete();
        }
    }

    public ItemStack[] getStoredExtractorContents(Player player) {
        return extractorStorage.getOrDefault(player.getUniqueId(), new ItemStack[9]);
    }

    // --- Persistence helpers ---

    private void saveExtractorInventoryToFile(Player player, ItemStack[] stored) {
        File playerFile = getPlayerFile(player);
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        for (int i = 0; i < 9; i++) {
            config.set("extractors.slot" + i, stored[i]);
        }
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save extractor inventory for player " + player.getName());
            e.printStackTrace();
        }
    }

    private ItemStack[] loadExtractorInventoryFromFile(Player player) {
        File playerFile = getPlayerFile(player);
        if (!playerFile.exists()) return null;

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        ItemStack[] stored = new ItemStack[9];

        for (int i = 0; i < 9; i++) {
            stored[i] = config.getItemStack("extractors.slot" + i);
        }

        return stored;
    }

    private File getPlayerFile(Player player) {
        File folder = new File(plugin.getDataFolder(), "extractors");
        if (!folder.exists()) folder.mkdirs();
        return new File(folder, player.getUniqueId().toString() + ".yml");
    }

    public void preloadExtractorInventory(Player player) {
    ItemStack[] stored = loadExtractorInventoryFromFile(player);
    if (stored != null) {
        extractorStorage.put(player.getUniqueId(), stored);
    }
}
}
