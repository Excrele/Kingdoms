package com.excrele.kingdoms.model;

/**
 * Configuration for world-specific claiming rules and settings
 */
public class WorldConfig {
    private String worldName;
    private int maxClaims; // Max claims per kingdom in this world (-1 = use default)
    private double claimCost; // Cost to claim in this world (-1 = use default)
    private boolean claimingEnabled; // Whether claiming is allowed in this world
    private int bufferZone; // Buffer zone between kingdoms in chunks
    
    // Economy settings
    private boolean economyEnabled; // Whether economy features are enabled in this world
    private double taxRate; // Tax rate for this world (-1 = use default)
    private double unclaimRefund; // Refund when unclaiming (-1 = use default)
    
    // Teleportation settings
    private boolean crossWorldTeleportEnabled; // Whether cross-world teleportation is allowed
    private boolean allowTeleportFrom; // Whether players can teleport FROM this world
    private boolean allowTeleportTo; // Whether players can teleport TO this world
    
    // Leaderboard settings
    private boolean separateLeaderboards; // Whether this world has separate leaderboards
    
    public WorldConfig(String worldName) {
        this.worldName = worldName;
        this.maxClaims = -1; // Use default
        this.claimCost = -1; // Use default
        this.claimingEnabled = true;
        this.bufferZone = 5; // Default 5-chunk buffer
        
        // Default economy settings
        this.economyEnabled = true;
        this.taxRate = -1; // Use default
        this.unclaimRefund = -1; // Use default
        
        // Default teleportation settings
        this.crossWorldTeleportEnabled = true;
        this.allowTeleportFrom = true;
        this.allowTeleportTo = true;
        
        // Default leaderboard settings
        this.separateLeaderboards = false;
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
    
    // Economy getters/setters
    public boolean isEconomyEnabled() { return economyEnabled; }
    public void setEconomyEnabled(boolean economyEnabled) { this.economyEnabled = economyEnabled; }
    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }
    public double getUnclaimRefund() { return unclaimRefund; }
    public void setUnclaimRefund(double unclaimRefund) { this.unclaimRefund = unclaimRefund; }
    
    // Teleportation getters/setters
    public boolean isCrossWorldTeleportEnabled() { return crossWorldTeleportEnabled; }
    public void setCrossWorldTeleportEnabled(boolean crossWorldTeleportEnabled) { this.crossWorldTeleportEnabled = crossWorldTeleportEnabled; }
    public boolean isAllowTeleportFrom() { return allowTeleportFrom; }
    public void setAllowTeleportFrom(boolean allowTeleportFrom) { this.allowTeleportFrom = allowTeleportFrom; }
    public boolean isAllowTeleportTo() { return allowTeleportTo; }
    public void setAllowTeleportTo(boolean allowTeleportTo) { this.allowTeleportTo = allowTeleportTo; }
    
    // Leaderboard getters/setters
    public boolean isSeparateLeaderboards() { return separateLeaderboards; }
    public void setSeparateLeaderboards(boolean separateLeaderboards) { this.separateLeaderboards = separateLeaderboards; }
}

