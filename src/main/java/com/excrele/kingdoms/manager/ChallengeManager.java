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

public class ChallengeManager {
    private List<Challenge> challenges;
    private Map<String, Map<String, PlayerChallengeData>> playerData;
    private Map<String, List<Challenge>> eventToChallenges;
    private FileConfiguration playerDataConfig;
    private File playerDataFile;

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
        loadChallenges(plugin.getConfig());
        loadPlayerData();
    }

    private void loadChallenges(FileConfiguration config) {
        if (!config.contains("challenges")) return;
        for (String key : config.getConfigurationSection("challenges").getKeys(false)) {
            String path = "challenges." + key;
            Challenge challenge = new Challenge(
                    key,
                    config.getString(path + ".description"),
                    config.getInt(path + ".difficulty"),
                    config.getInt(path + ".xp_reward"),
                    config.getConfigurationSection(path + ".task").getValues(false)
            );
            challenges.add(challenge);
            String eventType = (String) challenge.getTask().get("type");
            eventToChallenges.computeIfAbsent(eventType, k -> new ArrayList<>()).add(challenge);
        }
    }

    private void loadPlayerData() {
        if (!playerDataConfig.contains("players")) return;
        for (String player : playerDataConfig.getConfigurationSection("players").getKeys(false)) {
            Map<String, PlayerChallengeData> challengeData = new HashMap<>();
            String path = "players." + player + ".completions";
            for (String challengeId : playerDataConfig.getConfigurationSection(path).getKeys(false)) {
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
            e.printStackTrace();
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
            // Only show progress message if progress actually changed and challenge not completed
            player.sendMessage("Progress: " + data.getProgress() + "/" + requiredAmount + " for " + challenge.getDescription());
        }
        savePlayerData();
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
        player.sendMessage("§aChallenge completed: " + challenge.getDescription() + "! +" + xpReward + " XP");
        savePlayerData();
        KingdomsPlugin.getInstance().getKingdomManager().saveKingdoms(
                KingdomsPlugin.getInstance().getKingdomsConfig(),
                KingdomsPlugin.getInstance().getKingdomsFile()
        );
    }

    public PlayerChallengeData getPlayerChallengeData(Player player, Challenge challenge) {
        Map<String, PlayerChallengeData> challenges = playerData.get(player.getName());
        return challenges != null ? challenges.get(challenge.getId()) : null;
    }

    public List<Challenge> getChallenges() { return challenges; }
    public Map<String, List<Challenge>> getEventToChallenges() { return eventToChallenges; }
}