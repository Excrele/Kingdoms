package com.excrele.kingdoms.model;

import org.bukkit.Location;

/**
 * Represents a scheduled kingdom event
 */
public class KingdomEvent {
    private String id;
    private String kingdomName;
    private String name;
    private String description;
    private Location location;
    private long scheduledTime; // Unix timestamp
    private boolean active;
    
    public KingdomEvent(String id, String kingdomName, String name, String description, Location location, long scheduledTime) {
        this.id = id;
        this.kingdomName = kingdomName;
        this.name = name;
        this.description = description;
        this.location = location;
        this.scheduledTime = scheduledTime;
        this.active = true;
    }
    
    public String getId() { return id; }
    public String getKingdomName() { return kingdomName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public long getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(long scheduledTime) { this.scheduledTime = scheduledTime; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    /**
     * Check if event is in the past
     */
    public boolean isPast() {
        return System.currentTimeMillis() / 1000 >= scheduledTime;
    }
    
    /**
     * Get time until event (in seconds)
     */
    public long getTimeUntil() {
        long now = System.currentTimeMillis() / 1000;
        return scheduledTime - now;
    }
}

