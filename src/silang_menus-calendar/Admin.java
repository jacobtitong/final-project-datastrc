import java.util.ArrayList;

public class Admin extends User {
    private static String[] settingKeys = {"OPEN_TIME", "CLOSE_TIME", "MIN_DURATION_MIN", "MAX_DURATION_MIN",
            "MIN_LEAD_DAYS", "MAX_ADVANCE_DAYS", "BUFFER_MIN", "MAX_PENDING", "MIN_PASSWORD", "ID_DIGITS",
            "EMAIL_DOMAIN", "YEAR_START", "YEAR_END"};
    private static String[] settingRanges = {"HH:MM, before closing time", "HH:MM, after opening time",
            "15-720, not above the maximum", "15-720, not below the minimum", "0-60", "30-730", "0-120", "1-10",
            "6-20", "4-12", "must start with @", "a date before the year end", "a date after the year start"};
    private static int[] settingMins = {0, 0, 15, 15, 0, 30, 0, 1, 6, 4, 0, 0, 0};
    private static int[] settingMaxs = {0, 0, 720, 720, 60, 730, 120, 10, 20, 12, 0, 0, 0};

    public Admin(int id, String role, String idNumber, String name, String email,
            String password, String type, String organization, boolean active) {
        super(id, role, idNumber, name, email, password, type, organization, active);
    }

    @Override
    public User displayMenu() {
        User next = this;
        boolean leave = false;

        while (!leave) {
            System.out.print("\n=== ADMIN MENU ===");
            System.out.print("\n1 - Review queue\n2 - Undo last decision\n3 - View, search, filter and sort all records\n"
                    + "4 - Reschedule an event\n5 - Cancel an event\n6 - Venues\n7 - Blackout dates\n8 - Settings\n"
                    + "9 - Deactivate a user\n10 - Summary report\n11 - Calendar views\n0 - Logout\n");
            int option = InputHelper.getValidInt("--> Option: ", 0, 11);

            switch (option) {
                case 1:
                    RequestManager.reviewQueue(this);
                    break;
                case 2:
                    RequestManager.undoLastDecision(this);
                    break;
                case 3:
                    CalendarManager.viewAllRecords();
                    break;
                case 4:
                    RequestManager.rescheduleEvent(this);
                    break;
                case 5:
                    RequestManager.cancelEvent(this);
                    break;
                case 6:
                    venuesMenu();
                    break;
                case 7:
                    blackoutsMenu();
                    break;
                case 8:
                    settingsMenu();
                    break;
                case 9:
                    RequestManager.deactivateUser(this);
                    break;
                case 10:
                    ReportManager.printSummaryReport();
                    break;
                case 11:
                    CalendarManager.showCalendarMenu();
                    break;
                case 0:
                    System.out.println("Logged out.");
                    next = new Guest();
                    leave = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 0-11.");
            }
        }
        return next;
    }

    // ===== Venues =====

    private void venuesMenu() {
        boolean back = false;

        while (!back) {
            System.out.print("\n=== VENUES ===");
            System.out.print("\n1 - Add a venue\n2 - List venues\n3 - Edit a venue\n4 - Remove a venue\n0 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 0, 4);

            switch (option) {
                case 1:
                    addVenue();
                    break;
                case 2:
                    printVenueTable(DataManager.getVenues());
                    break;
                case 3:
                    editVenue();
                    break;
                case 4:
                    removeVenue();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 0-4.");
            }
        }
    }

    private void addVenue() {
        System.out.println("\n=== ADD A VENUE === (type 'back' at any prompt to cancel)");

        String name = InputHelper.getValidText("Venue name (2-40 characters): ", 2, 40);
        if (name == null) {
            return;
        }
        int capacity = InputHelper.getValidInt("Capacity (1-5000): ", 1, 5000);
        if (capacity == -1) {
            return;
        }
        String details = InputHelper.getValidText("Details (2-100 characters): ", 2, 100);
        if (details == null) {
            return;
        }

        DataManager.getVenues().add(new Venue(DataManager.generateId(), name, capacity, details, true));
        DataManager.saveVenues();
        System.out.println("Venue added.");
    }

    private void editVenue() {
        if (DataManager.getVenues().size() == 0) {
            System.out.println("There are no venues yet.");
            return;
        }
        System.out.println("\n=== EDIT A VENUE === (type 'back' at any prompt to cancel)");
        printVenueTable(DataManager.getVenues());
        int number = InputHelper.getValidInt("--> Venue number: ", 1, DataManager.getVenues().size());
        if (number == -1) {
            return;
        }
        Venue venue = DataManager.getVenues().get(number - 1);

        String name = InputHelper.getValidText("New name (2-40 characters): ", 2, 40);
        if (name == null) {
            return;
        }
        int capacity = InputHelper.getValidInt("New capacity (1-5000): ", 1, 5000);
        if (capacity == -1) {
            return;
        }
        String details = InputHelper.getValidText("New details (2-100 characters): ", 2, 100);
        if (details == null) {
            return;
        }

        venue.setName(name);
        venue.setCapacity(capacity);
        venue.setDetails(details);
        DataManager.saveVenues();
        System.out.println("Venue updated.");
    }

    private void removeVenue() {
        ArrayList<Venue> active = new ArrayList<Venue>();
        for (Venue v : DataManager.getVenues()) {
            if (v.isActive()) {
                active.add(v);
            }
        }
        if (active.size() == 0) {
            System.out.println("There are no active venues to remove.");
            return;
        }
        System.out.println("\n=== REMOVE A VENUE === (type 'back' at any prompt to cancel)");
        printVenueTable(active);
        int number = InputHelper.getValidInt("--> Venue number: ", 1, active.size());
        if (number == -1) {
            return;
        }
        Venue venue = active.get(number - 1);

        // a venue with an Approved event dated today or later cannot be removed
        int todayNumber = DateHelper.toDayNumber(DateHelper.today());
        for (Event e : venue.getApprovedEvents()) {
            if (DateHelper.toDayNumber(e.getDate()) >= todayNumber) {
                System.out.println("Error: " + venue.getName() + " has an approved event on or after today and cannot be removed.");
                return;
            }
        }

        boolean sure = InputHelper.getYesNo("Remove " + venue.getName() + "? (Y/N): ");
        if (!sure) {
            System.out.println("Removal aborted.");
            return;
        }
        venue.setActive(false);
        DataManager.saveVenues();
        System.out.println(venue.getName() + " is removed. It can no longer be booked.");
    }

    private void printVenueTable(ArrayList<Venue> list) {
        if (list.size() == 0) {
            System.out.println("There are no venues yet.");
            return;
        }
        System.out.println("---------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-6s | %-20s | %-8s | %-25s | %-8s%n", "No.", "ID", "Name", "Capacity", "Details", "Status");
        System.out.println("---------------------------------------------------------------------------------");
        for (int i = 0; i < list.size(); i++) {
            Venue v = list.get(i);
            String status = "Inactive";
            if (v.isActive()) {
                status = "Active";
            }
            System.out.printf("%-4d | %-6d | %-20.20s | %-8d | %-25.25s | %-8s%n",
                    i + 1, v.getId(), v.getName(), v.getCapacity(), v.getDetails(), status);
        }
    }

    // ===== Blackout dates =====

    private void blackoutsMenu() {
        boolean back = false;

        while (!back) {
            System.out.print("\n=== BLACKOUT DATES ===");
            System.out.print("\n1 - Add a blackout date\n2 - List blackout dates\n3 - Edit a blackout date\n4 - Remove a blackout date\n0 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 0, 4);

            switch (option) {
                case 1:
                    addBlackout();
                    break;
                case 2:
                    printBlackoutTable();
                    break;
                case 3:
                    editBlackout();
                    break;
                case 4:
                    removeBlackout();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 0-4.");
            }
        }
    }

    private void addBlackout() {
        System.out.println("\n=== ADD A BLACKOUT DATE === (type 'back' at any prompt to cancel)");

        String date = InputHelper.getValidDate("Date (YYYY-MM-DD): ");
        if (date == null) {
            return;
        }
        if (hasApprovedEvent(date)) {
            return;
        }
        String reason = InputHelper.getValidText("Reason (5-100 characters): ", 5, 100);
        if (reason == null) {
            return;
        }

        DataManager.getBlackouts().add(new Blackout(DataManager.generateId(), date, reason));
        DataManager.saveBlackouts();
        System.out.println("Blackout date added.");
    }

    private void editBlackout() {
        if (DataManager.getBlackouts().size() == 0) {
            System.out.println("There are no blackout dates yet.");
            return;
        }
        System.out.println("\n=== EDIT A BLACKOUT DATE === (type 'back' at any prompt to cancel)");
        printBlackoutTable();
        int number = InputHelper.getValidInt("--> Blackout number: ", 1, DataManager.getBlackouts().size());
        if (number == -1) {
            return;
        }
        Blackout blackout = DataManager.getBlackouts().get(number - 1);

        String date = InputHelper.getValidDate("New date (YYYY-MM-DD): ");
        if (date == null) {
            return;
        }
        if (hasApprovedEvent(date)) {
            return;
        }
        String reason = InputHelper.getValidText("New reason (5-100 characters): ", 5, 100);
        if (reason == null) {
            return;
        }

        blackout.setDate(date);
        blackout.setReason(reason);
        DataManager.saveBlackouts();
        System.out.println("Blackout date updated.");
    }

    private void removeBlackout() {
        if (DataManager.getBlackouts().size() == 0) {
            System.out.println("There are no blackout dates yet.");
            return;
        }
        System.out.println("\n=== REMOVE A BLACKOUT DATE === (type 'back' at any prompt to cancel)");
        printBlackoutTable();
        int number = InputHelper.getValidInt("--> Blackout number: ", 1, DataManager.getBlackouts().size());
        if (number == -1) {
            return;
        }

        boolean sure = InputHelper.getYesNo("Remove the blackout on " + DataManager.getBlackouts().get(number - 1).getDate() + "? (Y/N): ");
        if (!sure) {
            System.out.println("Removal aborted.");
            return;
        }
        DataManager.getBlackouts().remove(number - 1);
        DataManager.saveBlackouts();
        System.out.println("Blackout date removed.");
    }

    // A blackout cannot be set on a date that has an Approved event
    private boolean hasApprovedEvent(String date) {
        for (Event e : DataManager.getEvents()) {
            if (e.getStatus().equals("Approved") && e.getDate().equals(date)) {
                System.out.println("Error: \"" + e.getTitle() + "\" is an approved event on " + date + ". Reschedule or cancel it first.");
                return true;
            }
        }
        return false;
    }

    private void printBlackoutTable() {
        if (DataManager.getBlackouts().size() == 0) {
            System.out.println("There are no blackout dates yet.");
            return;
        }
        System.out.println("--------------------------------------------------------------");
        System.out.printf("%-4s | %-6s | %-10s | %-30s%n", "No.", "ID", "Date", "Reason");
        System.out.println("--------------------------------------------------------------");
        for (int i = 0; i < DataManager.getBlackouts().size(); i++) {
            Blackout b = DataManager.getBlackouts().get(i);
            System.out.printf("%-4d | %-6d | %-10s | %-30.30s%n", i + 1, b.getId(), b.getDate(), b.getReason());
        }
    }

    // ===== Settings =====

    private void settingsMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== SETTINGS ===");
            System.out.println("-------------------------------------------------------------------------------");
            System.out.printf("%-4s | %-18s | %-12s | %-35s%n", "No.", "Setting", "Value", "Allowed values");
            System.out.println("-------------------------------------------------------------------------------");
            for (int i = 0; i < settingKeys.length; i++) {
                System.out.printf("%-4d | %-18s | %-12.12s | %-35.35s%n",
                        i + 1, settingKeys[i], DataManager.getSettingString(settingKeys[i]), settingRanges[i]);
            }

            int number = InputHelper.getValidInt("--> Setting number to change (0 to go back): ", 0, settingKeys.length);
            if (number == 0 || number == -1) {
                back = true;
            } else {
                editSetting(number - 1);
            }
        }
    }

    private void editSetting(int index) {
        String key = settingKeys[index];

        if (index == 0 || index == 1) {
            String time = InputHelper.getValidClockTime("New " + key + " (HH:MM): ");
            if (time == null) {
                return;
            }
            String open = DataManager.getSettingString("OPEN_TIME");
            String close = DataManager.getSettingString("CLOSE_TIME");
            if (index == 0) {
                open = time;
            } else {
                close = time;
            }
            if (DateHelper.toMinutes(open) >= DateHelper.toMinutes(close)) {
                System.out.println("Error: The opening time must be before the closing time.");
                return;
            }
            DataManager.setSetting(key, time);
        } else if (index == 10) {
            String domain = InputHelper.getValidDomain("New " + key + " (starts with @): ");
            if (domain == null) {
                return;
            }
            DataManager.setSetting(key, domain);
        } else if (index == 11 || index == 12) {
            String date = InputHelper.getValidAnyDate("New " + key + " (YYYY-MM-DD): ");
            if (date == null) {
                return;
            }
            String start = DataManager.getSettingString("YEAR_START");
            String end = DataManager.getSettingString("YEAR_END");
            if (index == 11) {
                start = date;
            } else {
                end = date;
            }
            if (start.compareTo(end) >= 0) {
                System.out.println("Error: The year start must be before the year end.");
                return;
            }
            DataManager.setSetting(key, date);
        } else {
            int value = InputHelper.getValidInt("New " + key + " (" + settingMins[index] + "-" + settingMaxs[index] + "): ",
                    settingMins[index], settingMaxs[index]);
            if (value == -1) {
                return;
            }
            if (index == 2 && value > DataManager.getSettingInt("MAX_DURATION_MIN")) {
                System.out.println("Error: The minimum duration cannot be above the maximum duration.");
                return;
            }
            if (index == 3 && value < DataManager.getSettingInt("MIN_DURATION_MIN")) {
                System.out.println("Error: The maximum duration cannot be below the minimum duration.");
                return;
            }
            DataManager.setSetting(key, "" + value);
        }
        System.out.println("Setting updated.");
    }
}
