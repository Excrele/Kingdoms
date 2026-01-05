package com.excrele.kingdoms.api.event;

import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 * Called when a kingdom unclaims a chunk.
 * 
 * @since 1.6
 */
public class KingdomUnclaimEvent extends KingdomEvent {
    private final Chunk chunk;
    private final Player unclaimer;
    
    public KingdomUnclaimEvent(Kingdom kingdom, Chunk chunk, Player unclaimer) {
        super(kingdom);
        this.chunk = chunk;
        this.unclaimer = unclaimer;
    }
    
    public KingdomUnclaimEvent(Kingdom kingdom, Chunk chunk, Player unclaimer, boolean async) {
        super(kingdom, async);
        this.chunk = chunk;
        this.unclaimer = unclaimer;
    }
    
    /**
     * Get the chunk being unclaimed.
     * 
     * @return The chunk
     */
    public Chunk getChunk() {
        return chunk;
    }
    
    /**
     * Get the player who unclaimed the chunk.
     * 
     * @return The unclaimer
     */
    public Player getUnclaimer() {
        return unclaimer;
    }
}

