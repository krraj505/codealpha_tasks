package com.codealpha.chatbot.ui.components;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.Conversation;
import com.codealpha.chatbot.model.Role;
import com.codealpha.chatbot.service.ConversationService;
import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Collapsible right context panel displaying real-time conversation metadata,
 * detected intent, confidence gauge, and topic summary.
 */
public class ContextPanel extends JPanel {

    private final ConversationService conversationService;
    private final Runnable onClearChat;
    private final Runnable onNewChat;

    private JLabel titleLabel;
    private JLabel intentBadge;
    private JProgressBar confidenceBar;
    private JLabel confidenceLabel;
    private JLabel categoryLabel;
    private JLabel messageCountLabel;
    private JTextArea summaryArea;

    public ContextPanel(ConversationService conversationService, Runnable onClearChat, Runnable onNewChat) {
        this.conversationService = conversationService;
        this.onClearChat = onClearChat;
        this.onNewChat = onNewChat;

        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(270, 0));
        setMinimumSize(new Dimension(220, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 12));

        buildUI();
        updateMetadata();
    }

    private void buildUI() {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel headerTitle = new JLabel("Context & Insights");
        headerTitle.setFont(ThemeManager.getInstance().getBoldFont());
        headerTitle.setForeground(palette.textPrimary);
        header.add(headerTitle, BorderLayout.WEST);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        container.add(header);
        container.add(Box.createRigidArea(new Dimension(0, 14)));

        // Session Card
        JPanel sessionCard = createSectionCard("Active Conversation");
        titleLabel = new JLabel("New Conversation");
        titleLabel.setFont(ThemeManager.getInstance().getBoldFont());
        titleLabel.setForeground(palette.textPrimary);

        messageCountLabel = new JLabel("0 messages");
        messageCountLabel.setFont(ThemeManager.getInstance().getSmallFont());
        messageCountLabel.setForeground(palette.textSecondary);

        sessionCard.add(titleLabel);
        sessionCard.add(Box.createRigidArea(new Dimension(0, 4)));
        sessionCard.add(messageCountLabel);
        container.add(sessionCard);
        container.add(Box.createRigidArea(new Dimension(0, 12)));

        // AI Metadata Card
        JPanel aiCard = createSectionCard("Last Detected Intent");
        intentBadge = new JLabel("None");
        intentBadge.setFont(ThemeManager.getInstance().getRegularFont());
        intentBadge.setForeground(palette.accentPrimary);

        confidenceLabel = new JLabel("Confidence: 0%");
        confidenceLabel.setFont(ThemeManager.getInstance().getSmallFont());
        confidenceLabel.setForeground(palette.textSecondary);

        confidenceBar = new JProgressBar(0, 100);
        confidenceBar.setValue(0);
        confidenceBar.setPreferredSize(new Dimension(200, 6));
        confidenceBar.setForeground(palette.accentPrimary);
        confidenceBar.setBorderPainted(false);

        categoryLabel = new JLabel("Category: General");
        categoryLabel.setFont(ThemeManager.getInstance().getSmallFont());
        categoryLabel.setForeground(palette.textMuted);

        aiCard.add(intentBadge);
        aiCard.add(Box.createRigidArea(new Dimension(0, 6)));
        aiCard.add(confidenceLabel);
        aiCard.add(Box.createRigidArea(new Dimension(0, 4)));
        aiCard.add(confidenceBar);
        aiCard.add(Box.createRigidArea(new Dimension(0, 6)));
        aiCard.add(categoryLabel);

        container.add(aiCard);
        container.add(Box.createRigidArea(new Dimension(0, 12)));

        // Summary Card
        JPanel summaryCard = createSectionCard("Topic Summary");
        summaryArea = new JTextArea("No recent activity.");
        summaryArea.setEditable(false);
        summaryArea.setFont(ThemeManager.getInstance().getSmallFont());
        summaryArea.setForeground(palette.textSecondary);
        summaryArea.setOpaque(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setRows(3);

        summaryCard.add(summaryArea);
        container.add(summaryCard);
        container.add(Box.createRigidArea(new Dimension(0, 12)));

        // Quick Actions
        JPanel actionCard = createSectionCard("Quick Actions");
        JButton clearBtn = new JButton("Clear Messages");
        clearBtn.setFont(ThemeManager.getInstance().getSmallFont());
        clearBtn.setForeground(palette.accentDanger);
        clearBtn.setFocusPainted(false);
        clearBtn.setContentAreaFilled(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.setBorder(BorderFactory.createCompoundBorder(
                UiUtils.createRoundedBorder(palette.border, 8, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        clearBtn.addActionListener(e -> {
            if (onClearChat != null) onClearChat.run();
        });

        JButton newChatBtn = new JButton("+ New Chat");
        newChatBtn.setFont(ThemeManager.getInstance().getSmallFont());
        newChatBtn.setForeground(palette.accentPrimary);
        newChatBtn.setFocusPainted(false);
        newChatBtn.setContentAreaFilled(false);
        newChatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newChatBtn.setBorder(BorderFactory.createCompoundBorder(
                UiUtils.createRoundedBorder(palette.border, 8, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        newChatBtn.addActionListener(e -> {
            if (onNewChat != null) onNewChat.run();
        });

        actionCard.add(newChatBtn);
        actionCard.add(Box.createRigidArea(new Dimension(0, 6)));
        actionCard.add(clearBtn);

        container.add(actionCard);
        container.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createSectionCard(String sectionHeader) {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.setRenderingHints(g2);
                ColorPalette p = ThemeManager.getInstance().getPalette();
                g2.setColor(p.cardBackground);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(p.border);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel head = new JLabel(sectionHeader.toUpperCase());
        head.setFont(ThemeManager.getInstance().getSmallFont());
        head.setForeground(palette.textMuted);
        card.add(head);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        return card;
    }

    public void updateMetadata() {
        Conversation conv = conversationService.getActiveConversation();
        if (conv == null) return;

        titleLabel.setText(conv.getTitle());
        messageCountLabel.setText(conv.getMessageCount() + " messages (" + conv.getUserMessageCount() + " user)");

        List<ChatMessage> msgs = conv.getMessages();
        ChatMessage lastAssistant = null;
        for (int i = msgs.size() - 1; i >= 0; i--) {
            if (msgs.get(i).getRole() == Role.ASSISTANT) {
                lastAssistant = msgs.get(i);
                break;
            }
        }

        if (lastAssistant != null) {
            String intent = lastAssistant.getDetectedIntent() != null ? lastAssistant.getDetectedIntent() : "General";
            intentBadge.setText(intent);

            int confPct = (int) Math.round(lastAssistant.getConfidenceScore() * 100);
            confidenceLabel.setText("Confidence: " + confPct + "%");
            confidenceBar.setValue(confPct);

            String cat = lastAssistant.getCategory() != null ? lastAssistant.getCategory() : "General";
            categoryLabel.setText("Category: " + cat);
            summaryArea.setText("Last topic: " + cat + "\nStatus: Responded in " + lastAssistant.getResponseTimeMs() + "ms");
        } else {
            intentBadge.setText("Ready for query");
            confidenceLabel.setText("Confidence: N/A");
            confidenceBar.setValue(0);
            categoryLabel.setText("Category: General");
            summaryArea.setText("Start a conversation to see detected intent and confidence.");
        }

        revalidate();
        repaint();
    }
}
