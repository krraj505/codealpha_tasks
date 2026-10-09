package com.codealpha.chatbot.nlp;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Natural Language Processing text preprocessing utility.
 * Handles whitespace normalization, tokenization, stop-word removal with
 * preservation of programming terminology, and basic stemming.
 */
public class TextProcessor {

    // Words that carry little semantic value in general chat queries
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "is", "are", "was", "were", "be", "been",
            "being", "have", "has", "had", "do", "does", "did", "to", "at", "by", "for",
            "with", "about", "against", "between", "into", "through", "during", "before",
            "after", "above", "below", "from", "up", "down", "in", "out", "on", "off", "over",
            "under", "again", "further", "then", "once", "here", "there", "when", "where",
            "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some",
            "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very",
            "can", "will", "just", "should", "now", "i", "me", "my", "myself", "we", "our",
            "you", "your", "yours", "he", "him", "his", "she", "her", "they", "them", "their",
            "what", "which", "who", "whom", "this", "that", "these", "those", "am"
    ));

    // Vital programming and technical terms that MUST NEVER be stripped out
    private static final Set<String> PROTECTED_PROGRAMMING_TERMS = new HashSet<>(Arrays.asList(
            "class", "classes", "object", "objects", "oop", "java", "variable", "variables",
            "datatype", "datatypes", "type", "types", "array", "arrays", "arraylist",
            "loop", "loops", "for", "while", "do-while", "if", "else", "switch", "case",
            "condition", "conditions", "inheritance", "inherit", "polymorphism", "polymorphic",
            "encapsulation", "encapsulate", "abstraction", "abstract", "interface", "interfaces",
            "exception", "exceptions", "try", "catch", "finally", "throw", "throws",
            "collection", "collections", "list", "set", "map", "queue", "hashmap", "hashset",
            "file", "files", "io", "filereader", "filewriter", "stream", "reader", "writer",
            "public", "private", "protected", "static", "final", "void", "null", "new",
            "super", "this", "extends", "implements", "override", "overload", "constructor",
            "method", "methods", "function", "functions", "parameter", "return", "int", "float",
            "double", "boolean", "char", "string", "strings", "stringbuilder", "stringbuffer",
            "immutable", "immutability", "python", "javascript", "c++", "c", "ai", "ml",
            "api", "rest", "json", "sql", "html", "css", "docker", "cloud", "algorithm",
            "algorithms", "git", "github", "maven", "pom", "codealpha", "task", "internship",
            "example", "examples", "line", "code", "explain", "help", "resume", "email"
    ));

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9_#+\\-\\s]");
    private static final Pattern MULTI_WHITESPACE = Pattern.compile("\\s+");

    /**
     * Cleans, normalizes, and lowers the input string.
     */
    public String normalize(String input) {
        if (input == null) return "";
        String trimmed = input.trim();
        // Replace punctuation (preserving hyphens, pluses like C++, and hashes like C#)
        String cleaned = NON_ALPHANUMERIC.matcher(trimmed).replaceAll(" ");
        // Collapse whitespace and trim
        return MULTI_WHITESPACE.matcher(cleaned).replaceAll(" ").trim().toLowerCase();
    }

    /**
     * Tokenizes a raw sentence into normalized tokens.
     */
    public List<String> tokenize(String input) {
        if (input == null || input.isBlank()) return Collections.emptyList();
        String normalized = normalize(input);
        if (normalized.isBlank()) return Collections.emptyList();
        return Arrays.asList(normalized.split("\\s+"));
    }

    /**
     * Tokenizes and removes stop-words, strictly preserving programming keywords.
     */
    public List<String> extractKeywords(String input) {
        List<String> tokens = tokenize(input);
        List<String> filtered = new ArrayList<>();
        for (String token : tokens) {
            String lower = token.toLowerCase();
            if (PROTECTED_PROGRAMMING_TERMS.contains(lower)) {
                filtered.add(lower);
            } else if (!STOP_WORDS.contains(lower) && lower.length() > 2) {
                filtered.add(stem(lower));
            }
        }
        return filtered;
    }

    /**
     * Lightweight Porter-inspired stemmer for common English suffixes.
     */
    public String stem(String word) {
        if (word == null || word.length() <= 3) return word;
        String w = word.toLowerCase();

        // Do not alter protected terms
        if (PROTECTED_PROGRAMMING_TERMS.contains(w)) return w;

        if (w.endsWith("sses")) return w.substring(0, w.length() - 2);
        if (w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";
        if (w.endsWith("ing") && w.length() > 5) return w.substring(0, w.length() - 3);
        if (w.endsWith("ed") && w.length() > 4) return w.substring(0, w.length() - 2);
        if (w.endsWith("ly") && w.length() > 4) return w.substring(0, w.length() - 2);
        if (w.endsWith("tion") && w.length() > 5) return w.substring(0, w.length() - 4);
        if (w.endsWith("s") && !w.endsWith("ss") && w.length() > 3) return w.substring(0, w.length() - 1);

        return w;
    }

    /**
     * Determines whether user text is a follow-up request referencing recent context.
     * e.g., "give me an example", "show an example", "explain it line by line", "why?", "tell me more".
     */
    public FollowUpType detectFollowUpType(String input) {
        if (input == null) return FollowUpType.NONE;
        String norm = normalize(input);

        if (norm.matches(".*\\b(line by line|line-by-line|explain (it|this|that|each line)|step by step)\\b.*")) {
            return FollowUpType.LINE_BY_LINE;
        }

        if (norm.matches(".*\\b(give|show|provide)\\b.*\\b(example|code|snippet)\\b.*")
                || norm.contains("code example")
                || norm.equals("example")
                || norm.equals("give example")
                || norm.equals("show example")
                || norm.equals("code snippet")) {
            return FollowUpType.EXAMPLE;
        }

        if (norm.matches(".*\\b(tell me more|elaborate|details|more details|explain further|go deeper)\\b.*")) {
            return FollowUpType.ELABORATION;
        }

        return FollowUpType.NONE;
    }

    public enum FollowUpType {
        NONE,
        EXAMPLE,
        LINE_BY_LINE,
        ELABORATION
    }
}
