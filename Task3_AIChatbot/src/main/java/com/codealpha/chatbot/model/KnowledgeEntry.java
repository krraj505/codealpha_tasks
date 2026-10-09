package com.codealpha.chatbot.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single Q&A entry in the chatbot's knowledge base.
 */
public class KnowledgeEntry {
    private String id;
    private String category;
    private String question;
    private String answer;
    private List<String> keywords;
    private List<String> tags;
    private String followUpExample;
    private String followUpLineByLine;
    private int usageCount;
    private long lastUpdated;

    public KnowledgeEntry() {
        this.id = UUID.randomUUID().toString();
        this.keywords = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.usageCount = 0;
        this.lastUpdated = System.currentTimeMillis();
    }

    public KnowledgeEntry(String category, String question, String answer) {
        this();
        this.category = category;
        this.question = question;
        this.answer = answer;
    }

    public KnowledgeEntry(String category, String question, String answer,
                          List<String> keywords, String followUpExample, String followUpLineByLine) {
        this(category, question, answer);
        if (keywords != null) {
            this.keywords.addAll(keywords);
        }
        this.followUpExample = followUpExample;
        this.followUpLineByLine = followUpLineByLine;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category != null ? category : "General";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuestion() {
        return question != null ? question : "";
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer != null ? answer : "";
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getKeywords() {
        if (keywords == null) keywords = new ArrayList<>();
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords != null ? new ArrayList<>(keywords) : new ArrayList<>();
    }

    public List<String> getTags() {
        if (tags == null) tags = new ArrayList<>();
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public String getFollowUpExample() {
        return followUpExample;
    }

    public void setFollowUpExample(String followUpExample) {
        this.followUpExample = followUpExample;
    }

    public String getFollowUpLineByLine() {
        return followUpLineByLine;
    }

    public void setFollowUpLineByLine(String followUpLineByLine) {
        this.followUpLineByLine = followUpLineByLine;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public void incrementUsage() {
        this.usageCount++;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void touch() {
        this.lastUpdated = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeEntry that = (KnowledgeEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "[" + category + "] " + question;
    }
}
