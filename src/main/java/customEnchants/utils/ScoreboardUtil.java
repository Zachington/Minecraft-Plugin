package customEnchants.utils;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardUtil {



    public static void updateScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("info", "dummy", ChatColor.GREEN + "Minecraft Server Name");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 10;

        // Vault Balance
        double balance = VaultUtil.getEconomy(player);
        obj.getScore(ChatColor.YELLOW + "Balance:").setScore(line--);
        obj.getScore(ChatColor.WHITE + "$" + String.format("%.2f", balance)).setScore(line--);

        // Placeholder Rank
        obj.getScore(ChatColor.YELLOW + "Rank:").setScore(line--);
        obj.getScore(ChatColor.WHITE + "Member").setScore(line--);

        // Placeholder Quests
        obj.getScore(ChatColor.YELLOW + "Quests Complete:").setScore(line--);
        obj.getScore(ChatColor.WHITE + "0").setScore(line--);

        // Space
        obj.getScore(" ").setScore(line--);

        // Players Online
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        obj.getScore(ChatColor.YELLOW + "Players Online:").setScore(line--);
        obj.getScore(ChatColor.WHITE + "" + online + " / " + max).setScore(line--);

        player.setScoreboard(board);
    }
}
