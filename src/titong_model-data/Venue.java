import java.util.ArrayList;

public class Venue {
    private int id;
    private String name;
    private int capacity;
    private String details;
    private boolean active;
    private ArrayList<Event> approvedEvents;

    public Venue(int id, String name, int capacity, String details, boolean active) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.details = details;
        this.active = active;
        this.approvedEvents = new ArrayList<Event>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public String getDetails() { return details; }
    public boolean isActive() { return active; }
    public ArrayList<Event> getApprovedEvents() { return approvedEvents; }

    public void setName(String name) { this.name = name; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setDetails(String details) { this.details = details; }
    public void setActive(boolean active) { this.active = active; }

    public String toFileLine() {
        return id + ";" + name + ";" + capacity + ";" + details + ";" + active;
    }
}
