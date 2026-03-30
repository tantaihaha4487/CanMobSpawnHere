package net.thanachot.canMobSpawnHere.listener;

import net.thanachot.canMobSpawnHere.CanMobSpawnHere;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerStateListener implements Listener {

    private final CanMobSpawnHere plugin;

    public PlayerStateListener(CanMobSpawnHere plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getSpawnAbility().clearPlayer(event.getPlayer());
        plugin.getSpawnCheckService().clearPlayer(event.getPlayer().getUniqueId());
    }
}
