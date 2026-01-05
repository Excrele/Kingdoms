# Perks System

## Overview

The Perks system automatically applies beneficial effects to players when they are in their kingdom's claimed territory.

## Key Components

### Perk Task
- **Location**: `src/main/java/com/excrele/kingdoms/task/PerkTask.java`
- **Integration**: Runs periodically to apply perks

### Core Features

#### Automatic Application
- **Territory-Based**: Perks only apply in claimed chunks
- **Member-Only**: Only kingdom members receive perks
- **Continuous**: Perks apply as long as player is in territory
- **Stacking**: Multiple perks active simultaneously

#### Perk Types

##### Level 1+ Perks
- **Movement Speed**: Slight speed boost
  - Effect: Speed I (configurable)
  - Applies: Always in claims
  
- **Health Regeneration**: Regenerate health
  - Effect: 0.5 hearts every 5 seconds
  - Condition: Only in non-PvP areas
  - Applies: Continuously

##### Level 5+ Perks
- **Jump Boost**: Increased jump height
  - Effect: Jump Boost I (scales with level)
  - Applies: Always in claims

##### Level 7+ Perks
- **Mining Speed (Haste)**: Faster block breaking
  - Effect: Haste I (scales with level)
  - Applies: Always in claims

##### Level 10+ Perks
- **Night Vision**: See in darkness
  - Effect: Night Vision (infinite)
  - Applies: Always in claims

##### Level 15+ Perks
- **Water Breathing**: Never drown
  - Effect: Water Breathing (infinite)
  - Applies: Always in claims

##### Level 20+ Perks
- **Fire Resistance**: Immune to fire/lava
  - Effect: Fire Resistance (infinite)
  - Applies: Always in claims

## Perk Application

### Process
1. Player enters claimed chunk
2. Check if player is kingdom member
3. Check kingdom level
4. Apply appropriate perks
5. Continuously reapply while in territory
6. Remove perks when leaving territory

### Perk Removal
- **Leaving Territory**: Perks removed when leaving claims
- **Leaving Kingdom**: Perks removed if kicked/leave
- **Kingdom Disband**: All perks removed

## Configuration

### Perk Settings
```yaml
perks:
  enabled: true
  check-interval: 20  # Ticks between checks
  movement-speed:
    level-required: 1
    amplifier: 0  # Speed I
  health-regen:
    level-required: 1
    amount: 0.5
    interval: 100  # Ticks (5 seconds)
    pvp-disabled: true
```

## Integration

### Level System
- Perks unlock with levels
- See [Progression System](16-progression.md)

### Territory System
- Perks apply in claims
- See [Land Claiming](02-land-claiming.md)

## Related Systems

- [Progression System](16-progression.md)
- [Land Claiming](02-land-claiming.md)

