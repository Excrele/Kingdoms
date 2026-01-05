package com.excrele.kingdoms.api.event;

import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.War;

/**
 * Called when a war ends between two kingdoms.
 * 
 * @since 1.6
 */
public class WarEndEvent extends KingdomEvent {
    private final Kingdom enemyKingdom;
    private final War war;
    private final WarEndReason reason;
    private final Kingdom winner; // null if no winner
    
    public WarEndEvent(Kingdom kingdom, Kingdom enemyKingdom, War war, WarEndReason reason, Kingdom winner) {
        super(kingdom);
        this.enemyKingdom = enemyKingdom;
        this.war = war;
        this.reason = reason;
        this.winner = winner;
    }
    
    public WarEndEvent(Kingdom kingdom, Kingdom enemyKingdom, War war, WarEndReason reason, Kingdom winner, boolean async) {
        super(kingdom, async);
        this.enemyKingdom = enemyKingdom;
        this.war = war;
        this.reason = reason;
        this.winner = winner;
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
     * Get the reason the war ended.
     * 
     * @return The reason
     */
    public WarEndReason getReason() {
        return reason;
    }
    
    /**
     * Get the winning kingdom.
     * 
     * @return The winner, or null if no winner
     */
    public Kingdom getWinner() {
        return winner;
    }
    
    /**
     * Reasons for a war ending.
     */
    public enum WarEndReason {
        VICTORY,
        DEFEAT,
        PEACE,
        TIMEOUT,
        ADMIN,
        OTHER
    }
}

