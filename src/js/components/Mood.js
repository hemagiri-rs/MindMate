import { config } from '../config.js';
import { apiService } from '../utils/apiService.js';

export class Mood {
    constructor() {
        this.moods = [];
        this.selectedMood = null;
        this.initialized = false;
    }

    initialize() {
        if (this.initialized) return;

        if (!apiService.isAuthenticated()) {
            this.showLoginPrompt();
            return;
        }

        this.bindEvents();
        this.loadMoods();
        this.initialized = true;
    }

    showLoginPrompt() {
        const container = document.getElementById('mood-history');
        if (container) {
            container.innerHTML = '<div class="text-center py-12"><p class="text-gray-500">Please log in to track your mood</p></div>';
        }
    }

    bindEvents() {
        // Mood option buttons
        const moodOptions = document.querySelectorAll('.mood-option');
        moodOptions.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const value = parseInt(btn.dataset.mood);
                this.selectMood(value, btn);
            });
        });

        // Form submission
        const moodForm = document.getElementById('mood-form');
        if (moodForm) {
            moodForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.saveMood();
            });
        }

        // Log mood button (header)
        const logMoodBtn = document.getElementById('log-mood-btn');
        if (logMoodBtn) {
            logMoodBtn.addEventListener('click', () => {
                // Scroll to form
                const moodForm = document.getElementById('mood-form');
                if (moodForm) {
                    moodForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
            });
        }
    }

    selectMood(value, buttonElement) {
        // Remove active class from all mood options
        document.querySelectorAll('.mood-option').forEach(btn => {
            btn.classList.remove('ring-4', 'ring-white', 'ring-offset-2');
        });

        // Add active class to selected button
        if (buttonElement) {
            buttonElement.classList.add('ring-4', 'ring-white', 'ring-offset-2');
        }

        // Store selected mood value
        this.selectedMood = value;
        document.getElementById('mood-value').value = value;
    }

    async saveMood() {
        const moodValue = document.getElementById('mood-value').value;
        const moodNotes = document.getElementById('mood-notes').value.trim();

        if (!moodValue) {
            alert('Please select a mood before logging');
            return;
        }

        // Map mood value to backend mood type
        const moodTypeMap = {
            1: 'SAD',
            2: 'ANXIOUS',
            3: 'CALM',
            4: 'HAPPY',
            5: 'EXCITED'
        };

        const moodData = {
            moodType: moodTypeMap[moodValue],
            notes: moodNotes || ''
        };

        try {
            console.log('Saving mood:', moodData);
            const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.mood}/add`, {
                method: 'POST',
                headers: apiService.getHeaders(),
                body: JSON.stringify(moodData)
            });

            console.log('Response status:', response.status);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('Error response:', errorText);
                throw new Error(`Failed to save mood: ${response.status} - ${errorText}`);
            }

            const result = await response.json();
            console.log('Mood saved successfully:', result);
            
            await this.loadMoods();
            this.resetForm();
            this.showSuccess('Mood logged successfully! 💚');
            
            // Refresh dashboard if available
            if (window.app?.dashboard) {
                await window.app.dashboard.refresh();
            }
        } catch (error) {
            console.error('Error saving mood:', error);
            alert(`Failed to save mood: ${error.message}`);
        }
    }

    resetForm() {
        // Clear form
        document.getElementById('mood-form').reset();
        document.getElementById('mood-value').value = '';
        this.selectedMood = null;

        // Remove active state from all buttons
        document.querySelectorAll('.mood-option').forEach(btn => {
            btn.classList.remove('ring-4', 'ring-white', 'ring-offset-2');
        });
    }

    async loadMoods() {
        try {
            const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.mood}/history?limit=10`, {
                headers: apiService.getHeaders()
            });

            if (response.ok) {
                this.moods = await response.json();
                this.renderMoodHistory();
            } else {
                console.error('Failed to load moods:', response.statusText);
                this.moods = [];
                this.renderMoodHistory();
            }
        } catch (error) {
            console.error('Error loading moods:', error);
            this.moods = [];
            this.renderMoodHistory();
        }
    }

    renderMoodHistory() {
        const container = document.getElementById('mood-history');
        if (!container) return;

        if (this.moods.length === 0) {
            container.innerHTML = `
                <div class="text-center py-12">
                    <div class="text-6xl mb-4">📊</div>
                    <p class="text-gray-500 font-medium">No mood history yet</p>
                    <p class="text-sm text-gray-400 mt-2">Start tracking your emotions today!</p>
                </div>
            `;
            return;
        }

        const moodConfig = {
            'SAD': {
                icon: '😢',
                label: 'Very Low',
                color: 'bg-red-100 text-red-700 border-red-200'
            },
            'ANXIOUS': {
                icon: '😰',
                label: 'Low',
                color: 'bg-orange-100 text-orange-700 border-orange-200'
            },
            'CALM': {
                icon: '😌',
                label: 'Neutral',
                color: 'bg-yellow-100 text-yellow-700 border-yellow-200'
            },
            'HAPPY': {
                icon: '😊',
                label: 'Good',
                color: 'bg-blue-100 text-blue-700 border-blue-200'
            },
            'EXCITED': {
                icon: '🤩',
                label: 'Excellent',
                color: 'bg-green-100 text-green-700 border-green-200'
            }
        };

        container.innerHTML = this.moods.map(mood => {
            const moodInfo = moodConfig[mood.moodType] || {
                icon: '😐',
                label: mood.moodType,
                color: 'bg-gray-100 text-gray-700 border-gray-200'
            };

            const date = new Date(mood.timestamp);
            const dateStr = date.toLocaleDateString('en-US', { 
                month: 'short', 
                day: 'numeric', 
                year: 'numeric' 
            });
            const timeStr = date.toLocaleTimeString('en-US', { 
                hour: 'numeric', 
                minute: '2-digit' 
            });
            
            return `
                <div class="mood-history-item p-4 bg-white rounded-xl border border-gray-200 hover:shadow-md transition-all group">
                    <div class="flex items-start justify-between gap-3">
                        <div class="flex items-start gap-3 flex-1">
                            <div class="text-4xl flex-shrink-0">${moodInfo.icon}</div>
                            <div class="flex-1 min-w-0">
                                <div class="flex items-center gap-2 mb-1">
                                    <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-lg ${moodInfo.color} border">
                                        ${moodInfo.label}
                                    </span>
                                </div>
                                <p class="text-xs text-gray-500">
                                    <i class="far fa-calendar mr-1"></i>${dateStr}
                                    <span class="mx-1">•</span>
                                    <i class="far fa-clock mr-1"></i>${timeStr}
                                </p>
                                ${mood.notes ? `
                                    <p class="text-sm text-gray-600 mt-2 line-clamp-2">${this.escapeHtml(mood.notes)}</p>
                                ` : ''}
                            </div>
                        </div>
                        <button 
                            onclick="window.app.mood.deleteMood(${mood.id})" 
                            class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all opacity-0 group-hover:opacity-100"
                            title="Delete mood entry">
                            <i class="fas fa-trash text-sm"></i>
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    }

    async deleteMood(moodId) {
        if (!confirm('Are you sure you want to delete this mood entry?')) return;

        try {
            const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.mood}/${moodId}`, {
                method: 'DELETE',
                headers: apiService.getHeaders()
            });

            if (response.ok) {
                await this.loadMoods();
                this.showSuccess('Mood entry deleted successfully');
                
                // Refresh dashboard if available
                if (window.app?.dashboard) {
                    await window.app.dashboard.refresh();
                }
            } else {
                throw new Error('Failed to delete mood');
            }
        } catch (error) {
            console.error('Error deleting mood:', error);
            alert('Failed to delete mood entry. Please try again.');
        }
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    showSuccess(message) {
        const msg = document.createElement('div');
        msg.className = 'fixed top-4 right-4 px-6 py-3 bg-green-500 text-white rounded-xl shadow-lg z-50 animate-fade-in flex items-center gap-2';
        msg.innerHTML = `
            <i class="fas fa-check-circle"></i>
            <span>${message}</span>
        `;
        document.body.appendChild(msg);
        
        setTimeout(() => {
            msg.classList.add('animate-fade-out');
            setTimeout(() => msg.remove(), 300);
        }, 3000);
    }
}
