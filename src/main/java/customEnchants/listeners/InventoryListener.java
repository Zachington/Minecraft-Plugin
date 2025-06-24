package customEnchants.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import customEnchants.TestEnchants;
import customEnchants.utils.EnchantmentData;
import customEnchants.utils.InventoryParser;
import customEnchants.utils.RankQuest;
import customEnchants.utils.RankUtils;
import customEnchants.utils.StatTracker;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryListener implements Listener {

    private static final Random random = new Random();
    private static final Pattern SUCCESS_PATTERN = Pattern.compile("Success Rate: (\\d+)%");
    private final StatTracker statTracker;

    public InventoryListener (StatTracker statTracker) {
        this.statTracker = statTracker; 
    };
    


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        handleInventoryInteraction(event, player);

        //Dust Application
    if (cursor != null && cursor.getType() == Material.SUGAR) {
    int cursorRarity = InventoryParser.itemRarity(cursor);
    int clickedRarity = InventoryParser.itemRarity(clicked);

    if (cursorRarity != -1 && clickedRarity != -1 && cursorRarity == clickedRarity) {
        int boost = InventoryParser.getSuccessBoosterValue(cursor);
        int baseSuccess = InventoryParser.clickedSuccess(clicked);
        if (baseSuccess >= 100) {
            return; // Already maxed, skip applying booster
        }

        if (boost > 0 && baseSuccess >= 0) {
            int newSuccess = baseSuccess + boost;
            newSuccess = Math.min(newSuccess, 100); // Clamp to max 100%

            // Update the lore line
            ItemMeta meta = clicked.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 0; i < lore.size(); i++) {
                String clean = lore.get(i).replaceAll("§[0-9a-fk-or]", "").toLowerCase().trim();
                if (clean.startsWith("success rate:")) {
                    lore.set(i, "§7Success Rate: " + ChatColor.GREEN + newSuccess + "%");
                    break;
                }
            }

            meta.setLore(lore);
            clicked.setItemMeta(meta);

            // Consume the booster (1 item)
            cursor.setAmount(cursor.getAmount() - 1);
            event.setCancelled(true);

            }
        } else {
            return;
        }
        return;
    }
    
    //Durability Shard
    if (cursor != null && cursor.hasItemMeta() && cursor.getItemMeta().hasLore()) {
    List<String> lore = cursor.getItemMeta().getLore();
    if (lore != null && !lore.isEmpty()) {
        for (String line : lore) {
            String cleanLine = ChatColor.stripColor(line).toLowerCase();
            if (cleanLine.contains("durability")) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("repair (\\d+) durability").matcher(cleanLine);
                if (matcher.find()) {
                    int repairAmount = Integer.parseInt(matcher.group(1));
                    if (clicked != null && clicked.getType() != Material.AIR && clicked.getItemMeta() != null) {
                        if (clicked.getType().getMaxDurability() > 0) {
                            short currentDurability = clicked.getDurability();

                            if (currentDurability == 0) {
                                player.sendMessage(ChatColor.YELLOW + "This item is already fully repaired!");
                                return;
                            }

                            int newDurability = currentDurability - repairAmount;
                            if (newDurability < 0) newDurability = 0;

                            clicked.setDurability((short) newDurability);

                            cursor.setAmount(cursor.getAmount() - 1);
                            if (cursor.getAmount() <= 0) {
                                event.setCursor(null);
                            } else {
                                event.setCursor(cursor);
                            }

                            player.sendMessage(ChatColor.GREEN + "Repaired " + repairAmount + " durability on your " + ChatColor.WHITE + clicked.getType().toString().toLowerCase().replace("_", " "));
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }
}




}

    private void handleInventoryInteraction(InventoryClickEvent event, Player player) {
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        if (!isEnchantedBook(cursor) || !isValidTool(clicked)) return;

        event.setCancelled(true);

        EnchantmentBookInfo book = parseBook(cursor);
        if (book == null) {
            player.sendMessage(ChatColor.RED + "Invalid enchanted book!");
            return;
        }

        if (!canApplyToTool(book.name, clicked.getType())) {
            player.sendMessage(ChatColor.RED + book.name + " cannot be applied to that item.");
            return;
        }

        ToolEnchantInfo tool = parseTool(clicked);
        if (!canAddEnchant(tool, book, clicked.getType(), player)) return;

        String rarity = EnchantmentData.getRarity(book.name);

        if ("PRESTIGE".equalsIgnoreCase(rarity)) {
        String playerRank = RankUtils.getRank(player);
            if (RankUtils.compareRanks(playerRank, "p1a") < 1) {
                player.sendMessage(ChatColor.RED + "You must be at least Prestige 1 to use this enchantment.");
                event.setCancelled(true);
            return;
            }
        }

        if ("PRESTIGE+".equalsIgnoreCase(rarity)) {
            String playerRank = RankUtils.getRank(player);
            if (RankUtils.compareRanks(playerRank, "p10a") < 1) {
                player.sendMessage(ChatColor.RED + "You must be at least Prestige 10 to use this enchantment.");
                event.setCancelled(true);
            return;
            }
        }       


        if (random.nextInt(100) < book.successRate) {
            applyEnchant(clicked, tool, book);
            player.sendMessage(ChatColor.GREEN + "Applied " + book.name + " " + book.level + "!");
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.5f);
            String statKey = "enchants_applied_" + rarity.toLowerCase();
            TestEnchants.getInstance().statTracker.incrementPlayerStat(player.getUniqueId(), statKey);

            // --- Begin quest check logic ---
            UUID uuid = player.getUniqueId();
            Set<String> activeQuests = TestEnchants.getInstance().getQuestManager().getActiveQuests(player);

            for (String questKey : activeQuests) {
                RankQuest quest = TestEnchants.getInstance().getQuestManager().get(questKey);
                if (quest == null || quest.extraObjective == null) continue;

            String[] parts = questKey.split("-quest");
            if (parts.length == 0) continue;

                if (quest.extraObjective.startsWith("apply_enchant:")) {
                    String requiredRarity = quest.extraObjective.split(":")[1].toLowerCase();
                    if (requiredRarity.equals(rarity.toLowerCase())) {
                        // Check if player has at least one of this enchant applied
                    int applied = TestEnchants.getInstance().statTracker.getPlayerStat(uuid, statKey, false);
                        if (applied > 0) {
                            TestEnchants.getInstance().getQuestManager().completeQuest(player, questKey);
                            player.sendMessage("§aQuest complete: Apply a " + requiredRarity + " enchant!");
                        }
                    }
                }
            }

        } else {
            player.sendMessage(ChatColor.RED + "Failed to apply " + book.name + " (" + book.successRate + "%)");
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 0.8f);
        }

        if (cursor.getAmount() > 1) cursor.setAmount(cursor.getAmount() - 1);
        else event.getView().setCursor(null);
    }

    private boolean isEnchantedBook(ItemStack item) {
        return item != null && item.getType() == Material.ENCHANTED_BOOK &&
            item.hasItemMeta() && item.getItemMeta().hasDisplayName();
    }

    private boolean isValidTool(ItemStack item) {
        if (item == null) return false;
        String name = item.getType().toString();
        return name.contains("PICKAXE") || name.contains("SWORD") || name.contains("AXE") ||
            name.contains("SHOVEL") || name.contains("HOE") || name.contains("BOOTS") ||
            name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS");
    }

    private EnchantmentBookInfo parseBook(ItemStack book) {
        ItemMeta meta = book.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.hasLore()) return null;

        String name = ChatColor.stripColor(meta.getDisplayName());
        String[] parts = name.split(" ");
        if (parts.length < 2) return null;

        String enchantName = String.join(" ", Arrays.copyOf(parts, parts.length - 1));
        int level = parseRoman(parts[parts.length - 1]);
        if (level == -1) return null;

        int rate = 50;
        for (String line : meta.getLore()) {
            Matcher m = SUCCESS_PATTERN.matcher(ChatColor.stripColor(line));
            if (m.find()) {
                rate = Integer.parseInt(m.group(1));
                break;
            }
        }

        return new EnchantmentBookInfo(enchantName, level, rate);
    }

    private int parseRoman(String roman) {
        return switch (roman.toUpperCase()) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            default -> {
                try {
                    yield Integer.parseInt(roman);
                } catch (NumberFormatException e) {
                    yield -1;
                }
            }
        };
    }

    private boolean canApplyToTool(String name, Material type) {
        int id = EnchantmentData.getEnchantmentIndex(name);
        if (id == -1) return false;

        EnchantmentData.EnchantmentInfo info = EnchantmentData.getEnchantmentInfo(id);
        if (info.toolTypes.equals("ALL")) return true;

        return Arrays.stream(info.toolTypes.split(","))
                    .anyMatch(t -> type.toString().contains(t.trim()));
    }

    private ToolEnchantInfo parseTool(ItemStack item) {
        ToolEnchantInfo info = new ToolEnchantInfo();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return info;

        for (String line : meta.getLore()) {
            String text = ChatColor.stripColor(line).trim();
            for (int i = 0; i < EnchantmentData.getEnchantmentCount(); i++) {
                EnchantmentData.EnchantmentInfo ei = EnchantmentData.getEnchantmentInfo(i);
                if (text.startsWith(ei.name + " ")) {
                    int level = parseRoman(text.substring(ei.name.length()).trim());
                    if (level != -1) info.map.put(ei.name, level);
                    break;
                }
            }
        }

        return info;
    }

    private boolean canAddEnchant(ToolEnchantInfo tool, EnchantmentBookInfo book, Material type, Player player) {
    int id = EnchantmentData.getEnchantmentIndex(book.name);
    if (id == -1) {
        player.sendMessage(ChatColor.RED + "Unknown enchantment: " + book.name);
        return false;
    }

    EnchantmentData.EnchantmentInfo info = EnchantmentData.getEnchantmentInfo(id);

    // Rank checks for Prestige and Prestige+
    String rarity = EnchantmentData.getRarity(book.name);
    String playerRank = RankUtils.getRank(player);

    if ("PRESTIGE".equalsIgnoreCase(rarity)) {
        if (RankUtils.compareRanks(playerRank, "p1a") < 0) {
            player.sendMessage(ChatColor.RED + "You must be Prestige 1 to apply " + book.name + "!");
            return false;
        }
    } else if ("PRESTIGE+".equalsIgnoreCase(rarity)) {
        if (RankUtils.compareRanks(playerRank, "p10a") < 0) {
            player.sendMessage(ChatColor.RED + "You must be Prestige 10 to apply " + book.name + "!");
            return false;
        }
    }

    if (book.name.equalsIgnoreCase("Preservation") && tool.map.containsKey("Unbreakable")) {
        player.sendMessage(ChatColor.RED + "Preservation cannot be used on Unbreakable items!");
        return false;
    }

    if (book.name.equalsIgnoreCase("Final Echo")) {
    Integer legendsEchoLevel = tool.map.get("Legends Echo");
    EnchantmentData.EnchantmentInfo legendsEchoInfo = EnchantmentData.getEnchantmentInfoByName("Legends Echo");
    if (legendsEchoLevel == null || legendsEchoLevel < legendsEchoInfo.maxLevel) {
        player.sendMessage(ChatColor.RED + "You must have Legends Echo at max level to apply Final Echo!");
        return false;
    }
    }

    if (tool.map.containsKey(book.name)) {
        int current = tool.map.get(book.name);
        if (current >= info.maxLevel) {
            player.sendMessage(ChatColor.RED + book.name + " is already maxed out!");
            return false;
        }
        if (book.level < current) {
            player.sendMessage(ChatColor.RED + "This book is too weak to apply. You already have Level " + current + ".");
            return false;
        }
    } else if (tool.map.size() >= RankUtils.getMaxEnchantCount(player, type)) {
        player.sendMessage(ChatColor.RED + "This item already has the max number of enchantments!");
        return false;
    }

    return true;
}

    private void applyEnchant(ItemStack tool, ToolEnchantInfo toolInfo, EnchantmentBookInfo book) {
    ItemMeta meta = tool.getItemMeta();
    if (meta == null) return;

    int newLevel;
    if (toolInfo.map.containsKey(book.name)) {
        int current = toolInfo.map.get(book.name);
        if (book.level > current) {
            // Book is higher level: just upgrade directly to book.level
            newLevel = book.level;
        } else if (book.level == current) {
            // Book level equals current: apply +1 logic capped at maxLevel
            newLevel = Math.min(current + 1, EnchantmentData.getEnchantmentInfo(
                EnchantmentData.getEnchantmentIndex(book.name)).maxLevel);
        } else {
            // Book level lower than current, don't apply any change
            // You can just keep current level or return early if you want
            newLevel = current;
        }
    } else {
        // No current enchantment, just set to book's level
        newLevel = book.level;
    }

    toolInfo.map.put(book.name, newLevel);

    if (book.name.equalsIgnoreCase("Unbreakable")) {
        toolInfo.map.remove("Preservation");
    }

    if (book.name.equalsIgnoreCase("Final Echo")) {
    toolInfo.map.remove("Legends Echo");
    }

    meta.setLore(buildLore(tool, toolInfo));
    tool.setItemMeta(meta);
}

    private List<String> buildLore(ItemStack tool, ToolEnchantInfo info) {
        List<String> lore = new ArrayList<>();
        if (!tool.getEnchantments().isEmpty()) lore.add("");

        info.map.entrySet().stream()
                .sorted(Comparator.comparingInt(e ->
                        -getRarityRank(EnchantmentData.getEnchantmentInfo(
                                EnchantmentData.getEnchantmentIndex(e.getKey())).rarity)))
                .forEach(e -> {
                    EnchantmentData.EnchantmentInfo data =
                            EnchantmentData.getEnchantmentInfo(EnchantmentData.getEnchantmentIndex(e.getKey()));
                    String color = EnchantmentData.getRarityColor(data.rarity);
                    lore.add(color + e.getKey() + " " + EnchantmentData.getRomanNumeral(e.getValue()));
                });

        return lore;
    }

    private int getRarityRank(String rarity) {
        return switch (rarity.toLowerCase()) {
            case "common" -> 1;
            case "uncommon" -> 2;
            case "rare" -> 3;
            case "epic" -> 4;
            case "legendary" -> 5;
            case "prestige" -> 6;
            case "prestige+" -> 7;
            default -> 0;
        };
    }
    private static class EnchantmentBookInfo {
        final String name;
        final int level;
        final int successRate;

        EnchantmentBookInfo(String name, int level, int successRate) {
            this.name = name;
            this.level = level;
            this.successRate = successRate;
        }
    }
    private static class ToolEnchantInfo {
        final Map<String, Integer> map = new HashMap<>();
    }


}

