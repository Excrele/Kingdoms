package com.excrele.kingdoms.util;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Queue for batched save operations
 */
public class SaveQueue {
    private final ConcurrentLinkedQueue<Runnable> saveQueue;
    private volatile boolean processing;
    
    public SaveQueue() {
        this.saveQueue = new ConcurrentLinkedQueue<>();
        this.processing = false;
    }
    
    /**
     * Add a save operation to the queue
     */
    public void enqueue(Runnable saveOperation) {
        saveQueue.offer(saveOperation);
    }
    
    /**
     * Process all queued save operations
     */
    public void processQueue() {
        if (processing) return; // Prevent concurrent processing
        processing = true;
        
        try {
            Runnable operation;
            int processed = 0;
            while ((operation = saveQueue.poll()) != null) {
                try {
                    operation.run();
                    processed++;
                } catch (Exception e) {
                    // Log error but continue processing
                    System.err.println("Error processing save queue operation: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            if (processed > 0) {
                System.out.println("Processed " + processed + " queued save operations");
            }
        } finally {
            processing = false;
        }
    }
    
    /**
     * Get the current queue size
     */
    public int size() {
        return saveQueue.size();
    }
    
    /**
     * Clear the queue
     */
    public void clear() {
        saveQueue.clear();
    }
}

