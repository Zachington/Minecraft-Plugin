package customEnchants.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import customEnchants.TestEnchants;

public class SellManager {
    private final static Map<Material, Double> basePrices = new HashMap<>();
    private final Map<Material, Double> smeltedPrices = new HashMap<>();
    private final Set<Material> fillerBlocks = new HashSet<>();
    private boolean smeltedBonus;

    public SellManager(File dataFolder) {
        File file = new File(dataFolder, "sell-prices.yml");
        if (!file.exists()) {
            TestEnchants.getInstance().saveResource("sell-prices.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        smeltedBonus = config.getBoolean("smelted_bonus", false);

        ConfigurationSection pricesSection = config.getConfigurationSection("prices");
        if (pricesSection == null) {
            TestEnchants.getInstance().getLogger().warning("sell-prices.yml missing 'prices' section!");
            return;
        }

        for (String key : pricesSection.getKeys(false)) {
            if (key.equalsIgnoreCase("_LOG")) {
                // Handle logs later
                continue;
            }

            Object node = pricesSection.get(key);
            if (node instanceof ConfigurationSection) {
                ConfigurationSection sec = pricesSection.getConfigurationSection(key);
                try {
                    Material mat = Material.valueOf(key.toUpperCase());
                    double base = sec.getDouble("base", 0.0);
                    basePrices.put(mat, base);
                    if (smeltedBonus) {
                        double smelted = sec.getDouble("smelted", base);
                        smeltedPrices.put(mat, smelted);
                    }
                } catch (IllegalArgumentException e) {
                    TestEnchants.getInstance().getLogger().warning("Invalid material in sell-prices.yml: " + key);
                }
            } else if (node instanceof Number) {
                try {
                    Material mat = Material.valueOf(key.toUpperCase());
                    double price = pricesSection.getDouble(key);
                    basePrices.put(mat, price);
                } catch (IllegalArgumentException e) {
                    TestEnchants.getInstance().getLogger().warning("Invalid material in sell-prices.yml: " + key);
                }
            } else {
                TestEnchants.getInstance().getLogger().warning("Unknown data type for " + key + " in sell-prices.yml");
            }
        }

        // Apply _LOG price to all materials ending with _LOG
        double logPrice = pricesSection.getDouble("_LOG", 0.0);
        if (logPrice > 0.0) {
            for (Material mat : Material.values()) {
                if (mat.name().endsWith("_LOG")) {
                    basePrices.put(mat, logPrice);
                }
            }
        }

        loadFillerBlocks();
    }

    private void loadFillerBlocks() {
        // Add filler blocks here or load from config if desired
        fillerBlocks.add(Material.COBBLESTONE);
        fillerBlocks.add(Material.ANDESITE);
        fillerBlocks.add(Material.DIORITE);
        fillerBlocks.add(Material.MOSSY_COBBLESTONE);
        fillerBlocks.add(Material.BRICKS);
        fillerBlocks.add(Material.BLUE_TERRACOTTA);
        fillerBlocks.add(Material.RED_TERRACOTTA);
        fillerBlocks.add(Material.YELLOW_TERRACOTTA);
        fillerBlocks.add(Material.GREEN_TERRACOTTA);
        fillerBlocks.add(Material.GRAY_TERRACOTTA);
        fillerBlocks.add(Material.PURPUR_BLOCK);
        fillerBlocks.add(Material.LIGHT_GRAY_CONCRETE);
        fillerBlocks.add(Material.ORANGE_CONCRETE);
        fillerBlocks.add(Material.PINK_CONCRETE);
        fillerBlocks.add(Material.CYAN_CONCRETE);
        fillerBlocks.add(Material.LIME_CONCRETE);
        fillerBlocks.add(Material.END_STONE);
        fillerBlocks.add(Material.CALCITE);
        fillerBlocks.add(Material.WHITE_CONCRETE);
        fillerBlocks.add(Material.POLISHED_DIORITE);
        fillerBlocks.add(Material.RED_SANDSTONE);
        fillerBlocks.add(Material.BASALT);
        fillerBlocks.add(Material.BLACKSTONE);
        fillerBlocks.add(Material.QUARTZ_BLOCK);
        fillerBlocks.add(Material.LIGHT_BLUE_TERRACOTTA);
        fillerBlocks.add(Material.PRISMARINE);
        fillerBlocks.add(Material.PACKED_MUD);
        fillerBlocks.add(Material.TERRACOTTA);
        fillerBlocks.add(Material.BROWN_CONCRETE);
        // Add more if needed
    }

    public boolean isSellable(Material mat) {
        return basePrices.containsKey(mat);
    }

    public boolean isFillerBlock(Material mat) {
        return fillerBlocks.contains(mat);
    }

    public static double getPrice(Material mat) {
        if (!basePrices.containsKey(mat)) return 0.0;

        // For now, return base price only; if you want smelted price, you need to detect item smelted state elsewhere
        return basePrices.get(mat);
    }

    public double getSmeltedPrice(Material mat) {
        if (!smeltedPrices.containsKey(mat)) return getPrice(mat);
        return smeltedPrices.get(mat);
    }
}
