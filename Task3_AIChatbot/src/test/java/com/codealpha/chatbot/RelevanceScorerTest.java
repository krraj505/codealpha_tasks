package com.codealpha.chatbot;

import com.codealpha.chatbot.model.KnowledgeEntry;
import com.codealpha.chatbot.nlp.RelevanceScorer;
import com.codealpha.chatbot.nlp.TextProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RelevanceScorerTest {

    private RelevanceScorer scorer;

    @BeforeEach
    public void setUp() {
        scorer = new RelevanceScorer(new TextProcessor());
    }

    @Test
    public void testExactMatchScoresHighest() {
        KnowledgeEntry entry = new KnowledgeEntry("OOP", "Explain Polymorphism in Java", "Polymorphism means many forms.");
        double score = scorer.scoreEntry("Explain Polymorphism in Java", entry);
        assertEquals(1.0, score, 0.001);
    }

    @Test
    public void testFuzzyMatchHandlesTypos() {
        KnowledgeEntry entry = new KnowledgeEntry("OOP", "Explain Polymorphism", "Polymorphism is many forms.");
        entry.setKeywords(List.of("polymorphism", "oop"));

        // Typo: "polymorfism"
        double score = scorer.scoreEntry("polymorfism", entry);
        assertTrue(score > 0.40, "Fuzzy scorer should identify typo in polymorphism");
    }

    @Test
    public void testIrrelevantQueryReturnsLowScore() {
        KnowledgeEntry entry = new KnowledgeEntry("OOP", "Explain Polymorphism", "Polymorphism is many forms.");
        double score = scorer.scoreEntry("What is the capital of France?", entry);
        assertTrue(score < 0.20, "Unrelated query should receive very low score");
    }
}
