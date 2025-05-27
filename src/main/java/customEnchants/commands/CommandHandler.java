package customEnchants.commands;

import customEnchants.utils.EnchantmentData;
import customEnchants.utils.GuiUtil;
import customEnchants.utils.customItemUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final Random random = new Random();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }


        //Scrap menu
        if (command.getName().equalsIgnoreCase("scrap")) {
            Inventory gui = GuiUtil.createScrapGui(player);
            player.openInventory(gui);
            return true;
        }


        //Give Enchant 
        if (command.getName().equalsIgnoreCase("giveenchant")) {
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
            Integer customChance = null;

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

            ItemStack book = EnchantmentData.createEnchantedBook(enchantInfo, level, chance);

            player.getInventory().addItem(book);
            player.sendMessage(ChatColor.GREEN + "Given " + enchantInfo.name + " Level " + level + " with " + chance + "% chance");
            return true;
        }



        //Give Custom Item
        if (command.getName().equalsIgnoreCase("giveCustomItem")) {
    if (args.length < 1) {
        player.sendMessage(ChatColor.RED + "Usage: /giveCustomItem <itemName>");
        return true;
    }

    String inputName = String.join(" ", args).replace('_', ' ').toLowerCase();
    String matchedName = null;

    for (String itemName : customItemUtil.CUSTOM_ITEM) {
        String stripped = ChatColor.stripColor(itemName).toLowerCase();
        if (stripped.equals(inputName)) {
            matchedName = itemName; // Use original (with color) for creation
            break;
        }
    }

    if (matchedName == null) {
        player.sendMessage(ChatColor.RED + "Invalid custom item name: " + inputName);
        return true;
    }

    ItemStack customItem = customItemUtil.createCustomItem(matchedName);
    if (customItem == null) {
        player.sendMessage(ChatColor.RED + "Failed to create custom item.");
        return true;
    }

    player.getInventory().addItem(customItem);
    player.sendMessage(ChatColor.GREEN + "You have been given: " + customItem.getItemMeta().getDisplayName());
    return true;
}




    if (command.getName().equalsIgnoreCase("testblackscroll")) {
    if (!(sender instanceof Player)) {
        sender.sendMessage("Only players can use this command.");
        return true;
    }

    Inventory gui = GuiUtil.blackScrollInventory(player); // Replace YourClassName with your class name
    player.openInventory(gui);
    return true;
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
            case "PRESTIGE" -> 1 + random.nextInt(5);
            case "PRESTIGE+" -> 1 + random.nextInt(5);
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("giveenchant")) {
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
                        String levelStr = String.valueOf(i);
                        if (levelStr.startsWith(partialLevel)) {
                            levelCompletions.add(levelStr);
                        }
                    }
                    return levelCompletions;
                }
            }
            if (args.length == 3) {
                String partialChance = args[2];
                List<String> chanceCompletions = new ArrayList<>();
                int[] commonChances = {10, 25, 50, 75, 90, 100};
                for (int chance : commonChances) {
                    String chanceStr = String.valueOf(chance);
                    if (chanceStr.startsWith(partialChance)) {
                        chanceCompletions.add(chanceStr);
                    }
                }
                return chanceCompletions;
            }
            return Collections.emptyList();
        }
        if (command.getName().equalsIgnoreCase("giveCustomItem")) {
    if (args.length == 1) {
        List<String> completions = new ArrayList<>();
        String partial = args[0].replace('_', ' ').toLowerCase();

        for (String itemName : customItemUtil.CUSTOM_ITEM) {
            String stripped = ChatColor.stripColor(itemName).toLowerCase();
            if (stripped.startsWith(partial)) {
                completions.add(stripped.replace(' ', '_')); // return with underscores
            }
        }

        return completions;
    }
}


return Collections.emptyList();
    }
}
