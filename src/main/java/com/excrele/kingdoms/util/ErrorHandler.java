package com.excrele.kingdoms.util;

import com.excrele.kingdoms.KingdomsPlugin;
import org.bukkit.Bukkit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized error handling and logging
 */
public class ErrorHandler {
    private final Logger logger;
    private final KingdomsPlugin plugin;
    
    public ErrorHandler(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Log an error with full stack trace
     */
    public void logError(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
    
    /**
     * Log a warning
     */
    public void logWarning(String message) {
        logger.warning(message);
    }
    
    /**
     * Log an info message
     */
    public void logInfo(String message) {
        logger.info(message);
    }
    
    /**
     * Handle a save error with recovery attempt
     */
    public void handleSaveError(String operation, Throwable throwable, Runnable retryOperation) {
        logError("Failed to " + operation + ": " + throwable.getMessage(), throwable);
        
        // Attempt retry once
        if (retryOperation != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    retryOperation.run();
                    logInfo("Successfully retried " + operation);
                } catch (Exception e) {
                    logError("Retry failed for " + operation, e);
                }
            }, 20L); // Retry after 1 second
        }
    }
    
    /**
     * Get stack trace as string
     */
    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Handle null pointer with graceful degradation
     */
    public <T> T handleNull(String context, T value, T defaultValue) {
        if (value == null) {
            logWarning("Null value encountered in " + context + ", using default");
            return defaultValue;
        }
        return value;
    }
}

