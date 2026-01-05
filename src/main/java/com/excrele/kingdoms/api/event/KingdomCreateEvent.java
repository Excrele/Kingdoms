package com.excrele.kingdoms.api.event;

import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;

/**
 * Called when a kingdom is created.
 * 
 * @since 1.6
 */
public class KingdomCreateEvent extends KingdomEvent {
    private final Player creator;
    private boolean cancelled = false;
    
    public KingdomCreateEvent(Kingdom kingdom, Player creator) {
        super(kingdom);
        this.creator = creator;
    }
    
    public KingdomCreateEvent(Kingdom kingdom, Player creator, boolean async) {
        super(kingdom, async);
        this.creator = creator;
    }
    
    /**
     * Get the player who created the kingdom.
     * 
     * @return The creator
     */
    public Player getCreator() {
        return creator;
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

