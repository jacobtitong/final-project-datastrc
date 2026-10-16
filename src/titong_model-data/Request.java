public class Request extends BookingRecord {
    private int id;
    private int requesterId;
    private int attendees;
    private String purpose;
    private String rejectReason;
    private String submittedAt;

    public Request(String title, String organizer, String organization, int venueId,
            String date, String startTime, String endTime, String status,
            int id, int requesterId, int attendees, String purpose,
            String rejectReason, String submittedAt) {
        super(title, organizer, organization, venueId, date, startTime, endTime, status);
        this.id = id;
        this.requesterId = requesterId;
        this.attendees = attendees;
        this.purpose = purpose;
        this.rejectReason = rejectReason;
        this.submittedAt = submittedAt;
    }

    public int getId() { return id; }
    public int getRequesterId() { return requesterId; }
    public int getAttendees() { return attendees; }
    public String getPurpose() { return purpose; }
    public String getRejectReason() { return rejectReason; }
    public String getSubmittedAt() { return submittedAt; }

    public void setAttendees(int attendees) { this.attendees = attendees; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    @Override
    public String toFileLine() {
        return id + ";" + requesterId + ";" + getTitle() + ";" + getOrganizer() + ";"
                + getOrganization() + ";" + purpose + ";" + attendees + ";" + getVenueId() + ";"
                + getDate() + ";" + getStartTime() + ";" + getEndTime() + ";" + getStatus() + ";"
                + rejectReason + ";" + submittedAt;
    }
}
