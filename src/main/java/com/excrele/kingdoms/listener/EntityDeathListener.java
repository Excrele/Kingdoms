package com.excrele.kingdoms.listener;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Challenge;

public class EntityDeathListener implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) return;
        KingdomsPlugin plugin = KingdomsPlugin.getInstance();
        
        // Record entity interaction for statistics
        if (plugin.getStatisticsManager() != null) {
            org.bukkit.Chunk chunk = event.getEntity().getLocation().getChunk();
            plugin.getStatisticsManager().recordEntityInteraction(chunk);
        }
        
        List<Challenge> challenges = plugin.getChallengeManager().getEventToChallenges().get("entity_kill");
        if (challenges == null) return;

        for (Challenge challenge : challenges) {
            String requiredEntity = (String) challenge.getTask().get("entity");
            if (event.getEntity().getType().toString().equalsIgnoreCase(requiredEntity)) {
                plugin.getChallengeManager().updateChallengeProgress(player, challenge, 1);
            }
        }
    }
}