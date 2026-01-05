package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Notification;
import com.excrele.kingdoms.model.NotificationPreference;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages advanced notification system with preferences, channels, filtering, and history
 */
public class NotificationManager {
    private final KingdomsPlugin plugin;
    private final Map<String, NotificationPreference> preferences; // player -> preferences
    private final Map<String, List<Notification>> notificationHistory; // player -> notifications
    private final Map<String, List<Notification>> unreadNotifications; // player -> unread notifications
    
    public NotificationManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.preferences = new ConcurrentHashMap<>();
        this.notificationHistory = new ConcurrentHashMap<>();
        this.unreadNotifications = new ConcurrentHashMap<>();
    }
    
    /**
     * Get or create notification preferences for a player
     */
    public NotificationPreference getPreferences(String player) {
        return preferences.computeIfAbsent(player, NotificationPreference::new);
    }
    
    /**
     * Send a notification to a player
     */
    public void sendNotification(String player, String type, String message) {
        sendNotification(player, type, message, null, null, null);
    }
    
    /**
     * Send a notification with full options
     */
    public void sendNotification(String player, String type, String message, 
                               String title, String subtitle, Sound sound) {
        NotificationPreference prefs = getPreferences(player);
        
        // Check if this type is filtered
        if (prefs.isTypeFiltered(type)) {
            return; // Filtered out
        }
        
        String notificationId = UUID.randomUUID().toString().substring(0, 8);
        Notification notification = new Notification(notificationId, player, type, message, 
                                                     title, subtitle, sound);
        
        // Add to history
        notificationHistory.computeIfAbsent(player, k -> new ArrayList<>()).add(notification);
        unreadNotifications.computeIfAbsent(player, k -> new ArrayList<>()).add(notification);
        
        // Keep only last 1000 notifications per player
        List<Notification> history = notificationHistory.get(player);
        if (history.size() > 1000) {
            history.remove(0);
        }
        
        // Send to player if online
        Player p = plugin.getServer().getPlayer(player);
        if (p != null && p.isOnline()) {
            sendToPlayer(p, notification, prefs);
        }
        
        // Send Discord/webhook if enabled
        if (prefs.isChannelEnabled(NotificationPreference.NotificationChannel.DISCORD) && 
            plugin.getDiscordSRVIntegration() != null && 
            plugin.getDiscordSRVIntegration().isAvailable()) {
            // Discord integration would be handled by DiscordSRVIntegration separately
            // This is a placeholder for future webhook support
        }
    }
    
    /**
     * Send notification to player based on preferences
     */
    private void sendToPlayer(Player player, Notification notification, NotificationPreference prefs) {
        // Chat notification
        if (prefs.isChatEnabled() && prefs.isChannelEnabled(NotificationPreference.NotificationChannel.CHAT)) {
            player.sendMessage("§7[Notification] §f" + notification.getMessage());
        }
        
        // Action bar notification
        if (prefs.isActionBarEnabled() && 
            prefs.isChannelEnabled(NotificationPreference.NotificationChannel.ACTION_BAR)) {
            com.excrele.kingdoms.util.ActionBarManager.sendNotification(player, notification.getMessage());
        }
        
        // Title notification
        if (prefs.isTitleEnabled() && prefs.isChannelEnabled(NotificationPreference.NotificationChannel.TITLE)) {
            String title = notification.getTitle() != null ? notification.getTitle() : "";
            String subtitle = notification.getSubtitle() != null ? notification.getSubtitle() : 
                             notification.getMessage();
            player.sendTitle(title, subtitle, 10, 70, 20);
        }
        
        // Sound notification
        if (prefs.isSoundEnabled() && prefs.isChannelEnabled(NotificationPreference.NotificationChannel.SOUND) &&
            notification.getSound() != null) {
            player.playSound(player.getLocation(), notification.getSound(), 1.0f, 1.0f);
        }
    }
    
    /**
     * Get notification history for a player
     */
    public List<Notification> getNotificationHistory(String player) {
        return notificationHistory.getOrDefault(player, new ArrayList<>());
    }
    
    /**
     * Get unread notifications for a player
     */
    public List<Notification> getUnreadNotifications(String player) {
        return unreadNotifications.getOrDefault(player, new ArrayList<>());
    }
    
    /**
     * Mark notification as read
     */
    public void markAsRead(String player, String notificationId) {
        List<Notification> unread = unreadNotifications.get(player);
        if (unread != null) {
            unread.removeIf(n -> n.getNotificationId().equals(notificationId));
        }
        
        // Also mark in history
        List<Notification> history = notificationHistory.get(player);
        if (history != null) {
            for (Notification n : history) {
                if (n.getNotificationId().equals(notificationId)) {
                    n.setRead(true);
                    break;
                }
            }
        }
    }
    
    /**
     * Mark all notifications as read for a player
     */
    public void markAllAsRead(String player) {
        List<Notification> unread = unreadNotifications.get(player);
        if (unread != null) {
            for (Notification n : unread) {
                n.setRead(true);
            }
            unread.clear();
        }
    }
    
    /**
     * Clear notification history for a player
     */
    public void clearHistory(String player) {
        notificationHistory.remove(player);
        unreadNotifications.remove(player);
    }
}

