package com.excrele.kingdoms.listener;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Challenge;
import com.excrele.kingdoms.model.PlayerChallengeData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

import java.util.List;

public class CraftItemListener implements Listener {
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        List<Challenge> challenges = KingdomsPlugin.getInstance().getChallengeManager().getEventToChallenges().get("craft_item");
        if (challenges == null) return;

        for (Challenge challenge : challenges) {
            String requiredItem = (String) challenge.getTask().get("item");
            if (event.getRecipe().getResult().getType().toString().equalsIgnoreCase(requiredItem)) {
                KingdomsPlugin.getInstance().getChallengeManager().updateChallengeProgress(player, challenge, event.getRecipe().getResult().getAmount());
                PlayerChallengeData data = KingdomsPlugin.getInstance().getChallengeManager().getPlayerChallengeData(player, challenge);
                if (data != null && data.getProgress() > 0) {
                    player.sendMessage("Progress: " + data.getProgress() + "/" + challenge.getTask().get("amount") + " for " + challenge.getDescription());
                }
            }
        }
    }
}