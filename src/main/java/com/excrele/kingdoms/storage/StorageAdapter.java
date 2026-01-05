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
    default void savePlayerActivity(String player, String kingdomName, long lastLogin, long playtime, 
                                     long lastContribution, int contributions, int contributionStreak, long lastStreakDay) {
        // Default implementation calls the old method for backward compatibility
        savePlayerActivity(player, kingdomName, lastLogin, playtime);
    }
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
    
    // Achievement operations
    void savePlayerAchievement(String kingdomName, String playerName, String achievementId, String achievementName, 
                               String description, long unlockedAt, String unlockedBy, int progress, int target, boolean completed);
    List<Map<String, Object>> loadPlayerAchievements(String kingdomName, String playerName);
    
    // Mail operations
    void saveMail(String mailId, String recipient, String sender, String kingdomName, String subject, 
                  String message, long sentAt, boolean read, long readAt, boolean deleted);
    List<Map<String, Object>> loadPlayerMail(String playerName);
    void deleteMail(String mailId);
    
    // Siege operations
    void saveSiege(String siegeId, String warId, String attackingKingdom, String defendingKingdom,
                   String worldName, int chunkX, int chunkZ, long startTime, long endTime,
                   int attackProgress, boolean active);
    List<Map<String, Object>> loadActiveSieges();
    
    // Raid operations
    void saveRaid(String raidId, String raidingKingdom, String targetKingdom,
                  String worldName, int chunkX, int chunkZ, long startTime, long endTime,
                  int resourcesStolen, boolean active);
    List<Map<String, Object>> loadActiveRaids();
    
    // Tax operations
    void saveTaxSettings(String kingdomName, double taxRate, long taxInterval, long lastCollection);
    Map<String, Object> loadTaxSettings(String kingdomName);
    
    // Trade route operations
    void saveTradeRoute(String routeId, String kingdom1, String kingdom2,
                       String world1, double x1, double y1, double z1,
                       String world2, double x2, double y2, double z2,
                       long establishedAt, boolean active, double tradeVolume,
                       int tradeCount, long lastTradeTime);
    List<Map<String, Object>> loadTradeRoutes();
    
    // Advanced challenge operations
    void saveAdvancedChallenge(String challengeId, String name, String description, String type,
                              int difficulty, int xpReward, long startTime, long endTime,
                              int requiredMembers, String chainId, int chainOrder, boolean active);
    List<Map<String, Object>> loadAdvancedChallenges();
    
    // Kingdom structure operations
    void saveKingdomStructure(String structureId, String kingdomName, String type,
                             String worldName, double x, double y, double z,
                             long builtAt, int level, boolean active);
    List<Map<String, Object>> loadKingdomStructures();
    
    // Resource management operations
    void saveKingdomResource(String kingdomName, String resourceType, int amount);
    List<Map<String, Object>> loadKingdomResources();
    
    // Diplomacy operations
    void saveDiplomaticAgreement(String agreementId, String kingdom1, String kingdom2,
                                String type, long establishedAt, long expiresAt,
                                boolean active, String terms);
    List<Map<String, Object>> loadDiplomaticAgreements();
    
    // Theme operations
    void saveKingdomTheme(String kingdomName, String primaryColor, String secondaryColor,
                         String accentColor, String bannerMaterial, String flagMaterial,
                         String primaryParticle, String secondaryParticle, String themeName);
    List<Map<String, Object>> loadKingdomThemes();
    
    // Banner operations
    void saveKingdomBanner(String bannerId, String kingdomName, String worldName,
                          double x, double y, double z, String bannerMaterial);
    void deleteKingdomBanner(String bannerId);
    List<Map<String, Object>> loadKingdomBanners();
    
    // Initialize and cleanup
    void initialize();
    void close();
    boolean isConnected();
}

