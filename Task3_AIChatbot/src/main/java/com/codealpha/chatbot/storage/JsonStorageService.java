package com.codealpha.chatbot.storage;

import com.codealpha.chatbot.model.Conversation;
import com.codealpha.chatbot.model.KnowledgeEntry;
import com.codealpha.chatbot.model.UserSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe local JSON persistence service.
 * Manages loading and atomic saving of conversations, knowledge base, and settings.
 */
public class JsonStorageService {

    private final Path dataDirectory;
    private final Path conversationsFile;
    private final Path knowledgeBaseFile;
    private final Path settingsFile;

    private final Gson gson;
    private final Object lock = new Object();

    public JsonStorageService() {
        this(resolveDefaultDataDirectory());
    }

    public JsonStorageService(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.conversationsFile = dataDirectory.resolve("conversations.json");
        this.knowledgeBaseFile = dataDirectory.resolve("knowledge_base.json");
        this.settingsFile = dataDirectory.resolve("settings.json");

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        initDirectories();
    }

    private static Path resolveDefaultDataDirectory() {
        Path p1 = Paths.get("data");
        Path p2 = Paths.get("Task3_AIChatbot", "data");
        if (Files.exists(p2)) {
            return p2;
        }
        if (Files.exists(p1)) {
            return p1;
        }
        // If current directory contains src, use ./data; otherwise Task3_AIChatbot/data
        if (Files.exists(Paths.get("src"))) {
            return p1;
        }
        return p2;
    }

    private void initDirectories() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to initialize storage directory: " + dataDirectory, e);
        }
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    // ==========================================
    // CONVERSATIONS
    // ==========================================

    public List<Conversation> loadConversations() {
        synchronized (lock) {
            if (!Files.exists(conversationsFile)) {
                return new ArrayList<>();
            }
            try (Reader reader = Files.newBufferedReader(conversationsFile, StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<ArrayList<Conversation>>() {}.getType();
                List<Conversation> list = gson.fromJson(reader, listType);
                return list != null ? list : new ArrayList<>();
            } catch (Exception e) {
                System.err.println("Warning: Could not read conversations.json, returning empty list: " + e.getMessage());
                return new ArrayList<>();
            }
        }
    }

    public void saveConversations(List<Conversation> conversations) {
        synchronized (lock) {
            writeJsonAtomically(conversationsFile, conversations);
        }
    }

    // ==========================================
    // KNOWLEDGE BASE
    // ==========================================

    public List<KnowledgeEntry> loadKnowledgeBase() {
        synchronized (lock) {
            if (Files.exists(knowledgeBaseFile)) {
                try (Reader reader = Files.newBufferedReader(knowledgeBaseFile, StandardCharsets.UTF_8)) {
                    Type listType = new TypeToken<ArrayList<KnowledgeEntry>>() {}.getType();
                    List<KnowledgeEntry> list = gson.fromJson(reader, listType);
                    if (list != null && !list.isEmpty()) {
                        return list;
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Corrupted knowledge_base.json, falling back to seed: " + e.getMessage());
                }
            }

            // Seed from default classpath resource or fallback template
            List<KnowledgeEntry> defaultEntries = loadDefaultKnowledgeBaseSeed();
            saveKnowledgeBase(defaultEntries);
            return defaultEntries;
        }
    }

    public void saveKnowledgeBase(List<KnowledgeEntry> entries) {
        synchronized (lock) {
            writeJsonAtomically(knowledgeBaseFile, entries);
        }
    }

    private List<KnowledgeEntry> loadDefaultKnowledgeBaseSeed() {
        // Try reading from classloader
        try (InputStream in = getClass().getResourceAsStream("/knowledge-base/default_kb.json")) {
            if (in != null) {
                try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    Type listType = new TypeToken<ArrayList<KnowledgeEntry>>() {}.getType();
                    List<KnowledgeEntry> list = gson.fromJson(reader, listType);
                    if (list != null && !list.isEmpty()) {
                        return list;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // Try reading directly from file system path if running in dev
        Path resourcePath = Paths.get("src", "main", "resources", "knowledge-base", "default_kb.json");
        if (!Files.exists(resourcePath)) {
            resourcePath = Paths.get("Task3_AIChatbot", "src", "main", "resources", "knowledge-base", "default_kb.json");
        }
        if (Files.exists(resourcePath)) {
            try (Reader reader = Files.newBufferedReader(resourcePath, StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<ArrayList<KnowledgeEntry>>() {}.getType();
                List<KnowledgeEntry> list = gson.fromJson(reader, listType);
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Exception ignored) {
            }
        }

        // Fallback minimal in-memory entry if no file is present
        List<KnowledgeEntry> fallback = new ArrayList<>();
        KnowledgeEntry intro = new KnowledgeEntry("Conversational", "Hello", "Hello! I am your CodeAlpha AI Chatbot.");
        intro.getKeywords().add("hello");
        fallback.add(intro);
        return fallback;
    }

    // ==========================================
    // USER SETTINGS
    // ==========================================

    public UserSettings loadSettings() {
        synchronized (lock) {
            if (!Files.exists(settingsFile)) {
                UserSettings defaults = new UserSettings();
                saveSettings(defaults);
                return defaults;
            }
            try (Reader reader = Files.newBufferedReader(settingsFile, StandardCharsets.UTF_8)) {
                UserSettings settings = gson.fromJson(reader, UserSettings.class);
                return settings != null ? settings : new UserSettings();
            } catch (Exception e) {
                System.err.println("Warning: Corrupted settings.json, returning defaults: " + e.getMessage());
                return new UserSettings();
            }
        }
    }

    public void saveSettings(UserSettings settings) {
        synchronized (lock) {
            writeJsonAtomically(settingsFile, settings);
        }
    }

    // ==========================================
    // UTILITY: ATOMIC WRITE
    // ==========================================

    private void writeJsonAtomically(Path targetFile, Object data) {
        Path tempFile = targetFile.resolveSibling(targetFile.getFileName() + ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                gson.toJson(data, writer);
            }
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            try {
                Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new StorageException("Failed to save data to " + targetFile, ex);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
            }
            throw new StorageException("Failed to write data atomically to " + targetFile, e);
        }
    }

    public void clearAllData() {
        synchronized (lock) {
            try {
                Files.deleteIfExists(conversationsFile);
                Files.deleteIfExists(knowledgeBaseFile);
                Files.deleteIfExists(settingsFile);
            } catch (IOException e) {
                throw new StorageException("Failed to clear data files", e);
            }
        }
    }
}
