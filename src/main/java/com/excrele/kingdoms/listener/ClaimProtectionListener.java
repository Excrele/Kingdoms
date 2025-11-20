package com.excrele.kingdoms.listener;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;

public class ClaimProtectionListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (player.hasPermission("kingdoms.admin")) return; // Admins bypass
        
        Chunk chunk = event.getBlock().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return; // Unclaimed chunks are not protected
        
        String playerKingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom == null || !playerKingdom.equals(kingdom.getName())) {
            // Check if kingdoms are allied
            Kingdom playerK = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(playerKingdom);
            if (playerK != null && playerK.isAllied(kingdom.getName())) {
                // Allied kingdoms can break blocks (optional - you can remove this if you want stricter protection)
                return;
            }
            event.setCancelled(true);
            player.sendMessage("§cYou cannot break blocks in " + kingdom.getName() + "'s territory!");
            return;
        }
        
        // Check plot flags
        if (!KingdomsPlugin.getInstance().getFlagManager().canBreak(player, chunk)) {
            event.setCancelled(true);
            player.sendMessage("§cYou don't have permission to break blocks in this plot!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (player.hasPermission("kingdoms.admin")) return; // Admins bypass
        
        Chunk chunk = event.getBlock().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return; // Unclaimed chunks are not protected
        
        String playerKingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom == null || !playerKingdom.equals(kingdom.getName())) {
            // Check if kingdoms are allied
            Kingdom playerK = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(playerKingdom);
            if (playerK != null && playerK.isAllied(kingdom.getName())) {
                // Allied kingdoms can place blocks (optional - you can remove this if you want stricter protection)
                return;
            }
            event.setCancelled(true);
            player.sendMessage("§cYou cannot place blocks in " + kingdom.getName() + "'s territory!");
            return;
        }
        
        // Check plot flags
        if (!KingdomsPlugin.getInstance().getFlagManager().canBuild(player, chunk)) {
            event.setCancelled(true);
            player.sendMessage("§cYou don't have permission to place blocks in this plot!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (player.hasPermission("kingdoms.admin")) return; // Admins bypass
        
        Chunk chunk = event.getEntity().getLocation().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return; // Unclaimed chunks are not protected
        
        // Check PvP flag
        java.util.Map<String, String> chunkFlags = kingdom.getPlotFlags(chunk);
        String pvpFlag = chunkFlags.getOrDefault("pvp", "false");
        if (!pvpFlag.equalsIgnoreCase("true")) {
            if (event.getEntity() instanceof Player) {
                event.setCancelled(true);
                player.sendMessage("§cPvP is disabled in this area!");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == org.bukkit.event.Event.Result.DENY) return;
        if (event.getClickedBlock() == null) return;
        Player player = event.getPlayer();
        if (player.hasPermission("kingdoms.admin")) return; // Admins bypass
        
        Chunk chunk = event.getClickedBlock().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return; // Unclaimed chunks are not protected
        
        String playerKingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        if (playerKingdom == null || !playerKingdom.equals(kingdom.getName())) {
            // Check if kingdoms are allied
            Kingdom playerK = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(playerKingdom);
            if (playerK == null || !playerK.isAllied(kingdom.getName())) {
                // Prevent interaction with chests, doors, etc. in enemy territory
                String blockType = event.getClickedBlock().getType().toString();
                if (blockType.contains("CHEST") ||
                    blockType.contains("DOOR") ||
                    blockType.contains("FURNACE") ||
                    blockType.contains("ANVIL") ||
                    blockType.contains("BARREL") ||
                    blockType.contains("SHULKER_BOX")) {
                    event.setCancelled(true);
                    player.sendMessage("§cYou cannot interact with blocks in " + kingdom.getName() + "'s territory!");
                }
            }
        }
    }
}

