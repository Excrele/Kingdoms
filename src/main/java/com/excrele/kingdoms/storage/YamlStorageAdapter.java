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
    public void savePlayerActivity(String player, String kingdomName, long lastLogin, long playtime, 
                                     long lastContribution, int contributions, int contributionStreak, long lastStreakDay) {
        String path = "activity." + player;
        activityConfig.set(path + ".kingdom", kingdomName);
        activityConfig.set(path + ".lastLogin", lastLogin);
        activityConfig.set(path + ".playtime", playtime);
        activityConfig.set(path + ".lastContribution", lastContribution);
        activityConfig.set(path + ".contributions", contributions);
        activityConfig.set(path + ".contributionStreak", contributionStreak);
        activityConfig.set(path + ".lastStreakDay", lastStreakDay);
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
        activity.put("contributionStreak", activityConfig.getInt(path + ".contributionStreak", 0));
        long currentDay = System.currentTimeMillis() / 1000 / (24 * 60 * 60);
        activity.put("lastStreakDay", activityConfig.getLong(path + ".lastStreakDay", currentDay));
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
    public void savePlayerAchievement(String kingdomName, String playerName, String achievementId, String achievementName, 
                                      String description, long unlockedAt, String unlockedBy, int progress, int target, boolean completed) {
        String path = "achievements." + kingdomName + "." + playerName + "." + achievementId;
        File achievementFile = new File(plugin.getDataFolder(), "achievements.yml");
        FileConfiguration achievementConfig = YamlConfiguration.loadConfiguration(achievementFile);
        
        achievementConfig.set(path + ".id", achievementId);
        achievementConfig.set(path + ".name", achievementName);
        achievementConfig.set(path + ".description", description);
        achievementConfig.set(path + ".unlockedAt", unlockedAt);
        achievementConfig.set(path + ".unlockedBy", unlockedBy);
        achievementConfig.set(path + ".progress", progress);
        achievementConfig.set(path + ".target", target);
        achievementConfig.set(path + ".completed", completed);
        
        saveFile(achievementConfig, achievementFile);
    }

    @Override
    public List<Map<String, Object>> loadPlayerAchievements(String kingdomName, String playerName) {
        List<Map<String, Object>> achievements = new ArrayList<>();
        File achievementFile = new File(plugin.getDataFolder(), "achievements.yml");
        if (!achievementFile.exists()) return achievements;
        
        FileConfiguration achievementConfig = YamlConfiguration.loadConfiguration(achievementFile);
        String path = "achievements." + kingdomName + "." + playerName;
        
        if (achievementConfig.contains(path)) {
            org.bukkit.configuration.ConfigurationSection playerSection = achievementConfig.getConfigurationSection(path);
            if (playerSection != null) {
                for (String achievementId : playerSection.getKeys(false)) {
                    Map<String, Object> achievement = new HashMap<>();
                    achievement.put("id", achievementConfig.getString(path + "." + achievementId + ".id"));
                    achievement.put("name", achievementConfig.getString(path + "." + achievementId + ".name"));
                    achievement.put("description", achievementConfig.getString(path + "." + achievementId + ".description"));
                    achievement.put("unlockedAt", achievementConfig.getLong(path + "." + achievementId + ".unlockedAt", 0));
                    achievement.put("unlockedBy", achievementConfig.getString(path + "." + achievementId + ".unlockedBy"));
                    achievement.put("progress", achievementConfig.getInt(path + "." + achievementId + ".progress", 0));
                    achievement.put("target", achievementConfig.getInt(path + "." + achievementId + ".target", 0));
                    achievement.put("completed", achievementConfig.getBoolean(path + "." + achievementId + ".completed", false));
                    achievements.add(achievement);
                }
            }
        }
        return achievements;
    }

    @Override
    public void saveMail(String mailId, String recipient, String sender, String kingdomName, String subject, 
                         String message, long sentAt, boolean read, long readAt, boolean deleted) {
        File mailFile = new File(plugin.getDataFolder(), "mail.yml");
        FileConfiguration mailConfig = YamlConfiguration.loadConfiguration(mailFile);
        
        String path = "mail." + recipient + "." + mailId;
        mailConfig.set(path + ".mailId", mailId);
        mailConfig.set(path + ".recipient", recipient);
        mailConfig.set(path + ".sender", sender);
        mailConfig.set(path + ".kingdomName", kingdomName);
        mailConfig.set(path + ".subject", subject);
        mailConfig.set(path + ".message", message);
        mailConfig.set(path + ".sentAt", sentAt);
        mailConfig.set(path + ".read", read);
        mailConfig.set(path + ".readAt", readAt);
        mailConfig.set(path + ".deleted", deleted);
        
        saveFile(mailConfig, mailFile);
    }

    @Override
    public List<Map<String, Object>> loadPlayerMail(String playerName) {
        List<Map<String, Object>> mailList = new ArrayList<>();
        File mailFile = new File(plugin.getDataFolder(), "mail.yml");
        if (!mailFile.exists()) return mailList;
        
        FileConfiguration mailConfig = YamlConfiguration.loadConfiguration(mailFile);
        String path = "mail." + playerName;
        
        if (mailConfig.contains(path)) {
            org.bukkit.configuration.ConfigurationSection playerSection = mailConfig.getConfigurationSection(path);
            if (playerSection != null) {
                for (String mailId : playerSection.getKeys(false)) {
                    Map<String, Object> mail = new HashMap<>();
                    mail.put("mailId", mailConfig.getString(path + "." + mailId + ".mailId"));
                    mail.put("recipient", mailConfig.getString(path + "." + mailId + ".recipient"));
                    mail.put("sender", mailConfig.getString(path + "." + mailId + ".sender"));
                    mail.put("kingdomName", mailConfig.getString(path + "." + mailId + ".kingdomName"));
                    mail.put("subject", mailConfig.getString(path + "." + mailId + ".subject"));
                    mail.put("message", mailConfig.getString(path + "." + mailId + ".message"));
                    mail.put("sentAt", mailConfig.getLong(path + "." + mailId + ".sentAt", System.currentTimeMillis() / 1000));
                    mail.put("read", mailConfig.getBoolean(path + "." + mailId + ".read", false));
                    mail.put("readAt", mailConfig.getLong(path + "." + mailId + ".readAt", 0));
                    mail.put("deleted", mailConfig.getBoolean(path + "." + mailId + ".deleted", false));
                    mailList.add(mail);
                }
            }
        }
        return mailList;
    }

    @Override
    public void deleteMail(String mailId) {
        File mailFile = new File(plugin.getDataFolder(), "mail.yml");
        if (!mailFile.exists()) return;
        
        FileConfiguration mailConfig = YamlConfiguration.loadConfiguration(mailFile);
        
        // Find and delete mail by ID
        if (mailConfig.contains("mail")) {
            org.bukkit.configuration.ConfigurationSection mailSection = mailConfig.getConfigurationSection("mail");
            if (mailSection != null) {
                for (String recipient : mailSection.getKeys(false)) {
                    String path = "mail." + recipient + "." + mailId;
                    if (mailConfig.contains(path)) {
                        mailConfig.set(path, null);
                        saveFile(mailConfig, mailFile);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void saveSiege(String siegeId, String warId, String attackingKingdom, String defendingKingdom,
                          String worldName, int chunkX, int chunkZ, long startTime, long endTime,
                          int attackProgress, boolean active) {
        File siegeFile = new File(plugin.getDataFolder(), "sieges.yml");
        FileConfiguration siegeConfig = YamlConfiguration.loadConfiguration(siegeFile);
        String path = "sieges." + siegeId;
        siegeConfig.set(path + ".siegeId", siegeId);
        siegeConfig.set(path + ".warId", warId);
        siegeConfig.set(path + ".attackingKingdom", attackingKingdom);
        siegeConfig.set(path + ".defendingKingdom", defendingKingdom);
        siegeConfig.set(path + ".worldName", worldName);
        siegeConfig.set(path + ".chunkX", chunkX);
        siegeConfig.set(path + ".chunkZ", chunkZ);
        siegeConfig.set(path + ".startTime", startTime);
        siegeConfig.set(path + ".endTime", endTime);
        siegeConfig.set(path + ".attackProgress", attackProgress);
        siegeConfig.set(path + ".active", active);
        saveFile(siegeConfig, siegeFile);
    }

    @Override
    public List<Map<String, Object>> loadActiveSieges() {
        List<Map<String, Object>> sieges = new ArrayList<>();
        File siegeFile = new File(plugin.getDataFolder(), "sieges.yml");
        if (!siegeFile.exists()) return sieges;
        FileConfiguration siegeConfig = YamlConfiguration.loadConfiguration(siegeFile);
        if (siegeConfig.contains("sieges")) {
            org.bukkit.configuration.ConfigurationSection siegesSection = siegeConfig.getConfigurationSection("sieges");
            if (siegesSection != null) {
                for (String siegeId : siegesSection.getKeys(false)) {
                    String path = "sieges." + siegeId;
                    Map<String, Object> siege = new HashMap<>();
                    siege.put("siegeId", siegeConfig.getString(path + ".siegeId"));
                    siege.put("warId", siegeConfig.getString(path + ".warId"));
                    siege.put("attackingKingdom", siegeConfig.getString(path + ".attackingKingdom"));
                    siege.put("defendingKingdom", siegeConfig.getString(path + ".defendingKingdom"));
                    siege.put("worldName", siegeConfig.getString(path + ".worldName"));
                    siege.put("chunkX", siegeConfig.getInt(path + ".chunkX"));
                    siege.put("chunkZ", siegeConfig.getInt(path + ".chunkZ"));
                    siege.put("startTime", siegeConfig.getLong(path + ".startTime"));
                    siege.put("endTime", siegeConfig.getLong(path + ".endTime"));
                    siege.put("attackProgress", siegeConfig.getInt(path + ".attackProgress"));
                    siege.put("active", siegeConfig.getBoolean(path + ".active"));
                    sieges.add(siege);
                }
            }
        }
        return sieges;
    }

    @Override
    public void saveRaid(String raidId, String raidingKingdom, String targetKingdom,
                        String worldName, int chunkX, int chunkZ, long startTime, long endTime,
                        int resourcesStolen, boolean active) {
        File raidFile = new File(plugin.getDataFolder(), "raids.yml");
        FileConfiguration raidConfig = YamlConfiguration.loadConfiguration(raidFile);
        String path = "raids." + raidId;
        raidConfig.set(path + ".raidId", raidId);
        raidConfig.set(path + ".raidingKingdom", raidingKingdom);
        raidConfig.set(path + ".targetKingdom", targetKingdom);
        raidConfig.set(path + ".worldName", worldName);
        raidConfig.set(path + ".chunkX", chunkX);
        raidConfig.set(path + ".chunkZ", chunkZ);
        raidConfig.set(path + ".startTime", startTime);
        raidConfig.set(path + ".endTime", endTime);
        raidConfig.set(path + ".resourcesStolen", resourcesStolen);
        raidConfig.set(path + ".active", active);
        saveFile(raidConfig, raidFile);
    }

    @Override
    public List<Map<String, Object>> loadActiveRaids() {
        List<Map<String, Object>> raids = new ArrayList<>();
        File raidFile = new File(plugin.getDataFolder(), "raids.yml");
        if (!raidFile.exists()) return raids;
        FileConfiguration raidConfig = YamlConfiguration.loadConfiguration(raidFile);
        if (raidConfig.contains("raids")) {
            org.bukkit.configuration.ConfigurationSection raidsSection = raidConfig.getConfigurationSection("raids");
            if (raidsSection != null) {
                for (String raidId : raidsSection.getKeys(false)) {
                    String path = "raids." + raidId;
                    Map<String, Object> raid = new HashMap<>();
                    raid.put("raidId", raidConfig.getString(path + ".raidId"));
                    raid.put("raidingKingdom", raidConfig.getString(path + ".raidingKingdom"));
                    raid.put("targetKingdom", raidConfig.getString(path + ".targetKingdom"));
                    raid.put("worldName", raidConfig.getString(path + ".worldName"));
                    raid.put("chunkX", raidConfig.getInt(path + ".chunkX"));
                    raid.put("chunkZ", raidConfig.getInt(path + ".chunkZ"));
                    raid.put("startTime", raidConfig.getLong(path + ".startTime"));
                    raid.put("endTime", raidConfig.getLong(path + ".endTime"));
                    raid.put("resourcesStolen", raidConfig.getInt(path + ".resourcesStolen"));
                    raid.put("active", raidConfig.getBoolean(path + ".active"));
                    raids.add(raid);
                }
            }
        }
        return raids;
    }

    @Override
    public void saveTaxSettings(String kingdomName, double taxRate, long taxInterval, long lastCollection) {
        File taxFile = new File(plugin.getDataFolder(), "tax.yml");
        FileConfiguration taxConfig = YamlConfiguration.loadConfiguration(taxFile);
        String path = "tax." + kingdomName;
        taxConfig.set(path + ".taxRate", taxRate);
        taxConfig.set(path + ".taxInterval", taxInterval);
        taxConfig.set(path + ".lastCollection", lastCollection);
        saveFile(taxConfig, taxFile);
    }

    @Override
    public Map<String, Object> loadTaxSettings(String kingdomName) {
        Map<String, Object> settings = new HashMap<>();
        File taxFile = new File(plugin.getDataFolder(), "tax.yml");
        if (!taxFile.exists()) return settings;
        FileConfiguration taxConfig = YamlConfiguration.loadConfiguration(taxFile);
        String path = "tax." + kingdomName;
        if (taxConfig.contains(path)) {
            settings.put("taxRate", taxConfig.getDouble(path + ".taxRate", 5.0));
            settings.put("taxInterval", taxConfig.getLong(path + ".taxInterval", 86400L));
            settings.put("lastCollection", taxConfig.getLong(path + ".lastCollection", System.currentTimeMillis() / 1000));
        }
        return settings;
    }

    @Override
    public void saveTradeRoute(String routeId, String kingdom1, String kingdom2,
                              String world1, double x1, double y1, double z1,
                              String world2, double x2, double y2, double z2,
                              long establishedAt, boolean active, double tradeVolume,
                              int tradeCount, long lastTradeTime) {
        File routeFile = new File(plugin.getDataFolder(), "trade_routes.yml");
        FileConfiguration routeConfig = YamlConfiguration.loadConfiguration(routeFile);
        String path = "routes." + routeId;
        routeConfig.set(path + ".routeId", routeId);
        routeConfig.set(path + ".kingdom1", kingdom1);
        routeConfig.set(path + ".kingdom2", kingdom2);
        routeConfig.set(path + ".world1", world1);
        routeConfig.set(path + ".x1", x1);
        routeConfig.set(path + ".y1", y1);
        routeConfig.set(path + ".z1", z1);
        routeConfig.set(path + ".world2", world2);
        routeConfig.set(path + ".x2", x2);
        routeConfig.set(path + ".y2", y2);
        routeConfig.set(path + ".z2", z2);
        routeConfig.set(path + ".establishedAt", establishedAt);
        routeConfig.set(path + ".active", active);
        routeConfig.set(path + ".tradeVolume", tradeVolume);
        routeConfig.set(path + ".tradeCount", tradeCount);
        routeConfig.set(path + ".lastTradeTime", lastTradeTime);
        saveFile(routeConfig, routeFile);
    }

    @Override
    public List<Map<String, Object>> loadTradeRoutes() {
        List<Map<String, Object>> routes = new ArrayList<>();
        File routeFile = new File(plugin.getDataFolder(), "trade_routes.yml");
        if (!routeFile.exists()) return routes;
        FileConfiguration routeConfig = YamlConfiguration.loadConfiguration(routeFile);
        if (routeConfig.contains("routes")) {
            org.bukkit.configuration.ConfigurationSection routesSection = routeConfig.getConfigurationSection("routes");
            if (routesSection != null) {
                for (String routeId : routesSection.getKeys(false)) {
                    String path = "routes." + routeId;
                    Map<String, Object> route = new HashMap<>();
                    route.put("routeId", routeConfig.getString(path + ".routeId"));
                    route.put("kingdom1", routeConfig.getString(path + ".kingdom1"));
                    route.put("kingdom2", routeConfig.getString(path + ".kingdom2"));
                    route.put("world1", routeConfig.getString(path + ".world1"));
                    route.put("x1", routeConfig.getDouble(path + ".x1"));
                    route.put("y1", routeConfig.getDouble(path + ".y1"));
                    route.put("z1", routeConfig.getDouble(path + ".z1"));
                    route.put("world2", routeConfig.getString(path + ".world2"));
                    route.put("x2", routeConfig.getDouble(path + ".x2"));
                    route.put("y2", routeConfig.getDouble(path + ".y2"));
                    route.put("z2", routeConfig.getDouble(path + ".z2"));
                    route.put("establishedAt", routeConfig.getLong(path + ".establishedAt"));
                    route.put("active", routeConfig.getBoolean(path + ".active"));
                    route.put("tradeVolume", routeConfig.getDouble(path + ".tradeVolume"));
                    route.put("tradeCount", routeConfig.getInt(path + ".tradeCount"));
                    route.put("lastTradeTime", routeConfig.getLong(path + ".lastTradeTime"));
                    routes.add(route);
                }
            }
        }
        return routes;
    }

    @Override
    public void saveAdvancedChallenge(String challengeId, String name, String description, String type,
                                     int difficulty, int xpReward, long startTime, long endTime,
                                     int requiredMembers, String chainId, int chainOrder, boolean active) {
        File challengeFile = new File(plugin.getDataFolder(), "advanced_challenges.yml");
        FileConfiguration challengeConfig = YamlConfiguration.loadConfiguration(challengeFile);
        String path = "challenges." + challengeId;
        challengeConfig.set(path + ".challengeId", challengeId);
        challengeConfig.set(path + ".name", name);
        challengeConfig.set(path + ".description", description);
        challengeConfig.set(path + ".type", type);
        challengeConfig.set(path + ".difficulty", difficulty);
        challengeConfig.set(path + ".xpReward", xpReward);
        challengeConfig.set(path + ".startTime", startTime);
        challengeConfig.set(path + ".endTime", endTime);
        challengeConfig.set(path + ".requiredMembers", requiredMembers);
        challengeConfig.set(path + ".chainId", chainId);
        challengeConfig.set(path + ".chainOrder", chainOrder);
        challengeConfig.set(path + ".active", active);
        saveFile(challengeConfig, challengeFile);
    }

    @Override
    public List<Map<String, Object>> loadAdvancedChallenges() {
        List<Map<String, Object>> challenges = new ArrayList<>();
        File challengeFile = new File(plugin.getDataFolder(), "advanced_challenges.yml");
        if (!challengeFile.exists()) return challenges;
        FileConfiguration challengeConfig = YamlConfiguration.loadConfiguration(challengeFile);
        if (challengeConfig.contains("challenges")) {
            org.bukkit.configuration.ConfigurationSection challengesSection = challengeConfig.getConfigurationSection("challenges");
            if (challengesSection != null) {
                for (String challengeId : challengesSection.getKeys(false)) {
                    String path = "challenges." + challengeId;
                    Map<String, Object> challenge = new HashMap<>();
                    challenge.put("challengeId", challengeConfig.getString(path + ".challengeId"));
                    challenge.put("name", challengeConfig.getString(path + ".name"));
                    challenge.put("description", challengeConfig.getString(path + ".description"));
                    challenge.put("type", challengeConfig.getString(path + ".type"));
                    challenge.put("difficulty", challengeConfig.getInt(path + ".difficulty"));
                    challenge.put("xpReward", challengeConfig.getInt(path + ".xpReward"));
                    challenge.put("startTime", challengeConfig.getLong(path + ".startTime"));
                    challenge.put("endTime", challengeConfig.getLong(path + ".endTime"));
                    challenge.put("requiredMembers", challengeConfig.getInt(path + ".requiredMembers"));
                    challenge.put("chainId", challengeConfig.getString(path + ".chainId"));
                    challenge.put("chainOrder", challengeConfig.getInt(path + ".chainOrder"));
                    challenge.put("active", challengeConfig.getBoolean(path + ".active"));
                    challenges.add(challenge);
                }
            }
        }
        return challenges;
    }

    @Override
    public void saveKingdomStructure(String structureId, String kingdomName, String type,
                                    String worldName, double x, double y, double z,
                                    long builtAt, int level, boolean active) {
        File structureFile = new File(plugin.getDataFolder(), "structures.yml");
        FileConfiguration structureConfig = YamlConfiguration.loadConfiguration(structureFile);
        String path = "structures." + structureId;
        structureConfig.set(path + ".structureId", structureId);
        structureConfig.set(path + ".kingdomName", kingdomName);
        structureConfig.set(path + ".type", type);
        structureConfig.set(path + ".worldName", worldName);
        structureConfig.set(path + ".x", x);
        structureConfig.set(path + ".y", y);
        structureConfig.set(path + ".z", z);
        structureConfig.set(path + ".builtAt", builtAt);
        structureConfig.set(path + ".level", level);
        structureConfig.set(path + ".active", active);
        saveFile(structureConfig, structureFile);
    }

    @Override
    public List<Map<String, Object>> loadKingdomStructures() {
        List<Map<String, Object>> structures = new ArrayList<>();
        File structureFile = new File(plugin.getDataFolder(), "structures.yml");
        if (!structureFile.exists()) return structures;
        FileConfiguration structureConfig = YamlConfiguration.loadConfiguration(structureFile);
        if (structureConfig.contains("structures")) {
            org.bukkit.configuration.ConfigurationSection structuresSection = structureConfig.getConfigurationSection("structures");
            if (structuresSection != null) {
                for (String structureId : structuresSection.getKeys(false)) {
                    String path = "structures." + structureId;
                    Map<String, Object> structure = new HashMap<>();
                    structure.put("structureId", structureConfig.getString(path + ".structureId"));
                    structure.put("kingdomName", structureConfig.getString(path + ".kingdomName"));
                    structure.put("type", structureConfig.getString(path + ".type"));
                    structure.put("worldName", structureConfig.getString(path + ".worldName"));
                    structure.put("x", structureConfig.getDouble(path + ".x"));
                    structure.put("y", structureConfig.getDouble(path + ".y"));
                    structure.put("z", structureConfig.getDouble(path + ".z"));
                    structure.put("builtAt", structureConfig.getLong(path + ".builtAt"));
                    structure.put("level", structureConfig.getInt(path + ".level"));
                    structure.put("active", structureConfig.getBoolean(path + ".active"));
                    structures.add(structure);
                }
            }
        }
        return structures;
    }

    @Override
    public void saveKingdomResource(String kingdomName, String resourceType, int amount) {
        File resourceFile = new File(plugin.getDataFolder(), "resources.yml");
        FileConfiguration resourceConfig = YamlConfiguration.loadConfiguration(resourceFile);
        String path = "resources." + kingdomName + "." + resourceType;
        resourceConfig.set(path, amount);
        saveFile(resourceConfig, resourceFile);
    }

    @Override
    public List<Map<String, Object>> loadKingdomResources() {
        List<Map<String, Object>> resources = new ArrayList<>();
        File resourceFile = new File(plugin.getDataFolder(), "resources.yml");
        if (!resourceFile.exists()) return resources;
        FileConfiguration resourceConfig = YamlConfiguration.loadConfiguration(resourceFile);
        if (resourceConfig.contains("resources")) {
            org.bukkit.configuration.ConfigurationSection resourcesSection = resourceConfig.getConfigurationSection("resources");
            if (resourcesSection != null) {
                for (String kingdomName : resourcesSection.getKeys(false)) {
                    org.bukkit.configuration.ConfigurationSection kingdomSection = resourcesSection.getConfigurationSection(kingdomName);
                    if (kingdomSection != null) {
                        for (String resourceType : kingdomSection.getKeys(false)) {
                            Map<String, Object> resource = new HashMap<>();
                            resource.put("kingdomName", kingdomName);
                            resource.put("resourceType", resourceType);
                            resource.put("amount", resourceConfig.getInt("resources." + kingdomName + "." + resourceType));
                            resources.add(resource);
                        }
                    }
                }
            }
        }
        return resources;
    }

    @Override
    public void saveDiplomaticAgreement(String agreementId, String kingdom1, String kingdom2,
                                       String type, long establishedAt, long expiresAt,
                                       boolean active, String terms) {
        File diplomacyFile = new File(plugin.getDataFolder(), "diplomacy.yml");
        FileConfiguration diplomacyConfig = YamlConfiguration.loadConfiguration(diplomacyFile);
        String path = "agreements." + agreementId;
        diplomacyConfig.set(path + ".agreementId", agreementId);
        diplomacyConfig.set(path + ".kingdom1", kingdom1);
        diplomacyConfig.set(path + ".kingdom2", kingdom2);
        diplomacyConfig.set(path + ".type", type);
        diplomacyConfig.set(path + ".establishedAt", establishedAt);
        diplomacyConfig.set(path + ".expiresAt", expiresAt);
        diplomacyConfig.set(path + ".active", active);
        diplomacyConfig.set(path + ".terms", terms);
        saveFile(diplomacyConfig, diplomacyFile);
    }

    @Override
    public List<Map<String, Object>> loadDiplomaticAgreements() {
        List<Map<String, Object>> agreements = new ArrayList<>();
        File diplomacyFile = new File(plugin.getDataFolder(), "diplomacy.yml");
        if (!diplomacyFile.exists()) return agreements;
        FileConfiguration diplomacyConfig = YamlConfiguration.loadConfiguration(diplomacyFile);
        if (diplomacyConfig.contains("agreements")) {
            org.bukkit.configuration.ConfigurationSection agreementsSection = diplomacyConfig.getConfigurationSection("agreements");
            if (agreementsSection != null) {
                for (String agreementId : agreementsSection.getKeys(false)) {
                    String path = "agreements." + agreementId;
                    Map<String, Object> agreement = new HashMap<>();
                    agreement.put("agreementId", diplomacyConfig.getString(path + ".agreementId"));
                    agreement.put("kingdom1", diplomacyConfig.getString(path + ".kingdom1"));
                    agreement.put("kingdom2", diplomacyConfig.getString(path + ".kingdom2"));
                    agreement.put("type", diplomacyConfig.getString(path + ".type"));
                    agreement.put("establishedAt", diplomacyConfig.getLong(path + ".establishedAt"));
                    agreement.put("expiresAt", diplomacyConfig.getLong(path + ".expiresAt"));
                    agreement.put("active", diplomacyConfig.getBoolean(path + ".active"));
                    agreement.put("terms", diplomacyConfig.getString(path + ".terms"));
                    agreements.add(agreement);
                }
            }
        }
        return agreements;
    }

    @Override
    public void saveKingdomTheme(String kingdomName, String primaryColor, String secondaryColor,
                                String accentColor, String bannerMaterial, String flagMaterial,
                                String primaryParticle, String secondaryParticle, String themeName) {
        File themeFile = new File(plugin.getDataFolder(), "themes.yml");
        FileConfiguration themeConfig = YamlConfiguration.loadConfiguration(themeFile);
        String path = "themes." + kingdomName;
        themeConfig.set(path + ".kingdomName", kingdomName);
        themeConfig.set(path + ".primaryColor", primaryColor);
        themeConfig.set(path + ".secondaryColor", secondaryColor);
        themeConfig.set(path + ".accentColor", accentColor);
        themeConfig.set(path + ".bannerMaterial", bannerMaterial);
        themeConfig.set(path + ".flagMaterial", flagMaterial);
        themeConfig.set(path + ".primaryParticle", primaryParticle);
        themeConfig.set(path + ".secondaryParticle", secondaryParticle);
        themeConfig.set(path + ".themeName", themeName);
        saveFile(themeConfig, themeFile);
    }

    @Override
    public List<Map<String, Object>> loadKingdomThemes() {
        List<Map<String, Object>> themes = new ArrayList<>();
        File themeFile = new File(plugin.getDataFolder(), "themes.yml");
        if (!themeFile.exists()) return themes;
        FileConfiguration themeConfig = YamlConfiguration.loadConfiguration(themeFile);
        if (themeConfig.contains("themes")) {
            org.bukkit.configuration.ConfigurationSection themesSection = themeConfig.getConfigurationSection("themes");
            if (themesSection != null) {
                for (String kingdomName : themesSection.getKeys(false)) {
                    String path = "themes." + kingdomName;
                    Map<String, Object> theme = new HashMap<>();
                    theme.put("kingdomName", kingdomName);
                    theme.put("primaryColor", themeConfig.getString(path + ".primaryColor"));
                    theme.put("secondaryColor", themeConfig.getString(path + ".secondaryColor"));
                    theme.put("accentColor", themeConfig.getString(path + ".accentColor"));
                    theme.put("bannerMaterial", themeConfig.getString(path + ".bannerMaterial"));
                    theme.put("flagMaterial", themeConfig.getString(path + ".flagMaterial"));
                    theme.put("primaryParticle", themeConfig.getString(path + ".primaryParticle"));
                    theme.put("secondaryParticle", themeConfig.getString(path + ".secondaryParticle"));
                    theme.put("themeName", themeConfig.getString(path + ".themeName"));
                    themes.add(theme);
                }
            }
        }
        return themes;
    }

    @Override
    public void saveKingdomBanner(String bannerId, String kingdomName, String worldName,
                                 double x, double y, double z, String bannerMaterial) {
        File bannerFile = new File(plugin.getDataFolder(), "banners.yml");
        FileConfiguration bannerConfig = YamlConfiguration.loadConfiguration(bannerFile);
        String path = "banners." + bannerId;
        bannerConfig.set(path + ".bannerId", bannerId);
        bannerConfig.set(path + ".kingdomName", kingdomName);
        bannerConfig.set(path + ".worldName", worldName);
        bannerConfig.set(path + ".x", x);
        bannerConfig.set(path + ".y", y);
        bannerConfig.set(path + ".z", z);
        bannerConfig.set(path + ".bannerMaterial", bannerMaterial);
        saveFile(bannerConfig, bannerFile);
    }

    @Override
    public void deleteKingdomBanner(String bannerId) {
        File bannerFile = new File(plugin.getDataFolder(), "banners.yml");
        if (!bannerFile.exists()) return;
        FileConfiguration bannerConfig = YamlConfiguration.loadConfiguration(bannerFile);
        bannerConfig.set("banners." + bannerId, null);
        saveFile(bannerConfig, bannerFile);
    }

    @Override
    public List<Map<String, Object>> loadKingdomBanners() {
        List<Map<String, Object>> banners = new ArrayList<>();
        File bannerFile = new File(plugin.getDataFolder(), "banners.yml");
        if (!bannerFile.exists()) return banners;
        FileConfiguration bannerConfig = YamlConfiguration.loadConfiguration(bannerFile);
        if (bannerConfig.contains("banners")) {
            org.bukkit.configuration.ConfigurationSection bannersSection = bannerConfig.getConfigurationSection("banners");
            if (bannersSection != null) {
                for (String bannerId : bannersSection.getKeys(false)) {
                    String path = "banners." + bannerId;
                    Map<String, Object> banner = new HashMap<>();
                    banner.put("bannerId", bannerConfig.getString(path + ".bannerId"));
                    banner.put("kingdomName", bannerConfig.getString(path + ".kingdomName"));
                    banner.put("worldName", bannerConfig.getString(path + ".worldName"));
                    banner.put("x", bannerConfig.getDouble(path + ".x"));
                    banner.put("y", bannerConfig.getDouble(path + ".y"));
                    banner.put("z", bannerConfig.getDouble(path + ".z"));
                    banner.put("bannerMaterial", bannerConfig.getString(path + ".bannerMaterial"));
                    banners.add(banner);
                }
            }
        }
        return banners;
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

