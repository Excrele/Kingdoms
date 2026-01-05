# Commands Reference

## Overview

Complete reference for all plugin commands organized by category.

## Kingdom Management

### Creation & Membership
- `/kingdom create <name>` - Create a new kingdom
- `/kingdom invite <player>` - Invite a player (King/Advisor)
- `/kingdom accept` - Accept a pending invitation
- `/kingdom leave` - Leave your kingdom
- `/kingdom kick <player>` - Remove a member (King/Advisor)
- `/kingdom promote <player> <role>` - Change member role (King)
- `/kingdom demote <player> <role>` - Change member role (King)

### Information
- `/kingdom info` - View kingdom information
- `/kingdom members` - List all members
- `/kingdom gui` - Open management GUI

## Land Claiming

### Claiming
- `/kingdom claim` - Claim current chunk
- `/kingdom unclaim` - Unclaim current chunk (King/Advisor)
- `/kingdom unclaimall` - Unclaim all chunks (King only)

### Information
- `/kingdom map` - View claim map
- `/kingdom claims` - List all claims
- `/kingdom claiminfo` - Info about current chunk

### Plot Management
- `/kingdom setplot <type>` - Set plot type for chunk
- `/kingdom plotflag <flag> <value>` - Set plot flag
- `/kingdom plotinfo` - View plot information

## Flags & Settings

- `/kingdom flag <flag> <value>` - Set kingdom flag
- `/kingdom flag list` - List all kingdom flags
- `/kingdom flag remove <flag>` - Remove flag

## Spawn Points

- `/kingdom setspawn [name]` - Set spawn point (King/Advisor)
- `/kingdom spawn [name]` - Teleport to spawn
- `/kingdom spawn list` - List all spawn points

## Challenges & Progression

- `/kingdom challenges` - List all challenges
- `/kingdom challenges gui` - Open challenge GUI
- `/kingdom challenges progress` - View your progress
- `/kingdom contributions` - View member contributions
- `/kingdom stats` - View detailed statistics
- `/levelup` - Level up kingdom (King only)

## Communication

- `/kingdom chat` - Toggle kingdom chat
- `/kc <message>` - Send kingdom chat message
- `/kingdom announce <message>` - Create announcement

## Alliances

- `/kingdom alliance invite <kingdom>` - Request alliance
- `/kingdom alliance accept <kingdom>` - Accept request
- `/kingdom alliance deny <kingdom>` - Deny request
- `/kingdom alliance remove <kingdom>` - Dissolve alliance
- `/kingdom alliance list` - View all alliances

## Economy

### Banking
- `/kingdom bank balance` - View bank balance
- `/kingdom bank deposit <amount>` - Deposit money
- `/kingdom bank withdraw <amount>` - Withdraw money
- `/kingdom bank transfer <kingdom> <amount>` - Transfer money

### Claim Economy
- `/kingdom claim sell <price>` - Sell current chunk
- `/kingdom claim buy <kingdom>` - Buy chunk
- `/kingdom claim auction <price> <duration>` - Auction chunk
- `/kingdom claim rent <price> <duration>` - Rent chunk
- `/kingdom claim marketplace` - View marketplace

### Tax System
- `/kingdom tax setrate <percentage>` - Set tax rate
- `/kingdom tax setinterval <seconds>` - Set collection interval
- `/kingdom tax status` - View tax configuration

## War & Diplomacy

### War
- `/kingdom war declare <kingdom> [duration]` - Declare war
- `/kingdom war end <warId>` - End war
- `/kingdom war status` - View active wars
- `/kingdom war list` - List all wars

### Siege
- `/kingdom siege start <kingdom> <chunk>` - Start siege
- `/kingdom siege status` - View active sieges
- `/kingdom siege end <siegeId>` - End siege

### Raid
- `/kingdom raid start <kingdom> <chunk>` - Start raid
- `/kingdom raid status` - View active raids
- `/kingdom raid end <raidId>` - End raid

### Diplomacy
- `/kingdom diplomacy list` - List all agreements
- `/kingdom diplomacy propose <kingdom> <type> [duration]` - Propose agreement
- `/kingdom diplomacy accept <id>` - Accept agreement
- `/kingdom diplomacy terminate <id>` - Terminate agreement

## Structures & Resources

### Structures
- `/kingdom structure list` - List all structures
- `/kingdom structure build <type>` - Build structure
- `/kingdom structure upgrade <id>` - Upgrade structure
- `/kingdom structure destroy <id>` - Destroy structure

### Resources
- `/kingdom resource list` - List all resources
- `/kingdom resource deposit <material> <amount>` - Deposit resources
- `/kingdom resource withdraw <material> <amount>` - Withdraw resources
- `/kingdom resource trade <kingdom> <material> <amount> <price>` - Trade resources

## Trade Routes

- `/kingdom traderoute establish <kingdom>` - Create trade route
- `/kingdom traderoute trade <kingdom> <resource> <amount> <price>` - Execute trade
- `/kingdom traderoute list` - List all routes
- `/kingdom traderoute remove <kingdom>` - Remove route

## Social Features

### Mail
- `/kingdom mail inbox` - View mail
- `/kingdom mail send <player> <subject> <message>` - Send mail
- `/kingdom mail broadcast <subject> <message>` - Broadcast mail
- `/kingdom mail read <id>` - Read mail
- `/kingdom mail delete <id>` - Delete mail

### Achievements
- `/kingdom achievements` - View achievements
- `/kingdom achievements <player>` - View player achievements

### Activity
- `/kingdom activity` - View your activity
- `/kingdom activity <player>` - View player activity
- `/kingdom streak` - View contribution streak

## Visual Customization

### Themes
- `/kingdom theme view` - View current theme
- `/kingdom theme preset <name>` - Apply preset theme
- `/kingdom theme colors <primary> <secondary> <accent>` - Set colors
- `/kingdom theme particles <primary> <secondary>` - Set particles

### Banners
- `/kingdom banner list` - List banners
- `/kingdom banner place <material>` - Place banner
- `/kingdom banner remove <id>` - Remove banner

## Analytics

- `/kingdom dashboard` - Open statistics dashboard
- `/kingdom heatmap` - Open territory heat map
- `/kingdom leaderboard [type]` - View leaderboards
- `/kingdom event calendar` - Open event calendar

## Events

- `/kingdom event create <name> <description> <time>` - Create event
- `/kingdom event list` - List events
- `/kingdom event delete <id>` - Delete event
- `/kingdom event calendar` - Open calendar GUI

## Advanced Challenges

- `/kingdom advchallenge list` - List advanced challenges
- `/kingdom advchallenge progress` - View progress
- `/kingdom advchallenge complete <id>` - Complete challenge

## Admin Commands

- `/kingdom admin list` - List all kingdoms
- `/kingdom admin dissolve <kingdom>` - Dissolve kingdom
- `/kingdom admin forceunclaim <kingdom> <world:x:z>` - Force unclaim
- `/kingdom admin setflag <kingdom> <flag> <value>` - Set flag

## Permissions

- `kingdoms.admin` - Admin commands
- `kingdoms.use` - Basic plugin usage
- `kingdoms.create` - Create kingdoms

## Related Documentation

- [Kingdom Management](01-kingdom-management.md)
- [Land Claiming](02-land-claiming.md)
- [Economy System](05-economy.md)
- [War System](10-war-system.md)

