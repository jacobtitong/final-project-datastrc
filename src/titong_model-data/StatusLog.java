public class StatusLog {
    private int requestId;
    private String timestamp;
    private String oldStatus;
    private String newStatus;
    private int actorId;
    private String remarks;

    public StatusLog(int requestId, String timestamp, String oldStatus, String newStatus,
            int actorId, String remarks) {
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.actorId = actorId;
        this.remarks = remarks;
    }

    public int getRequestId() { return requestId; }
    public String getTimestamp() { return timestamp; }
    public String getOldStatus() { return oldStatus; }
    public String getNewStatus() { return newStatus; }
    public int getActorId() { return actorId; }
    public String getRemarks() { return remarks; }

    public String toFileLine() {
        return requestId + ";" + timestamp + ";" + oldStatus + ";" + newStatus + ";"
                + actorId + ";" + remarks;
    }
}
