package com.codealpha.chatbot;

import com.codealpha.chatbot.model.AnalyticsSnapshot;
import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.Conversation;
import com.codealpha.chatbot.model.Role;
import com.codealpha.chatbot.service.AnalyticsService;
import com.codealpha.chatbot.service.ConversationService;
import com.codealpha.chatbot.service.KnowledgeBaseService;
import com.codealpha.chatbot.storage.JsonStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsServiceTest {

    private Path tempDir;
    private ConversationService convService;
    private KnowledgeBaseService kbService;
    private AnalyticsService analyticsService;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("analytics_test_");
        JsonStorageService storage = new JsonStorageService(tempDir);
        convService = new ConversationService(storage);
        kbService = new KnowledgeBaseService(storage);
        analyticsService = new AnalyticsService(convService, kbService);
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
    public void testAnalyticsCalculations() {
        Conversation conv = convService.createNewConversation("Testing Analytics");

        ChatMessage user1 = ChatMessage.user(conv.getId(), "Explain Polymorphism");
        convService.addMessage(user1);

        ChatMessage asst1 = ChatMessage.assistant(conv.getId(), "Polymorphism explanation...", "Polymorphism", 0.90, "OOP Concepts", 25);
        convService.addMessage(asst1);

        ChatMessage user2 = ChatMessage.user(conv.getId(), "Give me an example");
        convService.addMessage(user2);

        ChatMessage asst2 = ChatMessage.assistant(conv.getId(), "Code example...", "Polymorphism", 0.95, "OOP Concepts", 35);
        convService.addMessage(asst2);

        AnalyticsSnapshot snapshot = analyticsService.getSnapshot();

        assertEquals(2, snapshot.getTotalUserMessages());
        assertEquals(2, snapshot.getTotalAssistantResponses());
        assertEquals(30.0, snapshot.getAverageResponseTimeMs(), 0.01);
        assertEquals(2, snapshot.getIntentDistribution().get("Polymorphism"));
        assertEquals(2, snapshot.getQualityOverview().get("High Confidence"));
    }
}
