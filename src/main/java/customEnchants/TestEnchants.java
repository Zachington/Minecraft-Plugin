package customEnchants;

import org.bukkit.plugin.java.JavaPlugin;

import customEnchants.managers.PluginManager;
import customEnchants.listeners.AnvilCombineListener;
import customEnchants.listeners.BlackScrollListener;
import customEnchants.listeners.InventoryListener;
import customEnchants.listeners.DurabilityEnchantListener;
import customEnchants.listeners.EnchantScrapListener;
import customEnchants.listeners.GiveItemListener;
import customEnchants.listeners.MagnetListener;
import customEnchants.listeners.BlockBreakListener;
import customEnchants.commands.CommandHandler;


public class TestEnchants extends JavaPlugin {
    
    private static TestEnchants instance;

    @Override
    public void onEnable() {
        instance = this;
        PluginManager.getInstance().initialize();
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new GiveItemListener(), this);
        getServer().getPluginManager().registerEvents(new MagnetListener(), this);
        getServer().getPluginManager().registerEvents(new DurabilityEnchantListener(), this);
        getServer().getPluginManager().registerEvents(new AnvilCombineListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantScrapListener(this), this);
        getServer().getPluginManager().registerEvents(new BlackScrollListener(this), this);

        getLogger().info("CustomEnchant has been enabled!");

        CommandHandler handler = new CommandHandler();

        getCommand("giveenchant").setExecutor(handler);
        getCommand("giveenchant").setTabCompleter(handler);
        getCommand("scrap").setExecutor(handler);
        getCommand("giveCustomItem").setExecutor(handler);
        getCommand("giveCustomItem").setTabCompleter(handler);
        getCommand("testblackscroll").setExecutor(handler);
    }

    public static TestEnchants getInstance() {
        return instance;
    }

}
