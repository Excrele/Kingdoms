# Economy System

## Overview

The Economy system provides comprehensive financial management for kingdoms, including banking, trading, and economic interactions.

## Key Components

### Bank Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/BankManager.java`
- **Integration**: Uses Vault API for economy

### Claim Economy Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/ClaimEconomyManager.java`
- **Features**: Claim selling, buying, auctions, renting

### Tax Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/TaxManager.java`
- **Features**: Automatic tax collection

### Trade Route Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/TradeRouteManager.java`
- **Features**: Inter-kingdom trading

## Core Features

### Kingdom Bank
- **Shared Treasury**: All members contribute to kingdom bank
- **Deposits**: Members can deposit money
- **Withdrawals**: Permission-based withdrawals
- **Capacity**: Configurable max capacity
- **Treasury Structure**: Increases bank capacity

### Claim Economy
- **Selling Claims**: Kingdoms can sell chunks
- **Buying Claims**: Purchase chunks from other kingdoms
- **Auctions**: Bid on available claims
- **Renting**: Rent chunks temporarily
- **Marketplace**: Browse available claims

### Tax System
- **Automatic Collection**: Scheduled tax collection
- **Configurable Rates**: Set tax percentage
- **Collection Interval**: Configurable timing
- **Member Contributions**: Automatic deduction
- **Bank Deposit**: Taxes go to kingdom bank

### Trade Routes
- **Establish Routes**: Create trade routes between kingdoms
- **Trade Execution**: Execute trades over routes
- **Trade Bonuses**: Diplomatic agreements provide bonuses
- **Resource Trading**: Trade resources between kingdoms

## Commands

### Banking
- `/kingdom bank balance` - View bank balance
- `/kingdom bank deposit <amount>` - Deposit money
- `/kingdom bank withdraw <amount>` - Withdraw money (permission required)
- `/kingdom bank transfer <kingdom> <amount>` - Transfer to another kingdom

### Claim Economy
- `/kingdom claim sell <price>` - Sell current chunk
- `/kingdom claim buy <kingdom>` - Buy chunk from kingdom
- `/kingdom claim auction <price> <duration>` - Auction chunk
- `/kingdom claim rent <price> <duration>` - Rent chunk
- `/kingdom claim marketplace` - View available claims

### Tax System
- `/kingdom tax setrate <percentage>` - Set tax rate (King/Advisor)
- `/kingdom tax setinterval <seconds>` - Set collection interval
- `/kingdom tax status` - View tax configuration

### Trade Routes
- `/kingdom traderoute establish <kingdom>` - Create trade route
- `/kingdom traderoute trade <kingdom> <resource> <amount> <price>` - Execute trade
- `/kingdom traderoute list` - List all routes
- `/kingdom traderoute remove <kingdom>` - Remove route

## Economy Integration

### Vault Integration
- Uses Vault API for economy
- Supports all Vault-compatible economy plugins
- Automatic economy detection

### Resource Management
- Kingdom resource storage
- Resource trading
- Resource requirements for actions
- See [Resource Management](11-resources.md)

## Storage

Economy data stored in:
- **YAML**: `kingdoms.yml` (bank balances)
- **MySQL**: `kingdoms.bank_balance` column
- **SQLite**: `kingdoms.bank_balance` column

## Related Systems

- [Kingdom Management](01-kingdom-management.md)
- [Resource Management](11-resources.md)
- [Tax System](12-tax-system.md)
- [Trade Routes](13-trade-routes.md)

