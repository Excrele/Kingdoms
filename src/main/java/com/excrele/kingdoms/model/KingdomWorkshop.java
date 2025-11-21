package com.excrele.kingdoms.model;

import org.bukkit.Chunk;
import org.bukkit.Location;

/**
 * Represents a workshop with crafting bonuses
 */
public class KingdomWorkshop {
    private String name;
    private String kingdomName;
    private Chunk chunk;
    private Location location;
    private WorkshopType type;
    private double bonusMultiplier; // e.g., 1.2 = 20% bonus
    private boolean isActive;
    
    public KingdomWorkshop(String name, String kingdomName, Chunk chunk, Location location, WorkshopType type) {
        this.name = name;
        this.kingdomName = kingdomName;
        this.chunk = chunk;
        this.location = location;
        this.type = type;
        this.bonusMultiplier = 1.1; // 10% default bonus
        this.isActive = true;
    }
    
    public enum WorkshopType {
        CRAFTING, SMITHING, ENCHANTING, BREWING, COOKING
    }
    
    public String getName() { return name; }
    public String getKingdomName() { return kingdomName; }
    public Chunk getChunk() { return chunk; }
    public Location getLocation() { return location; }
    public WorkshopType getType() { return type; }
    public double getBonusMultiplier() { return bonusMultiplier; }
    public void setBonusMultiplier(double bonusMultiplier) { this.bonusMultiplier = bonusMultiplier; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
}

