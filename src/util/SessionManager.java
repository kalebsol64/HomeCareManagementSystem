package util;

/**
 * SessionManager.java
 * --------------------
 * Tracks the currently logged-in user across the entire application.
 * After a successful login, store the user here.
 * Any screen can then call SessionManager.getCurrentUser() to know
 * who is logged in and what role they have.
 *
 * HOW TO USE:
 *   // After successful login:
 *   SessionManager.login("USR00001", "admin", "Admin", "PRO00001", "Yonas Tadesse");
 *
 *   // On any screen to check role:
 *   if (SessionManager.isAdmin()) { showAdminPanel(); }
 *
 *   // On logout:
 *   SessionManager.logout();
 */
public class SessionManager {

    // The single instance of the logged-in user (null = nobody logged in)
    private static LoggedInUser currentUser = null;

    /**
     * Inner class representing the logged-in user's session data.
     */
    public static class LoggedInUser {
        private final String userId;
        private final String username;
        private final String role;
        private final String professionalId; // null if Admin
        private final String fullName;

        public LoggedInUser(String userId, String username, String role,
                            String professionalId, String fullName) {
            this.userId         = userId;
            this.username       = username;
            this.role           = role;
            this.professionalId = professionalId;
            this.fullName       = fullName;
        }

        public String getUserId()         { return userId; }
        public String getUsername()       { return username; }
        public String getRole()           { return role; }
        public String getProfessionalId() { return professionalId; }
        public String getFullName()       { return fullName; }

        @Override
        public String toString() {
            return "LoggedInUser{userId='" + userId + "', username='" + username
                    + "', role='" + role + "', fullName='" + fullName + "'}";
        }
    }

    // ---------------------------------------------------------------
    // PUBLIC METHODS
    // ---------------------------------------------------------------

    /**
     * Call this after a successful login to store the session.
     */
    public static void login(String userId, String username, String role,
                             String professionalId, String fullName) {
        currentUser = new LoggedInUser(userId, username, role, professionalId, fullName);
        System.out.println("[SessionManager] Logged in: " + currentUser);
    }

    /**
     * Call this when the user clicks Logout.
     */
    public static void logout() {
        System.out.println("[SessionManager] Logged out: " + (currentUser != null ? currentUser.getUsername() : "nobody"));
        currentUser = null;
    }

    /**
     * Returns the current user object, or null if nobody is logged in.
     */
    public static LoggedInUser getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns true if someone is currently logged in.
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Returns true if the logged-in user is an Admin.
     */
    public static boolean isAdmin() {
        return currentUser != null && "Admin".equals(currentUser.getRole());
    }

    /**
     * Returns true if the logged-in user is a Caregiver.
     */
    public static boolean isCaregiver() {
        return currentUser != null && "Caregiver".equals(currentUser.getRole());
    }

    /**
     * Returns the full name of the logged-in user, or "Guest" if nobody is logged in.
     */
    public static String getFullName() {
        return currentUser != null ? currentUser.getFullName() : "Guest";
    }

    /**
     * Throws an exception if nobody is logged in.
     * Call this at the top of any screen that requires authentication.
     */
    public static void requireLogin() {
        if (currentUser == null) {
            throw new SecurityException("No user is logged in. Access denied.");
        }
    }

    /**
     * Throws an exception if the logged-in user is not an Admin.
     * Call this at the top of any Admin-only screen.
     */
    public static void requireAdmin() {
        requireLogin();
        if (!isAdmin()) {
            throw new SecurityException("Admin access required. Current role: " + currentUser.getRole());
        }
    }
}
