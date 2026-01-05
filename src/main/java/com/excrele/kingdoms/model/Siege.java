package com.excrele.kingdoms.model;

import org.bukkit.Chunk;

/**
 * Represents a siege on a chunk during war
 */
public class Siege {
    private String siegeId;
    private String warId;
    private String attackingKingdom;
    private String defendingKingdom;
    private Chunk targetChunk;
    private long startTime;
    private long endTime;
    private int attackProgress; // 0-100
    private boolean active;
    private String worldName;
    private int chunkX;
    private int chunkZ;
    
    public Siege(String warId, String attackingKingdom, String defendingKingdom, Chunk targetChunk, long duration) {
        this.siegeId = java.util.UUID.randomUUID().toString();
        this.warId = warId;
        this.attackingKingdom = attackingKingdom;
        this.defendingKingdom = defendingKingdom;
        this.targetChunk = targetChunk;
        this.worldName = targetChunk.getWorld().getName();
        this.chunkX = targetChunk.getX();
        this.chunkZ = targetChunk.getZ();
        this.startTime = System.currentTimeMillis() / 1000;
        this.endTime = this.startTime + duration;
        this.attackProgress = 0;
        this.active = true;
    }
    
    public Siege(String siegeId, String warId, String attackingKingdom, String defendingKingdom, 
                 String worldName, int chunkX, int chunkZ, long startTime, long endTime, 
                 int attackProgress, boolean active) {
        this.siegeId = siegeId;
        this.warId = warId;
        this.attackingKingdom = attackingKingdom;
        this.defendingKingdom = defendingKingdom;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.startTime = startTime;
        this.endTime = endTime;
        this.attackProgress = attackProgress;
        this.active = active;
    }
    
    public String getSiegeId() { return siegeId; }
    public String getWarId() { return warId; }
    public String getAttackingKingdom() { return attackingKingdom; }
    public String getDefendingKingdom() { return defendingKingdom; }
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
    public int getAttackProgress() { return attackProgress; }
    public void setAttackProgress(int progress) { 
        this.attackProgress = Math.max(0, Math.min(100, progress)); 
    }
    public void addProgress(int amount) {
        setAttackProgress(this.attackProgress + amount);
    }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isExpired() { return System.currentTimeMillis() / 1000 >= endTime; }
    public boolean isComplete() { return attackProgress >= 100; }
    public String getWorldName() { return worldName; }
    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    
    public long getTimeRemaining() {
        long remaining = endTime - (System.currentTimeMillis() / 1000);
        return Math.max(0, remaining);
    }
}

