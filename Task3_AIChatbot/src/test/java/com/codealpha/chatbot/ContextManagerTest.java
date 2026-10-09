package com.codealpha.chatbot;

import com.codealpha.chatbot.model.IntentType;
import com.codealpha.chatbot.model.KnowledgeEntry;
import com.codealpha.chatbot.nlp.ContextManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContextManagerTest {

    private ContextManager manager;

    @BeforeEach
    public void setUp() {
        manager = new ContextManager();
    }

    @Test
    public void testInitialState() {
        assertFalse(manager.hasActiveContext());
        assertEquals("General", manager.getCurrentTopic());
    }

    @Test
    public void testUpdateAndRetrieveContext() {
        KnowledgeEntry entry = new KnowledgeEntry("OOP Concepts", "What is Inheritance?", "Inheritance allows code reuse.");
        entry.setFollowUpExample("class Dog extends Animal {}");
        manager.updateContext(IntentType.INHERITANCE, entry, "OOP Concepts");

        assertTrue(manager.hasActiveContext());
        assertEquals(IntentType.INHERITANCE, manager.getLastIntent());
        assertEquals("OOP Concepts", manager.getCurrentTopic());
        assertEquals(entry, manager.getLastKnowledgeEntry());
        assertEquals("class Dog extends Animal {}", manager.getLastGeneratedExample());
    }

    @Test
    public void testResetClearsContext() {
        KnowledgeEntry entry = new KnowledgeEntry("OOP Concepts", "What is Inheritance?", "Inheritance allows code reuse.");
        manager.updateContext(IntentType.INHERITANCE, entry, "OOP Concepts");
        assertTrue(manager.hasActiveContext());

        manager.reset();
        assertFalse(manager.hasActiveContext());
        assertNull(manager.getLastKnowledgeEntry());
    }
}
