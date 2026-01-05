package com.excrele.kingdoms.model;

import org.bukkit.Sound;

/**
 * Represents a notification sent to a player
 */
public class Notification {
    private final String notificationId;
    private final String player;
    private final String type;
    private final String message;
    private final String title;
    private final String subtitle;
    private final Sound sound;
    private final long timestamp;
    private boolean read;
    
    public Notification(String notificationId, String player, String type, String message) {
        this(notificationId, player, type, message, null, null, null);
    }
    
    public Notification(String notificationId, String player, String type, String message, 
                       String title, String subtitle, Sound sound) {
        this.notificationId = notificationId;
        this.player = player;
        this.type = type;
        this.message = message;
        this.title = title;
        this.subtitle = subtitle;
        this.sound = sound;
        this.timestamp = System.currentTimeMillis() / 1000;
        this.read = false;
    }
    
    public String getNotificationId() {
        return notificationId;
    }
    
    public String getPlayer() {
        return player;
    }
    
    public String getType() {
        return type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public Sound getSound() {
        return sound;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
}

