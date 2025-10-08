
import { config } from '../config.js';
import { apiService } from '../utils/apiService.js';
import { groqService } from '../utils/groqService.js';

export class Chatbot {
    constructor() {
        this.messages = [];
        this.userNotes = [];
        this.userTasks = [];
        this.userMoods = [];
        this.isLoading = false;
        this.initialized = false;
        this.sessionId = this.getOrCreateSessionId();
        this.useBackendMemory = true; // Use backend for persistent memory
    }

    /**
     * Get or create a session ID for this chat session
     */
    getOrCreateSessionId() {
        const userId = apiService.getCurrentUser()?.id;
        const sessionKey = `chatbot_session_${userId}`;
        let sessionId = sessionStorage.getItem(sessionKey);
        
        if (!sessionId) {
            sessionId = `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
            sessionStorage.setItem(sessionKey, sessionId);
        }
        
        return sessionId;
    }

    async initialize() {
        // Wait for DOM to be fully ready
        await new Promise(resolve => setTimeout(resolve, 100));
        
        console.log('Chatbot initializing...');
        
        // Always reload user data when returning to chatbot
        await this.loadUserData();
        
        // Load chat history from backend first, fallback to localStorage
        const backendLoaded = await this.loadChatHistoryFromBackend();
        if (!backendLoaded) {
            this.loadChatHistory(); // Fallback to localStorage
        }
        
        // Always rebind events since DOM may have been replaced
        this.bindEvents();
        
        // Only setup auto-resize once
        if (!this.initialized) {
            this.setupAutoResize();
            this.initialized = true;
        }
        
        console.log('Chatbot initialized successfully!');
    }

    /**
     * Load user's notes, tasks, and moods from backend
     */
    async loadUserData() {
        try {
            console.log('Loading user data for chatbot...');
            const [notes, tasks, moods] = await Promise.all([
                this.fetchNotes(),
                this.fetchTasks(),
                this.fetchMoods()
            ]);

            this.userNotes = notes || [];
            this.userTasks = tasks || [];
            this.userMoods = moods || [];

            console.log('User data loaded:', {
                notes: this.userNotes.length,
                tasks: this.userTasks.length,
                moods: this.userMoods.length
            });
        } catch (error) {
            console.error('Error loading user data:', error);
        }
    }

    /**
     * Load chat history from localStorage
     */
    loadChatHistory() {
        try {
            const userId = apiService.getCurrentUser()?.id;
            if (!userId) return;

            const savedMessages = localStorage.getItem(`chatbot_history_${userId}`);
            if (savedMessages) {
                this.messages = JSON.parse(savedMessages);
                
                // Restore messages to UI
                const messagesContainer = document.getElementById('chat-messages');
                if (messagesContainer) {
                    messagesContainer.innerHTML = `
                        <div class="welcome-message flex gap-2.5 sm:gap-3.5 animate-fade-in max-w-full">
                            <div class="chatbot-avatar w-9 h-9 sm:w-11 sm:h-11 min-w-[36px] sm:min-w-[44px] bg-gradient-to-br from-primary to-primary-dark rounded-xl flex items-center justify-center text-white text-lg sm:text-xl shadow-md">
                                <i class="fas fa-robot"></i>
                            </div>
                            <div class="message-content flex-1 min-w-0 bg-white px-4 sm:px-5 py-3 sm:py-4 rounded-2xl shadow-sm border border-gray-100 break-words">
                                <p class="text-sm sm:text-base mb-3 text-gray-800 leading-relaxed"><strong class="text-primary font-semibold">Welcome back! 🤖</strong></p>
                                <p class="text-sm sm:text-base text-gray-700">Here's your previous conversation. I'm ready to help you with anything!</p>
                            </div>
                        </div>
                    `;
                    
                    // Restore each message
                    this.messages.forEach(msg => {
                        this.addMessageToUI(msg.content, msg.sender);
                    });
                    
                    console.log('Chat history loaded:', this.messages.length, 'messages');
                }
            }
        } catch (error) {
            console.error('Error loading chat history:', error);
        }
    }

    /**
     * Save chat history to localStorage
     */
    saveChatHistory() {
        try {
            const userId = apiService.getCurrentUser()?.id;
            if (!userId) return;

            localStorage.setItem(`chatbot_history_${userId}`, JSON.stringify(this.messages));
        } catch (error) {
            console.error('Error saving chat history:', error);
        }
    }

    /**
     * Load chat history from backend
     */
    async loadChatHistoryFromBackend() {
        try {
            console.log('Loading chat history from backend...');
            const response = await fetch(`${apiService.baseUrl}/api/conversations/recent?limit=50`, {
                method: 'GET',
                headers: apiService.getHeaders()
            });

            if (response.ok) {
                const conversations = await response.json();
                
                if (conversations && conversations.length > 0) {
                    // Convert backend format to local format and REVERSE order (oldest first)
                    this.messages = conversations.reverse().map(conv => ({
                        content: conv.message,
                        sender: conv.sender,
                        timestamp: new Date(conv.createdAt)
                    }));

                    // Restore messages to UI
                    const messagesContainer = document.getElementById('chat-messages');
                    if (messagesContainer) {
                        messagesContainer.innerHTML = `
                            <div class="welcome-message flex gap-2.5 sm:gap-3.5 animate-fade-in max-w-full">
                                <div class="chatbot-avatar w-9 h-9 sm:w-11 sm:h-11 min-w-[36px] sm:min-w-[44px] bg-gradient-to-br from-primary to-primary-dark rounded-xl flex items-center justify-center text-white text-lg sm:text-xl shadow-md">
                                    <i class="fas fa-robot"></i>
                                </div>
                                <div class="message-content flex-1 min-w-0 bg-white px-4 sm:px-5 py-3 sm:py-4 rounded-2xl shadow-sm border border-gray-100 break-words">
                                    <p class="text-sm sm:text-base mb-3 text-gray-800 leading-relaxed"><strong class="text-primary font-semibold">Welcome back! 🤖</strong></p>
                                    <p class="text-sm sm:text-base text-gray-700">Here's your previous conversation. I'm ready to help you with anything!</p>
                                </div>
                            </div>
                        `;

                        // Restore each message
                        this.messages.forEach(msg => {
                            this.addMessageToUI(msg.content, msg.sender);
                        });

                        console.log('Chat history loaded from backend:', this.messages.length, 'messages');
                    }
                    return true;
                }
            }
            return false;
        } catch (error) {
            console.error('Error loading chat history from backend:', error);
            return false;
        }
    }

    /**
     * Save message to backend
     */
    async saveMessageToBackend(message, sender) {
        if (!this.useBackendMemory) return;
        
        try {
            const response = await fetch(`${apiService.baseUrl}/api/conversations`, {
                method: 'POST',
                headers: apiService.getHeaders(),
                body: JSON.stringify({
                    message: message,
                    sender: sender,
                    sessionId: this.sessionId
                })
            });

            if (!response.ok) {
                console.error('Failed to save message to backend');
            }
        } catch (error) {
            console.error('Error saving message to backend:', error);
        }
    }

    /**
     * Clear chat history (both local and backend)
     */
    async clearChatHistory() {
        try {
            // Clear from backend
            if (this.useBackendMemory) {
                const response = await fetch(`${apiService.baseUrl}/api/conversations`, {
                    method: 'DELETE',
                    headers: apiService.getHeaders()
                });
                
                if (response.ok) {
                    console.log('Backend chat history cleared');
                }
            }

            // Clear from localStorage
            const userId = apiService.getCurrentUser()?.id;
            if (userId) {
                localStorage.removeItem(`chatbot_history_${userId}`);
            }

            // Clear from memory
            this.messages = [];
            
            console.log('Chat history cleared');
        } catch (error) {
            console.error('Error clearing chat history:', error);
        }
    }

    async fetchNotes() {
        try {
            const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.notes}`, {
                headers: apiService.getHeaders()
            });
            if (response.ok) {
                const notes = await response.json();
                console.log('Fetched notes:', notes.length);
                return notes;
            }
            return [];
        } catch (error) {
            console.error('Error fetching notes:', error);
            return [];
        }
    }

    async fetchTasks() {
        try {
            const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.tasks}`, {
                headers: apiService.getHeaders()
            });
            if (response.ok) {
                const tasks = await response.json();
                console.log('Fetched tasks:', tasks.length);
                return tasks;
            }
            return [];
        } catch (error) {
            console.error('Error fetching tasks:', error);
            return [];
        }
    }

    async fetchMoods() {
        try {
            const response = await fetch(`${apiService.baseUrl}${config.api.endpoints.mood}/history`, {
                headers: apiService.getHeaders()
            });
            if (response.ok) {
                const moods = await response.json();
                console.log('Fetched moods:', moods.length);
                return moods;
            }
            return [];
        } catch (error) {
            console.error('Error fetching moods:', error);
            return [];
        }
    }

    /**
     * Bind event listeners
     */
    bindEvents() {
        console.log('Binding chatbot events...');
        
        // Remove old listeners by cloning and replacing (prevents duplicates)
        const sendBtn = document.getElementById('send-btn');
        console.log('Send button found:', !!sendBtn);
        if (sendBtn) {
            const newSendBtn = sendBtn.cloneNode(true);
            sendBtn.parentNode.replaceChild(newSendBtn, sendBtn);
            newSendBtn.addEventListener('click', () => {
                console.log('Send button clicked!');
                this.handleSend();
            });
        }

        // Input field
        const chatInput = document.getElementById('chat-input');
        console.log('Chat input found:', !!chatInput);
        if (chatInput) {
            const newChatInput = chatInput.cloneNode(true);
            chatInput.parentNode.replaceChild(newChatInput, chatInput);
            newChatInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    console.log('Enter key pressed!');
                    this.handleSend();
                }
            });
        }

        // New chat button
        const newChatBtn = document.getElementById('new-chat-btn');
        newChatBtn?.addEventListener('click', () => this.newChat());

        // Clear chat button
        const clearBtn = document.getElementById('clear-chat-btn');
        clearBtn?.addEventListener('click', () => this.clearChat());

        // Close button
        const closeBtn = document.getElementById('close-chat-btn');
        closeBtn?.addEventListener('click', () => {
            window.app.router.navigate('/dashboard');
        });

        // Suggestion chips
        document.querySelectorAll('.suggestion-chip').forEach(chip => {
            chip.addEventListener('click', () => {
                const suggestion = chip.dataset.suggestion;
                document.getElementById('chat-input').value = suggestion;
                this.handleSend();
            });
        });

        // Attach button (quick actions)
        const attachBtn = document.getElementById('attach-btn');
        const quickActionsMenu = document.getElementById('quick-actions-menu');
        attachBtn?.addEventListener('click', (e) => {
            e.stopPropagation();
            if (quickActionsMenu) {
                quickActionsMenu.style.display = 
                    quickActionsMenu.style.display === 'none' || quickActionsMenu.style.display === '' 
                    ? 'block' : 'none';
            }
        });

        // Quick action items
        document.querySelectorAll('.quick-action-item').forEach(item => {
            item.addEventListener('click', () => {
                const action = item.dataset.action;
                this.handleQuickAction(action);
                if (quickActionsMenu) {
                    quickActionsMenu.style.display = 'none';
                }
            });
        });

        // Close quick actions when clicking outside
        document.addEventListener('click', (e) => {
            const menu = document.getElementById('quick-actions-menu');
            if (menu && !e.target.closest('.chat-input-container')) {
                menu.style.display = 'none';
            }
        });
    }

    /**
     * Setup auto-resize for textarea
     */
    setupAutoResize() {
        const chatInput = document.getElementById('chat-input');
        chatInput?.addEventListener('input', () => {
            chatInput.style.height = 'auto';
            chatInput.style.height = chatInput.scrollHeight + 'px';
        });
    }

    /**
     * Handle sending a message
     */
    async handleSend() {
        const chatInput = document.getElementById('chat-input');
        const message = chatInput?.value?.trim();

        console.log('handleSend called', { message, isLoading: this.isLoading, hasInput: !!chatInput });

        if (!message || this.isLoading) {
            console.log('Message send blocked:', { message, isLoading: this.isLoading });
            return;
        }

        // Clear input
        chatInput.value = '';
        chatInput.style.height = 'auto';

        // Add user message to UI
        this.addMessage(message, 'user');

        // Show typing indicator
        this.showTypingIndicator();

        // Determine intent and process message
        const intent = this.detectIntent(message);
        await this.processMessage(message, intent);

        // Hide typing indicator
        this.hideTypingIndicator();
    }

    /**
     * Detect user intent from message
     */
    detectIntent(message) {
        const lowerMessage = message.toLowerCase();
        console.log('Detecting intent for:', message);

        // Optimized intent patterns using regex for better performance
        const patterns = {
            'analyze-mood': () => /mood/.test(lowerMessage) && (/analyz|pattern|track|diagnosis|diagnos|how.*feel|feeling/.test(lowerMessage) || /what.*mood|my mood/.test(lowerMessage)),
            'analyze-notes': () => lowerMessage.includes('note') && /summar|analyz|review/.test(lowerMessage),
            'analyze-tasks': () => lowerMessage.includes('task') && /suggest|priorit|help/.test(lowerMessage),
            'recommend-songs': () => /song|music|recommend/.test(lowerMessage),
            'create-note': () => /create|add|write|make/.test(lowerMessage) && /note|reminder|memo/.test(lowerMessage),
            'create-task': () => /create|add|set|make/.test(lowerMessage) && /task|todo|to-do|to do/.test(lowerMessage),
            'delete-note': () => /delete|remove/.test(lowerMessage) && /note|notes/.test(lowerMessage),
            'delete-task': () => /delete|remove/.test(lowerMessage) && /task|tasks|todo/.test(lowerMessage),
            'log-mood': () => /log|add|record|feeling/.test(lowerMessage) && /mood|emotion/.test(lowerMessage),
            'update-note': () => {
                const hasEdit = /\b(edit|update|change|modify|esit|uodate|chnage|modiy)\b/i.test(lowerMessage);
                return hasEdit && (/\b\d+\b/.test(lowerMessage) || /\b(note|notes|the note|teh note)\b/i.test(lowerMessage));
            },
            'update-task': () => /update|edit|change|modify|mark|complete|finish|done/.test(lowerMessage) && /task|tasks/.test(lowerMessage),
            'view-notes': () => /show|list|view|see|give|display|what are|get/.test(lowerMessage) && /note|notes/.test(lowerMessage),
            'view-tasks': () => /show|list|view|see|give|display|what are|get/.test(lowerMessage) && /task|tasks/.test(lowerMessage),
            'view-moods': () => /show|list|view|see|give|display|what are|get/.test(lowerMessage) && /mood|moods|feeling/.test(lowerMessage),
            'comprehensive-analysis': () => /overall|comprehensive|wellbeing/.test(lowerMessage)
        };

        // Find and return first matching intent
        for (const [intent, check] of Object.entries(patterns)) {
            if (check()) {
                console.log('→ Intent:', intent);
                return intent;
            }
        }

        console.log('→ Intent: general-question');
        return 'general-question';
    }

    /**
     * Process message based on intent
     */
    async processMessage(message, intent) {
        this.isLoading = true;

        try {
            let response;

            // Check for compound commands (multiple actions in one message)
            // Pattern: "edit 1 to X and delete 2"
            const compoundPattern = /\b(edit|update|change)\s+(\d+)\s+.*?\b(and|then)\s+(delete|remove)\s+(\d+)\b/i;
            const compoundMatch = message.match(compoundPattern);
            
            if (compoundMatch) {
                console.log('Compound command detected:', compoundMatch[0]);
                
                // Extract the two commands
                const updateNumber = compoundMatch[2];
                const deleteNumber = compoundMatch[5];
                
                // Extract the update content (between number and "and")
                const updatePart = message.substring(0, message.toLowerCase().indexOf(' and ')).trim();
                const deletePart = `delete ${deleteNumber}`;
                
                console.log('Split commands:', { updatePart, deletePart });
                
                // Execute update first
                const updateResponse = await this.handleUpdateNote(updatePart);
                this.addMessage(updateResponse.message, 'bot');
                
                // Then execute delete
                const deleteResponse = await this.handleDeleteNote(deletePart);
                response = deleteResponse;
                
                // Combine responses
                response.message = `✅ **Actions completed:**\n\n1️⃣ ${updateResponse.message}\n\n2️⃣ ${deleteResponse.message}`;
            } else {
                // Single command - process normally
                switch (intent) {
                    case 'analyze-mood':
                        response = await this.analyzeMoodPatterns();
                        break;
                    case 'analyze-notes':
                        response = await this.analyzeNotes();
                        break;
                    case 'analyze-tasks':
                        response = await this.analyzeTasks();
                        break;
                    case 'recommend-songs':
                        response = await this.recommendSongs();
                        break;
                    case 'create-note':
                        response = await this.handleCreateNote(message);
                        break;
                    case 'create-task':
                        response = await this.handleCreateTask(message);
                        break;
                    case 'delete-note':
                        response = await this.handleDeleteNote(message);
                        break;
                    case 'delete-task':
                        response = await this.handleDeleteTask(message);
                        break;
                    case 'log-mood':
                        response = await this.handleLogMood(message);
                        break;
                    case 'update-note':
                        response = await this.handleUpdateNote(message);
                        break;
                    case 'update-task':
                        response = await this.handleUpdateTask(message);
                        break;
                case 'view-notes':
                    response = await this.handleViewNotes();
                    break;
                case 'view-tasks':
                    response = await this.handleViewTasks();
                    break;
                case 'view-moods':
                    response = await this.handleViewMoods();
                    break;
                case 'comprehensive-analysis':
                    response = await this.comprehensiveAnalysis();
                    break;
                default:
                    response = await this.handleGeneralQuestion(message);
                }
            }

            if (response.success) {
                this.addMessage(response.message, 'bot');
            } else {
                this.addMessage(`Sorry, I encountered an error: ${response.error}. Please try again.`, 'bot');
            }

            // Auto-detect and log mood from conversation (silent operation)
            try {
                await this.updateMoodFromConversation(message);
            } catch (moodError) {
                // Silent failure - don't interrupt conversation
                console.debug('Mood detection skipped:', moodError.message);
            }

        } catch (error) {
            console.error('Error processing message:', error);
            this.addMessage('Sorry, something went wrong. Please try again later.', 'bot');
        } finally {
            this.isLoading = false;
        }
    }

    /**
     * Auto-detect mood from conversation and log it silently
     */
    async updateMoodFromConversation(message) {
        try {
            // Skip mood detection for commands/short messages
            if (message.length < 10 || /^(show|list|view|get|edit|update|delete|create|add)\s/i.test(message)) {
                console.log('🚫 Auto-mood: Skipped (command or too short)');
                return; // Skip command-like messages
            }
            
            console.log('🔍 Auto-mood: Analyzing message:', message.substring(0, 50) + '...');
            
            // Use AI to analyze sentiment with correct mood types
            const sentimentPrompt = `Analyze the emotional sentiment of this message: "${message}"\n\nProvide ONLY a valid JSON response with: {"mood": "MOOD_TYPE", "confidence": 0-1, "keywords": ["word1", "word2"]}.\n\nAllowed MOOD_TYPE values: SAD, STRESSED, RELAXED, ANXIOUS, EXCITED, ANGRY, CONTENT, TIRED, ENERGETIC\n\nMapping guide:\n- "fine", "okay", "good" → CONTENT\n- "happy", "great", "awesome" → EXCITED\n- "calm", "peaceful" → RELAXED\n- "sad", "down", "depressed" → SAD\n- "stressed", "overwhelmed" → STRESSED\n- "anxious", "worried", "nervous" → ANXIOUS\n- "angry", "frustrated", "annoyed" → ANGRY\n- "tired", "exhausted", "sleepy" → TIRED\n- "energetic", "pumped", "motivated" → ENERGETIC\n\nOnly respond if there's a clear emotional indicator (confidence > 0.6). If neutral/unclear, respond with {"mood": "NEUTRAL", "confidence": 0}.\n\nRESPOND ONLY WITH JSON, NO OTHER TEXT.`;
            
            const response = await groqService.chat(sentimentPrompt);
            
            if (response.success) {
                console.log('🤖 Auto-mood: AI response received');
                
                // Extract JSON more carefully
                let jsonText = response.message.trim();
                
                // Remove markdown code blocks if present
                jsonText = jsonText.replace(/```json?\s*/g, '').replace(/```\s*/g, '');
                
                // Find JSON object
                const jsonMatch = jsonText.match(/\{[\s\S]*?\}/);
                if (jsonMatch) {
                    const sentiment = JSON.parse(jsonMatch[0]);
                    console.log('📊 Auto-mood: Sentiment detected:', sentiment);
                    
                    // Only log if confidence is high enough and not neutral
                    if (sentiment.confidence > 0.6 && sentiment.mood !== 'NEUTRAL') {
                        // Validate mood type against backend enum
                        const validMoods = ['SAD', 'STRESSED', 'RELAXED', 'ANXIOUS', 'EXCITED', 'ANGRY', 'CONTENT', 'TIRED', 'ENERGETIC'];
                        const moodType = sentiment.mood.toUpperCase();
                        
                        if (!validMoods.includes(moodType)) {
                            console.warn(`❌ Auto-mood: Invalid mood type: ${moodType}, skipping`);
                            return;
                        }
                        
                        const moodData = {
                            moodType: moodType,
                            notes: `Auto-detected from chat. Keywords: ${sentiment.keywords?.join(', ') || 'N/A'}`.substring(0, 500)
                        };
                        
                        console.log('📤 Auto-mood: Sending to backend:', moodData);
                        
                        // Log mood silently
                        const moodResponse = await fetch(`${apiService.baseUrl}${config.api.endpoints.mood}/add`, {
                            method: 'POST',
                            headers: apiService.getHeaders(),
                            body: JSON.stringify(moodData)
                        });
                        
                        if (moodResponse.ok) {
                            const result = await moodResponse.json();
                            console.log('✅ Auto-mood: Successfully logged!', result);
                            
                            // Refresh mood data silently
                            await this.loadUserData();
                        } else {
                            const errorText = await moodResponse.text();
                            console.error('❌ Auto-mood: Failed to log mood:', moodResponse.status, errorText);
                        }
                    } else {
                        console.log('🤷 Auto-mood: Confidence too low or neutral mood, skipping');
                    }
                } else {
                    console.warn('⚠️ Auto-mood: No JSON found in AI response');
                }
            } else {
                console.error('❌ Auto-mood: AI request failed:', response.error);
            }
        } catch (error) {
            // Silent failure - don't interrupt conversation
            console.error('❌ Auto-mood: Error during detection:', error);
        }
    }

    /**
     * Analyze mood patterns
     */
    async analyzeMoodPatterns() {
        if (this.userMoods.length === 0) {
            return {
                success: true,
                message: "You haven't logged any moods yet. Start tracking your mood to get personalized insights! You can log your mood by clicking the 'Log Mood' button on the dashboard."
            };
        }

        return await groqService.analyzeMood(this.userMoods);
    }

    /**
     * Analyze notes
     */
    async analyzeNotes() {
        if (this.userNotes.length === 0) {
            return {
                success: true,
                message: "You don't have any notes yet. Create some notes to get AI-powered insights and analysis!"
            };
        }

        return await groqService.analyzeNotes(this.userNotes);
    }

    /**
     * Analyze tasks
     */
    async analyzeTasks() {
        if (this.userTasks.length === 0) {
            return {
                success: true,
                message: "You don't have any tasks yet. Create some tasks and I'll help you prioritize and organize them!"
            };
        }

        return await groqService.analyzeTasks(this.userTasks);
    }

    /**
     * Recommend songs based on mood
     */
    async recommendSongs() {
        if (this.userMoods.length === 0) {
            return {
                success: true,
                message: "I don't have any mood data yet to base song recommendations on. Try:\n\n1. Log your current mood in the Mood Tracker\n2. Tell me how you're feeling, and I'll detect it automatically\n3. Just chat with me - I can sense your emotions!\n\nOnce I understand your mood, I'll recommend perfect songs for you 🎵"
            };
        }

        const latestMood = this.userMoods[0];
        const result = await groqService.recommendSongs(latestMood);
        
        // Enhance the response with emoji and formatting
        if (result.success && result.message) {
            const enhancedMessage = `🎵 **Songs for Your ${latestMood.moodType} Mood**\n\n${result.message}\n\n💡 *Tip: Music can really help shift or enhance your emotional state!*`;
            return {
                success: true,
                message: enhancedMessage
            };
        }
        
        return result;
    }

    /**
     * Comprehensive wellbeing analysis
     */
    async comprehensiveAnalysis() {
        if (this.userNotes.length === 0 && this.userTasks.length === 0 && this.userMoods.length === 0) {
            return {
                success: true,
                message: "I don't have enough data yet to provide a comprehensive analysis. Start by:\n\n✅ Creating some tasks\n📝 Writing some notes\n💚 Logging your mood\n\nOnce you have some data, I'll provide detailed insights about your wellbeing!"
            };
        }

        return await groqService.comprehensiveAnalysis(this.userNotes, this.userTasks, this.userMoods);
    }

    /**
     * Handle general questions
     */
    async handleGeneralQuestion(question) {
        const contextData = {
            notes: this.userNotes,
            tasks: this.userTasks,
            moods: this.userMoods
        };

        const response = await groqService.askQuestion(question, contextData);
        
        // Filter out raw JSON responses (from mood detection leaking into general responses)
        if (response.success && response.message) {
            // Check if response is ONLY JSON (starts with { and ends with })
            const trimmed = response.message.trim();
            if (trimmed.startsWith('{') && trimmed.endsWith('}') && !trimmed.includes('\n')) {
                // This is a JSON-only response, likely an error - provide helpful fallback
                return {
                    success: true,
                    message: "I'm here to help! You can:\n\n📝 View, create, update, or delete notes\n✅ Manage your tasks\n💚 Track your mood\n🎵 Get song recommendations\n\nWhat would you like to do?"
                };
            }
        }
        
        return response;
    }

    /**
     * Handle create note request
     */
    async handleCreateNote(message) {
        // Extract note details from message using AI
        const extractionPrompt = `Extract note details from this message: "${message}"\nProvide a JSON response with: {"title": "...", "content": "...", "category": "PERSONAL/WORK/HEALTH/IDEAS/GOALS"}. Make sure category is uppercase.`;
        
        const response = await groqService.chat(extractionPrompt);
        
        if (response.success) {
            try {
                // Try to parse JSON from the response
                const jsonMatch = response.message.match(/\{[\s\S]*\}/);
                if (jsonMatch) {
                    const noteData = JSON.parse(jsonMatch[0]);
                    
                    // Ensure category is uppercase and add empty tags array
                    noteData.category = noteData.category?.toUpperCase() || 'PERSONAL';
                    noteData.tags = noteData.tags || [];
                    
                    // Create the note using correct endpoint
                    const createResponse = await fetch(`${apiService.baseUrl}${config.api.endpoints.notes}`, {
                        method: 'POST',
                        headers: apiService.getHeaders(),
                        body: JSON.stringify(noteData)
                    });

                    if (createResponse.ok) {
                        await this.loadUserData(); // Refresh data
                        return {
                            success: true,
                            message: `✅ Note created successfully!\n\n**${noteData.title}**\n${noteData.content}\n\nCategory: ${noteData.category}`
                        };
                    } else {
                        const errorData = await createResponse.json();
                        console.error('Failed to create note:', errorData);
                        return {
                            success: false,
                            message: `Failed to create note: ${errorData.message || 'Unknown error'}`
                        };
                    }
                }
            } catch (error) {
                console.error('Error creating note:', error);
                return {
                    success: false,
                    message: `Error creating note: ${error.message}`
                };
            }
        }

        return {
            success: true,
            message: "I'd be happy to help you create a note! Please provide more details, such as:\n- What should the title be?\n- What content should it contain?\n- What category? (Personal/Work/Health/Ideas/Goals)"
        };
    }

    /**
     * Handle create task request
     */
    async handleCreateTask(message) {
        // Extract task details from message using AI
        const extractionPrompt = `Extract task details from this message: "${message}"\nProvide a JSON response with: {"title": "...", "description": "...", "priority": "low/medium/high", "category": "Personal/Work/Health/Shopping/Errands"}. Use lowercase for priority.`;
        
        const response = await groqService.chat(extractionPrompt);
        
        if (response.success) {
            try {
                // Try to parse JSON from the response
                const jsonMatch = response.message.match(/\{[\s\S]*\}/);
                if (jsonMatch) {
                    const taskData = JSON.parse(jsonMatch[0]);
                    
                    // Ensure priority is lowercase (backend requirement) and set defaults
                    taskData.priority = taskData.priority?.toLowerCase() || 'medium';
                    taskData.category = taskData.category || 'General';
                    taskData.description = taskData.description || '';
                    taskData.completed = false;
                    taskData.dueDate = null; // Optional field
                    
                    console.log('Creating task with data:', taskData);
                    
                    // Create the task using correct endpoint
                    const createResponse = await fetch(`${apiService.baseUrl}${config.api.endpoints.tasks}`, {
                        method: 'POST',
                        headers: apiService.getHeaders(),
                        body: JSON.stringify(taskData)
                    });

                    if (createResponse.ok) {
                        await this.loadUserData(); // Refresh data
                        return {
                            success: true,
                            message: `✅ Task created successfully!\n\n**${taskData.title}**\n${taskData.description}\n\nPriority: ${taskData.priority.toUpperCase()}\nCategory: ${taskData.category}`
                        };
                    } else {
                        const errorData = await createResponse.json();
                        console.error('Failed to create task:', errorData);
                        return {
                            success: false,
                            message: `Failed to create task: ${errorData.message || 'Unknown error'}. Please try again with more details.`
                        };
                    }
                }
            } catch (error) {
                console.error('Error creating task:', error);
                return {
                    success: false,
                    message: `Error creating task: ${error.message}`
                };
            }
        }

        return {
            success: true,
            message: "I'd be happy to help you create a task! Please provide more details, such as:\n- What's the task title?\n- What's the description?\n- What priority? (Low/Medium/High)\n- What category? (Personal/Work/Health/Shopping/Errands)"
        };
    }

    /**
     * Handle delete note request
     */
    async handleDeleteNote(message) {
        if (this.userNotes.length === 0) {
            return {
                success: true,
                message: "You don't have any notes to delete."
            };
        }

        // Use AI to identify which note to delete
        const notesList = this.userNotes.map((n, i) => `${i + 1}. ${n.title} (${n.category})`).join('\n');
        const identificationPrompt = `User wants to delete a note. Here are their notes:\n\n${notesList}\n\nUser message: "${message}"\n\nWhich note should be deleted? Respond with ONLY the number (1-${this.userNotes.length}) or "UNCLEAR" if you cannot determine which note.`;
        
        const response = await groqService.chat(identificationPrompt);
        
        if (response.success) {
            const match = response.message.match(/\d+/);
            if (match) {
                const index = parseInt(match[0]) - 1;
                if (index >= 0 && index < this.userNotes.length) {
                    const noteToDelete = this.userNotes[index];
                    
                    try {
                        const deleteResponse = await fetch(`${apiService.baseUrl}${config.api.endpoints.notes}/${noteToDelete.id}`, {
                            method: 'DELETE',
                            headers: apiService.getHeaders()
                        });

                        if (deleteResponse.ok) {
                            await this.loadUserData(); // Refresh data
                            return {
                                success: true,
                                message: `✅ Note deleted successfully!\n\n**"${noteToDelete.title}"** has been removed.`
                            };
                        } else {
                            const errorData = await deleteResponse.json().catch(() => ({}));
                            return {
                                success: false,
                                message: `Failed to delete note: ${errorData.message || 'Unknown error'}`
                            };
                        }
                    } catch (error) {
                        console.error('Error deleting note:', error);
                        return {
                            success: false,
                            message: `Error deleting note: ${error.message}`
                        };
                    }
                }
            }
        }

        // If we couldn't identify the note, show a list
        return {
            success: true,
            message: `I'm not sure which note you want to delete. Here are your notes:\n\n${notesList}\n\nPlease tell me the number or be more specific about which note to delete.`
        };
    }

    /**
     * Handle delete task request
     */
    async handleDeleteTask(message) {
        if (this.userTasks.length === 0) {
            return {
                success: true,
                message: "You don't have any tasks to delete."
            };
        }

        // Use AI to identify which task to delete
        const tasksList = this.userTasks.map((t, i) => `${i + 1}. ${t.title} (${t.priority} priority)`).join('\n');
        const identificationPrompt = `User wants to delete a task. Here are their tasks:\n\n${tasksList}\n\nUser message: "${message}"\n\nWhich task should be deleted? Respond with ONLY the number (1-${this.userTasks.length}) or "UNCLEAR" if you cannot determine which task.`;
        
        const response = await groqService.chat(identificationPrompt);
        
        if (response.success) {
            const match = response.message.match(/\d+/);
            if (match) {
                const index = parseInt(match[0]) - 1;
                if (index >= 0 && index < this.userTasks.length) {
                    const taskToDelete = this.userTasks[index];
                    
                    try {
                        const deleteResponse = await fetch(`${apiService.baseUrl}${config.api.endpoints.tasks}/${taskToDelete.id}`, {
                            method: 'DELETE',
                            headers: apiService.getHeaders()
                        });

                        if (deleteResponse.ok) {
                            await this.loadUserData(); // Refresh data
                            return {
                                success: true,
                                message: `✅ Task deleted successfully!\n\n**"${taskToDelete.title}"** has been removed.`
                            };
                        } else {
                            const errorData = await deleteResponse.json().catch(() => ({}));
                            return {
                                success: false,
                                message: `Failed to delete task: ${errorData.message || 'Unknown error'}`
                            };
                        }
                    } catch (error) {
                        console.error('Error deleting task:', error);
                        return {
                            success: false,
                            message: `Error deleting task: ${error.message}`
                        };
                    }
                }
            }
        }

        // If we couldn't identify the task, show a list
        return {
            success: true,
            message: `I'm not sure which task you want to delete. Here are your tasks:\n\n${tasksList}\n\nPlease tell me the number or be more specific about which task to delete.`
        };
    }

    /**
     * Handle log mood request
     */
    async handleLogMood(message) {
        // Extract mood from message using AI
        const extractionPrompt = `Extract mood details from this message: "${message}"\nProvide a JSON response with: {"moodType": "SAD/ANXIOUS/CALM/HAPPY/EXCITED", "notes": "optional description"}. Make sure moodType is uppercase.`;
        
        const response = await groqService.chat(extractionPrompt);
        
        if (response.success) {
            try {
                const jsonMatch = response.message.match(/\{[\s\S]*\}/);
                if (jsonMatch) {
                    const moodData = JSON.parse(jsonMatch[0]);
                    
                    // Ensure moodType is uppercase
                    moodData.moodType = moodData.moodType?.toUpperCase() || 'CALM';
                    moodData.notes = moodData.notes || '';
                    
                    // Log the mood
                    const createResponse = await fetch(`${apiService.baseUrl}${config.api.endpoints.mood}/add`, {
                        method: 'POST',
                        headers: apiService.getHeaders(),
                        body: JSON.stringify(moodData)
                    });

                    if (createResponse.ok) {
                        await this.loadUserData(); // Refresh data
                        const moodEmoji = {
                            'SAD': '😢',
                            'ANXIOUS': '😰',
                            'CALM': '😌',
                            'HAPPY': '😊',
                            'EXCITED': '🤩'
                        };
                        return {
                            success: true,
                            message: `✅ Mood logged successfully! ${moodEmoji[moodData.moodType] || '💚'}\n\n**Mood: ${moodData.moodType}**\n${moodData.notes ? `Notes: ${moodData.notes}` : ''}\n\nKeep tracking your emotional journey!`
                        };
                    } else {
                        const errorData = await createResponse.json();
                        return {
                            success: false,
                            message: `Failed to log mood: ${errorData.message || 'Unknown error'}`
                        };
                    }
                }
            } catch (error) {
                console.error('Error logging mood:', error);
                return {
                    success: false,
                    message: `Error logging mood: ${error.message}`
                };
            }
        }

        return {
            success: true,
            message: "I'd be happy to help you log your mood! Tell me how you're feeling:\n\n😢 Sad\n😰 Anxious\n😌 Calm\n😊 Happy\n🤩 Excited\n\nYou can also add notes about why you're feeling this way!"
        };
    }

    /**
     * Handle update note request
     */
    async handleUpdateNote(message) {
        if (this.userNotes.length === 0) {
            return {
                success: true,
                message: "You don't have any notes to update."
            };
        }

        // Pre-process message to extract the actual command
        // If message contains the full notes list, extract just the command part
        let cleanMessage = message;
        
        // Check if message contains note display pattern (from copy-paste)
        if (message.includes('📝 Your Notes') || message.includes('Category:') || message.includes('Created:')) {
            // Extract the command part (usually at the end)
            // Look for common command patterns: "change this to X", "update to X", "edit to X"
            const commandMatch = message.match(/(change|update|edit|modify).*?(to|into)\s+(.+)$/i);
            if (commandMatch) {
                // Extract: "change/edit/update [number] to [new content]"
                const afterTo = commandMatch[3].trim();
                // Try to find the number in the original message
                const numberMatch = message.match(/\b(\d+)\.\s/);
                const noteNumber = numberMatch ? numberMatch[1] : '1';
                cleanMessage = `edit ${noteNumber} to ${afterTo}`;
                console.log('Cleaned message from copy-paste:', cleanMessage);
            }
        }

        // Use AI to identify which note and what to update
        const notesList = this.userNotes.map((n, i) => `${i + 1}. Title: "${n.title}" | Content: "${n.content.substring(0, 80)}..." | Category: ${n.category}`).join('\n');
        const identificationPrompt = `User wants to update a note. Here are their notes:

${notesList}

User message: "${cleanMessage}"

CRITICAL PATTERN MATCHING:
1. ALWAYS prioritize direct number extraction:
   - "edit 1" → noteIndex: 1
   - "update note 2" → noteIndex: 2
   - "change 3" → noteIndex: 3

2. Extract new content from "to" pattern:
   - "edit 1 [ANY TEXT] to [NEW TEXT]" → newContent: "[NEW TEXT]"
   - IGNORE everything between the number and "to"
   - Take everything AFTER "to" as newContent
   
3. Example (EXACT USER PATTERN):
   Input: "edit 1 missing cs lecture to attending cs lecture"
   Step 1: Extract number → noteIndex: 1
   Step 2: Find "to" → split at "to"
   Step 3: Take text after "to" → newContent: "attending cs lecture"
   Output: {"noteIndex": 1, "newContent": "attending cs lecture"}

4. Other examples:
   - "update 2 old stuff to new stuff" → {"noteIndex": 2, "newContent": "new stuff"}
   - "change note 3 to different content" → {"noteIndex": 3, "newContent": "different content"}

Respond ONLY with valid JSON: {"noteIndex": number, "newTitle"?: "...", "newContent"?: "...", "newCategory"?: "WORK/PERSONAL/HEALTH/OTHER"}`;
        
        const response = await groqService.chat(identificationPrompt);
        
        if (response.success) {
            try {
                const jsonMatch = response.message.match(/\{[\s\S]*\}/);
                if (jsonMatch) {
                    const updateData = JSON.parse(jsonMatch[0]);
                    console.log('AI parsed update data:', updateData);
                    
                    const index = updateData.noteIndex - 1;
                    console.log('Note index:', index, 'Total notes:', this.userNotes.length);
                    
                    if (index >= 0 && index < this.userNotes.length) {
                        const noteToUpdate = this.userNotes[index];
                        console.log('Note to update:', {
                            id: noteToUpdate.id,
                            currentTitle: noteToUpdate.title,
                            currentContent: noteToUpdate.content,
                            currentCategory: noteToUpdate.category
                        });
                        
                        // Prepare update payload
                        const updatedNote = {
                            title: updateData.newTitle || noteToUpdate.title,
                            content: updateData.newContent || noteToUpdate.content,
                            category: updateData.newCategory?.toUpperCase() || noteToUpdate.category,
                            tags: noteToUpdate.tags || []
                        };
                        
                        console.log('UPDATE PAYLOAD:', updatedNote);
                        console.log('UPDATE URL:', `${apiService.baseUrl}${config.api.endpoints.notes}/${noteToUpdate.id}`);
                        
                        const updateResponse = await fetch(`${apiService.baseUrl}${config.api.endpoints.notes}/${noteToUpdate.id}`, {
                            method: 'PUT',
                            headers: apiService.getHeaders(),
                            body: JSON.stringify(updatedNote)
                        });

                        console.log('Update response status:', updateResponse.status);

                        if (updateResponse.ok) {
                            const responseData = await updateResponse.json();
                            console.log('Update successful, response:', responseData);
                            await this.loadUserData();
                            return {
                                success: true,
                                message: `✅ Note updated successfully!\n\n**${updatedNote.title}**\n${updatedNote.content}\n\nCategory: ${updatedNote.category}`
                            };
                        } else {
                            const errorData = await updateResponse.json().catch(() => ({}));
                            console.error('Update failed:', errorData);
                            return {
                                success: false,
                                message: `Failed to update note: ${errorData.message || updateResponse.statusText || 'Unknown error'}`
                            };
                        }
                    } else {
                        console.error('Invalid note index:', index);
                        return {
                            success: false,
                            message: `Invalid note number. Please choose between 1 and ${this.userNotes.length}.`
                        };
                    }
                } else {
                    console.error('No JSON found in AI response:', response.message);
                }
            } catch (error) {
                console.error('Error updating note:', error);
                return {
                    success: false,
                    message: `Error updating note: ${error.message}`
                };
            }
        }

        return {
            success: true,
            message: `I'm not sure which note to update or what changes to make. Here are your notes:\n\n${notesList}\n\nPlease specify:\n- Which note (by number or title)\n- What to update (title/content/category)\n- The new value`
        };
    }

    /**
     * Handle update task request
     */
    async handleUpdateTask(message) {
        if (this.userTasks.length === 0) {
            return {
                success: true,
                message: "You don't have any tasks to update."
            };
        }

        // Use AI to identify which task and what to update
        const tasksList = this.userTasks.map((t, i) => `${i + 1}. ${t.title} - ${t.priority} priority - ${t.completed ? 'Completed' : 'Pending'}`).join('\n');
        const identificationPrompt = `User wants to update a task. Here are their tasks:\n\n${tasksList}\n\nUser message: "${message}"\n\nProvide JSON with fields to update: {"taskIndex": 1-${this.userTasks.length}, "newTitle": "...", "newDescription": "...", "newPriority": "low/medium/high", "completed": true/false}. 

IMPORTANT:
- Only include fields that should be updated
- If user says "mark as complete", "finish", "done", or "complete", set "completed": true
- If user says "mark as incomplete" or "reopen", set "completed": false
- For priority changes, use lowercase: "low", "medium", or "high"
- taskIndex is REQUIRED, other fields are optional`;
        
        const response = await groqService.chat(identificationPrompt);
        
        if (response.success) {
            try {
                const jsonMatch = response.message.match(/\{[\s\S]*\}/);
                if (jsonMatch) {
                    const updateData = JSON.parse(jsonMatch[0]);
                    const index = updateData.taskIndex - 1;
                    
                    if (index >= 0 && index < this.userTasks.length) {
                        const taskToUpdate = this.userTasks[index];
                        
                        // Prepare update payload
                        const updatedTask = {
                            title: updateData.newTitle || taskToUpdate.title,
                            description: updateData.newDescription || taskToUpdate.description,
                            priority: (updateData.newPriority || taskToUpdate.priority).toLowerCase(),
                            category: taskToUpdate.category,
                            completed: updateData.completed !== undefined ? updateData.completed : taskToUpdate.completed,
                            dueDate: taskToUpdate.dueDate
                        };
                        
                        console.log('Updating task:', taskToUpdate.id, 'with data:', updatedTask);
                        
                        const updateResponse = await fetch(`${apiService.baseUrl}${config.api.endpoints.tasks}/${taskToUpdate.id}`, {
                            method: 'PUT',
                            headers: apiService.getHeaders(),
                            body: JSON.stringify(updatedTask)
                        });

                        console.log('Update response status:', updateResponse.status);

                        if (updateResponse.ok) {
                            const responseData = await updateResponse.json();
                            console.log('Update successful, response:', responseData);
                            await this.loadUserData();
                            return {
                                success: true,
                                message: `✅ Task updated successfully!\n\n**${updatedTask.title}**\n${updatedTask.description}\n\nPriority: ${updatedTask.priority.toUpperCase()}\nStatus: ${updatedTask.completed ? '✓ Completed' : '○ Pending'}`
                            };
                        } else {
                            const errorData = await updateResponse.json().catch(() => ({}));
                            console.error('Update failed:', errorData);
                            return {
                                success: false,
                                message: `Failed to update task: ${errorData.message || updateResponse.statusText || 'Unknown error'}`
                            };
                        }
                    }
                }
            } catch (error) {
                console.error('Error updating task:', error);
                return {
                    success: false,
                    message: `Error updating task: ${error.message}`
                };
            }
        }

        return {
            success: true,
            message: `I'm not sure which task to update or what changes to make. Here are your tasks:\n\n${tasksList}\n\nPlease specify:\n- Which task (by number or title)\n- What to update (title/description/priority/status)\n- The new value`
        };
    }

    /**
     * Handle view notes request
     */
    async handleViewNotes() {
        if (this.userNotes.length === 0) {
            return {
                success: true,
                message: "You don't have any notes yet. Create some notes to keep track of your thoughts and ideas! 📝"
            };
        }

        let notesDisplay = `📝 **Your Notes (${this.userNotes.length})**\n\n`;
        
        this.userNotes.forEach((note, index) => {
            notesDisplay += `**${index + 1}. ${note.title}**\n`;
            notesDisplay += `Category: ${note.category}\n`;
            notesDisplay += `${note.content.substring(0, 100)}${note.content.length > 100 ? '...' : ''}\n`;
            notesDisplay += `Created: ${new Date(note.createdAt).toLocaleDateString()}\n\n`;
        });

        notesDisplay += `\n💡 You can:\n- Create a new note\n- Update an existing note\n- Delete a note\n- Ask me to analyze your notes`;

        return {
            success: true,
            message: notesDisplay
        };
    }

    /**
     * Handle view tasks request
     */
    async handleViewTasks() {
        if (this.userTasks.length === 0) {
            return {
                success: true,
                message: "You don't have any tasks yet. Create some tasks to stay organized! ✅"
            };
        }

        const pendingTasks = this.userTasks.filter(t => !t.completed);
        const completedTasks = this.userTasks.filter(t => t.completed);

        let tasksDisplay = `✅ **Your Tasks (${this.userTasks.length} total)**\n\n`;
        
        if (pendingTasks.length > 0) {
            tasksDisplay += `**📋 Pending (${pendingTasks.length})**\n\n`;
            pendingTasks.forEach((task, index) => {
                const priorityEmoji = { low: '🟢', medium: '🟡', high: '🔴' };
                tasksDisplay += `${index + 1}. ${priorityEmoji[task.priority]} **${task.title}**\n`;
                tasksDisplay += `   Priority: ${task.priority.toUpperCase()} | Category: ${task.category}\n`;
                if (task.description) {
                    tasksDisplay += `   ${task.description.substring(0, 80)}${task.description.length > 80 ? '...' : ''}\n`;
                }
                tasksDisplay += `\n`;
            });
        }

        if (completedTasks.length > 0) {
            tasksDisplay += `\n**✓ Completed (${completedTasks.length})**\n\n`;
            completedTasks.forEach((task, index) => {
                tasksDisplay += `${index + 1}. ~~${task.title}~~\n`;
            });
        }

        tasksDisplay += `\n💡 You can:\n- Create a new task\n- Update a task\n- Mark a task as complete\n- Delete a task`;

        return {
            success: true,
            message: tasksDisplay
        };
    }

    /**
     * Handle view moods request
     */
    async handleViewMoods() {
        if (this.userMoods.length === 0) {
            return {
                success: true,
                message: "You haven't logged any moods yet. Start tracking your emotional journey! 💚\n\nYou can:\n- Log your current mood\n- Tell me how you're feeling\n- I'll auto-detect emotions from our conversation"
            };
        }

        const moodEmojis = {
            'SAD': '😢',
            'ANXIOUS': '😰',
            'CALM': '😌',
            'HAPPY': '😊',
            'EXCITED': '🤩'
        };

        let moodsDisplay = `💚 **Your Mood History (${this.userMoods.length} entries)**\n\n`;
        
        // Show recent moods (last 10)
        const recentMoods = this.userMoods.slice(0, 10);
        
        recentMoods.forEach((mood, index) => {
            const emoji = moodEmojis[mood.moodType] || '💚';
            const date = new Date(mood.createdAt).toLocaleDateString();
            const time = new Date(mood.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            
            moodsDisplay += `${emoji} **${mood.moodType}** - ${date} at ${time}\n`;
            if (mood.notes) {
                moodsDisplay += `   ${mood.notes}\n`;
            }
            moodsDisplay += `\n`;
        });

        if (this.userMoods.length > 10) {
            moodsDisplay += `\n_... and ${this.userMoods.length - 10} more entries_\n`;
        }

        // Add mood distribution
        const moodCounts = {};
        this.userMoods.forEach(mood => {
            moodCounts[mood.moodType] = (moodCounts[mood.moodType] || 0) + 1;
        });

        moodsDisplay += `\n**📊 Mood Distribution:**\n`;
        Object.entries(moodCounts).forEach(([mood, count]) => {
            const emoji = moodEmojis[mood] || '💚';
            const percentage = ((count / this.userMoods.length) * 100).toFixed(1);
            moodsDisplay += `${emoji} ${mood}: ${count} (${percentage}%)\n`;
        });

        moodsDisplay += `\n💡 You can:\n- Log a new mood\n- Ask me to analyze your mood patterns\n- Get song recommendations based on your mood`;

        return {
            success: true,
            message: moodsDisplay
        };
    }

    /**
     * Handle quick actions
     */
    async handleQuickAction(action) {
        const actionMap = {
            'analyze-mood': 'Analyze my mood patterns',
            'task-suggestions': 'What tasks should I focus on?',
            'song-recommendations': 'Recommend songs for my current mood',
            'note-summary': 'Summarize my recent notes'
        };

        const message = actionMap[action];
        if (message) {
            document.getElementById('chat-input').value = message;
            await this.handleSend();
        }
    }

    /**
     * Add message to chat
     */
    addMessage(content, sender) {
        // Add to UI
        this.addMessageToUI(content, sender);
        
        // Store message in memory
        this.messages.push({ content, sender, timestamp: new Date() });
        
        // Save to backend (async, don't wait)
        this.saveMessageToBackend(content, sender);
        
        // Save to localStorage as backup
        this.saveChatHistory();
    }

    /**
     * Add message to UI only (used for loading history)
     */
    addMessageToUI(content, sender) {
        const messagesContainer = document.getElementById('chat-messages');
        if (!messagesContainer) {
            console.warn('Messages container not found, skipping UI update');
            return;
        }
        
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender === 'user' ? 'user-message' : 'bot-message'}`;

        if (sender === 'user') {
            const userData = apiService.getCurrentUser();
            const initial = userData?.name?.charAt(0).toUpperCase() || 'U';
            
            messageDiv.innerHTML = `
                <div class="user-avatar">${initial}</div>
                <div class="message-content">
                    <p>${this.escapeHtml(content)}</p>
                </div>
            `;
        } else {
            messageDiv.innerHTML = `
                <div class="chatbot-avatar-small">
                    <i class="fas fa-robot"></i>
                </div>
                <div class="message-content">
                    ${this.formatBotMessage(content)}
                </div>
            `;
        }

        messagesContainer.appendChild(messageDiv);
        this.scrollToBottom();
    }

    /**
     * Format bot message with markdown-like styling
     */
    formatBotMessage(content) {
        // Convert markdown-style formatting to HTML
        let formatted = content
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\*(.*?)\*/g, '<em>$1</em>')
            .replace(/^- (.*$)/gim, '<li>$1</li>')
            .replace(/^(\d+)\. (.*$)/gim, '<li>$2</li>')
            .replace(/🎵 \*\*(.*?)\*\* by (.*?)$/gim, '<div class="song-recommendation"><div class="song-icon"><i class="fas fa-music"></i></div><div class="song-info"><div class="song-title">$1</div><div class="song-artist">$2</div></div></div>');

        // Wrap lists
        if (formatted.includes('<li>')) {
            formatted = formatted.replace(/(<li>.*<\/li>)/gis, '<ul>$1</ul>');
        }

        // Split into paragraphs
        formatted = formatted.split('\n').map(line => {
            line = line.trim();
            if (line && !line.startsWith('<')) {
                return `<p>${line}</p>`;
            }
            return line;
        }).join('');

        return formatted;
    }

    /**
     * Escape HTML to prevent XSS
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * Show typing indicator
     */
    showTypingIndicator() {
        const indicator = document.getElementById('typing-indicator');
        if (indicator) {
            indicator.classList.remove('hidden');
            indicator.style.display = 'flex';
            this.scrollToBottom();
        }
    }

    /**
     * Hide typing indicator
     */
    hideTypingIndicator() {
        const indicator = document.getElementById('typing-indicator');
        if (indicator) {
            indicator.classList.add('hidden');
            indicator.style.display = 'none';
        }
    }

    /**
     * Scroll to bottom of chat
     */
    scrollToBottom() {
        const messagesContainer = document.getElementById('chat-messages');
        setTimeout(() => {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }, 100);
    }

    /**
     * Start a new chat (clears UI but keeps history)
     */
    async newChat() {
        // Reset UI
        const messagesContainer = document.getElementById('chat-messages');
        if (messagesContainer) {
            messagesContainer.innerHTML = `
                <div class="welcome-message flex gap-2.5 sm:gap-3.5 animate-fade-in max-w-full">
                    <div class="chatbot-avatar w-9 h-9 sm:w-11 sm:h-11 min-w-[36px] sm:min-w-[44px] bg-gradient-to-br from-primary to-primary-dark rounded-xl flex items-center justify-center text-white text-lg sm:text-xl shadow-md">
                        <i class="fas fa-robot"></i>
                    </div>
                    <div class="message-content flex-1 min-w-0 bg-white px-4 sm:px-5 py-3 sm:py-4 rounded-2xl shadow-sm border border-gray-100 break-words">
                        <p class="text-sm sm:text-base mb-3 text-gray-800 leading-relaxed"><strong class="text-primary font-semibold">New chat started! 🤖</strong></p>
                        <p class="text-sm sm:text-base text-gray-700">How can I help you today?</p>
                    </div>
                </div>
            `;
        }
        
        // Clear AI conversation history (but keep backend history)
        groqService.clearHistory();
        
        console.log('New chat started');
    }

    /**
     * Clear chat
     */
    async clearChat() {
        if (confirm('Are you sure you want to clear the chat history? This will delete all messages from both your browser and the server.')) {
            // Clear history (both local and backend)
            await this.clearChatHistory();
            
            // Reset UI
            const messagesContainer = document.getElementById('chat-messages');
            messagesContainer.innerHTML = `
                <div class="welcome-message flex gap-2.5 sm:gap-3.5 animate-fade-in max-w-full">
                    <div class="chatbot-avatar w-9 h-9 sm:w-11 sm:h-11 min-w-[36px] sm:min-w-[44px] bg-gradient-to-br from-primary to-primary-dark rounded-xl flex items-center justify-center text-white text-lg sm:text-xl shadow-md">
                        <i class="fas fa-robot"></i>
                    </div>
                    <div class="message-content flex-1 min-w-0 bg-white px-4 sm:px-5 py-3 sm:py-4 rounded-2xl shadow-sm border border-gray-100 break-words">
                        <p class="text-sm sm:text-base mb-3 text-gray-800 leading-relaxed"><strong class="text-primary font-semibold">Chat cleared! 🤖</strong></p>
                        <p class="text-sm sm:text-base text-gray-700">How can I help you today?</p>
                    </div>
                </div>
            `;
            
            // Clear AI conversation history
            groqService.clearHistory();
            
            console.log('Chat cleared successfully');
        }
    }
}
