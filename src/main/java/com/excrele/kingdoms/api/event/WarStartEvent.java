package com.excrele.kingdoms.api.event;

import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.War;
import org.bukkit.entity.Player;

/**
 * Called when a war starts between two kingdoms.
 * 
 * @since 1.6
 */
public class WarStartEvent extends KingdomEvent {
    private final Kingdom enemyKingdom;
    private final War war;
    private final Player declarer;
    private boolean cancelled = false;
    
    public WarStartEvent(Kingdom kingdom, Kingdom enemyKingdom, War war, Player declarer) {
        super(kingdom);
        this.enemyKingdom = enemyKingdom;
        this.war = war;
        this.declarer = declarer;
    }
    
    public WarStartEvent(Kingdom kingdom, Kingdom enemyKingdom, War war, Player declarer, boolean async) {
        super(kingdom, async);
        this.enemyKingdom = enemyKingdom;
        this.war = war;
        this.declarer = declarer;
    }
    
    /**
     * Get the enemy kingdom.
     * 
     * @return The enemy kingdom
     */
    public Kingdom getEnemyKingdom() {
        return enemyKingdom;
    }
    
    /**
     * Get the war object.
     * 
     * @return The war
     */
    public War getWar() {
        return war;
    }
    
    /**
     * Get the player who declared the war.
     * 
     * @return The declarer
     */
    public Player getDeclarer() {
        return declarer;
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

