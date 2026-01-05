package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.Raid;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages territory raids - temporary resource stealing
 */
public class RaidManager {
    private final KingdomsPlugin plugin;
    // chunk key -> Raid
    private final Map<String, Raid> activeRaids;
    // kingdom -> List of raids
    private final Map<String, List<String>> kingdomRaids;
    
    public RaidManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.activeRaids = new ConcurrentHashMap<>();
        this.kingdomRaids = new ConcurrentHashMap<>();
        loadActiveRaids();
    }
    
    private void loadActiveRaids() {
        List<Map<String, Object>> raids = plugin.getStorageManager().getAdapter().loadActiveRaids();
        for (Map<String, Object> raidData : raids) {
            String raidId = (String) raidData.get("raidId");
            String raidingKingdom = (String) raidData.get("raidingKingdom");
            String targetKingdom = (String) raidData.get("targetKingdom");
            String worldName = (String) raidData.get("worldName");
            int chunkX = ((Number) raidData.get("chunkX")).intValue();
            int chunkZ = ((Number) raidData.get("chunkZ")).intValue();
            long startTime = ((Number) raidData.get("startTime")).longValue();
            long endTime = ((Number) raidData.get("endTime")).longValue();
            int resourcesStolen = ((Number) raidData.getOrDefault("resourcesStolen", 0)).intValue();
            boolean active = (Boolean) raidData.getOrDefault("active", true);
            
            Raid raid = new Raid(raidId, raidingKingdom, targetKingdom, 
                               worldName, chunkX, chunkZ, startTime, endTime, resourcesStolen, active);
            
            if (raid.isActive() && !raid.isExpired()) {
                String chunkKey = getChunkKey(worldName, chunkX, chunkZ);
                activeRaids.put(chunkKey, raid);
                kingdomRaids.computeIfAbsent(raidingKingdom, k -> new ArrayList<>()).add(raidId);
                kingdomRaids.computeIfAbsent(targetKingdom, k -> new ArrayList<>()).add(raidId);
            }
        }
    }
    
    /**
     * Start a raid on enemy territory
     */
    public boolean startRaid(String raidingKingdom, Chunk targetChunk, long duration) {
        Kingdom targetKingdom = plugin.getKingdomManager().getKingdomByChunk(targetChunk);
        if (targetKingdom == null) {
            return false; // Chunk not claimed
        }
        
        String targetKingdomName = targetKingdom.getName();
        if (targetKingdomName.equals(raidingKingdom)) {
            return false; // Can't raid your own territory
        }
        
        // Check if kingdoms are at war or if raiding is allowed
        boolean atWar = plugin.getWarManager().isAtWar(raidingKingdom, targetKingdomName);
        if (!atWar && !plugin.getConfig().getBoolean("raids.allow-peaceful-raids", false)) {
            return false; // Must be at war to raid
        }
        
        // Check if chunk is already being raided
        String chunkKey = getChunkKey(targetChunk);
        if (activeRaids.containsKey(chunkKey)) {
            return false; // Already being raided
        }
        
        // Create raid
        Raid raid = new Raid(raidingKingdom, targetKingdomName, targetChunk, duration);
        activeRaids.put(chunkKey, raid);
        kingdomRaids.computeIfAbsent(raidingKingdom, k -> new ArrayList<>()).add(raid.getRaidId());
        kingdomRaids.computeIfAbsent(targetKingdomName, k -> new ArrayList<>()).add(raid.getRaidId());
        
        // Save to storage
        saveRaid(raid);
        
        // Notify kingdoms
        broadcastRaidStart(raid);
        
        return true;
    }
    
    /**
     * Steal resources during a raid
     */
    public void stealResources(Player player, Chunk chunk) {
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom == null) return;
        
        String chunkKey = getChunkKey(chunk);
        Raid raid = activeRaids.get(chunkKey);
        if (raid == null || !raid.isActive()) return;
        
        // Only raiding kingdom members can steal
        if (!raid.getRaidingKingdom().equals(playerKingdom)) return;
        
        // Steal resources (percentage of kingdom bank)
        Kingdom targetKingdom = plugin.getKingdomManager().getKingdom(raid.getTargetKingdom());
        if (targetKingdom == null) return;
        
        double bankBalance = plugin.getBankManager().getBalance(targetKingdom.getName());
        double stealAmount = bankBalance * 0.01; // 1% per second of presence
        
        if (stealAmount > 0 && bankBalance >= stealAmount) {
            // Transfer to raiding kingdom
            plugin.getBankManager().withdraw(targetKingdom.getName(), stealAmount);
            plugin.getBankManager().deposit(raid.getRaidingKingdom(), stealAmount);
            
            raid.addResourcesStolen(1);
            saveRaid(raid);
            
            player.sendMessage("§6[Raid] Stole §e" + String.format("%.2f", stealAmount) + " §6from " + raid.getTargetKingdom());
        }
    }
    
    /**
     * Complete a raid
     */
    public void completeRaid(Raid raid) {
        raid.setActive(false);
        String chunkKey = getChunkKey(raid.getTargetChunk());
        activeRaids.remove(chunkKey);
        
        saveRaid(raid);
        broadcastRaidComplete(raid);
    }
    
    /**
     * Get active raid on a chunk
     */
    public Raid getRaid(Chunk chunk) {
        return activeRaids.get(getChunkKey(chunk));
    }
    
    /**
     * Check if a chunk is being raided
     */
    public boolean isBeingRaided(Chunk chunk) {
        Raid raid = getRaid(chunk);
        return raid != null && raid.isActive() && !raid.isExpired();
    }
    
    /**
     * Get all active raids for a kingdom
     */
    public List<Raid> getKingdomRaids(String kingdomName) {
        List<Raid> raids = new ArrayList<>();
        List<String> raidIds = kingdomRaids.getOrDefault(kingdomName, new ArrayList<>());
        for (String raidId : raidIds) {
            for (Raid raid : activeRaids.values()) {
                if (raid.getRaidId().equals(raidId) && raid.isActive() && !raid.isExpired()) {
                    raids.add(raid);
                }
            }
        }
        return raids;
    }
    
    /**
     * Check and expire old raids
     */
    public void checkExpiredRaids() {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Raid> entry : activeRaids.entrySet()) {
            Raid raid = entry.getValue();
            if (raid.isExpired() || !raid.isActive()) {
                completeRaid(raid);
                toRemove.add(entry.getKey());
            }
        }
        for (String key : toRemove) {
            activeRaids.remove(key);
        }
    }
    
    private void broadcastRaidStart(Raid raid) {
        Kingdom raiding = plugin.getKingdomManager().getKingdom(raid.getRaidingKingdom());
        Kingdom target = plugin.getKingdomManager().getKingdom(raid.getTargetKingdom());
        
        if (raiding != null) {
            String message = "§6§l⚔ Raid Started! §r§7Raid on " + raid.getTargetKingdom() + 
                " at (" + raid.getChunkX() + ", " + raid.getChunkZ() + ")";
            broadcastToKingdom(raiding, message);
        }
        
        if (target != null) {
            String message = "§c§l⚔ Under Raid! §r§7Your territory at (" + 
                raid.getChunkX() + ", " + raid.getChunkZ() + ") is being raided!";
            broadcastToKingdom(target, message);
        }
    }
    
    private void broadcastRaidComplete(Raid raid) {
        Kingdom raiding = plugin.getKingdomManager().getKingdom(raid.getRaidingKingdom());
        Kingdom target = plugin.getKingdomManager().getKingdom(raid.getTargetKingdom());
        
        if (raiding != null) {
            String message = "§a§l✓ Raid Complete! §r§7Stole §e" + raid.getResourcesStolen() + 
                "% §7resources from " + raid.getTargetKingdom();
            broadcastToKingdom(raiding, message);
        }
        
        if (target != null) {
            String message = "§c§l✗ Raid Ended! §r§7Lost §e" + raid.getResourcesStolen() + 
                "% §7resources to " + raid.getRaidingKingdom();
            broadcastToKingdom(target, message);
        }
    }
    
    private void broadcastToKingdom(Kingdom kingdom, String message) {
        for (String member : kingdom.getMembers()) {
            Player player = plugin.getServer().getPlayer(member);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
        Player king = plugin.getServer().getPlayer(kingdom.getKing());
        if (king != null && king.isOnline()) {
            king.sendMessage(message);
        }
    }
    
    private String getChunkKey(Chunk chunk) {
        return getChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }
    
    private String getChunkKey(String worldName, int x, int z) {
        return worldName + ":" + x + ":" + z;
    }
    
    private void saveRaid(Raid raid) {
        plugin.getStorageManager().getAdapter().saveRaid(
            raid.getRaidId(),
            raid.getRaidingKingdom(),
            raid.getTargetKingdom(),
            raid.getWorldName(),
            raid.getChunkX(),
            raid.getChunkZ(),
            raid.getStartTime(),
            raid.getEndTime(),
            raid.getResourcesStolen(),
            raid.isActive()
        );
    }
}

