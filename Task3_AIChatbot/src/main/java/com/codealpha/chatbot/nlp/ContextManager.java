package com.codealpha.chatbot.nlp;

import com.codealpha.chatbot.model.IntentType;
import com.codealpha.chatbot.model.KnowledgeEntry;

/**
 * Manages conversational state, topic tracking, and context resolution
 * across multi-turn interactions.
 */
public class ContextManager {

    private String currentTopic;
    private IntentType lastIntent;
    private KnowledgeEntry lastKnowledgeEntry;
    private String lastGeneratedExample;
    private long lastInteractionTime;

    public ContextManager() {
        reset();
    }

    public synchronized void reset() {
        this.currentTopic = "General";
        this.lastIntent = IntentType.GREETINGS;
        this.lastKnowledgeEntry = null;
        this.lastGeneratedExample = null;
        this.lastInteractionTime = System.currentTimeMillis();
    }

    public synchronized void updateContext(IntentType intent, KnowledgeEntry entry, String topic) {
        if (intent != null && intent != IntentType.UNKNOWN) {
            this.lastIntent = intent;
        }
        if (entry != null) {
            this.lastKnowledgeEntry = entry;
            if (entry.getCategory() != null) {
                this.currentTopic = entry.getCategory();
            }
            if (entry.getFollowUpExample() != null) {
                this.lastGeneratedExample = entry.getFollowUpExample();
            }
        } else if (topic != null && !topic.isBlank()) {
            this.currentTopic = topic;
        }
        this.lastInteractionTime = System.currentTimeMillis();
    }

    public synchronized boolean hasActiveContext() {
        return lastKnowledgeEntry != null && (System.currentTimeMillis() - lastInteractionTime < 1000 * 60 * 30);
    }

    public synchronized String getCurrentTopic() {
        return currentTopic != null ? currentTopic : "General";
    }

    public synchronized IntentType getLastIntent() {
        return lastIntent != null ? lastIntent : IntentType.GREETINGS;
    }

    public synchronized KnowledgeEntry getLastKnowledgeEntry() {
        return lastKnowledgeEntry;
    }

    public synchronized String getLastGeneratedExample() {
        return lastGeneratedExample;
    }

    public synchronized void setLastGeneratedExample(String example) {
        this.lastGeneratedExample = example;
    }
}
