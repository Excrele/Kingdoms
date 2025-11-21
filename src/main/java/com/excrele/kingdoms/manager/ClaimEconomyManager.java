package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.ClaimAuction;
import com.excrele.kingdoms.model.ClaimRent;
import com.excrele.kingdoms.model.ClaimSale;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.block.Biome;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the claim economy system (selling, buying, auctions, rent)
 */
public class ClaimEconomyManager {
    private final KingdomManager kingdomManager;
    private final ClaimManager claimManager;
    private final BankManager bankManager;
    
    // Storage for active sales, auctions, and rents
    private final Map<String, ClaimSale> activeSales; // chunkKey -> sale
    private final Map<String, ClaimAuction> activeAuctions; // chunkKey -> auction
    private final Map<String, ClaimRent> activeRents; // chunkKey -> rent
    private final Map<String, List<ClaimSale>> marketSales; // kingdom -> list of sales
    
    public ClaimEconomyManager(KingdomsPlugin plugin, KingdomManager kingdomManager, ClaimManager claimManager, BankManager bankManager) {
        this.kingdomManager = kingdomManager;
        this.claimManager = claimManager;
        this.bankManager = bankManager;
        this.activeSales = new ConcurrentHashMap<>();
        this.activeAuctions = new ConcurrentHashMap<>();
        this.activeRents = new ConcurrentHashMap<>();
        this.marketSales = new ConcurrentHashMap<>();
    }
    
    /**
     * List a claim for sale
     */
    public boolean listClaimForSale(Kingdom kingdom, Chunk chunk, double price) {
        String chunkKey = getChunkKey(chunk);
        
        // Check if chunk is already listed
        if (activeSales.containsKey(chunkKey) || activeAuctions.containsKey(chunkKey)) {
            return false;
        }
        
        // Check if kingdom owns the chunk
        if (kingdomManager.getKingdomByChunk(chunk) != kingdom) {
            return false;
        }
        
        String saleId = UUID.randomUUID().toString();
        ClaimSale sale = new ClaimSale(saleId, kingdom.getName(), chunk, price);
        activeSales.put(chunkKey, sale);
        
        // Add to market
        marketSales.computeIfAbsent(kingdom.getName(), k -> new ArrayList<>()).add(sale);
        
        return true;
    }
    
    /**
     * Buy a claim that is for sale
     */
    public boolean buyClaim(Kingdom buyerKingdom, Chunk chunk) {
        String chunkKey = getChunkKey(chunk);
        ClaimSale sale = activeSales.get(chunkKey);
        
        if (sale == null || !sale.isActive()) {
            return false;
        }
        
        // Check if buyer has enough money
        if (bankManager.getBalance(buyerKingdom.getName()) < sale.getPrice()) {
            return false;
        }
        
        Kingdom sellerKingdom = kingdomManager.getKingdom(sale.getSellerKingdom());
        if (sellerKingdom == null) {
            return false;
        }
        
        // Transfer money
        if (bankManager.withdraw(buyerKingdom.getName(), sale.getPrice())) {
            bankManager.deposit(sellerKingdom.getName(), sale.getPrice());
        } else {
            return false;
        }
        
        // Transfer claim
        if (transferClaim(sellerKingdom, buyerKingdom, chunk)) {
            sale.setActive(false);
            activeSales.remove(chunkKey);
            marketSales.get(sellerKingdom.getName()).remove(sale);
            return true;
        }
        
        return false;
    }
    
    /**
     * Create an auction for a claim
     */
    public boolean createAuction(Kingdom kingdom, Chunk chunk, double startingBid, long durationSeconds) {
        String chunkKey = getChunkKey(chunk);
        
        // Check if chunk is already listed
        if (activeSales.containsKey(chunkKey) || activeAuctions.containsKey(chunkKey)) {
            return false;
        }
        
        // Check if kingdom owns the chunk
        if (kingdomManager.getKingdomByChunk(chunk) != kingdom) {
            return false;
        }
        
        String auctionId = UUID.randomUUID().toString();
        ClaimAuction auction = new ClaimAuction(auctionId, kingdom.getName(), chunk, startingBid, durationSeconds);
        activeAuctions.put(chunkKey, auction);
        
        return true;
    }
    
    /**
     * Place a bid on an auction
     */
    public boolean placeBid(Kingdom bidderKingdom, Chunk chunk, double bidAmount) {
        String chunkKey = getChunkKey(chunk);
        ClaimAuction auction = activeAuctions.get(chunkKey);
        
        if (auction == null || !auction.isActive() || auction.isExpired()) {
            return false;
        }
        
        // Check if bidder has enough money
        if (bankManager.getBalance(bidderKingdom.getName()) < bidAmount) {
            return false;
        }
        
        // Refund previous bidder if exists
        if (auction.getCurrentBidder() != null) {
            Kingdom previousBidder = kingdomManager.getKingdom(auction.getCurrentBidder());
            if (previousBidder != null) {
                bankManager.deposit(previousBidder.getName(), auction.getCurrentBid());
            }
        }
        
        // Place new bid
        if (auction.placeBid(bidderKingdom.getName(), bidAmount)) {
            // Withdraw money from bidder
            if (bankManager.withdraw(bidderKingdom.getName(), bidAmount)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * End an auction and transfer claim to winner
     */
    public boolean endAuction(Chunk chunk) {
        String chunkKey = getChunkKey(chunk);
        ClaimAuction auction = activeAuctions.get(chunkKey);
        
        if (auction == null || !auction.isActive()) {
            return false;
        }
        
        if (!auction.isExpired() && auction.getCurrentBidder() == null) {
            return false; // Auction not expired and no bids
        }
        
        if (auction.getCurrentBidder() == null) {
            // No bids, just cancel
            auction.setActive(false);
            activeAuctions.remove(chunkKey);
            return true;
        }
        
        Kingdom sellerKingdom = kingdomManager.getKingdom(auction.getSellerKingdom());
        Kingdom buyerKingdom = kingdomManager.getKingdom(auction.getCurrentBidder());
        
        if (sellerKingdom == null || buyerKingdom == null) {
            return false;
        }
        
        // Transfer money to seller
        bankManager.deposit(sellerKingdom.getName(), auction.getCurrentBid());
        
        // Transfer claim
        if (transferClaim(sellerKingdom, buyerKingdom, chunk)) {
            auction.setActive(false);
            activeAuctions.remove(chunkKey);
            return true;
        }
        
        return false;
    }
    
    /**
     * Rent a claim
     */
    public boolean rentClaim(Kingdom renterKingdom, Chunk chunk, double dailyRate, long durationDays) {
        String chunkKey = getChunkKey(chunk);
        
        // Check if chunk is already rented
        if (activeRents.containsKey(chunkKey)) {
            return false;
        }
        
        Kingdom ownerKingdom = kingdomManager.getKingdomByChunk(chunk);
        if (ownerKingdom == null || ownerKingdom == renterKingdom) {
            return false;
        }
        
        // Calculate total cost
        double totalCost = dailyRate * durationDays;
        if (bankManager.getBalance(renterKingdom.getName()) < totalCost) {
            return false;
        }
        
        // Charge renter
        if (bankManager.withdraw(renterKingdom.getName(), totalCost)) {
            bankManager.deposit(ownerKingdom.getName(), totalCost);
        } else {
            return false;
        }
        
        String rentId = UUID.randomUUID().toString();
        ClaimRent rent = new ClaimRent(rentId, ownerKingdom.getName(), renterKingdom.getName(), chunk, dailyRate, durationDays);
        activeRents.put(chunkKey, rent);
        
        return true;
    }
    
    /**
     * Calculate claim value based on location and resources
     */
    public double calculateClaimValue(Chunk chunk) {
        double baseValue = 100.0;
        
        // Add value based on biome (some biomes are more valuable)
        Biome biome = chunk.getBlock(8, 64, 8).getBiome();
        String biomeName = biome.getKey().getKey().toUpperCase();
        if (biomeName.contains("OCEAN") || biomeName.contains("DEEP")) {
            baseValue += 50.0;
        } else if (biomeName.contains("PLAINS") || biomeName.contains("FOREST")) {
            baseValue += 30.0;
        }
        
        // Add value based on nearby claims (more isolated = more valuable)
        int nearbyClaims = 0;
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                if (x == 0 && z == 0) continue;
                Chunk nearby = chunk.getWorld().getChunkAt(chunk.getX() + x, chunk.getZ() + z);
                if (kingdomManager.getKingdomByChunk(nearby) != null) {
                    nearbyClaims++;
                }
            }
        }
        baseValue += (10 - Math.min(nearbyClaims, 10)) * 10.0;
        
        return baseValue;
    }
    
    /**
     * Transfer a claim from one kingdom to another
     */
    private boolean transferClaim(Kingdom fromKingdom, Kingdom toKingdom, Chunk chunk) {
        // Unclaim from seller
        if (!claimManager.unclaimChunk(fromKingdom, chunk)) {
            return false;
        }
        
        // Claim for buyer
        if (!claimManager.claimChunk(toKingdom, chunk)) {
            // If claim fails, try to reclaim for seller (rollback)
            claimManager.claimChunk(fromKingdom, chunk);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get all active sales
     */
    public List<ClaimSale> getActiveSales() {
        return new ArrayList<>(activeSales.values());
    }
    
    /**
     * Get all active auctions
     */
    public List<ClaimAuction> getActiveAuctions() {
        return new ArrayList<>(activeAuctions.values());
    }
    
    /**
     * Get all active rents
     */
    public List<ClaimRent> getActiveRents() {
        return new ArrayList<>(activeRents.values());
    }
    
    /**
     * Get market sales for a kingdom
     */
    public List<ClaimSale> getMarketSales(String kingdomName) {
        return marketSales.getOrDefault(kingdomName, new ArrayList<>());
    }
    
    /**
     * Get sale for a chunk
     */
    public ClaimSale getSale(Chunk chunk) {
        return activeSales.get(getChunkKey(chunk));
    }
    
    /**
     * Get auction for a chunk
     */
    public ClaimAuction getAuction(Chunk chunk) {
        return activeAuctions.get(getChunkKey(chunk));
    }
    
    /**
     * Get rent for a chunk
     */
    public ClaimRent getRent(Chunk chunk) {
        return activeRents.get(getChunkKey(chunk));
    }
    
    /**
     * Cancel a sale
     */
    public boolean cancelSale(Kingdom kingdom, Chunk chunk) {
        String chunkKey = getChunkKey(chunk);
        ClaimSale sale = activeSales.get(chunkKey);
        
        if (sale == null || !sale.getSellerKingdom().equals(kingdom.getName())) {
            return false;
        }
        
        sale.setActive(false);
        activeSales.remove(chunkKey);
        marketSales.get(kingdom.getName()).remove(sale);
        return true;
    }
    
    /**
     * Cancel an auction
     */
    public boolean cancelAuction(Kingdom kingdom, Chunk chunk) {
        String chunkKey = getChunkKey(chunk);
        ClaimAuction auction = activeAuctions.get(chunkKey);
        
        if (auction == null || !auction.getSellerKingdom().equals(kingdom.getName())) {
            return false;
        }
        
        // Refund bidders
        if (auction.getCurrentBidder() != null) {
            Kingdom bidder = kingdomManager.getKingdom(auction.getCurrentBidder());
            if (bidder != null) {
                bankManager.deposit(bidder.getName(), auction.getCurrentBid());
            }
        }
        
        auction.setActive(false);
        activeAuctions.remove(chunkKey);
        return true;
    }
    
    /**
     * Check and expire rents
     */
    public void checkExpiredRents() {
        List<String> expired = new ArrayList<>();
        for (Map.Entry<String, ClaimRent> entry : activeRents.entrySet()) {
            ClaimRent rent = entry.getValue();
            if (rent.isExpired()) {
                expired.add(entry.getKey());
                rent.setActive(false);
            }
        }
        for (String key : expired) {
            activeRents.remove(key);
        }
    }
    
    /**
     * Check and expire auctions
     */
    public void checkExpiredAuctions() {
        List<String> expired = new ArrayList<>();
        for (Map.Entry<String, ClaimAuction> entry : activeAuctions.entrySet()) {
            ClaimAuction auction = entry.getValue();
            if (auction.isExpired()) {
                expired.add(entry.getKey());
                endAuction(auction.getChunk());
            }
        }
    }
    
    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
}

