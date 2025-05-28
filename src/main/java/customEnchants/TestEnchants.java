package customEnchants;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;



import customEnchants.managers.PluginManager;
import customEnchants.utils.ScoreboardUtil;
import customEnchants.utils.StatTracker;
import customEnchants.utils.VaultUtil;
import customEnchants.utils.customItemUtil;
import customEnchants.listeners.AnvilCombineListener;
import customEnchants.listeners.BlackScrollListener;
import customEnchants.listeners.InventoryListener;
import customEnchants.listeners.DurabilityEnchantListener;
import customEnchants.listeners.EnchantScrapListener;
import customEnchants.listeners.GiveItemListener;
import customEnchants.listeners.MagnetListener;
import customEnchants.listeners.PlayerListener;
import customEnchants.listeners.VoucherListener;
import customEnchants.listeners.BlockBreakListener;
import customEnchants.listeners.CrateListener;
import customEnchants.listeners.GeneralBlockBreakListener;
import customEnchants.listeners.PlayerQuitListener;

import customEnchants.commands.CommandHandler;


public class TestEnchants extends JavaPlugin {
    
    private static TestEnchants instance;
    private StatTracker statTracker;
    private void scheduleDailyReset() {
    ZoneId zone = ZoneId.systemDefault(); // Server's timezone. Change if needed.

    // Calculate delay until next midnight
    LocalDateTime now = LocalDateTime.now(zone);
    LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();

    long delaySeconds = Duration.between(now, nextMidnight).getSeconds();
    long delayTicks = delaySeconds * 20; // 20 ticks per second
    long ticksPerDay = 20L * 60 * 60 * 24;

    Bukkit.getScheduler().runTaskTimer(this, () -> {
        statTracker.resetDailyIfNeeded();
        getLogger().info("Daily stats reset at midnight.");
    }, delayTicks, ticksPerDay);
}

    @Override
    public void onEnable() {
        instance = this;
        this.statTracker = new StatTracker(getDataFolder());

        long ticksPerHour = 20L * 60 * 60; // 72000 ticks = 1 hour

        // Get current time in minutes
        long currentMinute = java.time.LocalTime.now().getMinute();
        long currentSecond = java.time.LocalTime.now().getSecond();

        // Calculate how many seconds until the next hour (top of the hour)
        long secondsUntilNextHour = (60 - currentMinute - 1) * 60 + (60 - currentSecond);

        long ticksUntilNextHour = secondsUntilNextHour * 20L;

        if (!VaultUtil.setupEconomy()) {
        getLogger().severe("Vault or economy plugin not found! Disabling plugin.");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }
        //update scoreboard
        Bukkit.getScheduler().runTaskTimer(this, () -> {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ScoreboardUtil.updateScoreboard(player);
        }
    }, 0L, 100L);
        //Key all
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
        }, ticksUntilNextHour, ticksPerHour);
        //Data save
        getServer().getScheduler().runTaskTimer(this, () -> {
        statTracker.save();
    }, 0L, 100L); // 100 ticks = 5 seconds
        //Daily Blocks reset
        scheduleDailyReset();

        // Your existing event registrations, commands, etc.
        World world = Bukkit.getWorld("world"); // or your actual world name
        int xMin = 0, xMax = 16;
        int yMin = -10, yMax = 5;
        int zMin = 34, zMax = 50;

        PluginManager.getInstance().initialize();
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new GiveItemListener(), this);
        getServer().getPluginManager().registerEvents(new MagnetListener(), this);
        getServer().getPluginManager().registerEvents(new DurabilityEnchantListener(), this);
        getServer().getPluginManager().registerEvents(new AnvilCombineListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantScrapListener(this), this);
        getServer().getPluginManager().registerEvents(new BlackScrollListener(this), this);
        getServer().getPluginManager().registerEvents(new CrateListener(world, xMin, xMax, yMin, yMax, zMin, zMax), this);
        getServer().getPluginManager().registerEvents(new VoucherListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);


        getLogger().info("CustomEnchant has been enabled!");

        CommandHandler handler = new CommandHandler();

        getCommand("giveenchant").setExecutor(handler);
        getCommand("giveenchant").setTabCompleter(handler);
        getCommand("scrap").setExecutor(handler);
        getCommand("giveCustomItem").setExecutor(handler);
        getCommand("giveCustomItem").setTabCompleter(handler);
        getCommand("testblackscroll").setExecutor(handler);
        getCommand("keyall").setExecutor(new CommandHandler());

        getServer().getPluginManager().registerEvents(new GeneralBlockBreakListener(statTracker), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(statTracker), this);
    }


    public static TestEnchants getInstance() {
        return instance;
    }

    public StatTracker getStatTracker() {
        return statTracker;
    }
    
}

