package com.excrele.kingdoms.api.event;

import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;

/**
 * Called when a player joins a kingdom.
 * 
 * @since 1.6
 */
public class KingdomMemberJoinEvent extends KingdomEvent {
    private final Player player;
    private boolean cancelled = false;
    
    public KingdomMemberJoinEvent(Kingdom kingdom, Player player) {
        super(kingdom);
        this.player = player;
    }
    
    public KingdomMemberJoinEvent(Kingdom kingdom, Player player, boolean async) {
        super(kingdom, async);
        this.player = player;
    }
    
    /**
     * Get the player joining the kingdom.
     * 
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Check if the event is cancelled.
     * 
     * @return True if cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Set the cancellation state.
     * 
     * @param cancelled True to cancel the event
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

