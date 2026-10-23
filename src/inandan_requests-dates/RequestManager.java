import java.util.ArrayList;

public class RequestManager {
    // Answers collected by askDetails (shared by submitRequest and editRequest)
    private static String formTitle;
    private static String formOrganizer;
    private static String formPurpose;
    private static int formAttendees;
    private static Venue formVenue;
    private static String formDate;
    private static String formStart;
    private static String formEnd;

    private static String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    // ===== Conflicts and free slots =====

    public static Event findConflict(int venueId, String date, int startMin, int endMin, int ignoreEventId) {
        Venue venue = DataManager.findVenueById(venueId);
        if (venue == null) {
            return null;
        }
        int buffer = DataManager.getSettingInt("BUFFER_MIN");

        // work on a copy that skips the ignored event
        ArrayList<Event> list = new ArrayList<Event>();
        for (Event e : venue.getApprovedEvents()) {
            if (e.getId() != ignoreEventId) {
                list.add(e);
            }
        }

        int index = SearchHelper.lowerBound(list, date + " " + DateHelper.toTimeString(startMin));

        if (index > 0) {
            Event prev = list.get(index - 1);
            if (prev.getDate().equals(date) && startMin < prev.getEndMinutes() + buffer) {
                return prev;
            }
        }
        if (index < list.size()) {
            Event next = list.get(index);
            if (next.getDate().equals(date) && endMin + buffer > next.getStartMinutes()) {
                return next;
            }
        }
        return null;
    }

    public static String suggestSlot(int venueId, String date, int startMin, int endMin) {
        Venue venue = DataManager.findVenueById(venueId);
        if (venue == null) {
            return "No free slot on this date at this venue.";
        }
        int buffer = DataManager.getSettingInt("BUFFER_MIN");
        int open = DateHelper.toMinutes(DataManager.getSettingString("OPEN_TIME"));
        int close = DateHelper.toMinutes(DataManager.getSettingString("CLOSE_TIME"));
        int duration = endMin - startMin;
        ArrayList<Event> dayEvents = getDayEvents(venue, date);

        // today, a slot must start after the current time
        boolean today = date.equals(DateHelper.today());
        int now = DateHelper.nowMinutes();

        // candidate starts: opening time (or just after now), and for each event the slot
        // right after it (end plus buffer) and the slot right before it (start minus buffer minus duration)
        ArrayList<Integer> candidates = new ArrayList<Integer>();
        candidates.add(open);
        if (today) {
            candidates.add(now + 1);
        }
        for (Event e : dayEvents) {
            candidates.add(e.getEndMinutes() + buffer);
            candidates.add(e.getStartMinutes() - buffer - duration);
        }

        int best = -1;
        for (int i = 0; i < candidates.size(); i++) {
            int c = candidates.get(i);
            boolean fits = c >= open && c + duration <= close && !(today && c <= now);
            for (Event e : dayEvents) {
                boolean endedBefore = e.getEndMinutes() + buffer <= c;
                boolean startsAfter = c + duration + buffer <= e.getStartMinutes();
                if (!endedBefore && !startsAfter) {
                    fits = false;
                }
            }
            if (fits) {
                if (best == -1 || Math.abs(c - startMin) < Math.abs(best - startMin)) {
                    best = c;
                }
            }
        }

        if (best == -1) {
            return "No free slot on this date at this venue.";
        }
        return DateHelper.toTimeString(best) + "-" + DateHelper.toTimeString(best + duration);
    }

    private static ArrayList<Event> getDayEvents(Venue venue, String date) {
        ArrayList<Event> list = venue.getApprovedEvents();
        ArrayList<Event> result = new ArrayList<Event>();
        int index = SearchHelper.lowerBound(list, date + " 00:00");
        while (index < list.size() && list.get(index).getDate().equals(date)) {
            result.add(list.get(index));
            index++;
        }
        return result;
    }

    private static void printConflict(Event ev, int venueId, String date, int startMin, int endMin) {
        int buffer = DataManager.getSettingInt("BUFFER_MIN");
        String slot = suggestSlot(venueId, date, startMin, endMin);
        String message = "Conflicts with: " + ev.getTitle() + " at " + venueName(ev.getVenueId()) + ", "
                + ev.getStartTime() + "-" + ev.getEndTime() + " (" + buffer + "-minute buffer applies).";
        if (slot.contains("No free slot")) {
            System.out.println(message + " " + slot);
        } else {
            System.out.println(message + " Next free slot: " + slot + ".");
        }
    }

    // ===== Submit and edit a request =====

    public static void submitRequest(Requester r) {
        System.out.println("\n=== SUBMIT A REQUEST === (type 'back' at any prompt to cancel)");

        if (!r.isActive()) {
            System.out.println("Error: Your account is deactivated.");
            return;
        }
        int maxPending = DataManager.getSettingInt("MAX_PENDING");
        if (countPending(r.getId()) >= maxPending) {
            System.out.println("Error: You already have " + maxPending + " pending requests. Wait for a decision or cancel one.");
            return;
        }

        if (!askDetails(r, 0)) {
            return;
        }

        Request request = new Request(formTitle, formOrganizer, r.getOrganization(), formVenue.getId(),
                formDate, formStart, formEnd, "Pending", DataManager.generateId(), r.getId(),
                formAttendees, formPurpose, "none", DataManager.timestamp());
        DataManager.getRequests().add(request);
        DataManager.saveRequests();
        DataManager.getQueue().enqueue(request);
        DataManager.addStatusLog(request.getId(), "none", "Pending", r.getId(), "none");
        System.out.println("Request submitted. Request ID: " + request.getId() + " (Pending). Position in queue: "
                + DataManager.getQueue().size() + ".");
    }

    public static void editRequest(Requester r) {
        ArrayList<Request> mine = new ArrayList<Request>();
        for (Request q : DataManager.getRequests()) {
            if (q.getRequesterId() == r.getId() && q.getStatus().equals("Pending")) {
                mine.add(q);
            }
        }
        if (mine.size() == 0) {
            System.out.println("You have no pending requests to edit.");
            return;
        }

        System.out.println("\n=== EDIT A PENDING REQUEST === (type 'back' at any prompt to cancel)");
        printTableHeader();
        for (int i = 0; i < mine.size(); i++) {
            printRow(i + 1, mine.get(i).getId(), mine.get(i));
        }
        int number = InputHelper.getValidInt("--> Request number: ", 1, mine.size());
        if (number == -1) {
            return;
        }
        Request req = mine.get(number - 1);

        System.out.println("Enter the new details for request " + req.getId() + ".");
        if (!askDetails(r, req.getId())) {
            return;
        }

        req.setTitle(formTitle);
        req.setOrganizer(formOrganizer);
        req.setOrganization(r.getOrganization());
        req.setPurpose(formPurpose);
        req.setAttendees(formAttendees);
        req.setVenueId(formVenue.getId());
        req.setDate(formDate);
        req.setStartTime(formStart);
        req.setEndTime(formEnd);
        DataManager.saveRequests();
        DataManager.getDecisionStack().clear();
        System.out.println("Request " + req.getId() + " updated. It keeps its place in the queue.");
    }

    // Asks every detail and re-asks a field when it breaks a rule; true = the form fields are ready to use
    private static boolean askDetails(Requester r, int ignoreRequestId) {
        ArrayList<Venue> venues = getActiveVenues();
        if (venues.size() == 0) {
            System.out.println("Error: No venues are available. Ask the Admin to add one.");
            return false;
        }

        formTitle = InputHelper.getValidText("Event title (3-60 characters): ", 3, 60);
        if (formTitle == null) {
            return cancelled();
        }

        boolean useName = InputHelper.getYesNo("Use your name (" + r.getName() + ") as the organizer? (Y/N): ");
        if (useName) {
            formOrganizer = r.getName();
        } else {
            formOrganizer = InputHelper.getValidText("Organizer (2-40 characters): ", 2, 40);
            if (formOrganizer == null) {
                return cancelled();
            }
        }

        formPurpose = InputHelper.getValidText("Purpose (5-150 characters): ", 5, 150);
        if (formPurpose == null) {
            return cancelled();
        }

        formAttendees = InputHelper.getValidInt("Expected number of attendees: ", 1, 5000);
        if (formAttendees == -1) {
            return cancelled();
        }

        printVenueList(venues);
        formVenue = null;
        while (formVenue == null) {
            int venueNumber = InputHelper.getValidInt("--> Venue number: ", 1, venues.size());
            if (venueNumber == -1) {
                return cancelled();
            }
            Venue pick = venues.get(venueNumber - 1);
            String error = checkVenue(pick, formAttendees);
            while (error != null && pick != null) {
                System.out.println(error);
                if (InputHelper.getYesNo("Enter a different number of attendees? (Y/N): ")) {
                    formAttendees = InputHelper.getValidInt("Expected number of attendees: ", 1, 5000);
                    if (formAttendees == -1) {
                        return cancelled();
                    }
                    error = checkVenue(pick, formAttendees);
                } else {
                    pick = null;
                }
            }
            formVenue = pick;
        }

        if (!askDateAndTimes()) {
            return false;
        }

        boolean done = false;
        while (!done) {
            if (hasDuplicate(r.getId(), formVenue.getId(), formDate, formStart, formEnd, ignoreRequestId)) {
                System.out.println("Error: You already have a pending or approved request for this venue, date and time.");
                if (!askDateAndTimes()) {
                    return false;
                }
                continue;
            }

            int startMin = DateHelper.toMinutes(formStart);
            int endMin = DateHelper.toMinutes(formEnd);
            Event conflict = findConflict(formVenue.getId(), formDate, startMin, endMin, 0);
            if (conflict == null) {
                done = true;
            } else {
                printConflict(conflict, formVenue.getId(), formDate, startMin, endMin);
                boolean again = InputHelper.getYesNo("Enter a different date and time? (Y/N): ");
                if (!again) {
                    return cancelled();
                }
                if (!askDateAndTimes()) {
                    return false;
                }
            }
        }
        return true;
    }

    // Asks the date and times, re-asking only the part that breaks a rule; false = cancelled
    private static boolean askDateAndTimes() {
        boolean ok = false;
        while (!ok) {
            String error = "x";
            while (error != null) {
                formDate = InputHelper.getValidDate("Date (YYYY-MM-DD): ");
                if (formDate == null) {
                    return cancelled();
                }
                error = checkDate(formDate);
                if (error != null) {
                    System.out.println(error);
                }
            }

            error = "x";
            while (error != null) {
                formStart = InputHelper.getValidTime("Start time (HH:MM, 24-hour): ");
                if (formStart == null) {
                    return cancelled();
                }
                formEnd = InputHelper.getValidTime("End time (HH:MM, 24-hour): ");
                if (formEnd == null) {
                    return cancelled();
                }
                error = checkTimes(formStart, formEnd);
                if (error != null) {
                    System.out.println(error);
                }
            }

            // the times are fine; a start time that has passed means the date must change too
            if (isUpcoming(formDate, formStart)) {
                ok = true;
            } else {
                System.out.println("Error: The start time has already passed.");
            }
        }
        return true;
    }

    // ===== Cancel a request =====

    public static void cancelRequest(User actor) {
        int todayNumber = DateHelper.toDayNumber(DateHelper.today());
        ArrayList<Request> options = new ArrayList<Request>();
        for (Request q : DataManager.getRequests()) {
            if (q.getRequesterId() == actor.getId()) {
                if (q.getStatus().equals("Pending")) {
                    options.add(q);
                } else if (q.getStatus().equals("Approved") && DateHelper.toDayNumber(q.getDate()) > todayNumber) {
                    options.add(q);
                }
            }
        }
        if (options.size() == 0) {
            System.out.println("You have no requests that can be cancelled.");
            return;
        }

        System.out.println("\n=== CANCEL A REQUEST === (type 'back' at any prompt to cancel)");
        printTableHeader();
        for (int i = 0; i < options.size(); i++) {
            printRow(i + 1, options.get(i).getId(), options.get(i));
        }
        int number = InputHelper.getValidInt("--> Request number: ", 1, options.size());
        if (number == -1) {
            return;
        }
        Request req = options.get(number - 1);

        boolean sure = InputHelper.getYesNo("Cancel request " + req.getId() + "? (Y/N): ");
        if (!sure) {
            System.out.println("Cancellation aborted.");
            return;
        }

        if (req.getStatus().equals("Pending")) {
            DataManager.getQueue().remove(req.getId());
            req.setStatus("Cancelled");
            DataManager.saveRequests();
            DataManager.addStatusLog(req.getId(), "Pending", "Cancelled", actor.getId(), "none");
            DataManager.getDecisionStack().clear();
        } else {
            cancelApproved(findApprovedEventOfRequest(req.getId()), req, "none", actor.getId());
        }
        System.out.println("Request " + req.getId() + " is cancelled.");
    }

    // Cancels an Approved event and its request; ev or req may be null if the data is out of step
    private static void cancelApproved(Event ev, Request req, String reason, int actorId) {
        if (ev != null) {
            ev.setStatus("Cancelled");
            ev.setCancelReason(reason);
            removeFromVenueList(ev);
            DataManager.saveEvents();
        }
        if (req != null) {
            req.setStatus("Cancelled");
            DataManager.saveRequests();
            DataManager.addStatusLog(req.getId(), "Approved", "Cancelled", actorId, reason);
        }
        DataManager.getDecisionStack().clear();
    }

    // ===== Admin: review the queue =====

    public static void reviewQueue(Admin a) {
        boolean back = false;

        while (!back) {
            Request front = DataManager.getQueue().peek();
            if (front == null) {
                System.out.println("The queue is empty. No pending requests.");
                return;
            }
            showFront(front);

            System.out.print("\n1 - Approve\n2 - Reject\n3 - Defer (send to the back of the queue)\n4 - Back\n");
            int option = InputHelper.getValidInt("--> Option: ", 1, 4);
            if (option == -1) {
                option = 4;
            }

            switch (option) {
                case 1:
                    approveFront(a, front);
                    break;
                case 2:
                    rejectFront(a, front);
                    break;
                case 3:
                    deferFront();
                    break;
                case 4:
                    back = true;
                    break;
                default:
                    System.out.println("Error: Invalid option. Please choose 1-4.");
            }
        }
    }

    private static void showFront(Request front) {
        Venue venue = DataManager.findVenueById(front.getVenueId());
        String capacity = "?";
        if (venue != null) {
            capacity = "" + venue.getCapacity();
        }
        User requester = DataManager.findUserById(front.getRequesterId());
        String requesterName = "Unknown";
        if (requester != null) {
            requesterName = requester.getName();
        }

        System.out.println("\n=== REVIEW QUEUE ===");
        System.out.println("Position in queue: 1 of " + DataManager.getQueue().size());
        System.out.println("Request ID   : " + front.getId());
        System.out.println("Title        : " + front.getTitle());
        System.out.println("Organizer    : " + front.getOrganizer());
        System.out.println("Organization : " + front.getOrganization());
        System.out.println("Requested by : " + requesterName);
        System.out.println("Purpose      : " + front.getPurpose());
        System.out.println("Attendees    : " + front.getAttendees());
        System.out.println("Venue        : " + venueName(front.getVenueId()) + " (capacity " + capacity + ")");
        System.out.println("Date         : " + front.getDate() + " (" + dayName(front.getDate()) + ")");
        System.out.println("Time         : " + front.getStartTime() + "-" + front.getEndTime());
        System.out.println("Submitted at : " + front.getSubmittedAt());
    }

    private static void approveFront(Admin a, Request front) {
        Venue venue = DataManager.findVenueById(front.getVenueId());

        // the system may have changed while the request waited, so check it again
        String error = checkVenue(venue, front.getAttendees());
        User requester = DataManager.findUserById(front.getRequesterId());
        if (error == null && (requester == null || !requester.isActive())) {
            error = "Error: The requester's account is no longer active.";
        }
        if (error == null && !isUpcoming(front.getDate(), front.getStartTime())) {
            error = "Error: The requested date and time have already passed.";
        }
        String yearStart = DataManager.getSettingString("YEAR_START");
        String yearEnd = DataManager.getSettingString("YEAR_END");
        if (error == null && (front.getDate().compareTo(yearStart) < 0 || front.getDate().compareTo(yearEnd) > 0)) {
            error = "Error: The date is outside the school year (" + yearStart + " to " + yearEnd + ").";
        }
        if (error == null && findBlackoutReason(front.getDate()) != null) {
            error = "Error: That date is now a blackout date (" + findBlackoutReason(front.getDate()) + ").";
        }
        if (error == null) {
            error = checkTimes(front.getStartTime(), front.getEndTime());
        }
        if (error != null) {
            System.out.println(error);
            System.out.println("This request can no longer be approved. Reject or defer it.");
            return;
        }

        // the conflict re-check must still pass
        Event conflict = findConflict(front.getVenueId(), front.getDate(), front.getStartMinutes(), front.getEndMinutes(), 0);
        if (conflict != null) {
            printConflict(conflict, front.getVenueId(), front.getDate(), front.getStartMinutes(), front.getEndMinutes());
            System.out.println("Error: This request can no longer be approved. Reject or defer it.");
            return;
        }

        DataManager.getQueue().dequeue();

        Event ev = new Event(front.getTitle(), front.getOrganizer(), front.getOrganization(), front.getVenueId(),
                front.getDate(), front.getStartTime(), front.getEndTime(), "Approved",
                DataManager.generateId(), front.getId(), "none");
        DataManager.getEvents().add(ev);
        insertIntoVenueList(venue, ev);
        DataManager.saveEvents();

        front.setStatus("Approved");
        DataManager.addStatusLog(front.getId(), "Pending", "Approved", a.getId(), "none");

        Decision decision = new Decision(front.getId(), "Approved", ev.getId());
        int rejectedCount = autoRejectConflicts(ev, decision, a.getId());
        DataManager.saveRequests();
        DataManager.getDecisionStack().push(decision);

        System.out.println("Request " + front.getId() + " approved. Event ID: " + ev.getId() + ".");
        if (rejectedCount > 0) {
            System.out.println(rejectedCount + " pending request(s) were rejected automatically because they conflict with this event.");
        }
    }

    // Rejects every waiting request of the same venue and date that now conflicts with the new event
    private static int autoRejectConflicts(Event ev, Decision decision, int actorId) {
        int buffer = DataManager.getSettingInt("BUFFER_MIN");
        int count = 0;
        ArrayList<Request> waiting = DataManager.getQueue().toArrayList();

        for (Request p : waiting) {
            if (p.getVenueId() == ev.getVenueId() && p.getDate().equals(ev.getDate())
                    && p.getStartMinutes() < ev.getEndMinutes() + buffer
                    && p.getEndMinutes() + buffer > ev.getStartMinutes()) {
                p.setStatus("Rejected");
                p.setRejectReason("Conflicts with an approved event.");
                DataManager.getQueue().remove(p.getId());
                DataManager.addStatusLog(p.getId(), "Pending", "Rejected", actorId, "Conflicts with an approved event.");
                decision.addAutoRejected(p.getId());
                count++;
            }
        }
        return count;
    }

    private static void rejectFront(Admin a, Request front) {
        String reason = InputHelper.getValidText("Reason for rejection (5-100 characters): ", 5, 100);
        if (reason == null) {
            return;
        }
        boolean sure = InputHelper.getYesNo("Reject request " + front.getId() + "? (Y/N): ");
        if (!sure) {
            System.out.println("Rejection aborted.");
            return;
        }

        DataManager.getQueue().dequeue();
        front.setStatus("Rejected");
        front.setRejectReason(reason);
        DataManager.saveRequests();
        DataManager.addStatusLog(front.getId(), "Pending", "Rejected", a.getId(), reason);
        DataManager.getDecisionStack().push(new Decision(front.getId(), "Rejected", 0));
        System.out.println("Request " + front.getId() + " rejected.");
    }

    private static void deferFront() {
        RequestQueue queue = DataManager.getQueue();
        if (queue.size() == 1) {
            System.out.println("There is only one request in the queue. Nothing changes.");
            return;
        }
        Request moved = queue.dequeue();
        queue.enqueue(moved);
        System.out.println("Request " + moved.getId() + " was sent to the back of the queue.");
    }

    // ===== Admin: undo =====

    public static void undoLastDecision(Admin a) {
        DecisionStack stack = DataManager.getDecisionStack();
        if (stack.isEmpty()) {
            System.out.println("Nothing to undo.");
            return;
        }

        Decision d = stack.pop();
        Request req = DataManager.findRequestById(d.getRequestId());
        RequestQueue queue = DataManager.getQueue();

        if (d.getType().equals("Approved")) {
            Event ev = DataManager.findEventById(d.getEventId());
            if (ev != null) {
                removeFromVenueList(ev);
                int index = SearchHelper.findEventIndex(DataManager.getEvents(), ev.getId());
                if (index != -1) {
                    DataManager.getEvents().remove(index);
                }
                DataManager.saveEvents();
            }
            if (req != null) {
                req.setStatus("Pending");
                queue.enqueue(req);
                DataManager.addStatusLog(req.getId(), "Approved", "Pending", a.getId(), "Decision undone");
            }

            ArrayList<Integer> ids = d.getAutoRejectedIds();
            for (int i = 0; i < ids.size(); i++) {
                Request back = DataManager.findRequestById(ids.get(i));
                if (back != null) {
                    back.setStatus("Pending");
                    back.setRejectReason("none");
                    queue.enqueue(back);
                    DataManager.addStatusLog(back.getId(), "Rejected", "Pending", a.getId(), "Decision undone");
                }
            }
            DataManager.saveRequests();
            System.out.println("Approval undone. Request " + d.getRequestId() + " is Pending again, at the back of the queue.");
        } else {
            if (req != null) {
                req.setStatus("Pending");
                req.setRejectReason("none");
                queue.enqueue(req);
                DataManager.saveRequests();
                DataManager.addStatusLog(req.getId(), "Rejected", "Pending", a.getId(), "Decision undone");
            }
            System.out.println("Rejection undone. Request " + d.getRequestId() + " is Pending again, at the back of the queue.");
        }
    }

    // ===== Admin: reschedule and cancel an event =====

    public static void rescheduleEvent(Admin a) {
        ArrayList<Event> approved = new ArrayList<Event>();
        for (Event e : DataManager.getEvents()) {
            if (e.getStatus().equals("Approved") && isUpcoming(e.getDate(), e.getStartTime())) {
                approved.add(e);
            }
        }
        if (approved.size() == 0) {
            System.out.println("There are no upcoming approved events to reschedule.");
            return;
        }

        System.out.println("\n=== RESCHEDULE AN EVENT === (type 'back' at any prompt to cancel)");
        printTableHeader();
        for (int i = 0; i < approved.size(); i++) {
            printRow(i + 1, approved.get(i).getId(), approved.get(i));
        }
        int number = InputHelper.getValidInt("--> Event number: ", 1, approved.size());
        if (number == -1) {
            return;
        }
        Event ev = approved.get(number - 1);
        Request req = DataManager.findRequestById(ev.getRequestId());

        ArrayList<Venue> venues = getActiveVenues();
        if (venues.size() == 0) {
            System.out.println("Error: No venues are available.");
            return;
        }
        printVenueList(venues);
        int venueNumber = InputHelper.getValidInt("--> New venue number: ", 1, venues.size());
        if (venueNumber == -1) {
            return;
        }
        Venue newVenue = venues.get(venueNumber - 1);

        String date = InputHelper.getValidDate("New date (YYYY-MM-DD): ");
        if (date == null) {
            return;
        }
        String start = InputHelper.getValidTime("New start time (HH:MM, 24-hour): ");
        if (start == null) {
            return;
        }
        String end = InputHelper.getValidTime("New end time (HH:MM, 24-hour): ");
        if (end == null) {
            return;
        }

        // checks: not in the past, school year, max advance, blackout, hours, duration, capacity, conflict (ignoring itself)
        int lead = 0;
        if (DateHelper.isValidDate(date)) {
            lead = DateHelper.toDayNumber(date) - DateHelper.toDayNumber(DateHelper.today());
        }
        String yearStart = DataManager.getSettingString("YEAR_START");
        String yearEnd = DataManager.getSettingString("YEAR_END");
        String error = null;
        if (!DateHelper.isValidDate(date) || !DateHelper.isValidTime(start)) {
            error = "Error: Invalid date or time.";
        } else if (!isUpcoming(date, start)) {
            error = "Error: The new date and time cannot be in the past.";
        } else if (lead > DataManager.getSettingInt("MAX_ADVANCE_DAYS")) {
            error = "Error: The date can be at most " + DataManager.getSettingInt("MAX_ADVANCE_DAYS") + " days away.";
        } else if (date.compareTo(yearStart) < 0 || date.compareTo(yearEnd) > 0) {
            error = "Error: The date must be inside the school year (" + yearStart + " to " + yearEnd + ").";
        } else if (findBlackoutReason(date) != null) {
            error = "Error: That date is a blackout date (" + findBlackoutReason(date) + ").";
        } else {
            error = checkTimes(start, end);
        }
        if (error == null && req != null && req.getAttendees() > newVenue.getCapacity()) {
            error = "Error: This venue holds at most " + newVenue.getCapacity() + " people.";
        }
        if (error != null) {
            System.out.println(error);
            return;
        }

        int startMin = DateHelper.toMinutes(start);
        int endMin = DateHelper.toMinutes(end);
        Event conflict = findConflict(newVenue.getId(), date, startMin, endMin, ev.getId());
        if (conflict != null) {
            printConflict(conflict, newVenue.getId(), date, startMin, endMin);
            return;
        }

        String oldText = venueName(ev.getVenueId()) + " " + ev.getDate() + " " + ev.getStartTime() + "-" + ev.getEndTime();
        removeFromVenueList(ev);
        ev.setVenueId(newVenue.getId());
        ev.setDate(date);
        ev.setStartTime(start);
        ev.setEndTime(end);
        insertIntoVenueList(newVenue, ev);
        DataManager.saveEvents();

        // the decision stack is cleared below, so this Decision is only a holder for the auto-rejected ids
        int rejectedCount = autoRejectConflicts(ev, new Decision(ev.getRequestId(), "Approved", ev.getId()), a.getId());
        DataManager.saveRequests();

        String newText = newVenue.getName() + " " + date + " " + start + "-" + end;
        if (req != null) {
            req.setVenueId(newVenue.getId());
            req.setDate(date);
            req.setStartTime(start);
            req.setEndTime(end);
            DataManager.saveRequests();
            DataManager.addStatusLog(req.getId(), "Approved", "Approved", a.getId(), "Rescheduled from " + oldText + " to " + newText);
        }
        DataManager.getDecisionStack().clear();
        System.out.println("Event " + ev.getId() + " rescheduled to " + newText + ".");
        if (rejectedCount > 0) {
            System.out.println(rejectedCount + " pending request(s) were rejected automatically because they conflict with this event.");
        }
    }

    public static void cancelEvent(Admin a) {
        ArrayList<Event> upcoming = new ArrayList<Event>();
        for (Event e : DataManager.getEvents()) {
            if (e.getStatus().equals("Approved") && isUpcoming(e.getDate(), e.getStartTime())) {
                upcoming.add(e);
            }
        }
        if (upcoming.size() == 0) {
            System.out.println("There are no upcoming approved events to cancel.");
            return;
        }

        System.out.println("\n=== CANCEL AN EVENT === (type 'back' at any prompt to cancel)");
        printTableHeader();
        for (int i = 0; i < upcoming.size(); i++) {
            printRow(i + 1, upcoming.get(i).getId(), upcoming.get(i));
        }
        int number = InputHelper.getValidInt("--> Event number: ", 1, upcoming.size());
        if (number == -1) {
            return;
        }
        Event ev = upcoming.get(number - 1);

        String reason = InputHelper.getValidText("Reason for cancelling (5-100 characters): ", 5, 100);
        if (reason == null) {
            return;
        }
        boolean sure = InputHelper.getYesNo("Cancel event " + ev.getId() + "? (Y/N): ");
        if (!sure) {
            System.out.println("Cancellation aborted.");
            return;
        }

        cancelApproved(ev, DataManager.findRequestById(ev.getRequestId()), reason, a.getId());
        System.out.println("Event " + ev.getId() + " is cancelled.");
    }

    // ===== Deactivate an account =====

    public static void deactivateUser(User actor) {
        if (!actor.getRole().equals("ADMIN")) {
            boolean sure = InputHelper.getYesNo("Deactivate your own account? You will not be able to log in again. (Y/N): ");
            if (!sure) {
                System.out.println("Deactivation aborted.");
                return;
            }
            actor.setActive(false);
            DataManager.saveUsers();
            DataManager.getDecisionStack().clear();
            System.out.println("Your account is deactivated. Your requests and approved events stay.");
            return;
        }

        ArrayList<User> requesters = new ArrayList<User>();
        for (User u : DataManager.getUsers()) {
            if (u.getRole().equals("REQUESTER") && u.isActive()) {
                requesters.add(u);
            }
        }
        if (requesters.size() == 0) {
            System.out.println("There are no active Requester accounts.");
            return;
        }

        System.out.println("\n=== DEACTIVATE A USER === (type 'back' at any prompt to cancel)");
        System.out.println("-------------------------------------------------------------");
        System.out.printf("%-4s | %-6s | %-20s | %-25s%n", "No.", "ID", "Name", "Email");
        System.out.println("-------------------------------------------------------------");
        for (int i = 0; i < requesters.size(); i++) {
            User u = requesters.get(i);
            System.out.printf("%-4d | %-6d | %-20.20s | %-25.25s%n", i + 1, u.getId(), u.getName(), u.getEmail());
        }
        int number = InputHelper.getValidInt("--> User number: ", 1, requesters.size());
        if (number == -1) {
            return;
        }
        User target = requesters.get(number - 1);

        boolean sure = InputHelper.getYesNo("Deactivate " + target.getName() + "? (Y/N): ");
        if (!sure) {
            System.out.println("Deactivation aborted.");
            return;
        }
        target.setActive(false);
        DataManager.saveUsers();
        DataManager.getDecisionStack().clear();
        System.out.println(target.getName() + " is deactivated. Their requests and approved events stay.");
    }

    // ===== Checks (each returns an error message, or null when everything is fine) =====

    private static String checkDate(String date) {
        if (!DateHelper.isValidDate(date)) {
            return "Error: That is not a real date.";
        }
        int lead = DateHelper.toDayNumber(date) - DateHelper.toDayNumber(DateHelper.today());
        int minLead = DataManager.getSettingInt("MIN_LEAD_DAYS");
        int maxAdvance = DataManager.getSettingInt("MAX_ADVANCE_DAYS");
        String yearStart = DataManager.getSettingString("YEAR_START");
        String yearEnd = DataManager.getSettingString("YEAR_END");

        if (lead < 0) {
            return "Error: The date cannot be in the past.";
        }
        if (lead < minLead) {
            return "Error: Requests must be made at least " + minLead + " days before the event date.";
        }
        if (lead > maxAdvance) {
            return "Error: The date can be at most " + maxAdvance + " days away.";
        }
        if (date.compareTo(yearStart) < 0 || date.compareTo(yearEnd) > 0) {
            return "Error: The date must be inside the school year (" + yearStart + " to " + yearEnd + ").";
        }
        String reason = findBlackoutReason(date);
        if (reason != null) {
            return "Error: That date is a blackout date (" + reason + ").";
        }
        return null;
    }

    private static String checkTimes(String start, String end) {
        if (!DateHelper.isValidTime(start) || !DateHelper.isValidTime(end)) {
            return "Error: Times must be in HH:MM format (24-hour).";
        }
        int startMin = DateHelper.toMinutes(start);
        int endMin = DateHelper.toMinutes(end);
        String openTime = DataManager.getSettingString("OPEN_TIME");
        String closeTime = DataManager.getSettingString("CLOSE_TIME");

        if (startMin < DateHelper.toMinutes(openTime) || endMin > DateHelper.toMinutes(closeTime)) {
            return "Error: Times must be inside operating hours (" + openTime + " to " + closeTime + ").";
        }
        if (endMin <= startMin) {
            return "Error: The end time must be after the start time.";
        }
        int minDuration = DataManager.getSettingInt("MIN_DURATION_MIN");
        int maxDuration = DataManager.getSettingInt("MAX_DURATION_MIN");
        int duration = endMin - startMin;
        if (duration < minDuration || duration > maxDuration) {
            return "Error: The duration must be between " + minDuration + " and " + maxDuration + " minutes.";
        }
        return null;
    }

    private static String checkVenue(Venue venue, int attendees) {
        if (venue == null || !venue.isActive()) {
            return "Error: That venue is not available.";
        }
        if (attendees > venue.getCapacity()) {
            return "Error: This venue holds at most " + venue.getCapacity() + " people.";
        }
        return null;
    }

    private static boolean hasDuplicate(int requesterId, int venueId, String date, String start, String end, int ignoreRequestId) {
        for (Request q : DataManager.getRequests()) {
            boolean active = q.getStatus().equals("Pending") || q.getStatus().equals("Approved");
            if (active && q.getId() != ignoreRequestId && q.getRequesterId() == requesterId
                    && q.getVenueId() == venueId && q.getDate().equals(date)
                    && q.getStartTime().equals(start) && q.getEndTime().equals(end)) {
                return true;
            }
        }
        return false;
    }

    private static String findBlackoutReason(String date) {
        for (Blackout b : DataManager.getBlackouts()) {
            if (b.getDate().equals(date)) {
                return b.getReason();
            }
        }
        return null;
    }

    // ===== Small helpers =====

    private static boolean cancelled() {
        System.out.println("Cancelled.");
        return false;
    }

    private static int countPending(int requesterId) {
        int count = 0;
        for (Request q : DataManager.getRequests()) {
            if (q.getRequesterId() == requesterId && q.getStatus().equals("Pending")) {
                count++;
            }
        }
        return count;
    }

    private static boolean isUpcoming(String date, String startTime) {
        int dayDifference = DateHelper.toDayNumber(date) - DateHelper.toDayNumber(DateHelper.today());
        if (dayDifference > 0) {
            return true;
        }
        return dayDifference == 0 && DateHelper.toMinutes(startTime) > DateHelper.nowMinutes();
    }

    private static Event findApprovedEventOfRequest(int requestId) {
        for (Event e : DataManager.getEvents()) {
            if (e.getRequestId() == requestId && e.getStatus().equals("Approved")) {
                return e;
            }
        }
        return null;
    }

    private static void insertIntoVenueList(Venue venue, Event ev) {
        ArrayList<Event> list = venue.getApprovedEvents();
        int index = SearchHelper.lowerBound(list, ev.getDate() + " " + ev.getStartTime());
        list.add(index, ev);
    }

    private static void removeFromVenueList(Event ev) {
        Venue venue = DataManager.findVenueById(ev.getVenueId());
        if (venue == null) {
            return;
        }
        ArrayList<Event> list = venue.getApprovedEvents();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == ev.getId()) {
                list.remove(i);
                return;
            }
        }
    }

    private static ArrayList<Venue> getActiveVenues() {
        ArrayList<Venue> result = new ArrayList<Venue>();
        for (Venue v : DataManager.getVenues()) {
            if (v.isActive()) {
                result.add(v);
            }
        }
        return result;
    }

    private static void printVenueList(ArrayList<Venue> list) {
        System.out.println("Available venues:");
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("%d - %s (capacity %d)%n", i + 1, list.get(i).getName(), list.get(i).getCapacity());
        }
    }

    private static String venueName(int venueId) {
        Venue v = DataManager.findVenueById(venueId);
        if (v == null) {
            return "Unknown";
        }
        return v.getName();
    }

    private static String dayName(String date) {
        return dayNames[DateHelper.dayOfWeek(date)];
    }

    private static void printTableHeader() {
        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-6s | %-25s | %-15s | %-10s | %-11s | %-9s%n",
                "No.", "ID", "Title", "Venue", "Date", "Time", "Status");
        System.out.println("--------------------------------------------------------------------------------------------------");
    }

    private static void printRow(int number, int id, BookingRecord b) {
        System.out.printf("%-4d | %-6d | %-25.25s | %-15.15s | %-10s | %-11s | %-9s%n",
                number, id, b.getTitle(), venueName(b.getVenueId()), b.getDate(),
                b.getStartTime() + "-" + b.getEndTime(), b.getStatus());
    }
}
