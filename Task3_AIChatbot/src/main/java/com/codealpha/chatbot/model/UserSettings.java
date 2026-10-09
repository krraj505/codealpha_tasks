package com.codealpha.chatbot.model;

/**
 * User preferences and customization settings.
 */
public class UserSettings {
    private String theme; // "DARK" or "LIGHT"
    private int fontSize; // 12, 14, 16
    private double confidenceThreshold; // 0.20 to 0.80
    private boolean autoScroll;
    private boolean showContextPanel;
    private boolean showTimestamps;
    private String userName;
    private boolean soundEnabled;
    private boolean sidebarCollapsed;

    // General-purpose AI / LLM Integration Settings
    private boolean enableLlm;
    private String llmProvider;
    private String llmModel;
    private String llmBaseUrl;
    private String llmApiKeyOverride;
    private boolean preferOnlineAi;

    public UserSettings() {
        this.theme = "DARK";
        this.fontSize = 14;
        this.confidenceThreshold = 0.40;
        this.autoScroll = true;
        this.showContextPanel = true;
        this.showTimestamps = true;
        this.userName = "Raj";
        this.soundEnabled = false;
        this.sidebarCollapsed = false;

        this.enableLlm = true;
        this.llmProvider = "OpenAI Compatible";
        this.llmModel = "gpt-4o-mini";
        this.llmBaseUrl = "https://api.openai.com/v1/chat/completions";
        this.llmApiKeyOverride = "";
        this.preferOnlineAi = false;
    }

    public String getTheme() {
        return theme != null ? theme : "DARK";
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public boolean isDarkTheme() {
        return "DARK".equalsIgnoreCase(theme);
    }

    public int getFontSize() {
        return fontSize >= 11 && fontSize <= 22 ? fontSize : 14;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold > 0 ? confidenceThreshold : 0.40;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public boolean isAutoScroll() {
        return autoScroll;
    }

    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    public boolean isShowContextPanel() {
        return showContextPanel;
    }

    public void setShowContextPanel(boolean showContextPanel) {
        this.showContextPanel = showContextPanel;
    }

    public boolean isShowTimestamps() {
        return showTimestamps;
    }

    public void setShowTimestamps(boolean showTimestamps) {
        this.showTimestamps = showTimestamps;
    }

    public String getUserName() {
        return userName != null ? userName : "Student";
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean isSidebarCollapsed() {
        return sidebarCollapsed;
    }

    public void setSidebarCollapsed(boolean sidebarCollapsed) {
        this.sidebarCollapsed = sidebarCollapsed;
    }

    public boolean isEnableLlm() {
        return enableLlm;
    }

    public void setEnableLlm(boolean enableLlm) {
        this.enableLlm = enableLlm;
    }

    public String getLlmProvider() {
        return llmProvider != null ? llmProvider : "OpenAI Compatible";
    }

    public void setLlmProvider(String llmProvider) {
        this.llmProvider = llmProvider;
    }

    public String getLlmModel() {
        return (llmModel != null && !llmModel.isBlank()) ? llmModel : "gpt-4o-mini";
    }

    public void setLlmModel(String llmModel) {
        this.llmModel = llmModel;
    }

    public String getLlmBaseUrl() {
        return (llmBaseUrl != null && !llmBaseUrl.isBlank()) ? llmBaseUrl : "https://api.openai.com/v1/chat/completions";
    }

    public void setLlmBaseUrl(String llmBaseUrl) {
        this.llmBaseUrl = llmBaseUrl;
    }

    public String getLlmApiKeyOverride() {
        return llmApiKeyOverride != null ? llmApiKeyOverride : "";
    }

    public void setLlmApiKeyOverride(String llmApiKeyOverride) {
        this.llmApiKeyOverride = llmApiKeyOverride;
    }

    public boolean isPreferOnlineAi() {
        return preferOnlineAi;
    }

    public void setPreferOnlineAi(boolean preferOnlineAi) {
        this.preferOnlineAi = preferOnlineAi;
    }
}
