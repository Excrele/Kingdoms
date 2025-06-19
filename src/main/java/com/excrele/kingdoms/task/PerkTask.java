package com.excrele.kingdoms.task;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PerkTask extends BukkitRunnable {
    @Override
    public void run() {
        for (Player player : KingdomsPlugin.getInstance().getServer().getOnlinePlayers()) {
            String kingdomName = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
            if (kingdomName == null) continue;
            Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
            if (kingdom == null) continue;

            int level = kingdom.getLevel();
            if (level <= 0) continue;

            // Speed Perk
            int speedAmplifier = (int) (level * 0.05 * 20); // 5% speed per level
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, speedAmplifier, false, false));

            // Regen Perk (only in non-PvP areas)
            String pvpFlag = kingdom.getFlags().getOrDefault("pvp", "false");
            if (!pvpFlag.equalsIgnoreCase("true")) {
                double healthToAdd = level * 0.5; // 0.5 hearts per level
                double maxHealth = player.getHealthScale();
                double newHealth = Math.min(player.getHealth() + healthToAdd, maxHealth);
                player.setHealth(newHealth);
            }
        }
    }
}