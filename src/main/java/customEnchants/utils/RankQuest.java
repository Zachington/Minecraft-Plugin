package customEnchants.utils;

import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import customEnchants.TestEnchants;
import customEnchants.managers.QuestManager;

public class RankQuest {
    public final int blocksRequired;
    public final String extraObjective;
    public final List<String> rewardItems; 

    public RankQuest(int blocksRequired, String extraObjective) {
        this(blocksRequired, extraObjective, null);
    }

    public RankQuest(int blocksRequired, String extraObjective, List<String> rewardItems) {
        this.blocksRequired = blocksRequired;
        this.extraObjective = extraObjective;
        this.rewardItems = rewardItems;
    }

    public static final Map<String, RankQuest> rankQuests = new HashMap<>();

    public static boolean checkExtraObjective(Player player, String obj) {
        if (obj == null || obj.isEmpty()) return true;

        UUID uuid = player.getUniqueId();
        var tracker = TestEnchants.getInstance().statTracker;

        if (obj.startsWith("sell_filler:")) {
            int amount = Integer.parseInt(obj.split(":")[1]);
            return playerHasQuest(player, "sell_filler") &&
                tracker.getPlayerStat(uuid, "filler_sold", false) >= amount;
        }

        if (obj.startsWith("craft_shard:")) {
            int count = Integer.parseInt(obj.split(":")[1]);
            return playerHasQuest(player, "craft_shard") &&
                tracker.getPlayerStat(uuid, "shards_crafted", false) >= count;
        }

        if (obj.startsWith("apply_enchant:")) {
            String tier = obj.split(":")[1];
            return playerHasQuest(player, "apply_enchant") &&
                tracker.getPlayerStat(uuid, "enchants_applied_" + tier, false) > 0;
        }

        if (obj.startsWith("open_crates:")) {
            int required = Integer.parseInt(obj.split(":")[1]);
            return playerHasQuest(player, "open_crates") &&
                tracker.getPlayerStat(uuid, "crate_total", false) >= required;
        }

        if (obj.startsWith("earn_essence:")) {
            int count = Integer.parseInt(obj.split(":")[1]);
            return playerHasQuest(player, "earn_essence") &&
                tracker.getPlayerStat(uuid, "earn_essence", false) >= count;
        }

        return false;
    }

    private static boolean playerHasQuest(Player player, String questKey) {
        return QuestManager.hasQuest(player, questKey);
    }

    public static void giveQuestRewards(Player player, RankQuest quest, String questKey) {
    Random random = new Random();

    // Always give 1 random key
    String[] randomKeys = {"mining", "prison", "enchant"};
    String randomKey = randomKeys[random.nextInt(randomKeys.length)];
    giveKey(player, randomKey);

    // Hardcoded quest rewards
    if (questKey.startsWith("j-k-quest1")) {
        ItemStack redstoneExtractor = customItemUtil.createCustomItem("Redstone Extractor");
        if (redstoneExtractor != null) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(redstoneExtractor);
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create Redstone Extractor item!");
        }
    } else if (questKey.startsWith("s-t-quest1")) {
        ItemStack goldExtractor = customItemUtil.createCustomItem("Gold Extractor");
        if (goldExtractor != null) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(goldExtractor);
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create Gold Extractor item!");
        }
    }

    player.sendTitle(ChatColor.GREEN + "Quest Completed", "", 10, 70, 20);
}

    private static void giveKey(Player player, String type) {
    String formattedKey = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
    ClaimStorage.addKeys(player, formattedKey, 1);
    player.sendMessage(ChatColor.GREEN + "You received a " + formattedKey + " key (check /claim)!");
}
}
