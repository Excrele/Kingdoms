package com.excrele.kingdoms.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Challenge;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.PlayerChallengeData;
import com.excrele.kingdoms.util.ErrorHandler;
import com.excrele.kingdoms.util.SaveQueue;

public class ChallengeManager {
    private final List<Challenge> challenges;
    private Map<String, Map<String, PlayerChallengeData>> playerData;
    private Map<String, List<Challenge>> eventToChallenges;
    private final FileConfiguration playerDataConfig;
    private final File playerDataFile;
    private final ErrorHandler errorHandler;
    private SaveQueue saveQueue;

    public ChallengeManager(KingdomsPlugin plugin) {
        this.challenges = new ArrayList<>();
        this.playerData = new HashMap<>();
        this.eventToChallenges = new HashMap<>();
        this.playerDataFile = new File(plugin.getDataFolder(), "player_data.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create player_data.yml!");
            }
        }
        this.playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
        this.errorHandler = new ErrorHandler(plugin);
        loadChallenges(plugin.getConfig());
        loadPlayerData();
    }
    
    /**
     * Set the save queue for async operations
     */
    public void setSaveQueue(SaveQueue saveQueue) {
        this.saveQueue = saveQueue;
    }

    private void loadChallenges(FileConfiguration config) {
        if (!config.contains("challenges")) return;
        org.bukkit.configuration.ConfigurationSection challengesSection = config.getConfigurationSection("challenges");
        if (challengesSection == null) return;
        for (String key : challengesSection.getKeys(false)) {
            String path = "challenges." + key;
            String description = config.getString(path + ".description");
            if (description == null) continue;
            org.bukkit.configuration.ConfigurationSection taskSection = config.getConfigurationSection(path + ".task");
            if (taskSection == null) continue;
            Challenge challenge = new Challenge(
                    key,
                    description,
                    config.getInt(path + ".difficulty"),
                    config.getInt(path + ".xp_reward"),
                    taskSection.getValues(false)
            );
            challenges.add(challenge);
            String eventType = (String) challenge.getTask().get("type");
            if (eventType != null) {
                eventToChallenges.computeIfAbsent(eventType, k -> new ArrayList<>()).add(challenge);
            }
        }
    }

    private void loadPlayerData() {
        if (!playerDataConfig.contains("players")) return;
        org.bukkit.configuration.ConfigurationSection playersSection = playerDataConfig.getConfigurationSection("players");
        if (playersSection == null) return;
        for (String player : playersSection.getKeys(false)) {
            Map<String, PlayerChallengeData> challengeData = new HashMap<>();
            String path = "players." + player + ".completions";
            org.bukkit.configuration.ConfigurationSection completionsSection = playerDataConfig.getConfigurationSection(path);
            if (completionsSection == null) continue;
            for (String challengeId : completionsSection.getKeys(false)) {
                String cPath = path + "." + challengeId;
                PlayerChallengeData data = new PlayerChallengeData();
                data.setTimesCompleted(playerDataConfig.getInt(cPath + ".times"));
                data.setLastCompleted(playerDataConfig.getLong(cPath + ".last_completed"));
                data.setProgress(playerDataConfig.getInt(cPath + ".progress"));
                challengeData.put(challengeId, data);
            }
            playerData.put(player, challengeData);
        }
    }

    public void savePlayerData() {
        savePlayerData(false);
    }
    
    /**
     * Save player data synchronously or asynchronously
     */
    public void savePlayerData(boolean async) {
        if (async && saveQueue != null) {
            saveQueue.enqueue(this::performSave);
        } else {
            performSave();
        }
    }
    
    /**
     * Perform the actual save operation (can be called async)
     */
    private void performSave() {
        playerDataConfig.set("players", null);
        for (Map.Entry<String, Map<String, PlayerChallengeData>> entry : playerData.entrySet()) {
            String player = entry.getKey();
            for (Map.Entry<String, PlayerChallengeData> challengeEntry : entry.getValue().entrySet()) {
                String path = "players." + player + ".completions." + challengeEntry.getKey();
                PlayerChallengeData data = challengeEntry.getValue();
                playerDataConfig.set(path + ".times", data.getTimesCompleted());
                playerDataConfig.set(path + ".last_completed", data.getLastCompleted());
                playerDataConfig.set(path + ".progress", data.getProgress());
            }
        }
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            errorHandler.handleSaveError("save player challenge data", e, this::performSave);
        }
    }

    public boolean isChallengeOnCooldown(Player player, Challenge challenge) {
        PlayerChallengeData data = getPlayerChallengeData(player, challenge);
        if (data == null || data.getTimesCompleted() == 0) return false;
        long currentTime = System.currentTimeMillis() / 1000;
        long baseCooldown = KingdomsPlugin.getInstance().getConfig().getLong("cooldown.base", 86400);
        long increment = KingdomsPlugin.getInstance().getConfig().getLong("cooldown.increment", 86400);
        long cooldown = baseCooldown + (data.getTimesCompleted() - 1) * increment;
        return (currentTime - data.getLastCompleted()) < cooldown;
    }

    public void updateChallengeProgress(Player player, Challenge challenge, int amount) {
        if (isChallengeOnCooldown(player, challenge)) return;
        PlayerChallengeData data = getPlayerChallengeData(player, challenge);
        if (data == null) {
            data = new PlayerChallengeData();
            playerData.computeIfAbsent(player.getName(), k -> new HashMap<>()).put(challenge.getId(), data);
        }
        int requiredAmount = (int) challenge.getTask().get("amount");
        int previousProgress = data.getProgress();
        data.incrementProgress(amount);
        if (data.getProgress() >= requiredAmount) {
            completeChallenge(player, challenge);
            data.setProgress(0); // Reset progress after completion
        } else if (previousProgress != data.getProgress()) {
            // Update action bar with progress
            com.excrele.kingdoms.util.ActionBarManager.sendChallengeProgress(
                player, 
                challenge.getDescription(), 
                data.getProgress(), 
                requiredAmount
            );
            // Only show chat message every 25% progress to avoid spam
            int progressPercent = (data.getProgress() * 100) / requiredAmount;
            int prevProgressPercent = (previousProgress * 100) / requiredAmount;
            if (progressPercent % 25 == 0 && prevProgressPercent < progressPercent) {
                player.sendMessage("§7Progress: §e" + data.getProgress() + "§7/§e" + requiredAmount + " §7(" + progressPercent + "%) for §6" + challenge.getDescription());
            }
        }
        savePlayerData(true); // Use async save
    }

    public void completeChallenge(Player player, Challenge challenge) {
        String kingdomName = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null) return;
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        int xpReward = challenge.getXpReward();
        kingdom.addXp(xpReward);
        kingdom.addContribution(player.getName(), xpReward); // Track individual contribution
        kingdom.incrementChallengesCompleted(); // Track total challenges
        PlayerChallengeData data = getPlayerChallengeData(player, challenge);
        if (data == null) {
            data = new PlayerChallengeData();
            playerData.computeIfAbsent(player.getName(), k -> new HashMap<>()).put(challenge.getId(), data);
        }
        data.setTimesCompleted(data.getTimesCompleted() + 1);
        data.setLastCompleted(System.currentTimeMillis() / 1000);
        
        // Visual effects
        com.excrele.kingdoms.util.VisualEffects.playChallengeCompleteEffects(player);
        
        // Action bar notification
        com.excrele.kingdoms.util.ActionBarManager.sendChallengeComplete(player, challenge.getDescription(), xpReward);
        
        player.sendMessage("§aChallenge completed: " + challenge.getDescription() + "! +" + xpReward + " XP");
        savePlayerData(true); // Use async save
        KingdomsPlugin.getInstance().getKingdomManager().saveKingdoms(
                KingdomsPlugin.getInstance().getKingdomsConfig(),
                KingdomsPlugin.getInstance().getKingdomsFile(),
                true // Use async save
        );
    }

    public PlayerChallengeData getPlayerChallengeData(Player player, Challenge challenge) {
        Map<String, PlayerChallengeData> playerChallenges = playerData.get(player.getName());
        return playerChallenges != null ? playerChallenges.get(challenge.getId()) : null;
    }

    public List<Challenge> getChallenges() { return challenges; }
    public Map<String, List<Challenge>> getEventToChallenges() { return eventToChallenges; }
}