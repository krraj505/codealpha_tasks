package com.codealpha.chatbot.ui.components;

import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modern custom-painted Java2D chart component supporting:
 * - Line / Area Activity Chart
 * - Donut / Pie Chart
 * - Bar Chart
 * - Stacked Quality Meter
 */
public class ModernChartPanel extends JPanel {

    public enum ChartType {
        AREA_ACTIVITY,
        DONUT_INTENT,
        BAR_CATEGORY,
        STACKED_QUALITY
    }

    private final String title;
    private final ChartType chartType;
    private Map<String, Integer> data;

    private static final Color[] PALETTE_COLORS = new Color[]{
            new Color(0x3B, 0x82, 0xF6), // Blue
            new Color(0x8B, 0x5C, 0xF6), // Violet
            new Color(0x06, 0xB6, 0xD4), // Cyan
            new Color(0x10, 0xB9, 0x81), // Emerald
            new Color(0xF5, 0x9E, 0x0B), // Amber
            new Color(0xEC, 0x48, 0x99), // Pink
            new Color(0x63, 0x66, 0xF1)  // Indigo
    };

    public ModernChartPanel(String title, ChartType chartType) {
        this.title = title;
        this.chartType = chartType;
        setOpaque(false);
        setPreferredSize(new Dimension(340, 240));
        setMinimumSize(new Dimension(280, 200));
    }

    public void setData(Map<String, Integer> data) {
        this.data = data;
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

        // Draw card background
        g2.setColor(palette.cardBackground);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);

        g2.setColor(palette.border);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

        // Header Title
        g2.setColor(palette.textPrimary);
        g2.setFont(ThemeManager.getInstance().getBoldFont());
        g2.drawString(title, 16, 26);

        // Header line separator
        g2.setColor(palette.borderSubtle);
        g2.drawLine(16, 36, w - 16, 36);

        int contentX = 16;
        int contentY = 46;
        int contentW = w - 32;
        int contentH = h - 60;

        if (data == null || data.isEmpty() || isAllZero(data)) {
            drawEmptyState(g2, palette, contentX, contentY, contentW, contentH);
            g2.dispose();
            return;
        }

        switch (chartType) {
            case AREA_ACTIVITY -> drawAreaChart(g2, palette, contentX, contentY, contentW, contentH);
            case DONUT_INTENT -> drawDonutChart(g2, palette, contentX, contentY, contentW, contentH);
            case BAR_CATEGORY -> drawBarChart(g2, palette, contentX, contentY, contentW, contentH);
            case STACKED_QUALITY -> drawStackedQuality(g2, palette, contentX, contentY, contentW, contentH);
        }

        g2.dispose();
    }

    private boolean isAllZero(Map<String, Integer> map) {
        for (int v : map.values()) {
            if (v > 0) return false;
        }
        return true;
    }

    private void drawEmptyState(Graphics2D g2, ColorPalette palette, int x, int y, int w, int h) {
        g2.setColor(palette.textMuted);
        g2.setFont(ThemeManager.getInstance().getSmallFont());
        String msg = "No data recorded yet";
        FontMetrics fm = g2.getFontMetrics();
        int strW = fm.stringWidth(msg);
        g2.drawString(msg, x + (w - strW) / 2, y + h / 2);
    }

    // ==========================================
    // AREA / LINE CHART
    // ==========================================

    private void drawAreaChart(Graphics2D g2, ColorPalette palette, int x, int y, int w, int h) {
        List<String> keys = new ArrayList<>(data.keySet());
        List<Integer> values = new ArrayList<>(data.values());
        int n = keys.size();
        if (n == 0) return;

        int maxVal = 1;
        for (int v : values) {
            if (v > maxVal) maxVal = v;
        }

        int chartBottom = y + h - 22;
        int chartTop = y + 10;
        int chartH = chartBottom - chartTop;

        // Draw horizontal grid lines
        g2.setColor(palette.borderSubtle);
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0));
        g2.drawLine(x, chartBottom, x + w, chartBottom);
        g2.drawLine(x, chartBottom - chartH / 2, x + w, chartBottom - chartH / 2);
        g2.drawLine(x, chartTop, x + w, chartTop);

        float stepX = (float) w / Math.max(1, n - 1);
        Path2D linePath = new Path2D.Float();
        Path2D areaPath = new Path2D.Float();

        Point2D.Float[] points = new Point2D.Float[n];

        for (int i = 0; i < n; i++) {
            float px = x + (i * stepX);
            float ratio = (float) values.get(i) / maxVal;
            float py = chartBottom - (ratio * chartH);
            points[i] = new Point2D.Float(px, py);

            if (i == 0) {
                linePath.moveTo(px, py);
                areaPath.moveTo(px, chartBottom);
                areaPath.lineTo(px, py);
            } else {
                linePath.lineTo(px, py);
                areaPath.lineTo(px, py);
            }
        }

        areaPath.lineTo(x + ((n - 1) * stepX), chartBottom);
        areaPath.closePath();

        // Gradient Area fill
        Color startGrad = new Color(palette.accentPrimary.getRed(), palette.accentPrimary.getGreen(), palette.accentPrimary.getBlue(), 90);
        Color endGrad = new Color(palette.accentPrimary.getRed(), palette.accentPrimary.getGreen(), palette.accentPrimary.getBlue(), 0);
        GradientPaint grad = new GradientPaint(x, chartTop, startGrad, x, chartBottom, endGrad);
        g2.setPaint(grad);
        g2.fill(areaPath);

        // Smooth Line stroke
        g2.setColor(palette.accentPrimary);
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(linePath);

        // Draw points and X-axis labels
        g2.setFont(ThemeManager.getInstance().getSmallFont());
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < n; i++) {
            float px = points[i].x;
            float py = points[i].y;

            // Point dot
            g2.setColor(palette.cardBackground);
            g2.fill(new Ellipse2D.Float(px - 4, py - 4, 8, 8));
            g2.setColor(palette.accentPrimary);
            g2.setStroke(new BasicStroke(2.0f));
            g2.draw(new Ellipse2D.Float(px - 4, py - 4, 8, 8));

            // Label
            String key = keys.get(i);
            int kw = fm.stringWidth(key);
            g2.setColor(palette.textSecondary);
            g2.drawString(key, px - kw / 2f, chartBottom + 16);
        }
    }

    // ==========================================
    // DONUT CHART
    // ==========================================

    private void drawDonutChart(Graphics2D g2, ColorPalette palette, int x, int y, int w, int h) {
        int total = 0;
        for (int v : data.values()) total += v;
        if (total == 0) return;

        int size = Math.min(w / 2, h - 10);
        int cx = x + size / 2 + 5;
        int cy = y + h / 2;

        int outerR = size / 2;
        int innerR = (int) (outerR * 0.58);

        double startAngle = 90.0;
        int colorIdx = 0;

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(data.entrySet());
        // Sort descending
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        for (Map.Entry<String, Integer> entry : entries) {
            double angle = (double) entry.getValue() / total * 360.0;
            Color c = PALETTE_COLORS[colorIdx % PALETTE_COLORS.length];
            g2.setColor(c);

            Arc2D.Double outerArc = new Arc2D.Double(cx - outerR, cy - outerR, outerR * 2, outerR * 2,
                    startAngle, -angle, Arc2D.PIE);
            g2.fill(outerArc);

            startAngle -= angle;
            colorIdx++;
        }

        // Inner cutout for donut
        g2.setColor(palette.cardBackground);
        g2.fillOval(cx - innerR, cy - innerR, innerR * 2, innerR * 2);

        // Center total text
        g2.setColor(palette.textPrimary);
        g2.setFont(ThemeManager.getInstance().getBoldFont());
        String totalStr = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totalStr, cx - fm.stringWidth(totalStr) / 2, cy + 4);

        // Legend list on the right side
        int legendX = x + size + 20;
        int legendY = y + 10;
        g2.setFont(ThemeManager.getInstance().getSmallFont());
        FontMetrics lfm = g2.getFontMetrics();

        int maxItems = Math.min(5, entries.size());
        for (int i = 0; i < maxItems; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            Color c = PALETTE_COLORS[i % PALETTE_COLORS.length];

            g2.setColor(c);
            g2.fillRoundRect(legendX, legendY + (i * 20), 10, 10, 3, 3);

            g2.setColor(palette.textSecondary);
            int pct = (int) Math.round((double) entry.getValue() / total * 100);
            String label = entry.getKey();
            if (label.length() > 14) label = label.substring(0, 12) + "..";
            String line = label + " (" + pct + "%)";
            g2.drawString(line, legendX + 16, legendY + (i * 20) + 9);
        }
    }

    // ==========================================
    // BAR CHART
    // ==========================================

    private void drawBarChart(Graphics2D g2, ColorPalette palette, int x, int y, int w, int h) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(data.entrySet());
        // Sort descending
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        int maxItems = Math.min(5, entries.size());

        int maxVal = 1;
        for (int i = 0; i < maxItems; i++) {
            if (entries.get(i).getValue() > maxVal) maxVal = entries.get(i).getValue();
        }

        int itemH = (h - 10) / Math.max(1, maxItems);
        g2.setFont(ThemeManager.getInstance().getSmallFont());
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < maxItems; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            int rowY = y + (i * itemH);

            String label = entry.getKey();
            if (label.length() > 13) label = label.substring(0, 11) + "..";

            g2.setColor(palette.textSecondary);
            g2.drawString(label, x, rowY + 14);

            int barStartX = x + 90;
            int maxBarW = w - 130;
            int barW = (int) ((double) entry.getValue() / maxVal * maxBarW);
            barW = Math.max(6, barW);

            // Bar background slot
            g2.setColor(palette.borderSubtle);
            g2.fillRoundRect(barStartX, rowY + 4, maxBarW, 12, 6, 6);

            // Colored bar
            Color c = PALETTE_COLORS[i % PALETTE_COLORS.length];
            g2.setColor(c);
            g2.fillRoundRect(barStartX, rowY + 4, barW, 12, 6, 6);

            // Value text
            g2.setColor(palette.textPrimary);
            String valStr = String.valueOf(entry.getValue());
            g2.drawString(valStr, barStartX + maxBarW + 8, rowY + 14);
        }
    }

    // ==========================================
    // STACKED QUALITY METER
    // ==========================================

    private void drawStackedQuality(Graphics2D g2, ColorPalette palette, int x, int y, int w, int h) {
        int high = data.getOrDefault("High Confidence", 0);
        int med = data.getOrDefault("Clarifications", 0);
        int low = data.getOrDefault("Fallbacks", 0);
        int total = high + med + low;

        int barY = y + 30;
        int barH = 22;

        if (total == 0) {
            drawEmptyState(g2, palette, x, y, w, h);
            return;
        }

        int highW = (int) ((double) high / total * w);
        int medW = (int) ((double) med / total * w);
        int lowW = w - highW - medW;

        int curX = x;

        // High
        if (highW > 0) {
            g2.setColor(palette.accentSuccess);
            g2.fillRoundRect(curX, barY, highW, barH, 8, 8);
            curX += highW;
        }
        // Medium
        if (medW > 0) {
            g2.setColor(palette.accentWarning);
            g2.fillRoundRect(curX, barY, medW, barH, 8, 8);
            curX += medW;
        }
        // Low
        if (lowW > 0) {
            g2.setColor(palette.accentDanger);
            g2.fillRoundRect(curX, barY, lowW, barH, 8, 8);
        }

        // Legend below
        int legY = barY + 46;
        g2.setFont(ThemeManager.getInstance().getSmallFont());

        drawLegendItem(g2, palette.accentSuccess, "High Matches: " + high, x, legY);
        drawLegendItem(g2, palette.accentWarning, "Clarifications: " + med, x, legY + 20);
        drawLegendItem(g2, palette.accentDanger, "Fallbacks: " + low, x, legY + 40);
    }

    private void drawLegendItem(Graphics2D g2, Color color, String text, int x, int y) {
        g2.setColor(color);
        g2.fillOval(x, y - 9, 8, 8);
        g2.setColor(ThemeManager.getInstance().getPalette().textSecondary);
        g2.drawString(text, x + 14, y - 2);
    }
}
