# Integration System

## Overview

The Integration system provides compatibility and integration with other popular Minecraft plugins.

## Key Components

### Integration Classes
- `DynmapIntegration.java` - Dynmap integration
- `UnminedIntegration.java` - Unmined integration
- `WorldGuardIntegration.java` - WorldGuard integration
- `GriefPreventionIntegration.java` - GriefPrevention integration
- `DiscordSRVIntegration.java` - DiscordSRV integration

### Integration Manager
- **Location**: `src/main/java/com/excrele/kingdoms/integration/IntegrationManager.java`
- **Purpose**: Manage all integrations

## Core Features

### Dynmap Integration
- **Purpose**: Show kingdoms on Dynmap web map
- **Features**: 
  - Kingdom markers
  - Claim boundaries
  - Kingdom information
- **Status**: Auto-detected and enabled

### Unmined Integration
- **Purpose**: Show kingdoms on Unmined map
- **Features**: 
  - Kingdom overlays
  - Claim visualization
- **Status**: Auto-detected and enabled

### WorldGuard Integration
- **Purpose**: Compatibility with WorldGuard
- **Features**: 
  - Region compatibility
  - Flag compatibility
- **Status**: Auto-detected and enabled

### GriefPrevention Integration
- **Purpose**: Compatibility with GriefPrevention
- **Features**: 
  - Claim compatibility
  - Protection compatibility
- **Status**: Auto-detected and enabled

### DiscordSRV Integration
- **Purpose**: Send kingdom events to Discord
- **Features**: 
  - Kingdom creation
  - War declarations
  - Level ups
  - Major events
- **Status**: Auto-detected and enabled

## Integration Detection

### Auto-Detection
- Plugins are automatically detected
- Integrations enabled if available
- Graceful degradation if not available

### Manual Configuration
```yaml
integrations:
  dynmap:
    enabled: true
  discord:
    enabled: true
    channel: kingdoms
```

## Related Systems

- [Kingdom Management](01-kingdom-management.md)
- [War System](10-war-system.md)

