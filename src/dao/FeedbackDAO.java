package dao;

import model.Feedback;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FeedbackDAO.java
 * -----------------
 * All database operations for the Service_Feedback table.
 * JOINs Feedback → Appointment → Patient → Professional → Service_Category.
 */
public class FeedbackDAO {

    private static final String SELECT_FULL =
        "SELECT sf.Feedback_ID, sf.Appointment_ID, sf.Rating, sf.Comments, "
      + "       sf.Feedback_Date, sf.Followup_Required, sf.Service_Satisfaction, "
      + "       pd.Full_Name  AS Patient_Name, "
      + "       hpd.Full_Name AS Professional_Name, "
      + "       sc.Category_Name, "
      + "       a.Appointment_Date "
      + "FROM Service_Feedback sf "
      + "JOIN Appointment a              ON sf.Appointment_ID = a.Appointment_ID "
      + "JOIN Patient p                  ON a.Patient_ID      = p.Patient_ID "
      + "JOIN Demographic_Info pd        ON p.Demographic_ID  = pd.Demographic_ID "
      + "JOIN Healthcare_Professional hp ON a.Professional_ID = hp.Professional_ID "
      + "JOIN Demographic_Info hpd       ON hp.Demographic_ID = hpd.Demographic_ID "
      + "JOIN Service_Category sc        ON a.Category_ID     = sc.Category_ID ";

    // ---------------------------------------------------------------
    // READ — Get All Feedback
    // ---------------------------------------------------------------
    public List<Feedback> getAllFeedback() {
        List<Feedback> list = new ArrayList<>();
        String sql = SELECT_FULL + "ORDER BY sf.Feedback_Date DESC";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] getAllFeedback() error: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // READ — Get By ID
    // ---------------------------------------------------------------
    public Feedback getFeedbackById(String feedbackId) {
        String sql = SELECT_FULL + "WHERE sf.Feedback_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, feedbackId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] getFeedbackById() error: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // READ — Get By Satisfaction Level
    // ---------------------------------------------------------------
    public List<Feedback> getFeedbackBySatisfaction(String satisfaction) {
        List<Feedback> list = new ArrayList<>();
        String sql = SELECT_FULL
                   + "WHERE sf.Service_Satisfaction = ? "
                   + "ORDER BY sf.Feedback_Date DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, satisfaction);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] getFeedbackBySatisfaction() error: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // READ — Get Feedback Needing Followup
    // ---------------------------------------------------------------
    public List<Feedback> getFeedbackNeedingFollowup() {
        List<Feedback> list = new ArrayList<>();
        String sql = SELECT_FULL
                   + "WHERE sf.Followup_Required = 1 "
                   + "ORDER BY sf.Feedback_Date DESC";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] getFeedbackNeedingFollowup() error: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // CREATE — Add New Feedback
    // ---------------------------------------------------------------
    public boolean addFeedback(Feedback f) {
        // Check if feedback already exists for this appointment
        if (feedbackExistsForAppointment(f.getAppointmentId())) {
            System.err.println("[FeedbackDAO] Feedback already exists for appointment: " + f.getAppointmentId());
            return false;
        }

        String sql = "INSERT INTO Service_Feedback "
            + "(Feedback_ID, Appointment_ID, Rating, Comments, "
            + " Feedback_Date, Followup_Required, Service_Satisfaction) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, f.getFeedbackId());
            ps.setString(2, f.getAppointmentId());
            ps.setInt(3, f.getRating());
            ps.setString(4, f.getComments());
            ps.setString(5, f.getFeedbackDate());
            ps.setBoolean(6, f.isFollowupRequired());
            ps.setString(7, f.getServiceSatisfaction());

            boolean success = ps.executeUpdate() > 0;
            if (success) System.out.println("[FeedbackDAO] Feedback added: " + f.getFeedbackId());
            return success;

        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] addFeedback() error: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // UPDATE — Edit Feedback
    // ---------------------------------------------------------------
    public boolean updateFeedback(Feedback f) {
        String sql = "UPDATE Service_Feedback SET "
            + "Rating = ?, Comments = ?, Followup_Required = ?, "
            + "Service_Satisfaction = ? "
            + "WHERE Feedback_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, f.getRating());
            ps.setString(2, f.getComments());
            ps.setBoolean(3, f.isFollowupRequired());
            ps.setString(4, f.getServiceSatisfaction());
            ps.setString(5, f.getFeedbackId());

            boolean success = ps.executeUpdate() > 0;
            if (success) System.out.println("[FeedbackDAO] Feedback updated: " + f.getFeedbackId());
            return success;

        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] updateFeedback() error: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // DELETE — Hard Delete (feedback can be removed)
    // ---------------------------------------------------------------
    public boolean deleteFeedback(String feedbackId) {
        String sql = "DELETE FROM Service_Feedback WHERE Feedback_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, feedbackId);
            boolean success = ps.executeUpdate() > 0;
            if (success) System.out.println("[FeedbackDAO] Feedback deleted: " + feedbackId);
            return success;

        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] deleteFeedback() error: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // STATS — For Dashboard / Reports
    // ---------------------------------------------------------------
    public double getAverageRating() {
        String sql = "SELECT ISNULL(AVG(CAST(Rating AS FLOAT)), 0) FROM Service_Feedback";
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] getAverageRating() error: " + e.getMessage());
        }
        return 0.0;
    }

    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM Service_Feedback";
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] getTotalCount() error: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------
    private boolean feedbackExistsForAppointment(String appointmentId) {
        String sql = "SELECT COUNT(*) FROM Service_Feedback WHERE Appointment_ID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] feedbackExistsForAppointment() error: " + e.getMessage());
        }
        return false;
    }

    public String getNextFeedbackId() {
        String sql = "SELECT MAX(Feedback_ID) FROM Service_Feedback";
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(3));
                return String.format("FBK%05d", num + 1);
            }
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] getNextFeedbackId() error: " + e.getMessage());
        }
        return "FBK00001";
    }

    // ---------------------------------------------------------------
    // PRIVATE HELPER — Map row
    // ---------------------------------------------------------------
    private Feedback mapRow(ResultSet rs) throws SQLException {
        Feedback f = new Feedback();
        f.setFeedbackId(rs.getString("Feedback_ID"));
        f.setAppointmentId(rs.getString("Appointment_ID"));
        f.setRating(rs.getInt("Rating"));
        f.setComments(rs.getString("Comments"));
        f.setFeedbackDate(rs.getString("Feedback_Date"));
        f.setFollowupRequired(rs.getBoolean("Followup_Required"));
        f.setServiceSatisfaction(rs.getString("Service_Satisfaction"));
        f.setPatientName(rs.getString("Patient_Name"));
        f.setProfessionalName(rs.getString("Professional_Name"));
        f.setCategoryName(rs.getString("Category_Name"));
        f.setAppointmentDate(rs.getString("Appointment_Date"));
        return f;
    }
}
