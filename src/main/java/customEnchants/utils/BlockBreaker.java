package customEnchants.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class BlockBreaker {

    private static final Random random = new Random();

    private static boolean shouldProc(ItemStack tool, String enchantName, int level) {
        int index = EnchantmentData.getEnchantmentIndex(enchantName);
        if (index == -1) return false;

        int amplifyLevel = getAmplifyLevel(tool, enchantName);

        double baseChance = EnchantmentData.ENCHANT_PROC_CHANCE[index];
        double amplifyBonus = amplifyLevel * 0.05;
        double chance = Math.min(baseChance * level + amplifyBonus, 1.0);

        return random.nextDouble() < chance;
    }

    private static int getAmplifyLevel(ItemStack tool, String enchantName) {
        HeldToolInfo info = HeldToolInfo.fromItem(tool);
        return info.getLevel("Amplify");
    }

    public static int breakBlastArea(Block baseBlock, Player player, ItemStack tool, int blastLevel, int autoSmeltLevel) {
        if (!shouldProc(tool, "Blast", blastLevel)) return 0;

        HeldToolInfo toolInfo = HeldToolInfo.fromItem(tool);
        if (!RankUtils.canUseEnchants(toolInfo, player)) {
            return 0;
        }

        int broken = 0;
        BlockFace facing = getFacingDirection(player);
        Block center = baseBlock.getRelative(facing);

        int centerY = center.getY();
        int playerY = player.getLocation().getBlockY();

        if (centerY < playerY) {
            center = center.getRelative(0, 1, 0);
        }

        int minY = centerY < playerY ? -1 : (centerY == playerY ? 0 : -1);
        int maxY = centerY < playerY ? 1 : (centerY == playerY ? 2 : 1);

        for (int x = -1; x <= 1; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block target = center.getRelative(x, y, z);
                    if (target.getType().isAir() || target.getType() == Material.BEDROCK) continue;

                    if (autoSmeltLevel > 0 && AutoSmelt.canSmelt(target.getType())) {
                        AutoSmelt.tryAutoSmelt(target, tool, autoSmeltLevel);
                    } else {
                        target.breakNaturally(tool);
                    }

                    broken++;
                }
            }
        }

        return broken;
    }

    public static int breakWallBreakerArea(Block baseBlock, Player player, ItemStack tool, int wallBreakerLevel, int autoSmeltLevel) {
        if (!shouldProc(tool, "Wall Breaker", wallBreakerLevel)) return 0;

        HeldToolInfo toolInfo = HeldToolInfo.fromItem(tool);
        if (!RankUtils.canUseEnchants(toolInfo, player)) {
            return 0;
        }

        int broken = 0;
        BlockFace facing = getFacingDirection(player);
        int centerY = baseBlock.getY();
        int playerY = player.getLocation().getBlockY();

        int minY = centerY < playerY ? -1 : (centerY == playerY ? 0 : -1);
        int maxY = centerY < playerY ? 1 : (centerY == playerY ? 2 : 1);

        for (int dy = minY; dy <= maxY; dy++) {
            int y = centerY + dy;
            if (y < playerY - 1) continue;

            for (int dx = -1; dx <= 1; dx++) {
                Block target;
                switch (facing) {
                    case NORTH:
                    case SOUTH:
                        target = baseBlock.getRelative(dx, dy, 0);
                        break;
                    case EAST:
                    case WEST:
                        target = baseBlock.getRelative(0, dy, dx);
                        break;
                    default:
                        target = baseBlock.getRelative(dx, dy, 0);
                        break;
                }

                if (target.getType().isAir() || target.getType() == Material.BEDROCK) continue;

                if (autoSmeltLevel > 0 && AutoSmelt.canSmelt(target.getType())) {
                    AutoSmelt.tryAutoSmelt(target, tool, autoSmeltLevel);
                } else {
                    target.breakNaturally(tool);
                }

                broken++;
            }
        }

        return broken;
    }

    public static BlockFace getFacingDirection(Player player) {
        float yaw = (player.getLocation().getYaw() + 360) % 360;
        if (yaw < 45 || yaw >= 315) return BlockFace.SOUTH;
        if (yaw < 135) return BlockFace.WEST;
        if (yaw < 225) return BlockFace.NORTH;
        return BlockFace.EAST;
    }
}
