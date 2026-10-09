package com.codealpha.chatbot.ui.components;

import com.codealpha.chatbot.model.UserSettings;
import com.codealpha.chatbot.service.ConversationService;
import com.codealpha.chatbot.service.KnowledgeBaseService;
import com.codealpha.chatbot.service.LlmService;
import com.codealpha.chatbot.service.SettingsService;
import com.codealpha.chatbot.storage.JsonStorageService;
import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Settings and Personalization page for theme customization, font scaling,
 * general-purpose AI / LLM configuration, NLP matching parameters,
 * data clearing, and internship metadata.
 */
public class SettingsPanel extends JPanel {

    private final SettingsService settingsService;
    private final ConversationService conversationService;
    private final KnowledgeBaseService kbService;
    private final JsonStorageService storageService;
    private final LlmService llmService;
    private final Runnable onThemeChanged;

    private JRadioButton darkRadio;
    private JRadioButton lightRadio;
    private JComboBox<String> fontCombo;
    private JSlider thresholdSlider;
    private JLabel thresholdLabel;
    private JCheckBox autoScrollCheck;
    private JCheckBox timestampsCheck;

    // AI Configuration Elements
    private JLabel aiStatusLabel;
    private JLabel aiSourceLabel;
    private JComboBox<String> providerCombo;
    private JTextField modelField;
    private JTextField baseUrlField;
    private JPasswordField apiKeyField;
    private JCheckBox preferOnlineCheck;
    private JButton testConnectionBtn;

    public SettingsPanel(SettingsService settingsService,
                         ConversationService conversationService,
                         KnowledgeBaseService kbService,
                         JsonStorageService storageService,
                         Runnable onThemeChanged) {
        this(settingsService, conversationService, kbService, storageService, new LlmService(settingsService), onThemeChanged);
    }

    public SettingsPanel(SettingsService settingsService,
                         ConversationService conversationService,
                         KnowledgeBaseService kbService,
                         JsonStorageService storageService,
                         LlmService llmService,
                         Runnable onThemeChanged) {
        this.settingsService = settingsService;
        this.conversationService = conversationService;
        this.kbService = kbService;
        this.storageService = storageService;
        this.llmService = llmService;
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

        JLabel pageSubtitle = new JLabel("Customize visual styling, general AI models, and NLP response parameters");
        pageSubtitle.setFont(ThemeManager.getInstance().getSmallFont());
        pageSubtitle.setForeground(palette.textSecondary);

        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);

        container.add(titleBlock);
        container.add(Box.createRigidArea(new Dimension(0, 20)));

        // 1. GENERAL-PURPOSE AI / LLM CONFIGURATION CARD
        JPanel aiModelCard = createCard("General-Purpose AI & Model Configuration");
        aiModelCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));

        // Status Header Row
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        statusRow.setOpaque(false);

        boolean isConfigured = llmService.isConfigured();
        aiStatusLabel = new JLabel(isConfigured ? "● Online (AI Connected)" : "○ Offline (Knowledge Base Mode)");
        aiStatusLabel.setFont(ThemeManager.getInstance().getBoldFont());
        aiStatusLabel.setForeground(isConfigured ? new Color(16, 185, 129) : new Color(245, 158, 11));

        aiSourceLabel = new JLabel("Key: " + llmService.getKeySource() + " [" + llmService.getMaskedApiKey() + "]");
        aiSourceLabel.setFont(ThemeManager.getInstance().getSmallFont());
        aiSourceLabel.setForeground(palette.textSecondary);

        statusRow.add(aiStatusLabel);
        statusRow.add(aiSourceLabel);
        aiModelCard.add(statusRow);
        aiModelCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // Provider Selector
        JPanel providerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        providerRow.setOpaque(false);
        JLabel provLbl = new JLabel("AI Provider:");
        provLbl.setFont(ThemeManager.getInstance().getRegularFont());
        provLbl.setForeground(palette.textPrimary);

        providerCombo = new JComboBox<>(new String[]{
                "OpenAI (Official)",
                "Groq Cloud (Fast & Free Tier)",
                "OpenRouter (Multi-Model)",
                "Ollama (Local Self-Hosted)",
                "Custom OpenAI-Compatible"
        });
        providerCombo.setFont(ThemeManager.getInstance().getRegularFont());

        providerRow.add(provLbl);
        providerRow.add(providerCombo);
        aiModelCard.add(providerRow);
        aiModelCard.add(Box.createRigidArea(new Dimension(0, 6)));

        // Model and Endpoint Inputs
        JPanel configGrid = new JPanel(new GridLayout(3, 2, 10, 8));
        configGrid.setOpaque(false);
        configGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel modelLbl = new JLabel("Model Name:");
        modelLbl.setForeground(palette.textPrimary);
        modelField = new JTextField(current.getLlmModel());
        modelField.setFont(ThemeManager.getInstance().getRegularFont());

        JLabel urlLbl = new JLabel("API Base URL:");
        urlLbl.setForeground(palette.textPrimary);
        baseUrlField = new JTextField(current.getLlmBaseUrl());
        baseUrlField.setFont(ThemeManager.getInstance().getRegularFont());

        JLabel keyLbl = new JLabel("Manual API Key (Optional):");
        keyLbl.setForeground(palette.textPrimary);
        apiKeyField = new JPasswordField(current.getLlmApiKeyOverride());
        apiKeyField.setFont(ThemeManager.getInstance().getRegularFont());

        configGrid.add(modelLbl);
        configGrid.add(modelField);
        configGrid.add(urlLbl);
        configGrid.add(baseUrlField);
        configGrid.add(keyLbl);
        configGrid.add(apiKeyField);

        aiModelCard.add(configGrid);
        aiModelCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // Provider change auto-populates defaults
        providerCombo.addActionListener(e -> {
            int idx = providerCombo.getSelectedIndex();
            if (idx == 0) { // OpenAI
                modelField.setText("gpt-4o-mini");
                baseUrlField.setText("https://api.openai.com/v1/chat/completions");
            } else if (idx == 1) { // Groq
                modelField.setText("llama-3.3-70b-versatile");
                baseUrlField.setText("https://api.groq.com/openai/v1/chat/completions");
            } else if (idx == 2) { // OpenRouter
                modelField.setText("meta-llama/llama-3.3-70b-instruct");
                baseUrlField.setText("https://openrouter.ai/api/v1/chat/completions");
            } else if (idx == 3) { // Ollama
                modelField.setText("llama3.2");
                baseUrlField.setText("http://localhost:11434/v1/chat/completions");
            }
        });

        // Online Preference Checkbox
        preferOnlineCheck = new JCheckBox("Prefer Online AI for all queries (Skip local KB check)", current.isPreferOnlineAi());
        preferOnlineCheck.setOpaque(false);
        preferOnlineCheck.setFont(ThemeManager.getInstance().getRegularFont());
        preferOnlineCheck.setForeground(palette.textPrimary);
        aiModelCard.add(preferOnlineCheck);
        aiModelCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // Action Buttons Row: Save AI Config & Test Connection
        JPanel aiBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        aiBtnRow.setOpaque(false);

        JButton saveAiBtn = new JButton("Save AI Configuration");
        saveAiBtn.setFont(ThemeManager.getInstance().getRegularFont());
        saveAiBtn.addActionListener(e -> {
            current.setLlmProvider((String) providerCombo.getSelectedItem());
            current.setLlmModel(modelField.getText().trim());
            current.setLlmBaseUrl(baseUrlField.getText().trim());
            current.setLlmApiKeyOverride(new String(apiKeyField.getPassword()).trim());
            current.setPreferOnlineAi(preferOnlineCheck.isSelected());
            settingsService.updateSettings(current);

            boolean cfg = llmService.isConfigured();
            aiStatusLabel.setText(cfg ? "● Online (AI Connected)" : "○ Offline (Knowledge Base Mode)");
            aiStatusLabel.setForeground(cfg ? new Color(16, 185, 129) : new Color(245, 158, 11));
            aiSourceLabel.setText("Key: " + llmService.getKeySource() + " [" + llmService.getMaskedApiKey() + "]");

            JOptionPane.showMessageDialog(this,
                    "AI Configuration saved successfully!\nKey Source: " + llmService.getKeySource(),
                    "Configuration Saved", JOptionPane.INFORMATION_MESSAGE);
        });

        testConnectionBtn = new JButton("Test AI Connection");
        testConnectionBtn.setFont(ThemeManager.getInstance().getRegularFont());
        testConnectionBtn.addActionListener(e -> {
            // Apply current textfield values temporarily for testing
            current.setLlmModel(modelField.getText().trim());
            current.setLlmBaseUrl(baseUrlField.getText().trim());
            current.setLlmApiKeyOverride(new String(apiKeyField.getPassword()).trim());
            settingsService.updateSettings(current);

            testConnectionBtn.setEnabled(false);
            testConnectionBtn.setText("Testing...");

            SwingWorker<LlmService.LlmResponse, Void> testWorker = new SwingWorker<>() {
                @Override
                protected LlmService.LlmResponse doInBackground() {
                    return llmService.testConnection();
                }

                @Override
                protected void done() {
                    testConnectionBtn.setEnabled(true);
                    testConnectionBtn.setText("Test AI Connection");
                    try {
                        LlmService.LlmResponse resp = get();
                        if (resp.isSuccess()) {
                            boolean cfg = llmService.isConfigured();
                            aiStatusLabel.setText(cfg ? "● Online (AI Connected)" : "○ Offline (Knowledge Base Mode)");
                            aiStatusLabel.setForeground(new Color(16, 185, 129));
                            aiSourceLabel.setText("Key: " + llmService.getKeySource() + " [" + llmService.getMaskedApiKey() + "]");

                            JOptionPane.showMessageDialog(SettingsPanel.this,
                                    "Success! RAJ AI connected to " + resp.getModelUsed() + " in " + resp.getLatencyMs() + "ms.\nResponse: " + resp.getContent(),
                                    "Connection Test Passed", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(SettingsPanel.this,
                                    "Connection Failed:\n" + resp.getErrorMessage(),
                                    "Connection Test Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(SettingsPanel.this,
                                "Test execution error: " + ex.getMessage(),
                                "Test Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            testWorker.execute();
        });

        aiBtnRow.add(saveAiBtn);
        aiBtnRow.add(testConnectionBtn);
        aiModelCard.add(aiBtnRow);
        aiModelCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // Setup Guide snippet
        String guideHtml = "<html><body style='width: 520px; font-family: Segoe UI; font-size: 11px;'>"
                + "<p><b>Environment Variable Setup Guide:</b></p>"
                + "<p>• <b>Windows:</b> <code>setx AI_API_KEY \"your_key\"</code> or <code>setx OPENAI_API_KEY \"your_key\"</code></p>"
                + "<p>• <b>Groq (Free):</b> <code>setx GROQ_API_KEY \"gsk_...\"</code> (fastest inference)</p>"
                + "<p>• Or type your key in <i>Manual API Key</i> above and click <i>Save AI Configuration</i>.</p>"
                + "</body></html>";
        JLabel guideLabel = new JLabel(guideHtml);
        guideLabel.setForeground(palette.textMuted);
        aiModelCard.add(guideLabel);

        container.add(aiModelCard);
        container.add(Box.createRigidArea(new Dimension(0, 16)));

        // 2. APPEARANCE CARD
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

        // 3. NLP MATCHING & CHAT PREFERENCES CARD
        JPanel nlpCard = createCard("Local NLP Matching & Chat Preferences");

        JPanel threshRow = new JPanel(new BorderLayout(12, 0));
        threshRow.setOpaque(false);

        int currentPct = (int) Math.round(current.getConfidenceThreshold() * 100);
        thresholdLabel = new JLabel("Local Knowledge Base Match Threshold: " + currentPct + "%");
        thresholdLabel.setFont(ThemeManager.getInstance().getRegularFont());
        thresholdLabel.setForeground(palette.textPrimary);

        thresholdSlider = new JSlider(20, 80, currentPct);
        thresholdSlider.setOpaque(false);
        thresholdSlider.setMajorTickSpacing(20);
        thresholdSlider.setMinorTickSpacing(5);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.addChangeListener(e -> {
            int val = thresholdSlider.getValue();
            thresholdLabel.setText("Local Knowledge Base Match Threshold: " + val + "%");
            settingsService.setConfidenceThreshold(val / 100.0);
        });

        threshRow.add(thresholdLabel, BorderLayout.NORTH);
        threshRow.add(thresholdSlider, BorderLayout.CENTER);

        nlpCard.add(threshRow);
        nlpCard.add(Box.createRigidArea(new Dimension(0, 10)));

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

        nlpCard.add(autoScrollCheck);
        nlpCard.add(timestampsCheck);

        container.add(nlpCard);
        container.add(Box.createRigidArea(new Dimension(0, 16)));

        // 4. DATA & STORAGE MANAGEMENT CARD
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

        // 5. ABOUT CARD
        JPanel aboutCard = createCard("About RAJ AI");

        String infoHtml = "<html><body style='width: 520px; font-family: Segoe UI;'>"
                + "<p><b>RAJ AI — General-Purpose Desktop AI Assistant</b></p>"
                + "<p>A modern desktop AI companion designed and implemented for the <b>CodeAlpha Java Programming Internship (Task 3)</b>.</p>"
                + "<br/>"
                + "<p>• <b>Application:</b> RAJ AI</p>"
                + "<p>• <b>Developer:</b> Kumar Raj</p>"
                + "<p>• <b>Runtime:</b> Java 17+ (Swing GUI & FlatLaf Engine)</p>"
                + "<p>• <b>Architecture:</b> Modular Layered Model-Service-UI Architecture</p>"
                + "<p>• <b>AI Engine:</b> Hybrid general-purpose LLM integration with local NLP intent classifier and semantic relevance scoring</p>"
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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel t = new JLabel(title);
        t.setFont(ThemeManager.getInstance().getBoldFont());
        t.setForeground(palette.textPrimary);
        card.add(t);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        return card;
    }
}
