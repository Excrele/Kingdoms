package com.excrele.kingdoms.model;

import org.bukkit.Chunk;

/**
 * Represents a temporary permission that expires after a certain time
 */
public class TemporaryPermission {
    private final String player;
    private final String permission;
    private final Chunk chunk; // null for kingdom-wide permission
    private final long expirationTime;
    private final String grantedBy;
    
    public TemporaryPermission(String player, String permission, Chunk chunk, long expirationTime, String grantedBy) {
        this.player = player;
        this.permission = permission.toLowerCase();
        this.chunk = chunk;
        this.expirationTime = expirationTime;
        this.grantedBy = grantedBy;
    }
    
    public String getPlayer() {
        return player;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public Chunk getChunk() {
        return chunk;
    }
    
    public boolean isChunkSpecific() {
        return chunk != null;
    }
    
    public long getExpirationTime() {
        return expirationTime;
    }
    
    public String getGrantedBy() {
        return grantedBy;
    }
    
    /**
     * Check if this permission has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() / 1000 >= expirationTime;
    }
    
    /**
     * Get time remaining in seconds
     */
    public long getTimeRemaining() {
        long current = System.currentTimeMillis() / 1000;
        return Math.max(0, expirationTime - current);
    }
}

