public class Main {
    public static void main(String[] args) {
        DataManager.loadAll();
        System.out.println("=== SCHOOL CALENDAR YEAR BOOKING SYSTEM ===");

        User current = new Guest();
        while (current != null) {
            current = current.displayMenu();
        }
        System.out.println("Goodbye!");
    }
}
