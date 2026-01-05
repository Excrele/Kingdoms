# Trade Routes System

## Overview

The Trade Routes system enables formal trading relationships between kingdoms, facilitating resource and economic exchange.

## Key Components

### Trade Route Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/TradeRouteManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/TradeRoute.java`

### Core Features

#### Route Establishment
- **Bilateral Agreement**: Both kingdoms must agree
- **Route Status**: Active/inactive routes
- **Route History**: Track trade history
- **Route Benefits**: Bonuses for active routes

#### Trade Execution
- **Resource Trading**: Trade resources between kingdoms
- **Price Negotiation**: Set trade prices
- **Trade Bonuses**: Diplomatic agreements provide bonuses
- **Trade Logs**: Record all trades

## Commands

### Trade Route Management
- `/kingdom traderoute establish <kingdom>` - Create trade route
- `/kingdom traderoute trade <kingdom> <resource> <amount> <price>` - Execute trade
- `/kingdom traderoute list` - List all routes
- `/kingdom traderoute remove <kingdom>` - Remove route
- `/kingdom traderoute history <kingdom>` - View trade history

## Trade Process

### Route Establishment
1. Kingdom A proposes route to Kingdom B
2. Kingdom B accepts
3. Route becomes active
4. Both kingdoms can trade

### Trade Execution
1. Check route exists and is active
2. Verify resources available
3. Verify buyer has money
4. Execute trade
5. Transfer resources
6. Process payment
7. Log trade

## Trade Bonuses

### Diplomatic Agreements
- **Trade Agreement**: 10% discount
- **Embassy**: Additional bonuses
- **Alliance**: Small bonus

### Combined Bonuses
Bonuses stack multiplicatively:
```
Final Price = Base Price × (1 - Trade Agreement Bonus) × (1 - Embassy Bonus)
```

## Integration

### Resource Management
- Uses resource storage system
- See [Resource Management](11-resources.md)

### Diplomacy System
- Trade agreements enable routes
- See [Diplomacy System](09-diplomacy.md)

### Economy System
- Uses kingdom banks
- See [Economy System](05-economy.md)

## Storage

Trade route data stored in:
- **YAML**: `trade_routes.yml`
- **MySQL**: `trade_routes` table
- **SQLite**: `trade_routes` table

## Related Systems

- [Resource Management](11-resources.md)
- [Diplomacy System](09-diplomacy.md)
- [Economy System](05-economy.md)

