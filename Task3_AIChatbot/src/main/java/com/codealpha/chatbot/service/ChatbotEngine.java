package com.codealpha.chatbot.service;

import com.codealpha.chatbot.model.*;
import com.codealpha.chatbot.nlp.ContextManager;
import com.codealpha.chatbot.nlp.IntentClassifier;
import com.codealpha.chatbot.nlp.RelevanceScorer;
import com.codealpha.chatbot.nlp.TextProcessor;

import java.util.*;

/**
 * Intelligent NLP Chatbot Response Engine.
 * Coordinates text processing, intent recognition, context management,
 * relevance scoring, clarification generation, and fallback recovery.
 */
public class ChatbotEngine {

    private final TextProcessor textProcessor;
    private final RelevanceScorer relevanceScorer;
    private final ContextManager contextManager;
    private final IntentClassifier intentClassifier;
    private final KnowledgeBaseService knowledgeBaseService;
    private final SettingsService settingsService;

    public ChatbotEngine(KnowledgeBaseService knowledgeBaseService, SettingsService settingsService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.settingsService = settingsService;
        this.textProcessor = new TextProcessor();
        this.relevanceScorer = new RelevanceScorer(this.textProcessor);
        this.contextManager = new ContextManager();
        this.intentClassifier = new IntentClassifier(this.textProcessor, this.relevanceScorer, this.contextManager);
    }

    public ContextManager getContextManager() {
        return contextManager;
    }

    /**
     * Generates a contextual assistant response for a given user query.
     */
    public ChatMessage processQuery(String conversationId, String query) {
        long startTime = System.currentTimeMillis();

        if (query == null || query.trim().isBlank()) {
            long latency = Math.max(1, System.currentTimeMillis() - startTime);
            ChatMessage emptyMsg = ChatMessage.assistant(conversationId,
                    "Please type a question or choose from the suggested topics below.",
                    IntentType.GENERAL_HELP.getDisplayName(), 1.0, "General", latency);
            emptyMsg.setSuggestedQuestions(getDefaultSuggestions());
            return emptyMsg;
        }

        double threshold = settingsService.getSettings().getConfidenceThreshold();
        List<KnowledgeEntry> kb = knowledgeBaseService.getAllEntries();

        IntentMatchResult matchResult = intentClassifier.classify(query, kb, threshold);

        String responseContent;
        String intentDisplay = matchResult.getIntent().getDisplayName();
        String category = matchResult.getMatchedCategory();
        double confidence = matchResult.getConfidence();
        boolean isFollowUp = matchResult.isFollowUp();
        List<String> suggestions = new ArrayList<>();

        if (matchResult.isFollowUp()) {
            // Handled as contextual follow-up
            KnowledgeEntry entry = matchResult.getMatchedEntry();
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
                // Pre-configured intent response
                responseContent = getSystemIntentResponse(matchResult.getIntent());
                contextManager.updateContext(matchResult.getIntent(), null, matchResult.getIntent().getCategory());
                suggestions = getSuggestionsForIntent(matchResult.getIntent());
            }

        } else if (matchResult.isMediumConfidence(threshold)) {
            // Medium confidence clarification request
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
            // Low confidence fallback response
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

    private String getSystemIntentResponse(IntentType intent) {
        return switch (intent) {
            case GREETINGS -> "Hello! I am RAJ AI, your intelligent Java assistant. What Java or OOP topic can I help you master today?";
            case INTRODUCTION -> "I am RAJ AI, an intelligent desktop chatbot developed for Task 3 of the CodeAlpha Java Programming Internship.\n\n"
                    + "I specialize in Core Java, Object-Oriented Principles, Collections, Exception Handling, and File I/O.\n"
                    + "Ask me a question or try a suggested card!";
            case GENERAL_HELP -> "Here is how you can use RAJ AI:\n\n"
                    + "1. **Ask Java Questions**: Type concepts like 'Explain OOP', 'What is Polymorphism', 'How does try-catch work?'\n"
                    + "2. **Ask Follow-ups**: Say 'Give me an example' or 'Explain it line by line'\n"
                    + "3. **Analytics Dashboard**: Visit the Dashboard to see your conversation statistics and intent frequency\n"
                    + "4. **Knowledge Base**: Add and manage custom Q&A items in real-time\n"
                    + "5. **Settings**: Adjust font size and switch between Dark and Light themes.";
            case THANKS -> "You are very welcome! Let me know whenever you'd like to explore more Java concepts.";
            case GOODBYE -> "Goodbye! Happy coding and all the best with your Java development journey!";
            default -> "I am RAJ AI, ready to help you with Java and software development concepts. Please ask away!";
        };
    }

    private String getFallbackResponse(String query) {
        return "I couldn't find a high-confidence match for: *\"" + query + "\"*.\n\n"
                + "As a rule-based AI programming assistant, I can explain:\n"
                + "• **Object-Oriented Programming**: Classes, Objects, Inheritance, Polymorphism, Encapsulation, Abstraction\n"
                + "• **Java Fundamentals**: Variables, Data Types, Control Flow, Arrays, ArrayLists\n"
                + "• **Robustness & Architecture**: Exception Handling, Collections Framework, File I/O\n"
                + "• **Internship Guidance**: CodeAlpha Tasks, Git & Maven setup\n\n"
                + "Try rephrasing your question or click one of the suggested topics below!";
    }

    public List<String> getDefaultSuggestions() {
        return List.of(
                "Explain OOP and its 4 pillars",
                "What is Polymorphism in Java?",
                "Explain Java classes and objects",
                "How does Exception Handling work?",
                "What is the Collections Framework?",
                "How do I succeed in CodeAlpha Internship?"
        );
    }

    private List<String> getSuggestionsForIntent(IntentType intent) {
        return switch (intent) {
            case GREETINGS, INTRODUCTION -> List.of(
                    "Explain OOP and its 4 pillars",
                    "Explain Java classes and objects",
                    "What is Polymorphism in Java?"
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
            default -> getDefaultSuggestions();
        };
    }
}
