package com.codealpha.chatbot.ui.components;

import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Syntax styled code box with a header bar and one-click copy button.
 */
public class CodeBlockView extends JPanel {

    private final String codeText;
    private final JButton copyButton;

    public CodeBlockView(String code, String language) {
        this.codeText = code != null ? code.trim() : "";
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(6, 0, 8, 0));

        ColorPalette palette = ThemeManager.getInstance().getPalette();

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JLabel langLabel = new JLabel((language != null && !language.isBlank()) ? language.toUpperCase() : "JAVA");
        langLabel.setFont(ThemeManager.getInstance().getSmallFont());
        langLabel.setForeground(palette.accentPrimary);

        copyButton = new JButton("Copy Code", UiUtils.createCopyIcon(14, palette.textSecondary));
        copyButton.setFont(ThemeManager.getInstance().getSmallFont());
        copyButton.setForeground(palette.textSecondary);
        copyButton.setFocusPainted(false);
        copyButton.setContentAreaFilled(false);
        copyButton.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        copyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        copyButton.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(codeText), null);
            copyButton.setText("Copied!");
            copyButton.setForeground(palette.accentSuccess);
            Timer timer = new Timer(1800, evt -> {
                copyButton.setText("Copy Code");
                copyButton.setForeground(palette.textSecondary);
            });
            timer.setRepeats(false);
            timer.start();
        });

        headerPanel.add(langLabel, BorderLayout.WEST);
        headerPanel.add(copyButton, BorderLayout.EAST);

        // Code Content Area
        JTextArea textArea = new JTextArea(this.codeText);
        textArea.setEditable(false);
        textArea.setFont(ThemeManager.getInstance().getCodeFont());
        textArea.setBackground(palette.codeBlockBackground);
        textArea.setForeground(palette.textPrimary);
        textArea.setCaretColor(palette.accentPrimary);
        textArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 10, 12));
        textArea.setLineWrap(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Main container card
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.setRenderingHints(g2);
                ColorPalette p = ThemeManager.getInstance().getPalette();
                g2.setColor(p.codeBlockBackground);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(p.border);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }
}
