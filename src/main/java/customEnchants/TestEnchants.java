package customEnchants;


import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import customEnchants.managers.PluginManager;
import customEnchants.utils.ScoreboardUtil;
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

import customEnchants.commands.CommandHandler;


public class TestEnchants extends JavaPlugin {
    
    private static TestEnchants instance;

    @Override
    public void onEnable() {
        instance = this;

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


        // Your existing event registrations, commands, etc.
        World world = Bukkit.getWorld("world"); // or your actual world name
        int xMin = -10, xMax = 10;
        int yMin = -10, yMax = 10;
        int zMin = -10, zMax = 20;

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
    }


    public static TestEnchants getInstance() {
        return instance;
    }
    
}

