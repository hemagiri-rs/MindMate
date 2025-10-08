# MindMate - Mental Health Companion App

A modern, lightweight mental health tracking application built with vanilla JavaScript and Vite.

## Features

- 📊 **Dashboard**: Overview of your mental health journey with statistics
- 📝 **Notes**: Create and organize personal notes with categories and tags
- ✅ **Tasks**: Manage your daily tasks with priorities and due dates
- 😊 **Mood Tracker**: Log and track your emotional wellbeing over time
- 🤖 **AI Assistant**: Chat with an AI-powered mental health companion

## Tech Stack

- **Build System**: Vite 7.1.9
- **Styling**: Tailwind CSS 4.1.9
- **Frontend**: Vanilla JavaScript (ES6+)
- **Storage**: Local Storage for client-side data persistence
- **Icons**: Font Awesome

## Getting Started

### Prerequisites

- Node.js (v18 or higher)
- npm or pnpm

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Project Structure

```
v0/
├── src/
│   ├── index.html              # Main HTML entry point
│   ├── js/
│   │   ├── app.js              # Main application controller
│   │   ├── config.js           # Configuration constants
│   │   ├── components/         # Feature components
│   │   └── utils/              # Utility functions
│   ├── css/
│   │   ├── main.css            # Global styles
│   │   └── components/         # Component-specific styles
│   ├── components/             # HTML templates
│   └── assets/                 # Images and static files
├── public/                     # Public static assets
├── vite.config.js              # Vite configuration
└── package.json                # Project dependencies
```

## Development

The app runs on `http://localhost:5173` by default. 

### Available Scripts

- `npm run dev` - Start development server with hot reload
- `npm run build` - Build for production (outputs to `dist/`)
- `npm run preview` - Preview production build locally

## Features Overview

### Dashboard
View your mental health statistics at a glance with interactive charts and metrics.

### Notes
- Create rich text notes
- Organize with categories and tags
- Search and filter functionality
- Color-coded organization

### Tasks
- Add tasks with priorities (High, Medium, Low)
- Set due dates and categories
- Track completion status
- Filter by category and priority

### Mood Tracker
- Log daily mood (Excellent, Good, Neutral, Low, Very Low)
- Add optional notes for context
- View mood history
- Visual mood indicators

### AI Assistant
Chat with an AI companion for mental health support and guidance (requires backend API).

## Backend Integration

The app expects a backend API running on `http://localhost:7070` for:
- AI chatbot functionality
- Data synchronization (optional)
- User authentication (future feature)

Without the backend, the app functions fully with local storage.

## Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)

## License

Private - All rights reserved

## Contributing

This is a personal project. Feel free to fork and modify for your own use.
