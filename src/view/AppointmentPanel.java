package view;

import dao.AppointmentDAO;
import dao.PatientDAO;
import dao.ProfessionalDAO;
import model.Appointment;
import model.Patient;
import model.Professional;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * AppointmentPanel.java
 * ----------------------
 * Appointment scheduling and management screen.
 * Lists all appointments with status filter + Book / Edit / Cancel actions.
 */
public class AppointmentPanel extends JPanel {

    private JTable            table;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;
    private AppointmentDAO    dao;

    private static final String[] COLUMNS = {
        "ID", "Patient", "Professional", "Service",
        "Date", "Time", "Duration", "Status", "Payment"
    };

    public AppointmentPanel() {
        dao = new AppointmentDAO();
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
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
            "All", "Scheduled", "In Progress", "Completed", "Cancelled"
        });
        statusFilter.addActionListener(e -> refresh());

        left.add(UITheme.fieldLabel("Filter by Status:"));
        left.add(statusFilter);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(UITheme.WHITE);

        JButton bookBtn     = UITheme.primaryButton("+  Book Appointment");
        JButton completeBtn = UITheme.successButton("✔  Mark Completed");
        JButton cancelBtn   = UITheme.dangerButton("✖  Cancel Appointment");

        bookBtn.addActionListener(e -> showAppointmentForm(null));
        completeBtn.addActionListener(e -> handleStatusUpdate("Completed"));
        cancelBtn.addActionListener(e -> handleStatusUpdate("Cancelled"));

        right.add(completeBtn);
        right.add(cancelBtn);
        right.add(bookBtn);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ---------------------------------------------------------------
    // TABLE
    // ---------------------------------------------------------------
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showAppointmentDetails();
            }
        });

        int[] widths = {80, 150, 150, 120, 90, 70, 70, 90, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        scroll.getViewport().setBackground(UITheme.WHITE);
        return scroll;
    }

    // ---------------------------------------------------------------
    // LOAD / REFRESH
    // ---------------------------------------------------------------
    public void refresh() {
        String filter = (String) statusFilter.getSelectedItem();
        tableModel.setRowCount(0);

        List<Appointment> list = dao.getAllAppointments();
        for (Appointment a : list) {
            if (!"All".equals(filter) && !a.getStatus().equals(filter)) continue;
            tableModel.addRow(new Object[]{
                a.getAppointmentId(),
                a.getPatientName(),
                a.getProfessionalName(),
                a.getCategoryName(),
                a.getAppointmentDate(),
                a.getAppointmentTime(),
                a.getDurationHours() + " hr",
                a.getStatus(),
                a.getPaymentStatus() != null ? a.getPaymentStatus() : "—"
            });
        }
    }

    // ---------------------------------------------------------------
    // STATUS UPDATE (Complete / Cancel)
    // ---------------------------------------------------------------
    private void handleStatusUpdate(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment first.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String id     = (String) tableModel.getValueAt(row, 0);
        String cur    = (String) tableModel.getValueAt(row, 7);
        String patient= (String) tableModel.getValueAt(row, 1);

        if ("Completed".equals(cur) || "Cancelled".equals(cur)) {
            JOptionPane.showMessageDialog(this,
                "This appointment is already " + cur + ".",
                "Cannot Update", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Mark appointment for " + patient + " as " + newStatus + "?",
            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.updateStatus(id, newStatus)) {
                JOptionPane.showMessageDialog(this,
                    "Appointment marked as " + newStatus + ".",
                    "Updated", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------------------------------------------------------------
    // SHOW DETAILS (double-click)
    // ---------------------------------------------------------------
    private void showAppointmentDetails() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String id = (String) tableModel.getValueAt(row, 0);
        Appointment a = dao.getAppointmentById(id);
        if (a == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Appointment Details", true);
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Appointment — " + a.getAppointmentId());
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);
        header.add(title);
        main.add(header, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridBagLayout());
        details.setBackground(UITheme.WHITE);
        details.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        Object[][] rows = {
            {"Patient",        a.getPatientName() + " (Age: " + a.getPatientAge() + ")"},
            {"Phone",          a.getPatientPhone()},
            {"Professional",   a.getProfessionalName() + " — " + a.getProfession()},
            {"Service",        a.getCategoryName()},
            {"Date & Time",    a.getAppointmentDate() + " at " + a.getAppointmentTime()},
            {"Duration",       a.getDurationHours() + " hour(s)"},
            {"Status",         a.getStatus()},
            {"Payment",        a.getPaymentStatus() != null ? a.getPaymentStatus() : "Not recorded"},
            {"Instructions",   a.getSpecialInstructions() != null ? a.getSpecialInstructions() : "None"},
        };

        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            JLabel lbl = UITheme.fieldLabel((String) rows[i][0] + ":");
            lbl.setForeground(UITheme.TEXT_SECONDARY);
            details.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            JLabel val = new JLabel((String) rows[i][1]);
            val.setFont(UITheme.FONT_BODY);
            val.setForeground(UITheme.TEXT_PRIMARY);
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
    // BOOK NEW APPOINTMENT FORM
    // ---------------------------------------------------------------
    private void showAppointmentForm(Appointment existing) {
        boolean isEdit = existing != null;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Book New Appointment", true);
        dialog.setSize(520, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Book New Appointment");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);
        header.add(title);
        main.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);

        // Load patients and professionals for dropdowns
        List<Patient>      patients      = new PatientDAO().getAllPatients();
        List<Professional> professionals = new ProfessionalDAO().getAvailableProfessionals();

        String[] patientItems = patients.stream()
            .map(p -> p.getPatientId() + " — " + p.getFullName())
            .toArray(String[]::new);
        String[] proItems = professionals.stream()
            .map(p -> p.getProfessionalId() + " — " + p.getFullName() + " (" + p.getProfession() + ")")
            .toArray(String[]::new);
        String[] categoryItems = {
            "CAT00001 — Skilled Nursing",   "CAT00002 — Physiotherapy",
            "CAT00003 — Personal Care",     "CAT00004 — Companionship",
            "CAT00005 — Medical Consultation", "CAT00006 — Medication Management",
            "CAT00007 — Wound Care",        "CAT00008 — 24-Hour Care",
            "CAT00009 — Palliative Care",   "CAT00010 — Post-Surgery Care"
        };

        JComboBox<String> patientCombo  = UITheme.comboBox(patientItems);
        JComboBox<String> proCombo      = UITheme.comboBox(proItems);
        JComboBox<String> categoryCombo = UITheme.comboBox(categoryItems);
        JTextField dateField     = UITheme.textField();
        JTextField timeField     = UITheme.textField();
        JTextField durationField = UITheme.textField();
        JTextField notesField    = UITheme.textField();

        dateField.putClientProperty("JTextField.placeholderText", "YYYY-MM-DD");
        timeField.putClientProperty("JTextField.placeholderText", "HH:MM:SS  e.g. 09:00:00");
        durationField.putClientProperty("JTextField.placeholderText", "1 to 8");

        Object[][] rows = {
            {"Patient *",           patientCombo},
            {"Professional *",      proCombo},
            {"Service Category *",  categoryCombo},
            {"Date (YYYY-MM-DD) *", dateField},
            {"Time (HH:MM:SS) *",   timeField},
            {"Duration (hours) *",  durationField},
            {"Special Instructions",notesField},
        };

        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            form.add(UITheme.fieldLabel((String) rows[i][0]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            form.add((Component) rows[i][1], gbc);
        }

        main.add(new JScrollPane(form) {{ setBorder(null); }}, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(UITheme.WHITE);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton cancelBtn = UITheme.outlineButton("Cancel");
        JButton saveBtn   = UITheme.primaryButton("Book Appointment");

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            // Validate
            if (dateField.getText().trim().isEmpty() || timeField.getText().trim().isEmpty()
                    || durationField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Date, Time, and Duration are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Parse selected patient/professional IDs from combo text
            String patientSel = (String) patientCombo.getSelectedItem();
            String proSel     = (String) proCombo.getSelectedItem();
            String catSel     = (String) categoryCombo.getSelectedItem();

            if (patientSel == null || proSel == null || catSel == null) {
                JOptionPane.showMessageDialog(dialog,
                    "Please select Patient, Professional, and Category.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String patientId  = patientSel.split(" — ")[0].trim();
            String proId      = proSel.split(" — ")[0].trim();
            String categoryId = catSel.split(" — ")[0].trim();

            int duration;
            try { duration = Integer.parseInt(durationField.getText().trim()); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Duration must be a number.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Appointment a = new Appointment(
                dao.getNextAppointmentId(),
                patientId, proId, categoryId,
                dateField.getText().trim(),
                timeField.getText().trim(),
                duration,
                notesField.getText().trim()
            );

            if (dao.addAppointment(a)) {
                JOptionPane.showMessageDialog(dialog,
                    "Appointment booked successfully!\nID: " + a.getAppointmentId(),
                    "Booked", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refresh();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Booking failed. Please check your inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        main.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(main);
        dialog.setVisible(true);
    }
}
