package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.KingdomBanner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages custom kingdom banners/flags
 */
public class BannerManager {
    private final KingdomsPlugin plugin;
    // kingdom -> List of banners
    private final Map<String, List<KingdomBanner>> kingdomBanners;
    // bannerId -> Banner
    private final Map<String, KingdomBanner> allBanners;
    
    public BannerManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.kingdomBanners = new ConcurrentHashMap<>();
        this.allBanners = new ConcurrentHashMap<>();
        loadAllBanners();
    }
    
    private void loadAllBanners() {
        List<Map<String, Object>> bannersData = plugin.getStorageManager().getAdapter().loadKingdomBanners();
        for (Map<String, Object> data : bannersData) {
            String bannerId = (String) data.get("bannerId");
            String kingdomName = (String) data.get("kingdomName");
            String worldName = (String) data.get("worldName");
            double x = ((Number) data.get("x")).doubleValue();
            double y = ((Number) data.get("y")).doubleValue();
            double z = ((Number) data.get("z")).doubleValue();
            String bannerMaterialStr = (String) data.getOrDefault("bannerMaterial", "WHITE_BANNER");
            
            try {
                Material bannerMaterial = Material.valueOf(bannerMaterialStr);
                KingdomBanner banner = new KingdomBanner(bannerId, kingdomName, worldName, x, y, z, bannerMaterial);
                kingdomBanners.computeIfAbsent(kingdomName, k -> new ArrayList<>()).add(banner);
                allBanners.put(bannerId, banner);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid banner material: " + bannerMaterialStr);
            }
        }
    }
    
    /**
     * Place a banner at a location
     */
    public boolean placeBanner(String kingdomName, Location location, Material bannerMaterial, Player placer) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        // Check permission
        if (!kingdom.hasPermission(placer.getName(), "setflags")) {
            return false;
        }
        
        // Check if location is in kingdom claim
        org.bukkit.Chunk chunk = location.getChunk();
        Kingdom chunkKingdom = plugin.getKingdomManager().getKingdomByChunk(chunk);
        if (chunkKingdom == null || !chunkKingdom.getName().equals(kingdomName)) {
            return false; // Not in kingdom claim
        }
        
        // Check banner limit
        int maxBanners = plugin.getConfig().getInt("banners.max-per-kingdom", 10);
        List<KingdomBanner> banners = kingdomBanners.getOrDefault(kingdomName, new ArrayList<>());
        if (banners.size() >= maxBanners) {
            return false; // Too many banners
        }
        
        // Create banner
        KingdomBanner banner = new KingdomBanner(kingdomName, location, bannerMaterial);
        kingdomBanners.computeIfAbsent(kingdomName, k -> new ArrayList<>()).add(banner);
        allBanners.put(banner.getBannerId(), banner);
        
        // Place in world
        banner.placeBanner();
        
        // Save to storage
        saveBanner(banner);
        
        placer.sendMessage("§aBanner placed!");
        return true;
    }
    
    /**
     * Remove a banner
     */
    public boolean removeBanner(String kingdomName, String bannerId, Player remover) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        KingdomBanner banner = allBanners.get(bannerId);
        if (banner == null || !banner.getKingdomName().equals(kingdomName)) {
            return false;
        }
        
        // Check permission
        if (!kingdom.hasPermission(remover.getName(), "setflags")) {
            return false;
        }
        
        // Remove banner
        banner.removeBanner();
        kingdomBanners.get(kingdomName).remove(banner);
        allBanners.remove(bannerId);
        
        // Delete from storage
        plugin.getStorageManager().getAdapter().deleteKingdomBanner(bannerId);
        
        remover.sendMessage("§aBanner removed!");
        return true;
    }
    
    /**
     * Get all banners for a kingdom
     */
    public List<KingdomBanner> getKingdomBanners(String kingdomName) {
        return new ArrayList<>(kingdomBanners.getOrDefault(kingdomName, new ArrayList<>()));
    }
    
    /**
     * Update banner material
     */
    public boolean updateBanner(String kingdomName, String bannerId, Material newMaterial, Player updater) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        KingdomBanner banner = allBanners.get(bannerId);
        if (banner == null || !banner.getKingdomName().equals(kingdomName)) {
            return false;
        }
        
        // Check permission
        if (!kingdom.hasPermission(updater.getName(), "setflags")) {
            return false;
        }
        
        banner.setBannerMaterial(newMaterial);
        banner.placeBanner();
        saveBanner(banner);
        
        updater.sendMessage("§aBanner updated!");
        return true;
    }
    
    private void saveBanner(KingdomBanner banner) {
        plugin.getStorageManager().getAdapter().saveKingdomBanner(
            banner.getBannerId(),
            banner.getKingdomName(),
            banner.getWorldName(),
            banner.getX(),
            banner.getY(),
            banner.getZ(),
            banner.getBannerMaterial().name()
        );
    }
}

