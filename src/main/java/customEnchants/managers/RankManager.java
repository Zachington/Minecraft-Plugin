package customEnchants.managers;

import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import customEnchants.utils.RankUtils; // import the RankUtils class

public class RankManager {

    // Remove rankKey field and constructor entirely

    public String getRank(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        String rank = container.get(RankUtils.rankKey, PersistentDataType.STRING);
        if (rank == null) rank = "a";
        return rank;
    }

    public void setRank(Player player, String rank) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(RankUtils.rankKey, PersistentDataType.STRING, rank);
    }
}
