package net.thanachot.canMobSpawnHere.task;

import net.thanachot.canMobSpawnHere.CanMobSpawnHere;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnCheckTask extends BukkitRunnable {
    private final CanMobSpawnHere plugin;

    public SpawnCheckTask(CanMobSpawnHere plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getSpawnCheckService().tick();
    }
}
