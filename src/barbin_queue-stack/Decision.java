import java.util.ArrayList;

public class Decision {
    private int requestId;
    private String type;
    private int eventId;
    private ArrayList<Integer> autoRejectedIds;

    public Decision(int requestId, String type, int eventId) {
        this.requestId = requestId;
        this.type = type;
        this.eventId = eventId;
        this.autoRejectedIds = new ArrayList<Integer>();
    }

    public int getRequestId() { return requestId; }
    public String getType() { return type; }
    public int getEventId() { return eventId; }
    public ArrayList<Integer> getAutoRejectedIds() { return autoRejectedIds; }

    public void addAutoRejected(int requestId) {
        autoRejectedIds.add(requestId);
    }

    public String toFileLine() {
        String ids = "none";
        for (int i = 0; i < autoRejectedIds.size(); i++) {
            if (i == 0) {
                ids = "" + autoRejectedIds.get(i);
            } else {
                ids = ids + "," + autoRejectedIds.get(i);
            }
        }
        return requestId + ";" + type + ";" + eventId + ";" + ids;
    }
}
