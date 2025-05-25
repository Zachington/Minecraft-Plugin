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
import org.bukkit.plugin.Plugin;

import customEnchants.utils.EnchantmentData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryListener implements Listener {

    private static final Random random = new Random();
    private static final Pattern SUCCESS_PATTERN = Pattern.compile("Success Rate: (\\d+)%");
    private final Plugin yourPluginInstance; // Change Object to your actual plugin class
    



    public InventoryListener(Plugin pluginInstance) {
        this.yourPluginInstance = pluginInstance;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        handleEnchantmentApplication(event, player);
    }

    
    // -- The rest of your existing methods below remain unchanged --

    private void handleEnchantmentApplication(InventoryClickEvent event, Player player) {
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

        if (random.nextInt(100) < book.successRate) {
            applyEnchant(clicked, tool, book);
            player.sendMessage(ChatColor.GREEN + "Applied " + book.name + " " + book.level + "!");
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.5f);
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

        if (book.name.equalsIgnoreCase("Preservation") && tool.map.containsKey("Unbreakable")) {
            player.sendMessage(ChatColor.RED + "Preservation cannot be used on Unbreakable items!");
            return false;
        }

        if (tool.map.containsKey(book.name)) {
            int current = tool.map.get(book.name);
            if (current >= info.maxLevel) {
                player.sendMessage(ChatColor.RED + book.name + " is already maxed out!");
                return false;
            }
            if (book.level != current) {
                player.sendMessage(ChatColor.RED + "To upgrade " + book.name + ", use a Level " + current + " book.");
                return false;
            }
        } else if (tool.map.size() >= getMaxEnchantCount(type)) {
            player.sendMessage(ChatColor.RED + "This item already has the max number of enchantments!");
            return false;
        }

        return true;
    }

    private int getMaxEnchantCount(Material type) {
        return type.toString().contains("NETHERITE") ? 10 : 9;
    }

    private void applyEnchant(ItemStack tool, ToolEnchantInfo toolInfo, EnchantmentBookInfo book) {
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) return;

        int newLevel = book.level;
        if (toolInfo.map.containsKey(book.name)) {
            int current = toolInfo.map.get(book.name);
            newLevel = Math.min(current + 1, EnchantmentData.getEnchantmentInfo(
                    EnchantmentData.getEnchantmentIndex(book.name)).maxLevel);
        }

        toolInfo.map.put(book.name, newLevel);
        if (book.name.equalsIgnoreCase("Unbreakable")) {
            toolInfo.map.remove("Preservation");
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
                    lore.add(color + e.getKey() + " " + toRoman(e.getValue()));
                });

        return lore;
    }

    private String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(number);
        };
    }

    private int getRarityRank(String rarity) {
        return switch (rarity.toLowerCase()) {
            case "common" -> 1;
            case "uncommon" -> 2;
            case "rare" -> 3;
            case "epic" -> 4;
            case "legendary" -> 5;
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
