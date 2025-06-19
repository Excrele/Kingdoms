package com.excrele.kingdoms.command;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.gui.ChallengeGUI;
import com.excrele.kingdoms.gui.ClaimMapGenerator;
import com.excrele.kingdoms.gui.KingdomManagementGUI;
import com.excrele.kingdoms.model.Challenge;
import com.excrele.kingdoms.model.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class KingdomCommand implements CommandExecutor {
    private final KingdomsPlugin plugin;
    private final Map<String, String> pendingInvites = new HashMap<>();

    public KingdomCommand(KingdomsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("kingdom")) return false;
        if (args.length == 0) {
            sender.sendMessage("Usage: /kingdom <subcommand>");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "create":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /kingdom create <name>");
                    return true;
                }
                String kingdomName = args[1];
                if (plugin.getKingdomManager().getKingdom(kingdomName) != null) {
                    sender.sendMessage("Kingdom already exists!");
                    return true;
                }
                Kingdom newKingdom = new Kingdom(kingdomName, sender.getName());
                plugin.getKingdomManager().addKingdom(newKingdom);
                plugin.getKingdomManager().setPlayerKingdom(sender.getName(), kingdomName);
                sender.sendMessage("Kingdom created: " + kingdomName);
                plugin.getKingdomManager().saveKingdoms(
                        plugin.getKingdomsConfig(),
                        plugin.getKingdomsFile()
                );
                return true;

            case "invite":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /kingdom invite <player>");
                    return true;
                }
                String invitedPlayer = args[1];
                String inviterKingdom = plugin.getKingdomManager().getKingdomOfPlayer(sender.getName());
                if (inviterKingdom == null) {
                    sender.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom kingdom = plugin.getKingdomManager().getKingdom(inviterKingdom);
                if (!kingdom.getKing().equals(sender.getName())) {
                    sender.sendMessage("Only the king can invite players!");
                    return true;
                }
                pendingInvites.put(invitedPlayer, inviterKingdom);
                sender.sendMessage("Invited " + invitedPlayer + " to " + inviterKingdom);
                return true;

            case "accept":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can accept invites!");
                    return true;
                }
                String inviteKingdom = pendingInvites.get(sender.getName());
                if (inviteKingdom == null) {
                    sender.sendMessage("You have no pending invites!");
                    return true;
                }
                Kingdom joinKingdom = plugin.getKingdomManager().getKingdom(inviteKingdom);
                joinKingdom.addMember(sender.getName());
                plugin.getKingdomManager().setPlayerKingdom(sender.getName(), inviteKingdom);
                pendingInvites.remove(sender.getName());
                sender.sendMessage("Joined kingdom: " + inviteKingdom);
                plugin.getKingdomManager().saveKingdoms(
                        plugin.getKingdomsConfig(),
                        plugin.getKingdomsFile()
                );
                return true;

            case "leave":
                String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(sender.getName());
                if (playerKingdom == null) {
                    sender.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom leaveKingdom = plugin.getKingdomManager().getKingdom(playerKingdom);
                if (leaveKingdom.getKing().equals(sender.getName())) {
                    sender.sendMessage("The king cannot leave! Use /kingdom admin dissolve instead.");
                    return true;
                }
                leaveKingdom.getMembers().remove(sender.getName());
                plugin.getKingdomManager().removePlayerKingdom(sender.getName());
                sender.sendMessage("You have left " + playerKingdom);
                plugin.getKingdomManager().saveKingdoms(
                        plugin.getKingdomsConfig(),
                        plugin.getKingdomsFile()
                );
                return true;

            case "claim":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can claim chunks!");
                    return true;
                }
                Player claimPlayer = (Player) sender;
                String claimKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(claimPlayer.getName());
                if (claimKingdomName == null) {
                    claimPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom claimKingdom = plugin.getKingdomManager().getKingdom(claimKingdomName);
                Chunk claimChunk = claimPlayer.getLocation().getChunk();
                if (plugin.getClaimManager().claimChunk(claimKingdom, claimChunk)) {
                    claimPlayer.sendMessage("Chunk claimed!");
                    plugin.getKingdomManager().saveKingdoms(
                            plugin.getKingdomsConfig(),
                            plugin.getKingdomsFile()
                    );
                } else {
                    claimPlayer.sendMessage("Cannot claim this chunk!");
                }
                return true;

            case "unclaim":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can unclaim chunks!");
                    return true;
                }
                Player unclaimPlayer = (Player) sender;
                String unclaimKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(unclaimPlayer.getName());
                if (unclaimKingdomName == null) {
                    unclaimPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom unclaimKingdom = plugin.getKingdomManager().getKingdom(unclaimKingdomName);
                if (!unclaimKingdom.getKing().equals(unclaimPlayer.getName())) {
                    unclaimPlayer.sendMessage("Only the king can unclaim chunks!");
                    return true;
                }
                Chunk unclaimChunk = unclaimPlayer.getLocation().getChunk();
                if (plugin.getClaimManager().unclaimChunk(unclaimKingdom, unclaimChunk)) {
                    unclaimPlayer.sendMessage("Chunk unclaimed!");
                    plugin.getKingdomManager().saveKingdoms(
                            plugin.getKingdomsConfig(),
                            plugin.getKingdomsFile()
                    );
                } else {
                    unclaimPlayer.sendMessage("Cannot unclaim this chunk!");
                }
                return true;

            case "setplot":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can set plots!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("Usage: /kingdom setplot <type>");
                    return true;
                }
                Player setplotPlayer = (Player) sender;
                String setplotKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(setplotPlayer.getName());
                if (setplotKingdomName == null) {
                    setplotPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom setplotKingdom = plugin.getKingdomManager().getKingdom(setplotKingdomName);
                Chunk plotChunk = setplotPlayer.getLocation().getChunk();
                if (plugin.getKingdomManager().getKingdomByChunk(plotChunk) != setplotKingdom) {
                    setplotPlayer.sendMessage("This chunk is not claimed by your kingdom!");
                    return true;
                }
                String plotType = args[1];
                setplotKingdom.setPlotType(plotChunk, plotType);
                setplotPlayer.sendMessage("Plot type set to " + plotType);
                plugin.getKingdomManager().saveKingdoms(
                        plugin.getKingdomsConfig(),
                        plugin.getKingdomsFile()
                );
                return true;

            case "flag":
                if (args.length < 3) {
                    sender.sendMessage("Usage: /kingdom flag <flag> <value>");
                    return true;
                }
                String flagKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(sender.getName());
                if (flagKingdomName == null) {
                    sender.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom flagKingdom = plugin.getKingdomManager().getKingdom(flagKingdomName);
                if (!flagKingdom.getKing().equals(sender.getName())) {
                    sender.sendMessage("Only the king can set kingdom flags!");
                    return true;
                }
                String flag = args[1];
                String value = args[2];
                flagKingdom.getFlags().put(flag, value);
                sender.sendMessage("Set kingdom flag " + flag + " to " + value);
                plugin.getKingdomManager().saveKingdoms(
                        plugin.getKingdomsConfig(),
                        plugin.getKingdomsFile()
                );
                return true;

            case "plotflag":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can set plot flags!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("Usage: /kingdom plotflag <flag> <value>");
                    return true;
                }
                Player plotflagPlayer = (Player) sender;
                String plotflagKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(plotflagPlayer.getName());
                if (plotflagKingdomName == null) {
                    plotflagPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom plotflagKingdom = plugin.getKingdomManager().getKingdom(plotflagKingdomName);
                Chunk flagChunk = plotflagPlayer.getLocation().getChunk();
                if (plugin.getKingdomManager().getKingdomByChunk(flagChunk) != plotflagKingdom) {
                    plotflagPlayer.sendMessage("This chunk is not claimed by your kingdom!");
                    return true;
                }
                String plotFlag = args[1];
                String plotValue = args[2];
                plugin.getFlagManager().setPlotFlag(plotflagPlayer, plotFlag, plotValue, flagChunk);
                plotflagPlayer.sendMessage("Set plot flag " + plotFlag + " to " + plotValue);
                plugin.getKingdomManager().saveKingdoms(
                        plugin.getKingdomsConfig(),
                        plugin.getKingdomsFile()
                );
                return true;

            case "challenges":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can view challenges!");
                    return true;
                }
                Player challengePlayer = (Player) sender;
                String challengeKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(challengePlayer.getName());
                if (challengeKingdomName == null) {
                    challengePlayer.sendMessage("You must be in a kingdom to view challenges!");
                    return true;
                }
                if (args.length > 1 && args[1].equalsIgnoreCase("gui")) {
                    ChallengeGUI.openChallengeGUI(challengePlayer);
                } else {
                    challengePlayer.sendMessage("Available Challenges:");
                    for (Challenge challenge : plugin.getChallengeManager().getChallenges()) {
                        boolean onCooldown = plugin.getChallengeManager().isChallengeOnCooldown(challengePlayer, challenge);
                        challengePlayer.sendMessage(String.format(
                                "- %s (Difficulty: %d, XP: %d) %s",
                                challenge.getDescription(),
                                challenge.getDifficulty(),
                                challenge.getXpReward(),
                                onCooldown ? "[On Cooldown]" : ""
                        ));
                    }
                }
                return true;

            case "gui":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can open the kingdom GUI!");
                    return true;
                }
                KingdomManagementGUI.openKingdomGUI((Player) sender);
                return true;

            case "map":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can view the claim map!");
                    return true;
                }
                Player mapPlayer = (Player) sender;
                String mapKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(mapPlayer.getName());
                if (mapKingdomName == null) {
                    mapPlayer.sendMessage("You must be in a kingdom to view the claim map!");
                    return true;
                }
                mapPlayer.sendMessage(ClaimMapGenerator.generateClaimMap(mapPlayer));
                return true;

            case "admin":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /kingdom admin <list|dissolve|forceunclaim|setflag>");
                    return true;
                }
                if (!sender.hasPermission("kingdoms.admin")) {
                    sender.sendMessage("You do not have permission!");
                    return true;
                }
                String adminSub = args[1].toLowerCase();
                switch (adminSub) {
                    case "list":
                        sender.sendMessage("Kingdoms: " + String.join(", ", plugin.getKingdomManager().getKingdoms().keySet()));
                        return true;
                    case "dissolve":
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /kingdom admin dissolve <kingdom>");
                            return true;
                        }
                        String dissolveKingdom = args[2];
                        Kingdom toDissolve = plugin.getKingdomManager().getKingdom(dissolveKingdom);
                        if (toDissolve == null) {
                            sender.sendMessage("Kingdom not found!");
                            return true;
                        }
                        plugin.getKingdomManager().dissolveKingdom(dissolveKingdom);
                        sender.sendMessage("Kingdom " + dissolveKingdom + " dissolved!");
                        plugin.getKingdomManager().saveKingdoms(
                                plugin.getKingdomsConfig(),
                                plugin.getKingdomsFile()
                        );
                        return true;
                    case "forceunclaim":
                        if (args.length < 4) {
                            sender.sendMessage("Usage: /kingdom admin forceunclaim <kingdom> <world:x:z>");
                            return true;
                        }
                        String forceKingdomName = args[2];
                        Kingdom forceKingdom = plugin.getKingdomManager().getKingdom(forceKingdomName);
                        if (forceKingdom == null) {
                            sender.sendMessage("Kingdom not found!");
                            return true;
                        }
                        String[] coords = args[3].split(":");
                        Chunk forceChunk = plugin.getServer().getWorld(coords[0]).getChunkAt(Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
                        if (plugin.getClaimManager().unclaimChunk(forceKingdom, forceChunk)) {
                            sender.sendMessage("Chunk force unclaimed!");
                            plugin.getKingdomManager().saveKingdoms(
                                    plugin.getKingdomsConfig(),
                                    plugin.getKingdomsFile()
                            );
                        } else {
                            sender.sendMessage("Failed to unclaim chunk!");
                        }
                        return true;
                    case "setflag":
                        if (args.length < 5) {
                            sender.sendMessage("Usage: /kingdom admin setflag <kingdom> <flag> <value>");
                            return true;
                        }
                        String adminFlagKingdomName = args[2];
                        Kingdom adminFlagKingdom = plugin.getKingdomManager().getKingdom(adminFlagKingdomName);
                        if (adminFlagKingdom == null) {
                            sender.sendMessage("Kingdom not found!");
                            return true;
                        }
                        String adminFlag = args[3];
                        String adminValue = args[4];
                        adminFlagKingdom.getFlags().put(adminFlag, adminValue);
                        sender.sendMessage("Set flag " + adminFlag + " to " + adminValue + " for " + adminFlagKingdomName);
                        plugin.getKingdomManager().saveKingdoms(
                                plugin.getKingdomsConfig(),
                                plugin.getKingdomsFile()
                        );
                        return true;
                }
                return true;

            default:
                sender.sendMessage("Unknown subcommand!");
                return true;
        }
    }
}