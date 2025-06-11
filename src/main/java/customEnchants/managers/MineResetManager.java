package customEnchants.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class MineResetManager {
    private File minesFile;
    private YamlConfiguration minesConfig;

    public MineResetManager(JavaPlugin plugin) {
        this.minesFile = new File(plugin.getDataFolder(), "mines.yml");
        this.minesConfig = YamlConfiguration.loadConfiguration(minesFile);
    }

    public YamlConfiguration getMinesConfig() {
        return minesConfig;
    }

    public boolean resetMine(String mineName, JavaPlugin plugin) {
        YamlConfiguration config = minesConfig;

        String foundMineKey = null;
        for (String key : config.getConfigurationSection("mines").getKeys(false)) {
            if (key.equalsIgnoreCase(mineName)) {
                foundMineKey = key;
                break;
            }
        }
        if (foundMineKey == null) return false;

        String basePath = "mines." + foundMineKey;

        String worldName = config.getString(basePath + ".region.world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return false;

        int x1 = config.getInt(basePath + ".region.x1");
        int y1 = config.getInt(basePath + ".region.y1");
        int z1 = config.getInt(basePath + ".region.z1");
        int x2 = config.getInt(basePath + ".region.x2");
        int y2 = config.getInt(basePath + ".region.y2");
        int z2 = config.getInt(basePath + ".region.z2");

        Map<Material, Double> weightedMap = new HashMap<>();
        for (String material : config.getConfigurationSection(basePath + ".blocks").getKeys(false)) {
            double weight = config.getDouble(basePath + ".blocks." + material);
            Material mat = Material.getMaterial(material);
            if (mat != null && weight > 0) {
                weightedMap.put(mat, weight);
            }
        }
        if (weightedMap.isEmpty()) return false;

        // Calculate totalWeight as final
        final double totalWeight = weightedMap.values().stream().mapToDouble(Double::doubleValue).sum();

        Random rand = new Random();
        List<Chunk> chunks = new ArrayList<>();

        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
                if (!chunks.contains(chunk)) chunks.add(chunk);
            }
        }

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= chunks.size()) {
                    Bukkit.getLogger().info("[Mines] Reset " + mineName);
                    cancel();
                    return;
                }

                Chunk chunk = chunks.get(index++);
                int baseX = chunk.getX() << 4;
                int baseZ = chunk.getZ() << 4;

                for (int x = baseX; x < baseX + 16; x++) {
                    if (x < Math.min(x1, x2) || x > Math.max(x1, x2)) continue;
                    for (int z = baseZ; z < baseZ + 16; z++) {
                        if (z < Math.min(z1, z2) || z > Math.max(z1, z2)) continue;
                        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                            Material chosen = getRandomBlock(weightedMap, totalWeight, rand);
                            world.getBlockAt(x, y, z).setType(chosen, false);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private Material getRandomBlock(Map<Material, Double> weightedMap, double totalWeight, Random rand) {
    // First, create a sorted list (consistent order)
    List<Map.Entry<Material, Double>> entries = new ArrayList<>(weightedMap.entrySet());
    entries.sort(Map.Entry.comparingByKey()); // consistent order

    double roll = rand.nextDouble() * totalWeight;
    double cumulative = 0.0;

    for (Map.Entry<Material, Double> entry : entries) {
        cumulative += entry.getValue();
        if (roll <= cumulative) {
            return entry.getKey();
        }
    }

    return Material.STONE; // fallback
}

}
