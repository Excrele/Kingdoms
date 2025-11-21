package com.excrele.kingdoms.model;

import org.bukkit.Chunk;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a claim that is being auctioned
 */
public class ClaimAuction {
    private String id;
    private String sellerKingdom;
    private Chunk chunk;
    private double startingBid;
    private double currentBid;
    private String currentBidder;
    private long endTime;
    private long createdAt;
    private Map<String, Double> bids; // bidder -> bid amount
    private boolean active;

    public ClaimAuction(String id, String sellerKingdom, Chunk chunk, double startingBid, long durationSeconds) {
        this.id = id;
        this.sellerKingdom = sellerKingdom;
        this.chunk = chunk;
        this.startingBid = startingBid;
        this.currentBid = startingBid;
        this.currentBidder = null;
        this.endTime = System.currentTimeMillis() + (durationSeconds * 1000);
        this.createdAt = System.currentTimeMillis();
        this.bids = new HashMap<>();
        this.active = true;
    }

    public String getId() { return id; }
    public String getSellerKingdom() { return sellerKingdom; }
    public Chunk getChunk() { return chunk; }
    public double getStartingBid() { return startingBid; }
    public double getCurrentBid() { return currentBid; }
    public String getCurrentBidder() { return currentBidder; }
    public long getEndTime() { return endTime; }
    public long getCreatedAt() { return createdAt; }
    public Map<String, Double> getBids() { return bids; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public boolean placeBid(String bidder, double amount) {
        if (!active) return false;
        if (System.currentTimeMillis() >= endTime) {
            active = false;
            return false;
        }
        if (amount <= currentBid) return false;
        
        currentBid = amount;
        currentBidder = bidder;
        bids.put(bidder, amount);
        return true;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() >= endTime;
    }
    
    public String getChunkKey() {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
}

