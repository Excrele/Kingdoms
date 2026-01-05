# Kingdom Management System

## Overview

The Kingdom Management system is the core of the plugin, handling all aspects of kingdom creation, membership, roles, and administration.

## Key Components

### Kingdom Model
- **Location**: `src/main/java/com/excrele/kingdoms/model/Kingdom.java`
- **Manager**: `src/main/java/com/excrele/kingdoms/manager/KingdomManager.java`

### Core Features

#### Kingdom Creation
- Players can create kingdoms using `/kingdom create <name>`
- The creator automatically becomes the King
- The current chunk is automatically claimed upon creation
- Kingdom spawn point is set to the creator's location

#### Membership System
- **King**: The original creator, has full control
- **Members**: Players who have joined the kingdom
- **Invitation System**: Kings and Advisors can invite players
- **Acceptance**: Players must accept invitations with `/kingdom accept`
- **Leaving**: Members can leave with `/kingdom leave` (Kings cannot leave)

#### Role System
Five distinct roles with different permission levels:

1. **King** (KING)
   - Full control over all kingdom features
   - Cannot be demoted or removed
   - Can promote/demote members
   - Can disband the kingdom

2. **Advisor** (ADVISOR)
   - Can invite and kick members
   - Can claim/unclaim chunks
   - Can set kingdom flags
   - Can manage alliances
   - Can set spawn points

3. **Guard** (GUARD)
   - Can claim chunks
   - Can set plot flags
   - Focus on territory management

4. **Builder** (BUILDER)
   - Can claim chunks
   - Can set plot types
   - Focus on construction

5. **Member** (MEMBER)
   - Basic member
   - Can contribute XP through challenges
   - Can use kingdom chat
   - Can teleport to spawn

#### Kingdom Data
Each kingdom stores:
- Name and King
- Member list with roles
- Claimed chunks
- XP and Level
- Alliances
- Spawn points (multiple supported)
- Member contributions
- Creation timestamp
- Total challenges completed

## Commands

### Basic Management
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

## Permissions

Role-based permissions are enforced throughout the system:
- **setflags**: Set kingdom and plot flags
- **claim**: Claim chunks
- **unclaim**: Unclaim chunks
- **invite**: Invite members
- **kick**: Remove members
- **promote**: Change roles

## Storage

Kingdoms are stored in:
- **YAML**: `kingdoms.yml` (default)
- **MySQL**: `kingdoms` table
- **SQLite**: `kingdoms` table

## Related Systems

- [Land Claiming System](02-land-claiming.md)
- [Role & Permission System](03-roles-permissions.md)
- [Alliance System](04-alliances.md)
- [Economy System](05-economy.md)

