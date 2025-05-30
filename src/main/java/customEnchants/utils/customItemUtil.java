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
        "Mining Key",
        "Prison Key",
        "Enchant Key",
        "Divine Key",
        "Durability Key",
        "Prestige Key",
        "Prestige+ Key",
        "Transmutation Voucher",
        "Decoration Voucher",
        "$1500 Voucher",
        "Key All Voucher",
        "§fCommon Enchant",
        "§2Uncommon Enchant",
        "§3Rare Enchant",
        "§5Epic Enchant",
        "§6Legendary Enchant",
        "Coal Extractor",
        "Copper Extractor",
        "Iron Extractor",
        "Redstone Extractor",
        "Lapis Extractor",
        "Gold Extractor",
        "Diamond Extractor",
        "Emerald Extractor",
        "Deepslate Coal Extractor",
        "Deepslate Copper Extractor",
        "Deepslate Iron Extractor",
        "Deepslate Redstone Extractor",
        "Deepslate Lapis Extractor",
        "Deepslate Gold Extractor",
        "Deepslate Diamond Extractor",
        "Deepslate Emerald Extractor",
        "Nether Gold Extractor"
    };

    //Change these
    public static final String[] CUSTOM_ITEM_LORE = {
        "Drag and drop on a tool to remove an enchant",
        "Null",
        "Use on Mining crate at /warp crates",
        "Use on Prison crate at /warp crates",
        "Use on Enchant crate at /warp crates",
        "Use on Divine crate at /warp crates",
        "Use on Durability crate at /warp crates",
        "Use on Prestige crate at /warp crates",    
        "Use on Prestige+ crate at /warp crates",
        "Right click to open voucher",
        "Right click to open voucher",
        "Right click to open voucher",
        "Right click to open voucher",
        "Right Click to receive random enchant",
        "Right Click to receive random enchant",
        "Right Click to receive random enchant",
        "Right Click to receive random enchant",
        "Right Click to receive random enchant",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence",
        "Break blocks to generate Essence"
    };

    public static final Material[] CUSTOM_ITEM_MATERIAL = {
        Material.BLACK_CANDLE,
        Material.ECHO_SHARD,
        Material.TRIAL_KEY,
        Material.TRIAL_KEY,
        Material.TRIAL_KEY,
        Material.TRIAL_KEY,
        Material.TRIAL_KEY,
        Material.OMINOUS_TRIAL_KEY,
        Material.OMINOUS_TRIAL_KEY,
        Material.FLOWER_BANNER_PATTERN,
        Material.FIELD_MASONED_BANNER_PATTERN,
        Material.PIGLIN_BANNER_PATTERN,
        Material.BORDURE_INDENTED_BANNER_PATTERN,
        Material.POPPED_CHORUS_FRUIT,
        Material.POPPED_CHORUS_FRUIT,
        Material.POPPED_CHORUS_FRUIT,
        Material.POPPED_CHORUS_FRUIT,
        Material.POPPED_CHORUS_FRUIT,
        Material.COAL_ORE,
        Material.COPPER_ORE,
        Material.IRON_ORE,
        Material.REDSTONE_ORE,
        Material.LAPIS_ORE,
        Material.GOLD_ORE,
        Material.DIAMOND_ORE,
        Material.EMERALD_ORE,
        Material.DEEPSLATE_COAL_ORE,
        Material.DEEPSLATE_COPPER_ORE,
        Material.DEEPSLATE_IRON_ORE,
        Material.DEEPSLATE_REDSTONE_ORE,
        Material.DEEPSLATE_LAPIS_ORE,
        Material.DEEPSLATE_GOLD_ORE,
        Material.DEEPSLATE_DIAMOND_ORE,
        Material.DEEPSLATE_EMERALD_ORE,
        Material.NETHER_GOLD_ORE
    };

    //Change these
    public static final String[] CUSTOM_ITEM_GRADIENT_START = {
    null,
    "#00FFFF",
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
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
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
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

    

}



