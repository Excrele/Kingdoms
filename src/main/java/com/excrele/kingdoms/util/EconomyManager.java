package com.excrele.kingdoms.util;

import com.excrele.kingdoms.KingdomsPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private static Economy economy = null;
    private static boolean enabled = false;

    public static boolean setupEconomy() {
        if (KingdomsPlugin.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = KingdomsPlugin.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        enabled = economy != null;
        return enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static Economy getEconomy() {
        return economy;
    }

    /**
     * Check if player has enough money
     */
    public static boolean hasEnough(Player player, double amount) {
        if (!enabled) return true; // If economy disabled, allow
        return economy.has(player, amount);
    }

    /**
     * Withdraw money from player
     */
    public static boolean withdraw(Player player, double amount) {
        if (!enabled) return true; // If economy disabled, allow
        if (!hasEnough(player, amount)) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    /**
     * Deposit money to player
     */
    public static boolean deposit(Player player, double amount) {
        if (!enabled) return true; // If economy disabled, allow
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    /**
     * Get player's balance
     */
    public static double getBalance(Player player) {
        if (!enabled) return 0;
        return economy.getBalance(player);
    }

    /**
     * Format money amount
     */
    public static String format(double amount) {
        if (!enabled) return String.valueOf(amount);
        return economy.format(amount);
    }
}

