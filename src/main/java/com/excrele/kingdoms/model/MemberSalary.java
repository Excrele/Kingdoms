package com.excrele.kingdoms.model;

/**
 * Represents a salary payment for a kingdom member
 */
public class MemberSalary {
    private final String player;
    private final String kingdomName;
    private final double amount;
    private final long paymentInterval; // Time between payments in seconds
    private long lastPaymentTime;
    private boolean isActive;
    
    public MemberSalary(String player, String kingdomName, double amount, long paymentInterval) {
        this.player = player;
        this.kingdomName = kingdomName;
        this.amount = amount;
        this.paymentInterval = paymentInterval;
        this.lastPaymentTime = System.currentTimeMillis() / 1000;
        this.isActive = true;
    }
    
    public String getPlayer() {
        return player;
    }
    
    public String getKingdomName() {
        return kingdomName;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public long getPaymentInterval() {
        return paymentInterval;
    }
    
    public long getLastPaymentTime() {
        return lastPaymentTime;
    }
    
    public void setLastPaymentTime(long lastPaymentTime) {
        this.lastPaymentTime = lastPaymentTime;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    /**
     * Check if payment is due
     */
    public boolean isPaymentDue() {
        if (!isActive) return false;
        long currentTime = System.currentTimeMillis() / 1000;
        return (currentTime - lastPaymentTime) >= paymentInterval;
    }
}

