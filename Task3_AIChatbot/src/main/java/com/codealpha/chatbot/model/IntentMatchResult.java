package com.codealpha.chatbot.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result returned by the NLP Intent Classifier.
 */
public class IntentMatchResult {
    private final IntentType intent;
    private final double confidence;
    private final String matchedCategory;
    private final List<String> matchedKeywords;
    private final boolean isFollowUp;
    private final KnowledgeEntry matchedEntry;
    private final String followUpType; // "example", "line_by_line", "detail", null

    public IntentMatchResult(IntentType intent, double confidence, String matchedCategory,
                             List<String> matchedKeywords, boolean isFollowUp,
                             KnowledgeEntry matchedEntry, String followUpType) {
        this.intent = intent;
        this.confidence = confidence;
        this.matchedCategory = matchedCategory;
        this.matchedKeywords = matchedKeywords != null ? matchedKeywords : new ArrayList<>();
        this.isFollowUp = isFollowUp;
        this.matchedEntry = matchedEntry;
        this.followUpType = followUpType;
    }

    public static IntentMatchResult fallback(String reason) {
        List<String> kw = new ArrayList<>();
        kw.add(reason);
        return new IntentMatchResult(IntentType.UNKNOWN, 0.15, "Fallback", kw, false, null, null);
    }

    public IntentType getIntent() {
        return intent;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getMatchedCategory() {
        return matchedCategory != null ? matchedCategory : intent.getCategory();
    }

    public List<String> getMatchedKeywords() {
        return matchedKeywords;
    }

    public boolean isFollowUp() {
        return isFollowUp;
    }

    public KnowledgeEntry getMatchedEntry() {
        return matchedEntry;
    }

    public String getFollowUpType() {
        return followUpType;
    }

    public boolean isHighConfidence(double threshold) {
        return confidence >= threshold;
    }

    public boolean isMediumConfidence(double threshold) {
        return confidence >= (threshold * 0.65) && confidence < threshold;
    }

    @Override
    public String toString() {
        return String.format("%s (%.1f%%) in %s", intent.getDisplayName(), confidence * 100, matchedCategory);
    }
}
