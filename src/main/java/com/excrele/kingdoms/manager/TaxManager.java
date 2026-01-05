package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages automatic tax collection from kingdom members
 */
public class TaxManager {
    private final KingdomsPlugin plugin;
    // kingdom -> tax rate (percentage)
    private final Map<String, Double> taxRates;
    // kingdom -> last tax collection timestamp
    private final Map<String, Long> lastTaxCollection;
    // kingdom -> tax collection interval (seconds)
    private final Map<String, Long> taxIntervals;
    
    public TaxManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.taxRates = new ConcurrentHashMap<>();
        this.lastTaxCollection = new ConcurrentHashMap<>();
        this.taxIntervals = new ConcurrentHashMap<>();
        loadTaxSettings();
    }
    
    private void loadTaxSettings() {
        // Load from config/storage
        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            // Default tax settings
            taxRates.put(kingdom.getName(), plugin.getConfig().getDouble("tax.default-rate", 5.0));
            taxIntervals.put(kingdom.getName(), plugin.getConfig().getLong("tax.default-interval", 86400L)); // 24 hours
            lastTaxCollection.put(kingdom.getName(), System.currentTimeMillis() / 1000);
        }
    }
    
    /**
     * Set tax rate for a kingdom (percentage)
     */
    public boolean setTaxRate(String kingdomName, double rate) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        if (rate < 0 || rate > 100) return false; // Invalid rate
        
        taxRates.put(kingdomName, rate);
        saveTaxSettings(kingdomName);
        return true;
    }
    
    /**
     * Get tax rate for a kingdom
     */
    public double getTaxRate(String kingdomName) {
        return taxRates.getOrDefault(kingdomName, plugin.getConfig().getDouble("tax.default-rate", 5.0));
    }
    
    /**
     * Set tax collection interval
     */
    public boolean setTaxInterval(String kingdomName, long intervalSeconds) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        if (intervalSeconds < 3600) return false; // Minimum 1 hour
        
        taxIntervals.put(kingdomName, intervalSeconds);
        saveTaxSettings(kingdomName);
        return true;
    }
    
    /**
     * Get tax interval for a kingdom
     */
    public long getTaxInterval(String kingdomName) {
        return taxIntervals.getOrDefault(kingdomName, plugin.getConfig().getLong("tax.default-interval", 86400L));
    }
    
    /**
     * Collect taxes from all kingdom members
     */
    public void collectTaxes(String kingdomName) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        
        double taxRate = getTaxRate(kingdomName);
        if (taxRate <= 0) return; // No taxes
        
        double totalCollected = 0;
        int membersTaxed = 0;
        
        // Collect from all members including king
        for (String memberName : kingdom.getAllMembers()) {
            Player member = plugin.getServer().getPlayer(memberName);
            if (member == null || !member.isOnline()) continue; // Only tax online members
            
            double balance = com.excrele.kingdoms.util.EconomyManager.getBalance(member);
            if (balance <= 0) continue;
            
            double taxAmount = balance * (taxRate / 100.0);
            if (taxAmount > 0) {
                if (com.excrele.kingdoms.util.EconomyManager.withdraw(member, taxAmount)) {
                    plugin.getBankManager().deposit(kingdomName, taxAmount);
                    totalCollected += taxAmount;
                    membersTaxed++;
                    
                    member.sendMessage("§6[Tax] Paid §e" + String.format("%.2f", taxAmount) + 
                        " §6in taxes (" + taxRate + "%)");
                }
            }
        }
        
        if (totalCollected > 0) {
            lastTaxCollection.put(kingdomName, System.currentTimeMillis() / 1000);
            saveTaxSettings(kingdomName);
            
            // Notify kingdom
            String message = "§6[Tax] Collected §e" + String.format("%.2f", totalCollected) + 
                " §6from §e" + membersTaxed + " §6members";
            broadcastToKingdom(kingdom, message);
        }
    }
    
    /**
     * Check and collect taxes for all kingdoms
     */
    public void checkAndCollectTaxes() {
        long now = System.currentTimeMillis() / 1000;
        
        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            String kingdomName = kingdom.getName();
            long lastCollection = lastTaxCollection.getOrDefault(kingdomName, 0L);
            long interval = getTaxInterval(kingdomName);
            
            if (now - lastCollection >= interval) {
                collectTaxes(kingdomName);
            }
        }
    }
    
    /**
     * Get time until next tax collection
     */
    public long getTimeUntilNextCollection(String kingdomName) {
        long lastCollection = lastTaxCollection.getOrDefault(kingdomName, System.currentTimeMillis() / 1000);
        long interval = getTaxInterval(kingdomName);
        long nextCollection = lastCollection + interval;
        long now = System.currentTimeMillis() / 1000;
        
        return Math.max(0, nextCollection - now);
    }
    
    private void broadcastToKingdom(Kingdom kingdom, String message) {
        for (String member : kingdom.getMembers()) {
            Player player = plugin.getServer().getPlayer(member);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
        Player king = plugin.getServer().getPlayer(kingdom.getKing());
        if (king != null && king.isOnline()) {
            king.sendMessage(message);
        }
    }
    
    private void saveTaxSettings(String kingdomName) {
        plugin.getStorageManager().getAdapter().saveTaxSettings(
            kingdomName,
            getTaxRate(kingdomName),
            getTaxInterval(kingdomName),
            lastTaxCollection.getOrDefault(kingdomName, System.currentTimeMillis() / 1000)
        );
    }
}

