package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages enhanced leaderboards with seasonal competitions and rewards
 */
public class EnhancedLeaderboardManager {
    private final KingdomsPlugin plugin;
    
    // Season tracking
    private long currentSeasonStart;
    private long currentSeasonEnd;
    private String currentSeasonType; // "weekly" or "monthly"
    private int seasonNumber;
    
    // Seasonal scores: seasonId -> kingdomName -> score
    private final Map<String, Map<String, Integer>> seasonalScores;
    
    // Leaderboard types
    public enum LeaderboardType {
        LEVEL, XP, MEMBERS, CHALLENGES, CONTRIBUTIONS, STREAKS, HEALTH, GROWTH
    }
    
    // Season types
    public enum SeasonType {
        WEEKLY(7 * 86400), // 7 days
        MONTHLY(30 * 86400); // 30 days
        
        private final long duration;
        
        SeasonType(long duration) {
            this.duration = duration;
        }
        
        public long getDuration() {
            return duration;
        }
    }
    
    public EnhancedLeaderboardManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.seasonalScores = new ConcurrentHashMap<>();
        this.currentSeasonStart = System.currentTimeMillis() / 1000;
        this.currentSeasonType = plugin.getConfig().getString("leaderboards.season_type", "weekly");
        this.seasonNumber = plugin.getConfig().getInt("leaderboards.season_number", 1);
        calculateSeasonEnd();
        loadSeasonalData();
    }
    
    private void calculateSeasonEnd() {
        SeasonType type = SeasonType.valueOf(currentSeasonType.toUpperCase());
        currentSeasonEnd = currentSeasonStart + type.getDuration();
    }
    
    private void loadSeasonalData() {
        // Load from config/storage
        String seasonId = getCurrentSeasonId();
        Map<String, Integer> scores = new HashMap<>();
        // Load scores from storage adapter if needed
        seasonalScores.put(seasonId, scores);
    }
    
    /**
     * Get current season ID
     */
    public String getCurrentSeasonId() {
        return currentSeasonType + "_" + seasonNumber;
    }
    
    /**
     * Check if season has ended and start new one
     */
    public void checkSeasonEnd() {
        long now = System.currentTimeMillis() / 1000;
        if (now >= currentSeasonEnd) {
            endSeason();
            startNewSeason();
        }
    }
    
    /**
     * End current season and distribute rewards
     */
    private void endSeason() {
        String seasonId = getCurrentSeasonId();
        Map<String, Integer> scores = seasonalScores.getOrDefault(seasonId, new HashMap<>());
        
        // Get top kingdoms for each leaderboard type
        for (LeaderboardType type : LeaderboardType.values()) {
            List<Map.Entry<String, Integer>> topKingdoms = getTopKingdomsForType(type, scores, 3);
            
            // Distribute rewards
            distributeRewards(type, topKingdoms, seasonId);
        }
        
        // Announce season end
        announceSeasonEnd(seasonId);
        
        // Save season results
        saveSeasonResults(seasonId, scores);
    }
    
    /**
     * Start a new season
     */
    private void startNewSeason() {
        currentSeasonStart = System.currentTimeMillis() / 1000;
        seasonNumber++;
        calculateSeasonEnd();
        
        String newSeasonId = getCurrentSeasonId();
        seasonalScores.put(newSeasonId, new HashMap<>());
        
        // Announce new season
        announceSeasonStart();
        
        // Save config
        plugin.getConfig().set("leaderboards.season_number", seasonNumber);
        plugin.getConfig().set("leaderboards.season_start", currentSeasonStart);
        plugin.saveConfig();
    }
    
    /**
     * Get leaderboard for a specific type
     */
    public List<Map.Entry<String, Integer>> getLeaderboard(LeaderboardType type, int limit, boolean seasonal) {
        Map<String, Integer> scores;
        
        if (seasonal) {
            String seasonId = getCurrentSeasonId();
            Map<String, Integer> seasonalData = seasonalScores.getOrDefault(seasonId, new HashMap<>());
            // Extract scores for this specific type
            scores = new HashMap<>();
            for (Map.Entry<String, Integer> entry : seasonalData.entrySet()) {
                if (entry.getKey().endsWith("_" + type.name())) {
                    String kingdomName = entry.getKey().replace("_" + type.name(), "");
                    scores.put(kingdomName, entry.getValue());
                }
            }
            // If no seasonal data, use current scores
            if (scores.isEmpty()) {
                scores = calculateCurrentScores(type);
            }
        } else {
            scores = calculateCurrentScores(type);
        }
        
        return scores.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(limit)
            .toList();
    }
    
    /**
     * Calculate current scores for a leaderboard type
     */
    private Map<String, Integer> calculateCurrentScores(LeaderboardType type) {
        Map<String, Integer> scores = new HashMap<>();
        
        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            int score = switch (type) {
                case LEVEL -> kingdom.getLevel();
                case XP -> kingdom.getXp();
                case MEMBERS -> kingdom.getMembers().size() + 1;
                case CHALLENGES -> kingdom.getTotalChallengesCompleted();
                case CONTRIBUTIONS -> kingdom.getMemberContributions().values().stream()
                    .mapToInt(Integer::intValue).sum();
                case STREAKS -> calculateAverageStreak(kingdom);
                case HEALTH -> (int) plugin.getStatisticsManager().calculateKingdomHealthScore(kingdom.getName());
                case GROWTH -> (int) (plugin.getStatisticsManager().calculateGrowthScore(kingdom.getName()) * 10);
            };
            scores.put(kingdom.getName(), score);
        }
        
        return scores;
    }
    
    /**
     * Update seasonal scores
     */
    public void updateSeasonalScore(String kingdomName, LeaderboardType type, int points) {
        String seasonId = getCurrentSeasonId();
        Map<String, Integer> scores = seasonalScores.computeIfAbsent(seasonId, k -> new HashMap<>());
        
        // For seasonal, we track points gained during the season
        String key = kingdomName + "_" + type.name();
        scores.put(key, scores.getOrDefault(key, 0) + points);
        
        // Save to config
        plugin.getConfig().set("leaderboards.seasons." + seasonId + "." + key, scores.get(key));
        plugin.saveConfig();
    }
    
    /**
     * Get top kingdoms for a specific type
     */
    private List<Map.Entry<String, Integer>> getTopKingdomsForType(LeaderboardType type, 
                                                                    Map<String, Integer> scores, 
                                                                    int limit) {
        Map<String, Integer> typeScores = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getKey().endsWith("_" + type.name())) {
                String kingdomName = entry.getKey().replace("_" + type.name(), "");
                typeScores.put(kingdomName, entry.getValue());
            }
        }
        
        return typeScores.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(limit)
            .toList();
    }
    
    /**
     * Distribute rewards to top performers
     */
    private void distributeRewards(LeaderboardType type, List<Map.Entry<String, Integer>> topKingdoms, String seasonId) {
        int rank = 1;
        for (Map.Entry<String, Integer> entry : topKingdoms) {
            String kingdomName = entry.getKey();
            Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
            if (kingdom == null) continue;
            
            // Reward based on rank
            Reward reward = getRewardForRank(rank, type);
            if (reward != null) {
                giveReward(kingdom, reward, rank, type, seasonId);
            }
            rank++;
        }
    }
    
    /**
     * Get reward for a specific rank
     */
    private Reward getRewardForRank(int rank, LeaderboardType type) {
        // Configure rewards in config.yml
        String path = "leaderboards.rewards." + type.name().toLowerCase() + "." + rank;
        if (!plugin.getConfig().contains(path)) {
            return getDefaultReward(rank);
        }
        
        Reward reward = new Reward();
        reward.xp = plugin.getConfig().getInt(path + ".xp", 0);
        reward.money = plugin.getConfig().getDouble(path + ".money", 0.0);
        reward.title = plugin.getConfig().getString(path + ".title", null);
        return reward;
    }
    
    /**
     * Default rewards
     */
    private Reward getDefaultReward(int rank) {
        Reward reward = new Reward();
        switch (rank) {
            case 1 -> {
                reward.xp = 10000;
                reward.money = 1000.0;
                reward.title = "§6§lChampion";
            }
            case 2 -> {
                reward.xp = 5000;
                reward.money = 500.0;
                reward.title = "§e§lRunner-Up";
            }
            case 3 -> {
                reward.xp = 2500;
                reward.money = 250.0;
                reward.title = "§b§lThird Place";
            }
        }
        return reward;
    }
    
    /**
     * Give reward to kingdom
     */
    private void giveReward(Kingdom kingdom, Reward reward, int rank, LeaderboardType type, String seasonId) {
        // Give XP
        if (reward.xp > 0) {
            kingdom.addXp(reward.xp);
        }
        
        // Give money
        if (reward.money > 0 && plugin.getBankManager() != null) {
            plugin.getBankManager().deposit(kingdom.getName(), reward.money);
        }
        
        // Notify kingdom members
        String message = "§6§l[Season End] §r§eYour kingdom ranked #" + rank + 
            " in " + type.name() + " leaderboard!";
        if (reward.xp > 0) {
            message += " §7(+§e" + reward.xp + " XP§7)";
        }
        if (reward.money > 0) {
            message += " §7(+§e" + String.format("%.2f", reward.money) + "§7)";
        }
        
        for (String member : kingdom.getMembers()) {
            Player player = plugin.getServer().getPlayer(member);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
                if (reward.title != null) {
                    player.sendTitle("", reward.title, 10, 70, 20);
                }
            }
        }
        
        // Notify king
        Player king = plugin.getServer().getPlayer(kingdom.getKing());
        if (king != null && king.isOnline()) {
            king.sendMessage(message);
            if (reward.title != null) {
                king.sendTitle("", reward.title, 10, 70, 20);
            }
        }
    }
    
    /**
     * Announce season end
     */
    private void announceSeasonEnd(String seasonId) {
        String message = "§6§l[Season End] §r§eSeason " + seasonNumber + " has ended! Check leaderboards for results.";
        plugin.getServer().broadcastMessage(message);
    }
    
    /**
     * Announce season start
     */
    private void announceSeasonStart() {
        String message = "§6§l[New Season] §r§eSeason " + seasonNumber + " has begun! Compete for top spots!";
        plugin.getServer().broadcastMessage(message);
    }
    
    /**
     * Calculate average streak for a kingdom
     */
    private int calculateAverageStreak(Kingdom kingdom) {
        int totalStreak = 0;
        int count = 0;
        
        for (String member : kingdom.getMembers()) {
            com.excrele.kingdoms.model.PlayerActivity activity = plugin.getActivityManager().getActivity(member);
            if (activity != null) {
                totalStreak += activity.getContributionStreak();
                count++;
            }
        }
        
        com.excrele.kingdoms.model.PlayerActivity kingActivity = plugin.getActivityManager().getActivity(kingdom.getKing());
        if (kingActivity != null) {
            totalStreak += kingActivity.getContributionStreak();
            count++;
        }
        
        return count > 0 ? totalStreak / count : 0;
    }
    
    /**
     * Save season results
     */
    private void saveSeasonResults(String seasonId, Map<String, Integer> scores) {
        // Save to config or storage
        plugin.getConfig().set("leaderboards.seasons." + seasonId, scores);
        plugin.saveConfig();
    }
    
    /**
     * Get time remaining in current season
     */
    public long getTimeRemaining() {
        long now = System.currentTimeMillis() / 1000;
        return Math.max(0, currentSeasonEnd - now);
    }
    
    /**
     * Get formatted time remaining
     */
    public String getFormattedTimeRemaining() {
        long remaining = getTimeRemaining();
        if (remaining <= 0) return "Season ended";
        
        long days = remaining / 86400;
        long hours = (remaining % 86400) / 3600;
        long minutes = (remaining % 3600) / 60;
        
        if (days > 0) {
            return days + "d " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }
    
    /**
     * Reward class
     */
    private static class Reward {
        int xp = 0;
        double money = 0.0;
        String title = null;
    }
}

