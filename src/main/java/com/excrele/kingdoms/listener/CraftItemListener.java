package com.excrele.kingdoms.listener;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Challenge;

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
            }
        }
    }
}