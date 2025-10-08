# Notification Reminders System - Documentation

## Overview
The notification reminders system allows users to schedule daily mood check-in reminders using Spring Scheduler. Users can set custom reminder times, frequencies, and messages.

## Features
- ✅ **Schedule Daily Reminders** - Set custom reminder times for mood check-ins
- ✅ **Multiple Frequency Options** - Daily, Weekly, Weekdays, Weekends, Custom
- ✅ **Timezone Support** - Reminders respect user timezone settings
- ✅ **Automatic Processing** - Spring Scheduler processes reminders every minute
- ✅ **Flexible Management** - Activate/deactivate, update, or delete reminders
- ✅ **Custom Messages** - Personalized reminder messages

## API Endpoints

### Schedule Reminder
```
POST /notifications/schedule
```

**Request Body:**
```json
{
  "reminderTime": "09:00:00",
  "frequency": "DAILY",
  "message": "Time for your daily mood check-in!",
  "timezone": "America/New_York"
}
```

**Response:**
```json
{
  "id": 1,
  "reminderTime": "09:00:00",
  "frequency": "DAILY",
  "isActive": true,
  "message": "Time for your daily mood check-in!",
  "timezone": "America/New_York",
  "createdAt": "2025-10-02T10:00:00",
  "lastSentAt": null
}
```

### Get User Reminders
```
GET /notifications/reminders          # All reminders
GET /notifications/reminders/active   # Active reminders only
```

### Update Reminder Status
```
PATCH /notifications/reminders/{id}/status
```

**Request Body:**
```json
{
  "isActive": false
}
```

### Update Reminder
```
PUT /notifications/reminders/{id}
```

**Request Body:** Same as schedule reminder

### Delete Reminder
```
DELETE /notifications/reminders/{id}
```

### Get Reminder Statistics
```
GET /notifications/stats
```

**Response:**
```json
{
  "totalReminders": 3,
  "activeReminders": 2,
  "inactiveReminders": 1,
  "frequencyBreakdown": {
    "DAILY": 2,
    "WEEKLY": 1
  }
}
```

### Test Reminder Processing (Development)
```
POST /notifications/test/process
```

## Reminder Frequencies

| Frequency | Description | Behavior |
|-----------|-------------|----------|
| `DAILY` | Every day | Sends reminder every day at specified time |
| `WEEKLY` | Once per week | Sends reminder once per week |
| `WEEKDAYS` | Monday-Friday only | Sends reminder only on weekdays |
| `WEEKENDS` | Saturday-Sunday only | Sends reminder only on weekends |
| `CUSTOM` | Custom schedule | Sends reminder based on custom logic |

## Scheduler Configuration

### Automatic Processing
- **Frequency**: Every 60 seconds (1 minute)
- **Logic**: Checks for reminders within ±1 minute of current time
- **Timezone**: Considers user timezone settings
- **Duplicate Prevention**: Tracks last sent time to prevent duplicates

### Health Monitoring
- **Health Check**: Every 10 minutes
- **Cleanup**: Daily at 2:00 AM (removes old inactive reminders)
- **Error Handling**: Logs errors without stopping the scheduler

## Data Model

### NotificationReminder Entity
```java
{
  "id": Long,
  "user": User,
  "reminderTime": LocalTime,
  "frequency": ReminderFrequency,
  "isActive": Boolean,
  "message": String,
  "timezone": String,
  "createdAt": LocalDateTime,
  "updatedAt": LocalDateTime,
  "lastSentAt": LocalDateTime
}
```

## Usage Examples

### Schedule a Daily 9 AM Reminder
```bash
curl -X POST http://localhost:8080/notifications/schedule \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{
    "reminderTime": "09:00:00",
    "frequency": "DAILY",
    "message": "Good morning! Time to check in with your mood.",
    "timezone": "America/New_York"
  }'
```

### Schedule a Weekday Lunch Reminder
```bash
curl -X POST http://localhost:8080/notifications/schedule \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{
    "reminderTime": "12:30:00",
    "frequency": "WEEKDAYS",
    "message": "Lunch time mood check - how are you feeling?",
    "timezone": "UTC"
  }'
```

### Get All Active Reminders
```bash
curl -X GET http://localhost:8080/notifications/reminders/active \
  -H "Authorization: Bearer {jwt_token}"
```

### Deactivate a Reminder
```bash
curl -X PATCH http://localhost:8080/notifications/reminders/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{"isActive": false}'
```

## Integration with Mood Tracking

The notification system is designed to encourage regular mood tracking:

1. **Reminder Sent**: User receives notification at scheduled time
2. **User Action**: User opens app and logs mood
3. **Analytics Update**: New mood entry updates analytics and trends
4. **Next Reminder**: System schedules next reminder based on frequency

## Advanced Features

### Timezone Handling
- User can specify timezone for reminders
- System converts reminder times to user's local time
- Supports all standard timezone identifiers (e.g., "America/New_York", "Europe/London")

### Duplicate Prevention
- Tracks `lastSentAt` timestamp for each reminder
- Prevents sending duplicate reminders on the same day
- Respects frequency settings (daily, weekly, etc.)

### Error Handling
- Graceful handling of invalid timezone
- Validation of reminder time format
- User permission checks for reminder ownership

## Configuration

### Enable Scheduling
Add `@EnableScheduling` to main application class:
```java
@SpringBootApplication
@EnableScheduling
public class MoodTrackingSystemApplication {
    // ...
}
```

### Database Schema
The system automatically creates the `notification_reminders` table with:
- Primary key (`id`)
- Foreign key to users table (`user_id`)
- Reminder configuration fields
- Audit timestamps

## Future Enhancements

1. **Push Notifications**: Integration with Firebase/APNs for mobile push notifications
2. **Email Reminders**: SMTP integration for email notifications
3. **SMS Reminders**: Twilio integration for text message reminders
4. **Smart Scheduling**: ML-based optimal reminder timing
5. **Snooze Feature**: Allow users to snooze reminders
6. **Rich Content**: Include mood insights or tips in reminders

## Security

- All notification endpoints require JWT authentication
- Users can only manage their own reminders
- Reminder ownership validation on all operations
- CORS configured for cross-origin requests

## Testing

### Manual Testing
Use the test endpoint to trigger reminder processing:
```bash
curl -X POST http://localhost:8080/notifications/test/process \
  -H "Authorization: Bearer {jwt_token}"
```

### Scheduler Health
Monitor console logs for scheduler health checks every 10 minutes:
```
Notification scheduler is running - 2025-10-02T10:00:00
```

The notification reminder system is now fully integrated with the mood tracking application, providing users with automated prompts to maintain consistent mood logging habits!