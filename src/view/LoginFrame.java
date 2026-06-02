package view;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrame.java
 * ----------------
 * The first screen users see when the app opens.
 * Authenticates via UserDAO → on success opens DashboardFrame.
 *
 * Layout:
 *   Left panel  → blue branding panel with app name
 *   Right panel → white login form
 */
public class LoginFrame extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JButton        loginButton;
    private JLabel         errorLabel;
    private UserDAO        userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        UITheme.applyLookAndFeel();
        initUI();
    }

    private void initUI() {
        setTitle("Home Care Management System — Login");
        setSize(820, 500);
        setMinimumSize(new Dimension(700, 450));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildBrandPanel());
        root.add(buildFormPanel());
        setContentPane(root);

        // Allow pressing Enter to login
        getRootPane().setDefaultButton(loginButton);
    }

    // ---------------------------------------------------------------
    // LEFT — Blue Branding Panel
    // ---------------------------------------------------------------
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_SIDEBAR);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // App icon placeholder (circle with H)
        JLabel icon = new JLabel("🏥", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        panel.add(icon, gbc);

        // App name
        JLabel title = new JLabel("<html><center>Home Care<br>Management System</center></html>",
                                   SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        panel.add(title, gbc);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 60));
        sep.setBackground(new Color(255, 255, 255, 60));
        panel.add(sep, gbc);

        // Tagline
        JLabel tagline = new JLabel("<html><center>Connecting patients<br>with quality home care</center></html>",
                                     SwingConstants.CENTER);
        tagline.setFont(UITheme.FONT_BODY);
        tagline.setForeground(UITheme.SIDEBAR_TEXT);
        panel.add(tagline, gbc);

        // Version
        JLabel version = new JLabel("Version 1.0  •  ACT College", SwingConstants.CENTER);
        version.setFont(UITheme.FONT_SMALL);
        version.setForeground(new Color(150, 180, 210));
        gbc.insets = new Insets(30, 0, 0, 0);
        panel.add(version, gbc);

        return panel;
    }

    // ---------------------------------------------------------------
    // RIGHT — Login Form Panel
    // ---------------------------------------------------------------
    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.WHITE);
        form.setBorder(new EmptyBorder(0, 48, 0, 48));
        form.setMaximumSize(new Dimension(340, 400));

        // Heading
        JLabel heading = new JLabel("Welcome Back");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(heading);

        form.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sub);

        form.add(Box.createVerticalStrut(28));

        // Username
        JLabel userLabel = UITheme.fieldLabel("Username");
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userLabel);
        form.add(Box.createVerticalStrut(6));

        usernameField = UITheme.textField();
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.INPUT_HEIGHT));
        form.add(usernameField);

        form.add(Box.createVerticalStrut(16));

        // Password
        JLabel passLabel = UITheme.fieldLabel("Password");
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(passLabel);
        form.add(Box.createVerticalStrut(6));

        passwordField = UITheme.passwordField();
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.INPUT_HEIGHT));
        form.add(passwordField);

        form.add(Box.createVerticalStrut(8));

        // Error label (hidden until login fails)
        errorLabel = new JLabel(" ");
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);

        form.add(Box.createVerticalStrut(16));

        // Login button
        loginButton = UITheme.primaryButton("Sign In");
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.BUTTON_HEIGHT));
        loginButton.addActionListener(e -> handleLogin());
        form.add(loginButton);

        form.add(Box.createVerticalStrut(20));

        // Hint
        JLabel hint = new JLabel("Default: admin / admin");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_SECONDARY);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(hint);

        outer.add(form);
        return outer;
    }

    // ---------------------------------------------------------------
    // LOGIN LOGIC
    // ---------------------------------------------------------------
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        // Disable button and show loading state
        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");
        errorLabel.setText(" ");

        // Run authentication in background thread so UI doesn't freeze
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return userDAO.authenticate(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        // Success — open dashboard
                        dispose();
                        new DashboardFrame().setVisible(true);
                    } else {
                        showError("Invalid username or password.");
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception ex) {
                    showError("Connection error. Check database.");
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                }
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        errorLabel.setText("⚠  " + message);
    }
}
