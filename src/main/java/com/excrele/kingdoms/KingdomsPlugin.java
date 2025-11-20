package com.excrele.kingdoms;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.excrele.kingdoms.command.KingdomCommand;
import com.excrele.kingdoms.command.LevelUpCommand;
import com.excrele.kingdoms.listener.BlockBreakListener;
import com.excrele.kingdoms.listener.CraftItemListener;
import com.excrele.kingdoms.listener.EntityDeathListener;
import com.excrele.kingdoms.listener.InventoryClickListener;
import com.excrele.kingdoms.listener.PlayerMoveListener;
import com.excrele.kingdoms.manager.ChallengeManager;
import com.excrele.kingdoms.manager.ClaimManager;
import com.excrele.kingdoms.manager.FlagManager;
import com.excrele.kingdoms.manager.KingdomManager;
import com.excrele.kingdoms.task.PerkTask;

public class KingdomsPlugin extends JavaPlugin {
    private static KingdomsPlugin instance;
    private KingdomManager kingdomManager;
    private ClaimManager claimManager;
    private FlagManager flagManager;
    private ChallengeManager challengeManager;
    private FileConfiguration kingdomsConfig;
    private File kingdomsFile;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        initializeDataFiles();
        initializeManagers();
        registerCommands();
        registerListeners();
        startTasks();
        getLogger().info("KingdomsPlugin enabled!");
    }

    @Override
    public void onDisable() {
        kingdomManager.saveKingdoms(kingdomsConfig, kingdomsFile);
        challengeManager.savePlayerData();
        getLogger().info("KingdomsPlugin disabled!");
    }

    private void initializeDataFiles() {
        kingdomsFile = new File(getDataFolder(), "kingdoms.yml");
        if (!kingdomsFile.exists()) {
            try {
                kingdomsFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create kingdoms.yml!");
            }
        }
        kingdomsConfig = YamlConfiguration.loadConfiguration(kingdomsFile);
    }

    private void initializeManagers() {
        kingdomManager = new KingdomManager(this, kingdomsConfig, kingdomsFile); // Pass plugin instance
        claimManager = new ClaimManager(kingdomManager);
        flagManager = new FlagManager(this);
        challengeManager = new ChallengeManager(this);
    }

    private void registerCommands() {
        KingdomCommand kingdomCommand = new KingdomCommand(this);
        org.bukkit.command.PluginCommand kingdomCmd = getCommand("kingdom");
        if (kingdomCmd != null) {
            kingdomCmd.setExecutor(kingdomCommand);
        }
        org.bukkit.command.PluginCommand kCmd = getCommand("k");
        if (kCmd != null) {
            kCmd.setExecutor(kingdomCommand); // Alias
        }
        org.bukkit.command.PluginCommand kcCmd = getCommand("kc");
        if (kcCmd != null) {
            kcCmd.setExecutor(new com.excrele.kingdoms.command.KingdomChatCommand());
        }
        org.bukkit.command.PluginCommand levelUpCmd = getCommand("levelup");
        if (levelUpCmd != null) {
            levelUpCmd.setExecutor(new LevelUpCommand());
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(), this);
        getServer().getPluginManager().registerEvents(new CraftItemListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new com.excrele.kingdoms.listener.KingdomChatListener(), this);
        getServer().getPluginManager().registerEvents(new com.excrele.kingdoms.listener.ClaimProtectionListener(), this);
    }

    private void startTasks() {
        new PerkTask().runTaskTimer(this, 0L, 100L); // Every 5 seconds
    }

    public static KingdomsPlugin getInstance() { return instance; }
    public KingdomManager getKingdomManager() { return kingdomManager; }
    public ClaimManager getClaimManager() { return claimManager; }
    public FlagManager getFlagManager() { return flagManager; }
    public FileConfiguration getKingdomsConfig() { return kingdomsConfig; }
    public File getKingdomsFile() { return kingdomsFile; }
    public ChallengeManager getChallengeManager() { return challengeManager; }
}