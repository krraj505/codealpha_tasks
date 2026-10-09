package com.codealpha.chatbot;

import com.codealpha.chatbot.model.IntentMatchResult;
import com.codealpha.chatbot.model.IntentType;
import com.codealpha.chatbot.model.KnowledgeEntry;
import com.codealpha.chatbot.nlp.ContextManager;
import com.codealpha.chatbot.nlp.IntentClassifier;
import com.codealpha.chatbot.nlp.RelevanceScorer;
import com.codealpha.chatbot.nlp.TextProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntentClassifierTest {

    private IntentClassifier classifier;
    private ContextManager contextManager;
    private List<KnowledgeEntry> kb;

    @BeforeEach
    public void setUp() {
        TextProcessor processor = new TextProcessor();
        RelevanceScorer scorer = new RelevanceScorer(processor);
        contextManager = new ContextManager();
        classifier = new IntentClassifier(processor, scorer, contextManager);

        kb = new ArrayList<>();
        KnowledgeEntry oopEntry = new KnowledgeEntry("OOP Concepts",
                "Explain Object-Oriented Programming (OOP) and its 4 pillars",
                "OOP has 4 pillars: encapsulation, inheritance, polymorphism, abstraction.");
        oopEntry.setFollowUpExample("class Car extends Vehicle { }");
        oopEntry.setFollowUpLineByLine("1. Extends vehicle.");
        kb.add(oopEntry);

        KnowledgeEntry polyEntry = new KnowledgeEntry("OOP Concepts",
                "Explain Polymorphism in Java",
                "Polymorphism allows multiple forms.");
        polyEntry.setFollowUpExample("Payment p = new UpiPayment();");
        polyEntry.setFollowUpLineByLine("1. Dynamic dispatch at runtime.");
        kb.add(polyEntry);
    }

    @Test
    public void testClassifyGreetings() {
        IntentMatchResult result = classifier.classify("Hello there!", kb, 0.45);
        assertEquals(IntentType.GREETINGS, result.getIntent());
        assertTrue(result.getConfidence() > 0.80);
    }

    @Test
    public void testClassifyIntroduction() {
        IntentMatchResult result = classifier.classify("Who are you and what can you do?", kb, 0.45);
        assertEquals(IntentType.INTRODUCTION, result.getIntent());
    }

    @Test
    public void testClassifyKnowledgeBaseEntry() {
        IntentMatchResult result = classifier.classify("Explain Polymorphism in Java", kb, 0.40);
        assertNotNull(result.getMatchedEntry());
        assertEquals("OOP Concepts", result.getMatchedCategory());
        assertTrue(result.getConfidence() >= 0.80);
    }

    @Test
    public void testContextualFollowUp() {
        // Step 1: establish active context
        KnowledgeEntry entry = kb.get(1); // Polymorphism
        contextManager.updateContext(IntentType.POLYMORPHISM, entry, entry.getCategory());

        // Step 2: ask follow-up "give me an example"
        IntentMatchResult followUp = classifier.classify("give me an example", kb, 0.40);
        assertTrue(followUp.isFollowUp());
        assertEquals("example", followUp.getFollowUpType());
        assertEquals(entry, followUp.getMatchedEntry());

        // Step 3: ask "explain it line by line"
        IntentMatchResult lineByLine = classifier.classify("explain it line by line", kb, 0.40);
        assertTrue(lineByLine.isFollowUp());
        assertEquals("line_by_line", lineByLine.getFollowUpType());
    }

    @Test
    public void testFallbackOnUnknownQuery() {
        IntentMatchResult result = classifier.classify("xyz123 random nonsense quantum recipe", kb, 0.45);
        assertEquals(IntentType.UNKNOWN, result.getIntent());
        assertFalse(result.isHighConfidence(0.45));
    }
}
