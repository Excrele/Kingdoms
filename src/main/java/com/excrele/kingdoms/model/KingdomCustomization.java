package com.excrele.kingdoms.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores kingdom customization data (banner, colors, motto, achievements)
 */
public class KingdomCustomization {
    private String kingdomName;
    private ItemStack banner; // Custom banner/flag
    private String colorCode; // Chat color code (e.g., "§6" for gold)
    private String motto; // Kingdom motto/description
    private List<String> achievements; // Unlocked achievements
    
    public KingdomCustomization(String kingdomName) {
        this.kingdomName = kingdomName;
        this.banner = new ItemStack(Material.WHITE_BANNER);
        this.colorCode = "§f"; // Default white
        this.motto = "";
        this.achievements = new ArrayList<>();
    }
    
    public String getKingdomName() { return kingdomName; }
    
    public ItemStack getBanner() { return banner; }
    public void setBanner(ItemStack banner) { 
        if (banner != null && (banner.getType() == Material.WHITE_BANNER || 
            banner.getType().name().contains("BANNER"))) {
            this.banner = banner;
        }
    }
    
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { 
        if (colorCode != null && colorCode.startsWith("§")) {
            this.colorCode = colorCode;
        }
    }
    
    public String getMotto() { return motto; }
    public void setMotto(String motto) { 
        this.motto = motto != null ? motto : "";
    }
    
    public List<String> getAchievements() { return achievements; }
    public void addAchievement(String achievement) {
        if (!achievements.contains(achievement)) {
            achievements.add(achievement);
        }
    }
    public boolean hasAchievement(String achievement) {
        return achievements.contains(achievement);
    }
}

