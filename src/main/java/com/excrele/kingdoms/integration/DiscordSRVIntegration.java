package com.excrele.kingdoms.integration;

import com.excrele.kingdoms.KingdomsPlugin;

import java.lang.reflect.Method;

/**
 * Integration with DiscordSRV for kingdom chat, announcements, and events
 */
public class DiscordSRVIntegration extends IntegrationManager {
    private Object discordSRV;
    private Object kingdomChannel;
    
    public DiscordSRVIntegration(KingdomsPlugin plugin) {
        super(plugin);
    }
    
    @Override
    public boolean isAvailable() {
        return plugin.getServer().getPluginManager().getPlugin("DiscordSRV") != null;
    }
    
    @Override
    public void enable() {
        if (!isAvailable()) return;
        
        try {
            // Use reflection to get DiscordSRV plugin
            org.bukkit.plugin.Plugin discordPlugin = plugin.getServer().getPluginManager().getPlugin("DiscordSRV");
            if (discordPlugin == null) return;
            
            // Get DiscordSRV class and getPlugin method
            Class<?> discordSRVClass = Class.forName("github.scarsz.discordsrv.DiscordSRV");
            Method getPluginMethod = discordSRVClass.getMethod("getPlugin");
            discordSRV = getPluginMethod.invoke(null);
            
            // Get or create kingdom channel
            String channelId = plugin.getConfig().getString("discord.kingdom_channel_id");
            if (channelId != null && discordSRV != null) {
                Method getJdaMethod = discordSRVClass.getMethod("getJda");
                Object jda = getJdaMethod.invoke(discordSRV);
                if (jda != null) {
                    Method getTextChannelMethod = jda.getClass().getMethod("getTextChannelById", String.class);
                    kingdomChannel = getTextChannelMethod.invoke(jda, channelId);
                }
            }
            enabled = true;
            plugin.getLogger().info("DiscordSRV integration enabled!");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to enable DiscordSRV integration: " + e.getMessage());
            enabled = false;
        }
    }
    
    @Override
    public void disable() {
        enabled = false;
        discordSRV = null;
        kingdomChannel = null;
    }
    
    /**
     * Send kingdom chat message to Discord
     */
    public void sendKingdomChat(String kingdomName, String playerName, String message) {
        if (!isEnabled() || kingdomChannel == null) return;
        try {
            Method sendMessageMethod = kingdomChannel.getClass().getMethod("sendMessage", String.class);
            Object messageAction = sendMessageMethod.invoke(kingdomChannel, "**[" + kingdomName + "]** " + playerName + ": " + message);
            if (messageAction != null) {
                Method queueMethod = messageAction.getClass().getMethod("queue");
                queueMethod.invoke(messageAction);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send Discord message: " + e.getMessage());
        }
    }
    
    /**
     * Send kingdom announcement to Discord
     */
    public void sendAnnouncement(String kingdomName, String announcement) {
        if (!isEnabled() || kingdomChannel == null) return;
        try {
            Method sendMessageMethod = kingdomChannel.getClass().getMethod("sendMessage", String.class);
            Object messageAction = sendMessageMethod.invoke(kingdomChannel, "📢 **" + kingdomName + " Announcement:** " + announcement);
            if (messageAction != null) {
                Method queueMethod = messageAction.getClass().getMethod("queue");
                queueMethod.invoke(messageAction);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send Discord announcement: " + e.getMessage());
        }
    }
    
    /**
     * Send kingdom event notification to Discord
     */
    public void sendEventNotification(String kingdomName, String eventName, String description) {
        if (!isEnabled() || kingdomChannel == null) return;
        try {
            Method sendMessageMethod = kingdomChannel.getClass().getMethod("sendMessage", String.class);
            Object messageAction = sendMessageMethod.invoke(kingdomChannel, "📅 **" + kingdomName + " Event:** " + eventName + " - " + description);
            if (messageAction != null) {
                Method queueMethod = messageAction.getClass().getMethod("queue");
                queueMethod.invoke(messageAction);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send Discord event notification: " + e.getMessage());
        }
    }
}
