# Social Features System

## Overview

The Social Features system enhances player interaction and engagement through messaging, achievements, activity tracking, and member management.

## Key Components

### Mail Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/MailManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/Mail.java`

### Achievement Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/AchievementManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/MemberAchievement.java`

### Activity Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/ActivityManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/PlayerActivity.java`

### Communication Manager
- **Location**: `src/main/java/com/excrele/kingdoms/manager/CommunicationManager.java`
- **Model**: `src/main/java/com/excrele/kingdoms/model/KingdomAnnouncement.java`

## Core Features

### In-Game Mail
- **Send Mail**: Send messages to offline members
- **Mail Inbox**: View all received mail
- **Mail Notifications**: Notify on login
- **Mail Management**: Read, delete, mark as read
- **Broadcast Mail**: Send to all members

### Member Achievements
- **Achievement Types**: Contributions, streaks, challenges, levels
- **Achievement Unlocking**: Automatic unlocking
- **Achievement Display**: Show in GUIs
- **Achievement Rewards**: Optional rewards

### Activity Tracking
- **Last Seen**: Track when members were last online
- **Playtime**: Track total playtime
- **Contribution Streaks**: Track consecutive contribution days
- **Activity Scores**: Calculate activity metrics

### Contribution Streaks
- **Daily Contributions**: Track daily contributions
- **Streak Counting**: Count consecutive days
- **Streak Rewards**: Bonuses for long streaks
- **Streak Display**: Show in member info

### Member Notes
- **Personal Notes**: Notes about members
- **Private Notes**: Only visible to note creator
- **Note Management**: Add, edit, delete notes

### Kingdom Chat
- **Chat Channel**: Dedicated kingdom chat
- **Chat Toggle**: Switch between global and kingdom chat
- **Chat Commands**: `/kc <message>` shortcut

### Announcements
- **Kingdom Announcements**: Broadcast messages
- **Announcement Board**: View all announcements
- **Announcement Management**: Create, edit, delete

## Commands

### Mail
- `/kingdom mail inbox` - View mail
- `/kingdom mail send <player> <subject> <message>` - Send mail
- `/kingdom mail broadcast <subject> <message>` - Broadcast mail
- `/kingdom mail read <id>` - Read mail
- `/kingdom mail delete <id>` - Delete mail

### Achievements
- `/kingdom achievements` - View achievements
- `/kingdom achievements <player>` - View player achievements

### Activity
- `/kingdom activity` - View activity
- `/kingdom activity <player>` - View player activity
- `/kingdom streak` - View contribution streak

### Communication
- `/kingdom chat` - Toggle kingdom chat
- `/kc <message>` - Send kingdom chat message
- `/kingdom announce <message>` - Create announcement

## Integration

### Login System
- Mail delivered on login
- Activity tracked on login/logout
- See [Activity Tracking](19-activity.md)

### Challenge System
- Achievements unlock from challenges
- See [Challenge System](15-challenges.md)

## Storage

Social data stored in:
- **YAML**: `mail.yml`, `achievements.yml`, `activity.yml`
- **MySQL**: `player_mail`, `player_achievements`, `player_activity` tables
- **SQLite**: `player_mail`, `player_achievements`, `player_activity` tables

## Related Systems

- [Kingdom Management](01-kingdom-management.md)
- [Challenge System](15-challenges.md)
- [Activity Tracking](19-activity.md)

