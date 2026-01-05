# Tax System

## Overview

The Tax system automatically collects taxes from kingdom members and deposits them into the kingdom bank.

## Key Components

### Tax Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/TaxManager.java`
- **Integration**: Uses `BankManager` and `EconomyManager`

### Core Features

#### Automatic Collection
- **Scheduled Collection**: Configurable collection interval
- **Tax Rate**: Percentage-based taxation
- **Member Deduction**: Automatic deduction from members
- **Bank Deposit**: Taxes go to kingdom bank

#### Tax Configuration
- **Tax Rate**: Set percentage (0-100%)
- **Collection Interval**: Time between collections
- **Minimum Balance**: Don't tax below threshold
- **Exemptions**: Can exempt specific members

## Commands

### Tax Management
- `/kingdom tax setrate <percentage>` - Set tax rate (King/Advisor)
- `/kingdom tax setinterval <seconds>` - Set collection interval
- `/kingdom tax status` - View tax configuration
- `/kingdom tax exempt <player>` - Exempt player from taxes

## Tax Calculation

### Formula
```
Tax Amount = Player Balance × (Tax Rate / 100)
```

### Collection Process
1. Check collection interval
2. Get all kingdom members
3. Calculate tax for each member
4. Withdraw from player
5. Deposit to kingdom bank
6. Notify members

## Configuration

### Default Settings
- **Tax Rate**: 5%
- **Collection Interval**: 86400 seconds (1 day)
- **Minimum Balance**: 0 (no minimum)

### Tax Limits
- **Maximum Rate**: 100% (configurable)
- **Minimum Rate**: 0%
- **Collection Frequency**: Minimum 1 hour

## Integration

### Economy System
- Uses Vault API for player money
- See [Economy System](05-economy.md)

### Bank System
- Deposits to kingdom bank
- See [Economy System](05-economy.md)

## Storage

Tax configuration stored in:
- **YAML**: `kingdoms.yml` (within kingdom data)
- **MySQL**: `kingdoms.tax_config` column
- **SQLite**: `kingdoms.tax_config` column

## Related Systems

- [Economy System](05-economy.md)
- [Kingdom Management](01-kingdom-management.md)

