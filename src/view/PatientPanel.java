package view;

import dao.PatientDAO;
import model.Patient;
import util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * PatientPanel.java
 * ------------------
 * Full patient management screen with:
 *   - Searchable JTable listing all active patients
 *   - Add / Edit / Delete (soft) buttons
 *   - Modal form dialog for adding and editing
 */
public class PatientPanel extends JPanel {

    private JTable             table;
    private DefaultTableModel  tableModel;
    private JTextField         searchField;
    private PatientDAO         dao;

    private static final String[] COLUMNS = {
        "Patient ID", "Full Name", "Age", "Gender", "Phone",
        "Sub City", "Emergency Contact", "Insurance", "Status"
    };

    public PatientPanel() {
        dao = new PatientDAO();
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildToolbar(),  BorderLayout.NORTH);
        add(buildTable(),    BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------
    // TOOLBAR (Search + Buttons)
    // ---------------------------------------------------------------
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.WHITE);
        bar.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Left — search
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(UITheme.WHITE);

        searchField = UITheme.textField();
        searchField.setPreferredSize(new Dimension(240, UITheme.INPUT_HEIGHT));
        searchField.putClientProperty("JTextField.placeholderText", "Search by name or phone...");

        JButton searchBtn = UITheme.primaryButton("🔍  Search");
        searchBtn.addActionListener(e -> handleSearch());

        JButton clearBtn = UITheme.outlineButton("Clear");
        clearBtn.addActionListener(e -> { searchField.setText(""); refresh(); });

        searchField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleSearch();
            }
        });

        left.add(new JLabel("Patients:"));
        left.add(searchField);
        left.add(searchBtn);
        left.add(clearBtn);

        // Right — action buttons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(UITheme.WHITE);

        JButton addBtn  = UITheme.primaryButton("+  Add Patient");
        JButton editBtn = UITheme.outlineButton("✏  Edit");
        JButton delBtn  = UITheme.dangerButton("🗑  Delete");

        addBtn.addActionListener(e -> showPatientForm(null));
        editBtn.addActionListener(e -> handleEdit());
        delBtn.addActionListener(e -> handleDelete());

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
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Double-click to edit
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleEdit();
            }
        });

        // Column widths
        int[] widths = {80, 160, 40, 55, 120, 90, 130, 100, 65};
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
        loadPatients(dao.getAllPatients());
    }

    private void loadPatients(List<Patient> list) {
        tableModel.setRowCount(0);
        for (Patient p : list) {
            tableModel.addRow(new Object[]{
                p.getPatientId(),
                p.getFullName(),
                p.getAge(),
                p.getGenderFull(),
                p.getPhone(),
                p.getSubCity(),
                p.getEmergencyContactName(),
                p.getInsuranceProvider(),
                p.getStatusDisplay()
            });
        }
    }

    // ---------------------------------------------------------------
    // SEARCH
    // ---------------------------------------------------------------
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { refresh(); return; }
        loadPatients(dao.searchPatients(keyword));
    }

    // ---------------------------------------------------------------
    // EDIT
    // ---------------------------------------------------------------
    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a patient to edit.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String patientId = (String) tableModel.getValueAt(row, 0);
        Patient p = dao.getPatientById(patientId);
        if (p != null) showPatientForm(p);
    }

    // ---------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------
    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a patient to delete.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String patientId = (String) tableModel.getValueAt(row, 0);
        String name      = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Deactivate patient: " + name + "?\n(Record will be kept but marked inactive)",
            "Confirm Deactivation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deletePatient(patientId)) {
                JOptionPane.showMessageDialog(this,
                    name + " has been deactivated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to deactivate patient.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------------------------------------------------------------
    // ADD / EDIT FORM DIALOG
    // ---------------------------------------------------------------
    private void showPatientForm(Patient existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Patient" : "Add New Patient", true);
        dialog.setSize(560, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel(isEdit ? "Edit Patient Record" : "Register New Patient");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);
        header.add(title);
        main.add(header, BorderLayout.NORTH);

        // Form fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(6, 4, 6, 4);
        gbc.weightx = 1.0;

        // Form inputs
        JTextField fullNameField     = UITheme.textField();
        JTextField phoneField        = UITheme.textField();
        JTextField emailField        = UITheme.textField();
        JTextField ageField          = UITheme.textField();
        JComboBox<String> genderCombo= UITheme.comboBox(new String[]{"M","F"});
        JComboBox<String> maritalCombo=UITheme.comboBox(new String[]{"Single","Married","Divorced","Widowed"});
        JTextField emergencyField    = UITheme.textField();
        JTextField emergencyNameField= UITheme.textField();
        JTextField subCityField      = UITheme.textField();
        JTextField woredaField       = UITheme.textField();
        JTextField insuranceField    = UITheme.textField();

        // Pre-fill if editing
        if (isEdit) {
            fullNameField.setText(existing.getFullName());
            phoneField.setText(existing.getPhone());
            emailField.setText(existing.getEmail());
            ageField.setText(String.valueOf(existing.getAge()));
            genderCombo.setSelectedItem(existing.getGender());
            maritalCombo.setSelectedItem(existing.getMaritalStatus());
            emergencyField.setText(existing.getEmergencyContact());
            emergencyNameField.setText(existing.getEmergencyContactName());
            subCityField.setText(existing.getSubCity());
            woredaField.setText(existing.getWoreda());
            insuranceField.setText(existing.getInsuranceProvider());
        }

        // Add rows to form
        Object[][] rows = {
            {"Full Name *",        fullNameField},
            {"Phone (+251...) *",  phoneField},
            {"Email",              emailField},
            {"Age",                ageField},
            {"Gender",             genderCombo},
            {"Marital Status",     maritalCombo},
            {"Emergency Phone *",  emergencyField},
            {"Emergency Name",     emergencyNameField},
            {"Sub City",           subCityField},
            {"Woreda",             woredaField},
            {"Insurance Provider", insuranceField},
        };

        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            form.add(UITheme.fieldLabel((String) rows[i][0]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            form.add((Component) rows[i][1], gbc);
        }

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        main.add(formScroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(UITheme.WHITE);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton cancelBtn = UITheme.outlineButton("Cancel");
        JButton saveBtn   = UITheme.primaryButton(isEdit ? "Save Changes" : "Register Patient");

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            // Validate required fields
            if (fullNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Full Name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (phoneField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Phone is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (emergencyField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Emergency Contact phone is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Build patient object
            Patient p = isEdit ? existing : new Patient();
            if (!isEdit) {
                p.setPatientId(dao.getNextPatientId());
                p.setDemographicId(dao.getNextDemographicId());
                p.setAddressId(dao.getNextAddressId());
            }
            p.setFullName(fullNameField.getText().trim());
            p.setPhone(phoneField.getText().trim());
            p.setEmail(emailField.getText().trim());
            try { p.setAge(Integer.parseInt(ageField.getText().trim())); } catch (NumberFormatException ex) { p.setAge(0); }
            p.setGender((String) genderCombo.getSelectedItem());
            p.setMaritalStatus((String) maritalCombo.getSelectedItem());
            p.setEmergencyContact(emergencyField.getText().trim());
            p.setEmergencyContactName(emergencyNameField.getText().trim());
            p.setSubCity(subCityField.getText().trim());
            p.setWoreda(woredaField.getText().trim());
            p.setCity("Addis Ababa");
            p.setHouseNumber("N/A");
            p.setInsuranceProvider(insuranceField.getText().trim());

            boolean success = isEdit ? dao.updatePatient(p) : dao.addPatient(p);
            if (success) {
                JOptionPane.showMessageDialog(dialog,
                    isEdit ? "Patient updated successfully." : "Patient registered successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refresh();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Operation failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        main.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(main);
        dialog.setVisible(true);
    }
}
