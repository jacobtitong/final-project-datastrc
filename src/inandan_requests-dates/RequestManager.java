import java.util.ArrayList;

public class RequestManager {

    public static final String NO_SLOT = "No free slot on this date at this venue.";

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
        Venue venue = DataManager.findVenueById(venueId);
        if (venue == null) {
            return NO_SLOT;
        }
        int buffer = DataManager.getSettingInt("BUFFER_MIN");
        int open = settingMinutes("OPEN_TIME");
        int close = settingMinutes("CLOSE_TIME");
        int duration = endMin - startMin;

        // approved events on that date, already in time order
        ArrayList<Event> dayEvents = new ArrayList<Event>();
        ArrayList<Event> list = venue.getApprovedEvents();
        int idx = SearchHelper.lowerBound(list, date + " 00:00");
        while (idx < list.size() && list.get(idx).getDate().equals(date)) {
            dayEvents.add(list.get(idx));
            idx++;
        }

        // candidate starts: opening time, then the end of each event plus the buffer
        ArrayList<Integer> candidates = new ArrayList<Integer>();
        candidates.add(open);
        for (int i = 0; i < dayEvents.size(); i++) {
            candidates.add(dayEvents.get(i).getEndMinutes() + buffer);
        }

        int best = -1;
        for (int i = 0; i < candidates.size(); i++) {
            int c = candidates.get(i);
            boolean fits = true;

            if (c + duration > close) {
                fits = false;
            }
            for (int j = 0; j < dayEvents.size(); j++) {
                Event e = dayEvents.get(j);
                boolean endedBefore = e.getEndMinutes() + buffer <= c;
                boolean startsAfter = e.getStartMinutes() >= c + duration + buffer;
                if (!endedBefore && !startsAfter) {
                    fits = false;
                }
            }

            // closest to the requested start wins; on a tie the earlier one stays
            if (fits && (best == -1 || Math.abs(c - startMin) < Math.abs(best - startMin))) {
                best = c;
            }
        }

        if (best == -1) {
            return NO_SLOT;
        }
        return DateHelper.toTimeString(best) + "-" + DateHelper.toTimeString(best + duration);
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

    // ---------- helpers ----------

    // reads a time setting such as OPEN_TIME and returns it in minutes
    private static int settingMinutes(String key) {
        return DateHelper.toMinutes(DataManager.getSettingString(key));
    }
}
