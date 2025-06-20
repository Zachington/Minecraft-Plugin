package customEnchants.listeners;

import customEnchants.utils.EnchantmentData;
import customEnchants.utils.GuiUtil;
import customEnchants.utils.GuiUtil.EnchantParseResult;
import customEnchants.utils.customItemUtil;
import customEnchants.utils.RankUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.*;
import java.lang.reflect.Field;


public class AnvilCombineListener implements Listener {

    private final JavaPlugin plugin;

    public AnvilCombineListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAnvilCombine(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);

        if (first != null && first.getType() == Material.AIR) first = null;
        if (second != null && second.getType() == Material.AIR) second = null;

    // If both slots empty, do nothing
    if (first == null && second == null) return;

    // If only first slot has an item
    if (first != null && second == null) {
        handleSingleItemAnvil(event, first, inv);
        return;
    }

    // If only second slot has an item (probably invalid for anvil, but let's be safe)
    if (first == null && second != null) {
        handleSingleItemAnvil(event, first, inv);
        return;
    }

    // Now both items are non-null — do your combine logic here
    if (isCustomItem(first) || isCustomItem(second)) {
        event.setResult(null);
        return;
    }



        // 1) Handle enchanted book + enchanted book combination (existing logic)
        if (first.getType() == Material.ENCHANTED_BOOK && second.getType() == Material.ENCHANTED_BOOK) {
            handleBookCombine(event, first, second, inv);
            return;
        }

        // 2) Handle tool + tool combination (custom enchant merging + repair cost scaling)
        if (isValidTool(first) && isValidTool(second)) {
            handleToolCombine(event, first, second, inv);
        }
    }

    @EventHandler
    public void onAnvilClick(InventoryClickEvent event) {
    if (!(event.getInventory() instanceof AnvilInventory)) return;

    Location anvilLocation = event.getWhoClicked().getLocation().getBlock().getLocation(); 
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        Block block = anvilLocation.getBlock();
        if (block.getType().toString().contains("ANVIL")) {
            block.setType(Material.ANVIL);
        }
    }, 1L);
}

    private void handleBookCombine(PrepareAnvilEvent event, ItemStack first, ItemStack second, AnvilInventory inv) {
    EnchantParseResult result1 = GuiUtil.parseCustomEnchantBook(first);
    EnchantParseResult result2 = GuiUtil.parseCustomEnchantBook(second);
    if (result1 == null || result2 == null) return;

    if (!result1.name.equals(result2.name)) return;
    if (result1.level != result2.level) return;

    int enchantIndex = EnchantmentData.getEnchantmentIndex(result1.name);
    if (enchantIndex == -1) return;

    int maxLevel = EnchantmentData.getEnchantmentInfo(enchantIndex).maxLevel;
    if (result1.level >= maxLevel) return;

    int newLevel = result1.level + 1;
    double newChance = (result1.chance + result2.chance) / 2;

    ItemStack combined = GuiUtil.createCustomEnchantBook(result1.name, newLevel, newChance);
    event.setResult(combined);

    try {
        // Get the underlying NMS container of the anvil inventory
        Object nmsContainer = inv.getClass().getMethod("getHandle").invoke(inv);
        // Get the 'repairCost' field (name may differ based on server version)
        Field repairCostField = nmsContainer.getClass().getDeclaredField("repairCost");
        repairCostField.setAccessible(true);
        repairCostField.setInt(nmsContainer, 0);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private void handleToolCombine(PrepareAnvilEvent event, ItemStack first, ItemStack second, AnvilInventory inv) {
    if (first.getType() != second.getType()) {
        event.setResult(null);
        return;
    }

    ToolEnchantInfo tool1 = parseTool(first);
    ToolEnchantInfo tool2 = parseTool(second);

    Map<String, Integer> combinedEnchants = new HashMap<>();
    Set<String> allEnchants = new HashSet<>();
    allEnchants.addAll(tool1.map.keySet());
    allEnchants.addAll(tool2.map.keySet());

    for (String enchantName : allEnchants) {
        int lvl1 = tool1.map.getOrDefault(enchantName, 0);
        int lvl2 = tool2.map.getOrDefault(enchantName, 0);

        int enchantIndex = EnchantmentData.getEnchantmentIndex(enchantName);
        if (enchantIndex == -1) continue;

        int maxLevel = EnchantmentData.getEnchantmentInfo(enchantIndex).maxLevel;

        // Skip invalid combinations: both have enchant but different levels and neither is max
        if (lvl1 > 0 && lvl2 > 0 && lvl1 != lvl2 && lvl1 < maxLevel && lvl2 < maxLevel) {
            event.setResult(null);
            return;
        }

        int combinedLevel;
        if (lvl1 == lvl2 && lvl1 > 0 && lvl1 < maxLevel) {
            combinedLevel = lvl1 + 1;
        } else {
            combinedLevel = Math.max(lvl1, lvl2);
        }

        if (combinedLevel > 0) {
            combinedEnchants.put(enchantName, combinedLevel);
        }
    }

    if (combinedEnchants.isEmpty()) {
        // If no custom enchants, let Minecraft handle vanilla result
        return;
    }

    // Enforce max number of enchants
    Player player = (Player) event.getView().getPlayer();
    int maxEnchants = RankUtils.getMaxEnchantCount(player, first.getType());

    if (combinedEnchants.size() > maxEnchants) {
        player.sendMessage(ChatColor.RED + "You cannot apply more than " + maxEnchants + " enchantments for your rank.");
        event.setResult(null); // Cancel the combine
        return;
    }
    combinedEnchants = limitEnchantsByRarity(combinedEnchants, maxEnchants);


    ItemStack result = first.clone();
    ItemMeta meta = result.getItemMeta();
    if (meta == null) return;

    meta.setLore(buildLore(combinedEnchants));
    result.setItemMeta(meta);
    event.setResult(result);

    // Repair cost logic
    int firstRepairCost = getRepairCost(first);
    int secondRepairCost = getRepairCost(second);
    int maxRepairCost = Math.max(firstRepairCost, secondRepairCost);
    int newRepairCost = (maxRepairCost - 1) * 2 + 1;

    try {
        // Access the underlying NMS container and set repairCost field
        Object nmsContainer = inv.getClass().getMethod("getHandle").invoke(inv);
        Field repairCostField = nmsContainer.getClass().getDeclaredField("repairCost");
        repairCostField.setAccessible(true);
        repairCostField.setInt(nmsContainer, Math.max(newRepairCost, 0));
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private void handleSingleItemAnvil(PrepareAnvilEvent event, ItemStack item, AnvilInventory inv) {
    ItemStack first = inv.getItem(0);
    ItemStack second = inv.getItem(1);

    if (isCustomItem(first) || isCustomItem(second)) {
        event.setResult(null);
        return;
    }
}

    private int getRepairCost(ItemStack item) {
        if (item == null) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Repairable repairable) {
            return repairable.getRepairCost();
        }
        return 0;
    }

    private Map<String, Integer> limitEnchantsByRarity(Map<String, Integer> enchants, int max) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(enchants.entrySet());
        list.sort(Comparator.comparingInt(e -> getRarityRank(
                EnchantmentData.getEnchantmentInfo(EnchantmentData.getEnchantmentIndex(e.getKey())).rarity)));

        Map<String, Integer> limited = new LinkedHashMap<>();
        for (int i = list.size() - max; i < list.size(); i++) {
            if (i >= 0) {
                limited.put(list.get(i).getKey(), list.get(i).getValue());
            }
        }
        return limited;
    }

    private List<String> buildLore(Map<String, Integer> enchants) {
        List<String> lore = new ArrayList<>();
        if (!enchants.isEmpty()) lore.add("");

        enchants.entrySet().stream()
            .sorted((a, b) -> -Integer.compare(
                getRarityRank(EnchantmentData.getEnchantmentInfo(
                    EnchantmentData.getEnchantmentIndex(a.getKey())).rarity),
                getRarityRank(EnchantmentData.getEnchantmentInfo(
                    EnchantmentData.getEnchantmentIndex(b.getKey())).rarity)))
            .forEach(e -> {
                EnchantmentData.EnchantmentInfo data = EnchantmentData.getEnchantmentInfo(
                    EnchantmentData.getEnchantmentIndex(e.getKey()));
                String color = EnchantmentData.getRarityColor(data.rarity);
                lore.add(color + e.getKey() + " " + toRoman(e.getValue()));
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

    private boolean isValidTool(ItemStack item) {
        if (item == null) return false;
        String name = item.getType().toString();
        return name.contains("PICKAXE") || name.contains("SWORD") || name.contains("AXE") ||
            name.contains("SHOVEL") || name.contains("HOE") ||
            name.contains("BOOTS") || name.contains("HELMET") || 
            name.contains("CHESTPLATE") || name.contains("LEGGINGS");
    }

    private ToolEnchantInfo parseTool(ItemStack item) {
        ToolEnchantInfo info = new ToolEnchantInfo();
        if (item == null) return info;
        if (!item.hasItemMeta()) return info;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return info;

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
    private static class ToolEnchantInfo {
        final Map<String, Integer> map = new HashMap<>();
    }

    private boolean isCustomItem(ItemStack item) {
    if (item == null) {
        return false;
    }
    if (!item.hasItemMeta()) {
        return false;
    }
    ItemMeta meta = item.getItemMeta();

    String displayName = meta.hasDisplayName() ? meta.getDisplayName() : "";
    List<String> lore = meta.hasLore() ? meta.getLore() : Collections.emptyList();

    for (int i = 0; i < customItemUtil.CUSTOM_ITEM.length; i++) {
        customItemUtil.CustomItemInfo info = customItemUtil.getCustomItemInfo(i);
        if (info == null) {
            continue;
        }

        if (item.getType() != info.getMaterial()) {
            continue;
        }

        String strippedDisplayName = ChatColor.stripColor(displayName);
        String strippedExpectedName = ChatColor.stripColor(info.getName());
        if (!strippedDisplayName.equalsIgnoreCase(strippedExpectedName)) {
            continue;
        }

        if (lore.isEmpty()) {
            continue;
        }

        String strippedLore = ChatColor.stripColor(lore.get(0));
        String expectedLore = ChatColor.stripColor(info.getLore());

        if (!strippedLore.equalsIgnoreCase(expectedLore)) {
            continue;
        }
        return true;
    }
    return false;
}



}
