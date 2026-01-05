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
            
            // Load economy settings
            if (plugin.getConfig().contains(path + ".economy_enabled")) {
                config.setEconomyEnabled(plugin.getConfig().getBoolean(path + ".economy_enabled"));
            }
            if (plugin.getConfig().contains(path + ".tax_rate")) {
                config.setTaxRate(plugin.getConfig().getDouble(path + ".tax_rate"));
            }
            if (plugin.getConfig().contains(path + ".unclaim_refund")) {
                config.setUnclaimRefund(plugin.getConfig().getDouble(path + ".unclaim_refund"));
            }
            
            // Load teleportation settings
            if (plugin.getConfig().contains(path + ".cross_world_teleport")) {
                config.setCrossWorldTeleportEnabled(plugin.getConfig().getBoolean(path + ".cross_world_teleport"));
            }
            if (plugin.getConfig().contains(path + ".allow_teleport_from")) {
                config.setAllowTeleportFrom(plugin.getConfig().getBoolean(path + ".allow_teleport_from"));
            }
            if (plugin.getConfig().contains(path + ".allow_teleport_to")) {
                config.setAllowTeleportTo(plugin.getConfig().getBoolean(path + ".allow_teleport_to"));
            }
            
            // Load leaderboard settings
            if (plugin.getConfig().contains(path + ".separate_leaderboards")) {
                config.setSeparateLeaderboards(plugin.getConfig().getBoolean(path + ".separate_leaderboards"));
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
    
    /**
     * Get tax rate for a world
     */
    public double getTaxRateForWorld(World world) {
        WorldConfig config = getWorldConfig(world.getName());
        if (config.getTaxRate() >= 0) {
            return config.getTaxRate();
        }
        // Use default from config
        return plugin.getConfig().getDouble("economy.tax_rate", 0.05);
    }
    
    /**
     * Get unclaim refund for a world
     */
    public double getUnclaimRefundForWorld(World world) {
        WorldConfig config = getWorldConfig(world.getName());
        if (config.getUnclaimRefund() >= 0) {
            return config.getUnclaimRefund();
        }
        // Use default from config
        return plugin.getConfig().getDouble("economy.unclaim_refund", 50.0);
    }
    
    /**
     * Check if economy is enabled in a world
     */
    public boolean isEconomyEnabled(World world) {
        WorldConfig config = getWorldConfig(world.getName());
        return config.isEconomyEnabled();
    }
    
    /**
     * Check if cross-world teleportation is allowed
     */
    public boolean isCrossWorldTeleportEnabled(World fromWorld, World toWorld) {
        WorldConfig fromConfig = getWorldConfig(fromWorld.getName());
        WorldConfig toConfig = getWorldConfig(toWorld.getName());
        
        // Both worlds must allow cross-world teleportation
        if (!fromConfig.isCrossWorldTeleportEnabled() || !toConfig.isCrossWorldTeleportEnabled()) {
            return false;
        }
        
        // Check if teleporting FROM the source world is allowed
        if (!fromConfig.isAllowTeleportFrom()) {
            return false;
        }
        
        // Check if teleporting TO the destination world is allowed
        if (!toConfig.isAllowTeleportTo()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if a world has separate leaderboards
     */
    public boolean hasSeparateLeaderboards(World world) {
        WorldConfig config = getWorldConfig(world.getName());
        return config.isSeparateLeaderboards();
    }
}

