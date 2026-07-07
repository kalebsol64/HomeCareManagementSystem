package view;

import dao.AppointmentDAO;
import dao.PatientDAO;
import dao.ProfessionalDAO;
import model.Appointment;
import util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * DashboardFrame.java
 * --------------------
 * The main application window shown after login.
 * Contains a sidebar for navigation and a content area
 * that swaps panels when the user clicks a menu item.
 *
 * Layout:
 *   Top bar   → app title + logged-in user + logout button
 *   Sidebar   → navigation menu items
 *   Content   → swappable panels (Dashboard, Patients, Professionals, Appointments)
 */
public class DashboardFrame extends JFrame {

    private JPanel      contentArea;
    private CardLayout  cardLayout;
    private JLabel      pageTitleLabel;

    // Sidebar buttons
    private JButton btnDashboard;
    private JButton btnPatients;
    private JButton btnProfessionals;
    private JButton btnAppointments;
    private JButton btnPayments;
    private JButton btnFeedback;
    private JButton activeButton;

    // Panels (lazy-loaded)
    private JPanel      dashboardPanel;
    private PatientPanel    patientPanel;
    private ProfessionalPanel professionalPanel;
    private AppointmentPanel  appointmentPanel;
    private PaymentPanel      paymentPanel;
    private FeedbackPanel     feedbackPanel;

    public DashboardFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Home Care Management System");
        setSize(1200, 720);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildTopBar(),   BorderLayout.NORTH);
        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildContent(),  BorderLayout.CENTER);
        setContentPane(root);

        // Show dashboard by default
        showPanel("DASHBOARD");
        setActiveButton(btnDashboard);
    }

    // ---------------------------------------------------------------
    // TOP BAR
    // ---------------------------------------------------------------
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.WHITE);
        bar.setPreferredSize(new Dimension(0, UITheme.TOPBAR_HEIGHT));
        bar.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));

        // Left — page title
        pageTitleLabel = new JLabel("Dashboard");
        pageTitleLabel.setFont(UITheme.FONT_HEADING);
        pageTitleLabel.setForeground(UITheme.TEXT_PRIMARY);
        pageTitleLabel.setBorder(new EmptyBorder(0, 24, 0, 0));
        bar.add(pageTitleLabel, BorderLayout.WEST);

        // Right — user info + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setBackground(UITheme.WHITE);
        right.setBorder(new EmptyBorder(0, 0, 0, 16));

        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JLabel userName = new JLabel(SessionManager.getFullName());
        userName.setFont(UITheme.FONT_BODY);
        userName.setForeground(UITheme.TEXT_PRIMARY);

        JLabel userRole = new JLabel("  [" + SessionManager.getCurrentUser().getRole() + "]");
        userRole.setFont(UITheme.FONT_SMALL);
        userRole.setForeground(UITheme.TEXT_SECONDARY);

        JButton logoutBtn = UITheme.outlineButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(90, 32));
        logoutBtn.addActionListener(e -> handleLogout());

        right.add(userIcon);
        right.add(userName);
        right.add(userRole);
        right.add(logoutBtn);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    // ---------------------------------------------------------------
    // SIDEBAR
    // ---------------------------------------------------------------
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(UITheme.SIDEBAR_WIDTH, 0));
        sidebar.setBorder(new EmptyBorder(16, 0, 16, 0));

        // App brand mini header
        JLabel brand = new JLabel("  🏥  HCMS", SwingConstants.LEFT);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 14));
        brand.setForeground(Color.WHITE);
        brand.setBorder(new EmptyBorder(8, 16, 20, 16));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);

        // Section label
        sidebar.add(sidebarSectionLabel("MAIN MENU"));

        // Nav buttons
        btnDashboard     = sidebarButton("📊  Dashboard",     "DASHBOARD");
        btnPatients      = sidebarButton("🧑‍⚕️  Patients",       "PATIENTS");
        btnProfessionals = sidebarButton("👨‍⚕️  Professionals",   "PROFESSIONALS");
        btnAppointments  = sidebarButton("📅  Appointments",   "APPOINTMENTS");

        sidebar.add(btnDashboard);
        sidebar.add(btnPatients);
        sidebar.add(btnProfessionals);
        sidebar.add(btnAppointments);

        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sidebarSectionLabel("FINANCE & QUALITY"));
        btnPayments = sidebarButton("💳  Payments", "PAYMENTS");
        btnFeedback = sidebarButton("⭐  Feedback",  "FEEDBACK");
        sidebar.add(btnPayments);
        sidebar.add(btnFeedback);

        // Push remaining space to bottom
        sidebar.add(Box.createVerticalGlue());

        // Bottom info
        JLabel footer = new JLabel("  ACT College — 2025", SwingConstants.LEFT);
        footer.setFont(UITheme.FONT_SMALL);
        footer.setForeground(new Color(120, 150, 180));
        footer.setBorder(new EmptyBorder(0, 16, 0, 0));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        return sidebar;
    }

    private JLabel sidebarSectionLabel(String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(UITheme.FONT_SIDEBAR_HD);
        lbl.setForeground(new Color(120, 150, 180));
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton sidebarButton(String text, String panelName) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_SIDEBAR);
        btn.setForeground(UITheme.SIDEBAR_TEXT);
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 20, 12, 16));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(new Color(35, 65, 105));
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(UITheme.BG_SIDEBAR);
            }
        });

        btn.addActionListener(e -> {
            showPanel(panelName);
            setActiveButton(btn);
        });

        return btn;
    }

    private void setActiveButton(JButton btn) {
        // Reset previous active
        if (activeButton != null) {
            activeButton.setBackground(UITheme.BG_SIDEBAR);
            activeButton.setForeground(UITheme.SIDEBAR_TEXT);
        }
        // Highlight new active
        activeButton = btn;
        activeButton.setBackground(UITheme.SIDEBAR_ACTIVE);
        activeButton.setForeground(Color.WHITE);
    }

    // ---------------------------------------------------------------
    // CONTENT AREA
    // ---------------------------------------------------------------
    private JPanel buildContent() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UITheme.BG_MAIN);

        // Add all panels to card layout
        dashboardPanel    = buildDashboardPanel();
        patientPanel      = new PatientPanel();
        professionalPanel = new ProfessionalPanel();
        appointmentPanel  = new AppointmentPanel();

        contentArea.add(dashboardPanel,    "DASHBOARD");
        contentArea.add(patientPanel,      "PATIENTS");
        contentArea.add(professionalPanel, "PROFESSIONALS");
        contentArea.add(appointmentPanel,  "APPOINTMENTS");
        paymentPanel  = new PaymentPanel();
        feedbackPanel = new FeedbackPanel();
        contentArea.add(paymentPanel,  "PAYMENTS");
        contentArea.add(feedbackPanel, "FEEDBACK");

        return contentArea;
    }

    private void showPanel(String name) {
        cardLayout.show(contentArea, name);
        switch (name) {
            case "DASHBOARD"     -> { pageTitleLabel.setText("Dashboard");             refreshDashboard(); }
            case "PATIENTS"      -> { pageTitleLabel.setText("Patient Management");    patientPanel.refresh(); }
            case "PROFESSIONALS" -> { pageTitleLabel.setText("Professional Management"); professionalPanel.refresh(); }
            case "APPOINTMENTS"  -> { pageTitleLabel.setText("Appointment Scheduling"); appointmentPanel.refresh(); }
            case "PAYMENTS"      -> { pageTitleLabel.setText("Payment Management");      paymentPanel.refresh(); }
            case "FEEDBACK"      -> { pageTitleLabel.setText("Service Feedback");         feedbackPanel.refresh(); }
        }
    }

    // ---------------------------------------------------------------
    // DASHBOARD PANEL
    // ---------------------------------------------------------------
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Summary cards row
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setBackground(UITheme.BG_MAIN);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        cardsRow.add(buildSummaryCard("Total Patients",    "...", "🧑‍⚕️", UITheme.PRIMARY,  "totalPatients"));
        cardsRow.add(buildSummaryCard("Professionals",     "...", "👨‍⚕️", UITheme.ACCENT,   "totalProfessionals"));
        cardsRow.add(buildSummaryCard("Today's Visits",    "...", "📅", UITheme.SUCCESS,  "todayVisits"));
        cardsRow.add(buildSummaryCard("Upcoming",          "...", "⏰", UITheme.WARNING,  "upcoming"));

        panel.add(cardsRow, BorderLayout.NORTH);

        // Recent appointments table
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(UITheme.WHITE);
        tableCard.setBorder(UITheme.cardBorder());

        JLabel tableTitle = new JLabel("Recent Appointments");
        tableTitle.setFont(UITheme.FONT_SUBHEADING);
        tableTitle.setForeground(UITheme.TEXT_PRIMARY);
        tableTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Appointment ID", "Patient", "Professional", "Service", "Date", "Time", "Status"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        tableCard.putClientProperty("recentTable", model);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(UITheme.BG_MAIN);
        tableWrapper.setBorder(new EmptyBorder(16, 0, 0, 0));
        tableWrapper.add(tableCard);
        panel.add(tableWrapper, BorderLayout.CENTER);

        panel.putClientProperty("recentTableModel", model);
        return panel;
    }

    private JPanel buildSummaryCard(String title, String value, String icon,
                                     Color color, String key) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.WHITE);
        card.setBorder(UITheme.cardBorder());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.WHITE);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_SECONDARY);

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        top.add(titleLbl, BorderLayout.WEST);
        top.add(iconLbl,  BorderLayout.EAST);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLbl.setForeground(color);
        valueLbl.putClientProperty("cardKey", key);

        card.add(top,     BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        card.putClientProperty("valueLabel", valueLbl);
        return card;
    }

    private void refreshDashboard() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                PatientDAO      pDao  = new PatientDAO();
                ProfessionalDAO proDao = new ProfessionalDAO();
                AppointmentDAO  aDao  = new AppointmentDAO();
                return new int[]{
                    pDao.getTotalCount(),
                    proDao.getAvailableCount(),
                    aDao.getTodayCount(),
                    aDao.getUpcomingCount()
                };
            }

            @Override
            protected void done() {
                try {
                    int[] counts = get();
                    // Update summary card values
                    updateDashboardCards(counts);
                    // Update recent appointments table
                    updateRecentTable();
                } catch (Exception e) {
                    System.err.println("Dashboard refresh error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateDashboardCards(int[] counts) {
        // Walk through all cards and update their value labels
        JPanel cardsRow = (JPanel) ((JPanel) dashboardPanel
            .getComponent(0)); // NORTH component
        String[] values = {
            String.valueOf(counts[0]),
            String.valueOf(counts[1]),
            String.valueOf(counts[2]),
            String.valueOf(counts[3])
        };
        for (int i = 0; i < cardsRow.getComponentCount() && i < values.length; i++) {
            JPanel card = (JPanel) cardsRow.getComponent(i);
            JLabel valueLbl = (JLabel) card.getClientProperty("valueLabel");
            if (valueLbl != null) valueLbl.setText(values[i]);
        }
    }

    private void updateRecentTable() {
        javax.swing.table.DefaultTableModel model =
            (javax.swing.table.DefaultTableModel) dashboardPanel.getClientProperty("recentTableModel");
        if (model == null) return;

        model.setRowCount(0);
        List<Appointment> list = new AppointmentDAO().getAllAppointments();
        int limit = Math.min(list.size(), 10); // show latest 10
        for (int i = 0; i < limit; i++) {
            Appointment a = list.get(i);
            model.addRow(new Object[]{
                a.getAppointmentId(),
                a.getPatientName(),
                a.getProfessionalName(),
                a.getCategoryName(),
                a.getAppointmentDate(),
                a.getAppointmentTime(),
                a.getStatus()
            });
        }
    }

    // ---------------------------------------------------------------
    // LOGOUT
    // ---------------------------------------------------------------
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
