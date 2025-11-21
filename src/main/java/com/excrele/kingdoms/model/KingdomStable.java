package com.excrele.kingdoms.model;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Represents a stable for managing and storing mounts
 */
public class KingdomStable {
    private String name;
    private String kingdomName;
    private Chunk chunk;
    private Location location;
    private List<Entity> storedMounts; // Stored mounts
    private int maxMounts;
    private boolean isPublic; // Can all members access
    
    public KingdomStable(String name, String kingdomName, Chunk chunk, Location location) {
        this.name = name;
        this.kingdomName = kingdomName;
        this.chunk = chunk;
        this.location = location;
        this.storedMounts = new ArrayList<>();
        this.maxMounts = 10; // Default max
        this.isPublic = true;
    }
    
    public String getName() { return name; }
    public String getKingdomName() { return kingdomName; }
    public Chunk getChunk() { return chunk; }
    public Location getLocation() { return location; }
    public List<Entity> getStoredMounts() { return storedMounts; }
    public boolean addMount(Entity mount) {
        if (storedMounts.size() >= maxMounts) return false;
        storedMounts.add(mount);
        return true;
    }
    public boolean removeMount(Entity mount) {
        return storedMounts.remove(mount);
    }
    public int getMaxMounts() { return maxMounts; }
    public void setMaxMounts(int maxMounts) { this.maxMounts = maxMounts; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
}

