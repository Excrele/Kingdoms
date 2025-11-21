package com.excrele.kingdoms.gui;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.manager.VaultManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * GUI for kingdom vaults
 */
public class VaultGUI implements Listener {
    private final KingdomsPlugin plugin;
    private final VaultManager vaultManager;
    
    public VaultGUI(KingdomsPlugin plugin, VaultManager vaultManager) {
        this.plugin = plugin;
        this.vaultManager = vaultManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Open vault GUI for player
     */
    public void openVault(Player player, Kingdom kingdom) {
        if (!vaultManager.canAccessVault(player, kingdom)) {
            player.sendMessage("You don't have permission to access this vault!");
            return;
        }
        
        Inventory vault = vaultManager.getVault(kingdom);
        player.openInventory(vault);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        Inventory inventory = event.getInventory();
        String title = inventory.getViewers().isEmpty() ? "" : 
            event.getView().getTitle();
        
        if (!title.startsWith("Kingdom Vault: ")) return;
        
        String kingdomName = title.substring("Kingdom Vault: ".length());
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        
        if (kingdom == null) return;
        
        if (!vaultManager.canAccessVault(player, kingdom)) {
            event.setCancelled(true);
            player.sendMessage("You don't have permission to modify this vault!");
            return;
        }
        
        // Allow interaction - vault is shared storage
        // Save vault when player closes it
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        String title = event.getView().getTitle();
        
        if (!title.startsWith("Kingdom Vault: ")) return;
        
        String kingdomName = title.substring("Kingdom Vault: ".length());
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        
        if (kingdom != null) {
            // Save vault when closed
            vaultManager.saveVault(kingdom);
        }
    }
}

