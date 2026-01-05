package com.excrele.kingdoms.listener;

import com.excrele.kingdoms.KingdomsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens for player join/quit events to track activity
 */
public class ActivityListener implements Listener {
    private final KingdomsPlugin plugin;
    
    public ActivityListener(KingdomsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getActivityManager().recordLogin(event.getPlayer());
        
        // Deliver pending mail
        if (plugin.getMailManager() != null) {
            plugin.getMailManager().deliverPendingMail(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getActivityManager().recordLogout(event.getPlayer());
        
        // Cleanup chunk optimizer data
        if (plugin.getChunkOptimizer() != null) {
            plugin.getChunkOptimizer().cleanupPlayer(event.getPlayer().getName());
        }
    }
}

