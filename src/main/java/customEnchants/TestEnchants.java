package customEnchants;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.HashMap;


import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import customEnchants.managers.PluginManager;
import customEnchants.managers.RankManager;
import customEnchants.managers.MineResetManager;
import customEnchants.utils.ClaimStorage;
import customEnchants.utils.EssenceManager;
import customEnchants.utils.ScoreboardUtil;
import customEnchants.utils.StatTracker;
import customEnchants.utils.VaultUtil;
import customEnchants.utils.customItemUtil;
import net.milkbowl.vault.economy.Economy;
import customEnchants.listeners.AnvilCombineListener;
import customEnchants.listeners.BlackScrollListener;
import customEnchants.listeners.InventoryListener;
import customEnchants.listeners.DurabilityEnchantListener;
import customEnchants.listeners.EnchantScrapListener;
import customEnchants.listeners.EssenceGenerationListener;
import customEnchants.listeners.EssenceMenuListener;
import customEnchants.listeners.MagnetListener;
import customEnchants.listeners.PlayerListener;
import customEnchants.listeners.VoucherListener;
import customEnchants.listeners.BlockBreakListener;
import customEnchants.listeners.CrateListener;
import customEnchants.listeners.GeneralBlockBreakListener;
import customEnchants.listeners.PlayerQuitListener;
import customEnchants.listeners.RegionBlockBreakListener;
import customEnchants.listeners.RngBlockBreak;
import customEnchants.listeners.ClaimMenuListener;

import customEnchants.commands.CommandHandler;



public class TestEnchants extends JavaPlugin {

    private static TestEnchants instance;
    private EssenceManager essenceManager;
    private StatTracker statTracker;
    private final Map<Integer, Double> essenceSellPrices = new HashMap<>();
    private RankManager rankManager;
    private Economy economy;
    private ScoreboardUtil scoreboardUtil;
    private MineResetManager mineManager;

    @Override
    public void onEnable() {
        instance = this;
        customItemUtil.setPlugin(this);

        saveResource("mines.yml", false); // Copies default from jar if it doesn't exist
        ClaimStorage.init(getDataFolder());
        EssenceGenerationListener.init(getDataFolder());

        // Setup economy with Vault
        if (!VaultUtil.setupEconomy()) {
            getLogger().severe("Vault or economy plugin not found! Disabling plugin.");
            for (Plugin p : getServer().getPluginManager().getPlugins()) {
        getLogger().info("Plugin detected: " + p.getName() + " - Enabled: " + p.isEnabled());
    }
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        essenceSellPrices.put(1, 10.0);
        essenceSellPrices.put(2, 15.0);
        essenceSellPrices.put(3, 25.0);
        essenceSellPrices.put(4, 40.0);
        essenceSellPrices.put(5, 60.0);
        essenceSellPrices.put(6, 90.0);
        essenceSellPrices.put(7, 130.0);
        essenceSellPrices.put(8, 180.0);

        this.essenceManager = new EssenceManager(this);
        this.statTracker = new StatTracker(getDataFolder());
        this.rankManager = new RankManager();
        this.scoreboardUtil = new ScoreboardUtil();
        this.economy = VaultUtil.getEconomy();
        mineManager = new MineResetManager(this);

        // Initialize plugin manager
        PluginManager.getInstance().initialize();

        // Register event listeners
        registerListeners();

        // Register commands
        CommandHandler handler = new CommandHandler(this, economy, rankManager);
        registerCommands(handler);

        // Schedule tasks
        scheduleScoreboardUpdates();
        scheduleStatTrackerSaves();
        scheduleDailyReset();
        scheduleHourlyVoucherGiveaway();
        scheduleStaggeredMineResets();

        getLogger().info("CustomEnchant has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save data on shutdown
        if (statTracker != null) {
            statTracker.save();
        }
        ClaimStorage.saveKeys();
        EssenceGenerationListener.savePreferences();
        getLogger().info("CustomEnchant has been disabled!");
    }

    private void registerListeners() {
        World world = Bukkit.getWorld("world"); // Adjust if needed
        int xMin = 0, xMax = 16, yMin = -10, yMax = 5, zMin = 34, zMax = 50;

        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new MagnetListener(), this);
        getServer().getPluginManager().registerEvents(new DurabilityEnchantListener(), this);
        getServer().getPluginManager().registerEvents(new AnvilCombineListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantScrapListener(this), this);
        getServer().getPluginManager().registerEvents(new BlackScrollListener(this), this);
        getServer().getPluginManager().registerEvents(new CrateListener(world, xMin, xMax, yMin, yMax, zMin, zMax), this);
        getServer().getPluginManager().registerEvents(new VoucherListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(essenceManager), this);
        getServer().getPluginManager().registerEvents(new ClaimMenuListener(), this);
        getServer().getPluginManager().registerEvents(new EssenceGenerationListener(this, essenceManager), this);
        getServer().getPluginManager().registerEvents(new GeneralBlockBreakListener(statTracker, this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(statTracker), this);
        getServer().getPluginManager().registerEvents(new EssenceMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new RngBlockBreak(this, statTracker), this);
        getServer().getPluginManager().registerEvents(new RegionBlockBreakListener(), this);

    }

    private void registerCommands(CommandHandler handler) {
        getCommand("giveenchant").setExecutor(handler);
        getCommand("giveenchant").setTabCompleter(handler);
        getCommand("scrap").setExecutor(handler);
        getCommand("giveCustomItem").setExecutor(handler);
        getCommand("giveCustomItem").setTabCompleter(handler);
        getCommand("keyall").setExecutor(handler);
        getCommand("claim").setExecutor(handler);
        getCommand("extractor").setExecutor(handler);
        this.getCommand("essence").setExecutor(handler);
        getCommand("rankup").setExecutor(handler);
        getCommand("setrank").setExecutor(handler);
        getCommand("essencenotif").setExecutor(handler);
        getCommand("resetmine").setExecutor(handler);
    }

    private void scheduleScoreboardUpdates() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                TestEnchants.getInstance().getScoreboardUtil().updateScoreboard(player);
            }
        }, 0L, 100L); // every 5 seconds
    }

    private void scheduleStatTrackerSaves() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (statTracker != null) {
                statTracker.save();
            }
        }, 0L, 100L); // every 5 seconds
    }

    private void scheduleDailyReset() {
        ZoneId zone = ZoneId.systemDefault();

        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();

        long delaySeconds = Duration.between(now, nextMidnight).getSeconds();
        long delayTicks = delaySeconds * 20L;
        long ticksPerDay = 20L * 60 * 60 * 24;

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (statTracker != null) {
                statTracker.resetDailyIfNeeded();
            }
            getLogger().info("Daily stats reset at midnight.");
        }, delayTicks, ticksPerDay);
    }

    private void scheduleHourlyVoucherGiveaway() {
        long ticksPerHour = 20L * 60 * 60;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        long delaySeconds = Duration.between(now, nextHour).getSeconds();
        long delayTicks = delaySeconds * 20L;

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            ItemStack voucher = customItemUtil.createCustomItem("Key All Voucher");
            if (voucher == null) {
                getLogger().warning("Key All Voucher could not be created!");
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getInventory().addItem(voucher);
                player.sendMessage(ChatColor.GOLD + "You received a " + ChatColor.AQUA + "Key All Voucher" + ChatColor.GOLD + "!");
                player.sendTitle(ChatColor.GOLD + "Key All", ChatColor.AQUA + "You got a Key All Voucher!", 10, 70, 20);
            }
        }, delayTicks, ticksPerHour);
    }

    public Economy getEconomy() {
        return VaultUtil.getEconomy();
}

    public RankManager getRankManager() {
    return rankManager;
}

    public Map<Integer, Double> getEssenceSellPrices() {
    return essenceSellPrices;
}

    // Static getter for plugin instance
    public static TestEnchants getInstance() {
        return instance;
    }

    public EssenceManager getEssenceManager() {
        return essenceManager;
    }

    public StatTracker getStatTracker() {
        return statTracker;
    }

    public ScoreboardUtil getScoreboardUtil() {
        return scoreboardUtil;
    }

    private void scheduleStaggeredMineResets() {
    YamlConfiguration config = mineManager.getMinesConfig();
    if (config == null) {
        getLogger().warning("Mines config not loaded!");
        return;
    }

    if (config.getConfigurationSection("mines") == null) {
        getLogger().warning("No 'mines' section found in mines.yml!");
        return;
    }

    Map<String, Object> mines = config.getConfigurationSection("mines").getValues(false);

    int delayBetween = 28 * 20; // 28 seconds in ticks
    int resetInterval = 20 * 60 * 20; // 20 minutes in ticks
    int initialDelay = 0;

    for (String mineName : mines.keySet()) {
        final String mine = mineName;
        Bukkit.getScheduler().runTaskTimer(this, () -> {

            // Warnings
            int[] warnings = {60, 30, 10, 5};
            for (int warn : warnings) {
                Bukkit.getScheduler().runTaskLater(this, () -> {
                sendMineWarning(mine, warn);
                }, (60 - warn) * 20L);
            }

            // Teleport players 1 second before reset
            Bukkit.getScheduler().runTaskLater(this, () -> {
                teleportPlayersOutOfMine(mine);
            }, (60 - 1) * 20L);

            // Actual reset after 60 seconds
            Bukkit.getScheduler().runTaskLater(this, () -> {
                mineManager.resetMine(mine, this);
            }, 60 * 20L);

        }, initialDelay, resetInterval);
        
        initialDelay += delayBetween;
    }
}


    public MineResetManager getMineManager() {
        return mineManager;
    }

    private void teleportPlayersOutOfMine(String mineName) {
    YamlConfiguration config = mineManager.getMinesConfig();

    String basePath = "mines." + mineName;
    String worldName = config.getString(basePath + ".region.world");
    World world = Bukkit.getWorld(worldName);
    if (world == null) return;

    int x1 = config.getInt(basePath + ".region.x1");
    int y1 = config.getInt(basePath + ".region.y1");
    int z1 = config.getInt(basePath + ".region.z1");
    int x2 = config.getInt(basePath + ".region.x2");
    int y2 = config.getInt(basePath + ".region.y2");
    int z2 = config.getInt(basePath + ".region.z2");

    for (Player player : world.getPlayers()) {
        int px = player.getLocation().getBlockX();
        int py = player.getLocation().getBlockY();
        int pz = player.getLocation().getBlockZ();

        boolean inside = px >= Math.min(x1, x2) && px <= Math.max(x1, x2)
                      && py >= Math.min(y1, y2) && py <= Math.max(y1, y2)
                      && pz >= Math.min(z1, z2) && pz <= Math.max(z1, z2);

        if (inside) {
            int centerX = (x1 + x2) / 2;
            int centerZ = (z1 + z2) / 2;
            int safeY = world.getHighestBlockYAt(centerX, centerZ);
            player.teleport(new Location(world, centerX + 0.5, safeY + 1, centerZ + 0.5));
            player.sendMessage(ChatColor.RED + "You were teleported out of the mine for reset!");
        }
    }
}

    private void sendMineWarning(String mineName, int warnSeconds) {
    YamlConfiguration config = mineManager.getMinesConfig();
    String basePath = "mines." + mineName;
    String worldName = config.getString(basePath + ".region.world");
    World world = Bukkit.getWorld(worldName);
    if (world == null) return;

    int x1 = config.getInt(basePath + ".region.x1");
    int y1 = config.getInt(basePath + ".region.y1");
    int z1 = config.getInt(basePath + ".region.z1");
    int x2 = config.getInt(basePath + ".region.x2");
    int y2 = config.getInt(basePath + ".region.y2");
    int z2 = config.getInt(basePath + ".region.z2");

    for (Player player : world.getPlayers()) {
        int px = player.getLocation().getBlockX();
        int py = player.getLocation().getBlockY();
        int pz = player.getLocation().getBlockZ();

        boolean inside = px >= Math.min(x1, x2) && px <= Math.max(x1, x2)
                    && py >= Math.min(y1, y2) && py <= Math.max(y1, y2)
                    && pz >= Math.min(z1, z2) && pz <= Math.max(z1, z2);

        if (inside) {
            player.sendMessage(ChatColor.YELLOW + mineName + " resetting in " + warnSeconds + " seconds!");
        }
    }
}


}
