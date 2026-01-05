package com.excrele.kingdoms.gui;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI for displaying territory heat map based on activity
 */
public class TerritoryHeatMapGUI {
    
    public static void openHeatMap(Player player) {
        String kingdomName = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null) {
            player.sendMessage("§cYou must be in a kingdom to view the heat map!");
            return;
        }
        
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        
        KingdomsPlugin plugin = KingdomsPlugin.getInstance();
        Map<Chunk, Double> heatmap = plugin.getStatisticsManager().generateActivityHeatmap(kingdomName);
        
        if (heatmap.isEmpty()) {
            player.sendMessage("§cNo claim data available for heat map!");
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 54, "Territory Heat Map - " + kingdomName);
        
        // Find min and max activity scores for normalization
        double minActivity = heatmap.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double maxActivity = heatmap.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        double range = maxActivity - minActivity;
        if (range == 0) range = 1.0; // Avoid division by zero
        
        // Create heat map items
        int slot = 0;
        List<Map.Entry<Chunk, Double>> sortedChunks = heatmap.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(45) // Fit in GUI
            .toList();
        
        for (Map.Entry<Chunk, Double> entry : sortedChunks) {
            if (slot >= 45) break;
            
            Chunk chunk = entry.getKey();
            double activity = entry.getValue();
            double normalized = (activity - minActivity) / range; // 0.0 to 1.0
            
            ItemStack item = createHeatMapItem(chunk, activity, normalized, plugin);
            gui.setItem(slot, item);
            slot++;
        }
        
        // Legend
        gui.setItem(45, createLegendItem("§cHigh Activity", Material.RED_CONCRETE));
        gui.setItem(46, createLegendItem("§eMedium Activity", Material.YELLOW_CONCRETE));
        gui.setItem(47, createLegendItem("§aLow Activity", Material.GREEN_CONCRETE));
        gui.setItem(48, createLegendItem("§7No Activity", Material.GRAY_CONCRETE));
        
        // Info
        gui.setItem(49, createInfoItem(heatmap.size(), minActivity, maxActivity));
        
        // Navigation
        gui.setItem(53, createNavigationItem());
        
        player.openInventory(gui);
    }
    
    private static ItemStack createHeatMapItem(Chunk chunk, double activity, double normalized, KingdomsPlugin plugin) {
        Material material;
        String color;
        
        // Determine color based on normalized activity (0.0 to 1.0)
        if (normalized >= 0.75) {
            material = Material.RED_CONCRETE;
            color = "§c";
        } else if (normalized >= 0.5) {
            material = Material.ORANGE_CONCRETE;
            color = "§6";
        } else if (normalized >= 0.25) {
            material = Material.YELLOW_CONCRETE;
            color = "§e";
        } else if (normalized > 0.0) {
            material = Material.LIME_CONCRETE;
            color = "§a";
        } else {
            material = Material.GRAY_CONCRETE;
            color = "§7";
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + "Chunk " + chunk.getX() + ", " + chunk.getZ());
            List<String> lore = new ArrayList<>();
            lore.add("§7World: §e" + chunk.getWorld().getName());
            lore.add("§7Activity Score: §e" + String.format("%.2f", activity));
            lore.add("§7Intensity: §e" + String.format("%.0f", normalized * 100) + "%");
            
            // Get detailed analytics
            com.excrele.kingdoms.model.ClaimAnalytics analytics = 
                plugin.getStatisticsManager().getClaimAnalytics(chunk);
            if (analytics != null) {
                lore.add("");
                lore.add("§7Visits: §e" + analytics.getPlayerVisits());
                lore.add("§7Block Interactions: §e" + analytics.getBlockInteractions());
                lore.add("§7Entity Interactions: §e" + analytics.getEntityInteractions());
                lore.add("§7Estimated Value: §e" + String.format("%.2f", analytics.getEstimatedValue()));
                
                long daysSinceActivity = (System.currentTimeMillis() / 1000 - analytics.getLastActivity()) / 86400;
                lore.add("§7Last Activity: §e" + (daysSinceActivity == 0 ? "Today" : daysSinceActivity + " days ago"));
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createLegendItem(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("§7Activity intensity");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createInfoItem(int totalChunks, double minActivity, double maxActivity) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lHeat Map Info");
            List<String> lore = new ArrayList<>();
            lore.add("§7Total Claims: §e" + totalChunks);
            lore.add("§7Min Activity: §e" + String.format("%.2f", minActivity));
            lore.add("§7Max Activity: §e" + String.format("%.2f", maxActivity));
            lore.add("");
            lore.add("§7Colors represent activity:");
            lore.add("§cRed = High activity");
            lore.add("§6Orange = Medium-high");
            lore.add("§eYellow = Medium");
            lore.add("§aLime = Low");
            lore.add("§7Gray = No activity");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createNavigationItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lBack");
            List<String> lore = new ArrayList<>();
            lore.add("§7Return to dashboard");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}

