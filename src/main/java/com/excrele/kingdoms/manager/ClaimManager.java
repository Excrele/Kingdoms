package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class ClaimManager {
    private KingdomManager kingdomManager;
    private WorldManager worldManager;
    private KingdomsPlugin plugin;

    public ClaimManager(KingdomManager kingdomManager) {
        this.kingdomManager = kingdomManager;
        this.worldManager = null; // Will be set after WorldManager is created
        this.plugin = KingdomsPlugin.getInstance(); // Get plugin instance
    }
    
    public void setWorldManager(WorldManager worldManager) {
        this.worldManager = worldManager;
    }
    
    public void setPlugin(KingdomsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean claimChunk(Kingdom kingdom, Chunk chunk) {
        if (kingdom == null) {
            logClaimFailure("null", "unknown chunk", "Kingdom is null");
            return false;
        }
        if (chunk == null) {
            logClaimFailure(kingdom.getName(), "null chunk", "Chunk is null");
            return false;
        }
        
        World world = chunk.getWorld();
        if (world == null) {
            logClaimFailure(kingdom.getName(), String.format("Chunk: (%d, %d)", chunk.getX(), chunk.getZ()), 
                "Chunk world is null");
            return false;
        }
        
        String chunkInfo = String.format("World: %s, Chunk: (%d, %d)", 
            world.getName(), chunk.getX(), chunk.getZ());
        String kingdomInfo = kingdom.getName();
        
        if (isChunkClaimed(chunk)) {
            Kingdom existingOwner = kingdomManager.getKingdomByChunk(chunk);
            String ownerName = existingOwner != null ? existingOwner.getName() : "unknown";
            logClaimFailure(kingdomInfo, chunkInfo, 
                String.format("Chunk is already claimed by kingdom: %s", ownerName));
            return false;
        }
        
        // Check world-specific rules (applies to all claims, including first)
        if (worldManager != null) {
            if (!worldManager.isClaimingEnabled(world)) {
                logClaimFailure(kingdomInfo, chunkInfo, 
                    String.format("Claiming is disabled in world: %s", world.getName()));
                return false; // Claiming disabled in this world
            }
            if (!worldManager.canClaimInWorld(kingdom, world)) {
                int currentClaims = kingdom.getCurrentClaimChunks();
                int maxClaims = worldManager.getMaxClaimsForWorld(kingdom, world);
                logClaimFailure(kingdomInfo, chunkInfo, 
                    String.format("Exceeds world-specific claim limit. Current: %d, Max: %d", 
                        currentClaims, maxClaims));
                return false; // Exceeds world-specific claim limit
            }
        } else {
            // Fallback to default check
            if (kingdom.getCurrentClaimChunks() >= kingdom.getMaxClaimChunks()) {
                logClaimFailure(kingdomInfo, chunkInfo, 
                    String.format("Exceeds default claim limit. Current: %d, Max: %d", 
                        kingdom.getCurrentClaimChunks(), kingdom.getMaxClaimChunks()));
                return false; // Exceeds claim limit
            }
        }

        // Check buffer zone (applies to all claims, including first)
        int bufferZone = 5; // Default
        if (worldManager != null) {
            bufferZone = worldManager.getBufferZoneForWorld(world);
        }
        
        for (Kingdom otherK : kingdomManager.getKingdoms().values()) {
            if (otherK != kingdom) {
                for (List<Chunk> claim : otherK.getClaims()) {
                    // Only check buffer zone for chunks in the same world
                    for (Chunk otherChunk : claim) {
                        if (otherChunk.getWorld().equals(world)) {
                            int distance = minDistance(chunk, claim);
                            if (distance < (bufferZone + 1)) {
                                logClaimFailure(kingdomInfo, chunkInfo, 
                                    String.format("Too close to kingdom '%s'. Distance: %d, Required: %d (buffer zone: %d)", 
                                        otherK.getName(), distance, bufferZone + 1, bufferZone));
                                return false; // Too close to another kingdom
                            }
                        }
                    }
                }
            }
        }

        // Check if this is the first claim for this kingdom
        // First claims don't need adjacency checks, but buffer zone is still enforced
        List<List<Chunk>> claims = kingdom.getClaims();
        boolean isFirstClaim = claims.isEmpty() || kingdom.getCurrentClaimChunks() == 0;
        
        if (isFirstClaim) {
            // First claim - no adjacency required (buffer zone already checked above)
            List<Chunk> mainClaim = new ArrayList<>();
            mainClaim.add(chunk);
            
            // Call KingdomClaimEvent
            org.bukkit.entity.Player claimer = null;
            if (plugin != null && plugin.getServer() != null) {
                // Try to find the player who initiated the claim
                for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
                    if (p.getLocation().getChunk().equals(chunk)) {
                        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(p.getName());
                        if (kingdomName != null && kingdomName.equals(kingdom.getName())) {
                            claimer = p;
                            break;
                        }
                    }
                }
            }
            
            com.excrele.kingdoms.api.event.KingdomClaimEvent claimEvent = 
                new com.excrele.kingdoms.api.event.KingdomClaimEvent(kingdom, chunk, claimer);
            if (plugin != null && plugin.getServer() != null) {
                plugin.getServer().getPluginManager().callEvent(claimEvent);
                if (claimEvent.isCancelled()) {
                    logClaimFailure(kingdomInfo, chunkInfo, "Claim was cancelled by another plugin");
                    return false;
                }
            }
            
            claims.add(mainClaim);
            kingdomManager.claimChunk(kingdom, chunk, mainClaim);
            if (plugin != null) {
                plugin.getLogger().info(String.format("[ClaimManager] Successfully claimed chunk %s for kingdom '%s' (first claim)", 
                    chunkInfo, kingdomInfo));
            }
            return true;
        }

        // Check if chunk is adjacent to any existing claim
        for (List<Chunk> claim : claims) {
            if (isAdjacent(chunk, claim)) {
                // Call KingdomClaimEvent
                org.bukkit.entity.Player claimer = null;
                if (plugin != null && plugin.getServer() != null) {
                    // Try to find the player who initiated the claim
                    for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
                        if (p.getLocation().getChunk().equals(chunk)) {
                            String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(p.getName());
                            if (kingdomName != null && kingdomName.equals(kingdom.getName())) {
                                claimer = p;
                                break;
                            }
                        }
                    }
                }
                
                com.excrele.kingdoms.api.event.KingdomClaimEvent claimEvent = 
                    new com.excrele.kingdoms.api.event.KingdomClaimEvent(kingdom, chunk, claimer);
                if (plugin != null && plugin.getServer() != null) {
                    plugin.getServer().getPluginManager().callEvent(claimEvent);
                    if (claimEvent.isCancelled()) {
                        logClaimFailure(kingdomInfo, chunkInfo, "Claim was cancelled by another plugin");
                        return false;
                    }
                }
                
                claim.add(chunk);
                kingdomManager.claimChunk(kingdom, chunk, claim);
                if (plugin != null) {
                    plugin.getLogger().info(String.format("[ClaimManager] Successfully claimed chunk %s for kingdom '%s' (adjacent claim)", 
                        chunkInfo, kingdomInfo));
                }
                return true;
            }
        }

        // Chunk is not adjacent to any existing claim - reject
        logClaimFailure(kingdomInfo, chunkInfo, 
            "Chunk is not adjacent to any existing claims. New chunks must be adjacent to existing claims.");
        return false;
    }
    
    private void logClaimFailure(String kingdomName, String chunkInfo, String reason) {
        if (plugin != null) {
            plugin.getLogger().warning(String.format(
                "[ClaimManager] Failed to claim chunk for kingdom '%s' - %s. Reason: %s", 
                kingdomName, chunkInfo, reason));
        }
    }

    public boolean unclaimChunk(Kingdom kingdom, Chunk chunk) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        if (!kingdomManager.getClaimedChunks().containsKey(key) || kingdomManager.getKingdomByChunk(chunk) != kingdom) {
            return false;
        }
        
        // Call KingdomUnclaimEvent
        org.bukkit.entity.Player unclaimer = null;
        if (plugin != null && plugin.getServer() != null) {
            // Try to find the player who initiated the unclaim
            for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getLocation().getChunk().equals(chunk)) {
                    String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(p.getName());
                    if (kingdomName != null && kingdomName.equals(kingdom.getName())) {
                        unclaimer = p;
                        break;
                    }
                }
            }
        }
        
        com.excrele.kingdoms.api.event.KingdomUnclaimEvent unclaimEvent = 
            new com.excrele.kingdoms.api.event.KingdomUnclaimEvent(kingdom, chunk, unclaimer);
        if (plugin != null && plugin.getServer() != null) {
            plugin.getServer().getPluginManager().callEvent(unclaimEvent);
        }
        
        for (List<Chunk> claim : kingdom.getClaims()) {
            if (claim.contains(chunk)) {
                claim.remove(chunk);
                if (claim.isEmpty()) kingdom.getClaims().remove(claim);
                kingdomManager.unclaimChunk(chunk);
                return true;
            }
        }
        return false;
    }

    private int chebyshevDistance(Chunk c1, Chunk c2) {
        return Math.max(Math.abs(c1.getX() - c2.getX()), Math.abs(c1.getZ() - c2.getZ()));
    }

    private int minDistance(Chunk C, List<Chunk> claim) {
        int minDist = Integer.MAX_VALUE;
        for (Chunk chunk : claim) {
            int dist = chebyshevDistance(C, chunk);
            if (dist < minDist) minDist = dist;
        }
        return minDist;
    }

    private boolean isAdjacent(Chunk C, List<Chunk> claim) {
        for (Chunk chunk : claim) {
            if (chebyshevDistance(C, chunk) == 1) return true;
        }
        return false;
    }

    private boolean isChunkClaimed(Chunk C) {
        String key = C.getWorld().getName() + ":" + C.getX() + ":" + C.getZ();
        return kingdomManager.getClaimedChunks().containsKey(key);
    }

    /**
     * Claim chunks in a radius around a center chunk
     * Only claims chunks that are adjacent to existing claims (or the first chunk if no claims exist)
     * @param kingdom The kingdom claiming the chunks
     * @param centerChunk The center chunk
     * @param radius The radius in chunks (Chebyshev distance)
     * @return List of successfully claimed chunks
     */
    public java.util.List<Chunk> claimChunksInRadius(Kingdom kingdom, Chunk centerChunk, int radius) {
        java.util.List<Chunk> claimedChunks = new ArrayList<>();
        
        // Limit radius to prevent abuse
        if (radius < 1) radius = 1;
        if (radius > 10) radius = 10; // Max radius of 10 chunks
        
        // Get all chunks in radius
        java.util.List<Chunk> chunksInRadius = new ArrayList<>();
        for (int x = centerChunk.getX() - radius; x <= centerChunk.getX() + radius; x++) {
            for (int z = centerChunk.getZ() - radius; z <= centerChunk.getZ() + radius; z++) {
                Chunk chunk = centerChunk.getWorld().getChunkAt(x, z);
                int distance = chebyshevDistance(centerChunk, chunk);
                if (distance <= radius) {
                    chunksInRadius.add(chunk);
                }
            }
        }
        
        // Sort chunks by distance from center (closest first)
        chunksInRadius.sort((c1, c2) -> {
            int dist1 = chebyshevDistance(centerChunk, c1);
            int dist2 = chebyshevDistance(centerChunk, c2);
            return Integer.compare(dist1, dist2);
        });
        
        // Try to claim each chunk
        for (Chunk chunk : chunksInRadius) {
            // Check if we've hit the claim limit
            if (kingdom.getCurrentClaimChunks() >= kingdom.getMaxClaimChunks()) {
                break;
            }
            
            // Try to claim the chunk
            if (claimChunk(kingdom, chunk)) {
                claimedChunks.add(chunk);
            }
        }
        
        return claimedChunks;
    }
}