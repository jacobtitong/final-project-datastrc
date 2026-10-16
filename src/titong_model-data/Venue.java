import java.util.ArrayList;

public class Venue {
    public Venue(int id, String name, int capacity, String details, boolean active) {
    }

    public int getId() { return 0; }
    public String getName() { return "none"; }
    public int getCapacity() { return 0; }
    public String getDetails() { return "none"; }
    public boolean isActive() { return false; }
    public ArrayList<Event> getApprovedEvents() { return new ArrayList<Event>(); }

    public void setName(String name) { }
    public void setCapacity(int capacity) { }
    public void setDetails(String details) { }
    public void setActive(boolean active) { }

    public String toFileLine() { return "none"; }
}
