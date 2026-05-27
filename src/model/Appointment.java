package model;

/**
 * Appointment.java
 * -----------------
 * Represents a joined row from Appointment + Patient + Professional + Service_Category.
 * Used by AppointmentDAO and displayed in the scheduling panel.
 */
public class Appointment {

    // From Appointment table
    private String appointmentId;
    private String patientId;
    private String professionalId;
    private String categoryId;
    private String appointmentDate;
    private String appointmentTime;
    private String createdDate;
    private String lastModified;
    private String cancelledDate;
    private int    durationHours;
    private String status;              // Scheduled, In Progress, Completed, Cancelled
    private String specialInstructions;

    // From Patient + Demographic_Info (joined)
    private String patientName;
    private String patientPhone;
    private int    patientAge;

    // From Healthcare_Professional + Demographic_Info (joined)
    private String professionalName;
    private String profession;

    // From Service_Category (joined)
    private String categoryName;
    private double baseRate;

    // From Payment (joined — optional, may be null)
    private String paymentStatus;
    private double paymentAmount;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    public Appointment() {}

    // Constructor for creating a new appointment
    public Appointment(String appointmentId, String patientId, String professionalId,
                       String categoryId, String appointmentDate, String appointmentTime,
                       int durationHours, String specialInstructions) {
        this.appointmentId       = appointmentId;
        this.patientId           = patientId;
        this.professionalId      = professionalId;
        this.categoryId          = categoryId;
        this.appointmentDate     = appointmentDate;
        this.appointmentTime     = appointmentTime;
        this.durationHours       = durationHours;
        this.specialInstructions = specialInstructions;
        this.status              = "Scheduled"; // default when created
    }

    // ---------------------------------------------------------------
    // Getters & Setters — Appointment table fields
    // ---------------------------------------------------------------

    public String getAppointmentId()                           { return appointmentId; }
    public void   setAppointmentId(String appointmentId)       { this.appointmentId = appointmentId; }

    public String getPatientId()                       { return patientId; }
    public void   setPatientId(String patientId)       { this.patientId = patientId; }

    public String getProfessionalId()                              { return professionalId; }
    public void   setProfessionalId(String professionalId)         { this.professionalId = professionalId; }

    public String getCategoryId()                        { return categoryId; }
    public void   setCategoryId(String categoryId)       { this.categoryId = categoryId; }

    public String getAppointmentDate()                           { return appointmentDate; }
    public void   setAppointmentDate(String appointmentDate)     { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime()                           { return appointmentTime; }
    public void   setAppointmentTime(String appointmentTime)     { this.appointmentTime = appointmentTime; }

    public String getCreatedDate()                     { return createdDate; }
    public void   setCreatedDate(String createdDate)   { this.createdDate = createdDate; }

    public String getLastModified()                        { return lastModified; }
    public void   setLastModified(String lastModified)     { this.lastModified = lastModified; }

    public String getCancelledDate()                         { return cancelledDate; }
    public void   setCancelledDate(String cancelledDate)     { this.cancelledDate = cancelledDate; }

    public int  getDurationHours()                   { return durationHours; }
    public void setDurationHours(int durationHours)  { this.durationHours = durationHours; }

    public String getStatus()                { return status; }
    public void   setStatus(String status)   { this.status = status; }

    public String getSpecialInstructions()                             { return specialInstructions; }
    public void   setSpecialInstructions(String specialInstructions)   { this.specialInstructions = specialInstructions; }

    // ---------------------------------------------------------------
    // Getters & Setters — Joined fields from other tables
    // ---------------------------------------------------------------

    public String getPatientName()                     { return patientName; }
    public void   setPatientName(String patientName)   { this.patientName = patientName; }

    public String getPatientPhone()                        { return patientPhone; }
    public void   setPatientPhone(String patientPhone)     { this.patientPhone = patientPhone; }

    public int  getPatientAge()              { return patientAge; }
    public void setPatientAge(int patientAge){ this.patientAge = patientAge; }

    public String getProfessionalName()                            { return professionalName; }
    public void   setProfessionalName(String professionalName)     { this.professionalName = professionalName; }

    public String getProfession()                        { return profession; }
    public void   setProfession(String profession)       { this.profession = profession; }

    public String getCategoryName()                        { return categoryName; }
    public void   setCategoryName(String categoryName)     { this.categoryName = categoryName; }

    public double getBaseRate()                  { return baseRate; }
    public void   setBaseRate(double baseRate)   { this.baseRate = baseRate; }

    public String getPaymentStatus()                         { return paymentStatus; }
    public void   setPaymentStatus(String paymentStatus)     { this.paymentStatus = paymentStatus; }

    public double getPaymentAmount()                     { return paymentAmount; }
    public void   setPaymentAmount(double paymentAmount) { this.paymentAmount = paymentAmount; }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /** Calculates the estimated cost based on professional hourly rate * duration */
    public double getCalculatedCost(double hourlyRate) {
        return hourlyRate * durationHours;
    }

    /** Returns true if this appointment can still be cancelled */
    public boolean isCancellable() {
        return "Scheduled".equals(status) || "In Progress".equals(status);
    }

    /** Returns true if this appointment is done */
    public boolean isCompleted() {
        return "Completed".equals(status);
    }

    @Override
    public String toString() {
        return "Appointment{id='" + appointmentId + "', patient='" + patientName
                + "', date='" + appointmentDate + "', status='" + status + "'}";
    }
}
