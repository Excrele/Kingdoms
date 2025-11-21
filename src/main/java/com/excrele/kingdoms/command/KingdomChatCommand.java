package com.excrele.kingdoms.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.listener.KingdomChatListener;

public class KingdomChatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use kingdom chat!");
            return true;
        }
        KingdomsPlugin plugin = KingdomsPlugin.getInstance();
        if (plugin == null || plugin.getKingdomManager() == null) {
            sender.sendMessage("§cPlugin not initialized!");
            return true;
        }
        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null) {
            player.sendMessage("You must be in a kingdom to use kingdom chat!");
            return true;
        }
        if (args.length == 0) {
            // Toggle chat mode
            boolean currentMode = KingdomChatListener.isKingdomChatMode(player);
            KingdomChatListener.setKingdomChatMode(player, !currentMode);
            if (!currentMode) {
                player.sendMessage("§aKingdom chat enabled! Your messages will now go to kingdom chat.");
                player.sendMessage("§7Type /kc <message> to send a message, or /kingdom chat to toggle off.");
            } else {
                player.sendMessage("§cKingdom chat disabled.");
            }
            return true;
        }
        
        // Send message
        String message = String.join(" ", args);
        KingdomChatListener.setKingdomChatMode(player, true);
        player.chat(message);
        KingdomChatListener.setKingdomChatMode(player, false);
        return true;
    }
}

