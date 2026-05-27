package model;

/**
 * User.java
 * ----------
 * Represents a row in the Users table.
 * Used by UserDAO and SessionManager for login authentication.
 */
public class User {

    private String userId;
    private String username;
    private String passwordHash;
    private String role;           // "Admin" or "Caregiver"
    private boolean isActive;
    private String lastLogin;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public User() {}

    public User(String userId, String username, String passwordHash, String role) {
        this.userId       = userId;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.isActive     = true;
    }

    // ---------------------------------------------------------------
    // Getters & Setters
    // ---------------------------------------------------------------

    public String getUserId()                  { return userId; }
    public void   setUserId(String userId)     { this.userId = userId; }

    public String getUsername()                { return username; }
    public void   setUsername(String username) { this.username = username; }

    public String getPasswordHash()                      { return passwordHash; }
    public void   setPasswordHash(String passwordHash)   { this.passwordHash = passwordHash; }

    public String getRole()              { return role; }
    public void   setRole(String role)   { this.role = role; }

    public boolean isActive()                { return isActive; }
    public void    setActive(boolean active) { this.isActive = active; }

    public String getLastLogin()                   { return lastLogin; }
    public void   setLastLogin(String lastLogin)   { this.lastLogin = lastLogin; }

    // ---------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------

    /** Returns true if this user is an Admin */
    public boolean isAdmin()     { return "Admin".equals(role); }

    /** Returns true if this user is a Caregiver */
    public boolean isCaregiver() { return "Caregiver".equals(role); }

    @Override
    public String toString() {
        return "User{userId='" + userId + "', username='" + username
                + "', role='" + role + "', isActive=" + isActive + "}";
    }
}
