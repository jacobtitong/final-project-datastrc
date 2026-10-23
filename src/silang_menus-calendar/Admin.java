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
                    + "9 - Deactivate a user\n10 - Summary report\n11 - Calendar views\n12 - Edit profile\n0 - Logout\n");
            int option = InputHelper.getValidInt("--> Option: ", 0, 12);

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
                case 12:
                    editProfile();
                    break;
                case 0:
                    System.out.println("Logged out.");
                    next = new Guest();
                    leave = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 0-12.");
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
        if (venueNameTaken(name, 0)) {
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
        if (venueNameTaken(name, venue.getId())) {
            return;
        }
        int capacity = InputHelper.getValidInt("New capacity (1-5000): ", 1, 5000);
        if (capacity == -1) {
            return;
        }
        if (capacityTooSmall(venue, capacity)) {
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

        // waiting requests that no longer fit the smaller hall are rejected
        int rejected = rejectPending("The venue capacity was reduced.", venue.getId(), null, capacity + 1);
        if (rejected > 0) {
            System.out.println(rejected + " pending request(s) were rejected because they no longer fit.");
        }
    }

    // Venue names must be unique (ignoring case); ownId is the venue being edited, or 0 when adding
    private boolean venueNameTaken(String name, int ownId) {
        for (Venue v : DataManager.getVenues()) {
            if (v.getId() != ownId && v.getName().equalsIgnoreCase(name)) {
                System.out.println("Error: A venue named \"" + v.getName() + "\" already exists (ID " + v.getId() + ").");
                return true;
            }
        }
        return false;
    }

    // The capacity cannot drop below the attendees of an upcoming Approved event in this venue
    private boolean capacityTooSmall(Venue venue, int capacity) {
        int todayNumber = DateHelper.toDayNumber(DateHelper.today());
        for (Event e : venue.getApprovedEvents()) {
            Request r = DataManager.findRequestById(e.getRequestId());
            if (r != null && r.getAttendees() > capacity && DateHelper.toDayNumber(e.getDate()) >= todayNumber) {
                System.out.println("Error: \"" + e.getTitle() + "\" on " + e.getDate() + " has " + r.getAttendees()
                        + " attendees. The capacity cannot be below that.");
                return true;
            }
        }
        return false;
    }

    // Rejects every waiting request that matches the venue (0 = any), the date (null = any) and
    // has at least minAttendees. Clears the undo stack because the scheduling state changed.
    private int rejectPending(String reason, int venueId, String date, int minAttendees) {
        int count = 0;
        for (Request p : DataManager.getQueue().toArrayList()) {
            if ((venueId == 0 || p.getVenueId() == venueId) && (date == null || p.getDate().equals(date))
                    && p.getAttendees() >= minAttendees) {
                p.setStatus("Rejected");
                p.setRejectReason(reason);
                DataManager.getQueue().remove(p.getId());
                DataManager.addStatusLog(p.getId(), "Pending", "Rejected", getId(), reason);
                count++;
            }
        }
        if (count > 0) {
            DataManager.saveRequests();
        }
        DataManager.getDecisionStack().clear();
        return count;
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

        int rejected = rejectPending("The venue was removed.", venue.getId(), null, 0);
        if (rejected > 0) {
            System.out.println(rejected + " pending request(s) for this venue were rejected.");
        }
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
        if (hasApprovedEvent(date) || blackoutTaken(date, 0)) {
            return;
        }
        String reason = InputHelper.getValidText("Reason (5-100 characters): ", 5, 100);
        if (reason == null) {
            return;
        }

        DataManager.getBlackouts().add(new Blackout(DataManager.generateId(), date, reason));
        DataManager.saveBlackouts();
        System.out.println("Blackout date added.");
        rejectForBlackout(date);
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
        if (hasApprovedEvent(date) || blackoutTaken(date, blackout.getId())) {
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
        rejectForBlackout(date);
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

    // Each date can be a blackout only once; ownId is the blackout being edited, or 0 when adding
    private boolean blackoutTaken(String date, int ownId) {
        for (Blackout b : DataManager.getBlackouts()) {
            if (b.getId() != ownId && b.getDate().equals(date)) {
                System.out.println("Error: " + date + " is already a blackout date (" + b.getReason() + ").");
                return true;
            }
        }
        return false;
    }

    private void rejectForBlackout(String date) {
        int rejected = rejectPending("The date became a blackout date.", 0, date, 0);
        if (rejected > 0) {
            System.out.println(rejected + " pending request(s) on " + date + " were rejected.");
        }
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
            if (DateHelper.toMinutes(close) - DateHelper.toMinutes(open) < DataManager.getSettingInt("MIN_DURATION_MIN")) {
                System.out.println("Error: The operating hours must be at least as long as the minimum duration ("
                        + DataManager.getSettingInt("MIN_DURATION_MIN") + " minutes).");
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
            if (index == 2 && value > DateHelper.toMinutes(DataManager.getSettingString("CLOSE_TIME"))
                    - DateHelper.toMinutes(DataManager.getSettingString("OPEN_TIME"))) {
                System.out.println("Error: The minimum duration cannot be longer than the operating hours.");
                return;
            }
            if (index == 3 && value < DataManager.getSettingInt("MIN_DURATION_MIN")) {
                System.out.println("Error: The maximum duration cannot be below the minimum duration.");
                return;
            }
            if (index == 4 && value > DataManager.getSettingInt("MAX_ADVANCE_DAYS")) {
                System.out.println("Error: The minimum lead days cannot be above the maximum advance days.");
                return;
            }
            if (index == 5 && value < DataManager.getSettingInt("MIN_LEAD_DAYS")) {
                System.out.println("Error: The maximum advance days cannot be below the minimum lead days.");
                return;
            }
            DataManager.setSetting(key, "" + value);
        }
        System.out.println("Setting updated.");
    }

    // ===== Profile =====

    private void editProfile() {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== EDIT PROFILE ===");
            System.out.println("Name         : " + getName());
            System.out.println("ID number    : " + getIdNumber());
            System.out.println("Email        : " + getEmail());
            System.out.println("Type         : " + getType());
            System.out.println("Organization : " + getOrganization());
            System.out.print("\n1 - Change name\n2 - Change email\n3 - Change password\n0 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 0, 3);

            switch (option) {
                case 1:
                    String name = InputHelper.getValidName("New name (2-40 characters): ");
                    if (name != null) {
                        setName(name);
                        DataManager.saveUsers();
                        System.out.println("Name updated.");
                    }
                    break;
                case 2:
                    String email = InputHelper.getValidEmail("New email (name" + DataManager.getSettingString("EMAIL_DOMAIN") + "): ");
                    if (email != null) {
                        setEmail(email);
                        DataManager.saveUsers();
                        System.out.println("Email updated.");
                    }
                    break;
                case 3:
                    changePassword();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 0-3.");
            }
        }
    }

    private void changePassword() {
        boolean passwordOk = false;

        String current = InputHelper.getRequiredLine("Current password: ");
        if (current == null) {
            return;
        }
        if (!current.equals(getPassword())) {
            System.out.println("Error: The current password is wrong.");
            return;
        }

        while (!passwordOk) {
            String password = InputHelper.getValidPassword("New password: ", getIdNumber());
            if (password == null) {
                return;
            }
            if (password.equals(getPassword())) {
                System.out.println("Error: The new password must be different from the current one.");
                continue;
            }
            String confirm = InputHelper.getRequiredLine("Confirm new password: ");
            if (confirm == null) {
                return;
            }
            if (confirm.equals(password)) {
                setPassword(password);
                DataManager.saveUsers();
                System.out.println("Password updated.");
                passwordOk = true;
            } else {
                System.out.println("Error: The passwords do not match. Try again.");
            }
        }
    }
}
