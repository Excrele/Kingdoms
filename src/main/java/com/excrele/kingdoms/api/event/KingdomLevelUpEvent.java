package com.excrele.kingdoms.api.event;

import com.excrele.kingdoms.model.Kingdom;

/**
 * Called when a kingdom levels up.
 * 
 * @since 1.6
 */
public class KingdomLevelUpEvent extends KingdomEvent {
    private final int oldLevel;
    private final int newLevel;
    
    public KingdomLevelUpEvent(Kingdom kingdom, int oldLevel, int newLevel) {
        super(kingdom);
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }
    
    public KingdomLevelUpEvent(Kingdom kingdom, int oldLevel, int newLevel, boolean async) {
        super(kingdom, async);
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }
    
    /**
     * Get the old level.
     * 
     * @return The old level
     */
    public int getOldLevel() {
        return oldLevel;
    }
    
    /**
     * Get the new level.
     * 
     * @return The new level
     */
    public int getNewLevel() {
        return newLevel;
    }
}

