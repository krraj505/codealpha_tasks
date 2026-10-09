package com.codealpha.chatbot.ui.theme;

import java.awt.*;

/**
 * Color palettes for Dark Navy and Light Studio themes.
 */
public class ColorPalette {

    public final boolean isDark;
    public final Color background;
    public final Color sidebarBackground;
    public final Color cardBackground;
    public final Color cardBackgroundHover;
    public final Color border;
    public final Color borderSubtle;

    public final Color textPrimary;
    public final Color textSecondary;
    public final Color textMuted;

    public final Color accentPrimary;    // Electric Blue
    public final Color accentSecondary;  // Violet
    public final Color accentTertiary;   // Cyan
    public final Color accentSuccess;
    public final Color accentWarning;
    public final Color accentDanger;

    public final Color userBubble;
    public final Color userBubbleText;
    public final Color assistantBubble;
    public final Color assistantBubbleBorder;
    public final Color codeBlockBackground;

    public ColorPalette(boolean isDark) {
        this.isDark = isDark;
        if (isDark) {
            // Dark Navy & Charcoal Theme
            this.background = new Color(0x0A, 0x0F, 0x1D);
            this.sidebarBackground = new Color(0x0D, 0x13, 0x26);
            this.cardBackground = new Color(0x13, 0x1C, 0x33);
            this.cardBackgroundHover = new Color(0x18, 0x24, 0x42);
            this.border = new Color(0x23, 0x30, 0x54);
            this.borderSubtle = new Color(0x1A, 0x24, 0x3F);

            this.textPrimary = new Color(0xF1, 0xF5, 0xF9);
            this.textSecondary = new Color(0x94, 0xA3, 0xB8);
            this.textMuted = new Color(0x64, 0x74, 0x8B);

            this.accentPrimary = new Color(0x3B, 0x82, 0xF6);   // #3B82F6
            this.accentSecondary = new Color(0x8B, 0x5C, 0xF6); // #8B5CF6
            this.accentTertiary = new Color(0x06, 0xB6, 0xD4);  // #06B6D4
            this.accentSuccess = new Color(0x10, 0xB9, 0x81);
            this.accentWarning = new Color(0xF5, 0x9E, 0x0B);
            this.accentDanger = new Color(0xEF, 0x44, 0x44);

            this.userBubble = new Color(0x25, 0x63, 0xEB);
            this.userBubbleText = Color.WHITE;
            this.assistantBubble = new Color(0x13, 0x1B, 0x31);
            this.assistantBubbleBorder = new Color(0x23, 0x30, 0x54);
            this.codeBlockBackground = new Color(0x07, 0x0B, 0x14);
        } else {
            // Light Studio Theme
            this.background = new Color(0xF8, 0xFA, 0xFC);
            this.sidebarBackground = new Color(0xF1, 0xF5, 0xF9);
            this.cardBackground = Color.WHITE;
            this.cardBackgroundHover = new Color(0xFA, 0xFA, 0xFC);
            this.border = new Color(0xE2, 0xE8, 0xF0);
            this.borderSubtle = new Color(0xED, 0xF2, 0xF7);

            this.textPrimary = new Color(0x0F, 0x17, 0x2A);
            this.textSecondary = new Color(0x47, 0x55, 0x69);
            this.textMuted = new Color(0x94, 0xA3, 0xB8);

            this.accentPrimary = new Color(0x25, 0x63, 0xEB);
            this.accentSecondary = new Color(0x7C, 0x3A, 0xED);
            this.accentTertiary = new Color(0x08, 0x91, 0xB2);
            this.accentSuccess = new Color(0x05, 0x96, 0x69);
            this.accentWarning = new Color(0xD9, 0x77, 0x06);
            this.accentDanger = new Color(0xDC, 0x26, 0x26);

            this.userBubble = new Color(0x25, 0x63, 0xEB);
            this.userBubbleText = Color.WHITE;
            this.assistantBubble = Color.WHITE;
            this.assistantBubbleBorder = new Color(0xE2, 0xE8, 0xF0);
            this.codeBlockBackground = new Color(0xF1, 0xF5, 0xF9);
        }
    }
}
