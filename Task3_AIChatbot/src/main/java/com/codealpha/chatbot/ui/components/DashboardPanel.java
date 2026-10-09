package com.codealpha.chatbot.ui.components;

import com.codealpha.chatbot.model.AnalyticsSnapshot;
import com.codealpha.chatbot.service.AnalyticsService;
import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Advanced Analytics Dashboard visualizing real metrics, vector charts,
 * recent conversation activity table, and quick actions.
 */
public class DashboardPanel extends JPanel {

    private final AnalyticsService analyticsService;
    private final Consumer<String> onNavigate;
    private final Runnable onNewChat;

    private KpiCard convKpi;
    private KpiCard userMsgKpi;
    private KpiCard assistantMsgKpi;
    private KpiCard responseTimeKpi;

    private ModernChartPanel activityChart;
    private ModernChartPanel intentChart;
    private ModernChartPanel kbUsageChart;
    private ModernChartPanel qualityChart;

    private JTable recentTable;
    private DefaultTableModel tableModel;

    public DashboardPanel(AnalyticsService analyticsService,
                          Consumer<String> onNavigate,
                          Runnable onNewChat) {
        this.analyticsService = analyticsService;
        this.onNavigate = onNavigate;
        this.onNewChat = onNewChat;

        setLayout(new BorderLayout());
        setOpaque(false);

        buildUI();
        refreshAnalytics();
    }

    private void buildUI() {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // 1. TOP HEADER & QUICK ACTIONS
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Analytics & Insights");
        pageTitle.setFont(ThemeManager.getInstance().getLargeTitleFont());
        pageTitle.setForeground(palette.textPrimary);

        JLabel pageSubtitle = new JLabel("Real-time operational metrics calculated from actual conversation logs");
        pageSubtitle.setFont(ThemeManager.getInstance().getSmallFont());
        pageSubtitle.setForeground(palette.textSecondary);

        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);

        // Quick Actions toolbar
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionToolbar.setOpaque(false);

        JButton newChatBtn = createActionButton("+ New Chat", palette.accentPrimary, () -> {
            if (onNewChat != null) onNewChat.run();
            if (onNavigate != null) onNavigate.accept("CHAT");
        });

        JButton manageKbBtn = createActionButton("Manage KB", palette.accentSecondary, () -> {
            if (onNavigate != null) onNavigate.accept("KNOWLEDGE");
        });

        JButton settingsBtn = createActionButton("Settings", palette.textSecondary, () -> {
            if (onNavigate != null) onNavigate.accept("SETTINGS");
        });

        actionToolbar.add(newChatBtn);
        actionToolbar.add(manageKbBtn);
        actionToolbar.add(settingsBtn);

        headerRow.add(titleBlock, BorderLayout.WEST);
        headerRow.add(actionToolbar, BorderLayout.EAST);

        mainContainer.add(headerRow);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        // 2. TOP KPI CARDS (4 cards in a grid)
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        convKpi = new KpiCard("Total Conversations", "0", UiUtils.createChatIcon(18, palette.accentPrimary), palette.accentPrimary);
        userMsgKpi = new KpiCard("User Messages", "0", UiUtils.createSendIcon(18, palette.accentSecondary), palette.accentSecondary);
        assistantMsgKpi = new KpiCard("AI Responses", "0", UiUtils.createRajAiLogo(18, palette.accentTertiary, palette.accentPrimary), palette.accentTertiary);
        responseTimeKpi = new KpiCard("Avg Response Time", "0 ms", UiUtils.createDashboardIcon(18, palette.accentSuccess), palette.accentSuccess);

        kpiGrid.add(convKpi);
        kpiGrid.add(userMsgKpi);
        kpiGrid.add(assistantMsgKpi);
        kpiGrid.add(responseTimeKpi);

        mainContainer.add(kpiGrid);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        // 3. ANALYTICS CHARTS (2x2 Grid)
        JPanel chartsGrid = new JPanel(new GridLayout(2, 2, 16, 16));
        chartsGrid.setOpaque(false);
        chartsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 490));

        activityChart = new ModernChartPanel("Conversation Activity Over Time", ModernChartPanel.ChartType.AREA_ACTIVITY);
        intentChart = new ModernChartPanel("Intent Distribution", ModernChartPanel.ChartType.DONUT_INTENT);
        kbUsageChart = new ModernChartPanel("Knowledge Base Category Usage", ModernChartPanel.ChartType.BAR_CATEGORY);
        qualityChart = new ModernChartPanel("Response Quality Overview", ModernChartPanel.ChartType.STACKED_QUALITY);

        chartsGrid.add(activityChart);
        chartsGrid.add(intentChart);
        chartsGrid.add(kbUsageChart);
        chartsGrid.add(qualityChart);

        mainContainer.add(chartsGrid);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        // 4. RECENT ACTIVITY TABLE
        JPanel tableCard = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.setRenderingHints(g2);
                ColorPalette p = ThemeManager.getInstance().getPalette();
                g2.setColor(p.cardBackground);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(p.border);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        tableCard.setOpaque(false);
        tableCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));
        tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel tableTitle = new JLabel("Recent Conversation Activity");
        tableTitle.setFont(ThemeManager.getInstance().getBoldFont());
        tableTitle.setForeground(palette.textPrimary);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Conversation Title", "Last Activity", "Messages", "Last Intent"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTable = new JTable(tableModel);
        recentTable.setFont(ThemeManager.getInstance().getSmallFont());
        recentTable.setRowHeight(32);
        recentTable.setShowGrid(false);
        recentTable.setIntercellSpacing(new Dimension(0, 2));
        recentTable.setBackground(palette.cardBackground);
        recentTable.setForeground(palette.textPrimary);
        recentTable.setSelectionBackground(palette.cardBackgroundHover);
        recentTable.setSelectionForeground(palette.accentPrimary);

        // Header Styling
        recentTable.getTableHeader().setFont(ThemeManager.getInstance().getBoldFont());
        recentTable.getTableHeader().setBackground(palette.cardBackground);
        recentTable.getTableHeader().setForeground(palette.textSecondary);
        recentTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, palette.borderSubtle));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        recentTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        recentTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        recentTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane tableScroll = new JScrollPane(recentTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.setPreferredSize(new Dimension(0, 160));

        tableCard.add(tableScroll, BorderLayout.CENTER);
        mainContainer.add(tableCard);

        JScrollPane pageScroll = new JScrollPane(mainContainer);
        pageScroll.setBorder(BorderFactory.createEmptyBorder());
        pageScroll.setOpaque(false);
        pageScroll.getViewport().setOpaque(false);
        pageScroll.getVerticalScrollBar().setUnitIncrement(18);

        add(pageScroll, BorderLayout.CENTER);
    }

    private JButton createActionButton(String text, Color accent, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeManager.getInstance().getSmallFont());
        btn.setForeground(accent);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                UiUtils.createRoundedBorder(ThemeManager.getInstance().getPalette().border, 8, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.addActionListener(e -> {
            if (action != null) action.run();
        });
        return btn;
    }

    public void refreshAnalytics() {
        AnalyticsSnapshot snapshot = analyticsService.getSnapshot();

        // Update KPIs
        convKpi.setValue(String.valueOf(snapshot.getTotalConversations()));
        userMsgKpi.setValue(String.valueOf(snapshot.getTotalUserMessages()));
        assistantMsgKpi.setValue(String.valueOf(snapshot.getTotalAssistantResponses()));
        responseTimeKpi.setValue(String.format("%.0f ms", snapshot.getAverageResponseTimeMs()));

        // Update Charts
        activityChart.setData(snapshot.getActivityTimeline());
        intentChart.setData(snapshot.getIntentDistribution());
        kbUsageChart.setData(snapshot.getCategoryUsage());
        qualityChart.setData(snapshot.getQualityOverview());

        // Update Table
        tableModel.setRowCount(0);
        List<AnalyticsSnapshot.RecentConversationItem> recents = snapshot.getRecentConversations();
        for (AnalyticsSnapshot.RecentConversationItem item : recents) {
            tableModel.addRow(new Object[]{
                    item.getTitle(),
                    item.getLastActivity(),
                    item.getMessageCount(),
                    item.getLastIntent()
            });
        }

        revalidate();
        repaint();
    }
}
