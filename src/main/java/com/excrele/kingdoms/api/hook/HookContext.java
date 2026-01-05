package com.excrele.kingdoms.api.hook;

import java.util.HashMap;
import java.util.Map;

/**
 * Context object passed to hooks.
 * 
 * @since 1.6
 */
public class HookContext {
    private final Map<String, Object> data;
    
    public HookContext() {
        this.data = new HashMap<>();
    }
    
    /**
     * Set a value in the context.
     * 
     * @param key The key
     * @param value The value
     */
    public void set(String key, Object value) {
        data.put(key, value);
    }
    
    /**
     * Get a value from the context.
     * 
     * @param key The key
     * @return The value, or null if not found
     */
    public Object get(String key) {
        return data.get(key);
    }
    
    /**
     * Get a value from the context with a default.
     * 
     * @param key The key
     * @param defaultValue The default value
     * @return The value, or defaultValue if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        Object value = data.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
    
    /**
     * Check if a key exists in the context.
     * 
     * @param key The key
     * @return True if the key exists
     */
    public boolean has(String key) {
        return data.containsKey(key);
    }
    
    /**
     * Get all data in the context.
     * 
     * @return A copy of the data map
     */
    public Map<String, Object> getAll() {
        return new HashMap<>(data);
    }
}

