package customEnchants.managers;

import org.bukkit.entity.Player;

public class ScoreboardManager {
    
    public static void updateScoreboard(Player player) {
        ScoreboardManager.createOrUpdateScoreboard(player);
    }

    public static void createOrUpdateScoreboard(Player player) {
        ScoreboardManager.updateScoreboard(player);
    }

}
