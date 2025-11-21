package com.excrele.kingdoms.model;

import org.bukkit.Chunk;
import org.bukkit.Location;

/**
 * Represents a claim marker (visual post at claim corner)
 */
public class ClaimMarker {
    private String id;
    private String kingdomName;
    private Chunk chunk;
    private Location location; // Exact location of marker
    private String name; // Optional name for the claim
    private boolean active;
    
    public ClaimMarker(String id, String kingdomName, Chunk chunk, Location location) {
        this.id = id;
        this.kingdomName = kingdomName;
        this.chunk = chunk;
        this.location = location;
        this.name = "";
        this.active = true;
    }
    
    public String getId() { return id; }
    public String getKingdomName() { return kingdomName; }
    public Chunk getChunk() { return chunk; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name != null ? name : ""; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

