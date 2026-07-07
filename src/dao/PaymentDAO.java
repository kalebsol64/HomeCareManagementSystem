package dao;

import model.Payment;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PaymentDAO.java
 * ----------------
 * All database operations for the Payment table.
 * JOINs Payment → Appointment → Patient → Demographic_Info
 * so every Payment object includes the patient name and appointment info.
 */
public class PaymentDAO {

    private static final String SELECT_FULL =
        "SELECT pay.Payment_ID, pay.Appointment_ID, pay.Amount, "
      + "       pay.Payment_Date, pay.Payment_Method, pay.Status, "
      + "       pay.Transaction_Reference, pay.Receipt_Number, "
      + "       pd.Full_Name  AS Patient_Name, "
      + "       hpd.Full_Name AS Professional_Name, "
      + "       sc.Category_Name, "
      + "       a.Appointment_Date "
      + "FROM Payment pay "
      + "JOIN Appointment a              ON pay.Appointment_ID = a.Appointment_ID "
      + "JOIN Patient p                  ON a.Patient_ID       = p.Patient_ID "
      + "JOIN Demographic_Info pd        ON p.Demographic_ID   = pd.Demographic_ID "
      + "JOIN Healthcare_Professional hp ON a.Professional_ID  = hp.Professional_ID "
      + "JOIN Demographic_Info hpd       ON hp.Demographic_ID  = hpd.Demographic_ID "
      + "JOIN Service_Category sc        ON a.Category_ID      = sc.Category_ID ";

    // ---------------------------------------------------------------
    // READ — Get All Payments
    // ---------------------------------------------------------------
    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = SELECT_FULL + "ORDER BY pay.Payment_Date DESC";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[PaymentDAO] getAllPayments() error: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // READ — Get By ID
    // ---------------------------------------------------------------
    public Payment getPaymentById(String paymentId) {
        String sql = SELECT_FULL + "WHERE pay.Payment_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("[PaymentDAO] getPaymentById() error: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // READ — Get By Appointment ID
    // ---------------------------------------------------------------
    public Payment getPaymentByAppointment(String appointmentId) {
        String sql = SELECT_FULL + "WHERE pay.Appointment_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("[PaymentDAO] getPaymentByAppointment() error: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // READ — Get Payments By Status
    // ---------------------------------------------------------------
    public List<Payment> getPaymentsByStatus(String status) {
        List<Payment> list = new ArrayList<>();
        String sql = SELECT_FULL + "WHERE pay.Status = ? ORDER BY pay.Payment_Date DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[PaymentDAO] getPaymentsByStatus() error: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // CREATE — Add New Payment
    // ---------------------------------------------------------------
    public boolean addPayment(Payment p) {
        String sql = "INSERT INTO Payment "
            + "(Payment_ID, Appointment_ID, Amount, Payment_Date, "
            + " Payment_Method, Status, Transaction_Reference, Receipt_Number) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getPaymentId());
            ps.setString(2, p.getAppointmentId());
            ps.setDouble(3, p.getAmount());
            ps.setString(4, p.getPaymentDate());
            ps.setString(5, p.getPaymentMethod());
            ps.setString(6, p.getStatus());
            ps.setString(7, p.getTransactionReference());
            ps.setString(8, p.getReceiptNumber());

            boolean success = ps.executeUpdate() > 0;
            if (success) System.out.println("[PaymentDAO] Payment recorded: " + p.getPaymentId());
            return success;

        } catch (SQLException e) {
            System.err.println("[PaymentDAO] addPayment() error: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // UPDATE — Change Payment Status
    // ---------------------------------------------------------------
    public boolean updatePaymentStatus(String paymentId, String newStatus) {
        String sql = "UPDATE Payment SET Status = ? WHERE Payment_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setString(2, paymentId);
            boolean success = ps.executeUpdate() > 0;
            if (success) System.out.println("[PaymentDAO] Status updated: " + paymentId + " → " + newStatus);
            return success;

        } catch (SQLException e) {
            System.err.println("[PaymentDAO] updatePaymentStatus() error: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // COUNT — For Dashboard
    // ---------------------------------------------------------------
    public int getPendingCount() {
        String sql = "SELECT COUNT(*) FROM Payment WHERE Status = 'Pending'";
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] getPendingCount() error: " + e.getMessage());
        }
        return 0;
    }

    public double getTotalRevenue() {
        String sql = "SELECT ISNULL(SUM(Amount), 0) FROM Payment WHERE Status = 'Completed'";
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] getTotalRevenue() error: " + e.getMessage());
        }
        return 0.0;
    }

    // ---------------------------------------------------------------
    // NEXT ID
    // ---------------------------------------------------------------
    public String getNextPaymentId() {
        String sql = "SELECT MAX(Payment_ID) FROM Payment";
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(3));
                return String.format("PAY%05d", num + 1);
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] getNextPaymentId() error: " + e.getMessage());
        }
        return "PAY00001";
    }

    // ---------------------------------------------------------------
    // PRIVATE HELPER — Map row
    // ---------------------------------------------------------------
    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getString("Payment_ID"));
        p.setAppointmentId(rs.getString("Appointment_ID"));
        p.setAmount(rs.getDouble("Amount"));
        p.setPaymentDate(rs.getString("Payment_Date"));
        p.setPaymentMethod(rs.getString("Payment_Method"));
        p.setStatus(rs.getString("Status"));
        p.setTransactionReference(rs.getString("Transaction_Reference"));
        p.setReceiptNumber(rs.getString("Receipt_Number"));
        p.setPatientName(rs.getString("Patient_Name"));
        p.setProfessionalName(rs.getString("Professional_Name"));
        p.setCategoryName(rs.getString("Category_Name"));
        p.setAppointmentDate(rs.getString("Appointment_Date"));
        return p;
    }
}
