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


public class customItemUtil {
    
    public static final String[] CUSTOM_ITEM = {
        "§8Black Scroll",
        "Transmutation Shard",
        "Mining Key",
        "Prison Key",
        "Enchant Key",
        "Divine Key",
        "Durability Key",
        "Prestige Key",
        "Prestige+ Key"

    };

    //Change these
    public static final String[] CUSTOM_ITEM_LORE = {
        "Drag and drop on a tool to remove an enchant",
        "Null",
        "Use on Mining crate at /warp crates",
        "Use on Prison crate at /warp crates",
        "Use on Enchant crate at /warp crates",
        "Use on Divine crate at /warp cartes",
        "Use on Durability crate at /warp crates",
        "Use on Prestige crate at /warp crates",    
        "Use on Prestige+ crate at /warp crates"

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
        Material.OMINOUS_TRIAL_KEY
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
        null
};



    public static ItemStack createCustomItem(String itemName) {
    // Find CustomItemInfo by matching name (case insensitive)
    CustomItemInfo info = null;

    for (int i = 0; i < CUSTOM_ITEM.length; i++) {
        CustomItemInfo candidate = getCustomItemInfo(i);
        if (candidate != null && candidate.getName().equalsIgnoreCase(itemName)) {
            info = candidate;
            break;
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

        item.setItemMeta(meta);
    }

    return item;
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
            double ratio = (double) i / (length - 1);
            int r = (int) (r1 + (r2 - r1) * ratio);
            int g = (int) (g1 + (g2 - g1) * ratio);
            int b = (int) (b1 + (b2 - b1) * ratio);

            TextColor color = TextColor.color(r, g, b);
            result = result.append(Component.text(text.charAt(i)).color(color));
        }

        return result;
    }
}
}



