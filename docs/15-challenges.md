# Challenge System

## Overview

The Challenge system provides configurable tasks that players can complete to earn XP for their kingdoms, with progress tracking, cooldowns, and advanced challenge types.

## Key Components

### Challenge Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/ChallengeManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/Challenge.java`

### Advanced Challenge Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/AdvancedChallengeManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/AdvancedChallenge.java`

## Core Features

### Basic Challenges
- **50 Configurable Challenges**: Defined in config.yml
- **Task Types**: Block break, entity kill, item craft
- **Progress Tracking**: Multi-action challenges track progress
- **Cooldowns**: Increasing cooldowns per completion
- **XP Rewards**: Configurable XP per challenge

### Advanced Challenges
- **Weekly Challenges**: Time-limited weekly challenges
- **Monthly Challenges**: Longer-duration monthly challenges
- **Group Challenges**: Require multiple members to complete
- **Chain Challenges**: Sequential challenges that unlock

### Challenge Types

#### Basic Challenge Types
- `block_break` - Break specific blocks
- `entity_kill` - Kill specific entities
- `item_craft` - Craft specific items
- `item_consume` - Consume specific items

#### Advanced Challenge Types
- `WEEKLY` - Resets weekly
- `MONTHLY` - Resets monthly
- `GROUP` - Requires multiple members
- `CHAIN` - Part of a challenge chain

## Commands

### Challenge Management
- `/kingdom challenges` - List all challenges
- `/kingdom challenges gui` - Open challenge GUI
- `/kingdom challenges progress` - View your progress

### Advanced Challenges
- `/kingdom advchallenge list` - List advanced challenges
- `/kingdom advchallenge progress` - View progress
- `/kingdom advchallenge complete <id>` - Complete challenge

## Challenge Completion

### Basic Challenges
1. Player performs action (break block, kill mob, etc.)
2. Progress tracked
3. When requirement met, challenge completes
4. XP awarded to kingdom
5. Cooldown starts

### Advanced Challenges
1. Challenge available (time-limited)
2. Players work toward completion
3. Group challenges require multiple members
4. Chain challenges unlock next in sequence
5. Rewards distributed

## XP System

### XP Award
- Base XP from challenge
- Throne structure bonus applied
- XP added to kingdom
- Member contribution tracked

### Formula
```
Final XP = Base XP × Throne Bonus Multiplier
```

## Cooldown System

### Cooldown Calculation
```
Cooldown = Base Cooldown + (Completions - 1) × Increment
```

### Default Values
- **Base Cooldown**: 86400 seconds (1 day)
- **Increment**: 86400 seconds (1 day)

## Challenge GUI

### Features
- Visual challenge list
- Progress indicators
- Cooldown timers
- Reward information
- Click to view details

## Storage

Challenge data stored in:
- **YAML**: `player_data.yml` (progress)
- **MySQL**: `player_challenges` table
- **SQLite**: `player_challenges` table

## Related Systems

- [Kingdom Management](01-kingdom-management.md)
- [Progression System](16-progression.md)
- [Structures](14-structures.md)

