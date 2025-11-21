package com.excrele.kingdoms.listener;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakEvent;

public class EnhancedProtectionListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) return;
        
        Chunk chunk = event.getLocation().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return;
        
        if (!KingdomsPlugin.getInstance().getFlagManager().isExplosionAllowed(chunk)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.isCancelled()) return;
        
        Chunk chunk = event.getBlock().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return;
        
        if (!KingdomsPlugin.getInstance().getFlagManager().isExplosionAllowed(chunk)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBurn(org.bukkit.event.block.BlockBurnEvent event) {
        if (event.isCancelled()) return;
        
        Chunk chunk = event.getBlock().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return;
        
        if (!KingdomsPlugin.getInstance().getFlagManager().isFireSpreadAllowed(chunk)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockIgnite(org.bukkit.event.block.BlockIgniteEvent event) {
        if (event.isCancelled()) return;
        
        Chunk chunk = event.getBlock().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return;
        
        if (!KingdomsPlugin.getInstance().getFlagManager().isFireSpreadAllowed(chunk) && 
            event.getCause() == org.bukkit.event.block.BlockIgniteEvent.IgniteCause.SPREAD) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.isCancelled()) return;
        
        // Prevent endermen from picking up blocks, etc.
        Chunk chunk = event.getBlock().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return;
        
        if (!KingdomsPlugin.getInstance().getFlagManager().isMobGriefAllowed(chunk) && 
            event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;
        
        Chunk chunk = event.getLocation().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return;
        
        if (!KingdomsPlugin.getInstance().getFlagManager().isMobSpawningAllowed(chunk)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.isCancelled()) return;
        
        Chunk chunk = event.getEntity().getLocation().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return;
        
        // Protect item frames, paintings, etc.
        if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            if (!KingdomsPlugin.getInstance().getFlagManager().isExplosionAllowed(chunk)) {
                event.setCancelled(true);
            }
        }
    }
    
    // Redstone protection is handled through PlayerInteractEvent in ClaimProtectionListener
    // This checks when players interact with redstone components
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonExtend(org.bukkit.event.block.BlockPistonExtendEvent event) {
        if (event.isCancelled()) return;
        
        Chunk chunk = event.getBlock().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return;
        
        // Check if piston is allowed - check flag directly since we don't have player context
        java.util.Map<String, String> flags = kingdom.getPlotFlags(chunk);
        String pistonFlag = flags.getOrDefault("piston", "members");
        if (pistonFlag.equalsIgnoreCase("false")) {
            event.setCancelled(true);
            return;
        }
        
        // Also check blocks being moved - prevent cross-kingdom movement
        for (org.bukkit.block.Block block : event.getBlocks()) {
            Chunk movedChunk = block.getChunk();
            Kingdom movedKingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(movedChunk);
            if (movedKingdom != null && !movedKingdom.equals(kingdom)) {
                // Prevent moving blocks across kingdom boundaries
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonRetract(org.bukkit.event.block.BlockPistonRetractEvent event) {
        if (event.isCancelled()) return;
        
        Chunk chunk = event.getBlock().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return;
        
        // Check if piston is allowed - check flag directly since we don't have player context
        java.util.Map<String, String> flags = kingdom.getPlotFlags(chunk);
        String pistonFlag = flags.getOrDefault("piston", "members");
        if (pistonFlag.equalsIgnoreCase("false")) {
            event.setCancelled(true);
            return;
        }
        
        // Also check blocks being moved - prevent cross-kingdom movement
        for (org.bukkit.block.Block block : event.getBlocks()) {
            Chunk movedChunk = block.getChunk();
            Kingdom movedKingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(movedChunk);
            if (movedKingdom != null && !movedKingdom.equals(kingdom)) {
                // Prevent moving blocks across kingdom boundaries
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityBreed(EntityBreedEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getBreeder() instanceof Player)) return;
        
        Player player = (Player) event.getBreeder();
        if (player.hasPermission("kingdoms.admin")) return; // Admins bypass
        
        Chunk chunk = event.getEntity().getLocation().getChunk();
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return;
        
        // Check if animal breeding is allowed
        if (!KingdomsPlugin.getInstance().getFlagManager().canBreedAnimals(player, chunk)) {
            event.setCancelled(true);
            player.sendMessage("§cAnimal breeding is not allowed in this area!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractCrop(org.bukkit.event.player.PlayerInteractEvent event) {
        if (event.useInteractedBlock() == org.bukkit.event.Event.Result.DENY) return;
        if (event.getAction() != org.bukkit.event.block.Action.PHYSICAL) return;
        if (event.getClickedBlock() == null) return;
        
        Player player = event.getPlayer();
        if (player.hasPermission("kingdoms.admin")) return; // Admins bypass
        
        org.bukkit.Material blockType = event.getClickedBlock().getType();
        // Check if it's a crop that can be trampled
        if (blockType == org.bukkit.Material.FARMLAND ||
            blockType == org.bukkit.Material.WHEAT ||
            blockType == org.bukkit.Material.CARROTS ||
            blockType == org.bukkit.Material.POTATOES ||
            blockType == org.bukkit.Material.BEETROOTS ||
            blockType == org.bukkit.Material.MELON_STEM ||
            blockType == org.bukkit.Material.PUMPKIN_STEM) {
            
            Chunk chunk = event.getClickedBlock().getChunk();
            Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
            if (kingdom == null) return;
            
            // Check if crop trampling is allowed
            if (!KingdomsPlugin.getInstance().getFlagManager().canTrampleCrops(player, chunk)) {
                event.setCancelled(true);
            }
        }
    }
}

