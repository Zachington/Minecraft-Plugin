package customEnchants.utils;


import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import customEnchants.TestEnchants;

public class ScoreboardUtil {



    public static void updateScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        StatTracker statTracker = TestEnchants.getInstance().getStatTracker();
        UUID uuid = player.getUniqueId();

        int totalBroken = statTracker.getPlayerStat(uuid, "blocks_broken", false);
        int dailyBroken = statTracker.getPlayerStat(uuid, "blocks_broken", true);


        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("info", "dummy", ChatColor.GREEN + "Minecraft Server Name");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 10;

        obj.getScore("   ").setScore(line--);

        // Vault Balance
        double balance = VaultUtil.getEconomy(player);
        obj.getScore(ChatColor.YELLOW + "Balance: " + ChatColor.WHITE + "$" + formatBalance(balance)).setScore(line--);

        obj.getScore("     ").setScore(line--);

        // Placeholder Rank
        obj.getScore(ChatColor.YELLOW + "Rank: " + ChatColor.WHITE + "Member").setScore(line--);

        obj.getScore("    ").setScore(line--);

        // Placeholder Quests
        obj.getScore(ChatColor.YELLOW + "Quests Complete:" + ChatColor.WHITE + "0").setScore(line--);

        // Space
        obj.getScore("  ").setScore(line--);

        //Blocks Broken
        obj.getScore(ChatColor.YELLOW + "Total Block: " + ChatColor.WHITE + String.valueOf(totalBroken)).setScore(line--);
        obj.getScore(ChatColor.YELLOW + "DBM: " + ChatColor.WHITE + String.valueOf(dailyBroken)).setScore(line--);

        // Space
        obj.getScore(" ").setScore(line--);

        // Players Online
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        obj.getScore(ChatColor.YELLOW + "Players Online:").setScore(line--);
        obj.getScore(ChatColor.WHITE + "" + online + " / " + max).setScore(line--);

        player.setScoreboard(board);
    }

    private static String formatBalance(double balance) {
    long rounded = (long) balance; // Remove cents
    if (rounded >= 1_000_000_000) {
        return String.format("%.1fB", rounded / 1_000_000_000.0);
    } else if (rounded >= 1_000_000) {
        return String.format("%.1fM", rounded / 1_000_000.0);
    } else if (rounded >= 1_000) {
        return String.format("%.1fK", rounded / 1_000.0);
    } else {
        return String.valueOf(rounded);
    }
}

}
