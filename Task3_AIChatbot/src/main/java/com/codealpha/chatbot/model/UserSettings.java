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
}
