package view;

import dao.PaymentDAO;
import model.Payment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.List;

/**
 * PaymentPanel.java
 * -----------------
 * Payment management screen.
 * Lists all payments with filter by status.
 * Supports Add / Edit / Delete / View Details.
 */
public class PaymentPanel extends JPanel {

    private JTable            table;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;
    private PaymentDAO        dao;

    private static final String[] COLUMNS = {
            "Payment ID", "Appointment ID", "Patient",
            "Professional", "Amount", "Status", "Method", "Date"
    };

    public PaymentPanel() {
        dao = new PaymentDAO();
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildSummaryBar(), BorderLayout.NORTH);
        add(buildTableSection(), BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------
    // SUMMARY BAR (Total Payments, Revenue, Pending)
    // ---------------------------------------------------------------
    private JPanel buildSummaryBar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BG_MAIN);

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setBackground(UITheme.BG_MAIN);
        statsRow.setPreferredSize(new Dimension(0, 80));

        NumberFormat currency = NumberFormat.getCurrencyInstance();

        statsRow.add(buildStatCard("Total Payments",
                String.valueOf(dao.getTotalCount()), "📋", UITheme.PRIMARY));
        statsRow.add(buildStatCard("Total Revenue",
                currency.format(dao.getTotalRevenue()), "💰", UITheme.ACCENT));
        statsRow.add(buildStatCard("Pending Payments",
                String.valueOf(dao.getPendingCount()), "⏳", UITheme.WARNING));

        wrapper.add(statsRow, BorderLayout.NORTH);
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

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(UITheme.WHITE);

        statusFilter = UITheme.comboBox(new String[]{
                "All", "Pending", "Completed", "Overdue"
        });
        statusFilter.addActionListener(e -> refresh());

        left.add(UITheme.fieldLabel("Filter by Status:"));
        left.add(statusFilter);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(UITheme.WHITE);

        JButton addBtn  = UITheme.primaryButton("+  Add Payment");
        JButton editBtn = UITheme.outlineButton("✏  Edit");
        JButton delBtn  = UITheme.dangerButton("🗑  Delete");
        JButton viewBtn = UITheme.outlineButton("👁  View Details");

        addBtn.addActionListener(e -> showPaymentForm(null));
        editBtn.addActionListener(e -> handleEdit());
        delBtn.addActionListener(e -> handleDelete());
        viewBtn.addActionListener(e -> showPaymentDetails());

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
                if (e.getClickCount() == 2) showPaymentDetails();
            }
        });

        int[] widths = {100, 120, 150, 150, 100, 90, 100, 100};
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
        String filter = (String) statusFilter.getSelectedItem();
        tableModel.setRowCount(0);

        List<Payment> list = "All".equals(filter)
                ? dao.getAllPayments()
                : dao.getPaymentsByStatus(filter);

        NumberFormat currency = NumberFormat.getCurrencyInstance();
        for (Payment p : list) {
            tableModel.addRow(new Object[]{
                    p.getPaymentId(),
                    p.getAppointmentId(),
                    p.getPatientName(),
                    p.getProfessionalName(),
                    currency.format(p.getAmount()),
                    p.getStatus(),
                    p.getPaymentMethod(),
                    p.getPaymentDate()
            });
        }
    }

    // ---------------------------------------------------------------
    // EDIT
    // ---------------------------------------------------------------
    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a payment entry to edit.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        Payment p = dao.getPaymentById(id);
        if (p != null) showPaymentForm(p);
    }

    // ---------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------
    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a payment entry to delete.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id      = (String) tableModel.getValueAt(row, 0);
        String patient = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete payment record for " + patient + "?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deletePayment(id)) {
                JOptionPane.showMessageDialog(this, "Payment deleted successfully.",
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
    private void showPaymentDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a payment entry to view.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        Payment p = dao.getPaymentById(id);
        if (p == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Payment Details", true);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.ACCENT);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Payment — " + p.getPaymentId());
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

        NumberFormat currency = NumberFormat.getCurrencyInstance();
        Object[][] rows = {
                {"Payment ID",          p.getPaymentId()},
                {"Appointment ID",      p.getAppointmentId()},
                {"Appointment Date",    p.getAppointmentDate()},
                {"Patient",             p.getPatientName()},
                {"Professional",        p.getProfessionalName()},
                {"Service Category",    p.getCategoryName()},
                {"Amount",              currency.format(p.getAmount())},
                {"Status",              p.getStatus()},
                {"Payment Method",      p.getPaymentMethod()},
                {"Transaction Ref.",    p.getTransactionReference()},
                {"Receipt Number",      p.getReceiptNumber()},
                {"Payment Date",        p.getPaymentDate()}
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

            // Colour‑code the status
            if ("Status".equals(rows[i][0])) {
                switch (p.getStatus()) {
                    case "Completed" -> val.setForeground(UITheme.SUCCESS);
                    case "Pending"   -> val.setForeground(UITheme.WARNING);
                    case "Overdue"   -> val.setForeground(UITheme.DANGER);
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
    private void showPaymentForm(Payment existing) {
        boolean isEdit = existing != null;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Payment" : "Add New Payment", true);
        dialog.setSize(480, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.ACCENT);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel(isEdit ? "Edit Payment Record" : "Record New Payment");
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
        JTextField amountField   = UITheme.textField();
        JComboBox<String> statusCombo = UITheme.comboBox(
                new String[]{"Pending", "Completed", "Overdue"});
        JComboBox<String> methodCombo = UITheme.comboBox(
                new String[]{"Cash", "Credit Card", "Debit Card", "Bank Transfer", "Insurance", "Other"});
        JTextField dateField     = UITheme.textField();
        JTextField refField      = UITheme.textField();
        JTextField receiptField  = UITheme.textField();

        dateField.setText(java.time.LocalDate.now().toString());

        if (isEdit) {
            aptIdField.setText(existing.getAppointmentId());
            aptIdField.setEnabled(false);
            amountField.setText(String.valueOf(existing.getAmount()));
            statusCombo.setSelectedItem(existing.getStatus());
            methodCombo.setSelectedItem(existing.getPaymentMethod());
            dateField.setText(existing.getPaymentDate());
            refField.setText(existing.getTransactionReference());
            receiptField.setText(existing.getReceiptNumber());
        }

        Object[][] rows = {
                {"Appointment ID *",  aptIdField},
                {"Amount (KSh) *",    amountField},
                {"Status *",          statusCombo},
                {"Payment Method *",  methodCombo},
                {"Payment Date *",    dateField},
                {"Transaction Ref.",  refField},
                {"Receipt Number",    receiptField}
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
        JButton saveBtn   = UITheme.primaryButton(isEdit ? "Save Changes" : "Record Payment");

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            // Validation
            if (aptIdField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Appointment ID is required.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String amountText = amountField.getText().trim();
            if (amountText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Amount is required.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Amount must be a positive number.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Payment p = isEdit ? existing : new Payment();
            if (!isEdit) p.setPaymentId(dao.getNextPaymentId());
            p.setAppointmentId(aptIdField.getText().trim());
            p.setAmount(amount);
            p.setStatus((String) statusCombo.getSelectedItem());
            p.setPaymentMethod((String) methodCombo.getSelectedItem());
            p.setPaymentDate(dateField.getText().trim());
            p.setTransactionReference(refField.getText().trim());
            p.setReceiptNumber(receiptField.getText().trim());

            boolean success = isEdit ? dao.updatePayment(p) : dao.addPayment(p);
            if (success) {
                JOptionPane.showMessageDialog(dialog,
                        isEdit ? "Payment updated successfully." : "Payment recorded successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refresh();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        isEdit ? "Update failed." :
                                "Could not record payment.\nCheck the Appointment ID exists.",
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