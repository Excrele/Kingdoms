# Performance & Caching System

## Overview

The Performance system optimizes plugin performance through caching, chunk optimization, and async operations.

## Key Components

### Data Cache
- **Location**: `src/main/java/com/excrele/kingdoms/util/DataCache.java`
- **Purpose**: Cache frequently accessed data

### Chunk Loading Optimizer
- **Location**: `src/main/java/com/excrele/kingdoms/util/ChunkLoadingOptimizer.java`
- **Purpose**: Optimize chunk loading and claim lookups

### Save Queue
- **Location**: `src/main/java/com/excrele/kingdoms/util/SaveQueue.java`
- **Purpose**: Async file I/O operations

### Batch Save Task
- **Location**: `src/main/java/com/excrele/kingdoms/task/BatchSaveTask.java`
- **Purpose**: Batch save operations

## Core Features

### Data Caching
- **Kingdom Cache**: Cache kingdom data
- **Player Cache**: Cache player-kingdom mappings
- **Bank Cache**: Cache bank balances
- **Cache Expiration**: Automatic expiration
- **Cache Cleanup**: Periodic cleanup

### Chunk Optimization
- **Preloading**: Preload chunks around players
- **Access Tracking**: Track chunk access times
- **Unloading**: Unload distant chunks
- **Batch Loading**: Batch load claim data

### Async Operations
- **File I/O**: All saves are async
- **Database**: Async database operations
- **Batch Processing**: Batch multiple operations
- **Queue System**: Queue operations for processing

## Cache System

### Cache Types

#### Kingdom Cache
- **Key**: Kingdom name
- **Value**: Kingdom object
- **Expiry**: 5 minutes (configurable)
- **Invalidation**: On kingdom changes

#### Player Cache
- **Key**: Player name
- **Value**: Kingdom name
- **Expiry**: 5 minutes (configurable)
- **Invalidation**: On membership changes

#### Bank Cache
- **Key**: Kingdom name
- **Value**: Bank balance
- **Expiry**: 5 minutes (configurable)
- **Invalidation**: On balance changes

### Cache Operations
- **Get**: Retrieve from cache
- **Put**: Store in cache
- **Invalidate**: Remove from cache
- **Clear**: Clear all caches
- **Clean**: Remove expired entries

## Chunk Optimization

### Preloading
- **Radius**: 2 chunks around player (configurable)
- **Trigger**: Player movement
- **Benefit**: Faster claim lookups

### Access Tracking
- **Tracking**: Record last access time
- **Purpose**: Identify unused chunks
- **Cleanup**: Remove old entries

### Unloading
- **Delay**: 5 minutes (configurable)
- **Condition**: Not near any player
- **Benefit**: Reduce memory usage

## Async Operations

### Save Queue
- **Queue System**: Queue save operations
- **Batch Processing**: Process in batches
- **Error Handling**: Retry on failure
- **Priority**: Priority-based processing

### Batch Save Task
- **Interval**: Every 5 minutes (configurable)
- **Operations**: Batch multiple saves
- **Benefit**: Reduce I/O operations

## Performance Metrics

### Cache Statistics
- **Cache Size**: Number of cached entries
- **Hit Rate**: Cache hit percentage
- **Miss Rate**: Cache miss percentage
- **Eviction Count**: Number of evictions

### Chunk Statistics
- **Tracked Chunks**: Number of tracked chunks
- **Loaded Chunks**: Number of loaded chunks
- **Active Players**: Number of active players

## Configuration

### Cache Settings
```yaml
cache:
  expiry-time: 300000  # 5 minutes in milliseconds
  max-size: 1000
  cleanup-interval: 300000
```

### Chunk Optimization
```yaml
chunk-optimization:
  preload-radius: 2
  unload-delay: 300000  # 5 minutes
  batch-size: 100
```

## Related Systems

- [Kingdom Management](01-kingdom-management.md)
- [Land Claiming](02-land-claiming.md)
- [Storage Systems](21-storage.md)

