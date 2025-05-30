package customEnchants.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

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
    baseRankCosts.put('a', new RankCost(1000, 10, 1));
    baseRankCosts.put('b', new RankCost(1250, 12, 1));
    baseRankCosts.put('c', new RankCost(1500, 14, 1));
    baseRankCosts.put('d', new RankCost(1750, 16, 1));
    baseRankCosts.put('e', new RankCost(2000, 18, 1));
    baseRankCosts.put('f', new RankCost(2250, 20, 1));
    baseRankCosts.put('g', new RankCost(2500, 22, 1));
    baseRankCosts.put('h', new RankCost(2750, 24, 1));
    baseRankCosts.put('i', new RankCost(3000, 26, 1));
    baseRankCosts.put('j', new RankCost(3250, 28, 1));
    baseRankCosts.put('k', new RankCost(3500, 30, 1));
    baseRankCosts.put('l', new RankCost(3750, 32, 1));
    baseRankCosts.put('m', new RankCost(4000, 34, 1));
    
    // From n to z, essence tier changes to 2
    baseRankCosts.put('n', new RankCost(4250, 36, 2));
    baseRankCosts.put('o', new RankCost(4500, 38, 2));
    baseRankCosts.put('p', new RankCost(4750, 40, 2));
    baseRankCosts.put('q', new RankCost(5000, 42, 2));
    baseRankCosts.put('r', new RankCost(5250, 44, 2));
    baseRankCosts.put('s', new RankCost(5500, 46, 2));
    baseRankCosts.put('t', new RankCost(5750, 48, 2));
    baseRankCosts.put('u', new RankCost(6000, 50, 2));
    baseRankCosts.put('v', new RankCost(6250, 52, 2));
    baseRankCosts.put('w', new RankCost(6500, 54, 2));
    baseRankCosts.put('x', new RankCost(6750, 56, 2));
    baseRankCosts.put('y', new RankCost(7000, 58, 2));
    baseRankCosts.put('z', new RankCost(7250, 60, 2));
}

    private static final Map<String, RankCost> boundaryRankCosts = new HashMap<>();

    static {
    boundaryRankCosts.put("z_p1a", new RankCost(5000, 100, 3));
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

    // p25z -> c1p1a
    boundaryRankCosts.put("p25z_c1p1a", new RankCost(30000, 600, 8));

    // c1 boundaries
boundaryRankCosts.put("c1p1z_c1p2a", new RankCost(5000, 100, 8));
boundaryRankCosts.put("c1p2z_c1p3a", new RankCost(5500, 110, 8));
boundaryRankCosts.put("c1p3z_c1p4a", new RankCost(6000, 120, 8));
boundaryRankCosts.put("c1p4z_c1p5a", new RankCost(6500, 130, 8));
boundaryRankCosts.put("c1p5z_c1p6a", new RankCost(7000, 140, 8));
boundaryRankCosts.put("c1p6z_c1p7a", new RankCost(7500, 150, 8));
boundaryRankCosts.put("c1p7z_c1p8a", new RankCost(8000, 160, 8));
boundaryRankCosts.put("c1p8z_c1p9a", new RankCost(8500, 170, 8));
boundaryRankCosts.put("c1p9z_c1p10a", new RankCost(9000, 180, 8));
boundaryRankCosts.put("c1p10z_c1p11a", new RankCost(9500, 190, 8));
boundaryRankCosts.put("c1p11z_c1p12a", new RankCost(10000, 200, 8));
boundaryRankCosts.put("c1p12z_c1p13a", new RankCost(10500, 210, 8));
boundaryRankCosts.put("c1p13z_c1p14a", new RankCost(11000, 220, 8));
boundaryRankCosts.put("c1p14z_c1p15a", new RankCost(11500, 230, 8));
boundaryRankCosts.put("c1p15z_c1p16a", new RankCost(12000, 240, 8));
boundaryRankCosts.put("c1p16z_c1p17a", new RankCost(12500, 250, 8));
boundaryRankCosts.put("c1p17z_c1p18a", new RankCost(13000, 260, 8));
boundaryRankCosts.put("c1p18z_c1p19a", new RankCost(13500, 270, 8));
boundaryRankCosts.put("c1p19z_c1p20a", new RankCost(14000, 280, 8));
boundaryRankCosts.put("c1p20z_c1p21a", new RankCost(14500, 290, 8));
boundaryRankCosts.put("c1p21z_c1p22a", new RankCost(15000, 300, 8));
boundaryRankCosts.put("c1p22z_c1p23a", new RankCost(15500, 310, 8));
boundaryRankCosts.put("c1p23z_c1p24a", new RankCost(16000, 320, 8));
boundaryRankCosts.put("c1p24z_c1p25a", new RankCost(16500, 330, 8));

// c1 to c2 boundary
boundaryRankCosts.put("c1p25z_c2p1a", new RankCost(35000, 700, 8));

// c2 boundaries
boundaryRankCosts.put("c2p1z_c2p2a", new RankCost(36000, 720,  8));
boundaryRankCosts.put("c2p2z_c2p3a", new RankCost(36500, 730,  8));
boundaryRankCosts.put("c2p3z_c2p4a", new RankCost(37000, 740,  8));
boundaryRankCosts.put("c2p4z_c2p5a", new RankCost(37500, 750,  8));
boundaryRankCosts.put("c2p5z_c2p6a", new RankCost(38000, 760,  8));
boundaryRankCosts.put("c2p6z_c2p7a", new RankCost(38500, 770,  8));
boundaryRankCosts.put("c2p7z_c2p8a", new RankCost(39000, 780,  8));
boundaryRankCosts.put("c2p8z_c2p9a", new RankCost(39500, 790,  8));
boundaryRankCosts.put("c2p9z_c2p10a", new RankCost(40000, 800,  8));
boundaryRankCosts.put("c2p10z_c2p11a", new RankCost(40500, 810,  8));
boundaryRankCosts.put("c2p11z_c2p12a", new RankCost(41000, 820,  8));
boundaryRankCosts.put("c2p12z_c2p13a", new RankCost(41500, 830,  8));
boundaryRankCosts.put("c2p13z_c2p14a", new RankCost(42000, 840,  8));
boundaryRankCosts.put("c2p14z_c2p15a", new RankCost(42500, 850,  8));
boundaryRankCosts.put("c2p15z_c2p16a", new RankCost(43000, 860,  8));
boundaryRankCosts.put("c2p16z_c2p17a", new RankCost(43500, 870,  8));
boundaryRankCosts.put("c2p17z_c2p18a", new RankCost(44000, 880,  8));
boundaryRankCosts.put("c2p18z_c2p19a", new RankCost(44500, 890,  8));
boundaryRankCosts.put("c2p19z_c2p20a", new RankCost(45000, 900,  8));
boundaryRankCosts.put("c2p20z_c2p21a", new RankCost(45500, 910,  8));
boundaryRankCosts.put("c2p21z_c2p22a", new RankCost(46000, 920,  8));
boundaryRankCosts.put("c2p22z_c2p23a", new RankCost(46500, 930,  8));
boundaryRankCosts.put("c2p23z_c2p24a", new RankCost(47000, 940,  8));
boundaryRankCosts.put("c2p24z_c2p25a", new RankCost(47500, 950,  8));

// c2 to c3 boundary
boundaryRankCosts.put("c2p25z_c3p1a", new RankCost(40000, 800,  8));

// c3 boundaries
boundaryRankCosts.put("c3p1z_c3p2a", new RankCost(40500, 810,  8));
boundaryRankCosts.put("c3p2z_c3p3a", new RankCost(41000, 820,  8));
boundaryRankCosts.put("c3p3z_c3p4a", new RankCost(41500, 830,  8));
boundaryRankCosts.put("c3p4z_c3p5a", new RankCost(42000, 840,  8));
boundaryRankCosts.put("c3p5z_c3p6a", new RankCost(42500, 850,  8));
boundaryRankCosts.put("c3p6z_c3p7a", new RankCost(43000, 860,  8));
boundaryRankCosts.put("c3p7z_c3p8a", new RankCost(43500, 870,  8));
boundaryRankCosts.put("c3p8z_c3p9a", new RankCost(44000, 880,  8));
boundaryRankCosts.put("c3p9z_c3p10a", new RankCost(44500, 890,  8));
boundaryRankCosts.put("c3p10z_c3p11a", new RankCost(45000, 900,  8));
boundaryRankCosts.put("c3p11z_c3p12a", new RankCost(45500, 910,  8));
boundaryRankCosts.put("c3p12z_c3p13a", new RankCost(46000, 920,  8));
boundaryRankCosts.put("c3p13z_c3p14a", new RankCost(46500, 930,  8));
boundaryRankCosts.put("c3p14z_c3p15a", new RankCost(47000, 940,  8));
boundaryRankCosts.put("c3p15z_c3p16a", new RankCost(47500, 950,  8));
boundaryRankCosts.put("c3p16z_c3p17a", new RankCost(48000, 960,  8));
boundaryRankCosts.put("c3p17z_c3p18a", new RankCost(48500, 970,  8));
boundaryRankCosts.put("c3p18z_c3p19a", new RankCost(49000, 980,  8));
boundaryRankCosts.put("c3p19z_c3p20a", new RankCost(49500, 990,  8));
boundaryRankCosts.put("c3p20z_c3p21a", new RankCost(50000, 1000,  8));
boundaryRankCosts.put("c3p21z_c3p22a", new RankCost(50500, 1010,  8));
boundaryRankCosts.put("c3p22z_c3p23a", new RankCost(51000, 1020,  8));
boundaryRankCosts.put("c3p23z_c3p24a", new RankCost(51500, 1030,  8));
boundaryRankCosts.put("c3p24z_c3p25a", new RankCost(52000, 1040,  8));

// c3 to c4 boundary
boundaryRankCosts.put("c3p25z_c4p1a", new RankCost(45000, 900,  8));

// c4 boundaries
boundaryRankCosts.put("c4p1z_c4p2a", new RankCost(45500, 910,  8));
boundaryRankCosts.put("c4p2z_c4p3a", new RankCost(46000, 920,  8));
boundaryRankCosts.put("c4p3z_c4p4a", new RankCost(46500, 930,  8));
boundaryRankCosts.put("c4p4z_c4p5a", new RankCost(47000, 940,  8));
boundaryRankCosts.put("c4p5z_c4p6a", new RankCost(47500, 950,  8));
boundaryRankCosts.put("c4p6z_c4p7a", new RankCost(48000, 960,  8));
boundaryRankCosts.put("c4p7z_c4p8a", new RankCost(48500, 970,  8));
boundaryRankCosts.put("c4p8z_c4p9a", new RankCost(49000, 980,  8));
boundaryRankCosts.put("c4p9z_c4p10a", new RankCost(49500, 990,  8));
boundaryRankCosts.put("c4p10z_c4p11a", new RankCost(50000, 1000,  8));
boundaryRankCosts.put("c4p11z_c4p12a", new RankCost(50500, 1010,  8));
boundaryRankCosts.put("c4p12z_c4p13a", new RankCost(51000, 1020,  8));
boundaryRankCosts.put("c4p13z_c4p14a", new RankCost(51500, 1030, 17));
boundaryRankCosts.put("c4p14z_c4p15a", new RankCost(52000, 1040, 17));
boundaryRankCosts.put("c4p15z_c4p16a", new RankCost(52500, 1050, 17));
boundaryRankCosts.put("c4p16z_c4p17a", new RankCost(53000, 1060, 17));
boundaryRankCosts.put("c4p17z_c4p18a", new RankCost(53500, 1070, 17));
boundaryRankCosts.put("c4p18z_c4p19a", new RankCost(54000, 1080, 17));
boundaryRankCosts.put("c4p19z_c4p20a", new RankCost(54500, 1090, 18));
boundaryRankCosts.put("c4p20z_c4p21a", new RankCost(55000, 1100, 18));
boundaryRankCosts.put("c4p21z_c4p22a", new RankCost(55500, 1110, 18));
boundaryRankCosts.put("c4p22z_c4p23a", new RankCost(56000, 1120, 18));
boundaryRankCosts.put("c4p23z_c4p24a", new RankCost(56500, 1130, 18));
boundaryRankCosts.put("c4p24z_c4p25a", new RankCost(57000, 1140, 18));

// c4 to c5 boundary
boundaryRankCosts.put("c4p25z_c5p1a", new RankCost(50000, 1000,  8));

// c5 boundaries
boundaryRankCosts.put("c5p1z_c5p2a", new RankCost(50500, 1010,  8));
boundaryRankCosts.put("c5p2z_c5p3a", new RankCost(51000, 1020,  8));
boundaryRankCosts.put("c5p3z_c5p4a", new RankCost(51500, 1030,  8));
boundaryRankCosts.put("c5p4z_c5p5a", new RankCost(52000, 1040,  8));
boundaryRankCosts.put("c5p5z_c5p6a", new RankCost(52500, 1050,  8));
boundaryRankCosts.put("c5p6z_c5p7a", new RankCost(53000, 1060,  8));
boundaryRankCosts.put("c5p7z_c5p8a", new RankCost(53500, 1070,  8));
boundaryRankCosts.put("c5p8z_c5p9a", new RankCost(54000, 1080,  8));
boundaryRankCosts.put("c5p9z_c5p10a", new RankCost(54500, 1090,  8));
boundaryRankCosts.put("c5p10z_c5p11a", new RankCost(55000, 1100,  8));
boundaryRankCosts.put("c5p11z_c5p12a", new RankCost(55500, 1110,  8));
boundaryRankCosts.put("c5p12z_c5p13a", new RankCost(56000, 1120,  8));
boundaryRankCosts.put("c5p13z_c5p14a", new RankCost(56500, 1130,  8));
boundaryRankCosts.put("c5p14z_c5p15a", new RankCost(57000, 1140,  8));
boundaryRankCosts.put("c5p15z_c5p16a", new RankCost(57500, 1150,  8));
boundaryRankCosts.put("c5p16z_c5p17a", new RankCost(58000, 1160,  8));
boundaryRankCosts.put("c5p17z_c5p18a", new RankCost(58500, 1170,  8));
boundaryRankCosts.put("c5p18z_c5p19a", new RankCost(59000, 1180,  8));
boundaryRankCosts.put("c5p19z_c5p20a", new RankCost(59500, 1190,  8));
boundaryRankCosts.put("c5p20z_c5p21a", new RankCost(60000, 1200,  8));
boundaryRankCosts.put("c5p21z_c5p22a", new RankCost(60500, 1210,  8));
boundaryRankCosts.put("c5p22z_c5p23a", new RankCost(61000, 1220,  8));
boundaryRankCosts.put("c5p23z_c5p24a", new RankCost(61500, 1230,  8));
boundaryRankCosts.put("c5p24z_c5p25a", new RankCost(62000, 1240,  8));

}


    public static RankCost getRankCost(char rankChar) {
    // For ranks a-z
    return baseRankCosts.get(Character.toLowerCase(rankChar));
}

    public static RankCost getRankCost(String rank) {
    // For boundary ranks like "p1z_p2a", "c1p10z_c1p11a", etc.
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
                return "c1p1a";
            }
        }

        if (currentRank.matches("^c(\\d+)p(\\d+)([a-z])$")) {
            String[] parts = currentRank.substring(1).split("p");
            int c = Integer.parseInt(parts[0]);
            int p = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
            char letter = currentRank.charAt(currentRank.length() - 1);

            if (letter < 'z') {
                return "c" + c + "p" + p + (char)(letter + 1);
            } else if (p < 25) {
                return "c" + c + "p" + (p + 1) + "a";
            } else if (c < 5) {
                return "c" + (c + 1) + "p1a";
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

    if (rank.matches("^[a-z]$")) {
        // Single letter rank: uppercase
        return rank.toUpperCase();
    }

    if (rank.matches("^p(\\d+)([a-z])$")) {
        int prestige = Integer.parseInt(rank.replaceAll("^p(\\d+)([a-z])$", "$1"));
        char letter = rank.charAt(rank.length() - 1);
        return "P" + prestige + " " + Character.toUpperCase(letter);
    }

    if (rank.matches("^c(\\d+)p(\\d+)([a-z])$")) {
        String[] parts = rank.substring(1).split("p");
        int c = Integer.parseInt(parts[0]);
        int p = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
        char letter = rank.charAt(rank.length() - 1);
        return "C" + c + "P" + p + " " + Character.toUpperCase(letter);
    }

    return rank; // fallback, just return original
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
        // Check if from rank ends with 'z' and to rank ends with 'a'
        if (from.length() < 2 || to.length() < 2) return false;
        if (from.charAt(from.length() - 1) == 'z' && to.charAt(to.length() - 1) == 'a') {
            // Extra check: from and to ranks are consecutive boundary steps (e.g. p1z->p2a, c1p25z->c2p1a)
            // Could be extended here if needed
            return true;
        }
        return false;
    }





}