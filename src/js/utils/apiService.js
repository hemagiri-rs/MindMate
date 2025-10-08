import { config } from '../config.js';

class ApiService {
    constructor() {
        this.baseUrl = config.api.baseUrl;
        this.timeout = config.api.timeout;
    }

    // Get stored JWT token
    getToken() {
        return localStorage.getItem(config.storage.token);
    }

    // Set JWT token
    setToken(token) {
        localStorage.setItem(config.storage.token, token);
    }

    // Remove JWT token
    removeToken() {
        localStorage.removeItem(config.storage.token);
    }

    // Get default headers
    getHeaders(includeAuth = true) {
        const headers = {
            'Content-Type': 'application/json',
        };

        if (includeAuth) {
            const token = this.getToken();
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
        }

        return headers;
    }

    // Generic HTTP request method
    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const config = {
            ...options,
            headers: {
                ...this.getHeaders(options.includeAuth !== false),
                ...options.headers,
            },
        };

        try {
            console.log(`Making ${config.method || 'GET'} request to:`, url);
            
            const response = await fetch(url, config);
            
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP ${response.status}: ${errorText}`);
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            } else {
                return await response.text();
            }
        } catch (error) {
            console.error('API Request failed:', error);
            throw error;
        }
    }

    // GET request
    async get(endpoint, options = {}) {
        return this.request(endpoint, { ...options, method: 'GET' });
    }

    // POST request
    async post(endpoint, data, options = {}) {
        return this.request(endpoint, {
            ...options,
            method: 'POST',
            body: JSON.stringify(data),
        });
    }

    // PUT request
    async put(endpoint, data, options = {}) {
        return this.request(endpoint, {
            ...options,
            method: 'PUT',
            body: JSON.stringify(data),
        });
    }

    // DELETE request
    async delete(endpoint, options = {}) {
        return this.request(endpoint, { ...options, method: 'DELETE' });
    }

    // Authentication methods
    async login(email, password) {
        try {
            // Use email prefix as username (before @) or full email if no @
            const username = email.split('@')[0] || email;
            
            const response = await this.post(config.api.endpoints.auth.login, {
                username,
                password
            }, { includeAuth: false });

            if (response.token) {
                this.setToken(response.token);
                // Store user data
                localStorage.setItem(config.storage.userData, JSON.stringify({
                    id: response.userId,
                    email: response.email,
                    name: response.name
                }));
            }

            return response;
        } catch (error) {
            console.error('Login failed:', error);
            throw error;
        }
    }

    async register(name, email, password) {
        try {
            // Use email prefix as username (before @) or full email if no @
            const username = email.split('@')[0] || email;
            
            const response = await this.post(config.api.endpoints.auth.register, {
                username,
                name,
                email,
                password
            }, { includeAuth: false });

            if (response.token) {
                this.setToken(response.token);
                // Store user data
                localStorage.setItem(config.storage.userData, JSON.stringify({
                    id: response.userId,
                    email: response.email,
                    name: response.name
                }));
            }

            return response;
        } catch (error) {
            console.error('Registration failed:', error);
            throw error;
        }
    }

    logout() {
        this.removeToken();
        localStorage.removeItem(config.storage.userData);
        // Clear other stored data if needed
    }

    // Check if user is authenticated
    isAuthenticated() {
        const token = this.getToken();
        const userData = localStorage.getItem(config.storage.userData);
        return !!(token && userData);
    }

    // Get current user data
    getCurrentUser() {
        const userData = localStorage.getItem(config.storage.userData);
        return userData ? JSON.parse(userData) : null;
    }

    // Health check
    async healthCheck() {
        try {
            return await this.get(config.api.endpoints.health, { includeAuth: false });
        } catch (error) {
            console.error('Health check failed:', error);
            throw error;
        }
    }
}

// Create singleton instance
export const apiService = new ApiService();
