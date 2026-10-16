public class Request extends BookingRecord {
    public Request(String title, String organizer, String organization, int venueId,
            String date, String startTime, String endTime, String status,
            int id, int requesterId, int attendees, String purpose,
            String rejectReason, String submittedAt) {
        super(title, organizer, organization, venueId, date, startTime, endTime, status);
    }

    public int getId() { return 0; }
    public int getRequesterId() { return 0; }
    public int getAttendees() { return 0; }
    public String getPurpose() { return "none"; }
    public String getRejectReason() { return "none"; }
    public String getSubmittedAt() { return "none"; }

    public void setAttendees(int attendees) { }
    public void setPurpose(String purpose) { }
    public void setRejectReason(String rejectReason) { }

    @Override
    public String toFileLine() { return "none"; }
}
