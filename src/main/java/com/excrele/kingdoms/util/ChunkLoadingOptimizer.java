package com.excrele.kingdoms.util;

import com.excrele.kingdoms.KingdomsPlugin;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimizes chunk loading and claim lookups for better performance
 */
public class ChunkLoadingOptimizer {
    private final KingdomsPlugin plugin;
    // world -> Set of loaded chunk keys
    private final Map<String, Set<String>> loadedChunks;
    // chunk key -> last access time
    private final Map<String, Long> chunkAccessTimes;
    // player -> Set of nearby chunks
    private final Map<String, Set<String>> playerNearbyChunks;
    
    public ChunkLoadingOptimizer(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.loadedChunks = new ConcurrentHashMap<>();
        this.chunkAccessTimes = new ConcurrentHashMap<>();
        this.playerNearbyChunks = new ConcurrentHashMap<>();
    }
    
    /**
     * Get kingdom for chunk with optimization
     */
    public com.excrele.kingdoms.model.Kingdom getKingdomByChunkOptimized(Chunk chunk) {
        String chunkKey = getChunkKey(chunk);
        
        // Update access time
        chunkAccessTimes.put(chunkKey, System.currentTimeMillis());
        
        // Use existing cache from KingdomManager
        return plugin.getKingdomManager().getKingdomByChunk(chunk);
    }
    
    /**
     * Preload chunks around player
     */
    public void preloadChunksAroundPlayer(Player player, int radius) {
        Chunk centerChunk = player.getLocation().getChunk();
        World world = centerChunk.getWorld();
        String worldName = world.getName();
        
        Set<String> nearbyChunks = new HashSet<>();
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int chunkX = centerChunk.getX() + x;
                int chunkZ = centerChunk.getZ() + z;
                
                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                String chunkKey = getChunkKey(chunk);
                nearbyChunks.add(chunkKey);
                
                // Mark as loaded
                loadedChunks.computeIfAbsent(worldName, k -> ConcurrentHashMap.newKeySet()).add(chunkKey);
                
                // Preload claim data if chunk is claimed
                com.excrele.kingdoms.model.Kingdom kingdom = 
                    plugin.getKingdomManager().getKingdomByChunk(chunk);
                if (kingdom != null) {
                    // Cache is already updated by getKingdomByChunk
                }
            }
        }
        
        playerNearbyChunks.put(player.getName(), nearbyChunks);
    }
    
    /**
     * Unload chunks that are far from all players
     */
    public void unloadDistantChunks() {
        long now = System.currentTimeMillis();
        long unloadDelay = plugin.getConfig().getLong("chunk-optimization.unload-delay", 300000L); // 5 minutes
        
        // Get all currently online players' nearby chunks
        Set<String> activeChunks = new HashSet<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Set<String> nearby = playerNearbyChunks.get(player.getName());
            if (nearby != null) {
                activeChunks.addAll(nearby);
            }
        }
        
        // Unload chunks that haven't been accessed recently and aren't near players
        List<String> toUnload = new ArrayList<>();
        for (Map.Entry<String, Long> entry : chunkAccessTimes.entrySet()) {
            String chunkKey = entry.getKey();
            long lastAccess = entry.getValue();
            
            if (!activeChunks.contains(chunkKey) && (now - lastAccess > unloadDelay)) {
                toUnload.add(chunkKey);
            }
        }
        
        // Remove from tracking (actual chunk unloading is handled by Minecraft)
        for (String chunkKey : toUnload) {
            chunkAccessTimes.remove(chunkKey);
            String[] parts = chunkKey.split(":");
            if (parts.length == 3) {
                String worldName = parts[0];
                Set<String> worldChunks = loadedChunks.get(worldName);
                if (worldChunks != null) {
                    worldChunks.remove(chunkKey);
                }
            }
        }
    }
    
    /**
     * Batch load claim data for multiple chunks
     */
    public void batchLoadClaims(Collection<Chunk> chunks) {
        // Pre-load all chunks into cache
        for (Chunk chunk : chunks) {
            plugin.getKingdomManager().getKingdomByChunk(chunk);
        }
    }
    
    /**
     * Get optimized claim lookup (with caching)
     */
    public com.excrele.kingdoms.model.Kingdom getClaimOptimized(String worldName, int chunkX, int chunkZ) {
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) return null;
        
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        return getKingdomByChunkOptimized(chunk);
    }
    
    /**
     * Clean up player data when they leave
     */
    public void cleanupPlayer(String playerName) {
        playerNearbyChunks.remove(playerName);
    }
    
    /**
     * Get chunk key
     */
    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
    
    /**
     * Get performance statistics
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("trackedChunks", chunkAccessTimes.size());
        stats.put("loadedChunks", loadedChunks.values().stream().mapToInt(Set::size).sum());
        stats.put("activePlayers", playerNearbyChunks.size());
        return stats;
    }
}

