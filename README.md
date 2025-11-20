Kingdoms Plugin
The Kingdoms Plugin is a Minecraft server plugin designed for Spigot/Bukkit, enabling players to create and manage kingdoms, claim land, complete challenges to earn XP, and unlock perks through a progression system. This plugin enhances gameplay with territorial control, cooperative challenges, dynamic perks, role-based permissions, kingdom chat, alliances, and user-friendly GUIs, all stored in YAML files for persistence.
Features
Kingdom Management

Creation and Membership: Players can create kingdoms, invite others, and manage membership.
Leadership: Each kingdom has a king who can perform administrative actions like inviting members or unclaiming land.
Role System: Five roles with different permissions - King, Advisor, Guard, Builder, and Member. Each role has specific capabilities.
Member Management: Kings can promote/demote members and kick inactive players.
Disbanding: Admins can dissolve kingdoms using admin commands.
GUI Management: Use /kingdom gui to view and manage kingdom details (members, claims, flags, XP, contributions) via an inventory interface.
Statistics & Leaderboards: Track kingdom stats and compete on server-wide leaderboards.

Land Claiming

Chunk-Based Claims: Kingdoms claim chunks with a starting limit of 10 chunks, increasing by 5 per level.
Outposts: Kingdoms can create outposts at least 10 chunks away from their main claim.
Claim Limits: A 5-chunk buffer prevents overlapping claims between kingdoms.
Visual Feedback: When entering a claimed chunk, players see particles (VILLAGER_HAPPY for their kingdom, SMOKE for others) along chunk borders at their Y-level.
Claim Map: Use /kingdom map to view an 11x11 ASCII map of nearby chunks, showing own kingdom ([K]), allied kingdoms ([A]), enemy kingdoms ([E]), unclaimed areas ([-]), and player position ([P]).
Protection: Claims are automatically protected - only members and allies can build/interact.

Challenge System

50 Configurable Challenges: Players complete tasks (mining, killing mobs, crafting) to earn XP for their kingdom. Examples include:
Mine 100 stone blocks (100 XP, Difficulty 1)
Kill 20 zombies (200 XP, Difficulty 2)
Craft a beacon (800 XP, Difficulty 8)


Progress Tracking: Multi-action challenges track progress, showing updates like "50/100 blocks mined."
Cooldowns: Challenges have a base cooldown of 1 day, increasing by 1 day per completion, tracked per player.
XP Contribution: Completing challenges adds XP to the player's kingdom and tracks individual member contributions.
Challenge GUI: Use /kingdom challenges gui to view challenges in an inventory, with items representing tasks and lore showing details (description, progress, rewards, cooldown).

Progression and Leveling

Kingdom Levels: Kings can level up their kingdom using /levelup when enough XP is accumulated. XP requirements grow quadratically (e.g., Level 2 requires 1000 XP, Level 3 requires 4000 XP).
Enhanced Perks System (applied automatically in claimed territory):
Level 1+: Movement Speed boost and Health Regeneration (0.5 hearts every 5 seconds in non-PvP areas)
Level 5+: Jump Boost (increases with level)
Level 7+: Mining Speed (Haste) - faster block breaking
Level 10+: Night Vision - see clearly in the dark
Level 15+: Water Breathing - never drown
Level 20+: Fire Resistance - immune to fire and lava damage
Member Contributions: Track individual XP contributions from each member. View top contributors with /kingdom contributions.



Flags and Plot Types

Kingdom Flags: Set global flags (e.g., pvp: true) to control kingdom-wide settings.
Plot Flags: Set per-chunk flags within claims for granular control. Different roles have different permissions to set flags.
Plot Types: Assign types to chunks (e.g., "farm", "residential") for organizational purposes.
Claim Protection: Automatic protection prevents griefing - non-members cannot break/place blocks or interact with containers in your claims. Allied kingdoms have limited access.

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

Kingdom Management:
/kingdom create <name>: Creates a new kingdom with the sender as king.
/kingdom invite <player>: Invite a player to the kingdom (King/Advisor only).
/kingdom accept: Accepts a pending kingdom invite.
/kingdom leave: Leaves the current kingdom (not allowed for kings).
/kingdom promote <player> <role>: Promote a member to a new role (King only). Roles: ADVISOR, GUARD, BUILDER, MEMBER.
/kingdom kick <player>: Kick a member from the kingdom (King/Advisor only).

Land Claiming:
/kingdom claim: Claims the current chunk for the player's kingdom, respecting claim limits and buffers.
/kingdom unclaim: Unclaims the current chunk (King/Advisor only).
/kingdom setplot <type>: Sets a plot type for the current chunk.
/kingdom plotflag <flag> <value>: Sets a flag for the current chunk.
/kingdom map: Displays an ASCII map of nearby claims.

Flags & Settings:
/kingdom flag <flag> <value>: Set a kingdom-wide flag (King/Advisor only).
/kingdom setspawn: Set the kingdom spawn point (King/Advisor only).
/kingdom spawn: Teleport to your kingdom's spawn point.

Challenges & Progression:
/kingdom challenges [gui]: Lists challenges in chat or opens the Challenge GUI.
/kingdom contributions: View member XP contributions and top contributors.
/kingdom stats: View detailed kingdom statistics.
/levelup: King levels up the kingdom if enough XP is available.

Communication:
/kingdom chat: Toggle kingdom chat mode on/off.
/kc <message>: Send a message to kingdom chat (alternative to /kingdom chat <message>).

Alliances:
/kingdom alliance invite <kingdom>: Request an alliance with another kingdom (King/Advisor only).
/kingdom alliance accept <kingdom>: Accept an alliance request.
/kingdom alliance deny <kingdom>: Deny an alliance request.
/kingdom alliance list: View all current alliances.
/kingdom alliance remove <kingdom>: Dissolve an alliance (King/Advisor only).

Leaderboards:
/kingdom leaderboard [level|xp|members|challenges]: View server-wide leaderboards. Defaults to level.

GUI:
/kingdom gui: Opens the Kingdom Management GUI.

Admin Commands:
/kingdom admin <list|dissolve|forceunclaim|setflag>:
  list: Lists all kingdoms.
  dissolve <kingdom>: Dissolves a kingdom.
  forceunclaim <kingdom> <world:x:z>: Unclaims a specific chunk.
  setflag <kingdom> <flag> <value>: Sets a flag for a kingdom.

Permissions

kingdoms.admin: Grants access to /kingdom admin commands. Default: op.

Role Permissions:
- King: Full control - can do everything
- Advisor: Can invite, claim/unclaim, set flags, kick members
- Guard: Can claim chunks and set plot flags
- Builder: Can claim chunks and set plot types
- Member: Basic member - can only contribute XP through challenges

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
memberRoles:
  Player2: MEMBER
memberContributions:
  Player1: 5000
  Player2: 2000
alliances:
- AlliedKingdom
currentClaimChunks: 5
xp: 1500
level: 2
createdAt: 1623456789
totalChallengesCompleted: 15
spawn:
  world: world
  x: 100.5
  y: 64.0
  z: 200.5
  yaw: 0.0
  pitch: 0.0

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

Getting Started:
1. Create a Kingdom: Use /kingdom create MyKingdom to start a kingdom. Your current chunk will be automatically claimed.
2. Invite Members: Use /kingdom invite PlayerName to invite players. They accept with /kingdom accept.
3. Assign Roles: Use /kingdom promote <player> <role> to give members appropriate permissions.
4. Claim Land: Stand in a chunk and use /kingdom claim. View claims with /kingdom map.
5. Complete Challenges: Use /kingdom challenges gui to view and track challenges. Complete tasks to earn XP for your kingdom.
6. Level Up: As king, use /levelup when enough XP is earned to unlock more claim chunks and better perks.

Advanced Features:
- Kingdom Chat: Use /kingdom chat to toggle kingdom chat mode, or /kc <message> to send a quick message.
- View Statistics: Use /kingdom stats to see detailed kingdom information and /kingdom contributions to see who's contributing most.
- Form Alliances: Use /kingdom alliance invite <kingdom> to request alliances with other kingdoms for mutual cooperation.
- Manage Members: Use /kingdom gui to access the management interface, or /kingdom kick <player> to remove inactive members.
- Leaderboards: Use /kingdom leaderboard to compete with other kingdoms on the server.

Kingdom Management:
- Use /kingdom gui to view members, claims, flags, XP, and contributions. Click items for quick actions.
- Use /kingdom map to see a 2D map of nearby claims, including your kingdom, allies, and enemies.
- Set plot types and flags to organize your territory and control permissions.

Features Summary

✅ Kingdom creation and management with role-based permissions
✅ Land claiming system with outposts and buffer zones
✅ 50+ configurable challenges with progress tracking
✅ XP and leveling system with enhanced perks
✅ Member contribution tracking and statistics
✅ Kingdom chat for team communication
✅ Alliance system for diplomatic gameplay
✅ Claim protection against griefing
✅ Interactive GUIs for easy management
✅ Server-wide leaderboards
✅ Visual claim maps with alliance indicators
✅ Per-chunk flags and plot types

Notes

Performance: Optimized for large servers, but test GUI interactions and claim map rendering with many players.
Extensibility: The modular structure supports adding more GUI features or integrations.
API Version: Tested with Spigot 1.21+. Ensure compatibility with your server version.
Role System: The role-based permission system allows for flexible kingdom management and delegation of responsibilities.
Alliances: Allied kingdoms can build in each other's territory and are shown on claim maps for easy identification.

Support
For issues or feature requests, contact the developer or open an issue on the plugin’s repository (if hosted).
