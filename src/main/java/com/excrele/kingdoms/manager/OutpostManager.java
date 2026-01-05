package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.Outpost;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages outposts - remote claims with special features
 */
public class OutpostManager {
    private final KingdomsPlugin plugin;
    private final Map<String, Outpost> outposts; // outpost ID -> outpost
    private final Map<String, List<String>> kingdomOutposts; // kingdom -> outpost IDs
    private final double defaultMaintenanceCost;
    private final int minDistanceFromMainClaim; // Minimum chunks away from main claim
    
    public OutpostManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.outposts = new ConcurrentHashMap<>();
        this.kingdomOutposts = new ConcurrentHashMap<>();
        this.defaultMaintenanceCost = plugin.getConfig().getDouble("outposts.maintenance_cost", 100.0);
        this.minDistanceFromMainClaim = plugin.getConfig().getInt("outposts.min_distance", 10);
    }
    
    /**
     * Create an outpost at a chunk
     */
    public Outpost createOutpost(Kingdom kingdom, Chunk chunk, Location spawnLocation, String name) {
        // Check if chunk is far enough from main claim
        if (!isFarEnoughFromMainClaim(kingdom, chunk)) {
            return null;
        }
        
        // Check if chunk is already an outpost
        if (getOutpostByChunk(chunk) != null) {
            return null;
        }
        
        String outpostId = UUID.randomUUID().toString().substring(0, 8);
        Outpost outpost = new Outpost(outpostId, kingdom.getName(), chunk, spawnLocation);
        if (name != null && !name.isEmpty()) {
            outpost.setName(name);
        }
        outpost.setMaintenanceCost(defaultMaintenanceCost);
        
        outposts.put(outpostId, outpost);
        kingdomOutposts.computeIfAbsent(kingdom.getName(), k -> new ArrayList<>()).add(outpostId);
        
        return outpost;
    }
    
    /**
     * Delete an outpost
     */
    public boolean deleteOutpost(String outpostId) {
        Outpost outpost = outposts.get(outpostId);
        if (outpost == null) {
            return false;
        }
        
        // Remove from kingdom's outpost list
        List<String> kingdomOuts = kingdomOutposts.get(outpost.getKingdomName());
        if (kingdomOuts != null) {
            kingdomOuts.remove(outpostId);
        }
        
        // Remove connections from other outposts
        for (Outpost other : outposts.values()) {
            other.removeConnectedOutpost(outpostId);
        }
        
        outposts.remove(outpostId);
        return true;
    }
    
    /**
     * Get an outpost by ID
     */
    public Outpost getOutpost(String outpostId) {
        return outposts.get(outpostId);
    }
    
    /**
     * Get an outpost by chunk
     */
    public Outpost getOutpostByChunk(Chunk chunk) {
        for (Outpost outpost : outposts.values()) {
            if (outpost.getChunk().equals(chunk)) {
                return outpost;
            }
        }
        return null;
    }
    
    /**
     * Get all outposts for a kingdom
     */
    public List<Outpost> getKingdomOutposts(String kingdomName) {
        List<String> outpostIds = kingdomOutposts.get(kingdomName);
        if (outpostIds == null) {
            return new ArrayList<>();
        }
        
        List<Outpost> result = new ArrayList<>();
        for (String id : outpostIds) {
            Outpost outpost = outposts.get(id);
            if (outpost != null) {
                result.add(outpost);
            }
        }
        return result;
    }
    
    /**
     * Teleport a player to an outpost
     */
    public boolean teleportToOutpost(Player player, String outpostId) {
        Outpost outpost = outposts.get(outpostId);
        if (outpost == null || !outpost.isActive()) {
            return false;
        }
        
        // Check if player is in the kingdom
        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null || !kingdomName.equals(outpost.getKingdomName())) {
            return false;
        }
        
        // Check if maintenance is paid
        if (outpost.isMaintenanceOverdue()) {
            player.sendMessage("§cThis outpost is inactive due to unpaid maintenance!");
            return false;
        }
        
        player.teleport(outpost.getSpawnLocation());
        return true;
    }
    
    /**
     * Fast travel between two connected outposts
     */
    public boolean fastTravel(Player player, String fromOutpostId, String toOutpostId) {
        Outpost fromOutpost = outposts.get(fromOutpostId);
        Outpost toOutpost = outposts.get(toOutpostId);
        
        if (fromOutpost == null || toOutpost == null) {
            return false;
        }
        
        // Check if outposts are connected
        if (!fromOutpost.isConnectedTo(toOutpostId)) {
            return false;
        }
        
        // Check if player is in the kingdom
        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null || !kingdomName.equals(fromOutpost.getKingdomName()) || 
            !kingdomName.equals(toOutpost.getKingdomName())) {
            return false;
        }
        
        // Check if both outposts are active
        if (!fromOutpost.isActive() || !toOutpost.isActive()) {
            return false;
        }
        
        // Check maintenance
        if (fromOutpost.isMaintenanceOverdue() || toOutpost.isMaintenanceOverdue()) {
            player.sendMessage("§cOne or both outposts have unpaid maintenance!");
            return false;
        }
        
        player.teleport(toOutpost.getSpawnLocation());
        return true;
    }
    
    /**
     * Connect two outposts for fast travel
     */
    public boolean connectOutposts(String outpostId1, String outpostId2) {
        Outpost outpost1 = outposts.get(outpostId1);
        Outpost outpost2 = outposts.get(outpostId2);
        
        if (outpost1 == null || outpost2 == null) {
            return false;
        }
        
        // Outposts must be from the same kingdom
        if (!outpost1.getKingdomName().equals(outpost2.getKingdomName())) {
            return false;
        }
        
        outpost1.addConnectedOutpost(outpostId2);
        outpost2.addConnectedOutpost(outpostId1);
        
        return true;
    }
    
    /**
     * Pay maintenance for an outpost
     */
    public boolean payMaintenance(String outpostId, Player player) {
        Outpost outpost = outposts.get(outpostId);
        if (outpost == null) {
            return false;
        }
        
        // Check if player is in the kingdom
        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null || !kingdomName.equals(outpost.getKingdomName())) {
            return false;
        }
        
        // Check if maintenance is needed
        if (!outpost.isMaintenanceOverdue()) {
            player.sendMessage("§aMaintenance is already paid!");
            return false;
        }
        
        // Calculate cost (days overdue * daily cost)
        long daysOverdue = outpost.getDaysSinceMaintenance();
        double cost = outpost.getMaintenanceCost() * (daysOverdue + 1);
        
        // Check if player has enough money
        if (com.excrele.kingdoms.util.EconomyManager.isEnabled()) {
            if (!com.excrele.kingdoms.util.EconomyManager.hasEnough(player, cost)) {
                player.sendMessage("§cYou don't have enough money! Cost: " + 
                    com.excrele.kingdoms.util.EconomyManager.format(cost));
                return false;
            }
            
            com.excrele.kingdoms.util.EconomyManager.withdraw(player, cost);
            outpost.setLastMaintenancePaid(System.currentTimeMillis() / 1000);
            outpost.setActive(true);
            player.sendMessage("§aPaid maintenance: " + com.excrele.kingdoms.util.EconomyManager.format(cost));
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if a chunk is far enough from the main claim
     */
    private boolean isFarEnoughFromMainClaim(Kingdom kingdom, Chunk chunk) {
        List<List<org.bukkit.Chunk>> claims = kingdom.getClaims();
        if (claims.isEmpty()) {
            return true; // No main claim yet
        }
        
        // Check distance to first claim (main claim)
        List<org.bukkit.Chunk> mainClaim = claims.get(0);
        for (org.bukkit.Chunk mainChunk : mainClaim) {
            if (mainChunk.getWorld().equals(chunk.getWorld())) {
                int distance = Math.max(Math.abs(chunk.getX() - mainChunk.getX()), 
                                       Math.abs(chunk.getZ() - mainChunk.getZ()));
                if (distance < minDistanceFromMainClaim) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Get all outposts
     */
    public Collection<Outpost> getAllOutposts() {
        return outposts.values();
    }
}

