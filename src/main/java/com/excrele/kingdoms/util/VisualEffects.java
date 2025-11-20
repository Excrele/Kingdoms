package com.excrele.kingdoms.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class VisualEffects {
    
    /**
     * Play level-up celebration effects
     */
    public static void playLevelUpEffects(Player player, int newLevel) {
        Location loc = player.getLocation();
        
        // Firework-like particles
        for (int i = 0; i < 30; i++) {
            double angle = (2 * Math.PI * i) / 30;
            double x = Math.cos(angle) * 2;
            double z = Math.sin(angle) * 2;
            Location particleLoc = loc.clone().add(x, 1, z);
            player.spawnParticle(Particle.FIREWORK, particleLoc, 1, 0, 0, 0, 0.1);
        }
        
        // Heart particles
        for (int i = 0; i < 10; i++) {
            player.spawnParticle(Particle.HEART, loc.clone().add(0, 2, 0), 1, 0.5, 0.5, 0.5, 0.1);
        }
        
        // Sound effects
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }
    
    /**
     * Play challenge completion effects
     */
    public static void playChallengeCompleteEffects(Player player) {
        Location loc = player.getLocation();
        
        // Success particles
        for (int i = 0; i < 20; i++) {
            double angle = (2 * Math.PI * i) / 20;
            double x = Math.cos(angle) * 1.5;
            double z = Math.sin(angle) * 1.5;
            Location particleLoc = loc.clone().add(x, 1, z);
            player.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 1, 0, 0, 0, 0.1);
        }
        
        // Sound
        player.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }
    
    /**
     * Play claim chunk effects
     */
    public static void playClaimEffects(Player player, Location chunkCenter) {
        // Border particles
        int minX = (chunkCenter.getBlockX() >> 4) << 4;
        int minZ = (chunkCenter.getBlockZ() >> 4) << 4;
        int y = chunkCenter.getBlockY();
        
        for (int i = 0; i < 16; i++) {
            player.spawnParticle(Particle.HAPPY_VILLAGER, 
                new Location(chunkCenter.getWorld(), minX + i, y, minZ), 1, 0, 0, 0, 0);
            player.spawnParticle(Particle.HAPPY_VILLAGER, 
                new Location(chunkCenter.getWorld(), minX + i, y, minZ + 15), 1, 0, 0, 0, 0);
            player.spawnParticle(Particle.HAPPY_VILLAGER, 
                new Location(chunkCenter.getWorld(), minX, y, minZ + i), 1, 0, 0, 0, 0);
            player.spawnParticle(Particle.HAPPY_VILLAGER, 
                new Location(chunkCenter.getWorld(), minX + 15, y, minZ + i), 1, 0, 0, 0, 0);
        }
        
        // Sound
        player.playSound(chunkCenter, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
    }
    
    /**
     * Play unclaim chunk effects
     */
    public static void playUnclaimEffects(Player player, Location chunkCenter) {
        int minX = (chunkCenter.getBlockX() >> 4) << 4;
        int minZ = (chunkCenter.getBlockZ() >> 4) << 4;
        int y = chunkCenter.getBlockY();
        
        for (int i = 0; i < 16; i++) {
            player.spawnParticle(Particle.LARGE_SMOKE, 
                new Location(chunkCenter.getWorld(), minX + i, y, minZ), 1, 0, 0.5, 0, 0.1);
        }
        
        player.playSound(chunkCenter, Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
    }
    
    /**
     * Display level-based aura around player
     */
    public static void displayLevelAura(Player player, int level) {
        if (level < 5) return; // Only for level 5+
        
        Location loc = player.getLocation();
        Particle particle = switch (level / 5) {
            case 1 -> Particle.ENCHANT; // Level 5-9
            case 2 -> Particle.END_ROD; // Level 10-14
            case 3 -> Particle.TOTEM_OF_UNDYING; // Level 15-19
            default -> Particle.DRAGON_BREATH; // Level 20+
        };
        
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI * i) / 8;
            double x = Math.cos(angle) * 0.5;
            double z = Math.sin(angle) * 0.5;
            Location particleLoc = loc.clone().add(x, 0.5, z);
            player.spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0.05);
        }
    }
    
    /**
     * Play alliance formed effects
     */
    public static void playAllianceEffects(Player player) {
        Location loc = player.getLocation();
        
        // Celebration particles
        for (int i = 0; i < 25; i++) {
            double angle = (2 * Math.PI * i) / 25;
            double x = Math.cos(angle) * 2;
            double z = Math.sin(angle) * 2;
            Location particleLoc = loc.clone().add(x, 1.5, z);
            player.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 1, 0, 0, 0, 0.1);
        }
        
        player.playSound(loc, Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
    }
}

