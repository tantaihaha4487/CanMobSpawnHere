package net.thanachot.canMobSpawnHere;

import net.thanachot.canMobSpawnHere.listener.BlockChangeListener;
import net.thanachot.canMobSpawnHere.task.SpawnCheckTask;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class CanMobSpawnHere extends JavaPlugin {

    private final Set<Material> lightSources = new HashSet<>();

    // Keep a reference to the scheduled task so event listeners can trigger
    // immediate scans
    private SpawnCheckTask spawnTask;

    // When true, simulate night time conditions to show where mobs WOULD spawn at
    // night
    private boolean simulateNightTime = true;

    @Override
    public void onEnable() {
        // Plugin startup logic
        populateLightSources();
        this.spawnTask = new SpawnCheckTask(this);
        this.spawnTask.runTaskTimer(this, 0L, 4L);

        // Register block change listener to update highlights immediately when light
        // sources are placed/removed
        getServer().getPluginManager().registerEvents(new BlockChangeListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public SpawnCheckTask getSpawnTask() {
        return spawnTask;
    }

    private void populateLightSources() {
        lightSources.addAll(Arrays.asList(
                Material.BEACON, Material.CAMPFIRE, Material.LAVA_CAULDRON, Material.CONDUIT,
                Material.COPPER_BULB, Material.END_GATEWAY, Material.END_PORTAL, Material.FIRE,
                Material.OCHRE_FROGLIGHT, Material.PEARLESCENT_FROGLIGHT, Material.VERDANT_FROGLIGHT,
                Material.GLOWSTONE, Material.JACK_O_LANTERN, Material.LANTERN,
                Material.LAVA, Material.REDSTONE_LAMP, Material.RESPAWN_ANCHOR, Material.SEA_LANTERN,
                Material.SHROOMLIGHT, Material.TORCH, Material.FURNACE, Material.BLAST_FURNACE,
                Material.SMOKER, Material.CANDLE, Material.NETHER_PORTAL, Material.CRYING_OBSIDIAN,
                Material.SOUL_CAMPFIRE, Material.SOUL_FIRE, Material.SOUL_LANTERN, Material.SOUL_TORCH,
                Material.REDSTONE_ORE, Material.ENCHANTING_TABLE, Material.ENDER_CHEST, Material.GLOW_LICHEN,
                Material.REDSTONE_TORCH, Material.SEA_PICKLE, Material.AMETHYST_CLUSTER,
                Material.MAGMA_BLOCK, Material.BREWING_STAND, Material.DRAGON_EGG, Material.END_PORTAL_FRAME,
                Material.LIGHT));
    }

    public boolean isSimulateNightTime() {
        return simulateNightTime;
    }

    public void setSimulateNightTime(boolean simulateNightTime) {
        this.simulateNightTime = simulateNightTime;
    }

    public boolean isLightSource(Material material) {
        return lightSources.contains(material);
    }
}
