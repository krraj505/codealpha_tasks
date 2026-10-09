package com.codealpha.chatbot.ui.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.*;

/**
 * UI utility providing procedural vector icons, anti-aliasing hints, and rounded borders.
 */
public class UiUtils {

    public static void setRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    public static Border createRoundedBorder(Color color, int radius, int thickness) {
        return new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                setRenderingHints(g2);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(thickness));
                g2.drawRoundRect(x + thickness / 2, y + thickness / 2,
                        width - thickness, height - thickness, radius, radius);
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        };
    }

    // ==========================================
    // PROCEDURAL VECTOR ICONS
    // ==========================================

    public static Icon createChatIcon(int size, Color color) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f));
            RoundRectangle2D rect = new RoundRectangle2D.Float(2, 2, w - 4, h - 7, 6, 6);
            g2.draw(rect);
            Path2D tail = new Path2D.Float();
            tail.moveTo(5, h - 5);
            tail.lineTo(2, h - 1);
            tail.lineTo(9, h - 5);
            g2.draw(tail);
        });
    }

    public static Icon createDashboardIcon(int size, Color color) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            int halfW = (w - 6) / 2;
            int halfH = (h - 6) / 2;
            g2.drawRoundRect(2, 2, halfW, halfH, 3, 3);
            g2.drawRoundRect(4 + halfW, 2, halfW, halfH, 3, 3);
            g2.drawRoundRect(2, 4 + halfH, halfW, halfH, 3, 3);
            g2.drawRoundRect(4 + halfW, 4 + halfH, halfW, halfH, 3, 3);
        });
    }

    public static Icon createKnowledgeIcon(int size, Color color) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f));
            // Open book shape
            Path2D book = new Path2D.Float();
            book.moveTo(w / 2f, 4);
            book.lineTo(2, 6);
            book.lineTo(2, h - 3);
            book.lineTo(w / 2f, h - 5);
            book.lineTo(w - 2, h - 3);
            book.lineTo(w - 2, 6);
            book.closePath();
            g2.draw(book);
            g2.drawLine(w / 2, 4, w / 2, h - 5);
        });
    }

    public static Icon createSettingsIcon(int size, Color color) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawOval(w / 4, h / 4, w / 2, h / 2);
            // Outer gear notches
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4;
                int x1 = (int) (w / 2 + (w / 3.0) * Math.cos(angle));
                int y1 = (int) (h / 2 + (h / 3.0) * Math.sin(angle));
                int x2 = (int) (w / 2 + (w / 2.2) * Math.cos(angle));
                int y2 = (int) (h / 2 + (h / 2.2) * Math.sin(angle));
                g2.drawLine(x1, y1, x2, y2);
            }
        });
    }

    public static Icon createPlusIcon(int size, Color color) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(w / 2, 3, w / 2, h - 3);
            g2.drawLine(3, h / 2, w - 3, h / 2);
        });
    }

    public static Icon createSendIcon(int size, Color color) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            Path2D plane = new Path2D.Float();
            plane.moveTo(2, 2);
            plane.lineTo(w - 2, h / 2f);
            plane.lineTo(2, h - 2);
            plane.lineTo(w / 3f, h / 2f);
            plane.closePath();
            g2.fill(plane);
        });
    }

    public static Icon createSearchIcon(int size, Color color) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f));
            int r = w / 2 - 2;
            g2.drawOval(2, 2, r, r);
            g2.drawLine(r - 1, r - 1, w - 2, h - 2);
        });
    }

    public static Icon createPinIcon(int size, Color color, boolean filled) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            if (filled) {
                g2.fillOval(w / 4, 2, w / 2, h / 3);
                g2.fillRect(w / 3, h / 3, w / 3, h / 3);
            } else {
                g2.drawOval(w / 4, 2, w / 2, h / 3);
                g2.drawRect(w / 3, h / 3, w / 3, h / 3);
            }
            g2.drawLine(w / 2, 2 * h / 3, w / 2, h - 2);
        });
    }

    public static Icon createCopyIcon(int size, Color color) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            // Back page
            g2.drawRoundRect(w / 3, 2, w / 2, h / 2 + 2, 2, 2);
            // Front page
            g2.drawRoundRect(2, h / 4, w / 2, h / 2 + 2, 2, 2);
        });
    }

    public static Icon createTrashIcon(int size, Color color) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            // Lid
            g2.drawLine(2, 4, w - 2, 4);
            g2.drawLine(w / 3, 2, 2 * w / 3, 2);
            // Can
            g2.drawRoundRect(4, 4, w - 8, h - 6, 2, 2);
            g2.drawLine(w / 2, 7, w / 2, h - 4);
        });
    }

    public static Icon createRajAiLogo(int size, Color primary, Color accent) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            float pad = Math.max(1.5f, w * 0.05f);
            float innerW = w - (2 * pad);
            float innerH = h - (2 * pad);

            // Sleek rounded container background with gradient
            RoundRectangle2D bg = new RoundRectangle2D.Float(pad, pad, innerW, innerH, w * 0.28f, h * 0.28f);
            GradientPaint gp = new GradientPaint(
                    pad, pad, primary,
                    w - pad, h - pad, accent != null ? accent : primary
            );
            g2.setPaint(gp);
            g2.fill(bg);

            // Subtle border glow
            g2.setColor(new Color(255, 255, 255, 60));
            g2.setStroke(new BasicStroke(Math.max(1f, w * 0.04f)));
            g2.draw(bg);

            // Stylized 'R' Lettermark
            g2.setColor(Color.WHITE);
            float strokeW = Math.max(1.8f, w * 0.11f);
            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            float x0 = w * 0.31f;
            float x1 = w * 0.62f;
            float y0 = h * 0.26f;
            float yMid = h * 0.54f;
            float yBottom = h * 0.74f;

            // Vertical spine of 'R'
            g2.draw(new Line2D.Float(x0, y0, x0, yBottom));

            // Upper bowl of 'R'
            Path2D bowl = new Path2D.Float();
            bowl.moveTo(x0, y0);
            bowl.curveTo(x1 + (w * 0.06f), y0, x1 + (w * 0.06f), yMid, x0, yMid);
            g2.draw(bowl);

            // Diagonal leg of 'R'
            float legStartX = x0 + (w * 0.04f);
            float legStartY = yMid - (h * 0.02f);
            g2.draw(new Line2D.Float(legStartX, legStartY, x1, yBottom));

            // AI Accent dot on top right
            float dotR = Math.max(2.2f, w * 0.07f);
            g2.setColor(new Color(255, 255, 255, 230));
            g2.fill(new Ellipse2D.Float(w * 0.71f - dotR / 2f, h * 0.26f - dotR / 2f, dotR, dotR));
        });
    }

    public static Icon createRajAiLogo(int size) {
        return createRajAiLogo(size, new Color(0x3B, 0x82, 0xF6), new Color(0x8B, 0x5C, 0xF6));
    }

    public static Icon createRobotIcon(int size, Color color) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f));
            // Antenna
            g2.drawLine(w / 2, 1, w / 2, 4);
            g2.fillOval(w / 2 - 1, 1, 3, 3);
            // Head
            g2.drawRoundRect(3, 4, w - 6, h - 7, 5, 5);
            // Eyes
            g2.fillOval(w / 3 - 2, h / 2 - 2, 4, 4);
            g2.fillOval(2 * w / 3 - 2, h / 2 - 2, 4, 4);
            // Mouth line
            g2.drawLine(w / 3, h - 6, 2 * w / 3, h - 6);
        });
    }

    public static Icon createThemeToggleIcon(int size, boolean isDark, Color color) {
        return new ProceduralIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            if (isDark) {
                // Moon
                Area moon = new Area(new Ellipse2D.Float(2, 2, w - 4, h - 4));
                Area cut = new Area(new Ellipse2D.Float(w / 3f, 1, w - 4, h - 4));
                moon.subtract(cut);
                g2.fill(moon);
            } else {
                // Sun
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(w / 4, h / 4, w / 2, h / 2);
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    int x1 = (int) (w / 2 + (w / 3.0) * Math.cos(angle));
                    int y1 = (int) (h / 2 + (h / 3.0) * Math.sin(angle));
                    int x2 = (int) (w / 2 + (w / 2.1) * Math.cos(angle));
                    int y2 = (int) (h / 2 + (h / 2.1) * Math.sin(angle));
                    g2.drawLine(x1, y1, x2, y2);
                }
            }
        });
    }

    @FunctionalInterface
    public interface IconPainter {
        void paint(Graphics2D g2, int width, int height);
    }

    private static class ProceduralIcon implements Icon {
        private final int width;
        private final int height;
        private final IconPainter painter;

        public ProceduralIcon(int width, int height, IconPainter painter) {
            this.width = width;
            this.height = height;
            this.painter = painter;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            setRenderingHints(g2);
            g2.translate(x, y);
            painter.paint(g2, width, height);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }
}
