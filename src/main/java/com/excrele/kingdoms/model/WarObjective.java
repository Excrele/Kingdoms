package com.excrele.kingdoms.model;

/**
 * Represents a war objective or victory condition
 */
public class WarObjective {
    private final String objectiveId;
    private final String warId;
    private final ObjectiveType type;
    private final String description;
    private final int targetValue; // Target value for completion
    private int currentValue; // Current progress
    private final String targetKingdom; // Which kingdom this objective targets
    private boolean isCompleted;
    
    public WarObjective(String objectiveId, String warId, ObjectiveType type, 
                      String description, int targetValue, String targetKingdom) {
        this.objectiveId = objectiveId;
        this.warId = warId;
        this.type = type;
        this.description = description;
        this.targetValue = targetValue;
        this.currentValue = 0;
        this.targetKingdom = targetKingdom;
        this.isCompleted = false;
    }
    
    public String getObjectiveId() {
        return objectiveId;
    }
    
    public String getWarId() {
        return warId;
    }
    
    public ObjectiveType getType() {
        return type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getTargetValue() {
        return targetValue;
    }
    
    public int getCurrentValue() {
        return currentValue;
    }
    
    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
        if (this.currentValue >= targetValue) {
            this.isCompleted = true;
        }
    }
    
    public void addProgress(int amount) {
        setCurrentValue(currentValue + amount);
    }
    
    public String getTargetKingdom() {
        return targetKingdom;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public double getProgressPercentage() {
        if (targetValue == 0) return 0;
        return Math.min(100.0, (currentValue * 100.0) / targetValue);
    }
    
    public enum ObjectiveType {
        CAPTURE_CHUNKS,      // Capture X chunks
        KILL_PLAYERS,        // Kill X players
        DESTROY_STRUCTURES,  // Destroy X structures
        CONTROL_OUTPOSTS,    // Control X outposts
        DAMAGE_DEALT,        // Deal X damage
        TIME_CONTROL         // Control territory for X time
    }
}

