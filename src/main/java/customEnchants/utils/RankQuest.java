package customEnchants.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import customEnchants.TestEnchants;
import customEnchants.managers.QuestManager;

public class RankQuest {
    public final int blocksRequired;
    public final String extraObjective; 

    public RankQuest(int blocksRequired, String extraObjective) {
        this.blocksRequired = blocksRequired;
        this.extraObjective = extraObjective;
    }

    public static final Map<String, RankQuest> rankQuests = new HashMap<>();

    public static boolean checkExtraObjective(Player player, String obj) {
        if (obj == null || obj.isEmpty()) return true;

        if (obj.startsWith("sell_filler:")) {
            int amount = Integer.parseInt(obj.split(":")[1]);
            if (!playerHasQuest(player, "sell_filler")) {
                return false;
            }
            return TestEnchants.getInstance().statTracker.getPlayerStat(player.getUniqueId(), "filler_sold", false) >= amount;
        }

        //shards not yet implemented
        if (obj.startsWith("craft_shard:")) {
            int count = Integer.parseInt(obj.split(":")[1]);
            if (!playerHasQuest(player, "craft_shard")) {
                return false;
            }
            return TestEnchants.getInstance().statTracker.getPlayerStat(player.getUniqueId(), "shards_crafted", false) >= count;
        }

        if (obj.startsWith("apply_enchant:")) {
            String tier = obj.split(":")[1];
            if (!playerHasQuest(player, "apply_enchant")) {
                return false;
            }
            return TestEnchants.getInstance().statTracker.getPlayerStat(player.getUniqueId(), "enchants_applied_" + tier, false) > 0;
        }

        if (obj.startsWith("open_crates:")) {
            int required = Integer.parseInt(obj.split(":")[1]);
            // Only count if player has the quest active
            if (!playerHasQuest(player, "open_crates")) {
                return false;
            }
            int opened = TestEnchants.getInstance().statTracker.getPlayerStat(player.getUniqueId(), "crate_total", false);
            return opened >= required;
        }

        if (obj.startsWith("earn_essence:")) {
            int count = Integer.parseInt(obj.split(":")[1]);
            if (!playerHasQuest(player, "earn_essence")) {
                return false;
            }
            return TestEnchants.getInstance().statTracker.getPlayerStat(player.getUniqueId(), "earn_essence", false) >= count;
        }

        return false;
    }

    private static boolean playerHasQuest(Player player, String questKey) {
    TestEnchants.getInstance().getQuestManager();
    return QuestManager.hasQuest(player, questKey);
}

}
