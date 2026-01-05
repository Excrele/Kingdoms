package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.Siege;
import com.excrele.kingdoms.model.War;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages sieges during wars - allows capturing enemy chunks
 */
public class SiegeManager {
    private final KingdomsPlugin plugin;
    // chunk key -> Siege
    private final Map<String, Siege> activeSieges;
    // kingdom -> List of sieges
    private final Map<String, List<String>> kingdomSieges;
    
    public SiegeManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.activeSieges = new ConcurrentHashMap<>();
        this.kingdomSieges = new ConcurrentHashMap<>();
        loadActiveSieges();
    }
    
    private void loadActiveSieges() {
        List<Map<String, Object>> sieges = plugin.getStorageManager().getAdapter().loadActiveSieges();
        for (Map<String, Object> siegeData : sieges) {
            String siegeId = (String) siegeData.get("siegeId");
            String warId = (String) siegeData.get("warId");
            String attackingKingdom = (String) siegeData.get("attackingKingdom");
            String defendingKingdom = (String) siegeData.get("defendingKingdom");
            String worldName = (String) siegeData.get("worldName");
            int chunkX = ((Number) siegeData.get("chunkX")).intValue();
            int chunkZ = ((Number) siegeData.get("chunkZ")).intValue();
            long startTime = ((Number) siegeData.get("startTime")).longValue();
            long endTime = ((Number) siegeData.get("endTime")).longValue();
            int attackProgress = ((Number) siegeData.getOrDefault("attackProgress", 0)).intValue();
            boolean active = (Boolean) siegeData.getOrDefault("active", true);
            
            Siege siege = new Siege(siegeId, warId, attackingKingdom, defendingKingdom, 
                                   worldName, chunkX, chunkZ, startTime, endTime, attackProgress, active);
            
            if (siege.isActive() && !siege.isExpired()) {
                String chunkKey = getChunkKey(worldName, chunkX, chunkZ);
                activeSieges.put(chunkKey, siege);
                kingdomSieges.computeIfAbsent(attackingKingdom, k -> new ArrayList<>()).add(siegeId);
                kingdomSieges.computeIfAbsent(defendingKingdom, k -> new ArrayList<>()).add(siegeId);
            }
        }
    }
    
    /**
     * Start a siege on an enemy chunk during war
     */
    public boolean startSiege(String attackingKingdom, Chunk targetChunk, long duration) {
        // Check if kingdoms are at war
        Kingdom defendingKingdom = plugin.getKingdomManager().getKingdomByChunk(targetChunk);
        if (defendingKingdom == null) {
            return false; // Chunk not claimed
        }
        
        String defendingKingdomName = defendingKingdom.getName();
        if (defendingKingdomName.equals(attackingKingdom)) {
            return false; // Can't siege your own chunk
        }
        
        War war = plugin.getWarManager().getWar(attackingKingdom, defendingKingdomName);
        if (war == null || !war.isActive() || war.isExpired()) {
            return false; // Not at war
        }
        
        // Check if chunk is already under siege
        String chunkKey = getChunkKey(targetChunk);
        if (activeSieges.containsKey(chunkKey)) {
            return false; // Already under siege
        }
        
        // Create siege
        Siege siege = new Siege(war.getWarId(), attackingKingdom, defendingKingdomName, targetChunk, duration);
        activeSieges.put(chunkKey, siege);
        kingdomSieges.computeIfAbsent(attackingKingdom, k -> new ArrayList<>()).add(siege.getSiegeId());
        kingdomSieges.computeIfAbsent(defendingKingdomName, k -> new ArrayList<>()).add(siege.getSiegeId());
        
        // Save to storage
        saveSiege(siege);
        
        // Notify kingdoms
        broadcastSiegeStart(siege);
        
        return true;
    }
    
    /**
     * Contribute to siege progress (called when attacking players are in the chunk)
     */
    public void contributeToSiege(Player player, Chunk chunk) {
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom == null) return;
        
        String chunkKey = getChunkKey(chunk);
        Siege siege = activeSieges.get(chunkKey);
        if (siege == null || !siege.isActive()) return;
        
        // Only attacking kingdom members can contribute
        if (!siege.getAttackingKingdom().equals(playerKingdom)) return;
        
        // Add progress (1 point per second of presence)
        siege.addProgress(1);
        
        // Check if siege is complete
        if (siege.isComplete()) {
            completeSiege(siege);
        } else {
            saveSiege(siege);
        }
    }
    
    /**
     * Defend against siege (called when defending players are in the chunk)
     */
    public void defendSiege(Player player, Chunk chunk) {
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom == null) return;
        
        String chunkKey = getChunkKey(chunk);
        Siege siege = activeSieges.get(chunkKey);
        if (siege == null || !siege.isActive()) return;
        
        // Only defending kingdom members can defend
        if (!siege.getDefendingKingdom().equals(playerKingdom)) return;
        
        // Reduce progress (defense reduces attack progress)
        siege.addProgress(-2); // Defense is twice as effective
        
        saveSiege(siege);
    }
    
    /**
     * Complete a siege - transfer chunk ownership
     */
    private void completeSiege(Siege siege) {
        Chunk chunk = siege.getTargetChunk();
        if (chunk == null) return;
        
        Kingdom defendingKingdom = plugin.getKingdomManager().getKingdom(siege.getDefendingKingdom());
        Kingdom attackingKingdom = plugin.getKingdomManager().getKingdom(siege.getAttackingKingdom());
        
        if (defendingKingdom == null || attackingKingdom == null) return;
        
        // Unclaim from defending kingdom
        plugin.getClaimManager().unclaimChunk(defendingKingdom, chunk);
        
        // Claim for attacking kingdom
        plugin.getClaimManager().claimChunk(attackingKingdom, chunk);
        
        // Mark siege as complete
        siege.setActive(false);
        String chunkKey = getChunkKey(chunk);
        activeSieges.remove(chunkKey);
        
        // Save siege completion
        saveSiege(siege);
        
        // Notify kingdoms
        broadcastSiegeComplete(siege);
        
        // Award war score
        War war = plugin.getWarManager().getWar(siege.getAttackingKingdom(), siege.getDefendingKingdom());
        if (war != null) {
            war.addScore(siege.getAttackingKingdom(), 10); // 10 points for capturing a chunk
        }
    }
    
    /**
     * Get active siege on a chunk
     */
    public Siege getSiege(Chunk chunk) {
        return activeSieges.get(getChunkKey(chunk));
    }
    
    /**
     * Check if a chunk is under siege
     */
    public boolean isUnderSiege(Chunk chunk) {
        Siege siege = getSiege(chunk);
        return siege != null && siege.isActive() && !siege.isExpired();
    }
    
    /**
     * Get all active sieges for a kingdom
     */
    public List<Siege> getKingdomSieges(String kingdomName) {
        List<Siege> sieges = new ArrayList<>();
        List<String> siegeIds = kingdomSieges.getOrDefault(kingdomName, new ArrayList<>());
        for (String siegeId : siegeIds) {
            for (Siege siege : activeSieges.values()) {
                if (siege.getSiegeId().equals(siegeId) && siege.isActive() && !siege.isExpired()) {
                    sieges.add(siege);
                }
            }
        }
        return sieges;
    }
    
    /**
     * Check and expire old sieges
     */
    public void checkExpiredSieges() {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Siege> entry : activeSieges.entrySet()) {
            Siege siege = entry.getValue();
            if (siege.isExpired() || !siege.isActive()) {
                siege.setActive(false);
                saveSiege(siege);
                toRemove.add(entry.getKey());
            }
        }
        for (String key : toRemove) {
            activeSieges.remove(key);
        }
    }
    
    private void broadcastSiegeStart(Siege siege) {
        Kingdom attacking = plugin.getKingdomManager().getKingdom(siege.getAttackingKingdom());
        Kingdom defending = plugin.getKingdomManager().getKingdom(siege.getDefendingKingdom());
        
        if (attacking != null) {
            String message = "§c§l⚔ Siege Started! §r§7Sieging chunk at (" + 
                siege.getChunkX() + ", " + siege.getChunkZ() + ")";
            broadcastToKingdom(attacking, message);
        }
        
        if (defending != null) {
            String message = "§c§l⚔ Under Siege! §r§7Chunk at (" + 
                siege.getChunkX() + ", " + siege.getChunkZ() + ") is under attack!";
            broadcastToKingdom(defending, message);
        }
    }
    
    private void broadcastSiegeComplete(Siege siege) {
        Kingdom attacking = plugin.getKingdomManager().getKingdom(siege.getAttackingKingdom());
        Kingdom defending = plugin.getKingdomManager().getKingdom(siege.getDefendingKingdom());
        
        if (attacking != null) {
            String message = "§a§l✓ Siege Complete! §r§7Captured chunk at (" + 
                siege.getChunkX() + ", " + siege.getChunkZ() + ")";
            broadcastToKingdom(attacking, message);
        }
        
        if (defending != null) {
            String message = "§c§l✗ Chunk Lost! §r§7Chunk at (" + 
                siege.getChunkX() + ", " + siege.getChunkZ() + ") was captured!";
            broadcastToKingdom(defending, message);
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
    
    private void saveSiege(Siege siege) {
        plugin.getStorageManager().getAdapter().saveSiege(
            siege.getSiegeId(),
            siege.getWarId(),
            siege.getAttackingKingdom(),
            siege.getDefendingKingdom(),
            siege.getWorldName(),
            siege.getChunkX(),
            siege.getChunkZ(),
            siege.getStartTime(),
            siege.getEndTime(),
            siege.getAttackProgress(),
            siege.isActive()
        );
    }
}

