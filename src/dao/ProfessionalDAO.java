package dao;

import model.Professional;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProfessionalDAO.java
 * ---------------------
 * All database operations for Healthcare Professionals.
 * JOINs Healthcare_Professional + Demographic_Info + Address.
 *
 * METHODS:
 *   getAllProfessionals()           → load all for JTable
 *   getAvailableProfessionals()     → for appointment booking dropdown
 *   getProfessionalById(id)         → load one for edit form
 *   addProfessional(professional)   → insert (3 tables, transaction)
 *   updateProfessional(professional)→ update existing
 *   deleteProfessional(id)          → soft delete
 *   getNextProfessionalId()         → auto-generate next ID
 */
public class ProfessionalDAO {

    private static final String SELECT_FULL =
        "SELECT hp.Professional_ID, hp.User_ID, hp.Demographic_ID, hp.Address_ID, "
      + "       hp.Profession, hp.Specialization, hp.License_Number, "
      + "       hp.Years_Experience, hp.Hourly_Rate, hp.Is_Available, hp.Join_Date, "
      + "       d.Full_Name, d.Phone, d.Email, d.Age, d.Gender, "
      + "       a.Sub_City, a.Woreda, a.House_Number "
      + "FROM Healthcare_Professional hp "
      + "JOIN Demographic_Info d ON hp.Demographic_ID = d.Demographic_ID "
      + "LEFT JOIN Address a     ON hp.Address_ID     = a.Address_ID ";

    // ---------------------------------------------------------------
    // READ — Get All Professionals
    // ---------------------------------------------------------------

    public List<Professional> getAllProfessionals() {
        List<Professional> list = new ArrayList<>();
        String sql = SELECT_FULL + "ORDER BY d.Full_Name";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[ProfessionalDAO] getAllProfessionals() error: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // READ — Get Only Available Professionals (for appointment dropdown)
    // ---------------------------------------------------------------

    /**
     * Returns only professionals who are currently available.
     * Use this to populate the "Select Professional" dropdown
     * when creating a new appointment.
     */
    public List<Professional> getAvailableProfessionals() {
        List<Professional> list = new ArrayList<>();
        String sql = SELECT_FULL + "WHERE hp.Is_Available = 1 ORDER BY hp.Profession, d.Full_Name";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[ProfessionalDAO] getAvailableProfessionals() error: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // READ — Get One By ID
    // ---------------------------------------------------------------

    public Professional getProfessionalById(String professionalId) {
        String sql = SELECT_FULL + "WHERE hp.Professional_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, professionalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("[ProfessionalDAO] getProfessionalById() error: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // CREATE — Add New Professional
    // ---------------------------------------------------------------

    public boolean addProfessional(Professional p) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Step 1: Address
            String sqlAddress = "INSERT INTO Address "
                + "(Address_ID, City, Sub_City, Woreda, Kebele_Number, House_Number, Date_Added) "
                + "VALUES (?, 'Addis Ababa', ?, ?, 1, ?, CAST(GETDATE() AS DATE))";

            try (PreparedStatement ps = con.prepareStatement(sqlAddress)) {
                ps.setString(1, p.getAddressId());
                ps.setString(2, p.getSubCity());
                ps.setString(3, p.getWoreda());
                ps.setString(4, p.getHouseNumber());
                ps.executeUpdate();
            }

            // Step 2: Demographic_Info
            String sqlDemo = "INSERT INTO Demographic_Info "
                + "(Demographic_ID, Full_Name, Phone, Email, Age, Gender, Contact_Date) "
                + "VALUES (?, ?, ?, ?, ?, ?, CAST(GETDATE() AS DATE))";

            try (PreparedStatement ps = con.prepareStatement(sqlDemo)) {
                ps.setString(1, p.getDemographicId());
                ps.setString(2, p.getFullName());
                ps.setString(3, p.getPhone());
                ps.setString(4, p.getEmail());
                ps.setInt(5, p.getAge());
                ps.setString(6, p.getGender());
                ps.executeUpdate();
            }

            // Step 3: Healthcare_Professional
            String sqlPro = "INSERT INTO Healthcare_Professional "
                + "(Professional_ID, User_ID, Demographic_ID, Address_ID, Profession, "
                + " Specialization, License_Number, Years_Experience, Hourly_Rate, "
                + " Is_Available, Join_Date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, CAST(GETDATE() AS DATE))";

            try (PreparedStatement ps = con.prepareStatement(sqlPro)) {
                ps.setString(1, p.getProfessionalId());
                ps.setString(2, p.getUserId());   // may be null — that's OK
                ps.setString(3, p.getDemographicId());
                ps.setString(4, p.getAddressId());
                ps.setString(5, p.getProfession());
                ps.setString(6, p.getSpecialization());
                ps.setString(7, p.getLicenseNumber());
                ps.setInt(8, p.getYearsExperience());
                ps.setDouble(9, p.getHourlyRate());
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("[ProfessionalDAO] Professional added: " + p.getProfessionalId());
            return true;

        } catch (SQLException e) {
            System.err.println("[ProfessionalDAO] addProfessional() error: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) { /* ignore */ }
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { /* ignore */ }
        }
    }

    // ---------------------------------------------------------------
    // UPDATE — Edit Professional
    // ---------------------------------------------------------------

    public boolean updateProfessional(Professional p) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Update Demographic_Info
            String sqlDemo = "UPDATE Demographic_Info SET "
                + "Full_Name = ?, Phone = ?, Email = ?, Age = ?, Gender = ? "
                + "WHERE Demographic_ID = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlDemo)) {
                ps.setString(1, p.getFullName());
                ps.setString(2, p.getPhone());
                ps.setString(3, p.getEmail());
                ps.setInt(4, p.getAge());
                ps.setString(5, p.getGender());
                ps.setString(6, p.getDemographicId());
                ps.executeUpdate();
            }

            // Update Healthcare_Professional
            String sqlPro = "UPDATE Healthcare_Professional SET "
                + "Profession = ?, Specialization = ?, Years_Experience = ?, "
                + "Hourly_Rate = ?, Is_Available = ? "
                + "WHERE Professional_ID = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlPro)) {
                ps.setString(1, p.getProfession());
                ps.setString(2, p.getSpecialization());
                ps.setInt(3, p.getYearsExperience());
                ps.setDouble(4, p.getHourlyRate());
                ps.setBoolean(5, p.isAvailable());
                ps.setString(6, p.getProfessionalId());
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("[ProfessionalDAO] Professional updated: " + p.getProfessionalId());
            return true;

        } catch (SQLException e) {
            System.err.println("[ProfessionalDAO] updateProfessional() error: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) { /* ignore */ }
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { /* ignore */ }
        }
    }

    // ---------------------------------------------------------------
    // DELETE — Soft Delete (toggle availability)
    // ---------------------------------------------------------------

    public boolean deleteProfessional(String professionalId) {
        String sql = "UPDATE Healthcare_Professional SET Is_Available = 0 "
                   + "WHERE Professional_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, professionalId);
            boolean success = ps.executeUpdate() > 0;
            if (success) System.out.println("[ProfessionalDAO] Professional deactivated: " + professionalId);
            return success;

        } catch (SQLException e) {
            System.err.println("[ProfessionalDAO] deleteProfessional() error: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // COUNT — For Dashboard
    // ---------------------------------------------------------------

    public int getAvailableCount() {
        String sql = "SELECT COUNT(*) FROM Healthcare_Professional WHERE Is_Available = 1";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[ProfessionalDAO] getAvailableCount() error: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // NEXT ID
    // ---------------------------------------------------------------

    public String getNextProfessionalId() {
        String sql = "SELECT MAX(Professional_ID) FROM Healthcare_Professional";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(3));
                return String.format("PRO%05d", num + 1);
            }

        } catch (SQLException e) {
            System.err.println("[ProfessionalDAO] getNextProfessionalId() error: " + e.getMessage());
        }
        return "PRO00001";
    }


    /**
     * Generates the next available Address_ID from the Address table.
     */
    public String getNextAddressId() {
        String sql = "SELECT MAX(Address_ID) FROM Address";
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(3));
                return String.format("ADD%05d", num + 1);
            }
        } catch (SQLException e) {
            System.err.println("[ProfessionalDAO] getNextAddressId() error: " + e.getMessage());
        }
        return "ADD00001";
    }

    /**
     * Generates the next available Demographic_ID from the Demographic_Info table.
     */
    public String getNextDemographicId() {
        String sql = "SELECT MAX(Demographic_ID) FROM Demographic_Info";
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(3));
                return String.format("DEM%05d", num + 1);
            }
        } catch (SQLException e) {
            System.err.println("[ProfessionalDAO] getNextDemographicId() error: " + e.getMessage());
        }
        return "DEM00001";
    }

    // ---------------------------------------------------------------
    // PRIVATE HELPER — Map row
    // ---------------------------------------------------------------

    private Professional mapRow(ResultSet rs) throws SQLException {
        Professional p = new Professional();
        p.setProfessionalId(rs.getString("Professional_ID"));
        p.setUserId(rs.getString("User_ID"));
        p.setDemographicId(rs.getString("Demographic_ID"));
        p.setAddressId(rs.getString("Address_ID"));
        p.setProfession(rs.getString("Profession"));
        p.setSpecialization(rs.getString("Specialization"));
        p.setLicenseNumber(rs.getString("License_Number"));
        p.setYearsExperience(rs.getInt("Years_Experience"));
        p.setHourlyRate(rs.getDouble("Hourly_Rate"));
        p.setAvailable(rs.getBoolean("Is_Available"));
        p.setJoinDate(rs.getString("Join_Date"));
        p.setFullName(rs.getString("Full_Name"));
        p.setPhone(rs.getString("Phone"));
        p.setEmail(rs.getString("Email"));
        p.setAge(rs.getInt("Age"));
        p.setGender(rs.getString("Gender"));
        p.setSubCity(rs.getString("Sub_City"));
        p.setWoreda(rs.getString("Woreda"));
        p.setHouseNumber(rs.getString("House_Number"));
        return p;
    }
}
