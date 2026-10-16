public class Event extends BookingRecord {
    private int id;
    private int requestId;
    private String cancelReason;

    public Event(String title, String organizer, String organization, int venueId,
            String date, String startTime, String endTime, String status,
            int id, int requestId, String cancelReason) {
        super(title, organizer, organization, venueId, date, startTime, endTime, status);
        this.id = id;
        this.requestId = requestId;
        this.cancelReason = cancelReason;
    }

    public int getId() { return id; }
    public int getRequestId() { return requestId; }
    public String getCancelReason() { return cancelReason; }

    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    @Override
    public String toFileLine() {
        return id + ";" + requestId + ";" + getTitle() + ";" + getOrganizer() + ";"
                + getOrganization() + ";" + getVenueId() + ";" + getDate() + ";"
                + getStartTime() + ";" + getEndTime() + ";" + getStatus() + ";" + cancelReason;
    }
}
