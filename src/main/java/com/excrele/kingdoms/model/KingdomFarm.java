package com.excrele.kingdoms.model;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.Location;

/**
 * Represents an automated farm within a kingdom
 */
public class KingdomFarm {
    private String name;
    private String kingdomName;
    private Chunk chunk;
    private Location center;
    private FarmType type;
    private Map<String, Integer> resources; // Resource type -> amount
    private long lastHarvest;
    private boolean isActive;
    private int harvestInterval; // seconds between harvests
    
    public KingdomFarm(String name, String kingdomName, Chunk chunk, Location center, FarmType type) {
        this.name = name;
        this.kingdomName = kingdomName;
        this.chunk = chunk;
        this.center = center;
        this.type = type;
        this.resources = new HashMap<>();
        this.lastHarvest = System.currentTimeMillis() / 1000;
        this.isActive = true;
        this.harvestInterval = 3600; // 1 hour default
    }
    
    public enum FarmType {
        CROP, ANIMAL, TREE, FISH, MINERAL
    }
    
    public String getName() { return name; }
    public String getKingdomName() { return kingdomName; }
    public Chunk getChunk() { return chunk; }
    public Location getCenter() { return center; }
    public FarmType getType() { return type; }
    public Map<String, Integer> getResources() { return resources; }
    public void addResource(String resource, int amount) {
        resources.put(resource, resources.getOrDefault(resource, 0) + amount);
    }
    public long getLastHarvest() { return lastHarvest; }
    public void setLastHarvest(long lastHarvest) { this.lastHarvest = lastHarvest; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public int getHarvestInterval() { return harvestInterval; }
    public void setHarvestInterval(int harvestInterval) { this.harvestInterval = harvestInterval; }
    
    public boolean canHarvest() {
        return isActive && (System.currentTimeMillis() / 1000 - lastHarvest) >= harvestInterval;
    }
}

