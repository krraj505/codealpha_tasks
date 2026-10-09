package com.codealpha.chatbot;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.KnowledgeEntry;
import com.codealpha.chatbot.service.ChatbotEngine;
import com.codealpha.chatbot.service.KnowledgeBaseService;
import com.codealpha.chatbot.service.SettingsService;
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

public class ChatbotEngineTest {

    private Path tempDir;
    private ChatbotEngine engine;
    private KnowledgeBaseService kbService;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("engine_test_");
        JsonStorageService storage = new JsonStorageService(tempDir);
        kbService = new KnowledgeBaseService(storage);
        SettingsService settings = new SettingsService(storage);
        engine = new ChatbotEngine(kbService, settings);

        // KnowledgeBaseService is already populated with default seed containing "Explain Java classes and objects"
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
    public void testDirectKnowledgeQuery() {
        ChatMessage response = engine.processQuery("conv-1", "Explain Java classes and objects");
        assertNotNull(response);
        assertTrue(response.getContent().contains("blueprint"));
        assertTrue(response.getConfidenceScore() >= 0.70);
    }

    @Test
    public void testSequentialContextFollowUpFlow() {
        // Turn 1: Main topic
        ChatMessage r1 = engine.processQuery("conv-1", "Explain Java classes and objects");
        assertTrue(r1.getContent().contains("blueprint"));

        // Turn 2: Follow-up code example
        ChatMessage r2 = engine.processQuery("conv-1", "Give me an example");
        assertTrue(r2.getContent().contains("```java"));
        assertTrue(r2.getContent().contains("Student"));
        assertTrue(r2.isFollowUp());

        // Turn 3: Follow-up line-by-line explanation
        ChatMessage r3 = engine.processQuery("conv-1", "Explain it line by line");
        assertTrue(r3.getContent().contains("Line-by-Line"));
        assertTrue(r3.getContent().contains("Student"));
        assertTrue(r3.isFollowUp());
    }

    @Test
    public void testFallbackQuery() {
        ChatMessage response = engine.processQuery("conv-1", "Explain quantum rocket astrophysics");
        assertTrue(response.getContent().contains("couldn't find a high-confidence match"));
        assertEquals("Unknown or Unsupported Questions", response.getDetectedIntent());
    }
}
