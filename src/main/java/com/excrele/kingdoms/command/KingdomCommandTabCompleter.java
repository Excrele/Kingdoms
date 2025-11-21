package com.excrele.kingdoms.command;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.Kingdom;
import com.excrele.kingdoms.model.TrustPermission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KingdomCommandTabCompleter implements TabCompleter {
    private final KingdomsPlugin plugin;

    public KingdomCommandTabCompleter(KingdomsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Main subcommands
            String[] subcommands = {
                "create", "invite", "accept", "leave", "claim", "unclaim", "setplot",
                "flag", "plotflag", "setspawn", "spawn", "challenges", "gui", "map",
                "contributions", "contribs", "stats", "promote", "kick", "chat", "c",
                "leaderboard", "lb", "alliance", "trust", "untrust", "trustlist", "trusts",
                "war", "bank", "admin", "help"
            };
            
            for (String sub : subcommands) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "create":
                // No completion needed for kingdom name
                break;
                
            case "invite":
                if (args.length == 2) {
                    return getOnlinePlayerNames(args[1]);
                }
                break;
                
            case "accept":
            case "leave":
            case "claim":
                if (args.length == 2) {
                    // Suggest radius values 1-10
                    for (int i = 1; i <= 10; i++) {
                        String radius = String.valueOf(i);
                        if (radius.startsWith(args[1])) {
                            completions.add(radius);
                        }
                    }
                }
                break;
                
            case "unclaim":
            case "setspawn":
            case "spawn":
            case "challenges":
            case "gui":
            case "map":
            case "contributions":
            case "contribs":
            case "stats":
            case "chat":
            case "c":
                // No additional args
                break;
                
            case "help":
                if (args.length == 2) {
                    // Page numbers 1-6
                    String[] pages = {"1", "2", "3", "4", "5", "6"};
                    for (String page : pages) {
                        if (page.startsWith(args[1])) {
                            completions.add(page);
                        }
                    }
                }
                break;
                
            case "setplot":
                if (args.length == 2) {
                    // Common plot types
                    String[] plotTypes = {"residential", "commercial", "industrial", "farm", "military", "public", "private"};
                    for (String type : plotTypes) {
                        if (type.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(type);
                        }
                    }
                }
                break;
                
            case "flag":
                if (args.length == 2) {
                    // Flag names (deprecated but still supported)
                    String[] flags = {"pvp", "mob-spawning", "explosions"};
                    for (String flag : flags) {
                        if (flag.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(flag);
                        }
                    }
                } else if (args.length == 3) {
                    // Flag values
                    String[] values = {"true", "false"};
                    for (String value : values) {
                        if (value.toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(value);
                        }
                    }
                }
                break;
                
            case "plotflag":
                if (args.length == 2) {
                    // Plot flag names
                    String[] plotFlags = {"build", "break", "pvp", "mob-spawning", "explosions", 
                                         "redstone", "piston", "animal-breed", "crop-trample"};
                    for (String flag : plotFlags) {
                        if (flag.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(flag);
                        }
                    }
                } else if (args.length == 3) {
                    // Plot flag values
                    if (args[1].equalsIgnoreCase("build") || args[1].equalsIgnoreCase("break")) {
                        String[] values = {"king", "members", "all"};
                        for (String value : values) {
                            if (value.toLowerCase().startsWith(args[2].toLowerCase())) {
                                completions.add(value);
                            }
                        }
                    } else {
                        String[] values = {"true", "false"};
                        for (String value : values) {
                            if (value.toLowerCase().startsWith(args[2].toLowerCase())) {
                                completions.add(value);
                            }
                        }
                    }
                }
                break;
                
            case "promote":
                if (args.length == 2) {
                    // Get kingdom members
                    if (sender instanceof Player) {
                        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(sender.getName());
                        if (kingdomName != null) {
                            Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
                            if (kingdom != null) {
                                List<String> members = new ArrayList<>(kingdom.getMembers());
                                members.add(kingdom.getKing());
                                return filterList(members, args[1]);
                            }
                        }
                    }
                } else if (args.length == 3) {
                    // Roles
                    String[] roles = {"ADVISOR", "GUARD", "BUILDER", "MEMBER"};
                    for (String role : roles) {
                        if (role.toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(role);
                        }
                    }
                }
                break;
                
            case "kick":
                if (args.length == 2) {
                    // Get kingdom members
                    if (sender instanceof Player) {
                        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(sender.getName());
                        if (kingdomName != null) {
                            Kingdom kingdom = plugin.getKingdomManager().getKingdom(kingdomName);
                            if (kingdom != null) {
                                List<String> members = new ArrayList<>(kingdom.getMembers());
                                return filterList(members, args[1]);
                            }
                        }
                    }
                }
                break;
                
            case "leaderboard":
            case "lb":
                if (args.length == 2) {
                    String[] types = {"level", "xp", "members", "challenges"};
                    for (String type : types) {
                        if (type.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(type);
                        }
                    }
                }
                break;
                
            case "alliance":
                if (args.length == 2) {
                    String[] allianceSubs = {"invite", "accept", "deny", "list", "remove"};
                    for (String sub : allianceSubs) {
                        if (sub.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(sub);
                        }
                    }
                } else if (args.length == 3) {
                    if (args[1].equalsIgnoreCase("invite") || args[1].equalsIgnoreCase("accept") || 
                        args[1].equalsIgnoreCase("deny") || args[1].equalsIgnoreCase("remove")) {
                        return getKingdomNames(args[2]);
                    }
                }
                break;
                
            case "trust":
                if (args.length == 2) {
                    return getOnlinePlayerNames(args[1]);
                } else if (args.length == 3) {
                    // Trust permissions
                    for (TrustPermission perm : TrustPermission.values()) {
                        if (perm.getKey().toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(perm.getKey());
                        }
                    }
                }
                break;
                
            case "untrust":
                if (args.length == 2) {
                    // Get trusted players
                    if (sender instanceof Player) {
                        String kingdomName = plugin.getKingdomManager().getKingdomOfPlayer(sender.getName());
                        if (kingdomName != null) {
                            List<String> trusted = new ArrayList<>(plugin.getTrustManager().getAllTrusts(kingdomName).keySet());
                            return filterList(trusted, args[1]);
                        }
                    }
                }
                break;
                
            case "trustlist":
            case "trusts":
                // No additional args
                break;
                
            case "war":
                if (args.length == 2) {
                    String[] warSubs = {"declare", "end", "list", "status"};
                    for (String sub : warSubs) {
                        if (sub.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(sub);
                        }
                    }
                } else if (args.length == 3) {
                    if (args[1].equalsIgnoreCase("declare") || args[1].equalsIgnoreCase("end") || 
                        args[1].equalsIgnoreCase("status")) {
                        return getKingdomNames(args[2]);
                    }
                } else if (args.length == 4 && args[1].equalsIgnoreCase("declare")) {
                    // Duration (hours) - no completion needed
                    break;
                }
                break;
                
            case "bank":
                if (args.length == 2) {
                    String[] bankSubs = {"deposit", "withdraw", "balance", "transfer"};
                    for (String sub : bankSubs) {
                        if (sub.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(sub);
                        }
                    }
                } else if (args.length == 3) {
                    if (args[1].equalsIgnoreCase("deposit") || args[1].equalsIgnoreCase("withdraw")) {
                        // Amount - no completion
                        break;
                    } else if (args[1].equalsIgnoreCase("transfer")) {
                        return getKingdomNames(args[2]);
                    }
                } else if (args.length == 4 && args[1].equalsIgnoreCase("transfer")) {
                    // Amount - no completion
                    break;
                }
                break;
                
            case "admin":
                if (!sender.hasPermission("kingdoms.admin")) {
                    break;
                }
                if (args.length == 2) {
                    String[] adminSubs = {"list", "dissolve", "forceunclaim", "setflag"};
                    for (String sub : adminSubs) {
                        if (sub.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(sub);
                        }
                    }
                } else if (args.length == 3) {
                    if (args[1].equalsIgnoreCase("dissolve") || args[1].equalsIgnoreCase("forceunclaim") || 
                        args[1].equalsIgnoreCase("setflag")) {
                        return getKingdomNames(args[2]);
                    }
                } else if (args.length == 4) {
                    if (args[1].equalsIgnoreCase("forceunclaim")) {
                        // World:x:z format - no completion
                        break;
                    } else if (args[1].equalsIgnoreCase("setflag")) {
                        // Flag names
                        String[] flags = {"pvp", "mob-spawning", "explosions"};
                        for (String flag : flags) {
                            if (flag.toLowerCase().startsWith(args[3].toLowerCase())) {
                                completions.add(flag);
                            }
                        }
                    }
                } else if (args.length == 5 && args[1].equalsIgnoreCase("setflag")) {
                    // Flag values
                    String[] values = {"true", "false"};
                    for (String value : values) {
                        if (value.toLowerCase().startsWith(args[4].toLowerCase())) {
                            completions.add(value);
                        }
                    }
                }
                break;
        }

        return completions;
    }

    private List<String> getOnlinePlayerNames(String input) {
        List<String> names = new ArrayList<>();
        String lowerInput = input.toLowerCase();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(lowerInput)) {
                names.add(player.getName());
            }
        }
        return names;
    }

    private List<String> getKingdomNames(String input) {
        List<String> names = new ArrayList<>();
        String lowerInput = input.toLowerCase();
        for (String kingdomName : plugin.getKingdomManager().getKingdoms().keySet()) {
            if (kingdomName.toLowerCase().startsWith(lowerInput)) {
                names.add(kingdomName);
            }
        }
        return names;
    }

    private List<String> filterList(List<String> list, String input) {
        String lowerInput = input.toLowerCase();
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(lowerInput))
                .collect(Collectors.toList());
    }
}

