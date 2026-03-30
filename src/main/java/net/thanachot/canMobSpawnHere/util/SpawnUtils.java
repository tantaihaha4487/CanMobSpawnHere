package net.thanachot.canMobSpawnHere.util;

import org.bukkit.block.Block;

public class SpawnUtils {

    private SpawnUtils() {
    }

    /**
     * Check if a block is valid for mob spawning based on current light level (both
     * sky + block light).
     */
    public static boolean isSpawnable(Block block) {
        if (!hasSpawnableStructure(block)) {
            return false;
        }

        Block blockAbove = block.getRelative(0, 1, 0);
        return blockAbove.getLightLevel() <= 7;
    }

    /**
     * Check if a block is valid for mob spawning based on block structure only
     * (ignoring light).
     * This is used to determine if mobs COULD spawn here at night time.
     */
    public static boolean isSpawnableStructure(Block block) {
        return hasSpawnableStructure(block);
    }

    private static boolean hasSpawnableStructure(Block block) {
        if (block == null)
            return false;

        if (!block.getType().isSolid() || !block.getType().isOccluding()) {
            return false;
        }

        Block blockAbove = block.getRelative(0, 1, 0);
        Block blockTwoAbove = block.getRelative(0, 2, 0);

        return blockAbove.getType().isAir() && blockTwoAbove.getType().isAir();
    }

    /**
     * Check if a block would be spawnable AT NIGHT (only block light matters, not
     * sky light).
     * Returns true if the block structure is valid AND block light level is <= 7.
     */
    public static boolean isSpawnableAtNight(Block block) {
        if (!hasSpawnableStructure(block))
            return false;

        Block blockAbove = block.getRelative(0, 1, 0);
        return blockAbove.getLightFromBlocks() <= 7;
    }
}
