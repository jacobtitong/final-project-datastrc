public class Guest extends User {
    public Guest() {
        super(0, "GUEST", "none", "Guest", "none", "none", "none", "none", true);
    }

    @Override
    public User displayMenu() {
        User next = this;
        User loggedIn = null;
        boolean leave = false;

        while (!leave) {
            System.out.print("\n=== GUEST MENU ===");
            System.out.print("\n1 - Calendar\n2 - Event details\n3 - Search and filter\n4 - Login\n5 - Register\n0 - Exit\n");
            int option = InputHelper.getValidInt("--> Option: ", 0, 5);

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
                    loggedIn = login();
                    if (loggedIn != null) {
                        next = loggedIn;
                        leave = true;
                    }
                    break;
                case 5:
                    register();
                    break;
                case 0:
                    next = null;
                    leave = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 0-5.");
            }
        }
        return next;
    }

    private void register() {
        System.out.println("\n=== REGISTER === (type 'back' at any prompt to cancel)");

        String name = InputHelper.getValidName("Full name (2-40 characters): ");
        if (name == null) {
            return;
        }
        String idNumber = InputHelper.getValidIdNumber("ID number (" + DataManager.getSettingInt("ID_DIGITS") + " digits): ");
        if (idNumber == null) {
            return;
        }
        String email = InputHelper.getValidEmail("Email (name" + DataManager.getSettingString("EMAIL_DOMAIN") + "): ");
        if (email == null) {
            return;
        }

        String password = "";
        boolean passwordOk = false;
        while (!passwordOk) {
            password = InputHelper.getValidPassword("Password (at least " + DataManager.getSettingInt("MIN_PASSWORD") + " characters, with a letter and a digit, no spaces): ", idNumber);
            if (password == null) {
                return;
            }
            String confirm = InputHelper.getRequiredLine("Confirm password: ");
            if (confirm == null) {
                return;
            }
            if (confirm.equals(password)) {
                passwordOk = true;
            } else {
                System.out.println("Error: The passwords do not match. Try again.");
            }
        }

        String type = InputHelper.getValidType("--> Type: ");
        if (type == null) {
            return;
        }
        String organization = InputHelper.getValidText("Organization or department (2-40 characters): ", 2, 40);
        if (organization == null) {
            return;
        }

        Requester newUser = new Requester(DataManager.generateId(), "REQUESTER", idNumber, name, email,
                password, type, organization, true);
        DataManager.getUsers().add(newUser);
        DataManager.saveUsers();
        System.out.println("Registration successful. You can now log in.");
    }

    private User login() {
        System.out.println("\n=== LOGIN === (type 'back' at any prompt to cancel)");

        while (true) {
            String email = InputHelper.getRequiredLine("Email: ");
            if (email == null) {
                return null;
            }
            String password = InputHelper.getRequiredLine("Password: ");
            if (password == null) {
                return null;
            }

            User user = DataManager.findUserByEmail(email);
            if (user == null || !user.getPassword().equals(password)) {
                System.out.println("Error: Wrong email or password. Please try again.");
            } else if (!user.isActive()) {
                System.out.println("Account is deactivated.");
            } else {
                System.out.println("Welcome, " + user.getName() + "!");
                return user;
            }
        }
    }
}
