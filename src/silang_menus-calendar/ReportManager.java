import java.util.ArrayList;

public class ReportManager {
    public static void printSummaryReport() {
        ArrayList<String> venueNames = new ArrayList<String>();
        ArrayList<Integer> venueCounts = new ArrayList<Integer>();
        ArrayList<String> orgNames = new ArrayList<String>();
        ArrayList<Integer> orgCounts = new ArrayList<Integer>();

        for (Event e : DataManager.getEvents()) {
            if (e.getStatus().equals("Approved")) {
                tally(venueNames, venueCounts, String.valueOf(e.getVenueId()));
                tally(orgNames, orgCounts, e.getOrganization());
            }
        }

        // venues were tallied by id so two venues with the same name stay apart
        for (int i = 0; i < venueNames.size(); i++) {
            venueNames.set(i, CalendarManager.venueName(Integer.parseInt(venueNames.get(i))));
        }

        System.out.println("\n=== SUMMARY REPORT ===");
        printCounts("Approved events per venue", "Venue", venueNames, venueCounts);
        printCounts("Approved events per organization", "Organization", orgNames, orgCounts);

        int approved = 0;
        int rejected = 0;
        for (Request r : DataManager.getRequests()) {
            if (r.getStatus().equals("Approved")) {
                approved++;
            } else if (r.getStatus().equals("Rejected")) {
                rejected++;
            }
        }

        System.out.println("\nApproval rate");
        if (approved + rejected == 0) {
            System.out.println("No decided requests yet");
        } else {
            double rate = approved * 100.0 / (approved + rejected);
            System.out.printf("%.1f%% (%d approved, %d rejected)%n", rate, approved, rejected);
        }
    }

    // Adds one to the count of this name (names match ignoring case), or starts it at 1
    private static void tally(ArrayList<String> names, ArrayList<Integer> counts, String name) {
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equalsIgnoreCase(name)) {
                counts.set(i, counts.get(i) + 1);
                return;
            }
        }
        names.add(name);
        counts.add(1);
    }

    private static void printCounts(String title, String label, ArrayList<String> names, ArrayList<Integer> counts) {
        System.out.println("\n" + title);
        if (names.size() == 0) {
            System.out.println("No approved events yet");
            return;
        }
        System.out.println("-----------------------------------------");
        System.out.printf("%-30s | %6s%n", label, "Events");
        System.out.println("-----------------------------------------");
        for (int i = 0; i < names.size(); i++) {
            System.out.printf("%-30.30s | %6d%n", names.get(i), counts.get(i));
        }
    }
}
