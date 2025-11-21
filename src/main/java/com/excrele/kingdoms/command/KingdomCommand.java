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
        if (sender == null) return false;
        if (args.length == 0) {
            sender.sendMessage("Usage: /" + label + " <subcommand>");
            return true;
        }
        
        // Ensure plugin and managers are initialized
        if (plugin == null || plugin.getKingdomManager() == null) {
            sender.sendMessage("§cPlugin not initialized!");
            return true;
        }
        
        // Store managers in local variables for null safety
        com.excrele.kingdoms.manager.KingdomManager kingdomManager = plugin.getKingdomManager();
        com.excrele.kingdoms.manager.ClaimManager claimManager = plugin.getClaimManager();

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "create" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can create kingdoms!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " create <name>");
                    return true;
                }
                Player createPlayer = (Player) sender;
                String kingdomName = com.excrele.kingdoms.util.InputValidator.sanitize(args[1]);
                
                // Validate kingdom name
                com.excrele.kingdoms.util.InputValidator.ValidationResult validation = 
                    com.excrele.kingdoms.util.InputValidator.validateKingdomName(kingdomName);
                if (!validation.isValid()) {
                    sender.sendMessage("§c" + validation.getErrorMessage());
                    return true;
                }
                
                if (kingdomManager.getKingdom(kingdomName) != null) {
                    sender.sendMessage("§cKingdom already exists!");
                    return true;
                }
                Kingdom newKingdom = new Kingdom(kingdomName, createPlayer.getName());
                kingdomManager.addKingdom(newKingdom);
                kingdomManager.setPlayerKingdom(createPlayer.getName(), kingdomName);
                
                // Record history
                if (plugin.getStatisticsManager() != null) {
                    plugin.getStatisticsManager().addHistoryEntry(kingdomName, 
                        com.excrele.kingdoms.model.KingdomHistory.HistoryType.CREATED, 
                        "Kingdom created", createPlayer.getName());
                }

                // Auto-claim current chunk and set spawn
                org.bukkit.Location playerLoc = createPlayer.getLocation();
                if (playerLoc == null) {
                    createPlayer.sendMessage("§cUnable to claim chunk - invalid location!");
                    return true;
                }
                Chunk initialChunk = playerLoc.getChunk();
                org.bukkit.World world = initialChunk.getWorld();
                if (claimManager != null && claimManager.claimChunk(newKingdom, initialChunk)) {
                    // claimChunk already adds the chunk to claims, no need to add again
                    // Set default spawn to chunk center
                    double x = (initialChunk.getX() << 4) + 8.5;
                    double z = (initialChunk.getZ() << 4) + 8.5;
                    double y = world.getHighestBlockYAt((int) x, (int) z) + 1;
                    newKingdom.setSpawn(new Location(initialChunk.getWorld(), x, y, z, 0, 0));
                    createPlayer.sendMessage("Chunk claimed and spawn set!");
                } else {
                    createPlayer.sendMessage("Cannot claim this chunk!");
                }

                sender.sendMessage("§aKingdom created: " + kingdomName);
                kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile(), true);
                return true;
            }

            case "invite" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " invite <player>");
                    return true;
                }
                String invitedPlayer = args[1];
                String inviterKingdom = kingdomManager.getKingdomOfPlayer(sender.getName());
                if (inviterKingdom == null) {
                    sender.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom kingdom = kingdomManager.getKingdom(inviterKingdom);
                if (!kingdom.hasPermission(sender.getName(), "invite")) {
                    sender.sendMessage("You don't have permission to invite players!");
                    return true;
                }
                pendingInvites.put(invitedPlayer, inviterKingdom);
                sender.sendMessage("Invited " + invitedPlayer + " to " + inviterKingdom);
                return true;
            }
            case "accept" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can accept invites!");
                    return true;
                }
                String inviteKingdom = pendingInvites.get(sender.getName());
                if (inviteKingdom == null) {
                    sender.sendMessage("You have no pending invites!");
                    return true;
                }
                Kingdom joinKingdom = kingdomManager.getKingdom(inviteKingdom);
                joinKingdom.addMember(sender.getName());
                joinKingdom.setRole(sender.getName(), com.excrele.kingdoms.model.MemberRole.MEMBER); // Default role
                kingdomManager.setPlayerKingdom(sender.getName(), inviteKingdom);
                pendingInvites.remove(sender.getName());
                sender.sendMessage("Joined kingdom: " + inviteKingdom);
                kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                return true;
            }
            case "leave" -> {
                String playerKingdom = kingdomManager.getKingdomOfPlayer(sender.getName());
                if (playerKingdom == null) {
                    sender.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom leaveKingdom = kingdomManager.getKingdom(playerKingdom);
                if (leaveKingdom.getKing().equals(sender.getName())) {
                    sender.sendMessage("The king cannot leave! Use /" + label + " admin dissolve instead.");
                    return true;
                }
                leaveKingdom.getMembers().remove(sender.getName());
                kingdomManager.removePlayerKingdom(sender.getName());
                sender.sendMessage("You have left " + playerKingdom);
                kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                return true;
            }
            case "claim" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can claim chunks!");
                    return true;
                }
                Player claimPlayer = (Player) sender;
                String claimKingdomName = kingdomManager.getKingdomOfPlayer(claimPlayer.getName());
                if (claimKingdomName == null) {
                    claimPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom claimKingdom = kingdomManager.getKingdom(claimKingdomName);
                if (!claimKingdom.hasPermission(claimPlayer.getName(), "claim")) {
                    claimPlayer.sendMessage("You don't have permission to claim chunks!");
                    return true;
                }
                org.bukkit.Location claimLoc = claimPlayer.getLocation();
                if (claimLoc == null) {
                    claimPlayer.sendMessage("§cUnable to get your location!");
                    return true;
                }
                Chunk claimChunk = claimLoc.getChunk();
                
                // Check if radius is provided for bulk claiming
                int radius = 0;
                if (args.length >= 2) {
                    try {
                        radius = Integer.parseInt(args[1]);
                        if (radius < 1 || radius > 10) {
                            claimPlayer.sendMessage("§cRadius must be between 1 and 10!");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        claimPlayer.sendMessage("§cInvalid radius! Use a number between 1 and 10.");
                        return true;
                    }
                }
                
                // Check economy cost
                double claimCost = plugin.getConfig().getDouble("economy.claim_cost", 0.0);
                boolean economyEnabled = plugin.getConfig().getBoolean("economy.enabled", false);
                
                if (radius > 0) {
                    // Bulk claiming
                    if (claimManager == null) {
                        claimPlayer.sendMessage("§cClaim manager not initialized!");
                        return true;
                    }
                    java.util.List<Chunk> chunksToClaim = claimManager.claimChunksInRadius(claimKingdom, claimChunk, radius);
                    
                    if (chunksToClaim.isEmpty()) {
                        claimPlayer.sendMessage("§cNo chunks could be claimed in that radius!");
                        claimPlayer.sendMessage("§7Make sure chunks are adjacent to your existing claims.");
                        return true;
                    }
                    
                    // Calculate total cost
                    double totalCost = claimCost * chunksToClaim.size();
                    if (economyEnabled && claimCost > 0 && com.excrele.kingdoms.util.EconomyManager.isEnabled()) {
                        if (!com.excrele.kingdoms.util.EconomyManager.hasEnough(claimPlayer, totalCost)) {
                            claimPlayer.sendMessage("§cYou don't have enough money! Total cost: " + 
                                com.excrele.kingdoms.util.EconomyManager.format(totalCost) + 
                                " §7(" + chunksToClaim.size() + " chunks × " + 
                                com.excrele.kingdoms.util.EconomyManager.format(claimCost) + ")");
                            // Unclaim the chunks we just claimed
                            for (Chunk chunk : chunksToClaim) {
                                claimManager.unclaimChunk(claimKingdom, chunk);
                            }
                            return true;
                        }
                        com.excrele.kingdoms.util.EconomyManager.withdraw(claimPlayer, totalCost);
                        claimPlayer.sendMessage("§7Paid §e" + com.excrele.kingdoms.util.EconomyManager.format(totalCost) + 
                            " §7to claim " + chunksToClaim.size() + " chunks.");
                    }
                    
                    // Visual effects for each chunk
                    for (Chunk chunk : chunksToClaim) {
                        org.bukkit.Location claimPlayerLoc = claimPlayer.getLocation();
                        if (claimPlayerLoc == null) continue;
                        org.bukkit.block.Block block = chunk.getBlock(8, claimPlayerLoc.getBlockY(), 8);
                        org.bukkit.Location chunkCenter = block.getLocation();
                        com.excrele.kingdoms.util.VisualEffects.playClaimEffects(claimPlayer, chunkCenter);
                    }
                    
                    // Action bar notification
                    com.excrele.kingdoms.util.ActionBarManager.sendClaimNotification(claimPlayer, 
                        "Claimed " + chunksToClaim.size() + " chunks! (" + 
                        claimKingdom.getCurrentClaimChunks() + "/" + claimKingdom.getMaxClaimChunks() + ")");
                    
                    claimPlayer.sendMessage("§aSuccessfully claimed §e" + chunksToClaim.size() + " §achunks!");
                    kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                } else {
                    // Single chunk claiming
                    if (economyEnabled && claimCost > 0 && com.excrele.kingdoms.util.EconomyManager.isEnabled()) {
                        if (!com.excrele.kingdoms.util.EconomyManager.hasEnough(claimPlayer, claimCost)) {
                            claimPlayer.sendMessage("§cYou don't have enough money! Cost: " + 
                                com.excrele.kingdoms.util.EconomyManager.format(claimCost));
                            return true;
                        }
                    }
                    
                    if (claimManager != null && claimManager.claimChunk(claimKingdom, claimChunk)) {
                        // Charge economy cost
                        if (economyEnabled && claimCost > 0 && com.excrele.kingdoms.util.EconomyManager.isEnabled()) {
                            com.excrele.kingdoms.util.EconomyManager.withdraw(claimPlayer, claimCost);
                            claimPlayer.sendMessage("§7Paid §e" + com.excrele.kingdoms.util.EconomyManager.format(claimCost) + " §7to claim this chunk.");
                        }
                        
                        // Visual effects
                        org.bukkit.Location playerLoc2 = claimPlayer.getLocation();
                        if (playerLoc2 != null) {
                            org.bukkit.block.Block block = claimChunk.getBlock(8, playerLoc2.getBlockY(), 8);
                            org.bukkit.Location chunkCenter = block.getLocation();
                            com.excrele.kingdoms.util.VisualEffects.playClaimEffects(claimPlayer, chunkCenter);
                        }
                        
                        // Action bar notification
                        com.excrele.kingdoms.util.ActionBarManager.sendClaimNotification(claimPlayer, 
                            "Chunk claimed! (" + claimKingdom.getCurrentClaimChunks() + "/" + claimKingdom.getMaxClaimChunks() + ")");
                        
                        claimPlayer.sendMessage("§aChunk claimed!");
                        kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                    } else {
                        claimPlayer.sendMessage("§cCannot claim this chunk!");
                        claimPlayer.sendMessage("§7Make sure it's adjacent to your existing claims or this is your first claim.");
                    }
                }
                return true;
            }
            case "unclaim" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can unclaim chunks!");
                    return true;
                }
                Player unclaimPlayer = (Player) sender;
                String unclaimKingdomName = kingdomManager.getKingdomOfPlayer(unclaimPlayer.getName());
                if (unclaimKingdomName == null) {
                    unclaimPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom unclaimKingdom = kingdomManager.getKingdom(unclaimKingdomName);
                if (!unclaimKingdom.hasPermission(unclaimPlayer.getName(), "unclaim")) {
                    unclaimPlayer.sendMessage("You don't have permission to unclaim chunks!");
                    return true;
                }
                org.bukkit.Location unclaimLoc = unclaimPlayer.getLocation();
                if (unclaimLoc == null) {
                    unclaimPlayer.sendMessage("§cUnable to get your location!");
                    return true;
                }
                Chunk unclaimChunk = unclaimLoc.getChunk();
                if (claimManager != null && claimManager.unclaimChunk(unclaimKingdom, unclaimChunk)) {
                    // Refund economy cost
                    double refundAmount = plugin.getConfig().getDouble("economy.unclaim_refund", 0.0);
                    boolean unclaimEconomyEnabled = plugin.getConfig().getBoolean("economy.enabled", false);
                    if (unclaimEconomyEnabled && refundAmount > 0 && com.excrele.kingdoms.util.EconomyManager.isEnabled()) {
                        com.excrele.kingdoms.util.EconomyManager.deposit(unclaimPlayer, refundAmount);
                        unclaimPlayer.sendMessage("§aRefunded §e" + com.excrele.kingdoms.util.EconomyManager.format(refundAmount) + " §afor unclaiming this chunk.");
                    }
                    
                    // Visual effects
                    org.bukkit.Location unclaimPlayerLoc = unclaimPlayer.getLocation();
                    if (unclaimPlayerLoc != null) {
                        org.bukkit.block.Block block = unclaimChunk.getBlock(8, unclaimPlayerLoc.getBlockY(), 8);
                        org.bukkit.Location chunkCenter = block.getLocation();
                        com.excrele.kingdoms.util.VisualEffects.playUnclaimEffects(unclaimPlayer, chunkCenter);
                    }
                    
                    // Action bar notification
                    com.excrele.kingdoms.util.ActionBarManager.sendClaimNotification(unclaimPlayer, 
                        "Chunk unclaimed! (" + unclaimKingdom.getCurrentClaimChunks() + "/" + unclaimKingdom.getMaxClaimChunks() + ")");
                    
                    unclaimPlayer.sendMessage("§cChunk unclaimed!");
                    kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                } else {
                    unclaimPlayer.sendMessage("§cCannot unclaim this chunk!");
                }
                return true;
            }
            case "setplot" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can set plots!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " setplot <type>");
                    return true;
                }
                Player setplotPlayer = (Player) sender;
                String setplotKingdomName = kingdomManager.getKingdomOfPlayer(setplotPlayer.getName());
                if (setplotKingdomName == null) {
                    setplotPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom setplotKingdom = kingdomManager.getKingdom(setplotKingdomName);
                if (!setplotKingdom.hasPermission(setplotPlayer.getName(), "setplottype")) {
                    setplotPlayer.sendMessage("You don't have permission to set plot types!");
                    return true;
                }
                org.bukkit.Location plotLoc = setplotPlayer.getLocation();
                if (plotLoc == null) {
                    setplotPlayer.sendMessage("§cUnable to get your location!");
                    return true;
                }
                Chunk plotChunk = plotLoc.getChunk();
                if (kingdomManager.getKingdomByChunk(plotChunk) != setplotKingdom) {
                    setplotPlayer.sendMessage("This chunk is not claimed by your kingdom!");
                    return true;
                }
                String plotType = args[1];
                setplotKingdom.setPlotType(plotChunk, plotType);
                setplotPlayer.sendMessage("Plot type set to " + plotType);
                kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                return true;
            }
            case "flag" -> {
                if (args.length < 3) {
                    sender.sendMessage("Usage: /" + label + " flag <flag> <value>");
                    return true;
                }
                String flagKingdomName = kingdomManager.getKingdomOfPlayer(sender.getName());
                if (flagKingdomName == null) {
                    sender.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom flagKingdom = kingdomManager.getKingdom(flagKingdomName);
                if (!flagKingdom.hasPermission(sender.getName(), "setflags")) {
                    sender.sendMessage("You don't have permission to set kingdom flags!");
                    return true;
                }
                sender.sendMessage("Kingdom-wide flags are deprecated. Use /" + label + " plotflag instead.");
                return true;
            }
            case "plotflag" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can set plot flags!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("Usage: /" + label + " plotflag <flag> <value>");
                    return true;
                }
                Player plotflagPlayer = (Player) sender;
                String plotflagKingdomName = kingdomManager.getKingdomOfPlayer(plotflagPlayer.getName());
                if (plotflagKingdomName == null) {
                    plotflagPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom plotflagKingdom = kingdomManager.getKingdom(plotflagKingdomName);
                if (!plotflagKingdom.hasPermission(plotflagPlayer.getName(), "setplotflags")) {
                    plotflagPlayer.sendMessage("You don't have permission to set plot flags!");
                    return true;
                }
                org.bukkit.Location flagLoc = plotflagPlayer.getLocation();
                if (flagLoc == null) {
                    plotflagPlayer.sendMessage("§cUnable to get your location!");
                    return true;
                }
                Chunk flagChunk = flagLoc.getChunk();
                if (kingdomManager.getKingdomByChunk(flagChunk) != plotflagKingdom) {
                    plotflagPlayer.sendMessage("This chunk is not claimed by your kingdom!");
                    return true;
                }
                String plotFlag = args[1];
                String plotValue = args[2];
                plugin.getFlagManager().setPlotFlag(plotflagPlayer, plotFlag, plotValue, flagChunk);
                plotflagPlayer.sendMessage("Set plot flag " + plotFlag + " to " + plotValue);
                kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                return true;
            }
            case "setspawn" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can set kingdom spawn!");
                    return true;
                }
                Player setspawnPlayer = (Player) sender;
                String setspawnKingdomName = kingdomManager.getKingdomOfPlayer(setspawnPlayer.getName());
                if (setspawnKingdomName == null) {
                    setspawnPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom setspawnKingdom = kingdomManager.getKingdom(setspawnKingdomName);
                if (!setspawnKingdom.hasPermission(setspawnPlayer.getName(), "setflags")) {
                    setspawnPlayer.sendMessage("You don't have permission to set the spawn!");
                    return true;
                }
                org.bukkit.Location spawnLoc2 = setspawnPlayer.getLocation();
                if (spawnLoc2 == null) {
                    setspawnPlayer.sendMessage("§cUnable to get your location!");
                    return true;
                }
                Chunk spawnChunk = spawnLoc2.getChunk();
                if (kingdomManager.getKingdomByChunk(spawnChunk) != setspawnKingdom) {
                    setspawnPlayer.sendMessage("You must be in a claimed chunk!");
                    return true;
                }
                
                // Check if spawn name is provided
                String setSpawnName = "main";
                if (args.length >= 2) {
                    setSpawnName = args[1].toLowerCase();
                }
                
                setspawnKingdom.addSpawn(setSpawnName, setspawnPlayer.getLocation());
                if (setSpawnName.equals("main")) {
                    setspawnPlayer.sendMessage("§aKingdom spawn set!");
                } else {
                    setspawnPlayer.sendMessage("§aSpawn point '" + setSpawnName + "' set!");
                }
                kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                return true;
            }
            case "spawn" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can teleport to kingdom spawn!");
                    return true;
                }
                Player spawnPlayer = (Player) sender;
                String spawnKingdomName = kingdomManager.getKingdomOfPlayer(spawnPlayer.getName());
                if (spawnKingdomName == null) {
                    spawnPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom spawnKingdom = kingdomManager.getKingdom(spawnKingdomName);
                
                // Check if spawn name is provided
                if (args.length >= 2) {
                    String targetSpawnName = args[1].toLowerCase();
                    Location targetSpawn = spawnKingdom.getSpawn(targetSpawnName);
                    if (targetSpawn == null) {
                        spawnPlayer.sendMessage("§cSpawn point '" + targetSpawnName + "' not found!");
                        spawnPlayer.sendMessage("§7Use §e/kingdom spawn list §7to see available spawn points.");
                        return true;
                    }
                    if (targetSpawn.getWorld() == null) {
                        spawnPlayer.sendMessage("§cSpawn point is in an invalid world!");
                        return true;
                    }
                    spawnPlayer.teleport(targetSpawn);
                    spawnPlayer.sendMessage("§aTeleported to spawn point: §e" + targetSpawnName);
                    return true;
                }
                
                // List spawns if "list" is specified or show menu
                Location spawn = spawnKingdom.getSpawn();
                if (spawn == null) {
                    spawnPlayer.sendMessage("§cNo spawn set for this kingdom!");
                    spawnPlayer.sendMessage("§7Use §e/kingdom setspawn [name] §7to set a spawn point.");
                    return true;
                }
                if (spawn.getWorld() == null) {
                    spawnPlayer.sendMessage("§cSpawn is in an invalid world!");
                    return true;
                }
                
                // If multiple spawns exist, list them
                if (spawnKingdom.getSpawns().size() > 1) {
                    spawnPlayer.sendMessage("§6=== Available Spawn Points ===");
                    for (String listSpawnName : spawnKingdom.getSpawns().keySet()) {
                        Location spawnLoc = spawnKingdom.getSpawn(listSpawnName);
                        if (spawnLoc != null) {
                            org.bukkit.World spawnWorld = spawnLoc.getWorld();
                            if (spawnWorld != null) {
                                spawnPlayer.sendMessage("§7- §e" + listSpawnName + " §7(World: §e" + 
                                    spawnWorld.getName() + "§7, X: §e" + (int)spawnLoc.getX() + 
                                    "§7, Z: §e" + (int)spawnLoc.getZ() + "§7)");
                            }
                        }
                    }
                    spawnPlayer.sendMessage("§7Use §e/kingdom spawn <name> §7to teleport to a specific spawn.");
                    return true;
                }
                
                // Single spawn - just teleport
                spawnPlayer.teleport(spawn);
                spawnPlayer.sendMessage("§aTeleported to kingdom spawn!");
                return true;
            }
            case "challenges" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can view challenges!");
                    return true;
                }
                Player challengePlayer = (Player) sender;
                String challengeKingdomName = kingdomManager.getKingdomOfPlayer(challengePlayer.getName());
                if (challengeKingdomName == null) {
                    challengePlayer.sendMessage("You must be in a kingdom to view challenges!");
                    return true;
                }
                if (args.length > 1 && args[1].equalsIgnoreCase("gui")) {
                    ChallengeGUI.openChallengeGUI(challengePlayer);
                } else {
                    challengePlayer.sendMessage("Available Challenges:");
                    com.excrele.kingdoms.manager.ChallengeManager challengeManager = plugin.getChallengeManager();
                    if (challengeManager != null) {
                        for (Challenge challenge : challengeManager.getChallenges()) {
                            if (challenge == null) continue;
                            boolean onCooldown = challengeManager.isChallengeOnCooldown(challengePlayer, challenge);
                            String description = challenge.getDescription();
                            if (description == null) description = "Unknown";
                            challengePlayer.sendMessage(String.format(
                                    "- %s (Difficulty: %d, XP: %d) %s",
                                    description,
                                    challenge.getDifficulty(),
                                    challenge.getXpReward(),
                                    onCooldown ? "[On Cooldown]" : ""
                            ));
                        }
                    }
                }
                return true;
            }
            case "gui" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can open the kingdom GUI!");
                    return true;
                }
                KingdomManagementGUI.openKingdomGUI((Player) sender);
                return true;
            }
            case "map" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can view the claim map!");
                    return true;
                }
                Player mapPlayer = (Player) sender;
                String mapKingdomName = kingdomManager.getKingdomOfPlayer(mapPlayer.getName());
                if (mapKingdomName == null) {
                    mapPlayer.sendMessage("You must be in a kingdom to view the claim map!");
                    return true;
                }
                // Check if they want GUI or text version
                if (args.length > 1 && args[1].equalsIgnoreCase("gui")) {
                    com.excrele.kingdoms.gui.ClaimMapGUI.openClaimMapGUI(mapPlayer);
                } else {
                    mapPlayer.sendMessage(ClaimMapGenerator.generateClaimMap(mapPlayer));
                    mapPlayer.sendMessage("§7Tip: Use §e/kingdom map gui §7for an interactive map!");
                }
                return true;
            }
            case "contributions", "contribs" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can view contributions!");
                    return true;
                }
                Player contribPlayer = (Player) sender;
                String contribKingdomName = kingdomManager.getKingdomOfPlayer(contribPlayer.getName());
                if (contribKingdomName == null) {
                    contribPlayer.sendMessage("You must be in a kingdom to view contributions!");
                    return true;
                }
                Kingdom contribKingdom = kingdomManager.getKingdom(contribKingdomName);
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
            }
            case "stats" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can view kingdom stats!");
                    return true;
                }
                Player statsPlayer = (Player) sender;
                String statsKingdomName = kingdomManager.getKingdomOfPlayer(statsPlayer.getName());
                if (statsKingdomName == null) {
                    statsPlayer.sendMessage("You must be in a kingdom to view stats!");
                    return true;
                }
                Kingdom statsKingdom = kingdomManager.getKingdom(statsKingdomName);
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
            }
            case "promote" -> {
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
                String promoteKingdomName = kingdomManager.getKingdomOfPlayer(promotePlayer.getName());
                if (promoteKingdomName == null) {
                    promotePlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom promoteKingdom = kingdomManager.getKingdom(promoteKingdomName);
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
                    kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                } catch (IllegalArgumentException e) {
                    promotePlayer.sendMessage("Invalid role! Use: ADVISOR, GUARD, BUILDER, or MEMBER");
                }
                return true;
            }
            case "kick" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " kick <player>");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can kick members!");
                    return true;
                }
                Player kickPlayer = (Player) sender;
                String kickKingdomName = kingdomManager.getKingdomOfPlayer(kickPlayer.getName());
                if (kickKingdomName == null) {
                    kickPlayer.sendMessage("You are not in a kingdom!");
                    return true;
                }
                Kingdom kickKingdom = kingdomManager.getKingdom(kickKingdomName);
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
                kingdomManager.removePlayerKingdom(kickTarget);
                
                // Record history
                if (plugin.getStatisticsManager() != null) {
                    plugin.getStatisticsManager().addHistoryEntry(kickKingdomName, 
                        com.excrele.kingdoms.model.KingdomHistory.HistoryType.MEMBER_KICKED, 
                        kickTarget + " was kicked from the kingdom", kickPlayer.getName());
                }
                
                kickPlayer.sendMessage("§aKicked " + kickTarget + " from the kingdom");
                kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                return true;
            }
            case "member", "members" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " member <title|permission|note> [args]");
                    sender.sendMessage("§7/kingdom member title <player> [title] [color] - Set member title");
                    sender.sendMessage("§7/kingdom member permission <player> <permission> <allow|deny|remove> - Manage permissions");
                    sender.sendMessage("§7/kingdom member note <player> [note] - Set or view member note");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can manage members!");
                    return true;
                }
                Player memberPlayer = (Player) sender;
                String memberKingdomName = kingdomManager.getKingdomOfPlayer(memberPlayer.getName());
                if (memberKingdomName == null) {
                    memberPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom memberKingdom = kingdomManager.getKingdom(memberKingdomName);
                if (memberKingdom == null) {
                    memberPlayer.sendMessage("Your kingdom was not found!");
                    return true;
                }
                
                String memberSub = args[1].toLowerCase();
                switch (memberSub) {
                    case "title" -> {
                        if (args.length < 3) {
                            memberPlayer.sendMessage("Usage: /" + label + " member title <player> [title] [color]");
                            memberPlayer.sendMessage("§7Use 'clear' as title to remove it");
                            return true;
                        }
                        if (!memberKingdom.hasPermission(memberPlayer.getName(), "promote")) {
                            memberPlayer.sendMessage("§cYou don't have permission to set member titles!");
                            return true;
                        }
                        String titleTarget = args[2];
                        if (!memberKingdom.getMembers().contains(titleTarget) && !memberKingdom.getKing().equals(titleTarget)) {
                            memberPlayer.sendMessage("§cPlayer is not in your kingdom!");
                            return true;
                        }
                        if (args.length >= 4 && !args[3].equalsIgnoreCase("clear")) {
                            String title = args[3];
                            String color = args.length >= 5 ? args[4] : "§7";
                            if (plugin.getAdvancedMemberManager().setMemberTitle(memberKingdomName, titleTarget, title, color, memberPlayer.getName())) {
                                memberPlayer.sendMessage("§aSet title for " + titleTarget + ": " + color + title);
                            } else {
                                memberPlayer.sendMessage("§cFailed to set title! (Title too long or invalid)");
                            }
                        } else if (args.length >= 4 && args[3].equalsIgnoreCase("clear")) {
                            if (plugin.getAdvancedMemberManager().removeMemberTitle(memberKingdomName, titleTarget)) {
                                memberPlayer.sendMessage("§aRemoved title from " + titleTarget);
                            } else {
                                memberPlayer.sendMessage("§cNo title to remove!");
                            }
                        } else {
                            com.excrele.kingdoms.model.MemberTitle title = plugin.getAdvancedMemberManager().getMemberTitle(memberKingdomName, titleTarget);
                            if (title != null) {
                                memberPlayer.sendMessage("§6" + titleTarget + "'s Title: " + title.getFormattedTitle());
                            } else {
                                memberPlayer.sendMessage("§7" + titleTarget + " has no custom title");
                            }
                        }
                        return true;
                    }
                    case "permission", "perm" -> {
                        if (args.length < 4) {
                            memberPlayer.sendMessage("Usage: /" + label + " member permission <player> <permission> <allow|deny|remove>");
                            memberPlayer.sendMessage("§7Permissions: invite, claim, unclaim, setflags, setplotflags, setplottype, kick");
                            return true;
                        }
                        if (!memberKingdom.hasPermission(memberPlayer.getName(), "promote")) {
                            memberPlayer.sendMessage("§cYou don't have permission to manage member permissions!");
                            return true;
                        }
                        String permTarget = args[2];
                        if (!memberKingdom.getMembers().contains(permTarget) && !memberKingdom.getKing().equals(permTarget)) {
                            memberPlayer.sendMessage("§cPlayer is not in your kingdom!");
                            return true;
                        }
                        String permission = args[3].toLowerCase();
                        String action = args.length >= 5 ? args[4].toLowerCase() : "allow";
                        switch (action) {
                            case "allow" -> {
                                if (plugin.getAdvancedMemberManager().setMemberPermission(memberKingdomName, permTarget, permission, true, memberPlayer.getName())) {
                                    memberPlayer.sendMessage("§aAllowed permission '" + permission + "' for " + permTarget);
                                } else {
                                    memberPlayer.sendMessage("§cFailed to set permission!");
                                }
                            }
                            case "deny" -> {
                                if (plugin.getAdvancedMemberManager().setMemberPermission(memberKingdomName, permTarget, permission, false, memberPlayer.getName())) {
                                    memberPlayer.sendMessage("§cDenied permission '" + permission + "' for " + permTarget);
                                } else {
                                    memberPlayer.sendMessage("§cFailed to set permission!");
                                }
                            }
                            case "remove" -> {
                                if (plugin.getAdvancedMemberManager().removeMemberPermission(memberKingdomName, permTarget, permission)) {
                                    memberPlayer.sendMessage("§aRemoved permission override '" + permission + "' for " + permTarget);
                                } else {
                                    memberPlayer.sendMessage("§cNo permission override to remove!");
                                }
                            }
                            default -> memberPlayer.sendMessage("§cInvalid action! Use: allow, deny, or remove");
                        }
                        return true;
                    }
                    case "note" -> {
                        if (args.length < 3) {
                            memberPlayer.sendMessage("Usage: /" + label + " member note <player> [note]");
                            memberPlayer.sendMessage("§7Use 'clear' as note to remove it");
                            return true;
                        }
                        if (!memberKingdom.hasPermission(memberPlayer.getName(), "promote")) {
                            memberPlayer.sendMessage("§cYou don't have permission to manage member notes!");
                            return true;
                        }
                        String noteTarget = args[2];
                        if (!memberKingdom.getMembers().contains(noteTarget) && !memberKingdom.getKing().equals(noteTarget)) {
                            memberPlayer.sendMessage("§cPlayer is not in your kingdom!");
                            return true;
                        }
                        if (args.length >= 4 && !args[3].equalsIgnoreCase("clear")) {
                            String note = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                            if (plugin.getAdvancedMemberManager().setMemberNote(memberKingdomName, noteTarget, note, memberPlayer.getName())) {
                                memberPlayer.sendMessage("§aSet note for " + noteTarget);
                            } else {
                                memberPlayer.sendMessage("§cFailed to set note! (Note too long or invalid)");
                            }
                        } else if (args.length >= 4 && args[3].equalsIgnoreCase("clear")) {
                            if (plugin.getAdvancedMemberManager().removeMemberNote(memberKingdomName, noteTarget)) {
                                memberPlayer.sendMessage("§aRemoved note from " + noteTarget);
                            } else {
                                memberPlayer.sendMessage("§cNo note to remove!");
                            }
                        } else {
                            com.excrele.kingdoms.model.MemberNote note = plugin.getAdvancedMemberManager().getMemberNote(memberKingdomName, noteTarget);
                            if (note != null) {
                                memberPlayer.sendMessage("§6=== Note for " + noteTarget + " ===");
                                memberPlayer.sendMessage("§7" + note.getNote());
                                memberPlayer.sendMessage("§7Author: §e" + note.getAuthor());
                                memberPlayer.sendMessage("§7Last Modified: §e" + (System.currentTimeMillis() / 1000 - note.getLastModified()) / 86400 + " days ago");
                            } else {
                                memberPlayer.sendMessage("§7No note set for " + noteTarget);
                            }
                        }
                        return true;
                    }
                    default -> {
                        memberPlayer.sendMessage("Usage: /" + label + " member <title|permission|note> [args]");
                        return true;
                    }
                }
            }
            case "chat", "c" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use kingdom chat!");
                    return true;
                }
                Player chatPlayer = (Player) sender;
                String chatKingdomName = kingdomManager.getKingdomOfPlayer(chatPlayer.getName());
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
            }
            case "leaderboard", "lb" -> {
                String lbType = args.length > 1 ? args[1].toLowerCase() : "level";
                sender.sendMessage("§6=== Kingdom Leaderboard (" + lbType.toUpperCase() + ") ===");
                
                List<Kingdom> sortedKingdoms = new ArrayList<>(kingdomManager.getKingdoms().values());
                
                switch (lbType) {
                    case "level" -> {
                        sortedKingdoms.sort((a, b) -> {
                            int levelCompare = Integer.compare(b.getLevel(), a.getLevel());
                            if (levelCompare != 0) return levelCompare;
                            return Integer.compare(b.getXp(), a.getXp());
                        });
                    }
                    case "xp" -> {
                        sortedKingdoms.sort((a, b) -> {
                            int xpCompare = Integer.compare(b.getXp(), a.getXp());
                            if (xpCompare != 0) return xpCompare;
                            return Integer.compare(b.getLevel(), a.getLevel());
                        });
                    }
                    case "members" -> {
                        sortedKingdoms.sort((a, b) -> Integer.compare(
                            b.getMembers().size() + 1, // +1 for king
                            a.getMembers().size() + 1
                        ));
                    }
                    case "challenges" -> {
                        sortedKingdoms.sort((a, b) -> Integer.compare(
                            b.getTotalChallengesCompleted(),
                            a.getTotalChallengesCompleted()
                        ));
                    }
                    default -> {
                        sender.sendMessage("§cInvalid leaderboard type! Use: level, xp, members, or challenges");
                        return true;
                    }
                }
                
                int rank = 1;
                for (Kingdom k : sortedKingdoms) {
                    if (rank > 10) break; // Show top 10
                    String entry = "§7" + rank + ". §e" + k.getName();
                    switch (lbType) {
                        case "level" -> entry += " §7- Level §e" + k.getLevel() + " §7(§e" + k.getXp() + " XP§7)";
                        case "xp" -> entry += " §7- §e" + k.getXp() + " XP §7(Level §e" + k.getLevel() + "§7)";
                        case "members" -> entry += " §7- §e" + (k.getMembers().size() + 1) + " members";
                        case "challenges" -> entry += " §7- §e" + k.getTotalChallengesCompleted() + " challenges";
                    }
                    sender.sendMessage(entry);
                    rank++;
                }
                
                if (sortedKingdoms.isEmpty()) {
                    sender.sendMessage("§7No kingdoms found!");
                }
                return true;
            }
            case "alliance" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " alliance <invite|accept|deny|list|remove> [kingdom]");
                    return true;
                }
                String allianceSub = args[1].toLowerCase();
                switch (allianceSub) {
                    case "invite" -> {
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /" + label + " alliance invite <kingdom>");
                            return true;
                        }
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can invite to alliances!");
                            return true;
                        }
                        Player allianceInviter = (Player) sender;
                        String allianceInviterKingdom = kingdomManager.getKingdomOfPlayer(allianceInviter.getName());
                        if (allianceInviterKingdom == null) {
                            allianceInviter.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom allianceInviterK = kingdomManager.getKingdom(allianceInviterKingdom);
                        if (allianceInviterK == null) {
                            allianceInviter.sendMessage("Your kingdom was not found!");
                            return true;
                        }
                        if (!allianceInviterK.hasPermission(allianceInviter.getName(), "setflags")) {
                            allianceInviter.sendMessage("You don't have permission to manage alliances!");
                            return true;
                        }
                        String targetKingdom = args[2];
                        Kingdom targetK = kingdomManager.getKingdom(targetKingdom);
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
                    }
                    case "accept" -> {
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /" + label + " alliance accept <kingdom>");
                            return true;
                        }
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can accept alliance requests!");
                            return true;
                        }
                        Player allianceAccepter = (Player) sender;
                        String allianceAccepterKingdom = kingdomManager.getKingdomOfPlayer(allianceAccepter.getName());
                        if (allianceAccepterKingdom == null) {
                            allianceAccepter.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom allianceAccepterK = kingdomManager.getKingdom(allianceAccepterKingdom);
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
                        Kingdom requestingK = kingdomManager.getKingdom(requestingKingdom);
                        if (requestingK != null) {
                            requestingK.addAlliance(allianceAccepterKingdom);
                            pendingAllianceRequests.remove(allianceAccepterKingdom);
                            
                            // Visual effects
                            com.excrele.kingdoms.util.VisualEffects.playAllianceEffects(allianceAccepter);
                            
                            // Action bar notification
                            com.excrele.kingdoms.util.ActionBarManager.sendNotification(allianceAccepter, 
                                "§a§l✓ Alliance formed with " + requestingKingdom + "!");
                            
                            allianceAccepter.sendMessage("§aAlliance formed with " + requestingKingdom + "!");
                            Player requestingKing = plugin.getServer().getPlayer(requestingK.getKing());
                            if (requestingKing != null && requestingKing.isOnline()) {
                                com.excrele.kingdoms.util.VisualEffects.playAllianceEffects(requestingKing);
                                com.excrele.kingdoms.util.ActionBarManager.sendNotification(requestingKing, 
                                    "§a§l✓ " + allianceAccepterKingdom + " accepted your alliance request!");
                                requestingKing.sendMessage("§a" + allianceAccepterKingdom + " accepted your alliance request!");
                            }
                        } else {
                            allianceAccepter.sendMessage("§cError: Kingdom not found!");
                        }
                        kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                        return true;
                    }
                    case "deny" -> {
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /" + label + " alliance deny <kingdom>");
                            return true;
                        }
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can deny alliance requests!");
                            return true;
                        }
                        Player allianceDenier = (Player) sender;
                        String allianceDenierKingdom = kingdomManager.getKingdomOfPlayer(allianceDenier.getName());
                        if (allianceDenierKingdom == null) {
                            allianceDenier.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom allianceDenierK = kingdomManager.getKingdom(allianceDenierKingdom);
                        if (!allianceDenierK.hasPermission(allianceDenier.getName(), "setflags")) {
                            allianceDenier.sendMessage("You don't have permission to manage alliances!");
                            return true;
                        }
                        String deniedKingdom = args[2];
                        pendingAllianceRequests.remove(allianceDenierKingdom);
                        allianceDenier.sendMessage("§cAlliance request from " + deniedKingdom + " denied.");
                        return true;
                    }
                    case "list" -> {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can view alliances!");
                            return true;
                        }
                        Player allianceViewer = (Player) sender;
                        String allianceViewerKingdom = kingdomManager.getKingdomOfPlayer(allianceViewer.getName());
                        if (allianceViewerKingdom == null) {
                            allianceViewer.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom allianceViewerK = kingdomManager.getKingdom(allianceViewerKingdom);
                        allianceViewer.sendMessage("§6=== Alliances ===");
                        if (allianceViewerK.getAlliances().isEmpty()) {
                            allianceViewer.sendMessage("§7No alliances");
                        } else {
                            for (String ally : allianceViewerK.getAlliances()) {
                                allianceViewer.sendMessage("§7- §e" + ally);
                            }
                        }
                        return true;
                    }
                    case "remove" -> {
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /" + label + " alliance remove <kingdom>");
                            return true;
                        }
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can remove alliances!");
                            return true;
                        }
                        Player allianceRemover = (Player) sender;
                        String allianceRemoverKingdom = kingdomManager.getKingdomOfPlayer(allianceRemover.getName());
                        if (allianceRemoverKingdom == null) {
                            allianceRemover.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom allianceRemoverK = kingdomManager.getKingdom(allianceRemoverKingdom);
                        if (allianceRemoverK == null) {
                            allianceRemover.sendMessage("Your kingdom was not found!");
                            return true;
                        }
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
                        Kingdom removeK = kingdomManager.getKingdom(removeKingdom);
                        if (removeK != null) {
                            removeK.removeAlliance(allianceRemoverKingdom);
                        }
                        allianceRemover.sendMessage("§cAlliance with " + removeKingdom + " dissolved.");
                        kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                        return true;
                    }
                    default -> {
                        sender.sendMessage("Usage: /" + label + " alliance <invite|accept|deny|list|remove> [kingdom]");
                        return true;
                    }
                }
            }
            case "trust" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " trust <player> [permission]");
                    sender.sendMessage("Permissions: build, interact, use, redstone, container, teleport, fly, enderpearl, chorus_fruit, piston, animal_breed, crop_trample, all");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can manage trusts!");
                    return true;
                }
                Player trustPlayer = (Player) sender;
                String trustKingdomName = kingdomManager.getKingdomOfPlayer(trustPlayer.getName());
                if (trustKingdomName == null) {
                    trustPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom trustKingdom = kingdomManager.getKingdom(trustKingdomName);
                if (trustKingdom == null) {
                    trustPlayer.sendMessage("Your kingdom was not found!");
                    return true;
                }
                if (!trustKingdom.hasPermission(trustPlayer.getName(), "setflags")) {
                    trustPlayer.sendMessage("You don't have permission to manage trusts!");
                    return true;
                }
                String trustedPlayer = args[1];
                if (args.length >= 3) {
                    String permissionStr = args[2];
                    com.excrele.kingdoms.model.TrustPermission permission = com.excrele.kingdoms.model.TrustPermission.fromKey(permissionStr);
                    if (permission == null) {
                        trustPlayer.sendMessage("§cInvalid permission! Use: build, interact, use, redstone, container, teleport, fly, enderpearl, chorus_fruit, piston, animal_breed, crop_trample, all");
                        return true;
                    }
                    plugin.getTrustManager().trustPlayer(trustKingdomName, trustedPlayer, permission);
                    trustPlayer.sendMessage("§aTrusted " + trustedPlayer + " with permission: " + permission.getDescription());
                } else {
                    // Trust with all permissions
                    plugin.getTrustManager().trustPlayer(trustKingdomName, trustedPlayer, com.excrele.kingdoms.model.TrustPermission.ALL);
                    trustPlayer.sendMessage("§aTrusted " + trustedPlayer + " with all permissions");
                }
                return true;
            }
            case "untrust" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " untrust <player>");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can manage trusts!");
                    return true;
                }
                Player untrustPlayer = (Player) sender;
                String untrustKingdomName = kingdomManager.getKingdomOfPlayer(untrustPlayer.getName());
                if (untrustKingdomName == null) {
                    untrustPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom untrustKingdom = kingdomManager.getKingdom(untrustKingdomName);
                if (untrustKingdom == null) {
                    untrustPlayer.sendMessage("Your kingdom was not found!");
                    return true;
                }
                if (!untrustKingdom.hasPermission(untrustPlayer.getName(), "setflags")) {
                    untrustPlayer.sendMessage("You don't have permission to manage trusts!");
                    return true;
                }
                String untrustedPlayer = args[1];
                plugin.getTrustManager().untrustPlayer(untrustKingdomName, untrustedPlayer);
                untrustPlayer.sendMessage("§cRemoved trust from " + untrustedPlayer);
                return true;
            }
            case "trustlist", "trusts" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can view trusts!");
                    return true;
                }
                Player trustListPlayer = (Player) sender;
                String trustListKingdomName = kingdomManager.getKingdomOfPlayer(trustListPlayer.getName());
                if (trustListKingdomName == null) {
                    trustListPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom trustListKingdom = kingdomManager.getKingdom(trustListKingdomName);
                if (trustListKingdom == null) {
                    trustListPlayer.sendMessage("Your kingdom was not found!");
                    return true;
                }
                if (!trustListKingdom.hasPermission(trustListPlayer.getName(), "setflags")) {
                    trustListPlayer.sendMessage("You don't have permission to view trusts!");
                    return true;
                }
                com.excrele.kingdoms.manager.TrustManager trustManager = plugin.getTrustManager();
                if (trustManager == null) {
                    trustListPlayer.sendMessage("§cTrust manager not available!");
                    return true;
                }
                java.util.Map<String, java.util.Set<com.excrele.kingdoms.model.TrustPermission>> trusts = trustManager.getAllTrusts(trustListKingdomName);
                trustListPlayer.sendMessage("§6=== Trusted Players ===");
                if (trusts == null || trusts.isEmpty()) {
                    trustListPlayer.sendMessage("§7No trusted players");
                } else {
                    for (java.util.Map.Entry<String, java.util.Set<com.excrele.kingdoms.model.TrustPermission>> entry : trusts.entrySet()) {
                        if (entry.getKey() == null || entry.getValue() == null) continue;
                        java.util.List<String> permNames = new java.util.ArrayList<>();
                        for (com.excrele.kingdoms.model.TrustPermission perm : entry.getValue()) {
                            if (perm != null) {
                                permNames.add(perm.getKey());
                            }
                        }
                        trustListPlayer.sendMessage("§7- §e" + entry.getKey() + "§7: §a" + String.join(", ", permNames));
                    }
                }
                return true;
            }
            case "war" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " war <declare|end|list|status> [kingdom]");
                    return true;
                }
                String warSub = args[1].toLowerCase();
                switch (warSub) {
                    case "declare" -> {
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /" + label + " war declare <kingdom> [duration_hours]");
                            return true;
                        }
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can declare war!");
                            return true;
                        }
                        Player warDeclarer = (Player) sender;
                        String warDeclarerKingdom = kingdomManager.getKingdomOfPlayer(warDeclarer.getName());
                        if (warDeclarerKingdom == null) {
                            warDeclarer.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom warDeclarerK = kingdomManager.getKingdom(warDeclarerKingdom);
                        if (!warDeclarerK.hasPermission(warDeclarer.getName(), "setflags")) {
                            warDeclarer.sendMessage("You don't have permission to declare war!");
                            return true;
                        }
                        String targetWarKingdom = args[2];
                        Kingdom targetWarK = kingdomManager.getKingdom(targetWarKingdom);
                        if (targetWarK == null) {
                            warDeclarer.sendMessage("Kingdom not found!");
                            return true;
                        }
                        if (targetWarKingdom.equals(warDeclarerKingdom)) {
                            warDeclarer.sendMessage("§cYou cannot declare war on yourself!");
                            return true;
                        }
                        long duration = 24 * 3600; // Default 24 hours
                        if (args.length >= 4) {
                            try {
                                duration = Long.parseLong(args[3]) * 3600; // Convert hours to seconds
                            } catch (NumberFormatException e) {
                                warDeclarer.sendMessage("§cInvalid duration! Use a number of hours.");
                                return true;
                            }
                        }
                        if (plugin.getWarManager().declareWar(warDeclarerKingdom, targetWarKingdom, duration)) {
                            warDeclarer.sendMessage("§c§l⚔ War declared on " + targetWarKingdom + "! Duration: " + (duration / 3600) + " hours");
                            // Notify target kingdom
                            Player targetKing = plugin.getServer().getPlayer(targetWarK.getKing());
                            if (targetKing != null && targetKing.isOnline()) {
                                targetKing.sendMessage("§c§l⚔ " + warDeclarerKingdom + " has declared war on your kingdom!");
                                targetKing.sendMessage("§7War duration: " + (duration / 3600) + " hours");
                            }
                        } else {
                            warDeclarer.sendMessage("§cCannot declare war! You may already be at war with this kingdom.");
                        }
                        return true;
                    }
                    case "end" -> {
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /" + label + " war end <kingdom>");
                            return true;
                        }
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can end wars!");
                            return true;
                        }
                        Player warEnder = (Player) sender;
                        String warEnderKingdom = kingdomManager.getKingdomOfPlayer(warEnder.getName());
                        if (warEnderKingdom == null) {
                            warEnder.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom warEnderK = kingdomManager.getKingdom(warEnderKingdom);
                        if (warEnderK == null) {
                            warEnder.sendMessage("Your kingdom was not found!");
                            return true;
                        }
                        if (!warEnderK.hasPermission(warEnder.getName(), "setflags")) {
                            warEnder.sendMessage("You don't have permission to end wars!");
                            return true;
                        }
                        String endWarKingdom = args[2];
                        com.excrele.kingdoms.model.War war = plugin.getWarManager().getWar(warEnderKingdom, endWarKingdom);
                        if (war == null) {
                            warEnder.sendMessage("§cNo active war found with " + endWarKingdom);
                            return true;
                        }
                        plugin.getWarManager().endWar(war.getWarId());
                        warEnder.sendMessage("§aWar with " + endWarKingdom + " has ended!");
                        return true;
                    }
                    case "list" -> {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can view wars!");
                            return true;
                        }
                        Player warListPlayer = (Player) sender;
                        String warListKingdom = kingdomManager.getKingdomOfPlayer(warListPlayer.getName());
                        if (warListKingdom == null) {
                            warListPlayer.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        java.util.List<com.excrele.kingdoms.model.War> activeWars = plugin.getWarManager().getActiveWars(warListKingdom);
                        warListPlayer.sendMessage("§6=== Active Wars ===");
                        if (activeWars.isEmpty()) {
                            warListPlayer.sendMessage("§7No active wars");
                        } else {
                            for (com.excrele.kingdoms.model.War w : activeWars) {
                                String enemy = w.getKingdom1().equals(warListKingdom) ? w.getKingdom2() : w.getKingdom1();
                                long remaining = w.getEndTime() - (System.currentTimeMillis() / 1000);
                                long hours = remaining / 3600;
                                long minutes = (remaining % 3600) / 60;
                                warListPlayer.sendMessage("§c⚔ War with §e" + enemy + "§7 - Time remaining: §e" + hours + "h " + minutes + "m");
                            }
                        }
                        return true;
                    }
                    case "status" -> {
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /" + label + " war status <kingdom>");
                            return true;
                        }
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Only players can check war status!");
                            return true;
                        }
                        Player warStatusPlayer = (Player) sender;
                        String warStatusKingdom = kingdomManager.getKingdomOfPlayer(warStatusPlayer.getName());
                        if (warStatusKingdom == null) {
                            warStatusPlayer.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        String statusTargetKingdom = args[2];
                        com.excrele.kingdoms.model.War statusWar = plugin.getWarManager().getWar(warStatusKingdom, statusTargetKingdom);
                        if (statusWar == null) {
                            warStatusPlayer.sendMessage("§7No active war with " + statusTargetKingdom);
                        } else {
                            warStatusPlayer.sendMessage("§6=== War Status ===");
                            warStatusPlayer.sendMessage("§7Enemy: §e" + statusTargetKingdom);
                            warStatusPlayer.sendMessage("§7Your Score: §e" + (statusWar.getKingdom1().equals(warStatusKingdom) ? statusWar.getKingdom1Score() : statusWar.getKingdom2Score()));
                            warStatusPlayer.sendMessage("§7Enemy Score: §e" + (statusWar.getKingdom1().equals(warStatusKingdom) ? statusWar.getKingdom2Score() : statusWar.getKingdom1Score()));
                            long remaining = statusWar.getEndTime() - (System.currentTimeMillis() / 1000);
                            long hours = remaining / 3600;
                            long minutes = (remaining % 3600) / 60;
                            warStatusPlayer.sendMessage("§7Time Remaining: §e" + hours + "h " + minutes + "m");
                        }
                        return true;
                    }
                    default -> {
                        sender.sendMessage("Usage: /" + label + " war <declare|end|list|status> [kingdom]");
                        return true;
                    }
                }
            }
            case "bank" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " bank <deposit|withdraw|balance|transfer> [amount] [kingdom]");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use the bank!");
                    return true;
                }
                Player bankPlayer = (Player) sender;
                String bankKingdomName = kingdomManager.getKingdomOfPlayer(bankPlayer.getName());
                if (bankKingdomName == null) {
                    bankPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                String bankSub = args[1].toLowerCase();
                switch (bankSub) {
                    case "deposit" -> {
                        if (args.length < 3) {
                            bankPlayer.sendMessage("Usage: /" + label + " bank deposit <amount>");
                            return true;
                        }
                        try {
                            double depositAmount = Double.parseDouble(args[2]);
                            if (depositAmount <= 0) {
                                bankPlayer.sendMessage("§cAmount must be positive!");
                                return true;
                            }
                            if (plugin.getBankManager().depositFromPlayer(bankPlayer, bankKingdomName, depositAmount)) {
                                bankPlayer.sendMessage("§aDeposited §e" + com.excrele.kingdoms.util.EconomyManager.format(depositAmount) + " §ato kingdom bank");
                                bankPlayer.sendMessage("§7New balance: §e" + com.excrele.kingdoms.util.EconomyManager.format(plugin.getBankManager().getBalance(bankKingdomName)));
                            } else {
                                bankPlayer.sendMessage("§cFailed to deposit! Check your balance and economy setup.");
                            }
                        } catch (NumberFormatException e) {
                            bankPlayer.sendMessage("§cInvalid amount!");
                        }
                        return true;
                    }
                    case "withdraw" -> {
                        if (args.length < 3) {
                            bankPlayer.sendMessage("Usage: /" + label + " bank withdraw <amount>");
                            return true;
                        }
                        Kingdom bankKingdom = kingdomManager.getKingdom(bankKingdomName);
                        if (!bankKingdom.hasPermission(bankPlayer.getName(), "setflags")) {
                            bankPlayer.sendMessage("You don't have permission to withdraw from the bank!");
                            return true;
                        }
                        try {
                            double withdrawAmount = Double.parseDouble(args[2]);
                            if (withdrawAmount <= 0) {
                                bankPlayer.sendMessage("§cAmount must be positive!");
                                return true;
                            }
                            if (plugin.getBankManager().withdrawToPlayer(bankPlayer, bankKingdomName, withdrawAmount)) {
                                bankPlayer.sendMessage("§aWithdrew §e" + com.excrele.kingdoms.util.EconomyManager.format(withdrawAmount) + " §afrom kingdom bank");
                                bankPlayer.sendMessage("§7New balance: §e" + com.excrele.kingdoms.util.EconomyManager.format(plugin.getBankManager().getBalance(bankKingdomName)));
                            } else {
                                bankPlayer.sendMessage("§cFailed to withdraw! Check bank balance.");
                            }
                        } catch (NumberFormatException e) {
                            bankPlayer.sendMessage("§cInvalid amount!");
                        }
                        return true;
                    }
                    case "balance" -> {
                        double balance = plugin.getBankManager().getBalance(bankKingdomName);
                        bankPlayer.sendMessage("§6=== Kingdom Bank ===");
                        bankPlayer.sendMessage("§7Balance: §e" + com.excrele.kingdoms.util.EconomyManager.format(balance));
                        return true;
                    }
                    case "transfer" -> {
                        if (args.length < 4) {
                            bankPlayer.sendMessage("Usage: /" + label + " bank transfer <kingdom> <amount>");
                            return true;
                        }
                        Kingdom transferKingdom = kingdomManager.getKingdom(bankKingdomName);
                        if (!transferKingdom.hasPermission(bankPlayer.getName(), "setflags")) {
                            bankPlayer.sendMessage("You don't have permission to transfer money!");
                            return true;
                        }
                        String targetTransferKingdom = args[2];
                        Kingdom targetTransferK = kingdomManager.getKingdom(targetTransferKingdom);
                        if (targetTransferK == null) {
                            bankPlayer.sendMessage("Kingdom not found!");
                            return true;
                        }
                        try {
                            double transferAmount = Double.parseDouble(args[3]);
                            if (transferAmount <= 0) {
                                bankPlayer.sendMessage("§cAmount must be positive!");
                                return true;
                            }
                            if (plugin.getBankManager().transfer(bankKingdomName, targetTransferKingdom, transferAmount)) {
                                bankPlayer.sendMessage("§aTransferred §e" + com.excrele.kingdoms.util.EconomyManager.format(transferAmount) + " §ato " + targetTransferKingdom);
                            } else {
                                bankPlayer.sendMessage("§cFailed to transfer! Check bank balance.");
                            }
                        } catch (NumberFormatException e) {
                            bankPlayer.sendMessage("§cInvalid amount!");
                        }
                        return true;
                    }
                    default -> {
                        bankPlayer.sendMessage("Usage: /" + label + " bank <deposit|withdraw|balance|transfer> [amount] [kingdom]");
                        return true;
                    }
                }
            }
            case "admin" -> {
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
                    case "list" -> {
                        sender.sendMessage("Kingdoms: " + String.join(", ", kingdomManager.getKingdoms().keySet()));
                        return true;
                    }
                    case "dissolve" -> {
                        if (args.length < 3) {
                            sender.sendMessage("Usage: /" + label + " admin dissolve <kingdom>");
                            return true;
                        }
                        String dissolveKingdom = args[2];
                        Kingdom toDissolve = kingdomManager.getKingdom(dissolveKingdom);
                        if (toDissolve == null) {
                            sender.sendMessage("Kingdom not found!");
                            return true;
                        }
                        kingdomManager.dissolveKingdom(dissolveKingdom);
                        sender.sendMessage("Kingdom " + dissolveKingdom + " dissolved!");
                        kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                        return true;
                    }
                    case "forceunclaim" -> {
                        if (args.length < 4) {
                            sender.sendMessage("Usage: /" + label + " admin forceunclaim <kingdom> <world:x:z>");
                            return true;
                        }
                        String forceKingdomName = args[2];
                        Kingdom forceKingdom = kingdomManager.getKingdom(forceKingdomName);
                        if (forceKingdom == null) {
                            sender.sendMessage("Kingdom not found!");
                            return true;
                        }
                        String[] coords = args[3].split(":");
                        org.bukkit.World forceWorld = plugin.getServer().getWorld(coords[0]);
                        if (forceWorld == null) {
                            sender.sendMessage("§cWorld not found: " + coords[0]);
                            return true;
                        }
                        Chunk forceChunk = forceWorld.getChunkAt(Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
                        if (claimManager != null && claimManager.unclaimChunk(forceKingdom, forceChunk)) {
                            sender.sendMessage("Chunk force unclaimed!");
                            kingdomManager.saveKingdoms(plugin.getKingdomsConfig(), plugin.getKingdomsFile());
                        } else {
                            sender.sendMessage("Failed to unclaim chunk!");
                        }
                        return true;
                    }
                    case "setflag" -> {
                        if (args.length < 5) {
                            sender.sendMessage("Usage: /" + label + " admin setflag <kingdom> <flag> <value>");
                            return true;
                        }
                        String adminFlagKingdomName = args[2];
                        Kingdom adminFlagKingdom = kingdomManager.getKingdom(adminFlagKingdomName);
                        if (adminFlagKingdom == null) {
                            sender.sendMessage("Kingdom not found!");
                            return true;
                        }
                        sender.sendMessage("Kingdom-wide flags are deprecated. Use per-chunk flags instead.");
                        return true;
                    }
                }
                return true;
            }
            case "statistics", "analytics" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can view statistics!");
                    return true;
                }
                Player statsPlayer2 = (Player) sender;
                String statsKingdomName2 = kingdomManager.getKingdomOfPlayer(statsPlayer2.getName());
                if (statsKingdomName2 == null) {
                    statsPlayer2.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom statsKingdom2 = kingdomManager.getKingdom(statsKingdomName2);
                if (statsKingdom2 == null) return true;
                
                if (args.length >= 2 && args[1].equalsIgnoreCase("health")) {
                    double healthScore = plugin.getStatisticsManager().calculateKingdomHealthScore(statsKingdomName2);
                    statsPlayer2.sendMessage("§6=== Kingdom Health Score ===");
                    statsPlayer2.sendMessage("§7Overall Health: §e" + String.format("%.1f", healthScore) + "/100");
                    statsPlayer2.sendMessage("§7Status: " + (healthScore >= 80 ? "§aExcellent" : healthScore >= 60 ? "§eGood" : healthScore >= 40 ? "§6Fair" : "§cPoor"));
                    return true;
                }
                
                if (args.length >= 2 && args[1].equalsIgnoreCase("member") && args.length >= 3) {
                    String analyticsTargetPlayer = args[2];
                    java.util.Map<String, Object> analytics = plugin.getStatisticsManager().getMemberAnalytics(statsKingdomName2, analyticsTargetPlayer);
                    statsPlayer2.sendMessage("§6=== Member Analytics: " + analyticsTargetPlayer + " ===");
                    for (java.util.Map.Entry<String, Object> entry : analytics.entrySet()) {
                        statsPlayer2.sendMessage("§7" + entry.getKey() + ": §e" + entry.getValue());
                    }
                    return true;
                }
                
                if (args.length >= 2 && args[1].equalsIgnoreCase("heatmap")) {
                    java.util.Map<Chunk, Double> heatmap = plugin.getStatisticsManager().generateActivityHeatmap(statsKingdomName2);
                    statsPlayer2.sendMessage("§6=== Activity Heatmap ===");
                    statsPlayer2.sendMessage("§7Most Active Claims:");
                    heatmap.entrySet().stream()
                        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                        .limit(10)
                        .forEach(entry -> {
                            statsPlayer2.sendMessage("§7Chunk (" + entry.getKey().getX() + ", " + entry.getKey().getZ() + "): §e" + String.format("%.2f", entry.getValue()));
                        });
                    return true;
                }
                
                // Default statistics view
                statsPlayer2.sendMessage("§6=== Kingdom Statistics ===");
                statsPlayer2.sendMessage("§7Use §e/kingdom stats health §7for health score");
                statsPlayer2.sendMessage("§7Use §e/kingdom stats member <player> §7for member analytics");
                statsPlayer2.sendMessage("§7Use §e/kingdom stats heatmap §7for activity heatmap");
                return true;
            }
            case "autoclaim" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use auto-claim!");
                    return true;
                }
                Player autoClaimPlayer = (Player) sender;
                String autoClaimKingdomName = kingdomManager.getKingdomOfPlayer(autoClaimPlayer.getName());
                if (autoClaimKingdomName == null) {
                    autoClaimPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom autoClaimKingdom = kingdomManager.getKingdom(autoClaimKingdomName);
                if (autoClaimKingdom == null) return true;
                
                if (!autoClaimKingdom.hasPermission(autoClaimPlayer.getName(), "claim")) {
                    autoClaimPlayer.sendMessage("§cYou don't have permission to claim!");
                    return true;
                }
                
                boolean enabled = plugin.getAdvancedFeaturesManager().toggleAutoClaim(autoClaimPlayer);
                if (enabled) {
                    autoClaimPlayer.sendMessage("§aAuto-claim enabled! Chunks will be claimed automatically as you walk (with cooldown).");
                } else {
                    autoClaimPlayer.sendMessage("§cAuto-claim disabled.");
                }
                return true;
            }
            case "waypoint", "wp" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use waypoints!");
                    return true;
                }
                Player wpPlayer = (Player) sender;
                String wpKingdomName = kingdomManager.getKingdomOfPlayer(wpPlayer.getName());
                if (wpKingdomName == null) {
                    wpPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom wpKingdom = kingdomManager.getKingdom(wpKingdomName);
                if (wpKingdom == null) return true;
                
                if (args.length < 2) {
                    wpPlayer.sendMessage("Usage: /" + label + " waypoint <create|list|delete|tp> [name]");
                    return true;
                }
                
                String wpSub = args[1].toLowerCase();
                switch (wpSub) {
                    case "create" -> {
                        if (args.length < 3) {
                            wpPlayer.sendMessage("Usage: /" + label + " waypoint create <name>");
                            return true;
                        }
                        String wpName = args[2];
                        org.bukkit.Location wpLoc = wpPlayer.getLocation();
                        if (plugin.getAdvancedFeaturesManager().createWaypoint(wpKingdomName, wpName, wpLoc, wpPlayer.getName())) {
                            wpPlayer.sendMessage("§aWaypoint '" + wpName + "' created!");
                        } else {
                            wpPlayer.sendMessage("§cWaypoint already exists!");
                        }
                        return true;
                    }
                    case "list" -> {
                        java.util.Map<String, com.excrele.kingdoms.model.Waypoint> waypoints = plugin.getAdvancedFeaturesManager().getWaypoints(wpKingdomName);
                        wpPlayer.sendMessage("§6=== Waypoints ===");
                        if (waypoints.isEmpty()) {
                            wpPlayer.sendMessage("§7No waypoints set.");
                        } else {
                            for (com.excrele.kingdoms.model.Waypoint wp : waypoints.values()) {
                                wpPlayer.sendMessage("§7- §e" + wp.getName() + " §7(" + wp.getLocation().getBlockX() + ", " + wp.getLocation().getBlockY() + ", " + wp.getLocation().getBlockZ() + ")");
                            }
                        }
                        return true;
                    }
                    case "delete", "remove" -> {
                        if (args.length < 3) {
                            wpPlayer.sendMessage("Usage: /" + label + " waypoint delete <name>");
                            return true;
                        }
                        String deleteWpName = args[2];
                        if (plugin.getAdvancedFeaturesManager().deleteWaypoint(wpKingdomName, deleteWpName)) {
                            wpPlayer.sendMessage("§aWaypoint deleted!");
                        } else {
                            wpPlayer.sendMessage("§cWaypoint not found!");
                        }
                        return true;
                    }
                    case "tp", "teleport" -> {
                        if (args.length < 3) {
                            wpPlayer.sendMessage("Usage: /" + label + " waypoint tp <name>");
                            return true;
                        }
                        String tpWpName = args[2];
                        com.excrele.kingdoms.model.Waypoint tpWp = plugin.getAdvancedFeaturesManager().getWaypoint(wpKingdomName, tpWpName);
                        if (tpWp != null) {
                            wpPlayer.teleport(tpWp.getLocation());
                            wpPlayer.sendMessage("§aTeleported to waypoint '" + tpWpName + "'!");
                        } else {
                            wpPlayer.sendMessage("§cWaypoint not found!");
                        }
                        return true;
                    }
                    default -> {
                        wpPlayer.sendMessage("Usage: /" + label + " waypoint <create|list|delete|tp> [name]");
                        return true;
                    }
                }
            }
            case "farm" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use farms!");
                    return true;
                }
                Player farmPlayer = (Player) sender;
                String farmKingdomName = kingdomManager.getKingdomOfPlayer(farmPlayer.getName());
                if (farmKingdomName == null) {
                    farmPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom farmKingdom = kingdomManager.getKingdom(farmKingdomName);
                if (farmKingdom == null) return true;
                
                if (args.length < 2) {
                    farmPlayer.sendMessage("Usage: /" + label + " farm <create|list|harvest|info> [name] [type]");
                    return true;
                }
                
                String farmSub = args[1].toLowerCase();
                switch (farmSub) {
                    case "create" -> {
                        if (args.length < 4) {
                            farmPlayer.sendMessage("Usage: /" + label + " farm create <name> <type>");
                            farmPlayer.sendMessage("§7Types: CROP, ANIMAL, TREE, FISH, MINERAL");
                            return true;
                        }
                        String farmName = args[2];
                        try {
                            com.excrele.kingdoms.model.KingdomFarm.FarmType farmType = 
                                com.excrele.kingdoms.model.KingdomFarm.FarmType.valueOf(args[3].toUpperCase());
                            org.bukkit.Location farmCenter = farmPlayer.getLocation();
                            if (farmCenter == null) {
                                farmPlayer.sendMessage("§cUnable to get your location!");
                                return true;
                            }
                            Chunk farmChunk = farmCenter.getChunk();
                            if (plugin.getAdvancedFeaturesManager() != null && 
                                plugin.getAdvancedFeaturesManager().createFarm(farmKingdomName, farmName, farmChunk, farmCenter, farmType)) {
                                farmPlayer.sendMessage("§aFarm '" + farmName + "' created!");
                            } else {
                                farmPlayer.sendMessage("§cFarm already exists!");
                            }
                        } catch (IllegalArgumentException e) {
                            farmPlayer.sendMessage("§cInvalid farm type! Use: CROP, ANIMAL, TREE, FISH, MINERAL");
                        }
                        return true;
                    }
                    case "list" -> {
                        java.util.Map<String, com.excrele.kingdoms.model.KingdomFarm> farms = plugin.getAdvancedFeaturesManager().getFarms(farmKingdomName);
                        farmPlayer.sendMessage("§6=== Farms ===");
                        if (farms.isEmpty()) {
                            farmPlayer.sendMessage("§7No farms created.");
                        } else {
                            for (com.excrele.kingdoms.model.KingdomFarm farm : farms.values()) {
                                farmPlayer.sendMessage("§7- §e" + farm.getName() + " §7(" + farm.getType() + ")");
                            }
                        }
                        return true;
                    }
                    case "harvest" -> {
                        if (args.length < 3) {
                            farmPlayer.sendMessage("Usage: /" + label + " farm harvest <name>");
                            return true;
                        }
                        String harvestFarmName = args[2];
                        com.excrele.kingdoms.model.KingdomFarm harvestFarm = plugin.getAdvancedFeaturesManager().getFarm(farmKingdomName, harvestFarmName);
                        if (harvestFarm != null && harvestFarm.canHarvest()) {
                            farmPlayer.sendMessage("§aFarm harvested! Resources added to kingdom vault.");
                            harvestFarm.setLastHarvest(System.currentTimeMillis() / 1000);
                        } else {
                            farmPlayer.sendMessage("§cFarm not found or not ready to harvest!");
                        }
                        return true;
                    }
                    case "info" -> {
                        if (args.length < 3) {
                            farmPlayer.sendMessage("Usage: /" + label + " farm info <name>");
                            return true;
                        }
                        String infoFarmName = args[2];
                        com.excrele.kingdoms.model.KingdomFarm infoFarm = plugin.getAdvancedFeaturesManager().getFarm(farmKingdomName, infoFarmName);
                        if (infoFarm != null) {
                            farmPlayer.sendMessage("§6=== Farm: " + infoFarmName + " ===");
                            farmPlayer.sendMessage("§7Type: §e" + infoFarm.getType());
                            farmPlayer.sendMessage("§7Status: §e" + (infoFarm.isActive() ? "Active" : "Inactive"));
                            long timeUntilHarvest = infoFarm.getHarvestInterval() - ((System.currentTimeMillis() / 1000) - infoFarm.getLastHarvest());
                            farmPlayer.sendMessage("§7Time until harvest: §e" + (timeUntilHarvest > 0 ? timeUntilHarvest + " seconds" : "Ready"));
                        } else {
                            farmPlayer.sendMessage("§cFarm not found!");
                        }
                        return true;
                    }
                    default -> {
                        farmPlayer.sendMessage("Usage: /" + label + " farm <create|list|harvest|info> [name] [type]");
                        return true;
                    }
                }
            }
            case "workshop" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use workshops!");
                    return true;
                }
                Player workshopPlayer = (Player) sender;
                String workshopKingdomName = kingdomManager.getKingdomOfPlayer(workshopPlayer.getName());
                if (workshopKingdomName == null) {
                    workshopPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom workshopKingdom = kingdomManager.getKingdom(workshopKingdomName);
                if (workshopKingdom == null) return true;
                
                if (args.length < 2) {
                    workshopPlayer.sendMessage("Usage: /" + label + " workshop <create|list|info> [name] [type]");
                    return true;
                }
                
                String workshopSub = args[1].toLowerCase();
                switch (workshopSub) {
                    case "create" -> {
                        if (args.length < 4) {
                            workshopPlayer.sendMessage("Usage: /" + label + " workshop create <name> <type>");
                            workshopPlayer.sendMessage("§7Types: CRAFTING, SMITHING, ENCHANTING, BREWING, COOKING");
                            return true;
                        }
                        String workshopName = args[2];
                        try {
                            com.excrele.kingdoms.model.KingdomWorkshop.WorkshopType workshopType = 
                                com.excrele.kingdoms.model.KingdomWorkshop.WorkshopType.valueOf(args[3].toUpperCase());
                            org.bukkit.Location workshopLoc = workshopPlayer.getLocation();
                            if (workshopLoc == null) {
                                workshopPlayer.sendMessage("§cUnable to get your location!");
                                return true;
                            }
                            Chunk workshopChunk = workshopLoc.getChunk();
                            if (plugin.getAdvancedFeaturesManager() != null && 
                                plugin.getAdvancedFeaturesManager().createWorkshop(workshopKingdomName, workshopName, workshopChunk, workshopLoc, workshopType)) {
                                workshopPlayer.sendMessage("§aWorkshop '" + workshopName + "' created!");
                            } else {
                                workshopPlayer.sendMessage("§cWorkshop already exists!");
                            }
                        } catch (IllegalArgumentException e) {
                            workshopPlayer.sendMessage("§cInvalid workshop type! Use: CRAFTING, SMITHING, ENCHANTING, BREWING, COOKING");
                        }
                        return true;
                    }
                    case "list" -> {
                        workshopPlayer.sendMessage("§6=== Workshops ===");
                        workshopPlayer.sendMessage("§7Use §e/kingdom workshop info <name> §7for details");
                        return true;
                    }
                    case "info" -> {
                        if (args.length < 3) {
                            workshopPlayer.sendMessage("Usage: /" + label + " workshop info <name>");
                            return true;
                        }
                        String infoWorkshopName = args[2];
                        com.excrele.kingdoms.model.KingdomWorkshop infoWorkshop = plugin.getAdvancedFeaturesManager().getWorkshop(workshopKingdomName, infoWorkshopName);
                        if (infoWorkshop != null) {
                            workshopPlayer.sendMessage("§6=== Workshop: " + infoWorkshopName + " ===");
                            workshopPlayer.sendMessage("§7Type: §e" + infoWorkshop.getType());
                            workshopPlayer.sendMessage("§7Bonus: §e" + String.format("%.0f%%", (infoWorkshop.getBonusMultiplier() - 1.0) * 100));
                            workshopPlayer.sendMessage("§7Status: §e" + (infoWorkshop.isActive() ? "Active" : "Inactive"));
                        } else {
                            workshopPlayer.sendMessage("§cWorkshop not found!");
                        }
                        return true;
                    }
                    default -> {
                        workshopPlayer.sendMessage("Usage: /" + label + " workshop <create|list|info> [name] [type]");
                        return true;
                    }
                }
            }
            case "library" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use libraries!");
                    return true;
                }
                Player libraryPlayer = (Player) sender;
                String libraryKingdomName = kingdomManager.getKingdomOfPlayer(libraryPlayer.getName());
                if (libraryKingdomName == null) {
                    libraryPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom libraryKingdom = kingdomManager.getKingdom(libraryKingdomName);
                if (libraryKingdom == null) return true;
                
                if (args.length < 2) {
                    libraryPlayer.sendMessage("Usage: /" + label + " library <create|list|open> [name]");
                    return true;
                }
                
                String librarySub = args[1].toLowerCase();
                switch (librarySub) {
                    case "create" -> {
                        if (args.length < 3) {
                            libraryPlayer.sendMessage("Usage: /" + label + " library create <name>");
                            return true;
                        }
                        String libraryName = args[2];
                        org.bukkit.Location libraryLoc = libraryPlayer.getLocation();
                        if (libraryLoc == null) {
                            libraryPlayer.sendMessage("§cUnable to get your location!");
                            return true;
                        }
                        Chunk libraryChunk = libraryLoc.getChunk();
                        if (plugin.getAdvancedFeaturesManager() != null && 
                            plugin.getAdvancedFeaturesManager().createLibrary(libraryKingdomName, libraryName, libraryChunk, libraryLoc)) {
                            libraryPlayer.sendMessage("§aLibrary '" + libraryName + "' created!");
                        } else {
                            libraryPlayer.sendMessage("§cLibrary already exists!");
                        }
                        return true;
                    }
                    case "list" -> {
                        libraryPlayer.sendMessage("§6=== Libraries ===");
                        libraryPlayer.sendMessage("§7Use §e/kingdom library open <name> §7to access");
                        return true;
                    }
                    case "open" -> {
                        if (args.length < 3) {
                            libraryPlayer.sendMessage("Usage: /" + label + " library open <name>");
                            return true;
                        }
                        String openLibraryName = args[2];
                        com.excrele.kingdoms.model.KingdomLibrary library = plugin.getAdvancedFeaturesManager().getLibrary(libraryKingdomName, openLibraryName);
                        if (library != null) {
                            libraryPlayer.sendMessage("§6=== Library: " + openLibraryName + " ===");
                            libraryPlayer.sendMessage("§7Books: §e" + library.getBooks().size() + "/" + library.getMaxBooks());
                            libraryPlayer.sendMessage("§7Enchantments: §e" + library.getEnchantments().size());
                            // TODO: Open library GUI
                        } else {
                            libraryPlayer.sendMessage("§cLibrary not found!");
                        }
                        return true;
                    }
                    default -> {
                        libraryPlayer.sendMessage("Usage: /" + label + " library <create|list|open> [name]");
                        return true;
                    }
                }
            }
            case "stable" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use stables!");
                    return true;
                }
                Player stablePlayer = (Player) sender;
                String stableKingdomName = kingdomManager.getKingdomOfPlayer(stablePlayer.getName());
                if (stableKingdomName == null) {
                    stablePlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom stableKingdom = kingdomManager.getKingdom(stableKingdomName);
                if (stableKingdom == null) return true;
                
                if (args.length < 2) {
                    stablePlayer.sendMessage("Usage: /" + label + " stable <create|list|open> [name]");
                    return true;
                }
                
                String stableSub = args[1].toLowerCase();
                switch (stableSub) {
                    case "create" -> {
                        if (args.length < 3) {
                            stablePlayer.sendMessage("Usage: /" + label + " stable create <name>");
                            return true;
                        }
                        String stableName = args[2];
                        org.bukkit.Location stableLoc = stablePlayer.getLocation();
                        if (stableLoc == null) {
                            stablePlayer.sendMessage("§cUnable to get your location!");
                            return true;
                        }
                        Chunk stableChunk = stableLoc.getChunk();
                        if (plugin.getAdvancedFeaturesManager() != null && 
                            plugin.getAdvancedFeaturesManager().createStable(stableKingdomName, stableName, stableChunk, stableLoc)) {
                            stablePlayer.sendMessage("§aStable '" + stableName + "' created!");
                        } else {
                            stablePlayer.sendMessage("§cStable already exists!");
                        }
                        return true;
                    }
                    case "list" -> {
                        stablePlayer.sendMessage("§6=== Stables ===");
                        stablePlayer.sendMessage("§7Use §e/kingdom stable open <name> §7to access");
                        return true;
                    }
                    case "open" -> {
                        if (args.length < 3) {
                            stablePlayer.sendMessage("Usage: /" + label + " stable open <name>");
                            return true;
                        }
                        String openStableName = args[2];
                        com.excrele.kingdoms.model.KingdomStable stable = plugin.getAdvancedFeaturesManager().getStable(stableKingdomName, openStableName);
                        if (stable != null) {
                            stablePlayer.sendMessage("§6=== Stable: " + openStableName + " ===");
                            stablePlayer.sendMessage("§7Mounts: §e" + stable.getStoredMounts().size() + "/" + stable.getMaxMounts());
                            // TODO: Open stable GUI
                        } else {
                            stablePlayer.sendMessage("§cStable not found!");
                        }
                        return true;
                    }
                    default -> {
                        stablePlayer.sendMessage("Usage: /" + label + " stable <create|list|open> [name]");
                        return true;
                    }
                }
            }
            case "help" -> {
                int page = 1;
                if (args.length >= 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        // Invalid page number, use default
                    }
                }
                
                sender.sendMessage("§6=== Kingdom Commands Help (Page " + page + ") ===");
                
                switch (page) {
                    case 1 -> {
                    sender.sendMessage("§eBasic Commands:");
                    sender.sendMessage("§7/kingdom create <name> §f- Create a new kingdom");
                    sender.sendMessage("§7/kingdom invite <player> §f- Invite a player");
                    sender.sendMessage("§7/kingdom accept §f- Accept an invitation");
                    sender.sendMessage("§7/kingdom leave §f- Leave your kingdom");
                    sender.sendMessage("§7/kingdom stats §f- View kingdom statistics");
                    sender.sendMessage("§7/kingdom gui §f- Open kingdom management GUI");
                    sender.sendMessage("§7/kingdom map §f- View claim map");
                        sender.sendMessage("§eUse §7/kingdom help 2 §efor more commands");
                    }
                    case 2 -> {
                        sender.sendMessage("§eClaiming Commands:");
                        sender.sendMessage("§7/kingdom claim [radius] §f- Claim current chunk or radius");
                        sender.sendMessage("§7  §7Example: §e/kingdom claim 3 §7claims chunks in 3-chunk radius");
                        sender.sendMessage("§7/kingdom unclaim §f- Unclaim current chunk");
                        sender.sendMessage("§7/kingdom setplot <type> §f- Set plot type");
                        sender.sendMessage("§7/kingdom plotflag <flag> <value> §f- Set plot flag");
                        sender.sendMessage("§7/kingdom setspawn [name] §f- Set kingdom spawn");
                        sender.sendMessage("§7/kingdom spawn [name] §f- Teleport to spawn");
                        sender.sendMessage("§eUse §7/kingdom help 3 §efor more commands");
                    }
                    case 3 -> {
                        sender.sendMessage("§eMember Management:");
                        sender.sendMessage("§7/kingdom promote <player> <role> §f- Promote member");
                        sender.sendMessage("§7/kingdom kick <player> §f- Kick member");
                        sender.sendMessage("§7/kingdom contributions §f- View contributions");
                        sender.sendMessage("§7/kingdom leaderboard [type] §f- View leaderboard");
                        sender.sendMessage("§eRoles: §7ADVISOR, GUARD, BUILDER, MEMBER");
                        sender.sendMessage("§eUse §7/kingdom help 4 §efor more commands");
                    }
                    case 4 -> {
                        sender.sendMessage("§eTrust & Permissions:");
                        sender.sendMessage("§7/kingdom trust <player> [permission] §f- Trust player");
                        sender.sendMessage("§7/kingdom untrust <player> §f- Remove trust");
                        sender.sendMessage("§7/kingdom trustlist §f- List trusted players");
                        sender.sendMessage("§ePermissions: §7build, interact, use, redstone, container, etc.");
                        sender.sendMessage("§eUse §7/kingdom help 5 §efor more commands");
                    }
                    case 5 -> {
                        sender.sendMessage("§eAlliances & Wars:");
                        sender.sendMessage("§7/kingdom alliance invite <kingdom> §f- Invite to alliance");
                        sender.sendMessage("§7/kingdom alliance accept <kingdom> §f- Accept alliance");
                        sender.sendMessage("§7/kingdom alliance list §f- List alliances");
                        sender.sendMessage("§7/kingdom war declare <kingdom> [hours] §f- Declare war");
                        sender.sendMessage("§7/kingdom war end <kingdom> §f- End war");
                        sender.sendMessage("§7/kingdom war list §f- List active wars");
                        sender.sendMessage("§eUse §7/kingdom help 6 §efor more commands");
                    }
                    case 6 -> {
                        sender.sendMessage("§eBank & Economy:");
                        sender.sendMessage("§7/kingdom bank deposit <amount> §f- Deposit money");
                        sender.sendMessage("§7/kingdom bank withdraw <amount> §f- Withdraw money");
                        sender.sendMessage("§7/kingdom bank balance §f- Check balance");
                        sender.sendMessage("§7/kingdom bank transfer <kingdom> <amount> §f- Transfer");
                        sender.sendMessage("§eClaim Economy:");
                        sender.sendMessage("§7/kingdom claimeconomy sell <price> §f- Sell current claim");
                        sender.sendMessage("§7/kingdom claimeconomy buy §f- Buy current claim");
                        sender.sendMessage("§7/kingdom claimeconomy auction start <bid> <hours> §f- Start auction");
                        sender.sendMessage("§7/kingdom claimeconomy auction bid <amount> §f- Place bid");
                        sender.sendMessage("§7/kingdom claimeconomy rent <rate> <days> §f- Rent claim");
                        sender.sendMessage("§7/kingdom claimeconomy value §f- Calculate claim value");
                        sender.sendMessage("§eOther Commands:");
                        sender.sendMessage("§7/kingdom vault §f- Open kingdom vault");
                        sender.sendMessage("§7/kingdom activity §f- View your activity");
                        sender.sendMessage("§7/kingdom challenges §f- View challenges");
                        sender.sendMessage("§7/kingdom chat §f- Toggle kingdom chat");
                        sender.sendMessage("§7/kc <message> §f- Send kingdom chat message");
                    }
                    default -> sender.sendMessage("§cInvalid page number! Use 1-6");
                }
                return true;
            }
            case "claimeconomy", "claimeco" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " claimeconomy <sell|buy|auction|rent|value> [args]");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use claim economy!");
                    return true;
                }
                Player claimEcoPlayer = (Player) sender;
                String claimEcoKingdomName = kingdomManager.getKingdomOfPlayer(claimEcoPlayer.getName());
                if (claimEcoKingdomName == null) {
                    claimEcoPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom claimEcoKingdom = kingdomManager.getKingdom(claimEcoKingdomName);
                org.bukkit.Location claimEcoLoc = claimEcoPlayer.getLocation();
                if (claimEcoLoc == null) {
                    claimEcoPlayer.sendMessage("§cUnable to get your location!");
                    return true;
                }
                Chunk currentEcoChunk = claimEcoLoc.getChunk();
                String claimEcoSub = args[1].toLowerCase();
                switch (claimEcoSub) {
                    case "sell" -> {
                        if (args.length < 3) {
                            claimEcoPlayer.sendMessage("Usage: /" + label + " claimeconomy sell <price>");
                            return true;
                        }
                        try {
                            double price = Double.parseDouble(args[2]);
                            if (price <= 0) {
                                claimEcoPlayer.sendMessage("§cPrice must be positive!");
                                return true;
                            }
                            if (plugin.getClaimEconomyManager().listClaimForSale(claimEcoKingdom, currentEcoChunk, price)) {
                                claimEcoPlayer.sendMessage("§aClaim listed for sale at §e" + com.excrele.kingdoms.util.EconomyManager.format(price));
                            } else {
                                claimEcoPlayer.sendMessage("§cFailed to list claim! Make sure you own it and it's not already listed.");
                            }
                        } catch (NumberFormatException e) {
                            claimEcoPlayer.sendMessage("§cInvalid price!");
                        }
                        return true;
                    }
                    case "buy" -> {
                        com.excrele.kingdoms.model.ClaimSale sale = plugin.getClaimEconomyManager().getSale(currentEcoChunk);
                        if (sale == null) {
                            claimEcoPlayer.sendMessage("§cThis claim is not for sale!");
                            return true;
                        }
                        if (plugin.getClaimEconomyManager().buyClaim(claimEcoKingdom, currentEcoChunk)) {
                            claimEcoPlayer.sendMessage("§aClaim purchased successfully!");
                        } else {
                            claimEcoPlayer.sendMessage("§cFailed to purchase claim! Check your bank balance.");
                        }
                        return true;
                    }
                    case "auction" -> {
                        if (args.length < 3) {
                            claimEcoPlayer.sendMessage("Usage: /" + label + " claimeconomy auction <start|bid|cancel> [args]");
                            return true;
                        }
                        String auctionSub = args[2].toLowerCase();
                        switch (auctionSub) {
                            case "start" -> {
                                if (args.length < 5) {
                                    claimEcoPlayer.sendMessage("Usage: /" + label + " claimeconomy auction start <starting_bid> <duration_hours>");
                                    return true;
                                }
                                try {
                                    double startingBid = Double.parseDouble(args[3]);
                                    long durationHours = Long.parseLong(args[4]);
                                    if (plugin.getClaimEconomyManager().createAuction(claimEcoKingdom, currentEcoChunk, startingBid, durationHours * 3600)) {
                                        claimEcoPlayer.sendMessage("§aAuction created! Starting bid: §e" + com.excrele.kingdoms.util.EconomyManager.format(startingBid));
                                    } else {
                                        claimEcoPlayer.sendMessage("§cFailed to create auction!");
                                    }
                                } catch (NumberFormatException e) {
                                    claimEcoPlayer.sendMessage("§cInvalid amount or duration!");
                                }
                                return true;
                            }
                            case "bid" -> {
                                if (args.length < 4) {
                                    claimEcoPlayer.sendMessage("Usage: /" + label + " claimeconomy auction bid <amount>");
                                    return true;
                                }
                                try {
                                    double bidAmount = Double.parseDouble(args[3]);
                                    if (plugin.getClaimEconomyManager().placeBid(claimEcoKingdom, currentEcoChunk, bidAmount)) {
                                        claimEcoPlayer.sendMessage("§aBid placed: §e" + com.excrele.kingdoms.util.EconomyManager.format(bidAmount));
                                    } else {
                                        claimEcoPlayer.sendMessage("§cFailed to place bid!");
                                    }
                                } catch (NumberFormatException e) {
                                    claimEcoPlayer.sendMessage("§cInvalid bid amount!");
                                }
                                return true;
                            }
                            case "cancel" -> {
                                if (plugin.getClaimEconomyManager().cancelAuction(claimEcoKingdom, currentEcoChunk)) {
                                    claimEcoPlayer.sendMessage("§aAuction cancelled!");
                                } else {
                                    claimEcoPlayer.sendMessage("§cFailed to cancel auction!");
                                }
                                return true;
                            }
                        }
                        return true;
                    }
                    case "rent" -> {
                        if (args.length < 4) {
                            claimEcoPlayer.sendMessage("Usage: /" + label + " claimeconomy rent <daily_rate> <days>");
                            return true;
                        }
                        try {
                            double dailyRate = Double.parseDouble(args[2]);
                            long days = Long.parseLong(args[3]);
                            if (plugin.getClaimEconomyManager().rentClaim(claimEcoKingdom, currentEcoChunk, dailyRate, days)) {
                                claimEcoPlayer.sendMessage("§aClaim rented for §e" + days + " §adays at §e" + com.excrele.kingdoms.util.EconomyManager.format(dailyRate) + " §aper day");
                            } else {
                                claimEcoPlayer.sendMessage("§cFailed to rent claim!");
                            }
                        } catch (NumberFormatException e) {
                            claimEcoPlayer.sendMessage("§cInvalid rate or days!");
                        }
                        return true;
                    }
                    case "value" -> {
                        double value = plugin.getClaimEconomyManager().calculateClaimValue(currentEcoChunk);
                        claimEcoPlayer.sendMessage("§6Claim Value: §e" + com.excrele.kingdoms.util.EconomyManager.format(value));
                        return true;
                    }
                    default -> {
                        claimEcoPlayer.sendMessage("Usage: /" + label + " claimeconomy <sell|buy|auction|rent|value> [args]");
                        return true;
                    }
                }
            }
            case "vault" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use vaults!");
                    return true;
                }
                Player vaultPlayer = (Player) sender;
                String vaultKingdomName = kingdomManager.getKingdomOfPlayer(vaultPlayer.getName());
                if (vaultKingdomName == null) {
                    vaultPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom vaultKingdom = kingdomManager.getKingdom(vaultKingdomName);
                if (vaultKingdom == null) {
                    vaultPlayer.sendMessage("Your kingdom was not found!");
                    return true;
                }
                plugin.getVaultManager().getVault(vaultKingdom);
                new com.excrele.kingdoms.gui.VaultGUI(plugin, plugin.getVaultManager()).openVault(vaultPlayer, vaultKingdom);
                return true;
            }
            case "activity" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can view activity!");
                    return true;
                }
                Player activityPlayer = (Player) sender;
                String activityKingdomName = kingdomManager.getKingdomOfPlayer(activityPlayer.getName());
                if (activityKingdomName == null) {
                    activityPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                com.excrele.kingdoms.model.PlayerActivity activity = plugin.getActivityManager().getActivity(activityPlayer.getName());
                if (activity != null) {
                    activityPlayer.sendMessage("§6=== Your Activity ===");
                    activityPlayer.sendMessage("§7Last Login: §e" + (activity.getDaysSinceLastLogin() == 0 ? "Today" : activity.getDaysSinceLastLogin() + " days ago"));
                    activityPlayer.sendMessage("§7Total Playtime: §e" + (activity.getTotalPlaytime() / 3600) + " hours");
                    activityPlayer.sendMessage("§7Contributions: §e" + activity.getContributions());
                } else {
                    activityPlayer.sendMessage("§cNo activity data found!");
                }
                return true;
            }
            case "announcement", "announce" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use announcements!");
                    return true;
                }
                Player annPlayer = (Player) sender;
                String annKingdomName = kingdomManager.getKingdomOfPlayer(annPlayer.getName());
                if (annKingdomName == null) {
                    annPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom annKingdom = kingdomManager.getKingdom(annKingdomName);
                
                if (args.length < 2) {
                    annPlayer.sendMessage("Usage: /" + label + " announcement <create|list|delete> [message|id]");
                    return true;
                }
                
                String annSub = args[1].toLowerCase();
                switch (annSub) {
                    case "create" -> {
                        if (args.length < 3) {
                            annPlayer.sendMessage("Usage: /" + label + " announcement create <message>");
                            return true;
                        }
                        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                        com.excrele.kingdoms.manager.CommunicationManager commManager = plugin.getCommunicationManager();
                        if (commManager != null && commManager.createAnnouncement(annKingdom, annPlayer.getName(), message)) {
                            annPlayer.sendMessage("§aAnnouncement created!");
                        } else {
                            annPlayer.sendMessage("§cFailed to create announcement!");
                        }
                        return true;
                    }
                    case "list" -> {
                        com.excrele.kingdoms.manager.CommunicationManager commManager2 = plugin.getCommunicationManager();
                        java.util.List<com.excrele.kingdoms.model.KingdomAnnouncement> anns = 
                            commManager2 != null ? commManager2.getAnnouncements(annKingdomName) : new java.util.ArrayList<>();
                        annPlayer.sendMessage("§6=== Kingdom Announcements ===");
                        if (anns.isEmpty()) {
                            annPlayer.sendMessage("§7No announcements.");
                        } else {
                            for (com.excrele.kingdoms.model.KingdomAnnouncement ann : anns) {
                                annPlayer.sendMessage("§7[" + ann.getFormattedTime() + "] §e" + ann.getMessage() + 
                                    " §7- " + ann.getAuthor());
                            }
                        }
                        return true;
                    }
                    case "delete" -> {
                        if (args.length < 3) {
                            annPlayer.sendMessage("Usage: /" + label + " announcement delete <id>");
                            return true;
                        }
                        com.excrele.kingdoms.manager.CommunicationManager commManager3 = plugin.getCommunicationManager();
                        if (commManager3 != null && commManager3.deleteAnnouncement(annKingdom, args[2])) {
                            annPlayer.sendMessage("§aAnnouncement deleted!");
                        } else {
                            annPlayer.sendMessage("§cAnnouncement not found!");
                        }
                        return true;
                    }
                }
                return true;
            }
            case "event" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use events!");
                    return true;
                }
                Player eventPlayer = (Player) sender;
                String eventKingdomName = kingdomManager.getKingdomOfPlayer(eventPlayer.getName());
                if (eventKingdomName == null) {
                    eventPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom eventKingdom = kingdomManager.getKingdom(eventKingdomName);
                
                if (args.length < 2) {
                    eventPlayer.sendMessage("Usage: /" + label + " event <create|list|delete> [args]");
                    return true;
                }
                
                String eventSub = args[1].toLowerCase();
                switch (eventSub) {
                    case "create" -> {
                        if (args.length < 5) {
                            eventPlayer.sendMessage("Usage: /" + label + " event create <name> <description> <timestamp>");
                            return true;
                        }
                        String eventName = args[2];
                        String eventDesc = args[3];
                        try {
                            long timestamp = Long.parseLong(args[4]);
                            org.bukkit.Location eventLoc = eventPlayer.getLocation();
                            com.excrele.kingdoms.manager.CommunicationManager commManager4 = plugin.getCommunicationManager();
                            if (commManager4 != null && commManager4.createEvent(eventKingdom, eventName, eventDesc, eventLoc, timestamp)) {
                                eventPlayer.sendMessage("§aEvent created!");
                            } else {
                                eventPlayer.sendMessage("§cFailed to create event!");
                            }
                        } catch (NumberFormatException e) {
                            eventPlayer.sendMessage("§cInvalid timestamp!");
                        }
                        return true;
                    }
                    case "list" -> {
                        com.excrele.kingdoms.manager.CommunicationManager commManager5 = plugin.getCommunicationManager();
                        java.util.List<com.excrele.kingdoms.model.KingdomEvent> evts = 
                            commManager5 != null ? commManager5.getEvents(eventKingdomName) : new java.util.ArrayList<>();
                        eventPlayer.sendMessage("§6=== Kingdom Events ===");
                        if (evts.isEmpty()) {
                            eventPlayer.sendMessage("§7No upcoming events.");
                        } else {
                            for (com.excrele.kingdoms.model.KingdomEvent evt : evts) {
                                eventPlayer.sendMessage("§7- §e" + evt.getName() + " §7- " + evt.getDescription());
                            }
                        }
                        return true;
                    }
                    case "delete" -> {
                        if (args.length < 3) {
                            eventPlayer.sendMessage("Usage: /" + label + " event delete <id>");
                            return true;
                        }
                        com.excrele.kingdoms.manager.CommunicationManager commManager6 = plugin.getCommunicationManager();
                        if (commManager6 != null && commManager6.deleteEvent(eventKingdom, args[2])) {
                            eventPlayer.sendMessage("§aEvent deleted!");
                        } else {
                            eventPlayer.sendMessage("§cEvent not found!");
                        }
                        return true;
                    }
                }
                return true;
            }
            case "customize", "custom" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can customize kingdoms!");
                    return true;
                }
                Player customPlayer = (Player) sender;
                String customKingdomName = kingdomManager.getKingdomOfPlayer(customPlayer.getName());
                if (customKingdomName == null) {
                    customPlayer.sendMessage("You must be in a kingdom!");
                    return true;
                }
                Kingdom customKingdom = kingdomManager.getKingdom(customKingdomName);
                
                if (args.length < 2) {
                    customPlayer.sendMessage("Usage: /" + label + " customize <color|motto|banner> [value]");
                    return true;
                }
                
                String customSub = args[1].toLowerCase();
                switch (customSub) {
                    case "color" -> {
                        if (args.length < 3) {
                            customPlayer.sendMessage("Usage: /" + label + " customize color <color>");
                            customPlayer.sendMessage("§7Available colors: RED, BLUE, GREEN, YELLOW, PURPLE, AQUA, WHITE, GRAY, GOLD");
                            return true;
                        }
                        com.excrele.kingdoms.manager.CustomizationManager customManager1 = plugin.getCustomizationManager();
                        if (customManager1 != null && customManager1.setColor(customKingdom, args[2])) {
                            customPlayer.sendMessage("§aKingdom color set!");
                        } else {
                            customPlayer.sendMessage("§cInvalid color!");
                        }
                        return true;
                    }
                    case "motto" -> {
                        if (args.length < 3) {
                            customPlayer.sendMessage("Usage: /" + label + " customize motto <motto>");
                            return true;
                        }
                        String motto = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                        com.excrele.kingdoms.manager.CustomizationManager customManager2 = plugin.getCustomizationManager();
                        if (customManager2 != null && customManager2.setMotto(customKingdom, motto)) {
                            customPlayer.sendMessage("§aKingdom motto set!");
                        } else {
                            customPlayer.sendMessage("§cMotto too long! (max 100 characters)");
                        }
                        return true;
                    }
                    case "banner" -> {
                        org.bukkit.inventory.ItemStack heldItem = customPlayer.getInventory().getItemInMainHand();
                        com.excrele.kingdoms.manager.CustomizationManager customManager3 = plugin.getCustomizationManager();
                        if (customManager3 != null && customManager3.setBanner(customKingdom, heldItem)) {
                            customPlayer.sendMessage("§aKingdom banner set!");
                        } else {
                            customPlayer.sendMessage("§cHold a banner in your hand!");
                        }
                        return true;
                    }
                }
                return true;
            }
            case "visual", "border" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use visual features!");
                    return true;
                }
                Player visualPlayer = (Player) sender;
                
                if (args.length < 2) {
                    visualPlayer.sendMessage("Usage: /" + label + " visual <border|marker|name|3d> [args]");
                    return true;
                }
                
                String visualSub = args[1].toLowerCase();
                switch (visualSub) {
                    case "border" -> {
                        plugin.getVisualManager().togglePersistentBorders(visualPlayer);
                        return true;
                    }
                    case "marker" -> {
                        String markerKingdomName = kingdomManager.getKingdomOfPlayer(visualPlayer.getName());
                        if (markerKingdomName == null) {
                            visualPlayer.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        Kingdom markerKingdom = kingdomManager.getKingdom(markerKingdomName);
                        org.bukkit.Location markerLoc = visualPlayer.getLocation();
                        if (markerLoc == null) {
                            visualPlayer.sendMessage("§cUnable to get your location!");
                            return true;
                        }
                        Chunk markerChunk = markerLoc.getChunk();
                        
                        if (args.length >= 3 && args[2].equalsIgnoreCase("remove")) {
                            if (plugin.getVisualManager().removeMarker(markerChunk)) {
                                visualPlayer.sendMessage("§aMarker removed!");
                            } else {
                                visualPlayer.sendMessage("§cNo marker found at this location!");
                            }
                        } else {
                            String markerName = args.length >= 3 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "";
                            if (plugin.getVisualManager() != null && 
                                plugin.getVisualManager().createMarker(markerKingdom, markerChunk, markerLoc, markerName)) {
                                visualPlayer.sendMessage("§aMarker created!");
                            } else {
                                visualPlayer.sendMessage("§cMarker already exists at this location!");
                            }
                        }
                        return true;
                    }
                    case "name" -> {
                        String nameKingdomName = kingdomManager.getKingdomOfPlayer(visualPlayer.getName());
                        if (nameKingdomName == null) {
                            visualPlayer.sendMessage("You must be in a kingdom!");
                            return true;
                        }
                        org.bukkit.Location nameLoc = visualPlayer.getLocation();
                        if (nameLoc == null) {
                            visualPlayer.sendMessage("§cUnable to get your location!");
                            return true;
                        }
                        Chunk nameChunk = nameLoc.getChunk();
                        
                        if (args.length >= 3) {
                            String claimName = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                            plugin.getVisualManager().setClaimName(nameChunk, claimName);
                            visualPlayer.sendMessage("§aClaim name set to: §e" + claimName);
                        } else {
                            String currentName = plugin.getVisualManager().getClaimName(nameChunk);
                            if (currentName != null && !currentName.isEmpty()) {
                                visualPlayer.sendMessage("§7Current claim name: §e" + currentName);
                            } else {
                                visualPlayer.sendMessage("§7This claim has no name.");
                            }
                        }
                        return true;
                    }
                    case "3d" -> {
                        org.bukkit.Location vis3dLoc = visualPlayer.getLocation();
                        if (vis3dLoc == null) {
                            visualPlayer.sendMessage("§cUnable to get your location!");
                            return true;
                        }
                        Chunk vis3dChunk = vis3dLoc.getChunk();
                        if (plugin.getVisualManager() != null) {
                            plugin.getVisualManager().show3DVisualization(visualPlayer, vis3dChunk);
                        }
                        visualPlayer.sendMessage("§a3D visualization shown!");
                        return true;
                    }
                }
                return true;
            }
            default -> sender.sendMessage("Unknown subcommand! Use §e/kingdom help §7for a list of commands.");
        }
        return true;
    }
}