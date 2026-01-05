package com.excrele.kingdoms.model;

import org.bukkit.Chunk;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Represents permissions for a specific chunk
 */
public class ChunkPermission {
    private final Chunk chunk;
    private final Map<String, Set<String>> playerPermissions; // player -> permissions
    private final Map<String, Set<String>> rolePermissions; // role -> permissions
    private final Map<String, Boolean> defaultPermissions; // permission -> allowed/denied
    
    public ChunkPermission(Chunk chunk) {
        this.chunk = chunk;
        this.playerPermissions = new HashMap<>();
        this.rolePermissions = new HashMap<>();
        this.defaultPermissions = new HashMap<>();
    }
    
    public Chunk getChunk() {
        return chunk;
    }
    
    /**
     * Check if a player has a permission in this chunk
     */
    public boolean hasPermission(String player, String permission, MemberRole role) {
        String perm = permission.toLowerCase();
        
        // Check player-specific permissions first
        Set<String> playerPerms = playerPermissions.get(player);
        if (playerPerms != null && playerPerms.contains(perm)) {
            return true;
        }
        
        // Check role permissions
        Set<String> rolePerms = rolePermissions.get(role.name());
        if (rolePerms != null && rolePerms.contains(perm)) {
            return true;
        }
        
        // Check default permissions
        Boolean defaultPerm = defaultPermissions.get(perm);
        if (defaultPerm != null) {
            return defaultPerm;
        }
        
        // Default: deny
        return false;
    }
    
    /**
     * Grant a permission to a player in this chunk
     */
    public void grantPlayerPermission(String player, String permission) {
        playerPermissions.computeIfAbsent(player, k -> new HashSet<>()).add(permission.toLowerCase());
    }
    
    /**
     * Revoke a permission from a player in this chunk
     */
    public void revokePlayerPermission(String player, String permission) {
        Set<String> perms = playerPermissions.get(player);
        if (perms != null) {
            perms.remove(permission.toLowerCase());
            if (perms.isEmpty()) {
                playerPermissions.remove(player);
            }
        }
    }
    
    /**
     * Grant a permission to a role in this chunk
     */
    public void grantRolePermission(String role, String permission) {
        rolePermissions.computeIfAbsent(role, k -> new HashSet<>()).add(permission.toLowerCase());
    }
    
    /**
     * Set a default permission for this chunk
     */
    public void setDefaultPermission(String permission, boolean allowed) {
        defaultPermissions.put(permission.toLowerCase(), allowed);
    }
    
    public Map<String, Set<String>> getPlayerPermissions() {
        return playerPermissions;
    }
    
    public Map<String, Set<String>> getRolePermissions() {
        return rolePermissions;
    }
    
    public Map<String, Boolean> getDefaultPermissions() {
        return defaultPermissions;
    }
}

