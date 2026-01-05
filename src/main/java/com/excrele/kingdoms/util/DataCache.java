package com.excrele.kingdoms.util;

import com.excrele.kingdoms.model.Kingdom;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comprehensive caching system for frequently accessed data
 */
public class DataCache {
    private final Map<String, Kingdom> kingdomCache; // kingdomName -> Kingdom
    private final Map<String, String> playerKingdomCache; // playerName -> kingdomName
    private final Map<String, Double> bankBalanceCache; // kingdomName -> balance
    private final Map<String, Integer> kingdomLevelCache; // kingdomName -> level
    private final Map<String, Integer> kingdomXpCache; // kingdomName -> xp
    private final Map<String, Long> cacheTimestamps; // key -> last access time
    private final long cacheExpiryTime; // milliseconds
    
    public DataCache(long cacheExpiryTime) {
        this.kingdomCache = new ConcurrentHashMap<>();
        this.playerKingdomCache = new ConcurrentHashMap<>();
        this.bankBalanceCache = new ConcurrentHashMap<>();
        this.kingdomLevelCache = new ConcurrentHashMap<>();
        this.kingdomXpCache = new ConcurrentHashMap<>();
        this.cacheTimestamps = new ConcurrentHashMap<>();
        this.cacheExpiryTime = cacheExpiryTime;
    }
    
    /**
     * Cache kingdom data
     */
    public void cacheKingdom(Kingdom kingdom) {
        if (kingdom == null) return;
        String kingdomName = kingdom.getName();
        kingdomCache.put(kingdomName, kingdom);
        kingdomLevelCache.put(kingdomName, kingdom.getLevel());
        kingdomXpCache.put(kingdomName, kingdom.getXp());
        updateTimestamp("kingdom:" + kingdomName);
    }
    
    /**
     * Get cached kingdom
     */
    public Kingdom getCachedKingdom(String kingdomName) {
        if (isExpired("kingdom:" + kingdomName)) {
            kingdomCache.remove(kingdomName);
            return null;
        }
        return kingdomCache.get(kingdomName);
    }
    
    /**
     * Cache player kingdom mapping
     */
    public void cachePlayerKingdom(String playerName, String kingdomName) {
        playerKingdomCache.put(playerName, kingdomName);
        updateTimestamp("player:" + playerName);
    }
    
    /**
     * Get cached player kingdom
     */
    public String getCachedPlayerKingdom(String playerName) {
        if (isExpired("player:" + playerName)) {
            playerKingdomCache.remove(playerName);
            return null;
        }
        return playerKingdomCache.get(playerName);
    }
    
    /**
     * Cache bank balance
     */
    public void cacheBankBalance(String kingdomName, double balance) {
        bankBalanceCache.put(kingdomName, balance);
        updateTimestamp("bank:" + kingdomName);
    }
    
    /**
     * Get cached bank balance
     */
    public Double getCachedBankBalance(String kingdomName) {
        if (isExpired("bank:" + kingdomName)) {
            bankBalanceCache.remove(kingdomName);
            return null;
        }
        return bankBalanceCache.get(kingdomName);
    }
    
    /**
     * Invalidate kingdom cache
     */
    public void invalidateKingdom(String kingdomName) {
        kingdomCache.remove(kingdomName);
        kingdomLevelCache.remove(kingdomName);
        kingdomXpCache.remove(kingdomName);
        bankBalanceCache.remove(kingdomName);
        cacheTimestamps.remove("kingdom:" + kingdomName);
        cacheTimestamps.remove("bank:" + kingdomName);
    }
    
    /**
     * Invalidate player cache
     */
    public void invalidatePlayer(String playerName) {
        playerKingdomCache.remove(playerName);
        cacheTimestamps.remove("player:" + playerName);
    }
    
    /**
     * Clear all caches
     */
    public void clearAll() {
        kingdomCache.clear();
        playerKingdomCache.clear();
        bankBalanceCache.clear();
        kingdomLevelCache.clear();
        kingdomXpCache.clear();
        cacheTimestamps.clear();
    }
    
    /**
     * Clean expired cache entries
     */
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        List<String> toRemove = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : cacheTimestamps.entrySet()) {
            if (now - entry.getValue() > cacheExpiryTime) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (String key : toRemove) {
            cacheTimestamps.remove(key);
            if (key.startsWith("kingdom:")) {
                String kingdomName = key.substring(8);
                invalidateKingdom(kingdomName);
            } else if (key.startsWith("player:")) {
                String playerName = key.substring(7);
                invalidatePlayer(playerName);
            } else if (key.startsWith("bank:")) {
                String kingdomName = key.substring(5);
                bankBalanceCache.remove(kingdomName);
            }
        }
    }
    
    /**
     * Get cache statistics
     */
    public Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("kingdoms", kingdomCache.size());
        stats.put("players", playerKingdomCache.size());
        stats.put("balances", bankBalanceCache.size());
        stats.put("total", kingdomCache.size() + playerKingdomCache.size() + bankBalanceCache.size());
        return stats;
    }
    
    private void updateTimestamp(String key) {
        cacheTimestamps.put(key, System.currentTimeMillis());
    }
    
    private boolean isExpired(String key) {
        Long timestamp = cacheTimestamps.get(key);
        if (timestamp == null) return true;
        return System.currentTimeMillis() - timestamp > cacheExpiryTime;
    }
}

