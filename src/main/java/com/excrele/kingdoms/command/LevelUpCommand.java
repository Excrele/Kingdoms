package com.excrele.kingdoms.command;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelUpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;
        String kingdomName = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null) {
            player.sendMessage("You are not in a kingdom!");
            return true;
        }
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) {
            player.sendMessage("Kingdom not found!");
            return true;
        }
        if (!kingdom.getKing().equals(player.getName())) {
            player.sendMessage("Only the king can level up the kingdom!");
            return true;
        }
        int currentLevel = kingdom.getLevel();
        int requiredXp = currentLevel * currentLevel * 1000;
        if (kingdom.getXp() < requiredXp) {
            player.sendMessage("Not enough XP! Need " + requiredXp + " XP, have " + kingdom.getXp());
            return true;
        }
        kingdom.setXp(kingdom.getXp() - requiredXp);
        kingdom.setLevel(currentLevel + 1);
        player.sendMessage("Kingdom leveled up to level " + (currentLevel + 1) + "!");
        KingdomsPlugin.getInstance().getKingdomManager().saveKingdoms(
                KingdomsPlugin.getInstance().getKingdomsConfig(),
                KingdomsPlugin.getInstance().getKingdomsFile()
        );
        return true;
    }
}