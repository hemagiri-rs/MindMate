import { apiService } from '../utils/apiService.js';
import { config } from '../config.js';

export class Dashboard {
    constructor() {
        this.data = {
            notes: [],
            tasks: [],
            moods: []
        };
    }

    async initialize() {
        // Check if user is authenticated
        if (!apiService.isAuthenticated()) {
            console.log('User not authenticated, showing login prompt');
            this.showLoginPrompt();
            return;
        }

        this.bindEvents();
        await this.loadData();
        this.updateStats();
        this.updateRecentActivity();
        this.checkDataSeeding();
    }

    showLoginPrompt() {
        const appContent = document.getElementById('app-content');
        if (appContent) {
            appContent.innerHTML = `
                <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 via-blue-50/30 to-purple-50/40 p-4">
                    <div class="relative max-w-md w-full">
                        <div class="absolute -top-4 -left-4 w-32 h-32 bg-gradient-to-br from-emerald-500/20 to-blue-500/20 rounded-full blur-3xl"></div>
                        <div class="absolute -bottom-4 -right-4 w-32 h-32 bg-gradient-to-br from-purple-500/20 to-pink-500/20 rounded-full blur-3xl"></div>
                        
                        <div class="relative backdrop-blur-xl bg-white/80 rounded-3xl p-8 border border-white/20 shadow-2xl text-center space-y-6">
                            <div class="w-20 h-20 mx-auto flex items-center justify-center rounded-3xl bg-gradient-to-br from-emerald-500 to-blue-600 shadow-lg shadow-emerald-500/50">
                                <i class="fas fa-lock text-4xl text-white"></i>
                            </div>
                            
                            <div class="space-y-2">
                                <h2 class="text-3xl font-black bg-gradient-to-r from-emerald-600 via-blue-600 to-purple-600 bg-clip-text text-transparent">
                                    Authentication Required
                                </h2>
                                <p class="text-gray-600 font-medium">
                                    Please log in to access your dashboard
                                </p>
                            </div>

                            <button onclick="window.app.router.navigate('/auth')" class="w-full group relative px-8 py-4 bg-gradient-to-r from-emerald-600 via-emerald-500 to-teal-500 hover:from-emerald-700 hover:via-emerald-600 hover:to-teal-600 text-white font-bold rounded-2xl transition-all duration-300 shadow-xl shadow-emerald-500/30 hover:shadow-2xl hover:shadow-emerald-500/40 hover:-translate-y-1 active:scale-95">
                                <div class="absolute inset-0 bg-gradient-to-r from-white/20 to-transparent rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity"></div>
                                <div class="relative flex items-center justify-center gap-2">
                                    <i class="fas fa-sign-in-alt"></i>
                                    <span>Go to Login</span>
                                </div>
                            </button>

                            <p class="text-sm text-gray-500">
                                Don't have an account? 
                                <button onclick="window.app.router.navigate('/auth')" class="font-bold text-emerald-600 hover:text-emerald-700 hover:underline transition-colors">
                                    Sign up now
                                </button>
                            </p>
                        </div>
                    </div>
                </div>
            `;
        }
    }

    async loadData() {
        try {
            // Load data from backend
            const [notes, tasks, analytics] = await Promise.all([
                this.loadNotes(),
                this.loadTasks(),
                this.loadAnalytics()
            ]);

            this.data.notes = notes || [];
            this.data.tasks = tasks || [];
            this.data.analytics = analytics || null;
            this.data.moods = analytics?.moodHistory || [];
        } catch (error) {
            console.error('Error loading dashboard data:', error);
            // Use empty arrays if API calls fail
            this.data.notes = [];
            this.data.tasks = [];
            this.data.moods = [];
            this.data.analytics = null;
        }
    }

    async loadNotes() {
        try {
            const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.notes}`, {
                headers: {
                    'Authorization': `Bearer ${apiService.getToken()}`,
                    'Content-Type': 'application/json'
                }
            });
            if (response.ok) {
                return await response.json();
            }
        } catch (error) {
            console.error('Error loading notes:', error);
        }
        return [];
    }

    async loadTasks() {
        try {
            const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.tasks}`, {
                headers: {
                    'Authorization': `Bearer ${apiService.getToken()}`,
                    'Content-Type': 'application/json'
                }
            });
            if (response.ok) {
                return await response.json();
            }
        } catch (error) {
            console.error('Error loading tasks:', error);
        }
        return [];
    }

    async loadMoods() {
        try {
            const response = await fetch(`${apiService.baseUrl}/mood/history`, {
                headers: {
                    'Authorization': `Bearer ${apiService.getToken()}`,
                    'Content-Type': 'application/json'
                }
            });
            if (response.ok) {
                return await response.json();
            }
        } catch (error) {
            console.error('Error loading moods:', error);
        }
        return [];
    }

    async loadAnalytics() {
        try {
            const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.analytics}/summary`, {
                headers: {
                    'Authorization': `Bearer ${apiService.getToken()}`,
                    'Content-Type': 'application/json'
                }
            });
            if (response.ok) {
                return await response.json();
            }
        } catch (error) {
            console.error('Error loading analytics:', error);
        }
        return null;
    }

    bindEvents() {
        // Quick action buttons
        document.querySelectorAll('[data-action]').forEach(btn => {
            btn.addEventListener('click', () => this.handleQuickAction(btn.dataset.action));
        });

        // Seed data button
        document.getElementById('seed-data-btn')?.addEventListener('click', () => {
            this.seedSampleData();
        });

        // Refresh dashboard button
        document.getElementById('refresh-dashboard')?.addEventListener('click', () => {
            this.refresh();
        });
    }

    async refresh() {
        await this.loadData();
        this.updateStats();
        this.updateRecentActivity();
        this.checkDataSeeding();
    }

    handleQuickAction(action) {
        switch (action) {
            case 'new-note':
                window.app.router.navigate('/notes');
                break;
            case 'new-task':
                window.app.router.navigate('/tasks');
                break;
            case 'log-mood':
                window.app.router.navigate('/mood');
                break;
            case 'ai-assistant':
                window.app.router.navigate('/chatbot');
                break;
        }
    }

    updateStats() {
        const notes = this.data.notes;
        const tasks = this.data.tasks;
        const analytics = this.data.analytics;

        const completedTasks = tasks.filter(task => task.completed).length;
        const totalTasks = tasks.length;
        
        // Use backend analytics if available
        const totalMoods = analytics?.totalMoods || 0;
        const currentMood = analytics?.currentMood?.type || 'N/A';
        const streak = analytics?.currentStreak || 0;
        const streakType = analytics?.currentStreak > 0 ? 'positive' : analytics?.currentStreak < 0 ? 'negative' : 'none';

        // Safely update elements if they exist
        const totalNotesEl = document.getElementById('total-notes');
        const completedTasksEl = document.getElementById('completed-tasks');
        const averageMoodEl = document.getElementById('average-mood');
        const weeklyProgressEl = document.getElementById('weekly-progress');

        if (totalNotesEl) totalNotesEl.textContent = notes.length;
        if (completedTasksEl) completedTasksEl.textContent = `${completedTasks}/${totalTasks}`;
        if (averageMoodEl) averageMoodEl.textContent = currentMood;
        if (weeklyProgressEl) weeklyProgressEl.textContent = streak > 0 ? `${streak} day streak!` : `${totalMoods} logged`;
    }

    updateRecentActivity() {
        const notes = this.data.notes;
        const tasks = this.data.tasks;
        const moods = this.data.moods;

        const activities = [
            ...notes.slice(0, 2).map(note => ({
                type: 'note',
                title: note.title,
                timestamp: new Date(note.updatedAt || note.createdAt).toLocaleString(),
                icon: 'sticky-note',
                color: 'blue'
            })),
            ...tasks.slice(0, 2).map(task => ({
                type: 'task',
                title: task.title,
                timestamp: new Date(task.updatedAt || task.createdAt).toLocaleString(),
                status: task.completed ? 'completed' : 'pending',
                icon: 'check-circle',
                color: task.completed ? 'emerald' : 'purple'
            })),
            ...moods.slice(0, 1).map(mood => ({
                type: 'mood',
                title: `Mood: ${mood.mood}/5`,
                timestamp: new Date(mood.createdAt).toLocaleString(),
                icon: 'heart',
                color: 'pink'
            }))
        ]
        .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
        .slice(0, 5);

        const container = document.getElementById('recent-activity');
        if (!container) return;

        if (activities.length === 0) {
            container.innerHTML = `
                <div class="flex flex-col items-center justify-center py-12 text-center">
                    <div class="w-20 h-20 mb-6 flex items-center justify-center rounded-3xl bg-gradient-to-br from-gray-100 to-gray-200">
                        <i class="fas fa-inbox text-4xl text-gray-400"></i>
                    </div>
                    <h4 class="text-lg font-bold text-gray-900 mb-2">No recent activity</h4>
                    <p class="text-sm text-gray-500 max-w-xs">Start by creating a note or task to see your activity here</p>
                </div>
            `;
        } else {
            container.innerHTML = `
                <div class="space-y-4">
                    ${activities.map(activity => `
                        <div class="group relative p-4 rounded-2xl bg-gradient-to-br from-${activity.color}-50/50 to-white border border-${activity.color}-100/50 hover:shadow-lg hover:shadow-${activity.color}-500/10 transition-all duration-300">
                            <div class="flex items-start gap-4">
                                <div class="w-12 h-12 flex-shrink-0 flex items-center justify-center rounded-xl bg-gradient-to-br from-${activity.color}-500 to-${activity.color}-600 shadow-lg shadow-${activity.color}-500/30">
                                    <i class="fas fa-${activity.icon} text-white text-lg"></i>
                                </div>
                                <div class="flex-1 min-w-0">
                                    <div class="flex items-start justify-between gap-2 mb-1">
                                        <h4 class="text-sm font-bold text-gray-900 truncate">${activity.title}</h4>
                                        ${activity.status === 'completed' ? '<span class="px-2 py-1 bg-emerald-100 text-emerald-700 rounded-full text-xs font-semibold">Done</span>' : ''}
                                    </div>
                                    <p class="text-xs text-gray-500 flex items-center gap-1">
                                        <i class="far fa-clock"></i>
                                        ${activity.timestamp}
                                    </p>
                                </div>
                            </div>
                        </div>
                    `).join('')}
                </div>
            `;
        }
    }

    checkDataSeeding() {
        const notes = this.data.notes;
        const tasks = this.data.tasks;
        const moods = this.data.moods;

        const hasData = notes.length > 0 || tasks.length > 0 || moods.length > 0;
        const seedingElement = document.getElementById('data-seeding');
        if (seedingElement) {
            seedingElement.style.display = hasData ? 'none' : 'block';
        }
    }

    async seedSampleData() {
        try {
            // Sample notes data
            const sampleNotes = [
                {
                    title: 'Welcome to MindMate',
                    content: 'This is your first note! Use notes to capture thoughts, ideas, and important information.',
                    category: 'Personal',
                    tags: ['welcome', 'getting-started']
                },
                {
                    title: 'Wellness Goals',
                    content: 'Set daily wellness goals:\n- Drink 8 glasses of water\n- Take a 10-minute walk\n- Practice gratitude',
                    category: 'Health',
                    tags: ['wellness', 'goals']
                }
            ];

            // Sample tasks data
            const sampleTasks = [
                {
                    title: 'Complete daily meditation',
                    description: '10 minutes of mindfulness meditation',
                    category: 'Health',
                    priority: 'HIGH',
                    dueDate: new Date().toISOString().split('T')[0],
                    completed: false
                },
                {
                    title: 'Review weekly goals',
                    description: 'Check progress on personal and professional goals',
                    category: 'Personal',
                    priority: 'MEDIUM',
                    dueDate: new Date(Date.now() + 86400000).toISOString().split('T')[0],
                    completed: false
                }
            ];

            // Sample mood data
            const sampleMoods = [
                {
                    date: new Date().toISOString().split('T')[0],
                    mood: 4,
                    energy: 3,
                    stress: 2,
                    sleep: 4,
                    notes: 'Feeling good today! Had a productive morning.'
                }
            ];

            // Create notes
            for (const note of sampleNotes) {
                await fetch(`${apiService.baseUrl}${config.api.endpoints.notes}`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${apiService.getToken()}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(note)
                });
            }

            // Create tasks
            for (const task of sampleTasks) {
                await fetch(`${apiService.baseUrl}${config.api.endpoints.tasks}`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${apiService.getToken()}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(task)
                });
            }

            // Create moods
            for (const mood of sampleMoods) {
                await fetch(`${apiService.baseUrl}/mood/log`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${apiService.getToken()}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        moodType: 'HAPPY',
                        energyLevel: mood.energy,
                        stressLevel: mood.stress,
                        sleepQuality: mood.sleep,
                        notes: mood.notes
                    })
                });
            }

            // Refresh dashboard
            await this.refresh();
            
        } catch (error) {
            console.error('Error seeding sample data:', error);
            alert('Failed to create sample data. Please try again.');
        }
    }
}
