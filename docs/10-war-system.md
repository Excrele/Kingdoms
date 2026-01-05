# War System

## Overview

The War system enables kingdoms to declare and engage in wars, with mechanics for sieges, raids, and territorial conquest.

## Key Components

### War Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/WarManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/War.java`

### Siege Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/SiegeManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/Siege.java`

### Raid Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/RaidManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/Raid.java`

## Core Features

### War Declaration
- **Requirements**: 
  - Cannot be at war already
  - No non-aggression pact
  - Both kingdoms must exist
- **Duration**: Configurable war duration
- **Announcement**: Server-wide announcement

### War Mechanics
- **War Status**: Active wars tracked
- **War Statistics**: Kills, deaths, territory captured
- **War Objectives**: Capture chunks, destroy structures
- **War End**: Time-based or objective-based

### Siege System
- **Chunk Capture**: Attack enemy chunks during war
- **Progress Tracking**: Siege progress over time
- **Defense**: Defenders can resist sieges
- **Capture**: Successful sieges capture chunks

### Raid System
- **Temporary Raids**: Short-duration attacks
- **Resource Stealing**: Steal resources from enemies
- **Quick Strikes**: Fast, focused attacks
- **Cooldowns**: Raid cooldowns between kingdoms

## Commands

### War Management
- `/kingdom war declare <kingdom> [duration]` - Declare war
- `/kingdom war end <warId>` - End war early
- `/kingdom war status` - View active wars
- `/kingdom war list` - List all wars

### Siege Management
- `/kingdom siege start <kingdom> <chunk>` - Start siege
- `/kingdom siege status` - View active sieges
- `/kingdom siege end <siegeId>` - End siege

### Raid Management
- `/kingdom raid start <kingdom> <chunk>` - Start raid
- `/kingdom raid status` - View active raids
- `/kingdom raid end <raidId>` - End raid

## War Restrictions

### Diplomacy Integration
- Non-aggression pacts prevent war
- Trade agreements don't prevent war
- Mutual defense activates during war

### War Bonuses
- **War Room Structure**: Provides war bonuses
- **Barracks Structure**: Provides defense bonuses
- See [Structures](14-structures.md)

## Storage

War data stored in:
- **YAML**: `wars.yml`, `sieges.yml`, `raids.yml`
- **MySQL**: `wars`, `sieges`, `raids` tables
- **SQLite**: `wars`, `sieges`, `raids` tables

## Related Systems

- [Diplomacy System](09-diplomacy.md)
- [Structures](14-structures.md)
- [Resource Management](11-resources.md)

