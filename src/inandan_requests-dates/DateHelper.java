import java.time.LocalDate;
import java.time.LocalTime;

public class DateHelper {
    public static boolean isLeapYear(int y) {
        return (y % 4 == 0 && y % 100 != 0) || y % 400 == 0;
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
        if (d == null || !d.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }
        int y = Integer.parseInt(d.substring(0, 4));
        int m = Integer.parseInt(d.substring(5, 7));
        int day = Integer.parseInt(d.substring(8, 10));
        if (y < 1970 || y > 2100) {
            return false;
        }
        if (m < 1 || m > 12) {
            return false;
        }
        return day >= 1 && day <= daysInMonth(y, m);
    }

    public static boolean isValidTime(String t) {
        return t != null && t.matches("([01][0-9]|2[0-3]):[0-5][0-9]");
    }

    public static int toMinutes(String t) {
        if (!isValidTime(t)) {
            return 0;
        }
        int hours = Integer.parseInt(t.substring(0, 2));
        int minutes = Integer.parseInt(t.substring(3, 5));
        return hours * 60 + minutes;
    }

    public static String toTimeString(int min) {
        return String.format("%02d:%02d", min / 60, min % 60);
    }

    // Days since 2000-01-01 (day 0); dates before the year 2000 give negative numbers
    public static int toDayNumber(String d) {
        if (!isValidDate(d)) {
            return 0;
        }
        int year = Integer.parseInt(d.substring(0, 4));
        int month = Integer.parseInt(d.substring(5, 7));
        int day = Integer.parseInt(d.substring(8, 10));
        int total = 0;

        for (int y = 2000; y < year; y++) {
            total += daysInYear(y);
        }
        for (int y = year; y < 2000; y++) {
            total -= daysInYear(y);
        }
        for (int m = 1; m < month; m++) {
            total += daysInMonth(year, m);
        }
        return total + day - 1;
    }

    public static String fromDayNumber(int n) {
        int days = n;
        int year = 2000;

        while (days < 0) {
            year--;
            days += daysInYear(year);
        }
        while (days >= daysInYear(year)) {
            days -= daysInYear(year);
            year++;
        }

        int month = 1;
        while (days >= daysInMonth(year, month)) {
            days -= daysInMonth(year, month);
            month++;
        }
        return String.format("%04d-%02d-%02d", year, month, days + 1);
    }

    private static int daysInYear(int y) {
        if (isLeapYear(y)) {
            return 366;
        }
        return 365;
    }

    // 0 = Sunday ... 6 = Saturday (2000-01-01 was a Saturday)
    public static int dayOfWeek(String d) {
        return ((toDayNumber(d) + 6) % 7 + 7) % 7;
    }

    public static String today() {
        return LocalDate.now().toString();
    }

    public static int nowMinutes() {
        LocalTime now = LocalTime.now();
        return now.getHour() * 60 + now.getMinute();
    }
}
