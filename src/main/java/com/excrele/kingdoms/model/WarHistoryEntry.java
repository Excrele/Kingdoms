package com.excrele.kingdoms.model;

/**
 * Represents a single entry in war history
 */
public class WarHistoryEntry {
    private final long timestamp;
    private final String warId;
    private final String eventType;
    private final String description;
    private final String actor; // Who performed the action
    
    public WarHistoryEntry(long timestamp, String warId, String eventType, 
                          String description, String actor) {
        this.timestamp = timestamp;
        this.warId = warId;
        this.eventType = eventType;
        this.description = description;
        this.actor = actor;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getWarId() {
        return warId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getActor() {
        return actor;
    }
}

