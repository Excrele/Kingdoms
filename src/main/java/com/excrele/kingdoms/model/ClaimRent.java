package com.excrele.kingdoms.model;

import org.bukkit.Chunk;

/**
 * Represents a claim that is being rented
 */
public class ClaimRent {
    private String id;
    private String ownerKingdom;
    private String renterKingdom;
    private Chunk chunk;
    private double dailyRate;
    private long startTime;
    private long endTime;
    private long lastPayment;
    private boolean active;

    public ClaimRent(String id, String ownerKingdom, String renterKingdom, Chunk chunk, double dailyRate, long durationDays) {
        this.id = id;
        this.ownerKingdom = ownerKingdom;
        this.renterKingdom = renterKingdom;
        this.chunk = chunk;
        this.dailyRate = dailyRate;
        this.startTime = System.currentTimeMillis();
        this.endTime = System.currentTimeMillis() + (durationDays * 24 * 60 * 60 * 1000);
        this.lastPayment = System.currentTimeMillis();
        this.active = true;
    }

    public String getId() { return id; }
    public String getOwnerKingdom() { return ownerKingdom; }
    public String getRenterKingdom() { return renterKingdom; }
    public Chunk getChunk() { return chunk; }
    public double getDailyRate() { return dailyRate; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public long getLastPayment() { return lastPayment; }
    public void setLastPayment(long lastPayment) { this.lastPayment = lastPayment; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public boolean isExpired() {
        return System.currentTimeMillis() >= endTime;
    }
    
    public long getDaysRemaining() {
        long remaining = endTime - System.currentTimeMillis();
        return remaining > 0 ? remaining / (24 * 60 * 60 * 1000) : 0;
    }
    
    public String getChunkKey() {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
}

