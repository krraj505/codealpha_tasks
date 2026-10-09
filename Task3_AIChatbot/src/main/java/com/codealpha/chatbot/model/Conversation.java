package com.codealpha.chatbot.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a conversation session containing a sequence of messages.
 */
public class Conversation {
    private String id;
    private String title;
    private long createdAt;
    private long updatedAt;
    private boolean pinned;
    private List<ChatMessage> messages;
    private String lastIntent;
    private String lastTopic;

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault());

    public Conversation() {
        this.id = UUID.randomUUID().toString();
        this.title = "New Conversation";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.pinned = false;
        this.messages = new ArrayList<>();
        this.lastIntent = "Greetings";
        this.lastTopic = "General";
    }

    public Conversation(String title) {
        this();
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null && !title.isBlank() ? title : "Untitled Conversation";
    }

    public void setTitle(String title) {
        this.title = title;
        touch();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
        touch();
    }

    public List<ChatMessage> getMessages() {
        if (messages == null) messages = new ArrayList<>();
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
        touch();
    }

    public void addMessage(ChatMessage message) {
        if (messages == null) messages = new ArrayList<>();
        messages.add(message);
        touch();
    }

    public String getLastIntent() {
        return lastIntent != null ? lastIntent : "None";
    }

    public void setLastIntent(String lastIntent) {
        this.lastIntent = lastIntent;
    }

    public String getLastTopic() {
        return lastTopic != null ? lastTopic : "General";
    }

    public void setLastTopic(String lastTopic) {
        this.lastTopic = lastTopic;
    }

    public int getMessageCount() {
        return messages != null ? messages.size() : 0;
    }

    public int getUserMessageCount() {
        if (messages == null) return 0;
        int count = 0;
        for (ChatMessage m : messages) {
            if (m.getRole() == Role.USER) count++;
        }
        return count;
    }

    public int getAssistantResponseCount() {
        if (messages == null) return 0;
        int count = 0;
        for (ChatMessage m : messages) {
            if (m.getRole() == Role.ASSISTANT) count++;
        }
        return count;
    }

    public String getFormattedDate() {
        return DISPLAY_FORMAT.format(Instant.ofEpochMilli(updatedAt));
    }

    public String getSummary() {
        if (messages == null || messages.isEmpty()) {
            return "No messages yet";
        }
        ChatMessage last = messages.get(messages.size() - 1);
        String text = last.getContent().replaceAll("\\s+", " ").trim();
        return text.length() > 60 ? text.substring(0, 57) + "..." : text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
