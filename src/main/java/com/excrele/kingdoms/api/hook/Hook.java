package com.excrele.kingdoms.api.hook;

import org.bukkit.plugin.Plugin;

/**
 * Interface for hooks that can extend plugin functionality.
 * 
 * @since 1.6
 */
public interface Hook {
    /**
     * Get the plugin that registered this hook.
     * 
     * @return The plugin
     */
    Plugin getPlugin();
    
    /**
     * Called when the hook is triggered.
     * 
     * @param context The hook context
     * @return True to allow the action, false to deny it
     */
    boolean onHook(HookContext context);
}

