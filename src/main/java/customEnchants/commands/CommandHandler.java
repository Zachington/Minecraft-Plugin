package customEnchants.commands;

import customEnchants.TestEnchants;
import customEnchants.listeners.EssenceGenerationListener;
import customEnchants.managers.RankManager;
import customEnchants.managers.SellManager;
import customEnchants.utils.CellUtil;
import customEnchants.utils.EnchantmentData;
import customEnchants.utils.GuiUtil;
import customEnchants.utils.RankQuest;
import customEnchants.utils.RankUtils;
import customEnchants.utils.RankUtils.RankCost;
import customEnchants.utils.StatTracker;
import customEnchants.utils.VaultUtil;
import customEnchants.utils.customItemUtil;
import net.milkbowl.vault.economy.Economy;
import customEnchants.utils.EssenceManager;
import customEnchants.managers.VaultManager;
import customEnchants.managers.WarpManager;
import customEnchants.managers.CellManager;
import customEnchants.managers.QuestManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final Random random = new Random();
    private final Economy economy;
    private final EssenceManager essenceManager;
    private final RankManager rankManager;
    private final VaultManager vaultManager;
    private final TestEnchants testEnchants;
    StatTracker statTracker = TestEnchants.getInstance().statTracker;
    QuestManager questManager = TestEnchants.getInstance().getQuestManager();
    private static final Map<UUID, UUID> pendingInvites = new HashMap<>();

    public CommandHandler(TestEnchants plugin, Economy economy, RankManager rankManager, VaultManager vaultManager, TestEnchants testEnchants) {
        this.economy = economy;
        this.rankManager = rankManager;
        this.essenceManager = plugin.getEssenceManager();
        this.vaultManager = plugin.getVaultManager();
        this.testEnchants = testEnchants;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "scrap" -> {
                Inventory gui = GuiUtil.createScrapGui(player);
                player.openInventory(gui);
                return true;
            }
            case "giveenchant" -> {
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /giveenchant <enchantName> [level] [chance]");
                    return true;
                }

                String enchantName = args[0].replace('_', ' ');
                int enchantIndex = EnchantmentData.getEnchantmentIndex(enchantName);
                if (enchantIndex == -1) {
                    player.sendMessage(ChatColor.RED + "Unknown enchantment: " + enchantName);
                    player.sendMessage(ChatColor.GRAY + "Available enchantments: " + getAvailableEnchantsList());
                    return true;
                }

                EnchantmentData.EnchantmentInfo enchantInfo = EnchantmentData.getEnchantmentInfo(enchantIndex);

                int level = 1;
                if (args.length >= 2) {
                    try {
                        level = Integer.parseInt(args[1]);
                        if (level < 1 || level > enchantInfo.maxLevel) {
                            player.sendMessage(ChatColor.RED + enchantName + " level must be between 1 and " + enchantInfo.maxLevel);
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Level must be a number between 1 and " + enchantInfo.maxLevel);
                        return true;
                    }
                }

                Integer customChance = null;
                if (args.length >= 3) {
                    try {
                        customChance = Integer.parseInt(args[2]);
                        if (customChance < 1 || customChance > 100) {
                            player.sendMessage(ChatColor.RED + "Chance must be between 1 and 100");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Chance must be a number between 1 and 100");
                        return true;
                    }
                }

                int chance = (customChance != null) ? customChance : generateRandomChance(enchantInfo.rarity);

                ItemStack book = EnchantmentData.createEnchantedBook(enchantInfo, level, chance, false);

                player.getInventory().addItem(book);
                player.sendMessage(ChatColor.GREEN + "Given " + enchantInfo.name + " Level " + level + " with " + chance + "% chance");
                return true;
            }
            case "keyall" -> {
                if (!sender.hasPermission("customenchants.keyall")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }

                ItemStack voucher = customItemUtil.createCustomItem("Key All Voucher");
                if (voucher == null) {
                    sender.sendMessage(ChatColor.RED + "Voucher item not found!");
                    return true;
                }

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.getInventory().addItem(voucher);
                    onlinePlayer.sendTitle("§6Key All", "§aYou got a Key All Voucher!", 10, 70, 20);
                }

                sender.sendMessage(ChatColor.GREEN + "Given Key All Voucher to all online players.");
                return true;
            }
            case "givecustomitem" -> {
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /giveCustomItem <itemName> [amount]");
                    return true;
                }

                String inputName;
                int amount = 1;

                if (args.length > 1) {
                    try {
                        amount = Integer.parseInt(args[args.length - 1]);
                        if (amount < 1) {
                            player.sendMessage(ChatColor.RED + "Amount must be at least 1.");
                            return true;
                        }
                        inputName = String.join(" ", Arrays.copyOf(args, args.length - 1)).replace('_', ' ').toLowerCase();
                    } catch (NumberFormatException e) {
                        inputName = String.join(" ", args).replace('_', ' ').toLowerCase();
                    }
                } else {
                    inputName = args[0].replace('_', ' ').toLowerCase();
                }


                customItemUtil.CustomItemInfo matchedInfo = null;
                for (int i = 0; i < customItemUtil.CUSTOM_ITEM.length; i++) {
                    customItemUtil.CustomItemInfo candidate = customItemUtil.getCustomItemInfo(i);
                    if (candidate == null) continue;

                    String rawName = candidate.getName();
                    String cleanName = ChatColor.stripColor(rawName);
                    if (cleanName == null || cleanName.trim().isEmpty()) {
                        cleanName = rawName.replaceAll("§[0-9A-FK-ORa-fk-or]", "");
                    }

                    if (cleanName.toLowerCase().equals(inputName)) {
                        matchedInfo = candidate;
                        break;
                    }
                }

                if (matchedInfo == null) {
                    player.sendMessage(ChatColor.RED + "Invalid custom item name: " + inputName);
                    return true;
                }


                ItemStack customItem = customItemUtil.createCustomItem(matchedInfo.getName());

                if (customItem == null) {
                    player.sendMessage(ChatColor.RED + "Failed to create custom item.");
                    return true;
                }

                customItem.setAmount(amount);

                player.getInventory().addItem(customItem);
                player.sendMessage(ChatColor.GREEN + "You have been given " + amount + "x: " + customItem.getItemMeta().getDisplayName());

                return true;
            }
            case "claim" -> {
                Inventory gui = GuiUtil.claimInventory(player);
                player.openInventory(gui);
                return true;
            }
            case "extractor" -> {
                Inventory gui = GuiUtil.createExtractorStorageGUI(player, essenceManager);
                player.openInventory(gui);
                return true;
            }
            case "essence" -> {
                Inventory gui = GuiUtil.essenceInventory(player);  // assuming essenceInventory is in GuiUtil
                player.openInventory(gui);
                return true;
            }
            case "rankup" -> {
    String currentRank = RankUtils.getRank(player);
    String nextRank = RankUtils.getNextRank(currentRank);
    UUID uuid = player.getUniqueId();

    if (nextRank == null) {
        player.sendMessage(ChatColor.RED + "You are already at the max rank!");
        return true;
    }
    if (nextRank == "a1p1a") {
        player.sendMessage(ChatColor.RED + "You are already at the max rank!");
        return true;
    }

    boolean isPrestige = RankUtils.compareRanks(currentRank, "p1a") >= 0;
    boolean isStartOfPrestige = currentRank.endsWith("a");

    // Quest check
    int requiredQuests = isPrestige ? (isStartOfPrestige ? 1 : 0) : 2;
    if (statTracker.getPlayerStat(uuid, "quests_completed", false) < requiredQuests) {
        player.sendMessage(ChatColor.RED + "You must complete " + requiredQuests + " quest" + (requiredQuests == 1 ? "" : "s") + " before ranking up.");
        return true;
    }

    RankCost cost = RankUtils.getRankCost(nextRank, currentRank);
    if (cost == null) {
        player.sendMessage(ChatColor.RED + "Rankup cost could not be calculated.");
        return true;
    }

    if (essenceManager.getEssence(player, cost.essenceTier) < cost.essence) {
        player.sendMessage(ChatColor.RED + "You need §e" + cost.essence + " Tier " + cost.essenceTier + " Essence§c to rank up.");
        return true;
    }

    if (economy.getBalance(player) < cost.money) {
        player.sendMessage(ChatColor.RED + "You need §a$" + String.format("%,.2f", cost.money) + "§c to rank up.");
        return true;
    }

    // Deduct cost
    essenceManager.removeEssence(player, cost.essenceTier, cost.essence);
    economy.withdrawPlayer(player, cost.money);

    // Set new rank
    rankManager.setRank(player, nextRank);
    player.sendMessage(ChatColor.GREEN + "You ranked up to " + RankUtils.formatRankName(nextRank) + ChatColor.GREEN + "!");

    // Reset quests if needed
    if (!isPrestige) {
    statTracker.setPlayerStat(uuid, "quests_completed", 0, false);
    }

    // Stat tracking
    if (isPrestige && nextRank.endsWith("a")) {
        // Snapshot stats once per prestige
        String prestigePrefix = nextRank.substring(0, nextRank.length() - 1); // "p1", "p2", etc.
        for (String rarity : new String[]{"common", "uncommon", "rare", "epic", "legendary"}) {
            int current = statTracker.getPlayerStat(uuid, "enchants_applied_" + rarity, false);
            statTracker.setPlayerStat(uuid, "enchants_applied_" + rarity + "_at_rank_start." + prestigePrefix, current, false);
        }
        statTracker.setPlayerStat(uuid, "blocks_broken_at_rank_start." + prestigePrefix, statTracker.getPlayerStat(uuid, "blocks_broken", false), false);
        statTracker.setPlayerStat(uuid, "earned_essence_at_rank_start." + prestigePrefix, statTracker.getPlayerStat(uuid, "earned_essence", false), false);
        statTracker.setPlayerStat(uuid, "total_crates_at_rank_start." + prestigePrefix, statTracker.getPlayerStat(uuid, "crate_total", false), false);
        statTracker.setPlayerStat(uuid, "filler_sold_at_rank_start." + prestigePrefix, statTracker.getPlayerStat(uuid, "filler_sold", false), false);
        statTracker.setPlayerStat(uuid, "crafted_essence_at_rank_start." + prestigePrefix, statTracker.getPlayerStat(uuid, "crafted_essence", false), false);
        statTracker.setPlayerStat(uuid, "crafted_extractors_at_rank_start." + prestigePrefix, statTracker.getPlayerStat(uuid, "crafted_extractors", false), false);
    } else if (!isPrestige) {
        // Snapshot per-rank stats pre-prestige
        String questPrefix = currentRank + "-" + nextRank;
        for (String rarity : new String[]{"common", "uncommon", "rare", "epic", "legendary"}) {
            int current = statTracker.getPlayerStat(uuid, "enchants_applied_" + rarity, false);
            statTracker.setPlayerStat(uuid, "enchants_applied_" + rarity + "_at_rank_start." + questPrefix, current, false);
        }
        statTracker.setPlayerStat(uuid, "blocks_broken_at_rank_start." + questPrefix, statTracker.getPlayerStat(uuid, "blocks_broken", false), false);
        statTracker.setPlayerStat(uuid, "earned_essence_at_rank_start." + questPrefix, statTracker.getPlayerStat(uuid, "earned_essence", false), false);
        statTracker.setPlayerStat(uuid, "total_crates_at_rank_start." + questPrefix, statTracker.getPlayerStat(uuid, "crate_total", false), false);
        statTracker.setPlayerStat(uuid, "filler_sold_at_rank_start." + questPrefix, statTracker.getPlayerStat(uuid, "filler_sold", false), false);
        statTracker.setPlayerStat(uuid, "crafted_essence_at_rank_start." + questPrefix, statTracker.getPlayerStat(uuid, "crafted_essence", false), false);
        statTracker.setPlayerStat(uuid, "crafted_extractors_at_rank_start." + questPrefix, statTracker.getPlayerStat(uuid, "crafted_extractors", false), false);
    }

    // Quests only if not prestige or ranking into a new prestige
    if (!isPrestige || nextRank.endsWith("a")) {
        String newQuestPrefix = nextRank + "-" + RankUtils.getNextRank(nextRank);
        questManager.addQuest(player, newQuestPrefix + "-quest1");
        questManager.addQuest(player, newQuestPrefix + "-quest2");
    }

    // Broadcast
    if (RankUtils.isAscendUpgrade(currentRank, nextRank)) {
        Bukkit.broadcastMessage("§6" + player.getName() + " §ehas §4Ascended §eto: " + ChatColor.DARK_RED + RankUtils.formatRankTierOnly(nextRank) + "§e!");
    } else if (RankUtils.isPrestigeUpgrade(currentRank, nextRank)) {
        Bukkit.broadcastMessage("§6" + player.getName() + " §ehas §dPrestiged §eto: " + ChatColor.LIGHT_PURPLE + RankUtils.formatRankPrestigeOnly(nextRank) + "§e!");
    }

    return true;
}
            case "setrank" -> {
    if (!sender.hasPermission("customenchants.setrank")) {
        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        return true;
    }

    if (args.length != 2) {
        sender.sendMessage(ChatColor.RED + "Usage: /setrank <player> <rank>");
        return true;
    }

    Player target = Bukkit.getPlayerExact(args[0]);
    if (target == null) {
        sender.sendMessage(ChatColor.RED + "Player not found.");
        return true;
    }

    String newRank = args[1].toLowerCase();
    UUID uuid = target.getUniqueId();

    // Set the player's rank
    rankManager.setRank(target, newRank);

    StatTracker statTracker = TestEnchants.getInstance().getStatTracker();
    QuestManager questManager = TestEnchants.getInstance().getQuestManager();

    // Remove any existing quests for the player
    Set<String> currentQuests = new HashSet<>(questManager.getActiveQuests(target));
    for (String questKey : currentQuests) {
        questManager.removeQuest(target, questKey);
    }

    // Reset quest completed stat
    statTracker.setPlayerStat(uuid, "quests_completed", 0, false);

    // Get next rank for quest prefix
    String nextRank = RankUtils.getNextRank(newRank);
    if (nextRank != null) {
        String questPrefix = newRank + "-" + nextRank;

        // Add new quests for the rank
        questManager.addQuest(target, questPrefix + "-quest1");
        questManager.addQuest(target, questPrefix + "-quest2");

        // Set "at rank start" stats with the correct quest prefix (no double nextRank)
        int currentBlocks = statTracker.getPlayerStat(uuid, "blocks_broken", false);
        int currentEssence = statTracker.getPlayerStat(uuid, "earned_essence", false);
        int currentCrates = statTracker.getPlayerStat(uuid, "crate_total", false);
        int currentFiller = statTracker.getPlayerStat(uuid, "filler_sold", false);
        int currentShards = statTracker.getPlayerStat(uuid, "crafted_essence", false);
        int currentExtractor = statTracker.getPlayerStat(uuid, "crafted_extractors", false); 

        statTracker.setPlayerStat(uuid, "blocks_broken_at_rank_start." + questPrefix, currentBlocks, false);
        statTracker.setPlayerStat(uuid, "earned_essence_at_rank_start." + questPrefix, currentEssence, false);
        statTracker.setPlayerStat(uuid, "total_crates_at_rank_start." + questPrefix, currentCrates, false);
        statTracker.setPlayerStat(uuid, "filler_sold_at_rank_start." + questPrefix, currentFiller, false);
        statTracker.setPlayerStat(uuid, "crafted_essence_at_rank_start." + questPrefix, currentShards, false);
        statTracker.setPlayerStat(uuid,"crafted_extractor_at_rank_start." + questPrefix, currentExtractor, false);

        // Set enchants_applied stats at rank start with nextRank suffix
        for (String rarity : new String[]{"common", "uncommon", "rare", "epic", "legendary"}) {
            String statKey = "enchants_applied_" + rarity;
            int current = statTracker.getPlayerStat(uuid, statKey, false);
            statTracker.setPlayerStat(uuid, statKey + "_at_rank_start." + nextRank, current, false);
        }
    }

    sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s rank to " + newRank.toUpperCase() + ".");
    if (target.isOnline()) {
        target.sendMessage(ChatColor.GREEN + "Your rank has been set to " + newRank.toUpperCase() + ".");
        TestEnchants.getInstance().getScoreboardUtil().updateScoreboard(target);
    }

    return true;
}

            case "essencenotif" -> {
    if (EssenceGenerationListener.essenceNotifDisabled.contains(player.getUniqueId())) {
        EssenceGenerationListener.essenceNotifDisabled.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Essence notifications are now ENABLED.");
    } else {
        EssenceGenerationListener.essenceNotifDisabled.add(player.getUniqueId());
        player.sendMessage(ChatColor.RED + "Essence notifications are now DISABLED.");
    }
    return true;
}
            case "resetmine" -> {
    if (!sender.hasPermission("customenchants.resetmine")) {
        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        return true;
    }

    if (args.length != 1) {
        sender.sendMessage(ChatColor.RED + "Usage: /resetmine <mineName>");
        return true;
    }

    String mineName = args[0].toLowerCase();

    TestEnchants.getInstance().teleportPlayersOutOfMine(mineName);
    
    // Call your mine reset logic here
    boolean success = TestEnchants.getInstance().getMineManager().resetMine(mineName, TestEnchants.getInstance());

    if (success) {
        sender.sendMessage(ChatColor.GREEN + "Mine '" + mineName + "' has been reset.");
    } else {
        sender.sendMessage(ChatColor.RED + "Mine '" + mineName + "' does not exist or could not be reset.");
    }

    return true;
}
            case "pv" -> {
    int index;

    if (args.length == 0) {
        index = 1; // default to vault 1
    } else if (args.length == 1) {
        try {
            index = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid vault number.");
            return true;
        }
    } else {
        player.sendMessage(ChatColor.RED + "Usage: /pv [1-5]");
        return true;
    }

    if (index < 1 || index > 5) {
        player.sendMessage(ChatColor.RED + "Vault must be between 1 and 5.");
        return true;
    }

    if (player.getOpenInventory().getTitle().startsWith("Vault")) {
        player.sendMessage(ChatColor.RED + "You already have a vault open!");
        return true;
    }

    Inventory vault = vaultManager.getVault(player.getUniqueId(), index);
    player.openInventory(vault);
    return true;
}

            case "pvsee" -> {
    if (!sender.hasPermission("customenchants.pvsee")) {
        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        return true;
    }

    if (args.length != 2) {
        sender.sendMessage(ChatColor.RED + "Usage: /pvsee <player> <1-5>");
        return true;
    }

    Player target = Bukkit.getPlayerExact(args[0]);
    if (target == null) {
        sender.sendMessage(ChatColor.RED + "Player not found.");
        return true;
    }

    int index;
    try {
        index = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
        sender.sendMessage(ChatColor.RED + "Invalid vault number.");
        return true;
    }

    if (index < 1 || index > 5) {
        sender.sendMessage(ChatColor.RED + "Vault must be between 1 and 5.");
        return true;
    }

    Inventory vault = vaultManager.getVault(target.getUniqueId(), index);
    player.openInventory(vault);
    return true;
}
            case "sell" -> {
    String mode = (args.length > 0 && args[0].equalsIgnoreCase("hand")) ? "hand" : "all";

    SellManager sellManager = TestEnchants.getInstance().getSellManager();
    double total = 0;
    double fillerTotal = 0;  // Track filler sold value separately

    if (mode.equals("hand")) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR || !sellManager.isSellable(item.getType())) {
            player.sendMessage(ChatColor.RED + "You're not holding a sellable item.");
            return true;
        }
        double price = SellManager.getPrice(item.getType()) * item.getAmount();
        total += price;

        if (sellManager.isFillerBlock(item.getType())) {
            fillerTotal += price;
        }

        player.getInventory().setItemInMainHand(null);
    } else {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            if (sellManager.isSellable(item.getType())) {
                double price = SellManager.getPrice(item.getType()) * item.getAmount();
                total += price;

                if (sellManager.isFillerBlock(item.getType())) {
                    fillerTotal += price;
                }

                player.getInventory().clear(i);
            }
        }
    }

    if (total > 0) {
    String rank = RankUtils.getRank(player);
    int prestigeLevel = 0;

    // Check how many prestige levels the player has (up to 10)
    for (int i = 1; i <= 10; i++) {
        if (RankUtils.compareRanks(rank, "p" + i + "a") >= 0) {
            prestigeLevel = i;
        } else {
            break;
        }
    }

    double multiplier = 1.0 + (prestigeLevel * 0.01);
    double finalAmount = total * multiplier;

    VaultUtil.getEconomy().depositPlayer(player, finalAmount);
    player.sendMessage(ChatColor.GREEN + "Sold items for $" + String.format("%.2f", finalAmount));



        
        if (fillerTotal > 0) {
    UUID uuid = player.getUniqueId();
    TestEnchants.getInstance().statTracker.incrementPlayerStat(uuid, "filler_sold", (int) fillerTotal);

    // Get active quests
    QuestManager questManager = TestEnchants.getInstance().getQuestManager();
    Set<String> activeQuests = questManager.getActiveQuests(player);
    int currentFiller = TestEnchants.getInstance().statTracker.getPlayerStat(uuid, "filler_sold", false);

    for (String questKey : activeQuests) {
        RankQuest quest = questManager.get(questKey);
        if (quest == null || quest.extraObjective == null || !quest.extraObjective.startsWith("sell_filler:")) continue;

        String[] parts = quest.extraObjective.split(":");
        int required = Integer.parseInt(parts[1]);
        String baseRank = questKey.split("-quest")[0]; // e.g., "a-b" from "a-b-quest2"

        int fillerAtStart = TestEnchants.getInstance().statTracker.getPlayerStat(uuid, "filler_sold_at_rank_start." + baseRank, false);
        int fillerSinceStart = currentFiller - fillerAtStart;

        if (fillerSinceStart >= required) {
            questManager.completeQuest(player, questKey);
            player.sendMessage("§aQuest complete: Sell filler worth $" + required);
        }
    }
}
    } else {
        player.sendMessage(ChatColor.RED + "No sellable items found.");
    }

    return true;
}
            case "quest", "q" -> {
    if (args.length != 0) {
        player.sendMessage(ChatColor.RED + "Usage: /quest");
        return true;
    }
    player.setMetadata("viewing_quests", new FixedMetadataValue(TestEnchants.getInstance(), true));
    GuiUtil.openQuestGUI(player);
    return true;
}

            case "completequest" -> {
    if (!player.hasPermission("customenchants.admin")) {
        player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
        return true;
    }

    if (args.length != 2) {
        player.sendMessage(ChatColor.RED + "Usage: /completequest <1|2> <player>");
        return true;
    }

    int questNumber;
    try {
        questNumber = Integer.parseInt(args[0]);
        if (questNumber != 1 && questNumber != 2) throw new NumberFormatException();
    } catch (NumberFormatException e) {
        player.sendMessage(ChatColor.RED + "Quest number must be 1 or 2.");
        return true;
    }

    Player target = Bukkit.getPlayer(args[1]);
    if (target == null || !target.isOnline()) {
        player.sendMessage(ChatColor.RED + "That player is not online.");
        return true;
    }

    Set<String> active = questManager.getActiveQuests(target);
    List<String> sorted = new ArrayList<>(active).stream().sorted().toList();

    if (questNumber > sorted.size()) {
        player.sendMessage(ChatColor.RED + "That player doesn't have that many active quests.");
        return true;
    }

    String questKey = sorted.get(questNumber - 1);
    questManager.completeQuest(target, questKey);
    target.sendMessage(ChatColor.GREEN + "Quest completed by admin: " + ChatColor.YELLOW + questKey);
    player.sendMessage(ChatColor.GREEN + "You completed " + target.getName() + "'s quest: " + ChatColor.YELLOW + questKey);
    return true;
}
            case "nightvision", "nv" -> {
    PersistentDataContainer container = player.getPersistentDataContainer();
    NamespacedKey nvkey = new NamespacedKey(testEnchants, "night_vision_enabled");

    boolean enabled = container.has(nvkey, PersistentDataType.INTEGER) && container.get(nvkey, PersistentDataType.INTEGER) == 1;

    if (enabled) {
        // Disable night vision
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        container.remove(nvkey);
        player.sendMessage(ChatColor.RED + "Night vision disabled.");
    } else {
        // Enable night vision indefinitely
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
        container.set(nvkey, PersistentDataType.INTEGER, 1);
        player.sendMessage(ChatColor.GREEN + "Night vision enabled.");
    }

    return true;
}
            case "spawntrader" -> {
    if (!player.hasPermission("customenchants.admin")) {
        player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
        return true;
    }

    if (args.length != 1) {
        player.sendMessage(ChatColor.RED + "Usage: /customenchants spawntrader <essence|deep>");
        return true;
    }

    Location loc = player.getLocation();

    switch (args[0].toLowerCase()) {
        case "essence" -> {
            VillagerSpawner.spawnCustomVillager(loc, "Essence Trader", Villager.Profession.LIBRARIAN);
            player.sendMessage(ChatColor.GREEN + "Spawned Essence Trader.");
        }
        case "deep" -> {
            VillagerSpawner.spawnCustomVillager(loc, "Deep Essence Trader", Villager.Profession.CLERIC);
            player.sendMessage(ChatColor.GREEN + "Spawned Deep Essence Trader.");
        }
        case "tinkerer" -> {
            VillagerSpawner.spawnCustomVillager(loc, "Tinkerer", Villager.Profession.TOOLSMITH);
            player.sendMessage(ChatColor.GREEN + "Spawned Tinkerer.");
        }
        case "extractor" -> {
            VillagerSpawner.spawnCustomVillager(loc, "Extractor Trader", Villager.Profession.TOOLSMITH);
            player.sendMessage(ChatColor.GREEN + "Spawned Extractor Trader.");
        }
        case "deepextractor" -> {
            VillagerSpawner.spawnCustomVillager(loc, "Deep Extractor Trader", Villager.Profession.TOOLSMITH);
            player.sendMessage(ChatColor.GREEN + "Spawned Deep Extractor Trader.");
        }
        case "furnace" -> {
            VillagerSpawner.spawnCustomVillager(loc, "Furnace Upgrader", Villager.Profession.TOOLSMITH);
            player.sendMessage(ChatColor.GREEN + "Spawned Furnace Upgrader");
        }
        default -> player.sendMessage(ChatColor.RED + "Unknown trader type: " + args[0]);
    }

    return true;
}
            case "savewarp" -> {
    if (args.length != 1) {
        player.sendMessage(ChatColor.RED + "Usage: /savewarp <mineName>");
        return true;
    }

    String mineName = args[0].toLowerCase();
    Location loc = player.getLocation();

    WarpManager warpManager = new WarpManager(TestEnchants.getInstance()); // use your plugin instance
    warpManager.saveWarp(mineName, loc);

    player.sendMessage(ChatColor.GREEN + "Saved warp location for '" + mineName + "' at your position.");
    return true;
}
            case "removewarp" -> {
    if (args.length != 1) {
        player.sendMessage(ChatColor.RED + "Usage: /removewarp <mineName>");
        return true;
    }

    String mineName = args[0].toLowerCase();
    WarpManager warpManager = new WarpManager(TestEnchants.getInstance());

    if (!warpManager.removeWarp(mineName)) {
        player.sendMessage(ChatColor.RED + "No saved warp found for '" + mineName + "'.");
        return true;
    }

    player.sendMessage(ChatColor.YELLOW + "Removed warp location for '" + mineName + "'.");
    return true;
}
            case "cell" -> {
    if (args.length == 0) {
    if (!(sender instanceof Player)) {
        sender.sendMessage(ChatColor.RED + "Only players can use this command.");
        return true;
    }

    if (!CellManager.hasCell(player.getUniqueId())) {
        player.sendMessage(ChatColor.RED + "You do not own a cell.");
        return true;
    }

    Location loc = CellManager.getCellTeleportLocation(player.getUniqueId());
    if (loc == null) {
        player.sendMessage(ChatColor.RED + "Failed to find your cell.");
        return true;
    }

    player.teleport(loc);
    player.sendMessage(ChatColor.GREEN + "Teleported to your cell.");
    return true;
}

String subCommand = args[0].toLowerCase();
if (sender instanceof Player) {
    player = (Player) sender;
}

    switch (subCommand) {
        case "create" -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }

            // Check rank
            String rank = RankUtils.getRank(player);
            if (RankUtils.compareRanks(rank, "p1a") < 0) {
                player.sendMessage(ChatColor.RED + "You must be at least rank P1A to create a cell.");
                return true;
            }

            // Check if they already have a cell
            if (CellManager.hasCell(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You already own a cell.");
                return true;
            }

            // Check money
            double cost = 500_000;
            Economy economy = VaultUtil.getEconomy();

            if (economy == null) {
                player.sendMessage(ChatColor.RED + "Economy system is not available.");
                return true;
            }

            if (!economy.has(player, cost)) {
                player.sendMessage(ChatColor.RED + "You need $" + String.format("%,.0f", cost) + " to create a cell.");
                return true;
            }

            economy.withdrawPlayer(player, cost);

            boolean success = CellManager.createPlayerCell(player);

            if (success) {
                player.sendMessage(ChatColor.GREEN + "Your prestige cell has been created!");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to create your cell. Please contact staff.");
            }

            return true;
        }
        case "invite" -> {
    if (args.length < 2) {
        player.sendMessage(ChatColor.RED + "Usage: /cell invite <player>");
        return true;
    }

    UUID inviterId = player.getUniqueId();
    if (!CellManager.hasCell(inviterId)) {
        player.sendMessage(ChatColor.RED + "You don't own a cell.");
        return true;
    }

    CellUtil.PlayerCellData data = CellUtil.getPlayerCellData(inviterId);
    if (data == null) {
        player.sendMessage(ChatColor.RED + "Error: Your cell data could not be found.");
        return true;
    }

    if (data.getMembers().size() >= CellUtil.getMaxMembersForLevel(data.getMemberCapacityLevel())) {
        player.sendMessage(ChatColor.RED + "You have reached the maximum member limit for your cell.");
        return true;
    }

    Player target = Bukkit.getPlayer(args[1]);
    if (target == null || !target.isOnline()) {
        player.sendMessage(ChatColor.RED + "That player is not online.");
        return true;
    }

    if (target.getUniqueId().equals(inviterId)) {
        player.sendMessage(ChatColor.RED + "You cannot invite yourself to your cell.");
        return true;
    }

    pendingInvites.put(target.getUniqueId(), inviterId);
    player.sendMessage(ChatColor.GREEN + "Invite sent to " + target.getName());
    target.sendMessage(ChatColor.AQUA + player.getName() + " has invited you to their cell. Use /cell accept to join.");
    return true;
}
        case "upgrade" -> {
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Usage: /cell upgrade");
                return true;
            }
            GuiUtil.openCellUpgradeGUI(player);
            return true;
        }
        case "delete" -> {

    if (!sender.hasPermission("cells.delete")) {
        sender.sendMessage(ChatColor.RED + "You don't have permission to delete cells.");
        return true;
    }

    if (!(sender instanceof Player) && args.length != 1) {
        sender.sendMessage(ChatColor.RED + "Usage: /cell delete <player>");
        return true;
    }

    String targetName;
    if (args.length == 1) {
        targetName = args[0];
    } else if (sender instanceof Player) {
        targetName = ((Player) sender).getName();
    } else {
        sender.sendMessage(ChatColor.RED + "Usage: /cell delete <player>");
        return true;
    }

    Player target = Bukkit.getPlayerExact(targetName);
    if (target == null) {
        sender.sendMessage(ChatColor.RED + "Player not found or not online.");
        return true;
    }

    UUID targetId = target.getUniqueId();

    boolean hasCell = CellManager.hasCell(targetId);
    World world = Bukkit.getWorld("cell_world");

    boolean hasRegion = false;
    if (world != null) {
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionManager != null) {
            String regionId = "cell_" + targetId.toString().replace("-", "");
            hasRegion = regionManager.hasRegion(regionId);
        }
    }

    if (!hasCell && !hasRegion) {
        sender.sendMessage(ChatColor.RED + targetName + " does not own a cell.");
        return true;
    }
    boolean removed = CellManager.deletePlayerCell(targetId); // You need to implement this method

    if (removed) {
        sender.sendMessage(ChatColor.GREEN + "Deleted cell for " + targetName + ".");
        target.sendMessage(ChatColor.RED + "Your cell was deleted by a developer.");
    } else {
        sender.sendMessage(ChatColor.RED + "Failed to delete cell for " + targetName + ".");
    }
    return true;
}
        case "accept" -> {
    UUID inviteeId = player.getUniqueId();
    if (!pendingInvites.containsKey(inviteeId)) {
        player.sendMessage(ChatColor.RED + "You don't have any pending cell invites.");
        return true;
    }

    UUID ownerId = pendingInvites.remove(inviteeId);
    CellManager.CellData data = CellManager.getCellData(ownerId);
    if (data == null) {
        player.sendMessage(ChatColor.RED + "That cell no longer exists.");
        return true;
    }

    World world = Bukkit.getWorld("cell_world");
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
    if (regionManager == null) {
        player.sendMessage(ChatColor.RED + "Region manager error.");
        return true;
    }

    ProtectedCuboidRegion region = (ProtectedCuboidRegion) regionManager.getRegion(data.regionId);
    if (region == null) {
        player.sendMessage(ChatColor.RED + "Cell region not found.");
        return true;
    }

    region.getMembers().addPlayer(inviteeId);
    player.sendMessage(ChatColor.GREEN + "You've joined the cell!");
    return true;
}
        case "kick" -> {
    if (args.length < 2) {
        player.sendMessage(ChatColor.RED + "Usage: /cell kick <player>");
        return true;
    }

    UUID ownerId = player.getUniqueId();
    if (!CellManager.hasCell(ownerId)) {
        player.sendMessage(ChatColor.RED + "You don't own a cell.");
        return true;
    }

    Player target = Bukkit.getPlayer(args[1]);
    if (target == null || !target.isOnline()) {
        player.sendMessage(ChatColor.RED + "That player is not online.");
        return true;
    }

    UUID targetId = target.getUniqueId();
    if (targetId.equals(ownerId)) {
        player.sendMessage(ChatColor.RED + "You cannot kick yourself.");
        return true;
    }

    CellManager.CellData data = CellManager.getCellData(ownerId);
    if (data == null) {
        player.sendMessage(ChatColor.RED + "Cell data not found.");
        return true;
    }

    World world = Bukkit.getWorld("cell_world");
    RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    if (regionManager == null) {
        player.sendMessage(ChatColor.RED + "Region manager error.");
        return true;
    }

    ProtectedCuboidRegion region = (ProtectedCuboidRegion) regionManager.getRegion(data.regionId);
    if (region == null) {
        player.sendMessage(ChatColor.RED + "Region not found.");
        return true;
    }

    if (!region.getMembers().contains(targetId)) {
        player.sendMessage(ChatColor.RED + target.getName() + " is not a member of your cell.");
        return true;
    }

    region.getMembers().removePlayer(targetId);
    player.sendMessage(ChatColor.YELLOW + "You kicked " + target.getName() + " from your cell.");
    target.sendMessage(ChatColor.RED + "You were kicked from " + player.getName() + "'s cell.");
    return true;
}
        default -> {
        // Invalid argument, send usage message or error
        if (sender instanceof Player) {
            player.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /cell <create|invite|upgrade|delete>");
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
        }
        return true;
    }
        
}
}
            case "rankupmax" -> {
    UUID uuid = player.getUniqueId();
    String currentRank = RankUtils.getRank(player);

    if (RankUtils.compareRanks(currentRank, "p1a") < 0) {
        player.sendMessage(ChatColor.RED + "You must be at least rank p1a to use this command.");
        return true;
    }

    if (currentRank.length() < 2 || !Character.isLetter(currentRank.charAt(currentRank.length() - 1))) {
    player.sendMessage(ChatColor.RED + "Invalid rank format: " + currentRank);
    return true;
}

    String prestigePrefix = currentRank.substring(0, currentRank.length() - 1); // e.g., "p1"
    String prestigeCap = prestigePrefix + "z";

    double balance = economy.getBalance(player);

    // Cache original essence counts for all tiers
    Map<Integer, Integer> essenceInventory = new HashMap<>();
    Map<Integer, Integer> essenceOriginal = new HashMap<>();
    for (int i = 1; i <= 8; i++) {
        int amount = essenceManager.getEssence(player, i);
        essenceInventory.put(i, amount);
        essenceOriginal.put(i, amount);
    }

    int rankups = 0;
    String lastTrackedPrefix = null;

    while (true) {
        String nextRank = RankUtils.getNextRank(currentRank);
        if (nextRank == null || RankUtils.compareRanks(nextRank, prestigeCap) > 0) break;

        RankCost cost = RankUtils.getRankCost(nextRank, currentRank);
        if (cost == null) break;

        // Check if player can afford money and essence cost
        if (balance < cost.money) {
            player.sendMessage(ChatColor.RED + "Insufficient money to rank up to " + nextRank);
            break;
        }

        int currentEssence = essenceInventory.getOrDefault(cost.essenceTier, 0);
        if (currentEssence < cost.essence) {
            player.sendMessage(ChatColor.RED + "Insufficient essence tier " + cost.essenceTier + " to rank up to " + nextRank);
            break;
        }

        // Deduct cost from cached values
        balance -= cost.money;
        essenceInventory.put(cost.essenceTier, currentEssence - cost.essence);

        // Update rank
        currentRank = nextRank;
        rankManager.setRank(player, currentRank);
        rankups++;

        // Add quests at start of prestige tier only (if ends with 'a')
        if (currentRank.endsWith("a")) {
            String questKey = currentRank + "-" + RankUtils.getNextRank(currentRank);
            questManager.addQuest(player, questKey + "-quest1");
            questManager.addQuest(player, questKey + "-quest2");
        }

        // Track stats once per prestige block (e.g., p1)
        String currentPrefix = currentRank.substring(0, currentRank.length() - 1); // e.g., "p1"
        if (!currentPrefix.equals(lastTrackedPrefix)) {
            lastTrackedPrefix = currentPrefix;

            for (String rarity : new String[]{"common", "uncommon", "rare", "epic", "legendary"}) {
                String statKey = "enchants_applied_" + rarity;
                int current = statTracker.getPlayerStat(uuid, statKey, false);
                statTracker.setPlayerStat(uuid, statKey + "_at_rank_start." + currentPrefix, current, false);
            }

            int blocks = statTracker.getPlayerStat(uuid, "blocks_broken", false);
            int essence = statTracker.getPlayerStat(uuid, "earned_essence", false);
            int crates = statTracker.getPlayerStat(uuid, "crate_total", false);
            int filler = statTracker.getPlayerStat(uuid, "filler_sold", false);
            int shards = statTracker.getPlayerStat(uuid, "crafted_essence", false);
            int extractors = statTracker.getPlayerStat(uuid, "crafted_extractors", false);

            statTracker.setPlayerStat(uuid, "blocks_broken_at_rank_start." + currentPrefix, blocks, false);
            statTracker.setPlayerStat(uuid, "earned_essence_at_rank_start." + currentPrefix, essence, false);
            statTracker.setPlayerStat(uuid, "total_crates_at_rank_start." + currentPrefix, crates, false);
            statTracker.setPlayerStat(uuid, "filler_sold_at_rank_start." + currentPrefix, filler, false);
            statTracker.setPlayerStat(uuid, "crafted_essence_at_rank_start." + currentPrefix, shards, false);
            statTracker.setPlayerStat(uuid, "crafted_extractors_at_rank_start." + currentPrefix, extractors, false);
        }
    }

    if (rankups > 0) {
        player.sendMessage(ChatColor.GREEN + "You ranked up " + rankups + " time" + (rankups == 1 ? "" : "s") + " to " + RankUtils.formatRankName(currentRank) + "!");

        // Withdraw money spent
        economy.withdrawPlayer(player, economy.getBalance(player) - balance);

        // Remove the used essence based on difference between original and remaining cached essence
        for (Map.Entry<Integer, Integer> entry : essenceInventory.entrySet()) {
            int tier = entry.getKey();
            int remaining = entry.getValue();
            int original = essenceOriginal.getOrDefault(tier, 0);

            int used = original - remaining;
            if (used > 0) {
                essenceManager.removeEssence(player, tier, used);
            }
        }
    } else {
        player.sendMessage(ChatColor.RED + "You can't afford to rank up any further.");
    }

    return true;
}


            case "enchants" -> {
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) page = 1; // minimum page is 1
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid page number, defaulting to page 1.");
                page = 1;
            }
        }
        
        Inventory gui = GuiUtil.getEnchantInfoGUI(page);
        player.openInventory(gui);
        return true;
    }




}




        return false; // command not handled here
    }

    private int generateRandomChance(String rarity) {
        return switch (rarity.toUpperCase()) {
            case "COMMON" -> 70 + random.nextInt(21);
            case "UNCOMMON" -> 50 + random.nextInt(21);
            case "RARE" -> 30 + random.nextInt(21);
            case "EPIC" -> 15 + random.nextInt(16);
            case "LEGENDARY" -> 5 + random.nextInt(11);
            case "PRESTIGE", "PRESTIGE+" -> 1 + random.nextInt(5);
            default -> 50;
        };
    }

    private String getAvailableEnchantsList() {
        List<String> enchants = new ArrayList<>();
        for (String enchant : EnchantmentData.ENCHANT_NAMES) {
            enchants.add(enchant.replace(' ', '_'));
        }
        return String.join(", ", enchants);
    }

    public class VillagerSpawner {
    public static void spawnCustomVillager(Location location, String name, Villager.Profession profession) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setCustomName(ChatColor.GOLD + name);
        villager.setCustomNameVisible(true);
        villager.setProfession(profession);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setCollidable(false);
        villager.setSilent(true);
        villager.setCanPickupItems(false);
    }
}

    public class Cost {
    public int tier;
    public int essence;

    public Cost(int tier, int essence) {
        this.tier = tier;
        this.essence = essence;
    }
}

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase();

        if ("giveenchant".equals(cmd)) {
            if (args.length == 1) {
                String partial = args[0].toLowerCase();
                List<String> completions = new ArrayList<>();
                for (String enchant : EnchantmentData.ENCHANT_NAMES) {
                    String cmdStyle = enchant.replace(' ', '_');
                    if (cmdStyle.toLowerCase().startsWith(partial)) {
                        completions.add(cmdStyle);
                    }
                }
                return completions;
            }
            if (args.length == 2) {
                String enchantName = args[0].replace('_', ' ');
                int enchantIndex = EnchantmentData.getEnchantmentIndex(enchantName);
                if (enchantIndex != -1) {
                    EnchantmentData.EnchantmentInfo enchantInfo = EnchantmentData.getEnchantmentInfo(enchantIndex);
                    String partialLevel = args[1];
                    List<String> levelCompletions = new ArrayList<>();
                    for (int i = 1; i <= enchantInfo.maxLevel; i++) {
                        String levelStr = Integer.toString(i);
                        if (levelStr.startsWith(partialLevel)) {
                            levelCompletions.add(levelStr);
                        }
                    }
                    return levelCompletions;
                }
            }
            if (args.length == 3) {
                List<String> chances = Arrays.asList("5", "10", "15", "20", "25", "50", "75", "100");
                String partialChance = args[2];
                List<String> completions = new ArrayList<>();
                for (String c : chances) {
                    if (c.startsWith(partialChance)) completions.add(c);
                }
                return completions;
            }
        }

        if ("givecustomitem".equals(cmd)) {
            if (args.length == 1) {
                String partial = args[0].toLowerCase().replace('_', ' ');
                List<String> completions = new ArrayList<>();
                for (int i = 0; i < customItemUtil.CUSTOM_ITEM.length; i++) {
                    customItemUtil.CustomItemInfo candidate = customItemUtil.getCustomItemInfo(i);
                    if (candidate == null) continue;

                    String rawName = candidate.getName();
                    String cleanName = ChatColor.stripColor(rawName);
                    if (cleanName == null || cleanName.trim().isEmpty()) {
                        cleanName = rawName.replaceAll("§[0-9A-FK-ORa-fk-or]", "");
                    }

                    if (cleanName.toLowerCase().startsWith(partial)) {
                        completions.add(cleanName.replace(' ', '_'));
                    }
                }
                return completions;
            }
        }

        if ("setrank".equals(cmd)) {
    if (args.length == 1) {
        String partialPlayer = args[0].toLowerCase();
        List<String> matchingPlayers = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().toLowerCase().startsWith(partialPlayer)) {
                matchingPlayers.add(online.getName());
            }
        }
        return matchingPlayers;
    } 
}

        if ("resetmine".equals(cmd)) {
    if (args.length == 1) {
        String partial = args[0].toLowerCase();
        YamlConfiguration config = TestEnchants.getInstance().getMineManager().getMinesConfig();
        if (config == null || config.getConfigurationSection("mines") == null) {
            return Collections.emptyList();
        }

        Set<String> mineNames = config.getConfigurationSection("mines").getKeys(false);
        List<String> completions = new ArrayList<>();

        for (String name : mineNames) {
            if (name.toLowerCase().startsWith(partial)) {
                completions.add(name); // preserve original case for display
            }
        }

        return completions;
    }
}

        if ("pvsee".equals(cmd)) {
    if (args.length == 1) {
        String partial = args[0].toLowerCase();
        List<String> matchingPlayers = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().toLowerCase().startsWith(partial)) {
                matchingPlayers.add(online.getName());
            }
        }
        return matchingPlayers;
    }

    if (args.length == 2) {
        String partial = args[1];
        List<String> vaultNumbers = Arrays.asList("1", "2", "3", "4", "5");
        List<String> completions = new ArrayList<>();
        for (String num : vaultNumbers) {
            if (num.startsWith(partial)) {
                completions.add(num);
            }
        }
        return completions;
    }
}

        if ("sell".equals(cmd)) {
    if (args.length == 1) {
        String partial = args[0].toLowerCase();
        List<String> options = Arrays.asList("hand", "all");
        List<String> completions = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(partial)) {
                completions.add(option);
            }
        }
        return completions;
    }
}

        if ("completequest".equals(cmd)) {
    if (args.length == 1) {
        List<String> options = Arrays.asList("1", "2");
        String partial = args[0];
        List<String> completions = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(partial)) {
                completions.add(option);
            }
        }
        return completions;
    }

    if (args.length == 2) {
        String partial = args[1].toLowerCase();
        List<String> matchingPlayers = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().toLowerCase().startsWith(partial)) {
                matchingPlayers.add(online.getName());
            }
        }
        return matchingPlayers;
    }
}

        if ("spawntrader".equalsIgnoreCase(cmd)) {
    if (args.length == 1) {
        String partial = args[0].toLowerCase();
        List<String> options = Arrays.asList("essence", "deep", "tinkerer", "extractor", "deepextractor","furnace");
        List<String> completions = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(partial)) {
                completions.add(option);
            }
        }
        return completions;
    }
}

        if ("cell".equalsIgnoreCase(cmd)) {
    if (args.length == 1) {
        List<String> options = Arrays.asList("create", "invite", "upgrade", "delete", "kick", "accept");
        String partial = args[0].toLowerCase();
        List<String> completions = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(partial)) {
                completions.add(option);
            }
        }
        return completions;
    }

    if (args.length == 2) {
        String sub = args[0].toLowerCase();
        if (sub.equals("invite") || sub.equals("kick") || sub.equals("delete")) {
            String partial = args[1].toLowerCase();
            List<String> matchingPlayers = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(partial)) {
                    matchingPlayers.add(online.getName());
                }
            }
            return matchingPlayers;
        }
    }
}

        return Collections.emptyList();
    }

    




    


}
