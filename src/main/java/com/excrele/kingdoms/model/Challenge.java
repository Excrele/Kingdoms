package com.excrele.kingdoms.model;

import java.util.Map;

public class Challenge {
    private String id;
    private String description;
    private int difficulty;
    private int xpReward;
    private Map<String, Object> task;

    public Challenge(String id, String description, int difficulty, int xpReward, Map<String, Object> task) {
        this.id = id;
        this.description = description;
        this.difficulty = difficulty;
        this.xpReward = xpReward;
        this.task = task;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public int getDifficulty() { return difficulty; }
    public int getXpReward() { return xpReward; }
    public Map<String, Object> getTask() { return task; }
}