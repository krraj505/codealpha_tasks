package com.codealpha.chatbot.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of calculated application analytics.
 */
public class AnalyticsSnapshot {
    private final int totalConversations;
    private final int totalUserMessages;
    private final int totalAssistantResponses;
    private final double averageResponseTimeMs;
    private final Map<String, Integer> intentDistribution;
    private final Map<String, Integer> categoryUsage;
    private final Map<String, Integer> qualityOverview;
    private final Map<String, Integer> activityTimeline;
    private final List<RecentConversationItem> recentConversations;

    public AnalyticsSnapshot(int totalConversations, int totalUserMessages, int totalAssistantResponses,
                             double averageResponseTimeMs, Map<String, Integer> intentDistribution,
                             Map<String, Integer> categoryUsage, Map<String, Integer> qualityOverview,
                             Map<String, Integer> activityTimeline, List<RecentConversationItem> recentConversations) {
        this.totalConversations = totalConversations;
        this.totalUserMessages = totalUserMessages;
        this.totalAssistantResponses = totalAssistantResponses;
        this.averageResponseTimeMs = averageResponseTimeMs;
        this.intentDistribution = intentDistribution != null ? intentDistribution : new LinkedHashMap<>();
        this.categoryUsage = categoryUsage != null ? categoryUsage : new LinkedHashMap<>();
        this.qualityOverview = qualityOverview != null ? qualityOverview : new LinkedHashMap<>();
        this.activityTimeline = activityTimeline != null ? activityTimeline : new LinkedHashMap<>();
        this.recentConversations = recentConversations != null ? recentConversations : new ArrayList<>();
    }

    public static AnalyticsSnapshot empty() {
        return new AnalyticsSnapshot(0, 0, 0, 0.0,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), new ArrayList<>());
    }

    public int getTotalConversations() {
        return totalConversations;
    }

    public int getTotalUserMessages() {
        return totalUserMessages;
    }

    public int getTotalAssistantResponses() {
        return totalAssistantResponses;
    }

    public double getAverageResponseTimeMs() {
        return averageResponseTimeMs;
    }

    public Map<String, Integer> getIntentDistribution() {
        return intentDistribution;
    }

    public Map<String, Integer> getCategoryUsage() {
        return categoryUsage;
    }

    public Map<String, Integer> getQualityOverview() {
        return qualityOverview;
    }

    public Map<String, Integer> getActivityTimeline() {
        return activityTimeline;
    }

    public List<RecentConversationItem> getRecentConversations() {
        return recentConversations;
    }

    public static class RecentConversationItem {
        private final String conversationId;
        private final String title;
        private final String lastActivity;
        private final int messageCount;
        private final String lastIntent;

        public RecentConversationItem(String conversationId, String title, String lastActivity, int messageCount, String lastIntent) {
            this.conversationId = conversationId;
            this.title = title;
            this.lastActivity = lastActivity;
            this.messageCount = messageCount;
            this.lastIntent = lastIntent;
        }

        public String getConversationId() {
            return conversationId;
        }

        public String getTitle() {
            return title;
        }

        public String getLastActivity() {
            return lastActivity;
        }

        public int getMessageCount() {
            return messageCount;
        }

        public String getLastIntent() {
            return lastIntent;
        }
    }
}
