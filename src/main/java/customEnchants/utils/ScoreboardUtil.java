package customEnchants.utils;


import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import customEnchants.TestEnchants;
import customEnchants.managers.RankManager;

public class ScoreboardUtil {

    private final RankManager rankManager;

    public ScoreboardUtil(TestEnchants plugin) {
        this.rankManager = new RankManager();
    }


    public void updateScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        String rank = rankManager.getRank(player);  // fetches rank string, e.g. "Member" or "VIP"

        StatTracker statTracker = TestEnchants.getInstance().getStatTracker();
        UUID uuid = player.getUniqueId();

        int totalBroken = statTracker.getPlayerStat(uuid, "blocks_broken", false);
        int dailyBroken = statTracker.getPlayerStat(uuid, "blocks_broken", true);


        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("info", "dummy", ChatColor.GREEN + "Minecraft Server Name");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 12;

        obj.getScore("   ").setScore(line--);

        // Vault Balance
        double balance = VaultUtil.getEconomy().getBalance(player);
        obj.getScore(ChatColor.YELLOW + "Balance: " + ChatColor.WHITE + "$" + formatBalance(balance)).setScore(line--);

        obj.getScore("     ").setScore(line--);

        
        obj.getScore(ChatColor.YELLOW + "Rank: " + ChatColor.WHITE + formatRankFancy(rank)).setScore(line--);
        
        obj.getScore("    ").setScore(line--);

        // Placeholder Quests
        obj.getScore(ChatColor.YELLOW + "Quests Complete:" + ChatColor.WHITE + "0").setScore(line--);

        // Space
        obj.getScore("  ").setScore(line--);

        //Blocks Broken
        obj.getScore(ChatColor.YELLOW + "Total Blocks: " + ChatColor.WHITE + String.valueOf(totalBroken)).setScore(line--);
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

    public static String formatRankFancy(String rank) {
    if (rank == null || rank.isEmpty()) return "§7[?]";

    String upper = rank.toUpperCase();

    // Matches something like A1P4G, A2P10Z, etc.
    if (upper.matches("[A-Z]\\d+[A-Z]\\d+[A-Z]")) {
        // Example: A2P10Z -> prefix=A2, middle=P10, suffix=Z
        String prefix = upper.replaceAll("^([A-Z]\\d+)[A-Z]\\d+[A-Z]$", "$1");
        String middle = upper.replaceAll("^[A-Z]\\d+([A-Z]\\d+)[A-Z]$", "$1");
        String suffix = upper.replaceAll(".*([A-Z])$", "$1");

        return "§6" + prefix + " §f[" + "§d" + middle + "§f] §f[" + suffix + "]";
    }

    // Matches something like P25Z
    if (upper.matches("[A-Z]\\d+[A-Z]")) {
        String middle = upper.substring(0, upper.length() - 1);
        String suffix = upper.substring(upper.length() - 1);
        return "§f[" + "§d" + middle + "§f] §f[" + suffix + "]";
    }

    // Single rank like "G"
    return "§f[" + upper + "]";
}


}
