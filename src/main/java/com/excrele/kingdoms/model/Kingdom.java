package com.excrele.kingdoms.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;

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
    private Map<String, Integer> memberContributions; // Track XP contributions per member
    private Map<String, MemberRole> memberRoles; // Track roles for each member
    private List<String> alliances; // List of allied kingdom names
    private long createdAt; // Kingdom creation timestamp
    private int totalChallengesCompleted; // Total challenges completed by all members

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
        this.memberContributions = new HashMap<>();
        this.memberRoles = new HashMap<>();
        this.memberRoles.put(king, MemberRole.KING); // King always has KING role
        this.alliances = new ArrayList<>();
        this.createdAt = System.currentTimeMillis() / 1000;
        this.totalChallengesCompleted = 0;
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
    public Map<String, Integer> getMemberContributions() { return memberContributions; }
    public void addContribution(String player, int amount) {
        memberContributions.put(player, memberContributions.getOrDefault(player, 0) + amount);
    }
    public int getContribution(String player) {
        return memberContributions.getOrDefault(player, 0);
    }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public int getTotalChallengesCompleted() { return totalChallengesCompleted; }
    public void incrementChallengesCompleted() { this.totalChallengesCompleted++; }
    public void setTotalChallengesCompleted(int count) { this.totalChallengesCompleted = count; }
    public Map<String, MemberRole> getMemberRoles() { return memberRoles; }
    public MemberRole getRole(String player) {
        if (player.equals(king)) return MemberRole.KING;
        return memberRoles.getOrDefault(player, MemberRole.MEMBER);
    }
    public void setRole(String player, MemberRole role) {
        if (player.equals(king) && role != MemberRole.KING) return; // Can't change king's role
        memberRoles.put(player, role);
    }
    public boolean hasPermission(String player, String permission) {
        MemberRole role = getRole(player);
        return switch (permission) {
            case "invite" -> role.canInvite();
            case "claim" -> role.canClaim();
            case "unclaim" -> role.canUnclaim();
            case "setflags" -> role.canSetFlags();
            case "setplotflags" -> role.canSetPlotFlags();
            case "setplottype" -> role.canSetPlotType();
            case "levelup" -> role.canLevelUp();
            case "kick" -> role.canKick();
            case "promote" -> role.canPromote();
            default -> false;
        };
    }
    public List<String> getAlliances() { return alliances; }
    public void addAlliance(String kingdomName) {
        if (!alliances.contains(kingdomName)) {
            alliances.add(kingdomName);
        }
    }
    public void removeAlliance(String kingdomName) {
        alliances.remove(kingdomName);
    }
    public boolean isAllied(String kingdomName) {
        return alliances.contains(kingdomName);
    }
}