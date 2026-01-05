package com.excrele.kingdoms.api;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.api.hook.HookManager;
import com.excrele.kingdoms.manager.*;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;

import java.util.List;

/**
 * Public API for the Kingdoms plugin.
 * 
 * <p>This API provides access to kingdom management functionality for other plugins.
 * All methods are thread-safe and can be called from async contexts unless otherwise noted.</p>
 * 
 * <p><b>API Version:</b> 1.0.0</p>
 * 
 * @since 1.6
 * @version 1.0.0
 */
public class KingdomsAPI {
    private static KingdomsAPI instance;
    private final KingdomsPlugin plugin;
    private final HookManager hookManager;
    private static final String API_VERSION = "1.0.0";
    
    private KingdomsAPI(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.hookManager = new HookManager(plugin);
    }
    
    /**
     * Initialize the API instance.
     * This is called automatically by the plugin.
     * 
     * @param plugin The plugin instance
     */
    public static void initialize(KingdomsPlugin plugin) {
        if (instance == null) {
            instance = new KingdomsAPI(plugin);
        }
    }
    
    /**
     * Get the API instance.
     * 
     * @return The API instance, or null if not initialized
     */
    public static KingdomsAPI getInstance() {
        return instance;
    }
    
    /**
     * Get the API version.
     * 
     * @return The API version string
     */
    public static String getAPIVersion() {
        return API_VERSION;
    }
    
    /**
     * Get the hook manager for extensibility.
     * 
     * @return The hook manager
     */
    public HookManager getHookManager() {
        return hookManager;
    }
    
    /**
     * Get a kingdom by name.
     * 
     * @param name The kingdom name
     * @return The kingdom, or null if not found
     */
    public Kingdom getKingdom(String name) {
        if (plugin == null || plugin.getKingdomManager() == null) {
            return null;
        }
        return plugin.getKingdomManager().getKingdom(name);
    }
    
    /**
     * Get a kingdom by player.
     * 
     * @param player The player
     * @return The kingdom the player belongs to, or null if not in a kingdom
     */
    public Kingdom getKingdom(OfflinePlayer player) {
        if (plugin == null || plugin.getKingdomManager() == null) {
            return null;
        }
        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        return kingdomName != null ? plugin.getKingdomManager().getKingdom(kingdomName) : null;
    }
    
    /**
     * Get a kingdom by player name.
     * 
     * @param playerName The player name
     * @return The kingdom the player belongs to, or null if not in a kingdom
     */
    public Kingdom getKingdomByPlayer(String playerName) {
        if (plugin == null || plugin.getKingdomManager() == null) {
            return null;
        }
        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(playerName);
        return kingdomName != null ? plugin.getKingdomManager().getKingdom(kingdomName) : null;
    }
    
    /**
     * Get a kingdom by chunk.
     * 
     * @param chunk The chunk
     * @return The kingdom that owns the chunk, or null if unclaimed
     */
    public Kingdom getKingdomByChunk(Chunk chunk) {
        if (plugin == null || plugin.getKingdomManager() == null) {
            return null;
        }
        return plugin.getKingdomManager().getKingdomByChunk(chunk);
    }
    
    /**
     * Get all kingdoms.
     * 
     * @return A list of all kingdoms
     */
    public List<Kingdom> getKingdoms() {
        if (plugin == null || plugin.getKingdomManager() == null) {
            return List.of();
        }
        return List.copyOf(plugin.getKingdomManager().getKingdoms().values());
    }
    
    /**
     * Check if a chunk is claimed.
     * 
     * @param chunk The chunk to check
     * @return True if the chunk is claimed
     */
    public boolean isChunkClaimed(Chunk chunk) {
        if (plugin == null || plugin.getClaimManager() == null) {
            return false;
        }
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        return plugin.getKingdomManager().getClaimedChunks().containsKey(key);
    }
    
    /**
     * Check if a player is in a kingdom.
     * 
     * @param player The player
     * @return True if the player is in a kingdom
     */
    public boolean isPlayerInKingdom(OfflinePlayer player) {
        return getKingdom(player) != null;
    }
    
    /**
     * Check if a player is in a kingdom.
     * 
     * @param playerName The player name
     * @return True if the player is in a kingdom
     */
    public boolean isPlayerInKingdom(String playerName) {
        return getKingdomByPlayer(playerName) != null;
    }
    
    /**
     * Get the kingdom manager.
     * 
     * @return The kingdom manager
     */
    public KingdomManager getKingdomManager() {
        return plugin != null ? plugin.getKingdomManager() : null;
    }
    
    /**
     * Get the claim manager.
     * 
     * @return The claim manager
     */
    public ClaimManager getClaimManager() {
        return plugin != null ? plugin.getClaimManager() : null;
    }
    
    /**
     * Get the war manager.
     * 
     * @return The war manager
     */
    public WarManager getWarManager() {
        return plugin != null ? plugin.getWarManager() : null;
    }
    
    /**
     * Get the bank manager.
     * 
     * @return The bank manager
     */
    public BankManager getBankManager() {
        return plugin != null ? plugin.getBankManager() : null;
    }
    
    /**
     * Get the challenge manager.
     * 
     * @return The challenge manager
     */
    public ChallengeManager getChallengeManager() {
        return plugin != null ? plugin.getChallengeManager() : null;
    }
    
    /**
     * Get the alliance manager (via kingdom manager).
     * 
     * @return The alliance manager functionality
     */
    public boolean areAllied(String kingdom1, String kingdom2) {
        Kingdom k1 = getKingdom(kingdom1);
        Kingdom k2 = getKingdom(kingdom2);
        if (k1 == null || k2 == null) {
            return false;
        }
        return k1.isAllied(kingdom2) || k2.isAllied(kingdom1);
    }
}

