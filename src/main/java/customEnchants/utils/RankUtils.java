package customEnchants.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.ChatColor;

import customEnchants.TestEnchants;

public class RankUtils {

    public static final NamespacedKey rankKey = new NamespacedKey(TestEnchants.getInstance(), "playerRank");

    // Get the player's rank from NBT, return null if not set
    public static String getRank(Player player) {
    String rank = player.getPersistentDataContainer().get(rankKey, PersistentDataType.STRING);
    return rank != null ? rank : "a";
}

    // Set the player's rank in NBT
    public static void setRank(Player player, String rank) {
        player.getPersistentDataContainer().set(rankKey, PersistentDataType.STRING, rank);
    }

    public static class RankCost {
        public final double money;
        public final int essence;
        public final int essenceTier;

        public RankCost(double money, int essence, int essenceTier) {
            this.money = money;
            this.essence = essence;
            this.essenceTier = essenceTier;
        }
    }

    // Base cost config for a-z ranks
    private static final Map<Character, RankCost> baseRankCosts = new HashMap<>();

    static {
    baseRankCosts.put('b', new RankCost(1000, 100, 1)); 
    baseRankCosts.put('c', new RankCost(1450, 155, 1));
    baseRankCosts.put('d', new RankCost(2100, 240, 1));
    baseRankCosts.put('e', new RankCost(3000, 375, 1));
    baseRankCosts.put('f', new RankCost(4400, 580, 1));
    baseRankCosts.put('g', new RankCost(6400, 900, 1));
    baseRankCosts.put('h', new RankCost(9200, 1400, 1));
    baseRankCosts.put('i', new RankCost(13300, 2200, 1));
    baseRankCosts.put('j', new RankCost(19300, 1000, 2));
    baseRankCosts.put('k', new RankCost(27900, 1340, 2));
    baseRankCosts.put('l', new RankCost(40400, 1800, 2));
    baseRankCosts.put('m', new RankCost(58400, 2400, 2));
    baseRankCosts.put('n', new RankCost(84600, 3250, 2));
    baseRankCosts.put('o', new RankCost(122400, 4300, 2));
    baseRankCosts.put('p', new RankCost(177300, 5800, 2));
    baseRankCosts.put('q', new RankCost(256600, 7800, 2));
    baseRankCosts.put('r', new RankCost(371500, 10000, 2));
    baseRankCosts.put('s', new RankCost(537700, 5000, 3));
    baseRankCosts.put('t', new RankCost(778300, 6100, 3));
    baseRankCosts.put('u', new RankCost(1126500, 7500, 3));
    baseRankCosts.put('v', new RankCost(1630700, 9000,3));
    baseRankCosts.put('w', new RankCost(2360400, 11000, 3));
    baseRankCosts.put('x', new RankCost(3416700, 13500, 3));
    baseRankCosts.put('y', new RankCost(4945700, 16500, 3));
    baseRankCosts.put('z', new RankCost(7158900, 20000, 3));
}

    private static final Map<String, RankCost> boundaryRankCosts = new HashMap<>();

    static {
    boundaryRankCosts.put("z_p1a", new RankCost(10000000, 25000, 3));
    // p1z->p2a to p24z->p25a
    boundaryRankCosts.put("p1z_p2a", new RankCost(5000, 100, 3));
    boundaryRankCosts.put("p2z_p3a", new RankCost(6000, 120, 3));
    boundaryRankCosts.put("p3z_p4a", new RankCost(7000, 140, 3));
    boundaryRankCosts.put("p4z_p5a", new RankCost(8000, 160, 3));
    boundaryRankCosts.put("p5z_p6a", new RankCost(9000, 180, 4));
    boundaryRankCosts.put("p6z_p7a", new RankCost(10000, 200, 4));
    boundaryRankCosts.put("p7z_p8a", new RankCost(11000, 220, 4));
    boundaryRankCosts.put("p8z_p9a", new RankCost(12000, 240, 4));
    boundaryRankCosts.put("p9z_p10a", new RankCost(13000, 260, 4));
    boundaryRankCosts.put("p10z_p11a", new RankCost(14000, 280, 5));
    boundaryRankCosts.put("p11z_p12a", new RankCost(15000, 300, 5));
    boundaryRankCosts.put("p12z_p13a", new RankCost(16000, 320, 5));
    boundaryRankCosts.put("p13z_p14a", new RankCost(17000, 340, 5));
    boundaryRankCosts.put("p14z_p15a", new RankCost(18000, 360, 5));
    boundaryRankCosts.put("p15z_p16a", new RankCost(19000, 380, 6));
    boundaryRankCosts.put("p16z_p17a", new RankCost(20000, 400, 6));
    boundaryRankCosts.put("p17z_p18a", new RankCost(21000, 420, 6));
    boundaryRankCosts.put("p18z_p19a", new RankCost(22000, 440, 6));
    boundaryRankCosts.put("p19z_p20a", new RankCost(23000, 460, 6));
    boundaryRankCosts.put("p20z_p21a", new RankCost(24000, 480, 7));
    boundaryRankCosts.put("p21z_p22a", new RankCost(25000, 500, 7));
    boundaryRankCosts.put("p22z_p23a", new RankCost(26000, 520, 7));
    boundaryRankCosts.put("p23z_p24a", new RankCost(27000, 540, 7));
    boundaryRankCosts.put("p24z_p25a", new RankCost(28000, 560, 7));

    // p25z -> a1p1a
    boundaryRankCosts.put("p25z_a1p1a", new RankCost(30000, 600, 8));

    // a1 boundaries
boundaryRankCosts.put("a1p1z_a1p2a", new RankCost(5000, 100, 8));
boundaryRankCosts.put("a1p2z_a1p3a", new RankCost(5500, 110, 8));
boundaryRankCosts.put("a1p3z_a1p4a", new RankCost(6000, 120, 8));
boundaryRankCosts.put("a1p4z_a1p5a", new RankCost(6500, 130, 8));
boundaryRankCosts.put("a1p5z_a1p6a", new RankCost(7000, 140, 8));
boundaryRankCosts.put("a1p6z_a1p7a", new RankCost(7500, 150, 8));
boundaryRankCosts.put("a1p7z_a1p8a", new RankCost(8000, 160, 8));
boundaryRankCosts.put("a1p8z_a1p9a", new RankCost(8500, 170, 8));
boundaryRankCosts.put("a1p9z_a1p10a", new RankCost(9000, 180, 8));
boundaryRankCosts.put("a1p10z_a1p11a", new RankCost(9500, 190, 8));
boundaryRankCosts.put("a1p11z_a1p12a", new RankCost(10000, 200, 8));
boundaryRankCosts.put("a1p12z_a1p13a", new RankCost(10500, 210, 8));
boundaryRankCosts.put("a1p13z_a1p14a", new RankCost(11000, 220, 8));
boundaryRankCosts.put("a1p14z_a1p15a", new RankCost(11500, 230, 8));
boundaryRankCosts.put("a1p15z_a1p16a", new RankCost(12000, 240, 8));
boundaryRankCosts.put("a1p16z_a1p17a", new RankCost(12500, 250, 8));
boundaryRankCosts.put("a1p17z_a1p18a", new RankCost(13000, 260, 8));
boundaryRankCosts.put("a1p18z_a1p19a", new RankCost(13500, 270, 8));
boundaryRankCosts.put("a1p19z_a1p20a", new RankCost(14000, 280, 8));
boundaryRankCosts.put("a1p20z_a1p21a", new RankCost(14500, 290, 8));
boundaryRankCosts.put("a1p21z_a1p22a", new RankCost(15000, 300, 8));
boundaryRankCosts.put("a1p22z_a1p23a", new RankCost(15500, 310, 8));
boundaryRankCosts.put("a1p23z_a1p24a", new RankCost(16000, 320, 8));
boundaryRankCosts.put("a1p24z_a1p25a", new RankCost(16500, 330, 8));

// a1 to a2 boundary
boundaryRankCosts.put("a1p25z_a2p1a", new RankCost(35000, 700, 8));

// a2 boundaries
boundaryRankCosts.put("a2p1z_a2p2a", new RankCost(36000, 720,  8));
boundaryRankCosts.put("a2p2z_a2p3a", new RankCost(36500, 730,  8));
boundaryRankCosts.put("a2p3z_a2p4a", new RankCost(37000, 740,  8));
boundaryRankCosts.put("a2p4z_a2p5a", new RankCost(37500, 750,  8));
boundaryRankCosts.put("a2p5z_a2p6a", new RankCost(38000, 760,  8));
boundaryRankCosts.put("a2p6z_a2p7a", new RankCost(38500, 770,  8));
boundaryRankCosts.put("a2p7z_a2p8a", new RankCost(39000, 780,  8));
boundaryRankCosts.put("a2p8z_a2p9a", new RankCost(39500, 790,  8));
boundaryRankCosts.put("a2p9z_a2p10a", new RankCost(40000, 800,  8));
boundaryRankCosts.put("a2p10z_a2p11a", new RankCost(40500, 810,  8));
boundaryRankCosts.put("a2p11z_a2p12a", new RankCost(41000, 820,  8));
boundaryRankCosts.put("a2p12z_a2p13a", new RankCost(41500, 830,  8));
boundaryRankCosts.put("a2p13z_a2p14a", new RankCost(42000, 840,  8));
boundaryRankCosts.put("a2p14z_a2p15a", new RankCost(42500, 850,  8));
boundaryRankCosts.put("a2p15z_a2p16a", new RankCost(43000, 860,  8));
boundaryRankCosts.put("a2p16z_a2p17a", new RankCost(43500, 870,  8));
boundaryRankCosts.put("a2p17z_a2p18a", new RankCost(44000, 880,  8));
boundaryRankCosts.put("a2p18z_a2p19a", new RankCost(44500, 890,  8));
boundaryRankCosts.put("a2p19z_a2p20a", new RankCost(45000, 900,  8));
boundaryRankCosts.put("a2p20z_a2p21a", new RankCost(45500, 910,  8));
boundaryRankCosts.put("a2p21z_a2p22a", new RankCost(46000, 920,  8));
boundaryRankCosts.put("a2p22z_a2p23a", new RankCost(46500, 930,  8));
boundaryRankCosts.put("a2p23z_a2p24a", new RankCost(47000, 940,  8));
boundaryRankCosts.put("a2p24z_a2p25a", new RankCost(47500, 950,  8));

// a2 to a3 boundary
boundaryRankCosts.put("a2p25z_a3p1a", new RankCost(40000, 800,  8));

// a3 boundaries
boundaryRankCosts.put("a3p1z_a3p2a", new RankCost(40500, 810,  8));
boundaryRankCosts.put("a3p2z_a3p3a", new RankCost(41000, 820,  8));
boundaryRankCosts.put("a3p3z_a3p4a", new RankCost(41500, 830,  8));
boundaryRankCosts.put("a3p4z_a3p5a", new RankCost(42000, 840,  8));
boundaryRankCosts.put("a3p5z_a3p6a", new RankCost(42500, 850,  8));
boundaryRankCosts.put("a3p6z_a3p7a", new RankCost(43000, 860,  8));
boundaryRankCosts.put("a3p7z_a3p8a", new RankCost(43500, 870,  8));
boundaryRankCosts.put("a3p8z_a3p9a", new RankCost(44000, 880,  8));
boundaryRankCosts.put("a3p9z_a3p10a", new RankCost(44500, 890,  8));
boundaryRankCosts.put("a3p10z_a3p11a", new RankCost(45000, 900,  8));
boundaryRankCosts.put("a3p11z_a3p12a", new RankCost(45500, 910,  8));
boundaryRankCosts.put("a3p12z_a3p13a", new RankCost(46000, 920,  8));
boundaryRankCosts.put("a3p13z_a3p14a", new RankCost(46500, 930,  8));
boundaryRankCosts.put("a3p14z_a3p15a", new RankCost(47000, 940,  8));
boundaryRankCosts.put("a3p15z_a3p16a", new RankCost(47500, 950,  8));
boundaryRankCosts.put("a3p16z_a3p17a", new RankCost(48000, 960,  8));
boundaryRankCosts.put("a3p17z_a3p18a", new RankCost(48500, 970,  8));
boundaryRankCosts.put("a3p18z_a3p19a", new RankCost(49000, 980,  8));
boundaryRankCosts.put("a3p19z_a3p20a", new RankCost(49500, 990,  8));
boundaryRankCosts.put("a3p20z_a3p21a", new RankCost(50000, 1000,  8));
boundaryRankCosts.put("a3p21z_a3p22a", new RankCost(50500, 1010,  8));
boundaryRankCosts.put("a3p22z_a3p23a", new RankCost(51000, 1020,  8));
boundaryRankCosts.put("a3p23z_a3p24a", new RankCost(51500, 1030,  8));
boundaryRankCosts.put("a3p24z_a3p25a", new RankCost(52000, 1040,  8));

// a3 to a4 boundary
boundaryRankCosts.put("a3p25z_a4p1a", new RankCost(45000, 900,  8));

// a4 boundaries
boundaryRankCosts.put("a4p1z_a4p2a", new RankCost(45500, 910,  8));
boundaryRankCosts.put("a4p2z_a4p3a", new RankCost(46000, 920,  8));
boundaryRankCosts.put("a4p3z_a4p4a", new RankCost(46500, 930,  8));
boundaryRankCosts.put("a4p4z_a4p5a", new RankCost(47000, 940,  8));
boundaryRankCosts.put("a4p5z_a4p6a", new RankCost(47500, 950,  8));
boundaryRankCosts.put("a4p6z_a4p7a", new RankCost(48000, 960,  8));
boundaryRankCosts.put("a4p7z_a4p8a", new RankCost(48500, 970,  8));
boundaryRankCosts.put("a4p8z_a4p9a", new RankCost(49000, 980,  8));
boundaryRankCosts.put("a4p9z_a4p10a", new RankCost(49500, 990,  8));
boundaryRankCosts.put("a4p10z_a4p11a", new RankCost(50000, 1000,  8));
boundaryRankCosts.put("a4p11z_a4p12a", new RankCost(50500, 1010,  8));
boundaryRankCosts.put("a4p12z_a4p13a", new RankCost(51000, 1020,  8));
boundaryRankCosts.put("a4p13z_a4p14a", new RankCost(51500, 1030, 17));
boundaryRankCosts.put("a4p14z_a4p15a", new RankCost(52000, 1040, 17));
boundaryRankCosts.put("a4p15z_a4p16a", new RankCost(52500, 1050, 17));
boundaryRankCosts.put("a4p16z_a4p17a", new RankCost(53000, 1060, 17));
boundaryRankCosts.put("a4p17z_a4p18a", new RankCost(53500, 1070, 17));
boundaryRankCosts.put("a4p18z_a4p19a", new RankCost(54000, 1080, 17));
boundaryRankCosts.put("a4p19z_a4p20a", new RankCost(54500, 1090, 18));
boundaryRankCosts.put("a4p20z_a4p21a", new RankCost(55000, 1100, 18));
boundaryRankCosts.put("a4p21z_a4p22a", new RankCost(55500, 1110, 18));
boundaryRankCosts.put("a4p22z_a4p23a", new RankCost(56000, 1120, 18));
boundaryRankCosts.put("a4p23z_a4p24a", new RankCost(56500, 1130, 18));
boundaryRankCosts.put("a4p24z_a4p25a", new RankCost(57000, 1140, 18));

// a4 to a5 boundary
boundaryRankCosts.put("a4p25z_a5p1a", new RankCost(50000, 1000,  8));

// a5 boundaries
boundaryRankCosts.put("a5p1z_a5p2a", new RankCost(50500, 1010,  8));
boundaryRankCosts.put("a5p2z_a5p3a", new RankCost(51000, 1020,  8));
boundaryRankCosts.put("a5p3z_a5p4a", new RankCost(51500, 1030,  8));
boundaryRankCosts.put("a5p4z_a5p5a", new RankCost(52000, 1040,  8));
boundaryRankCosts.put("a5p5z_a5p6a", new RankCost(52500, 1050,  8));
boundaryRankCosts.put("a5p6z_a5p7a", new RankCost(53000, 1060,  8));
boundaryRankCosts.put("a5p7z_a5p8a", new RankCost(53500, 1070,  8));
boundaryRankCosts.put("a5p8z_a5p9a", new RankCost(54000, 1080,  8));
boundaryRankCosts.put("a5p9z_a5p10a", new RankCost(54500, 1090,  8));
boundaryRankCosts.put("a5p10z_a5p11a", new RankCost(55000, 1100,  8));
boundaryRankCosts.put("a5p11z_a5p12a", new RankCost(55500, 1110,  8));
boundaryRankCosts.put("a5p12z_a5p13a", new RankCost(56000, 1120,  8));
boundaryRankCosts.put("a5p13z_a5p14a", new RankCost(56500, 1130,  8));
boundaryRankCosts.put("a5p14z_a5p15a", new RankCost(57000, 1140,  8));
boundaryRankCosts.put("a5p15z_a5p16a", new RankCost(57500, 1150,  8));
boundaryRankCosts.put("a5p16z_a5p17a", new RankCost(58000, 1160,  8));
boundaryRankCosts.put("a5p17z_a5p18a", new RankCost(58500, 1170,  8));
boundaryRankCosts.put("a5p18z_a5p19a", new RankCost(59000, 1180,  8));
boundaryRankCosts.put("a5p19z_a5p20a", new RankCost(59500, 1190,  8));
boundaryRankCosts.put("a5p20z_a5p21a", new RankCost(60000, 1200,  8));
boundaryRankCosts.put("a5p21z_a5p22a", new RankCost(60500, 1210,  8));
boundaryRankCosts.put("a5p22z_a5p23a", new RankCost(61000, 1220,  8));
boundaryRankCosts.put("a5p23z_a5p24a", new RankCost(61500, 1230,  8));
boundaryRankCosts.put("a5p24z_a5p25a", new RankCost(62000, 1240,  8));

}


    public static RankCost getRankCost(char rankChar) {
    // For ranks a-z
    return baseRankCosts.get(Character.toLowerCase(rankChar));
}

    public static RankCost getRankCost(String rank) {
    // For boundary ranks like "p1z_p2a", "a1p10z_a1p11a", etc.
    return boundaryRankCosts.get(rank);
}

    public static int getEssenceTier(Object rank) {
    RankCost cost;
    if (rank instanceof Character) {
        cost = getRankCost((char) rank);
    } else if (rank instanceof String) {
        cost = getRankCost((String) rank);
    } else {
        return -1;
    }
    return cost != null ? cost.essenceTier : -1;
}


    // Get the cost of a rank
    public static RankCost getRankCost(String rank, String previousRank) {
    if (rank == null) return null;

    // Check boundary costs first
    if (previousRank != null) {
        String boundaryKey = previousRank + "_" + rank;
        if (boundaryRankCosts.containsKey(boundaryKey)) {
            return boundaryRankCosts.get(boundaryKey);
        }
    }

    if (rank.matches("^[a-z]$")) {
        return baseRankCosts.get(rank.charAt(0));
    }

    if (rank.matches("^p(\\d+)[a-z]$")) {
        int prestige = Integer.parseInt(rank.substring(1, rank.length() - 1));
        char letter = rank.charAt(rank.length() - 1);
        RankCost base = baseRankCosts.get(letter);
        if (base == null) return null;

        double money = base.money * (1 + 0.05 * prestige);
        int tier = getEssenceTier(rank);

        int essenceCost = 0;
        if (essenceCostApplies(previousRank, rank)) {
            essenceCost = base.essence;
        }

        return new RankCost(money, essenceCost, tier);
    }

    if (rank.matches("^c(\\d+)p(\\d+)[a-z]$")) {
        String[] parts = rank.substring(1).split("p");
        if (parts.length < 2) return null;

        int p = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        char letter = rank.charAt(rank.length() - 1);
        RankCost base = baseRankCosts.get(letter);
        if (base == null) return null;

        double money = base.money * (1 + 0.05 * p);  // prestige replaced by p here
        int tier = getEssenceTier(rank);

        int essenceCost = base.essence;

        return new RankCost(money, essenceCost, tier);
    }

    return null;
}


    // Get the next rank
    public static String getNextRank(String currentRank) {
    if (currentRank.matches("^[a-z]$")) {
        char next = (char)(currentRank.charAt(0) + 1);
        return next <= 'z' ? String.valueOf(next) : "p1a";
    }

    if (currentRank.matches("^p(\\d+)([a-z])$")) {
        int prestige = Integer.parseInt(currentRank.substring(1, currentRank.length() - 1));
        char letter = currentRank.charAt(currentRank.length() - 1);

        if (letter < 'z') {
            return "p" + prestige + (char)(letter + 1);
        } else if (prestige < 25) {
            return "p" + (prestige + 1) + "a";
        } else {
            return "a1p1a";
        }
    }

    if (currentRank.matches("^a(\\d+)p(\\d+)([a-z])$")) {
        String[] parts = currentRank.substring(1).split("p");
        int ascension = Integer.parseInt(parts[0]);
        int prestige = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        char letter = currentRank.charAt(currentRank.length() - 1);

        if (letter < 'z') {
            return "a" + ascension + "p" + prestige + (char)(letter + 1);
        } else if (prestige < 25) {
            return "a" + ascension + "p" + (prestige + 1) + "a";
        } else if (ascension < 5) {
            return "a" + (ascension + 1) + "p1a";
        } else {
            return null; // Max rank reached
        }
    }

    return null;
}


    // Check if rank is valid
    public static boolean isValidRank(String rank) {
        return rank.matches("^[a-z]$")
            || rank.matches("^p(\\d+)[a-z]$")
            || rank.matches("^c(\\d+)p(\\d+)[a-z]$");
    }

    // Get correct essence tier
    public static int getEssenceTier(String rank) {
        if (rank.matches("^[a-z]$")) {
            char letter = rank.charAt(0);
            return (letter <= 'm') ? 1 : 2;
        }

        if (rank.matches("^p(\\d+)[a-z]$")) {
            int prestige = Integer.parseInt(rank.substring(1, rank.length() - 1));
            if (prestige <= 5) return 3;
            if (prestige <= 10) return 4;
            if (prestige <= 15) return 5;
            if (prestige <= 20) return 6;
            return 7; // 21–25
        }

        if (rank.matches("^c(\\d+)p(\\d+)[a-z]$")) {
            return 8;
        }

        return 0; // Invalid rank
    }

    public static String formatRankName(String rank) {
    if (rank == null || rank.isEmpty()) return "";

    rank = rank.toLowerCase();

    // Single letter rank (e.g. "g")
    if (rank.matches("^[a-z]$")) {
        return rank.toUpperCase();
    }

    // Prestige rank: p#letter (e.g. "p25z")
    if (rank.matches("^p(\\d+)[a-z]$")) {
        int prestige = Integer.parseInt(rank.replaceAll("^p(\\d+)[a-z]$", "$1"));
        char letter = rank.charAt(rank.length() - 1);
        return "P" + prestige + " " + Character.toUpperCase(letter);
    }

    // Ascension rank: a#p#letter (e.g. "a1p1a")
    if (rank.matches("^a\\d+p\\d+[a-z]$")) {
        String[] parts = rank.split("p");
        int ascension = Integer.parseInt(parts[0].substring(1)); // skip "a"
        int prestige = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        char letter = rank.charAt(rank.length() - 1);
        return "A" + ascension + " P" + prestige + " " + Character.toUpperCase(letter);
    }

    // Fallback
    return rank;
}

    public static boolean essenceCostApplies(String currentRank, String nextRank) {
    if (currentRank == null || nextRank == null) return true;

    if (isBoundaryUpgrade(currentRank, nextRank)) {
        return true;
    }

    if (samePrestigeOrChapter(currentRank, nextRank)) {
        return false;
    }

    return true;
}

    private static boolean samePrestigeOrChapter(String rank1, String rank2) {
    if (rank1.length() < 2 || rank2.length() < 2) return false;

    String prefix1 = rank1.substring(0, rank1.length() - 1);
    String prefix2 = rank2.substring(0, rank2.length() - 1);

    return prefix1.equals(prefix2);
}

    public static boolean isBoundaryUpgrade(String from, String to) {
    if (from == null || to == null) return false;

    from = from.toLowerCase();
    to = to.toLowerCase();

    boolean fromEndsWithZ = from.endsWith("z");
    boolean toEndsWithA = to.endsWith("a");

    // e.g., z -> p1a, p1z -> p2a, a1p4z -> a2p1a
    return fromEndsWithZ && toEndsWithA;
}

    public static boolean isPrestigeUpgrade(String from, String to) {
    if (from == null || to == null) return false;

    from = from.toLowerCase();
    to = to.toLowerCase();

    boolean fromEndsWithZ = from.matches(".*p\\d+z$");
    boolean toEndsWithA = to.matches(".*p\\d+a$");

    return fromEndsWithZ && toEndsWithA;
}

    public static boolean isAscendUpgrade(String from, String to) {
    if (from == null || to == null) return false;

    from = from.toLowerCase();
    to = to.toLowerCase();

    boolean fromEndsWithMax = from.matches(".*p25z$");
    boolean toIsNextAscension = to.matches("a\\d+p1a$");

    return fromEndsWithMax && toIsNextAscension;
}

    public static String formatRankTierOnly(String rank) {
    if (rank == null || rank.isEmpty()) return "";

    rank = rank.toLowerCase();

    if (rank.matches("^p\\d+[a-z]$")) {
        int prestige = Integer.parseInt(rank.replaceAll("^p(\\d+)[a-z]$", "$1"));
        return "P" + prestige;
    }

    if (rank.matches("^a\\d+p\\d+[a-z]$")) {
        String[] parts = rank.split("p");
        int ascension = Integer.parseInt(parts[0].substring(1)); // a#
        return "A" + ascension;
    }

    return rank;
}

    public static String formatRankPrestigeOnly(String rank) {
    if (rank == null || !rank.toLowerCase().contains("p")) return "";

    String prestige = rank.replaceAll(".*p(\\d+)[a-z]$", "$1");
    return "P" + prestige;
}

    public static int compareRanks(String r1, String r2) {
        return Integer.compare(getValue(r1), getValue(r2));
    }

    private static int getValue(String rank) {
        if (rank == null || rank.isEmpty()) return 0;
        rank = rank.toLowerCase();

        int prestige = 0;
        StringBuilder number = new StringBuilder();
        int i = 0;

        // Handle single-letter legacy ranks like "a"
        if (rank.length() == 1) return rank.charAt(0) - 'a' + 1;

        while (i < rank.length()) {
            if (rank.charAt(i) == 'p') {
                i++;
                while (i < rank.length() && Character.isDigit(rank.charAt(i))) {
                    number.append(rank.charAt(i++));
                }
                prestige += Integer.parseInt(number.toString());
                number.setLength(0);
            } else if (Character.isDigit(rank.charAt(i))) {
                while (i < rank.length() && Character.isDigit(rank.charAt(i))) {
                    number.append(rank.charAt(i++));
                }
                prestige += Integer.parseInt(number.toString()) * 1000; // Era level
                number.setLength(0);
            } else if (Character.isLetter(rank.charAt(i))) {
                prestige += (rank.charAt(i) - 'a' + 1);
                i++;
            } else {
                i++;
            }
        }

        return prestige;
    }

    public static boolean isAtLeastP1a(Player player) {
        return compareRanks(getRank(player), "p1a") >= 0;
    }

    public static boolean isAtLeastP10a(Player player) {
        return compareRanks(getRank(player), "p10a") >= 0;
    }

    public static int getMaxEnchantCount(Player player, Material type) {
    String rank = getRank(player); // e.g. "a", "p1a", "p5a"

    int baseMax = 5;
    int maxAtCap = 9;

    int prestige = 0;

    if (rank != null && rank.matches("^p(\\d+)[a-z]$")) {
        prestige = Integer.parseInt(rank.replaceAll("^p(\\d+)[a-z]$", "$1"));
    }

    int maxEnchantsByRank;

    if (prestige == 0) {
        maxEnchantsByRank = baseMax;
    } else {
        maxEnchantsByRank = baseMax + (prestige - 1);
    }

    if (maxEnchantsByRank > maxAtCap) maxEnchantsByRank = maxAtCap;

    if (type.toString().contains("NETHERITE")) {
        maxEnchantsByRank++;
    }

    return maxEnchantsByRank;
}

    public static boolean canUseEnchants(HeldToolInfo tool, Player player) {
    String playerRank = getRank(player);
    List<String> failMessages = new ArrayList<>();

    for (String enchant : tool.customEnchants.keySet()) {
        String rarity = EnchantmentData.getRarity(enchant);

        if ("PRESTIGE".equalsIgnoreCase(rarity)) {
            if (compareRanks(playerRank, "p1a") < 1) {
                failMessages.add("You must be Prestige 1 to use tools with " + enchant + "!");
            }
        } else if ("PRESTIGE+".equalsIgnoreCase(rarity)) {
            if (compareRanks(playerRank, "p10a") < 1) {
                failMessages.add("You must be Prestige 10 to use tools with " + enchant + "!");
            }
        }
    }

    if (!failMessages.isEmpty()) {
        player.sendMessage(ChatColor.RED + failMessages.get(0)); // only send first message
        return false;
    }
    return true;
}


}