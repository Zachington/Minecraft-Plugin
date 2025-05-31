package customEnchants.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;







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

    int x1 = config.getInt("mines." + foundMineKey + ".region.x1");
    int y1 = config.getInt("mines." + foundMineKey + ".region.y1");
    int z1 = config.getInt("mines." + foundMineKey + ".region.z1");
    int x2 = config.getInt("mines." + foundMineKey + ".region.x2");
    int y2 = config.getInt("mines." + foundMineKey + ".region.y2");
    int z2 = config.getInt("mines." + foundMineKey + ".region.z2");

    Map<String, Integer> blockMap = new HashMap<>();
    if (config.getConfigurationSection("mines." + foundMineKey + ".blocks") == null) return false;

    for (String material : config.getConfigurationSection("mines." + foundMineKey + ".blocks").getKeys(false)) {
        int weight = config.getInt("mines." + foundMineKey + ".blocks." + material);
        blockMap.put(material, weight);
    }

    List<Material> weightedList = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : blockMap.entrySet()) {
        Material mat = Material.getMaterial(entry.getKey());
        if (mat != null) {
            weightedList.addAll(Collections.nCopies(entry.getValue(), mat));
        }
    }

    if (weightedList.isEmpty()) return false;

    Random rand = new Random();
    List<Chunk> chunks = new ArrayList<>();

    for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x += 16) {
        for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z += 16) {
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
                        Material chosen = weightedList.get(rand.nextInt(weightedList.size()));
                        world.getBlockAt(x, y, z).setType(chosen, false);
                    }
                }
            }
        }
    }.runTaskTimer(plugin, 0L, 1L); // 1 chunk per tick

    return true; // Successfully scheduled reset
}



}



