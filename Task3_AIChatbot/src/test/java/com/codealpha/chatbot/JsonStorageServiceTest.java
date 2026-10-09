package com.codealpha.chatbot;

import com.codealpha.chatbot.model.Conversation;
import com.codealpha.chatbot.model.KnowledgeEntry;
import com.codealpha.chatbot.model.UserSettings;
import com.codealpha.chatbot.storage.JsonStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonStorageServiceTest {

    private Path tempDir;
    private JsonStorageService storage;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("storage_test_");
        storage = new JsonStorageService(tempDir);
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                    });
        }
    }

    @Test
    public void testPersistAndLoadSettings() {
        UserSettings s = new UserSettings();
        s.setTheme("LIGHT");
        s.setFontSize(16);
        s.setConfidenceThreshold(0.65);
        storage.saveSettings(s);

        UserSettings loaded = storage.loadSettings();
        assertEquals("LIGHT", loaded.getTheme());
        assertEquals(16, loaded.getFontSize());
        assertEquals(0.65, loaded.getConfidenceThreshold(), 0.001);
    }

    @Test
    public void testPersistAndLoadConversations() {
        Conversation c1 = new Conversation("Test Session");
        storage.saveConversations(List.of(c1));

        List<Conversation> loaded = storage.loadConversations();
        assertEquals(1, loaded.size());
        assertEquals("Test Session", loaded.get(0).getTitle());
    }

    @Test
    public void testPersistAndLoadKnowledgeBase() {
        KnowledgeEntry entry = new KnowledgeEntry("Core", "Test Question", "Test Answer");
        storage.saveKnowledgeBase(List.of(entry));

        List<KnowledgeEntry> loaded = storage.loadKnowledgeBase();
        assertEquals(1, loaded.size());
        assertEquals("Test Question", loaded.get(0).getQuestion());
    }
}
