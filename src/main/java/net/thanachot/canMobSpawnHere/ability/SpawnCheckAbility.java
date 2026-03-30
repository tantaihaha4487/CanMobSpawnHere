package net.thanachot.canMobSpawnHere.ability;

import net.kyori.adventure.text.format.NamedTextColor;
import net.thanachot.shiroverse.api.ability.ShiftAbility;
import net.thanachot.shiroverse.api.text.ActionbarMessage;
import net.thanachot.canMobSpawnHere.CanMobSpawnHere;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpawnCheckAbility extends ShiftAbility {

    private final CanMobSpawnHere plugin;
    private final Set<UUID> activePlayers = new HashSet<>();

    public SpawnCheckAbility(CanMobSpawnHere plugin) {
        super("spawn_check", item -> plugin.isLightSource(item.getType()));
        this.plugin = plugin;
    }

    @Override
    public void onActivate(@NotNull Player player, @NotNull ItemStack item) {
        activePlayers.add(player.getUniqueId());
        player.sendActionBar(ActionbarMessage.getAlert("Spawn Check Activated!", NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);

        plugin.getSpawnCheckService().refreshPlayer(player);
    }

    @Override
    public void onDeactivate(@NotNull Player player) {
        // Allow swapping main hand item if off-hand still holds a light source
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        if (plugin.isLightSource(main.getType()) || plugin.isLightSource(off.getType())) {
            return;
        }

        activePlayers.remove(player.getUniqueId());
        player.sendActionBar(ActionbarMessage.getAlert("Spawn Check Deactivated!", NamedTextColor.RED));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
    }

    @Override
    public boolean isActive(@NotNull Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    public void clearPlayer(@NotNull Player player) {
        activePlayers.remove(player.getUniqueId());
    }
}
