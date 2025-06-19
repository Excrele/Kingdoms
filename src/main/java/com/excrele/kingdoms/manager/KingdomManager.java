package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KingdomManager {
    private final KingdomsPlugin plugin; // Add plugin reference
    private final Map<String, Kingdom> kingdoms;
    private final Map<String, Kingdom> claimedChunks;
    private final Map<String, String> playerToKingdom;
    private final FileConfiguration kingdomsConfig;
    private final File kingdomsFile;

    public KingdomManager(KingdomsPlugin plugin, FileConfiguration kingdomsConfig, File kingdomsFile) {
        this.plugin = plugin; // Initialize plugin
        this.kingdoms = new HashMap<>();
        this.claimedChunks = new HashMap<>();
        this.playerToKingdom = new HashMap<>();
        this.kingdomsConfig = kingdomsConfig;
        this.kingdomsFile = kingdomsFile;
        loadKingdoms();
    }

    private void loadKingdoms() {
        if (!kingdomsConfig.contains("kingdoms")) return;
        for (String name : kingdomsConfig.getConfigurationSection("kingdoms").getKeys(false)) {
            String path = "kingdoms." + name;
            Kingdom kingdom = new Kingdom(name, kingdomsConfig.getString(path + ".king"));
            kingdom.getMembers().addAll(kingdomsConfig.getStringList(path + ".members"));
            kingdom.setCurrentClaimChunks(kingdomsConfig.getInt(path + ".currentClaimChunks"));
            kingdom.addXp(kingdomsConfig.getInt(path + ".xp"));
            kingdom.setLevel(kingdomsConfig.getInt(path + ".level", 1));

            // Load spawn location
            if (kingdomsConfig.contains(path + ".spawn")) {
                String worldName = kingdomsConfig.getString(path + ".spawn.world");
                World world = plugin.getServer().getWorld(worldName); // Use plugin.getServer()
                if (world != null) {
                    double x = kingdomsConfig.getDouble(path + ".spawn.x");
                    double y = kingdomsConfig.getDouble(path + ".spawn.y");
                    double z = kingdomsConfig.getDouble(path + ".spawn.z");
                    float yaw = (float) kingdomsConfig.getDouble(path + ".spawn.yaw");
                    float pitch = (float) kingdomsConfig.getDouble(path + ".spawn.pitch");
                    kingdom.setSpawn(new Location(world, x, y, z, yaw, pitch));
                }
            }

            // Load plot types
            if (kingdomsConfig.contains(path + ".plotTypes")) {
                for (String chunkKey : kingdomsConfig.getConfigurationSection(path + ".plotTypes").getKeys(false)) {
                    String[] coords = chunkKey.split(":");
                    World world = plugin.getServer().getWorld(coords[0]); // Use plugin.getServer()
                    if (world != null) {
                        Chunk chunk = world.getChunkAt(Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
                        kingdom.setPlotType(chunk, kingdomsConfig.getString(path + ".plotTypes." + chunkKey));
                    }
                }
            }

            // Load per-chunk flags
            if (kingdomsConfig.contains(path + ".chunkFlags")) {
                for (String chunkKey : kingdomsConfig.getConfigurationSection(path + ".chunkFlags").getKeys(false)) {
                    String[] coords = chunkKey.split(":");
                    World world = plugin.getServer().getWorld(coords[0]); // Use plugin.getServer()
                    if (world != null) {
                        Chunk chunk = world.getChunkAt(Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
                        Map<String, String> flags = new HashMap<>();
                        for (String flag : kingdomsConfig.getConfigurationSection(path + ".chunkFlags." + chunkKey).getKeys(false)) {
                            flags.put(flag, kingdomsConfig.getString(path + ".chunkFlags." + chunkKey + "." + flag));
                        }
                        kingdom.getChunkFlags().put(chunk, flags);
                    }
                }
            }

            // Load claims
            if (kingdomsConfig.contains(path + ".claims")) {
                List<List<String>> claimList = (List<List<String>>) kingdomsConfig.getList(path + ".claims");
                for (List<String> tier : claimList) {
                    List<Chunk> chunks = new ArrayList<>();
                    for (String chunkKey : tier) {
                        String[] coords = chunkKey.split(":");
                        World world = plugin.getServer().getWorld(coords[0]); // Use plugin.getServer()
                        if (world != null) {
                            Chunk chunk = world.getChunkAt(Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
                            chunks.add(chunk);
                            String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
                            claimedChunks.put(key, kingdom);
                        }
                    }
                    kingdom.getClaims().add(chunks);
                }
            }

            kingdoms.put(name, kingdom);
            playerToKingdom.put(kingdom.getKing(), name);
            for (String member : kingdom.getMembers()) {
                playerToKingdom.put(member, name);
            }
        }
    }

    public void saveKingdoms(FileConfiguration kingdomsConfig, File kingdomsFile) {
        kingdomsConfig.set("kingdoms", null);
        for (Kingdom kingdom : kingdoms.values()) {
            String path = "kingdoms." + kingdom.getName();
            kingdomsConfig.set(path + ".king", kingdom.getKing());
            kingdomsConfig.set(path + ".members", kingdom.getMembers());
            kingdomsConfig.set(path + ".currentClaimChunks", kingdom.getCurrentClaimChunks());
            kingdomsConfig.set(path + ".xp", kingdom.getXp());
            kingdomsConfig.set(path + ".level", kingdom.getLevel());

            // Save spawn location
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

            // Save per-chunk flags
            kingdomsConfig.set(path + ".chunkFlags", null);
            for (Map.Entry<Chunk, Map<String, String>> entry : kingdom.getChunkFlags().entrySet()) {
                Chunk chunk = entry.getKey();
                String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
                for (Map.Entry<String, String> flag : entry.getValue().entrySet()) {
                    kingdomsConfig.set(path + ".chunkFlags." + chunkKey + "." + flag.getKey(), flag.getValue());
                }
            }

            // Save claims
            List<List<String>> claimList = new ArrayList<>();
            for (List<Chunk> tier : kingdom.getClaims()) {
                List<String> chunks = new ArrayList<>();
                for (Chunk chunk : tier) {
                    chunks.add(chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ());
                }
                claimList.add(chunks);
            }
            kingdomsConfig.set(path + ".claims", claimList);
        }
        try {
            kingdomsConfig.save(kingdomsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addKingdom(Kingdom kingdom) { kingdoms.put(kingdom.getName(), kingdom); }
    public Kingdom getKingdom(String name) { return kingdoms.get(name); }
    public Map<String, Kingdom> getKingdoms() { return kingdoms; }
    public Kingdom getKingdomByChunk(Chunk chunk) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        return claimedChunks.get(key);
    }
    public Map<String, Kingdom> getClaimedChunks() { return claimedChunks; }
    public void setPlayerKingdom(String player, String kingdomName) { playerToKingdom.put(player, kingdomName); }
    public String getKingdomOfPlayer(String player) { return playerToKingdom.get(player); }
    public void removePlayerKingdom(String player) { playerToKingdom.remove(player); }

    public void claimChunk(Kingdom kingdom, Chunk chunk, List<Chunk> claim) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        claimedChunks.put(key, kingdom);
        claim.add(chunk);
        kingdom.setCurrentClaimChunks(kingdom.getCurrentClaimChunks() + 1);
    }

    public void unclaimChunk(Chunk chunk) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        Kingdom kingdom = claimedChunks.get(key);
        if (kingdom != null) {
            kingdom.setCurrentClaimChunks(kingdom.getCurrentClaimChunks() - 1);
            kingdom.getChunkFlags().remove(chunk); // Remove chunk flags
            kingdom.getPlotTypes().remove(chunk); // Remove plot type
        }
        claimedChunks.remove(key);
    }

    public void dissolveKingdom(String kingdomName) {
        Kingdom kingdom = kingdoms.get(kingdomName);
        for (List<Chunk> claim : kingdom.getClaims()) {
            for (Chunk chunk : claim) {
                unclaimChunk(chunk);
            }
        }
        for (String member : kingdom.getMembers()) {
            playerToKingdom.remove(member);
        }
        playerToKingdom.remove(kingdom.getKing());
        kingdoms.remove(kingdomName);
    }
}