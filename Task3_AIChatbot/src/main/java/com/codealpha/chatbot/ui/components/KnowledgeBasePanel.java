package com.codealpha.chatbot.ui.components;

import com.codealpha.chatbot.model.KnowledgeEntry;
import com.codealpha.chatbot.service.KnowledgeBaseService;
import com.codealpha.chatbot.ui.theme.ColorPalette;
import com.codealpha.chatbot.ui.theme.ThemeManager;
import com.codealpha.chatbot.ui.util.UiUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Knowledge Base Management page with search, category filtering,
 * interactive Q&A catalog table, add/edit modal dialogs, and JSON import/export.
 */
public class KnowledgeBasePanel extends JPanel {

    private final KnowledgeBaseService kbService;

    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<KnowledgeEntry> displayedEntries = new ArrayList<>();

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault());

    public KnowledgeBasePanel(KnowledgeBaseService kbService) {
        this.kbService = kbService;
        setLayout(new BorderLayout());
        setOpaque(false);

        buildUI();
        refreshCategories();
        refreshTable();
    }

    private void buildUI() {
        ColorPalette palette = ThemeManager.getInstance().getPalette();

        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // 1. HEADER & SEARCH TOOLBAR
        JPanel topSection = new JPanel(new BorderLayout(0, 12));
        topSection.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Knowledge Base Management");
        pageTitle.setFont(ThemeManager.getInstance().getLargeTitleFont());
        pageTitle.setForeground(palette.textPrimary);

        JLabel pageSubtitle = new JLabel("Curate Q&A entries queried by the NLP engine in real-time");
        pageSubtitle.setFont(ThemeManager.getInstance().getSmallFont());
        pageSubtitle.setForeground(palette.textSecondary);

        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);

        // Filter / Search Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JPanel filterGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterGroup.setOpaque(false);

        // Search bar
        JPanel searchBox = new JPanel(new BorderLayout(6, 0));
        searchBox.setOpaque(false);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                UiUtils.createRoundedBorder(palette.border, 10, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        searchBox.setPreferredSize(new Dimension(240, 36));

        searchField = new JTextField();
        searchField.setBorder(BorderFactory.createEmptyBorder());
        searchField.setOpaque(false);
        searchField.setForeground(palette.textPrimary);
        searchField.setCaretColor(palette.accentPrimary);
        searchField.setFont(ThemeManager.getInstance().getSmallFont());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshTable(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshTable(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshTable(); }
        });

        searchBox.add(new JLabel(UiUtils.createSearchIcon(14, palette.textMuted)), BorderLayout.WEST);
        searchBox.add(searchField, BorderLayout.CENTER);

        // Category dropdown
        categoryCombo = new JComboBox<>(new String[]{"All Categories"});
        categoryCombo.setPreferredSize(new Dimension(180, 36));
        categoryCombo.addActionListener(e -> refreshTable());

        filterGroup.add(searchBox);
        filterGroup.add(categoryCombo);

        // Action buttons
        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionGroup.setOpaque(false);

        JButton addBtn = createToolbarButton("+ Add Question", palette.accentPrimary, this::showAddDialog);
        JButton exportBtn = createToolbarButton("Export JSON", palette.accentSecondary, this::exportJson);
        JButton importBtn = createToolbarButton("Import JSON", palette.textSecondary, this::importJson);

        actionGroup.add(addBtn);
        actionGroup.add(exportBtn);
        actionGroup.add(importBtn);

        toolbar.add(filterGroup, BorderLayout.WEST);
        toolbar.add(actionGroup, BorderLayout.EAST);

        topSection.add(titleBlock, BorderLayout.NORTH);
        topSection.add(toolbar, BorderLayout.SOUTH);

        // 2. TABLE VIEW
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
        tableCard.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] cols = {"Category", "Question", "Answer Preview", "Usage", "Last Updated"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(ThemeManager.getInstance().getSmallFont());
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setBackground(palette.cardBackground);
        table.setForeground(palette.textPrimary);
        table.setSelectionBackground(palette.cardBackgroundHover);
        table.setSelectionForeground(palette.accentPrimary);

        table.getTableHeader().setFont(ThemeManager.getInstance().getBoldFont());
        table.getTableHeader().setBackground(palette.cardBackground);
        table.getTableHeader().setForeground(palette.textSecondary);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, palette.borderSubtle));

        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(340);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRender);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRender);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        tableCard.add(scroll, BorderLayout.CENTER);

        // 3. BOTTOM ACTIONS (Edit / Delete / Details)
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        JButton editBtn = createToolbarButton("Edit Entry", palette.accentPrimary, this::showEditDialog);
        JButton deleteBtn = createToolbarButton("Delete Entry", palette.accentDanger, this::deleteSelected);

        bottomBar.add(editBtn);
        bottomBar.add(deleteBtn);

        tableCard.add(bottomBar, BorderLayout.SOUTH);

        mainPanel.add(topSection, BorderLayout.NORTH);
        mainPanel.add(tableCard, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JButton createToolbarButton(String text, Color accent, Runnable action) {
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
        btn.addActionListener(e -> action.run());
        return btn;
    }

    public void refreshCategories() {
        String current = (String) categoryCombo.getSelectedItem();
        categoryCombo.removeAllItems();
        categoryCombo.addItem("All Categories");
        for (String c : kbService.getCategories()) {
            categoryCombo.addItem(c);
        }
        if (current != null) {
            categoryCombo.setSelectedItem(current);
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        String q = searchField != null ? searchField.getText().trim() : "";
        String cat = categoryCombo != null ? (String) categoryCombo.getSelectedItem() : "All Categories";

        displayedEntries = kbService.search(q, cat);

        for (KnowledgeEntry e : displayedEntries) {
            String ans = e.getAnswer().replaceAll("\\s+", " ").trim();
            if (ans.length() > 65) ans = ans.substring(0, 62) + "...";

            String dateStr = DATE_FMT.format(Instant.ofEpochMilli(e.getLastUpdated()));

            tableModel.addRow(new Object[]{
                    e.getCategory(),
                    e.getQuestion(),
                    ans,
                    e.getUsageCount(),
                    dateStr
            });
        }
    }

    private KnowledgeEntry getSelectedEntry() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < displayedEntries.size()) {
            return displayedEntries.get(row);
        }
        return null;
    }

    private void showAddDialog() {
        showEntryFormDialog("Add Knowledge Base Entry", null);
    }

    private void showEditDialog() {
        KnowledgeEntry selected = getSelectedEntry();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an entry to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        showEntryFormDialog("Edit Knowledge Base Entry", selected);
    }

    private void showEntryFormDialog(String title, KnowledgeEntry existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(520, 560);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        JTextField catField = new JTextField(existing != null ? existing.getCategory() : "Java Core");
        JTextField qField = new JTextField(existing != null ? existing.getQuestion() : "");
        JTextArea aArea = new JTextArea(existing != null ? existing.getAnswer() : "", 4, 20);
        aArea.setLineWrap(true);
        aArea.setWrapStyleWord(true);
        JTextField kwField = new JTextField(existing != null ? String.join(", ", existing.getKeywords()) : "");
        JTextArea exArea = new JTextArea(existing != null && existing.getFollowUpExample() != null ? existing.getFollowUpExample() : "", 3, 20);
        JTextArea lineArea = new JTextArea(existing != null && existing.getFollowUpLineByLine() != null ? existing.getFollowUpLineByLine() : "", 3, 20);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        form.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        form.add(catField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Question:"), gbc);
        gbc.gridx = 1;
        form.add(qField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Answer:"), gbc);
        gbc.gridx = 1;
        form.add(new JScrollPane(aArea), gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Keywords (csv):"), gbc);
        gbc.gridx = 1;
        form.add(kwField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        form.add(new JLabel("Code Example:"), gbc);
        gbc.gridx = 1;
        form.add(new JScrollPane(exArea), gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        form.add(new JLabel("Line Breakdown:"), gbc);
        gbc.gridx = 1;
        form.add(new JScrollPane(lineArea), gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton saveBtn = new JButton("Save Entry");
        saveBtn.addActionListener(e -> {
            String question = qField.getText().trim();
            String answer = aArea.getText().trim();
            String category = catField.getText().trim();

            if (question.isBlank() || answer.isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Question and Answer cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String> keywords = new ArrayList<>();
            for (String k : kwField.getText().split(",")) {
                if (!k.trim().isEmpty()) keywords.add(k.trim().toLowerCase());
            }

            try {
                if (existing != null) {
                    existing.setCategory(category);
                    existing.setQuestion(question);
                    existing.setAnswer(answer);
                    existing.setKeywords(keywords);
                    existing.setFollowUpExample(exArea.getText().trim());
                    existing.setFollowUpLineByLine(lineArea.getText().trim());
                    kbService.updateEntry(existing);
                } else {
                    KnowledgeEntry newEntry = new KnowledgeEntry(category, question, answer,
                            keywords, exArea.getText().trim(), lineArea.getText().trim());
                    kbService.addEntry(newEntry);
                }
                dialog.dispose();
                refreshCategories();
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        KnowledgeEntry selected = getSelectedEntry();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an entry to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the entry:\n\"" + selected.getQuestion() + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            kbService.deleteEntry(selected.getId());
            refreshCategories();
            refreshTable();
        }
    }

    private void exportJson() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("knowledge_base_export.json"));
        chooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                Files.writeString(file.toPath(), kbService.exportToJson());
                JOptionPane.showMessageDialog(this, "Exported successfully to " + file.getName(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importJson() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                String content = Files.readString(file.toPath());
                int count = kbService.importFromJson(content);
                JOptionPane.showMessageDialog(this, "Imported " + count + " new knowledge base entries.", "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                refreshCategories();
                refreshTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Import failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
