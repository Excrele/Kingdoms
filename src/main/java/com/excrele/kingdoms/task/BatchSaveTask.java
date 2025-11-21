package com.excrele.kingdoms.task;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.util.SaveQueue;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Periodic task to process batched save operations
 */
public class BatchSaveTask extends BukkitRunnable {
    private final KingdomsPlugin plugin;
    private final SaveQueue saveQueue;
    private final long saveInterval; // in ticks
    
    public BatchSaveTask(KingdomsPlugin plugin, SaveQueue saveQueue, long saveInterval) {
        this.plugin = plugin;
        this.saveQueue = saveQueue;
        this.saveInterval = saveInterval;
    }
    
    @Override
    public void run() {
        if (saveQueue.size() > 0) {
            plugin.getLogger().info("Processing " + saveQueue.size() + " queued save operations...");
            saveQueue.processQueue();
        }
    }
    
    /**
     * Start the batch save task
     */
    public void start() {
        this.runTaskTimer(plugin, saveInterval, saveInterval);
    }
    
    /**
     * Force immediate save
     */
    public void saveNow() {
        saveQueue.processQueue();
    }
}

