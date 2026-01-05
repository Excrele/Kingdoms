# Resource Management System

## Overview

The Resource Management system provides kingdom-wide resource storage, trading, and resource-based requirements for actions.

## Key Components

### Resource Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/ResourceManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/KingdomResource.java`

### Core Features

#### Resource Storage
- **Kingdom Storage**: Centralized resource storage
- **Capacity Limits**: Configurable storage capacity
- **Capacity Scaling**: Increases with kingdom level
- **Granary Structure**: Adds bonus capacity

#### Resource Operations
- **Deposit**: Members can deposit resources
- **Withdraw**: Members can withdraw resources
- **Trading**: Inter-kingdom resource trading
- **Requirements**: Actions can require resources

#### Resource Types
- **Materials**: Minecraft materials (diamonds, iron, etc.)
- **Custom Resources**: Server-defined resources
- **Resource Values**: Estimated values for trading

## Commands

### Resource Management
- `/kingdom resource list` - List all resources
- `/kingdom resource deposit <material> <amount>` - Deposit resources
- `/kingdom resource withdraw <material> <amount>` - Withdraw resources
- `/kingdom resource trade <kingdom> <material> <amount> <price>` - Trade resources

## Storage Capacity

### Base Capacity
- **Starting**: 1000 units
- **Per Level**: +100 units per level
- **Granary Bonus**: +500 units (if structure exists)

### Formula
```
Capacity = 1000 + (Level × 100) + (Granary ? 500 : 0)
```

## Resource Trading

### Trade Requirements
- **Trade Route**: Must have active trade route
- **Resources**: Must have sufficient resources
- **Money**: Buyer must have enough money
- **Agreement**: Trade agreements provide bonuses

### Trade Process
1. Establish trade route
2. Negotiate trade terms
3. Execute trade
4. Resources transferred
5. Payment processed

## Integration

### Trade Routes
- Requires active trade route
- See [Trade Routes](13-trade-routes.md)

### Structures
- Granary increases capacity
- See [Structures](14-structures.md)

### Economy
- Resources can be valued in money
- See [Economy System](05-economy.md)

## Storage

Resource data stored in:
- **YAML**: `resources.yml`
- **MySQL**: `kingdom_resources` table
- **SQLite**: `kingdom_resources` table

## Related Systems

- [Economy System](05-economy.md)
- [Trade Routes](13-trade-routes.md)
- [Structures](14-structures.md)

