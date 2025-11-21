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
        return bankBalances.getOrDefault(kingdomName, 0.0);
    }

    public boolean deposit(String kingdomName, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(kingdomName);
        double newBalance = current + amount;
        bankBalances.put(kingdomName, newBalance);
        plugin.getStorageManager().getAdapter().saveBankBalance(kingdomName, newBalance);
        return true;
    }

    public boolean withdraw(String kingdomName, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(kingdomName);
        if (current < amount) return false;
        double newBalance = current - amount;
        bankBalances.put(kingdomName, newBalance);
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

