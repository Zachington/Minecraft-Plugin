package customEnchants.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VaultManager {
    private final JavaPlugin plugin;
    private final File vaultFolder;
    private final Map<UUID, Inventory[]> vaults = new ConcurrentHashMap<>();

    public VaultManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.vaultFolder = new File(plugin.getDataFolder(), "vaults");
        if (!vaultFolder.exists()) vaultFolder.mkdirs();
    }

    public Inventory getVault(UUID uuid, int index) {
        loadIfNeeded(uuid);
        return vaults.get(uuid)[index - 1];
    }

    public void setVault(UUID uuid, int index, Inventory inv) {
        loadIfNeeded(uuid);
        vaults.get(uuid)[index - 1] = inv;
    }

    private void loadIfNeeded(UUID uuid) {
        if (vaults.containsKey(uuid)) return;

        File file = new File(vaultFolder, uuid + ".yml");
        Inventory[] invs = new Inventory[5];
        for (int i = 0; i < 5; i++) {
            invs[i] = Bukkit.createInventory(null, 54, "Vault " + (i + 1));
        }

        if (!file.exists()) {
            vaults.put(uuid, invs);
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (int i = 0; i < 5; i++) {
            List<?> list = config.getList("vault" + (i + 1));
            if (list != null) {
                ItemStack[] items = list.toArray(new ItemStack[0]);
                invs[i].setContents(items);
            }
        }
        vaults.put(uuid, invs);
    }

    public void saveVault(UUID uuid) {
        if (!vaults.containsKey(uuid)) return;
        File file = new File(vaultFolder, uuid + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        Inventory[] invs = vaults.get(uuid);
        for (int i = 0; i < invs.length; i++) {
            config.set("vault" + (i + 1), Arrays.asList(invs[i].getContents()));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAll() {
        for (UUID uuid : vaults.keySet()) {
            saveVault(uuid);
        }
    }
} 
