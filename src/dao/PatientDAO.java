package dao;

import model.Patient;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PatientDAO.java
 * ----------------
 * All database operations for Patients.
 * Uses JOIN across Patient + Demographic_Info + Address tables
 * so every Patient object returned is fully populated.
 *
 * METHODS:
 *   getAllPatients()         → load all active patients into JTable
 *   getPatientById(id)       → load one patient for edit form
 *   searchPatients(keyword)  → search by name or phone
 *   addPatient(patient)      → insert new patient (3 tables)
 *   updatePatient(patient)   → update existing patient
 *   deletePatient(id)        → soft delete (sets Is_Active = 0)
 *   getTotalCount()          → for dashboard summary card
 */
public class PatientDAO {

    // ---------------------------------------------------------------
    // SHARED SQL — the full JOIN query used by get methods
    // ---------------------------------------------------------------
    private static final String SELECT_FULL =
        "SELECT p.Patient_ID, p.Demographic_ID, p.Address_ID, "
      + "       p.Emergency_Contact, p.Emergency_Contact_Name, p.Relationship, "
      + "       p.Insurance_Policy_No, p.Registration_Date, p.Is_Active, "
      + "       d.Full_Name, d.Phone, d.Email, d.Age, d.Gender, "
      + "       d.Marital_Status, d.Insurance_Provider, d.Referral_Source, "
      + "       a.City, a.Sub_City, a.Woreda, a.House_Number, a.Landmark "
      + "FROM Patient p "
      + "JOIN Demographic_Info d ON p.Demographic_ID = d.Demographic_ID "
      + "JOIN Address a          ON p.Address_ID     = a.Address_ID ";

    // ---------------------------------------------------------------
    // READ — Get All Active Patients
    // ---------------------------------------------------------------

    /**
     * Returns all active patients with full demographic and address info.
     * Use this to populate the patient JTable.
     */
    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = SELECT_FULL + "WHERE p.Is_Active = 1 ORDER BY d.Full_Name";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[PatientDAO] getAllPatients() error: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // READ — Get One Patient By ID
    // ---------------------------------------------------------------

    /**
     * Returns a single patient by Patient_ID.
     * Use this when the user clicks a row and opens the edit form.
     *
     * @param patientId e.g. "PAT00001"
     */
    public Patient getPatientById(String patientId) {
        String sql = SELECT_FULL + "WHERE p.Patient_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("[PatientDAO] getPatientById() error: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // READ — Search Patients
    // ---------------------------------------------------------------

    /**
     * Searches active patients by name or phone number.
     * Use this for the search bar on the patient panel.
     *
     * @param keyword anything the user types in the search box
     */
    public List<Patient> searchPatients(String keyword) {
        List<Patient> list = new ArrayList<>();
        String sql = SELECT_FULL
                   + "WHERE p.Is_Active = 1 "
                   + "AND (d.Full_Name LIKE ? OR d.Phone LIKE ?) "
                   + "ORDER BY d.Full_Name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[PatientDAO] searchPatients() error: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // CREATE — Add New Patient (inserts into 3 tables)
    // ---------------------------------------------------------------

    /**
     * Inserts a new patient into the database.
     * This touches 3 tables in order: Address → Demographic_Info → Patient
     * All 3 inserts happen inside one transaction — if any fails, all roll back.
     *
     * @param p a fully populated Patient object
     * @return true if all 3 inserts succeeded
     */
    public boolean addPatient(Patient p) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // START TRANSACTION

            // Step 1: Insert into Address
            String sqlAddress = "INSERT INTO Address "
                + "(Address_ID, City, Sub_City, Woreda, Kebele_Number, House_Number, Landmark, Date_Added) "
                + "VALUES (?, ?, ?, ?, 1, ?, ?, CAST(GETDATE() AS DATE))";

            try (PreparedStatement ps = con.prepareStatement(sqlAddress)) {
                ps.setString(1, p.getAddressId());
                ps.setString(2, p.getCity() != null ? p.getCity() : "Addis Ababa");
                ps.setString(3, p.getSubCity());
                ps.setString(4, p.getWoreda());
                ps.setString(5, p.getHouseNumber());
                ps.setString(6, p.getLandmark());
                ps.executeUpdate();
            }

            // Step 2: Insert into Demographic_Info
            String sqlDemo = "INSERT INTO Demographic_Info "
                + "(Demographic_ID, Full_Name, Phone, Email, Age, Gender, "
                + " Marital_Status, Insurance_Provider, Referral_Source, Contact_Date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(GETDATE() AS DATE))";

            try (PreparedStatement ps = con.prepareStatement(sqlDemo)) {
                ps.setString(1, p.getDemographicId());
                ps.setString(2, p.getFullName());
                ps.setString(3, p.getPhone());
                ps.setString(4, p.getEmail());
                ps.setInt(5, p.getAge());
                ps.setString(6, p.getGender());
                ps.setString(7, p.getMaritalStatus());
                ps.setString(8, p.getInsuranceProvider());
                ps.setString(9, p.getReferralSource());
                ps.executeUpdate();
            }

            // Step 3: Insert into Patient
            String sqlPatient = "INSERT INTO Patient "
                + "(Patient_ID, Demographic_ID, Address_ID, Emergency_Contact, "
                + " Emergency_Contact_Name, Relationship, Insurance_Policy_No, "
                + " Registration_Date, Is_Active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, CAST(GETDATE() AS DATE), 1)";

            try (PreparedStatement ps = con.prepareStatement(sqlPatient)) {
                ps.setString(1, p.getPatientId());
                ps.setString(2, p.getDemographicId());
                ps.setString(3, p.getAddressId());
                ps.setString(4, p.getEmergencyContact());
                ps.setString(5, p.getEmergencyContactName());
                ps.setString(6, p.getRelationship());
                ps.setString(7, p.getInsurancePolicyNo());
                ps.executeUpdate();
            }

            con.commit(); // COMMIT ALL 3 INSERTS
            System.out.println("[PatientDAO] Patient added: " + p.getPatientId());
            return true;

        } catch (SQLException e) {
            System.err.println("[PatientDAO] addPatient() error: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) { /* ignore */ }
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { /* ignore */ }
        }
    }

    // ---------------------------------------------------------------
    // UPDATE — Edit Existing Patient
    // ---------------------------------------------------------------

    /**
     * Updates an existing patient's demographic and address info.
     * Uses a transaction across 2 tables.
     *
     * @param p Patient object with updated values (patientId must be set)
     * @return true if update succeeded
     */
    public boolean updatePatient(Patient p) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Update Demographic_Info
            String sqlDemo = "UPDATE Demographic_Info SET "
                + "Full_Name = ?, Phone = ?, Email = ?, Age = ?, Gender = ?, "
                + "Marital_Status = ?, Insurance_Provider = ? "
                + "WHERE Demographic_ID = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlDemo)) {
                ps.setString(1, p.getFullName());
                ps.setString(2, p.getPhone());
                ps.setString(3, p.getEmail());
                ps.setInt(4, p.getAge());
                ps.setString(5, p.getGender());
                ps.setString(6, p.getMaritalStatus());
                ps.setString(7, p.getInsuranceProvider());
                ps.setString(8, p.getDemographicId());
                ps.executeUpdate();
            }

            // Update Patient
            String sqlPatient = "UPDATE Patient SET "
                + "Emergency_Contact = ?, Emergency_Contact_Name = ?, "
                + "Relationship = ?, Insurance_Policy_No = ? "
                + "WHERE Patient_ID = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlPatient)) {
                ps.setString(1, p.getEmergencyContact());
                ps.setString(2, p.getEmergencyContactName());
                ps.setString(3, p.getRelationship());
                ps.setString(4, p.getInsurancePolicyNo());
                ps.setString(5, p.getPatientId());
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("[PatientDAO] Patient updated: " + p.getPatientId());
            return true;

        } catch (SQLException e) {
            System.err.println("[PatientDAO] updatePatient() error: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) { /* ignore */ }
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { /* ignore */ }
        }
    }

    // ---------------------------------------------------------------
    // DELETE — Soft Delete (keeps data, just marks inactive)
    // ---------------------------------------------------------------

    /**
     * Soft-deletes a patient by setting Is_Active = 0.
     * NEVER hard-deletes — medical records must be kept.
     *
     * @param patientId e.g. "PAT00001"
     * @return true if the update succeeded
     */
    public boolean deletePatient(String patientId) {
        String sql = "UPDATE Patient SET Is_Active = 0 WHERE Patient_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, patientId);
            boolean success = ps.executeUpdate() > 0;
            if (success) System.out.println("[PatientDAO] Patient deactivated: " + patientId);
            return success;

        } catch (SQLException e) {
            System.err.println("[PatientDAO] deletePatient() error: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // COUNT — For Dashboard
    // ---------------------------------------------------------------

    /**
     * Returns the total number of active patients.
     * Used by DashboardFrame to show the "Total Patients" summary card.
     */
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM Patient WHERE Is_Active = 1";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[PatientDAO] getTotalCount() error: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // NEXT ID — Generate next Patient_ID automatically
    // ---------------------------------------------------------------

    /**
     * Generates the next available Patient_ID in format PAT00013, PAT00014, etc.
     * Call this before addPatient() to get the next ID.
     */
    public String getNextPatientId() {
        String sql = "SELECT MAX(Patient_ID) FROM Patient";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            if (rs.next() && rs.getString(1) != null) {
                String last   = rs.getString(1);          // e.g. "PAT00012"
                int    num    = Integer.parseInt(last.substring(3)); // 12
                return String.format("PAT%05d", num + 1); // "PAT00013"
            }

        } catch (SQLException e) {
            System.err.println("[PatientDAO] getNextPatientId() error: " + e.getMessage());
        }
        return "PAT00001"; // fallback if table is empty
    }


    /**
     * Generates the next available Address_ID — checks MAX across the whole Address table
     * so it never collides with existing sample data (ADD00001-ADD00015).
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
            System.err.println("[PatientDAO] getNextAddressId() error: " + e.getMessage());
        }
        return "ADD00001";
    }

    /**
     * Generates the next available Demographic_ID — checks MAX across the whole table
     * so it never collides with existing sample data (DEM00001-DEM00020).
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
            System.err.println("[PatientDAO] getNextDemographicId() error: " + e.getMessage());
        }
        return "DEM00001";
    }

    // ---------------------------------------------------------------
    // PRIVATE HELPER — Map ResultSet row to Patient object
    // ---------------------------------------------------------------

    /**
     * Converts one ResultSet row into a Patient object.
     * Called by all SELECT methods above to avoid repeating mapping code.
     */
    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getString("Patient_ID"));
        p.setDemographicId(rs.getString("Demographic_ID"));
        p.setAddressId(rs.getString("Address_ID"));
        p.setEmergencyContact(rs.getString("Emergency_Contact"));
        p.setEmergencyContactName(rs.getString("Emergency_Contact_Name"));
        p.setRelationship(rs.getString("Relationship"));
        p.setInsurancePolicyNo(rs.getString("Insurance_Policy_No"));
        p.setRegistrationDate(rs.getString("Registration_Date"));
        p.setActive(rs.getBoolean("Is_Active"));
        // Demographic
        p.setFullName(rs.getString("Full_Name"));
        p.setPhone(rs.getString("Phone"));
        p.setEmail(rs.getString("Email"));
        p.setAge(rs.getInt("Age"));
        p.setGender(rs.getString("Gender"));
        p.setMaritalStatus(rs.getString("Marital_Status"));
        p.setInsuranceProvider(rs.getString("Insurance_Provider"));
        p.setReferralSource(rs.getString("Referral_Source"));
        // Address
        p.setCity(rs.getString("City"));
        p.setSubCity(rs.getString("Sub_City"));
        p.setWoreda(rs.getString("Woreda"));
        p.setHouseNumber(rs.getString("House_Number"));
        p.setLandmark(rs.getString("Landmark"));
        return p;
    }
}
