package com.excrele.kingdoms.gui;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.PlayerActivity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI for displaying comprehensive kingdom statistics and analytics
 */
public class StatisticsDashboardGUI {
    
    public static void openDashboard(Player player) {
        String kingdomName = KingdomsPlugin.getInstance().getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null) {
            player.sendMessage("§cYou must be in a kingdom to view statistics!");
            return;
        }
        
        Kingdom kingdom = KingdomsPlugin.getInstance().getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) return;
        
        KingdomsPlugin plugin = KingdomsPlugin.getInstance();
        Inventory gui = Bukkit.createInventory(null, 54, "Statistics Dashboard - " + kingdomName);
        
        // Overview Section (Top row)
        gui.setItem(0, createOverviewItem(kingdom, plugin));
        gui.setItem(1, createHealthScoreItem(kingdomName, plugin));
        gui.setItem(2, createGrowthItem(kingdomName, plugin));
        
        // Member Analytics (Row 2)
        gui.setItem(9, createMemberStatsItem(kingdom, plugin));
        gui.setItem(10, createActivityStatsItem(kingdom, plugin));
        gui.setItem(11, createContributionStatsItem(kingdom, plugin));
        
        // Claim Analytics (Row 3)
        gui.setItem(18, createClaimStatsItem(kingdom, plugin));
        gui.setItem(19, createClaimActivityItem(kingdom, plugin));
        gui.setItem(20, createTerritoryValueItem(kingdom, plugin));
        
        // Financial Analytics (Row 4)
        gui.setItem(27, createFinancialItem(kingdomName, plugin));
        gui.setItem(28, createBankActivityItem(kingdomName, plugin));
        
        // Historical Data (Row 5)
        gui.setItem(36, createHistoryItem(kingdomName, plugin));
        gui.setItem(37, createTimelineItem(kingdomName, plugin));
        
        // Growth Charts (Row 6)
        gui.setItem(45, createGrowthChartItem(kingdomName, plugin));
        gui.setItem(46, createTrendItem(kingdomName, plugin));
        
        // Navigation
        gui.setItem(53, createNavigationItem());
        
        player.openInventory(gui);
    }
    
    private static ItemStack createOverviewItem(Kingdom kingdom, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lKingdom Overview");
            List<String> lore = new ArrayList<>();
            lore.add("§7Level: §e" + kingdom.getLevel());
            lore.add("§7XP: §e" + formatNumber(kingdom.getXp()));
            lore.add("§7Members: §e" + (kingdom.getMembers().size() + 1));
            lore.add("§7Claims: §e" + kingdom.getCurrentClaimChunks() + "§7/§e" + kingdom.getMaxClaimChunks());
            lore.add("§7Alliances: §e" + kingdom.getAlliances().size());
            lore.add("§7Challenges: §e" + kingdom.getTotalChallengesCompleted());
            long age = (System.currentTimeMillis() / 1000 - kingdom.getCreatedAt()) / 86400;
            lore.add("§7Age: §e" + age + " days");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createHealthScoreItem(String kingdomName, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            double healthScore = plugin.getStatisticsManager().calculateKingdomHealthScore(kingdomName);
            meta.setDisplayName("§6§lKingdom Health Score");
            List<String> lore = new ArrayList<>();
            lore.add("§7Score: §e" + String.format("%.1f", healthScore) + "/100");
            lore.add("");
            lore.add("§7Member Activity: §e" + String.format("%.1f", 
                plugin.getStatisticsManager().calculateMemberActivityScore(kingdomName)) + "%");
            lore.add("§7Claim Activity: §e" + String.format("%.1f", 
                plugin.getStatisticsManager().calculateClaimActivityScore(kingdomName)) + "%");
            lore.add("§7Growth Rate: §e" + String.format("%.1f", 
                plugin.getStatisticsManager().calculateGrowthScore(kingdomName)) + "%");
            lore.add("§7Financial: §e" + String.format("%.1f", 
                plugin.getStatisticsManager().calculateFinancialScore(kingdomName)) + "%");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createGrowthItem(String kingdomName, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<com.excrele.kingdoms.manager.StatisticsManager.GrowthData> growthData = 
                plugin.getStatisticsManager().getGrowthData(kingdomName);
            meta.setDisplayName("§6§lGrowth Analytics");
            List<String> lore = new ArrayList<>();
            if (growthData != null && !growthData.isEmpty()) {
                com.excrele.kingdoms.manager.StatisticsManager.GrowthData latest = growthData.get(growthData.size() - 1);
                if (growthData.size() > 1) {
                    com.excrele.kingdoms.manager.StatisticsManager.GrowthData previous = growthData.get(growthData.size() - 2);
                    int levelChange = latest.level - previous.level;
                    int claimChange = latest.claims - previous.claims;
                    int memberChange = latest.members - previous.members;
                    lore.add("§7Level Change: §e" + (levelChange >= 0 ? "+" : "") + levelChange);
                    lore.add("§7Claim Change: §e" + (claimChange >= 0 ? "+" : "") + claimChange);
                    lore.add("§7Member Change: §e" + (memberChange >= 0 ? "+" : "") + memberChange);
                }
                lore.add("");
                lore.add("§7Current Level: §e" + latest.level);
                lore.add("§7Current Claims: §e" + latest.claims);
                lore.add("§7Current Members: §e" + latest.members);
            } else {
                lore.add("§7No growth data available");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createMemberStatsItem(Kingdom kingdom, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lMember Statistics");
            List<String> lore = new ArrayList<>();
            lore.add("§7Total Members: §e" + (kingdom.getMembers().size() + 1));
            lore.add("§7Active (7 days): §e" + getActiveMemberCount(kingdom, plugin));
            lore.add("§7Inactive: §e" + getInactiveMemberCount(kingdom, plugin));
            lore.add("");
            lore.add("§7Top Contributors:");
            List<Map.Entry<String, Integer>> topContributors = kingdom.getMemberContributions().entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .toList();
            int rank = 1;
            for (Map.Entry<String, Integer> entry : topContributors) {
                lore.add("§7" + rank + ". §e" + entry.getKey() + " §7- §e" + entry.getValue() + " XP");
                rank++;
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createActivityStatsItem(Kingdom kingdom, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lActivity Statistics");
            List<String> lore = new ArrayList<>();
            long totalPlaytime = 0;
            int totalContributions = 0;
            int totalStreaks = 0;
            for (String member : kingdom.getMembers()) {
                PlayerActivity activity = plugin.getActivityManager().getActivity(member);
                if (activity != null) {
                    totalPlaytime += activity.getTotalPlaytime();
                    totalContributions += activity.getContributions();
                    totalStreaks += activity.getContributionStreak();
                }
            }
            PlayerActivity kingActivity = plugin.getActivityManager().getActivity(kingdom.getKing());
            if (kingActivity != null) {
                totalPlaytime += kingActivity.getTotalPlaytime();
                totalContributions += kingActivity.getContributions();
                totalStreaks += kingActivity.getContributionStreak();
            }
            lore.add("§7Total Playtime: §e" + (totalPlaytime / 3600) + " hours");
            lore.add("§7Total Contributions: §e" + totalContributions);
            lore.add("§7Avg Streak: §e" + (kingdom.getMembers().size() + 1 > 0 ? 
                totalStreaks / (kingdom.getMembers().size() + 1) : 0) + " days");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createContributionStatsItem(Kingdom kingdom, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lContribution Statistics");
            List<String> lore = new ArrayList<>();
            int totalContrib = kingdom.getMemberContributions().values().stream().mapToInt(Integer::intValue).sum();
            lore.add("§7Total Contributions: §e" + formatNumber(totalContrib) + " XP");
            lore.add("§7Average per Member: §e" + (kingdom.getMembers().size() + 1 > 0 ? 
                totalContrib / (kingdom.getMembers().size() + 1) : 0) + " XP");
            lore.add("");
            Player player = plugin.getServer().getPlayer(kingdom.getKing());
            String playerName = player != null ? player.getName() : "";
            lore.add("§7Your Contribution: §e" + kingdom.getContribution(playerName) + " XP");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createClaimStatsItem(Kingdom kingdom, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lClaim Statistics");
            List<String> lore = new ArrayList<>();
            lore.add("§7Total Claims: §e" + kingdom.getCurrentClaimChunks());
            lore.add("§7Max Claims: §e" + kingdom.getMaxClaimChunks());
            lore.add("§7Available: §e" + (kingdom.getMaxClaimChunks() - kingdom.getCurrentClaimChunks()));
            int totalVisits = 0;
            for (List<org.bukkit.Chunk> claimGroup : kingdom.getClaims()) {
                for (org.bukkit.Chunk chunk : claimGroup) {
                    com.excrele.kingdoms.model.ClaimAnalytics analytics = 
                        plugin.getStatisticsManager().getClaimAnalytics(chunk);
                    if (analytics != null) {
                        totalVisits += analytics.getPlayerVisits();
                    }
                }
            }
            lore.add("§7Total Visits: §e" + totalVisits);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createClaimActivityItem(Kingdom kingdom, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lClaim Activity");
            List<String> lore = new ArrayList<>();
            int totalBlockInteractions = 0;
            int totalEntityInteractions = 0;
            for (List<org.bukkit.Chunk> claimGroup : kingdom.getClaims()) {
                for (org.bukkit.Chunk chunk : claimGroup) {
                    com.excrele.kingdoms.model.ClaimAnalytics analytics = 
                        plugin.getStatisticsManager().getClaimAnalytics(chunk);
                    if (analytics != null) {
                        totalBlockInteractions += analytics.getBlockInteractions();
                        totalEntityInteractions += analytics.getEntityInteractions();
                    }
                }
            }
            lore.add("§7Block Interactions: §e" + formatNumber(totalBlockInteractions));
            lore.add("§7Entity Interactions: §e" + formatNumber(totalEntityInteractions));
            lore.add("§7Total Activity: §e" + formatNumber(totalBlockInteractions + totalEntityInteractions));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createTerritoryValueItem(Kingdom kingdom, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lTerritory Value");
            List<String> lore = new ArrayList<>();
            double totalValue = 0;
            for (List<org.bukkit.Chunk> claimGroup : kingdom.getClaims()) {
                for (org.bukkit.Chunk chunk : claimGroup) {
                    com.excrele.kingdoms.model.ClaimAnalytics analytics = 
                        plugin.getStatisticsManager().getClaimAnalytics(chunk);
                    if (analytics != null) {
                        totalValue += analytics.getEstimatedValue();
                    }
                }
            }
            lore.add("§7Estimated Value: §e" + String.format("%.2f", totalValue));
            lore.add("§7Average per Claim: §e" + (kingdom.getCurrentClaimChunks() > 0 ? 
                String.format("%.2f", totalValue / kingdom.getCurrentClaimChunks()) : "0.00"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createFinancialItem(String kingdomName, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            double balance = plugin.getBankManager().getBalance(kingdomName);
            meta.setDisplayName("§6§lFinancial Statistics");
            List<String> lore = new ArrayList<>();
            lore.add("§7Bank Balance: §e" + (plugin.getServer().getPluginManager().getPlugin("Vault") != null ? 
                com.excrele.kingdoms.util.EconomyManager.format(balance) : String.format("%.2f", balance)));
            lore.add("§7Financial Score: §e" + String.format("%.1f", 
                plugin.getStatisticsManager().calculateFinancialScore(kingdomName)) + "/100");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createBankActivityItem(String kingdomName, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lBank Activity");
            List<String> lore = new ArrayList<>();
            lore.add("§7Vault Access: §e" + (plugin.getVaultManager().getVault(
                plugin.getKingdomManager().getKingdom(kingdomName)) != null ? "Available" : "Not available"));
            lore.add("§aClick to open vault");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createHistoryItem(String kingdomName, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            com.excrele.kingdoms.model.KingdomHistory history = 
                plugin.getStatisticsManager().getKingdomHistory(kingdomName);
            meta.setDisplayName("§6§lKingdom History");
            List<String> lore = new ArrayList<>();
            lore.add("§7Total Events: §e" + (history != null ? history.getEntries().size() : 0));
            if (history != null && !history.getEntries().isEmpty()) {
                lore.add("");
                lore.add("§7Recent Events:");
                history.getEntries().stream()
                    .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                    .limit(3)
                    .forEach(entry -> {
                        long daysAgo = (System.currentTimeMillis() / 1000 - entry.getTimestamp()) / 86400;
                        lore.add("§7- §e" + entry.getType().name() + " §7(" + daysAgo + "d ago)");
                    });
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createTimelineItem(String kingdomName, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
            meta.setDisplayName("§6§lTimeline");
            List<String> lore = new ArrayList<>();
            if (kingdom != null) {
                long age = (System.currentTimeMillis() / 1000 - kingdom.getCreatedAt()) / 86400;
                lore.add("§7Kingdom Age: §e" + age + " days");
                lore.add("§7Created: §e" + formatTime(kingdom.getCreatedAt()));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createGrowthChartItem(String kingdomName, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.MAP);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<com.excrele.kingdoms.manager.StatisticsManager.GrowthData> growthData = 
                plugin.getStatisticsManager().getGrowthData(kingdomName);
            meta.setDisplayName("§6§lGrowth Chart");
            List<String> lore = new ArrayList<>();
            if (growthData != null && !growthData.isEmpty()) {
                lore.add("§7Data Points: §e" + growthData.size());
                lore.add("§7Time Range: §e" + ((growthData.get(growthData.size() - 1).timestamp - 
                    growthData.get(0).timestamp) / 86400) + " days");
                lore.add("");
                lore.add("§aClick to view detailed chart");
            } else {
                lore.add("§7No growth data available");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createTrendItem(String kingdomName, KingdomsPlugin plugin) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lTrend Analysis");
            List<String> lore = new ArrayList<>();
            double growthScore = plugin.getStatisticsManager().calculateGrowthScore(kingdomName);
            String trend = growthScore > 60 ? "§aRising" : growthScore > 40 ? "§eStable" : "§cDeclining";
            lore.add("§7Trend: " + trend);
            lore.add("§7Growth Score: §e" + String.format("%.1f", growthScore) + "/100");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createNavigationItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lNavigation");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to go back");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    // Helper methods
    private static int getActiveMemberCount(Kingdom kingdom, KingdomsPlugin plugin) {
        int count = 0;
        for (String member : kingdom.getMembers()) {
            PlayerActivity activity = plugin.getActivityManager().getActivity(member);
            if (activity != null && activity.getDaysSinceLastLogin() <= 7) {
                count++;
            }
        }
        PlayerActivity kingActivity = plugin.getActivityManager().getActivity(kingdom.getKing());
        if (kingActivity != null && kingActivity.getDaysSinceLastLogin() <= 7) {
            count++;
        }
        return count;
    }
    
    private static int getInactiveMemberCount(Kingdom kingdom, KingdomsPlugin plugin) {
        return (kingdom.getMembers().size() + 1) - getActiveMemberCount(kingdom, plugin);
    }
    
    private static String formatNumber(int number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }
    
    private static String formatTime(long timestamp) {
        long daysAgo = (System.currentTimeMillis() / 1000 - timestamp) / 86400;
        if (daysAgo == 0) return "Today";
        if (daysAgo == 1) return "Yesterday";
        return daysAgo + " days ago";
    }
}

