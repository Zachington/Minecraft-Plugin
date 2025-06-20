package customEnchants.managers;

import customEnchants.TestEnchants;
import customEnchants.utils.RankQuest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class QuestManager {
    public final static Map<String, RankQuest> rankQuests = new HashMap<>();
    public final static Map<UUID, Set<String>> playerQuests = new HashMap<>();

    public QuestManager(File dataFolder) {
        File file = new File(dataFolder, "rank-quests.yml");
        if (!file.exists()) {
            TestEnchants.getInstance().saveResource("rank-quests.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String rankKey : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(rankKey);
            if (section == null) continue;

            ConfigurationSection q1 = section.getConfigurationSection("quest1");
            if (q1 != null && q1.contains("blocks")) {
                int blocks = q1.getInt("blocks");
                rankQuests.put(rankKey + "-quest1", new RankQuest(blocks, null));
            }

            ConfigurationSection q2 = section.getConfigurationSection("quest2");
            if (q2 != null && q2.contains("objective")) {
                String obj = q2.getString("objective");
                rankQuests.put(rankKey + "-quest2", new RankQuest(0, obj));
            }
        }
    }

    public RankQuest get(String questKey) {
        return rankQuests.get(questKey);
    }

    public Map<String, RankQuest> getRankQuests() {
        return Collections.unmodifiableMap(rankQuests);
    }

    public void addQuest(Player player, String questKey) {
        if (!rankQuests.containsKey(questKey)) return;
        playerQuests.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(questKey);
    }

    public void removeQuest(Player player, String questKey) {
        Set<String> quests = playerQuests.get(player.getUniqueId());
        if (quests != null) {
            quests.remove(questKey);
            if (quests.isEmpty()) {
                playerQuests.remove(player.getUniqueId());
            }
        }
    }

    public void clearQuests(Player player) {
        playerQuests.remove(player.getUniqueId());
    }

    public Set<String> getActiveQuests(Player player) {
        return Collections.unmodifiableSet(playerQuests.getOrDefault(player.getUniqueId(), Collections.emptySet()));
    }

    public static boolean hasQuest(Player player, String questKey) {
        Set<String> quests = playerQuests.get(player.getUniqueId());
        return quests != null && quests.contains(questKey);
    }

    public static boolean hasCompletedQuest(Player player, String questKey) {
        RankQuest quest = rankQuests.get(questKey);
        if (quest == null) return false;

        UUID uuid = player.getUniqueId();
        int currentBlocks = TestEnchants.getInstance().statTracker.getPlayerStat(uuid, "blocks_broken", false);
        int blocksAtStart = TestEnchants.getInstance().statTracker.getPlayerStat(uuid, "blocks_broken_at_rank_start." + questKey, false);
        int blocksSinceStart = currentBlocks - blocksAtStart;

        if (quest.blocksRequired > 0 && blocksSinceStart < quest.blocksRequired) return false;

        return RankQuest.checkExtraObjective(player, quest.extraObjective);
    }

    public void completeQuest(Player player, String questKey) {
    removeQuest(player, questKey); // existing logic

    UUID uuid = player.getUniqueId();
    TestEnchants.getInstance().statTracker.incrementPlayerStat(uuid, "quests_completed");
}

}
