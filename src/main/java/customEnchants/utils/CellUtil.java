package customEnchants.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CellUtil {

    // Stores player upgrade data in memory (replace with persistent storage if needed)
    public static final Map<UUID, PlayerCellData> cellDataMap = new HashMap<>();

    public static class PlayerCellData {
    private int cellSizeLevel = 1;
    private int memberCapacityLevel = 1;
    private int hopperLimitLevel = 1;
    private int farmUpgradeLevel = 1;
    private final List<UUID> members = new ArrayList<>();

    public int getCellSizeLevel() { return cellSizeLevel; }
    public void setCellSizeLevel(int level) { this.cellSizeLevel = level; }

    public int getMemberCapacityLevel() { return memberCapacityLevel; }
    public void setMemberCapacityLevel(int level) { this.memberCapacityLevel = level; }

    public int getHopperLimitLevel() { return hopperLimitLevel; }
    public void setHopperLimitLevel(int level) { this.hopperLimitLevel = level; }

    public int getFarmUpgradeLevel() { return farmUpgradeLevel; }
    public void setFarmUpgradeLevel(int level) { this.farmUpgradeLevel = level; }

    public List<UUID> getMembers() {
        return members;
    }
}

    public static List<UUID> getMembers(UUID ownerId) {
    PlayerCellData data = getPlayerCellData(ownerId);
    return new ArrayList<>(data.getMembers()); // return a copy to prevent outside modification
}

    public static PlayerCellData getPlayerCellData(UUID playerId) {
        return cellDataMap.computeIfAbsent(playerId, k -> new PlayerCellData());
    }

    // Convenience methods for each upgrade:
    public static int getCellSizeLevel(UUID playerId) {
        return getPlayerCellData(playerId).getCellSizeLevel();
    }

    public static void setCellSizeLevel(UUID playerId, int level) {
        getPlayerCellData(playerId).setCellSizeLevel(level);
    }

    public static int getMemberCapacityLevel(UUID playerId) {
        return getPlayerCellData(playerId).getMemberCapacityLevel();
    }

    public static void setMemberCapacityLevel(UUID playerId, int level) {
        getPlayerCellData(playerId).setMemberCapacityLevel(level);
    }

    public static int getHopperLimitLevel(UUID playerId) {
        return getPlayerCellData(playerId).getHopperLimitLevel();
    }

    public static void setHopperLimitLevel(UUID playerId, int level) {
        getPlayerCellData(playerId).setHopperLimitLevel(level);
    }

    public static int getFarmUpgradeLevel(UUID playerId) {
        return getPlayerCellData(playerId).getFarmUpgradeLevel();
    }

    public static void setFarmUpgradeLevel(UUID playerId, int level) {
        getPlayerCellData(playerId).setFarmUpgradeLevel(level);
    }

    public static void clearPlayerCellData(UUID playerId) {
    cellDataMap.remove(playerId);
}

    public static int getMaxMembersForLevel(int level) {
    return switch (level) {
        case 0 -> 1;
        case 1 -> 2;
        case 2 -> 3;
        case 3 -> 4;
        default -> 1;
    };
}

    public static int getHopperLimitForLevel(int level) {
    return switch (level) {
        case 0 -> 25;
        case 1 -> 75;
        case 2 -> 125;
        case 3 -> 175;
        case 4 -> 200;
        default -> 25; // default fallback
    };
}

}
