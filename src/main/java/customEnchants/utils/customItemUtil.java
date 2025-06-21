package customEnchants.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;



public class customItemUtil {
    private static JavaPlugin plugin;

    public static void setPlugin(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }
    
    public static final String[] CUSTOM_ITEM = {
        "§8Black Scroll",
        "Transmutation Shard",
        "§3Mining Key",
        "§1Prison Key",
        "§5Enchant Key",  //5
        "§cDivine Key",
        "§8Durability Key",
        "§dPrestige Key",
        "§4Prestige+ Key",
        "Transmutation Voucher",    //10
        "Decoration Voucher",
        "$1500 Voucher",
        "§2Key All Voucher",
        "§fCommon Enchant",
        "§2Uncommon Enchant",   //15
        "§3Rare Enchant",
        "§5Epic Enchant",
        "§6Legendary Enchant",
        "§8Coal Extractor",
        "§6Copper Extractor",     //20
        "§7Iron Extractor",
        "§cRedstone Extractor",
        "§9Lapis Extractor",
        "§eGold Extractor",
        "§9Diamond Extractor",    //25
        "§aEmerald Extractor",
        "Deepslate Coal Extractor",
        "Deepslate Copper Extractor",
        "Deepslate Iron Extractor",
        "Deepslate Redstone Extractor", //30
        "Deepslate Lapis Extractor",
        "Deepslate Gold Extractor",
        "Deepslate Diamond Extractor",
        "Deepslate Emerald Extractor",
        "Nether Gold Extractor",    //35
        "Durability Shard Tier 1",
        "Durability Shard Tier 2",
        "Durability Shard Tier 3",
        "Durability Shard Tier 4",
        "Durability Shard Tier 5",  //40
        "Durability Shard Tier 6",
        "Durability Shard Tier 7",
        "§6Preservation Voucher",
        "Coal Essence Shard",
        "Copper Essence Shard",     //45
        "Iron Essence Shard",
        "Redstone Essence Shard",
        "Lapis Essence Shard",
        "Gold Essence Shard",
        "Diamond Essence Shard",    //50
        "Emerald Essence Shard",
        "Deep Coal Essence Shard",
        "Deep Copper Essence Shard",
        "Deep Iron Essence Shard",
        "Deep Redstone Essence Shard",  //55
        "Deep Lapis Essence Shard",
        "Deep Gold Essence Shard",
        "Deep Diamond Essence Shard",
        "Deep Emerald Essence Shard",
        "Amethyst Essence Shard",       //60
        "Extractor Core",
        "Tier 1 Furnace",
        "Tier 2 Furnace",
        "Tier 3 Furnace",
        "Tier 4 Furnace",   //65
        "Tier 5 Furnace",
        "Tier 6 Furnace",
        "Blast Furnace"
    };

    //Change these
    public static final String[] CUSTOM_ITEM_LORE = {
        "Drag and drop on a tool to remove an enchant",
        "Combine 8 shards for a Transmutation Voucher",
        "Use on Mining crate at /warp crates",
        "Use on Prison crate at /warp crates",
        "Use on Enchant crate at /warp crates", //5
        "Use on Divine crate at /warp crates",
        "Use on Durability crate at /warp crates",
        "Use on Prestige crate at /warp crates",    
        "Use on Prestige+ crate at /warp crates",
        "Right click to open voucher",  //10
        "Right click to open voucher",
        "Right click to open voucher",
        "Right click to open voucher",
        "Right Click to receive random enchant",
        "Right Click to receive random enchant",    //15
        "Right Click to receive random enchant",
        "Right Click to receive random enchant",
        "Right Click to receive random enchant",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence", //20
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence", //25
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",//30
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",//35
        "Drag and drop onto equipment to repair 25 Durability",
        "Drag and drop onto equipment to repair 50 Durability",
        "Drag and drop onto equipment to repair 75 Durability",
        "Drag and drop onto equipment to repair 100 Durability",
        "Drag and drop onto equipment to repair 150 Durability",//40
        "Drag and drop onto equipment to repair 200 Durability",
        "Drag and drop onto equipment to repair 250 Durability",
        "Right Click to get a Preservation book of a random level",
        "Used to craft Extractors",
        "Used to craft Extractors", //45
        "Used to craft Extractors",
        "Used to craft Extractors",
        "Used to craft Extractors",
        "Used to craft Extractors",
        "Used to craft Extractors", //50
        "Used to craft Extractors",
        "Used to craft Extractors",
        "Used to craft Extractors",
        "Used to craft Extractors",
        "Used to craft Extractors", //55
        "Used to craft Extractors",
        "Used to craft Extractors",
        "Used to craft Extractors",
        "Used to craft Extractors",
        "Used to craft Extractors", //60
        "Used to craft Extractors",
        "10% Double Smelt Chance",
        "20% Double Smelt Chance", 
        "30% Double Smelt Chance", 
        "45% Double Smelt Chance",  //65
        "60% Double Smelt Chance", 
        "80% Double Smelt Chance", 
        "100% Double Smelt Chance"

    };

    public static final Material[] CUSTOM_ITEM_MATERIAL = {
        Material.BLACK_CANDLE,
        Material.ECHO_SHARD,
        Material.TRIAL_KEY,
        Material.TRIAL_KEY,
        Material.TRIAL_KEY, //5
        Material.TRIAL_KEY,
        Material.TRIAL_KEY,
        Material.OMINOUS_TRIAL_KEY,
        Material.OMINOUS_TRIAL_KEY,
        Material.FLOWER_BANNER_PATTERN, //10
        Material.BRICK,
        Material.PIGLIN_BANNER_PATTERN,
        Material.BORDURE_INDENTED_BANNER_PATTERN,
        Material.POPPED_CHORUS_FRUIT,
        Material.POPPED_CHORUS_FRUIT,   //15
        Material.POPPED_CHORUS_FRUIT,
        Material.POPPED_CHORUS_FRUIT,
        Material.POPPED_CHORUS_FRUIT,
        Material.COAL_ORE,
        Material.COPPER_ORE,    //20
        Material.IRON_ORE,
        Material.REDSTONE_ORE,
        Material.LAPIS_ORE,
        Material.GOLD_ORE,
        Material.DIAMOND_ORE,   //25
        Material.EMERALD_ORE,
        Material.DEEPSLATE_COAL_ORE,
        Material.DEEPSLATE_COPPER_ORE,
        Material.DEEPSLATE_IRON_ORE,
        Material.DEEPSLATE_REDSTONE_ORE,    //30
        Material.DEEPSLATE_LAPIS_ORE,
        Material.DEEPSLATE_GOLD_ORE,
        Material.DEEPSLATE_DIAMOND_ORE,
        Material.DEEPSLATE_EMERALD_ORE,
        Material.NETHER_GOLD_ORE,   //35
        Material.PRISMARINE_SHARD,
        Material.PRISMARINE_SHARD,
        Material.PRISMARINE_SHARD,
        Material.PRISMARINE_SHARD,
        Material.PRISMARINE_SHARD,  //40
        Material.PRISMARINE_SHARD,
        Material.PRISMARINE_SHARD,
        Material.CREEPER_BANNER_PATTERN,
        Material.COAL,
        Material.COPPER_INGOT,    //45
        Material.IRON_INGOT,
        Material.REDSTONE,
        Material.LAPIS_LAZULI,
        Material.GOLD_INGOT,
        Material.DIAMOND,    //50
        Material.EMERALD,
        Material.COAL_BLOCK,
        Material.COPPER_BLOCK,
        Material.IRON_BLOCK,
        Material.REDSTONE_BLOCK,    //55
        Material.LAPIS_BLOCK,
        Material.GOLD_BLOCK,
        Material.DIAMOND_BLOCK,
        Material.EMERALD_BLOCK,
        Material.AMETHYST_SHARD,    //60
        Material.HEAVY_CORE,
        Material.FURNACE,  
        Material.FURNACE,
        Material.FURNACE,
        Material.FURNACE,   //65
        Material.FURNACE,
        Material.FURNACE,
        Material.BLAST_FURNACE   
    };

    //Change these
    public static final String[] CUSTOM_ITEM_GRADIENT_START = {
    null,
    "#00FFFF",
    null,
    null,
    null,   //5
    null,
    null,
    null,
    null,
    null,   //10
    null,
    null,
    null,
    null,
    null,   //15
    null,
    null,
    null,
    null,
    null,   //20
    null,
    null,
    null,
    null,
    null,   //25
    null,
    "#646464",
    "#FFC400",
    "#FFFFFF",
    "#DB0404",   //30
    "#0070CA",
    "#FFED00",
    "#00E0FF",
    "#00FF1C",
    "#721C00",   //35
    "#646464",
    "#646464",
    "#646464",
    "#646464",
    "#646464",   //40
    "#646464",
    "#646464",
    null,
    null,   
    null,   //45
    null,
    null,
    null,
    null,   
    null,   //50
    null,
    null,
    null,
    null,   
    null,   //55
    null,
    null,
    null,
    null,
    null,    //60
    null,
    null,
    null,
    null,
    null,   //65
    null,
    null,
    null
};

    //Change these
    public static final String[] CUSTOM_ITEM_GRADIENT_END = {
        null,
        "#3C3C3C",
        null,
        null,
        null,   //5
        null,
        null,
        null,
        null,
        null,   //10
        null,
        null,
        null,
        null,
        null,   //15
        null,
        null,
        null,
        null,
        null,   //20
        null,
        null,
        null,
        null,
        null,   //25
        null,
        "#646464",
        "#816902",
        "#484848",
        "#390000",   //30
        "#2200FF",
        "#EAE26E",
        "#0775B5",
        "#016400",
        "#8F8B00",   //35
        "#39853E",
        "#39853E",
        "#39853E",
        "#39853E",
        "#39853E",   //40
        "#39853E",
        "#39853E",
        null,
        null,   
        null,   //45
        null,
        null,
        null,
        null,   
        null,   //50
        null,
        null,
        null,
        null,   
        null,   //55
        null,
        null,
        null,
        null,
        null,    //60
        null,
        null,
        null,
        null,
        null,   //65
        null,
        null,
        null,
    };



    public static ItemStack createCustomItem(String itemName) {
    CustomItemInfo info = null;
    String cleanInput = ChatColor.stripColor(itemName).toLowerCase();

    for (int i = 0; i < CUSTOM_ITEM.length; i++) {
        CustomItemInfo candidate = getCustomItemInfo(i);
        if (candidate != null) {
            String cleanCandidate = ChatColor.stripColor(candidate.getName()).toLowerCase();
            if (cleanCandidate.equals(cleanInput)) {
                info = candidate;
                break;
            }
        }
    }

    if (info == null) return null;

    ItemStack item = new ItemStack(info.getMaterial());
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
        if (info.getStartHex() != null && info.getEndHex() != null) {
            Component gradient = GradientUtil.gradient(info.getName(), info.getStartHex(), info.getEndHex());
            String legacyName = LegacyComponentSerializer.legacySection().serialize(gradient);
            meta.setDisplayName(legacyName);
        } else {
            meta.setDisplayName(ChatColor.WHITE + info.getName());
        }

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + info.getLore());
        meta.setLore(lore);

        // --- ADD UNIQUE ID FOR EXTRACTORS TO MAKE THEM NON-STACKABLE ---
        if (isExtractor(info.getName())) {
            // Generate unique UUID string
            String uniqueID = UUID.randomUUID().toString();
            NamespacedKey key = new NamespacedKey(plugin, "unique_extractor_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, uniqueID);
        }

        item.setItemMeta(meta);
    }

    return item;
}

// Helper method to identify if an item is an extractor by name
    public static boolean isExtractor(String name) {
    String stripped = ChatColor.stripColor(name).toLowerCase();
    // Check if name contains "extractor" (case insensitive)
    return stripped.contains("extractor");
}
    public static class CustomItemInfo {
        private final String name;
        private final String lore;
        private final Material material;
        private final String startHex;
        private final String endHex;

        public CustomItemInfo(String name, String lore, Material material, String startHex, String endHex) {
            this.name = name;
            this.lore = lore;
            this.material = material;
            this.startHex = startHex;
            this.endHex = endHex;
        }

        public String getName() { return name; }
        public String getLore() { return lore; }
        public Material getMaterial() { return material; }
        public String getStartHex() { return startHex; }
        public String getEndHex() { return endHex; }
    }

    public static CustomItemInfo getCustomItemInfo(int index) {
        if (index < 0 || index >= CUSTOM_ITEM.length) {
            return null;
        }

        return new CustomItemInfo(
            CUSTOM_ITEM[index],
            CUSTOM_ITEM_LORE[index],
            CUSTOM_ITEM_MATERIAL[index],
            CUSTOM_ITEM_GRADIENT_START[index],
            CUSTOM_ITEM_GRADIENT_END[index]
        );
    }
    public class GradientUtil {

    public static Component gradient(String text, String startHex, String endHex) {
        int length = text.length();
        Component result = Component.empty();

        // Convert hex to RGB
        int r1 = Integer.valueOf(startHex.substring(1, 3), 16);
        int g1 = Integer.valueOf(startHex.substring(3, 5), 16);
        int b1 = Integer.valueOf(startHex.substring(5, 7), 16);

        int r2 = Integer.valueOf(endHex.substring(1, 3), 16);
        int g2 = Integer.valueOf(endHex.substring(3, 5), 16);
        int b2 = Integer.valueOf(endHex.substring(5, 7), 16);

        for (int i = 0; i < length; i++) {
            double ratio = (length == 1) ? 0 : (double) i / (length - 1);
            int r = (int) (r1 + (r2 - r1) * ratio);
            int g = (int) (g1 + (g2 - g1) * ratio);
            int b = (int) (b1 + (b2 - b1) * ratio);

            TextColor color = TextColor.color(r, g, b);
            result = result.append(Component.text(text.charAt(i)).color(color));
        }

        return result;
    }
}

    public static int getCustomItemIndexByStrippedName(String strippedName) {
    for (int i = 0; i < CUSTOM_ITEM.length; i++) {
        String stripped = ChatColor.stripColor(CUSTOM_ITEM[i]).toLowerCase();
        if (stripped.equals(strippedName.toLowerCase())) {
            return i;
        }
    }
    return -1;
}

    public static boolean isCustomItem(ItemStack item) {
    if (item == null) return false;
    if (!item.hasItemMeta()) return false;

    ItemMeta meta = item.getItemMeta();
    if (!meta.hasDisplayName() || !meta.hasLore()) return false;

    String displayName = ChatColor.stripColor(meta.getDisplayName());
    List<String> lore = meta.getLore();

    for (int i = 0; i < CUSTOM_ITEM.length; i++) {
        String expectedName = ChatColor.stripColor(CUSTOM_ITEM[i]);
        String expectedLore = ChatColor.stripColor(CUSTOM_ITEM_LORE[i]);

        if (expectedName.equalsIgnoreCase(displayName)) {
            // Check material
            if (item.getType() != CUSTOM_ITEM_MATERIAL[i]) continue;

            // Check lore - make sure lore list contains the expected lore line somewhere
            boolean loreMatches = lore.stream()
                .map(ChatColor::stripColor)
                .anyMatch(line -> line.equalsIgnoreCase(expectedLore));

            if (loreMatches) {
                return true;
            }
        }
    }
    return false;
}

}



