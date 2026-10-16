public class Blackout {
    private int id;
    private String date;
    private String reason;

    public Blackout(int id, String date, String reason) {
        this.id = id;
        this.date = date;
        this.reason = reason;
    }

    public int getId() { return id; }
    public String getDate() { return date; }
    public String getReason() { return reason; }

    public void setDate(String date) { this.date = date; }
    public void setReason(String reason) { this.reason = reason; }

    public String toFileLine() {
        return id + ";" + date + ";" + reason;
    }
}
