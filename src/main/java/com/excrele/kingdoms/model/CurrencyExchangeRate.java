package com.excrele.kingdoms.model;

/**
 * Represents currency exchange rates between different economies
 */
public class CurrencyExchangeRate {
    private final String fromCurrency;
    private final String toCurrency;
    private double rate; // How many toCurrency units per fromCurrency unit
    private long lastUpdated;
    
    public CurrencyExchangeRate(String fromCurrency, String toCurrency, double rate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.lastUpdated = System.currentTimeMillis() / 1000;
    }
    
    public String getFromCurrency() {
        return fromCurrency;
    }
    
    public String getToCurrency() {
        return toCurrency;
    }
    
    public double getRate() {
        return rate;
    }
    
    public void setRate(double rate) {
        this.rate = rate;
        this.lastUpdated = System.currentTimeMillis() / 1000;
    }
    
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    /**
     * Convert amount from one currency to another
     */
    public double convert(double amount) {
        return amount * rate;
    }
}

