package com.excrele.kingdoms.expansion;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class KingdomsExpansion extends PlaceholderExpansion {
    private final KingdomsPlugin plugin;

    public KingdomsExpansion(KingdomsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "kingdoms";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(player.getName());
        if (kingdomName == null) {
            return switch (identifier.toLowerCase()) {
                case "has_kingdom", "in_kingdom" -> "false";
                default -> "";
            };
        }

        Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
        if (kingdom == null) {
            return "";
        }

        return switch (identifier.toLowerCase()) {
            // Player kingdom info
            case "kingdom" -> kingdomName;
            case "kingdom_name" -> kingdomName;
            case "has_kingdom", "in_kingdom" -> "true";
            
            // Player role
            case "role" -> kingdom.getRole(player.getName()).getDisplayName();
            case "role_name" -> kingdom.getRole(player.getName()).name();
            
            // Kingdom level and XP
            case "level" -> String.valueOf(kingdom.getLevel());
            case "xp" -> String.valueOf(kingdom.getXp());
            case "xp_formatted" -> formatNumber(kingdom.getXp());
            case "xp_required" -> {
                int currentLevel = kingdom.getLevel();
                int required = currentLevel * currentLevel * 1000;
                yield String.valueOf(required);
            }
            case "xp_progress" -> {
                int currentLevel = kingdom.getLevel();
                int required = currentLevel * currentLevel * 1000;
                int current = kingdom.getXp();
                yield String.valueOf(Math.min(100, (current * 100) / required));
            }
            case "xp_progress_bar" -> {
                int currentLevel = kingdom.getLevel();
                int required = currentLevel * currentLevel * 1000;
                int current = kingdom.getXp();
                int progress = Math.min(100, (current * 100) / required);
                yield generateProgressBar(progress, 20);
            }
            
            // Kingdom members
            case "members" -> String.valueOf(kingdom.getMembers().size() + 1); // +1 for king
            case "members_count" -> String.valueOf(kingdom.getMembers().size() + 1);
            case "king" -> kingdom.getKing();
            case "is_king" -> kingdom.getKing().equals(player.getName()) ? "true" : "false";
            
            // Claims
            case "claims" -> String.valueOf(kingdom.getCurrentClaimChunks());
            case "claims_current" -> String.valueOf(kingdom.getCurrentClaimChunks());
            case "claims_max" -> String.valueOf(kingdom.getMaxClaimChunks());
            case "claims_remaining" -> String.valueOf(kingdom.getMaxClaimChunks() - kingdom.getCurrentClaimChunks());
            
            // Contributions
            case "contribution" -> String.valueOf(kingdom.getContribution(player.getName()));
            case "contribution_formatted" -> formatNumber(kingdom.getContribution(player.getName()));
            case "total_contributions" -> {
                int total = kingdom.getMemberContributions().values().stream().mapToInt(Integer::intValue).sum();
                yield String.valueOf(total);
            }
            case "total_contributions_formatted" -> {
                int total = kingdom.getMemberContributions().values().stream().mapToInt(Integer::intValue).sum();
                yield formatNumber(total);
            }
            
            // Challenges
            case "challenges_completed" -> String.valueOf(kingdom.getTotalChallengesCompleted());
            case "challenges_completed_formatted" -> formatNumber(kingdom.getTotalChallengesCompleted());
            
            // Alliances
            case "alliances" -> String.valueOf(kingdom.getAlliances().size());
            case "alliances_count" -> String.valueOf(kingdom.getAlliances().size());
            case "alliances_list" -> String.join(", ", kingdom.getAlliances());
            case "has_alliances" -> kingdom.getAlliances().isEmpty() ? "false" : "true";
            
            // Kingdom age
            case "age_days" -> {
                long days = (System.currentTimeMillis() / 1000 - kingdom.getCreatedAt()) / 86400;
                yield String.valueOf(days);
            }
            case "age_formatted" -> {
                long days = (System.currentTimeMillis() / 1000 - kingdom.getCreatedAt()) / 86400;
                yield formatDays(days);
            }
            
            // Leaderboard positions (requires calculation)
            case "rank_level" -> String.valueOf(getRank(kingdom, "level"));
            case "rank_xp" -> String.valueOf(getRank(kingdom, "xp"));
            case "rank_members" -> String.valueOf(getRank(kingdom, "members"));
            case "rank_challenges" -> String.valueOf(getRank(kingdom, "challenges"));
            
            // Bank
            case "bank_balance" -> {
                double balance = plugin.getBankManager().getBalance(kingdomName);
                yield String.format("%.2f", balance);
            }
            case "bank_balance_formatted" -> {
                double balance = plugin.getBankManager().getBalance(kingdomName);
                yield plugin.getServer().getPluginManager().getPlugin("Vault") != null ?
                    com.excrele.kingdoms.util.EconomyManager.format(balance) : String.format("%.2f", balance);
            }
            
            // Activity
            case "activity_last_login" -> {
                com.excrele.kingdoms.model.PlayerActivity activity = plugin.getActivityManager().getActivity(player.getName());
                if (activity != null) {
                    long days = activity.getDaysSinceLastLogin();
                    yield days == 0 ? "Today" : days + " days ago";
                }
                yield "Never";
            }
            case "activity_playtime" -> {
                com.excrele.kingdoms.model.PlayerActivity activity = plugin.getActivityManager().getActivity(player.getName());
                if (activity != null) {
                    long hours = activity.getTotalPlaytime() / 3600;
                    yield String.valueOf(hours);
                }
                yield "0";
            }
            case "activity_playtime_formatted" -> {
                com.excrele.kingdoms.model.PlayerActivity activity = plugin.getActivityManager().getActivity(player.getName());
                if (activity != null) {
                    long total = activity.getTotalPlaytime();
                    long hours = total / 3600;
                    long minutes = (total % 3600) / 60;
                    yield hours + "h " + minutes + "m";
                }
                yield "0h 0m";
            }
            
            // Member Title
            case "member_title" -> {
                com.excrele.kingdoms.manager.AdvancedMemberManager manager = plugin.getAdvancedMemberManager();
                if (manager != null) {
                    com.excrele.kingdoms.model.MemberTitle title = manager.getMemberTitle(kingdomName, player.getName());
                    yield title != null ? title.getFormattedTitle() : "";
                }
                yield "";
            }
            case "member_title_raw" -> {
                com.excrele.kingdoms.manager.AdvancedMemberManager manager = plugin.getAdvancedMemberManager();
                if (manager != null) {
                    com.excrele.kingdoms.model.MemberTitle title = manager.getMemberTitle(kingdomName, player.getName());
                    yield title != null ? title.getTitle() : "";
                }
                yield "";
            }
            
            // Kingdom Health Score
            case "health_score" -> {
                if (plugin.getStatisticsManager() != null) {
                    double score = plugin.getStatisticsManager().calculateKingdomHealthScore(kingdomName);
                    yield String.format("%.1f", score);
                }
                yield "0.0";
            }
            case "health_score_int" -> {
                if (plugin.getStatisticsManager() != null) {
                    double score = plugin.getStatisticsManager().calculateKingdomHealthScore(kingdomName);
                    yield String.valueOf((int) score);
                }
                yield "0";
            }
            
            // Vault
            case "vault_items" -> {
                if (plugin.getVaultManager() != null) {
                    org.bukkit.inventory.Inventory vault = plugin.getVaultManager().getVault(kingdom);
                    if (vault != null) {
                        int count = 0;
                        for (org.bukkit.inventory.ItemStack item : vault.getContents()) {
                            if (item != null && !item.getType().isAir()) count++;
                        }
                        yield String.valueOf(count);
                    }
                }
                yield "0";
            }
            
            // Customization
            case "motto" -> {
                if (plugin.getCustomizationManager() != null) {
                    com.excrele.kingdoms.model.KingdomCustomization custom = plugin.getCustomizationManager().getCustomization(kingdomName);
                    yield custom != null && custom.getMotto() != null ? custom.getMotto() : "";
                }
                yield "";
            }
            case "color" -> {
                if (plugin.getCustomizationManager() != null) {
                    com.excrele.kingdoms.model.KingdomCustomization custom = plugin.getCustomizationManager().getCustomization(kingdomName);
                    yield custom != null && custom.getColorCode() != null ? custom.getColorCode() : "§7";
                }
                yield "§7";
            }
            
            // Wars
            case "wars_active" -> {
                if (plugin.getWarManager() != null) {
                    java.util.List<com.excrele.kingdoms.model.War> wars = plugin.getWarManager().getActiveWars(kingdomName);
                    yield String.valueOf(wars != null ? wars.size() : 0);
                }
                yield "0";
            }
            case "at_war" -> {
                if (plugin.getWarManager() != null) {
                    java.util.List<com.excrele.kingdoms.model.War> wars = plugin.getWarManager().getActiveWars(kingdomName);
                    yield (wars != null && !wars.isEmpty()) ? "true" : "false";
                }
                yield "false";
            }
            
            // Trusts
            case "trusts_count" -> {
                if (plugin.getTrustManager() != null) {
                    java.util.Map<String, java.util.Set<com.excrele.kingdoms.model.TrustPermission>> trusts = plugin.getTrustManager().getAllTrusts(kingdomName);
                    yield String.valueOf(trusts != null ? trusts.size() : 0);
                }
                yield "0";
            }
            
            // Advanced Features
            case "waypoints_count" -> {
                if (plugin.getAdvancedFeaturesManager() != null) {
                    java.util.Map<String, com.excrele.kingdoms.model.Waypoint> waypoints = plugin.getAdvancedFeaturesManager().getWaypoints(kingdomName);
                    yield String.valueOf(waypoints != null ? waypoints.size() : 0);
                }
                yield "0";
            }
            case "farms_count" -> {
                if (plugin.getAdvancedFeaturesManager() != null) {
                    java.util.Map<String, com.excrele.kingdoms.model.KingdomFarm> farms = plugin.getAdvancedFeaturesManager().getFarms(kingdomName);
                    yield String.valueOf(farms != null ? farms.size() : 0);
                }
                yield "0";
            }
            case "workshops_count" -> {
                if (plugin.getAdvancedFeaturesManager() != null) {
                    // Would need to add getWorkshops method
                    yield "0";
                }
                yield "0";
            }
            case "auto_claim_enabled" -> {
                if (plugin.getAdvancedFeaturesManager() != null) {
                    yield plugin.getAdvancedFeaturesManager().isAutoClaimEnabled(player) ? "true" : "false";
                }
                yield "false";
            }
            
            // Statistics
            case "growth_rate" -> {
                if (plugin.getStatisticsManager() != null) {
                    java.util.List<com.excrele.kingdoms.manager.StatisticsManager.GrowthData> growth = plugin.getStatisticsManager().getGrowthData(kingdomName);
                    if (growth != null && growth.size() >= 2) {
                        com.excrele.kingdoms.manager.StatisticsManager.GrowthData latest = growth.get(growth.size() - 1);
                        com.excrele.kingdoms.manager.StatisticsManager.GrowthData previous = growth.get(growth.size() - 2);
                        int levelGrowth = latest.level - previous.level;
                        yield String.valueOf(levelGrowth);
                    }
                }
                yield "0";
            }
            
            // Communication
            case "announcements_count" -> {
                if (plugin.getCommunicationManager() != null) {
                    java.util.List<com.excrele.kingdoms.model.KingdomAnnouncement> anns = plugin.getCommunicationManager().getAnnouncements(kingdomName);
                    yield String.valueOf(anns != null ? anns.size() : 0);
                }
                yield "0";
            }
            case "events_upcoming" -> {
                if (plugin.getCommunicationManager() != null) {
                    java.util.List<com.excrele.kingdoms.model.KingdomEvent> events = plugin.getCommunicationManager().getEvents(kingdomName);
                    if (events != null) {
                        long now = System.currentTimeMillis() / 1000;
                        long count = events.stream().filter(e -> e.getScheduledTime() > now).count();
                        yield String.valueOf(count);
                    }
                }
                yield "0";
            }
            
            // Claim Economy
            case "claims_for_sale" -> {
                if (plugin.getClaimEconomyManager() != null) {
                    // Would need method to get count
                    yield "0";
                }
                yield "0";
            }
            
            // World-specific
            case "claims_in_world" -> {
                String worldName = player.getWorld().getName();
                int count = 0;
                for (java.util.List<org.bukkit.Chunk> claimGroup : kingdom.getClaims()) {
                    for (org.bukkit.Chunk chunk : claimGroup) {
                        if (chunk.getWorld().getName().equals(worldName)) {
                            count++;
                        }
                    }
                }
                yield String.valueOf(count);
            }
            
            default -> null;
        };
    }

    private String formatNumber(int number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }

    private String formatDays(long days) {
        if (days >= 365) {
            long years = days / 365;
            long remainingDays = days % 365;
            if (remainingDays > 0) {
                return years + "y " + remainingDays + "d";
            }
            return years + "y";
        }
        return days + "d";
    }

    private String generateProgressBar(int progress, int length) {
        int filled = (progress * length) / 100;
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        bar.append("§7");
        for (int i = filled; i < length; i++) {
            bar.append("█");
        }
        return bar.toString();
    }

    private int getRank(Kingdom kingdom, String type) {
        int rank = 1;
        com.excrele.kingdoms.manager.KingdomManager km = plugin.getKingdomManager();
        if (km == null) return rank;
        for (Kingdom k : km.getKingdoms().values()) {
            if (k == kingdom) continue;
            boolean higher = switch (type) {
                case "level" -> k.getLevel() > kingdom.getLevel() || 
                              (k.getLevel() == kingdom.getLevel() && k.getXp() > kingdom.getXp());
                case "xp" -> k.getXp() > kingdom.getXp() || 
                            (k.getXp() == kingdom.getXp() && k.getLevel() > kingdom.getLevel());
                case "members" -> (k.getMembers().size() + 1) > (kingdom.getMembers().size() + 1);
                case "challenges" -> k.getTotalChallengesCompleted() > kingdom.getTotalChallengesCompleted();
                default -> false;
            };
            if (higher) rank++;
        }
        return rank;
    }
}

