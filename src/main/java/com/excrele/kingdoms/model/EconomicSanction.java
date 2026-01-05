package com.excrele.kingdoms.model;

/**
 * Represents an economic sanction between kingdoms
 */
public class EconomicSanction {
    private final String sanctionId;
    private final String imposingKingdom;
    private final String targetKingdom;
    private final SanctionType type;
    private final double penaltyRate; // Percentage penalty on transactions
    private final long startTime;
    private final long expirationTime; // -1 for permanent
    private final String reason;
    
    public EconomicSanction(String sanctionId, String imposingKingdom, String targetKingdom, 
                           SanctionType type, double penaltyRate, long durationSeconds, String reason) {
        this.sanctionId = sanctionId;
        this.imposingKingdom = imposingKingdom;
        this.targetKingdom = targetKingdom;
        this.type = type;
        this.penaltyRate = penaltyRate;
        this.startTime = System.currentTimeMillis() / 1000;
        this.expirationTime = durationSeconds > 0 ? startTime + durationSeconds : -1;
        this.reason = reason;
    }
    
    public String getSanctionId() {
        return sanctionId;
    }
    
    public String getImposingKingdom() {
        return imposingKingdom;
    }
    
    public String getTargetKingdom() {
        return targetKingdom;
    }
    
    public SanctionType getType() {
        return type;
    }
    
    public double getPenaltyRate() {
        return penaltyRate;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getExpirationTime() {
        return expirationTime;
    }
    
    public boolean isPermanent() {
        return expirationTime == -1;
    }
    
    public boolean isExpired() {
        if (isPermanent()) return false;
        return System.currentTimeMillis() / 1000 > expirationTime;
    }
    
    public String getReason() {
        return reason;
    }
    
    /**
     * Calculate penalty amount for a transaction
     */
    public double calculatePenalty(double transactionAmount) {
        return transactionAmount * (penaltyRate / 100.0);
    }
    
    public enum SanctionType {
        TRADE_EMBARGO,      // Block all trade
        TAX_PENALTY,        // Increase tax rate
        TRANSACTION_FEE,    // Add fee to transactions
        RESOURCE_BLOCK      // Block resource trading
    }
}

