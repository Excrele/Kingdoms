# World Management System

## Overview

The World Management system handles multi-world support, world-specific configurations, and world-based restrictions.

## Key Components

### World Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/WorldManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/WorldConfig.java`

### Core Features

#### Multi-World Support
- Support for multiple worlds
- Per-world claim limits
- Per-world economy settings
- Per-world rules and permissions

#### World Configuration
- **Claimable Worlds**: Configure which worlds allow claiming
- **World-Specific Limits**: Different claim limits per world
- **World Restrictions**: Disable features in specific worlds
- **World Types**: Survival, Creative, Resource, etc.

#### World-Based Features
- **Cross-World Teleportation**: Control teleportation between worlds
- **World-Specific Spawns**: Different spawn points per world
- **World Leaderboards**: Separate leaderboards per world
- **World Economy**: Independent economy per world

## Configuration

### World Config Structure
```yaml
worlds:
  world:
    claimable: true
    max-claims: 10
    economy-enabled: true
    pvp-enabled: true
  creative:
    claimable: false
    max-claims: 0
    economy-enabled: false
    pvp-enabled: false
```

## Commands

### World Management
- `/kingdom world list` - List all configured worlds
- `/kingdom world info <world>` - View world configuration
- `/kingdom world setclaimable <world> <true/false>` - Set claimable status
- `/kingdom world setlimit <world> <limit>` - Set claim limit

## Storage

World configurations stored in:
- **YAML**: `worlds.yml`
- **MySQL**: `world_configs` table
- **SQLite**: `world_configs` table

## Related Systems

- [Land Claiming](02-land-claiming.md)
- [Kingdom Management](01-kingdom-management.md)

