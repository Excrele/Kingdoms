package com.excrele.kingdoms.model;

import org.bukkit.Chunk;

/**
 * Represents a temporary raid on enemy territory
 */
public class Raid {
    private String raidId;
    private String raidingKingdom;
    private String targetKingdom;
    private Chunk targetChunk;
    private long startTime;
    private long endTime;
    private int resourcesStolen; // Percentage of resources stolen (0-100)
    private boolean active;
    private String worldName;
    private int chunkX;
    private int chunkZ;
    
    public Raid(String raidingKingdom, String targetKingdom, Chunk targetChunk, long duration) {
        this.raidId = java.util.UUID.randomUUID().toString();
        this.raidingKingdom = raidingKingdom;
        this.targetKingdom = targetKingdom;
        this.targetChunk = targetChunk;
        this.worldName = targetChunk.getWorld().getName();
        this.chunkX = targetChunk.getX();
        this.chunkZ = targetChunk.getZ();
        this.startTime = System.currentTimeMillis() / 1000;
        this.endTime = this.startTime + duration;
        this.resourcesStolen = 0;
        this.active = true;
    }
    
    public Raid(String raidId, String raidingKingdom, String targetKingdom, 
                String worldName, int chunkX, int chunkZ, long startTime, long endTime, 
                int resourcesStolen, boolean active) {
        this.raidId = raidId;
        this.raidingKingdom = raidingKingdom;
        this.targetKingdom = targetKingdom;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.startTime = startTime;
        this.endTime = endTime;
        this.resourcesStolen = resourcesStolen;
        this.active = active;
    }
    
    public String getRaidId() { return raidId; }
    public String getRaidingKingdom() { return raidingKingdom; }
    public String getTargetKingdom() { return targetKingdom; }
    public Chunk getTargetChunk() {
        if (targetChunk == null && worldName != null) {
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
            if (world != null) {
                targetChunk = world.getChunkAt(chunkX, chunkZ);
            }
        }
        return targetChunk;
    }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public int getResourcesStolen() { return resourcesStolen; }
    public void setResourcesStolen(int amount) { 
        this.resourcesStolen = Math.max(0, Math.min(100, amount)); 
    }
    public void addResourcesStolen(int amount) {
        setResourcesStolen(this.resourcesStolen + amount);
    }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isExpired() { return System.currentTimeMillis() / 1000 >= endTime; }
    public String getWorldName() { return worldName; }
    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    
    public long getTimeRemaining() {
        long remaining = endTime - (System.currentTimeMillis() / 1000);
        return Math.max(0, remaining);
    }
}

