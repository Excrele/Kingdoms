package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.ClaimMarker;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages visual feedback for claims (borders, markers, names)
 */
public class VisualManager {
    private final KingdomsPlugin plugin;
    private final Map<String, ClaimMarker> markers; // chunkKey -> marker
    private final Map<String, String> claimNames; // chunkKey -> name
    private final Map<UUID, Set<String>> activeBorders; // player -> set of chunk keys showing borders
    
    public VisualManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.markers = new ConcurrentHashMap<>();
        this.claimNames = new ConcurrentHashMap<>();
        this.activeBorders = new ConcurrentHashMap<>();
    }
    
    /**
     * Show claim border to player
     */
    public void showBorder(Player player, Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return;
        
        String chunkKey = getChunkKey(chunk);
        activeBorders.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(chunkKey);
        
        // Get kingdom color (with fallback)
        String colorCode = "§f"; // Default white
        try {
            com.excrele.kingdoms.manager.CustomizationManager customManager = plugin.getCustomizationManager();
            if (customManager != null) {
                com.excrele.kingdoms.model.KingdomCustomization customization = customManager.getCustomization(kingdom.getName());
                if (customization != null) {
                    colorCode = customization.getColorCode();
                }
            }
        } catch (Exception e) {
            // Fallback to default color
        }
        Particle particle = getParticleFromColor(colorCode);
        
        displayChunkBorder(player, chunk, particle);
    }
    
    /**
     * Hide claim border for player
     */
    public void hideBorder(Player player, Chunk chunk) {
        String chunkKey = getChunkKey(chunk);
        Set<String> borders = activeBorders.get(player.getUniqueId());
        if (borders != null) {
            borders.remove(chunkKey);
        }
    }
    
    /**
     * Toggle persistent borders for player
     */
    public void togglePersistentBorders(Player player) {
        UUID playerId = player.getUniqueId();
        if (activeBorders.containsKey(playerId) && !activeBorders.get(playerId).isEmpty()) {
            activeBorders.remove(playerId);
            player.sendMessage("§7Persistent borders disabled");
        } else {
            // Show borders for all chunks player is near
            Chunk currentChunk = player.getLocation().getChunk();
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    Chunk nearby = currentChunk.getWorld().getChunkAt(
                        currentChunk.getX() + x, currentChunk.getZ() + z);
                    Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(nearby);
                    if (kingdom != null) {
                        showBorder(player, nearby);
                    }
                }
            }
            player.sendMessage("§aPersistent borders enabled");
        }
    }
    
    /**
     * Create a claim marker at chunk corner
     */
    public boolean createMarker(Kingdom kingdom, Chunk chunk, Location location, String name) {
        String chunkKey = getChunkKey(chunk);
        
        // Check if marker already exists
        if (markers.containsKey(chunkKey)) {
            return false;
        }
        
        String id = UUID.randomUUID().toString();
        ClaimMarker marker = new ClaimMarker(id, kingdom.getName(), chunk, location);
        if (name != null && !name.isEmpty()) {
            marker.setName(name);
            claimNames.put(chunkKey, name);
        }
        
        markers.put(chunkKey, marker);
        
        // Spawn armor stand marker
        spawnMarkerEntity(marker);
        
        return true;
    }
    
    /**
     * Remove a claim marker
     */
    public boolean removeMarker(Chunk chunk) {
        String chunkKey = getChunkKey(chunk);
        ClaimMarker marker = markers.remove(chunkKey);
        if (marker != null) {
            // Remove armor stand
            removeMarkerEntity(marker);
            claimNames.remove(chunkKey);
            return true;
        }
        return false;
    }
    
    /**
     * Set claim name
     */
    public void setClaimName(Chunk chunk, String name) {
        String chunkKey = getChunkKey(chunk);
        if (name != null && !name.isEmpty()) {
            claimNames.put(chunkKey, name);
            // Update marker if exists
            ClaimMarker marker = markers.get(chunkKey);
            if (marker != null) {
                marker.setName(name);
                updateMarkerEntity(marker);
            }
        } else {
            claimNames.remove(chunkKey);
            ClaimMarker marker = markers.get(chunkKey);
            if (marker != null) {
                marker.setName("");
                updateMarkerEntity(marker);
            }
        }
    }
    
    /**
     * Get claim name
     */
    public String getClaimName(Chunk chunk) {
        return claimNames.get(getChunkKey(chunk));
    }
    
    /**
     * Display chunk border with particles
     */
    private void displayChunkBorder(Player player, Chunk chunk, Particle particle) {
        int minX = chunk.getX() << 4;
        int minZ = chunk.getZ() << 4;
        int maxX = minX + 16;
        int maxZ = minZ + 16;
        int y = (int) player.getLocation().getY() + 1;
        
        org.bukkit.World world = chunk.getWorld();
        
        // Draw border lines
        for (int x = minX; x <= maxX; x += 2) {
            player.spawnParticle(particle, new Location(world, x, y, minZ), 1, 0, 0, 0, 0);
            player.spawnParticle(particle, new Location(world, x, y, maxZ), 1, 0, 0, 0, 0);
        }
        for (int z = minZ; z <= maxZ; z += 2) {
            player.spawnParticle(particle, new Location(world, minX, y, z), 1, 0, 0, 0, 0);
            player.spawnParticle(particle, new Location(world, maxX, y, z), 1, 0, 0, 0, 0);
        }
    }
    
    /**
     * Spawn marker entity (armor stand)
     */
    private void spawnMarkerEntity(ClaimMarker marker) {
        Location loc = marker.getLocation();
        if (loc.getWorld() == null) return;
        
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        String displayName = marker.getName().isEmpty() ? 
            marker.getChunk().getWorld().getName() + ":" + marker.getChunk().getX() + ":" + marker.getChunk().getZ() : 
            marker.getName();
        stand.setCustomName("§6[Claim Marker] " + displayName);
        stand.setMarker(true); // Makes it not interactable
    }
    
    /**
     * Remove marker entity
     */
    private void removeMarkerEntity(ClaimMarker marker) {
        Location loc = marker.getLocation();
        if (loc.getWorld() == null) return;
        
        // Find and remove armor stand
        for (org.bukkit.entity.Entity entity : loc.getWorld().getNearbyEntities(loc, 2, 2, 2)) {
            if (entity instanceof ArmorStand && entity.getCustomName() != null && 
                entity.getCustomName().contains("Claim Marker")) {
                entity.remove();
            }
        }
    }
    
    /**
     * Update marker entity
     */
    private void updateMarkerEntity(ClaimMarker marker) {
        Location loc = marker.getLocation();
        if (loc.getWorld() == null) return;
        
        for (org.bukkit.entity.Entity entity : loc.getWorld().getNearbyEntities(loc, 2, 2, 2)) {
            if (entity instanceof ArmorStand && entity.getCustomName() != null && 
                entity.getCustomName().contains("Claim Marker")) {
                String displayName = marker.getName().isEmpty() ? 
                    marker.getChunk().getWorld().getName() + ":" + marker.getChunk().getX() + ":" + marker.getChunk().getZ() : 
                    marker.getName();
                entity.setCustomName("§6[Claim Marker] " + displayName);
            }
        }
    }
    
    /**
     * Get particle from color code
     */
    private Particle getParticleFromColor(String colorCode) {
        // Map color codes to particles
        if (colorCode.contains("§c") || colorCode.contains("§4")) return Particle.DUST;
        if (colorCode.contains("§9") || colorCode.contains("§1")) return Particle.ENCHANT;
        if (colorCode.contains("§a") || colorCode.contains("§2")) return Particle.HAPPY_VILLAGER;
        if (colorCode.contains("§e") || colorCode.contains("§6")) return Particle.FLAME;
        if (colorCode.contains("§d") || colorCode.contains("§5")) return Particle.PORTAL;
        if (colorCode.contains("§b") || colorCode.contains("§3")) return Particle.SPLASH;
        return Particle.CLOUD; // Default
    }
    
    /**
     * Show 3D claim visualization (height limits)
     */
    public void show3DVisualization(Player player, Chunk chunk) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (kingdom == null) return;
        
        int minX = chunk.getX() << 4;
        int minZ = chunk.getZ() << 4;
        int maxX = minX + 16;
        int maxZ = minZ + 16;
        int minY = 0;
        int maxY = 256;
        
        org.bukkit.World world = chunk.getWorld();
        Particle particle = Particle.END_ROD;
        
        // Draw vertical lines at corners
        for (int y = minY; y <= maxY; y += 10) {
            player.spawnParticle(particle, new Location(world, minX, y, minZ), 1, 0, 0, 0, 0);
            player.spawnParticle(particle, new Location(world, maxX, y, minZ), 1, 0, 0, 0, 0);
            player.spawnParticle(particle, new Location(world, minX, y, maxZ), 1, 0, 0, 0, 0);
            player.spawnParticle(particle, new Location(world, maxX, y, maxZ), 1, 0, 0, 0, 0);
        }
    }
    
    /**
     * Get all markers for a kingdom
     */
    public List<ClaimMarker> getMarkers(String kingdomName) {
        List<ClaimMarker> result = new ArrayList<>();
        for (ClaimMarker marker : markers.values()) {
            if (marker.getKingdomName().equals(kingdomName) && marker.isActive()) {
                result.add(marker);
            }
        }
        return result;
    }
    
    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
}

