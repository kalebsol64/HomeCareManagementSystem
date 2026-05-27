package model;

/**
 * Professional.java
 * ------------------
 * Represents a joined row from Healthcare_Professional + Demographic_Info + Address.
 * Used by ProfessionalDAO and displayed in the Caregiver/Professional management panel.
 */
public class Professional {

    // From Healthcare_Professional table
    private String  professionalId;
    private String  userId;            // linked Users table FK (nullable)
    private String  demographicId;
    private String  addressId;
    private String  profession;        // Doctor, Nurse, Caregiver, Physiotherapist
    private String  specialization;
    private String  licenseNumber;
    private int     yearsExperience;
    private double  hourlyRate;
    private boolean isAvailable;
    private String  joinDate;

    // From Demographic_Info table (joined)
    private String fullName;
    private String phone;
    private String email;
    private int    age;
    private String gender;

    // From Address table (joined)
    private String subCity;
    private String woreda;
    private String houseNumber;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public Professional() {}

    public Professional(String professionalId, String userId, String demographicId,
                        String addressId, String profession, String specialization,
                        String licenseNumber, int yearsExperience, double hourlyRate) {
        this.professionalId  = professionalId;
        this.userId          = userId;
        this.demographicId   = demographicId;
        this.addressId       = addressId;
        this.profession      = profession;
        this.specialization  = specialization;
        this.licenseNumber   = licenseNumber;
        this.yearsExperience = yearsExperience;
        this.hourlyRate      = hourlyRate;
        this.isAvailable     = true;
    }

    // ---------------------------------------------------------------
    // Getters & Setters — Healthcare_Professional fields
    // ---------------------------------------------------------------

    public String getProfessionalId()                              { return professionalId; }
    public void   setProfessionalId(String professionalId)         { this.professionalId = professionalId; }

    public String getUserId()                  { return userId; }
    public void   setUserId(String userId)     { this.userId = userId; }

    public String getDemographicId()                         { return demographicId; }
    public void   setDemographicId(String demographicId)     { this.demographicId = demographicId; }

    public String getAddressId()                       { return addressId; }
    public void   setAddressId(String addressId)       { this.addressId = addressId; }

    public String getProfession()                        { return profession; }
    public void   setProfession(String profession)       { this.profession = profession; }

    public String getSpecialization()                            { return specialization; }
    public void   setSpecialization(String specialization)       { this.specialization = specialization; }

    public String getLicenseNumber()                           { return licenseNumber; }
    public void   setLicenseNumber(String licenseNumber)       { this.licenseNumber = licenseNumber; }

    public int  getYearsExperience()                       { return yearsExperience; }
    public void setYearsExperience(int yearsExperience)    { this.yearsExperience = yearsExperience; }

    public double getHourlyRate()                    { return hourlyRate; }
    public void   setHourlyRate(double hourlyRate)   { this.hourlyRate = hourlyRate; }

    public boolean isAvailable()                     { return isAvailable; }
    public void    setAvailable(boolean available)   { this.isAvailable = available; }

    public String getJoinDate()                    { return joinDate; }
    public void   setJoinDate(String joinDate)     { this.joinDate = joinDate; }

    // ---------------------------------------------------------------
    // Getters & Setters — Demographic_Info fields (from JOIN)
    // ---------------------------------------------------------------

    public String getFullName()                    { return fullName; }
    public void   setFullName(String fullName)     { this.fullName = fullName; }

    public String getPhone()               { return phone; }
    public void   setPhone(String phone)   { this.phone = phone; }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }

    public int  getAge()        { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender()                { return gender; }
    public void   setGender(String gender)   { this.gender = gender; }

    // ---------------------------------------------------------------
    // Getters & Setters — Address fields (from JOIN)
    // ---------------------------------------------------------------

    public String getSubCity()                 { return subCity; }
    public void   setSubCity(String subCity)   { this.subCity = subCity; }

    public String getWoreda()                { return woreda; }
    public void   setWoreda(String woreda)   { this.woreda = woreda; }

    public String getHouseNumber()                     { return houseNumber; }
    public void   setHouseNumber(String houseNumber)   { this.houseNumber = houseNumber; }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /** Returns availability as a readable string */
    public String getAvailabilityDisplay() {
        return isAvailable ? "Available" : "Unavailable";
    }

    /** Returns hourly rate formatted as a string e.g. "800.00 ETB" */
    public String getHourlyRateDisplay() {
        return String.format("%.2f ETB", hourlyRate);
    }

    @Override
    public String toString() {
        return "Professional{id='" + professionalId + "', name='" + fullName
                + "', profession='" + profession + "', available=" + isAvailable + "}";
    }
}
