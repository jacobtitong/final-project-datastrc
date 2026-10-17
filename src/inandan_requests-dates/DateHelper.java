public class DateHelper {

    public static boolean isLeapYear(int y) {
        if (y % 400 == 0) {
            return true;
        }
        if (y % 100 == 0) {
            return false;
        }
        return y % 4 == 0;
    }

    public static int daysInMonth(int y, int m) {
        switch (m) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                if (isLeapYear(y)) {
                    return 29;
                }
                return 28;
            default:
                return 0;
        }
    }

    public static boolean isValidDate(String d) {
        if (d == null) {
            return false;
        }
        if (!d.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }
        try {
            String[] p = d.split("-");
            int y = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);
            int day = Integer.parseInt(p[2]);
            if (y < 2000) {
                return false;   // day numbers start at 2000-01-01
            }
            if (m < 1 || m > 12) {
                return false;
            }
            if (day < 1 || day > daysInMonth(y, m)) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidTime(String t) {
        return false;
    }

    public static int toMinutes(String t) {
        return 0;
    }

    public static String toTimeString(int min) {
        return "";
    }

    public static int toDayNumber(String d) {
        return 0;
    }

    public static String fromDayNumber(int n) {
        return "";
    }

    public static int dayOfWeek(String d) {
        return 0;
    }

    public static String today() {
        return "";
    }

    public static int nowMinutes() {
        return 0;
    }
}
