package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages kingdom resources, storage, and trading
 */
public class ResourceManager {
    private final KingdomsPlugin plugin;
    // kingdom -> resourceType -> amount
    private final Map<String, Map<String, Integer>> kingdomResources;
    // kingdom -> storage capacity
    private final Map<String, Integer> storageCapacity;
    
    public ResourceManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.kingdomResources = new ConcurrentHashMap<>();
        this.storageCapacity = new ConcurrentHashMap<>();
        loadAllResources();
    }
    
    private void loadAllResources() {
        List<Map<String, Object>> resources = plugin.getStorageManager().getAdapter().loadKingdomResources();
        for (Map<String, Object> data : resources) {
            String kingdomName = (String) data.get("kingdomName");
            String resourceType = (String) data.get("resourceType");
            int amount = ((Number) data.get("amount")).intValue();
            
            kingdomResources.computeIfAbsent(kingdomName, k -> new ConcurrentHashMap<>())
                .put(resourceType, amount);
        }
        
        // Initialize storage capacity
        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            updateStorageCapacity(kingdom.getName());
        }
    }
    
    /**
     * Update storage capacity based on kingdom level and structures
     */
    private void updateStorageCapacity(String kingdomName) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        
        int baseCapacity = 1000; // Base storage
        int levelBonus = kingdom.getLevel() * 100; // 100 per level
        int structureBonus = 0;
        
        // Check for granary structure
        if (plugin.getStructureManager() != null) {
            if (plugin.getStructureManager().hasStructure(kingdomName, 
                com.excrele.kingdoms.model.KingdomStructure.StructureType.GRANARY)) {
                structureBonus = 500; // Granary adds 500 capacity
            }
        }
        
        storageCapacity.put(kingdomName, baseCapacity + levelBonus + structureBonus);
    }
    
    /**
     * Deposit resource to kingdom storage
     */
    public boolean depositResource(String kingdomName, Material material, int amount, Player depositor) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        // Check if player is member
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(depositor.getName());
        if (!kingdomName.equals(playerKingdom)) {
            return false;
        }
        
        // Check storage capacity
        int currentTotal = getTotalResources(kingdomName);
        if (currentTotal + amount > getStorageCapacity(kingdomName)) {
            return false; // Exceeds capacity
        }
        
        // Add resource
        String resourceType = material.name();
        kingdomResources.computeIfAbsent(kingdomName, k -> new ConcurrentHashMap<>())
            .put(resourceType, getResourceAmount(kingdomName, resourceType) + amount);
        
        saveResource(kingdomName, resourceType);
        
        depositor.sendMessage("§6[Resources] Deposited §e" + amount + " §6" + material.name());
        return true;
    }
    
    /**
     * Withdraw resource from kingdom storage
     */
    public boolean withdrawResource(String kingdomName, Material material, int amount, Player withdrawer) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        // Check if player is member
        String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(withdrawer.getName());
        if (!kingdomName.equals(playerKingdom)) {
            return false;
        }
        
        String resourceType = material.name();
        int currentAmount = getResourceAmount(kingdomName, resourceType);
        if (currentAmount < amount) {
            return false; // Not enough resources
        }
        
        // Remove resource
        kingdomResources.computeIfAbsent(kingdomName, k -> new ConcurrentHashMap<>())
            .put(resourceType, currentAmount - amount);
        
        saveResource(kingdomName, resourceType);
        
        // Give item to player
        ItemStack item = new ItemStack(material, amount);
        withdrawer.getInventory().addItem(item);
        
        withdrawer.sendMessage("§6[Resources] Withdrew §e" + amount + " §6" + material.name());
        return true;
    }
    
    /**
     * Trade resources between kingdoms
     */
    public boolean tradeResources(String fromKingdom, String toKingdom, 
                                 Material material, int amount, double price) {
        // Check if kingdoms have trade route
        if (plugin.getTradeRouteManager() != null) {
            if (!plugin.getTradeRouteManager().hasRoute(fromKingdom, toKingdom)) {
                return false; // No trade route
            }
        }
        
        // Check if from kingdom has resources
        int available = getResourceAmount(fromKingdom, material.name());
        if (available < amount) {
            return false;
        }
        
        // Check if to kingdom has enough money
        double toBalance = plugin.getBankManager().getBalance(toKingdom);
        if (toBalance < price) {
            return false;
        }
        
        // Execute trade
        kingdomResources.computeIfAbsent(fromKingdom, k -> new ConcurrentHashMap<>())
            .put(material.name(), available - amount);
        kingdomResources.computeIfAbsent(toKingdom, k -> new ConcurrentHashMap<>())
            .put(material.name(), getResourceAmount(toKingdom, material.name()) + amount);
        
        plugin.getBankManager().withdraw(toKingdom, price);
        plugin.getBankManager().deposit(fromKingdom, price);
        
        saveResource(fromKingdom, material.name());
        saveResource(toKingdom, material.name());
        
        // Notify kingdoms
        Kingdom fromK = plugin.getKingdomManager().getKingdom(fromKingdom);
        Kingdom toK = plugin.getKingdomManager().getKingdom(toKingdom);
        
        if (fromK != null) {
            String message = "§6[Trade] Sold §e" + amount + " §6" + material.name() + 
                " §6to " + toKingdom + " for §e" + String.format("%.2f", price);
            broadcastToKingdom(fromK, message);
        }
        
        if (toK != null) {
            String message = "§6[Trade] Purchased §e" + amount + " §6" + material.name() + 
                " §6from " + fromKingdom + " for §e" + String.format("%.2f", price);
            broadcastToKingdom(toK, message);
        }
        
        return true;
    }
    
    /**
     * Get resource amount for a kingdom
     */
    public int getResourceAmount(String kingdomName, String resourceType) {
        Map<String, Integer> resources = kingdomResources.get(kingdomName);
        if (resources == null) return 0;
        return resources.getOrDefault(resourceType, 0);
    }
    
    /**
     * Get total resources (for capacity checking)
     */
    public int getTotalResources(String kingdomName) {
        Map<String, Integer> resources = kingdomResources.get(kingdomName);
        if (resources == null) return 0;
        return resources.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Get storage capacity
     */
    public int getStorageCapacity(String kingdomName) {
        return storageCapacity.getOrDefault(kingdomName, 1000);
    }
    
    /**
     * Get all resources for a kingdom
     */
    public Map<String, Integer> getKingdomResources(String kingdomName) {
        return new HashMap<>(kingdomResources.getOrDefault(kingdomName, new ConcurrentHashMap<>()));
    }
    
    /**
     * Check resource requirements for actions
     */
    public boolean hasResources(String kingdomName, Map<String, Integer> requirements) {
        Map<String, Integer> resources = kingdomResources.get(kingdomName);
        if (resources == null) return false;
        
        for (Map.Entry<String, Integer> requirement : requirements.entrySet()) {
            int available = resources.getOrDefault(requirement.getKey(), 0);
            if (available < requirement.getValue()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Consume resources (for requirements)
     */
    public boolean consumeResources(String kingdomName, Map<String, Integer> requirements) {
        if (!hasResources(kingdomName, requirements)) {
            return false;
        }
        
        Map<String, Integer> resources = kingdomResources.computeIfAbsent(kingdomName, 
            k -> new ConcurrentHashMap<>());
        
        for (Map.Entry<String, Integer> requirement : requirements.entrySet()) {
            String resourceType = requirement.getKey();
            int amount = requirement.getValue();
            int current = resources.getOrDefault(resourceType, 0);
            resources.put(resourceType, current - amount);
            saveResource(kingdomName, resourceType);
        }
        
        return true;
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
    
    private void saveResource(String kingdomName, String resourceType) {
        int amount = getResourceAmount(kingdomName, resourceType);
        plugin.getStorageManager().getAdapter().saveKingdomResource(
            kingdomName, resourceType, amount
        );
    }
}

