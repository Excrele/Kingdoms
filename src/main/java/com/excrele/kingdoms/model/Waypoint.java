package com.excrele.kingdoms.model;

import org.bukkit.Location;

/**
 * Represents a waypoint within a kingdom claim
 */
public class Waypoint {
    private String name;
    private String kingdomName;
    private Location location;
    private String description;
    private long createdAt;
    private String createdBy;
    private boolean isPublic; // Can all kingdom members see it
    
    public Waypoint(String name, String kingdomName, Location location, String createdBy) {
        this.name = name;
        this.kingdomName = kingdomName;
        this.location = location;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis() / 1000;
        this.description = "";
        this.isPublic = true;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getKingdomName() { return kingdomName; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
}

