# Progression & Leveling System

## Overview

The Progression system tracks kingdom growth through XP, levels, and unlocks powerful perks as kingdoms advance.

## Key Components

### Level System
- Integrated into `Kingdom` model
- Managed by `KingdomManager`
- Perks applied by `PerkTask`

### Core Features

#### XP System
- **XP Sources**: Challenges, advanced challenges
- **XP Tracking**: Per-kingdom and per-member
- **XP Display**: Shown in GUIs and commands
- **XP Bonuses**: Throne structure provides bonus

#### Level System
- **Starting Level**: Level 1
- **Level Requirements**: Quadratic growth
- **Level Benefits**: More claims, better perks
- **Level Cap**: Configurable (default: unlimited)

#### Perks System
- **Automatic Application**: Perks apply in claimed territory
- **Level-Based**: Unlock at specific levels
- **Stacking**: Multiple perks active simultaneously

## Level Requirements

### XP Formula
```
Required XP = Base XP × (Level)²
```

### Example Requirements
- **Level 1→2**: 1000 XP
- **Level 2→3**: 4000 XP
- **Level 3→4**: 9000 XP
- **Level 4→5**: 16000 XP

## Perks by Level

### Level 1+
- **Movement Speed**: Slight speed boost
- **Health Regeneration**: 0.5 hearts every 5 seconds (non-PvP)

### Level 5+
- **Jump Boost**: Increased jump height
- **Scaling**: Increases with level

### Level 7+
- **Mining Speed (Haste)**: Faster block breaking
- **Productivity**: Significant mining improvement

### Level 10+
- **Night Vision**: See clearly in darkness
- **Exploration**: Better visibility

### Level 15+
- **Water Breathing**: Never drown
- **Aquatic**: Underwater exploration

### Level 20+
- **Fire Resistance**: Immune to fire and lava
- **Safety**: Nether exploration

## Claim Limits

### Formula
```
Max Claims = 10 + (Level × 5)
```

### Examples
- **Level 1**: 15 claims
- **Level 5**: 35 claims
- **Level 10**: 60 claims
- **Level 20**: 110 claims

## Commands

### Progression
- `/levelup` - Level up kingdom (King only)
- `/kingdom level` - View current level
- `/kingdom xp` - View XP and requirements
- `/kingdom contributions` - View member contributions

## Member Contributions

### Tracking
- Individual XP contributions tracked
- Top contributors displayed
- Contribution streaks tracked
- See [Social Features](17-social.md)

## Integration

### Challenge System
- Challenges provide XP
- See [Challenge System](15-challenges.md)

### Structures
- Throne increases XP gains
- See [Structures](14-structures.md)

### Perks
- Applied automatically in claims
- See [Perks System](18-perks.md)

## Storage

Progression data stored in:
- **YAML**: `kingdoms.yml` (level, XP)
- **MySQL**: `kingdoms.level`, `kingdoms.xp` columns
- **SQLite**: `kingdoms.level`, `kingdoms.xp` columns

## Related Systems

- [Challenge System](15-challenges.md)
- [Structures](14-structures.md)
- [Social Features](17-social.md)
- [Perks System](18-perks.md)

