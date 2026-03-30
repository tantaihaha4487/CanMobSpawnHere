package net.thanachot.canMobSpawnHere.service;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HighlightCache {

    private final Map<UUID, Map<HighlightKey, Integer>> highlightsByPlayer = new HashMap<>();

    public Map<HighlightKey, Integer> get(Player player) {
        return highlightsByPlayer.getOrDefault(player.getUniqueId(), Map.of());
    }

    public void put(Player player, Map<HighlightKey, Integer> highlights) {
        highlightsByPlayer.put(player.getUniqueId(), highlights);
    }

    public void clear(Player player) {
        highlightsByPlayer.remove(player.getUniqueId());
    }

    public void clear(UUID playerId) {
        highlightsByPlayer.remove(playerId);
    }

    public Map<UUID, Map<HighlightKey, Integer>> snapshot() {
        return Map.copyOf(highlightsByPlayer);
    }

    public record HighlightKey(String worldName, int x, int y, int z) {
        public static HighlightKey from(Block block) {
            return new HighlightKey(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        }

        public boolean isWithinRange(Block block, int horizontalRadius, int minYOffset, int maxYOffset) {
            int dx = x - block.getX();
            int dz = z - block.getZ();
            int dy = y - block.getY();
            return worldName.equals(block.getWorld().getName())
                    && Math.abs(dx) <= horizontalRadius
                    && Math.abs(dz) <= horizontalRadius
                    && dy >= minYOffset
                    && dy <= maxYOffset;
        }
    }
}
