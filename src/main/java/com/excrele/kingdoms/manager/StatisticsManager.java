package com.excrele.kingdoms.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Chunk;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.ClaimAnalytics;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.KingdomHistory;
import com.excrele.kingdoms.model.PlayerActivity;

/**
 * Manages advanced statistics and analytics for kingdoms
 */
public class StatisticsManager {
    private final KingdomsPlugin plugin;
    // chunkKey -> ClaimAnalytics
    private final Map<String, ClaimAnalytics> claimAnalytics;
    // kingdomName -> KingdomHistory
    private final Map<String, KingdomHistory> kingdomHistories;
    // kingdomName -> daily growth data
    private final Map<String, List<GrowthData>> growthData;
    
    public StatisticsManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.claimAnalytics = new HashMap<>();
        this.kingdomHistories = new HashMap<>();
        this.growthData = new HashMap<>();
        loadAllData();
    }
    
    private void loadAllData() {
        // Load kingdom histories and growth data
        for (String kingdomName : plugin.getKingdomManager().getKingdoms().keySet()) {
            loadKingdomHistory(kingdomName);
            loadGrowthData(kingdomName);
        }
    }
    
    private void loadKingdomHistory(String kingdomName) {
        List<Map<String, Object>> historyData = plugin.getStorageManager().getAdapter().loadKingdomHistory(kingdomName);
        if (historyData != null) {
            KingdomHistory history = getKingdomHistory(kingdomName);
            for (Map<String, Object> entryData : historyData) {
                try {
                    KingdomHistory.HistoryType type = KingdomHistory.HistoryType.valueOf((String) entryData.get("type"));
                    KingdomHistory.HistoryEntry entry = new KingdomHistory.HistoryEntry(
                        type,
                        (String) entryData.get("description"),
                        (String) entryData.get("actor")
                    );
                    entry.setTimestamp(((Number) entryData.get("timestamp")).longValue());
                    history.getEntries().add(entry);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid history type: " + entryData.get("type"));
                }
            }
        }
    }
    
    private void loadGrowthData(String kingdomName) {
        List<Map<String, Object>> growthDataList = plugin.getStorageManager().getAdapter().loadGrowthData(kingdomName);
        if (growthDataList != null) {
            List<GrowthData> data = new ArrayList<>();
            for (Map<String, Object> entry : growthDataList) {
                GrowthData gd = new GrowthData();
                gd.timestamp = ((Number) entry.get("timestamp")).longValue();
                gd.level = ((Number) entry.get("level")).intValue();
                gd.xp = ((Number) entry.get("xp")).intValue();
                gd.claims = ((Number) entry.get("claims")).intValue();
                gd.members = ((Number) entry.get("members")).intValue();
                gd.alliances = ((Number) entry.get("alliances")).intValue();
                data.add(gd);
            }
            growthData.put(kingdomName, data);
        }
    }
    
    // Claim Analytics
    public ClaimAnalytics getClaimAnalytics(Chunk chunk) {
        String key = getChunkKey(chunk);
        return claimAnalytics.computeIfAbsent(key, k -> 
            new ClaimAnalytics(chunk, plugin.getKingdomManager().getKingdomByChunk(chunk) != null ? 
                plugin.getKingdomManager().getKingdomByChunk(chunk).getName() : null));
    }
    
    public void recordClaimVisit(Chunk chunk, String playerName) {
        ClaimAnalytics analytics = getClaimAnalytics(chunk);
        if (analytics != null) {
            analytics.incrementVisits(playerName);
        }
    }
    
    public void recordBlockInteraction(Chunk chunk) {
        ClaimAnalytics analytics = getClaimAnalytics(chunk);
        if (analytics != null) {
            analytics.incrementBlockInteractions();
            saveClaimAnalytics(analytics);
        }
    }
    
    public void recordEntityInteraction(Chunk chunk) {
        ClaimAnalytics analytics = getClaimAnalytics(chunk);
        if (analytics != null) {
            analytics.incrementEntityInteractions();
            saveClaimAnalytics(analytics);
        }
    }
    
    private void saveClaimAnalytics(ClaimAnalytics analytics) {
        String chunkKey = getChunkKey(analytics.getChunk());
        plugin.getStorageManager().getAdapter().saveClaimAnalytics(
            chunkKey,
            analytics.getKingdomName(),
            analytics.getClaimedAt(),
            analytics.getLastActivity(),
            analytics.getPlayerVisits(),
            analytics.getBlockInteractions(),
            analytics.getEntityInteractions(),
            analytics.getEstimatedValue()
        );
    }
    
    // Kingdom Health Score
    public double calculateKingdomHealthScore(String kingdomName) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return 0.0;
        
        double score = 0.0;
        
        // Member activity (40%)
        double memberActivityScore = calculateMemberActivityScore(kingdomName);
        score += memberActivityScore * 0.4;
        
        // Claim activity (30%)
        double claimActivityScore = calculateClaimActivityScore(kingdomName);
        score += claimActivityScore * 0.3;
        
        // Growth rate (20%)
        double growthScore = calculateGrowthScore(kingdomName);
        score += growthScore * 0.2;
        
        // Financial health (10%)
        double financialScore = calculateFinancialScore(kingdomName);
        score += financialScore * 0.1;
        
        return Math.min(100.0, Math.max(0.0, score));
    }
    
    private double calculateMemberActivityScore(String kingdomName) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return 0.0;
        
        int activeMembers = 0;
        int totalMembers = kingdom.getMembers().size() + 1; // +1 for king
        
        for (String member : kingdom.getMembers()) {
            PlayerActivity activity = plugin.getActivityManager().getActivity(member);
            if (activity != null && activity.getDaysSinceLastLogin() <= 7) {
                activeMembers++;
            }
        }
        
        // Check king activity
        PlayerActivity kingActivity = plugin.getActivityManager().getActivity(kingdom.getKing());
        if (kingActivity != null && kingActivity.getDaysSinceLastLogin() <= 7) {
            activeMembers++;
        }
        
        return totalMembers > 0 ? (activeMembers * 100.0 / totalMembers) : 0.0;
    }
    
    private double calculateClaimActivityScore(String kingdomName) {
        double totalScore = 0.0;
        int claimCount = 0;
        
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return 0.0;
        
        for (List<Chunk> claimGroup : kingdom.getClaims()) {
            for (Chunk chunk : claimGroup) {
                ClaimAnalytics analytics = getClaimAnalytics(chunk);
                if (analytics != null) {
                    totalScore += analytics.getActivityScore();
                    claimCount++;
                }
            }
        }
        
        return claimCount > 0 ? Math.min(100.0, totalScore / claimCount * 10) : 0.0;
    }
    
    private double calculateGrowthScore(String kingdomName) {
        List<GrowthData> data = growthData.get(kingdomName);
        if (data == null || data.size() < 2) return 50.0; // Neutral if no data
        
        // Calculate average growth rate over last 7 days
        long now = System.currentTimeMillis() / 1000;
        List<GrowthData> recent = data.stream()
            .filter(g -> (now - g.timestamp) <= 7 * 86400)
            .collect(Collectors.toList());
        
        if (recent.size() < 2) return 50.0;
        
        GrowthData oldest = recent.get(0);
        GrowthData newest = recent.get(recent.size() - 1);
        
        double growthRate = ((newest.level - oldest.level) * 10.0 + 
                            (newest.claims - oldest.claims) * 2.0 +
                            (newest.members - oldest.members) * 5.0);
        
        return Math.min(100.0, Math.max(0.0, 50.0 + growthRate));
    }
    
    private double calculateFinancialScore(String kingdomName) {
        double balance = plugin.getBankManager().getBalance(kingdomName);
        // Score based on balance (capped at 100)
        return Math.min(100.0, balance / 10000.0 * 100.0);
    }
    
    // Member Analytics
    public Map<String, Object> getMemberAnalytics(String kingdomName, String playerName) {
        Map<String, Object> analytics = new HashMap<>();
        
        PlayerActivity activity = plugin.getActivityManager().getActivity(playerName);
        if (activity != null) {
            analytics.put("lastLogin", activity.getLastLogin());
            analytics.put("daysSinceLogin", activity.getDaysSinceLastLogin());
            analytics.put("totalPlaytime", activity.getTotalPlaytime());
            analytics.put("contributions", activity.getContributions());
            analytics.put("lastContribution", activity.getLastContribution());
        }
        
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom != null) {
            analytics.put("contribution", kingdom.getContribution(playerName));
            analytics.put("role", kingdom.getRole(playerName).getDisplayName());
        }
        
        // Calculate claim activity for this player
        int claimVisits = 0;
        if (kingdom != null) {
            for (List<Chunk> claimGroup : kingdom.getClaims()) {
                for (Chunk chunk : claimGroup) {
                    ClaimAnalytics claimAnalytics = getClaimAnalytics(chunk);
                    if (claimAnalytics != null) {
                        claimVisits += claimAnalytics.getActivityByPlayer().getOrDefault(playerName, 0);
                    }
                }
            }
        }
        analytics.put("claimVisits", claimVisits);
        
        return analytics;
    }
    
    // Growth Tracking
    public void recordGrowthSnapshot(String kingdomName) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        
        GrowthData data = new GrowthData();
        data.timestamp = System.currentTimeMillis() / 1000;
        data.level = kingdom.getLevel();
        data.xp = kingdom.getXp();
        data.claims = kingdom.getCurrentClaimChunks();
        data.members = kingdom.getMembers().size() + 1;
        data.alliances = kingdom.getAlliances().size();
        
        growthData.computeIfAbsent(kingdomName, k -> new ArrayList<>()).add(data);
        
        // Keep only last 90 days of data
        long cutoff = System.currentTimeMillis() / 1000 - (90 * 86400);
        growthData.get(kingdomName).removeIf(g -> g.timestamp < cutoff);
        
        // Save to storage
        plugin.getStorageManager().getAdapter().saveGrowthData(kingdomName, data.timestamp, data.level, data.xp, data.claims, data.members, data.alliances);
    }
    
    public List<GrowthData> getGrowthData(String kingdomName) {
        return growthData.getOrDefault(kingdomName, new ArrayList<>());
    }
    
    // Historical Data
    public KingdomHistory getKingdomHistory(String kingdomName) {
        return kingdomHistories.computeIfAbsent(kingdomName, k -> new KingdomHistory(kingdomName));
    }
    
    public void addHistoryEntry(String kingdomName, KingdomHistory.HistoryType type, String description, String actor) {
        KingdomHistory history = getKingdomHistory(kingdomName);
        KingdomHistory.HistoryEntry entry = new KingdomHistory.HistoryEntry(type, description, actor);
        history.addEntry(entry);
        
        // Save to storage
        plugin.getStorageManager().getAdapter().saveKingdomHistory(kingdomName, entry.getTimestamp(), type.name(), description, actor);
    }
    
    // Activity Heatmap
    public Map<Chunk, Double> generateActivityHeatmap(String kingdomName) {
        Map<Chunk, Double> heatmap = new HashMap<>();
        
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return heatmap;
        
        for (List<Chunk> claimGroup : kingdom.getClaims()) {
            for (Chunk chunk : claimGroup) {
                ClaimAnalytics analytics = getClaimAnalytics(chunk);
                if (analytics != null) {
                    heatmap.put(chunk, analytics.getActivityScore());
                }
            }
        }
        
        return heatmap;
    }
    
    // Helper methods
    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
    
    public static class GrowthData {
        public long timestamp;
        public int level;
        public int xp;
        public int claims;
        public int members;
        public int alliances;
    }
}

