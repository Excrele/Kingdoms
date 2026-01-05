package com.excrele.kingdoms.model;

/**
 * Represents reputation between two kingdoms
 */
public class KingdomReputation {
    private final String kingdom1;
    private final String kingdom2;
    private double reputation; // -100 to 100
    private long lastUpdated;
    private long lastDecayTime;
    
    public KingdomReputation(String kingdom1, String kingdom2) {
        this.kingdom1 = kingdom1;
        this.kingdom2 = kingdom2;
        this.reputation = 0.0; // Neutral
        this.lastUpdated = System.currentTimeMillis() / 1000;
        this.lastDecayTime = System.currentTimeMillis() / 1000;
    }
    
    public String getKingdom1() {
        return kingdom1;
    }
    
    public String getKingdom2() {
        return kingdom2;
    }
    
    public double getReputation() {
        return reputation;
    }
    
    public void setReputation(double reputation) {
        this.reputation = Math.max(-100.0, Math.min(100.0, reputation)); // Clamp to -100 to 100
        this.lastUpdated = System.currentTimeMillis() / 1000;
    }
    
    public void addReputation(double amount) {
        setReputation(reputation + amount);
    }
    
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    public long getLastDecayTime() {
        return lastDecayTime;
    }
    
    public void setLastDecayTime(long lastDecayTime) {
        this.lastDecayTime = lastDecayTime;
    }
    
    /**
     * Get reputation category
     */
    public ReputationCategory getCategory() {
        if (reputation >= 75) return ReputationCategory.ALLIED;
        if (reputation >= 50) return ReputationCategory.FRIENDLY;
        if (reputation >= 25) return ReputationCategory.POSITIVE;
        if (reputation >= -25) return ReputationCategory.NEUTRAL;
        if (reputation >= -50) return ReputationCategory.NEGATIVE;
        if (reputation >= -75) return ReputationCategory.HOSTILE;
        return ReputationCategory.ENEMY;
    }
    
    /**
     * Calculate trade price modifier based on reputation
     */
    public double getTradePriceModifier() {
        // Better reputation = better prices (up to 20% discount)
        // Worse reputation = worse prices (up to 20% markup)
        return 1.0 - (reputation / 500.0); // -0.2 to +0.2 modifier
    }
    
    public enum ReputationCategory {
        ALLIED,     // 75-100
        FRIENDLY,   // 50-74
        POSITIVE,   // 25-49
        NEUTRAL,    // -24 to 24
        NEGATIVE,   // -49 to -25
        HOSTILE,    // -74 to -50
        ENEMY       // -100 to -75
    }
}

