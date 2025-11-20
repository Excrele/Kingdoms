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

public class ClaimMapGUI {
    public static void openClaimMapGUI(Player player) {
        String kingdomName = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null) {
            player.sendMessage("§cYou must be in a kingdom to view the claim map!");
            return;
        }
        
        Kingdom playerKingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
        if (playerKingdom == null) return;
        
        Chunk center = player.getLocation().getChunk();
        int radius = 5; // 11x11 grid
        
        Inventory gui = Bukkit.createInventory(null, 54, "Claim Map - " + kingdomName);
        
        int slot = 0;
        for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
            for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
                if (slot >= 54) break;
                
                Chunk chunk = center.getWorld().getChunkAt(x, z);
                Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(chunk);
                
                ItemStack item = createMapItem(chunk, kingdom, kingdomName, playerKingdom, player, x == center.getX() && z == center.getZ());
                gui.setItem(slot, item);
                slot++;
            }
        }
        
        // Add info item at bottom
        ItemStack infoItem = new ItemStack(Material.COMPASS);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Map Legend");
            List<String> lore = new ArrayList<>();
            lore.add("§a[K] §7Your Kingdom");
            lore.add("§b[A] §7Allied Kingdom");
            lore.add("§c[E] §7Enemy Kingdom");
            lore.add("§7[-] §7Unclaimed");
            lore.add("§b[P] §7Your Position");
            lore.add("");
            lore.add("§7Click a chunk to view details");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        gui.setItem(49, infoItem);
        
        player.openInventory(gui);
    }
    
    private static ItemStack createMapItem(Chunk chunk, Kingdom kingdom, String playerKingdomName, Kingdom playerKingdom, Player player, boolean isPlayerPosition) {
        Material material;
        String displayName;
        List<String> lore = new ArrayList<>();
        
        if (isPlayerPosition) {
            material = Material.BEACON;
            displayName = "§b[P] §7Your Position";
            lore.add("§7Chunk: §e" + chunk.getX() + ", " + chunk.getZ());
            lore.add("§7World: §e" + chunk.getWorld().getName());
        } else if (kingdom != null) {
            if (kingdom.getName().equals(playerKingdomName)) {
                material = Material.GRASS_BLOCK;
                displayName = "§a[K] §7Your Kingdom";
                lore.add("§7Kingdom: §e" + kingdom.getName());
                lore.add("§7Level: §e" + kingdom.getLevel());
                lore.add("§7Chunk: §e" + chunk.getX() + ", " + chunk.getZ());
            } else if (playerKingdom != null && playerKingdom.isAllied(kingdom.getName())) {
                material = Material.LAPIS_BLOCK;
                displayName = "§b[A] §7Allied Kingdom";
                lore.add("§7Kingdom: §e" + kingdom.getName());
                lore.add("§7Level: §e" + kingdom.getLevel());
                lore.add("§7Chunk: §e" + chunk.getX() + ", " + chunk.getZ());
            } else {
                material = Material.REDSTONE_BLOCK;
                displayName = "§c[E] §7Enemy Kingdom";
                lore.add("§7Kingdom: §e" + kingdom.getName());
                lore.add("§7Level: §e" + kingdom.getLevel());
                lore.add("§7Chunk: §e" + chunk.getX() + ", " + chunk.getZ());
            }
        } else {
            material = Material.GRAY_CONCRETE;
            displayName = "§7[-] §7Unclaimed";
            lore.add("§7Chunk: §e" + chunk.getX() + ", " + chunk.getZ());
            lore.add("§7World: §e" + chunk.getWorld().getName());
            if (playerKingdom.hasPermission(player.getName(), "claim")) {
                lore.add("");
                lore.add("§aClick to claim this chunk!");
            }
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
}

