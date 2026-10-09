package com.codealpha.chatbot.ui.components;

import com.codealpha.chatbot.model.Conversation;
import com.codealpha.chatbot.service.ConversationService;
import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern Left Sidebar containing Application branding, New Chat button,
 * view navigation pills, conversation search, pinned items, and history list.
 */
public class SidebarPanel extends JPanel {

    private final ConversationService conversationService;
    private final Consumer<String> onNavigate;
    private final Runnable onNewChat;
    private final Consumer<Conversation> onSelectConversation;
    private final Runnable onToggleTheme;

    private String activeView = "CHAT";
    private boolean collapsed = false;

    private JPanel navContainer;
    private JPanel historyListPanel;
    private JTextField searchField;
    private String searchQuery = "";

    public SidebarPanel(ConversationService conversationService,
                        Consumer<String> onNavigate,
                        Runnable onNewChat,
                        Consumer<Conversation> onSelectConversation,
                        Runnable onToggleTheme) {
        this.conversationService = conversationService;
        this.onNavigate = onNavigate;
        this.onNewChat = onNewChat;
        this.onSelectConversation = onSelectConversation;
        this.onToggleTheme = onToggleTheme;

        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(260, 0));
        setMinimumSize(new Dimension(68, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        buildUI();
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        setPreferredSize(new Dimension(collapsed ? 72 : 260, 0));
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setActiveView(String view) {
        this.activeView = view;
        refreshNav();
    }

    private void buildUI() {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        // 1. TOP HEADER: Logo & Brand
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        if (!collapsed) {
            JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            brandPanel.setOpaque(false);

            JLabel logo = new JLabel(UiUtils.createRajAiLogo(26));
            JLabel title = new JLabel("RAJ AI");
            title.setFont(ThemeManager.getInstance().getTitleFont());
            title.setForeground(palette.textPrimary);

            brandPanel.add(logo);
            brandPanel.add(title);
            top.add(brandPanel, BorderLayout.WEST);
        } else {
            JLabel logo = new JLabel(UiUtils.createRajAiLogo(32));
            logo.setHorizontalAlignment(SwingConstants.CENTER);
            top.add(logo, BorderLayout.CENTER);
        }

        // New Chat Button
        JButton newChatBtn = new JButton(collapsed ? "" : "New Chat", UiUtils.createPlusIcon(16, Color.WHITE)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.setRenderingHints(g2);
                ColorPalette p = ThemeManager.getInstance().getPalette();
                g2.setColor(p.accentPrimary);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        newChatBtn.setFont(ThemeManager.getInstance().getBoldFont());
        newChatBtn.setForeground(Color.WHITE);
        newChatBtn.setFocusPainted(false);
        newChatBtn.setContentAreaFilled(false);
        newChatBtn.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        newChatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newChatBtn.addActionListener(e -> {
            if (onNewChat != null) onNewChat.run();
        });

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.setBorder(BorderFactory.createEmptyBorder(12, 0, 10, 0));
        btnWrapper.add(newChatBtn, BorderLayout.CENTER);

        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setOpaque(false);
        headerSection.add(top, BorderLayout.NORTH);
        headerSection.add(btnWrapper, BorderLayout.SOUTH);

        // 2. NAVIGATION PILLS
        navContainer = new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setOpaque(false);
        refreshNav();

        // 3. MIDDLE: SEARCH & CONVERSATION HISTORY (Only if expanded)
        JPanel middle = new JPanel(new BorderLayout());
        middle.setOpaque(false);

        if (!collapsed) {
            // Search field
            JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
            searchPanel.setOpaque(false);
            searchPanel.setBorder(BorderFactory.createCompoundBorder(
                    UiUtils.createRoundedBorder(palette.border, 10, 1),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));

            JLabel searchIcon = new JLabel(UiUtils.createSearchIcon(14, palette.textMuted));
            searchField = new JTextField();
            searchField.setBorder(BorderFactory.createEmptyBorder());
            searchField.setOpaque(false);
            searchField.setForeground(palette.textPrimary);
            searchField.setCaretColor(palette.accentPrimary);
            searchField.setFont(ThemeManager.getInstance().getSmallFont());
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { updateSearch(); }
                @Override
                public void removeUpdate(DocumentEvent e) { updateSearch(); }
                @Override
                public void changedUpdate(DocumentEvent e) { updateSearch(); }
            });

            searchPanel.add(searchIcon, BorderLayout.WEST);
            searchPanel.add(searchField, BorderLayout.CENTER);

            JPanel midHeader = new JPanel(new BorderLayout());
            midHeader.setOpaque(false);
            midHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 8, 0));
            midHeader.add(searchPanel, BorderLayout.CENTER);

            // History List
            historyListPanel = new JPanel();
            historyListPanel.setLayout(new BoxLayout(historyListPanel, BoxLayout.Y_AXIS));
            historyListPanel.setOpaque(false);
            refreshHistoryList();

            JScrollPane historyScroll = new JScrollPane(historyListPanel);
            historyScroll.setBorder(BorderFactory.createEmptyBorder());
            historyScroll.setOpaque(false);
            historyScroll.getViewport().setOpaque(false);

            middle.add(midHeader, BorderLayout.NORTH);
            middle.add(historyScroll, BorderLayout.CENTER);
        }

        // 4. BOTTOM BAR: Theme Switcher & Collapse Toggle
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, palette.borderSubtle),
                BorderFactory.createEmptyBorder(8, 0, 0, 0)
        ));

        boolean isDark = ThemeManager.getInstance().getPalette().isDark;
        JButton themeBtn = new JButton(collapsed ? "" : (isDark ? "Light Mode" : "Dark Mode"),
                UiUtils.createThemeToggleIcon(16, isDark, palette.textSecondary));
        themeBtn.setFont(ThemeManager.getInstance().getSmallFont());
        themeBtn.setForeground(palette.textSecondary);
        themeBtn.setFocusPainted(false);
        themeBtn.setContentAreaFilled(false);
        themeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeBtn.addActionListener(e -> {
            if (onToggleTheme != null) onToggleTheme.run();
        });

        JButton collapseBtn = new JButton(collapsed ? "»" : "«");
        collapseBtn.setFont(ThemeManager.getInstance().getBoldFont());
        collapseBtn.setForeground(palette.textMuted);
        collapseBtn.setFocusPainted(false);
        collapseBtn.setContentAreaFilled(false);
        collapseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        collapseBtn.addActionListener(e -> setCollapsed(!collapsed));

        bottom.add(themeBtn, BorderLayout.WEST);
        bottom.add(collapseBtn, BorderLayout.EAST);

        // Assemble Top
        JPanel upper = new JPanel(new BorderLayout());
        upper.setOpaque(false);
        upper.add(headerSection, BorderLayout.NORTH);
        upper.add(navContainer, BorderLayout.SOUTH);

        add(upper, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void updateSearch() {
        if (searchField != null) {
            this.searchQuery = searchField.getText().trim();
            refreshHistoryList();
        }
    }

    private void refreshNav() {
        navContainer.removeAll();
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        navContainer.add(createNavItem("CHAT", "Chat Assistant", UiUtils.createChatIcon(16, palette.textSecondary)));
        navContainer.add(Box.createRigidArea(new Dimension(0, 4)));
        navContainer.add(createNavItem("DASHBOARD", "Analytics", UiUtils.createDashboardIcon(16, palette.textSecondary)));
        navContainer.add(Box.createRigidArea(new Dimension(0, 4)));
        navContainer.add(createNavItem("KNOWLEDGE", "Knowledge Base", UiUtils.createKnowledgeIcon(16, palette.textSecondary)));
        navContainer.add(Box.createRigidArea(new Dimension(0, 4)));
        navContainer.add(createNavItem("SETTINGS", "Settings", UiUtils.createSettingsIcon(16, palette.textSecondary)));

        navContainer.revalidate();
        navContainer.repaint();
    }

    private JPanel createNavItem(String viewKey, String labelText, Icon icon) {
        boolean active = activeView.equalsIgnoreCase(viewKey);
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        JPanel item = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                if (active) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    UiUtils.setRenderingHints(g2);
                    ColorPalette p = ThemeManager.getInstance().getPalette();
                    g2.setColor(new Color(p.accentPrimary.getRed(), p.accentPrimary.getGreen(), p.accentPrimary.getBlue(), 35));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.setColor(p.accentPrimary);
                    g2.fillRoundRect(0, 4, 3, getHeight() - 8, 2, 2);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        item.setOpaque(false);
        item.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        item.add(iconLabel, BorderLayout.WEST);

        if (!collapsed) {
            JLabel textLabel = new JLabel(labelText);
            textLabel.setFont(active ? ThemeManager.getInstance().getBoldFont() : ThemeManager.getInstance().getRegularFont());
            textLabel.setForeground(active ? palette.accentPrimary : palette.textSecondary);
            item.add(textLabel, BorderLayout.CENTER);
        }

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activeView = viewKey;
                refreshNav();
                if (onNavigate != null) onNavigate.accept(viewKey);
            }
        });

        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return item;
    }

    public void refreshHistoryList() {
        if (historyListPanel == null) return;
        historyListPanel.removeAll();
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        List<Conversation> list = searchQuery.isBlank() ?
                conversationService.getAllConversations() :
                conversationService.search(searchQuery);

        Conversation activeConv = conversationService.getActiveConversation();

        if (list.isEmpty()) {
            JLabel empty = new JLabel("No conversations");
            empty.setFont(ThemeManager.getInstance().getSmallFont());
            empty.setForeground(palette.textMuted);
            empty.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
            historyListPanel.add(empty);
        } else {
            for (Conversation conv : list) {
                boolean active = (activeConv != null && activeConv.getId().equals(conv.getId()));
                JPanel item = createHistoryItem(conv, active);
                historyListPanel.add(item);
                historyListPanel.add(Box.createRigidArea(new Dimension(0, 2)));
            }
        }

        historyListPanel.revalidate();
        historyListPanel.repaint();
    }

    private JPanel createHistoryItem(Conversation conv, boolean active) {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        JPanel panel = new JPanel(new BorderLayout(6, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                if (active) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    UiUtils.setRenderingHints(g2);
                    ColorPalette p = ThemeManager.getInstance().getPalette();
                    g2.setColor(p.cardBackgroundHover);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel titleLabel = new JLabel(conv.getTitle());
        titleLabel.setFont(ThemeManager.getInstance().getSmallFont());
        titleLabel.setForeground(active ? palette.accentPrimary : palette.textPrimary);
        panel.add(titleLabel, BorderLayout.CENTER);

        if (conv.isPinned()) {
            JLabel pinLabel = new JLabel(UiUtils.createPinIcon(12, palette.accentSecondary, true));
            panel.add(pinLabel, BorderLayout.EAST);
        }

        // Popup Context Menu
        JPopupMenu popup = new JPopupMenu();
        JMenuItem renameItem = new JMenuItem("Rename");
        renameItem.addActionListener(e -> {
            String newTitle = JOptionPane.showInputDialog(this, "Enter new conversation title:", conv.getTitle());
            if (newTitle != null && !newTitle.isBlank()) {
                conversationService.renameConversation(conv.getId(), newTitle.trim());
                refreshHistoryList();
            }
        });

        JMenuItem pinItem = new JMenuItem(conv.isPinned() ? "Unpin Conversation" : "Pin Conversation");
        pinItem.addActionListener(e -> {
            conversationService.togglePin(conv.getId());
            refreshHistoryList();
        });

        JMenuItem deleteItem = new JMenuItem("Delete Conversation");
        deleteItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete \"" + conv.getTitle() + "\"?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                conversationService.deleteConversation(conv.getId());
                refreshHistoryList();
            }
        });

        popup.add(renameItem);
        popup.add(pinItem);
        popup.addSeparator();
        popup.add(deleteItem);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popup.show(panel, e.getX(), e.getY());
                } else {
                    if (onSelectConversation != null) {
                        onSelectConversation.accept(conv);
                    }
                }
            }
        });

        return panel;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        UiUtils.setRenderingHints(g2);
        ColorPalette p = ThemeManager.getInstance().getPalette();
        g2.setColor(p.sidebarBackground);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(p.border);
        g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
        g2.dispose();
    }
}
