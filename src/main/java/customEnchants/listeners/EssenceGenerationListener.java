package customEnchants.listeners;

import customEnchants.utils.EssenceManager;
import customEnchants.utils.customItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class EssenceGenerationListener implements Listener {

    private final EssenceManager essenceManager;
    private final Random random = new Random();
    private static File file;
    private static FileConfiguration config;    
    

    // Map extractorIndex -> TierAmountRange (tier and min/max amount)
    private final Map<Integer, TierAmountRange> extractorConfig = new HashMap<>();

    public EssenceGenerationListener(JavaPlugin plugin, EssenceManager essenceManager) {
        this.essenceManager = essenceManager;

        // Example config: extractorIndex -> Tier, minAmount, maxAmount (inclusive)
        extractorConfig.put(18, new TierAmountRange(1, 1, 1));  
        extractorConfig.put(19, new TierAmountRange(1, 1, 2));  
        extractorConfig.put(20, new TierAmountRange(1, 1, 3));  
        extractorConfig.put(21, new TierAmountRange(2, 1, 2));
        extractorConfig.put(22, new TierAmountRange(2, 1, 3));
        extractorConfig.put(23, new TierAmountRange(3, 1, 2));
        extractorConfig.put(24, new TierAmountRange(3, 1, 3));
        extractorConfig.put(25, new TierAmountRange(4, 1, 2));
        extractorConfig.put(26, new TierAmountRange(4, 1, 3));
        extractorConfig.put(27, new TierAmountRange(5, 1, 2));
        extractorConfig.put(28, new TierAmountRange(5, 1, 3));
        extractorConfig.put(29, new TierAmountRange(6, 1, 2));
        extractorConfig.put(30, new TierAmountRange(6, 1, 3));
        extractorConfig.put(31, new TierAmountRange(7, 1, 2));
        extractorConfig.put(32, new TierAmountRange(7, 1, 3));
        extractorConfig.put(33, new TierAmountRange(8, 1, 2));
        extractorConfig.put(34, new TierAmountRange(8, 2, 3));
        extractorConfig.put(35, new TierAmountRange(8, 3, 7));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        String title = ChatColor.stripColor(event.getView().getTitle());

        if (!title.equalsIgnoreCase("Extractor Storage")) return; // Only handle your GUI

        int slot = event.getRawSlot();

        // Prevent interaction outside the GUI slots (player inventory)
        if (slot >= inv.getSize()) return;

        ItemStack clickedItem = event.getCurrentItem();

        // Prevent clicking glass panes anywhere in the GUI
        if (clickedItem != null && clickedItem.getType() == Material.GLASS_PANE) {
            event.setCancelled(true);
            return;
        }

        int[] allowedSlots = {12, 13, 14, 21, 22, 23, 30, 31, 32};
        boolean isAllowedSlot = false;
        for (int allowedSlot : allowedSlots) {
            if (slot == allowedSlot) {
                isAllowedSlot = true;
                break;
            }
        }

        if (!isAllowedSlot) {
            // Block all interaction in GUI slots outside the allowed ones
            event.setCancelled(true);
        }

        // No stacking prevention logic - all allowed slots are free to stack or move items freely
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory closedInv = event.getInventory();

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (title.equalsIgnoreCase("Extractor Storage")) {
            essenceManager.saveExtractorInventory(player, closedInv);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        ItemStack[] storedContents = essenceManager.getStoredExtractorContents(player);
        if (storedContents == null || storedContents.length != 9) {
            return;
        }


        for (int slot = 0; slot < storedContents.length; slot++) {
            ItemStack item = storedContents[slot];
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
                continue;
            }

            String strippedName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

            int extractorIndex = customItemUtil.getCustomItemIndexByStrippedName(strippedName);
            if (extractorIndex == -1) {
                continue;
            }
            if (!strippedName.toLowerCase().contains("extractor")) {
                continue;
            }

            TierAmountRange config = extractorConfig.get(extractorIndex);
            if (config == null) {
                continue;
            }

            if (random.nextInt(100) < 50) {  // change 100 to 20 for normal use
                int amount = config.minAmount + random.nextInt(config.maxAmount - config.minAmount + 1);
                essenceManager.addEssence(player, config.tier, amount);
                if (!isEssenceNotifDisabled(player)) {
                    player.sendMessage(ChatColor.GREEN + "You found Essence Tier " + config.tier + " x" + amount + "!");
                }
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.equalsIgnoreCase("Extractor Storage")) return;

        Inventory inv = event.getInventory();
        essenceManager.loadExtractorInventory(player, inv);
    }

    private static class TierAmountRange {
        final int tier;
        final int minAmount;
        final int maxAmount;

        TierAmountRange(int tier, int minAmount, int maxAmount) {
            this.tier = tier;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }
    }

    public static Set<UUID> essenceNotifDisabled = new HashSet<>();

    public boolean isEssenceNotifDisabled(Player player) {
    return essenceNotifDisabled.contains(player.getUniqueId());
}

    public void toggleEssenceNotif(Player player) {
    UUID id = player.getUniqueId();
    if (essenceNotifDisabled.contains(id)) {
        essenceNotifDisabled.remove(id);
        player.sendMessage(ChatColor.GREEN + "Essence notifications enabled.");
    } else {
        essenceNotifDisabled.add(id);
        player.sendMessage(ChatColor.RED + "Essence notifications disabled.");
    }
}

    public static void init(File pluginFolder) {
        file = new File(pluginFolder, "essence_prefs.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Could not create essence_prefs.yml");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadPreferences();
    }

    private static void loadPreferences() {
        List<String> list = config.getStringList("disabled");
        for (String uuid : list) {
            try {
                essenceNotifDisabled.add(UUID.fromString(uuid));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public static void savePreferences() {
        List<String> list = essenceNotifDisabled.stream()
            .map(UUID::toString)
            .toList();
        config.set("disabled", list);
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save essence_prefs.yml");
        }
    }

}
