package net.thanachot.canMobSpawnHere.listener;

import net.thanachot.canMobSpawnHere.CanMobSpawnHere;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockChangeListener implements Listener {
    private final CanMobSpawnHere plugin;

    public BlockChangeListener(CanMobSpawnHere plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getSpawnTask() != null)
            plugin.getSpawnTask().scanAndUpdateForBlock(event.getBlock());

        // schedule a delayed rescan to allow lighting propagation to finish
        if (plugin.getSpawnTask() != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getSpawnTask().scanAndUpdateForBlock(event.getBlock());
                }
            }.runTaskLater(plugin, 2L);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getSpawnTask() != null)
            plugin.getSpawnTask().scanAndUpdateForBlock(event.getBlock());

        // schedule a delayed rescan to allow lighting propagation to finish
        if (plugin.getSpawnTask() != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getSpawnTask().scanAndUpdateForBlock(event.getBlock());
                }
            }.runTaskLater(plugin, 2L);
        }
    }
}
