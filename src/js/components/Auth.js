import { Helpers } from '../utils/helpers.js';
import { Storage } from '../utils/storage.js';
import { apiService } from '../utils/apiService.js';

export class Auth {
    constructor() {
        // Don't bind events immediately, wait for component to load
    }

    initialize() {
        this.bindEvents();
        this.showLoginForm();
        this.checkBackendConnection();
    }

    async checkBackendConnection() {
        try {
            await apiService.healthCheck();
            console.log('Backend connection successful');
        } catch (error) {
            console.error('Backend connection failed:', error);
            this.showConnectionError();
        }
    }

    showConnectionError() {
        const errorHtml = `
            <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                <strong>Connection Error:</strong> Unable to connect to the backend server. 
                Please make sure the server is running on http://localhost:7070
            </div>
        `;
        document.getElementById('auth-container')?.insertAdjacentHTML('afterbegin', errorHtml);
    }

    bindEvents() {
        // Switch between login and register forms
        document.getElementById('show-register')?.addEventListener('click', (e) => {
            e.preventDefault();
            this.showRegisterForm();
        });

        document.getElementById('show-login')?.addEventListener('click', (e) => {
            e.preventDefault();
            this.showLoginForm();
        });

        // Form submissions
        document.getElementById('login-form-element')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleLogin();
        });

        document.getElementById('register-form-element')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleRegister();
        });
    }

    showLoginForm() {
        document.getElementById('login-form')?.classList.remove('hidden');
        document.getElementById('register-form')?.classList.add('hidden');
        document.getElementById('login-error')?.classList.add('hidden');
    }

    showRegisterForm() {
        document.getElementById('login-form')?.classList.add('hidden');
        document.getElementById('register-form')?.classList.remove('hidden');
        document.getElementById('register-error')?.classList.add('hidden');
    }

    async handleLogin() {
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        const errorElement = document.getElementById('login-error');
        const submitButton = document.querySelector('#login-form-element button[type="submit"]');

        // Clear previous errors
        errorElement?.classList.add('hidden');

        if (!Helpers.validateEmail(email)) {
            errorElement.textContent = 'Please enter a valid email address';
            errorElement.classList.remove('hidden');
            return;
        }

        if (!password.trim()) {
            errorElement.textContent = 'Please enter your password';
            errorElement.classList.remove('hidden');
            return;
        }

        try {
            // Show loading state
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = 'Signing in...';
            }

            // Call backend API
            const response = await apiService.login(email, password);
            
            console.log('Login successful:', response);
            
            // Navigate to dashboard
            window.app.router.navigate('/dashboard');
        } catch (error) {
            console.error('Login error:', error);
            let errorMessage = 'Login failed. Please try again.';
            
            if (error.message.includes('400')) {
                errorMessage = 'Invalid email or password';
            } else if (error.message.includes('connection')) {
                errorMessage = 'Unable to connect to server. Please try again later.';
            }
            
            errorElement.textContent = errorMessage;
            errorElement.classList.remove('hidden');
        } finally {
            // Reset button state
            if (submitButton) {
                submitButton.disabled = false;
                submitButton.textContent = 'Sign In';
            }
        }
    }

    async handleRegister() {
        const name = document.getElementById('register-name').value;
        const email = document.getElementById('register-email').value;
        const password = document.getElementById('register-password').value;
        const errorElement = document.getElementById('register-error');
        const submitButton = document.querySelector('#register-form-element button[type="submit"]');

        // Clear previous errors
        errorElement?.classList.add('hidden');

        if (!name.trim()) {
            errorElement.textContent = 'Please enter your name';
            errorElement.classList.remove('hidden');
            return;
        }

        if (!Helpers.validateEmail(email)) {
            errorElement.textContent = 'Please enter a valid email address';
            errorElement.classList.remove('hidden');
            return;
        }

        if (password.length < 6) {
            errorElement.textContent = 'Password must be at least 6 characters long';
            errorElement.classList.remove('hidden');
            return;
        }

        try {
            // Show loading state
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = 'Creating account...';
            }

            // Call backend API
            const response = await apiService.register(name, email, password);
            
            console.log('Registration successful:', response);
            
            // Navigate to dashboard
            window.app.router.navigate('/dashboard');
        } catch (error) {
            console.error('Registration error:', error);
            let errorMessage = 'Registration failed. Please try again.';
            
            if (error.message.includes('already registered')) {
                errorMessage = 'Email is already registered. Please use a different email or try logging in.';
            } else if (error.message.includes('400')) {
                errorMessage = 'Please check your information and try again.';
            } else if (error.message.includes('connection')) {
                errorMessage = 'Unable to connect to server. Please try again later.';
            }
            
            errorElement.textContent = errorMessage;
            errorElement.classList.remove('hidden');
        } finally {
            // Reset button state
            if (submitButton) {
                submitButton.disabled = false;
                submitButton.textContent = 'Create Account';
            }
        }
    }
}
