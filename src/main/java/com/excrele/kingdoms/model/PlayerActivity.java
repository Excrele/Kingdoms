package com.excrele.kingdoms.model;

/**
 * Tracks player activity within a kingdom
 */
public class PlayerActivity {
    private String playerName;
    private String kingdomName;
    private long lastLogin;
    private long totalPlaytime; // in seconds
    private long lastContribution; // timestamp of last contribution
    private int contributions; // number of contributions
    
    public PlayerActivity(String playerName, String kingdomName) {
        this.playerName = playerName;
        this.kingdomName = kingdomName;
        this.lastLogin = System.currentTimeMillis() / 1000;
        this.totalPlaytime = 0;
        this.lastContribution = System.currentTimeMillis() / 1000;
        this.contributions = 0;
    }
    
    public String getPlayerName() { return playerName; }
    public String getKingdomName() { return kingdomName; }
    public long getLastLogin() { return lastLogin; }
    public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }
    public void updateLastLogin() { this.lastLogin = System.currentTimeMillis() / 1000; }
    public long getTotalPlaytime() { return totalPlaytime; }
    public void addPlaytime(long seconds) { this.totalPlaytime += seconds; }
    public long getLastContribution() { return lastContribution; }
    public void setLastContribution(long lastContribution) { this.lastContribution = lastContribution; }
    public void updateLastContribution() { 
        this.lastContribution = System.currentTimeMillis() / 1000;
        this.contributions++;
    }
    public int getContributions() { return contributions; }
    public void setContributions(int contributions) { this.contributions = contributions; }
    
    /**
     * Get days since last login
     */
    public long getDaysSinceLastLogin() {
        long now = System.currentTimeMillis() / 1000;
        return (now - lastLogin) / (24 * 60 * 60);
    }
    
    /**
     * Check if player is inactive (no login for X days)
     */
    public boolean isInactive(long inactiveDays) {
        return getDaysSinceLastLogin() >= inactiveDays;
    }
}

