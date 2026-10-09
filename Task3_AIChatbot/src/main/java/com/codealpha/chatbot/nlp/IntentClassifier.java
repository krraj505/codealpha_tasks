package com.codealpha.chatbot.nlp;

import com.codealpha.chatbot.model.IntentMatchResult;
import com.codealpha.chatbot.model.IntentType;
import com.codealpha.chatbot.model.KnowledgeEntry;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Intelligent NLP-based Intent Classifier.
 * Detects conversational and domain-specific intents using rule-based pattern matching,
 * keyword weighting, phrase patterns, and contextual cues.
 */
public class IntentClassifier {

    private final TextProcessor textProcessor;
    private final RelevanceScorer relevanceScorer;
    private final ContextManager contextManager;

    private final Map<IntentType, List<Pattern>> intentPatterns = new EnumMap<>(IntentType.class);
    private final Map<IntentType, List<String>> intentKeywords = new EnumMap<>(IntentType.class);

    public IntentClassifier(TextProcessor textProcessor, RelevanceScorer relevanceScorer, ContextManager contextManager) {
        this.textProcessor = textProcessor != null ? textProcessor : new TextProcessor();
        this.relevanceScorer = relevanceScorer != null ? relevanceScorer : new RelevanceScorer(this.textProcessor);
        this.contextManager = contextManager != null ? contextManager : new ContextManager();
        initPatterns();
        initKeywords();
    }

    private void initPatterns() {
        // Greetings
        addPattern(IntentType.GREETINGS,
                "^(hi|hello|hey|hola|greetings|good (morning|afternoon|evening|day))\\b.*");

        // Introduction
        addPattern(IntentType.INTRODUCTION,
                ".*\\b(who are you|what is your name|what can you do|introduce yourself|tell me about yourself|your purpose)\\b.*");

        // General Help
        addPattern(IntentType.GENERAL_HELP,
                "^(help|commands|how to use|features|what do you know|options|instructions|guide me)\\b.*");

        // Thanks
        addPattern(IntentType.THANKS,
                ".*\\b(thank(s| you)?|appreciate (it|your help)|awesome|great job|helpful)\\b.*");

        // Goodbye
        addPattern(IntentType.GOODBYE,
                ".*\\b(bye|goodbye|see you|farewell|take care|exit|quit|catch you later)\\b.*");

        // Java Programming
        addPattern(IntentType.JAVA_PROGRAMMING,
                ".*\\b(what is java|features of java|jvm|jdk|jre|bytecode|platform independent|write once run anywhere|java edition)\\b.*");

        // OOP
        addPattern(IntentType.OOP,
                ".*\\b(what is oop|object oriented programming|pillars of oop|oops concepts|oop principles)\\b.*");

        // Variables and Data Types
        addPattern(IntentType.VARIABLES_DATA_TYPES,
                ".*\\b(variables?|data ?types?|primitive types?|casting|type casting|int|double|boolean|char|float)\\b.*");

        // Arrays and ArrayLists
        addPattern(IntentType.ARRAYS_ARRAYLISTS,
                ".*\\b(arrays?|arraylist|difference between array and arraylist|dynamic array|multi-dimensional array)\\b.*");

        // Loops and Conditions
        addPattern(IntentType.LOOPS_CONDITIONS,
                ".*\\b(loops?|for loop|while loop|do while|if else|switch case|conditions?|break|continue)\\b.*");

        // Classes and Objects
        addPattern(IntentType.CLASSES_OBJECTS,
                ".*\\b(what is (a )?(class|object)|constructors?|instances?|instantiation|blueprint|default constructor)\\b.*");

        // Inheritance
        addPattern(IntentType.INHERITANCE,
                ".*\\b(inheritance|inherit|subclass|superclass|extends keyword|single inheritance|multiple inheritance|is-a relationship)\\b.*");

        // Polymorphism
        addPattern(IntentType.POLYMORPHISM,
                ".*\\b(polymorphism|polymorphic|method overloading|method overriding|runtime polymorphism|compile-time polymorphism|dynamic binding)\\b.*");

        // Encapsulation
        addPattern(IntentType.ENCAPSULATION,
                ".*\\b(encapsulation|data hiding|getters? and setters?|private variables?|access modifiers?|protect data)\\b.*");

        // Abstraction
        addPattern(IntentType.ABSTRACTION,
                ".*\\b(abstraction|abstract class(es)?|interfaces?|abstract methods?|difference between abstract class and interface)\\b.*");

        // Exception Handling
        addPattern(IntentType.EXCEPTION_HANDLING,
                ".*\\b(exceptions?|try catch|try-catch-finally|throw vs throws|checked vs unchecked|nullpointerexception|runtime exception)\\b.*");

        // Collections
        addPattern(IntentType.COLLECTIONS,
                ".*\\b(collections? framework|list|set|map|hashmap|hashset|arraylist vs linkedlist|queue|iterator|generics)\\b.*");

        // File Handling
        addPattern(IntentType.FILE_HANDLING,
                ".*\\b(file handling|files?|filereader|filewriter|bufferedreader|read file|write file|scanner file|java io|nio)\\b.*");

        // Study and College FAQs
        addPattern(IntentType.STUDY_COLLEGE_FAQS,
                ".*\\b(codealpha|internship|viva|exam preparation|syllabus|assignment|college questions|interview prep|project submission)\\b.*");

        // Technology FAQs
        addPattern(IntentType.TECHNOLOGY_FAQS,
                ".*\\b(git|github|ide|maven|intellij|vs code|eclipse|build tool|pom.xml|version control|clean code|software architecture)\\b.*");
    }

    private void addPattern(IntentType type, String regex) {
        intentPatterns.computeIfAbsent(type, k -> new ArrayList<>())
                .add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    }

    private void initKeywords() {
        setKeywords(IntentType.GREETINGS, "hi", "hello", "hey", "greetings", "morning", "evening");
        setKeywords(IntentType.INTRODUCTION, "who", "are", "you", "name", "bot", "assistant", "introduce", "purpose");
        setKeywords(IntentType.GENERAL_HELP, "help", "guide", "menu", "commands", "features", "options", "assist");
        setKeywords(IntentType.THANKS, "thank", "thanks", "helpful", "appreciate", "great", "awesome");
        setKeywords(IntentType.GOODBYE, "bye", "goodbye", "farewell", "exit", "quit", "later");
        setKeywords(IntentType.JAVA_PROGRAMMING, "java", "jvm", "jdk", "jre", "bytecode", "platform", "independent");
        setKeywords(IntentType.OOP, "oop", "oops", "object", "oriented", "pillars", "principles");
        setKeywords(IntentType.VARIABLES_DATA_TYPES, "variable", "datatype", "type", "primitive", "casting", "scope");
        setKeywords(IntentType.ARRAYS_ARRAYLISTS, "array", "arrays", "arraylist", "elements", "fixed", "dynamic");
        setKeywords(IntentType.LOOPS_CONDITIONS, "loop", "loops", "for", "while", "condition", "switch", "if", "else");
        setKeywords(IntentType.CLASSES_OBJECTS, "class", "classes", "object", "objects", "constructor", "blueprint");
        setKeywords(IntentType.INHERITANCE, "inheritance", "inherit", "extends", "super", "subclass", "parent");
        setKeywords(IntentType.POLYMORPHISM, "polymorphism", "overload", "overloading", "override", "overriding");
        setKeywords(IntentType.ENCAPSULATION, "encapsulation", "getter", "setter", "private", "hiding", "modifier");
        setKeywords(IntentType.ABSTRACTION, "abstraction", "abstract", "interface", "interfaces");
        setKeywords(IntentType.EXCEPTION_HANDLING, "exception", "exceptions", "try", "catch", "finally", "throw", "throws");
        setKeywords(IntentType.COLLECTIONS, "collection", "collections", "list", "set", "map", "hashmap", "queue");
        setKeywords(IntentType.FILE_HANDLING, "file", "files", "io", "reader", "writer", "bufferedreader", "stream");
        setKeywords(IntentType.STUDY_COLLEGE_FAQS, "codealpha", "internship", "college", "viva", "exam", "task");
        setKeywords(IntentType.TECHNOLOGY_FAQS, "git", "github", "maven", "ide", "intellij", "vscode", "software");
    }

    private void setKeywords(IntentType type, String... kws) {
        intentKeywords.put(type, Arrays.asList(kws));
    }

    /**
     * Classifies a user query against known patterns, knowledge base entries, and multi-turn context.
     */
    public IntentMatchResult classify(String query, List<KnowledgeEntry> knowledgeBase, double threshold) {
        if (query == null || query.isBlank()) {
            return IntentMatchResult.fallback("Empty message");
        }

        String norm = textProcessor.normalize(query);

        // 1. Check for Contextual Follow-Up Questions (e.g. "give me an example", "explain it line by line")
        TextProcessor.FollowUpType followUp = textProcessor.detectFollowUpType(query);
        if (followUp != TextProcessor.FollowUpType.NONE && contextManager.hasActiveContext()) {
            KnowledgeEntry contextEntry = contextManager.getLastKnowledgeEntry();
            IntentType contextIntent = contextManager.getLastIntent();
            String typeStr = followUp == TextProcessor.FollowUpType.EXAMPLE ? "example" :
                    followUp == TextProcessor.FollowUpType.LINE_BY_LINE ? "line_by_line" : "elaboration";

            List<String> kw = new ArrayList<>();
            kw.add("follow_up");
            kw.add(typeStr);
            kw.add(contextEntry.getCategory());

            return new IntentMatchResult(contextIntent, 0.95, contextEntry.getCategory(), kw, true, contextEntry, typeStr);
        }

        // 2. Check Regex Pattern Rules for high-confidence direct intent hits
        for (Map.Entry<IntentType, List<Pattern>> entry : intentPatterns.entrySet()) {
            for (Pattern p : entry.getValue()) {
                if (p.matcher(norm).matches()) {
                    List<String> matchedKw = new ArrayList<>();
                    matchedKw.add(p.pattern());
                    KnowledgeEntry bestMatch = findBestKnowledgeEntry(query, knowledgeBase, entry.getKey());
                    return new IntentMatchResult(entry.getKey(), 0.90, entry.getKey().getCategory(), matchedKw, false, bestMatch, null);
                }
            }
        }

        // 3. Search Knowledge Base using Relevance Scoring
        KnowledgeEntry bestEntry = null;
        double bestScore = 0.0;
        if (knowledgeBase != null) {
            for (KnowledgeEntry ke : knowledgeBase) {
                double score = relevanceScorer.scoreEntry(query, ke);
                if (score > bestScore) {
                    bestScore = score;
                    bestEntry = ke;
                }
            }
        }

        if (bestEntry != null && bestScore >= threshold) {
            IntentType mappedIntent = mapCategoryToIntent(bestEntry.getCategory());
            List<String> kw = textProcessor.extractKeywords(query);
            return new IntentMatchResult(mappedIntent, bestScore, bestEntry.getCategory(), kw, false, bestEntry, null);
        }

        // 4. Keyword Frequency Scoring across Intent categories
        List<String> queryTokens = textProcessor.extractKeywords(query);
        IntentType topIntent = IntentType.UNKNOWN;
        double topIntentScore = 0.0;

        for (Map.Entry<IntentType, List<String>> entry : intentKeywords.entrySet()) {
            int hits = 0;
            for (String token : queryTokens) {
                if (entry.getValue().contains(token)) {
                    hits++;
                }
            }
            if (hits > 0) {
                double score = (double) hits / Math.max(1, queryTokens.size()) * 0.75;
                if (score > topIntentScore) {
                    topIntentScore = score;
                    topIntent = entry.getKey();
                }
            }
        }

        if (topIntent != IntentType.UNKNOWN && topIntentScore >= threshold) {
            KnowledgeEntry relatedEntry = findBestKnowledgeEntry(query, knowledgeBase, topIntent);
            return new IntentMatchResult(topIntent, topIntentScore, topIntent.getCategory(), queryTokens, false, relatedEntry, null);
        }

        // Medium confidence candidate for clarification, or fallback
        if (bestEntry != null && bestScore >= (threshold * 0.65)) {
            IntentType mappedIntent = mapCategoryToIntent(bestEntry.getCategory());
            return new IntentMatchResult(mappedIntent, bestScore, bestEntry.getCategory(), queryTokens, false, bestEntry, null);
        }

        return IntentMatchResult.fallback("No matching intent or knowledge base entry with sufficient confidence");
    }

    private KnowledgeEntry findBestKnowledgeEntry(String query, List<KnowledgeEntry> kb, IntentType intent) {
        if (kb == null || kb.isEmpty()) return null;
        KnowledgeEntry best = null;
        double bestScore = -1.0;

        for (KnowledgeEntry entry : kb) {
            double score = relevanceScorer.scoreEntry(query, entry);
            if (intentMatchesCategory(intent, entry.getCategory())) {
                score += 0.20;
            }
            if (score > bestScore) {
                bestScore = score;
                best = entry;
            }
        }
        return (bestScore > 0.25) ? best : null;
    }

    private boolean intentMatchesCategory(IntentType intent, String category) {
        if (category == null || intent == null) return false;
        String catLower = category.toLowerCase();
        String intentLower = intent.getDisplayName().toLowerCase();
        String intentCat = intent.getCategory().toLowerCase();
        return catLower.contains(intentLower) || intentLower.contains(catLower)
                || catLower.contains(intentCat) || intentCat.contains(catLower);
    }

    private IntentType mapCategoryToIntent(String category) {
        if (category == null) return IntentType.JAVA_PROGRAMMING;
        for (IntentType type : IntentType.values()) {
            if (type.getDisplayName().equalsIgnoreCase(category) || type.getCategory().equalsIgnoreCase(category)) {
                return type;
            }
        }
        return IntentType.JAVA_PROGRAMMING;
    }
}
