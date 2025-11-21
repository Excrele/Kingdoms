package com.excrele.kingdoms.integration;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;

import java.lang.reflect.Method;

/**
 * Integration with Dynmap for claim visualization
 */
public class DynmapIntegration extends IntegrationManager {
    private Object dynmapAPI;
    private Object markerSet;
    private Map<String, Object> claimMarkers; // kingdomName -> marker
    
    public DynmapIntegration(KingdomsPlugin plugin) {
        super(plugin);
        this.claimMarkers = new HashMap<>();
    }
    
    @Override
    public boolean isAvailable() {
        return plugin.getServer().getPluginManager().getPlugin("dynmap") != null;
    }
    
    @Override
    public void enable() {
        if (!isAvailable()) return;
        
        try {
            dynmapAPI = plugin.getServer().getPluginManager().getPlugin("dynmap");
            if (dynmapAPI == null) return;
            
            // Use reflection to get marker API
            Method getMarkerAPIMethod = dynmapAPI.getClass().getMethod("getMarkerAPI");
            Object markerAPI = getMarkerAPIMethod.invoke(dynmapAPI);
            if (markerAPI != null) {
                Method createMarkerSetMethod = markerAPI.getClass().getMethod("createMarkerSet", 
                    String.class, String.class, java.util.Set.class, boolean.class);
                markerSet = createMarkerSetMethod.invoke(markerAPI,
                    "kingdoms.claims",
                    "Kingdom Claims",
                    null,
                    false
                );
            }
            
            // Update all existing claims
            updateAllClaims();
            enabled = true;
            plugin.getLogger().info("Dynmap integration enabled!");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to enable Dynmap integration: " + e.getMessage());
            enabled = false;
        }
    }
    
    @Override
    public void disable() {
        if (markerSet != null) {
            try {
                Method deleteMarkerSetMethod = markerSet.getClass().getMethod("deleteMarkerSet");
                deleteMarkerSetMethod.invoke(markerSet);
            } catch (Exception e) {
                // Ignore
            }
            markerSet = null;
        }
        claimMarkers.clear();
        enabled = false;
    }
    
    public void updateClaim(Kingdom kingdom, Chunk chunk) {
        if (!isEnabled() || markerSet == null) return;
        
        try {
            String kingdomName = kingdom.getName();
            String markerId = kingdomName + "_" + chunk.getX() + "_" + chunk.getZ();
            
            Object marker = claimMarkers.get(markerId);
            if (marker == null) {
                // Create new marker
                double[] x = new double[4];
                double[] z = new double[4];
                x[0] = chunk.getX() * 16;
                z[0] = chunk.getZ() * 16;
                x[1] = (chunk.getX() + 1) * 16;
                z[1] = chunk.getZ() * 16;
                x[2] = (chunk.getX() + 1) * 16;
                z[2] = (chunk.getZ() + 1) * 16;
                x[3] = chunk.getX() * 16;
                z[3] = (chunk.getZ() + 1) * 16;
                
                Method createAreaMarkerMethod = markerSet.getClass().getMethod("createAreaMarker",
                    String.class, String.class, boolean.class, String.class, double[].class, double[].class, boolean.class);
                marker = createAreaMarkerMethod.invoke(markerSet,
                    markerId,
                    "Kingdom: " + kingdomName,
                    false,
                    chunk.getWorld().getName(),
                    x, z,
                    false
                );
                claimMarkers.put(markerId, marker);
            }
            
            // Update marker properties
            if (marker != null) {
                Method setLabelMethod = marker.getClass().getMethod("setLabel", String.class);
                setLabelMethod.invoke(marker, "Kingdom: " + kingdomName);
                
                int color = getKingdomColor(kingdom);
                Method setFillStyleMethod = marker.getClass().getMethod("setFillStyle", double.class, int.class);
                setFillStyleMethod.invoke(marker, 0.3, color);
                
                Method setLineStyleMethod = marker.getClass().getMethod("setLineStyle", int.class, double.class, int.class);
                setLineStyleMethod.invoke(marker, 2, 1.0, color);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update Dynmap claim: " + e.getMessage());
        }
    }
    
    public void removeClaim(Chunk chunk) {
        if (!isEnabled()) return;
        
        try {
            // Find and remove marker
            String markerId = chunk.getX() + "_" + chunk.getZ();
            for (Map.Entry<String, Object> entry : claimMarkers.entrySet()) {
                if (entry.getKey().endsWith(markerId)) {
                    Method deleteMarkerMethod = entry.getValue().getClass().getMethod("deleteMarker");
                    deleteMarkerMethod.invoke(entry.getValue());
                    claimMarkers.remove(entry.getKey());
                    break;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to remove Dynmap claim: " + e.getMessage());
        }
    }
    
    private void updateAllClaims() {
        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            for (java.util.List<Chunk> claimGroup : kingdom.getClaims()) {
                for (Chunk chunk : claimGroup) {
                    updateClaim(kingdom, chunk);
                }
            }
        }
    }
    
    private int getKingdomColor(Kingdom kingdom) {
        // Simple color based on kingdom name hash
        return kingdom.getName().hashCode() & 0xFFFFFF;
    }
}
