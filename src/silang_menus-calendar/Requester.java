import java.util.ArrayList;

public class Requester extends User {
    public Requester(int id, String role, String idNumber, String name, String email,
            String password, String type, String organization, boolean active) {
        super(id, role, idNumber, name, email, password, type, organization, active);
    }

    @Override
    public User displayMenu() {
        User next = this;
        boolean leave = false;

        while (!leave) {
            System.out.print("\n=== REQUESTER MENU ===");
            System.out.print("\n1 - Calendar\n2 - Event details\n3 - Search and filter\n4 - Submit a request\n"
                    + "5 - My requests\n6 - Edit a pending request\n7 - Cancel a request\n8 - Edit profile\n"
                    + "9 - Deactivate my account\n0 - Logout\n");
            int option = InputHelper.getValidInt("--> Option: ", 0, 9);

            switch (option) {
                case 1:
                    CalendarManager.showCalendarMenu();
                    break;
                case 2:
                    CalendarManager.showEventDetails();
                    break;
                case 3:
                    CalendarManager.searchAndFilter(false);
                    break;
                case 4:
                    RequestManager.submitRequest(this);
                    break;
                case 5:
                    viewMyRequests();
                    break;
                case 6:
                    RequestManager.editRequest(this);
                    break;
                case 7:
                    RequestManager.cancelRequest(this);
                    break;
                case 8:
                    editProfile();
                    break;
                case 9:
                    RequestManager.deactivateUser(this);
                    if (!isActive()) {
                        System.out.println("You have been logged out.");
                        next = new Guest();
                        leave = true;
                    }
                    break;
                case 0:
                    System.out.println("Logged out.");
                    next = new Guest();
                    leave = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 0-9.");
            }
        }
        return next;
    }

    private void viewMyRequests() {
        ArrayList<BookingRecord> mine = new ArrayList<BookingRecord>();
        for (Request r : DataManager.getRequests()) {
            if (r.getRequesterId() == getId()) {
                mine.add(r);
            }
        }
        if (mine.size() == 0) {
            System.out.println("You have no requests yet.");
            return;
        }
        MergeSorter.mergeSort(mine, "DATETIME");

        System.out.println("\n=== MY REQUESTS ===");
        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-6s | %-25s | %-15s | %-10s | %-11s | %-9s%n",
                "No.", "ID", "Title", "Venue", "Date", "Time", "Status");
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (int i = 0; i < mine.size(); i++) {
            Request r = (Request) mine.get(i);
            System.out.printf("%-4d | %-6d | %-25.25s | %-15.15s | %-10s | %-11s | %-9s%n",
                    i + 1, r.getId(), r.getTitle(), CalendarManager.venueName(r.getVenueId()), r.getDate(),
                    r.getStartTime() + "-" + r.getEndTime(), r.getStatus());
        }

        int number = InputHelper.getValidInt("--> Request number to see its history (or 'back'): ", 1, mine.size());
        if (number == -1) {
            return;
        }
        showHistory((Request) mine.get(number - 1));
    }

    private void showHistory(Request r) {
        System.out.println("\n=== HISTORY OF REQUEST " + r.getId() + " (" + r.getTitle() + ") ===");
        System.out.println("Status: " + r.getStatus());
        if (r.getStatus().equals("Rejected")) {
            System.out.println("Reject reason: " + r.getRejectReason());
        }

        boolean found = false;
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("%-16s | %-10s | %-10s | %-15s | %s%n", "When", "From", "To", "By", "Remarks");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (StatusLog log : DataManager.getStatusLogs()) {
            if (log.getRequestId() == r.getId()) {
                User actor = DataManager.findUserById(log.getActorId());
                String actorName = "Unknown";
                if (actor != null) {
                    actorName = actor.getName();
                }
                System.out.printf("%-16s | %-10s | %-10s | %-15.15s | %s%n",
                        log.getTimestamp(), log.getOldStatus(), log.getNewStatus(), actorName, log.getRemarks());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No history recorded.");
        }
    }

    private void editProfile() {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== EDIT PROFILE ===");
            System.out.println("Name         : " + getName());
            System.out.println("ID number    : " + getIdNumber());
            System.out.println("Email        : " + getEmail());
            System.out.println("Type         : " + getType());
            System.out.println("Organization : " + getOrganization());
            System.out.print("\n1 - Change name\n2 - Change type\n3 - Change organization\n4 - Change password\n0 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 0, 4);

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
                    String type = InputHelper.getValidType("--> New type: ");
                    if (type != null) {
                        setType(type);
                        DataManager.saveUsers();
                        System.out.println("Type updated.");
                    }
                    break;
                case 3:
                    String organization = InputHelper.getValidText("New organization or department (2-40 characters): ", 2, 40);
                    if (organization != null) {
                        setOrganization(organization);
                        DataManager.saveUsers();
                        System.out.println("Organization updated.");
                    }
                    break;
                case 4:
                    changePassword();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 0-4.");
            }
        }
    }

    private void changePassword() {
        boolean passwordOk = false;

        while (!passwordOk) {
            String password = InputHelper.getValidPassword("New password: ", getIdNumber());
            if (password == null) {
                return;
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
