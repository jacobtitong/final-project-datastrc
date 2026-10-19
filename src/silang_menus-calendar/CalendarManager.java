import java.util.ArrayList;

public class CalendarManager {
    private static String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    // ===== Calendar menu =====

    public static void showCalendarMenu() {
        boolean back = false;

        while (!back) {
            System.out.print("\n=== CALENDAR ===");
            System.out.print("\n1 - Day view\n2 - Week view\n3 - Month view\n4 - View by venue\n0 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 0, 4);

            switch (option) {
                case 1:
                    dayView();
                    break;
                case 2:
                    weekView();
                    break;
                case 3:
                    monthView();
                    break;
                case 4:
                    venueView();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 0-4.");
            }
        }
    }

    private static void dayView() {
        String date = InputHelper.getValidAnyDate("Date (YYYY-MM-DD): ");
        if (date == null) {
            return;
        }
        printDay(date, getEventsOn(date));
    }

    private static void weekView() {
        String date = InputHelper.getValidAnyDate("Any date in the week (YYYY-MM-DD): ");
        if (date == null) {
            return;
        }
        int startDay = DateHelper.toDayNumber(date) - DateHelper.dayOfWeek(date);
        System.out.println("\n=== WEEK: " + DateHelper.fromDayNumber(startDay) + " to "
                + DateHelper.fromDayNumber(startDay + 6) + " ===");

        for (int i = 0; i < 7; i++) {
            String day = DateHelper.fromDayNumber(startDay + i);
            printDay(day, getEventsOn(day));
        }
    }

    private static void monthView() {
        String date = InputHelper.getValidAnyDate("Any date in the month (YYYY-MM-DD): ");
        if (date == null) {
            return;
        }
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        String prefix = date.substring(0, 8);
        int total = 0;

        System.out.println("\n=== MONTH: " + date.substring(0, 7) + " ===");
        for (int day = 1; day <= DateHelper.daysInMonth(year, month); day++) {
            String d = prefix + String.format("%02d", day);
            ArrayList<BookingRecord> events = getEventsOn(d);
            if (events.size() > 0) {
                printDay(d, events);
                total += events.size();
            }
        }
        if (total == 0) {
            System.out.println("No events this month.");
        }
    }

    private static void venueView() {
        ArrayList<Venue> venues = new ArrayList<Venue>();
        for (Venue v : DataManager.getVenues()) {
            if (v.isActive()) {
                venues.add(v);
            }
        }
        if (venues.size() == 0) {
            System.out.println("There are no venues yet.");
            return;
        }

        System.out.println("\nVenues:");
        for (int i = 0; i < venues.size(); i++) {
            System.out.println((i + 1) + " - " + venues.get(i).getName());
        }
        int number = InputHelper.getValidInt("--> Venue number: ", 1, venues.size());
        if (number == -1) {
            return;
        }

        Venue venue = venues.get(number - 1);
        ArrayList<Event> list = venue.getApprovedEvents();
        int index = SearchHelper.lowerBound(list, DateHelper.today() + " 00:00");

        System.out.println("\n=== UPCOMING EVENTS AT " + venue.getName() + " ===");
        if (index >= list.size()) {
            System.out.println("No upcoming events.");
            return;
        }
        System.out.println("------------------------------------------------------------------------------");
        System.out.printf("%-6s | %-10s | %-11s | %-25s | %-15s%n", "ID", "Date", "Time", "Title", "Organizer");
        System.out.println("------------------------------------------------------------------------------");
        while (index < list.size()) {
            Event e = list.get(index);
            System.out.printf("%-6d | %-10s | %-11s | %-25.25s | %-15.15s%n",
                    e.getId(), e.getDate(), e.getStartTime() + "-" + e.getEndTime(), e.getTitle(), e.getOrganizer());
            index++;
        }
    }

    // Approved events of every active venue on this date, sorted by time
    private static ArrayList<BookingRecord> getEventsOn(String date) {
        ArrayList<BookingRecord> found = new ArrayList<BookingRecord>();

        for (Venue v : DataManager.getVenues()) {
            if (v.isActive()) {
                ArrayList<Event> list = v.getApprovedEvents();
                int index = SearchHelper.lowerBound(list, date + " 00:00");
                while (index < list.size() && list.get(index).getDate().equals(date)) {
                    found.add(list.get(index));
                    index++;
                }
            }
        }
        MergeSorter.mergeSort(found, "DATETIME");
        return found;
    }

    private static void printDay(String date, ArrayList<BookingRecord> events) {
        System.out.println("\n=== " + date + " (" + dayNames[DateHelper.dayOfWeek(date)] + ") ===");
        if (events.size() == 0) {
            System.out.println("No events");
            return;
        }
        System.out.println("------------------------------------------------------------------------------");
        System.out.printf("%-6s | %-11s | %-25s | %-15s | %-15s%n", "ID", "Time", "Title", "Organizer", "Venue");
        System.out.println("------------------------------------------------------------------------------");
        for (BookingRecord b : events) {
            System.out.printf("%-6d | %-11s | %-25.25s | %-15.15s | %-15.15s%n",
                    getRecordId(b), b.getStartTime() + "-" + b.getEndTime(), b.getTitle(),
                    b.getOrganizer(), venueName(b.getVenueId()));
        }
    }

    // ===== Event details =====

    public static void showEventDetails() {
        int id = InputHelper.getValidInt("Event ID (or 'back'): ", 1, Integer.MAX_VALUE);
        if (id == -1) {
            return;
        }

        Event ev = DataManager.findEventById(id);
        if (ev == null || !ev.getStatus().equals("Approved")) {
            System.out.println("Error: No approved event has that ID.");
            return;
        }

        Venue venue = DataManager.findVenueById(ev.getVenueId());
        String capacity = "?";
        if (venue != null) {
            capacity = "" + venue.getCapacity();
        }

        System.out.println("\n=== EVENT " + ev.getId() + " ===");
        System.out.println("Title        : " + ev.getTitle());
        System.out.println("Organizer    : " + ev.getOrganizer());
        System.out.println("Organization : " + ev.getOrganization());
        System.out.println("Venue        : " + venueName(ev.getVenueId()) + " (capacity " + capacity + ")");
        System.out.println("Date         : " + ev.getDate() + " (" + dayNames[DateHelper.dayOfWeek(ev.getDate())] + ")");
        System.out.println("Time         : " + ev.getStartTime() + "-" + ev.getEndTime());
        System.out.println("Status       : " + ev.getStatus());
    }

    // ===== Search and filter =====

    public static void searchAndFilter(boolean adminView) {
        System.out.println("\n=== SEARCH AND FILTER === (type 'back' at any prompt to cancel)");

        // Admin chooses requests or events; everyone else sees Approved events only
        boolean searchRequests = false;
        if (adminView) {
            System.out.print("1 - Search requests\n2 - Search events\n");
            int source = InputHelper.getValidInt("--> Search in: ", 1, 2);
            if (source == -1) {
                return;
            }
            searchRequests = source == 1;
        }

        ArrayList<BookingRecord> records = new ArrayList<BookingRecord>();
        if (searchRequests) {
            for (Request r : DataManager.getRequests()) {
                records.add(r);
            }
        } else {
            for (Event e : DataManager.getEvents()) {
                if (adminView || e.getStatus().equals("Approved")) {
                    records.add(e);
                }
            }
        }

        String keyword = InputHelper.getOptionalLine("Keyword in title or organizer (Enter to skip): ");
        if (keyword == null) {
            return;
        }

        int venueId = 0;
        if (DataManager.getVenues().size() > 0) {
            System.out.println("\n0 - Any venue");
            for (int i = 0; i < DataManager.getVenues().size(); i++) {
                System.out.println((i + 1) + " - " + DataManager.getVenues().get(i).getName());
            }
            int choice = InputHelper.getValidInt("--> Venue: ", 0, DataManager.getVenues().size());
            if (choice == -1) {
                return;
            }
            if (choice > 0) {
                venueId = DataManager.getVenues().get(choice - 1).getId();
            }
        }

        String fromDate = "";
        String toDate = "";
        boolean rangeOk = false;
        while (!rangeOk) {
            fromDate = InputHelper.getOptionalDate("From date YYYY-MM-DD (Enter to skip): ");
            if (fromDate == null) {
                return;
            }
            toDate = InputHelper.getOptionalDate("To date YYYY-MM-DD (Enter to skip): ");
            if (toDate == null) {
                return;
            }
            if (fromDate.length() > 0 && toDate.length() > 0 && fromDate.compareTo(toDate) > 0) {
                System.out.println("Error: The 'from' date is after the 'to' date.");
            } else {
                rangeOk = true;
            }
        }

        String organization = InputHelper.getOptionalLine("Organization or department (Enter to skip): ");
        if (organization == null) {
            return;
        }

        String status = "";
        if (adminView) {
            String[] statusNames = {"", "Pending", "Approved", "Rejected", "Cancelled"};
            System.out.print("\n0 - Any status\n1 - Pending\n2 - Approved\n3 - Rejected\n4 - Cancelled\n");
            int statusChoice = InputHelper.getValidInt("--> Status: ", 0, 4);
            if (statusChoice == -1) {
                return;
            }
            status = statusNames[statusChoice];
        }

        int sortChoice = chooseSort();
        if (sortChoice == -1) {
            return;
        }

        // Linear Search first, then the other filters
        if (keyword.length() > 0) {
            records = SearchHelper.searchKeyword(records, keyword);
        }
        ArrayList<BookingRecord> results = new ArrayList<BookingRecord>();
        for (BookingRecord b : records) {
            boolean keep = true;
            if (venueId != 0 && b.getVenueId() != venueId) {
                keep = false;
            }
            if (fromDate.length() > 0 && b.getDate().compareTo(fromDate) < 0) {
                keep = false;
            }
            if (toDate.length() > 0 && b.getDate().compareTo(toDate) > 0) {
                keep = false;
            }
            if (organization.length() > 0 && !b.getOrganization().toLowerCase().contains(organization.toLowerCase())) {
                keep = false;
            }
            if (status.length() > 0 && !b.getStatus().equals(status)) {
                keep = false;
            }
            if (keep) {
                results.add(b);
            }
        }

        sortRecords(results, sortChoice);
        if (results.size() == 0) {
            System.out.println("No results found.");
        } else {
            printRecordTable(results);
            System.out.println(results.size() + " result(s) found.");
        }
    }

    // ===== Admin: all requests and events =====

    public static void viewAllRecords() {
        boolean back = false;

        while (!back) {
            System.out.print("\n=== ALL RECORDS ===");
            System.out.print("\n1 - View all requests\n2 - View all events\n3 - Search and filter\n0 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 0, 3);

            switch (option) {
                case 1:
                    viewAll(true);
                    break;
                case 2:
                    viewAll(false);
                    break;
                case 3:
                    searchAndFilter(true);
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 0-3.");
            }
        }
    }

    private static void viewAll(boolean requests) {
        ArrayList<BookingRecord> list = new ArrayList<BookingRecord>();
        if (requests) {
            for (Request r : DataManager.getRequests()) {
                list.add(r);
            }
        } else {
            for (Event e : DataManager.getEvents()) {
                list.add(e);
            }
        }
        if (list.size() == 0) {
            System.out.println("There are no records yet.");
            return;
        }

        int sortChoice = chooseSort();
        if (sortChoice == -1) {
            return;
        }
        sortRecords(list, sortChoice);
        printRecordTable(list);
        System.out.println(list.size() + " record(s).");
    }

    // ===== Shared helpers (also used by the other menu classes) =====

    private static int chooseSort() {
        System.out.print("\nSort by:\n1 - Date and time\n2 - Venue\n3 - Organization\n4 - Status\n");
        return InputHelper.getValidInt("--> Sort by: ", 1, 4);
    }

    // Time order first, then the chosen mode: stability keeps each group in time order
    private static void sortRecords(ArrayList<BookingRecord> list, int choice) {
        MergeSorter.mergeSort(list, "DATETIME");
        if (choice == 2) {
            MergeSorter.mergeSort(list, "VENUE");
        } else if (choice == 3) {
            MergeSorter.mergeSort(list, "ORGANIZATION");
        } else if (choice == 4) {
            MergeSorter.mergeSort(list, "STATUS");
        }
    }

    private static int getRecordId(BookingRecord b) {
        if (b instanceof Request) {
            return ((Request) b).getId();
        }
        return ((Event) b).getId();
    }

    public static String venueName(int venueId) {
        Venue v = DataManager.findVenueById(venueId);
        if (v == null) {
            return "Unknown";
        }
        return v.getName();
    }

    public static void printRecordTable(ArrayList<BookingRecord> list) {
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-6s | %-25s | %-15s | %-15s | %-10s | %-11s | %-9s%n",
                "ID", "Title", "Organizer", "Venue", "Date", "Time", "Status");
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        for (BookingRecord b : list) {
            System.out.printf("%-6d | %-25.25s | %-15.15s | %-15.15s | %-10s | %-11s | %-9s%n",
                    getRecordId(b), b.getTitle(), b.getOrganizer(), venueName(b.getVenueId()), b.getDate(),
                    b.getStartTime() + "-" + b.getEndTime(), b.getStatus());
        }
    }
}
