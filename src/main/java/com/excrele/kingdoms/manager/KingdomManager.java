package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KingdomManager {
    private Map<String, Kingdom> kingdoms;
    private Map<String, Kingdom> claimedChunks;
    private Map<String, String> playerToKingdom;
    private FileConfiguration kingdomsConfig;
    private File kingdomsFile;

    public KingdomManager(FileConfiguration kingdomsConfig, File kingdomsFile) {
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
            kingdoms.put(name, kingdom);
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