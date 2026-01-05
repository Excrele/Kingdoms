package com.excrele.kingdoms.api.hook;

import com.excrele.kingdoms.KingdomsPlugin;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages hooks for extensibility.
 * 
 * <p>Hooks allow other plugins to extend the functionality of the Kingdoms plugin
 * without modifying its core code.</p>
 * 
 * @since 1.6
 */
public class HookManager {
    private final KingdomsPlugin plugin;
    private final Map<String, List<Hook>> hooks;
    
    public HookManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.hooks = new HashMap<>();
    }
    
    /**
     * Register a hook.
     * 
     * @param hookName The name of the hook
     * @param hook The hook implementation
     * @param plugin The plugin registering the hook
     */
    public void registerHook(String hookName, Hook hook, Plugin plugin) {
        hooks.computeIfAbsent(hookName, k -> new ArrayList<>()).add(hook);
        this.plugin.getLogger().info("Hook '" + hookName + "' registered by " + plugin.getName());
    }
    
    /**
     * Unregister all hooks for a plugin.
     * 
     * @param plugin The plugin
     */
    public void unregisterHooks(Plugin plugin) {
        hooks.values().forEach(hookList -> hookList.removeIf(hook -> hook.getPlugin().equals(plugin)));
    }
    
    /**
     * Call all hooks for a given hook name.
     * 
     * @param hookName The name of the hook
     * @param context The context object passed to hooks
     * @return True if all hooks returned true (or no hooks), false otherwise
     */
    public boolean callHooks(String hookName, HookContext context) {
        List<Hook> hookList = hooks.get(hookName);
        if (hookList == null || hookList.isEmpty()) {
            return true;
        }
        
        boolean result = true;
        for (Hook hook : hookList) {
            try {
                if (!hook.onHook(context)) {
                    result = false;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error calling hook '" + hookName + "' from " + 
                    hook.getPlugin().getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }
    
    /**
     * Get all hooks for a given hook name.
     * 
     * @param hookName The name of the hook
     * @return List of hooks
     */
    public List<Hook> getHooks(String hookName) {
        return hooks.getOrDefault(hookName, new ArrayList<>());
    }
}

