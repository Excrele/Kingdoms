package com.excrele.kingdoms.model;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;

/**
 * Tracks analytics for a claim/chunk
 */
public class ClaimAnalytics {
    private Chunk chunk;
    private String kingdomName;
    private long claimedAt;
    private long lastActivity;
    private int playerVisits; // Number of unique player visits
    private int blockInteractions; // Blocks broken/placed
    private int entityInteractions; // Entities killed/spawned
    private double estimatedValue;
    private Map<String, Integer> activityByPlayer; // Player -> visit count
    
    public ClaimAnalytics(Chunk chunk, String kingdomName) {
        this.chunk = chunk;
        this.kingdomName = kingdomName;
        this.claimedAt = System.currentTimeMillis() / 1000;
        this.lastActivity = this.claimedAt;
        this.playerVisits = 0;
        this.blockInteractions = 0;
        this.entityInteractions = 0;
        this.estimatedValue = 0.0;
        this.activityByPlayer = new HashMap<>();
    }
    
    public Chunk getChunk() { return chunk; }
    public String getKingdomName() { return kingdomName; }
    public long getClaimedAt() { return claimedAt; }
    public void setClaimedAt(long claimedAt) { this.claimedAt = claimedAt; }
    public long getLastActivity() { return lastActivity; }
    public void updateActivity() { this.lastActivity = System.currentTimeMillis() / 1000; }
    public int getPlayerVisits() { return playerVisits; }
    public void incrementVisits(String playerName) {
        playerVisits++;
        activityByPlayer.put(playerName, activityByPlayer.getOrDefault(playerName, 0) + 1);
        updateActivity();
    }
    public int getBlockInteractions() { return blockInteractions; }
    public void incrementBlockInteractions() { 
        blockInteractions++; 
        updateActivity();
    }
    public int getEntityInteractions() { return entityInteractions; }
    public void incrementEntityInteractions() { 
        entityInteractions++; 
        updateActivity();
    }
    public double getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(double estimatedValue) { this.estimatedValue = estimatedValue; }
    public Map<String, Integer> getActivityByPlayer() { return activityByPlayer; }
    
    public double getActivityScore() {
        // Calculate activity score based on various factors
        long daysSinceClaim = (System.currentTimeMillis() / 1000 - claimedAt) / 86400;
        if (daysSinceClaim == 0) daysSinceClaim = 1;
        return (playerVisits * 0.3 + blockInteractions * 0.4 + entityInteractions * 0.3) / daysSinceClaim;
    }
}

