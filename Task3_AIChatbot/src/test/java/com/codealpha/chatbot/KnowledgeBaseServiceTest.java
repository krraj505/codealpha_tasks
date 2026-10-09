package com.codealpha.chatbot;

import com.codealpha.chatbot.model.KnowledgeEntry;
import com.codealpha.chatbot.service.KnowledgeBaseService;
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

public class KnowledgeBaseServiceTest {

    private Path tempDir;
    private KnowledgeBaseService service;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("kb_test_");
        JsonStorageService storage = new JsonStorageService(tempDir);
        service = new KnowledgeBaseService(storage);
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
    public void testAddAndRetrieveEntry() {
        KnowledgeEntry entry = new KnowledgeEntry("Testing", "What is JUnit?", "JUnit is a testing framework for Java.");
        boolean added = service.addEntry(entry);
        assertTrue(added);

        assertTrue(service.findById(entry.getId()).isPresent());
    }

    @Test
    public void testPreventDuplicateQuestions() {
        KnowledgeEntry entry1 = new KnowledgeEntry("Testing", "Unique Question", "Answer 1");
        service.addEntry(entry1);

        KnowledgeEntry entry2 = new KnowledgeEntry("Testing", "Unique Question", "Answer 2");
        assertThrows(IllegalArgumentException.class, () -> service.addEntry(entry2));
    }

    @Test
    public void testValidateEmptyFields() {
        assertThrows(IllegalArgumentException.class, () ->
                service.addEntry(new KnowledgeEntry("Testing", "", "Valid Answer")));

        assertThrows(IllegalArgumentException.class, () ->
                service.addEntry(new KnowledgeEntry("Testing", "Valid Question", "   ")));
    }

    @Test
    public void testSearchAndFilter() {
        service.addEntry(new KnowledgeEntry("Java Core", "Explain JVM bytecode architecture", "Bytecode is intermediate representation."));
        service.addEntry(new KnowledgeEntry("CustomCategory", "Explain OOP Custom", "OOP is based on objects."));

        List<KnowledgeEntry> searchResults = service.search("bytecode architecture", "All Categories");
        assertEquals(1, searchResults.size());
        assertEquals("Explain JVM bytecode architecture", searchResults.get(0).getQuestion());

        List<KnowledgeEntry> customResults = service.search("", "CustomCategory");
        assertEquals(1, customResults.size());
        assertEquals("Explain OOP Custom", customResults.get(0).getQuestion());
    }

    @Test
    public void testUpdateAndDelete() {
        KnowledgeEntry entry = new KnowledgeEntry("Java", "To Delete", "Delete me");
        service.addEntry(entry);

        entry.setAnswer("Updated answer");
        service.updateEntry(entry);
        assertEquals("Updated answer", service.findById(entry.getId()).get().getAnswer());

        boolean deleted = service.deleteEntry(entry.getId());
        assertTrue(deleted);
        assertFalse(service.findById(entry.getId()).isPresent());
    }
}
