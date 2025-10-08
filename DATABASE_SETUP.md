# Database Setup Instructions

## Prerequisites
1. Install MySQL Server 8.0 or later
2. Create a database and user for the application

## Database Setup Commands

```sql
-- Create database
CREATE DATABASE mood_tracking_db;

-- Create user (replace 'mood_password' with a secure password)
CREATE USER 'mood_user'@'localhost' IDENTIFIED BY 'mood_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON mood_tracking_db.* TO 'mood_user'@'localhost';

-- Flush privileges
FLUSH PRIVILEGES;
```

## Application Configuration
Update the following properties in `application.properties`:
- `spring.datasource.username`: Your database username
- `spring.datasource.password`: Your database password
- `spring.datasource.url`: Your database URL

## Tables
The application will automatically create the following tables:
- `users`: Store user account information
- `mood_entries`: Store mood tracking data

## Sample Data
After running the application, you can test it with these API endpoints:
- POST `/api/auth/signup` - Register a new user
- POST `/api/auth/signin` - Login
- POST `/api/mood/entries` - Create mood entry
- GET `/api/mood/entries` - Get user's mood entries
- GET `/api/mood/recommendations` - Get mood-based recommendations