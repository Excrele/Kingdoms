package com.excrele.kingdoms.task;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

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
        int inactiveDays = plugin.getConfig().getInt("economy.auto_features.auto_kick_inactive_days", 0);
        if (inactiveDays <= 0) {
            return;
        }

        // Note: Full implementation would require tracking last seen times
        // This is a placeholder for future enhancement

        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            List<String> toKick = new java.util.ArrayList<>();
            
            for (String memberName : kingdom.getMembers()) {
                Player member = plugin.getServer().getPlayer(memberName);
                if (member == null || !member.isOnline()) {
                    // Check last seen time (would need to track this in Kingdom model)
                    // For now, we'll skip this as it requires additional data tracking
                    // This is a placeholder for future implementation
                }
            }
            
            // Kick inactive members
            for (String memberName : toKick) {
                kingdom.getMembers().remove(memberName);
                kingdom.getMemberRoles().remove(memberName);
                kingdom.getMemberContributions().remove(memberName);
                plugin.getKingdomManager().removePlayerKingdom(memberName);
                
                Player member = plugin.getServer().getPlayer(memberName);
                if (member != null && member.isOnline()) {
                    member.sendMessage("§cYou were removed from " + kingdom.getName() + " for inactivity (" + inactiveDays + " days).");
                }
            }
            
            if (!toKick.isEmpty()) {
                // Notify kingdom
                Player king = plugin.getServer().getPlayer(kingdom.getKing());
                if (king != null && king.isOnline()) {
                    king.sendMessage("§7" + toKick.size() + " inactive member(s) were automatically removed from your kingdom.");
                }
                plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
            }
        }
    }
}

