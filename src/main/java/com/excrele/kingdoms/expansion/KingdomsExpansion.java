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
        for (Kingdom k : plugin.getKingdomManager().getKingdoms().values()) {
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

