package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;

public class ClaimManager {
    private KingdomManager kingdomManager;

    public ClaimManager(KingdomManager kingdomManager) {
        this.kingdomManager = kingdomManager;
    }

    public boolean claimChunk(Kingdom kingdom, Chunk chunk) {
        if (isChunkClaimed(chunk)) return false;
        if (kingdom.getCurrentClaimChunks() >= kingdom.getMaxClaimChunks()) {
            return false; // Exceeds claim limit
        }

        for (Kingdom otherK : kingdomManager.getKingdoms().values()) {
            if (otherK != kingdom) {
                for (List<Chunk> claim : otherK.getClaims()) {
                    if (minDistance(chunk, claim) < 6) return false; // 5-chunk buffer
                }
            }
        }

        List<List<Chunk>> claims = kingdom.getClaims();
        if (claims.isEmpty()) {
            List<Chunk> mainClaim = new ArrayList<>();
            mainClaim.add(chunk);
            claims.add(mainClaim);
            kingdomManager.claimChunk(kingdom, chunk, mainClaim);
            return true;
        }

        for (List<Chunk> claim : claims) {
            if (isAdjacent(chunk, claim)) {
                claim.add(chunk);
                kingdomManager.claimChunk(kingdom, chunk, claim);
                return true;
            }
        }

        List<Chunk> mainClaim = claims.get(0);
        if (minDistance(chunk, mainClaim) >= 11) { // Outpost 10+ chunks away
            List<Chunk> newClaim = new ArrayList<>();
            newClaim.add(chunk);
            claims.add(newClaim);
            kingdomManager.claimChunk(kingdom, chunk, newClaim);
            return true;
        }
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
}