package com.codealpha.chatbot.ui.theme;

import com.codealpha.chatbot.model.UserSettings;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages look-and-feel configuration, active ColorPalette, and dynamic theme switching.
 */
public class ThemeManager {

    private static ThemeManager instance;
    private ColorPalette currentPalette;
    private int currentFontSize = 14;
    private final List<Runnable> themeChangeListeners = new ArrayList<>();

    private ThemeManager() {
        this.currentPalette = new ColorPalette(true);
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void init(UserSettings settings) {
        this.currentFontSize = settings.getFontSize();
        boolean dark = settings.isDarkTheme();
        this.currentPalette = new ColorPalette(dark);
        applyFlatLaf(dark);
    }

    public ColorPalette getPalette() {
        return currentPalette;
    }

    public int getFontSize() {
        return currentFontSize;
    }

    public void setFontSize(int size, Component root) {
        this.currentFontSize = size;
        notifyThemeChanged(root);
    }

    public void setTheme(boolean dark, Component root) {
        this.currentPalette = new ColorPalette(dark);
        applyFlatLaf(dark);
        notifyThemeChanged(root);
    }

    private void applyFlatLaf(boolean dark) {
        try {
            if (dark) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }
    }

    public Font getRegularFont() {
        return new Font("Segoe UI", Font.PLAIN, currentFontSize);
    }

    public Font getBoldFont() {
        return new Font("Segoe UI", Font.BOLD, currentFontSize);
    }

    public Font getTitleFont() {
        return new Font("Segoe UI", Font.BOLD, currentFontSize + 4);
    }

    public Font getLargeTitleFont() {
        return new Font("Segoe UI", Font.BOLD, currentFontSize + 8);
    }

    public Font getSmallFont() {
        return new Font("Segoe UI", Font.PLAIN, Math.max(10, currentFontSize - 2));
    }

    public Font getCodeFont() {
        return new Font("Consolas", Font.PLAIN, Math.max(11, currentFontSize - 1));
    }

    public void addThemeChangeListener(Runnable listener) {
        themeChangeListeners.add(listener);
    }

    private void notifyThemeChanged(Component root) {
        if (root != null) {
            SwingUtilities.updateComponentTreeUI(root);
            root.repaint();
        }
        for (Runnable r : themeChangeListeners) {
            try {
                r.run();
            } catch (Exception ignored) {
            }
        }
    }
}
