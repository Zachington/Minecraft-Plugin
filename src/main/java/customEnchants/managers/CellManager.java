package customEnchants.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import customEnchants.utils.CellUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CellManager {

    public static class CellData {
        public String regionId;
        public int baseCellId;      // grid position for chunk alignment
        public int openSpaceLevel;  // from 0 to 5 controlling open space size

        public CellData(String regionId, int baseCellId, int openSpaceLevel) {
            this.regionId = regionId;
            this.baseCellId = baseCellId;
            this.openSpaceLevel = openSpaceLevel;
        }
    }

    private static final int CHUNK_SIZE = 16;
    private static final int CELL_HEIGHT = 64;
    private static final int MAX_CELL_GRID_WIDTH = 100;   // cells per row in grid
    private static final int CELL_SPACING_CHUNKS = 5;   // chunks between cells

    private static final int MAX_CELL_CHUNKS = 3;       // 3x3 chunks max cell size
    private static final int MAX_CELL_WIDTH_BLOCKS = MAX_CELL_CHUNKS * CHUNK_SIZE + 2;   // 48 blocks wide/deep
    private static final int MAX_CELL_DEPTH_BLOCKS = MAX_CELL_WIDTH_BLOCKS;
    private static final int MAX_CELL_HEIGHT_BLOCKS = CELL_HEIGHT + 2; 
    private static File dataFile;
    private static YamlConfiguration dataConfig;                  
    private static int nextCellId = 0;
    private static final Map<UUID, CellData> playerCells = new HashMap<>();

    public static boolean hasCell(UUID playerId) {
        return playerCells.containsKey(playerId);
    }

    private static void buildCellStructure(World world, int minX, int minY, int minZ, int openSpaceLevel, boolean clearInside) {
    int maxX = minX + MAX_CELL_WIDTH_BLOCKS - 1;
    int maxY = minY + MAX_CELL_HEIGHT_BLOCKS - 1;
    int maxZ = minZ + MAX_CELL_DEPTH_BLOCKS - 1;

    int openWidth = 5 + openSpaceLevel * (MAX_CELL_WIDTH_BLOCKS - 5) / 5 + 1;
    int openHeight = 5 + openSpaceLevel * (MAX_CELL_HEIGHT_BLOCKS - 5) / 5 + 1;

    openWidth = Math.min(openWidth, MAX_CELL_WIDTH_BLOCKS - 2);
    openHeight = Math.min(openHeight, MAX_CELL_HEIGHT_BLOCKS - 2);

    int openMinX = minX + (MAX_CELL_WIDTH_BLOCKS - openWidth) / 2;
    int openMinY = minY + 1;
    int openMinZ = minZ + (MAX_CELL_DEPTH_BLOCKS - openWidth) / 2;

    int openMaxX = openMinX + openWidth - 1;
    int openMaxY = openMinY + openHeight - 1;
    int openMaxZ = openMinZ + openWidth - 1;

    for (int x = minX; x <= maxX; x++) {
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                Block block = world.getBlockAt(x, y, z);

                boolean isWall = (x == minX || x == maxX) || (y == minY || y == maxY) || (z == minZ || z == maxZ);
                boolean isInsideOpenSpace = (x >= openMinX && x <= openMaxX) &&
                                            (y >= openMinY && y <= openMaxY) &&
                                            (z >= openMinZ && z <= openMaxZ);

                if (isWall) {
                    // Always place bedrock walls/boundaries
                    block.setType(Material.BEDROCK, false);
                } else if (!isInsideOpenSpace) {
                    // Outside open space but inside cell, also bedrock boundary
                    block.setType(Material.BEDROCK, false);
                } else {
                    // Inside open space
                    if (clearInside) {
                        // Clear all inside blocks
                        block.setType(Material.AIR, false);
                    } else {
                        // Only clear existing bedrock blocks (old walls) inside open space
                        if (block.getType() == Material.BEDROCK) {
                            block.setType(Material.AIR, false);
                        }
                        // Otherwise, leave player-built blocks untouched
                    }
                }
            }
        }
    }
}

    public static boolean createPlayerCell(Player player) {
        UUID playerId = player.getUniqueId();

        if (hasCell(playerId)) {
            player.sendMessage(ChatColor.RED + "You already own a cell.");
            return false;
        }

        World world = Bukkit.getWorld("cell_world");
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Cell world does not exist.");
            return false;
        }

        int openSpaceLevel = CellUtil.getCellSizeLevel(playerId);
        if (openSpaceLevel < 0) openSpaceLevel = 0;  // level 0 = smallest

        // Calculate grid position
        int cellGridX = nextCellId % MAX_CELL_GRID_WIDTH;
        int cellGridZ = nextCellId / MAX_CELL_GRID_WIDTH;
        nextCellId++;

        int minX = cellGridX * (MAX_CELL_CHUNKS + CELL_SPACING_CHUNKS) * CHUNK_SIZE - 1;
        int minZ = cellGridZ * (MAX_CELL_CHUNKS + CELL_SPACING_CHUNKS) * CHUNK_SIZE - 1;
        int minY = 0;

        // Build the initial cell structure
        buildCellStructure(world, minX, minY, minZ, openSpaceLevel, true);

        // Create WorldGuard region for full max cell size (3x3 chunks)
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            player.sendMessage(ChatColor.RED + "Region manager not found.");
            return false;
        }

        String regionId = "cell_" + playerId.toString().replace("-", "");
        BlockVector3 min = BlockVector3.at(minX, minY, minZ);
        BlockVector3 max = BlockVector3.at(minX + MAX_CELL_WIDTH_BLOCKS - 1, minY + MAX_CELL_HEIGHT_BLOCKS - 1, minZ + MAX_CELL_DEPTH_BLOCKS - 1);

        if (regionManager.hasRegion(regionId)) {
            player.sendMessage(ChatColor.RED + "You already have a cell region.");
            return false;
        }

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, min, max);
        region.getOwners().addPlayer(playerId);
        region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);
        region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.ALLOW);

        regionManager.addRegion(region);

        // Save cell data
        CellData data = new CellData(regionId, nextCellId - 1, openSpaceLevel);
        playerCells.put(playerId, data);

        // Teleport player to center of open space
        double tpX = minX + (MAX_CELL_WIDTH_BLOCKS - (5 + openSpaceLevel * (MAX_CELL_WIDTH_BLOCKS - 5) / 5)) / 2.0 + 
                     (5 + openSpaceLevel * (MAX_CELL_WIDTH_BLOCKS - 5) / 5) / 2.0;
        double tpY = minY + 1;
        double tpZ = minZ + (MAX_CELL_DEPTH_BLOCKS - (5 + openSpaceLevel * (MAX_CELL_DEPTH_BLOCKS - 5) / 5)) / 2.0 +
                     (5 + openSpaceLevel * (MAX_CELL_DEPTH_BLOCKS - 5) / 5) / 2.0;

        player.teleport(new Location(world, tpX, tpY, tpZ));
        player.sendMessage(ChatColor.GREEN + "Your cell has been created!");

        return true;
    }

    public static boolean upgradeCellSize(Player player) {
        UUID playerId = player.getUniqueId();

        if (!hasCell(playerId)) {
            player.sendMessage(ChatColor.RED + "You do not own a cell to upgrade.");
            return false;
        }

        int currentLevel = CellUtil.getCellSizeLevel(playerId);
        int maxLevel = 5;

        if (currentLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Your cell is already at maximum size.");
            return false;
        }

        int newLevel = currentLevel + 1;
        CellUtil.setCellSizeLevel(playerId, newLevel);

        CellData cellData = playerCells.get(playerId);

        World world = Bukkit.getWorld("cell_world");
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Cell world does not exist.");
            return false;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            player.sendMessage(ChatColor.RED + "Region manager not found.");
            return false;
        }

        // Calculate cell grid position from baseCellId
        int cellGridX = cellData.baseCellId % MAX_CELL_GRID_WIDTH;
        int cellGridZ = cellData.baseCellId / MAX_CELL_GRID_WIDTH;

        int alignedChunkX = cellGridX * (MAX_CELL_CHUNKS + CELL_SPACING_CHUNKS);
        int alignedChunkZ = cellGridZ * (MAX_CELL_CHUNKS + CELL_SPACING_CHUNKS);

        int minX = alignedChunkX * CHUNK_SIZE - 1;
        int minZ = alignedChunkZ * CHUNK_SIZE - 1;
        int minY = 0;

        // Remove old region
        regionManager.removeRegion(cellData.regionId);

        // Rebuild cell with new open space size
        buildCellStructure(world, minX, minY, minZ, newLevel, false);

        // Re-add region (same max size)
        BlockVector3 min = BlockVector3.at(minX, minY, minZ);
        BlockVector3 max = BlockVector3.at(minX + MAX_CELL_WIDTH_BLOCKS - 1, minY + MAX_CELL_HEIGHT_BLOCKS - 1, minZ + MAX_CELL_DEPTH_BLOCKS - 1);

        ProtectedCuboidRegion newRegion = new ProtectedCuboidRegion(cellData.regionId, min, max);
        newRegion.getOwners().addPlayer(playerId);
        newRegion.setFlag(Flags.BUILD, StateFlag.State.ALLOW);
        newRegion.setFlag(Flags.CHEST_ACCESS, StateFlag.State.ALLOW);

        regionManager.addRegion(newRegion);

        // Update stored level
        cellData.openSpaceLevel = newLevel;

        player.sendMessage(ChatColor.GREEN + "Your cell size has been upgraded to level " + newLevel + "!");

        // Teleport to new open space center
        double tpX = minX + (MAX_CELL_WIDTH_BLOCKS - (5 + newLevel * (MAX_CELL_WIDTH_BLOCKS - 5) / 5)) / 2.0 + 
                     (5 + newLevel * (MAX_CELL_WIDTH_BLOCKS - 5) / 5) / 2.0;
        double tpY = minY + 1;
        double tpZ = minZ + (MAX_CELL_DEPTH_BLOCKS - (5 + newLevel * (MAX_CELL_DEPTH_BLOCKS - 5) / 5)) / 2.0 +
                     (5 + newLevel * (MAX_CELL_DEPTH_BLOCKS - 5) / 5) / 2.0;

        player.teleport(new Location(world, tpX, tpY, tpZ));

        return true;
    }

    public static void loadCellData(JavaPlugin plugin) {
    dataFile = new File(plugin.getDataFolder(), "celldata.yml");
    if (!dataFile.exists()) {
        try {
            plugin.getDataFolder().mkdirs();
            dataFile.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create celldata.yml");
            e.printStackTrace();
            return;
        }
    }

    dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    playerCells.clear(); // Clear current cache before loading

    for (String key : dataConfig.getKeys(false)) {
        try {
            UUID playerId = UUID.fromString(key);

            // Load CellManager CellData
            String regionId = dataConfig.getString(key + ".regionId");
            int baseCellId = dataConfig.getInt(key + ".baseCellId", -1);
            int openSpaceLevel = dataConfig.getInt(key + ".openSpaceLevel", 0);

            if (regionId == null || baseCellId == -1) {
                plugin.getLogger().warning("Missing cell data fields for player: " + playerId);
                continue;
            }

            // Rebuild playerCells map
            CellData cellData = new CellData(regionId, baseCellId, openSpaceLevel);
            playerCells.put(playerId, cellData);

            // Load CellUtil.PlayerCellData fields
            int cellSizeLevel = dataConfig.getInt(key + ".cellSizeLevel", 1);
            int memberCapacityLevel = dataConfig.getInt(key + ".memberCapacityLevel", 1);
            int hopperLimitLevel = dataConfig.getInt(key + ".hopperLimitLevel", 1);
            int farmUpgradeLevel = dataConfig.getInt(key + ".farmUpgradeLevel", 1);

            CellUtil.PlayerCellData pcd = CellUtil.getPlayerCellData(playerId);
            pcd.setCellSizeLevel(cellSizeLevel);
            pcd.setMemberCapacityLevel(memberCapacityLevel);
            pcd.setHopperLimitLevel(hopperLimitLevel);
            pcd.setFarmUpgradeLevel(farmUpgradeLevel);

        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Invalid UUID in celldata.yml: " + key);
        }
    }

    plugin.getLogger().info("Cell data loaded.");
}

    public static void saveCellData(JavaPlugin plugin) {
    if (dataConfig == null || dataFile == null) {
        plugin.getLogger().warning("Data file or config not initialized, skipping save.");
        return;
    }

    // Clear old data
    for (String key : dataConfig.getKeys(false)) {
        dataConfig.set(key, null);
    }

    // Save CellManager playerCells map
    for (Map.Entry<UUID, CellData> entry : playerCells.entrySet()) {
        UUID playerId = entry.getKey();
        CellData data = entry.getValue();
        String key = playerId.toString();

        dataConfig.set(key + ".regionId", data.regionId);
        dataConfig.set(key + ".baseCellId", data.baseCellId);
        dataConfig.set(key + ".openSpaceLevel", data.openSpaceLevel);

        // Save CellUtil.PlayerCellData fields too
        CellUtil.PlayerCellData pcd = CellUtil.getPlayerCellData(playerId);
        dataConfig.set(key + ".cellSizeLevel", pcd.getCellSizeLevel());
        dataConfig.set(key + ".memberCapacityLevel", pcd.getMemberCapacityLevel());
        dataConfig.set(key + ".hopperLimitLevel", pcd.getHopperLimitLevel());
        dataConfig.set(key + ".farmUpgradeLevel", pcd.getFarmUpgradeLevel());
    }

    try {
        dataConfig.save(dataFile);
        plugin.getLogger().info("Cell data saved.");
    } catch (IOException e) {
        plugin.getLogger().severe("Failed to save celldata.yml");
        e.printStackTrace();
    }
}

    public static boolean deletePlayerCell(UUID playerId) {
    World world = Bukkit.getWorld("cell_world");
    if (world == null) return false;

    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
    if (regionManager == null) return false;

    CellData cellData = playerCells.get(playerId);
    if (cellData == null) {
        // No cell data found, just remove region if it exists
        String regionId = "cell_" + playerId.toString().replace("-", "");
        if (regionManager.hasRegion(regionId)) {
            regionManager.removeRegion(regionId);
        }
        CellUtil.clearPlayerCellData(playerId);
        return true;
    }

    // Calculate cell boundaries from baseCellId
    int cellGridX = cellData.baseCellId % MAX_CELL_GRID_WIDTH;
    int cellGridZ = cellData.baseCellId / MAX_CELL_GRID_WIDTH;
    int minY = 0;

    int alignedChunkX = cellGridX * (MAX_CELL_CHUNKS + CELL_SPACING_CHUNKS);
    int alignedChunkZ = cellGridZ * (MAX_CELL_CHUNKS + CELL_SPACING_CHUNKS);

    int minX = alignedChunkX * CHUNK_SIZE - 1;
    int minZ = alignedChunkZ * CHUNK_SIZE - 1;

    int maxX = minX + MAX_CELL_WIDTH_BLOCKS;
    int maxY = minY + MAX_CELL_HEIGHT_BLOCKS - 1;
    int maxZ = minZ + MAX_CELL_DEPTH_BLOCKS;

    // Clear the structure blocks inside the cell (replace with air)
    for (int x = minX; x <= maxX; x++) {
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, y, z).setType(Material.AIR, false);
            }
        }
    }

    // Remove WorldGuard region
    if (regionManager.hasRegion(cellData.regionId)) {
        regionManager.removeRegion(cellData.regionId);
    }

    // Remove plugin memory data
    playerCells.remove(playerId);

    // Remove upgrade data
    CellUtil.clearPlayerCellData(playerId);

    return true;
}

    public static Location getCellTeleportLocation(UUID playerId) {
    CellData data = playerCells.get(playerId);
    if (data == null) return null;

    World world = Bukkit.getWorld("cell_world");
    if (world == null) return null;

    int cellGridX = data.baseCellId % MAX_CELL_GRID_WIDTH;
    int cellGridZ = data.baseCellId / MAX_CELL_GRID_WIDTH;

    int minX = cellGridX * (MAX_CELL_CHUNKS + CELL_SPACING_CHUNKS) * CHUNK_SIZE;
    int minZ = cellGridZ * (MAX_CELL_CHUNKS + CELL_SPACING_CHUNKS) * CHUNK_SIZE;
    int minY = 0;

    int level = data.openSpaceLevel;
    double tpX = minX + (MAX_CELL_WIDTH_BLOCKS - (5 + level * (MAX_CELL_WIDTH_BLOCKS - 5) / 5)) / 2.0 +
                 (5 + level * (MAX_CELL_WIDTH_BLOCKS - 5) / 5) / 2.0;
    double tpY = minY + 1;
    double tpZ = minZ + (MAX_CELL_DEPTH_BLOCKS - (5 + level * (MAX_CELL_DEPTH_BLOCKS - 5) / 5)) / 2.0 +
                 (5 + level * (MAX_CELL_DEPTH_BLOCKS - 5) / 5) / 2.0;

    return new Location(world, tpX, tpY, tpZ);
}

    public static CellData getCellData(UUID ownerId) {
    return playerCells.get(ownerId);
}

    public static ProtectedRegion getCellRegion(UUID playerId) {
    String regionId = "cell_" + playerId.toString().replace("-", ""); // adjust to your naming scheme

    World bukkitWorld = Bukkit.getWorld("cell_world"); 
    if (bukkitWorld == null) return null;

    RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(bukkitWorld));
    if (regionManager == null) return null;

    return regionManager.getRegion(regionId);
}

    public static UUID getCellOwnerForLocation(Location loc) {
    if (loc == null) return null;
    
    World bukkitWorld = loc.getWorld();
    if (bukkitWorld == null) return null;

    BlockVector3 point = BukkitAdapter.asBlockVector(loc);

    for (UUID ownerId : playerCells.keySet()) {
        ProtectedRegion region = getCellRegion(ownerId);
        if (region == null) {
            continue;
        }
        if (region.contains(point)) {
            return ownerId;
        }
    }
    return null;
}

}
