package com.excrele.kingdoms.util;

import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU cache for claim lookups to improve performance
 */
public class ClaimCache {
    private final Map<String, Kingdom> cache;
    private final int maxSize;
    
    public ClaimCache(int maxSize) {
        this.maxSize = maxSize;
        // LinkedHashMap with access order for LRU behavior
        this.cache = new LinkedHashMap<String, Kingdom>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Kingdom> eldest) {
                return size() > ClaimCache.this.maxSize;
            }
        };
    }
    
    /**
     * Get kingdom for a chunk from cache
     */
    public Kingdom get(Chunk chunk) {
        String key = getChunkKey(chunk);
        return cache.get(key);
    }
    
    /**
     * Put a chunk-kingdom mapping in cache
     */
    public void put(Chunk chunk, Kingdom kingdom) {
        String key = getChunkKey(chunk);
        cache.put(key, kingdom);
    }
    
    /**
     * Remove a chunk from cache
     */
    public void remove(Chunk chunk) {
        String key = getChunkKey(chunk);
        cache.remove(key);
    }
    
    /**
     * Clear the entire cache
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * Invalidate cache for all chunks in a world
     */
    public void invalidateWorld(String worldName) {
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(worldName + ":"));
    }
    
    /**
     * Get cache size
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * Generate chunk key for caching
     */
    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
}

