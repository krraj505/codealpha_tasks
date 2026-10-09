package com.codealpha.chatbot;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.Conversation;
import com.codealpha.chatbot.model.Role;
import com.codealpha.chatbot.service.ConversationService;
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

public class ConversationServiceTest {

    private Path tempDir;
    private ConversationService service;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("conv_test_");
        JsonStorageService storage = new JsonStorageService(tempDir);
        service = new ConversationService(storage);
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
    public void testCreateAndSwitchConversation() {
        Conversation c1 = service.createNewConversation("Session 1");
        assertEquals("Session 1", service.getActiveConversation().getTitle());

        Conversation c2 = service.createNewConversation("Session 2");
        assertEquals("Session 2", service.getActiveConversation().getTitle());

        service.setActiveConversation(c1);
        assertEquals(c1.getId(), service.getActiveConversation().getId());
    }

    @Test
    public void testAutoTitleGeneration() {
        Conversation conv = service.createNewConversation("New Conversation");
        service.setActiveConversation(conv);

        ChatMessage msg = ChatMessage.user(conv.getId(), "Explain Java classes and objects in depth");
        service.addMessage(msg);

        assertNotEquals("New Conversation", service.getActiveConversation().getTitle());
        assertTrue(service.getActiveConversation().getTitle().toLowerCase().contains("explain java"));
    }

    @Test
    public void testTogglePin() {
        Conversation conv = service.createNewConversation("Pin Test");
        assertFalse(conv.isPinned());

        service.togglePin(conv.getId());
        assertTrue(service.getActiveConversation().isPinned());
    }

    @Test
    public void testDeleteConversation() {
        Conversation c1 = service.createNewConversation("First");
        Conversation c2 = service.createNewConversation("Second");

        assertTrue(service.deleteConversation(c1.getId()));
        List<Conversation> remaining = service.getAllConversations();
        assertTrue(remaining.stream().noneMatch(c -> c.getId().equals(c1.getId())));
    }
}
