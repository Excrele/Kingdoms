package com.excrele.kingdoms.model;

import org.bukkit.Chunk;

/**
 * Represents an audit log entry for permission changes
 */
public class PermissionAuditLog {
    private final long timestamp;
    private final String kingdomName;
    private final String player; // Player whose permissions were changed
    private final String permission;
    private final Chunk chunk; // null for kingdom-wide permission
    private final AuditAction action;
    private final String modifiedBy; // Who made the change
    private final String reason;
    
    public PermissionAuditLog(long timestamp, String kingdomName, String player, String permission, 
                              Chunk chunk, AuditAction action, String modifiedBy, String reason) {
        this.timestamp = timestamp;
        this.kingdomName = kingdomName;
        this.player = player;
        this.permission = permission;
        this.chunk = chunk;
        this.action = action;
        this.modifiedBy = modifiedBy;
        this.reason = reason;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getKingdomName() {
        return kingdomName;
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
    
    public AuditAction getAction() {
        return action;
    }
    
    public String getModifiedBy() {
        return modifiedBy;
    }
    
    public String getReason() {
        return reason;
    }
    
    public enum AuditAction {
        GRANTED,
        REVOKED,
        TEMPORARY_GRANTED,
        TEMPORARY_EXPIRED,
        GROUP_APPLIED,
        GROUP_REMOVED
    }
}

