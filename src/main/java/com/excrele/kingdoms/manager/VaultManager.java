package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages kingdom vaults (shared storage)
 */
public class VaultManager {
    private final KingdomsPlugin plugin;
    private final Map<String, org.bukkit.inventory.Inventory> vaults; // kingdom -> inventory
    
    public VaultManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.vaults = new HashMap<>();
    }
    
    /**
     * Get or create vault inventory for a kingdom
     */
    public org.bukkit.inventory.Inventory getVault(Kingdom kingdom) {
        String kingdomName = kingdom.getName();
        if (!vaults.containsKey(kingdomName)) {
            // Create a new vault inventory (54 slots = double chest)
            org.bukkit.inventory.Inventory vault = plugin.getServer().createInventory(null, 54, 
                "Kingdom Vault: " + kingdomName);
            vaults.put(kingdomName, vault);
            loadVault(kingdom);
        }
        return vaults.get(kingdomName);
    }
    
    /**
     * Check if player can access vault
     */
    public boolean canAccessVault(Player player, Kingdom kingdom) {
        if (player.getName().equals(kingdom.getKing())) {
            return true; // King always has access
        }
        
        if (kingdom.getMembers().contains(player.getName())) {
            // All members can access, but you could restrict based on role
            return true;
        }
        
        return false;
    }
    
    /**
     * Add item to vault
     */
    public boolean addItem(Kingdom kingdom, ItemStack item) {
        org.bukkit.inventory.Inventory vault = getVault(kingdom);
        Map<Integer, ItemStack> leftover = vault.addItem(item);
        saveVault(kingdom);
        return leftover.isEmpty(); // Returns true if all items were added
    }
    
    /**
     * Remove item from vault
     */
    public boolean removeItem(Kingdom kingdom, ItemStack item) {
        org.bukkit.inventory.Inventory vault = getVault(kingdom);
        boolean removed = vault.removeItem(item).isEmpty();
        saveVault(kingdom);
        return removed;
    }
    
    /**
     * Save vault to storage
     */
    public void saveVault(Kingdom kingdom) {
        org.bukkit.inventory.Inventory vault = getVault(kingdom);
        // Save vault contents to storage adapter
        // This would need to be implemented in storage adapters
        plugin.getStorageManager().getAdapter().saveVault(kingdom.getName(), vault);
    }
    
    /**
     * Load vault from storage
     */
    public void loadVault(Kingdom kingdom) {
        org.bukkit.inventory.Inventory vault = getVault(kingdom);
        // Load vault contents from storage adapter
        Map<Integer, ItemStack> items = plugin.getStorageManager().getAdapter().loadVault(kingdom.getName());
        if (items != null) {
            for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                vault.setItem(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * Get vault size (number of slots)
     */
    public int getVaultSize(Kingdom kingdom) {
        return getVault(kingdom).getSize();
    }
    
    /**
     * Check if vault has space
     */
    public boolean hasSpace(Kingdom kingdom, ItemStack item) {
        org.bukkit.inventory.Inventory vault = getVault(kingdom);
        Map<Integer, ItemStack> leftover = vault.addItem(item);
        // Remove the item we just added for testing
        if (!leftover.isEmpty()) {
            vault.removeItem(item);
            return false;
        }
        vault.removeItem(item);
        return true;
    }
}

