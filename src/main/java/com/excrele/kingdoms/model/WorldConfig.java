package com.excrele.kingdoms.model;

/**
 * Configuration for world-specific claiming rules
 */
public class WorldConfig {
    private String worldName;
    private int maxClaims; // Max claims per kingdom in this world (-1 = use default)
    private double claimCost; // Cost to claim in this world (-1 = use default)
    private boolean claimingEnabled; // Whether claiming is allowed in this world
    private int bufferZone; // Buffer zone between kingdoms in chunks
    
    public WorldConfig(String worldName) {
        this.worldName = worldName;
        this.maxClaims = -1; // Use default
        this.claimCost = -1; // Use default
        this.claimingEnabled = true;
        this.bufferZone = 5; // Default 5-chunk buffer
    }
    
    public String getWorldName() { return worldName; }
    public int getMaxClaims() { return maxClaims; }
    public void setMaxClaims(int maxClaims) { this.maxClaims = maxClaims; }
    public double getClaimCost() { return claimCost; }
    public void setClaimCost(double claimCost) { this.claimCost = claimCost; }
    public boolean isClaimingEnabled() { return claimingEnabled; }
    public void setClaimingEnabled(boolean claimingEnabled) { this.claimingEnabled = claimingEnabled; }
    public int getBufferZone() { return bufferZone; }
    public void setBufferZone(int bufferZone) { this.bufferZone = bufferZone; }
}

