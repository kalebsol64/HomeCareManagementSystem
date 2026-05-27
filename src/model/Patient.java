package model;

/**
 * Patient.java
 * -------------
 * Represents a joined row from Patient + Demographic_Info + Address tables.
 * This is what PatientDAO returns — all patient info in one object.
 * No need to juggle 3 separate objects in your UI code.
 */
public class Patient {

    // From Patient table
    private String patientId;
    private String demographicId;
    private String addressId;
    private String emergencyContact;
    private String emergencyContactName;
    private String relationship;
    private String insurancePolicyNo;
    private String registrationDate;
    private boolean isActive;

    // From Demographic_Info table (joined)
    private String fullName;
    private String phone;
    private String email;
    private int    age;
    private String gender;
    private String maritalStatus;
    private String insuranceProvider;
    private String referralSource;

    // From Address table (joined)
    private String city;
    private String subCity;
    private String woreda;
    private String houseNumber;
    private String landmark;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public Patient() {}

    // Constructor for creating a new patient (minimum required fields)
    public Patient(String patientId, String demographicId, String addressId,
                   String emergencyContact, String emergencyContactName,
                   String relationship, String insurancePolicyNo) {
        this.patientId            = patientId;
        this.demographicId        = demographicId;
        this.addressId            = addressId;
        this.emergencyContact     = emergencyContact;
        this.emergencyContactName = emergencyContactName;
        this.relationship         = relationship;
        this.insurancePolicyNo    = insurancePolicyNo;
        this.isActive             = true;
    }

    // ---------------------------------------------------------------
    // Getters & Setters — Patient table fields
    // ---------------------------------------------------------------

    public String getPatientId()                       { return patientId; }
    public void   setPatientId(String patientId)       { this.patientId = patientId; }

    public String getDemographicId()                         { return demographicId; }
    public void   setDemographicId(String demographicId)     { this.demographicId = demographicId; }

    public String getAddressId()                       { return addressId; }
    public void   setAddressId(String addressId)       { this.addressId = addressId; }

    public String getEmergencyContact()                            { return emergencyContact; }
    public void   setEmergencyContact(String emergencyContact)     { this.emergencyContact = emergencyContact; }

    public String getEmergencyContactName()                                { return emergencyContactName; }
    public void   setEmergencyContactName(String emergencyContactName)     { this.emergencyContactName = emergencyContactName; }

    public String getRelationship()                        { return relationship; }
    public void   setRelationship(String relationship)     { this.relationship = relationship; }

    public String getInsurancePolicyNo()                             { return insurancePolicyNo; }
    public void   setInsurancePolicyNo(String insurancePolicyNo)     { this.insurancePolicyNo = insurancePolicyNo; }

    public String getRegistrationDate()                          { return registrationDate; }
    public void   setRegistrationDate(String registrationDate)   { this.registrationDate = registrationDate; }

    public boolean isActive()                { return isActive; }
    public void    setActive(boolean active) { this.isActive = active; }

    // ---------------------------------------------------------------
    // Getters & Setters — Demographic_Info fields (from JOIN)
    // ---------------------------------------------------------------

    public String getFullName()                    { return fullName; }
    public void   setFullName(String fullName)     { this.fullName = fullName; }

    public String getPhone()               { return phone; }
    public void   setPhone(String phone)   { this.phone = phone; }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }

    public int  getAge()           { return age; }
    public void setAge(int age)    { this.age = age; }

    public String getGender()                { return gender; }
    public void   setGender(String gender)   { this.gender = gender; }

    public String getMaritalStatus()                       { return maritalStatus; }
    public void   setMaritalStatus(String maritalStatus)   { this.maritalStatus = maritalStatus; }

    public String getInsuranceProvider()                           { return insuranceProvider; }
    public void   setInsuranceProvider(String insuranceProvider)   { this.insuranceProvider = insuranceProvider; }

    public String getReferralSource()                        { return referralSource; }
    public void   setReferralSource(String referralSource)   { this.referralSource = referralSource; }

    // ---------------------------------------------------------------
    // Getters & Setters — Address fields (from JOIN)
    // ---------------------------------------------------------------

    public String getCity()              { return city; }
    public void   setCity(String city)   { this.city = city; }

    public String getSubCity()                 { return subCity; }
    public void   setSubCity(String subCity)   { this.subCity = subCity; }

    public String getWoreda()                { return woreda; }
    public void   setWoreda(String woreda)   { this.woreda = woreda; }

    public String getHouseNumber()                     { return houseNumber; }
    public void   setHouseNumber(String houseNumber)   { this.houseNumber = houseNumber; }

    public String getLandmark()                    { return landmark; }
    public void   setLandmark(String landmark)     { this.landmark = landmark; }

    // ---------------------------------------------------------------
    // Helper — useful for JTable display
    // ---------------------------------------------------------------

    /** Returns display-friendly gender: M → Male, F → Female */
    public String getGenderFull() {
        if ("M".equals(gender)) return "Male";
        if ("F".equals(gender)) return "Female";
        return gender;
    }

    /** Returns Active or Inactive string for display */
    public String getStatusDisplay() {
        return isActive ? "Active" : "Inactive";
    }

    @Override
    public String toString() {
        return "Patient{id='" + patientId + "', name='" + fullName
                + "', age=" + age + ", status=" + getStatusDisplay() + "}";
    }
}
