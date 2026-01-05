package com.excrele.kingdoms.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an advanced challenge (weekly/monthly, group, or chain)
 */
public class AdvancedChallenge {
    private String challengeId;
    private String name;
    private String description;
    private ChallengeType type; // WEEKLY, MONTHLY, GROUP, CHAIN
    private int difficulty;
    private int xpReward;
    private long startTime;
    private long endTime;
    private int requiredMembers; // For group challenges
    private String chainId; // For chain challenges
    private int chainOrder; // Position in chain
    private List<String> prerequisites; // Challenge IDs that must be completed first
    private boolean active;
    
    public enum ChallengeType {
        WEEKLY, MONTHLY, GROUP, CHAIN
    }
    
    public AdvancedChallenge(String challengeId, String name, String description, ChallengeType type, 
                            int difficulty, int xpReward, long duration) {
        this.challengeId = challengeId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.difficulty = difficulty;
        this.xpReward = xpReward;
        this.startTime = System.currentTimeMillis() / 1000;
        this.endTime = this.startTime + duration;
        this.requiredMembers = 1;
        this.chainId = null;
        this.chainOrder = 0;
        this.prerequisites = new ArrayList<>();
        this.active = true;
    }
    
    public String getChallengeId() { return challengeId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ChallengeType getType() { return type; }
    public int getDifficulty() { return difficulty; }
    public int getXpReward() { return xpReward; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public int getRequiredMembers() { return requiredMembers; }
    public void setRequiredMembers(int members) { this.requiredMembers = members; }
    public String getChainId() { return chainId; }
    public void setChainId(String chainId) { this.chainId = chainId; }
    public int getChainOrder() { return chainOrder; }
    public void setChainOrder(int order) { this.chainOrder = order; }
    public List<String> getPrerequisites() { return prerequisites; }
    public void addPrerequisite(String challengeId) { prerequisites.add(challengeId); }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isExpired() { 
        if (type == ChallengeType.WEEKLY || type == ChallengeType.MONTHLY) {
            return System.currentTimeMillis() / 1000 >= endTime;
        }
        return false;
    }
    
    public long getTimeRemaining() {
        if (type == ChallengeType.WEEKLY || type == ChallengeType.MONTHLY) {
            long remaining = endTime - (System.currentTimeMillis() / 1000);
            return Math.max(0, remaining);
        }
        return -1; // No time limit
    }
}

