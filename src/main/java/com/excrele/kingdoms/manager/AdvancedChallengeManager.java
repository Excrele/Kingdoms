package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.AdvancedChallenge;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages advanced challenges: weekly/monthly, group, and chain challenges
 */
public class AdvancedChallengeManager {
    private final KingdomsPlugin plugin;
    // challengeId -> AdvancedChallenge
    private final Map<String, AdvancedChallenge> challenges;
    // kingdom -> challengeId -> progress (for group challenges: member -> progress)
    private final Map<String, Map<String, Object>> kingdomProgress;
    // chainId -> List of challengeIds in order
    private final Map<String, List<String>> challengeChains;
    
    public AdvancedChallengeManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.challenges = new ConcurrentHashMap<>();
        this.kingdomProgress = new ConcurrentHashMap<>();
        this.challengeChains = new ConcurrentHashMap<>();
        loadChallenges();
        initializeWeeklyMonthlyChallenges();
    }
    
    private void loadChallenges() {
        List<Map<String, Object>> challengeData = plugin.getStorageManager().getAdapter().loadAdvancedChallenges();
        for (Map<String, Object> data : challengeData) {
            String challengeId = (String) data.get("challengeId");
            String name = (String) data.get("name");
            String description = (String) data.get("description");
            AdvancedChallenge.ChallengeType type = AdvancedChallenge.ChallengeType.valueOf(
                (String) data.get("type"));
            int difficulty = ((Number) data.get("difficulty")).intValue();
            int xpReward = ((Number) data.get("xpReward")).intValue();
            long startTime = ((Number) data.get("startTime")).longValue();
            long endTime = ((Number) data.get("endTime")).longValue();
            int requiredMembers = ((Number) data.getOrDefault("requiredMembers", 1)).intValue();
            String chainId = (String) data.get("chainId");
            int chainOrder = ((Number) data.getOrDefault("chainOrder", 0)).intValue();
            boolean active = (Boolean) data.getOrDefault("active", true);
            
            AdvancedChallenge challenge = new AdvancedChallenge(challengeId, name, description, type, 
                                                             difficulty, xpReward, endTime - startTime);
            challenge.setRequiredMembers(requiredMembers);
            challenge.setChainId(chainId);
            challenge.setChainOrder(chainOrder);
            challenge.setActive(active);
            
            challenges.put(challengeId, challenge);
            
            if (chainId != null) {
                challengeChains.computeIfAbsent(chainId, k -> new ArrayList<>()).add(challengeId);
            }
        }
    }
    
    /**
     * Initialize weekly and monthly challenges
     */
    private void initializeWeeklyMonthlyChallenges() {
        long now = System.currentTimeMillis() / 1000;
        
        // Check for expired weekly/monthly challenges and create new ones
        for (AdvancedChallenge challenge : challenges.values()) {
            if ((challenge.getType() == AdvancedChallenge.ChallengeType.WEEKLY || 
                 challenge.getType() == AdvancedChallenge.ChallengeType.MONTHLY) &&
                challenge.isExpired()) {
                // Reset challenge
                resetTimeLimitedChallenge(challenge);
            }
        }
        
        // Create default weekly challenges if none exist
        if (getActiveChallenges(AdvancedChallenge.ChallengeType.WEEKLY).isEmpty()) {
            createDefaultWeeklyChallenges();
        }
        
        // Create default monthly challenges if none exist
        if (getActiveChallenges(AdvancedChallenge.ChallengeType.MONTHLY).isEmpty()) {
            createDefaultMonthlyChallenges();
        }
    }
    
    private void createDefaultWeeklyChallenges() {
        // Weekly challenge: Complete 10 regular challenges
        createChallenge("weekly_challenges", "Weekly Challenge Master", 
                       "Complete 10 regular challenges this week", 
                       AdvancedChallenge.ChallengeType.WEEKLY, 3, 500, 7 * 24 * 3600);
        
        // Weekly challenge: Kingdom XP goal
        createChallenge("weekly_xp", "Weekly XP Goal", 
                       "Earn 5000 XP for your kingdom this week", 
                       AdvancedChallenge.ChallengeType.WEEKLY, 4, 750, 7 * 24 * 3600);
    }
    
    private void createDefaultMonthlyChallenges() {
        // Monthly challenge: Level up kingdom
        createChallenge("monthly_levelup", "Monthly Level Up", 
                       "Level up your kingdom this month", 
                       AdvancedChallenge.ChallengeType.MONTHLY, 5, 2000, 30 * 24 * 3600);
        
        // Monthly challenge: Complete 50 challenges
        createChallenge("monthly_challenges", "Monthly Challenge Master", 
                       "Complete 50 regular challenges this month", 
                       AdvancedChallenge.ChallengeType.MONTHLY, 6, 3000, 30 * 24 * 3600);
    }
    
    /**
     * Create a new advanced challenge
     */
    public boolean createChallenge(String challengeId, String name, String description, 
                                  AdvancedChallenge.ChallengeType type, int difficulty, 
                                  int xpReward, long duration) {
        if (challenges.containsKey(challengeId)) {
            return false; // Already exists
        }
        
        AdvancedChallenge challenge = new AdvancedChallenge(challengeId, name, description, 
                                                           type, difficulty, xpReward, duration);
        challenges.put(challengeId, challenge);
        saveChallenge(challenge);
        
        return true;
    }
    
    /**
     * Create a group challenge
     */
    public boolean createGroupChallenge(String challengeId, String name, String description, 
                                       int difficulty, int xpReward, int requiredMembers) {
        AdvancedChallenge challenge = new AdvancedChallenge(challengeId, name, description, 
                                                           AdvancedChallenge.ChallengeType.GROUP, 
                                                           difficulty, xpReward, 0);
        challenge.setRequiredMembers(requiredMembers);
        challenges.put(challengeId, challenge);
        saveChallenge(challenge);
        
        return true;
    }
    
    /**
     * Create a challenge chain
     */
    public boolean createChallengeChain(String chainId, List<String> challengeIds) {
        challengeChains.put(chainId, new ArrayList<>(challengeIds));
        
        // Set chain properties on challenges
        for (int i = 0; i < challengeIds.size(); i++) {
            AdvancedChallenge challenge = challenges.get(challengeIds.get(i));
            if (challenge != null) {
                challenge.setChainId(chainId);
                challenge.setChainOrder(i);
                
                // Set prerequisites
                if (i > 0) {
                    challenge.addPrerequisite(challengeIds.get(i - 1));
                }
                
                saveChallenge(challenge);
            }
        }
        
        return true;
    }
    
    /**
     * Update progress on a challenge
     */
    public void updateProgress(String kingdomName, String challengeId, String playerName, int amount) {
        AdvancedChallenge challenge = challenges.get(challengeId);
        if (challenge == null || !challenge.isActive()) return;
        
        if (challenge.getType() == AdvancedChallenge.ChallengeType.GROUP) {
            // Group challenge: track per-member progress
            Map<String, Object> progress = kingdomProgress.computeIfAbsent(kingdomName, k -> new ConcurrentHashMap<>());
            @SuppressWarnings("unchecked")
            Map<String, Integer> memberProgress = (Map<String, Integer>) progress.computeIfAbsent(
                challengeId, k -> new ConcurrentHashMap<String, Integer>());
            memberProgress.put(playerName, memberProgress.getOrDefault(playerName, 0) + amount);
        } else {
            // Individual challenge: track kingdom progress
            Map<String, Object> progress = kingdomProgress.computeIfAbsent(kingdomName, k -> new ConcurrentHashMap<>());
            int currentProgress = ((Number) progress.getOrDefault(challengeId, 0)).intValue();
            progress.put(challengeId, currentProgress + amount);
        }
        
        // Check if challenge is complete
        checkChallengeCompletion(kingdomName, challengeId);
    }
    
    /**
     * Check if a challenge is complete
     */
    private void checkChallengeCompletion(String kingdomName, String challengeId) {
        AdvancedChallenge challenge = challenges.get(challengeId);
        if (challenge == null) return;
        
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        
        boolean completed = false;
        
        if (challenge.getType() == AdvancedChallenge.ChallengeType.GROUP) {
            // Check if enough members have completed
            Map<String, Object> progress = kingdomProgress.get(kingdomName);
            if (progress != null) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> memberProgress = (Map<String, Integer>) progress.get(challengeId);
                if (memberProgress != null) {
                    int completedMembers = 0;
                    for (int memberProgressValue : memberProgress.values()) {
                        if (memberProgressValue >= challenge.getXpReward()) { // Use xpReward as target
                            completedMembers++;
                        }
                    }
                    completed = completedMembers >= challenge.getRequiredMembers();
                }
            }
        } else {
            // Check kingdom progress
            Map<String, Object> progress = kingdomProgress.get(kingdomName);
            if (progress != null) {
                int currentProgress = ((Number) progress.getOrDefault(challengeId, 0)).intValue();
                completed = currentProgress >= challenge.getXpReward(); // Use xpReward as target
            }
        }
        
        if (completed) {
            completeChallenge(kingdomName, challengeId);
        }
    }
    
    /**
     * Complete a challenge
     */
    private void completeChallenge(String kingdomName, String challengeId) {
        AdvancedChallenge challenge = challenges.get(challengeId);
        if (challenge == null) return;
        
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        
        // Award XP with structure bonuses
        int xpReward = challenge.getXpReward();
        if (plugin.getStructureManager() != null) {
            double throneBonus = plugin.getStructureManager()
                .getStructureBonus(kingdomName, com.excrele.kingdoms.model.KingdomStructure.StructureType.THRONE);
            xpReward = (int) (xpReward * throneBonus);
        }
        kingdom.addXp(xpReward);
        
        // Notify kingdom
        String message = "§a§l[Challenge Complete] §r§e" + challenge.getName() + 
            " §7- Earned §e" + challenge.getXpReward() + " XP";
        broadcastToKingdom(kingdom, message);
        
        // If chain challenge, unlock next in chain
        if (challenge.getChainId() != null) {
            unlockNextInChain(kingdomName, challenge.getChainId(), challenge.getChainOrder());
        }
        
        // Reset progress
        Map<String, Object> progress = kingdomProgress.get(kingdomName);
        if (progress != null) {
            progress.remove(challengeId);
        }
    }
    
    /**
     * Unlock next challenge in chain
     */
    private void unlockNextInChain(String kingdomName, String chainId, int currentOrder) {
        List<String> chain = challengeChains.get(chainId);
        if (chain == null) return;
        
        int nextIndex = currentOrder + 1;
        if (nextIndex < chain.size()) {
            String nextChallengeId = chain.get(nextIndex);
            AdvancedChallenge nextChallenge = challenges.get(nextChallengeId);
            if (nextChallenge != null) {
                nextChallenge.setActive(true);
                saveChallenge(nextChallenge);
                
                Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
                if (kingdom != null) {
                    String message = "§6[Chain Challenge] §e" + nextChallenge.getName() + 
                        " §7is now available!";
                    broadcastToKingdom(kingdom, message);
                }
            }
        }
    }
    
    /**
     * Get active challenges of a type
     */
    public List<AdvancedChallenge> getActiveChallenges(AdvancedChallenge.ChallengeType type) {
        List<AdvancedChallenge> result = new ArrayList<>();
        for (AdvancedChallenge challenge : challenges.values()) {
            if (challenge.getType() == type && challenge.isActive() && !challenge.isExpired()) {
                result.add(challenge);
            }
        }
        return result;
    }
    
    /**
     * Get all active challenges for a kingdom
     */
    public List<AdvancedChallenge> getKingdomChallenges(String kingdomName) {
        List<AdvancedChallenge> result = new ArrayList<>();
        for (AdvancedChallenge challenge : challenges.values()) {
            if (challenge.isActive() && !challenge.isExpired()) {
                result.add(challenge);
            }
        }
        return result;
    }
    
    /**
     * Reset time-limited challenge
     */
    private void resetTimeLimitedChallenge(AdvancedChallenge challenge) {
        long duration = challenge.getEndTime() - challenge.getStartTime();
        challenge = new AdvancedChallenge(challenge.getChallengeId(), challenge.getName(), 
                                         challenge.getDescription(), challenge.getType(), 
                                         challenge.getDifficulty(), challenge.getXpReward(), duration);
        challenge.setRequiredMembers(challenge.getRequiredMembers());
        challenges.put(challenge.getChallengeId(), challenge);
        saveChallenge(challenge);
    }
    
    /**
     * Check and reset expired weekly/monthly challenges
     */
    public void checkExpiredChallenges() {
        for (AdvancedChallenge challenge : challenges.values()) {
            if ((challenge.getType() == AdvancedChallenge.ChallengeType.WEEKLY || 
                 challenge.getType() == AdvancedChallenge.ChallengeType.MONTHLY) &&
                challenge.isExpired()) {
                resetTimeLimitedChallenge(challenge);
            }
        }
    }
    
    private void broadcastToKingdom(Kingdom kingdom, String message) {
        for (String member : kingdom.getMembers()) {
            Player player = plugin.getServer().getPlayer(member);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
        Player king = plugin.getServer().getPlayer(kingdom.getKing());
        if (king != null && king.isOnline()) {
            king.sendMessage(message);
        }
    }
    
    private void saveChallenge(AdvancedChallenge challenge) {
        plugin.getStorageManager().getAdapter().saveAdvancedChallenge(
            challenge.getChallengeId(),
            challenge.getName(),
            challenge.getDescription(),
            challenge.getType().name(),
            challenge.getDifficulty(),
            challenge.getXpReward(),
            challenge.getStartTime(),
            challenge.getEndTime(),
            challenge.getRequiredMembers(),
            challenge.getChainId(),
            challenge.getChainOrder(),
            challenge.isActive()
        );
    }
}

