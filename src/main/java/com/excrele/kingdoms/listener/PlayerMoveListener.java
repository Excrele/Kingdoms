package com.excrele.kingdoms.listener;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMoveListener implements Listener {
    private final Map<UUID, Chunk> lastChunk = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Chunk currentChunk = player.getLocation().getChunk();
        UUID playerId = player.getUniqueId();
        Chunk previousChunk = lastChunk.get(playerId);

        if (previousChunk == null || !currentChunk.equals(previousChunk)) {
            Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(currentChunk);
            if (kingdom != null) {
                Particle particle = kingdom.getKing().equals(player.getName()) ?
                        Particle.HAPPY_VILLAGER : Particle.SMOKE;
                displayChunkBorder(player, currentChunk, particle);
            }
            lastChunk.put(playerId, currentChunk);
        }
    }

    private void displayChunkBorder(Player player, Chunk chunk, Particle particle) {
        int minX = chunk.getX() << 4;
        int minZ = chunk.getZ() << 4;
        int maxX = minX + 16;
        int maxZ = minZ + 16;
        int y = (int) player.getLocation().getY() + 1;

        for (int x = minX; x <= maxX; x++) {
            spawnParticle(player, new Location(chunk.getWorld(), x, y, minZ), particle);
            spawnParticle(player, new Location(chunk.getWorld(), x, y, maxZ), particle);
        }
        for (int z = minZ; z <= maxZ; z++) {
            spawnParticle(player, new Location(chunk.getWorld(), minX, y, z), particle);
            spawnParticle(player, new Location(chunk.getWorld(), maxX, y, z), particle);
        }
    }

    private void spawnParticle(Player player, Location loc, Particle particle) {
        player.spawnParticle(particle, loc, 1, 0, 0, 0, 0);
    }
}