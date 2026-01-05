package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.*;
import org.bukkit.Chunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages advanced permissions including per-chunk permissions, temporary permissions,
 * permission groups, and audit logs
 */
public class AdvancedPermissionManager {
    private final KingdomsPlugin plugin;
    private final Map<String, Map<Chunk, ChunkPermission>> chunkPermissions; // kingdom -> chunk -> permissions
    private final Map<String, List<TemporaryPermission>> temporaryPermissions; // kingdom -> temp permissions
    private final Map<String, PermissionGroup> permissionGroups; // group name -> group
    private final Map<String, List<PermissionAuditLog>> auditLogs; // kingdom -> logs
    private final Map<String, String> memberPermissionGroups; // player -> group name
    
    public AdvancedPermissionManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.chunkPermissions = new ConcurrentHashMap<>();
        this.temporaryPermissions = new ConcurrentHashMap<>();
        this.permissionGroups = new ConcurrentHashMap<>();
        this.auditLogs = new ConcurrentHashMap<>();
        this.memberPermissionGroups = new ConcurrentHashMap<>();
        
        // Initialize default permission groups
        initializeDefaultGroups();
    }
    
    private void initializeDefaultGroups() {
        // Builder group - can build and interact
        Set<String> builderPerms = new HashSet<>(Arrays.asList("build", "break", "interact", "use"));
        permissionGroups.put("builder", new PermissionGroup("builder", builderPerms, 
            "Can build and interact in claims", false));
        
        // Guard group - can claim and protect
        Set<String> guardPerms = new HashSet<>(Arrays.asList("claim", "unclaim", "setflags", "pvp"));
        permissionGroups.put("guard", new PermissionGroup("guard", guardPerms, 
            "Can claim and protect territory", false));
        
        // Admin group - most permissions
        Set<String> adminPerms = new HashSet<>(Arrays.asList("build", "break", "interact", "claim", 
            "unclaim", "setflags", "invite", "kick", "promote", "bank", "structure"));
        permissionGroups.put("admin", new PermissionGroup("admin", adminPerms, 
            "Has most administrative permissions", false));
    }
    
    /**
     * Check if a player has a permission in a chunk
     */
    public boolean hasChunkPermission(String kingdomName, String player, Chunk chunk, 
                                     String permission, MemberRole role) {
        // Check temporary permissions first
        if (hasTemporaryPermission(kingdomName, player, chunk, permission)) {
            return true;
        }
        
        // Check chunk-specific permissions
        Map<Chunk, ChunkPermission> kingdomChunks = chunkPermissions.get(kingdomName);
        if (kingdomChunks != null) {
            ChunkPermission chunkPerm = kingdomChunks.get(chunk);
            if (chunkPerm != null) {
                if (chunkPerm.hasPermission(player, permission, role)) {
                    return true;
                }
            }
        }
        
        // Check permission group
        String groupName = memberPermissionGroups.get(player);
        if (groupName != null) {
            PermissionGroup group = permissionGroups.get(groupName);
            if (group != null && group.hasPermission(permission)) {
                return true;
            }
        }
        
        // Fall back to role-based permissions
        return role != null && checkRolePermission(role, permission);
    }
    
    /**
     * Grant a permission to a player in a specific chunk
     */
    public void grantChunkPermission(String kingdomName, String player, Chunk chunk, 
                                    String permission, String grantedBy, String reason) {
        chunkPermissions.computeIfAbsent(kingdomName, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(chunk, k -> new ChunkPermission(chunk))
            .grantPlayerPermission(player, permission);
        
        // Log to audit
        logPermissionChange(kingdomName, player, permission, chunk, 
            PermissionAuditLog.AuditAction.GRANTED, grantedBy, reason);
    }
    
    /**
     * Revoke a permission from a player in a specific chunk
     */
    public void revokeChunkPermission(String kingdomName, String player, Chunk chunk, 
                                     String permission, String revokedBy, String reason) {
        Map<Chunk, ChunkPermission> kingdomChunks = chunkPermissions.get(kingdomName);
        if (kingdomChunks != null) {
            ChunkPermission chunkPerm = kingdomChunks.get(chunk);
            if (chunkPerm != null) {
                chunkPerm.revokePlayerPermission(player, permission);
            }
        }
        
        // Log to audit
        logPermissionChange(kingdomName, player, permission, chunk, 
            PermissionAuditLog.AuditAction.REVOKED, revokedBy, reason);
    }
    
    /**
     * Grant a temporary permission
     */
    public void grantTemporaryPermission(String kingdomName, String player, Chunk chunk, 
                                       String permission, long durationSeconds, String grantedBy) {
        long expirationTime = (System.currentTimeMillis() / 1000) + durationSeconds;
        TemporaryPermission tempPerm = new TemporaryPermission(player, permission, chunk, 
            expirationTime, grantedBy);
        
        temporaryPermissions.computeIfAbsent(kingdomName, k -> new ArrayList<>()).add(tempPerm);
        
        // Log to audit
        logPermissionChange(kingdomName, player, permission, chunk, 
            PermissionAuditLog.AuditAction.TEMPORARY_GRANTED, grantedBy, 
            "Temporary permission for " + durationSeconds + " seconds");
    }
    
    /**
     * Check if a player has a temporary permission
     */
    private boolean hasTemporaryPermission(String kingdomName, String player, Chunk chunk, String permission) {
        List<TemporaryPermission> tempPerms = temporaryPermissions.get(kingdomName);
        if (tempPerms == null) {
            return false;
        }
        
        // Remove expired permissions
        tempPerms.removeIf(perm -> {
            if (perm.isExpired()) {
                logPermissionChange(kingdomName, perm.getPlayer(), perm.getPermission(), perm.getChunk(),
                    PermissionAuditLog.AuditAction.TEMPORARY_EXPIRED, "SYSTEM", "Permission expired");
                return true;
            }
            return false;
        });
        
        // Check for matching permission
        for (TemporaryPermission tempPerm : tempPerms) {
            if (tempPerm.getPlayer().equals(player) && 
                tempPerm.getPermission().equals(permission.toLowerCase())) {
                if (chunk == null || tempPerm.getChunk() == null || tempPerm.getChunk().equals(chunk)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Apply a permission group to a player
     */
    public void applyPermissionGroup(String kingdomName, String player, String groupName, String appliedBy) {
        if (!permissionGroups.containsKey(groupName)) {
            return;
        }
        
        memberPermissionGroups.put(player, groupName);
        
        // Log to audit
        logPermissionChange(kingdomName, player, "group:" + groupName, null, 
            PermissionAuditLog.AuditAction.GROUP_APPLIED, appliedBy, "Applied permission group");
    }
    
    /**
     * Remove a permission group from a player
     */
    public void removePermissionGroup(String kingdomName, String player, String removedBy) {
        String groupName = memberPermissionGroups.remove(player);
        if (groupName != null) {
            logPermissionChange(kingdomName, player, "group:" + groupName, null, 
                PermissionAuditLog.AuditAction.GROUP_REMOVED, removedBy, "Removed permission group");
        }
    }
    
    /**
     * Create a new permission group
     */
    public void createPermissionGroup(String name, Set<String> permissions, String description) {
        permissionGroups.put(name.toLowerCase(), new PermissionGroup(name, permissions, description, false));
    }
    
    /**
     * Get a permission group
     */
    public PermissionGroup getPermissionGroup(String name) {
        return permissionGroups.get(name.toLowerCase());
    }
    
    /**
     * Get all permission groups
     */
    public Collection<PermissionGroup> getPermissionGroups() {
        return permissionGroups.values();
    }
    
    /**
     * Get audit logs for a kingdom
     */
    public List<PermissionAuditLog> getAuditLogs(String kingdomName) {
        return auditLogs.getOrDefault(kingdomName, new ArrayList<>());
    }
    
    /**
     * Log a permission change
     */
    private void logPermissionChange(String kingdomName, String player, String permission, Chunk chunk,
                                    PermissionAuditLog.AuditAction action, String modifiedBy, String reason) {
        PermissionAuditLog log = new PermissionAuditLog(
            System.currentTimeMillis() / 1000,
            kingdomName,
            player,
            permission,
            chunk,
            action,
            modifiedBy,
            reason
        );
        
        auditLogs.computeIfAbsent(kingdomName, k -> new ArrayList<>()).add(log);
        
        // Keep only last 1000 logs per kingdom
        List<PermissionAuditLog> logs = auditLogs.get(kingdomName);
        if (logs.size() > 1000) {
            logs.remove(0);
        }
    }
    
    /**
     * Check if a role has a permission (fallback to default role system)
     */
    private boolean checkRolePermission(MemberRole role, String permission) {
        return switch (permission.toLowerCase()) {
            case "build", "break", "interact" -> role != MemberRole.MEMBER;
            case "claim" -> role.canClaim();
            case "unclaim" -> role.canUnclaim();
            case "setflags" -> role.canSetFlags();
            case "invite" -> role.canInvite();
            case "kick" -> role.canKick();
            case "promote" -> role.canPromote();
            default -> false;
        };
    }
    
    /**
     * Get chunk permissions for a kingdom
     */
    public Map<Chunk, ChunkPermission> getChunkPermissions(String kingdomName) {
        return chunkPermissions.getOrDefault(kingdomName, new HashMap<>());
    }
    
    /**
     * Get temporary permissions for a kingdom
     */
    public List<TemporaryPermission> getTemporaryPermissions(String kingdomName) {
        return temporaryPermissions.getOrDefault(kingdomName, new ArrayList<>());
    }
}

