package com.codealpha.chatbot.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Encapsulates a chat message with NLP metadata and timestamps.
 */
public class ChatMessage {
    private String id;
    private String conversationId;
    private Role role;
    private String content;
    private long timestamp;
    private String detectedIntent;
    private double confidenceScore;
    private String category;
    private long responseTimeMs;
    private boolean isFollowUp;
    private List<String> suggestedQuestions;

    private static final DateTimeFormatter TIME_FORMAT = 
            DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter FULL_FORMAT = 
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a").withZone(ZoneId.systemDefault());

    public ChatMessage() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.suggestedQuestions = new ArrayList<>();
    }

    public ChatMessage(String conversationId, Role role, String content) {
        this();
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
    }

    public static ChatMessage user(String conversationId, String content) {
        return new ChatMessage(conversationId, Role.USER, content);
    }

    public static ChatMessage assistant(String conversationId, String content, String intent, double confidence, String category, long responseTimeMs) {
        ChatMessage msg = new ChatMessage(conversationId, Role.ASSISTANT, content);
        msg.detectedIntent = intent;
        msg.confidenceScore = confidence;
        msg.category = category;
        msg.responseTimeMs = responseTimeMs;
        return msg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getContent() {
        return content != null ? content : "";
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTime() {
        return TIME_FORMAT.format(Instant.ofEpochMilli(timestamp));
    }

    public String getFormattedFullDate() {
        return FULL_FORMAT.format(Instant.ofEpochMilli(timestamp));
    }

    public String getDetectedIntent() {
        return detectedIntent;
    }

    public void setDetectedIntent(String detectedIntent) {
        this.detectedIntent = detectedIntent;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public boolean isFollowUp() {
        return isFollowUp;
    }

    public void setFollowUp(boolean followUp) {
        isFollowUp = followUp;
    }

    public List<String> getSuggestedQuestions() {
        if (suggestedQuestions == null) suggestedQuestions = new ArrayList<>();
        return suggestedQuestions;
    }

    public void setSuggestedQuestions(List<String> suggestedQuestions) {
        this.suggestedQuestions = suggestedQuestions != null ? new ArrayList<>(suggestedQuestions) : new ArrayList<>();
    }
}
