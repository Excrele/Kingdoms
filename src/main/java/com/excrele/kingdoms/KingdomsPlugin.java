package com.excrele.kingdoms;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.excrele.kingdoms.command.KingdomCommand;
import com.excrele.kingdoms.command.LevelUpCommand;
import com.excrele.kingdoms.integration.DiscordSRVIntegration;
import com.excrele.kingdoms.integration.DynmapIntegration;
import com.excrele.kingdoms.integration.GriefPreventionIntegration;
import com.excrele.kingdoms.integration.UnminedIntegration;
import com.excrele.kingdoms.integration.WorldGuardIntegration;
import com.excrele.kingdoms.listener.BlockBreakListener;
import com.excrele.kingdoms.listener.CraftItemListener;
import com.excrele.kingdoms.listener.EntityDeathListener;
import com.excrele.kingdoms.listener.InventoryClickListener;
import com.excrele.kingdoms.listener.PlayerMoveListener;
import com.excrele.kingdoms.manager.ActivityManager;
import com.excrele.kingdoms.manager.AdvancedFeaturesManager;
import com.excrele.kingdoms.manager.AdvancedMemberManager;
import com.excrele.kingdoms.manager.BankManager;
import com.excrele.kingdoms.manager.ChallengeManager;
import com.excrele.kingdoms.manager.ClaimEconomyManager;
import com.excrele.kingdoms.manager.ClaimManager;
import com.excrele.kingdoms.manager.CommunicationManager;
import com.excrele.kingdoms.manager.CustomizationManager;
import com.excrele.kingdoms.manager.FlagManager;
import com.excrele.kingdoms.manager.KingdomManager;
import com.excrele.kingdoms.manager.StatisticsManager;
import com.excrele.kingdoms.manager.TrustManager;
import com.excrele.kingdoms.manager.VaultManager;
import com.excrele.kingdoms.manager.WarManager;
import com.excrele.kingdoms.manager.WorldManager;
import com.excrele.kingdoms.storage.StorageManager;
import com.excrele.kingdoms.task.BatchSaveTask;
import com.excrele.kingdoms.task.PerkTask;
import com.excrele.kingdoms.util.SaveQueue;

public class KingdomsPlugin extends JavaPlugin {
    private static KingdomsPlugin instance;
    private KingdomManager kingdomManager;
    private ClaimManager claimManager;
    private FlagManager flagManager;
    private ChallengeManager challengeManager;
    private TrustManager trustManager;
    private WarManager warManager;
    private BankManager bankManager;
    private ClaimEconomyManager claimEconomyManager;
    private VaultManager vaultManager;
    private ActivityManager activityManager;
    private WorldManager worldManager;
    private CustomizationManager customizationManager;
    private CommunicationManager communicationManager;
    private com.excrele.kingdoms.manager.VisualManager visualManager;
    private AdvancedMemberManager advancedMemberManager;
    private StatisticsManager statisticsManager;
    private AdvancedFeaturesManager advancedFeaturesManager;
    private com.excrele.kingdoms.manager.AchievementManager achievementManager;
    private com.excrele.kingdoms.manager.EnhancedLeaderboardManager enhancedLeaderboardManager;
    private com.excrele.kingdoms.manager.MailManager mailManager;
    private com.excrele.kingdoms.manager.SiegeManager siegeManager;
    private com.excrele.kingdoms.manager.RaidManager raidManager;
    private com.excrele.kingdoms.manager.TaxManager taxManager;
    private com.excrele.kingdoms.manager.TradeRouteManager tradeRouteManager;
    private com.excrele.kingdoms.manager.AdvancedChallengeManager advancedChallengeManager;
    private com.excrele.kingdoms.manager.StructureManager structureManager;
    private com.excrele.kingdoms.manager.ResourceManager resourceManager;
    private com.excrele.kingdoms.manager.DiplomacyManager diplomacyManager;
    private com.excrele.kingdoms.util.HologramManager hologramManager;
    private com.excrele.kingdoms.manager.ThemeManager themeManager;
    private com.excrele.kingdoms.manager.BannerManager bannerManager;
    private com.excrele.kingdoms.util.DataCache dataCache;
    private com.excrele.kingdoms.util.ChunkLoadingOptimizer chunkOptimizer;
    private DynmapIntegration dynmapIntegration;
    private UnminedIntegration unminedIntegration;
    private WorldGuardIntegration worldGuardIntegration;
    private GriefPreventionIntegration griefPreventionIntegration;
    private DiscordSRVIntegration discordSRVIntegration;
    private StorageManager storageManager;
    private FileConfiguration kingdomsConfig;
    private File kingdomsFile;
    private SaveQueue saveQueue;
    private BatchSaveTask batchSaveTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        initializeDataFiles();
        initializeManagers();
        registerCommands();
        registerListeners();
        startTasks();
        registerPlaceholderAPI();
        setupEconomy();
        getLogger().info("KingdomsPlugin enabled!");
    }

    @Override
    public void onDisable() {
        // Process any remaining queued saves before shutdown
        if (batchSaveTask != null) {
            batchSaveTask.saveNow();
        }
        
        // Force synchronous saves on shutdown
        kingdomManager.saveKingdoms(kingdomsConfig, kingdomsFile, false);
        challengeManager.savePlayerData(false);
        if (advancedMemberManager != null) {
            advancedMemberManager.saveAllData(false);
        }
        
        // Disable integrations
        if (dynmapIntegration != null) dynmapIntegration.disable();
        if (unminedIntegration != null) unminedIntegration.disable();
        if (worldGuardIntegration != null) worldGuardIntegration.disable();
        if (griefPreventionIntegration != null) griefPreventionIntegration.disable();
        if (discordSRVIntegration != null) discordSRVIntegration.disable();
        
        if (storageManager != null) {
            storageManager.close();
        }
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
        // Initialize save queue for async operations
        saveQueue = new SaveQueue();
        
        storageManager = new StorageManager(this);
        kingdomManager = new KingdomManager(this, kingdomsConfig, kingdomsFile); // Pass plugin instance
        kingdomManager.setSaveQueue(saveQueue); // Set save queue
        worldManager = new WorldManager(this);
        claimManager = new ClaimManager(kingdomManager);
        claimManager.setWorldManager(worldManager); // Set world manager after creation
        flagManager = new FlagManager(this);
        challengeManager = new ChallengeManager(this);
        challengeManager.setSaveQueue(saveQueue); // Set save queue
        trustManager = new TrustManager(this);
        warManager = new WarManager(this);
        bankManager = new BankManager(this);
        claimEconomyManager = new ClaimEconomyManager(this, kingdomManager, claimManager, bankManager);
        vaultManager = new VaultManager(this);
        activityManager = new ActivityManager(this);
        customizationManager = new CustomizationManager(this);
        communicationManager = new CommunicationManager(this);
        visualManager = new com.excrele.kingdoms.manager.VisualManager(this);
        advancedMemberManager = new AdvancedMemberManager(this);
        advancedMemberManager.setSaveQueue(saveQueue); // Set save queue
        statisticsManager = new StatisticsManager(this);
        advancedFeaturesManager = new AdvancedFeaturesManager(this);
        achievementManager = new com.excrele.kingdoms.manager.AchievementManager(this);
        enhancedLeaderboardManager = new com.excrele.kingdoms.manager.EnhancedLeaderboardManager(this);
        mailManager = new com.excrele.kingdoms.manager.MailManager(this);
        siegeManager = new com.excrele.kingdoms.manager.SiegeManager(this);
        raidManager = new com.excrele.kingdoms.manager.RaidManager(this);
        taxManager = new com.excrele.kingdoms.manager.TaxManager(this);
        tradeRouteManager = new com.excrele.kingdoms.manager.TradeRouteManager(this);
        advancedChallengeManager = new com.excrele.kingdoms.manager.AdvancedChallengeManager(this);
        structureManager = new com.excrele.kingdoms.manager.StructureManager(this);
        resourceManager = new com.excrele.kingdoms.manager.ResourceManager(this);
        diplomacyManager = new com.excrele.kingdoms.manager.DiplomacyManager(this);
        hologramManager = new com.excrele.kingdoms.util.HologramManager(this);
        themeManager = new com.excrele.kingdoms.manager.ThemeManager(this);
        bannerManager = new com.excrele.kingdoms.manager.BannerManager(this);
        dataCache = new com.excrele.kingdoms.util.DataCache(getConfig().getLong("cache.expiry-time", 300000L)); // 5 minutes
        chunkOptimizer = new com.excrele.kingdoms.util.ChunkLoadingOptimizer(this);
        
        // Initialize integrations
        dynmapIntegration = new DynmapIntegration(this);
        unminedIntegration = new UnminedIntegration(this);
        worldGuardIntegration = new WorldGuardIntegration(this);
        griefPreventionIntegration = new GriefPreventionIntegration(this);
        discordSRVIntegration = new DiscordSRVIntegration(this);
        
        // Enable available integrations
        if (dynmapIntegration.isAvailable()) dynmapIntegration.enable();
        if (unminedIntegration.isAvailable()) unminedIntegration.enable();
        if (worldGuardIntegration.isAvailable()) worldGuardIntegration.enable();
        if (griefPreventionIntegration.isAvailable()) griefPreventionIntegration.enable();
        if (discordSRVIntegration.isAvailable()) discordSRVIntegration.enable();
        
        com.excrele.kingdoms.gui.VaultGUI vaultGUI = new com.excrele.kingdoms.gui.VaultGUI(this, vaultManager); // Register vault GUI listener
        getServer().getPluginManager().registerEvents(vaultGUI, this);
    }

    private void registerCommands() {
        KingdomCommand kingdomCommand = new KingdomCommand(this);
        com.excrele.kingdoms.command.KingdomCommandTabCompleter kingdomTabCompleter = 
            new com.excrele.kingdoms.command.KingdomCommandTabCompleter(this);
        
        org.bukkit.command.PluginCommand kingdomCmd = getCommand("kingdom");
        if (kingdomCmd != null) {
            kingdomCmd.setExecutor(kingdomCommand);
            kingdomCmd.setTabCompleter(kingdomTabCompleter);
        }
        org.bukkit.command.PluginCommand kCmd = getCommand("k");
        if (kCmd != null) {
            kCmd.setExecutor(kingdomCommand); // Alias
            kCmd.setTabCompleter(kingdomTabCompleter); // Same tab completer for alias
        }
        org.bukkit.command.PluginCommand kcCmd = getCommand("kc");
        if (kcCmd != null) {
            kcCmd.setExecutor(new com.excrele.kingdoms.command.KingdomChatCommand());
            kcCmd.setTabCompleter(new com.excrele.kingdoms.command.KingdomChatCommandTabCompleter());
        }
        org.bukkit.command.PluginCommand kingdomChatCmd = getCommand("kingdomchat");
        if (kingdomChatCmd != null) {
            kingdomChatCmd.setExecutor(new com.excrele.kingdoms.command.KingdomChatCommand());
            kingdomChatCmd.setTabCompleter(new com.excrele.kingdoms.command.KingdomChatCommandTabCompleter());
        }
        org.bukkit.command.PluginCommand levelUpCmd = getCommand("levelup");
        if (levelUpCmd != null) {
            levelUpCmd.setExecutor(new LevelUpCommand());
            levelUpCmd.setTabCompleter(new com.excrele.kingdoms.command.LevelUpCommandTabCompleter());
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
        getServer().getPluginManager().registerEvents(new com.excrele.kingdoms.listener.EnhancedProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new com.excrele.kingdoms.listener.ActivityListener(this), this);
    }

    private void startTasks() {
        new PerkTask().runTaskTimer(this, 0L, 100L); // Every 5 seconds
        new com.excrele.kingdoms.task.AutoFeaturesTask().runTaskTimer(this, 0L, 20L); // Every second
        
        // Start batch save task (every 60 seconds = 1200 ticks)
        long saveInterval = getConfig().getLong("batch-save-interval", 1200L);
        batchSaveTask = new BatchSaveTask(this, saveQueue, saveInterval);
        batchSaveTask.start();
        
        // Check for season end every hour (72000 ticks)
        if (enhancedLeaderboardManager != null) {
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    enhancedLeaderboardManager.checkSeasonEnd();
                }
            }.runTaskTimer(this, 0L, 72000L);
        }
    }

    private void registerPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new com.excrele.kingdoms.expansion.KingdomsExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered!");
        }
    }

    private void setupEconomy() {
        if (com.excrele.kingdoms.util.EconomyManager.setupEconomy()) {
            getLogger().info("Vault economy integration enabled!");
        } else {
            getLogger().info("Vault not found - economy features disabled.");
        }
    }

    public static KingdomsPlugin getInstance() { return instance; }
    public KingdomManager getKingdomManager() { return kingdomManager; }
    public ClaimManager getClaimManager() { return claimManager; }
    public FlagManager getFlagManager() { return flagManager; }
    public FileConfiguration getKingdomsConfig() { return kingdomsConfig; }
    public File getKingdomsFile() { return kingdomsFile; }
    public ChallengeManager getChallengeManager() { return challengeManager; }
    public TrustManager getTrustManager() { return trustManager; }
    public WarManager getWarManager() { return warManager; }
    public BankManager getBankManager() { return bankManager; }
    public ClaimEconomyManager getClaimEconomyManager() { return claimEconomyManager; }
    public VaultManager getVaultManager() { return vaultManager; }
    public ActivityManager getActivityManager() { return activityManager; }
    public WorldManager getWorldManager() { return worldManager; }
    public CustomizationManager getCustomizationManager() { return customizationManager; }
    public CommunicationManager getCommunicationManager() { return communicationManager; }
    public com.excrele.kingdoms.manager.VisualManager getVisualManager() { return visualManager; }
    public AdvancedMemberManager getAdvancedMemberManager() { return advancedMemberManager; }
    public StatisticsManager getStatisticsManager() { return statisticsManager; }
    public AdvancedFeaturesManager getAdvancedFeaturesManager() { return advancedFeaturesManager; }
    public com.excrele.kingdoms.manager.AchievementManager getAchievementManager() { return achievementManager; }
    public com.excrele.kingdoms.manager.EnhancedLeaderboardManager getEnhancedLeaderboardManager() { return enhancedLeaderboardManager; }
    public com.excrele.kingdoms.manager.MailManager getMailManager() { return mailManager; }
    public com.excrele.kingdoms.manager.SiegeManager getSiegeManager() { return siegeManager; }
    public com.excrele.kingdoms.manager.RaidManager getRaidManager() { return raidManager; }
    public com.excrele.kingdoms.manager.TaxManager getTaxManager() { return taxManager; }
    public com.excrele.kingdoms.manager.TradeRouteManager getTradeRouteManager() { return tradeRouteManager; }
    public com.excrele.kingdoms.manager.AdvancedChallengeManager getAdvancedChallengeManager() { return advancedChallengeManager; }
    public com.excrele.kingdoms.manager.StructureManager getStructureManager() { return structureManager; }
    public com.excrele.kingdoms.manager.ResourceManager getResourceManager() { return resourceManager; }
    public com.excrele.kingdoms.manager.DiplomacyManager getDiplomacyManager() { return diplomacyManager; }
    public com.excrele.kingdoms.util.HologramManager getHologramManager() { return hologramManager; }
    public com.excrele.kingdoms.manager.ThemeManager getThemeManager() { return themeManager; }
    public com.excrele.kingdoms.manager.BannerManager getBannerManager() { return bannerManager; }
    public com.excrele.kingdoms.util.DataCache getDataCache() { return dataCache; }
    public com.excrele.kingdoms.util.ChunkLoadingOptimizer getChunkOptimizer() { return chunkOptimizer; }
    public DynmapIntegration getDynmapIntegration() { return dynmapIntegration; }
    public UnminedIntegration getUnminedIntegration() { return unminedIntegration; }
    public WorldGuardIntegration getWorldGuardIntegration() { return worldGuardIntegration; }
    public GriefPreventionIntegration getGriefPreventionIntegration() { return griefPreventionIntegration; }
    public DiscordSRVIntegration getDiscordSRVIntegration() { return discordSRVIntegration; }
    public StorageManager getStorageManager() { return storageManager; }
}