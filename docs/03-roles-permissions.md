# Role & Permission System

## Overview

The Role & Permission system provides granular control over what members can do within a kingdom.

## Role Hierarchy

### 1. King (KING)
**Highest Authority**
- Full control over all kingdom features
- Cannot be demoted or removed
- Can perform all actions
- Can disband the kingdom

**Permissions:**
- ✅ Create/delete kingdom
- ✅ Invite/kick members
- ✅ Promote/demote members
- ✅ Claim/unclaim chunks
- ✅ Set all flags
- ✅ Manage alliances
- ✅ Set spawn points
- ✅ Access kingdom bank
- ✅ Manage structures
- ✅ Declare wars
- ✅ All other permissions

### 2. Advisor (ADVISOR)
**Second-in-Command**
- Administrative role
- Can manage members and territory
- Cannot disband kingdom

**Permissions:**
- ✅ Invite/kick members
- ✅ Claim/unclaim chunks
- ✅ Set kingdom flags
- ✅ Set plot flags
- ✅ Manage alliances
- ✅ Set spawn points
- ✅ Access kingdom bank
- ✅ Manage structures
- ❌ Promote/demote members
- ❌ Disband kingdom

### 3. Guard (GUARD)
**Territory Protection**
- Focus on security and territory
- Can claim and protect chunks

**Permissions:**
- ✅ Claim chunks
- ✅ Set plot flags
- ✅ View kingdom info
- ✅ Use kingdom chat
- ❌ Unclaim chunks
- ❌ Set kingdom flags
- ❌ Manage members

### 4. Builder (BUILDER)
**Construction Focus**
- Focus on building and development
- Can claim and organize chunks

**Permissions:**
- ✅ Claim chunks
- ✅ Set plot types
- ✅ Build in claims
- ✅ View kingdom info
- ✅ Use kingdom chat
- ❌ Unclaim chunks
- ❌ Set flags
- ❌ Manage members

### 5. Member (MEMBER)
**Basic Member**
- Standard membership
- Can contribute and participate

**Permissions:**
- ✅ Complete challenges
- ✅ Contribute XP
- ✅ Use kingdom chat
- ✅ Teleport to spawn
- ✅ View kingdom info
- ❌ Claim chunks
- ❌ Set flags
- ❌ Manage members

## Permission Checks

The system uses `Kingdom.hasPermission(playerName, permission)` to check permissions:

```java
if (kingdom.hasPermission(player.getName(), "claim")) {
    // Allow claiming
}
```

## Permission Types

### Kingdom-Level Permissions
- `setflags` - Set kingdom-wide flags
- `claim` - Claim chunks
- `unclaim` - Unclaim chunks
- `invite` - Invite members
- `kick` - Remove members
- `promote` - Change roles
- `alliance` - Manage alliances
- `spawn` - Set spawn points
- `bank` - Access kingdom bank
- `structure` - Manage structures
- `war` - Declare wars

### Plot-Level Permissions
- `plotflag` - Set plot flags
- `plottype` - Set plot types
- `build` - Build in plot
- `interact` - Interact in plot

## Role Management

### Promoting Members
```
/kingdom promote <player> <role>
```

Available roles:
- `ADVISOR`
- `GUARD`
- `BUILDER`
- `MEMBER`

### Demoting Members
```
/kingdom demote <player> <role>
```

**Note**: Cannot demote to KING or promote to KING (only original creator)

## Trust System

Additional permission system for non-members:
- **Trust Permissions**: Grant specific permissions to trusted players
- **Temporary Trust**: Trust with expiration
- **Permission Types**: build, interact, container, etc.

See [Trust System](08-trust.md) for details.

## Related Systems

- [Kingdom Management](01-kingdom-management.md)
- [Land Claiming](02-land-claiming.md)
- [Trust System](08-trust.md)

