package com.codealpha.chatbot;

import com.codealpha.chatbot.ui.MainFrame;

import javax.swing.*;

/**
 * Main application entry point for RAJ AI.
 * Launches the Swing Event-Dispatch Thread with modern High-DPI
 * and subpixel text antialiasing configurations.
 */
public class Main {

    public static void main(String[] args) {
        // Optimize text rendering and High-DPI scaling on Windows
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.dpiaware", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch RAJ AI: " + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
