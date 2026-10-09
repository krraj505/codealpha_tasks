package com.codealpha.chatbot.service;

import com.codealpha.chatbot.model.*;
import com.codealpha.chatbot.nlp.ContextManager;
import com.codealpha.chatbot.nlp.IntentClassifier;
import com.codealpha.chatbot.nlp.RelevanceScorer;
import com.codealpha.chatbot.nlp.TextProcessor;

import java.util.*;

/**
 * Intelligent Hybrid Chatbot Response Engine.
 * Coordinates text processing, intent recognition, context management,
 * relevance scoring, local knowledge base matching, and general-purpose LLM integration.
 */
public class ChatbotEngine {

    private final TextProcessor textProcessor;
    private final RelevanceScorer relevanceScorer;
    private final ContextManager contextManager;
    private final IntentClassifier intentClassifier;
    private final KnowledgeBaseService knowledgeBaseService;
    private final SettingsService settingsService;
    private final LlmService llmService;

    public ChatbotEngine(KnowledgeBaseService knowledgeBaseService, SettingsService settingsService) {
        this(knowledgeBaseService, settingsService, new LlmService(settingsService));
    }

    public ChatbotEngine(KnowledgeBaseService knowledgeBaseService, SettingsService settingsService, LlmService llmService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.settingsService = settingsService;
        this.llmService = llmService;
        this.textProcessor = new TextProcessor();
        this.relevanceScorer = new RelevanceScorer(this.textProcessor);
        this.contextManager = new ContextManager();
        this.intentClassifier = new IntentClassifier(this.textProcessor, this.relevanceScorer, this.contextManager);
    }

    public ContextManager getContextManager() {
        return contextManager;
    }

    public LlmService getLlmService() {
        return llmService;
    }

    /**
     * Generates a contextual assistant response for a given user query.
     */
    public ChatMessage processQuery(String conversationId, String query) {
        return processQuery(conversationId, query, Collections.emptyList());
    }

    /**
     * Generates a contextual assistant response for a given user query with multi-turn history.
     */
    public ChatMessage processQuery(String conversationId, String query, List<ChatMessage> history) {
        long startTime = System.currentTimeMillis();

        if (query == null || query.trim().isBlank()) {
            long latency = Math.max(1, System.currentTimeMillis() - startTime);
            ChatMessage emptyMsg = ChatMessage.assistant(conversationId,
                    "Please type a question or choose from the suggested topics below.",
                    IntentType.GENERAL_HELP.getDisplayName(), 1.0, "General", latency);
            emptyMsg.setSuggestedQuestions(getDefaultSuggestions());
            return emptyMsg;
        }

        UserSettings settings = settingsService.getSettings();
        double threshold = settings.getConfidenceThreshold();
        boolean llmEnabled = settings.isEnableLlm();
        boolean llmConfigured = llmService != null && llmService.isConfigured() && llmEnabled;
        boolean preferOnline = settings.isPreferOnlineAi();

        List<KnowledgeEntry> kb = knowledgeBaseService.getAllEntries();
        IntentMatchResult matchResult = intentClassifier.classify(query, kb, threshold);

        String responseContent;
        String intentDisplay = matchResult.getIntent().getDisplayName();
        String category = matchResult.getMatchedCategory();
        double confidence = matchResult.getConfidence();
        boolean isFollowUp = matchResult.isFollowUp();
        List<String> suggestions = new ArrayList<>();

        // 1. If user explicitly configured "Prefer Online AI" and it's not a greeting/goodbye, route directly to LLM
        if (preferOnline && llmConfigured && !isSystemIntent(matchResult.getIntent())) {
            return generateLlmChatMessage(conversationId, query, history, startTime);
        }

        // 2. Check for Contextual Follow-Up Questions on local Knowledge Base entry
        if (matchResult.isFollowUp()) {
            KnowledgeEntry entry = matchResult.getMatchedEntry();
            if (entry != null) {
                String followUpType = matchResult.getFollowUpType();

                if ("example".equalsIgnoreCase(followUpType)) {
                    if (entry.getFollowUpExample() != null && !entry.getFollowUpExample().isBlank()) {
                        responseContent = "Here is a code example for **" + entry.getQuestion() + "**:\n\n```java\n"
                                + entry.getFollowUpExample() + "\n```";
                        suggestions.add("Explain it line by line");
                        suggestions.add("What are the advantages of this?");
                    } else {
                        responseContent = "Here is the implementation code for **" + entry.getQuestion() + "**:\n\n"
                                + entry.getAnswer();
                    }
                } else if ("line_by_line".equalsIgnoreCase(followUpType)) {
                    if (entry.getFollowUpLineByLine() != null && !entry.getFollowUpLineByLine().isBlank()) {
                        responseContent = "### Line-by-Line Breakdown: " + entry.getQuestion() + "\n\n"
                                + entry.getFollowUpLineByLine();
                        suggestions.add("Give me another example");
                        suggestions.add("What exceptions can occur?");
                    } else {
                        responseContent = "Let's review the key elements of **" + entry.getQuestion() + "** step by step:\n\n"
                                + entry.getAnswer();
                    }
                } else {
                    responseContent = "### Deep Dive: " + entry.getQuestion() + "\n\n" + entry.getAnswer();
                    suggestions.add("Give me an example");
                    suggestions.add("Explain it line by line");
                }

                contextManager.updateContext(matchResult.getIntent(), entry, entry.getCategory());
                knowledgeBaseService.recordUsage(entry.getId());
            } else if (llmConfigured) {
                // If follow-up in chat without local KB entry, delegate to LLM with full conversation history
                return generateLlmChatMessage(conversationId, query, history, startTime);
            } else {
                responseContent = getFallbackResponse(query);
                suggestions = getDefaultSuggestions();
            }

        } else if (matchResult.isHighConfidence(threshold)) {
            // High confidence direct answer
            KnowledgeEntry entry = matchResult.getMatchedEntry();
            if (entry != null) {
                responseContent = entry.getAnswer();
                contextManager.updateContext(matchResult.getIntent(), entry, entry.getCategory());
                knowledgeBaseService.recordUsage(entry.getId());

                if (entry.getFollowUpExample() != null && !entry.getFollowUpExample().isBlank()) {
                    suggestions.add("Give me an example");
                }
                suggestions.add("Explain it line by line");
                suggestions.add("Tell me more");
            } else {
                // Pre-configured system intent response (greetings, intro, help, thanks, goodbye)
                responseContent = getSystemIntentResponse(matchResult.getIntent());
                contextManager.updateContext(matchResult.getIntent(), null, matchResult.getIntent().getCategory());
                suggestions = getSuggestionsForIntent(matchResult.getIntent());
            }

        } else if (llmConfigured) {
            // Unmapped, medium, or general knowledge question routed to General-Purpose LLM
            return generateLlmChatMessage(conversationId, query, history, startTime);

        } else if (matchResult.isMediumConfidence(threshold)) {
            // Medium confidence clarification request (in offline mode)
            KnowledgeEntry entry = matchResult.getMatchedEntry();
            if (entry != null) {
                responseContent = "I noticed you asked about something related to **" + entry.getQuestion() + "** ("
                        + entry.getCategory() + ").\n\n"
                        + entry.getAnswer() + "\n\n"
                        + "*Was this what you were looking for? Feel free to ask a follow-up or explore other topics.*";
                contextManager.updateContext(matchResult.getIntent(), entry, entry.getCategory());
                knowledgeBaseService.recordUsage(entry.getId());
                suggestions.add("Give me an example");
                suggestions.add("Show another topic");
            } else {
                responseContent = getFallbackResponse(query);
                suggestions = getDefaultSuggestions();
            }

        } else {
            // Low confidence fallback response (offline mode)
            responseContent = getFallbackResponse(query);
            intentDisplay = IntentType.UNKNOWN.getDisplayName();
            category = "Fallback";
            suggestions = getDefaultSuggestions();
        }

        long latency = Math.max(2, System.currentTimeMillis() - startTime);

        ChatMessage responseMessage = ChatMessage.assistant(
                conversationId, responseContent, intentDisplay, confidence, category, latency);
        responseMessage.setFollowUp(isFollowUp);
        responseMessage.setSuggestedQuestions(suggestions);

        return responseMessage;
    }

    private ChatMessage generateLlmChatMessage(String conversationId, String query, List<ChatMessage> history, long startTime) {
        LlmService.LlmResponse llmResp = llmService.generateResponse(history, query, null);
        long latency = Math.max(llmResp.getLatencyMs(), System.currentTimeMillis() - startTime);

        if (llmResp.isSuccess()) {
            String model = llmResp.getModelUsed();
            ChatMessage msg = ChatMessage.assistant(
                    conversationId,
                    llmResp.getContent(),
                    "General AI (" + model + ")",
                    0.96,
                    "General AI",
                    latency
            );
            msg.setSuggestedQuestions(List.of(
                    "Can you provide a code example for this?",
                    "Explain this step by step",
                    "What are the best practices or pitfalls?"
            ));
            contextManager.updateContext(IntentType.UNKNOWN, null, "General AI");
            return msg;
        } else {
            // LLM failed due to network, quota, or authentication
            String errorMsg = "### ⚠️ AI Model Communication Notice\n\n"
                    + llmResp.getErrorMessage() + "\n\n"
                    + "---\n\n"
                    + getFallbackResponse(query);

            ChatMessage msg = ChatMessage.assistant(
                    conversationId,
                    errorMsg,
                    "AI Error / Offline Fallback",
                    0.30,
                    "Fallback",
                    latency
            );
            msg.setSuggestedQuestions(getDefaultSuggestions());
            return msg;
        }
    }

    private boolean isSystemIntent(IntentType intent) {
        return intent == IntentType.GREETINGS
                || intent == IntentType.INTRODUCTION
                || intent == IntentType.GENERAL_HELP
                || intent == IntentType.THANKS
                || intent == IntentType.GOODBYE;
    }

    private String getSystemIntentResponse(IntentType intent) {
        return switch (intent) {
            case GREETINGS -> "Hello! I am RAJ AI, your intelligent programming and general-purpose text assistant. What topic can I help you explore today?";
            case INTRODUCTION -> "I am RAJ AI, an intelligent desktop assistant developed for Task 3 of the CodeAlpha Java Programming Internship.\n\n"
                    + "I specialize in Core Java, OOP Principles, Data Structures, and general-purpose software queries across multiple languages.\n"
                    + "Ask me any question or pick a suggestion from the cards below!";
            case GENERAL_HELP -> "Here is how you can use RAJ AI:\n\n"
                    + "1. **General-Purpose AI**: Ask questions about Java, Python, C++, Web, Machine Learning, Science, Math, or Writing.\n"
                    + "2. **Ask Follow-ups**: Say 'Give me an example', 'Explain it line by line', or continue the conversation naturally.\n"
                    + "3. **Analytics Dashboard**: Visit the Dashboard to see your conversation statistics and intent frequency.\n"
                    + "4. **Knowledge Base**: Add and manage custom Q&A items in real-time.\n"
                    + "5. **Settings & AI Configuration**: Configure OpenAI/Groq/Ollama models, API keys, fonts, and themes.";
            case THANKS -> "You are very welcome! Let me know whenever you'd like to explore more concepts or write more code.";
            case GOODBYE -> "Goodbye! Happy coding and best of luck with your software development journey!";
            default -> "I am RAJ AI, ready to help you with software engineering, learning, and general problem solving. Please ask away!";
        };
    }

    private String getFallbackResponse(String query) {
        return "I couldn't find a high-confidence match for: *\"" + query + "\"*.\n\n"
                + "### 💡 Activate General-Purpose AI (ChatGPT Mode)\n"
                + "RAJ AI can answer questions across **all domains** (Python, C++, ML, science, mathematics, resume writing, general QA) when connected to an AI model!\n\n"
                + "**How to enable Online AI:**\n"
                + "1. Set the `AI_API_KEY` or `OPENAI_API_KEY` environment variable in your terminal/system:\n"
                + "   ```bash\n"
                + "   setx AI_API_KEY \"your_api_key_here\"\n"
                + "   ```\n"
                + "2. Or enter your key directly in the **Settings** tab under *AI Model & Provider Configuration*.\n"
                + "3. Any OpenAI-compatible provider is supported (OpenAI, Groq, OpenRouter, Ollama).\n\n"
                + "### 📚 Offline Knowledge Base Available Topics:\n"
                + "• **Object-Oriented Programming**: Classes, Objects, Inheritance, Polymorphism, Encapsulation, Abstraction\n"
                + "• **Java Core**: Strings & SCP Immutability, Variables, Data Types, Control Flow, Arrays, ArrayLists\n"
                + "• **Robustness & Architecture**: Exception Handling, Collections Framework, File I/O, REST APIs\n"
                + "• **Internship Guidance**: CodeAlpha Tasks, Git & Maven pom.xml\n\n"
                + "Try asking about any of these topics or choose a suggestion below!";
    }

    public List<String> getDefaultSuggestions() {
        return List.of(
                "Explain OOP and its 4 pillars",
                "What is String in Java and why is it immutable?",
                "What is Polymorphism in Java?",
                "Compare Python and Java for Machine Learning",
                "How does Exception Handling work?",
                "Explain Java Collections Framework"
        );
    }

    private List<String> getSuggestionsForIntent(IntentType intent) {
        return switch (intent) {
            case GREETINGS, INTRODUCTION -> List.of(
                    "Explain OOP and its 4 pillars",
                    "What is String in Java and why is it immutable?",
                    "Compare Python and Java for Machine Learning"
            );
            case OOP, CLASSES_OBJECTS -> List.of(
                    "Give me an example",
                    "Explain Inheritance in Java",
                    "Explain Polymorphism in Java"
            );
            case INHERITANCE, POLYMORPHISM, ENCAPSULATION, ABSTRACTION -> List.of(
                    "Give me an example",
                    "Explain it line by line",
                    "What is the difference between abstract class and interface?"
            );
            case VARIABLES_DATA_TYPES -> List.of(
                    "What is String in Java and why is it immutable?",
                    "Give me an example",
                    "Explain it line by line"
            );
            default -> getDefaultSuggestions();
        };
    }
}
