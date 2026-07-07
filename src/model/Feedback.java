package model;

/**
 * Feedback.java
 * --------------
 * Represents a row from Service_Feedback joined with
 * Appointment + Patient + Professional info for display.
 */
public class Feedback {

    private String  feedbackId;
    private String  appointmentId;
    private int     rating;
    private String  comments;
    private String  feedbackDate;
    private boolean followupRequired;
    private String  serviceSatisfaction;

    // Joined fields
    private String patientName;
    private String professionalName;
    private String categoryName;
    private String appointmentDate;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------
    public Feedback() {}

    public Feedback(String feedbackId, String appointmentId, int rating,
                    String comments, String feedbackDate,
                    boolean followupRequired, String serviceSatisfaction) {
        this.feedbackId          = feedbackId;
        this.appointmentId       = appointmentId;
        this.rating              = rating;
        this.comments            = comments;
        this.feedbackDate        = feedbackDate;
        this.followupRequired    = followupRequired;
        this.serviceSatisfaction = serviceSatisfaction;
    }

    // ---------------------------------------------------------------
    // Getters & Setters
    // ---------------------------------------------------------------
    public String getFeedbackId()                        { return feedbackId; }
    public void   setFeedbackId(String feedbackId)       { this.feedbackId = feedbackId; }

    public String getAppointmentId()                           { return appointmentId; }
    public void   setAppointmentId(String appointmentId)       { this.appointmentId = appointmentId; }

    public int  getRating()              { return rating; }
    public void setRating(int rating)    { this.rating = rating; }

    public String getComments()                    { return comments; }
    public void   setComments(String comments)     { this.comments = comments; }

    public String getFeedbackDate()                        { return feedbackDate; }
    public void   setFeedbackDate(String feedbackDate)     { this.feedbackDate = feedbackDate; }

    public boolean isFollowupRequired()                        { return followupRequired; }
    public void    setFollowupRequired(boolean followupRequired){ this.followupRequired = followupRequired; }

    public String getServiceSatisfaction()                             { return serviceSatisfaction; }
    public void   setServiceSatisfaction(String serviceSatisfaction)   { this.serviceSatisfaction = serviceSatisfaction; }

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

    /** Returns star string e.g. "★★★★☆" for rating 4 */
    public String getRatingStars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++)
            sb.append(i <= rating ? "★" : "☆");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Feedback{id='" + feedbackId + "', appointment='" + appointmentId
                + "', rating=" + rating + ", satisfaction='" + serviceSatisfaction + "'}";
    }
}
