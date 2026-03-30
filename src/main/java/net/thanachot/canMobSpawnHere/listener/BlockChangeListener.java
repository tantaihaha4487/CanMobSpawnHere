package net.thanachot.canMobSpawnHere.listener;

import net.thanachot.canMobSpawnHere.CanMobSpawnHere;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
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
        refreshAroundChangedBlock(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        refreshAroundChangedBlock(event);
    }

    private void refreshAroundChangedBlock(BlockEvent event) {
        plugin.getSpawnCheckService().refreshForBlock(event.getBlock());

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getSpawnCheckService().refreshForBlock(event.getBlock());
            }
        }.runTaskLater(plugin, 2L);
    }
}
