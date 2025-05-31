package customEnchants.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

import customEnchants.TestEnchants;


public class customItemUtil {
    private static JavaPlugin plugin;

    public static void setPlugin(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }
    
    public static final String[] CUSTOM_ITEM = {
        "§8Black Scroll",
        "Transmutation Shard",
        "Mining Key",
        "Prison Key",
        "Enchant Key",  //5
        "Divine Key",
        "Durability Key",
        "Prestige Key",
        "Prestige+ Key",
        "Transmutation Voucher",    //10
        "Decoration Voucher",
        "$1500 Voucher",
        "Key All Voucher",
        "§fCommon Enchant",
        "§2Uncommon Enchant",   //15
        "§3Rare Enchant",
        "§5Epic Enchant",
        "§6Legendary Enchant",
        "Coal Extractor",
        "Copper Extractor",     //20
        "Iron Extractor",
        "Redstone Extractor",
        "Lapis Extractor",
        "Gold Extractor",
        "Diamond Extractor",    //25
        "Emerald Extractor",
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
        "Preservation Voucher"
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
        "Drag and drop onto a piece of equipment to repair some durability",
        "Drag and drop onto a piece of equipment to repair some durability",
        "Drag and drop onto a piece of equipment to repair some durability",
        "Drag and drop onto a piece of equipment to repair some durability",
        "Drag and drop onto a piece of equipment to repair some durability",//40
        "Drag and drop onto a piece of equipment to repair some durability",
        "Drag and drop onto a piece of equipment to repair some durability",
        "Right Click to get a Preservation book of a random level"
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
        Material.CREEPER_BANNER_PATTERN
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
    null,
    null,
    null,
    null,   //30
    null,
    null,
    null,
    null,
    null,   //35
    null,
    null,
    null,
    null,
    null,   //40
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
        null,
        null,
        null,
        null,   //30
        null,
        null,
        null,
        null,
        null,   //35
        null,
        null,
        null,
        null,
        null,   //40
        null,
        null,
        null
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
    private static boolean isExtractor(String name) {
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

    public static final NamespacedKey DURABILITY_KEY = new NamespacedKey("enchants", "durability_value");

    private static final Map<Integer, Integer> DURABILITY_VALUES = Map.of(
    1, 25,
    2, 50,
    3, 75,
    4,125,
    5, 175,
    6, 225,
    7, 275
);

    public static ItemStack createDurabilityShard(int tier) {
    if (tier < 1 || tier > 7) return null;

    int index = 35 + (tier - 1);
    int durabilityValue = DURABILITY_VALUES.getOrDefault(tier, 10);

    ItemStack shard = new ItemStack(CUSTOM_ITEM_MATERIAL[index]);
    ItemMeta meta = shard.getItemMeta();

    if (meta != null) {
        meta.setDisplayName(CUSTOM_ITEM[index]);

        List<String> lore = new ArrayList<>();
        lore.add(CUSTOM_ITEM_LORE[index]);
        lore.add("§7Restores §e" + durabilityValue + "§7 durability");
        meta.setLore(lore);

        NamespacedKey key = new NamespacedKey(TestEnchants.getInstance(), "durability_value");
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, durabilityValue);

        shard.setItemMeta(meta);
    }

    return shard;
}

    public static boolean isDurabilityShard(ItemStack item) {
    if (item == null || item.getType() != Material.PRISMARINE_CRYSTALS) return false;
    ItemMeta meta = item.getItemMeta();
    return meta != null && meta.getPersistentDataContainer().has(DURABILITY_KEY, PersistentDataType.INTEGER);
}

    public static int getDurabilityValue(ItemStack item) {
    if (!isDurabilityShard(item)) return 0;
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();
    return container.getOrDefault(DURABILITY_KEY, PersistentDataType.INTEGER, 0);
}

}



