export class Storage {
    static getItem(key) {
        try {
            const item = localStorage.getItem(`mindmate_${key}`);
            return item ? JSON.parse(item) : null;
        } catch (error) {
            console.error(`Error retrieving ${key} from storage:`, error);
            return null;
        }
    }

    static setItem(key, value) {
        try {
            localStorage.setItem(`mindmate_${key}`, JSON.stringify(value));
            return true;
        } catch (error) {
            console.error(`Error storing ${key} in storage:`, error);
            return false;
        }
    }

    static removeItem(key) {
        try {
            localStorage.removeItem(`mindmate_${key}`);
            return true;
        } catch (error) {
            console.error(`Error removing ${key} from storage:`, error);
            return false;
        }
    }

    // User-specific storage methods
    static getUserData() {
        return this.getItem('user');
    }

    static setUserData(userData) {
        return this.setItem('user', userData);
    }

    static clearUserData() {
        return this.removeItem('user');
    }

    // Notes storage methods
    static getNotes() {
        return this.getItem('notes') || [];
    }

    static setNotes(notes) {
        return this.setItem('notes', notes);
    }

    // Tasks storage methods
    static getTasks() {
        return this.getItem('tasks') || [];
    }

    static setTasks(tasks) {
        return this.setItem('tasks', tasks);
    }

    // Mood entries storage methods
    static getMoods() {
        return this.getItem('moods') || [];
    }

    static setMoods(moods) {
        return this.setItem('moods', moods);
    }

    // Chat messages storage methods
    static getChatMessages() {
        return this.getItem('chat_messages') || [];
    }

    static setChatMessages(messages) {
        return this.setItem('chat_messages', messages);
    }

    static clearChatMessages() {
        return this.removeItem('chat_messages');
    }
}
