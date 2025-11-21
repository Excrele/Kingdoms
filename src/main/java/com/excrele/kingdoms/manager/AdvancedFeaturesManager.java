package com.excrele.kingdoms.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.KingdomFarm;
import com.excrele.kingdoms.model.KingdomLibrary;
import com.excrele.kingdoms.model.KingdomStable;
import com.excrele.kingdoms.model.KingdomWorkshop;
import com.excrele.kingdoms.model.Waypoint;

/**
 * Manages advanced features: auto-claim, waypoints, farms, workshops, libraries, stables
 */
public class AdvancedFeaturesManager {
    private final KingdomsPlugin plugin;
    
    // Auto-claim mode
    private final Set<String> autoClaimEnabled; // Player names with auto-claim enabled
    private final Map<String, Long> lastAutoClaim; // Player -> last claim timestamp
    
    // Waypoints: kingdomName -> waypointName -> Waypoint
    private final Map<String, Map<String, Waypoint>> waypoints;
    
    // Farms: kingdomName -> farmName -> KingdomFarm
    private final Map<String, Map<String, KingdomFarm>> farms;
    
    // Workshops: kingdomName -> workshopName -> KingdomWorkshop
    private final Map<String, Map<String, KingdomWorkshop>> workshops;
    
    // Libraries: kingdomName -> libraryName -> KingdomLibrary
    private final Map<String, Map<String, KingdomLibrary>> libraries;
    
    // Stables: kingdomName -> stableName -> KingdomStable
    private final Map<String, Map<String, KingdomStable>> stables;
    
    public AdvancedFeaturesManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.autoClaimEnabled = new HashSet<>();
        this.lastAutoClaim = new HashMap<>();
        this.waypoints = new HashMap<>();
        this.farms = new HashMap<>();
        this.workshops = new HashMap<>();
        this.libraries = new HashMap<>();
        this.stables = new HashMap<>();
        loadAllData();
    }
    
    private void loadAllData() {
        // Load waypoints, farms, workshops, libraries, stables from storage
        for (String kingdomName : plugin.getKingdomManager().getKingdoms().keySet()) {
            loadWaypoints(kingdomName);
            loadFarms(kingdomName);
            loadWorkshops(kingdomName);
            loadLibraries(kingdomName);
            loadStables(kingdomName);
        }
    }
    
    private void loadWaypoints(String kingdomName) {
        java.util.Map<String, java.util.Map<String, Object>> waypointData = 
            plugin.getStorageManager().getAdapter().loadWaypoints(kingdomName);
        if (waypointData != null) {
            for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : waypointData.entrySet()) {
                java.util.Map<String, Object> data = entry.getValue();
                org.bukkit.World world = plugin.getServer().getWorld((String) data.get("world"));
                if (world != null) {
                    org.bukkit.Location loc = new org.bukkit.Location(
                        world,
                        (Double) data.get("x"),
                        (Double) data.get("y"),
                        (Double) data.get("z"),
                        ((Number) data.get("yaw")).floatValue(),
                        ((Number) data.get("pitch")).floatValue()
                    );
                    waypoints.computeIfAbsent(kingdomName, k -> new HashMap<>())
                        .put(entry.getKey(), new Waypoint(entry.getKey(), kingdomName, loc, (String) data.get("createdBy")));
                }
            }
        }
    }
    
    private void loadFarms(String kingdomName) {
        java.util.Map<String, java.util.Map<String, Object>> farmData = 
            plugin.getStorageManager().getAdapter().loadFarms(kingdomName);
        if (farmData != null) {
            for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : farmData.entrySet()) {
                java.util.Map<String, Object> data = entry.getValue();
                org.bukkit.World world = plugin.getServer().getWorld((String) data.get("world"));
                if (world != null) {
                    Chunk chunk = world.getChunkAt((Integer) data.get("chunkX"), (Integer) data.get("chunkZ"));
                    org.bukkit.Location center = new org.bukkit.Location(
                        world,
                        (Double) data.get("centerX"),
                        (Double) data.get("centerY"),
                        (Double) data.get("centerZ")
                    );
                    try {
                        KingdomFarm.FarmType type = KingdomFarm.FarmType.valueOf((String) data.get("type"));
                        KingdomFarm farm = new KingdomFarm(entry.getKey(), kingdomName, chunk, center, type);
                        farm.setLastHarvest((Long) data.get("lastHarvest"));
                        farm.setActive((Boolean) data.get("isActive"));
                        farms.computeIfAbsent(kingdomName, k -> new HashMap<>()).put(entry.getKey(), farm);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid farm type for " + entry.getKey() + ": " + data.get("type"));
                    }
                }
            }
        }
    }
    
    private void loadWorkshops(String kingdomName) {
        java.util.Map<String, java.util.Map<String, Object>> workshopData = 
            plugin.getStorageManager().getAdapter().loadWorkshops(kingdomName);
        if (workshopData != null) {
            for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : workshopData.entrySet()) {
                java.util.Map<String, Object> data = entry.getValue();
                org.bukkit.World world = plugin.getServer().getWorld((String) data.get("world"));
                if (world != null) {
                    Chunk chunk = world.getChunkAt((Integer) data.get("chunkX"), (Integer) data.get("chunkZ"));
                    org.bukkit.Location loc = new org.bukkit.Location(
                        world,
                        (Double) data.get("x"),
                        (Double) data.get("y"),
                        (Double) data.get("z")
                    );
                    try {
                        KingdomWorkshop.WorkshopType type = KingdomWorkshop.WorkshopType.valueOf((String) data.get("type"));
                        KingdomWorkshop workshop = new KingdomWorkshop(entry.getKey(), kingdomName, chunk, loc, type);
                        workshop.setBonusMultiplier(((Number) data.get("bonusMultiplier")).doubleValue());
                        workshop.setActive((Boolean) data.get("isActive"));
                        workshops.computeIfAbsent(kingdomName, k -> new HashMap<>()).put(entry.getKey(), workshop);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid workshop type for " + entry.getKey() + ": " + data.get("type"));
                    }
                }
            }
        }
    }
    
    private void loadLibraries(String kingdomName) {
        java.util.Map<String, java.util.Map<String, Object>> libraryData = 
            plugin.getStorageManager().getAdapter().loadLibraries(kingdomName);
        if (libraryData != null) {
            for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : libraryData.entrySet()) {
                java.util.Map<String, Object> data = entry.getValue();
                org.bukkit.World world = plugin.getServer().getWorld((String) data.get("world"));
                if (world != null) {
                    Chunk chunk = world.getChunkAt((Integer) data.get("chunkX"), (Integer) data.get("chunkZ"));
                    org.bukkit.Location loc = new org.bukkit.Location(
                        world,
                        (Double) data.get("x"),
                        (Double) data.get("y"),
                        (Double) data.get("z")
                    );
                    KingdomLibrary library = new KingdomLibrary(entry.getKey(), kingdomName, chunk, loc);
                    library.setMaxBooks((Integer) data.get("maxBooks"));
                    library.setPublic((Boolean) data.get("isPublic"));
                    libraries.computeIfAbsent(kingdomName, k -> new HashMap<>()).put(entry.getKey(), library);
                }
            }
        }
    }
    
    private void loadStables(String kingdomName) {
        java.util.Map<String, java.util.Map<String, Object>> stableData = 
            plugin.getStorageManager().getAdapter().loadStables(kingdomName);
        if (stableData != null) {
            for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : stableData.entrySet()) {
                java.util.Map<String, Object> data = entry.getValue();
                org.bukkit.World world = plugin.getServer().getWorld((String) data.get("world"));
                if (world != null) {
                    Chunk chunk = world.getChunkAt((Integer) data.get("chunkX"), (Integer) data.get("chunkZ"));
                    org.bukkit.Location loc = new org.bukkit.Location(
                        world,
                        (Double) data.get("x"),
                        (Double) data.get("y"),
                        (Double) data.get("z")
                    );
                    KingdomStable stable = new KingdomStable(entry.getKey(), kingdomName, chunk, loc);
                    stable.setMaxMounts((Integer) data.get("maxMounts"));
                    stable.setPublic((Boolean) data.get("isPublic"));
                    stables.computeIfAbsent(kingdomName, k -> new HashMap<>()).put(entry.getKey(), stable);
                }
            }
        }
    }
    
    // Auto-claim Mode
    public boolean toggleAutoClaim(Player player) {
        String playerName = player.getName();
        if (autoClaimEnabled.contains(playerName)) {
            autoClaimEnabled.remove(playerName);
            lastAutoClaim.remove(playerName);
            return false;
        } else {
            autoClaimEnabled.add(playerName);
            lastAutoClaim.put(playerName, System.currentTimeMillis());
            return true;
        }
    }
    
    public boolean isAutoClaimEnabled(Player player) {
        return autoClaimEnabled.contains(player.getName());
    }
    
    public boolean canAutoClaim(Player player) {
        String playerName = player.getName();
        if (!autoClaimEnabled.contains(playerName)) return false;
        
        long lastClaim = lastAutoClaim.getOrDefault(playerName, 0L);
        long cooldown = plugin.getConfig().getLong("auto_claim.cooldown", 300); // 5 minutes default
        return (System.currentTimeMillis() - lastClaim) >= (cooldown * 1000);
    }
    
    public void recordAutoClaim(Player player) {
        lastAutoClaim.put(player.getName(), System.currentTimeMillis());
    }
    
    // Waypoints
    public boolean createWaypoint(String kingdomName, String name, Location location, String createdBy) {
        waypoints.computeIfAbsent(kingdomName, k -> new HashMap<>());
        if (waypoints.get(kingdomName).containsKey(name)) {
            return false; // Waypoint already exists
        }
        waypoints.get(kingdomName).put(name, new Waypoint(name, kingdomName, location, createdBy));
        plugin.getStorageManager().getAdapter().saveWaypoint(kingdomName, name, location, createdBy);
        return true;
    }
    
    public Waypoint getWaypoint(String kingdomName, String name) {
        Map<String, Waypoint> kingdomWaypoints = waypoints.get(kingdomName);
        return kingdomWaypoints != null ? kingdomWaypoints.get(name) : null;
    }
    
    public Map<String, Waypoint> getWaypoints(String kingdomName) {
        return waypoints.getOrDefault(kingdomName, new HashMap<>());
    }
    
    public boolean deleteWaypoint(String kingdomName, String name) {
        Map<String, Waypoint> kingdomWaypoints = waypoints.get(kingdomName);
        boolean removed = kingdomWaypoints != null && kingdomWaypoints.remove(name) != null;
        if (removed) {
            plugin.getStorageManager().getAdapter().deleteWaypoint(kingdomName, name);
        }
        return removed;
    }
    
    // Farms
    public boolean createFarm(String kingdomName, String name, Chunk chunk, Location center, KingdomFarm.FarmType type) {
        farms.computeIfAbsent(kingdomName, k -> new HashMap<>());
        if (farms.get(kingdomName).containsKey(name)) {
            return false; // Farm already exists
        }
        KingdomFarm farm = new KingdomFarm(name, kingdomName, chunk, center, type);
        farms.get(kingdomName).put(name, farm);
        plugin.getStorageManager().getAdapter().saveFarm(kingdomName, name, type.name(), chunk, center, farm.getLastHarvest(), farm.isActive());
        return true;
    }
    
    public KingdomFarm getFarm(String kingdomName, String name) {
        Map<String, KingdomFarm> kingdomFarms = farms.get(kingdomName);
        return kingdomFarms != null ? kingdomFarms.get(name) : null;
    }
    
    public Map<String, KingdomFarm> getFarms(String kingdomName) {
        return farms.getOrDefault(kingdomName, new HashMap<>());
    }
    
    // Workshops
    public boolean createWorkshop(String kingdomName, String name, Chunk chunk, Location location, KingdomWorkshop.WorkshopType type) {
        workshops.computeIfAbsent(kingdomName, k -> new HashMap<>());
        if (workshops.get(kingdomName).containsKey(name)) {
            return false; // Workshop already exists
        }
        KingdomWorkshop workshop = new KingdomWorkshop(name, kingdomName, chunk, location, type);
        workshops.get(kingdomName).put(name, workshop);
        plugin.getStorageManager().getAdapter().saveWorkshop(kingdomName, name, type.name(), chunk, location, workshop.getBonusMultiplier(), workshop.isActive());
        return true;
    }
    
    public KingdomWorkshop getWorkshop(String kingdomName, String name) {
        Map<String, KingdomWorkshop> kingdomWorkshops = workshops.get(kingdomName);
        return kingdomWorkshops != null ? kingdomWorkshops.get(name) : null;
    }
    
    public KingdomWorkshop getWorkshopAt(Chunk chunk) {
        String kingdomName = plugin.getKingdomManager().getKingdomByChunk(chunk) != null ?
            plugin.getKingdomManager().getKingdomByChunk(chunk).getName() : null;
        if (kingdomName == null) return null;
        
        for (KingdomWorkshop workshop : workshops.getOrDefault(kingdomName, new HashMap<>()).values()) {
            if (workshop.getChunk().equals(chunk)) {
                return workshop;
            }
        }
        return null;
    }
    
    // Libraries
    public boolean createLibrary(String kingdomName, String name, Chunk chunk, Location location) {
        libraries.computeIfAbsent(kingdomName, k -> new HashMap<>());
        if (libraries.get(kingdomName).containsKey(name)) {
            return false; // Library already exists
        }
        KingdomLibrary library = new KingdomLibrary(name, kingdomName, chunk, location);
        libraries.get(kingdomName).put(name, library);
        plugin.getStorageManager().getAdapter().saveLibrary(kingdomName, name, chunk, location, library.getMaxBooks(), library.isPublic());
        return true;
    }
    
    public KingdomLibrary getLibrary(String kingdomName, String name) {
        Map<String, KingdomLibrary> kingdomLibraries = libraries.get(kingdomName);
        return kingdomLibraries != null ? kingdomLibraries.get(name) : null;
    }
    
    // Stables
    public boolean createStable(String kingdomName, String name, Chunk chunk, Location location) {
        stables.computeIfAbsent(kingdomName, k -> new HashMap<>());
        if (stables.get(kingdomName).containsKey(name)) {
            return false; // Stable already exists
        }
        KingdomStable stable = new KingdomStable(name, kingdomName, chunk, location);
        stables.get(kingdomName).put(name, stable);
        plugin.getStorageManager().getAdapter().saveStable(kingdomName, name, chunk, location, stable.getMaxMounts(), stable.isPublic());
        return true;
    }
    
    public KingdomStable getStable(String kingdomName, String name) {
        Map<String, KingdomStable> kingdomStables = stables.get(kingdomName);
        return kingdomStables != null ? kingdomStables.get(name) : null;
    }
}

