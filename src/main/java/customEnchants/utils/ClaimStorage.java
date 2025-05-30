package customEnchants.utils;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimStorage {
    private static final Map<UUID, Map<String, Integer>> playerKeys = new HashMap<>();

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

