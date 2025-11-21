package com.excrele.kingdoms.storage;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlStorageAdapter implements StorageAdapter {
    private final KingdomsPlugin plugin;
    private FileConfiguration kingdomsConfig;
    private File kingdomsFile;
    private FileConfiguration trustsConfig;
    private File trustsFile;
    private FileConfiguration warsConfig;
    private File warsFile;
    private FileConfiguration bankConfig;
    private File bankFile;
    private FileConfiguration activityConfig;
    private File activityFile;

    public YamlStorageAdapter(KingdomsPlugin plugin) {
        this.plugin = plugin;
        initialize();
    }

    @Override
    public void initialize() {
        kingdomsFile = new File(plugin.getDataFolder(), "kingdoms.yml");
        trustsFile = new File(plugin.getDataFolder(), "trusts.yml");
        warsFile = new File(plugin.getDataFolder(), "wars.yml");
        bankFile = new File(plugin.getDataFolder(), "bank.yml");
        activityFile = new File(plugin.getDataFolder(), "activity.yml");
        
        try {
            if (!kingdomsFile.exists()) kingdomsFile.createNewFile();
            if (!trustsFile.exists()) trustsFile.createNewFile();
            if (!warsFile.exists()) warsFile.createNewFile();
            if (!bankFile.exists()) bankFile.createNewFile();
            if (!activityFile.exists()) activityFile.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create storage files!");
        }
        
        kingdomsConfig = YamlConfiguration.loadConfiguration(kingdomsFile);
        trustsConfig = YamlConfiguration.loadConfiguration(trustsFile);
        warsConfig = YamlConfiguration.loadConfiguration(warsFile);
        bankConfig = YamlConfiguration.loadConfiguration(bankFile);
        activityConfig = YamlConfiguration.loadConfiguration(activityFile);
    }

    @Override
    public void saveKingdom(Kingdom kingdom) {
        String path = "kingdoms." + kingdom.getName();
        kingdomsConfig.set(path + ".king", kingdom.getKing());
        kingdomsConfig.set(path + ".members", kingdom.getMembers());
        kingdomsConfig.set(path + ".currentClaimChunks", kingdom.getCurrentClaimChunks());
        kingdomsConfig.set(path + ".xp", kingdom.getXp());
        kingdomsConfig.set(path + ".level", kingdom.getLevel());
        kingdomsConfig.set(path + ".createdAt", kingdom.getCreatedAt());
        kingdomsConfig.set(path + ".totalChallengesCompleted", kingdom.getTotalChallengesCompleted());
        kingdomsConfig.set(path + ".alliances", kingdom.getAlliances());
        
        // Save member contributions
        kingdomsConfig.set(path + ".memberContributions", null);
        for (Map.Entry<String, Integer> entry : kingdom.getMemberContributions().entrySet()) {
            kingdomsConfig.set(path + ".memberContributions." + entry.getKey(), entry.getValue());
        }
        
        // Save member roles
        kingdomsConfig.set(path + ".memberRoles", null);
        for (Map.Entry<String, com.excrele.kingdoms.model.MemberRole> entry : kingdom.getMemberRoles().entrySet()) {
            kingdomsConfig.set(path + ".memberRoles." + entry.getKey(), entry.getValue().name());
        }
        
        // Save spawn
        if (kingdom.getSpawn() != null) {
            Location spawn = kingdom.getSpawn();
            kingdomsConfig.set(path + ".spawn.world", spawn.getWorld().getName());
            kingdomsConfig.set(path + ".spawn.x", spawn.getX());
            kingdomsConfig.set(path + ".spawn.y", spawn.getY());
            kingdomsConfig.set(path + ".spawn.z", spawn.getZ());
            kingdomsConfig.set(path + ".spawn.yaw", spawn.getYaw());
            kingdomsConfig.set(path + ".spawn.pitch", spawn.getPitch());
        }
        
        // Save plot types
        kingdomsConfig.set(path + ".plotTypes", null);
        for (Map.Entry<Chunk, String> entry : kingdom.getPlotTypes().entrySet()) {
            Chunk chunk = entry.getKey();
            String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
            kingdomsConfig.set(path + ".plotTypes." + chunkKey, entry.getValue());
        }
        
        // Save chunk flags
        kingdomsConfig.set(path + ".chunkFlags", null);
        for (Map.Entry<Chunk, Map<String, String>> entry : kingdom.getChunkFlags().entrySet()) {
            Chunk chunk = entry.getKey();
            String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
            for (Map.Entry<String, String> flag : entry.getValue().entrySet()) {
                kingdomsConfig.set(path + ".chunkFlags." + chunkKey + "." + flag.getKey(), flag.getValue());
            }
        }
        
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public Kingdom loadKingdom(String name) {
        String path = "kingdoms." + name;
        if (!kingdomsConfig.contains(path)) return null;
        
        String kingName = kingdomsConfig.getString(path + ".king");
        if (kingName == null) return null;
        
        Kingdom kingdom = new Kingdom(name, kingName);
        kingdom.getMembers().addAll(kingdomsConfig.getStringList(path + ".members"));
        kingdom.setCurrentClaimChunks(kingdomsConfig.getInt(path + ".currentClaimChunks"));
        kingdom.addXp(kingdomsConfig.getInt(path + ".xp"));
        kingdom.setLevel(kingdomsConfig.getInt(path + ".level", 1));
        kingdom.setCreatedAt(kingdomsConfig.getLong(path + ".createdAt", System.currentTimeMillis() / 1000));
        kingdom.setTotalChallengesCompleted(kingdomsConfig.getInt(path + ".totalChallengesCompleted", 0));
        kingdom.getAlliances().addAll(kingdomsConfig.getStringList(path + ".alliances"));
        
        // Load member contributions
        if (kingdomsConfig.contains(path + ".memberContributions")) {
            for (String player : kingdomsConfig.getConfigurationSection(path + ".memberContributions").getKeys(false)) {
                kingdom.getMemberContributions().put(player, kingdomsConfig.getInt(path + ".memberContributions." + player));
            }
        }
        
        // Load member roles
        if (kingdomsConfig.contains(path + ".memberRoles")) {
            for (String player : kingdomsConfig.getConfigurationSection(path + ".memberRoles").getKeys(false)) {
                String roleName = kingdomsConfig.getString(path + ".memberRoles." + player);
                if (roleName != null) {
                    try {
                        com.excrele.kingdoms.model.MemberRole role = com.excrele.kingdoms.model.MemberRole.valueOf(roleName.toUpperCase());
                        kingdom.setRole(player, role);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid role for player " + player + " in kingdom " + name);
                    }
                }
            }
        }
        
        // Load spawn
        if (kingdomsConfig.contains(path + ".spawn")) {
            String worldName = kingdomsConfig.getString(path + ".spawn.world");
            if (worldName != null && plugin.getServer().getWorld(worldName) != null) {
                double x = kingdomsConfig.getDouble(path + ".spawn.x");
                double y = kingdomsConfig.getDouble(path + ".spawn.y");
                double z = kingdomsConfig.getDouble(path + ".spawn.z");
                float yaw = (float) kingdomsConfig.getDouble(path + ".spawn.yaw");
                float pitch = (float) kingdomsConfig.getDouble(path + ".spawn.pitch");
                kingdom.setSpawn(new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch));
            }
        }
        
        // Load plot types and flags (handled by KingdomManager for chunk loading)
        
        return kingdom;
    }

    @Override
    public List<String> loadAllKingdomNames() {
        if (!kingdomsConfig.contains("kingdoms")) return new ArrayList<>();
        return new ArrayList<>(kingdomsConfig.getConfigurationSection("kingdoms").getKeys(false));
    }

    @Override
    public void deleteKingdom(String name) {
        kingdomsConfig.set("kingdoms." + name, null);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public void saveClaim(String kingdomName, Chunk chunk, String claimId) {
        String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        kingdomsConfig.set("claims." + chunkKey + ".kingdom", kingdomName);
        kingdomsConfig.set("claims." + chunkKey + ".claimId", claimId);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public void deleteClaim(String kingdomName, Chunk chunk) {
        String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        kingdomsConfig.set("claims." + chunkKey, null);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public Map<String, String> loadAllClaims() {
        Map<String, String> claims = new HashMap<>();
        if (!kingdomsConfig.contains("claims")) return claims;
        for (String chunkKey : kingdomsConfig.getConfigurationSection("claims").getKeys(false)) {
            String kingdomName = kingdomsConfig.getString("claims." + chunkKey + ".kingdom");
            if (kingdomName != null) {
                claims.put(chunkKey, kingdomName);
            }
        }
        return claims;
    }

    @Override
    public void saveTrust(String kingdomName, String player, String permission) {
        String path = "trusts." + kingdomName + "." + player;
        List<String> permissions = trustsConfig.getStringList(path);
        if (!permissions.contains(permission)) {
            permissions.add(permission);
            trustsConfig.set(path, permissions);
            saveFile(trustsConfig, trustsFile);
        }
    }

    @Override
    public void deleteTrust(String kingdomName, String player) {
        trustsConfig.set("trusts." + kingdomName + "." + player, null);
        saveFile(trustsConfig, trustsFile);
    }

    @Override
    public Map<String, List<String>> loadTrusts(String kingdomName) {
        Map<String, List<String>> trusts = new HashMap<>();
        String path = "trusts." + kingdomName;
        if (!trustsConfig.contains(path)) return trusts;
        for (String player : trustsConfig.getConfigurationSection(path).getKeys(false)) {
            trusts.put(player, trustsConfig.getStringList(path + "." + player));
        }
        return trusts;
    }

    @Override
    public void saveWar(String warId, String kingdom1, String kingdom2, long startTime, long endTime, boolean active) {
        String path = "wars." + warId;
        warsConfig.set(path + ".kingdom1", kingdom1);
        warsConfig.set(path + ".kingdom2", kingdom2);
        warsConfig.set(path + ".startTime", startTime);
        warsConfig.set(path + ".endTime", endTime);
        warsConfig.set(path + ".active", active);
        saveFile(warsConfig, warsFile);
    }

    @Override
    public Map<String, Object> loadWar(String warId) {
        String path = "wars." + warId;
        if (!warsConfig.contains(path)) return null;
        Map<String, Object> war = new HashMap<>();
        war.put("kingdom1", warsConfig.getString(path + ".kingdom1"));
        war.put("kingdom2", warsConfig.getString(path + ".kingdom2"));
        war.put("startTime", warsConfig.getLong(path + ".startTime"));
        war.put("endTime", warsConfig.getLong(path + ".endTime"));
        war.put("active", warsConfig.getBoolean(path + ".active"));
        return war;
    }

    @Override
    public List<Map<String, Object>> loadActiveWars() {
        List<Map<String, Object>> wars = new ArrayList<>();
        if (!warsConfig.contains("wars")) return wars;
        for (String warId : warsConfig.getConfigurationSection("wars").getKeys(false)) {
            Map<String, Object> war = loadWar(warId);
            if (war != null && (Boolean) war.get("active")) {
                war.put("warId", warId);
                wars.add(war);
            }
        }
        return wars;
    }

    @Override
    public void saveBankBalance(String kingdomName, double balance) {
        bankConfig.set("balances." + kingdomName, balance);
        saveFile(bankConfig, bankFile);
    }

    @Override
    public double loadBankBalance(String kingdomName) {
        return bankConfig.getDouble("balances." + kingdomName, 0.0);
    }

    @Override
    public void savePlayerActivity(String player, String kingdomName, long lastLogin, long playtime) {
        String path = "activity." + player;
        activityConfig.set(path + ".kingdom", kingdomName);
        activityConfig.set(path + ".lastLogin", lastLogin);
        activityConfig.set(path + ".playtime", playtime);
        saveFile(activityConfig, activityFile);
    }

    @Override
    public Map<String, Object> loadPlayerActivity(String player) {
        String path = "activity." + player;
        if (!activityConfig.contains(path)) return null;
        Map<String, Object> activity = new HashMap<>();
        activity.put("kingdom", activityConfig.getString(path + ".kingdom"));
        activity.put("lastLogin", activityConfig.getLong(path + ".lastLogin"));
        activity.put("playtime", activityConfig.getLong(path + ".playtime"));
        activity.put("lastContribution", activityConfig.getLong(path + ".lastContribution", System.currentTimeMillis() / 1000));
        activity.put("contributions", activityConfig.getInt(path + ".contributions", 0));
        return activity;
    }

    @Override
    public List<Map<String, Object>> loadMemberHistory(String kingdomName) {
        List<Map<String, Object>> history = new ArrayList<>();
        String path = "history." + kingdomName;
        if (!activityConfig.contains(path)) return history;
        List<Map<?, ?>> historyList = activityConfig.getMapList(path);
        for (Map<?, ?> entry : historyList) {
            Map<String, Object> historyEntry = new HashMap<>();
            historyEntry.put("player", entry.get("player"));
            historyEntry.put("action", entry.get("action")); // "join" or "leave"
            historyEntry.put("timestamp", entry.get("timestamp"));
            history.add(historyEntry);
        }
        return history;
    }

    @Override
    public void saveVault(String kingdomName, org.bukkit.inventory.Inventory vault) {
        String path = "vaults." + kingdomName;
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < vault.getSize(); i++) {
            org.bukkit.inventory.ItemStack item = vault.getItem(i);
            if (item != null) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("slot", i);
                itemData.put("item", item.serialize());
                items.add(itemData);
            }
        }
        activityConfig.set(path, items);
        saveFile(activityConfig, activityFile);
    }

    @Override
    public Map<Integer, org.bukkit.inventory.ItemStack> loadVault(String kingdomName) {
        Map<Integer, org.bukkit.inventory.ItemStack> items = new HashMap<>();
        String path = "vaults." + kingdomName;
        if (!activityConfig.contains(path)) return items;
        List<Map<?, ?>> itemsList = activityConfig.getMapList(path);
        for (Map<?, ?> itemData : itemsList) {
            int slot = ((Number) itemData.get("slot")).intValue();
            @SuppressWarnings("unchecked")
            Map<String, Object> itemMap = (Map<String, Object>) itemData.get("item");
            if (itemMap != null) {
                org.bukkit.inventory.ItemStack item = org.bukkit.inventory.ItemStack.deserialize(itemMap);
                items.put(slot, item);
            }
        }
        return items;
    }

    // Advanced Features operations
    @Override
    public void saveWaypoint(String kingdomName, String waypointName, Location location, String createdBy) {
        String path = "waypoints." + kingdomName + "." + waypointName;
        kingdomsConfig.set(path + ".world", location.getWorld().getName());
        kingdomsConfig.set(path + ".x", location.getX());
        kingdomsConfig.set(path + ".y", location.getY());
        kingdomsConfig.set(path + ".z", location.getZ());
        kingdomsConfig.set(path + ".yaw", location.getYaw());
        kingdomsConfig.set(path + ".pitch", location.getPitch());
        kingdomsConfig.set(path + ".createdBy", createdBy);
        kingdomsConfig.set(path + ".createdAt", System.currentTimeMillis() / 1000);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public void deleteWaypoint(String kingdomName, String waypointName) {
        kingdomsConfig.set("waypoints." + kingdomName + "." + waypointName, null);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public Map<String, Map<String, Object>> loadWaypoints(String kingdomName) {
        Map<String, Map<String, Object>> waypoints = new HashMap<>();
        String path = "waypoints." + kingdomName;
        if (kingdomsConfig.contains(path)) {
            org.bukkit.configuration.ConfigurationSection section = kingdomsConfig.getConfigurationSection(path);
            if (section != null) {
                for (String name : section.getKeys(false)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("world", kingdomsConfig.getString(path + "." + name + ".world"));
                    data.put("x", kingdomsConfig.getDouble(path + "." + name + ".x"));
                    data.put("y", kingdomsConfig.getDouble(path + "." + name + ".y"));
                    data.put("z", kingdomsConfig.getDouble(path + "." + name + ".z"));
                    data.put("yaw", kingdomsConfig.getDouble(path + "." + name + ".yaw"));
                    data.put("pitch", kingdomsConfig.getDouble(path + "." + name + ".pitch"));
                    data.put("createdBy", kingdomsConfig.getString(path + "." + name + ".createdBy"));
                    data.put("createdAt", kingdomsConfig.getLong(path + "." + name + ".createdAt"));
                    waypoints.put(name, data);
                }
            }
        }
        return waypoints;
    }

    @Override
    public void saveFarm(String kingdomName, String farmName, String farmType, Chunk chunk, Location center, long lastHarvest, boolean isActive) {
        String path = "farms." + kingdomName + "." + farmName;
        kingdomsConfig.set(path + ".type", farmType);
        kingdomsConfig.set(path + ".world", chunk.getWorld().getName());
        kingdomsConfig.set(path + ".chunkX", chunk.getX());
        kingdomsConfig.set(path + ".chunkZ", chunk.getZ());
        kingdomsConfig.set(path + ".centerX", center.getX());
        kingdomsConfig.set(path + ".centerY", center.getY());
        kingdomsConfig.set(path + ".centerZ", center.getZ());
        kingdomsConfig.set(path + ".lastHarvest", lastHarvest);
        kingdomsConfig.set(path + ".isActive", isActive);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public void deleteFarm(String kingdomName, String farmName) {
        kingdomsConfig.set("farms." + kingdomName + "." + farmName, null);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public Map<String, Map<String, Object>> loadFarms(String kingdomName) {
        Map<String, Map<String, Object>> farms = new HashMap<>();
        String path = "farms." + kingdomName;
        if (kingdomsConfig.contains(path)) {
            org.bukkit.configuration.ConfigurationSection section = kingdomsConfig.getConfigurationSection(path);
            if (section != null) {
                for (String name : section.getKeys(false)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("type", kingdomsConfig.getString(path + "." + name + ".type"));
                    data.put("world", kingdomsConfig.getString(path + "." + name + ".world"));
                    data.put("chunkX", kingdomsConfig.getInt(path + "." + name + ".chunkX"));
                    data.put("chunkZ", kingdomsConfig.getInt(path + "." + name + ".chunkZ"));
                    data.put("centerX", kingdomsConfig.getDouble(path + "." + name + ".centerX"));
                    data.put("centerY", kingdomsConfig.getDouble(path + "." + name + ".centerY"));
                    data.put("centerZ", kingdomsConfig.getDouble(path + "." + name + ".centerZ"));
                    data.put("lastHarvest", kingdomsConfig.getLong(path + "." + name + ".lastHarvest"));
                    data.put("isActive", kingdomsConfig.getBoolean(path + "." + name + ".isActive"));
                    farms.put(name, data);
                }
            }
        }
        return farms;
    }

    @Override
    public void saveWorkshop(String kingdomName, String workshopName, String workshopType, Chunk chunk, Location location, double bonusMultiplier, boolean isActive) {
        String path = "workshops." + kingdomName + "." + workshopName;
        kingdomsConfig.set(path + ".type", workshopType);
        kingdomsConfig.set(path + ".world", chunk.getWorld().getName());
        kingdomsConfig.set(path + ".chunkX", chunk.getX());
        kingdomsConfig.set(path + ".chunkZ", chunk.getZ());
        kingdomsConfig.set(path + ".x", location.getX());
        kingdomsConfig.set(path + ".y", location.getY());
        kingdomsConfig.set(path + ".z", location.getZ());
        kingdomsConfig.set(path + ".bonusMultiplier", bonusMultiplier);
        kingdomsConfig.set(path + ".isActive", isActive);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public void deleteWorkshop(String kingdomName, String workshopName) {
        kingdomsConfig.set("workshops." + kingdomName + "." + workshopName, null);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public Map<String, Map<String, Object>> loadWorkshops(String kingdomName) {
        Map<String, Map<String, Object>> workshops = new HashMap<>();
        String path = "workshops." + kingdomName;
        if (kingdomsConfig.contains(path)) {
            org.bukkit.configuration.ConfigurationSection section = kingdomsConfig.getConfigurationSection(path);
            if (section != null) {
                for (String name : section.getKeys(false)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("type", kingdomsConfig.getString(path + "." + name + ".type"));
                    data.put("world", kingdomsConfig.getString(path + "." + name + ".world"));
                    data.put("chunkX", kingdomsConfig.getInt(path + "." + name + ".chunkX"));
                    data.put("chunkZ", kingdomsConfig.getInt(path + "." + name + ".chunkZ"));
                    data.put("x", kingdomsConfig.getDouble(path + "." + name + ".x"));
                    data.put("y", kingdomsConfig.getDouble(path + "." + name + ".y"));
                    data.put("z", kingdomsConfig.getDouble(path + "." + name + ".z"));
                    data.put("bonusMultiplier", kingdomsConfig.getDouble(path + "." + name + ".bonusMultiplier"));
                    data.put("isActive", kingdomsConfig.getBoolean(path + "." + name + ".isActive"));
                    workshops.put(name, data);
                }
            }
        }
        return workshops;
    }

    @Override
    public void saveLibrary(String kingdomName, String libraryName, Chunk chunk, Location location, int maxBooks, boolean isPublic) {
        String path = "libraries." + kingdomName + "." + libraryName;
        kingdomsConfig.set(path + ".world", chunk.getWorld().getName());
        kingdomsConfig.set(path + ".chunkX", chunk.getX());
        kingdomsConfig.set(path + ".chunkZ", chunk.getZ());
        kingdomsConfig.set(path + ".x", location.getX());
        kingdomsConfig.set(path + ".y", location.getY());
        kingdomsConfig.set(path + ".z", location.getZ());
        kingdomsConfig.set(path + ".maxBooks", maxBooks);
        kingdomsConfig.set(path + ".isPublic", isPublic);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public void deleteLibrary(String kingdomName, String libraryName) {
        kingdomsConfig.set("libraries." + kingdomName + "." + libraryName, null);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public Map<String, Map<String, Object>> loadLibraries(String kingdomName) {
        Map<String, Map<String, Object>> libraries = new HashMap<>();
        String path = "libraries." + kingdomName;
        if (kingdomsConfig.contains(path)) {
            org.bukkit.configuration.ConfigurationSection section = kingdomsConfig.getConfigurationSection(path);
            if (section != null) {
                for (String name : section.getKeys(false)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("world", kingdomsConfig.getString(path + "." + name + ".world"));
                    data.put("chunkX", kingdomsConfig.getInt(path + "." + name + ".chunkX"));
                    data.put("chunkZ", kingdomsConfig.getInt(path + "." + name + ".chunkZ"));
                    data.put("x", kingdomsConfig.getDouble(path + "." + name + ".x"));
                    data.put("y", kingdomsConfig.getDouble(path + "." + name + ".y"));
                    data.put("z", kingdomsConfig.getDouble(path + "." + name + ".z"));
                    data.put("maxBooks", kingdomsConfig.getInt(path + "." + name + ".maxBooks"));
                    data.put("isPublic", kingdomsConfig.getBoolean(path + "." + name + ".isPublic"));
                    libraries.put(name, data);
                }
            }
        }
        return libraries;
    }

    @Override
    public void saveStable(String kingdomName, String stableName, Chunk chunk, Location location, int maxMounts, boolean isPublic) {
        String path = "stables." + kingdomName + "." + stableName;
        kingdomsConfig.set(path + ".world", chunk.getWorld().getName());
        kingdomsConfig.set(path + ".chunkX", chunk.getX());
        kingdomsConfig.set(path + ".chunkZ", chunk.getZ());
        kingdomsConfig.set(path + ".x", location.getX());
        kingdomsConfig.set(path + ".y", location.getY());
        kingdomsConfig.set(path + ".z", location.getZ());
        kingdomsConfig.set(path + ".maxMounts", maxMounts);
        kingdomsConfig.set(path + ".isPublic", isPublic);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public void deleteStable(String kingdomName, String stableName) {
        kingdomsConfig.set("stables." + kingdomName + "." + stableName, null);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public Map<String, Map<String, Object>> loadStables(String kingdomName) {
        Map<String, Map<String, Object>> stables = new HashMap<>();
        String path = "stables." + kingdomName;
        if (kingdomsConfig.contains(path)) {
            org.bukkit.configuration.ConfigurationSection section = kingdomsConfig.getConfigurationSection(path);
            if (section != null) {
                for (String name : section.getKeys(false)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("world", kingdomsConfig.getString(path + "." + name + ".world"));
                    data.put("chunkX", kingdomsConfig.getInt(path + "." + name + ".chunkX"));
                    data.put("chunkZ", kingdomsConfig.getInt(path + "." + name + ".chunkZ"));
                    data.put("x", kingdomsConfig.getDouble(path + "." + name + ".x"));
                    data.put("y", kingdomsConfig.getDouble(path + "." + name + ".y"));
                    data.put("z", kingdomsConfig.getDouble(path + "." + name + ".z"));
                    data.put("maxMounts", kingdomsConfig.getInt(path + "." + name + ".maxMounts"));
                    data.put("isPublic", kingdomsConfig.getBoolean(path + "." + name + ".isPublic"));
                    stables.put(name, data);
                }
            }
        }
        return stables;
    }

    // Statistics operations
    @Override
    public void saveClaimAnalytics(String chunkKey, String kingdomName, long claimedAt, long lastActivity, int playerVisits, int blockInteractions, int entityInteractions, double estimatedValue) {
        String path = "analytics.claims." + chunkKey;
        kingdomsConfig.set(path + ".kingdomName", kingdomName);
        kingdomsConfig.set(path + ".claimedAt", claimedAt);
        kingdomsConfig.set(path + ".lastActivity", lastActivity);
        kingdomsConfig.set(path + ".playerVisits", playerVisits);
        kingdomsConfig.set(path + ".blockInteractions", blockInteractions);
        kingdomsConfig.set(path + ".entityInteractions", entityInteractions);
        kingdomsConfig.set(path + ".estimatedValue", estimatedValue);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public Map<String, Object> loadClaimAnalytics(String chunkKey) {
        String path = "analytics.claims." + chunkKey;
        if (!kingdomsConfig.contains(path)) return null;
        
        Map<String, Object> data = new HashMap<>();
        data.put("kingdomName", kingdomsConfig.getString(path + ".kingdomName"));
        data.put("claimedAt", kingdomsConfig.getLong(path + ".claimedAt"));
        data.put("lastActivity", kingdomsConfig.getLong(path + ".lastActivity"));
        data.put("playerVisits", kingdomsConfig.getInt(path + ".playerVisits"));
        data.put("blockInteractions", kingdomsConfig.getInt(path + ".blockInteractions"));
        data.put("entityInteractions", kingdomsConfig.getInt(path + ".entityInteractions"));
        data.put("estimatedValue", kingdomsConfig.getDouble(path + ".estimatedValue"));
        return data;
    }

    @Override
    public void saveKingdomHistory(String kingdomName, long timestamp, String type, String description, String actor) {
        String path = "history." + kingdomName;
        int index = kingdomsConfig.getInt(path + ".count", 0);
        kingdomsConfig.set(path + ".entries." + index + ".timestamp", timestamp);
        kingdomsConfig.set(path + ".entries." + index + ".type", type);
        kingdomsConfig.set(path + ".entries." + index + ".description", description);
        kingdomsConfig.set(path + ".entries." + index + ".actor", actor);
        kingdomsConfig.set(path + ".count", index + 1);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public List<Map<String, Object>> loadKingdomHistory(String kingdomName) {
        List<Map<String, Object>> history = new ArrayList<>();
        String path = "history." + kingdomName;
        if (kingdomsConfig.contains(path + ".entries")) {
            org.bukkit.configuration.ConfigurationSection section = kingdomsConfig.getConfigurationSection(path + ".entries");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("timestamp", kingdomsConfig.getLong(path + ".entries." + key + ".timestamp"));
                    entry.put("type", kingdomsConfig.getString(path + ".entries." + key + ".type"));
                    entry.put("description", kingdomsConfig.getString(path + ".entries." + key + ".description"));
                    entry.put("actor", kingdomsConfig.getString(path + ".entries." + key + ".actor"));
                    history.add(entry);
                }
            }
        }
        return history;
    }

    @Override
    public void saveGrowthData(String kingdomName, long timestamp, int level, int xp, int claims, int members, int alliances) {
        String path = "growth." + kingdomName;
        int index = kingdomsConfig.getInt(path + ".count", 0);
        kingdomsConfig.set(path + ".data." + index + ".timestamp", timestamp);
        kingdomsConfig.set(path + ".data." + index + ".level", level);
        kingdomsConfig.set(path + ".data." + index + ".xp", xp);
        kingdomsConfig.set(path + ".data." + index + ".claims", claims);
        kingdomsConfig.set(path + ".data." + index + ".members", members);
        kingdomsConfig.set(path + ".data." + index + ".alliances", alliances);
        kingdomsConfig.set(path + ".count", index + 1);
        saveFile(kingdomsConfig, kingdomsFile);
    }

    @Override
    public List<Map<String, Object>> loadGrowthData(String kingdomName) {
        List<Map<String, Object>> growth = new ArrayList<>();
        String path = "growth." + kingdomName;
        if (kingdomsConfig.contains(path + ".data")) {
            org.bukkit.configuration.ConfigurationSection section = kingdomsConfig.getConfigurationSection(path + ".data");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("timestamp", kingdomsConfig.getLong(path + ".data." + key + ".timestamp"));
                    data.put("level", kingdomsConfig.getInt(path + ".data." + key + ".level"));
                    data.put("xp", kingdomsConfig.getInt(path + ".data." + key + ".xp"));
                    data.put("claims", kingdomsConfig.getInt(path + ".data." + key + ".claims"));
                    data.put("members", kingdomsConfig.getInt(path + ".data." + key + ".members"));
                    data.put("alliances", kingdomsConfig.getInt(path + ".data." + key + ".alliances"));
                    growth.add(data);
                }
            }
        }
        return growth;
    }

    @Override
    public void close() {
        // YAML doesn't need closing
    }

    @Override
    public boolean isConnected() {
        return true; // YAML is always "connected"
    }

    private void saveFile(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save " + file.getName() + ": " + e.getMessage());
        }
    }
}

