import { apiService } from '../utils/apiService.js';
import { config } from '../config.js';

export class Tasks {
    constructor() {
        this.tasks = [];
        this.editingTask = null;
        this.currentFilter = { search: '', category: '', priority: '' };
    }

    async initialize() {
        if (!apiService.isAuthenticated()) {
            this.showLoginPrompt();
            return;
        }
        this.bindEvents();
        await this.loadTasks();
    }

    showLoginPrompt() {
        const container = document.getElementById('tasks-grid');
        if (container) {
            container.innerHTML = '<div class="col-span-full text-center py-12"><p class="text-gray-500">Please log in to manage tasks</p></div>';
        }
    }

    bindEvents() {
        document.getElementById('create-task-btn')?.addEventListener('click', () => {
            this.showTaskEditor();
        });

        document.getElementById('tasks-search')?.addEventListener('input', (e) => {
            this.searchTasks(e.target.value);
        });

        document.getElementById('tasks-category-filter')?.addEventListener('change', (e) => {
            this.filterByCategory(e.target.value);
        });

        document.getElementById('tasks-priority-filter')?.addEventListener('change', (e) => {
            this.filterByPriority(e.target.value);
        });

        document.getElementById('task-form')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveTask();
        });

        document.getElementById('cancel-task-edit')?.addEventListener('click', () => {
            this.hideTaskEditor();
        });

        document.getElementById('close-task-editor')?.addEventListener('click', () => {
            this.hideTaskEditor();
        });
    }

    async loadTasks() {
        try {
            const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.tasks}`, {
                headers: {
                    'Authorization': `Bearer ${apiService.getToken()}`
                }
            });

            if (response.ok) {
                this.tasks = await response.json();
                this.renderTasks();
                this.updateFilters();
            }
        } catch (error) {
            console.error('Error loading tasks:', error);
        }
    }

    renderTasks() {
        const container = document.getElementById('tasks-grid');
        if (!container) return;

        const filtered = this.getFilteredTasks();

        if (filtered.length === 0) {
            container.innerHTML = '<div class="col-span-full text-center py-12"><p class="text-gray-500">No tasks yet</p></div>';
            return;
        }

        const priorityColors = {
            'high': 'border-red-300 bg-red-50',
            'medium': 'border-yellow-300 bg-yellow-50',
            'low': 'border-green-300 bg-green-50'
        };

        const priorityBadges = {
            'high': 'bg-red-100 text-red-700',
            'medium': 'bg-yellow-100 text-yellow-700',
            'low': 'bg-green-100 text-green-700'
        };

        container.innerHTML = filtered.map(task => {
            const pColor = priorityColors[task.priority?.toLowerCase()] || 'border-gray-300';
            const pBadge = priorityBadges[task.priority?.toLowerCase()] || 'bg-gray-100';
            const dueDate = task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'No due date';
            const titleClass = task.completed ? 'line-through text-gray-500' : 'text-gray-900';
            const descHtml = task.description ? '<p class="text-gray-600 text-sm mt-1">' + this.escapeHtml(task.description) + '</p>' : '';
            const displayPriority = task.priority?.charAt(0).toUpperCase() + task.priority?.slice(1).toLowerCase();
            
            return '<div class="task-card p-6 bg-white rounded-2xl border-2 ' + pColor + ' hover:shadow-lg transition-all">' +
                '<div class="flex items-start gap-3 mb-3">' +
                '<input type="checkbox" ' + (task.completed ? 'checked' : '') + ' ' +
                'onchange="window.app.tasks.toggleTask(' + task.id + ', this.checked)" ' +
                'class="w-5 h-5 mt-1 text-blue-600 rounded">' +
                '<div class="flex-1">' +
                '<h3 class="text-lg font-bold ' + titleClass + '">' + this.escapeHtml(task.title) + '</h3>' +
                descHtml +
                '</div>' +
                '<div class="flex gap-2">' +
                '<button onclick="window.app.tasks.editTask(' + task.id + ')" class="text-blue-600 hover:text-blue-700"><i class="fas fa-edit"></i></button>' +
                '<button onclick="window.app.tasks.deleteTask(' + task.id + ')" class="text-red-600 hover:text-red-700"><i class="fas fa-trash"></i></button>' +
                '</div>' +
                '</div>' +
                '<div class="flex items-center gap-2 text-xs">' +
                '<span class="px-2 py-1 ' + pBadge + ' rounded-full font-semibold">' + displayPriority + '</span>' +
                '<span class="px-2 py-1 bg-blue-100 text-blue-700 rounded-full">' + (task.category || 'General') + '</span>' +
                '<span class="text-gray-500"><i class="far fa-calendar mr-1"></i>' + dueDate + '</span>' +
                '</div>' +
                '</div>';
        }).join('');
    }

    getFilteredTasks() {
        let filtered = this.tasks;

        if (this.currentFilter.search) {
            const search = this.currentFilter.search.toLowerCase();
            filtered = filtered.filter(task => 
                task.title.toLowerCase().includes(search) || 
                (task.description && task.description.toLowerCase().includes(search))
            );
        }

        if (this.currentFilter.category) {
            filtered = filtered.filter(task => task.category === this.currentFilter.category);
        }

        if (this.currentFilter.priority) {
            filtered = filtered.filter(task => task.priority === this.currentFilter.priority);
        }

        return filtered.sort((a, b) => {
            if (a.completed !== b.completed) return a.completed ? 1 : -1;
            return new Date(b.createdAt) - new Date(a.createdAt);
        });
    }

    searchTasks(query) {
        this.currentFilter.search = query;
        this.renderTasks();
    }

    filterByCategory(category) {
        this.currentFilter.category = category;
        this.renderTasks();
    }

    filterByPriority(priority) {
        this.currentFilter.priority = priority;
        this.renderTasks();
    }

    updateFilters() {
        const categorySelect = document.getElementById('tasks-category-filter');
        if (categorySelect) {
            const categories = ['All', ...new Set(this.tasks.map(task => task.category).filter(Boolean))];
            categorySelect.innerHTML = categories.map(cat => 
                '<option value="' + (cat === 'All' ? '' : cat) + '">' + cat + '</option>'
            ).join('');
        }
    }

    showTaskEditor(task = null) {
        this.editingTask = task;
        const editor = document.getElementById('task-editor');
        if (!editor) return;

        editor.classList.remove('hidden');

        const form = document.getElementById('task-form');
        if (form) {
            if (task) {
                form.querySelector('#task-title').value = task.title;
                form.querySelector('#task-description').value = task.description || '';
                form.querySelector('#task-category').value = task.category || '';
                form.querySelector('#task-priority').value = task.priority?.toLowerCase() || 'medium';
                form.querySelector('#task-due-date').value = task.dueDate || '';
            } else {
                form.reset();
                form.querySelector('#task-priority').value = 'medium';
            }
        }
    }

    hideTaskEditor() {
        const editor = document.getElementById('task-editor');
        if (editor) {
            editor.classList.add('hidden');
        }
        this.editingTask = null;
    }

    async saveTask() {
        const form = document.getElementById('task-form');
        if (!form) return;

        const title = form.querySelector('#task-title').value.trim();
        const description = form.querySelector('#task-description').value.trim();
        const category = form.querySelector('#task-category').value.trim();
        const priority = form.querySelector('#task-priority').value;
        const dueDate = form.querySelector('#task-due-date').value;

        if (!title) {
            alert('Please enter a task title');
            return;
        }

        // Convert date to LocalDateTime format (add time component)
        let dueDateFormatted = null;
        if (dueDate) {
            // Add time component (end of day) to make it a valid LocalDateTime
            dueDateFormatted = `${dueDate}T23:59:59`;
        }

        const taskData = {
            title,
            description: description || null,
            category: category || 'General',
            priority: (priority || 'medium').toLowerCase(),
            dueDate: dueDateFormatted,
            completed: false
        };

        try {
            const url = this.editingTask 
                ? `${apiService.baseUrl}${config.api.endpoints.tasks}/${this.editingTask.id}`
                : `${apiService.baseUrl}${config.api.endpoints.tasks}`;

            const method = this.editingTask ? 'PUT' : 'POST';

            if (this.editingTask) {
                taskData.id = this.editingTask.id;
                taskData.completed = this.editingTask.completed;
            }

            const response = await fetch(url, {
                method,
                headers: {
                    'Authorization': `Bearer ${apiService.getToken()}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(taskData)
            });

            if (response.ok) {
                await this.loadTasks();
                this.hideTaskEditor();
                this.showSuccess(this.editingTask ? 'Task updated!' : 'Task created!');

                if (window.app?.dashboard) {
                    await window.app.dashboard.refresh();
                }
            } else {
                const errorText = await response.text();
                console.error('Failed to save task:', errorText);
                alert(`Failed to save task: ${errorText}`);
            }
        } catch (error) {
            console.error('Error saving task:', error);
            alert('Failed to save task. Please try again.');
        }
    }

    async toggleTask(taskId, completed) {
        try {
            const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.tasks}/${taskId}`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${apiService.getToken()}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ completed })
            });

            if (response.ok) {
                await this.loadTasks();
                if (window.app?.dashboard) {
                    await window.app.dashboard.refresh();
                }
            }
        } catch (error) {
            console.error('Error toggling task:', error);
            alert('Failed to update task');
        }
    }

    editTask(taskId) {
        const task = this.tasks.find(t => t.id === taskId);
        if (task) {
            this.showTaskEditor(task);
        }
    }

    async deleteTask(taskId) {
        if (!confirm('Delete this task?')) return;

        console.log('Deleting task:', taskId);

        try {
            const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.tasks}/${taskId}`, {
                method: 'DELETE',
                headers: apiService.getHeaders()
            });

            if (response.ok) {
                console.log('Task deleted successfully');
                await this.loadTasks();
                this.showSuccess('Task deleted');

                if (window.app?.dashboard) {
                    await window.app.dashboard.refresh();
                }
            } else {
                const errorData = await response.json().catch(() => ({}));
                console.error('Failed to delete task:', errorData);
                alert(`Failed to delete task: ${errorData.message || 'Unknown error'}`);
            }
        } catch (error) {
            console.error('Error deleting task:', error);
            alert('Failed to delete task: ' + error.message);
        }
    }

    showSuccess(message) {
        const msg = document.createElement('div');
        msg.className = 'fixed top-4 right-4 px-6 py-3 bg-green-500 text-white rounded-lg z-50';
        msg.textContent = message;
        document.body.appendChild(msg);
        setTimeout(() => msg.remove(), 3000);
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}
