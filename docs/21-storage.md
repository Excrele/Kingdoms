# Storage Systems

## Overview

The Storage system provides flexible data persistence through multiple storage adapters (YAML, MySQL, SQLite).

## Key Components

### Storage Manager
- **Location**: `src/main/java/com/excrele/kingdoms/storage/StorageManager.java`
- **Purpose**: Manage storage adapters

### Storage Adapter Interface
- **Location**: `src/main/java/com/excrele/kingdoms/storage/StorageAdapter.java`
- **Purpose**: Define storage operations

### YAML Storage Adapter
- **Location**: `src/main/java/com/excrele/kingdoms/storage/YamlStorageAdapter.java`
- **Purpose**: YAML file storage

### MySQL Storage Adapter
- **Location**: `src/main/java/com/excrele/kingdoms/storage/MySQLStorageAdapter.java`
- **Purpose**: MySQL database storage

### SQLite Storage Adapter
- **Location**: `src/main/java/com/excrele/kingdoms/storage/SQLiteStorageAdapter.java`
- **Purpose**: SQLite database storage

## Storage Types

### YAML Storage
- **Format**: YAML files
- **Location**: Plugin data folder
- **Files**: 
  - `kingdoms.yml`
  - `player_data.yml`
  - `challenges.yml`
  - `wars.yml`
  - `mail.yml`
  - And more...

### MySQL Storage
- **Format**: MySQL database
- **Tables**: 
  - `kingdoms`
  - `claims`
  - `player_data`
  - `wars`
  - `mail`
  - And more...

### SQLite Storage
- **Format**: SQLite database file
- **Location**: Plugin data folder
- **File**: `kingdoms.db`
- **Tables**: Same as MySQL

## Storage Operations

### Kingdom Operations
- `saveKingdom(Kingdom)` - Save kingdom
- `loadKingdom(String)` - Load kingdom
- `loadAllKingdomNames()` - Load all names
- `deleteKingdom(String)` - Delete kingdom

### Claim Operations
- `saveClaim(String, Chunk, String)` - Save claim
- `deleteClaim(String, Chunk)` - Delete claim
- `loadAllClaims()` - Load all claims

### Player Operations
- `savePlayerActivity(...)` - Save activity
- `loadPlayerActivity(String)` - Load activity
- `savePlayerChallengeData(...)` - Save challenge data

### And More...
- War operations
- Mail operations
- Structure operations
- Resource operations
- Diplomacy operations
- And many more...

## Configuration

### Storage Type Selection
```yaml
storage:
  type: yaml  # yaml, mysql, or sqlite
```

### MySQL Configuration
```yaml
storage:
  type: mysql
  mysql:
    host: localhost
    port: 3306
    database: kingdoms
    username: root
    password: password
```

### SQLite Configuration
```yaml
storage:
  type: sqlite
  sqlite:
    file: kingdoms.db
```

## Async Operations

### Save Queue
- All saves go through save queue
- Async processing
- Batch operations
- Error handling

### Batch Saves
- Periodic batch saves
- Reduce I/O operations
- Improve performance

## Migration

### Between Storage Types
- Automatic migration support
- Data preservation
- Validation

## Related Systems

- [Performance & Caching](20-performance.md)
- [Kingdom Management](01-kingdom-management.md)

