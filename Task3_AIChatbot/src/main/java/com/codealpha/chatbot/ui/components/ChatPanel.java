package com.codealpha.chatbot.ui.components;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.Conversation;
import com.codealpha.chatbot.model.Role;
import com.codealpha.chatbot.service.ChatbotEngine;
import com.codealpha.chatbot.service.ConversationService;
import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Primary chat interface featuring attractive empty state with question cards,
 * message bubble rendering, multiline input, typing indicator, and background processing.
 */
public class ChatPanel extends JPanel {

    private final ConversationService conversationService;
    private final ChatbotEngine chatbotEngine;
    private final Runnable onConversationUpdated;

    private JPanel messageListPanel;
    private JScrollPane scrollPane;
    private JTextArea inputArea;
    private JButton sendButton;
    private TypingIndicator typingIndicator;
    private JPanel emptyStatePanel;

    private boolean isProcessing = false;

    public ChatPanel(ConversationService conversationService,
                     ChatbotEngine chatbotEngine,
                     Runnable onConversationUpdated) {
        this.conversationService = conversationService;
        this.chatbotEngine = chatbotEngine;
        this.onConversationUpdated = onConversationUpdated;

        setLayout(new BorderLayout());
        setOpaque(false);

        buildUI();
        refreshMessages();
    }

    private void buildUI() {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        // 1. MESSAGES VIEWPORT
        messageListPanel = new JPanel();
        messageListPanel.setLayout(new BoxLayout(messageListPanel, BoxLayout.Y_AXIS));
        messageListPanel.setOpaque(false);
        messageListPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        typingIndicator = new TypingIndicator();
        typingIndicator.setAlignmentX(Component.LEFT_ALIGNMENT);

        scrollPane = new JScrollPane(messageListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 2. BOTTOM INPUT AREA
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 16, 20));

        JPanel inputCard = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.setRenderingHints(g2);
                ColorPalette p = ThemeManager.getInstance().getPalette();
                g2.setColor(p.cardBackground);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(p.border);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        inputCard.setOpaque(false);
        inputCard.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 12));

        inputArea = new JTextArea();
        inputArea.setRows(2);
        inputArea.setFont(ThemeManager.getInstance().getRegularFont());
        inputArea.setForeground(palette.textPrimary);
        inputArea.setCaretColor(palette.accentPrimary);
        inputArea.setOpaque(false);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        // Enter sends message, Shift+Enter inserts newline
        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown()) {
                        inputArea.append("\n");
                    } else {
                        e.consume();
                        sendMessage(inputArea.getText());
                    }
                }
            }
        });

        sendButton = new JButton(UiUtils.createSendIcon(16, Color.WHITE)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.setRenderingHints(g2);
                ColorPalette p = ThemeManager.getInstance().getPalette();
                g2.setColor(p.accentPrimary);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        sendButton.setPreferredSize(new Dimension(38, 38));
        sendButton.setFocusPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setToolTipText("Send Message (Enter)");
        sendButton.addActionListener(e -> sendMessage(inputArea.getText()));

        inputCard.add(inputArea, BorderLayout.CENTER);
        inputCard.add(sendButton, BorderLayout.EAST);

        bottomContainer.add(inputCard, BorderLayout.CENTER);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);
    }

    public void refreshMessages() {
        messageListPanel.removeAll();
        Conversation conv = conversationService.getActiveConversation();

        if (conv == null || conv.getMessages().isEmpty()) {
            buildWelcomeState();
        } else {
            for (ChatMessage msg : conv.getMessages()) {
                MessageBubble bubble = new MessageBubble(msg,
                        this::sendMessage,
                        () -> regenerateLastResponse(conv));
                bubble.setAlignmentX(Component.CENTER_ALIGNMENT);
                messageListPanel.add(bubble);
            }

            if (isProcessing) {
                JPanel indWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
                indWrapper.setOpaque(false);
                indWrapper.add(typingIndicator);
                indWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
                messageListPanel.add(indWrapper);
            }
        }

        messageListPanel.revalidate();
        messageListPanel.repaint();
        scrollToBottom();
    }

    private void buildWelcomeState() {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        emptyStatePanel = new JPanel();
        emptyStatePanel.setLayout(new BoxLayout(emptyStatePanel, BoxLayout.Y_AXIS));
        emptyStatePanel.setOpaque(false);
        emptyStatePanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
        emptyStatePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLabel = new JLabel(UiUtils.createRobotIcon(54, palette.accentPrimary));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Welcome to CodeAlpha AI Assistant");
        titleLabel.setFont(ThemeManager.getInstance().getLargeTitleFont());
        titleLabel.setForeground(palette.textPrimary);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Your intelligent companion for Java programming, OOP mastery, and internship guidance.");
        subtitleLabel.setFont(ThemeManager.getInstance().getRegularFont());
        subtitleLabel.setForeground(palette.textSecondary);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyStatePanel.add(iconLabel);
        emptyStatePanel.add(Box.createRigidArea(new Dimension(0, 14)));
        emptyStatePanel.add(titleLabel);
        emptyStatePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        emptyStatePanel.add(subtitleLabel);
        emptyStatePanel.add(Box.createRigidArea(new Dimension(0, 28)));

        // 2x3 Grid of Suggested Question Cards
        JPanel cardsGrid = new JPanel(new GridLayout(3, 2, 12, 12));
        cardsGrid.setOpaque(false);
        cardsGrid.setMaximumSize(new Dimension(640, 220));

        cardsGrid.add(createPromptCard("OOP Pillars", "Explain OOP and its 4 pillars"));
        cardsGrid.add(createPromptCard("Polymorphism", "What is Polymorphism in Java?"));
        cardsGrid.add(createPromptCard("Classes & Objects", "Explain Java classes and objects"));
        cardsGrid.add(createPromptCard("Exception Handling", "How does Exception Handling work?"));
        cardsGrid.add(createPromptCard("Collections", "Explain Java Collections Framework"));
        cardsGrid.add(createPromptCard("CodeAlpha Tasks", "How do I succeed in CodeAlpha Internship?"));

        emptyStatePanel.add(cardsGrid);
        messageListPanel.add(emptyStatePanel);
    }

    private JPanel createPromptCard(String title, String query) {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        JPanel card = new JPanel(new BorderLayout(8, 4)) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                    @Override
                    public void mouseClicked(MouseEvent e) { sendMessage(query); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.setRenderingHints(g2);
                ColorPalette p = ThemeManager.getInstance().getPalette();
                g2.setColor(hovered ? p.cardBackgroundHover : p.cardBackground);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(hovered ? p.accentPrimary : p.border);
                g2.setStroke(new BasicStroke(hovered ? 1.5f : 1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ThemeManager.getInstance().getBoldFont());
        titleLbl.setForeground(palette.accentPrimary);

        JLabel queryLbl = new JLabel(query);
        queryLbl.setFont(ThemeManager.getInstance().getSmallFont());
        queryLbl.setForeground(palette.textSecondary);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(queryLbl, BorderLayout.CENTER);
        return card;
    }

    public void sendMessage(String text) {
        if (text == null || text.trim().isBlank() || isProcessing) return;
        String userQuery = text.trim();
        inputArea.setText("");

        Conversation activeConv = conversationService.getActiveConversation();
        ChatMessage userMsg = ChatMessage.user(activeConv.getId(), userQuery);
        conversationService.addMessage(userMsg);

        isProcessing = true;
        typingIndicator.start();
        refreshMessages();

        if (onConversationUpdated != null) {
            onConversationUpdated.run();
        }

        // Background worker to prevent any UI freezing
        SwingWorker<ChatMessage, Void> worker = new SwingWorker<>() {
            @Override
            protected ChatMessage doInBackground() throws Exception {
                // Natural simulated cognitive delay
                Thread.sleep(220);
                return chatbotEngine.processQuery(activeConv.getId(), userQuery);
            }

            @Override
            protected void done() {
                try {
                    ChatMessage assistantResponse = get();
                    conversationService.addMessage(assistantResponse);
                } catch (Exception e) {
                    ChatMessage err = ChatMessage.assistant(activeConv.getId(),
                            "An unexpected error occurred while processing: " + e.getMessage(),
                            "Error", 0.0, "System", 1);
                    conversationService.addMessage(err);
                } finally {
                    isProcessing = false;
                    typingIndicator.stop();
                    refreshMessages();
                    if (onConversationUpdated != null) {
                        onConversationUpdated.run();
                    }
                    inputArea.requestFocusInWindow();
                }
            }
        };
        worker.execute();
    }

    private void regenerateLastResponse(Conversation conv) {
        List<ChatMessage> msgs = conv.getMessages();
        if (msgs.isEmpty() || isProcessing) return;

        // Find last user prompt
        String lastPrompt = null;
        for (int i = msgs.size() - 1; i >= 0; i--) {
            if (msgs.get(i).getRole() == Role.USER) {
                lastPrompt = msgs.get(i).getContent();
                break;
            }
        }

        if (lastPrompt != null) {
            sendMessage(lastPrompt);
        }
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
}
