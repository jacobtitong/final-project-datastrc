import java.util.ArrayList;

public class SearchHelper {
    // Binary Search: first index whose "date startTime" is >= key (list.size() if none)
    public static int lowerBound(ArrayList<Event> list, String key) {
        int low = 0;
        int high = list.size();

        while (low < high) {
            int mid = (low + high) / 2;
            Event e = list.get(mid);
            String eventKey = e.getDate() + " " + e.getStartTime();
            if (eventKey.compareTo(key) < 0) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }

    // Binary Search by id on the events master list (sorted by id); -1 if absent
    public static int findEventIndex(ArrayList<Event> list, int id) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int midId = list.get(mid).getId();
            if (midId == id) {
                return mid;
            } else if (midId < id) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    // Linear Search: records whose title or organizer contains the keyword (ignoring case)
    public static ArrayList<BookingRecord> searchKeyword(ArrayList<BookingRecord> list, String keyword) {
        ArrayList<BookingRecord> result = new ArrayList<BookingRecord>();
        String lowered = "";
        if (keyword != null) {
            lowered = keyword.toLowerCase();
        }

        for (BookingRecord b : list) {
            if (b.getTitle().toLowerCase().contains(lowered) || b.getOrganizer().toLowerCase().contains(lowered)) {
                result.add(b);
            }
        }
        return result;
    }
}
