package com.excrele.kingdoms.util;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ActionBarManager {
    
    /**
     * Send XP progress to action bar
     */
    public static void sendXPProgress(Player player) {
        String kingdomName = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null) return;
        
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        
        int currentLevel = kingdom.getLevel();
        int required = currentLevel * currentLevel * 1000;
        int current = kingdom.getXp();
        int progress = required > 0 ? Math.min(100, (current * 100) / required) : 100;
        
        String progressBar = generateProgressBar(progress, 20);
        String message = String.format("§6Kingdom Level §e%d §7| §6XP: §e%s§7/§e%s §7(%d%%) %s",
            kingdom.getLevel(),
            formatNumber(current),
            formatNumber(required),
            progress,
            progressBar
        );
        
        // Use Spigot API for action bar (available in 1.11+)
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } catch (Exception e) {
            // Fallback for older versions or if method doesn't exist
            player.sendMessage(message);
        }
    }
    
    /**
     * Send challenge progress to action bar
     */
    public static void sendChallengeProgress(Player player, String challengeName, int progress, int required) {
        int percentage = required > 0 ? (progress * 100) / required : 100;
        String progressBar = generateProgressBar(percentage, 15);
        String message = String.format("§6Challenge: §e%s §7| §e%d§7/§e%d §7(%d%%) %s",
            challengeName,
            progress,
            required,
            percentage,
            progressBar
        );
        
        // Use Spigot API for action bar (available in 1.11+)
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } catch (Exception e) {
            // Fallback for older versions or if method doesn't exist
            player.sendMessage(message);
        }
    }
    
    /**
     * Send claim notification to action bar
     */
    public static void sendClaimNotification(Player player, String message) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§a" + message));
        } catch (Exception e) {
            player.sendMessage("§a" + message);
        }
    }
    
    /**
     * Send level-up notification to action bar
     */
    public static void sendLevelUpNotification(Player player, int newLevel) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6§l⚡ LEVEL UP! §e§lLevel " + newLevel + " §6§l⚡"));
        } catch (Exception e) {
            player.sendMessage("§6§l⚡ LEVEL UP! §e§lLevel " + newLevel + " §6§l⚡");
        }
    }
    
    /**
     * Send challenge completion notification
     */
    public static void sendChallengeComplete(Player player, String challengeName, int xpReward) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§a§l✓ Challenge Complete! §e" + challengeName + " §7(+§a" + xpReward + " XP§7)"));
        } catch (Exception e) {
            player.sendMessage("§a§l✓ Challenge Complete! §e" + challengeName + " §7(+§a" + xpReward + " XP§7)");
        }
    }
    
    /**
     * Send general kingdom notification
     */
    public static void sendNotification(Player player, String message) {
        // Use Spigot API for action bar (available in 1.11+)
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } catch (Exception e) {
            // Fallback for older versions or if method doesn't exist
            player.sendMessage(message);
        }
    }
    
    public static String generateProgressBar(int progress, int length) {
        int filled = (progress * length) / 100;
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        bar.append("§7");
        for (int i = filled; i < length; i++) {
            bar.append("█");
        }
        return bar.toString();
    }
    
    private static String formatNumber(int number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }
}

