package dao;

import model.User;
import util.DBConnection;
import util.SessionManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

/**
 * UserDAO.java
 * -------------
 * Handles all database operations for the Users table.
 * Main job: authenticate users during login.
 *
 * HOW LOGIN WORKS:
 *   1. User types username + password in LoginFrame
 *   2. LoginFrame calls UserDAO.authenticate(username, password)
 *   3. UserDAO hashes the password with MD5
 *   4. Calls sp_AuthenticateUser stored procedure in the DB
 *   5. If a row comes back → login success → store in SessionManager
 *   6. If no row → wrong credentials → show error
 */
public class UserDAO {

    // ---------------------------------------------------------------
    // PASSWORD HASHING
    // ---------------------------------------------------------------

    /**
     * Converts a plain-text password into an MD5 hash string.
     * This matches the format stored in the Users table.
     *
     * Example: hashPassword("admin") → "21232f297a57a5a743894a0e4a801fc3"
     *
     * @param plainPassword the raw password the user typed
     * @return MD5 hash as a lowercase hex string
     */
    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(plainPassword.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    // ---------------------------------------------------------------
    // AUTHENTICATE (used by LoginFrame)
    // ---------------------------------------------------------------

    /**
     * Authenticates a user against the database using the stored procedure.
     * If successful, also stores the session in SessionManager automatically.
     *
     * @param username      the username typed in the login form
     * @param plainPassword the plain-text password typed in the login form
     * @return User object if login succeeds, null if credentials are wrong
     */
    public User authenticate(String username, String plainPassword) {
        String hashedPassword = hashPassword(plainPassword);
        String sql = "{CALL sp_AuthenticateUser(?, ?)}";

        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, username);
            cs.setString(2, hashedPassword);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    // Build User object from result
                    User user = new User();
                    user.setUserId(rs.getString("User_ID"));
                    user.setUsername(rs.getString("Username"));
                    user.setRole(rs.getString("Role"));

                    // Store session — now any screen can call SessionManager.getCurrentUser()
                    String professionalId = rs.getString("Professional_ID");
                    String fullName       = rs.getString("Full_Name");
                    // For Admin, Full_Name may be null since they aren't linked to a Professional
                    if (fullName == null) fullName = username;

                    SessionManager.login(
                        user.getUserId(),
                        user.getUsername(),
                        user.getRole(),
                        professionalId,
                        fullName
                    );

                    System.out.println("[UserDAO] Login successful: " + user);
                    return user;
                } else {
                    System.out.println("[UserDAO] Login failed: wrong username or password.");
                    return null;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] authenticate() error: " + e.getMessage());
            return null;
        }
    }

    // ---------------------------------------------------------------
    // GET USER BY ID (used by admin user management screen)
    // ---------------------------------------------------------------

    /**
     * Fetches a single user by their User_ID.
     *
     * @param userId e.g. "USR00001"
     * @return User object or null if not found
     */
    public User getUserById(String userId) {
        String sql = "SELECT User_ID, Username, Role, Is_Active, Last_Login "
                   + "FROM Users WHERE User_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getString("User_ID"));
                    user.setUsername(rs.getString("Username"));
                    user.setRole(rs.getString("Role"));
                    user.setActive(rs.getBoolean("Is_Active"));
                    user.setLastLogin(rs.getString("Last_Login"));
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] getUserById() error: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // UPDATE PASSWORD (optional feature — good to have)
    // ---------------------------------------------------------------

    /**
     * Changes the password for a user.
     *
     * @param userId          the user whose password to change
     * @param newPlainPassword the new plain-text password
     * @return true if update succeeded
     */
    public boolean updatePassword(String userId, String newPlainPassword) {
        String sql = "UPDATE Users SET Password_Hash = ? WHERE User_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hashPassword(newPlainPassword));
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] updatePassword() error: " + e.getMessage());
            return false;
        }
    }
}
