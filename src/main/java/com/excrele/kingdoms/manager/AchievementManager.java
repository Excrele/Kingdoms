package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.MemberAchievement;
import com.excrele.kingdoms.model.PlayerActivity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages member achievements for kingdoms
 */
public class AchievementManager {
    private final KingdomsPlugin plugin;
    // kingdomName -> playerName -> List of achievements
    private final Map<String, Map<String, List<MemberAchievement>>> achievements;
    
    // Predefined achievement templates
    private final Map<String, AchievementTemplate> achievementTemplates;
    
    public AchievementManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.achievements = new ConcurrentHashMap<>();
        this.achievementTemplates = new HashMap<>();
        initializeAchievementTemplates();
        loadAllAchievements();
    }
    
    /**
     * Initialize predefined achievement templates
     */
    private void initializeAchievementTemplates() {
        // Contribution achievements
        achievementTemplates.put("contributor_10", new AchievementTemplate(
            "contributor_10", "Contributor", "Contribute 10 times to your kingdom", 10));
        achievementTemplates.put("contributor_50", new AchievementTemplate(
            "contributor_50", "Dedicated Contributor", "Contribute 50 times to your kingdom", 50));
        achievementTemplates.put("contributor_100", new AchievementTemplate(
            "contributor_100", "Master Contributor", "Contribute 100 times to your kingdom", 100));
        
        // Streak achievements
        achievementTemplates.put("streak_7", new AchievementTemplate(
            "streak_7", "Week Warrior", "Maintain a 7-day contribution streak", 7));
        achievementTemplates.put("streak_30", new AchievementTemplate(
            "streak_30", "Monthly Master", "Maintain a 30-day contribution streak", 30));
        achievementTemplates.put("streak_100", new AchievementTemplate(
            "streak_100", "Century Streak", "Maintain a 100-day contribution streak", 100));
        
        // Challenge achievements
        achievementTemplates.put("challenges_10", new AchievementTemplate(
            "challenges_10", "Challenge Seeker", "Complete 10 challenges", 10));
        achievementTemplates.put("challenges_50", new AchievementTemplate(
            "challenges_50", "Challenge Master", "Complete 50 challenges", 50));
        
        // Level achievements
        achievementTemplates.put("kingdom_level_5", new AchievementTemplate(
            "kingdom_level_5", "Growing Kingdom", "Reach kingdom level 5", 5));
        achievementTemplates.put("kingdom_level_10", new AchievementTemplate(
            "kingdom_level_10", "Established Kingdom", "Reach kingdom level 10", 10));
        achievementTemplates.put("kingdom_level_20", new AchievementTemplate(
            "kingdom_level_20", "Legendary Kingdom", "Reach kingdom level 20", 20));
        
        // Instant achievements (no progress needed)
        achievementTemplates.put("first_contribution", new AchievementTemplate(
            "first_contribution", "First Steps", "Make your first contribution", 0));
        achievementTemplates.put("join_kingdom", new AchievementTemplate(
            "join_kingdom", "New Member", "Join a kingdom", 0));
    }
    
    /**
     * Load all achievements from storage
     */
    private void loadAllAchievements() {
        // Load from storage adapters
        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            for (String member : kingdom.getMembers()) {
                loadPlayerAchievements(kingdom.getName(), member);
            }
            // Also load king's achievements
            loadPlayerAchievements(kingdom.getName(), kingdom.getKing());
        }
    }
    
    /**
     * Load achievements for a specific player
     */
    private void loadPlayerAchievements(String kingdomName, String playerName) {
        List<Map<String, Object>> achievementData = plugin.getStorageManager().getAdapter().loadPlayerAchievements(kingdomName, playerName);
        if (achievementData != null && !achievementData.isEmpty()) {
            List<MemberAchievement> playerAchievements = new ArrayList<>();
            for (Map<String, Object> data : achievementData) {
                String id = (String) data.get("id");
                String name = (String) data.get("name");
                String description = (String) data.get("description");
                long unlockedAt = ((Number) data.getOrDefault("unlockedAt", 0)).longValue();
                String unlockedBy = (String) data.get("unlockedBy");
                int progress = ((Number) data.getOrDefault("progress", 0)).intValue();
                int target = ((Number) data.getOrDefault("target", 0)).intValue();
                boolean completed = (Boolean) data.getOrDefault("completed", false);
                
                MemberAchievement achievement = new MemberAchievement(id, name, description, 
                                                                      unlockedAt, unlockedBy, progress, target, completed);
                playerAchievements.add(achievement);
            }
            achievements.computeIfAbsent(kingdomName, k -> new ConcurrentHashMap<>())
                        .put(playerName, playerAchievements);
        }
    }
    
    /**
     * Check and unlock achievements for a player based on their activity
     */
    public void checkAchievements(String kingdomName, String playerName) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        
        PlayerActivity activity = plugin.getActivityManager().getActivity(playerName);
        if (activity == null) return;
        
        // Check contribution achievements
        checkContributionAchievements(kingdomName, playerName, activity.getContributions());
        
        // Check streak achievements
        checkStreakAchievements(kingdomName, playerName, activity.getContributionStreak());
        
        // Check challenge achievements (from kingdom stats)
        int challengesCompleted = kingdom.getTotalChallengesCompleted();
        checkChallengeAchievements(kingdomName, playerName, challengesCompleted);
        
        // Check level achievements
        checkLevelAchievements(kingdomName, playerName, kingdom.getLevel());
        
        // Check first contribution
        if (activity.getContributions() >= 1) {
            unlockAchievement(kingdomName, playerName, "first_contribution", playerName);
        }
    }
    
    /**
     * Check contribution-based achievements
     */
    private void checkContributionAchievements(String kingdomName, String playerName, int contributions) {
        updateProgressAchievement(kingdomName, playerName, "contributor_10", contributions);
        updateProgressAchievement(kingdomName, playerName, "contributor_50", contributions);
        updateProgressAchievement(kingdomName, playerName, "contributor_100", contributions);
    }
    
    /**
     * Check streak-based achievements
     */
    private void checkStreakAchievements(String kingdomName, String playerName, int streak) {
        updateProgressAchievement(kingdomName, playerName, "streak_7", streak);
        updateProgressAchievement(kingdomName, playerName, "streak_30", streak);
        updateProgressAchievement(kingdomName, playerName, "streak_100", streak);
    }
    
    /**
     * Check challenge-based achievements
     */
    private void checkChallengeAchievements(String kingdomName, String playerName, int challenges) {
        updateProgressAchievement(kingdomName, playerName, "challenges_10", challenges);
        updateProgressAchievement(kingdomName, playerName, "challenges_50", challenges);
    }
    
    /**
     * Check level-based achievements
     */
    private void checkLevelAchievements(String kingdomName, String playerName, int level) {
        updateProgressAchievement(kingdomName, playerName, "kingdom_level_5", level);
        updateProgressAchievement(kingdomName, playerName, "kingdom_level_10", level);
        updateProgressAchievement(kingdomName, playerName, "kingdom_level_20", level);
    }
    
    /**
     * Update progress for a progress-based achievement
     */
    private void updateProgressAchievement(String kingdomName, String playerName, String achievementId, int currentProgress) {
        AchievementTemplate template = achievementTemplates.get(achievementId);
        if (template == null || template.target == 0) return;
        
        MemberAchievement achievement = getAchievement(kingdomName, playerName, achievementId);
        if (achievement == null) {
            // Create new achievement
            achievement = new MemberAchievement(achievementId, template.name, template.description);
            achievement.setTarget(template.target);
            addAchievement(kingdomName, playerName, achievement);
        }
        
        if (!achievement.isCompleted()) {
            achievement.setProgress(Math.min(currentProgress, template.target));
            if (achievement.getProgress() >= template.target) {
                achievement.unlock(playerName);
                notifyAchievementUnlocked(kingdomName, playerName, achievement);
            }
            saveAchievement(kingdomName, playerName, achievement);
        }
    }
    
    /**
     * Unlock an instant achievement (no progress needed)
     */
    public boolean unlockAchievement(String kingdomName, String playerName, String achievementId, String unlockedBy) {
        AchievementTemplate template = achievementTemplates.get(achievementId);
        if (template == null) return false;
        
        MemberAchievement achievement = getAchievement(kingdomName, playerName, achievementId);
        if (achievement != null && achievement.isCompleted()) {
            return false; // Already unlocked
        }
        
        if (achievement == null) {
            achievement = new MemberAchievement(achievementId, template.name, template.description);
            addAchievement(kingdomName, playerName, achievement);
        }
        
        achievement.unlock(unlockedBy);
        saveAchievement(kingdomName, playerName, achievement);
        notifyAchievementUnlocked(kingdomName, playerName, achievement);
        return true;
    }
    
    /**
     * Get an achievement for a player
     */
    public MemberAchievement getAchievement(String kingdomName, String playerName, String achievementId) {
        Map<String, List<MemberAchievement>> kingdomAchievements = achievements.get(kingdomName);
        if (kingdomAchievements == null) return null;
        
        List<MemberAchievement> playerAchievements = kingdomAchievements.get(playerName);
        if (playerAchievements == null) return null;
        
        return playerAchievements.stream()
                .filter(a -> a.getAchievementId().equals(achievementId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get all achievements for a player
     */
    public List<MemberAchievement> getPlayerAchievements(String kingdomName, String playerName) {
        Map<String, List<MemberAchievement>> kingdomAchievements = achievements.get(kingdomName);
        if (kingdomAchievements == null) return new ArrayList<>();
        
        return kingdomAchievements.getOrDefault(playerName, new ArrayList<>());
    }
    
    /**
     * Add an achievement to a player
     */
    private void addAchievement(String kingdomName, String playerName, MemberAchievement achievement) {
        achievements.computeIfAbsent(kingdomName, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(playerName, k -> new ArrayList<>())
                    .add(achievement);
    }
    
    /**
     * Save an achievement to storage
     */
    private void saveAchievement(String kingdomName, String playerName, MemberAchievement achievement) {
        plugin.getStorageManager().getAdapter().savePlayerAchievement(
            kingdomName, playerName, achievement.getAchievementId(), achievement.getAchievementName(),
            achievement.getDescription(), achievement.getUnlockedAt(), achievement.getUnlockedBy(),
            achievement.getProgress(), achievement.getTarget(), achievement.isCompleted()
        );
    }
    
    /**
     * Notify player and kingdom about achievement unlock
     */
    private void notifyAchievementUnlocked(String kingdomName, String playerName, MemberAchievement achievement) {
        Player player = plugin.getServer().getPlayer(playerName);
        if (player != null && player.isOnline()) {
            player.sendMessage("§6§l[Achievement Unlocked!] §r§e" + achievement.getAchievementName());
            player.sendMessage("§7" + achievement.getDescription());
            com.excrele.kingdoms.util.VisualEffects.playChallengeCompleteEffects(player);
        }
        
        // Notify kingdom members
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom != null) {
            String message = "§6§l[Achievement] §r§e" + playerName + " §7unlocked: §e" + achievement.getAchievementName();
            for (String member : kingdom.getMembers()) {
                Player memberPlayer = plugin.getServer().getPlayer(member);
                if (memberPlayer != null && memberPlayer.isOnline() && !member.equals(playerName)) {
                    memberPlayer.sendMessage(message);
                }
            }
        }
    }
    
    /**
     * Achievement template for predefined achievements
     */
    private static class AchievementTemplate {
        String id;
        String name;
        String description;
        int target;
        
        AchievementTemplate(String id, String name, String description, int target) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.target = target;
        }
    }
}

