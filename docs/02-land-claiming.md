# Land Claiming System

## Overview

The Land Claiming system allows kingdoms to claim chunks of land, providing protection and territorial control.

## Key Components

### Claim Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/ClaimManager.java`
- **Integration**: Works with `KingdomManager` and `WorldManager`

### Core Features

#### Chunk-Based Claims
- Claims are made on a per-chunk basis (16x16 blocks)
- Each kingdom starts with 10 claimable chunks
- Additional chunks unlock with leveling (5 per level)
- Formula: `Max Claims = 10 + (Level × 5)`

#### Claim Limits
- **Buffer Zone**: 5-chunk minimum distance between kingdoms
- Prevents overlapping claims
- Ensures territorial separation

#### Outposts
- Kingdoms can create outposts
- Must be at least 10 chunks away from main claim
- Allows territorial expansion
- Separate from main claim limits

#### Claim Protection
- **Automatic Protection**: Claims are automatically protected
- **Build Protection**: Only members and allies can build
- **Break Protection**: Only members and allies can break blocks
- **Interaction Protection**: Only members and allies can use containers
- **PvP Control**: Controlled by kingdom/plot flags

#### Visual Feedback
- **Particle Borders**: Chunk borders show particles when entered
- **Own Kingdom**: Green particles (HAPPY_VILLAGER)
- **Other Kingdom**: Gray particles (SMOKE)
- **Theme Integration**: Uses kingdom theme particles if configured

#### Claim Map
- ASCII-based map showing 11x11 chunk area
- Visual indicators:
  - `[K]` - Your kingdom
  - `[A]` - Allied kingdom
  - `[E]` - Enemy kingdom
  - `[-]` - Unclaimed
  - `[P]` - Your position
- Interactive GUI with click-to-teleport

## Commands

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

## Plot Types

Common plot types:
- `farm` - Agricultural area
- `residential` - Housing area
- `commercial` - Trading area
- `industrial` - Production area
- `military` - Defense area
- `public` - Open to all members

## Plot Flags

Per-chunk flags for granular control:
- `pvp` - Enable/disable PvP
- `mob-spawning` - Control mob spawning
- `fire-spread` - Control fire spread
- `explosions` - Control explosion damage
- `entry` - Control who can enter
- `build` - Control building permissions
- `interact` - Control interaction permissions

## Storage

Claims are stored in:
- **YAML**: `kingdoms.yml` (within kingdom data)
- **MySQL**: `claims` table
- **SQLite**: `claims` table

## Performance

- **Caching**: Claim lookups are cached for performance
- **Chunk Optimization**: Intelligent chunk loading
- **Batch Operations**: Efficient claim processing

## Related Systems

- [Kingdom Management](01-kingdom-management.md)
- [Flag System](06-flags.md)
- [World Management](07-world-management.md)
- [Performance & Caching](20-performance.md)

