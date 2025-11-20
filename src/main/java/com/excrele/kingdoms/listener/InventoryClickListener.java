package com.excrele.kingdoms.listener;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;

public class InventoryClickListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals("Kingdom Challenges")) {
            event.setCancelled(true); // Prevent item pickup
            // Future: Add tracking functionality for specific challenges
            return;
        }

        if (title.startsWith("Claim Map - ")) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (slot >= 54) return; // Outside inventory bounds
            
            String kingdomName = title.replace("Claim Map - ", "");
            Kingdom playerKingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
            if (playerKingdom == null) return;
            
            Chunk center = player.getLocation().getChunk();
            int radius = 5;
            int row = slot / 9;
            int col = slot % 9;
            
            if (row >= 6) return; // Bottom rows are for info
            
            int chunkX = center.getX() - radius + col;
            int chunkZ = center.getZ() - radius + row;
            Chunk clickedChunk = center.getWorld().getChunkAt(chunkX, chunkZ);
            
            Kingdom clickedKingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdomByChunk(clickedChunk);
            
            if (clickedKingdom == null) {
                // Unclaimed chunk - try to claim if player has permission
                if (playerKingdom.hasPermission(player.getName(), "claim")) {
                    player.closeInventory();
                    // Teleport player to chunk center for claiming
                    org.bukkit.Location chunkLoc = new org.bukkit.Location(
                        clickedChunk.getWorld(),
                        (clickedChunk.getX() << 4) + 8,
                        player.getLocation().getY(),
                        (clickedChunk.getZ() << 4) + 8
                    );
                    player.teleport(chunkLoc);
                    player.sendMessage("§eTeleported to chunk! Use §6/kingdom claim §eto claim it.");
                    com.excrele.kingdoms.util.ActionBarManager.sendNotification(player, 
                        "§eTeleported! Use §6/kingdom claim §eto claim this chunk.");
                } else {
                    player.sendMessage("§cYou don't have permission to claim chunks!");
                }
            } else {
                // Show chunk info
                player.sendMessage("§6=== Chunk Information ===");
                player.sendMessage("§7Chunk: §e" + clickedChunk.getX() + ", " + clickedChunk.getZ());
                player.sendMessage("§7Kingdom: §e" + clickedKingdom.getName());
                player.sendMessage("§7Level: §e" + clickedKingdom.getLevel());
                if (clickedKingdom.getName().equals(kingdomName)) {
                    player.sendMessage("§7This is your kingdom's territory!");
                } else if (playerKingdom.isAllied(clickedKingdom.getName())) {
                    player.sendMessage("§7This is an allied kingdom's territory!");
                } else {
                    player.sendMessage("§7This is an enemy kingdom's territory!");
                }
            }
            return;
        }

        if (title.startsWith("Kingdom: ")) {
            event.setCancelled(true);
            String kingdomName = title.replace("Kingdom: ", "");
            Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
            if (kingdom == null) return;
            
            int slot = event.getSlot();
            switch (slot) {
                case 10: // Members (Invite)
                    if (kingdom.hasPermission(player.getName(), "invite")) {
                        player.closeInventory();
                        com.excrele.kingdoms.util.ActionBarManager.sendNotification(player, 
                            "§eType: §6/kingdom invite <player>");
                        player.sendMessage("§6Enter player name to invite: §e/kingdom invite <player>");
                    } else {
                        player.sendMessage("§cYou don't have permission to invite members!");
                    }
                    break;
                case 12: // Claims (Unclaim)
                    if (kingdom.hasPermission(player.getName(), "unclaim")) {
                        player.closeInventory();
                        com.excrele.kingdoms.util.ActionBarManager.sendNotification(player, 
                            "§eStand in a chunk and use: §6/kingdom unclaim");
                        player.sendMessage("§6Stand in a chunk and use: §e/kingdom unclaim");
                    } else {
                        player.sendMessage("§cYou don't have permission to unclaim chunks!");
                    }
                    break;
                case 14: // Flags (Set Flag)
                    if (kingdom.hasPermission(player.getName(), "setplotflags")) {
                        player.closeInventory();
                        com.excrele.kingdoms.util.ActionBarManager.sendNotification(player, 
                            "§eUse: §6/kingdom plotflag <flag> <value>");
                        player.sendMessage("§6Set a plot flag: §e/kingdom plotflag <flag> <value>");
                    } else {
                        player.sendMessage("§cYou don't have permission to set flags!");
                    }
                    break;
                case 16: // XP and Level (Show detailed info)
                    int currentLevel = kingdom.getLevel();
                    int required = currentLevel * currentLevel * 1000;
                    int progress = required > 0 ? Math.min(100, (kingdom.getXp() * 100) / required) : 100;
                    String progressBar = com.excrele.kingdoms.util.ActionBarManager.generateProgressBar(progress, 20);
                    player.sendMessage("§6=== Kingdom Status ===");
                    player.sendMessage("§7Level: §e" + kingdom.getLevel());
                    player.sendMessage("§7XP: §e" + kingdom.getXp() + "§7/§e" + required + " §7(" + progress + "%)");
                    player.sendMessage("§7Progress: " + progressBar);
                    player.sendMessage("§7Challenges Completed: §e" + kingdom.getTotalChallengesCompleted());
                    break;
                case 22: // Contributions
                    player.closeInventory();
                    player.performCommand("kingdom contributions");
                    break;
            }
        }
    }
}