package com.codealpha.chatbot.service;

import com.codealpha.chatbot.model.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Computes live analytics and key performance indicators (KPIs)
 * directly from stored conversation sessions and knowledge base usage counters.
 */
public class AnalyticsService {

    private final ConversationService conversationService;
    private final KnowledgeBaseService knowledgeBaseService;

    private static final DateTimeFormatter DATE_KEY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd").withZone(ZoneId.systemDefault());

    public AnalyticsService(ConversationService conversationService, KnowledgeBaseService knowledgeBaseService) {
        this.conversationService = conversationService;
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * Calculates an up-to-date AnalyticsSnapshot from actual stored data.
     */
    public synchronized AnalyticsSnapshot getSnapshot() {
        List<Conversation> conversations = conversationService.getAllConversations();
        List<KnowledgeEntry> kbEntries = knowledgeBaseService.getAllEntries();

        if (conversations.isEmpty()) {
            return AnalyticsSnapshot.empty();
        }

        int totalConvs = conversations.size();
        int totalUserMsgs = 0;
        int totalAssistantMsgs = 0;
        long totalResponseTime = 0;
        int responseTimeCount = 0;

        Map<String, Integer> intentCounts = new LinkedHashMap<>();
        Map<String, Integer> categoryUsage = new LinkedHashMap<>();
        Map<String, Integer> qualityCounts = new LinkedHashMap<>();
        qualityCounts.put("High Confidence", 0);
        qualityCounts.put("Clarifications", 0);
        qualityCounts.put("Fallbacks", 0);

        Map<String, Integer> timeline = new LinkedHashMap<>();

        // Initialize timeline for the last 5 days with 0
        LocalDate today = LocalDate.now();
        for (int i = 4; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String label = d.format(DateTimeFormatter.ofPattern("MMM dd"));
            timeline.put(label, 0);
        }

        List<AnalyticsSnapshot.RecentConversationItem> recentList = new ArrayList<>();

        for (Conversation c : conversations) {
            recentList.add(new AnalyticsSnapshot.RecentConversationItem(
                    c.getId(),
                    c.getTitle(),
                    c.getFormattedDate(),
                    c.getMessageCount(),
                    c.getLastIntent()
            ));

            for (ChatMessage m : c.getMessages()) {
                // Day activity
                String dayLabel = DATE_KEY_FORMAT.format(Instant.ofEpochMilli(m.getTimestamp()));
                timeline.put(dayLabel, timeline.getOrDefault(dayLabel, 0) + 1);

                if (m.getRole() == Role.USER) {
                    totalUserMsgs++;
                } else if (m.getRole() == Role.ASSISTANT) {
                    totalAssistantMsgs++;
                    if (m.getResponseTimeMs() > 0) {
                        totalResponseTime += m.getResponseTimeMs();
                        responseTimeCount++;
                    }

                    // Intent tally
                    String intent = m.getDetectedIntent();
                    if (intent != null && !intent.isBlank()) {
                        intentCounts.put(intent, intentCounts.getOrDefault(intent, 0) + 1);
                    }

                    // Category tally
                    String cat = m.getCategory();
                    if (cat != null && !cat.isBlank() && !cat.equalsIgnoreCase("Fallback")) {
                        categoryUsage.put(cat, categoryUsage.getOrDefault(cat, 0) + 1);
                    }

                    // Quality tally
                    double conf = m.getConfidenceScore();
                    if (conf >= 0.70) {
                        qualityCounts.put("High Confidence", qualityCounts.get("High Confidence") + 1);
                    } else if (conf >= 0.40) {
                        qualityCounts.put("Clarifications", qualityCounts.get("Clarifications") + 1);
                    } else {
                        qualityCounts.put("Fallbacks", qualityCounts.get("Fallbacks") + 1);
                    }
                }
            }
        }

        // Incorporate knowledge base usage counters into category usage if available
        for (KnowledgeEntry ke : kbEntries) {
            if (ke.getUsageCount() > 0) {
                String cat = ke.getCategory();
                categoryUsage.put(cat, categoryUsage.getOrDefault(cat, 0) + ke.getUsageCount());
            }
        }

        double avgResponseTime = responseTimeCount > 0 ? (double) totalResponseTime / responseTimeCount : 0.0;

        return new AnalyticsSnapshot(
                totalConvs,
                totalUserMsgs,
                totalAssistantMsgs,
                avgResponseTime,
                intentCounts,
                categoryUsage,
                qualityCounts,
                timeline,
                recentList
        );
    }
}
