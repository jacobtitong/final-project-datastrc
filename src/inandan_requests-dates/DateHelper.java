import java.time.LocalDate;
import java.time.LocalTime;

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
        if (t == null) {
            return false;
        }
        if (!t.matches("\\d{2}:\\d{2}")) {
            return false;
        }
        int hour = Integer.parseInt(t.substring(0, 2));
        int minute = Integer.parseInt(t.substring(3, 5));
        return hour <= 23 && minute <= 59;
    }

    public static int toMinutes(String t) {
        int hour = Integer.parseInt(t.substring(0, 2));
        int minute = Integer.parseInt(t.substring(3, 5));
        return hour * 60 + minute;
    }

    public static String toTimeString(int min) {
        return String.format("%02d:%02d", min / 60, min % 60);
    }

    public static int toDayNumber(String d) {
        String[] p = d.split("-");
        int y = Integer.parseInt(p[0]);
        int m = Integer.parseInt(p[1]);
        int day = Integer.parseInt(p[2]);
        int total = 0;

        for (int year = 2000; year < y; year++) {
            if (isLeapYear(year)) {
                total += 366;
            } else {
                total += 365;
            }
        }
        for (int month = 1; month < m; month++) {
            total += daysInMonth(y, month);
        }
        total += day - 1;
        return total;
    }

    public static String fromDayNumber(int n) {
        int year = 2000;
        int left = n;
        boolean yearDone = false;

        while (!yearDone) {
            int yearDays = 365;
            if (isLeapYear(year)) {
                yearDays = 366;
            }
            if (left >= yearDays) {
                left -= yearDays;
                year++;
            } else {
                yearDone = true;
            }
        }

        int month = 1;
        while (left >= daysInMonth(year, month)) {
            left -= daysInMonth(year, month);
            month++;
        }
        int day = left + 1;
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    // 0 = Sunday; 2000-01-01 was a Saturday (6)
    public static int dayOfWeek(String d) {
        return (toDayNumber(d) + 6) % 7;
    }

    public static String today() {
        return LocalDate.now().toString();
    }

    public static int nowMinutes() {
        return LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
    }
}
