package net.thanachot.canMobSpawnHere.render;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ParticleHighlighter {

    public void show(Player player, Block block, boolean currentlySpawnable) {
        Color particleColor = currentlySpawnable
                ? Color.fromRGB(255, 0, 0)
                : Color.fromRGB(255, 200, 0);

        player.spawnParticle(Particle.DUST, block.getLocation().add(0.5, 0.2, 0.5), 6,
                new Particle.DustOptions(particleColor, 0.9f));
    }
}
