import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class DataManager {
    private static ArrayList<Setting> settings = new ArrayList<Setting>();
    private static ArrayList<User> users = new ArrayList<User>();
    private static ArrayList<Venue> venues = new ArrayList<Venue>();
    private static ArrayList<Event> events = new ArrayList<Event>();
    private static ArrayList<Request> requests = new ArrayList<Request>();
    private static ArrayList<StatusLog> statusLogs = new ArrayList<StatusLog>();
    private static ArrayList<Blackout> blackouts = new ArrayList<Blackout>();
    private static RequestQueue queue = new RequestQueue();
    private static DecisionStack decisionStack = new DecisionStack();

    public static void loadAll() {
        loadSettings();
        loadUsers();
        loadVenues();
        loadEvents();
        loadRequests();
        loadStatusLogs();
        loadBlackouts();
        rebuildQueue();
        fixNextId();
    }

    // NEXT_ID must stay above every id in use (settings.txt may have been deleted or edited)
    private static void fixNextId() {
        int highest = 0;
        for (User u : users) {
            highest = Math.max(highest, u.getId());
        }
        for (Venue v : venues) {
            highest = Math.max(highest, v.getId());
        }
        for (Event e : events) {
            highest = Math.max(highest, e.getId());
        }
        for (Request r : requests) {
            highest = Math.max(highest, r.getId());
        }
        for (Blackout b : blackouts) {
            highest = Math.max(highest, b.getId());
        }
        if (getSettingInt("NEXT_ID") <= highest) {
            setSetting("NEXT_ID", "" + (highest + 1));
        }
    }

    public static ArrayList<User> getUsers() {
        return users;
    }

    public static ArrayList<Venue> getVenues() {
        return venues;
    }

    public static ArrayList<Event> getEvents() {
        return events;
    }

    public static ArrayList<Request> getRequests() {
        return requests;
    }

    public static ArrayList<StatusLog> getStatusLogs() {
        return statusLogs;
    }

    public static ArrayList<Blackout> getBlackouts() {
        return blackouts;
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

    // ----- Users -----
    private static void loadUsers() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("users.txt"));
            String line = br.readLine();
            int lineNo = 1;
            while (line != null) {
                try {
                    String[] p = line.split(";");
                    int id = Integer.parseInt(p[0]);
                    boolean active = p[8].equals("true");
                    if (p[1].equals("ADMIN")) {
                        users.add(new Admin(id, p[1], p[2], p[3], p[4], p[5], p[6], p[7], active));
                    } else if (p[1].equals("REQUESTER")) {
                        users.add(new Requester(id, p[1], p[2], p[3], p[4], p[5], p[6], p[7], active));
                    } else {
                        System.out.println("Warning: skipped bad line " + lineNo + " in users.txt");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Warning: skipped bad line " + lineNo + " in users.txt");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Warning: skipped incomplete line " + lineNo + " in users.txt");
                }
                line = br.readLine();
                lineNo++;
            }
            br.close();
        } catch (IOException e) {
            saveUsers();
        }
        if (users.size() == 0) {
            addDefaultAdmin();
        }
    }

    private static void addDefaultAdmin() {
        String email = "admin" + getSettingString("EMAIL_DOMAIN");
        users.add(new Admin(generateId(), "ADMIN", "none", "Admin", email, "Admin123", "Staff", "none", true));
        saveUsers();
    }

    public static void saveUsers() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("users.txt"));
            for (User u : users) {
                pw.println(u.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save users.txt");
        }
    }

    // ----- Venues and blackouts -----
    private static void loadVenues() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("venues.txt"));
            String line = br.readLine();
            int lineNo = 1;
            while (line != null) {
                try {
                    String[] p = line.split(";");
                    venues.add(new Venue(Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2]), p[3], p[4].equals("true")));
                } catch (NumberFormatException e) {
                    System.out.println("Warning: skipped bad line " + lineNo + " in venues.txt");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Warning: skipped incomplete line " + lineNo + " in venues.txt");
                }
                line = br.readLine();
                lineNo++;
            }
            br.close();
        } catch (IOException e) {
            saveVenues();
        }
    }

    public static void saveVenues() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("venues.txt"));
            for (Venue v : venues) {
                pw.println(v.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save venues.txt");
        }
    }

    private static void loadBlackouts() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("blackouts.txt"));
            String line = br.readLine();
            int lineNo = 1;
            while (line != null) {
                try {
                    String[] p = line.split(";");
                    blackouts.add(new Blackout(Integer.parseInt(p[0]), p[1], p[2]));
                } catch (NumberFormatException e) {
                    System.out.println("Warning: skipped bad line " + lineNo + " in blackouts.txt");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Warning: skipped incomplete line " + lineNo + " in blackouts.txt");
                }
                line = br.readLine();
                lineNo++;
            }
            br.close();
        } catch (IOException e) {
            saveBlackouts();
        }
    }

    public static void saveBlackouts() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("blackouts.txt"));
            for (Blackout b : blackouts) {
                pw.println(b.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save blackouts.txt");
        }
    }

    // ----- Events, requests and the queue -----
    private static void loadEvents() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("events.txt"));
            String line = br.readLine();
            int lineNo = 1;
            while (line != null) {
                try {
                    String[] p = line.split(";");
                    if (!DateHelper.isValidDate(p[6]) || !DateHelper.isValidTime(p[7]) || !DateHelper.isValidTime(p[8])) {
                        throw new NumberFormatException("bad date or time");
                    }
                    Event ev = new Event(p[2], p[3], p[4], Integer.parseInt(p[5]), p[6], p[7], p[8], p[9],
                            Integer.parseInt(p[0]), Integer.parseInt(p[1]), p[10]);
                    events.add(ev);
                    addToVenueList(ev);
                } catch (NumberFormatException e) {
                    System.out.println("Warning: skipped bad line " + lineNo + " in events.txt");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Warning: skipped incomplete line " + lineNo + " in events.txt");
                }
                line = br.readLine();
                lineNo++;
            }
            br.close();
        } catch (IOException e) {
            saveEvents();
        }
    }

    private static void addToVenueList(Event ev) {
        if (ev.getStatus().equals("Approved")) {
            Venue venue = findVenueById(ev.getVenueId());
            if (venue == null) {
                System.out.println("Warning: event " + ev.getId() + " has an unknown venue");
            } else {
                ArrayList<Event> list = venue.getApprovedEvents();
                int index = SearchHelper.lowerBound(list, ev.getDate() + " " + ev.getStartTime());
                list.add(index, ev);
            }
        }
    }

    public static void saveEvents() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("events.txt"));
            for (Event e : events) {
                pw.println(e.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save events.txt");
        }
    }

    private static void loadRequests() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("requests.txt"));
            String line = br.readLine();
            int lineNo = 1;
            while (line != null) {
                try {
                    String[] p = line.split(";");
                    if (!DateHelper.isValidDate(p[8]) || !DateHelper.isValidTime(p[9]) || !DateHelper.isValidTime(p[10])) {
                        throw new NumberFormatException("bad date or time");
                    }
                    requests.add(new Request(p[2], p[3], p[4], Integer.parseInt(p[7]), p[8], p[9], p[10], p[11],
                            Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[6]), p[5], p[12], p[13]));
                } catch (NumberFormatException e) {
                    System.out.println("Warning: skipped bad line " + lineNo + " in requests.txt");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Warning: skipped incomplete line " + lineNo + " in requests.txt");
                }
                line = br.readLine();
                lineNo++;
            }
            br.close();
        } catch (IOException e) {
            saveRequests();
        }
    }

    public static void saveRequests() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("requests.txt"));
            for (Request r : requests) {
                pw.println(r.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save requests.txt");
        }
    }

    private static void rebuildQueue() {
        queue = new RequestQueue();
        for (Request r : requests) {
            if (r.getStatus().equals("Pending")) {
                queue.enqueue(r);
            }
        }
    }

    public static RequestQueue getQueue() {
        return queue;
    }

    public static DecisionStack getDecisionStack() {
        return decisionStack;
    }

    // ----- Status log and timestamp -----
    private static void loadStatusLogs() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("status_log.txt"));
            String line = br.readLine();
            int lineNo = 1;
            while (line != null) {
                try {
                    String[] p = line.split(";");
                    statusLogs.add(new StatusLog(Integer.parseInt(p[0]), p[1], p[2], p[3], Integer.parseInt(p[4]), p[5]));
                } catch (NumberFormatException e) {
                    System.out.println("Warning: skipped bad line " + lineNo + " in status_log.txt");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Warning: skipped incomplete line " + lineNo + " in status_log.txt");
                }
                line = br.readLine();
                lineNo++;
            }
            br.close();
        } catch (IOException e) {
            saveStatusLogs();
        }
    }

    public static void saveStatusLogs() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("status_log.txt"));
            for (StatusLog log : statusLogs) {
                pw.println(log.toFileLine());
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error: could not save status_log.txt");
        }
    }

    public static void addStatusLog(int requestId, String oldStatus, String newStatus, int actorId, String remarks) {
        statusLogs.add(new StatusLog(requestId, timestamp(), oldStatus, newStatus, actorId, remarks));
        saveStatusLogs();
    }

    public static String timestamp() {
        LocalTime now = LocalTime.now();
        return LocalDate.now().toString() + " " + String.format("%02d:%02d", now.getHour(), now.getMinute());
    }

    // ----- Find methods (return null if absent) -----
    public static Venue findVenueById(int id) {
        for (Venue v : venues) {
            if (v.getId() == id) {
                return v;
            }
        }
        return null;
    }

    public static Request findRequestById(int id) {
        for (Request r : requests) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public static Event findEventById(int id) {
        int index = SearchHelper.findEventIndex(events, id);
        if (index == -1) {
            return null;
        }
        return events.get(index);
    }

    public static User findUserById(int id) {
        for (User u : users) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    public static User findUserByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }
}
