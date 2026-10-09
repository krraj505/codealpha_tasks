package com.codealpha.chatbot.service;

import com.codealpha.chatbot.model.KnowledgeEntry;
import com.codealpha.chatbot.storage.JsonStorageService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing Knowledge Base entries, indexing, searching, and updates.
 */
public class KnowledgeBaseService {

    private final JsonStorageService storageService;
    private final List<KnowledgeEntry> entries = new ArrayList<>();
    private final List<Runnable> changeListeners = new ArrayList<>();

    public KnowledgeBaseService(JsonStorageService storageService) {
        this.storageService = storageService;
        reload();
    }

    public synchronized void reload() {
        entries.clear();
        entries.addAll(storageService.loadKnowledgeBase());
        notifyListeners();
    }

    public synchronized List<KnowledgeEntry> getAllEntries() {
        return new ArrayList<>(entries);
    }

    public synchronized Optional<KnowledgeEntry> findById(String id) {
        if (id == null) return Optional.empty();
        return entries.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public synchronized List<String> getCategories() {
        Set<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (KnowledgeEntry e : entries) {
            if (e.getCategory() != null && !e.getCategory().isBlank()) {
                categories.add(e.getCategory());
            }
        }
        return new ArrayList<>(categories);
    }

    public synchronized List<KnowledgeEntry> search(String query, String categoryFilter) {
        String lowerQuery = query != null ? query.toLowerCase().trim() : "";
        boolean filterCategory = categoryFilter != null && !categoryFilter.isBlank() && !categoryFilter.equalsIgnoreCase("All Categories");

        return entries.stream()
                .filter(e -> {
                    if (filterCategory && !e.getCategory().equalsIgnoreCase(categoryFilter)) {
                        return false;
                    }
                    if (lowerQuery.isEmpty()) {
                        return true;
                    }
                    if (e.getQuestion().toLowerCase().contains(lowerQuery)) return true;
                    if (e.getAnswer().toLowerCase().contains(lowerQuery)) return true;
                    for (String kw : e.getKeywords()) {
                        if (kw.toLowerCase().contains(lowerQuery)) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    public synchronized boolean addEntry(KnowledgeEntry entry) {
        validateEntry(entry);

        // Check for duplicate questions
        boolean exists = entries.stream()
                .anyMatch(e -> e.getQuestion().trim().equalsIgnoreCase(entry.getQuestion().trim()));
        if (exists) {
            throw new IllegalArgumentException("An entry with this question already exists.");
        }

        entry.touch();
        entries.add(entry);
        save();
        return true;
    }

    public synchronized boolean updateEntry(KnowledgeEntry updated) {
        validateEntry(updated);

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId().equals(updated.getId())) {
                updated.touch();
                entries.set(i, updated);
                save();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteEntry(String id) {
        boolean removed = entries.removeIf(e -> e.getId().equals(id));
        if (removed) {
            save();
        }
        return removed;
    }

    public synchronized void recordUsage(String entryId) {
        findById(entryId).ifPresent(e -> {
            e.incrementUsage();
            save();
        });
    }

    private void validateEntry(KnowledgeEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Knowledge entry cannot be null.");
        }
        if (entry.getQuestion() == null || entry.getQuestion().trim().isBlank()) {
            throw new IllegalArgumentException("Question cannot be empty.");
        }
        if (entry.getAnswer() == null || entry.getAnswer().trim().isBlank()) {
            throw new IllegalArgumentException("Answer cannot be empty.");
        }
        if (entry.getCategory() == null || entry.getCategory().trim().isBlank()) {
            entry.setCategory("General");
        }
    }

    private void save() {
        storageService.saveKnowledgeBase(entries);
        notifyListeners();
    }

    public synchronized String exportToJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(entries);
    }

    public synchronized int importFromJson(String jsonContent) {
        if (jsonContent == null || jsonContent.isBlank()) {
            throw new IllegalArgumentException("JSON content is empty.");
        }
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<KnowledgeEntry>>() {}.getType();
        List<KnowledgeEntry> imported = gson.fromJson(jsonContent, listType);
        if (imported == null || imported.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (KnowledgeEntry item : imported) {
            if (item.getQuestion() != null && !item.getQuestion().isBlank()
                    && item.getAnswer() != null && !item.getAnswer().isBlank()) {
                boolean duplicate = entries.stream()
                        .anyMatch(e -> e.getQuestion().trim().equalsIgnoreCase(item.getQuestion().trim()));
                if (!duplicate) {
                    item.touch();
                    entries.add(item);
                    count++;
                }
            }
        }
        if (count > 0) {
            save();
        }
        return count;
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
