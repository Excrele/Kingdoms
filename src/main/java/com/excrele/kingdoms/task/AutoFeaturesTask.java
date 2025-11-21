package com.excrele.kingdoms.task;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoFeaturesTask extends BukkitRunnable {
    private int tickCount = 0;

    @Override
    public void run() {
        tickCount++;
        KingdomsPlugin plugin = KingdomsPlugin.getInstance();
        
        // Check if auto-features are enabled
        if (!plugin.getConfig().getBoolean("economy.auto_features.enabled", true)) {
            return;
        }

        // Auto-level-up check (every 5 seconds)
        if (tickCount % 100 == 0) {
            checkAutoLevelUp(plugin);
        }

        // Auto-kick inactive members (every 5 minutes)
        if (tickCount % 6000 == 0) {
            checkAutoKickInactive(plugin);
        }

        // Check for expired wars (every 30 seconds)
        if (tickCount % 600 == 0) {
            plugin.getWarManager().checkExpiredWars();
        }
        
        // Check for expired auctions and rents (every 30 seconds)
        if (tickCount % 600 == 0) {
            plugin.getClaimEconomyManager().checkExpiredAuctions();
            plugin.getClaimEconomyManager().checkExpiredRents();
        }
        
        // Check for upcoming events (every 60 seconds)
        if (tickCount % 1200 == 0) {
            plugin.getCommunicationManager().checkUpcomingEvents();
        }
        
        // Record growth snapshots (once per day)
        if (tickCount % 172800 == 0) { // 24 hours = 172800 ticks (at 20 TPS)
            for (String kingdomName : plugin.getKingdomManager().getKingdoms().keySet()) {
                plugin.getStatisticsManager().recordGrowthSnapshot(kingdomName);
            }
        }
        
        // Process farms (every 5 minutes)
        if (tickCount % 6000 == 0 && plugin.getAdvancedFeaturesManager() != null) {
            processFarms(plugin);
        }
    }
    
    private void processFarms(KingdomsPlugin plugin) {
        // Auto-harvest farms that are ready
        for (String kingdomName : plugin.getKingdomManager().getKingdoms().keySet()) {
            java.util.Map<String, com.excrele.kingdoms.model.KingdomFarm> farms = 
                plugin.getAdvancedFeaturesManager().getFarms(kingdomName);
            if (farms != null) {
                for (com.excrele.kingdoms.model.KingdomFarm farm : farms.values()) {
                    if (farm.canHarvest()) {
                        // Auto-harvest logic would go here
                        // For now, just update last harvest time
                        farm.setLastHarvest(System.currentTimeMillis() / 1000);
                    }
                }
            }
        }
    }

    private void checkAutoLevelUp(KingdomsPlugin plugin) {
        if (!plugin.getConfig().getBoolean("economy.auto_features.auto_levelup", false)) {
            return;
        }

        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            int currentLevel = kingdom.getLevel();
            int requiredXp = currentLevel * currentLevel * 1000;
            
            if (kingdom.getXp() >= requiredXp) {
                // Check economy cost if enabled
                double levelupCost = plugin.getConfig().getDouble("economy.levelup_cost", 0.0);
                boolean economyEnabled = plugin.getConfig().getBoolean("economy.enabled", false);
                
                if (economyEnabled && levelupCost > 0 && com.excrele.kingdoms.util.EconomyManager.isEnabled()) {
                    // Try to get king to pay
                    Player king = plugin.getServer().getPlayer(kingdom.getKing());
                    if (king != null && king.isOnline()) {
                        if (com.excrele.kingdoms.util.EconomyManager.hasEnough(king, levelupCost)) {
                            com.excrele.kingdoms.util.EconomyManager.withdraw(king, levelupCost);
                            performAutoLevelUp(plugin, kingdom, king);
                        }
                    }
                } else {
                    // Free level-up, use king if online
                    Player king = plugin.getServer().getPlayer(kingdom.getKing());
                    if (king != null && king.isOnline()) {
                        performAutoLevelUp(plugin, kingdom, king);
                    }
                }
            }
        }
    }

    private void performAutoLevelUp(KingdomsPlugin plugin, Kingdom kingdom, Player king) {
        int currentLevel = kingdom.getLevel();
        int requiredXp = currentLevel * currentLevel * 1000;
        
        kingdom.setXp(kingdom.getXp() - requiredXp);
        kingdom.setLevel(currentLevel + 1);
        int newLevel = currentLevel + 1;
        
        // Visual effects
        com.excrele.kingdoms.util.VisualEffects.playLevelUpEffects(king, newLevel);
        
        // Action bar notification
        com.excrele.kingdoms.util.ActionBarManager.sendLevelUpNotification(king, newLevel);
        
        // Title announcement
        king.sendTitle("§6§l⚡ AUTO LEVEL UP! ⚡", "§eYour kingdom reached level " + newLevel + "!", 10, 70, 20);
        
        // Message to all kingdom members
        for (String memberName : kingdom.getMembers()) {
            Player member = plugin.getServer().getPlayer(memberName);
            if (member != null && member.isOnline()) {
                member.sendMessage("§6§l[Kingdom] §eYour kingdom automatically leveled up to level " + newLevel + "!");
                com.excrele.kingdoms.util.ActionBarManager.sendLevelUpNotification(member, newLevel);
            }
        }
        
        king.sendMessage("§6Your kingdom automatically leveled up to level " + newLevel + "!");
        plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
    }

    private void checkAutoKickInactive(KingdomsPlugin plugin) {
        // Use ActivityManager to check and kick inactive members
        plugin.getActivityManager().checkInactiveMembers();
    }
}

