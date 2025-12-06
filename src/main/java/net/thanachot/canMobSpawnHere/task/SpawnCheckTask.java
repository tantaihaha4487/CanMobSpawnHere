package net.thanachot.canMobSpawnHere.task;

import net.thanachot.canMobSpawnHere.CanMobSpawnHere;
import net.thanachot.canMobSpawnHere.util.SpawnUtils;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpawnCheckTask extends BukkitRunnable {
    private final CanMobSpawnHere plugin;

    // Cache of previously highlighted spawn coordinates per player mapping coord ->
    // last observed light level
    private final Map<UUID, Map<String, Integer>> previousHighlights = new HashMap<>();

    // scanning parameters
    private static final int HORIZONTAL_RADIUS = 16;
    private static final int Y_MIN = -2;
    private static final int Y_MAX = 2;

    public SpawnCheckTask(CanMobSpawnHere plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // For each online player, if they're holding a configured light-producing item,
        // scan nearby blocks
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player == null || !player.isOnline())
                continue;

            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();

            // Bukkit guarantees non-null ItemStack; simplify checks
            boolean holdingLight = plugin.isLightSource(mainHand.getType()) || plugin.isLightSource(offHand.getType());

            if (holdingLight) {
                checkAndShowSpawnableBlocks(player);
            } else {
                // Clear any previous highlights for this player so next time they hold an item
                // highlights will reappear fresh
                previousHighlights.remove(player.getUniqueId());
            }
        }
    }

    // Public wrapper so event listener can trigger a scan around a specific block
    public void scanAndUpdateForBlock(Block changedBlock) {
        if (changedBlock == null)
            return;

        // First, find players who already had highlights near this block and force a
        // refresh for them
        Set<UUID> playersToRefresh = new HashSet<>();
        for (Map.Entry<UUID, Map<String, Integer>> entry : previousHighlights.entrySet()) {
            UUID uuid = entry.getKey();
            Map<String, Integer> highlights = entry.getValue();
            if (highlights == null || highlights.isEmpty())
                continue;
            for (String key : highlights.keySet()) {
                // key format: world:x,y,z
                try {
                    String[] parts = key.split(":", 2);
                    if (parts.length != 2)
                        continue;
                    String[] coords = parts[1].split(",");
                    if (coords.length != 3)
                        continue;
                    int bx = Integer.parseInt(coords[0]);
                    int by = Integer.parseInt(coords[1]);
                    int bz = Integer.parseInt(coords[2]);

                    int dx = bx - changedBlock.getX();
                    int dz = bz - changedBlock.getZ();
                    int dy = by - changedBlock.getY();

                    if (Math.abs(dx) <= HORIZONTAL_RADIUS && Math.abs(dz) <= HORIZONTAL_RADIUS && dy >= Y_MIN
                            && dy <= Y_MAX) {
                        playersToRefresh.add(uuid);
                        break;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        for (UUID uuid : playersToRefresh) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null && p.isOnline()) {
                ItemStack mh = p.getInventory().getItemInMainHand();
                ItemStack oh = p.getInventory().getItemInOffHand();
                boolean holdingLight = plugin.isLightSource(mh.getType()) || plugin.isLightSource(oh.getType());
                if (holdingLight)
                    checkAndShowSpawnableBlocks(p);
            }
        }

        // Then, also check any online players close to the changed block (covers
        // players who may not have had highlights cached)
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player == null || !player.isOnline())
                continue;

            // only update players who are holding light-producing items
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            boolean holdingLight = plugin.isLightSource(mainHand.getType()) || plugin.isLightSource(offHand.getType());
            if (!holdingLight)
                continue;

            // if the changedBlock is within horizontal/vertical bounds of the player,
            // trigger a full check for that player
            if (isBlockNearPlayer(changedBlock, player)) {
                checkAndShowSpawnableBlocks(player);
            }
        }
    }

    private boolean isBlockNearPlayer(Block block, Player player) {
        int dx = block.getX() - player.getLocation().getBlockX();
        int dz = block.getZ() - player.getLocation().getBlockZ();
        int dy = block.getY() - player.getLocation().getBlockY();
        return Math.abs(dx) <= HORIZONTAL_RADIUS && Math.abs(dz) <= HORIZONTAL_RADIUS && dy >= Y_MIN && dy <= Y_MAX;
    }

    // Made public so event-triggered scans can call it directly
    public void checkAndShowSpawnableBlocks(Player player) {
        Map<String, Integer> currentHighlights = new HashMap<>();

        Block origin = player.getLocation().getBlock();
        boolean nightMode = plugin.isSimulateNightTime();

        for (int x = -HORIZONTAL_RADIUS; x <= HORIZONTAL_RADIUS; x++) {
            for (int z = -HORIZONTAL_RADIUS; z <= HORIZONTAL_RADIUS; z++) {
                for (int y = Y_MIN; y <= Y_MAX; y++) {
                    Block block = origin.getRelative(x, y, z);

                    // Check if spawnable at night (block light only, ignoring sky light)
                    boolean spawnableAtNight = SpawnUtils.isSpawnableAtNight(block);
                    // Check if currently spawnable (current light conditions)
                    boolean currentlySpawnable = SpawnUtils.isSpawnable(block);

                    // In night simulation mode, show blocks that WOULD be spawnable at night
                    // Otherwise, only show currently spawnable blocks
                    boolean shouldHighlight = nightMode ? spawnableAtNight : currentlySpawnable;

                    if (shouldHighlight) {
                        Block spawnPos = block.getRelative(0, 1, 0);
                        // Use block light for determining danger level (ignores sky light)
                        int blockLight = spawnPos.getLightFromBlocks();

                        String coordKey = makeKey(spawnPos);
                        currentHighlights.put(coordKey, blockLight);

                        Map<String, Integer> prev = previousHighlights.get(player.getUniqueId());
                        Integer prevLight = (prev == null) ? null : prev.get(coordKey);

                        // Spawn if newly highlighted or if light level changed since last run
                        if (prevLight == null || !prevLight.equals(blockLight)) {
                            spawnHighlightParticle(player, spawnPos, currentlySpawnable);
                        } else {
                            // Still spawn occasionally to keep particle visible while the player holds the
                            // item
                            // but we can do a lower-frequency refresh when nothing changed: spawn ~25% of
                            // the time
                            if (Math.random() < 0.25) {
                                spawnHighlightParticle(player, spawnPos, currentlySpawnable);
                            }
                        }
                    }
                }
            }
        }

        // Replace previous highlights with current for next comparison
        previousHighlights.put(player.getUniqueId(), currentHighlights);
    }

    private String makeKey(Block b) {
        return b.getWorld().getName() + ":" + b.getX() + "," + b.getY() + "," + b.getZ();
    }

    /**
     * Spawn highlight particle at the given location.
     * 
     * @param player             The player to show the particle to
     * @param block              The block to highlight
     * @param currentlySpawnable If true, mobs can spawn NOW (red). If false, they
     *                           can only spawn at night (yellow).
     */
    private void spawnHighlightParticle(Player player, Block block, boolean currentlySpawnable) {
        // Red = currently spawnable (dangerous now!)
        // Yellow = spawnable at night only (will be dangerous at night)
        Color particleColor = currentlySpawnable
                ? Color.fromRGB(255, 0, 0) // Red - danger now!
                : Color.fromRGB(255, 200, 0); // Yellow/Orange - danger at night

        // Spawn the particle centered at the spawn position (mob feet) so it's visually
        // over the spot
        player.spawnParticle(Particle.DUST, block.getLocation().add(0.5, 0.0, 0.5), 6,
                new Particle.DustOptions(particleColor, 0.9f));
    }
}
