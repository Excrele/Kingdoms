package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.WorldConfig;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages world-specific settings and rules
 */
public class WorldManager {
    private final KingdomsPlugin plugin;
    private final Map<String, WorldConfig> worldConfigs; // worldName -> config
    private final List<String> blacklistedWorlds;
    
    public WorldManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.worldConfigs = new HashMap<>();
        this.blacklistedWorlds = plugin.getConfig().getStringList("worlds.blacklist");
        loadWorldConfigs();
    }
    
    /**
     * Load world configurations from config
     */
    private void loadWorldConfigs() {
        if (!plugin.getConfig().contains("worlds.settings")) {
            return;
        }
        
        for (String worldName : plugin.getConfig().getConfigurationSection("worlds.settings").getKeys(false)) {
            WorldConfig config = new WorldConfig(worldName);
            String path = "worlds.settings." + worldName;
            
            if (plugin.getConfig().contains(path + ".max_claims")) {
                config.setMaxClaims(plugin.getConfig().getInt(path + ".max_claims"));
            }
            if (plugin.getConfig().contains(path + ".claim_cost")) {
                config.setClaimCost(plugin.getConfig().getDouble(path + ".claim_cost"));
            }
            if (plugin.getConfig().contains(path + ".claiming_enabled")) {
                config.setClaimingEnabled(plugin.getConfig().getBoolean(path + ".claiming_enabled"));
            }
            if (plugin.getConfig().contains(path + ".buffer_zone")) {
                config.setBufferZone(plugin.getConfig().getInt(path + ".buffer_zone"));
            }
            
            worldConfigs.put(worldName, config);
        }
    }
    
    /**
     * Get world config, creating default if not exists
     */
    public WorldConfig getWorldConfig(String worldName) {
        return worldConfigs.computeIfAbsent(worldName, WorldConfig::new);
    }
    
    /**
     * Check if world is blacklisted
     */
    public boolean isWorldBlacklisted(String worldName) {
        return blacklistedWorlds.contains(worldName);
    }
    
    /**
     * Check if claiming is enabled in a world
     */
    public boolean isClaimingEnabled(World world) {
        if (isWorldBlacklisted(world.getName())) {
            return false;
        }
        WorldConfig config = getWorldConfig(world.getName());
        return config.isClaimingEnabled();
    }
    
    /**
     * Get max claims for a kingdom in a specific world
     */
    public int getMaxClaimsForWorld(Kingdom kingdom, World world) {
        WorldConfig config = getWorldConfig(world.getName());
        if (config.getMaxClaims() > 0) {
            return config.getMaxClaims();
        }
        // Use default kingdom limit
        return kingdom.getMaxClaimChunks();
    }
    
    /**
     * Get claim cost for a world
     */
    public double getClaimCostForWorld(World world) {
        WorldConfig config = getWorldConfig(world.getName());
        if (config.getClaimCost() >= 0) {
            return config.getClaimCost();
        }
        // Use default from config
        return plugin.getConfig().getDouble("economy.claim_cost", 100.0);
    }
    
    /**
     * Get buffer zone for a world
     */
    public int getBufferZoneForWorld(World world) {
        WorldConfig config = getWorldConfig(world.getName());
        return config.getBufferZone();
    }
    
    /**
     * Get number of claims a kingdom has in a specific world
     */
    public int getClaimsInWorld(Kingdom kingdom, World world) {
        int count = 0;
        for (List<Chunk> claim : kingdom.getClaims()) {
            for (Chunk chunk : claim) {
                if (chunk.getWorld().equals(world)) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Check if kingdom can claim more chunks in this world
     */
    public boolean canClaimInWorld(Kingdom kingdom, World world) {
        if (!isClaimingEnabled(world)) {
            return false;
        }
        int currentClaims = getClaimsInWorld(kingdom, world);
        int maxClaims = getMaxClaimsForWorld(kingdom, world);
        return currentClaims < maxClaims;
    }
}

