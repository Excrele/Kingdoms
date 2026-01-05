package com.excrele.kingdoms.api.event;

import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;

/**
 * Called when a kingdom is disbanded.
 * 
 * @since 1.6
 */
public class KingdomDisbandEvent extends KingdomEvent {
    private final Player disbander;
    private final DisbandReason reason;
    
    public KingdomDisbandEvent(Kingdom kingdom, Player disbander, DisbandReason reason) {
        super(kingdom);
        this.disbander = disbander;
        this.reason = reason;
    }
    
    public KingdomDisbandEvent(Kingdom kingdom, Player disbander, DisbandReason reason, boolean async) {
        super(kingdom, async);
        this.disbander = disbander;
        this.reason = reason;
    }
    
    /**
     * Get the player who disbanded the kingdom.
     * 
     * @return The disbander, or null if disbanded by system/admin
     */
    public Player getDisbander() {
        return disbander;
    }
    
    /**
     * Get the reason for disbanding.
     * 
     * @return The reason
     */
    public DisbandReason getReason() {
        return reason;
    }
    
    /**
     * Reasons for disbanding a kingdom.
     */
    public enum DisbandReason {
        ADMIN,
        KING_LEFT,
        INACTIVITY,
        PLUGIN,
        OTHER
    }
}

