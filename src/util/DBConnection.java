package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java
 * ------------------
 * Single point of database connection for the entire application.
 * All DAO classes call DBConnection.getConnection() to talk to SQL Server.
 *
 * HOW TO USE IN YOUR DAO CLASSES:
 *   try (Connection con = DBConnection.getConnection()) {
 *       // your SQL here
 *   }
 *
 * SETUP REQUIREMENTS:
 *   1. Download Microsoft JDBC Driver from:
 *      https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server
 *   2. Add the .jar file to your project /lib folder
 *   3. In IntelliJ: File > Project Structure > Libraries > + > select the jar
 *   4. Change SERVER_NAME below to your SQL Server instance name
 *      (find it in SQL Server Management Studio top-left when you connect)
 */
public class DBConnection {

    // ---------------------------------------------------------------
    // CHANGE THESE TO MATCH YOUR SQL SERVER SETUP
    // ---------------------------------------------------------------
    private static final String SERVER_NAME   = "DESKTOP-VQQKVME";        // or your PC name e.g. "DESKTOP-ABC123"
    private static final String PORT          = "1433";             // default SQL Server port
    private static final String DATABASE_NAME = "HomeCare_Java_App";
    private static final String USERNAME      = "Homecare_a";               // your SQL Server username
    private static final String PASSWORD      = "Homecare"; // your SQL Server password
    // ---------------------------------------------------------------

    // Build the full JDBC connection URL for SQL Server
    private static final String URL =
            "jdbc:sqlserver://" + SERVER_NAME + ":" + PORT + ";"
            + "databaseName=" + DATABASE_NAME + ";"
            + "encrypt=false;"                     // set true if your server requires encryption
            + "trustServerCertificate=true;";      // required for local development

    // Load the SQL Server JDBC driver once when the class is first used
    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("[DBConnection] SQL Server JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] ERROR: JDBC Driver not found!");
            System.err.println("  → Make sure mssql-jdbc.jar is in your /lib folder");
            System.err.println("  → And added to IntelliJ Project Structure > Libraries");
            e.printStackTrace();
        }
    }

    /**
     * Returns a live connection to the Home_Care_Management database.
     * Always use this inside a try-with-resources block so it closes automatically.
     *
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Quick test — run this main() to verify your DB connection works
     * before building any UI or DAO classes.
     *
     * Right-click DBConnection.java in IntelliJ > Run 'DBConnection.main()'
     */
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        System.out.println("URL: " + URL);

        try (Connection con = getConnection()) {
            if (con != null && !con.isClosed()) {
                System.out.println("SUCCESS! Connected to: " + DATABASE_NAME);
                System.out.println("Server version: " + con.getMetaData().getDatabaseProductVersion());
            }
        } catch (SQLException e) {
            System.err.println("FAILED to connect. Common fixes:");
            System.err.println("  1. Is SQL Server running? Check SQL Server Configuration Manager");
            System.err.println("  2. Is TCP/IP enabled? Enable it in SQL Server Config Manager > Protocols");
            System.err.println("  3. Is the server name correct? Check SSMS connection dialog");
            System.err.println("  4. Is SQL Server Authentication enabled? Enable in SSMS > Server Properties > Security");
            System.err.println("  5. Is port 1433 open? Check Windows Firewall");
            System.err.println("\nError message: " + e.getMessage());
        }
    }
}


