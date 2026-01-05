package com.excrele.kingdoms.model;

/**
 * Represents a reputation event that affects kingdom relationships
 */
public class ReputationEvent {
    private final String eventId;
    private final String kingdom1;
    private final String kingdom2;
    private final EventType type;
    private final double reputationChange;
    private final String description;
    private final long timestamp;
    private final String causedBy; // Player or system
    
    public ReputationEvent(String eventId, String kingdom1, String kingdom2, EventType type, 
                          double reputationChange, String description, String causedBy) {
        this.eventId = eventId;
        this.kingdom1 = kingdom1;
        this.kingdom2 = kingdom2;
        this.type = type;
        this.reputationChange = reputationChange;
        this.description = description;
        this.timestamp = System.currentTimeMillis() / 1000;
        this.causedBy = causedBy;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public String getKingdom1() {
        return kingdom1;
    }
    
    public String getKingdom2() {
        return kingdom2;
    }
    
    public EventType getType() {
        return type;
    }
    
    public double getReputationChange() {
        return reputationChange;
    }
    
    public String getDescription() {
        return description;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getCausedBy() {
        return causedBy;
    }
    
    public enum EventType {
        TRADE_COMPLETED,        // +5
        ALLIANCE_FORMED,        // +20
        ALLIANCE_BROKEN,        // -30
        WAR_DECLARED,           // -50
        WAR_ENDED_PEACEFULLY,  // +10
        HELPED_IN_WAR,          // +15
        ATTACKED_ALLY,          // -40
        SHARED_RESOURCES,       // +8
        BROKE_TRADE_AGREEMENT,   // -25
        AIDED_IN_CRISIS,        // +12
        OTHER
    }
}

