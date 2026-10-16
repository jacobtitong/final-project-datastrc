public class Event extends BookingRecord {
    public Event(String title, String organizer, String organization, int venueId,
            String date, String startTime, String endTime, String status,
            int id, int requestId, String cancelReason) {
        super(title, organizer, organization, venueId, date, startTime, endTime, status);
    }

    public int getId() { return 0; }
    public int getRequestId() { return 0; }
    public String getCancelReason() { return "none"; }

    public void setCancelReason(String cancelReason) { }

    @Override
    public String toFileLine() { return "none"; }
}
