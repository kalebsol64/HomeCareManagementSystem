package model;

/**
 * Payment.java
 * -------------
 * Represents a row from the Payment table joined with
 * Appointment + Patient Demographic info for display.
 */
public class Payment {

    private String paymentId;
    private String appointmentId;
    private double amount;
    private String paymentDate;
    private String paymentMethod;
    private String status;
    private String transactionReference;
    private String receiptNumber;

    // Joined from Appointment → Patient → Demographic_Info
    private String patientName;
    private String professionalName;
    private String categoryName;
    private String appointmentDate;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------
    public Payment() {}

    public Payment(String paymentId, String appointmentId, double amount,
                   String paymentDate, String paymentMethod, String status) {
        this.paymentId     = paymentId;
        this.appointmentId = appointmentId;
        this.amount        = amount;
        this.paymentDate   = paymentDate;
        this.paymentMethod = paymentMethod;
        this.status        = status;
    }

    // ---------------------------------------------------------------
    // Getters & Setters
    // ---------------------------------------------------------------
    public String getPaymentId()                       { return paymentId; }
    public void   setPaymentId(String paymentId)       { this.paymentId = paymentId; }

    public String getAppointmentId()                           { return appointmentId; }
    public void   setAppointmentId(String appointmentId)       { this.appointmentId = appointmentId; }

    public double getAmount()                  { return amount; }
    public void   setAmount(double amount)     { this.amount = amount; }

    public String getPaymentDate()                     { return paymentDate; }
    public void   setPaymentDate(String paymentDate)   { this.paymentDate = paymentDate; }

    public String getPaymentMethod()                           { return paymentMethod; }
    public void   setPaymentMethod(String paymentMethod)       { this.paymentMethod = paymentMethod; }

    public String getStatus()                { return status; }
    public void   setStatus(String status)   { this.status = status; }

    public String getTransactionReference()                                { return transactionReference; }
    public void   setTransactionReference(String transactionReference)     { this.transactionReference = transactionReference; }

    public String getReceiptNumber()                           { return receiptNumber; }
    public void   setReceiptNumber(String receiptNumber)       { this.receiptNumber = receiptNumber; }

    public String getPatientName()                     { return patientName; }
    public void   setPatientName(String patientName)   { this.patientName = patientName; }

    public String getProfessionalName()                            { return professionalName; }
    public void   setProfessionalName(String professionalName)     { this.professionalName = professionalName; }

    public String getCategoryName()                        { return categoryName; }
    public void   setCategoryName(String categoryName)     { this.categoryName = categoryName; }

    public String getAppointmentDate()                         { return appointmentDate; }
    public void   setAppointmentDate(String appointmentDate)   { this.appointmentDate = appointmentDate; }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    public String getAmountDisplay() { return String.format("%.2f ETB", amount); }

    @Override
    public String toString() {
        return "Payment{id='" + paymentId + "', appointment='" + appointmentId
                + "', amount=" + amount + ", status='" + status + "'}";
    }
}
