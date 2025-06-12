package customEnchants.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantmentData {
    //enchantmentcount 
        public static int getEnchantmentCount() {
            return ENCHANT_NAMES.length;
    }
    // Enchantment names
    public static final String[] ENCHANT_NAMES = {
        "Wall Breaker",
        "Blast",
        "Gold Digger",
        "Amplify",
        "Ore Scavenger",
        "Auto Smelt",
        "Unbreakable",
        "Magnet",
        "Preservation",
        "Frost Touch",
        "Key Miner",
        "Delayed Dynamite",
        "Regenerate",
        "Conjure",
        "Xp Syphon",
        "Light Weight",
        "Speed Breaker",
        "Sprinter",
        "Bounder",
        "Vein Miner"
    };
    
    // Maximum levels for each enchantment (corresponds to ENCHANT_NAMES array)
    public static final int[] ENCHANT_MAX_LEVELS = {
        3,  // Wall Breaker
        5,  // Blast
        3,  // Gold Digger
        3,  // Amplify
        4,  // Ore Scavenger
        1,  // Auto Smelt
        1,  // Unbreakable
        3,  // Magnet
        10,   // Preservation
        5,
        5,
        3,
        4,
        3,
        5,
        3,
        5,
        3,
        3,
        5
    };
    
    // Tool types that can receive each enchantment
    public static final String[] ENCHANT_TOOL_TYPES = {
        "PICKAXE",         // Wall Breaker
        "PICKAXE",         // Blast
        "PICKAXE",         // Gold Digger
        "PICKAXE",         // Amplify
        "PICKAXE",         // Ore Scavenger
        "PICKAXE",         // Auto Smelt
        "ALL",             // Unbreakable
        "PICKAXE",         // Magnet
        "ALL",             // Preservation 
        "PICKAXE",          //Frost Touch
        "PICKAXE",
        "PICKAXE",
        "PICKAXE",
        "PICKAXE",
        "ALL",
        "ALL",
        "ALL",
        "ALL",
        "ALL",
        "PICKAXE"
    };
    
    // Rarity levels with ChatColors (COMMON=WHITE, UNCOMMON=GREEN, RARE=BLUE, EPIC=PURPLE, LEGENDARY=GOLD, PRESTIGE=PINK)
    public static final String[] ENCHANT_RARITY = {
        "PRESTIGE",     // Wall Breaker
        "PRESTIGE",     // Blast
        "PRESTIGE",     // Gold Digger
        "LEGENDARY",    // Amplify
        "UNCOMMON",     // Ore Scavenger
        "PRESTIGE+",    // Auto Smelt
        "PRESTIGE+",    // Unbreakable
        "PRESTIGE",     // Magnet
        "LEGENDARY",    //Preservation
        "EPIC",         //Frost touch
        "LEGENDARY",    //Key miner
        "UNCOMMON",     //Delayed dynamite
        "UNCOMMON",     //Regenerate
        "RARE",         //Conjure
        "EPIC",         //Xp syphon
        "LEGENDARY",    //Light Weight
        "PRESTIGE",     //Speed breaker
        "COMMON",       //sprinter
        "COMMON",       //bounder
        "LEGENDARY"     //vein miner
    };
    
    // ChatColor mappings for each rarity
    public static final String[] RARITY_COLORS = {
        "§f", // COMMON - White
        "§a", // UNCOMMON - Green
        "§9", // RARE - Blue
        "§5", // EPIC - Purple
        "§6", // LEGENDARY - Gold
        "§d",  // PRESTIGE - Pink
        "§c"  //PRESTIGE+ - Red
    };
    
    // Rarity names for display
    public static final String[] RARITY_NAMES = {
        "COMMON",
        "UNCOMMON", 
        "RARE",
        "EPIC",
        "LEGENDARY",
        "PRESTIGE",
        "PRESTIGE+" 
    };
    
    // Lore descriptions for each enchantment
    public static final String[] ENCHANT_LORE = {
        "Breaks multiple blocks in a wall pattern",           // Wall Breaker
        "Breaks a 3x3x3 cube",                     // Blast
        "Chance to give money when breaking blocks",         // Gold Digger
        "Increases other enchants proc chance",            // Amplify
        "Chance to spawn more ore",              // Ore Scavenger
        "Automatically smelts mined blocks",                   // Auto Smelt
        "Tool never breaks from durability damage",           // Unbreakable
        "Attracts nearby dropped items to player",             // Magnet
        "Chance to return durability to the tool",      //Preservation
        "Chance to spawn ice around the player",         //Frost Touch
        "Chance to give keys",
        "Spawns a block of coal that explodes after being mined",
        "Chance to regenerate ore blocks",
        "Chance to upgrade ore to block form",
        "Chance for any block to drop xp",
        "Chance to give haste when breaking blocks",
        "Chance to give haste when breaking blocks",
        "Chance to give speed when breaking blocks",
        "Chance to give jump boost when breaking blocks",
        "Chance to break connected ores in a 3 block radius"
    };
    
    // Proc chance percentages (0.0 to 1.0)
    public static final double[] ENCHANT_PROC_CHANCE = {
        .1,    // Wall Breaker - 10% per level
        .05,   // Blast - 5% per level
        0.5,   // Gold Digger - Always active (passive)
        1.0,   //Amplify never used
        0.35,  // Ore Scavenger - 35% chance
        1.0,   // Auto Smelt - Always active (passive)
        1.0,   // Unbreakable - Always active (passive)
        1.0,   // Magnet - Always active (passive)
        0.1,    // Preservation 5%
        0.01,    //Frost Touch 1%
        0.05,   //Key miner
        .05,    //delayed dynamite
        1,       //regenerate
        1,
        1,
        .3,
        .3,
        .1,
        .1,
        .1
    };
    
    // Helper method to get rarity color by rarity name
    public static String getRarityColor(String rarity) {
        for (int i = 0; i < RARITY_NAMES.length; i++) {
            if (RARITY_NAMES[i].equalsIgnoreCase(rarity)) {
                return RARITY_COLORS[i];
            }
        }
        return "§f"; // Default to white if not found
    }
    
    // Helper method to get colored rarity display text
    public static String getColoredRarity(String rarity) {
        return getRarityColor(rarity) + rarity.toUpperCase();
    }
    
    // Helper method to get enchantment data by index
    public static EnchantmentInfo getEnchantmentInfo(int index) {
        if (index < 0 || index >= ENCHANT_NAMES.length) {
            return null;
        }
        
        return new EnchantmentInfo(
            ENCHANT_NAMES[index],
            ENCHANT_MAX_LEVELS[index],
            ENCHANT_TOOL_TYPES[index],
            ENCHANT_RARITY[index],
            ENCHANT_LORE[index],
            ENCHANT_PROC_CHANCE[index]
        );
    }
    
    // Helper method to get enchantment index by name
    public static int getEnchantmentIndex(String name) {
        for (int i = 0; i < ENCHANT_NAMES.length; i++) {
            if (ENCHANT_NAMES[i].equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1; // Not found
    }
    
    // Inner class to hold enchantment data
    public static class EnchantmentInfo {
        public final String name;
        public final int maxLevel;
        public final String toolTypes;
        public final String rarity;
        public final String lore;
        public final double procChance;
        
        public EnchantmentInfo(String name, int maxLevel, String toolTypes, 
                            String rarity, String lore, double procChance) {
            this.name = name;
            this.maxLevel = maxLevel;
            this.toolTypes = toolTypes;
            this.rarity = rarity;
            this.lore = lore;
            this.procChance = procChance;
        }
        
        // Check if this enchantment can be applied to a specific tool type
        public boolean canApplyTo(String toolType) {
            return toolTypes.toUpperCase().contains(toolType.toUpperCase());
        }
    }
    
    public static final List<EnchantmentInfo> ENCHANTMENTS = new ArrayList<>();

    static {
    for (int i = 0; i < ENCHANT_NAMES.length; i++) {
        ENCHANTMENTS.add(new EnchantmentInfo(
            ENCHANT_NAMES[i],
            ENCHANT_MAX_LEVELS[i],
            ENCHANT_TOOL_TYPES[i],
            ENCHANT_RARITY[i],
            ENCHANT_LORE[i],
            ENCHANT_PROC_CHANCE[i]
        ));
    }
}

    public static EnchantmentInfo getEnchantmentInfoByName(String name) {
    for (EnchantmentInfo info : ENCHANTMENTS) {
        if (info.name.equalsIgnoreCase(name)) return info;
    }
    return null;
    }

    public static ItemStack createEnchantedBook(EnchantmentData.EnchantmentInfo enchantInfo, int level, int chance, boolean obfuscateChance) {
    ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
    ItemMeta meta = book.getItemMeta();

    if (meta != null) {
        String rarityColor = EnchantmentData.getRarityColor(enchantInfo.rarity);
        String displayName = rarityColor + enchantInfo.name + " " + getRomanNumeral(level);
        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + enchantInfo.lore);
        lore.add("");
        
        if (obfuscateChance) {
            lore.add(ChatColor.GREEN + "Success Rate: " + ChatColor.MAGIC + chance + "%" + ChatColor.RESET);
        } else {
            lore.add(ChatColor.GREEN + "Success Rate: " + chance + "%");
        }
        
        lore.add(EnchantmentData.getColoredRarity(enchantInfo.rarity));
        lore.add("");
        lore.add(ChatColor.GRAY + "Applicable to: " + formatToolTypes(enchantInfo.toolTypes));
        lore.add(ChatColor.GRAY + "Drag and drop onto item to apply");

        meta.setLore(lore);
        book.setItemMeta(meta);
    }

    return book;
}
    

    public static String formatToolTypes(String toolTypes) {
        if ("ALL".equals(toolTypes)) {
            return "All Tools";
        }
        return toolTypes.replace(",", ", ");
    }
    
    public static String getRomanNumeral(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(number);
        };
    }

    public static String getRarity(String enchantName) {
    for (int i = 0; i < ENCHANT_NAMES.length; i++) {
        if (ENCHANT_NAMES[i].equalsIgnoreCase(enchantName)) {
            return ENCHANT_RARITY[i];
        }
    }
    return null;
}



}