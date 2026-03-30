package net.thanachot.canMobSpawnHere.service;

import net.thanachot.canMobSpawnHere.CanMobSpawnHere;
import net.thanachot.canMobSpawnHere.render.ParticleHighlighter;
import net.thanachot.canMobSpawnHere.util.SpawnUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpawnCheckService {

    public static final int HORIZONTAL_RADIUS = 16;
    public static final int Y_MIN = -2;
    public static final int Y_MAX = 2;

    private final CanMobSpawnHere plugin;
    private final HighlightCache highlightCache;
    private final ParticleHighlighter particleHighlighter;

    public SpawnCheckService(CanMobSpawnHere plugin, HighlightCache highlightCache, ParticleHighlighter particleHighlighter) {
        this.plugin = plugin;
        this.highlightCache = highlightCache;
        this.particleHighlighter = particleHighlighter;
    }

    public void tick() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.isOnline()) {
                continue;
            }

            if (shouldShowSpawnChecks(player)) {
                refreshPlayer(player);
            } else {
                highlightCache.clear(player);
            }
        }
    }

    public void refreshPlayer(Player player) {
        Map<HighlightCache.HighlightKey, Integer> previousHighlights = highlightCache.get(player);
        Map<HighlightCache.HighlightKey, Integer> currentHighlights = new HashMap<>();

        Block origin = player.getLocation().getBlock();
        boolean nightMode = plugin.isSimulateNightTime();

        for (int x = -HORIZONTAL_RADIUS; x <= HORIZONTAL_RADIUS; x++) {
            for (int z = -HORIZONTAL_RADIUS; z <= HORIZONTAL_RADIUS; z++) {
                for (int y = Y_MIN; y <= Y_MAX; y++) {
                    Block block = origin.getRelative(x, y, z);
                    boolean spawnableAtNight = SpawnUtils.isSpawnableAtNight(block);
                    boolean currentlySpawnable = SpawnUtils.isSpawnable(block);
                    boolean shouldHighlight = nightMode ? spawnableAtNight : currentlySpawnable;

                    if (!shouldHighlight) {
                        continue;
                    }

                    Block spawnPos = block.getRelative(0, 1, 0);
                    int blockLight = spawnPos.getLightFromBlocks();
                    HighlightCache.HighlightKey key = HighlightCache.HighlightKey.from(spawnPos);
                    currentHighlights.put(key, blockLight);

                    Integer previousLight = previousHighlights.get(key);
                    if (previousLight == null || !previousLight.equals(blockLight) || Math.random() < 0.25) {
                        particleHighlighter.show(player, spawnPos, currentlySpawnable);
                    }
                }
            }
        }

        highlightCache.put(player, currentHighlights);
    }

    public void refreshForBlock(Block changedBlock) {
        if (changedBlock == null) {
            return;
        }

        Set<UUID> playersToRefresh = new HashSet<>();
        for (Map.Entry<UUID, Map<HighlightCache.HighlightKey, Integer>> entry : highlightCache.snapshot().entrySet()) {
            for (HighlightCache.HighlightKey key : entry.getValue().keySet()) {
                if (key.isWithinRange(changedBlock, HORIZONTAL_RADIUS, Y_MIN, Y_MAX)) {
                    playersToRefresh.add(entry.getKey());
                    break;
                }
            }
        }

        for (UUID uuid : playersToRefresh) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline() && shouldShowSpawnChecks(player)) {
                refreshPlayer(player);
            }
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.isOnline() || !shouldShowSpawnChecks(player)) {
                continue;
            }

            if (isBlockNearPlayer(changedBlock, player)) {
                refreshPlayer(player);
            }
        }
    }

    public void clearPlayer(UUID playerId) {
        highlightCache.clear(playerId);
    }

    public boolean shouldShowSpawnChecks(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        boolean holdingLight = plugin.isLightSource(mainHand.getType()) || plugin.isLightSource(offHand.getType());

        boolean active = plugin.getSpawnAbility().isActive(player);
        if (active && !holdingLight) {
            plugin.getSpawnAbility().onDeactivate(player);
            return false;
        }

        return active;
    }

    private boolean isBlockNearPlayer(Block block, Player player) {
        int dx = block.getX() - player.getLocation().getBlockX();
        int dz = block.getZ() - player.getLocation().getBlockZ();
        int dy = block.getY() - player.getLocation().getBlockY();
        return block.getWorld().equals(player.getWorld())
                && Math.abs(dx) <= HORIZONTAL_RADIUS
                && Math.abs(dz) <= HORIZONTAL_RADIUS
                && dy >= Y_MIN
                && dy <= Y_MAX;
    }
}
