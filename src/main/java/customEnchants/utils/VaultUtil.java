package customEnchants.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.entity.Player;

public class VaultUtil {
    private static Economy economy;

    public static boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;

        economy = rsp.getProvider();
        return economy != null;
    }

    public static double getEconomy(Player player) {
    if (economy != null) {
        return economy.getBalance(player);
    }
    return 0;
}


    public static boolean isEconomyAvailable() {
        return economy != null;
    }

    public static void giveMoney(Player player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }
}
