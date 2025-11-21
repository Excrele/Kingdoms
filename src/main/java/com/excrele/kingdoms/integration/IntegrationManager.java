package com.excrele.kingdoms.integration;

import com.excrele.kingdoms.KingdomsPlugin;

/**
 * Base class for managing plugin integrations
 */
public abstract class IntegrationManager {
    protected final KingdomsPlugin plugin;
    protected boolean enabled;
    
    public IntegrationManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }
    
    public abstract boolean isAvailable();
    public abstract void enable();
    public abstract void disable();
    
    public boolean isEnabled() {
        return enabled && isAvailable();
    }
}

