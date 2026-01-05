package com.excrele.kingdoms.model;

/**
 * Represents war reparations to be paid after a war
 */
public class WarReparation {
    private final String reparationId;
    private final String warId;
    private final String payingKingdom;
    private final String receivingKingdom;
    private final double amount;
    private final long dueDate;
    private double paidAmount;
    private boolean isPaidOff;
    
    public WarReparation(String reparationId, String warId, String payingKingdom, 
                        String receivingKingdom, double amount, long dueDate) {
        this.reparationId = reparationId;
        this.warId = warId;
        this.payingKingdom = payingKingdom;
        this.receivingKingdom = receivingKingdom;
        this.amount = amount;
        this.dueDate = dueDate;
        this.paidAmount = 0;
        this.isPaidOff = false;
    }
    
    public String getReparationId() {
        return reparationId;
    }
    
    public String getWarId() {
        return warId;
    }
    
    public String getPayingKingdom() {
        return payingKingdom;
    }
    
    public String getReceivingKingdom() {
        return receivingKingdom;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public long getDueDate() {
        return dueDate;
    }
    
    public double getPaidAmount() {
        return paidAmount;
    }
    
    public void addPayment(double amount) {
        paidAmount += amount;
        if (paidAmount >= this.amount) {
            isPaidOff = true;
        }
    }
    
    public double getRemainingAmount() {
        return Math.max(0, amount - paidAmount);
    }
    
    public boolean isPaidOff() {
        return isPaidOff;
    }
    
    public boolean isOverdue() {
        return System.currentTimeMillis() / 1000 > dueDate && !isPaidOff;
    }
}

