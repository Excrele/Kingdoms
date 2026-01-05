# Visual Effects System

## Overview

The Visual Effects system provides particle effects, holograms, themes, and banners to enhance the visual experience.

## Key Components

### Visual Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/VisualManager.java`
- **Purpose**: Manage visual effects

### Hologram Manager
- **Location**: `src/main/java/com/excrele/kingdoms/util/HologramManager.java`
- **Purpose**: Create floating text holograms

### Theme Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/ThemeManager.java`
- **Purpose**: Manage kingdom color themes

### Banner Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/BannerManager.java`
- **Purpose**: Manage kingdom banners

### Visual Effects Utility
- **Location**: `src/main/java/com/excrele/kingdoms/util/VisualEffects.java`
- **Purpose**: Static visual effect methods

## Core Features

### Particle Effects
- **Level-Up**: Celebration particles
- **Challenge Complete**: Success particles
- **Claim Effects**: Border particles
- **Territory Auras**: Level-based auras
- **Alliance Effects**: Celebration particles

### Holographic Displays
- **Spawn Holograms**: Floating text at spawns
- **Chunk Borders**: Border indicators
- **Multi-Line**: Support for multiple lines
- **Auto-Creation**: Created automatically

### Kingdom Themes
- **Color Schemes**: Customizable colors
- **Particle Effects**: Theme-based particles
- **Banner Materials**: Custom banners
- **Preset Themes**: Royal, Warrior, Nature, Mystic, Ocean

### Custom Banners
- **Placeable Banners**: Place banners in claims
- **Visual Identity**: Kingdom representation
- **Banner Limits**: Configurable per kingdom
- **Banner Management**: Place, remove, update

## Particle Types

### Celebration Particles
- Firework particles
- Heart particles
- Happy villager particles

### Territory Particles
- Happy villager (own kingdom)
- Smoke (other kingdom)
- Theme-based particles

### Level Auras
- Enchant (Level 5-9)
- End Rod (Level 10-14)
- Totem (Level 15-19)
- Dragon Breath (Level 20+)

## Hologram Features

### Spawn Holograms
- Kingdom name
- Level
- Member count
- Claim count

### Border Holograms
- Kingdom name
- Status information

## Theme Presets

### Royal
- Colors: Gold, Yellow, White
- Particles: Flame, Enchant
- Banner: Yellow

### Warrior
- Colors: Red, Dark Red, Gray
- Particles: Lava, Large Smoke
- Banner: Red

### Nature
- Colors: Green, Dark Green, Yellow
- Particles: Happy Villager, End Rod
- Banner: Green

### Mystic
- Colors: Dark Purple, Light Purple, White
- Particles: Portal, Enchant
- Banner: Purple

### Ocean
- Colors: Blue, Aqua, White
- Particles: Splash, Bubble
- Banner: Blue

## Commands

### Theme Management
- `/kingdom theme view` - View current theme
- `/kingdom theme preset <name>` - Apply preset
- `/kingdom theme colors <primary> <secondary> <accent>` - Set colors
- `/kingdom theme particles <primary> <secondary>` - Set particles

### Banner Management
- `/kingdom banner list` - List banners
- `/kingdom banner place <material>` - Place banner
- `/kingdom banner remove <id>` - Remove banner

## Related Systems

- [Kingdom Management](01-kingdom-management.md)
- [Land Claiming](02-land-claiming.md)
- [GUI System](22-guis.md)

