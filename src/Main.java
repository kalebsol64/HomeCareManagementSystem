import view.LoginFrame;
import view.UITheme;

import javax.swing.*;

/**
 * Main.java
 * ----------
 * Application entry point.
 *
 * This opens the Login screen. From there the full app is accessible.
 *
 * DEFAULT LOGIN CREDENTIALS:
 *   Admin    → username: admin    / password: admin
 *   Caregiver→ username: jsmith   / password: password
 */
public class Main {

    public static void main(String[] args) {
        // Always start UI on the Event Dispatch Thread (EDT)
        // This is required for all Swing applications
        SwingUtilities.invokeLater(() -> {
            UITheme.applyLookAndFeel();
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
