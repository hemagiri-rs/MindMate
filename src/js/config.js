export const config = {
    // App information
    appName: 'MindMate',
    version: '1.0.0',
    
    // API Configuration
    api: {
        baseUrl: 'https://mindmate-02n0j.sevalla.app',
        timeout: 10000,
        endpoints: {
            auth: {
                login: '/api/auth/login',
                register: '/api/auth/register'
            },
            user: '/user/profile',
            notes: '/api/notes',
            tasks: '/api/tasks',
            mood: '/api/mood',
            analytics: '/analytics/mood',
            health: '/api/health/status'
        }
    },
    
    // Feature flags
    features: {
        aiEnabled: false,
        moodTracking: true,
        taskSuggestions: false,
        noteAnalysis: false
    },

    // Storage keys
    storage: {
        token: 'mindmate_token',
        userData: 'mindmate_user',
        notes: 'mindmate_notes',
        tasks: 'mindmate_tasks',
        moods: 'mindmate_moods',
        settings: 'mindmate_settings'
    },

    // Default settings
    defaults: {
        theme: 'light',
        notesPerPage: 10,
        tasksPerPage: 15,
        moodEntryReminder: true,
        aiSuggestions: false
    },

    // Route definitions
    routes: {
        intro: '/',
        auth: '/auth',
        dashboard: '/dashboard',
        notes: '/notes',
        tasks: '/tasks',
        mood: '/mood',
        chatbot: '/chatbot'
    },

    // Component paths
    components: {
        auth: '/components/auth.html',
        dashboard: '/components/dashboard.html',
        intro: '/components/intro.html',
        mood: '/components/mood.html',
        notes: '/components/notes.html',
        sidebar: '/components/sidebar.html',
        tasks: '/components/tasks.html',
        chatbot: '/components/chatbot.html'
    },

    // Default categories
    categories: {
        notes: ['Personal', 'Work', 'Health', 'Ideas', 'Goals'],
        tasks: ['Personal', 'Work', 'Health', 'Shopping', 'Errands'],
        moods: ['Happy', 'Calm', 'Anxious', 'Sad', 'Energetic', 'Tired']
    },

    // Task priorities
    taskPriorities: ['low', 'medium', 'high'],

    // Mood scale configuration
    moodScale: {
        min: 1,
        max: 5,
        default: 3,
        labels: {
            1: '😢 Very Low',
            2: '😕 Low',
            3: '😐 Neutral',
            4: '😊 Good',
            5: '😄 Excellent'
        }
    },

    // Chart colors
    chartColors: {
        mood: 'rgb(59, 130, 246)',    // Blue
        energy: 'rgb(16, 185, 129)',   // Green
        stress: 'rgb(239, 68, 68)',    // Red
        sleep: 'rgb(139, 92, 246)'     // Purple
    }
};
