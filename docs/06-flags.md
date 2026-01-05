# Flag System

## Overview

The Flag system provides granular control over kingdom and chunk behavior through configurable flags.

## Key Components

### Flag Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/FlagManager.java`
- **Integration**: Works with `KingdomManager` and `ClaimManager`

### Flag Types

#### Kingdom Flags
Global flags that apply to all kingdom claims:
- `pvp` - Enable/disable PvP
- `mob-spawning` - Control mob spawning
- `fire-spread` - Control fire spread
- `explosions` - Control explosion damage
- `entry` - Control who can enter
- `animals` - Control animal spawning
- `monsters` - Control monster spawning

#### Plot Flags
Per-chunk flags for granular control:
- All kingdom flags (can override)
- `build` - Control building permissions
- `interact` - Control interaction permissions
- `container` - Control container access
- `pvp` - Override kingdom PvP setting
- `entry` - Override kingdom entry setting

## Commands

### Kingdom Flags
- `/kingdom flag <flag> <value>` - Set kingdom flag
- `/kingdom flag list` - List all kingdom flags
- `/kingdom flag remove <flag>` - Remove flag

### Plot Flags
- `/kingdom plotflag <flag> <value>` - Set plot flag
- `/kingdom plotflag list` - List plot flags
- `/kingdom plotflag remove <flag>` - Remove plot flag

## Flag Values

### Boolean Flags
- `true` / `false`
- `yes` / `no`
- `1` / `0`
- `on` / `off`

### Entry Flags
- `members` - Only members
- `allies` - Members and allies
- `all` - Everyone
- `none` - No one

## Flag Priority

1. **Plot Flags** (highest priority)
2. **Kingdom Flags** (default)
3. **Server Defaults** (fallback)

## Permission Requirements

- **Kingdom Flags**: King or Advisor
- **Plot Flags**: King, Advisor, Guard, or Builder (depending on flag)

## Storage

Flags stored in:
- **YAML**: `kingdoms.yml` (within kingdom/chunk data)
- **MySQL**: `kingdoms.flags` and `chunks.flags` tables
- **SQLite**: `kingdoms.flags` and `chunks.flags` tables

## Related Systems

- [Kingdom Management](01-kingdom-management.md)
- [Land Claiming](02-land-claiming.md)
- [Role & Permissions](03-roles-permissions.md)

