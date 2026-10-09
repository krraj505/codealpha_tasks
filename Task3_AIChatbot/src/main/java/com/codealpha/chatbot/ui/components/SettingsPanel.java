package com.codealpha.chatbot.ui.components;

import com.codealpha.chatbot.model.UserSettings;
import com.codealpha.chatbot.service.ConversationService;
import com.codealpha.chatbot.service.KnowledgeBaseService;
import com.codealpha.chatbot.service.SettingsService;
import com.codealpha.chatbot.storage.JsonStorageService;
import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Settings and Personalization page for theme customization, font scaling,
 * AI confidence thresholds, data clearing, and internship metadata.
 */
public class SettingsPanel extends JPanel {

    private final SettingsService settingsService;
    private final ConversationService conversationService;
    private final KnowledgeBaseService kbService;
    private final JsonStorageService storageService;
    private final Runnable onThemeChanged;

    private JRadioButton darkRadio;
    private JRadioButton lightRadio;
    private JComboBox<String> fontCombo;
    private JSlider thresholdSlider;
    private JLabel thresholdLabel;
    private JCheckBox autoScrollCheck;
    private JCheckBox timestampsCheck;

    public SettingsPanel(SettingsService settingsService,
                         ConversationService conversationService,
                         KnowledgeBaseService kbService,
                         JsonStorageService storageService,
                         Runnable onThemeChanged) {
        this.settingsService = settingsService;
        this.conversationService = conversationService;
        this.kbService = kbService;
        this.storageService = storageService;
        this.onThemeChanged = onThemeChanged;

        setLayout(new BorderLayout());
        setOpaque(false);

        buildUI();
    }

    private void buildUI() {
        ColorPalette palette = ThemeManager.getInstance().getPalette();
        UserSettings current = settingsService.getSettings();

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Page Header
        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        titleBlock.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel pageTitle = new JLabel("Settings & Personalization");
        pageTitle.setFont(ThemeManager.getInstance().getLargeTitleFont());
        pageTitle.setForeground(palette.textPrimary);

        JLabel pageSubtitle = new JLabel("Customize visual styling, font scaling, and AI response parameters");
        pageSubtitle.setFont(ThemeManager.getInstance().getSmallFont());
        pageSubtitle.setForeground(palette.textSecondary);

        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);

        container.add(titleBlock);
        container.add(Box.createRigidArea(new Dimension(0, 20)));

        // 1. APPEARANCE CARD
        JPanel appearanceCard = createCard("Visual Appearance & Themes");

        JPanel themeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        themeRow.setOpaque(false);

        darkRadio = new JRadioButton("Dark Navy (Default)", current.isDarkTheme());
        darkRadio.setOpaque(false);
        darkRadio.setFont(ThemeManager.getInstance().getRegularFont());
        darkRadio.setForeground(palette.textPrimary);

        lightRadio = new JRadioButton("Light Studio", !current.isDarkTheme());
        lightRadio.setOpaque(false);
        lightRadio.setFont(ThemeManager.getInstance().getRegularFont());
        lightRadio.setForeground(palette.textPrimary);

        ButtonGroup group = new ButtonGroup();
        group.add(darkRadio);
        group.add(lightRadio);

        darkRadio.addActionListener(e -> {
            settingsService.setTheme("DARK");
            if (onThemeChanged != null) onThemeChanged.run();
        });

        lightRadio.addActionListener(e -> {
            settingsService.setTheme("LIGHT");
            if (onThemeChanged != null) onThemeChanged.run();
        });

        themeRow.add(new JLabel("Color Theme:"));
        themeRow.add(darkRadio);
        themeRow.add(lightRadio);

        // Font scaling
        JPanel fontRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        fontRow.setOpaque(false);

        fontCombo = new JComboBox<>(new String[]{"Compact (12 pt)", "Standard (14 pt)", "Comfortable (16 pt)"});
        int size = current.getFontSize();
        if (size == 12) fontCombo.setSelectedIndex(0);
        else if (size == 16) fontCombo.setSelectedIndex(2);
        else fontCombo.setSelectedIndex(1);

        fontCombo.addActionListener(e -> {
            int selected = fontCombo.getSelectedIndex();
            int newSize = (selected == 0) ? 12 : (selected == 2) ? 16 : 14;
            settingsService.setFontSize(newSize);
            ThemeManager.getInstance().setFontSize(newSize, SwingUtilities.getWindowAncestor(this));
        });

        fontRow.add(new JLabel("Font Size:"));
        fontRow.add(fontCombo);

        appearanceCard.add(themeRow);
        appearanceCard.add(Box.createRigidArea(new Dimension(0, 8)));
        appearanceCard.add(fontRow);

        container.add(appearanceCard);
        container.add(Box.createRigidArea(new Dimension(0, 16)));

        // 2. AI ENGINE PARAMETERS CARD
        JPanel aiCard = createCard("AI Engine & NLP Parameters");

        JPanel threshRow = new JPanel(new BorderLayout(12, 0));
        threshRow.setOpaque(false);

        int currentPct = (int) Math.round(current.getConfidenceThreshold() * 100);
        thresholdLabel = new JLabel("Confidence Match Threshold: " + currentPct + "%");
        thresholdLabel.setFont(ThemeManager.getInstance().getRegularFont());
        thresholdLabel.setForeground(palette.textPrimary);

        thresholdSlider = new JSlider(20, 80, currentPct);
        thresholdSlider.setOpaque(false);
        thresholdSlider.setMajorTickSpacing(20);
        thresholdSlider.setMinorTickSpacing(5);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.addChangeListener(e -> {
            int val = thresholdSlider.getValue();
            thresholdLabel.setText("Confidence Match Threshold: " + val + "%");
            settingsService.setConfidenceThreshold(val / 100.0);
        });

        threshRow.add(thresholdLabel, BorderLayout.NORTH);
        threshRow.add(thresholdSlider, BorderLayout.CENTER);

        aiCard.add(threshRow);
        aiCard.add(Box.createRigidArea(new Dimension(0, 10)));

        autoScrollCheck = new JCheckBox("Auto-scroll to latest response", current.isAutoScroll());
        autoScrollCheck.setOpaque(false);
        autoScrollCheck.addActionListener(e -> {
            current.setAutoScroll(autoScrollCheck.isSelected());
            settingsService.updateSettings(current);
        });

        timestampsCheck = new JCheckBox("Display timestamps on message bubbles", current.isShowTimestamps());
        timestampsCheck.setOpaque(false);
        timestampsCheck.addActionListener(e -> {
            current.setShowTimestamps(timestampsCheck.isSelected());
            settingsService.updateSettings(current);
        });

        aiCard.add(autoScrollCheck);
        aiCard.add(timestampsCheck);

        container.add(aiCard);
        container.add(Box.createRigidArea(new Dimension(0, 16)));

        // 3. DATA & STORAGE MANAGEMENT CARD
        JPanel dataCard = createCard("Data Storage & Maintenance");

        JPanel dataBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        dataBtnRow.setOpaque(false);

        JButton clearDataBtn = new JButton("Clear Conversation History");
        clearDataBtn.setForeground(palette.accentDanger);
        clearDataBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete all saved conversations?\nThis action cannot be undone.",
                    "Confirm Clear History", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                storageService.saveConversations(new java.util.ArrayList<>());
                conversationService.reload();
                JOptionPane.showMessageDialog(this, "Conversation history cleared successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton resetKbBtn = new JButton("Reset Knowledge Base to Defaults");
        resetKbBtn.setForeground(palette.accentPrimary);
        resetKbBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reset knowledge base to factory default Q&A entries?",
                    "Confirm Reset", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                storageService.clearAllData();
                kbService.reload();
                conversationService.reload();
                JOptionPane.showMessageDialog(this, "Knowledge base reset to defaults.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        dataBtnRow.add(clearDataBtn);
        dataBtnRow.add(resetKbBtn);
        dataCard.add(dataBtnRow);

        container.add(dataCard);
        container.add(Box.createRigidArea(new Dimension(0, 16)));

        // 4. ABOUT CARD
        JPanel aboutCard = createCard("About RAJ AI");

        String infoHtml = "<html><body style='width: 520px; font-family: Segoe UI;'>"
                + "<p><b>RAJ AI — Java Programming Assistant</b></p>"
                + "<p>A modern desktop AI companion designed and implemented for the <b>CodeAlpha Java Programming Internship (Task 3)</b>.</p>"
                + "<br/>"
                + "<p>• <b>Application:</b> RAJ AI</p>"
                + "<p>• <b>Developer:</b> Kumar Raj</p>"
                + "<p>• <b>Runtime:</b> Java 17+ (Swing GUI & FlatLaf Engine)</p>"
                + "<p>• <b>Architecture:</b> Modular Layered Model-Service-UI Architecture</p>"
                + "<p>• <b>NLP Engine:</b> Rule-based intent classifier, multi-turn context tracking, and semantic relevance scoring</p>"
                + "<p>• <b>Persistence:</b> Local thread-safe atomic JSON storage</p>"
                + "</body></html>";

        JLabel infoLabel = new JLabel(infoHtml);
        infoLabel.setFont(ThemeManager.getInstance().getRegularFont());
        infoLabel.setForeground(palette.textSecondary);
        aboutCard.add(infoLabel);

        container.add(aboutCard);

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createCard(String title) {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.setRenderingHints(g2);
                ColorPalette p = ThemeManager.getInstance().getPalette();
                g2.setColor(p.cardBackground);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(p.border);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 16, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        JLabel t = new JLabel(title);
        t.setFont(ThemeManager.getInstance().getBoldFont());
        t.setForeground(palette.textPrimary);
        card.add(t);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        return card;
    }
}
