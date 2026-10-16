public abstract class BookingRecord {
    public BookingRecord(String title, String organizer, String organization, int venueId,
            String date, String startTime, String endTime, String status) {
    }

    public String getTitle() { return "none"; }
    public String getOrganizer() { return "none"; }
    public String getOrganization() { return "none"; }
    public int getVenueId() { return 0; }
    public String getDate() { return "none"; }
    public String getStartTime() { return "none"; }
    public String getEndTime() { return "none"; }
    public String getStatus() { return "none"; }

    public void setTitle(String title) { }
    public void setOrganizer(String organizer) { }
    public void setVenueId(int venueId) { }
    public void setDate(String date) { }
    public void setStartTime(String startTime) { }
    public void setEndTime(String endTime) { }
    public void setStatus(String status) { }

    public String getSortKey(String mode) { return "none"; }
    public int getStartMinutes() { return 0; }
    public int getEndMinutes() { return 0; }
    public String getVenueKey() { return "none"; }

    public abstract String toFileLine();
}
