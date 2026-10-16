public abstract class BookingRecord {
    private String title;
    private String organizer;
    private String organization;
    private int venueId;
    private String date;
    private String startTime;
    private String endTime;
    private String status;

    public BookingRecord(String title, String organizer, String organization, int venueId,
            String date, String startTime, String endTime, String status) {
        this.title = title;
        this.organizer = organizer;
        this.organization = organization;
        this.venueId = venueId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public String getTitle() { return title; }
    public String getOrganizer() { return organizer; }
    public String getOrganization() { return organization; }
    public int getVenueId() { return venueId; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStatus() { return status; }

    public void setTitle(String title) { this.title = title; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
    public void setVenueId(int venueId) { this.venueId = venueId; }
    public void setDate(String date) { this.date = date; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setStatus(String status) { this.status = status; }

    public int getStartMinutes() {
        return DateHelper.toMinutes(startTime);
    }

    public int getEndMinutes() {
        return DateHelper.toMinutes(endTime);
    }

    public String getSortKey(String mode) {
        return date + " " + startTime;
    }

    // Name of the venue (looked up by venueId); Step 4 uses it for the VENUE mode
    public String getVenueKey() {
        Venue venue = DataManager.findVenueById(venueId);
        if (venue == null) {
            return "none";
        }
        return venue.getName();
    }

    public abstract String toFileLine();
}
