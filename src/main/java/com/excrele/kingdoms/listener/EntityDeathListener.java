package com.excrele.kingdoms.listener;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Challenge;
import com.excrele.kingdoms.model.PlayerChallengeData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;

public class EntityDeathListener implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) return;
        List<Challenge> challenges = KingdomsPlugin.getInstance().getChallengeManager().getEventToChallenges().get("entity_kill");
        if (challenges == null) return;

        for (Challenge challenge : challenges) {
            String requiredEntity = (String) challenge.getTask().get("entity");
            if (event.getEntity().getType().toString().equalsIgnoreCase(requiredEntity)) {
                KingdomsPlugin.getInstance().getChallengeManager().updateChallengeProgress(player, challenge, 1);
                PlayerChallengeData data = KingdomsPlugin.getInstance().getChallengeManager().getPlayerChallengeData(player, challenge);
                if (data != null && data.getProgress() > 0) {
                    player.sendMessage("Progress: " + data.getProgress() + "/" + challenge.getTask().get("amount") + " for " + challenge.getDescription());
                }
            }
        }
    }
}