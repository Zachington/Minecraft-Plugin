package customEnchants.commands;

import customEnchants.TestEnchants;
import customEnchants.listeners.EssenceGenerationListener;
import customEnchants.managers.RankManager;
import customEnchants.utils.EnchantmentData;
import customEnchants.utils.GuiUtil;
import customEnchants.utils.RankUtils;
import customEnchants.utils.RankUtils.RankCost;
import customEnchants.utils.customItemUtil;
import net.milkbowl.vault.economy.Economy;
import customEnchants.utils.EssenceManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;



public class CommandHandler implements CommandExecutor, TabCompleter {

    private final Random random = new Random();
    private final Economy economy;
    private final EssenceManager essenceManager;
    private final RankManager rankManager;
    

    public CommandHandler(TestEnchants plugin, Economy economy, RankManager rankManager) {
        this.economy = economy;
        this.rankManager = rankManager;
        this.essenceManager = plugin.getEssenceManager();
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

    if (nextRank == null) {
        player.sendMessage(ChatColor.RED + "You are already at the max rank!");
        return true;
    }

    RankCost cost = RankUtils.getRankCost(nextRank, currentRank);
    if (cost == null) {
        player.sendMessage(ChatColor.RED + "Rankup cost could not be calculated.");
        return true;
    }

    // Check essence with EssenceManager instance
    int playerEssence = essenceManager.getEssence(player, cost.essenceTier);
    if (playerEssence < cost.essence) {
        player.sendMessage(ChatColor.RED + "You need §e" + cost.essence + " Tier " + cost.essenceTier + " Essence§c to rank up.");
        return true;
    }

    // Check money with Vault economy
    double balance = economy.getBalance(player);
    if (balance < cost.money) {
        player.sendMessage(ChatColor.RED + "You need §a$" + String.format("%,.2f", cost.money) + "§c to rank up.");
        return true;
    }

    // Remove essence and money
    essenceManager.removeEssence(player, cost.essenceTier, cost.essence);
    economy.withdrawPlayer(player, cost.money);
    String oldRank = RankUtils.getRank(player);
    // Update rank in your player data system
    rankManager.setRank(player, nextRank);



    player.sendMessage(ChatColor.GREEN + "You ranked up to " + RankUtils.formatRankName(nextRank) + ChatColor.GREEN + "!");

    
    String newRank = nextRank;

if (RankUtils.isAscendUpgrade(oldRank, newRank)) {
    Bukkit.broadcastMessage("§6" + player.getName() + " §ehas §4Ascended §eto: " + ChatColor.DARK_RED + RankUtils.formatRankTierOnly(newRank) + "§e!");
} else if (RankUtils.isPrestigeUpgrade(oldRank, newRank)) {
    Bukkit.broadcastMessage("§6" + player.getName() + " §ehas §dPrestiged §eto: " + ChatColor.LIGHT_PURPLE + RankUtils.formatRankPrestigeOnly(newRank) + "§e!");
}

    return true;}       
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

    rankManager.setRank(target, newRank);

    sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s rank to " + newRank.toUpperCase() + ".");
    if (target.isOnline()) {
        target.sendMessage(ChatColor.GREEN + "Your rank has been set to " + newRank.toUpperCase() + ".");
        // Optionally update scoreboard immediately
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

        return Collections.emptyList();
    }






    


}
