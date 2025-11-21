package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.KingdomCustomization;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages kingdom customization (banners, colors, motto, achievements)
 */
public class CustomizationManager {
    private final KingdomsPlugin plugin;
    private final Map<String, KingdomCustomization> customizations; // kingdomName -> customization
    
    public CustomizationManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.customizations = new HashMap<>();
        loadAllCustomizations();
    }
    
    /**
     * Load all customizations from storage
     */
    private void loadAllCustomizations() {
        for (String kingdomName : plugin.getKingdomManager().getKingdoms().keySet()) {
            loadCustomization(kingdomName);
        }
    }
    
    /**
     * Load customization for a kingdom
     */
    private void loadCustomization(String kingdomName) {
        // Load from config/storage
        String path = "customizations." + kingdomName;
        KingdomCustomization customization = new KingdomCustomization(kingdomName);
        
        if (plugin.getConfig().contains(path)) {
            if (plugin.getConfig().contains(path + ".color")) {
                customization.setColorCode(plugin.getConfig().getString(path + ".color"));
            }
            if (plugin.getConfig().contains(path + ".motto")) {
                customization.setMotto(plugin.getConfig().getString(path + ".motto"));
            }
            if (plugin.getConfig().contains(path + ".achievements")) {
                customization.getAchievements().addAll(plugin.getConfig().getStringList(path + ".achievements"));
            }
        }
        
        customizations.put(kingdomName, customization);
    }
    
    /**
     * Get customization for a kingdom
     */
    public KingdomCustomization getCustomization(String kingdomName) {
        return customizations.computeIfAbsent(kingdomName, KingdomCustomization::new);
    }
    
    /**
     * Set kingdom color
     */
    public boolean setColor(Kingdom kingdom, String colorName) {
        try {
            ChatColor color = ChatColor.valueOf(colorName.toUpperCase());
            KingdomCustomization customization = getCustomization(kingdom.getName());
            customization.setColorCode(color.toString());
            saveCustomization(kingdom.getName());
            return true;
        } catch (IllegalArgumentException e) {
            return false; // Invalid color name
        }
    }
    
    /**
     * Set kingdom motto
     */
    public boolean setMotto(Kingdom kingdom, String motto) {
        if (motto.length() > 100) return false; // Limit length
        
        KingdomCustomization customization = getCustomization(kingdom.getName());
        customization.setMotto(motto);
        saveCustomization(kingdom.getName());
        return true;
    }
    
    /**
     * Set kingdom banner
     */
    public boolean setBanner(Kingdom kingdom, ItemStack banner) {
        if (banner == null || (banner.getType() != Material.WHITE_BANNER && 
            !banner.getType().name().contains("BANNER"))) {
            return false;
        }
        
        KingdomCustomization customization = getCustomization(kingdom.getName());
        customization.setBanner(banner);
        saveCustomization(kingdom.getName());
        return true;
    }
    
    /**
     * Unlock achievement
     */
    public void unlockAchievement(Kingdom kingdom, String achievement) {
        KingdomCustomization customization = getCustomization(kingdom.getName());
        if (!customization.hasAchievement(achievement)) {
            customization.addAchievement(achievement);
            saveCustomization(kingdom.getName());
            
            // Notify kingdom members
            String message = customization.getColorCode() + "§l[Kingdom] §r" + 
                customization.getColorCode() + "Achievement Unlocked: §e" + achievement;
            for (String member : kingdom.getMembers()) {
                org.bukkit.entity.Player player = plugin.getServer().getPlayer(member);
                if (player != null && player.isOnline()) {
                    player.sendMessage(message);
                }
            }
            org.bukkit.entity.Player king = plugin.getServer().getPlayer(kingdom.getKing());
            if (king != null && king.isOnline()) {
                king.sendMessage(message);
            }
        }
    }
    
    /**
     * Save customization to storage
     */
    private void saveCustomization(String kingdomName) {
        KingdomCustomization customization = customizations.get(kingdomName);
        if (customization == null) return;
        
        String path = "customizations." + kingdomName;
        plugin.getConfig().set(path + ".color", customization.getColorCode());
        plugin.getConfig().set(path + ".motto", customization.getMotto());
        plugin.getConfig().set(path + ".achievements", customization.getAchievements());
        plugin.saveConfig();
    }
}

