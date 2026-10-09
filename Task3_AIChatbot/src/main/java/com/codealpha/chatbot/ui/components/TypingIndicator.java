package com.codealpha.chatbot.ui.components;

import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Animated three-dot typing indicator displayed while the assistant is processing.
 */
public class TypingIndicator extends JPanel {

    private final Timer timer;
    private float phase = 0f;

    public TypingIndicator() {
        setOpaque(false);
        setPreferredSize(new Dimension(84, 38));
        setMaximumSize(new Dimension(84, 38));

        this.timer = new Timer(50, e -> {
            phase = (phase + 0.18f) % ((float) Math.PI * 2);
            repaint();
        });
    }

    public void start() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    public void stop() {
        if (timer.isRunning()) {
            timer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        UiUtils.setRenderingHints(g2);

        ColorPalette palette = ThemeManager.getInstance().getPalette();
        int w = getWidth();
        int h = getHeight();

        // Bubble background
        g2.setColor(palette.assistantBubble);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);

        g2.setColor(palette.border);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

        // Three animated bouncing/pulsing dots
        int dotRadius = 5;
        int spacing = 18;
        int startX = (w - (2 * spacing)) / 2;
        int centerY = h / 2;

        for (int i = 0; i < 3; i++) {
            float offset = (float) Math.sin(phase - (i * 0.8f));
            float y = centerY + (offset * 3.5f);
            float alpha = 0.35f + (0.65f * Math.max(0f, (offset + 1f) / 2f));

            Color dotColor = new Color(
                    palette.accentPrimary.getRed(),
                    palette.accentPrimary.getGreen(),
                    palette.accentPrimary.getBlue(),
                    (int) (alpha * 255)
            );

            g2.setColor(dotColor);
            g2.fill(new Ellipse2D.Float(startX + (i * spacing) - dotRadius / 2f, y - dotRadius / 2f, dotRadius, dotRadius));
        }

        g2.dispose();
    }
}
