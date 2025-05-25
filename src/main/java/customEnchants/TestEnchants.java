package customEnchants;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import customEnchants.managers.PluginManager;
import customEnchants.listeners.AnvilCombineListener;
import customEnchants.listeners.InventoryListener;
import customEnchants.listeners.DurabilityEnchantListener;
import customEnchants.listeners.EnchantScrapListener;
import customEnchants.listeners.GiveItemListener;
import customEnchants.listeners.MagnetListener;
import customEnchants.listeners.BlockBreakListener;
import customEnchants.utils.EnchantmentData;
import customEnchants.utils.GuiUtil;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TestEnchants extends JavaPlugin {
    
    private static final Random random = new Random();

    @Override
    public void onEnable() {
        PluginManager.getInstance().initialize();
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new GiveItemListener(), this);
        getServer().getPluginManager().registerEvents(new MagnetListener(), this);
        getServer().getPluginManager().registerEvents(new DurabilityEnchantListener(), this);
        getServer().getPluginManager().registerEvents(new AnvilCombineListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantScrapListener(this), this);

        getLogger().info("CustomEnchant has been enabled!");

        this.getCommand("giveenchant").setExecutor(this);
        this.getCommand("giveenchant").setTabCompleter(this);
        this.getCommand("scrap").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("scrap")) {
            Inventory gui = GuiUtil.createScrapGui(player);
            player.openInventory(gui);
            return true;
        }

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

            ItemStack book = createEnchantedBook(enchantInfo, level, chance);

            player.getInventory().addItem(book);
            player.sendMessage(ChatColor.GREEN + "Given " + enchantInfo.name + " Level " + level + " with " + chance + "% chance");
            return true;
        }

        return false; // command not handled here
    }

    private ItemStack createEnchantedBook(EnchantmentData.EnchantmentInfo enchantInfo, int level, int chance) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta != null) {
            String rarityColor = EnchantmentData.getRarityColor(enchantInfo.rarity);
            String displayName = rarityColor + enchantInfo.name + " " + getRomanNumeral(level);
            meta.setDisplayName(displayName);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.WHITE + enchantInfo.lore);
            lore.add("");
            lore.add(ChatColor.GREEN + "Success Rate: " + chance + "%");
            lore.add(EnchantmentData.getColoredRarity(enchantInfo.rarity));
            lore.add("");
            lore.add(ChatColor.GRAY + "Applicable to: " + formatToolTypes(enchantInfo.toolTypes));
            lore.add(ChatColor.GRAY + "Drag and drop onto item to apply");

            meta.setLore(lore);
            book.setItemMeta(meta);
        }

        return book;
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

    private String getRomanNumeral(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(number);
        };
    }

    private String formatToolTypes(String toolTypes) {
        if ("ALL".equals(toolTypes)) {
            return "All Tools";
        }
        return toolTypes.replace(",", ", ");
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
        return Collections.emptyList();
    }
}
