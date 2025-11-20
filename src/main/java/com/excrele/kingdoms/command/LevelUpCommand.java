package com.excrele.kingdoms.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;

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
        if (!kingdom.hasPermission(player.getName(), "levelup")) {
            player.sendMessage("Only the king can level up the kingdom!");
            return true;
        }
        int currentLevel = kingdom.getLevel();
        int requiredXp = currentLevel * currentLevel * 1000;
        if (kingdom.getXp() < requiredXp) {
            player.sendMessage("Not enough XP! Need " + requiredXp + " XP, have " + kingdom.getXp());
            return true;
        }
        
        // Check economy cost
        double levelupCost = KingdomsPlugin.getInstance().getConfig().getDouble("economy.levelup_cost", 0.0);
        boolean economyEnabled = KingdomsPlugin.getInstance().getConfig().getBoolean("economy.enabled", false);
        if (economyEnabled && levelupCost > 0 && com.excrele.kingdoms.util.EconomyManager.isEnabled()) {
            if (!com.excrele.kingdoms.util.EconomyManager.hasEnough(player, levelupCost)) {
                player.sendMessage("§cYou don't have enough money! Cost: " + 
                    com.excrele.kingdoms.util.EconomyManager.format(levelupCost));
                return true;
            }
        }
        // Charge economy cost
        if (economyEnabled && levelupCost > 0 && com.excrele.kingdoms.util.EconomyManager.isEnabled()) {
            com.excrele.kingdoms.util.EconomyManager.withdraw(player, levelupCost);
            player.sendMessage("§7Paid §e" + com.excrele.kingdoms.util.EconomyManager.format(levelupCost) + " §7to level up.");
        }
        
        kingdom.setXp(kingdom.getXp() - requiredXp);
        kingdom.setLevel(currentLevel + 1);
        int newLevel = currentLevel + 1;
        
        // Visual effects
        com.excrele.kingdoms.util.VisualEffects.playLevelUpEffects(player, newLevel);
        
        // Action bar notification
        com.excrele.kingdoms.util.ActionBarManager.sendLevelUpNotification(player, newLevel);
        
        // Title announcement
        player.sendTitle("§6§l⚡ LEVEL UP! ⚡", "§eYour kingdom reached level " + newLevel + "!", 10, 70, 20);
        
        // Message to all kingdom members
        for (String memberName : kingdom.getMembers()) {
            org.bukkit.entity.Player member = player.getServer().getPlayer(memberName);
            if (member != null && member.isOnline()) {
                member.sendMessage("§6§l[Kingdom] §e" + player.getName() + " leveled up the kingdom to level " + newLevel + "!");
                com.excrele.kingdoms.util.ActionBarManager.sendLevelUpNotification(member, newLevel);
            }
        }
        
        player.sendMessage("§6Kingdom leveled up to level " + newLevel + "!");
        KingdomsPlugin.getInstance().getKingdomManager().saveKingdoms(
                KingdomsPlugin.getInstance().getKingdomsConfig(),
                KingdomsPlugin.getInstance().getKingdomsFile()
        );
        return true;
    }
}