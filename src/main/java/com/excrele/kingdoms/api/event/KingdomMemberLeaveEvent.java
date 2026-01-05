package com.excrele.kingdoms.api.event;

import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;

/**
 * Called when a player leaves a kingdom.
 * 
 * @since 1.6
 */
public class KingdomMemberLeaveEvent extends KingdomEvent {
    private final Player player;
    private final LeaveReason reason;
    
    public KingdomMemberLeaveEvent(Kingdom kingdom, Player player, LeaveReason reason) {
        super(kingdom);
        this.player = player;
        this.reason = reason;
    }
    
    public KingdomMemberLeaveEvent(Kingdom kingdom, Player player, LeaveReason reason, boolean async) {
        super(kingdom, async);
        this.player = player;
        this.reason = reason;
    }
    
    /**
     * Get the player leaving the kingdom.
     * 
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Get the reason for leaving.
     * 
     * @return The reason
     */
    public LeaveReason getReason() {
        return reason;
    }
    
    /**
     * Reasons for leaving a kingdom.
     */
    public enum LeaveReason {
        VOLUNTARY,
        KICKED,
        KINGDOM_DISBANDED,
        OTHER
    }
}

