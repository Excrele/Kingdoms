package com.excrele.kingdoms.model;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.*;

public class Kingdom {
    private String name;
    private String king;
    private List<String> members;
    private Map<Chunk, String> plotTypes;
    private Map<Chunk, Map<String, String>> chunkFlags; // Per-chunk flags
    private List<List<Chunk>> claims;
    private int currentClaimChunks;
    private int xp;
    private int level;
    private Location spawn; // Kingdom spawn location

    public Kingdom(String name, String king) {
        this.name = name;
        this.king = king;
        this.members = new ArrayList<>();
        this.plotTypes = new HashMap<>();
        this.chunkFlags = new HashMap<>();
        this.claims = new ArrayList<>();
        this.claims.add(new ArrayList<>());
        this.currentClaimChunks = 0;
        this.xp = 0;
        this.level = 1;
        this.spawn = null; // Set later during chunk claim
    }

    public String getName() { return name; }
    public String getKing() { return king; }
    public List<String> getMembers() { return members; }
    public void addMember(String player) { members.add(player); }
    public Map<Chunk, String> getPlotTypes() { return plotTypes; }
    public void setPlotType(Chunk chunk, String type) { plotTypes.put(chunk, type); }
    public Map<String, String> getFlags() { return new HashMap<>(); } // Deprecated, use chunkFlags
    public Map<Chunk, Map<String, String>> getChunkFlags() { return chunkFlags; }
    public Map<String, String> getPlotFlags() { return new HashMap<>(); } // Deprecated
    public Map<String, String> getPlotFlags(Chunk chunk) { return chunkFlags.computeIfAbsent(chunk, k -> new HashMap<>()); }
    public List<List<Chunk>> getClaims() { return claims; }
    public int getCurrentClaimChunks() { return currentClaimChunks; }
    public void setCurrentClaimChunks(int currentClaimChunks) { this.currentClaimChunks = currentClaimChunks; }
    public int getXp() { return xp; }
    public void addXp(int amount) { this.xp += amount; }
    public void setXp(int xp) { this.xp = xp; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getMaxClaimChunks() { return 10 + 5 * level; }
    public Location getSpawn() { return spawn; }
    public void setSpawn(Location spawn) { this.spawn = spawn; }
}