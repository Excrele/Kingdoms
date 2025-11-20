package com.excrele.kingdoms.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.gui.ChallengeGUI;
import com.excrele.kingdoms.gui.ClaimMapGenerator;
import com.excrele.kingdoms.gui.KingdomManagementGUI;
import com.excrele.kingdoms.model.Challenge;
import com.excrele.kingdoms.model.Kingdom;

public class KingdomCommand implements CommandExecutor {
    private final KingdomsPlugin plugin;
    private final Map<String, String> pendingInvites = new HashMap<>();
    private final Map<String, String> pendingAllianceRequests = new HashMap<>(); // kingdom -> requesting kingdom

    public KingdomCommand(KingdomsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("kingdom") && !cmd.getName().equalsIgnoreCase("k")) return false;
        if (args.length == 0) {
            sender.sendMessage("Usage: /" + label + " <subcommand>");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "create":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can create kingdoms!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " create <name>");
                    return true;
                }
                Player createPlayer = (Player) sender;
                String kingdomName = args[1];
                if (plugin.getKingdomManager().getKingdom(kingdomName) != null) {
                    sender.sendMessage("Kingdom already exists!");
                    return true;
                }
                Kingdom newKingdom = new Kingdom(kingdomName, createPlayer.getName());
                plugin.getKingdomManager().addKingdom(newKingdom);
                plugin.getKingdomManager().setPlayerKingdom(createPlayer.getName(), kingdomName);

                // Auto-claim current chunk and set spawn
                Chunk initialChunk = createPlayer.getLocation().getChunk();
                if (plugin.getClaimManager().claimChunk(newKingdom, initialChunk)) {
                    // claimChunk already adds the chunk to claims, no need to add again
                    // Set default spawn to chunk center
                    double x = (initialChunk.getX() << 4) + 8.5;
                    double z = (initialChunk.getZ() << 4) + 8.5;
                    double y = initialChunk.getWorld().getHighestBlockYAt((int) x, (int) z) + 1;
                    newKingdom.setSpawn(new Location(initialChunk.getWorld(), x, y, z, 0, 0));
                    createPlayer.sendMessage("Chunk claimed and spawn set!");
                } else {
                    createPlayer.sendMessage("Cannot claim this chunk!");
                }

                sender.sendMessage("Kingdom created: " + kingdomName);
                plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                return true;

            case "invite":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " invite <player>");
                    return true;
                }
                String invitedPlayer = args[1];
                String inviterKingdom = plugin.getKingdomManager().getKingdomOfPlayer(sender.getName());
                if (inviterKingdom == null) {
                    sender.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom kingdom = plugin.getKingdomManager().getKingdom(inviterKingdom);
                if (!kingdom.hasPermission(sender.getName(), "invite")) {
                    sender.sendMessage("You don't have permission to invite players!");
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
                joinKingdom.setRole(sender.getName(), com.excrele.kingdoms.model.MemberRole.MEMBER); // Default role
                plugin.getKingdomManager().setPlayerKingdom(sender.getName(), inviteKingdom);
                pendingInvites.remove(sender.getName());
                sender.sendMessage("Joined kingdom: " + inviteKingdom);
                plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                return true;

            case "leave":
                String playerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(sender.getName());
                if (playerKingdom == null) {
                    sender.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom leaveKingdom = plugin.getKingdomManager().getKingdom(playerKingdom);
                if (leaveKingdom.getKing().equals(sender.getName())) {
                    sender.sendMessage("The king cannot leave! Use /" + label + " admin dissolve instead.");
                    return true;
                }
                leaveKingdom.getMembers().remove(sender.getName());
                plugin.getKingdomManager().removePlayerKingdom(sender.getName());
                sender.sendMessage("You have left " + playerKingdom);
                plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
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
                if (!claimKingdom.hasPermission(claimPlayer.getName(), "claim")) {
                    claimPlayer.sendMessage("You don't have permission to claim chunks!");
                    return true;
                }
                Chunk claimChunk = claimPlayer.getLocation().getChunk();
                if (plugin.getClaimManager().claimChunk(claimKingdom, claimChunk)) {
                    claimPlayer.sendMessage("Chunk claimed!");
                    plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
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
                if (!unclaimKingdom.hasPermission(unclaimPlayer.getName(), "unclaim")) {
                    unclaimPlayer.sendMessage("You don't have permission to unclaim chunks!");
                    return true;
                }
                Chunk unclaimChunk = unclaimPlayer.getLocation().getChunk();
                if (plugin.getClaimManager().unclaimChunk(unclaimKingdom, unclaimChunk)) {
                    unclaimPlayer.sendMessage("Chunk unclaimed!");
                    plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
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
                    sender.sendMessage("Usage: /" + label + " setplot <type>");
                    return true;
                }
                Player setplotPlayer = (Player) sender;
                String setplotKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(setplotPlayer.getName());
                if (setplotKingdomName == null) {
                    setplotPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom setplotKingdom = plugin.getKingdomManager().getKingdom(setplotKingdomName);
                if (!setplotKingdom.hasPermission(setplotPlayer.getName(), "setplottype")) {
                    setplotPlayer.sendMessage("You don't have permission to set plot types!");
                    return true;
                }
                Chunk plotChunk = setplotPlayer.getLocation().getChunk();
                if (plugin.getKingdomManager().getKingdomByChunk(plotChunk) != setplotKingdom) {
                    setplotPlayer.sendMessage("This chunk is not claimed by your kingdom!");
                    return true;
                }
                String plotType = args[1];
                setplotKingdom.setPlotType(plotChunk, plotType);
                setplotPlayer.sendMessage("Plot type set to " + plotType);
                plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                return true;

            case "flag":
                if (args.length < 3) {
                    sender.sendMessage("Usage: /" + label + " flag <flag> <value>");
                    return true;
                }
                String flagKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(sender.getName());
                if (flagKingdomName == null) {
                    sender.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom flagKingdom = plugin.getKingdomManager().getKingdom(flagKingdomName);
                if (!flagKingdom.hasPermission(sender.getName(), "setflags")) {
                    sender.sendMessage("You don't have permission to set kingdom flags!");
                    return true;
                }
                sender.sendMessage("Kingdom-wide flags are deprecated. Use /" + label + " plotflag instead.");
                return true;

            case "plotflag":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can set plot flags!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("Usage: /" + label + " plotflag <flag> <value>");
                    return true;
                }
                Player plotflagPlayer = (Player) sender;
                String plotflagKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(plotflagPlayer.getName());
                if (plotflagKingdomName == null) {
                    plotflagPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom plotflagKingdom = plugin.getKingdomManager().getKingdom(plotflagKingdomName);
                if (!plotflagKingdom.hasPermission(plotflagPlayer.getName(), "setplotflags")) {
                    plotflagPlayer.sendMessage("You don't have permission to set plot flags!");
                    return true;
                }
                Chunk flagChunk = plotflagPlayer.getLocation().getChunk();
                if (plugin.getKingdomManager().getKingdomByChunk(flagChunk) != plotflagKingdom) {
                    plotflagPlayer.sendMessage("This chunk is not claimed by your kingdom!");
                    return true;
                }
                String plotFlag = args[1];
                String plotValue = args[2];
                plugin.getFlagManager().setPlotFlag(plotflagPlayer, plotFlag, plotValue, flagChunk);
                plotflagPlayer.sendMessage("Set plot flag " + plotFlag + " to " + plotValue);
                plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                return true;

            case "setspawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can set kingdom spawn!");
                    return true;
                }
                Player setspawnPlayer = (Player) sender;
                String setspawnKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(setspawnPlayer.getName());
                if (setspawnKingdomName == null) {
                    setspawnPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom setspawnKingdom = plugin.getKingdomManager().getKingdom(setspawnKingdomName);
                if (!setspawnKingdom.hasPermission(setspawnPlayer.getName(), "setflags")) {
                    setspawnPlayer.sendMessage("You don't have permission to set the spawn!");
                    return true;
                }
                Chunk spawnChunk = setspawnPlayer.getLocation().getChunk();
                if (plugin.getKingdomManager().getKingdomByChunk(spawnChunk) != setspawnKingdom) {
                    setspawnPlayer.sendMessage("You must be in a claimed chunk!");
                    return true;
                }
                setspawnKingdom.setSpawn(setspawnPlayer.getLocation());
                setspawnPlayer.sendMessage("Kingdom spawn set!");
                plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                return true;

            case "spawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can teleport to kingdom spawn!");
                    return true;
                }
                Player spawnPlayer = (Player) sender;
                String spawnKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(spawnPlayer.getName());
                if (spawnKingdomName == null) {
                    spawnPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom spawnKingdom = plugin.getKingdomManager().getKingdom(spawnKingdomName);
                Location spawn = spawnKingdom.getSpawn();
                if (spawn == null) {
                    spawnPlayer.sendMessage("No spawn set for this kingdom!");
                    return true;
                }
                spawnPlayer.teleport(spawn);
                spawnPlayer.sendMessage("Teleported to kingdom spawn!");
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

            case "contributions":
            case "contribs":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can view contributions!");
                    return true;
                }
                Player contribPlayer = (Player) sender;
                String contribKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(contribPlayer.getName());
                if (contribKingdomName == null) {
                    contribPlayer.sendMessage("You must be in a kingdom to view contributions!");
                    return true;
                }
                Kingdom contribKingdom = plugin.getKingdomManager().getKingdom(contribKingdomName);
                if (contribKingdom == null) return true;
                
                contribPlayer.sendMessage("§6=== Kingdom Contributions ===");
                contribPlayer.sendMessage("§7Kingdom: §e" + contribKingdomName);
                
                // Sort by contribution amount
                List<Map.Entry<String, Integer>> sortedContribs = new ArrayList<>(contribKingdom.getMemberContributions().entrySet());
                sortedContribs.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                
                // Include king
                int kingContrib = contribKingdom.getContribution(contribKingdom.getKing());
                contribPlayer.sendMessage("§7King §e" + contribKingdom.getKing() + "§7: §a" + kingContrib + " XP");
                
                // Show top contributors
                int shown = 0;
                for (Map.Entry<String, Integer> entry : sortedContribs) {
                    if (entry.getKey().equals(contribKingdom.getKing())) continue; // Already shown
                    contribPlayer.sendMessage("§7- §e" + entry.getKey() + "§7: §a" + entry.getValue() + " XP");
                    shown++;
                    if (shown >= 9) break; // Show top 10 total
                }
                
                if (sortedContribs.isEmpty() && kingContrib == 0) {
                    contribPlayer.sendMessage("§7No contributions yet!");
                }
                return true;

            case "stats":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can view kingdom stats!");
                    return true;
                }
                Player statsPlayer = (Player) sender;
                String statsKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(statsPlayer.getName());
                if (statsKingdomName == null) {
                    statsPlayer.sendMessage("You must be in a kingdom to view stats!");
                    return true;
                }
                Kingdom statsKingdom = plugin.getKingdomManager().getKingdom(statsKingdomName);
                if (statsKingdom == null) return true;
                
                long daysSinceCreation = (System.currentTimeMillis() / 1000 - statsKingdom.getCreatedAt()) / 86400;
                
                statsPlayer.sendMessage("§6=== Kingdom Statistics ===");
                statsPlayer.sendMessage("§7Kingdom: §e" + statsKingdomName);
                statsPlayer.sendMessage("§7Level: §e" + statsKingdom.getLevel());
                statsPlayer.sendMessage("§7Total XP: §e" + statsKingdom.getXp());
                statsPlayer.sendMessage("§7Members: §e" + (statsKingdom.getMembers().size() + 1)); // +1 for king
                statsPlayer.sendMessage("§7Claims: §e" + statsKingdom.getCurrentClaimChunks() + "/" + statsKingdom.getMaxClaimChunks());
                statsPlayer.sendMessage("§7Challenges Completed: §e" + statsKingdom.getTotalChallengesCompleted());
                statsPlayer.sendMessage("§7Created: §e" + daysSinceCreation + " days ago");
                
                int totalContributions = statsKingdom.getMemberContributions().values().stream().mapToInt(Integer::intValue).sum();
                statsPlayer.sendMessage("§7Total Contributions: §e" + totalContributions + " XP");
                return true;

            case "promote":
                if (args.length < 3) {
                    sender.sendMessage("Usage: /" + label + " promote <player> <role>");
                    sender.sendMessage("Roles: ADVISOR, GUARD, BUILDER, MEMBER");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can promote members!");
                    return true;
                }
                Player promotePlayer = (Player) sender;
                String promoteKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(promotePlayer.getName());
                if (promoteKingdomName == null) {
                    promotePlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom promoteKingdom = plugin.getKingdomManager().getKingdom(promoteKingdomName);
                if (!promoteKingdom.hasPermission(promotePlayer.getName(), "promote")) {
                    promotePlayer.sendMessage("You don't have permission to promote members!");
                    return true;
                }
                String targetPlayer = args[1];
                if (!promoteKingdom.getMembers().contains(targetPlayer) && !promoteKingdom.getKing().equals(targetPlayer)) {
                    promotePlayer.sendMessage("Player is not in your kingdom!");
                    return true;
                }
                try {
                    com.excrele.kingdoms.model.MemberRole newRole = com.excrele.kingdoms.model.MemberRole.valueOf(args[2].toUpperCase());
                    if (newRole == com.excrele.kingdoms.model.MemberRole.KING) {
                        promotePlayer.sendMessage("Cannot set role to KING!");
                        return true;
                    }
                    promoteKingdom.setRole(targetPlayer, newRole);
                    promotePlayer.sendMessage("§aPromoted " + targetPlayer + " to " + newRole.getDisplayName());
                    plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                } catch (IllegalArgumentException e) {
                    promotePlayer.sendMessage("Invalid role! Use: ADVISOR, GUARD, BUILDER, or MEMBER");
                }
                return true;

            case "kick":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " kick <player>");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can kick members!");
                    return true;
                }
                Player kickPlayer = (Player) sender;
                String kickKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(kickPlayer.getName());
                if (kickKingdomName == null) {
                    kickPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom kickKingdom = plugin.getKingdomManager().getKingdom(kickKingdomName);
                if (!kickKingdom.hasPermission(kickPlayer.getName(), "kick")) {
                    kickPlayer.sendMessage("You don't have permission to kick members!");
                    return true;
                }
                String kickTarget = args[1];
                if (kickKingdom.getKing().equals(kickTarget)) {
                    kickPlayer.sendMessage("Cannot kick the king!");
                    return true;
                }
                if (!kickKingdom.getMembers().contains(kickTarget)) {
                    kickPlayer.sendMessage("Player is not in your kingdom!");
                    return true;
                }
                kickKingdom.getMembers().remove(kickTarget);
                kickKingdom.getMemberRoles().remove(kickTarget);
                kickKingdom.getMemberContributions().remove(kickTarget);
                plugin.getKingdomManager().removePlayerKingdom(kickTarget);
                kickPlayer.sendMessage("§aKicked " + kickTarget + " from the kingdom");
                plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                return true;

            case "chat":
            case "c":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use kingdom chat!");
                    return true;
                }
                Player chatPlayer = (Player) sender;
                String chatKingdomName = plugin.getKingdomManager().getKingdomOfPlayer(chatPlayer.getName());
                if (chatKingdomName == null) {
                    chatPlayer.sendMessage("You must be in a kingdom to use kingdom chat!");
                    return true;
                }
                if (args.length > 1) {
                    // Send message directly
                    String chatMessage = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                    com.excrele.kingdoms.listener.KingdomChatListener.setKingdomChatMode(chatPlayer, true);
                    chatPlayer.chat(chatMessage);
                    com.excrele.kingdoms.listener.KingdomChatListener.setKingdomChatMode(chatPlayer, false);
                } else {
                    // Toggle chat mode
                    boolean currentMode = com.excrele.kingdoms.listener.KingdomChatListener.isKingdomChatMode(chatPlayer);
                    com.excrele.kingdoms.listener.KingdomChatListener.setKingdomChatMode(chatPlayer, !currentMode);
                    if (!currentMode) {
                        chatPlayer.sendMessage("§aKingdom chat enabled! Type /kingdom chat <message> or /kc <message> to send messages.");
                    } else {
                        chatPlayer.sendMessage("§cKingdom chat disabled.");
                    }
                }
                return true;

            case "leaderboard":
            case "lb":
                String lbType = args.length > 1 ? args[1].toLowerCase() : "level";
                sender.sendMessage("§6=== Kingdom Leaderboard (" + lbType.toUpperCase() + ") ===");
                
                List<Kingdom> sortedKingdoms = new ArrayList<>(plugin.getKingdomManager().getKingdoms().values());
                
                switch (lbType) {
                    case "level":
                        sortedKingdoms.sort((a, b) -> {
                            int levelCompare = Integer.compare(b.getLevel(), a.getLevel());
                            if (levelCompare != 0) return levelCompare;
                            return Integer.compare(b.getXp(), a.getXp());
                        });
                        break;
                    case "xp":
                        sortedKingdoms.sort((a, b) -> {
                            int xpCompare = Integer.compare(b.getXp(), a.getXp());
                            if (xpCompare != 0) return xpCompare;
                            return Integer.compare(b.getLevel(), a.getLevel());
                        });
                        break;
                    case "members":
                        sortedKingdoms.sort((a, b) -> Integer.compare(
                            b.getMembers().size() + 1, // +1 for king
                            a.getMembers().size() + 1
                        ));
                        break;
                    case "challenges":
                        sortedKingdoms.sort((a, b) -> Integer.compare(
                            b.getTotalChallengesCompleted(),
                            a.getTotalChallengesCompleted()
                        ));
                        break;
                    default:
                        sender.sendMessage("§cInvalid leaderboard type! Use: level, xp, members, or challenges");
                        return true;
                }
                
                int rank = 1;
                for (Kingdom k : sortedKingdoms) {
                    if (rank > 10) break; // Show top 10
                    String entry = "§7" + rank + ". §e" + k.getName();
                    switch (lbType) {
                        case "level":
                            entry += " §7- Level §e" + k.getLevel() + " §7(§e" + k.getXp() + " XP§7)";
                            break;
                        case "xp":
                            entry += " §7- §e" + k.getXp() + " XP §7(Level §e" + k.getLevel() + "§7)";
                            break;
                        case "members":
                            entry += " §7- §e" + (k.getMembers().size() + 1) + " members";
                            break;
                        case "challenges":
                            entry += " §7- §e" + k.getTotalChallengesCompleted() + " challenges";
                            break;
                    }
                    sender.sendMessage(entry);
                    rank++;
                }
                
                if (sortedKingdoms.isEmpty()) {
                    sender.sendMessage("§7No kingdoms found!");
                }
                return true;

            case "alliance":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " alliance <invite|accept|deny|list|remove> [kingdom]");
                    return true;
                }
                String allianceSub = args[1].toLowerCase();
                switch (allianceSub) {
                    case "invite":
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /" + label + " alliance invite <kingdom>");
                            return true;
                        }
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can invite to alliances!");
                            return true;
                        }
                        Player allianceInviter = (Player) sender;
                        String allianceInviterKingdom = plugin.getKingdomManager().getKingdomOfPlayer(allianceInviter.getName());
                        if (allianceInviterKingdom == null) {
                            allianceInviter.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom allianceInviterK = plugin.getKingdomManager().getKingdom(allianceInviterKingdom);
                        if (!allianceInviterK.hasPermission(allianceInviter.getName(), "setflags")) {
                            allianceInviter.sendMessage("You don't have permission to manage alliances!");
                            return true;
                        }
                        String targetKingdom = args[2];
                        Kingdom targetK = plugin.getKingdomManager().getKingdom(targetKingdom);
                        if (targetK == null) {
                            allianceInviter.sendMessage("Kingdom not found!");
                            return true;
                        }
                        if (targetKingdom.equals(allianceInviterKingdom)) {
                            allianceInviter.sendMessage("You cannot form an alliance with yourself!");
                            return true;
                        }
                        if (allianceInviterK.isAllied(targetKingdom)) {
                            allianceInviter.sendMessage("You are already allied with " + targetKingdom + "!");
                            return true;
                        }
                        pendingAllianceRequests.put(targetKingdom, allianceInviterKingdom);
                        allianceInviter.sendMessage("§aAlliance request sent to " + targetKingdom);
                        Player targetKing = plugin.getServer().getPlayer(targetK.getKing());
                        if (targetKing != null && targetKing.isOnline()) {
                            targetKing.sendMessage("§6" + allianceInviterKingdom + " has requested an alliance!");
                            targetKing.sendMessage("§7Use /kingdom alliance accept " + allianceInviterKingdom + " to accept");
                        }
                        return true;
                    case "accept":
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /" + label + " alliance accept <kingdom>");
                            return true;
                        }
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can accept alliance requests!");
                            return true;
                        }
                        Player allianceAccepter = (Player) sender;
                        String allianceAccepterKingdom = plugin.getKingdomManager().getKingdomOfPlayer(allianceAccepter.getName());
                        if (allianceAccepterKingdom == null) {
                            allianceAccepter.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom allianceAccepterK = plugin.getKingdomManager().getKingdom(allianceAccepterKingdom);
                        if (!allianceAccepterK.hasPermission(allianceAccepter.getName(), "setflags")) {
                            allianceAccepter.sendMessage("You don't have permission to manage alliances!");
                            return true;
                        }
                        String requestingKingdom = args[2];
                        if (!pendingAllianceRequests.getOrDefault(allianceAccepterKingdom, "").equals(requestingKingdom)) {
                            allianceAccepter.sendMessage("No pending alliance request from " + requestingKingdom);
                            return true;
                        }
                        allianceAccepterK.addAlliance(requestingKingdom);
                        Kingdom requestingK = plugin.getKingdomManager().getKingdom(requestingKingdom);
                        if (requestingK != null) {
                            requestingK.addAlliance(allianceAccepterKingdom);
                            pendingAllianceRequests.remove(allianceAccepterKingdom);
                            allianceAccepter.sendMessage("§aAlliance formed with " + requestingKingdom + "!");
                            Player requestingKing = plugin.getServer().getPlayer(requestingK.getKing());
                            if (requestingKing != null && requestingKing.isOnline()) {
                                requestingKing.sendMessage("§a" + allianceAccepterKingdom + " accepted your alliance request!");
                            }
                        } else {
                            allianceAccepter.sendMessage("§cError: Kingdom not found!");
                        }
                        plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                        return true;
                    case "deny":
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /" + label + " alliance deny <kingdom>");
                            return true;
                        }
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can deny alliance requests!");
                            return true;
                        }
                        Player allianceDenier = (Player) sender;
                        String allianceDenierKingdom = plugin.getKingdomManager().getKingdomOfPlayer(allianceDenier.getName());
                        if (allianceDenierKingdom == null) {
                            allianceDenier.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom allianceDenierK = plugin.getKingdomManager().getKingdom(allianceDenierKingdom);
                        if (!allianceDenierK.hasPermission(allianceDenier.getName(), "setflags")) {
                            allianceDenier.sendMessage("You don't have permission to manage alliances!");
                            return true;
                        }
                        String deniedKingdom = args[2];
                        pendingAllianceRequests.remove(allianceDenierKingdom);
                        allianceDenier.sendMessage("§cAlliance request from " + deniedKingdom + " denied.");
                        return true;
                    case "list":
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can view alliances!");
                            return true;
                        }
                        Player allianceViewer = (Player) sender;
                        String allianceViewerKingdom = plugin.getKingdomManager().getKingdomOfPlayer(allianceViewer.getName());
                        if (allianceViewerKingdom == null) {
                            allianceViewer.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom allianceViewerK = plugin.getKingdomManager().getKingdom(allianceViewerKingdom);
                        allianceViewer.sendMessage("§6=== Alliances ===");
                        if (allianceViewerK.getAlliances().isEmpty()) {
                            allianceViewer.sendMessage("§7No alliances");
                        } else {
                            for (String ally : allianceViewerK.getAlliances()) {
                                allianceViewer.sendMessage("§7- §e" + ally);
                            }
                        }
                        return true;
                    case "remove":
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /" + label + " alliance remove <kingdom>");
                            return true;
                        }
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can remove alliances!");
                            return true;
                        }
                        Player allianceRemover = (Player) sender;
                        String allianceRemoverKingdom = plugin.getKingdomManager().getKingdomOfPlayer(allianceRemover.getName());
                        if (allianceRemoverKingdom == null) {
                            allianceRemover.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom allianceRemoverK = plugin.getKingdomManager().getKingdom(allianceRemoverKingdom);
                        if (!allianceRemoverK.hasPermission(allianceRemover.getName(), "setflags")) {
                            allianceRemover.sendMessage("You don't have permission to manage alliances!");
                            return true;
                        }
                        String removeKingdom = args[2];
                        if (!allianceRemoverK.isAllied(removeKingdom)) {
                            allianceRemover.sendMessage("You are not allied with " + removeKingdom);
                            return true;
                        }
                        allianceRemoverK.removeAlliance(removeKingdom);
                        Kingdom removeK = plugin.getKingdomManager().getKingdom(removeKingdom);
                        if (removeK != null) {
                            removeK.removeAlliance(allianceRemoverKingdom);
                        }
                        allianceRemover.sendMessage("§cAlliance with " + removeKingdom + " dissolved.");
                        plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                        return true;
                    default:
                        sender.sendMessage("Usage: /" + label + " alliance <invite|accept|deny|list|remove> [kingdom]");
                        return true;
                }

            case "admin":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " admin <list|dissolve|forceunclaim|setflag>");
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
                            sender.sendMessage("Usage: /" + label + " admin dissolve <kingdom>");
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
                        plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                        return true;
                    case "forceunclaim":
                        if (args.length < 4) {
                            sender.sendMessage("Usage: /" + label + " admin forceunclaim <kingdom> <world:x:z>");
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
                            plugin.getKingdomManager().saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                        } else {
                            sender.sendMessage("Failed to unclaim chunk!");
                        }
                        return true;
                    case "setflag":
                        if (args.length < 5) {
                            sender.sendMessage("Usage: /" + label + " admin setflag <kingdom> <flag> <value>");
                            return true;
                        }
                        String adminFlagKingdomName = args[2];
                        Kingdom adminFlagKingdom = plugin.getKingdomManager().getKingdom(adminFlagKingdomName);
                        if (adminFlagKingdom == null) {
                            sender.sendMessage("Kingdom not found!");
                            return true;
                        }
                        sender.sendMessage("Kingdom-wide flags are deprecated. Use per-chunk flags instead.");
                        return true;
                }
                return true;

            default:
                sender.sendMessage("Unknown subcommand!");
                return true;
        }
    }
}