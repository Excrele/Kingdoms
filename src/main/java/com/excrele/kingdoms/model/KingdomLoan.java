package com.excrele.kingdoms.model;

/**
 * Represents a loan taken by a kingdom
 */
public class KingdomLoan {
    private final String loanId;
    private final String kingdomName;
    private final String lenderKingdom; // null for bank loans
    private final double principal; // Original loan amount
    private double remainingBalance; // Remaining amount to pay
    private final double interestRate; // Annual interest rate
    private final long startTime;
    private final long dueDate; // When loan must be repaid
    private final long paymentInterval; // Time between payments in seconds
    private long lastPaymentTime;
    private boolean isPaidOff;
    
    public KingdomLoan(String loanId, String kingdomName, String lenderKingdom, 
                      double principal, double interestRate, long durationSeconds, 
                      long paymentInterval) {
        this.loanId = loanId;
        this.kingdomName = kingdomName;
        this.lenderKingdom = lenderKingdom;
        this.principal = principal;
        this.remainingBalance = principal;
        this.interestRate = interestRate;
        this.startTime = System.currentTimeMillis() / 1000;
        this.dueDate = startTime + durationSeconds;
        this.paymentInterval = paymentInterval;
        this.lastPaymentTime = startTime;
        this.isPaidOff = false;
    }
    
    public String getLoanId() {
        return loanId;
    }
    
    public String getKingdomName() {
        return kingdomName;
    }
    
    public String getLenderKingdom() {
        return lenderKingdom;
    }
    
    public boolean isBankLoan() {
        return lenderKingdom == null;
    }
    
    public double getPrincipal() {
        return principal;
    }
    
    public double getRemainingBalance() {
        return remainingBalance;
    }
    
    public void setRemainingBalance(double remainingBalance) {
        this.remainingBalance = remainingBalance;
        if (remainingBalance <= 0) {
            this.isPaidOff = true;
        }
    }
    
    public double getInterestRate() {
        return interestRate;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getDueDate() {
        return dueDate;
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
    
    public boolean isPaidOff() {
        return isPaidOff;
    }
    
    /**
     * Calculate interest accrued since last payment
     */
    public double calculateInterest() {
        long currentTime = System.currentTimeMillis() / 1000;
        long timeSinceLastPayment = currentTime - lastPaymentTime;
        
        // Calculate interest: (balance * rate * time) / (365 * 24 * 3600) for annual rate
        double timeInYears = (double) timeSinceLastPayment / (365.0 * 24.0 * 3600.0);
        return remainingBalance * interestRate * timeInYears;
    }
    
    /**
     * Calculate next payment amount (principal + interest)
     */
    public double calculateNextPayment() {
        double interest = calculateInterest();
        double principalPayment = principal / (dueDate - startTime) * paymentInterval;
        return principalPayment + interest;
    }
    
    /**
     * Check if loan is overdue
     */
    public boolean isOverdue() {
        return System.currentTimeMillis() / 1000 > dueDate && !isPaidOff;
    }
    
    /**
     * Check if payment is due
     */
    public boolean isPaymentDue() {
        long currentTime = System.currentTimeMillis() / 1000;
        return (currentTime - lastPaymentTime) >= paymentInterval && !isPaidOff;
    }
}

