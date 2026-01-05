package com.excrele.kingdoms.model;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;

/**
 * Represents a kingdom's color theme and visual identity
 */
public class KingdomTheme {
    private String kingdomName;
    private ChatColor primaryColor;
    private ChatColor secondaryColor;
    private ChatColor accentColor;
    private Material bannerMaterial;
    private Material flagMaterial;
    private Particle primaryParticle;
    private Particle secondaryParticle;
    private String themeName;
    
    public KingdomTheme(String kingdomName) {
        this.kingdomName = kingdomName;
        this.primaryColor = ChatColor.GOLD; // Default
        this.secondaryColor = ChatColor.YELLOW;
        this.accentColor = ChatColor.WHITE;
        this.bannerMaterial = Material.WHITE_BANNER;
        this.flagMaterial = Material.WHITE_BANNER;
        this.primaryParticle = Particle.FLAME;
        this.secondaryParticle = Particle.ENCHANT;
        this.themeName = "Default";
    }
    
    public KingdomTheme(String kingdomName, ChatColor primaryColor, ChatColor secondaryColor,
                       ChatColor accentColor, Material bannerMaterial, Material flagMaterial,
                       Particle primaryParticle, Particle secondaryParticle, String themeName) {
        this.kingdomName = kingdomName;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.accentColor = accentColor;
        this.bannerMaterial = bannerMaterial;
        this.flagMaterial = flagMaterial;
        this.primaryParticle = primaryParticle;
        this.secondaryParticle = secondaryParticle;
        this.themeName = themeName;
    }
    
    public String getKingdomName() { return kingdomName; }
    public ChatColor getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(ChatColor color) { this.primaryColor = color; }
    public ChatColor getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(ChatColor color) { this.secondaryColor = color; }
    public ChatColor getAccentColor() { return accentColor; }
    public void setAccentColor(ChatColor color) { this.accentColor = color; }
    public Material getBannerMaterial() { return bannerMaterial; }
    public void setBannerMaterial(Material material) { this.bannerMaterial = material; }
    public Material getFlagMaterial() { return flagMaterial; }
    public void setFlagMaterial(Material material) { this.flagMaterial = material; }
    public Particle getPrimaryParticle() { return primaryParticle; }
    public void setPrimaryParticle(Particle particle) { this.primaryParticle = particle; }
    public Particle getSecondaryParticle() { return secondaryParticle; }
    public void setSecondaryParticle(Particle particle) { this.secondaryParticle = particle; }
    public String getThemeName() { return themeName; }
    public void setThemeName(String name) { this.themeName = name; }
    
    /**
     * Format text with kingdom colors
     */
    public String formatText(String text) {
        return primaryColor + text;
    }
    
    /**
     * Format text with secondary color
     */
    public String formatSecondary(String text) {
        return secondaryColor + text;
    }
    
    /**
     * Get color code string
     */
    public String getPrimaryColorCode() {
        return primaryColor.toString();
    }
    
    public String getSecondaryColorCode() {
        return secondaryColor.toString();
    }
}

