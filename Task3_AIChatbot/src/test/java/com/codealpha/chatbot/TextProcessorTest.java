package com.codealpha.chatbot;

import com.codealpha.chatbot.nlp.TextProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TextProcessorTest {

    private TextProcessor processor;

    @BeforeEach
    public void setUp() {
        processor = new TextProcessor();
    }

    @Test
    public void testNormalize() {
        String raw = "  Hello, World!!   What is Java 17?  ";
        String normalized = processor.normalize(raw);
        assertEquals("hello world what is java 17", normalized);
    }

    @Test
    public void testTokenize() {
        String input = "Object oriented programming concepts";
        List<String> tokens = processor.tokenize(input);
        assertEquals(4, tokens.size());
        assertEquals("object", tokens.get(0));
        assertEquals("oriented", tokens.get(1));
    }

    @Test
    public void testPreserveProgrammingTerms() {
        String query = "How does a class implement an interface in Java?";
        List<String> keywords = processor.extractKeywords(query);

        assertTrue(keywords.contains("class"), "Must preserve 'class'");
        assertTrue(keywords.contains("interface"), "Must preserve 'interface'");
        assertTrue(keywords.contains("java"), "Must preserve 'java'");
        assertFalse(keywords.contains("how"), "Stop-word 'how' should be filtered");
        assertFalse(keywords.contains("does"), "Stop-word 'does' should be filtered");
    }

    @Test
    public void testDetectFollowUpTypes() {
        assertEquals(TextProcessor.FollowUpType.EXAMPLE,
                processor.detectFollowUpType("give me an example"));
        assertEquals(TextProcessor.FollowUpType.EXAMPLE,
                processor.detectFollowUpType("show code example"));
        assertEquals(TextProcessor.FollowUpType.LINE_BY_LINE,
                processor.detectFollowUpType("explain it line by line"));
        assertEquals(TextProcessor.FollowUpType.LINE_BY_LINE,
                processor.detectFollowUpType("step by step explanation"));
        assertEquals(TextProcessor.FollowUpType.ELABORATION,
                processor.detectFollowUpType("tell me more about this"));
        assertEquals(TextProcessor.FollowUpType.NONE,
                processor.detectFollowUpType("What is polymorphism?"));
    }
}
