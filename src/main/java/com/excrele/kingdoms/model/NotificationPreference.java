package com.excrele.kingdoms.model;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents notification preferences for a player
 */
public class NotificationPreference {
    private final String player;
    private final Set<NotificationChannel> enabledChannels;
    private final Set<String> filteredTypes; // Notification types to filter out
    private boolean soundEnabled;
    private boolean actionBarEnabled;
    private boolean titleEnabled;
    private boolean chatEnabled;
    
    public NotificationPreference(String player) {
        this.player = player;
        this.enabledChannels = EnumSet.allOf(NotificationChannel.class);
        this.filteredTypes = new HashSet<>();
        this.soundEnabled = true;
        this.actionBarEnabled = true;
        this.titleEnabled = true;
        this.chatEnabled = true;
    }
    
    public String getPlayer() {
        return player;
    }
    
    public Set<NotificationChannel> getEnabledChannels() {
        return enabledChannels;
    }
    
    public void enableChannel(NotificationChannel channel) {
        enabledChannels.add(channel);
    }
    
    public void disableChannel(NotificationChannel channel) {
        enabledChannels.remove(channel);
    }
    
    public boolean isChannelEnabled(NotificationChannel channel) {
        return enabledChannels.contains(channel);
    }
    
    public Set<String> getFilteredTypes() {
        return filteredTypes;
    }
    
    public void addFilteredType(String type) {
        filteredTypes.add(type.toLowerCase());
    }
    
    public void removeFilteredType(String type) {
        filteredTypes.remove(type.toLowerCase());
    }
    
    public boolean isTypeFiltered(String type) {
        return filteredTypes.contains(type.toLowerCase());
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }
    
    public boolean isActionBarEnabled() {
        return actionBarEnabled;
    }
    
    public void setActionBarEnabled(boolean actionBarEnabled) {
        this.actionBarEnabled = actionBarEnabled;
    }
    
    public boolean isTitleEnabled() {
        return titleEnabled;
    }
    
    public void setTitleEnabled(boolean titleEnabled) {
        this.titleEnabled = titleEnabled;
    }
    
    public boolean isChatEnabled() {
        return chatEnabled;
    }
    
    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }
    
    public enum NotificationChannel {
        CHAT,
        ACTION_BAR,
        TITLE,
        SOUND,
        DISCORD,
        WEBHOOK
    }
}

