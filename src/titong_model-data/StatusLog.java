public class StatusLog {
    public StatusLog(int requestId, String timestamp, String oldStatus, String newStatus,
            int actorId, String remarks) {
    }

    public int getRequestId() { return 0; }
    public String getTimestamp() { return "none"; }
    public String getOldStatus() { return "none"; }
    public String getNewStatus() { return "none"; }
    public int getActorId() { return 0; }
    public String getRemarks() { return "none"; }

    public String toFileLine() { return "none"; }
}
