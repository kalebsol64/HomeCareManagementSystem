package dao;

import model.Appointment;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AppointmentDAO.java
 * --------------------
 * All database operations for Appointments.
 * JOINs Appointment + Patient + Professional + Service_Category + Payment.
 *
 * METHODS:
 *   getAllAppointments()          → load all for JTable
 *   getTodaysAppointments()       → for dashboard
 *   getAppointmentsByPatient(id)  → patient history
 *   getAppointmentById(id)        → for edit form
 *   addAppointment(appointment)   → book new appointment
 *   updateStatus(id, status)      → mark as Completed / Cancelled
 *   updateAppointment(appointment)→ edit full appointment
 *   deleteAppointment(id)         → cancel appointment
 *   getTodayCount()               → for dashboard card
 *   getNextAppointmentId()        → auto-generate ID
 */
public class AppointmentDAO {

    private static final String SELECT_FULL =
        "SELECT a.Appointment_ID, a.Patient_ID, a.Professional_ID, a.Category_ID, "
      + "       a.Appointment_Date, a.Appointment_Time, a.Created_Date, "
      + "       a.Last_Modified, a.Cancelled_Date, a.Duration_Hours, "
      + "       a.Status, a.Special_Instructions, "
      + "       pd.Full_Name  AS Patient_Name,  pd.Phone AS Patient_Phone, pd.Age AS Patient_Age, "
      + "       hpd.Full_Name AS Professional_Name, hp.Profession, "
      + "       sc.Category_Name, sc.Base_Rate, "
      + "       pay.Status    AS Payment_Status, pay.Amount AS Payment_Amount "
      + "FROM Appointment a "
      + "JOIN Patient p                  ON a.Patient_ID      = p.Patient_ID "
      + "JOIN Demographic_Info pd        ON p.Demographic_ID  = pd.Demographic_ID "
      + "JOIN Healthcare_Professional hp ON a.Professional_ID = hp.Professional_ID "
      + "JOIN Demographic_Info hpd       ON hp.Demographic_ID = hpd.Demographic_ID "
      + "JOIN Service_Category sc        ON a.Category_ID     = sc.Category_ID "
      + "LEFT JOIN Payment pay           ON a.Appointment_ID  = pay.Appointment_ID ";

    // ---------------------------------------------------------------
    // READ — Get All Appointments
    // ---------------------------------------------------------------

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        // ✅ FIXED — only active appointments
        String sql = SELECT_FULL
                + "WHERE a.Status != 'Cancelled' "
                + "ORDER BY a.Appointment_Date DESC, a.Appointment_Time DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] getAllAppointments() error: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // READ — Today's Appointments (for Dashboard)
    // ---------------------------------------------------------------

    /**
     * Returns all appointments scheduled for today.
     * Used by the Dashboard to show today's schedule.
     */
    public List<Appointment> getTodaysAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = SELECT_FULL
                   + "WHERE a.Appointment_Date = CAST(GETDATE() AS DATE) "
                   + "ORDER BY a.Appointment_Time";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] getTodaysAppointments() error: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // READ — Appointments by Patient (for patient history view)
    // ---------------------------------------------------------------

    public List<Appointment> getAppointmentsByPatient(String patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = SELECT_FULL
                   + "WHERE a.Patient_ID = ? "
                   + "ORDER BY a.Appointment_Date DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] getAppointmentsByPatient() error: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // READ — Get One By ID
    // ---------------------------------------------------------------

    public Appointment getAppointmentById(String appointmentId) {
        String sql = SELECT_FULL + "WHERE a.Appointment_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] getAppointmentById() error: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // CREATE — Book New Appointment
    // ---------------------------------------------------------------

    /**
     * Inserts a new appointment into the database.
     * Status defaults to "Scheduled".
     *
     * @param a a fully populated Appointment object
     * @return true if insert succeeded
     */
    public boolean addAppointment(Appointment a) {
        String sql = "INSERT INTO Appointment "
            + "(Appointment_ID, Patient_ID, Professional_ID, Category_ID, "
            + " Appointment_Date, Appointment_Time, Created_Date, "
            + " Duration_Hours, Status, Special_Instructions) "
            + "VALUES (?, ?, ?, ?, ?, ?, GETDATE(), ?, 'Scheduled', ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, a.getAppointmentId());
            ps.setString(2, a.getPatientId());
            ps.setString(3, a.getProfessionalId());
            ps.setString(4, a.getCategoryId());
            ps.setString(5, a.getAppointmentDate());
            ps.setString(6, a.getAppointmentTime());
            ps.setInt(7, a.getDurationHours());
            ps.setString(8, a.getSpecialInstructions());

            boolean success = ps.executeUpdate() > 0;
            if (success) System.out.println("[AppointmentDAO] Appointment booked: " + a.getAppointmentId());
            return success;

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] addAppointment() error: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // UPDATE — Change Appointment Status only
    // ---------------------------------------------------------------

    /**
     * Updates just the status of an appointment.
     * Use this for quick actions like "Mark as Completed" or "Cancel".
     *
     * @param appointmentId the appointment to update
     * @param newStatus     "Scheduled", "In Progress", "Completed", or "Cancelled"
     * @return true if update succeeded
     */
    public boolean updateStatus(String appointmentId, String newStatus) {
        String sql;
        if ("Cancelled".equals(newStatus)) {
            // Also record the cancellation date
            sql = "UPDATE Appointment SET Status = ?, Cancelled_Date = GETDATE(), "
                + "Last_Modified = GETDATE() WHERE Appointment_ID = ?";
        } else {
            sql = "UPDATE Appointment SET Status = ?, Last_Modified = GETDATE() "
                + "WHERE Appointment_ID = ?";
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setString(2, appointmentId);
            boolean success = ps.executeUpdate() > 0;
            if (success) System.out.println("[AppointmentDAO] Status updated: " + appointmentId + " → " + newStatus);
            return success;

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] updateStatus() error: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // UPDATE — Edit Full Appointment
    // ---------------------------------------------------------------

    public boolean updateAppointment(Appointment a) {
        String sql = "UPDATE Appointment SET "
            + "Patient_ID = ?, Professional_ID = ?, Category_ID = ?, "
            + "Appointment_Date = ?, Appointment_Time = ?, Duration_Hours = ?, "
            + "Special_Instructions = ?, Last_Modified = GETDATE() "
            + "WHERE Appointment_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, a.getPatientId());
            ps.setString(2, a.getProfessionalId());
            ps.setString(3, a.getCategoryId());
            ps.setString(4, a.getAppointmentDate());
            ps.setString(5, a.getAppointmentTime());
            ps.setInt(6, a.getDurationHours());
            ps.setString(7, a.getSpecialInstructions());
            ps.setString(8, a.getAppointmentId());

            boolean success = ps.executeUpdate() > 0;
            if (success) System.out.println("[AppointmentDAO] Appointment updated: " + a.getAppointmentId());
            return success;

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] updateAppointment() error: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // DELETE — Cancel Appointment (soft delete via status)
    // ---------------------------------------------------------------

    public boolean deleteAppointment(String appointmentId) {
        return updateStatus(appointmentId, "Cancelled");
    }

    // ---------------------------------------------------------------
    // COUNT — For Dashboard Cards
    // ---------------------------------------------------------------

    /** Total appointments today */
    public int getTodayCount() {
        String sql = "SELECT COUNT(*) FROM Appointment "
                   + "WHERE Appointment_Date = CAST(GETDATE() AS DATE)";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] getTodayCount() error: " + e.getMessage());
        }
        return 0;
    }

    /** Upcoming scheduled appointments */
    public int getUpcomingCount() {
        String sql = "SELECT COUNT(*) FROM Appointment "
                   + "WHERE Status = 'Scheduled' "
                   + "AND Appointment_Date >= CAST(GETDATE() AS DATE)";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] getUpcomingCount() error: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // NEXT ID
    // ---------------------------------------------------------------

    public String getNextAppointmentId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(Appointment_ID, 4, 5) AS INT)) FROM Appointment";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int maxNum = rs.getInt(1);
                return String.format("APT%05d", maxNum + 1);
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] getNextAppointmentId() error: " + e.getMessage());
        }
        return "APT00001";
    }

    // ---------------------------------------------------------------
    // PRIVATE HELPER — Map row
    // ---------------------------------------------------------------

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getString("Appointment_ID"));
        a.setPatientId(rs.getString("Patient_ID"));
        a.setProfessionalId(rs.getString("Professional_ID"));
        a.setCategoryId(rs.getString("Category_ID"));
        a.setAppointmentDate(rs.getString("Appointment_Date"));
        a.setAppointmentTime(rs.getString("Appointment_Time"));
        a.setCreatedDate(rs.getString("Created_Date"));
        a.setLastModified(rs.getString("Last_Modified"));
        a.setCancelledDate(rs.getString("Cancelled_Date"));
        a.setDurationHours(rs.getInt("Duration_Hours"));
        a.setStatus(rs.getString("Status"));
        a.setSpecialInstructions(rs.getString("Special_Instructions"));
        // Joined fields
        a.setPatientName(rs.getString("Patient_Name"));
        a.setPatientPhone(rs.getString("Patient_Phone"));
        a.setPatientAge(rs.getInt("Patient_Age"));
        a.setProfessionalName(rs.getString("Professional_Name"));
        a.setProfession(rs.getString("Profession"));
        a.setCategoryName(rs.getString("Category_Name"));
        a.setBaseRate(rs.getDouble("Base_Rate"));
        a.setPaymentStatus(rs.getString("Payment_Status"));
        a.setPaymentAmount(rs.getDouble("Payment_Amount"));
        return a;
    }
}
