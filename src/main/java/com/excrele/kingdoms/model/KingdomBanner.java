package com.excrele.kingdoms.model;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;

/**
 * Represents a custom kingdom banner/flag
 */
public class KingdomBanner {
    private String bannerId;
    private String kingdomName;
    private Location location;
    private Material bannerMaterial;
    private org.bukkit.block.banner.Pattern pattern; // Can be extended to support multiple patterns
    private String worldName;
    private double x, y, z;
    
    public KingdomBanner(String kingdomName, Location location, Material bannerMaterial) {
        this.bannerId = java.util.UUID.randomUUID().toString();
        this.kingdomName = kingdomName;
        this.location = location;
        this.bannerMaterial = bannerMaterial;
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }
    
    public KingdomBanner(String bannerId, String kingdomName, String worldName, 
                        double x, double y, double z, Material bannerMaterial) {
        this.bannerId = bannerId;
        this.kingdomName = kingdomName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.bannerMaterial = bannerMaterial;
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
        if (world != null) {
            this.location = new Location(world, x, y, z);
        }
    }
    
    public String getBannerId() { return bannerId; }
    public String getKingdomName() { return kingdomName; }
    public Location getLocation() { return location; }
    public Material getBannerMaterial() { return bannerMaterial; }
    public void setBannerMaterial(Material material) { this.bannerMaterial = material; }
    public org.bukkit.block.banner.Pattern getPattern() { return pattern; }
    public void setPattern(org.bukkit.block.banner.Pattern pattern) { this.pattern = pattern; }
    public String getWorldName() { return worldName; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    
    /**
     * Place banner in world
     */
    public void placeBanner() {
        if (location == null) return;
        
        location.getBlock().setType(bannerMaterial);
        BlockState state = location.getBlock().getState();
        if (state instanceof Banner) {
            Banner banner = (Banner) state;
            if (pattern != null) {
                java.util.List<org.bukkit.block.banner.Pattern> patterns = banner.getPatterns();
                if (patterns.isEmpty()) {
                    banner.setPattern(0, pattern);
                } else {
                    banner.setPattern(0, pattern);
                }
            }
            banner.update();
        }
    }
    
    /**
     * Remove banner from world
     */
    public void removeBanner() {
        if (location != null) {
            location.getBlock().setType(Material.AIR);
        }
    }
}

