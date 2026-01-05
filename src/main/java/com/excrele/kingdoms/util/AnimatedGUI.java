package com.excrele.kingdoms.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import com.excrele.kingdoms.KingdomsPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating animated GUI elements
 */
public class AnimatedGUI {
    
    /**
     * Create an animated progress bar item
     */
    public static ItemStack createProgressBar(int current, int max, int barLength, 
                                              Material filledMaterial, Material emptyMaterial,
                                              String title) {
        ItemStack item = new ItemStack(filledMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        double percentage = max > 0 ? (double) current / max : 0.0;
        int filled = (int) (barLength * percentage);
        int empty = barLength - filled;
        
        StringBuilder bar = new StringBuilder();
        bar.append("§a");
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        bar.append("§7");
        for (int i = 0; i < empty; i++) {
            bar.append("█");
        }
        
        meta.setDisplayName(title);
        List<String> lore = new ArrayList<>();
        lore.add(bar.toString());
        lore.add("§7Progress: §e" + current + "§7/§e" + max + " §7(" + 
            String.format("%.1f", percentage * 100) + "%)");
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Create animated border for GUI
     */
    public static void createAnimatedBorder(Inventory gui, Material borderMaterial, 
                                           int updateInterval, KingdomsPlugin plugin) {
        new BukkitRunnable() {
            private int frame = 0;
            private final Material[] materials = {
                borderMaterial,
                Material.GLASS_PANE,
                Material.WHITE_STAINED_GLASS_PANE
            };
            
            @Override
            public void run() {
                // Animate border slots
                int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, // Top row
                                    9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53}; // Sides and bottom
                
                Material currentMaterial = materials[frame % materials.length];
                
                for (int slot : borderSlots) {
                    if (slot < gui.getSize()) {
                        ItemStack borderItem = new ItemStack(currentMaterial);
                        ItemMeta meta = borderItem.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName("§r");
                            borderItem.setItemMeta(meta);
                        }
                        gui.setItem(slot, borderItem);
                    }
                }
                
                frame++;
            }
        }.runTaskTimer(plugin, 0L, updateInterval);
    }
    
    /**
     * Add particle effects to GUI item
     */
    public static void addParticleEffectToItem(Player player, int slot, 
                                              org.bukkit.Particle particle, 
                                              int count, KingdomsPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.getOpenInventory().getTopInventory() == null) {
                    cancel();
                    return;
                }
                
                org.bukkit.inventory.InventoryView view = player.getOpenInventory();
                if (view.getTopInventory() != null) {
                    org.bukkit.Location loc = player.getLocation();
                    player.spawnParticle(particle, loc, count, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Every 0.5 seconds
    }
    
    /**
     * Create pulsing item (brightness animation)
     */
    public static ItemStack createPulsingItem(Material material, String name, 
                                             List<String> lore, int pulseSpeed) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Add glow effect
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Animate item in slot (rotate through materials)
     */
    public static void animateItemSlot(Inventory gui, int slot, Material[] materials, 
                                     int updateInterval, KingdomsPlugin plugin) {
        new BukkitRunnable() {
            private int frame = 0;
            
            @Override
            public void run() {
                if (gui.getViewers().isEmpty()) {
                    cancel();
                    return;
                }
                
                Material currentMaterial = materials[frame % materials.length];
                ItemStack item = gui.getItem(slot);
                if (item != null) {
                    item.setType(currentMaterial);
                    gui.setItem(slot, item);
                }
                
                frame++;
            }
        }.runTaskTimer(plugin, 0L, updateInterval);
    }
}

