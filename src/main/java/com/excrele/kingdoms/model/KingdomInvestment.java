package com.excrele.kingdoms.model;

/**
 * Represents an investment made by a kingdom
 */
public class KingdomInvestment {
    private final String investmentId;
    private final String kingdomName;
    private final InvestmentType type;
    private final double principal; // Initial investment
    private final double expectedReturnRate; // Expected annual return
    private final long startTime;
    private final long maturityDate; // When investment matures
    private double currentValue;
    private boolean isMatured;
    
    public KingdomInvestment(String investmentId, String kingdomName, InvestmentType type, 
                            double principal, double expectedReturnRate, long durationSeconds) {
        this.investmentId = investmentId;
        this.kingdomName = kingdomName;
        this.type = type;
        this.principal = principal;
        this.expectedReturnRate = expectedReturnRate;
        this.startTime = System.currentTimeMillis() / 1000;
        this.maturityDate = startTime + durationSeconds;
        this.currentValue = principal;
        this.isMatured = false;
    }
    
    public String getInvestmentId() {
        return investmentId;
    }
    
    public String getKingdomName() {
        return kingdomName;
    }
    
    public InvestmentType getType() {
        return type;
    }
    
    public double getPrincipal() {
        return principal;
    }
    
    public double getExpectedReturnRate() {
        return expectedReturnRate;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getMaturityDate() {
        return maturityDate;
    }
    
    public double getCurrentValue() {
        return currentValue;
    }
    
    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }
    
    public boolean isMatured() {
        return isMatured;
    }
    
    public void setMatured(boolean matured) {
        isMatured = matured;
    }
    
    /**
     * Calculate current value with returns
     */
    public double calculateCurrentValue() {
        if (isMatured) {
            return currentValue;
        }
        
        long currentTime = System.currentTimeMillis() / 1000;
        long timeElapsed = currentTime - startTime;
        double timeInYears = (double) timeElapsed / (365.0 * 24.0 * 3600.0);
        
        // Simple interest calculation
        double returns = principal * expectedReturnRate * timeInYears;
        return principal + returns;
    }
    
    /**
     * Check if investment has matured
     */
    public boolean checkMaturity() {
        if (isMatured) return true;
        
        long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime >= maturityDate) {
            isMatured = true;
            currentValue = calculateCurrentValue();
            return true;
        }
        return false;
    }
    
    public enum InvestmentType {
        BANK_DEPOSIT,       // Deposit in bank with interest
        INFRASTRUCTURE,     // Invest in kingdom infrastructure
        TRADE_ROUTE,        // Invest in trade routes
        RESEARCH           // Invest in research/technology
    }
}

