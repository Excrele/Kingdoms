package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class ClaimManager {
    private KingdomManager kingdomManager;
    private WorldManager worldManager;

    public ClaimManager(KingdomManager kingdomManager) {
        this.kingdomManager = kingdomManager;
        this.worldManager = null; // Will be set after WorldManager is created
    }
    
    public void setWorldManager(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    public boolean claimChunk(Kingdom kingdom, Chunk chunk) {
        if (isChunkClaimed(chunk)) return false;
        
        World world = chunk.getWorld();
        
        // Check world-specific rules
        if (worldManager != null) {
            if (!worldManager.isClaimingEnabled(world)) {
                return false; // Claiming disabled in this world
            }
            if (!worldManager.canClaimInWorld(kingdom, world)) {
                return false; // Exceeds world-specific claim limit
            }
        } else {
            // Fallback to default check
            if (kingdom.getCurrentClaimChunks() >= kingdom.getMaxClaimChunks()) {
                return false; // Exceeds claim limit
            }
        }

        // Check buffer zone (world-specific if available)
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
                            if (minDistance(chunk, claim) < (bufferZone + 1)) {
                                return false; // Too close to another kingdom
                            }
                        }
                    }
                }
            }
        }

        List<List<Chunk>> claims = kingdom.getClaims();
        if (claims.isEmpty()) {
            // First claim - no adjacency required
            List<Chunk> mainClaim = new ArrayList<>();
            mainClaim.add(chunk);
            claims.add(mainClaim);
            kingdomManager.claimChunk(kingdom, chunk, mainClaim);
            return true;
        }

        // Check if chunk is adjacent to any existing claim
        for (List<Chunk> claim : claims) {
            if (isAdjacent(chunk, claim)) {
                claim.add(chunk);
                kingdomManager.claimChunk(kingdom, chunk, claim);
                return true;
            }
        }

        // Chunk is not adjacent to any existing claim - reject
        return false;
    }

    public boolean unclaimChunk(Kingdom kingdom, Chunk chunk) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        if (!kingdomManager.getClaimedChunks().containsKey(key) || kingdomManager.getKingdomByChunk(chunk) != kingdom) {
            return false;
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