package view;

import dao.ProfessionalDAO;
import model.Professional;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * ProfessionalPanel.java
 * -----------------------
 * Healthcare professional management screen.
 * Lists all professionals with Add / Edit / Toggle Availability.
 */
public class ProfessionalPanel extends JPanel {

    private JTable            table;
    private DefaultTableModel tableModel;
    private ProfessionalDAO   dao;

    private static final String[] COLUMNS = {
        "ID", "Full Name", "Profession", "Specialization",
        "License No", "Experience", "Hourly Rate", "Availability"
    };

    public ProfessionalPanel() {
        dao = new ProfessionalDAO();
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
        JLabel countLabel = new JLabel("All Healthcare Professionals");
        countLabel.setFont(UITheme.FONT_BODY);
        countLabel.setForeground(UITheme.TEXT_SECONDARY);
        left.add(countLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(UITheme.WHITE);

        JButton addBtn    = UITheme.primaryButton("+  Add Professional");
        JButton editBtn   = UITheme.outlineButton("✏  Edit");
        JButton toggleBtn = UITheme.outlineButton("↻  Toggle Availability");
        JButton delBtn    = UITheme.dangerButton("🗑  Remove");

        addBtn.addActionListener(e -> showProfessionalForm(null));
        editBtn.addActionListener(e -> handleEdit());
        toggleBtn.addActionListener(e -> handleToggleAvailability());
        delBtn.addActionListener(e -> handleDelete());

        right.add(toggleBtn);
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

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleEdit();
            }
        });

        int[] widths = {80, 160, 110, 130, 110, 80, 90, 90};
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
        tableModel.setRowCount(0);
        for (Professional p : dao.getAllProfessionals()) {
            tableModel.addRow(new Object[]{
                p.getProfessionalId(),
                p.getFullName(),
                p.getProfession(),
                p.getSpecialization(),
                p.getLicenseNumber(),
                p.getYearsExperience() + " yrs",
                p.getHourlyRateDisplay(),
                p.getAvailabilityDisplay()
            });
        }
    }

    // ---------------------------------------------------------------
    // EDIT
    // ---------------------------------------------------------------
    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a professional to edit.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        Professional p = dao.getProfessionalById(id);
        if (p != null) showProfessionalForm(p);
    }

    // ---------------------------------------------------------------
    // TOGGLE AVAILABILITY
    // ---------------------------------------------------------------
    private void handleToggleAvailability() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a professional.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id   = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String cur  = (String) tableModel.getValueAt(row, 7);
        boolean nowAvailable = "Unavailable".equals(cur);

        Professional p = dao.getProfessionalById(id);
        if (p == null) return;
        p.setAvailable(nowAvailable);
        if (dao.updateProfessional(p)) {
            JOptionPane.showMessageDialog(this,
                name + " is now " + (nowAvailable ? "Available" : "Unavailable") + ".",
                "Updated", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        }
    }

    // ---------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------
    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a professional to remove.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id   = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Mark " + name + " as unavailable?",
            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deleteProfessional(id)) {
                JOptionPane.showMessageDialog(this, name + " marked as unavailable.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            }
        }
    }

    // ---------------------------------------------------------------
    // FORM DIALOG
    // ---------------------------------------------------------------
    private void showProfessionalForm(Professional existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Professional" : "Add New Professional", true);
        dialog.setSize(520, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel(isEdit ? "Edit Professional" : "Register New Professional");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);
        header.add(title);
        main.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JTextField fullNameField    = UITheme.textField();
        JTextField phoneField       = UITheme.textField();
        JTextField emailField       = UITheme.textField();
        JTextField ageField         = UITheme.textField();
        JComboBox<String> profCombo = UITheme.comboBox(
            new String[]{"Doctor","Nurse","Caregiver","Physiotherapist"});
        JTextField specField        = UITheme.textField();
        JTextField licenseField     = UITheme.textField();
        JTextField expField         = UITheme.textField();
        JTextField rateField        = UITheme.textField();
        JTextField subCityField     = UITheme.textField();

        if (isEdit) {
            fullNameField.setText(existing.getFullName());
            phoneField.setText(existing.getPhone());
            emailField.setText(existing.getEmail());
            ageField.setText(String.valueOf(existing.getAge()));
            profCombo.setSelectedItem(existing.getProfession());
            specField.setText(existing.getSpecialization());
            licenseField.setText(existing.getLicenseNumber());
            expField.setText(String.valueOf(existing.getYearsExperience()));
            rateField.setText(String.valueOf(existing.getHourlyRate()));
            subCityField.setText(existing.getSubCity());
        }

        Object[][] rows = {
            {"Full Name *",       fullNameField},
            {"Phone (+251...) *", phoneField},
            {"Email",             emailField},
            {"Age",               ageField},
            {"Profession *",      profCombo},
            {"Specialization *",  specField},
            {"License Number *",  licenseField},
            {"Years Experience",  expField},
            {"Hourly Rate (ETB)", rateField},
            {"Sub City",          subCityField},
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
        JButton saveBtn   = UITheme.primaryButton(isEdit ? "Save Changes" : "Register");

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            if (fullNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Full Name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (licenseField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "License Number is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Professional p = isEdit ? existing : new Professional();
            if (!isEdit) {
                p.setProfessionalId(dao.getNextProfessionalId());
                p.setDemographicId(dao.getNextDemographicId());
                p.setAddressId(dao.getNextAddressId());
            }
            p.setFullName(fullNameField.getText().trim());
            p.setPhone(phoneField.getText().trim());
            p.setEmail(emailField.getText().trim());
            try { p.setAge(Integer.parseInt(ageField.getText().trim())); } catch (Exception ex) { p.setAge(0); }
            p.setProfession((String) profCombo.getSelectedItem());
            p.setSpecialization(specField.getText().trim());
            p.setLicenseNumber(licenseField.getText().trim());
            try { p.setYearsExperience(Integer.parseInt(expField.getText().trim())); } catch (Exception ex) { p.setYearsExperience(0); }
            try { p.setHourlyRate(Double.parseDouble(rateField.getText().trim())); } catch (Exception ex) { p.setHourlyRate(0); }
            p.setSubCity(subCityField.getText().trim());
            p.setWoreda("N/A");
            p.setHouseNumber("N/A");
            p.setGender("M");

            boolean success = isEdit ? dao.updateProfessional(p) : dao.addProfessional(p);
            if (success) {
                JOptionPane.showMessageDialog(dialog,
                    isEdit ? "Professional updated." : "Professional registered.",
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
