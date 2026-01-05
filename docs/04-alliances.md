# Alliance System

## Overview

The Alliance system allows kingdoms to form diplomatic relationships, enabling cooperation and mutual benefits.

## Key Components

### Alliance Storage
- Stored in `Kingdom.alliances` list
- Managed by `KingdomManager`

### Core Features

#### Alliance Benefits
- **Build Access**: Allies can build in each other's claims
- **Visual Indicators**: Shown as `[A]` on claim maps
- **Particle Colors**: Different particle effects for allies
- **Trade Bonuses**: Reduced trade costs (if configured)
- **War Cooperation**: Can form war alliances

#### Alliance Management
- **Invitations**: Kings and Advisors can invite
- **Acceptance**: Both kingdoms must accept
- **Removal**: Either kingdom can dissolve alliance
- **Limits**: Configurable maximum alliances per kingdom

## Commands

### Alliance Management
- `/kingdom alliance invite <kingdom>` - Request alliance
- `/kingdom alliance accept <kingdom>` - Accept request
- `/kingdom alliance deny <kingdom>` - Deny request
- `/kingdom alliance remove <kingdom>` - Dissolve alliance
- `/kingdom alliance list` - View all alliances

## Alliance Lifecycle

1. **Invitation**: Kingdom A invites Kingdom B
2. **Pending**: Kingdom B receives notification
3. **Acceptance**: Kingdom B accepts
4. **Active**: Alliance is formed
5. **Dissolution**: Either kingdom can remove

## Visual Indicators

### Claim Map
- Allied kingdoms shown as `[A]`
- Different color coding
- Easy identification

### Particles
- Different particle effects when entering allied claims
- Theme-based particles if configured

## Integration with Other Systems

### War System
- Alliances can form war coalitions
- Mutual defense pacts possible
- Alliance-based war objectives

### Trade System
- Trade bonuses with allies
- Reduced fees
- Priority trading

### Diplomacy System
- Alliances are a form of diplomatic agreement
- Can be upgraded to more formal agreements
- See [Diplomacy System](09-diplomacy.md)

## Storage

Alliances are stored in:
- **YAML**: `kingdoms.yml` (within kingdom data)
- **MySQL**: `kingdoms.alliances` column
- **SQLite**: `kingdoms.alliances` column

## Related Systems

- [Kingdom Management](01-kingdom-management.md)
- [Diplomacy System](09-diplomacy.md)
- [War System](10-war-system.md)

