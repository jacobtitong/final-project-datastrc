import java.util.NoSuchElementException;
import java.util.Scanner;

public class InputHelper {
    static Scanner sc = new Scanner(System.in);

    // Reads one line; if the input has ended (Ctrl+D / Ctrl+Z) the program closes instead of crashing
    private static String readLine() {
        try {
            return sc.nextLine();
        } catch (NoSuchElementException e) {
            System.out.println("\nInput ended. Goodbye!");
            System.exit(0);
            return "";
        }
    }

    // Typing "back" (any case) at a prompt cancels: numbers return -1, text returns null

    public static int getValidInt(String prompt, int min, int max) {
        int number = -1;
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            String input = readLine().trim();

            if (input.isEmpty()) {
                System.out.println("Error: Input cannot be empty or just spaces.");
                continue;
            }
            if (input.equalsIgnoreCase("back")) {
                return -1;
            }

            try {
                number = Integer.parseInt(input);
                if (number < min || number > max) {
                    System.out.println("Error: Please enter a number from " + min + " to " + max + ".");
                } else {
                    isValid = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number, not text.");
            }
        }
        return number;
    }

    public static String getValidName(String prompt) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = readLine().trim();

            if (input.isEmpty()) {
                System.out.println("Error: Input cannot be empty or just spaces.");
                continue;
            }
            if (input.equalsIgnoreCase("back")) {
                return null;
            }

            if (input.length() < 2 || input.length() > 40) {
                System.out.println("Error: A name must be 2 to 40 characters.");
            } else if (!input.matches("[A-Za-z .\\-]+")) {
                System.out.println("Error: Use only letters, spaces, hyphens and periods.");
            } else {
                isValid = true;
            }
        }
        return input;
    }

    public static String getValidText(String prompt, int minLen, int maxLen) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = readLine().trim();

            if (input.isEmpty()) {
                System.out.println("Error: Input cannot be empty or just spaces.");
                continue;
            }
            if (input.equalsIgnoreCase("back")) {
                return null;
            }

            if (input.length() < minLen || input.length() > maxLen) {
                System.out.println("Error: Please enter " + minLen + " to " + maxLen + " characters.");
            } else if (!input.matches("[A-Za-z0-9 .,'&()\\-]+")) {
                System.out.println("Error: Use only letters, digits, spaces and . , - ' & ( )");
            } else {
                isValid = true;
            }
        }
        return input;
    }

    public static String getValidEmail(String prompt) {
        String domain = DataManager.getSettingString("EMAIL_DOMAIN");
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = readLine().trim();

            if (input.isEmpty()) {
                System.out.println("Error: Input cannot be empty or just spaces.");
                continue;
            }
            if (input.equalsIgnoreCase("back")) {
                return null;
            }

            if (input.length() <= domain.length()) {
                System.out.println("Error: An email looks like name" + domain);
                continue;
            }
            String local = input.substring(0, input.length() - domain.length());
            String ending = input.substring(input.length() - domain.length());

            if (!local.matches("[A-Za-z0-9._]+") || !ending.equalsIgnoreCase(domain)) {
                System.out.println("Error: An email looks like name" + domain + " (letters, digits, . and _ before the @).");
            } else if (DataManager.findUserByEmail(input) != null) {
                System.out.println("Error: That email is already registered.");
            } else {
                isValid = true;
            }
        }
        return input;
    }

    public static String getValidIdNumber(String prompt) {
        int digits = DataManager.getSettingInt("ID_DIGITS");
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = readLine().trim();

            if (input.isEmpty()) {
                System.out.println("Error: Input cannot be empty or just spaces.");
                continue;
            }
            if (input.equalsIgnoreCase("back")) {
                return null;
            }

            if (!input.matches("\\d{" + digits + "}")) {
                System.out.println("Error: The ID number must be exactly " + digits + " digits.");
            } else if (idNumberExists(input)) {
                System.out.println("Error: That ID number is already registered.");
            } else {
                isValid = true;
            }
        }
        return input;
    }

    private static boolean idNumberExists(String idNumber) {
        for (User u : DataManager.getUsers()) {
            if (u.getIdNumber().equals(idNumber)) {
                return true;
            }
        }
        return false;
    }

    public static String getValidPassword(String prompt) {
        return getValidPassword(prompt, "");
    }

    // Same as above, and the password may not equal the given ID number
    public static String getValidPassword(String prompt, String idNumber) {
        int minLength = DataManager.getSettingInt("MIN_PASSWORD");
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = readLine();

            if (input.trim().isEmpty()) {
                System.out.println("Error: Input cannot be empty or just spaces.");
                continue;
            }
            if (input.equalsIgnoreCase("back")) {
                return null;
            }

            if (input.length() < minLength) {
                System.out.println("Error: The password must be at least " + minLength + " characters.");
            } else if (input.contains(" ")) {
                System.out.println("Error: The password cannot contain spaces.");
            } else if (input.contains(";")) {
                System.out.println("Error: The password cannot contain a semicolon.");
            } else if (input.equals(idNumber)) {
                System.out.println("Error: The password cannot be the same as your ID number.");
            } else {
                isValid = true;
            }
        }
        return input;
    }

    // Any date that exists (used for browsing the calendar and filters)
    public static String getValidAnyDate(String prompt) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = readLine().trim();

            if (input.isEmpty()) {
                System.out.println("Error: Input cannot be empty or just spaces.");
                continue;
            }
            if (input.equalsIgnoreCase("back")) {
                return null;
            }

            if (DateHelper.isValidDate(input)) {
                isValid = true;
            } else {
                System.out.println("Error: Enter a real date in the format YYYY-MM-DD.");
            }
        }
        return input;
    }

    // A date that exists and is not in the past
    public static String getValidDate(String prompt) {
        String date = "";
        boolean isValid = false;

        while (!isValid) {
            date = getValidAnyDate(prompt);
            if (date == null) {
                return null;
            }
            if (DateHelper.toDayNumber(date) < DateHelper.toDayNumber(DateHelper.today())) {
                System.out.println("Error: The date cannot be in the past.");
            } else {
                isValid = true;
            }
        }
        return date;
    }

    // Blank is allowed (returns ""); "back" returns null
    public static String getOptionalDate(String prompt) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = readLine().trim();

            if (input.isEmpty()) {
                return "";
            }
            if (input.equalsIgnoreCase("back")) {
                return null;
            }

            if (DateHelper.isValidDate(input)) {
                isValid = true;
            } else {
                System.out.println("Error: Enter a real date in the format YYYY-MM-DD, or press Enter to skip.");
            }
        }
        return input;
    }

    // A time inside the operating hours (OPEN_TIME to CLOSE_TIME)
    public static String getValidTime(String prompt) {
        int open = DateHelper.toMinutes(DataManager.getSettingString("OPEN_TIME"));
        int close = DateHelper.toMinutes(DataManager.getSettingString("CLOSE_TIME"));
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = readLine().trim();

            if (input.isEmpty()) {
                System.out.println("Error: Input cannot be empty or just spaces.");
                continue;
            }
            if (input.equalsIgnoreCase("back")) {
                return null;
            }

            if (!DateHelper.isValidTime(input)) {
                System.out.println("Error: Enter a time in the format HH:MM (24-hour), 00:00 to 23:59.");
            } else if (DateHelper.toMinutes(input) < open || DateHelper.toMinutes(input) > close) {
                System.out.println("Error: The time must be inside operating hours ("
                        + DataManager.getSettingString("OPEN_TIME") + " to " + DataManager.getSettingString("CLOSE_TIME") + ").");
            } else {
                isValid = true;
            }
        }
        return input;
    }

    // Any HH:MM time (used when the Admin edits the operating hours)
    public static String getValidClockTime(String prompt) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = readLine().trim();

            if (input.isEmpty()) {
                System.out.println("Error: Input cannot be empty or just spaces.");
                continue;
            }
            if (input.equalsIgnoreCase("back")) {
                return null;
            }

            if (DateHelper.isValidTime(input)) {
                isValid = true;
            } else {
                System.out.println("Error: Enter a time in the format HH:MM (24-hour), 00:00 to 23:59.");
            }
        }
        return input;
    }

    // An email domain such as @dlsl.edu.ph
    public static String getValidDomain(String prompt) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = readLine().trim();

            if (input.isEmpty()) {
                System.out.println("Error: Input cannot be empty or just spaces.");
                continue;
            }
            if (input.equalsIgnoreCase("back")) {
                return null;
            }

            if (input.matches("@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)+")) {
                isValid = true;
            } else {
                System.out.println("Error: The domain must start with @, for example @dlsl.edu.ph");
            }
        }
        return input;
    }

    // Student / Faculty / Staff / Other
    public static String getValidType(String prompt) {
        System.out.print("\n1 - Student\n2 - Faculty\n3 - Staff\n4 - Other\n");
        int number = getValidInt(prompt, 1, 4);

        if (number == -1) {
            return null;
        }
        if (number == 1) {
            return "Student";
        } else if (number == 2) {
            return "Faculty";
        } else if (number == 3) {
            return "Staff";
        }
        return "Other";
    }

    // A required line with no other rules (login, confirm password)
    public static String getRequiredLine(String prompt) {
        String input = "";
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            input = readLine().trim();

            if (input.isEmpty()) {
                System.out.println("Error: Input cannot be empty or just spaces.");
                continue;
            }
            if (input.equalsIgnoreCase("back")) {
                return null;
            }
            isValid = true;
        }
        return input;
    }

    // A line that may be left blank (returns ""); "back" returns null
    public static String getOptionalLine(String prompt) {
        System.out.print(prompt);
        String input = readLine().trim();

        if (input.equalsIgnoreCase("back")) {
            return null;
        }
        return input;
    }

    // Y or N; typing "back" counts as No
    public static boolean getYesNo(String prompt) {
        boolean answer = false;
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            String input = readLine().trim();

            if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) {
                answer = true;
                isValid = true;
            } else if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no") || input.equalsIgnoreCase("back")) {
                answer = false;
                isValid = true;
            } else {
                System.out.println("Error: Please answer Y or N.");
            }
        }
        return answer;
    }
}
