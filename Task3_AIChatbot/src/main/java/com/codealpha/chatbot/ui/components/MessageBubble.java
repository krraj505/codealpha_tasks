package com.codealpha.chatbot.ui.components;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.Role;
import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modern chat message bubble supporting code syntax blocks, metadata badges,
 * response copying, and interactive follow-up question chips.
 */
public class MessageBubble extends JPanel {

    private final ChatMessage message;
    private final Consumer<String> onSuggestionClick;
    private final Runnable onRegenerate;

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```([a-zA-Z0-9]*)\\r?\\n([\\s\\S]*?)```");

    public MessageBubble(ChatMessage message, Consumer<String> onSuggestionClick, Runnable onRegenerate) {
        this.message = message;
        this.onSuggestionClick = onSuggestionClick;
        this.onRegenerate = onRegenerate;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));

        if (message.getRole() == Role.USER) {
            buildUserLayout();
        } else {
            buildAssistantLayout();
        }
    }

    private void buildUserLayout() {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        JPanel outer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        outer.setOpaque(false);

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.setRenderingHints(g2);
                ColorPalette p = ThemeManager.getInstance().getPalette();

                // Gradient from electric blue to indigo
                GradientPaint gp = new GradientPaint(0, 0, p.userBubble, getWidth(), getHeight(), p.accentSecondary);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 16, 8, 16));

        JTextArea text = new JTextArea(message.getContent());
        text.setEditable(false);
        text.setFont(ThemeManager.getInstance().getRegularFont());
        text.setForeground(Color.WHITE);
        text.setOpaque(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setColumns(Math.min(42, Math.max(14, message.getContent().length())));

        JLabel time = new JLabel(message.getFormattedTime());
        time.setFont(ThemeManager.getInstance().getSmallFont());
        time.setForeground(new Color(255, 255, 255, 180));
        time.setHorizontalAlignment(SwingConstants.RIGHT);
        time.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        bubble.add(text, BorderLayout.CENTER);
        bubble.add(time, BorderLayout.SOUTH);

        outer.add(bubble);
        add(outer, BorderLayout.CENTER);
    }

    private void buildAssistantLayout() {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        // Assistant Avatar
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.setRenderingHints(g2);
                ColorPalette p = ThemeManager.getInstance().getPalette();

                g2.setColor(new Color(p.accentPrimary.getRed(), p.accentPrimary.getGreen(), p.accentPrimary.getBlue(), 35));
                g2.fillOval(0, 0, 32, 32);
                g2.setColor(p.accentPrimary);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(0, 0, 32, 32);

                Icon icon = UiUtils.createRobotIcon(18, p.accentPrimary);
                icon.paintIcon(this, g2, 7, 7);
                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(34, 34));
        row.add(avatarPanel, BorderLayout.WEST);

        // Card Container
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.setRenderingHints(g2);
                ColorPalette p = ThemeManager.getInstance().getPalette();

                g2.setColor(p.assistantBubble);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                g2.setColor(p.assistantBubbleBorder);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Metadata Header: Intent Badge + Latency Badge
        JPanel metaHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        metaHeader.setOpaque(false);
        metaHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (message.getDetectedIntent() != null) {
            String intentText = message.getDetectedIntent();
            if (message.getConfidenceScore() > 0) {
                intentText += String.format(" • %.0f%%", message.getConfidenceScore() * 100);
            }
            JLabel badge = createPillLabel(intentText, palette.accentPrimary);
            metaHeader.add(badge);
        }

        if (message.getResponseTimeMs() > 0) {
            JLabel timeBadge = createPillLabel(message.getResponseTimeMs() + "ms", palette.textMuted);
            metaHeader.add(timeBadge);
        }

        card.add(metaHeader);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        // Render Message Content with Code Blocks support
        renderContent(card, message.getContent());

        // Footer Actions: Copy Response + Regenerate + Timestamp
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        JButton copyBtn = new JButton("Copy", UiUtils.createCopyIcon(13, palette.textSecondary));
        copyBtn.setFont(ThemeManager.getInstance().getSmallFont());
        copyBtn.setForeground(palette.textSecondary);
        copyBtn.setFocusPainted(false);
        copyBtn.setContentAreaFilled(false);
        copyBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        copyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(message.getContent()), null);
            copyBtn.setText("Copied!");
            copyBtn.setForeground(palette.accentSuccess);
            Timer t = new Timer(1500, evt -> {
                copyBtn.setText("Copy");
                copyBtn.setForeground(palette.textSecondary);
            });
            t.setRepeats(false);
            t.start();
        });
        actions.add(copyBtn);

        if (onRegenerate != null) {
            JButton regenBtn = new JButton("Regenerate");
            regenBtn.setFont(ThemeManager.getInstance().getSmallFont());
            regenBtn.setForeground(palette.textSecondary);
            regenBtn.setFocusPainted(false);
            regenBtn.setContentAreaFilled(false);
            regenBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            regenBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            regenBtn.addActionListener(e -> onRegenerate.run());
            actions.add(regenBtn);
        }

        JLabel time = new JLabel(message.getFormattedTime());
        time.setFont(ThemeManager.getInstance().getSmallFont());
        time.setForeground(palette.textMuted);

        footer.add(actions, BorderLayout.WEST);
        footer.add(time, BorderLayout.EAST);
        card.add(footer);

        // Suggested follow-up chips
        List<String> suggestions = message.getSuggestedQuestions();
        if (suggestions != null && !suggestions.isEmpty() && onSuggestionClick != null) {
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            JPanel chipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
            chipPanel.setOpaque(false);
            chipPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            for (String suggestion : suggestions) {
                JButton chip = createSuggestionChip(suggestion);
                chipPanel.add(chip);
            }
            card.add(chipPanel);
        }

        row.add(card, BorderLayout.CENTER);
        add(row, BorderLayout.CENTER);
    }

    private void renderContent(JPanel container, String rawText) {
        ColorPalette palette = ThemeManager.getInstance().getPalette();
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(rawText);

        int lastIndex = 0;
        boolean hasCode = false;

        while (matcher.find()) {
            hasCode = true;
            String textBefore = rawText.substring(lastIndex, matcher.start()).trim();
            if (!textBefore.isEmpty()) {
                container.add(createTextBlock(textBefore, palette));
            }

            String lang = matcher.group(1);
            String code = matcher.group(2);
            CodeBlockView codeView = new CodeBlockView(code, lang);
            codeView.setAlignmentX(Component.LEFT_ALIGNMENT);
            container.add(codeView);

            lastIndex = matcher.end();
        }

        String remaining = rawText.substring(lastIndex).trim();
        if (!remaining.isEmpty() || !hasCode) {
            container.add(createTextBlock(rawText.substring(lastIndex), palette));
        }
    }

    private JTextArea createTextBlock(String text, ColorPalette palette) {
        JTextArea area = new JTextArea(text.trim());
        area.setEditable(false);
        area.setFont(ThemeManager.getInstance().getRegularFont());
        area.setForeground(palette.textPrimary);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        area.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));
        return area;
    }

    private JLabel createPillLabel(String text, Color accent) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getInstance().getSmallFont());
        l.setForeground(accent);
        l.setBorder(BorderFactory.createCompoundBorder(
                UiUtils.createRoundedBorder(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80), 10, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        return l;
    }

    private JButton createSuggestionChip(String question) {
        ColorPalette palette = ThemeManager.getInstance().getPalette();
        JButton btn = new JButton(question);
        btn.setFont(ThemeManager.getInstance().getSmallFont());
        btn.setForeground(palette.accentPrimary);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                UiUtils.createRoundedBorder(palette.border, 12, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        btn.addActionListener(e -> {
            if (onSuggestionClick != null) {
                onSuggestionClick.accept(question);
            }
        });
        return btn;
    }
}
