package com.codealpha.chatbot.ui;

import com.codealpha.chatbot.model.Conversation;
import com.codealpha.chatbot.model.UserSettings;
import com.codealpha.chatbot.service.*;
import com.codealpha.chatbot.storage.JsonStorageService;
import com.codealpha.chatbot.ui.components.*;
import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Main application window coordinating layout, sidebar navigation,
 * workspace view switching, and collapsible context panel.
 */
public class MainFrame extends JFrame {

    private final JsonStorageService storageService;
    private final SettingsService settingsService;
    private final KnowledgeBaseService kbService;
    private final ConversationService conversationService;
    private final ChatbotEngine chatbotEngine;
    private final AnalyticsService analyticsService;

    private SidebarPanel sidebarPanel;
    private CardLayout workspaceCardLayout;
    private JPanel workspacePanel;

    private ChatPanel chatPanel;
    private DashboardPanel dashboardPanel;
    private KnowledgeBasePanel kbPanel;
    private SettingsPanel settingsPanel;
    private ContextPanel contextPanel;

    private JButton contextToggleBtn;
    private boolean contextPanelVisible = true;

    public MainFrame() {
        super("CodeAlpha AI Chatbot | Java Assistant");

        // Initialize Data and Services
        this.storageService = new JsonStorageService();
        this.settingsService = new SettingsService(storageService);
        this.kbService = new KnowledgeBaseService(storageService);
        this.conversationService = new ConversationService(storageService);
        this.chatbotEngine = new ChatbotEngine(kbService, settingsService);
        this.analyticsService = new AnalyticsService(conversationService, kbService);

        // Initialize Theme
        ThemeManager.getInstance().init(settingsService.getSettings());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1020, 680));
        setPreferredSize(new Dimension(1240, 800));

        setAppIcon();
        buildUI();
        pack();
        setLocationRelativeTo(null);

        // Register reactive update listeners
        conversationService.addChangeListener(this::onDataChanged);
        kbService.addChangeListener(this::onDataChanged);
    }

    private void setAppIcon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        UiUtils.setRenderingHints(g2);
        Icon icon = UiUtils.createRobotIcon(30, new Color(0x3B, 0x82, 0xF6));
        icon.paintIcon(this, g2, 1, 1);
        g2.dispose();
        setIconImage(img);
    }

    private void buildUI() {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(palette.background);

        // 1. LEFT SIDEBAR
        sidebarPanel = new SidebarPanel(
                conversationService,
                this::switchView,
                this::handleNewChat,
                this::handleSelectConversation,
                this::toggleTheme
        );

        // 2. CENTRAL WORKSPACE
        workspaceCardLayout = new CardLayout();
        workspacePanel = new JPanel(workspaceCardLayout);
        workspacePanel.setOpaque(false);

        chatPanel = new ChatPanel(conversationService, chatbotEngine, this::onConversationUpdated);
        dashboardPanel = new DashboardPanel(analyticsService, this::switchView, this::handleNewChat);
        kbPanel = new KnowledgeBasePanel(kbService);
        settingsPanel = new SettingsPanel(settingsService, conversationService, kbService, storageService, this::onThemeApplied);

        workspacePanel.add(chatPanel, "CHAT");
        workspacePanel.add(dashboardPanel, "DASHBOARD");
        workspacePanel.add(kbPanel, "KNOWLEDGE");
        workspacePanel.add(settingsPanel, "SETTINGS");

        // 3. RIGHT CONTEXT PANEL
        contextPanel = new ContextPanel(
                conversationService,
                () -> {
                    conversationService.clearActiveConversationMessages();
                    chatPanel.refreshMessages();
                    contextPanel.updateMetadata();
                },
                this::handleNewChat
        );

        // Central Area with top toolbar containing Context Panel toggle button
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        topBar.setOpaque(false);

        contextToggleBtn = new JButton("Insights Panel »");
        contextToggleBtn.setFont(ThemeManager.getInstance().getSmallFont());
        contextToggleBtn.setForeground(palette.textSecondary);
        contextToggleBtn.setFocusPainted(false);
        contextToggleBtn.setContentAreaFilled(false);
        contextToggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        contextToggleBtn.setBorder(BorderFactory.createCompoundBorder(
                UiUtils.createRoundedBorder(palette.border, 8, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        contextToggleBtn.addActionListener(e -> toggleContextPanel());
        topBar.add(contextToggleBtn);

        centerContainer.add(topBar, BorderLayout.NORTH);
        centerContainer.add(workspacePanel, BorderLayout.CENTER);

        root.add(sidebarPanel, BorderLayout.WEST);
        root.add(centerContainer, BorderLayout.CENTER);
        root.add(contextPanel, BorderLayout.EAST);

        setContentPane(root);
    }

    public void switchView(String viewKey) {
        workspaceCardLayout.show(workspacePanel, viewKey);
        sidebarPanel.setActiveView(viewKey);
        if ("DASHBOARD".equalsIgnoreCase(viewKey)) {
            dashboardPanel.refreshAnalytics();
        } else if ("KNOWLEDGE".equalsIgnoreCase(viewKey)) {
            kbPanel.refreshCategories();
            kbPanel.refreshTable();
        }
    }

    private void handleNewChat() {
        Conversation conv = conversationService.createNewConversation("New Conversation");
        conversationService.setActiveConversation(conv);
        chatPanel.refreshMessages();
        contextPanel.updateMetadata();
        sidebarPanel.refreshHistoryList();
        switchView("CHAT");
    }

    private void handleSelectConversation(Conversation conv) {
        conversationService.setActiveConversation(conv);
        chatPanel.refreshMessages();
        contextPanel.updateMetadata();
        sidebarPanel.refreshHistoryList();
        switchView("CHAT");
    }

    private void toggleContextPanel() {
        contextPanelVisible = !contextPanelVisible;
        contextPanel.setVisible(contextPanelVisible);
        contextToggleBtn.setText(contextPanelVisible ? "Insights Panel »" : "« Insights Panel");
        revalidate();
        repaint();
    }

    private void toggleTheme() {
        UserSettings settings = settingsService.getSettings();
        boolean newDark = !settings.isDarkTheme();
        settings.setTheme(newDark ? "DARK" : "LIGHT");
        settingsService.updateSettings(settings);
        onThemeApplied();
    }

    private void onThemeApplied() {
        boolean dark = settingsService.getSettings().isDarkTheme();
        ThemeManager.getInstance().setTheme(dark, this);
        ColorPalette palette = ThemeManager.getInstance().getPalette();
        getContentPane().setBackground(palette.background);
        revalidate();
        repaint();
    }

    private void onConversationUpdated() {
        contextPanel.updateMetadata();
        sidebarPanel.refreshHistoryList();
        dashboardPanel.refreshAnalytics();
    }

    private void onDataChanged() {
        sidebarPanel.refreshHistoryList();
        contextPanel.updateMetadata();
        dashboardPanel.refreshAnalytics();
    }
}
