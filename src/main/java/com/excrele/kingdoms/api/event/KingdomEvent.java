package com.excrele.kingdoms.api.event;

import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base class for all kingdom-related events.
 * 
 * @since 1.6
 */
public abstract class KingdomEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Kingdom kingdom;
    
    public KingdomEvent(Kingdom kingdom) {
        this.kingdom = kingdom;
    }
    
    public KingdomEvent(Kingdom kingdom, boolean async) {
        super(async);
        this.kingdom = kingdom;
    }
    
    /**
     * Get the kingdom involved in this event.
     * 
     * @return The kingdom
     */
    public Kingdom getKingdom() {
        return kingdom;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

