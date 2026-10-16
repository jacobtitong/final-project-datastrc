import java.util.ArrayList;

public class DataManager {
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
        return new ArrayList<Setting>();
    }

    public static void saveSettings() {
    }

    public static int getSettingInt(String key) {
        return 0;
    }

    public static String getSettingString(String key) {
        return "none";
    }

    public static void setSetting(String key, String value) {
    }

    public static int generateId() {
        return 0;
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
