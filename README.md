Kingdoms Plugin
The Kingdoms Plugin is a Minecraft server plugin designed for Spigot/Bukkit, enabling players to create and manage kingdoms, claim land, complete challenges to earn XP, and unlock perks through a progression system. This plugin enhances gameplay with territorial control, cooperative challenges, dynamic perks, and user-friendly GUIs, all stored in YAML files for persistence.
Features
Kingdom Management

Creation and Membership: Players can create kingdoms, invite others, and manage membership.
Leadership: Each kingdom has a king who can perform administrative actions like inviting members or unclaiming land.
Disbanding: Admins can dissolve kingdoms using admin commands.
GUI Management: Use /kingdom gui to view and manage kingdom details (members, claims, flags, XP) via an inventory interface (kings only for actions).

Land Claiming

Chunk-Based Claims: Kingdoms claim chunks with a starting limit of 10 chunks, increasing by 5 per level.
Outposts: Kingdoms can create outposts at least 10 chunks away from their main claim.
Claim Limits: A 5-chunk buffer prevents overlapping claims between kingdoms.
Visual Feedback: When entering a claimed chunk, players see particles (VILLAGER_HAPPY for their kingdom, SMOKE for others) along chunk borders at their Y-level.
Claim Map: Use /kingdom map to view an 11x11 ASCII map of nearby chunks, showing own kingdom ([K]), enemy kingdoms ([E]), unclaimed areas ([-]), and player position ([P]).

Challenge System

50 Configurable Challenges: Players complete tasks (mining, killing mobs, crafting) to earn XP for their kingdom. Examples include:
Mine 100 stone blocks (100 XP, Difficulty 1)
Kill 20 zombies (200 XP, Difficulty 2)
Craft a beacon (800 XP, Difficulty 8)


Progress Tracking: Multi-action challenges track progress, showing updates like "50/100 blocks mined."
Cooldowns: Challenges have a base cooldown of 1 day, increasing by 1 day per completion, tracked per player.
XP Contribution: Completing challenges adds XP to the player's kingdom.
Challenge GUI: Use /kingdom challenges gui to view challenges in an inventory, with items representing tasks and lore showing details (description, progress, rewards, cooldown).

Progression and Leveling

Kingdom Levels: Kings can level up their kingdom using /levelup when enough XP is accumulated. XP requirements grow quadratically (e.g., Level 2 requires 1000 XP, Level 3 requires 4000 XP).
Perks:
Movement Speed: +5% speed per level (e.g., 10% at Level 3) in kingdom claims.
Health Regeneration: +0.5 hearts every 5 seconds per level in non-PvP claims.



Flags and Plot Types

Kingdom Flags: Set global flags (e.g., pvp: true) to control kingdom-wide settings.
Plot Flags: Set per-chunk flags within claims for granular control.
Plot Types: Assign types to chunks (e.g., "farm", "residential") for organizational purposes.

Data Persistence

YAML Storage:
config.yml: Defines challenges and cooldown settings.
kingdoms.yml: Stores kingdom data (members, claims, XP, level).
player_data.yml: Tracks player challenge progress and cooldowns.



Installation

Download: Place the compiled KingdomsPlugin.jar in your server's plugins folder.
Dependencies: Requires Spigot/Bukkit (API version 1.13 or later).
Configuration:
On first run, the plugin generates config.yml, kingdoms.yml, and player_data.yml in the plugins/KingdomsPlugin folder.
Customize config.yml to adjust challenges or cooldowns.


Start Server: Run your server to load the plugin. Check the console for "KingdomsPlugin enabled!".

Commands

/kingdom create : Creates a new kingdom with the sender as king.
/kingdom invite : King invites a player to the kingdom.
/kingdom accept: Accepts a pending kingdom invite.
/kingdom leave: Leaves the current kingdom (not allowed for kings).
/kingdom claim: Claims the current chunk for the player's kingdom, respecting claim limits and buffers.
/kingdom unclaim: King unclaims the current chunk.
/kingdom setplot : Sets a plot type for the current chunk.
/kingdom flag  : King sets a kingdom-wide flag.
/kingdom plotflag  : Sets a flag for the current chunk.
/kingdom challenges [gui]: Lists challenges in chat or opens the Challenge GUI.
/kingdom gui: Opens the Kingdom Management GUI.
/kingdom map: Displays an ASCII map of nearby claims.
/kingdom admin <list|dissolve|forceunclaim|setflag>:
list: Lists all kingdoms.
dissolve <kingdom>: Dissolves a kingdom.
forceunclaim <kingdom> <world:x:z>: Unclaims a specific chunk.
setflag <kingdom> <flag> <value>: Sets a flag for a kingdom.


/levelup: King levels up the kingdom if enough XP is available.

Permissions

kingdoms.admin: Grants access to /kingdom admin commands. Default: op.

Configuration
config.yml

cooldown:
base: Base cooldown for challenges (default: 86400 seconds = 1 day).
increment: Additional cooldown per completion (default: 86400 seconds).


challenges: Defines 50 challenges with id, description, difficulty (1-10), xp_reward, and task (type, block/entity/item, amount).

Example config.yml
cooldown:
base: 86400
increment: 86400
challenges:
mine_stone:
description: "Mine 100 stone blocks"
difficulty: 1
xp_reward: 100
task:
type: "block_break"
block: "STONE"
amount: 100
# ... 49 more challenges

kingdoms.yml
Stores kingdom data (created dynamically):
kingdoms:
ExampleKingdom:
king: Player1
members:
- Player2
currentClaimChunks: 5
xp: 1500

player_data.yml
Stores challenge progress (created dynamically):
players:
Player1:
completions:
mine_stone:
times: 2
last_completed: 1623456789
progress: 50

Usage

Create a Kingdom: Use /kingdom create MyKingdom to start a kingdom.
Invite Members: Use /kingdom invite PlayerName or the Kingdom GUI to grow your kingdom.
Claim Land: Stand in a chunk and use /kingdom claim. View claims with /kingdom map.
Complete Challenges: Use /kingdom challenges gui to view and track challenges. Complete tasks to earn XP.
Level Up: As king, use /levelup when enough XP is earned to unlock more claim chunks and better perks.
Manage Kingdom: Use /kingdom gui to view members, claims, flags, and XP. Kings can perform actions via clicks.
View Claims: Use /kingdom map to see a 2D map of nearby claims.

Notes

Performance: Optimized for large servers, but test GUI interactions and claim map rendering with many players.
Extensibility: The modular structure supports adding more GUI features or integrations.
API Version: Tested with Spigot 1.13+. Ensure compatibility with your server version.
Future Enhancements: Consider adding challenge tracking via GUI clicks, economy integration, or alliances.

Support
For issues or feature requests, contact the developer or open an issue on the plugin’s repository (if hosted).
