package view;

import dao.FeedbackDAO;
import model.Feedback;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * FeedbackPanel.java
 * -------------------
 * Service feedback management screen.
 * Lists all patient feedback with filter by satisfaction level.
 * Supports Add / Edit / Delete / View Details.
 */
public class FeedbackPanel extends JPanel {

    private JTable            table;
    private DefaultTableModel tableModel;
    private JComboBox<String> satisfactionFilter;
    private FeedbackDAO       dao;

    private static final String[] COLUMNS = {
        "Feedback ID", "Appointment ID", "Patient",
        "Professional", "Rating", "Satisfaction", "Followup", "Date"
    };

    public FeedbackPanel() {
        dao = new FeedbackDAO();
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildSummaryBar(), BorderLayout.NORTH);
        add(buildTableSection(), BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------
    // SUMMARY BAR (avg rating + total count)
    // ---------------------------------------------------------------
    private JPanel buildSummaryBar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BG_MAIN);

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        statsRow.setBackground(UITheme.BG_MAIN);
        statsRow.setPreferredSize(new Dimension(0, 80));

        statsRow.add(buildStatCard("Total Feedback Entries",
            String.valueOf(dao.getTotalCount()), "💬", UITheme.PRIMARY));
        statsRow.add(buildStatCard("Average Rating",
            String.format("%.1f / 5.0", dao.getAverageRating()), "⭐", UITheme.WARNING));

        wrapper.add(statsRow, BorderLayout.NORTH);

        // Toolbar below stats
        wrapper.add(buildToolbar(), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildStatCard(String title, String value, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.WHITE);
        card.setBorder(UITheme.cardBorder());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.WHITE);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_SECONDARY);
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        top.add(titleLbl, BorderLayout.WEST);
        top.add(iconLbl,  BorderLayout.EAST);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLbl.setForeground(color);

        card.add(top,      BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    // ---------------------------------------------------------------
    // TOOLBAR
    // ---------------------------------------------------------------
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.WHITE);
        bar.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Left — filter
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(UITheme.WHITE);

        satisfactionFilter = UITheme.comboBox(new String[]{
            "All", "Excellent", "Good", "Average", "Poor"
        });
        satisfactionFilter.addActionListener(e -> refresh());

        left.add(UITheme.fieldLabel("Filter by Satisfaction:"));
        left.add(satisfactionFilter);

        // Right — buttons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(UITheme.WHITE);

        JButton addBtn  = UITheme.primaryButton("＋  Add Feedback");
        JButton editBtn = UITheme.outlineButton("✏  Edit");
        JButton delBtn  = UITheme.dangerButton("🗑  Delete");
        JButton viewBtn = UITheme.outlineButton("👁  View Details");

        addBtn.addActionListener(e -> showFeedbackForm(null));
        editBtn.addActionListener(e -> handleEdit());
        delBtn.addActionListener(e -> handleDelete());
        viewBtn.addActionListener(e -> showFeedbackDetails());

        right.add(viewBtn);
        right.add(editBtn);
        right.add(delBtn);
        right.add(addBtn);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ---------------------------------------------------------------
    // TABLE
    // ---------------------------------------------------------------
    private JPanel buildTableSection() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showFeedbackDetails();
            }
        });

        int[] widths = {90, 110, 150, 150, 70, 90, 70, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        scroll.getViewport().setBackground(UITheme.WHITE);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BG_MAIN);
        wrapper.setBorder(new EmptyBorder(16, 0, 0, 0));
        wrapper.add(scroll);
        return wrapper;
    }

    // ---------------------------------------------------------------
    // LOAD / REFRESH
    // ---------------------------------------------------------------
    public void refresh() {
        String filter = (String) satisfactionFilter.getSelectedItem();
        tableModel.setRowCount(0);

        List<Feedback> list = "All".equals(filter)
            ? dao.getAllFeedback()
            : dao.getFeedbackBySatisfaction(filter);

        for (Feedback f : list) {
            tableModel.addRow(new Object[]{
                f.getFeedbackId(),
                f.getAppointmentId(),
                f.getPatientName(),
                f.getProfessionalName(),
                f.getRatingStars(),
                f.getServiceSatisfaction(),
                f.isFollowupRequired() ? "Yes" : "No",
                f.getFeedbackDate()
            });
        }
    }

    // ---------------------------------------------------------------
    // EDIT
    // ---------------------------------------------------------------
    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a feedback entry to edit.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        Feedback f = dao.getFeedbackById(id);
        if (f != null) showFeedbackForm(f);
    }

    // ---------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------
    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a feedback entry to delete.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id      = (String) tableModel.getValueAt(row, 0);
        String patient = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete feedback from " + patient + "?\nThis cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deleteFeedback(id)) {
                JOptionPane.showMessageDialog(this, "Feedback deleted successfully.",
                    "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------------------------------------------------------------
    // VIEW DETAILS
    // ---------------------------------------------------------------
    private void showFeedbackDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a feedback entry to view.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        Feedback f = dao.getFeedbackById(id);
        if (f == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Feedback Details", true);
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.WARNING);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Feedback — " + f.getFeedbackId());
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);
        header.add(title);
        main.add(header, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridBagLayout());
        details.setBackground(UITheme.WHITE);
        details.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 4, 7, 4);

        Object[][] rows = {
            {"Appointment ID",  f.getAppointmentId()},
            {"Appointment Date",f.getAppointmentDate()},
            {"Patient",         f.getPatientName()},
            {"Professional",    f.getProfessionalName()},
            {"Service",         f.getCategoryName()},
            {"Rating",          f.getRatingStars() + "  (" + f.getRating() + "/5)"},
            {"Satisfaction",    f.getServiceSatisfaction()},
            {"Followup Needed", f.isFollowupRequired() ? "Yes" : "No"},
            {"Comments",        f.getComments() != null ? f.getComments() : "No comments"},
            {"Feedback Date",   f.getFeedbackDate()},
        };

        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.38;
            JLabel lbl = UITheme.fieldLabel((String) rows[i][0] + ":");
            lbl.setForeground(UITheme.TEXT_SECONDARY);
            details.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 0.62;
            JLabel val = new JLabel((String) rows[i][1]);
            val.setFont(UITheme.FONT_BODY);
            val.setForeground(UITheme.TEXT_PRIMARY);
            if ("Satisfaction".equals(rows[i][0])) {
                switch (f.getServiceSatisfaction() != null ? f.getServiceSatisfaction() : "") {
                    case "Excellent" -> val.setForeground(UITheme.SUCCESS);
                    case "Good"      -> val.setForeground(UITheme.ACCENT);
                    case "Average"   -> val.setForeground(UITheme.WARNING);
                    case "Poor"      -> val.setForeground(UITheme.DANGER);
                }
            }
            details.add(val, gbc);
        }

        main.add(new JScrollPane(details) {{ setBorder(null); }}, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(UITheme.WHITE);
        JButton closeBtn = UITheme.primaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(closeBtn);
        main.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(main);
        dialog.setVisible(true);
    }

    // ---------------------------------------------------------------
    // ADD / EDIT FORM
    // ---------------------------------------------------------------
    private void showFeedbackForm(Feedback existing) {
        boolean isEdit = existing != null;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Feedback" : "Add New Feedback", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.WARNING);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel(isEdit ? "Edit Feedback Entry" : "Submit New Feedback");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);
        header.add(title);
        main.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);

        JTextField aptIdField    = UITheme.textField();
        JTextField dateField     = UITheme.textField();
        JTextField commentsField = UITheme.textField();
        JComboBox<String> ratingCombo = UITheme.comboBox(
            new String[]{"1", "2", "3", "4", "5"});
        JComboBox<String> satisfactionCombo = UITheme.comboBox(
            new String[]{"Excellent", "Good", "Average", "Poor"});
        JComboBox<String> followupCombo = UITheme.comboBox(
            new String[]{"No", "Yes"});

        dateField.setText(java.time.LocalDate.now().toString());

        if (isEdit) {
            aptIdField.setText(existing.getAppointmentId());
            aptIdField.setEnabled(false); // can't change appointment after submission
            dateField.setText(existing.getFeedbackDate());
            commentsField.setText(existing.getComments());
            ratingCombo.setSelectedItem(String.valueOf(existing.getRating()));
            satisfactionCombo.setSelectedItem(existing.getServiceSatisfaction());
            followupCombo.setSelectedItem(existing.isFollowupRequired() ? "Yes" : "No");
        }

        Object[][] rows = {
            {"Appointment ID *",  aptIdField},
            {"Rating (1-5) *",    ratingCombo},
            {"Satisfaction *",    satisfactionCombo},
            {"Followup Required", followupCombo},
            {"Comments",          commentsField},
            {"Feedback Date *",   dateField},
        };

        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.38;
            form.add(UITheme.fieldLabel((String) rows[i][0]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.62;
            form.add((Component) rows[i][1], gbc);
        }

        main.add(new JScrollPane(form) {{ setBorder(null); }}, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(UITheme.WHITE);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton cancelBtn = UITheme.outlineButton("Cancel");
        JButton saveBtn   = UITheme.primaryButton(isEdit ? "Save Changes" : "Submit Feedback");

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            if (aptIdField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Appointment ID is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Feedback f = isEdit ? existing : new Feedback();
            if (!isEdit) f.setFeedbackId(dao.getNextFeedbackId());
            f.setAppointmentId(aptIdField.getText().trim());
            f.setRating(Integer.parseInt((String) ratingCombo.getSelectedItem()));
            f.setServiceSatisfaction((String) satisfactionCombo.getSelectedItem());
            f.setFollowupRequired("Yes".equals(followupCombo.getSelectedItem()));
            f.setComments(commentsField.getText().trim());
            f.setFeedbackDate(dateField.getText().trim());

            boolean success = isEdit ? dao.updateFeedback(f) : dao.addFeedback(f);
            if (success) {
                JOptionPane.showMessageDialog(dialog,
                    isEdit ? "Feedback updated successfully." : "Feedback submitted successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refresh();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    isEdit ? "Update failed." :
                    "Could not submit feedback.\nCheck the Appointment ID exists and doesn't already have feedback.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        main.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(main);
        dialog.setVisible(true);
    }
}
