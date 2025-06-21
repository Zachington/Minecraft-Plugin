package customEnchants.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WarpManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Location> warpCache = new HashMap<>();

    public WarpManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "warps.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        loadAllWarps();
    }

    private void loadAllWarps() {
        if (!config.contains("warps")) return;
        for (String key : config.getConfigurationSection("warps").getKeys(false)) {
            warpCache.put(key, getWarpLocationFromConfig(key));
        }
    }

    public Location getWarp(String name) {
        return warpCache.get(name.toLowerCase());
    }

    private Location getWarpLocationFromConfig(String name) {
        String base = "warps." + name;
        String worldName = config.getString(base + ".world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = config.getDouble(base + ".x");
        double y = config.getDouble(base + ".y");
        double z = config.getDouble(base + ".z");
        float yaw = (float) config.getDouble(base + ".yaw");
        float pitch = (float) config.getDouble(base + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public void saveWarp(String name, Location loc) {
    String base = "warps." + name.toLowerCase();
    config.set(base + ".world", loc.getWorld().getName());
    config.set(base + ".x", loc.getX());
    config.set(base + ".y", loc.getY());
    config.set(base + ".z", loc.getZ());
    config.set(base + ".yaw", loc.getYaw());
    config.set(base + ".pitch", loc.getPitch());

    try {
        config.save(file);
        warpCache.put(name.toLowerCase(), loc);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public boolean removeWarp(String name) {
    if (config.contains("warps." + name)) {
        config.set("warps." + name, null);
        save();
        return true;
    }
    return false;
}

    private void save() {
    try {
        config.save(file);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

}
