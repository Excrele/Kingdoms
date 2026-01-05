# Trust System

## Overview

The Trust system allows kingdoms to grant specific permissions to non-members, enabling controlled access to kingdom territory.

## Key Components

### Trust Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/TrustManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/TrustPermission.java`

### Core Features

#### Trust Permissions
Grant specific permissions to trusted players:
- `build` - Allow building
- `break` - Allow breaking blocks
- `interact` - Allow interactions
- `container` - Allow container access
- `pvp` - Allow PvP
- `entry` - Allow entry to claims

#### Trust Types
- **Kingdom Trust**: Applies to all kingdom claims
- **Plot Trust**: Applies to specific chunks
- **Temporary Trust**: Trust with expiration time

#### Trust Management
- **Grant Trust**: Kings and Advisors can grant trust
- **Revoke Trust**: Can be removed at any time
- **Trust List**: View all trusted players
- **Trust Expiration**: Automatic removal after expiration

## Commands

### Trust Management
- `/kingdom trust add <player> <permission>` - Grant trust
- `/kingdom trust remove <player>` - Revoke trust
- `/kingdom trust list` - List trusted players
- `/kingdom trust plot <player> <permission>` - Grant plot trust

## Permission Hierarchy

1. **Kingdom Members** (full access)
2. **Allies** (limited access)
3. **Trusted Players** (specific permissions)
4. **Everyone Else** (no access)

## Storage

Trust data stored in:
- **YAML**: `kingdoms.yml` (within kingdom data)
- **MySQL**: `trusts` table
- **SQLite**: `trusts` table

## Related Systems

- [Kingdom Management](01-kingdom-management.md)
- [Role & Permissions](03-roles-permissions.md)
- [Land Claiming](02-land-claiming.md)

