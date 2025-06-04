package customEnchants.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimStorage {
    private static final Map<UUID, Map<String, Integer>> playerKeys = new HashMap<>();
    private static File file;
    private static FileConfiguration config;

    public static void init(File pluginFolder) {
        file = new File(pluginFolder, "claim_keys.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Could not create claim_keys.yml");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadKeys();
    }

    private static void loadKeys() {
        for (String uuidStr : config.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Map<String, Integer> keys = new HashMap<>();
            for (String keyType : config.getConfigurationSection(uuidStr).getKeys(false)) {
                keys.put(keyType.toLowerCase(), config.getInt(uuidStr + "." + keyType));
            }
            playerKeys.put(uuid, keys);
        }
    }

    public static void saveKeys() {
        for (Map.Entry<UUID, Map<String, Integer>> entry : playerKeys.entrySet()) {
            String uuid = entry.getKey().toString();
            for (Map.Entry<String, Integer> keyEntry : entry.getValue().entrySet()) {
                config.set(uuid + "." + keyEntry.getKey(), keyEntry.getValue());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save claim_keys.yml");
        }
    }

    public static int getKeyCount(Player player, String keyType) {
        return playerKeys
            .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .getOrDefault(keyType.toLowerCase(), 0);
    }

    public static void addKeys(Player player, String keyType, int amount) {
        Map<String, Integer> keys = playerKeys.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        keys.put(keyType.toLowerCase(), getKeyCount(player, keyType) + amount);
    }

    public static void removeKeys(Player player, String keyType, int amount) {
        addKeys(player, keyType, -amount);
    }
}
