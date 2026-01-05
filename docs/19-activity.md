# Activity Tracking System

## Overview

The Activity Tracking system monitors player activity, contributions, and engagement to help kingdoms manage their members effectively.

## Key Components

### Activity Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/ActivityManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/PlayerActivity.java`

### Core Features

#### Activity Metrics
- **Last Login**: Track when players were last online
- **Playtime**: Track total playtime in kingdom
- **Last Contribution**: Track last contribution time
- **Contribution Count**: Total contributions made
- **Contribution Streak**: Consecutive days of contributions

#### Contribution Tracking
- **Daily Contributions**: Track contributions per day
- **Streak Calculation**: Count consecutive contribution days
- **Streak Reset**: Reset on missed day
- **Streak Rewards**: Optional bonuses for streaks

#### Activity Scores
- **Activity Calculation**: Formula-based activity score
- **Inactivity Detection**: Identify inactive members
- **Activity Reports**: Generate activity reports

## Commands

### Activity Management
- `/kingdom activity` - View your activity
- `/kingdom activity <player>` - View player activity
- `/kingdom streak` - View contribution streak
- `/kingdom members activity` - List all members' activity

## Activity Metrics

### Last Login
- **Tracking**: Recorded on player join
- **Display**: Shown in member info
- **Format**: Relative time (e.g., "2 days ago")

### Playtime
- **Tracking**: Accumulated while online
- **Display**: Total hours/minutes
- **Calculation**: Sum of all sessions

### Contributions
- **Tracking**: Recorded on challenge completion
- **Count**: Total contributions
- **Last**: Last contribution timestamp

### Contribution Streaks
- **Daily Tracking**: Contributions per day
- **Streak Count**: Consecutive days
- **Streak Reset**: Missed day resets streak
- **Longest Streak**: Track record streak

## Inactivity Detection

### Criteria
- **No Login**: Not logged in for X days
- **No Contributions**: No contributions for X days
- **Low Activity**: Low activity score

### Auto-Kick
- **Configurable**: Enable/disable auto-kick
- **Threshold**: Days of inactivity
- **Notification**: Warn before kick
- **Grace Period**: Time to become active

## Integration

### Challenge System
- Contributions tracked from challenges
- See [Challenge System](15-challenges.md)

### Achievement System
- Activity unlocks achievements
- See [Social Features](17-social.md)

### Mail System
- Activity notifications via mail
- See [Social Features](17-social.md)

## Storage

Activity data stored in:
- **YAML**: `activity.yml`
- **MySQL**: `player_activity` table
- **SQLite**: `player_activity` table

## Related Systems

- [Challenge System](15-challenges.md)
- [Social Features](17-social.md)
- [Kingdom Management](01-kingdom-management.md)

