# API Testing Guide

## Application is Running Successfully! ✅

The 401 Unauthorized error means your Spring Security is working correctly. All endpoints except `/api/auth/register` and `/api/auth/login` require authentication.

## Base URL
```
http://localhost:7070
```

## Step-by-Step Testing

### 1. Register a New User
**Endpoint:** `POST http://localhost:7070/api/auth/register`

**Request Body (JSON):**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "testuser",
  "email": "test@example.com"
}
```

### 2. Login
**Endpoint:** `POST http://localhost:7070/api/auth/login`

**Request Body (JSON):**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "testuser",
  "email": "test@example.com"
}
```

**⚠️ Important:** Copy the `token` value - you'll need it for all other requests!

### 3. Create a Mood Entry (Requires Authentication)
**Endpoint:** `POST http://localhost:7070/api/mood/entries`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Request Body (JSON):**
```json
{
  "moodType": "HAPPY",
  "intensity": 8,
  "notes": "Had a great day at work!",
  "activities": ["exercise", "socializing"]
}
```

**Valid Mood Types:**
- HAPPY
- SAD
- ANXIOUS
- STRESSED
- CALM
- ANGRY
- EXCITED
- TIRED
- RELAXED
- ENERGETIC

**Expected Response:**
```json
{
  "id": 1,
  "moodType": "HAPPY",
  "intensity": 8,
  "notes": "Had a great day at work!",
  "activities": ["exercise", "socializing"],
  "timestamp": "2025-10-02T23:15:00"
}
```

### 4. Get All Mood Entries (Requires Authentication)
**Endpoint:** `GET http://localhost:7070/api/mood/entries`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "moodType": "HAPPY",
    "intensity": 8,
    "notes": "Had a great day at work!",
    "activities": ["exercise", "socializing"],
    "timestamp": "2025-10-02T23:15:00"
  }
]
```

### 5. Get Mood Analytics (Requires Authentication)
**Endpoint:** `GET http://localhost:7070/api/mood/analytics`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Optional Query Parameters:**
- `startDate`: Start date (format: YYYY-MM-DD)
- `endDate`: End date (format: YYYY-MM-DD)

**Example:**
```
GET http://localhost:7070/api/mood/analytics?startDate=2025-10-01&endDate=2025-10-31
```

**Expected Response:**
```json
{
  "totalEntries": 5,
  "daysTracked": 3,
  "averageIntensity": 7.2,
  "mostFrequentMood": "HAPPY",
  "moodDistribution": {
    "HAPPY": 3,
    "SAD": 1,
    "ANXIOUS": 1
  },
  "intensityTrend": "IMPROVING",
  "commonActivities": ["exercise", "socializing", "reading"]
}
```

### 6. Get Mood Recommendations (Requires Authentication)
**Endpoint:** `GET http://localhost:7070/api/mood/recommendations`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Expected Response:**
```json
{
  "recommendations": [
    {
      "title": "Exercise Regularly",
      "description": "Physical activity boosts mood...",
      "category": "PHYSICAL_ACTIVITY"
    },
    {
      "title": "Practice Mindfulness",
      "description": "Meditation can reduce stress...",
      "category": "MENTAL_HEALTH"
    }
  ]
}
```

## Testing Tools

### Option 1: Using cURL (Command Line)

#### Register:
```bash
curl -X POST http://localhost:7070/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password123\"}"
```

#### Login:
```bash
curl -X POST http://localhost:7070/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser\",\"password\":\"password123\"}"
```

#### Create Mood Entry (replace YOUR_TOKEN with actual token):
```bash
curl -X POST http://localhost:7070/api/mood/entries \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"moodType\":\"HAPPY\",\"intensity\":8,\"notes\":\"Great day!\",\"activities\":[\"exercise\"]}"
```

### Option 2: Using Postman
1. Download Postman from https://www.postman.com/downloads/
2. Create a new request
3. Set the method (GET/POST)
4. Enter the URL
5. For protected endpoints, add Authorization header: `Bearer YOUR_TOKEN`
6. For POST requests, add JSON body

### Option 3: Using Browser (Limited)
- You can only test GET endpoints in browser after getting a token
- For POST requests, use cURL or Postman

## Common Issues

### 401 Unauthorized
- **Cause:** Missing or invalid token
- **Solution:** Make sure to include `Authorization: Bearer YOUR_TOKEN` header

### 403 Forbidden
- **Cause:** Token expired or invalid
- **Solution:** Login again to get a new token

### 400 Bad Request
- **Cause:** Invalid request body or missing required fields
- **Solution:** Check your JSON format and required fields

## Health Check Endpoint
**Endpoint:** `GET http://localhost:7070/api/health`

This endpoint is public and doesn't require authentication.

**Expected Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-10-02T23:15:00"
}
```

## Next Steps
1. ✅ Register a user
2. ✅ Login to get JWT token
3. ✅ Use the token in Authorization header for all subsequent requests
4. ✅ Test creating mood entries
5. ✅ Test getting analytics and recommendations
