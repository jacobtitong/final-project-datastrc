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
        if (!r.isActive()) {
            System.out.println("Error: Your account is deactivated.");
            return;
        }
        int maxPending = DataManager.getSettingInt("MAX_PENDING");
        if (countPending(r.getId()) >= maxPending) {
            System.out.println("Error: You already have " + maxPending + " pending requests. Wait for a decision or cancel one.");
            return;
        }

        System.out.println("\n=== SUBMIT A REQUEST === (type 'back' at any prompt to cancel)");
        Request draft = askDetails(r, 0);
        if (draft == null) {
            return;
        }

        int id = DataManager.generateId();
        Request request = new Request(draft.getTitle(), draft.getOrganizer(), draft.getOrganization(),
                draft.getVenueId(), draft.getDate(), draft.getStartTime(), draft.getEndTime(),
                "Pending", id, r.getId(), draft.getAttendees(), draft.getPurpose(), "none", DataManager.timestamp());
        DataManager.getRequests().add(request);
        DataManager.getQueue().enqueue(request);
        DataManager.saveRequests();
        DataManager.addStatusLog(id, "none", "Pending", r.getId(), "none");
        System.out.println("Request submitted. Your request ID is " + id + ".");
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

    private static int countPending(int requesterId) {
        int count = 0;
        for (int i = 0; i < DataManager.getRequests().size(); i++) {
            Request q = DataManager.getRequests().get(i);
            if (q.getRequesterId() == requesterId && q.getStatus().equals("Pending")) {
                count++;
            }
        }
        return count;
    }

    private static boolean isBlackout(String date) {
        for (int i = 0; i < DataManager.getBlackouts().size(); i++) {
            if (DataManager.getBlackouts().get(i).getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    // returns an error message, or null when the date is fine; userRules = false skips lead time and year checks (Admin)
    private static String checkDate(String date, boolean userRules) {
        if (!DateHelper.isValidDate(date)) {
            return "Error: Date must be a real date in the format YYYY-MM-DD.";
        }
        int lead = DateHelper.toDayNumber(date) - DateHelper.toDayNumber(DateHelper.today());
        if (lead < 0) {
            return "Error: Date cannot be in the past.";
        }
        if (userRules) {
            int minLead = DataManager.getSettingInt("MIN_LEAD_DAYS");
            int maxAdvance = DataManager.getSettingInt("MAX_ADVANCE_DAYS");
            String yearStart = DataManager.getSettingString("YEAR_START");
            String yearEnd = DataManager.getSettingString("YEAR_END");

            if (lead < minLead) {
                return "Error: Requests must be made at least " + minLead + " days before the date.";
            }
            if (lead > maxAdvance) {
                return "Error: Dates more than " + maxAdvance + " days ahead cannot be booked yet.";
            }
            if (date.compareTo(yearStart) < 0 || date.compareTo(yearEnd) > 0) {
                return "Error: Date is outside the school year (" + yearStart + " to " + yearEnd + ").";
            }
        }
        if (isBlackout(date)) {
            return "Error: That date is a blackout date.";
        }
        return null;
    }

    // returns an error message, or null when the times are fine
    private static String checkTimes(String date, String startTime, String endTime) {
        if (!DateHelper.isValidTime(startTime) || !DateHelper.isValidTime(endTime)) {
            return "Error: Times must be in the format HH:MM (24-hour).";
        }
        int start = DateHelper.toMinutes(startTime);
        int end = DateHelper.toMinutes(endTime);
        int open = settingMinutes("OPEN_TIME");
        int close = settingMinutes("CLOSE_TIME");
        int minDuration = DataManager.getSettingInt("MIN_DURATION_MIN");
        int maxDuration = DataManager.getSettingInt("MAX_DURATION_MIN");

        if (start < open || end > close) {
            return "Error: Times must be inside operating hours (" + DataManager.getSettingString("OPEN_TIME")
                    + " to " + DataManager.getSettingString("CLOSE_TIME") + ").";
        }
        if (end <= start) {
            return "Error: End time must be after the start time.";
        }
        if (end - start < minDuration || end - start > maxDuration) {
            return "Error: Duration must be between " + minDuration + " and " + maxDuration + " minutes.";
        }
        if (date.equals(DateHelper.today()) && start <= DateHelper.nowMinutes()) {
            return "Error: That start time has already passed today.";
        }
        return null;
    }

    // returns an error message, or null when the venue can take the attendees
    private static String checkVenueAndAttendees(Venue venue, int attendees) {
        if (venue == null || !venue.isActive()) {
            return "Error: The venue does not exist or is not active.";
        }
        if (attendees > venue.getCapacity()) {
            return "Error: " + attendees + " attendees is more than the venue capacity of " + venue.getCapacity() + ".";
        }
        return null;
    }

    private static boolean isDuplicate(int requesterId, int venueId, String date, String startTime, String endTime, int ignoreRequestId) {
        for (int i = 0; i < DataManager.getRequests().size(); i++) {
            Request q = DataManager.getRequests().get(i);
            if (q.getId() == ignoreRequestId) {
                continue;
            }
            boolean active = q.getStatus().equals("Pending") || q.getStatus().equals("Approved");
            if (active && q.getRequesterId() == requesterId && q.getVenueId() == venueId
                    && q.getDate().equals(date) && q.getStartTime().equals(startTime) && q.getEndTime().equals(endTime)) {
                return true;
            }
        }
        return false;
    }

    // shows the active venues and returns the chosen one, or null if none exist or the person typed back
    private static Venue chooseVenue() {
        ArrayList<Venue> active = new ArrayList<Venue>();
        for (int i = 0; i < DataManager.getVenues().size(); i++) {
            if (DataManager.getVenues().get(i).isActive()) {
                active.add(DataManager.getVenues().get(i));
            }
        }
        if (active.size() == 0) {
            System.out.println("Error: There are no active venues.");
            return null;
        }

        System.out.println("-----------------------------------------");
        System.out.printf("%-4s | %-25s | %-8s%n", "No.", "Venue", "Capacity");
        System.out.println("-----------------------------------------");
        for (int i = 0; i < active.size(); i++) {
            System.out.printf("%-4d | %-25.25s | %-8d%n", i + 1, active.get(i).getName(), active.get(i).getCapacity());
        }
        int choice = InputHelper.getValidInt("--> Venue number: ", 1, active.size());
        if (choice == -1) {
            return null;
        }
        return active.get(choice - 1);
    }

    private static void printConflict(Event conflict, int venueId, String date, int startMin, int endMin) {
        int buffer = DataManager.getSettingInt("BUFFER_MIN");
        Venue venue = DataManager.findVenueById(venueId);
        String venueName = "Unknown";
        if (venue != null) {
            venueName = venue.getName();
        }
        String slot = suggestSlot(venueId, date, startMin, endMin);

        String message = "Conflicts with: " + conflict.getTitle() + " at " + venueName + ", "
                + conflict.getStartTime() + "-" + conflict.getEndTime() + " (" + buffer + "-minute buffer applies). ";
        if (slot.equals(NO_SLOT)) {
            message += NO_SLOT;
        } else {
            message += "Next free slot: " + slot + ".";
        }
        System.out.println(message);
    }

    // asks for every field and runs checks 2-6; returns a filled-in draft (id 0) or null when cancelled or failed
    private static Request askDetails(Requester r, int ignoreRequestId) {
        String title = InputHelper.getValidText("Title (3-60 characters): ", 3, 60);
        if (title == null) {
            System.out.println("Cancelled.");
            return null;
        }

        String organizer = r.getName();
        if (!InputHelper.getYesNo("Is the organizer " + r.getName() + "? (Y/N): ")) {
            organizer = InputHelper.getValidText("Organizer (2-40 characters): ", 2, 40);
            if (organizer == null) {
                System.out.println("Cancelled.");
                return null;
            }
        }

        String purpose = InputHelper.getValidText("Purpose (5-150 characters): ", 5, 150);
        if (purpose == null) {
            System.out.println("Cancelled.");
            return null;
        }

        int attendees = InputHelper.getValidInt("Number of attendees: ", 1, 5000);
        if (attendees == -1) {
            System.out.println("Cancelled.");
            return null;
        }

        Request draft = null;
        boolean done = false;

        while (!done) {
            Venue venue = chooseVenue();
            if (venue == null) {
                System.out.println("Cancelled.");
                return null;
            }
            String date = InputHelper.getValidDate("Date (YYYY-MM-DD): ");
            if (date == null) {
                System.out.println("Cancelled.");
                return null;
            }
            String startTime = InputHelper.getValidTime("Start time (HH:MM, 24-hour): ");
            if (startTime == null) {
                System.out.println("Cancelled.");
                return null;
            }
            String endTime = InputHelper.getValidTime("End time (HH:MM, 24-hour): ");
            if (endTime == null) {
                System.out.println("Cancelled.");
                return null;
            }

            // checks 2 to 5 in order; the first failure stops
            String error = checkDate(date, true);
            if (error == null) {
                error = checkTimes(date, startTime, endTime);
            }
            if (error == null) {
                error = checkVenueAndAttendees(venue, attendees);
            }
            if (error == null && isDuplicate(r.getId(), venue.getId(), date, startTime, endTime, ignoreRequestId)) {
                error = "Error: You already have a pending or approved request for that venue, date and time.";
            }
            if (error != null) {
                System.out.println(error);
                return null;
            }

            // check 6: conflict with an approved event
            int startMin = DateHelper.toMinutes(startTime);
            int endMin = DateHelper.toMinutes(endTime);
            Event conflict = findConflict(venue.getId(), date, startMin, endMin, 0);
            if (conflict != null) {
                printConflict(conflict, venue.getId(), date, startMin, endMin);
                if (!InputHelper.getYesNo("Enter a different venue, date or time? (Y/N): ")) {
                    return null;
                }
            } else {
                draft = new Request(title, organizer, r.getOrganization(), venue.getId(), date, startTime, endTime,
                        "Pending", 0, r.getId(), attendees, purpose, "none", "none");
                done = true;
            }
        }
        return draft;
    }
}
