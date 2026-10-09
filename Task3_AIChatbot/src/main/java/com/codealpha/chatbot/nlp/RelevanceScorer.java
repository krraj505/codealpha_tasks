package com.codealpha.chatbot.nlp;

import com.codealpha.chatbot.model.KnowledgeEntry;

import java.util.*;

/**
 * Computes semantic and lexical relevance scores between user input
 * and knowledge base entries using weighted keyword matching, phrase overlap,
 * and fuzzy Levenshtein distance.
 */
public class RelevanceScorer {

    private final TextProcessor textProcessor;

    public RelevanceScorer(TextProcessor textProcessor) {
        this.textProcessor = textProcessor != null ? textProcessor : new TextProcessor();
    }

    /**
     * Scores a knowledge entry against a user query. Returns value in [0.0, 1.0].
     */
    public double scoreEntry(String query, KnowledgeEntry entry) {
        if (query == null || entry == null) return 0.0;

        String normQuery = textProcessor.normalize(query);
        String normQuestion = textProcessor.normalize(entry.getQuestion());

        if (normQuery.isBlank() || normQuestion.isBlank()) return 0.0;

        // Exact match boost
        if (normQuery.equals(normQuestion)) {
            return 1.0;
        }

        // Substring match boost
        if (normQuestion.contains(normQuery) || normQuery.contains(normQuestion)) {
            return 0.92;
        }

        List<String> queryKeywords = textProcessor.extractKeywords(query);
        List<String> questionKeywords = textProcessor.extractKeywords(entry.getQuestion());

        if (queryKeywords.isEmpty()) {
            return 0.0;
        }

        // Calculate keyword intersection with entry question and tagged keywords
        Set<String> entryKeywordsSet = new HashSet<>(questionKeywords);
        if (entry.getKeywords() != null) {
            for (String kw : entry.getKeywords()) {
                entryKeywordsSet.add(kw.toLowerCase().trim());
            }
        }

        int matchedCount = 0;
        for (String qk : queryKeywords) {
            if (entryKeywordsSet.contains(qk)) {
                matchedCount++;
            } else {
                // Check fuzzy match for typos
                for (String ek : entryKeywordsSet) {
                    if (isFuzzyMatch(qk, ek)) {
                        matchedCount++;
                        break;
                    }
                }
            }
        }

        // Jaccard similarity
        double keywordRecall = (double) matchedCount / queryKeywords.size();
        double jaccard = (double) matchedCount / (queryKeywords.size() + entryKeywordsSet.size() - matchedCount + 0.001);

        // Check answer text overlap for supporting context
        double answerOverlapScore = 0.0;
        String normAnswer = textProcessor.normalize(entry.getAnswer());
        int answerMatches = 0;
        for (String qk : queryKeywords) {
            if (normAnswer.contains(qk)) {
                answerMatches++;
            }
        }
        if (!queryKeywords.isEmpty()) {
            answerOverlapScore = (double) answerMatches / queryKeywords.size() * 0.35;
        }

        double finalScore = (keywordRecall * 0.55) + (jaccard * 0.30) + (answerOverlapScore * 0.15);
        return Math.min(1.0, Math.max(0.0, finalScore));
    }

    /**
     * Checks if two tokens are fuzzy matches within Levenshtein edit distance of 1 or 2.
     */
    public boolean isFuzzyMatch(String a, String b) {
        if (a == null || b == null) return false;
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen < 4) return a.equalsIgnoreCase(b);
        int distance = levenshteinDistance(a.toLowerCase(), b.toLowerCase());
        int allowed = maxLen > 6 ? 2 : 1;
        return distance <= allowed;
    }

    /**
     * Standard Wagner-Fischer algorithm for Levenshtein Distance.
     */
    public int levenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int j = 0; j <= s2.length(); j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= s1.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= s2.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        s1.charAt(i - 1) == s2.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[s2.length()];
    }
}
