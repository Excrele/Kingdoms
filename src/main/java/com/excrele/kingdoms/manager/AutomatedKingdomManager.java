package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;

import java.util.*;

/**
 * Manages automated kingdom operations to reduce admin workload
 */
public class AutomatedKingdomManager {
    private final KingdomsPlugin plugin;
    private final Map<String, Long> lastAutoClaim; // kingdom -> last auto-claim timestamp
    private final long autoClaimCooldown; // Cooldown between auto-claims in seconds
    
    public AutomatedKingdomManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.lastAutoClaim = new HashMap<>();
        this.autoClaimCooldown = plugin.getConfig().getLong("automation.auto_claim_cooldown", 300L); // 5 minutes
    }
    
    /**
     * Auto-claim adjacent chunks for a kingdom (if enabled and has funds)
     */
    public int autoClaimAdjacentChunks(Kingdom kingdom) {
        if (!plugin.getConfig().getBoolean("automation.auto_claim_adjacent", false)) {
            return 0;
        }
        
        // Check cooldown
        Long lastClaim = lastAutoClaim.get(kingdom.getName());
        long currentTime = System.currentTimeMillis() / 1000;
        if (lastClaim != null && (currentTime - lastClaim) < autoClaimCooldown) {
            return 0;
        }
        
        // Check if kingdom has enough claim slots
        if (kingdom.getCurrentClaimChunks() >= kingdom.getMaxClaimChunks()) {
            return 0;
        }
        
        // Get claim cost
        double claimCost = plugin.getConfig().getDouble("economy.claim_cost", 100.0);
        boolean economyEnabled = plugin.getConfig().getBoolean("economy.enabled", false);
        
        // Check if kingdom bank has enough funds
        if (economyEnabled && claimCost > 0 && plugin.getBankManager() != null) {
            double bankBalance = plugin.getBankManager().getBalance(kingdom.getName());
            if (bankBalance < claimCost) {
                return 0; // Not enough funds
            }
        }
        
        // Find adjacent unclaimed chunks
        List<Chunk> adjacentChunks = findAdjacentUnclaimedChunks(kingdom);
        if (adjacentChunks.isEmpty()) {
            return 0;
        }
        
        int claimed = 0;
        for (Chunk chunk : adjacentChunks) {
            // Check if we've hit the limit
            if (kingdom.getCurrentClaimChunks() >= kingdom.getMaxClaimChunks()) {
                break;
            }
            
            // Check if we have enough funds for this claim
            if (economyEnabled && claimCost > 0 && plugin.getBankManager() != null) {
                double bankBalance = plugin.getBankManager().getBalance(kingdom.getName());
                if (bankBalance < claimCost) {
                    break; // Not enough funds
                }
            }
            
            // Try to claim the chunk
            if (plugin.getClaimManager().claimChunk(kingdom, chunk)) {
                claimed++;
                
                // Deduct cost from bank
                if (economyEnabled && claimCost > 0 && plugin.getBankManager() != null) {
                    plugin.getBankManager().withdraw(kingdom.getName(), claimCost);
                }
            }
        }
        
        if (claimed > 0) {
            lastAutoClaim.put(kingdom.getName(), currentTime);
            plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile(), true);
        }
        
        return claimed;
    }
    
    /**
     * Find adjacent unclaimed chunks for a kingdom
     */
    private List<Chunk> findAdjacentUnclaimedChunks(Kingdom kingdom) {
        List<Chunk> adjacent = new ArrayList<>();
        Set<String> claimedKeys = new HashSet<>(plugin.getKingdomManager().getClaimedChunks().keySet());
        
        for (List<Chunk> claim : kingdom.getClaims()) {
            for (Chunk chunk : claim) {
                // Check all 8 adjacent chunks
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue; // Skip self
                        
                        Chunk adjacentChunk = chunk.getWorld().getChunkAt(chunk.getX() + dx, chunk.getZ() + dz);
                        String key = adjacentChunk.getWorld().getName() + ":" + 
                                    adjacentChunk.getX() + ":" + adjacentChunk.getZ();
                        
                        if (!claimedKeys.contains(key)) {
                            // Check if it's far enough from other kingdoms
                            if (isValidForClaiming(adjacentChunk, kingdom)) {
                                adjacent.add(adjacentChunk);
                            }
                        }
                    }
                }
            }
        }
        
        return adjacent;
    }
    
    /**
     * Check if a chunk is valid for claiming (respects buffer zones)
     */
    private boolean isValidForClaiming(Chunk chunk, Kingdom kingdom) {
        int bufferZone = 5; // Default
        if (plugin.getWorldManager() != null) {
            bufferZone = plugin.getWorldManager().getBufferZoneForWorld(chunk.getWorld());
        }
        
        for (Kingdom otherK : plugin.getKingdomManager().getKingdoms().values()) {
            if (otherK != kingdom) {
                for (List<Chunk> claim : otherK.getClaims()) {
                    for (Chunk otherChunk : claim) {
                        if (otherChunk.getWorld().equals(chunk.getWorld())) {
                            int distance = Math.max(Math.abs(chunk.getX() - otherChunk.getX()), 
                                                   Math.abs(chunk.getZ() - otherChunk.getZ()));
                            if (distance < (bufferZone + 1)) {
                                return false; // Too close
                            }
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Auto-disband inactive kingdoms
     */
    public int autoDisbandInactiveKingdoms() {
        if (!plugin.getConfig().getBoolean("automation.auto_disband_inactive", false)) {
            return 0;
        }
        
        long inactiveDays = plugin.getConfig().getLong("automation.inactive_days_to_disband", 30L);
        long inactiveTime = inactiveDays * 86400L; // Convert to seconds
        long currentTime = System.currentTimeMillis() / 1000;
        
        int disbanded = 0;
        List<String> toDisband = new ArrayList<>();
        
        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            // Check if kingdom has been inactive
            long lastActivity = getLastActivity(kingdom);
            if ((currentTime - lastActivity) > inactiveTime) {
                // Check if kingdom has minimum members
                int minMembers = plugin.getConfig().getInt("automation.min_members_to_keep", 1);
                if (kingdom.getAllMembers().size() < minMembers) {
                    toDisband.add(kingdom.getName());
                }
            }
        }
        
        for (String kingdomName : toDisband) {
            plugin.getKingdomManager().dissolveKingdom(kingdomName);
            disbanded++;
        }
        
        return disbanded;
    }
    
    /**
     * Auto-merge small kingdoms
     */
    public int autoMergeSmallKingdoms() {
        if (!plugin.getConfig().getBoolean("automation.auto_merge_small", false)) {
            return 0;
        }
        
        int maxMembersForMerge = plugin.getConfig().getInt("automation.max_members_to_merge", 2);
        int maxClaimsForMerge = plugin.getConfig().getInt("automation.max_claims_to_merge", 5);
        
        int merged = 0;
        List<Kingdom> smallKingdoms = new ArrayList<>();
        
        // Find small kingdoms
        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            if (kingdom.getAllMembers().size() <= maxMembersForMerge && 
                kingdom.getCurrentClaimChunks() <= maxClaimsForMerge) {
                smallKingdoms.add(kingdom);
            }
        }
        
        // Try to merge adjacent small kingdoms
        for (int i = 0; i < smallKingdoms.size(); i++) {
            Kingdom kingdom1 = smallKingdoms.get(i);
            if (kingdom1 == null) continue;
            
            for (int j = i + 1; j < smallKingdoms.size(); j++) {
                Kingdom kingdom2 = smallKingdoms.get(j);
                if (kingdom2 == null) continue;
                
                // Check if kingdoms are adjacent
                if (areKingdomsAdjacent(kingdom1, kingdom2)) {
                    // Merge kingdom2 into kingdom1
                    mergeKingdoms(kingdom1, kingdom2);
                    smallKingdoms.set(j, null); // Mark as merged
                    merged++;
                    break;
                }
            }
        }
        
        return merged;
    }
    
    /**
     * Check if two kingdoms are adjacent
     */
    private boolean areKingdomsAdjacent(Kingdom k1, Kingdom k2) {
        for (List<Chunk> claim1 : k1.getClaims()) {
            for (Chunk chunk1 : claim1) {
                for (List<Chunk> claim2 : k2.getClaims()) {
                    for (Chunk chunk2 : claim2) {
                        if (chunk1.getWorld().equals(chunk2.getWorld())) {
                            int distance = Math.max(Math.abs(chunk1.getX() - chunk2.getX()), 
                                                   Math.abs(chunk1.getZ() - chunk2.getZ()));
                            if (distance <= 6) { // Within merge distance (buffer + 1)
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Merge two kingdoms (kingdom2 into kingdom1)
     */
    private void mergeKingdoms(Kingdom kingdom1, Kingdom kingdom2) {
        // Add all members from kingdom2 to kingdom1
        for (String member : kingdom2.getMembers()) {
            if (!kingdom1.getMembers().contains(member)) {
                kingdom1.addMember(member);
                plugin.getKingdomManager().setPlayerKingdom(member, kingdom1.getName());
            }
        }
        
        // Add all claims from kingdom2 to kingdom1
        for (List<Chunk> claim : kingdom2.getClaims()) {
            kingdom1.getClaims().add(claim);
            for (Chunk chunk : claim) {
                String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
                plugin.getKingdomManager().getClaimedChunks().put(key, kingdom1);
            }
        }
        
        kingdom1.setCurrentClaimChunks(kingdom1.getCurrentClaimChunks() + kingdom2.getCurrentClaimChunks());
        
        // Merge XP
        kingdom1.addXp(kingdom2.getXp());
        
        // Merge bank balance
        if (plugin.getBankManager() != null) {
            double balance2 = plugin.getBankManager().getBalance(kingdom2.getName());
            if (balance2 > 0) {
                plugin.getBankManager().deposit(kingdom1.getName(), balance2);
            }
        }
        
        // Dissolve kingdom2
        plugin.getKingdomManager().dissolveKingdom(kingdom2.getName());
    }
    
    /**
     * Get last activity time for a kingdom
     */
    private long getLastActivity(Kingdom kingdom) {
        long lastActivity = kingdom.getCreatedAt();
        
        // Check member activity
        if (plugin.getActivityManager() != null) {
            for (String member : kingdom.getAllMembers()) {
                com.excrele.kingdoms.model.PlayerActivity activity = plugin.getActivityManager().getActivity(member);
                if (activity != null) {
                    long lastLogin = activity.getLastLogin();
                    if (lastLogin > lastActivity) {
                        lastActivity = lastLogin;
                    }
                }
            }
        }
        
        return lastActivity;
    }
}

