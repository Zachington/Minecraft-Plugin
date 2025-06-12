package customEnchants.utils;

import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.Comparator;    

public class StructureUtil {


    // Ore Scav 
    public static void replaceNearbyBlocksRandomized(Location center, int radius, Predicate<Block> filter, Material replacement, int max) {
    World world = center.getWorld();
    int cx = center.getBlockX();
    int cy = center.getBlockY();
    int cz = center.getBlockZ();
    int playerY = center.getBlockY(); // Redundant with cy, but clear

    List<Block> candidates = new ArrayList<>();

    for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
                int bx = cx + x;
                int by = cy + y;
                int bz = cz + z;

                if (by < playerY - 1) continue;

                Block block = world.getBlockAt(bx, by, bz);
                if (filter.test(block) && block.getType() != replacement) {
                    candidates.add(block);
                }
            }
        }
    }

    Collections.shuffle(candidates);

    for (int i = 0; i < Math.min(max, candidates.size()); i++) {
        candidates.get(i).setType(replacement);
    }
}



    //Frost Touch
    public static void spawnPackedIceClump(Location center, int level) {
    World world = center.getWorld();
    int cx = center.getBlockX();
    int cy = center.getBlockY();
    int cz = center.getBlockZ();

    int radius = 5;
    int maxBlocks = 20 + 10 * level;
    List<BlockDistance> candidates = new ArrayList<>();

    for (int x = -radius; x <= radius; x++) {
        for (int y = -1; y <= 4; y++) {
            for (int z = -radius; z <= radius; z++) {
                int dx = cx + x;
                int dy = cy + y;
                int dz = cz + z;

                double distanceSq = x * x + y * y + z * z;
                if (distanceSq > radius * radius) continue;

                Block block = world.getBlockAt(dx, dy, dz);
                if (!block.getType().isAir() && block.getType() != Material.PACKED_ICE && block.getType() != Material.BEDROCK && block.getType() != Material.POLISHED_TUFF) {
                    candidates.add(new BlockDistance(block, distanceSq));
                }
            }
        }
    }

    // Sort by distance to ensure closer blocks are prioritized
        candidates.sort(Comparator.comparingDouble(b -> b.distanceSq));

        for (int i = 0; i < Math.min(maxBlocks, candidates.size()); i++) {
            candidates.get(i).block.setType(Material.PACKED_ICE);
        }
    }

    private static class BlockDistance {
        Block block;
        double distanceSq;

        BlockDistance(Block block, double distanceSq) {
            this.block = block;
            this.distanceSq = distanceSq;
        }
    }

}
