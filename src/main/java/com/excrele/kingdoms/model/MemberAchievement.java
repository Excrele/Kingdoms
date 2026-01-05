package com.excrele.kingdoms.model;

/**
 * Represents an achievement unlocked by a kingdom member
 */
public class MemberAchievement {
    private String achievementId;
    private String achievementName;
    private String description;
    private long unlockedAt;
    private String unlockedBy; // Usually the player themselves, but could be awarded by king
    private int progress; // For progress-based achievements
    private int target; // Target value for progress-based achievements
    private boolean completed;
    
    public MemberAchievement(String achievementId, String achievementName, String description) {
        this.achievementId = achievementId;
        this.achievementName = achievementName;
        this.description = description;
        this.unlockedAt = 0;
        this.unlockedBy = null;
        this.progress = 0;
        this.target = 0;
        this.completed = false;
    }
    
    public MemberAchievement(String achievementId, String achievementName, String description, 
                             long unlockedAt, String unlockedBy, int progress, int target, boolean completed) {
        this.achievementId = achievementId;
        this.achievementName = achievementName;
        this.description = description;
        this.unlockedAt = unlockedAt;
        this.unlockedBy = unlockedBy;
        this.progress = progress;
        this.target = target;
        this.completed = completed;
    }
    
    public String getAchievementId() { return achievementId; }
    public String getAchievementName() { return achievementName; }
    public String getDescription() { return description; }
    public long getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(long unlockedAt) { this.unlockedAt = unlockedAt; }
    public String getUnlockedBy() { return unlockedBy; }
    public void setUnlockedBy(String unlockedBy) { this.unlockedBy = unlockedBy; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public void addProgress(int amount) { 
        this.progress += amount;
        if (this.target > 0 && this.progress >= this.target) {
            this.completed = true;
        }
    }
    public int getTarget() { return target; }
    public void setTarget(int target) { 
        this.target = target;
        if (this.progress >= target) {
            this.completed = true;
        }
    }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    /**
     * Unlock this achievement
     */
    public void unlock(String unlockedBy) {
        this.unlockedAt = System.currentTimeMillis() / 1000;
        this.unlockedBy = unlockedBy;
        this.completed = true;
    }
    
    /**
     * Get formatted time since unlock
     */
    public String getFormattedTimeSince() {
        if (unlockedAt == 0) return "Not unlocked";
        long now = System.currentTimeMillis() / 1000;
        long diff = now - unlockedAt;
        
        if (diff < 60) return "just now";
        if (diff < 3600) return (diff / 60) + " minutes ago";
        if (diff < 86400) return (diff / 3600) + " hours ago";
        return (diff / 86400) + " days ago";
    }
}

