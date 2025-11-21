package com.excrele.kingdoms.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class KingdomChatCommandTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Kingdom chat doesn't need tab completion - it's just a message
        // Return empty list to allow normal chat tab completion
        return new ArrayList<>();
    }
}

