package com.codealpha.chatbot.ui.components;

import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Polished KPI metric card with custom painting, icon badge, and hover glow.
 */
public class KpiCard extends JPanel {

    private final String title;
    private String value;
    private final Icon icon;
    private final Color accentColor;
    private boolean isHovered = false;

    public KpiCard(String title, String value, Icon icon, Color accentColor) {
        this.title = title;
        this.value = value;
        this.icon = icon;
        this.accentColor = accentColor;

        setOpaque(false);
        setPreferredSize(new Dimension(210, 110));
        setMinimumSize(new Dimension(180, 100));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public void setValue(String value) {
        this.value = value;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        UiUtils.setRenderingHints(g2);

        ColorPalette palette = ThemeManager.getInstance().getPalette();
        int w = getWidth();
        int h = getHeight();

        // Background
        g2.setColor(isHovered ? palette.cardBackgroundHover : palette.cardBackground);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);

        // Border with accent highlight on hover
        g2.setColor(isHovered ? accentColor : palette.border);
        g2.setStroke(new BasicStroke(isHovered ? 1.5f : 1.0f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

        // Icon badge background circle
        int iconCircleR = 36;
        int iconX = 16;
        int iconY = 16;
        Color badgeBg = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 28);
        g2.setColor(badgeBg);
        g2.fillOval(iconX, iconY, iconCircleR, iconCircleR);

        // Paint icon in center of badge
        if (icon != null) {
            int ix = iconX + (iconCircleR - icon.getIconWidth()) / 2;
            int iy = iconY + (iconCircleR - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g2, ix, iy);
        }

        // Title label
        g2.setColor(palette.textSecondary);
        g2.setFont(ThemeManager.getInstance().getSmallFont());
        g2.drawString(title, iconX + iconCircleR + 14, iconY + 16);

        // Value text
        g2.setColor(palette.textPrimary);
        g2.setFont(ThemeManager.getInstance().getLargeTitleFont());
        g2.drawString(value, iconX + iconCircleR + 14, iconY + 44);

        g2.dispose();
    }
}
