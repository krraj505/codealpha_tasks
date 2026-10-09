package com.codealpha.chatbot.model;

/**
 * Message sender role in the chat interface.
 */
public enum Role {
    USER("User"),
    ASSISTANT("Assistant"),
    SYSTEM("System");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
