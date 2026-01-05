package com.excrele.kingdoms.util;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages holographic displays (floating text) using ArmorStands
 * Works without external dependencies
 */
public class HologramManager {
    private final KingdomsPlugin plugin;
    // location key -> List of ArmorStands (hologram lines)
    private final Map<String, List<ArmorStand>> holograms;
    // kingdom -> List of hologram location keys
    private final Map<String, List<String>> kingdomHolograms;
    
    public HologramManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.holograms = new ConcurrentHashMap<>();
        this.kingdomHolograms = new ConcurrentHashMap<>();
    }
    
    /**
     * Create a hologram at a location
     */
    public void createHologram(Location location, List<String> lines) {
        String key = getLocationKey(location);
        
        // Remove existing hologram if any
        removeHologram(location);
        
        List<ArmorStand> stands = new ArrayList<>();
        Location currentLoc = location.clone().add(0, lines.size() * 0.3, 0);
        
        for (String line : lines) {
            ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(currentLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName(line);
            stand.setInvulnerable(true);
            stand.setMarker(true); // Makes it not interactable
            stand.setSmall(true);
            
            stands.add(stand);
            currentLoc.subtract(0, 0.3, 0); // Move down for next line
        }
        
        holograms.put(key, stands);
    }
    
    /**
     * Create kingdom spawn hologram
     */
    public void createSpawnHologram(String kingdomName, Location spawnLocation) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        
        List<String> lines = new ArrayList<>();
        lines.add("§6§l" + kingdomName);
        lines.add("§7Level: §e" + kingdom.getLevel());
        lines.add("§7Members: §e" + (kingdom.getMembers().size() + 1));
        lines.add("§7Claims: §e" + kingdom.getCurrentClaimChunks());
        
        createHologram(spawnLocation.clone().add(0.5, 2, 0.5), lines);
        
        kingdomHolograms.computeIfAbsent(kingdomName, k -> new ArrayList<>()).add(getLocationKey(spawnLocation));
    }
    
    /**
     * Create chunk border hologram
     */
    public void createChunkBorderHologram(Location location, String kingdomName, String displayText) {
        List<String> lines = new ArrayList<>();
        lines.add("§6§l" + kingdomName);
        lines.add("§7" + displayText);
        
        createHologram(location.clone().add(8, 1, 8), lines);
    }
    
    /**
     * Update hologram text
     */
    public void updateHologram(Location location, List<String> newLines) {
        String key = getLocationKey(location);
        List<ArmorStand> stands = holograms.get(key);
        if (stands == null) {
            createHologram(location, newLines);
            return;
        }
        
        // Update existing stands
        int i = 0;
        for (ArmorStand stand : stands) {
            if (i < newLines.size()) {
                stand.setCustomName(newLines.get(i));
            } else {
                stand.remove(); // Remove extra lines
            }
            i++;
        }
        
        // Add new lines if needed
        if (newLines.size() > stands.size()) {
            Location currentLoc = location.clone().add(0, stands.size() * 0.3, 0);
            for (int j = stands.size(); j < newLines.size(); j++) {
                ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(currentLoc, EntityType.ARMOR_STAND);
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setCustomNameVisible(true);
                stand.setCustomName(newLines.get(j));
                stand.setInvulnerable(true);
                stand.setMarker(true);
                stand.setSmall(true);
                stands.add(stand);
                currentLoc.subtract(0, 0.3, 0);
            }
        }
    }
    
    /**
     * Remove hologram at location
     */
    public void removeHologram(Location location) {
        String key = getLocationKey(location);
        List<ArmorStand> stands = holograms.remove(key);
        if (stands != null) {
            for (ArmorStand stand : stands) {
                stand.remove();
            }
        }
    }
    
    /**
     * Remove all holograms for a kingdom
     */
    public void removeKingdomHolograms(String kingdomName) {
        List<String> keys = kingdomHolograms.remove(kingdomName);
        if (keys != null) {
            for (String key : keys) {
                List<ArmorStand> stands = holograms.remove(key);
                if (stands != null) {
                    for (ArmorStand stand : stands) {
                        stand.remove();
                    }
                }
            }
        }
    }
    
    /**
     * Show hologram to specific player (for per-player visibility)
     */
    public void showHologramToPlayer(Player player, Location location, List<String> lines) {
        // For per-player holograms, we'd need to use packets
        // For now, create visible hologram
        createHologram(location, lines);
    }
    
    private String getLocationKey(Location location) {
        return location.getWorld().getName() + ":" + 
               (int)location.getX() + ":" + (int)location.getY() + ":" + (int)location.getZ();
    }
    
    /**
     * Cleanup all holograms
     */
    public void cleanup() {
        for (List<ArmorStand> stands : holograms.values()) {
            for (ArmorStand stand : stands) {
                stand.remove();
            }
        }
        holograms.clear();
        kingdomHolograms.clear();
    }
}

