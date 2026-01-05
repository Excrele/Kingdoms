package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BankManager {
    private final KingdomsPlugin plugin;
    private final Map<String, Double> bankBalances; // kingdom -> balance

    public BankManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.bankBalances = new HashMap<>();
        loadAllBalances();
    }

    private void loadAllBalances() {
        for (String kingdomName : plugin.getKingdomManager().getKingdoms().keySet()) {
            double balance = plugin.getStorageManager().getAdapter().loadBankBalance(kingdomName);
            bankBalances.put(kingdomName, balance);
        }
    }

    public double getBalance(String kingdomName) {
        // Try cache first
        if (plugin.getDataCache() != null) {
            Double cached = plugin.getDataCache().getCachedBankBalance(kingdomName);
            if (cached != null) {
                return cached;
            }
        }
        
        double balance = bankBalances.getOrDefault(kingdomName, 0.0);
        
        // Cache the result
        if (plugin.getDataCache() != null) {
            plugin.getDataCache().cacheBankBalance(kingdomName, balance);
        }
        
        return balance;
    }
    
    /**
     * Get maximum bank capacity (with treasury structure bonus)
     */
    public double getMaxCapacity(String kingdomName) {
        double baseCapacity = plugin.getConfig().getDouble("bank.max-capacity", 1000000.0);
        
        if (plugin.getStructureManager() != null) {
            double treasuryBonus = plugin.getStructureManager()
                .getStructureBonus(kingdomName, com.excrele.kingdoms.model.KingdomStructure.StructureType.TREASURY);
            return baseCapacity * treasuryBonus;
        }
        
        return baseCapacity;
    }

    public boolean deposit(String kingdomName, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(kingdomName);
        double newBalance = current + amount;
        bankBalances.put(kingdomName, newBalance);
        
        // Update cache
        if (plugin.getDataCache() != null) {
            plugin.getDataCache().cacheBankBalance(kingdomName, newBalance);
        }
        
        plugin.getStorageManager().getAdapter().saveBankBalance(kingdomName, newBalance);
        return true;
    }

    public boolean withdraw(String kingdomName, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(kingdomName);
        if (current < amount) return false;
        double newBalance = current - amount;
        bankBalances.put(kingdomName, newBalance);
        
        // Update cache
        if (plugin.getDataCache() != null) {
            plugin.getDataCache().cacheBankBalance(kingdomName, newBalance);
        }
        
        plugin.getStorageManager().getAdapter().saveBankBalance(kingdomName, newBalance);
        return true;
    }

    public boolean transfer(String fromKingdom, String toKingdom, double amount) {
        if (withdraw(fromKingdom, amount)) {
            return deposit(toKingdom, amount);
        }
        return false;
    }

    public boolean depositFromPlayer(Player player, String kingdomName, double amount) {
        if (!com.excrele.kingdoms.util.EconomyManager.isEnabled()) {
            return false;
        }
        if (!com.excrele.kingdoms.util.EconomyManager.hasEnough(player, amount)) {
            return false;
        }
        if (com.excrele.kingdoms.util.EconomyManager.withdraw(player, amount)) {
            return deposit(kingdomName, amount);
        }
        return false;
    }

    public boolean withdrawToPlayer(Player player, String kingdomName, double amount) {
        if (!com.excrele.kingdoms.util.EconomyManager.isEnabled()) {
            return false;
        }
        if (withdraw(kingdomName, amount)) {
            com.excrele.kingdoms.util.EconomyManager.deposit(player, amount);
            return true;
        }
        return false;
    }

    public void collectTax(String kingdomName) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        
        // Collect tax from each member (if enabled)
        // This is a simplified version - you could make it more sophisticated
        // Implementation would depend on your tax collection strategy
        // TODO: Implement tax collection logic
    }
}

