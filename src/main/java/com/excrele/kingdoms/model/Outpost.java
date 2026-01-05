package com.excrele.kingdoms.model;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an outpost - a remote claim that can have special features
 */
public class Outpost {
    private final String id;
    private final String kingdomName;
    private final Chunk chunk;
    private final Location spawnLocation;
    private final long createdAt;
    private String name;
    private double maintenanceCost; // Daily maintenance cost
    private long lastMaintenancePaid;
    private boolean isActive;
    private List<String> connectedOutposts; // IDs of connected outposts for fast travel
    private List<String> structures; // Structure IDs in this outpost
    
    public Outpost(String id, String kingdomName, Chunk chunk, Location spawnLocation) {
        this.id = id;
        this.kingdomName = kingdomName;
        this.chunk = chunk;
        this.spawnLocation = spawnLocation;
        this.createdAt = System.currentTimeMillis() / 1000;
        this.name = "Outpost " + id;
        this.maintenanceCost = 0.0;
        this.lastMaintenancePaid = System.currentTimeMillis() / 1000;
        this.isActive = true;
        this.connectedOutposts = new ArrayList<>();
        this.structures = new ArrayList<>();
    }
    
    public String getId() {
        return id;
    }
    
    public String getKingdomName() {
        return kingdomName;
    }
    
    public Chunk getChunk() {
        return chunk;
    }
    
    public Location getSpawnLocation() {
        return spawnLocation;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public double getMaintenanceCost() {
        return maintenanceCost;
    }
    
    public void setMaintenanceCost(double maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }
    
    public long getLastMaintenancePaid() {
        return lastMaintenancePaid;
    }
    
    public void setLastMaintenancePaid(long lastMaintenancePaid) {
        this.lastMaintenancePaid = lastMaintenancePaid;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    /**
     * Check if maintenance is overdue (more than 24 hours)
     */
    public boolean isMaintenanceOverdue() {
        long currentTime = System.currentTimeMillis() / 1000;
        return (currentTime - lastMaintenancePaid) > 86400; // 24 hours
    }
    
    /**
     * Get days since last maintenance
     */
    public long getDaysSinceMaintenance() {
        long currentTime = System.currentTimeMillis() / 1000;
        return (currentTime - lastMaintenancePaid) / 86400;
    }
    
    public List<String> getConnectedOutposts() {
        return connectedOutposts;
    }
    
    public void addConnectedOutpost(String outpostId) {
        if (!connectedOutposts.contains(outpostId)) {
            connectedOutposts.add(outpostId);
        }
    }
    
    public void removeConnectedOutpost(String outpostId) {
        connectedOutposts.remove(outpostId);
    }
    
    public boolean isConnectedTo(String outpostId) {
        return connectedOutposts.contains(outpostId);
    }
    
    public List<String> getStructures() {
        return structures;
    }
    
    public void addStructure(String structureId) {
        if (!structures.contains(structureId)) {
            structures.add(structureId);
        }
    }
    
    public void removeStructure(String structureId) {
        structures.remove(structureId);
    }
}

