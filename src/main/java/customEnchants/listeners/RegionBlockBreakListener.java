package customEnchants.listeners;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import customEnchants.managers.RankManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegionBlockBreakListener implements Listener {

    private final RegionContainer container;
    

    public RegionBlockBreakListener() {
        this.container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        RegionManager regions = container.get(BukkitAdapter.adapt(loc.getWorld()));
        if (regions == null) return;

        ApplicableRegionSet regionSet = regions.getApplicableRegions(BukkitAdapter.asBlockVector(loc));

        for (ProtectedRegion region : regionSet) {
            String regionId = region.getId(); // example: "mine_p5b" or "mine_a2p10z"

            if (regionId.startsWith("mine_")) {
                String requiredRank = regionId.substring(5);  // strip "mine_"
                String playerRank = RankManager.getRank(player);

                if (!hasRequiredRank(playerRank, requiredRank)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to mine here.");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private boolean hasRequiredRank(String playerRank, String requiredRank) {
        int playerValue = rankToValue(playerRank);
        int requiredValue = rankToValue(requiredRank);
        return playerValue >= requiredValue;
    }

    private int rankToValue(String rank) {
    Pattern pattern = Pattern.compile(
        "^(?:([a-z])|p(\\d{1,2})([a-z])|a(\\d)p(\\d{1,2})([a-z]))$",
        Pattern.CASE_INSENSITIVE
    );
    Matcher matcher = pattern.matcher(rank.toLowerCase());

    if (!matcher.matches()) {
        return 0; // invalid rank
    }

    if (matcher.group(1) != null) {
        // Simple letter rank e.g. "a"
        char letter = matcher.group(1).charAt(0);
        return (letter - 'a');  // 0 for 'a', 25 for 'z'
    } else if (matcher.group(2) != null && matcher.group(3) != null) {
        // Prestige rank e.g. p1a
        int prestige = Integer.parseInt(matcher.group(2));
        char letter = matcher.group(3).charAt(0);
        return 1000 * prestige + (letter - 'a');
    } else {
        // Ascension rank e.g. a1p1a
        int ascension = Integer.parseInt(matcher.group(4));
        int prestige = Integer.parseInt(matcher.group(5));
        char letter = matcher.group(6).charAt(0);
        return 100000 * ascension + 1000 * prestige + (letter - 'a');
    }
}

}
