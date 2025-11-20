package com.excrele.kingdoms.listener;

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

        if (title.startsWith("Kingdom: ")) {
            event.setCancelled(true);
            String kingdomName = title.replace("Kingdom: ", "");
            Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
            if (kingdom == null || !kingdom.getKing().equals(player.getName())) {
                player.sendMessage("Only the king can perform actions in this GUI!");
                return;
            }

            int slot = event.getSlot();
            switch (slot) {
                case 10: // Members (Invite)
                    player.closeInventory();
                    player.sendMessage("Enter player name to invite: /kingdom invite <player>");
                    break;
                case 12: // Claims (Unclaim)
                    player.closeInventory();
                    player.sendMessage("Stand in a chunk and use /kingdom unclaim");
                    break;
                case 14: // Flags (Set Flag)
                    player.closeInventory();
                    player.sendMessage("Set a flag: /kingdom flag <flag> <value>");
                    break;
                case 16: // XP and Level (No action)
                    player.sendMessage("Kingdom Level: " + kingdom.getLevel() + ", XP: " + kingdom.getXp());
                    break;
                case 22: // Contributions
                    player.closeInventory();
                    player.performCommand("kingdom contributions");
                    break;
            }
        }
    }
}