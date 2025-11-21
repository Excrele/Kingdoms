package com.excrele.kingdoms.storage;

import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Abstract storage adapter interface for different storage backends
 */
public interface StorageAdapter {
    // Kingdom operations
    void saveKingdom(Kingdom kingdom);
    Kingdom loadKingdom(String name);
    List<String> loadAllKingdomNames();
    void deleteKingdom(String name);
    
    // Claim operations
    void saveClaim(String kingdomName, Chunk chunk, String claimId);
    void deleteClaim(String kingdomName, Chunk chunk);
    Map<String, String> loadAllClaims(); // chunkKey -> kingdomName
    
    // Trust operations
    void saveTrust(String kingdomName, String player, String permission);
    void deleteTrust(String kingdomName, String player);
    Map<String, List<String>> loadTrusts(String kingdomName); // player -> permissions
    
    // War operations
    void saveWar(String warId, String kingdom1, String kingdom2, long startTime, long endTime, boolean active);
    Map<String, Object> loadWar(String warId);
    List<Map<String, Object>> loadActiveWars();
    
    // Bank operations
    void saveBankBalance(String kingdomName, double balance);
    double loadBankBalance(String kingdomName);
    
    // Activity tracking
    void savePlayerActivity(String player, String kingdomName, long lastLogin, long playtime);
    Map<String, Object> loadPlayerActivity(String player);
    List<Map<String, Object>> loadMemberHistory(String kingdomName); // member join/leave history
    
    // Vault operations
    void saveVault(String kingdomName, org.bukkit.inventory.Inventory vault);
    Map<Integer, ItemStack> loadVault(String kingdomName); // slot -> item
    
    // Advanced Features operations
    void saveWaypoint(String kingdomName, String waypointName, org.bukkit.Location location, String createdBy);
    void deleteWaypoint(String kingdomName, String waypointName);
    Map<String, Map<String, Object>> loadWaypoints(String kingdomName); // waypointName -> data
    
    void saveFarm(String kingdomName, String farmName, String farmType, org.bukkit.Chunk chunk, org.bukkit.Location center, long lastHarvest, boolean isActive);
    void deleteFarm(String kingdomName, String farmName);
    Map<String, Map<String, Object>> loadFarms(String kingdomName); // farmName -> data
    
    void saveWorkshop(String kingdomName, String workshopName, String workshopType, org.bukkit.Chunk chunk, org.bukkit.Location location, double bonusMultiplier, boolean isActive);
    void deleteWorkshop(String kingdomName, String workshopName);
    Map<String, Map<String, Object>> loadWorkshops(String kingdomName); // workshopName -> data
    
    void saveLibrary(String kingdomName, String libraryName, org.bukkit.Chunk chunk, org.bukkit.Location location, int maxBooks, boolean isPublic);
    void deleteLibrary(String kingdomName, String libraryName);
    Map<String, Map<String, Object>> loadLibraries(String kingdomName); // libraryName -> data
    
    void saveStable(String kingdomName, String stableName, org.bukkit.Chunk chunk, org.bukkit.Location location, int maxMounts, boolean isPublic);
    void deleteStable(String kingdomName, String stableName);
    Map<String, Map<String, Object>> loadStables(String kingdomName); // stableName -> data
    
    // Statistics operations
    void saveClaimAnalytics(String chunkKey, String kingdomName, long claimedAt, long lastActivity, int playerVisits, int blockInteractions, int entityInteractions, double estimatedValue);
    Map<String, Object> loadClaimAnalytics(String chunkKey);
    
    void saveKingdomHistory(String kingdomName, long timestamp, String type, String description, String actor);
    List<Map<String, Object>> loadKingdomHistory(String kingdomName);
    
    void saveGrowthData(String kingdomName, long timestamp, int level, int xp, int claims, int members, int alliances);
    List<Map<String, Object>> loadGrowthData(String kingdomName);
    
    // Initialize and cleanup
    void initialize();
    void close();
    boolean isConnected();
}

