package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.KingdomTheme;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages kingdom color themes and visual customization
 */
public class ThemeManager {
    private final KingdomsPlugin plugin;
    // kingdom -> Theme
    private final Map<String, KingdomTheme> themes;
    
    public ThemeManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.themes = new ConcurrentHashMap<>();
        loadAllThemes();
    }
    
    private void loadAllThemes() {
        List<Map<String, Object>> themesData = plugin.getStorageManager().getAdapter().loadKingdomThemes();
        for (Map<String, Object> data : themesData) {
            String kingdomName = (String) data.get("kingdomName");
            String primaryColorStr = (String) data.getOrDefault("primaryColor", "GOLD");
            String secondaryColorStr = (String) data.getOrDefault("secondaryColor", "YELLOW");
            String accentColorStr = (String) data.getOrDefault("accentColor", "WHITE");
            String bannerMaterialStr = (String) data.getOrDefault("bannerMaterial", "WHITE_BANNER");
            String flagMaterialStr = (String) data.getOrDefault("flagMaterial", "WHITE_BANNER");
            String primaryParticleStr = (String) data.getOrDefault("primaryParticle", "FLAME");
            String secondaryParticleStr = (String) data.getOrDefault("secondaryParticle", "ENCHANT");
            String themeName = (String) data.getOrDefault("themeName", "Default");
            
            try {
                ChatColor primaryColor = ChatColor.valueOf(primaryColorStr);
                ChatColor secondaryColor = ChatColor.valueOf(secondaryColorStr);
                ChatColor accentColor = ChatColor.valueOf(accentColorStr);
                Material bannerMaterial = Material.valueOf(bannerMaterialStr);
                Material flagMaterial = Material.valueOf(flagMaterialStr);
                Particle primaryParticle = Particle.valueOf(primaryParticleStr);
                Particle secondaryParticle = Particle.valueOf(secondaryParticleStr);
                
                KingdomTheme theme = new KingdomTheme(kingdomName, primaryColor, secondaryColor,
                                                     accentColor, bannerMaterial, flagMaterial,
                                                     primaryParticle, secondaryParticle, themeName);
                themes.put(kingdomName, theme);
            } catch (IllegalArgumentException e) {
                // Use default theme
                themes.put(kingdomName, new KingdomTheme(kingdomName));
            }
        }
        
        // Create default themes for kingdoms without themes
        for (Kingdom kingdom : plugin.getKingdomManager().getKingdoms().values()) {
            if (!themes.containsKey(kingdom.getName())) {
                themes.put(kingdom.getName(), new KingdomTheme(kingdom.getName()));
            }
        }
    }
    
    /**
     * Get theme for a kingdom
     */
    public KingdomTheme getTheme(String kingdomName) {
        return themes.getOrDefault(kingdomName, new KingdomTheme(kingdomName));
    }
    
    /**
     * Set theme for a kingdom
     */
    public boolean setTheme(String kingdomName, KingdomTheme theme) {
        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return false;
        
        themes.put(kingdomName, theme);
        saveTheme(theme);
        return true;
    }
    
    /**
     * Update theme colors
     */
    public boolean updateThemeColors(String kingdomName, ChatColor primary, ChatColor secondary, ChatColor accent) {
        KingdomTheme theme = getTheme(kingdomName);
        theme.setPrimaryColor(primary);
        theme.setSecondaryColor(secondary);
        theme.setAccentColor(accent);
        saveTheme(theme);
        return true;
    }
    
    /**
     * Update theme particles
     */
    public boolean updateThemeParticles(String kingdomName, Particle primary, Particle secondary) {
        KingdomTheme theme = getTheme(kingdomName);
        theme.setPrimaryParticle(primary);
        theme.setSecondaryParticle(secondary);
        saveTheme(theme);
        return true;
    }
    
    /**
     * Update theme banner/flag
     */
    public boolean updateThemeBanner(String kingdomName, Material bannerMaterial, Material flagMaterial) {
        KingdomTheme theme = getTheme(kingdomName);
        theme.setBannerMaterial(bannerMaterial);
        theme.setFlagMaterial(flagMaterial);
        saveTheme(theme);
        return true;
    }
    
    /**
     * Apply preset theme
     */
    public boolean applyPresetTheme(String kingdomName, String presetName) {
        KingdomTheme preset = getPresetTheme(presetName);
        if (preset == null) return false;
        
        KingdomTheme theme = new KingdomTheme(kingdomName, preset.getPrimaryColor(), 
                                            preset.getSecondaryColor(), preset.getAccentColor(),
                                            preset.getBannerMaterial(), preset.getFlagMaterial(),
                                            preset.getPrimaryParticle(), preset.getSecondaryParticle(),
                                            presetName);
        return setTheme(kingdomName, theme);
    }
    
    /**
     * Get preset theme
     */
    private KingdomTheme getPresetTheme(String presetName) {
        return switch (presetName.toLowerCase()) {
            case "royal" -> new KingdomTheme("preset", ChatColor.GOLD, ChatColor.YELLOW, ChatColor.WHITE,
                                            Material.YELLOW_BANNER, Material.YELLOW_BANNER,
                                            Particle.FLAME, Particle.ENCHANT, "Royal");
            case "warrior" -> new KingdomTheme("preset", ChatColor.RED, ChatColor.DARK_RED, ChatColor.GRAY,
                                              Material.RED_BANNER, Material.RED_BANNER,
                                              Particle.LAVA, Particle.LARGE_SMOKE, "Warrior");
            case "nature" -> new KingdomTheme("preset", ChatColor.GREEN, ChatColor.DARK_GREEN, ChatColor.YELLOW,
                                             Material.GREEN_BANNER, Material.GREEN_BANNER,
                                             Particle.HAPPY_VILLAGER, Particle.END_ROD, "Nature");
            case "mystic" -> new KingdomTheme("preset", ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE, ChatColor.WHITE,
                                             Material.PURPLE_BANNER, Material.PURPLE_BANNER,
                                             Particle.PORTAL, Particle.ENCHANT, "Mystic");
            case "ocean" -> new KingdomTheme("preset", ChatColor.BLUE, ChatColor.AQUA, ChatColor.WHITE,
                                            Material.BLUE_BANNER, Material.BLUE_BANNER,
                                            Particle.SPLASH, Particle.BUBBLE, "Ocean");
            default -> null;
        };
    }
    
    private void saveTheme(KingdomTheme theme) {
        plugin.getStorageManager().getAdapter().saveKingdomTheme(
            theme.getKingdomName(),
            theme.getPrimaryColor().name(),
            theme.getSecondaryColor().name(),
            theme.getAccentColor().name(),
            theme.getBannerMaterial().name(),
            theme.getFlagMaterial().name(),
            theme.getPrimaryParticle().name(),
            theme.getSecondaryParticle().name(),
            theme.getThemeName()
        );
    }
}

