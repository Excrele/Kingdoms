package com.excrele.kingdoms.model;

import org.bukkit.Chunk;

/**
 * Represents a claim that is for sale
 */
public class ClaimSale {
    private String id;
    private String sellerKingdom;
    private Chunk chunk;
    private double price;
    private long createdAt;
    private boolean active;

    public ClaimSale(String id, String sellerKingdom, Chunk chunk, double price) {
        this.id = id;
        this.sellerKingdom = sellerKingdom;
        this.chunk = chunk;
        this.price = price;
        this.createdAt = System.currentTimeMillis();
        this.active = true;
    }

    public String getId() { return id; }
    public String getSellerKingdom() { return sellerKingdom; }
    public Chunk getChunk() { return chunk; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getChunkKey() {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
}

