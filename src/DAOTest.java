import dao.AppointmentDAO;
import dao.PatientDAO;
import dao.ProfessionalDAO;
import dao.UserDAO;
import model.Appointment;
import model.Patient;
import model.Professional;
import model.User;
import java.sql.Connection;
import java.sql.Statement;
import util.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAOTest.java
 * -------------
 * Run this file BEFORE building any UI to verify every DAO
 * method works correctly against your live database.
 *
 * HOW TO RUN IN INTELLIJ:
 *   Right-click DAOTest.java → Run 'DAOTest.main()'
 *
 * WHAT TO EXPECT:
 *   Every test prints PASS or FAIL with details.
 *   All tests should print PASS before you move to Week 3 (UI).
 */
public class DAOTest {

    // Counters
    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       HOME CARE SYSTEM — DAO TESTS       ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        testUserDAO();
        testPatientDAO();
        testProfessionalDAO();
        testAppointmentDAO();

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.printf( "║  RESULTS:  %2d PASSED  |  %2d FAILED       ║%n", passed, failed);
        System.out.println("╚══════════════════════════════════════════╝");

        if (failed == 0) {
            System.out.println("\n✅ All tests passed — ready to build the UI!");
        } else {
            System.out.println("\n❌ Fix the failing tests before building UI.");
        }
    }
    private static String getNextAddressId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(Address_ID, 4, 5) AS INT)) FROM Address";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int maxNum = rs.getInt(1);
                return String.format("ADD%05d", maxNum + 1);
            }
        } catch (SQLException e) {
            System.err.println("getNextAddressId() error: " + e.getMessage());
        }
        return "ADD00001";
    }

    private static String getNextDemographicId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(Demographic_ID, 4, 5) AS INT)) FROM Demographic_Info";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int maxNum = rs.getInt(1);
                return String.format("DEM%05d", maxNum + 1);
            }
        } catch (SQLException e) {
            System.err.println("getNextDemographicId() error: " + e.getMessage());
        }
        return "DEM00001";
    }

    // ---------------------------------------------------------------
    // USER DAO TESTS
    // ---------------------------------------------------------------
    static void testUserDAO() {
        System.out.println("─── UserDAO Tests ───────────────────────────");
        UserDAO dao = new UserDAO();

        // Test 1: Hash password
        String hash = UserDAO.hashPassword("admin");
        check("hashPassword('admin')",
              "21232f297a57a5a743894a0e4a801fc3".equals(hash),
              "Got: " + hash);

        // Test 2: Login with correct credentials
        User user = dao.authenticate("admin", "admin");
        check("authenticate('admin', 'admin') — valid login",
              user != null,
              user != null ? "Role=" + user.getRole() : "returned null");

        // Test 3: Login with wrong credentials
        User badUser = dao.authenticate("admin", "wrongpassword");
        check("authenticate('admin', 'wrongpassword') — should fail",
              badUser == null,
              badUser == null ? "correctly returned null" : "PROBLEM: returned a user!");

        // Test 4: Get user by ID
        User byId = dao.getUserById("USR00001");
        check("getUserById('USR00001')",
              byId != null,
              byId != null ? "Username=" + byId.getUsername() : "returned null");

        System.out.println();
    }

    // ---------------------------------------------------------------
    // PATIENT DAO TESTS
    // ---------------------------------------------------------------
    static void testPatientDAO() {
        System.out.println("─── PatientDAO Tests ────────────────────────");
        PatientDAO dao = new PatientDAO();

        // ── PRE-TEST CLEANUP: Remove any leftover test data from previous runs ──
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM Patient WHERE Patient_ID > 'PAT00012'");
            st.executeUpdate(
                    "DELETE FROM Demographic_Info " +
                            "WHERE Demographic_ID NOT IN (SELECT Demographic_ID FROM Patient) " +
                            "AND Demographic_ID NOT IN (SELECT Demographic_ID FROM Healthcare_Professional) " +
                            "AND Demographic_ID > 'DEM00012'"
            );
            st.executeUpdate(
                    "DELETE FROM Address " +
                            "WHERE Address_ID NOT IN (SELECT Address_ID FROM Patient) " +
                            "AND Address_ID NOT IN (SELECT Address_ID FROM Healthcare_Professional) " +
                            "AND Address_ID > 'ADD00012'"
            );
        } catch (Exception e) {
            System.out.println("  ⚠️  Pre-cleanup failed: " + e.getMessage());
        }

        // Test 1: Get all patients
        List<Patient> all = dao.getAllPatients();
        check("getAllPatients() — should return 12 rows",
                all.size() == 12,
                "Got " + all.size() + " rows");

        // Test 2: Check first patient has a name
        check("getAllPatients() — first patient has a Full_Name",
                all.size() > 0 && all.get(0).getFullName() != null,
                all.size() > 0 ? "Name: " + all.get(0).getFullName() : "list is empty");

        // Test 3: Get by ID
        Patient p = dao.getPatientById("PAT00001");
        check("getPatientById('PAT00001')",
                p != null && p.getFullName() != null,
                p != null ? "Name: " + p.getFullName() : "returned null");

        // Test 4: Search
        List<Patient> results = dao.searchPatients("Abebe");
        check("searchPatients('Abebe') — should find at least 1",
                results.size() > 0,
                "Found " + results.size() + " results");

        // Test 5: Total count
        int count = dao.getTotalCount();
        check("getTotalCount() — should be 12",
                count == 12,
                "Got: " + count);

        // Test 6: Next ID generation
        String nextId = dao.getNextPatientId();
        check("getNextPatientId() — should be PAT00013",
                "PAT00013".equals(nextId),
                "Got: " + nextId);

        // Test 7: Add new patient – use real next available IDs for all tables
        String testPatientId = dao.getNextPatientId();               // PAT00013
        String testAddrId    = getNextAddressId();                   // ADD00016
        String testDemoId    = getNextDemographicId();               // DEM00021

        Patient newP = new Patient(testPatientId, testDemoId, testAddrId,
                "+251911000099", "Test Contact", "Son", "TEST-999");
        newP.setFullName("Test Patient");
        newP.setPhone("+251911000099");
        newP.setEmail("test@test.com");
        newP.setAge(60);
        newP.setGender("M");
        newP.setMaritalStatus("Single");
        newP.setInsuranceProvider("Test Insurance");
        newP.setReferralSource("Other");
        newP.setCity("Addis Ababa");
        newP.setSubCity("Bole");
        newP.setWoreda("Woreda 01");
        newP.setHouseNumber("T-001");
        newP.setLandmark("Test Landmark");

        boolean added = dao.addPatient(newP);
        check("addPatient() — insert test patient",
                added,
                added ? "Inserted " + testPatientId : "Insert FAILED");

        // Test 8: Update the patient we just added
        if (added) {
            newP.setAge(61);
            newP.setEmail("updated@test.com");
            boolean updated = dao.updatePatient(newP);
            check("updatePatient() — update test patient age",
                    updated,
                    updated ? "Updated successfully" : "Update FAILED");
        }

        // Test 9: Soft-delete the test patient
        if (added) {
            boolean deleted = dao.deletePatient(testPatientId);
            check("deletePatient() — soft delete test patient",
                    deleted,
                    deleted ? "Deactivated successfully" : "Delete FAILED");
        }

        // ── POST-TEST CLEANUP: Hard delete the test patient and its orphans ──
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM Patient WHERE Patient_ID > 'PAT00012'");
            st.executeUpdate(
                    "DELETE FROM Demographic_Info " +
                            "WHERE Demographic_ID NOT IN (SELECT Demographic_ID FROM Patient) " +
                            "AND Demographic_ID NOT IN (SELECT Demographic_ID FROM Healthcare_Professional) " +
                            "AND Demographic_ID > 'DEM00012'"
            );
            st.executeUpdate(
                    "DELETE FROM Address " +
                            "WHERE Address_ID NOT IN (SELECT Address_ID FROM Patient) " +
                            "AND Address_ID NOT IN (SELECT Address_ID FROM Healthcare_Professional) " +
                            "AND Address_ID > 'ADD00012'"
            );
        } catch (Exception e) {
            System.out.println("  ⚠️  Post-cleanup failed: " + e.getMessage());
        }

        System.out.println();
    }
    // ---------------------------------------------------------------
    // PROFESSIONAL DAO TESTS
    // ---------------------------------------------------------------
    static void testProfessionalDAO() {
        System.out.println("─── ProfessionalDAO Tests ───────────────────");
        ProfessionalDAO dao = new ProfessionalDAO();

        // Test 1: Get all
        List<Professional> all = dao.getAllProfessionals();
        check("getAllProfessionals() — should return 8",
              all.size() == 8,
              "Got " + all.size() + " rows");

        // Test 2: First professional has a name
        check("getAllProfessionals() — first has a Full_Name",
              all.size() > 0 && all.get(0).getFullName() != null,
              all.size() > 0 ? "Name: " + all.get(0).getFullName() : "list empty");

        // Test 3: Get available
        List<Professional> available = dao.getAvailableProfessionals();
        check("getAvailableProfessionals() — should be > 0",
              available.size() > 0,
              "Got " + available.size() + " available");

        // Test 4: Get by ID
        Professional pro = dao.getProfessionalById("PRO00001");
        check("getProfessionalById('PRO00001')",
              pro != null && pro.getFullName() != null,
              pro != null ? "Name: " + pro.getFullName() + ", " + pro.getProfession() : "null");

        // Test 5: Available count
        int count = dao.getAvailableCount();
        check("getAvailableCount() — should be > 0",
              count > 0,
              "Got: " + count);

        // Test 6: Next ID
        String nextId = dao.getNextProfessionalId();
        check("getNextProfessionalId() — should be PRO00009",
              "PRO00009".equals(nextId),
              "Got: " + nextId);

        System.out.println();
    }

    // ---------------------------------------------------------------
    // APPOINTMENT DAO TESTS
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
// APPOINTMENT DAO TESTS
// ---------------------------------------------------------------
    static void testAppointmentDAO() {
        System.out.println("─── AppointmentDAO Tests ────────────────────");
        AppointmentDAO dao = new AppointmentDAO();

        // Test 1: Get all
        List<Appointment> all = dao.getAllAppointments();
        check("getAllAppointments() — should return 14 (excluding cancelled)",
                all.size() == 14,
                "Got " + all.size() + " rows");

        // Test 2: Patient name is populated (JOIN worked)
        check("getAllAppointments() — first has Patient_Name from JOIN",
                all.size() > 0 && all.get(0).getPatientName() != null,
                all.size() > 0 ? "Patient: " + all.get(0).getPatientName() : "list empty");

        // Test 3: Get by patient
        List<Appointment> byPatient = dao.getAppointmentsByPatient("PAT00001");
        check("getAppointmentsByPatient('PAT00001') — should be > 0",
                byPatient.size() > 0,
                "Got " + byPatient.size() + " appointments");

        // Test 4: Get by ID
        Appointment a = dao.getAppointmentById("APT00001");
        check("getAppointmentById('APT00001')",
                a != null && a.getPatientName() != null,
                a != null ? "Patient: " + a.getPatientName() + ", Status: " + a.getStatus() : "null");

        // Test 5: Today count (may be 0 since sample data is from 2023)
        int todayCount = dao.getTodayCount();
        check("getTodayCount() — should return a number (0+ is fine)",
                todayCount >= 0,
                "Got: " + todayCount);

        // Test 6: Upcoming count
        int upcomingCount = dao.getUpcomingCount();
        check("getUpcomingCount() — should return a number",
                upcomingCount >= 0,
                "Got: " + upcomingCount);

        // Test 7: Next ID
        String nextId = dao.getNextAppointmentId();
        check("getNextAppointmentId() — should be APT00016",
                "APT00016".equals(nextId),
                "Got: " + nextId);

        // Test 8: Add a new appointment
        String testAptId = dao.getNextAppointmentId();
        Appointment newApt = new Appointment(
                testAptId, "PAT00001", "PRO00002", "CAT00001",
                "2025-06-15", "10:00:00", 2, "Test appointment"
        );
        boolean added = dao.addAppointment(newApt);
        check("addAppointment() — insert test appointment",
                added,
                added ? "Inserted " + testAptId : "Insert FAILED");

        // Test 9: Update status to Completed
        if (added) {
            boolean updated = dao.updateStatus(testAptId, "Completed");
            check("updateStatus() — mark test appointment Completed",
                    updated,
                    updated ? "Status updated" : "Update FAILED");
        }

        // Test 10: Cancel (soft delete) the test appointment
        if (added) {
            boolean cancelled = dao.deleteAppointment(testAptId);
            check("deleteAppointment() — cancel test appointment",
                    cancelled,
                    cancelled ? "Cancelled successfully" : "Cancel FAILED");
        }

        // clean up
        String cleanSql = "DELETE FROM Appointment WHERE Appointment_ID > 'APT00015'";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            int rows = st.executeUpdate(cleanSql);
            System.out.println("  🧹 Cleanup: " + rows + " test appointment(s) hard-deleted");
        } catch (Exception e) {
            System.out.println("  ⚠️  Cleanup failed: " + e.getMessage());
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    // HELPER — Print pass/fail for each test
    // ---------------------------------------------------------------
    static void check(String testName, boolean condition, String detail) {
        if (condition) {
            System.out.printf("  ✅ PASS  %-45s  (%s)%n", testName, detail);
            passed++;
        } else {
            System.out.printf("  ❌ FAIL  %-45s  (%s)%n", testName, detail);
            failed++;
        }
    }
}
