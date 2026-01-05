package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.PlayerActivity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages player activity tracking and auto-kick for inactive members
 */
public class ActivityManager {
    private final KingdomsPlugin plugin;
    private final Map<String, PlayerActivity> activities; // player -> activity
    private final Map<String, Long> sessionStartTimes; // player -> session start time
    
    public ActivityManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.activities = new HashMap<>();
        this.sessionStartTimes = new HashMap<>();
        loadAllActivities();
    }
    
    /**
     * Load all activities from storage
     */
    private void loadAllActivities() {
        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            for (String member : kingdom.getMembers()) {
                loadActivity(member, kingdom.getName());
            }
            // Also load king's activity
            loadActivity(kingdom.getKing(), kingdom.getName());
        }
    }
    
    /**
     * Load activity for a player
     */
    private void loadActivity(String playerName, String kingdomName) {
        Map<String, Object> data = plugin.getStorageManager().getAdapter().loadPlayerActivity(playerName);
        if (data != null) {
            PlayerActivity activity = new PlayerActivity(playerName, kingdomName);
            activity.setLastLogin((Long) data.getOrDefault("lastLogin", System.currentTimeMillis() / 1000));
            activity.addPlaytime((Long) data.getOrDefault("playtime", 0L));
            activity.setLastContribution((Long) data.getOrDefault("lastContribution", System.currentTimeMillis() / 1000));
            activity.setContributions(((Number) data.getOrDefault("contributions", 0)).intValue());
            activity.setContributionStreak(((Number) data.getOrDefault("contributionStreak", 0)).intValue());
            activity.setLastStreakDay(((Number) data.getOrDefault("lastStreakDay", System.currentTimeMillis() / 1000 / (24 * 60 * 60))).longValue());
            activities.put(playerName, activity);
        } else {
            // Create new activity
            PlayerActivity activity = new PlayerActivity(playerName, kingdomName);
            activities.put(playerName, activity);
            saveActivity(activity);
        }
    }
    
    /**
     * Record player login
     */
    public void recordLogin(Player player) {
        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null) return;
        
        PlayerActivity activity = activities.get(player.getName());
        if (activity == null) {
            activity = new PlayerActivity(player.getName(), kingdomName);
            activities.put(player.getName(), activity);
        }
        
        activity.updateLastLogin();
        sessionStartTimes.put(player.getName(), System.currentTimeMillis() / 1000);
        saveActivity(activity);
    }
    
    /**
     * Record player logout
     */
    public void recordLogout(Player player) {
        String playerName = player.getName();
        if (sessionStartTimes.containsKey(playerName)) {
            long sessionStart = sessionStartTimes.get(playerName);
            long sessionEnd = System.currentTimeMillis() / 1000;
            long sessionDuration = sessionEnd - sessionStart;
            
            PlayerActivity activity = activities.get(playerName);
            if (activity != null) {
                activity.addPlaytime(sessionDuration);
                saveActivity(activity);
            }
            
            sessionStartTimes.remove(playerName);
        }
    }
    
    /**
     * Record a contribution (XP, challenges, etc.)
     */
    public void recordContribution(String playerName) {
        PlayerActivity activity = activities.get(playerName);
        if (activity != null) {
            activity.updateLastContribution();
            saveActivity(activity);
            
            // Check achievements after contribution
            String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(playerName);
            if (kingdomName != null && plugin.getAchievementManager() != null) {
                plugin.getAchievementManager().checkAchievements(kingdomName, playerName);
            }
        }
    }
    
    /**
     * Check and kick inactive members
     */
    public void checkInactiveMembers() {
        long inactiveDays = plugin.getConfig().getLong("economy.auto_features.auto_kick_inactive_days", 0);
        if (inactiveDays <= 0) return; // Auto-kick disabled
        
        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            List<String> toKick = new java.util.ArrayList<>();
            
            // Check members (not king)
            for (String member : kingdom.getMembers()) {
                PlayerActivity activity = activities.get(member);
                if (activity != null && activity.isInactive(inactiveDays)) {
                    toKick.add(member);
                }
            }
            
            // Kick inactive members
            for (String member : toKick) {
                kickInactiveMember(kingdom, member);
            }
        }
    }
    
    /**
     * Kick an inactive member
     */
    private void kickInactiveMember(Kingdom kingdom, String memberName) {
        kingdom.getMembers().remove(memberName);
        plugin.getKingdomManager().removePlayerKingdom(memberName);
        
        // Notify kingdom
        String message = "Member " + memberName + " was automatically removed for inactivity (" + 
            plugin.getConfig().getLong("economy.auto_features.auto_kick_inactive_days") + " days).";
        
        // Send message to online members
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (kingdom.getMembers().contains(player.getName()) || player.getName().equals(kingdom.getKing())) {
                player.sendMessage(message);
            }
        }
        
        plugin.getLogger().info("Auto-kicked inactive member " + memberName + " from kingdom " + kingdom.getName());
    }
    
    /**
     * Get activity for a player
     */
    public PlayerActivity getActivity(String playerName) {
        return activities.get(playerName);
    }
    
    /**
     * Save activity to storage
     */
    private void saveActivity(PlayerActivity activity) {
        plugin.getStorageManager().getAdapter().savePlayerActivity(
            activity.getPlayerName(),
            activity.getKingdomName(),
            activity.getLastLogin(),
            activity.getTotalPlaytime(),
            activity.getLastContribution(),
            activity.getContributions(),
            activity.getContributionStreak(),
            activity.getLastStreakDay()
        );
    }
    
    /**
     * Get contribution streak for a player
     */
    public int getContributionStreak(String playerName) {
        PlayerActivity activity = activities.get(playerName);
        return activity != null ? activity.getContributionStreak() : 0;
    }
    
    /**
     * Get member history (join/leave events)
     * This would need to be implemented in storage adapters
     */
    public List<Map<String, Object>> getMemberHistory(String kingdomName) {
        return plugin.getStorageManager().getAdapter().loadMemberHistory(kingdomName);
    }
}

