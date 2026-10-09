package com.codealpha.chatbot.service;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.Conversation;
import com.codealpha.chatbot.model.Role;
import com.codealpha.chatbot.storage.JsonStorageService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing conversation sessions, message history, pinning, and persistence.
 */
public class ConversationService {

    private final JsonStorageService storageService;
    private final List<Conversation> conversations = new ArrayList<>();
    private Conversation activeConversation;
    private final List<Runnable> changeListeners = new ArrayList<>();

    public ConversationService(JsonStorageService storageService) {
        this.storageService = storageService;
        reload();
    }

    public synchronized void reload() {
        conversations.clear();
        conversations.addAll(storageService.loadConversations());
        if (conversations.isEmpty()) {
            createNewConversation("General Discussion");
        } else {
            activeConversation = conversations.get(0);
        }
        notifyListeners();
    }

    public synchronized List<Conversation> getAllConversations() {
        return new ArrayList<>(conversations);
    }

    public synchronized List<Conversation> getPinnedConversations() {
        return conversations.stream()
                .filter(Conversation::isPinned)
                .sorted((a, b) -> Long.compare(b.getUpdatedAt(), a.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public synchronized List<Conversation> getRecentConversations() {
        return conversations.stream()
                .filter(c -> !c.isPinned())
                .sorted((a, b) -> Long.compare(b.getUpdatedAt(), a.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public synchronized Conversation getActiveConversation() {
        if (activeConversation == null) {
            if (conversations.isEmpty()) {
                createNewConversation("General Discussion");
            } else {
                activeConversation = conversations.get(0);
            }
        }
        return activeConversation;
    }

    public synchronized void setActiveConversation(Conversation conversation) {
        if (conversation != null && conversations.contains(conversation)) {
            this.activeConversation = conversation;
            notifyListeners();
        }
    }

    public synchronized void setActiveConversationById(String id) {
        if (id == null) return;
        conversations.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .ifPresent(this::setActiveConversation);
    }

    public synchronized Conversation createNewConversation(String title) {
        String cleanTitle = (title != null && !title.isBlank()) ? title : "New Conversation";
        Conversation conv = new Conversation(cleanTitle);
        conversations.add(0, conv);
        activeConversation = conv;
        save();
        return conv;
    }

    public synchronized boolean renameConversation(String id, String newTitle) {
        if (newTitle == null || newTitle.isBlank()) return false;
        for (Conversation c : conversations) {
            if (c.getId().equals(id)) {
                c.setTitle(newTitle.trim());
                save();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean togglePin(String id) {
        for (Conversation c : conversations) {
            if (c.getId().equals(id)) {
                c.setPinned(!c.isPinned());
                save();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteConversation(String id) {
        if (conversations.size() <= 1) {
            // Keep at least one conversation
            Conversation current = conversations.get(0);
            current.getMessages().clear();
            current.setTitle("New Conversation");
            current.touch();
            save();
            return true;
        }

        boolean removed = conversations.removeIf(c -> c.getId().equals(id));
        if (removed) {
            if (activeConversation != null && activeConversation.getId().equals(id)) {
                activeConversation = conversations.get(0);
            }
            save();
        }
        return removed;
    }

    public synchronized void clearActiveConversationMessages() {
        if (activeConversation != null) {
            activeConversation.getMessages().clear();
            activeConversation.touch();
            save();
        }
    }

    public synchronized void addMessage(ChatMessage message) {
        if (message == null) return;
        Conversation current = getActiveConversation();
        message.setConversationId(current.getId());
        current.addMessage(message);

        // Auto-generate title if this is the first user prompt and title is still default
        if (message.getRole() == Role.USER && current.getUserMessageCount() == 1
                && (current.getTitle().equals("New Conversation") || current.getTitle().equals("General Discussion"))) {
            current.setTitle(generateTitleFromPrompt(message.getContent()));
        }

        if (message.getDetectedIntent() != null) {
            current.setLastIntent(message.getDetectedIntent());
        }
        if (message.getCategory() != null) {
            current.setLastTopic(message.getCategory());
        }

        save();
    }

    private String generateTitleFromPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) return "Discussion";
        String clean = prompt.replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        if (clean.length() > 28) {
            clean = clean.substring(0, 25).trim() + "...";
        }
        if (clean.isEmpty()) return "Discussion";
        // Capitalize first letter
        return Character.toUpperCase(clean.charAt(0)) + clean.substring(1);
    }

    public synchronized List<Conversation> search(String query) {
        if (query == null || query.isBlank()) {
            return getAllConversations();
        }
        String lower = query.toLowerCase().trim();
        return conversations.stream()
                .filter(c -> {
                    if (c.getTitle().toLowerCase().contains(lower)) return true;
                    for (ChatMessage m : c.getMessages()) {
                        if (m.getContent().toLowerCase().contains(lower)) return true;
                    }
                    return false;
                })
                .sorted((a, b) -> Long.compare(b.getUpdatedAt(), a.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    private void save() {
        storageService.saveConversations(conversations);
        notifyListeners();
    }

    public synchronized void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable r : changeListeners) {
            try {
                r.run();
            } catch (Exception ignored) {
            }
        }
    }
}
