import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class DataManager {
    private static ArrayList<Setting> settings = new ArrayList<Setting>();

    public static void loadAll() {
    }

    public static ArrayList<User> getUsers() {
        return new ArrayList<User>();
    }

    public static ArrayList<Venue> getVenues() {
        return new ArrayList<Venue>();
    }

    public static ArrayList<Event> getEvents() {
        return new ArrayList<Event>();
    }

    public static ArrayList<Request> getRequests() {
        return new ArrayList<Request>();
    }

    public static ArrayList<StatusLog> getStatusLogs() {
        return new ArrayList<StatusLog>();
    }

    public static ArrayList<Blackout> getBlackouts() {
        return new ArrayList<Blackout>();
    }

    public static ArrayList<Setting> getSettings() {
        return settings;
    }

    // ----- Settings -----
    private static void loadSettings() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("settings.txt"));
            String line = br.readLine();
            int lineNo = 1;
            while (line != null) {
                try {
                    String[] p = line.split(";");
                    settings.add(new Setting(p[0], p[1]));
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Warning: skipped incomplete line " + lineNo + " in settings.txt");
                }
                line = br.readLine();
                lineNo++;
            }
            br.close();
        } catch (IOException e) {
            saveSettings();
        }
        addDefaultSettings();
    }

    private static void addDefaultSettings() {
        addSettingIfMissing("OPEN_TIME", "07:00");
        addSettingIfMissing("CLOSE_TIME", "18:00");
        addSettingIfMissing("MIN_DURATION_MIN", "60");
        addSettingIfMissing("MAX_DURATION_MIN", "480");
        addSettingIfMissing("MIN_LEAD_DAYS", "7");
        addSettingIfMissing("MAX_ADVANCE_DAYS", "180");
        addSettingIfMissing("BUFFER_MIN", "30");
        addSettingIfMissing("MAX_PENDING", "3");
        addSettingIfMissing("MIN_PASSWORD", "8");
        addSettingIfMissing("ID_DIGITS", "8");
        addSettingIfMissing("EMAIL_DOMAIN", "@dlsl.edu.ph");
        addSettingIfMissing("YEAR_START", "2026-08-01");
        addSettingIfMissing("YEAR_END", "2027-07-31");
        addSettingIfMissing("NEXT_ID", "1");
        saveSettings();
    }

    private static void addSettingIfMissing(String key, String value) {
        if (findSetting(key) == null) {
            settings.add(new Setting(key, value));
        }
    }

    private static Setting findSetting(String key) {
        for (Setting s : settings) {
            if (s.getKey().equals(key)) {
                return s;
            }
        }
        return null;
    }

    public static void saveSettings() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("settings.txt"));
            for (Setting s : settings) {
                pw.println(s.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save settings.txt");
        }
    }

    public static int getSettingInt(String key) {
        int number = 0;
        try {
            number = Integer.parseInt(getSettingString(key));
        } catch (NumberFormatException e) {
            number = 0;
        }
        return number;
    }

    public static String getSettingString(String key) {
        Setting s = findSetting(key);
        if (s == null) {
            return "none";
        }
        return s.getValue();
    }

    public static void setSetting(String key, String value) {
        Setting s = findSetting(key);
        if (s == null) {
            settings.add(new Setting(key, value));
        } else {
            s.setValue(value);
        }
        saveSettings();
    }

    public static int generateId() {
        int id = getSettingInt("NEXT_ID");
        setSetting("NEXT_ID", "" + (id + 1));
        return id;
    }

    public static void saveUsers() {
    }

    public static void saveVenues() {
    }

    public static void saveBlackouts() {
    }

    public static void saveEvents() {
    }

    public static void saveRequests() {
    }

    public static RequestQueue getQueue() {
        return null;
    }

    public static DecisionStack getDecisionStack() {
        return null;
    }

    public static void saveStatusLogs() {
    }

    public static void addStatusLog(int requestId, String oldStatus, String newStatus, int actorId, String remarks) {
    }

    public static String timestamp() {
        return "none";
    }

    public static Venue findVenueById(int id) {
        return null;
    }

    public static Request findRequestById(int id) {
        return null;
    }

    public static Event findEventById(int id) {
        return null;
    }

    public static User findUserById(int id) {
        return null;
    }

    public static User findUserByEmail(String email) {
        return null;
    }
}
