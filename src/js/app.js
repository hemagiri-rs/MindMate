// Import dependencies
import { Router } from './utils/router.js';
import { Storage } from './utils/storage.js';
import { sidebarManager } from './utils/sidebarManager.js';
import { responsiveManager } from './utils/responsiveManager.js';
import { Auth } from './components/Auth.js';
import { Dashboard } from './components/Dashboard.js';
import { Notes } from './components/Notes.js';
import { Tasks } from './components/Tasks.js';
import { Mood } from './components/Mood.js';
import { Chatbot } from './components/Chatbot.js';
import { config } from './config.js';

class MindMateApp {
    constructor() {
        this.router = new Router();
        this.setupRouter();
        this.initializeApp();
        
        // Initialize components
        this.auth = new Auth();
        this.dashboard = new Dashboard();
        this.notes = new Notes();
        this.tasks = new Tasks();
        this.mood = new Mood();
        this.chatbot = new Chatbot();
        
        // Store manager references but don't initialize yet
        this.sidebarManager = sidebarManager;
        this.responsiveManager = responsiveManager;
        
        // Initialize responsive manager (doesn't depend on sidebar)
        this.initializeManagers();
    }

    initializeManagers() {
        // Initialize responsive manager
        responsiveManager.initialize();
        
        // Note: sidebarManager will be initialized by router after sidebar HTML is loaded
        console.log('Responsive manager initialized');
        
        // Setup responsive breakpoint listeners
        responsiveManager.onBreakpointChange((newBp, oldBp) => {
            console.log(`Breakpoint changed from ${oldBp} to ${newBp}`);
            this.handleBreakpointChange(newBp);
        });
    }

    handleBreakpointChange(breakpoint) {
        // Handle any responsive behavior changes
        if (responsiveManager.isMobile()) {
            // Mobile-specific adjustments
            console.log('Mobile view active');
        } else if (responsiveManager.isTablet()) {
            // Tablet-specific adjustments
            console.log('Tablet view active');
        } else {
            // Desktop-specific adjustments
            console.log('Desktop view active');
        }
    }

    setupRouter() {
        // Define routes
        this.router.addRoute('/', 'intro', { default: true });
        this.router.addRoute('/auth', 'auth');
        this.router.addRoute('/dashboard', 'dashboard', {
            onEnter: () => this.checkAuth()
        });
        this.router.addRoute('/notes', 'notes', {
            onEnter: () => this.checkAuth()
        });
        this.router.addRoute('/tasks', 'tasks', {
            onEnter: () => this.checkAuth()
        });
        this.router.addRoute('/mood', 'mood', {
            onEnter: () => this.checkAuth()
        });
        this.router.addRoute('/chatbot', 'chatbot', {
            onEnter: () => this.checkAuth()
        });
    }

    async initializeApp() {
        this.showLoading();
        
        try {
            // Start the router first
            this.router.start();
            
            // Check authentication status
            const user = Storage.getUserData();
            
            if (user) {
                // Load dashboard for authenticated users
                await this.router.navigate('/dashboard');
            } else {
                // Show intro for new users
                await this.router.navigate('/');
            }
        } catch (error) {
            console.error('Error initializing app:', error);
        } finally {
            this.hideLoading();
        }

        // Setup global event listeners
        this.setupEventListeners();
    }

    showLoading() {
        document.getElementById('loading-screen').classList.remove('hidden');
    }

    hideLoading() {
        document.getElementById('loading-screen').classList.add('hidden');
    }

    async checkAuth() {
        const user = Storage.getUserData();
        if (!user) {
            await this.router.navigate('/auth');
            return false;
        }
        return true;
    }

    async handleLogout() {
        Storage.clearUserData();
        await this.router.navigate('/auth');
    }

    setupEventListeners() {
        // Handle intro page "Get Started" button
        document.addEventListener('click', async (e) => {
            if (e.target.classList.contains('get-started')) {
                await this.router.navigate('/auth');
            }
        });
        
        // Handle logout button
        document.addEventListener('click', async (e) => {
            if (e.target.id === 'logout-btn' || e.target.closest('#logout-btn')) {
                await this.handleLogout();
            }
        });

        // Handle sidebar navigation
        document.addEventListener('click', async (e) => {
            const navItem = e.target.closest('.nav-item');
            if (navItem && navItem.hasAttribute('data-route')) {
                e.preventDefault();
                const route = navItem.getAttribute('data-route');
                
                // Remove active class from all nav items
                document.querySelectorAll('.nav-item').forEach(item => {
                    item.classList.remove('active');
                });
                
                // Add active class to clicked item
                navItem.classList.add('active');
                
                // Navigate to route
                await this.router.navigate('/' + route);
            }
        });
    }
}

// Initialize the app when the DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.app = new MindMateApp();
});
