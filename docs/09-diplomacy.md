# Diplomacy System

## Overview

The Diplomacy system enables formal diplomatic agreements between kingdoms, including non-aggression pacts, trade agreements, and embassies.

## Key Components

### Diplomacy Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/DiplomacyManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/DiplomaticAgreement.java`

### Agreement Types

#### 1. Non-Aggression Pact
- Prevents war declarations
- Mutual protection
- Can be time-limited or permanent
- **Requirement**: Both kingdoms must accept

#### 2. Trade Agreement
- Provides trade bonuses (10% discount)
- Enables priority trading
- Reduces trade fees
- **Requirement**: Both kingdoms must accept

#### 3. Embassy
- Establishes formal diplomatic relations
- Requires embassy structures in both kingdoms
- Enables advanced diplomacy
- **Requirement**: Embassy structures must exist

#### 4. Mutual Defense
- Defense pact between kingdoms
- Automatic assistance in wars
- Shared war objectives
- **Requirement**: Both kingdoms must accept

## Commands

### Diplomacy Management
- `/kingdom diplomacy list` - List all agreements
- `/kingdom diplomacy propose <kingdom> <type> [duration]` - Propose agreement
- `/kingdom diplomacy accept <id>` - Accept agreement
- `/kingdom diplomacy terminate <id>` - Terminate agreement

## Agreement Lifecycle

1. **Proposal**: Kingdom A proposes to Kingdom B
2. **Pending**: Kingdom B receives notification
3. **Acceptance**: Kingdom B accepts
4. **Active**: Agreement is active
5. **Expiration**: Automatic expiration (if time-limited)
6. **Termination**: Either kingdom can terminate

## Integration

### War System
- Non-aggression pacts prevent war
- Mutual defense activates during wars
- See [War System](10-war-system.md)

### Trade System
- Trade agreements provide bonuses
- Reduced fees and priority trading
- See [Trade Routes](13-trade-routes.md)

### Structures
- Embassy agreements require embassy structures
- See [Structures](14-structures.md)

## Storage

Diplomatic agreements stored in:
- **YAML**: `diplomacy.yml`
- **MySQL**: `diplomatic_agreements` table
- **SQLite**: `diplomatic_agreements` table

## Related Systems

- [War System](10-war-system.md)
- [Trade Routes](13-trade-routes.md)
- [Structures](14-structures.md)

