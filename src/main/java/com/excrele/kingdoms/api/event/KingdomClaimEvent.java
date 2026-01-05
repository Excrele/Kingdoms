package com.excrele.kingdoms.api.event;

import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 * Called when a kingdom claims a chunk.
 * 
 * @since 1.6
 */
public class KingdomClaimEvent extends KingdomEvent {
    private final Chunk chunk;
    private final Player claimer;
    private boolean cancelled = false;
    
    public KingdomClaimEvent(Kingdom kingdom, Chunk chunk, Player claimer) {
        super(kingdom);
        this.chunk = chunk;
        this.claimer = claimer;
    }
    
    public KingdomClaimEvent(Kingdom kingdom, Chunk chunk, Player claimer, boolean async) {
        super(kingdom, async);
        this.chunk = chunk;
        this.claimer = claimer;
    }
    
    /**
     * Get the chunk being claimed.
     * 
     * @return The chunk
     */
    public Chunk getChunk() {
        return chunk;
    }
    
    /**
     * Get the player who claimed the chunk.
     * 
     * @return The claimer
     */
    public Player getClaimer() {
        return claimer;
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

