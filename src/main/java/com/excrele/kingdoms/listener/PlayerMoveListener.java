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
            KingdomsPlugin plugin = KingdomsPlugin.getInstance();
            Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(currentChunk);
            
            // Auto-claim check
            if (kingdom == null && plugin.getAdvancedFeaturesManager() != null) {
                if (plugin.getAdvancedFeaturesManager().isAutoClaimEnabled(player) && 
                    plugin.getAdvancedFeaturesManager().canAutoClaim(player)) {
                    String playerKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
                    if (playerKingdomName != null) {
                        Kingdom playerKingdom = plugin.getKingdomManager().getKingdom(playerKingdomName);
                        if (playerKingdom != null && playerKingdom.hasPermission(player.getName(), "claim")) {
                            if (plugin.getClaimManager().claimChunk(playerKingdom, currentChunk)) {
                                plugin.getAdvancedFeaturesManager().recordAutoClaim(player);
                                player.sendMessage("§aAuto-claimed chunk!");
                            }
                        }
                    }
                }
            }
            
            if (kingdom != null) {
                String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
                
                // Use theme particles if available
                Particle particle;
                if (plugin.getThemeManager() != null) {
                    com.excrele.kingdoms.model.KingdomTheme theme = 
                        plugin.getThemeManager().getTheme(kingdom.getName());
                    if (playerKingdom != null && playerKingdom.equals(kingdom.getName())) {
                        particle = theme.getPrimaryParticle();
                    } else {
                        particle = theme.getSecondaryParticle();
                    }
                } else {
                    // Fallback to default
                    particle = (playerKingdom != null && playerKingdom.equals(kingdom.getName())) ?
                            Particle.HAPPY_VILLAGER : Particle.SMOKE;
                }
                
                displayChunkBorder(player, currentChunk, particle);
                
                // Preload nearby chunks for optimization
                if (plugin.getChunkOptimizer() != null) {
                    plugin.getChunkOptimizer().preloadChunksAroundPlayer(player, 2);
                }
                
                // Record claim visit for statistics
                if (plugin.getStatisticsManager() != null) {
                    plugin.getStatisticsManager().recordClaimVisit(currentChunk, player.getName());
                }
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