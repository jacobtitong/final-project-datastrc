import java.util.ArrayList;

public class RequestManager {

    public static Event findConflict(int venueId, String date, int startMin, int endMin, int ignoreEventId) {
        Venue venue = DataManager.findVenueById(venueId);
        if (venue == null) {
            return null;
        }
        int buffer = DataManager.getSettingInt("BUFFER_MIN");

        // copy of the venue list without the event being moved
        ArrayList<Event> list = new ArrayList<Event>();
        for (int i = 0; i < venue.getApprovedEvents().size(); i++) {
            Event e = venue.getApprovedEvents().get(i);
            if (e.getId() != ignoreEventId) {
                list.add(e);
            }
        }

        int idx = SearchHelper.lowerBound(list, date + " " + DateHelper.toTimeString(startMin));

        if (idx > 0) {
            Event prev = list.get(idx - 1);
            if (prev.getDate().equals(date) && startMin < prev.getEndMinutes() + buffer) {
                return prev;
            }
        }
        if (idx < list.size()) {
            Event next = list.get(idx);
            if (next.getDate().equals(date) && endMin + buffer > next.getStartMinutes()) {
                return next;
            }
        }
        return null;
    }

    public static String suggestSlot(int venueId, String date, int startMin, int endMin) {
        return "";
    }

    public static void submitRequest(Requester r) {
    }

    public static void editRequest(Requester r) {
    }

    public static void cancelRequest(User actor) {
    }

    public static void reviewQueue(Admin a) {
    }

    public static void undoLastDecision(Admin a) {
    }

    public static void rescheduleEvent(Admin a) {
    }

    public static void cancelEvent(Admin a) {
    }

    public static void deactivateUser(User actor) {
    }
}
