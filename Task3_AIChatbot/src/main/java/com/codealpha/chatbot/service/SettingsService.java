package com.codealpha.chatbot.service;

import com.codealpha.chatbot.model.UserSettings;
import com.codealpha.chatbot.storage.JsonStorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Service managing user settings, persistence, and live property update listeners.
 */
public class SettingsService {

    private final JsonStorageService storageService;
    private UserSettings currentSettings;
    private final List<Consumer<UserSettings>> changeListeners = new ArrayList<>();

    public SettingsService(JsonStorageService storageService) {
        this.storageService = storageService;
        this.currentSettings = storageService.loadSettings();
    }

    public synchronized UserSettings getSettings() {
        return currentSettings;
    }

    public synchronized void updateSettings(UserSettings updated) {
        this.currentSettings = updated;
        storageService.saveSettings(updated);
        notifyListeners();
    }

    public synchronized void setTheme(String theme) {
        currentSettings.setTheme(theme);
        storageService.saveSettings(currentSettings);
        notifyListeners();
    }

    public synchronized void setFontSize(int size) {
        currentSettings.setFontSize(size);
        storageService.saveSettings(currentSettings);
        notifyListeners();
    }

    public synchronized void setConfidenceThreshold(double threshold) {
        currentSettings.setConfidenceThreshold(threshold);
        storageService.saveSettings(currentSettings);
        notifyListeners();
    }

    public synchronized void setSidebarCollapsed(boolean collapsed) {
        currentSettings.setSidebarCollapsed(collapsed);
        storageService.saveSettings(currentSettings);
        notifyListeners();
    }

    public synchronized void addChangeListener(Consumer<UserSettings> listener) {
        changeListeners.add(listener);
    }

    private void notifyListeners() {
        for (Consumer<UserSettings> l : changeListeners) {
            try {
                l.accept(currentSettings);
            } catch (Exception ignored) {
            }
        }
    }
}
