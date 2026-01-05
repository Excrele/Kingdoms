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
    private int contributionStreak; // consecutive days with contributions
    private long lastStreakDay; // last day (day number) contribution was made
    
    public PlayerActivity(String playerName, String kingdomName) {
        this.playerName = playerName;
        this.kingdomName = kingdomName;
        this.lastLogin = System.currentTimeMillis() / 1000;
        this.totalPlaytime = 0;
        this.lastContribution = System.currentTimeMillis() / 1000;
        this.contributions = 0;
        this.contributionStreak = 0;
        this.lastStreakDay = getCurrentDay();
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
        updateContributionStreak();
    }
    
    /**
     * Update contribution streak based on daily contributions
     */
    private void updateContributionStreak() {
        long currentDay = getCurrentDay();
        if (currentDay == lastStreakDay) {
            // Already contributed today, no change
            return;
        } else if (currentDay == lastStreakDay + 1) {
            // Consecutive day - increment streak
            contributionStreak++;
        } else {
            // Streak broken - reset to 1
            contributionStreak = 1;
        }
        lastStreakDay = currentDay;
    }
    
    /**
     * Get current day number (days since epoch)
     */
    private long getCurrentDay() {
        return System.currentTimeMillis() / 1000 / (24 * 60 * 60);
    }
    
    public int getContributions() { return contributions; }
    public void setContributions(int contributions) { this.contributions = contributions; }
    public int getContributionStreak() { return contributionStreak; }
    public void setContributionStreak(int streak) { this.contributionStreak = streak; }
    public long getLastStreakDay() { return lastStreakDay; }
    public void setLastStreakDay(long day) { this.lastStreakDay = day; }
    
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

