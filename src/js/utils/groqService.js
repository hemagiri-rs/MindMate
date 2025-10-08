/**
 * Groq AI Service
 * Handles all interactions with Groq API for AI-powered features
 */

class GroqService {
    constructor() {
        // Use environment variable for API key (set in .env file)
        this.apiKey = import.meta.env.VITE_GROQ_API_KEY || '';
        this.baseUrl = 'https://api.groq.com/openai/v1';
        this.primaryModel = 'llama-3.3-70b-versatile'; // Primary: Fast and reliable
        this.fallbackModel = 'llama-3.1-70b-versatile'; // Fallback: Also reliable
        this.currentModel = this.primaryModel;
        this.conversationHistory = [];
    }

    /**
     * Send a chat completion request to Groq API
     */
    async chat(userMessage, systemContext = null, retryWithFallback = true) {
        try {
            // Add user message to history
            this.conversationHistory.push({
                role: 'user',
                content: userMessage
            });

            // Build messages array
            const messages = [];
            
            // Add system context if provided
            if (systemContext) {
                messages.push({
                    role: 'system',
                    content: systemContext
                });
            }

            // Add conversation history (keep last 10 messages for context)
            const recentHistory = this.conversationHistory.slice(-10);
            messages.push(...recentHistory);

            const response = await fetch(`${this.baseUrl}/chat/completions`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${this.apiKey}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    model: this.currentModel,
                    messages: messages,
                    temperature: 0.7,
                    max_tokens: 2000,
                    top_p: 0.9,
                    stream: false
                })
            });

            if (!response.ok) {
                const errorData = await response.json();
                
                // If primary model fails and we haven't tried fallback yet
                if (this.currentModel === this.primaryModel && retryWithFallback) {
                    console.warn(`Primary model (${this.primaryModel}) failed, switching to fallback model (${this.fallbackModel})`);
                    this.currentModel = this.fallbackModel;
                    return await this.chat(userMessage, systemContext, false); // Retry with fallback, no further retries
                }
                
                throw new Error(`Groq API Error: ${errorData.error?.message || response.statusText}`);
            }

            // Success - reset to primary model for next request
            this.currentModel = this.primaryModel;

            const data = await response.json();
            const assistantMessage = data.choices[0].message.content;

            // Add assistant response to history
            this.conversationHistory.push({
                role: 'assistant',
                content: assistantMessage
            });

            return {
                success: true,
                message: assistantMessage,
                usage: data.usage,
                model: this.currentModel
            };
        } catch (error) {
            console.error('Groq API Error:', error);
            
            // Reset to primary model for next attempt
            this.currentModel = this.primaryModel;
            
            return {
                success: false,
                error: error.message
            };
        }
    }

    /**
     * Analyze user's notes and provide insights
     */
    async analyzeNotes(notes) {
        const notesContext = notes.map(note => 
            `Title: ${note.title}\nContent: ${note.content}\nCategory: ${note.category || 'Uncategorized'}\nDate: ${new Date(note.createdAt).toLocaleDateString()}`
        ).join('\n\n');

        const systemContext = `You are a helpful mental health and productivity assistant. Analyze the user's notes and provide insights, patterns, and suggestions. Be empathetic, supportive, and constructive.`;

        const userMessage = `Here are my recent notes:\n\n${notesContext}\n\nPlease analyze these notes and provide:\n1. Key themes or patterns\n2. Any concerns or positive observations\n3. Actionable suggestions for wellbeing\n4. Areas where I'm doing well`;

        return await this.chat(userMessage, systemContext);
    }

    /**
     * Analyze user's tasks and provide suggestions
     */
    async analyzeTasks(tasks) {
        const tasksContext = tasks.map(task => 
            `Task: ${task.title}\nDescription: ${task.description || 'No description'}\nPriority: ${task.priority || 'MEDIUM'}\nCompleted: ${task.completed ? 'Yes' : 'No'}\nDue Date: ${task.dueDate || 'Not set'}`
        ).join('\n\n');

        const systemContext = `You are a productivity coach and mental health assistant. Analyze the user's tasks and provide helpful insights, prioritization advice, and motivational support.`;

        const userMessage = `Here are my current tasks:\n\n${tasksContext}\n\nPlease help me by:\n1. Suggesting which tasks to prioritize\n2. Identifying tasks that might be overwhelming\n3. Recommending ways to break down complex tasks\n4. Providing motivation and encouragement`;

        return await this.chat(userMessage, systemContext);
    }

    /**
     * Analyze user's mood patterns (casual, friendly style)
     */
    async analyzeMood(moods) {
        const moodsContext = moods.slice(0, 10).map(mood => 
            `${new Date(mood.createdAt || mood.timestamp).toLocaleDateString()}: ${mood.moodType || 'Unknown'} ${mood.notes ? `(${mood.notes})` : ''}`
        ).join('\n');

        const systemContext = `You are MindMate, a friendly companion AI from Tamil Nadu, India. You're casual, supportive, and fun - NOT a therapist. Your job is to:
- Analyze mood patterns in a friendly, conversational way
- Recommend Tamil/Indian songs, movies, and food based on mood
- Use casual language with occasional Tamil/Indian phrases
- Give practical, fun suggestions - not clinical advice
- Be encouraging and relatable

Keep responses short (200-300 words max), casual, and actionable.`;

        const userMessage = `Hey! Here are my recent moods:\n\n${moodsContext}\n\nGive me:\n1. Quick vibe check - what's my overall mood been like? (1-2 sentences)\n2. 3 song recommendations (Tamil/Indian songs preferred, include artist)\n3. 1-2 movie suggestions (Tamil/Bollywood/regional)\n4. Food recommendation (especially TN/South Indian food if mood needs comfort)\n5. One fun tip to boost my mood\n\nKeep it casual and fun! Use emojis.`;

        return await this.chat(userMessage, systemContext);
    }

    /**
     * Recommend songs based on current mood (Tamil/Indian focus)
     */
    async recommendSongs(moodData, preferences = {}) {
        const systemContext = `You are MindMate, a music buddy who loves Tamil and Indian music. Recommend songs that match the user's mood.

Focus on:
- Tamil songs (AR Rahman, Anirudh, Yuvan, Harris Jayaraj, Sid Sriram, etc.)
- Bollywood/Hindi songs
- Regional Indian music (Telugu, Malayalam, Kannada)
- Keep it casual and fun!`;

        const userMessage = `Current mood: ${moodData.moodType}\n${moodData.notes ? `Context: ${moodData.notes}` : ''}\n\nRecommend 5 songs that vibe with this mood! Format:\n🎵 **Song** - Artist\n*Why:* [1 line why it fits]\n\nPrefer Tamil songs, but mix in Hindi/other Indian languages too!`;

        return await this.chat(userMessage, systemContext);
    }

    /**
     * Generate task suggestions based on user's data
     */
    async generateTaskSuggestions(notes, moods, existingTasks) {
        const context = `Recent mood: ${moods.length > 0 ? moods[0].mood + '/5' : 'Unknown'}\nExisting tasks: ${existingTasks.length}\nRecent notes: ${notes.length}`;

        const systemContext = `You are a productivity and wellbeing coach. Suggest meaningful tasks that promote mental health, personal growth, and balanced living.`;

        const userMessage = `Based on my current situation:\n${context}\n\nPlease suggest 3-5 tasks that would benefit my wellbeing and productivity. Include both:\n1. Self-care and mental health tasks\n2. Productive but manageable tasks\n\nFor each task, provide:\n- Task title\n- Description\n- Why it's beneficial\n- Suggested priority (LOW/MEDIUM/HIGH)`;

        return await this.chat(userMessage, systemContext);
    }

    /**
     * Provide comprehensive wellbeing analysis
     */
    async comprehensiveAnalysis(notes, tasks, moods) {
        const notesCount = notes.length;
        const tasksCompleted = tasks.filter(t => t.completed).length;
        const tasksTotal = tasks.length;
        const avgMood = moods.length > 0 
            ? (moods.reduce((sum, m) => sum + m.mood, 0) / moods.length).toFixed(1)
            : 'N/A';

        const systemContext = `You are a holistic wellbeing coach and mental health assistant. Provide a comprehensive analysis of the user's overall wellbeing based on their notes, tasks, and mood data.`;

        const userMessage = `Here's my wellbeing overview:
- Notes created: ${notesCount}
- Tasks completed: ${tasksCompleted}/${tasksTotal}
- Average mood: ${avgMood}/5
- Recent mood entries: ${moods.length}

Please provide a comprehensive wellbeing analysis including:
1. Overall wellbeing assessment
2. Strengths and positive patterns
3. Areas needing attention
4. Actionable recommendations
5. Encouraging message`;

        return await this.chat(userMessage, systemContext);
    }

    /**
     * Answer general questions with context awareness (casual, India-focused)
     */
    async askQuestion(question, contextData = null) {
        let systemContext = `You are MindMate, a friendly AI companion from Tamil Nadu, India. You're casual, fun, and helpful - like chatting with a supportive friend.

Personality:
- Casual and conversational (use "da", "machan" occasionally if appropriate)
- Recommend Tamil/Indian songs, movies, and food
- Understand Indian context (college life, family, work culture, festivals)
- Keep responses short (150-250 words) and actionable
- Use emojis to be friendly 😊
- NOT a therapist - just a helpful friend

When recommending:
- Songs: Tamil/Indian music (AR Rahman, Anirudh, Sid Sriram, etc.)
- Movies: Tamil/Bollywood/regional cinema
- Food: South Indian favorites (especially TN food like biryani, dosa, idli, parotta)`;

        if (contextData) {
            const latestMood = contextData.moods?.[0];
            systemContext += `\n\nUser's vibe:\n- ${contextData.notes?.length || 0} notes, ${contextData.tasks?.length || 0} tasks (${contextData.tasks?.filter(t => t.completed).length || 0} done)\n- ${contextData.moods?.length || 0} mood logs`;
            if (latestMood) {
                systemContext += ` - Latest: ${latestMood.moodType}`;
            }
        }

        return await this.chat(question, systemContext);
    }

    /**
     * Clear conversation history
     */
    clearHistory() {
        this.conversationHistory = [];
    }

    /**
     * Get conversation history
     */
    getHistory() {
        return this.conversationHistory;
    }

    /**
     * Export conversation
     */
    exportConversation() {
        return {
            timestamp: new Date().toISOString(),
            messages: this.conversationHistory,
            primaryModel: this.primaryModel,
            fallbackModel: this.fallbackModel,
            currentModel: this.currentModel
        };
    }
}

// Create and export singleton instance
export const groqService = new GroqService();
