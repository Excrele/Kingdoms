# Structures System

## Overview

The Structures system allows kingdoms to build special buildings that provide bonuses and enhance kingdom capabilities.

## Key Components

### Structure Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/StructureManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/KingdomStructure.java`

### Structure Types

#### 1. Throne
- **Bonus**: Increases XP gain
- **Multiplier**: 10% per level
- **Material**: Golden Apple block
- **Effect**: All XP gains multiplied

#### 2. War Room
- **Bonus**: Improves war/siege capabilities
- **Multiplier**: 12% per level (war-focused)
- **Material**: Iron Sword block
- **Effect**: War bonuses, siege improvements

#### 3. Treasury
- **Bonus**: Increases bank capacity
- **Multiplier**: 10% per level
- **Material**: Gold Block
- **Effect**: Bank capacity multiplier

#### 4. Embassy
- **Bonus**: Enables diplomacy
- **Requirement**: Needed for embassy agreements
- **Material**: Enchanting Table
- **Effect**: Unlocks advanced diplomacy

#### 5. Granary
- **Bonus**: Increases resource storage capacity
- **Multiplier**: 10% per level
- **Material**: Hay Block
- **Effect**: +500 base capacity bonus

#### 6. Barracks
- **Bonus**: Defense bonuses
- **Multiplier**: 11.5% per level (defense-focused)
- **Material**: Iron Block
- **Effect**: Defense improvements, PvP bonuses

## Core Features

### Structure Building
- **One Per Type**: Only one structure of each type per kingdom
- **Location Requirement**: Must be in kingdom claim
- **Permission Required**: King or Advisor
- **Visual Placement**: Structure block placed in world

### Structure Upgrading
- **Level System**: Structures can be upgraded (max level 5)
- **XP Cost**: Upgrades cost kingdom XP
- **Cost Formula**: `Level × 1000 XP`
- **Bonus Scaling**: Bonuses increase with level

### Structure Bonuses
- **Automatic Application**: Bonuses apply automatically
- **Multiplicative**: Bonuses multiply base values
- **Type-Specific**: Each structure has unique bonuses

## Commands

### Structure Management
- `/kingdom structure list` - List all structures
- `/kingdom structure build <type>` - Build structure
- `/kingdom structure upgrade <id>` - Upgrade structure
- `/kingdom structure destroy <id>` - Destroy structure

## Structure Levels

### Level Progression
- **Level 1**: Base bonus (10% per structure type)
- **Level 2**: 20% bonus
- **Level 3**: 30% bonus
- **Level 4**: 40% bonus
- **Level 5**: 50% bonus (maximum)

### Upgrade Costs
- **Level 1→2**: 1000 XP
- **Level 2→3**: 2000 XP
- **Level 3→4**: 3000 XP
- **Level 4→5**: 4000 XP

## Integration

### XP System
- Throne affects XP gains
- See [Challenge System](15-challenges.md)

### Bank System
- Treasury affects bank capacity
- See [Economy System](05-economy.md)

### Resource System
- Granary affects storage capacity
- See [Resource Management](11-resources.md)

### War System
- War Room and Barracks affect war mechanics
- See [War System](10-war-system.md)

### Diplomacy System
- Embassy enables advanced diplomacy
- See [Diplomacy System](09-diplomacy.md)

## Storage

Structure data stored in:
- **YAML**: `structures.yml`
- **MySQL**: `kingdom_structures` table
- **SQLite**: `kingdom_structures` table

## Related Systems

- [Challenge System](15-challenges.md)
- [Economy System](05-economy.md)
- [Resource Management](11-resources.md)
- [War System](10-war-system.md)
- [Diplomacy System](09-diplomacy.md)

