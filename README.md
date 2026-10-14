# School Calendar Year Booking System — Implementation Guide (Source of Truth)

Group 1 · Section C2B — Titong, Barbin, De Leon, Inandan, Silang

**For any AI agent or developer implementing this program**: this file is the single authoritative spec, built from the Final Project Proposal. Read it fully before writing code. Do not change a schema, class contract, or rule here — extend only. If this file and a chat summary disagree, this file wins.

## 0. Skill-Level Rules (READ FIRST — these override any "better" way you know)

Write code like the team's earlier projects (Vehicle Rental, Inventory, Library): plain classes, `Scanner`, static helper methods, `while` menu loops with `switch`, `try/catch` around parsing. Simplest code that works; more lines over cleverness; one short comment above every method.

| ✅ ALLOWED | ❌ FORBIDDEN |
| --- | --- |
| Default package (no `package` statements, no `import` of our own classes; member folders are source roots, not packages — see §10) | Packages, inner/nested/anonymous classes, `record`, `enum` (use `String` constants like `"Pending"`) |
| Classes, `abstract` classes, `extends`, `super(...)`, `@Override`, `private` fields + getters/setters, constructors, `static` fields/methods | Lambdas (`->`), streams, method references, `Optional`, `var`, switch expressions, `interface` (not needed) |
| `ArrayList<Type>`: `add`, `add(index, x)`, `get`, `set`, `size`, `remove(index)`, loops; plain arrays | `HashMap`, `HashSet`, `Map`, `Set`, `LinkedList`, `java.util.Queue`/`Stack`/`Deque`/`PriorityQueue` |
| Our own `QueueNode` (a normal top-level class with a `next` field) for the queue | `Collections.sort`, `Arrays.sort`, `List.sort`, `Arrays.binarySearch`, `Comparator` |
| Recursion — **only** inside Merge Sort | Other recursion, `try`-with-resources, `Files`/`Path`/NIO, threads, reflection, external libraries, GUI |
| `Scanner(System.in)`, `System.out.print/println/printf`, `String.format` | `BigDecimal`, `MessageDigest`, regex lookaheads |
| `String`: `trim`, `equals`, `equalsIgnoreCase`, `split`, `matches` (simple regex), `substring`, `length`, `contains`, `toLowerCase`, `compareTo`, `charAt` | `java.time` other than the three calls listed in the next row |
| `Integer.parseInt`, `Math.round/max/min/abs`, `try/catch/finally`, `throw new`, `NumberFormatException`, `IOException` |  |
| `BufferedReader`+`FileReader`, `PrintWriter`+`FileWriter` |  |
| `LocalDate.now().toString()`, `LocalTime.now()` (`getHour()`, `getMinute()`) — **only these** for "today"/"now" |  |
| `String.valueOf(password.hashCode())` for passwords (coursework-level protection, not real security — say so in the docs) |  |

**Technology**: Java + plain `.txt` files only; terminal only. Run with the working directory = project root so `"data/users.txt"` resolves. **The five Data Structures concepts are written by us** (§5): `RequestQueue`, `DecisionStack`, `MergeSorter`, `SearchHelper` (Binary + Linear Search). Never swap them for a library class.

## 1. Project Layout — One Folder per Team Member

Folder name = `<member>_<kind>`. **One member edits one folder; nobody creates, edits, renames, or deletes files in another member's folder.**

```
SchoolCalendar/
  src/
    titong_model-data/           Titong  — model classes + DataManager
    barbin_queue-stack/          Barbin  — RequestQueue + DecisionStack
    deleon_sorting-searching/    De Leon — MergeSorter + SearchHelper
    inandan_requests-dates/      Inandan — RequestManager + DateHelper
    silang_menus-calendar/       Silang  — Main, users, InputHelper, CalendarManager, ReportManager
  data/                          runtime .txt files (not committed)
```

Full file lists, signatures, and commit steps are in §10.

**OOP used (kept light)**: `Request extends BookingRecord` and `Event extends BookingRecord` so one Merge Sort and one search work on both; `Guest`/`Requester`/`Admin extends User` with `public abstract User displayMenu();`. Every field `private`, accessed via getters/setters. `displayMenu()` runs that role's menu loop and **returns the next user**: logout → `new Guest()`; Guest after login → the logged-in user; Exit → `null`. `Main`: `User current = new Guest(); while (current != null) { current = current.displayMenu(); }`

## 2. Classes

| Class | Fields (all `private`) | Notes |
| --- | --- | --- |
| `User` | `int id; String role; String idNumber, name, email, passwordHash, type, organization; boolean active` | type = Student/Faculty/Staff/Other |
| `BookingRecord` (abstract) | `String title, organizer, organization; int venueId; String date, startTime, endTime, status` | `String getSortKey(String mode)` (§5.3); `getStartMinutes()`, `getEndMinutes()`; `String getVenueKey()` helper |
| `Request` (extends BookingRecord) | `int id, requesterId, attendees, queueNumber; String purpose, rejectReason, submittedAt` | status: `Pending`/`Approved`/`Rejected`/`Cancelled` |
| `Event` (extends BookingRecord) | `int id, requestId; String cancelReason` | status: `Approved`/`Cancelled`. Calendar shows `Approved` only |
| `Venue` | `int id; String name; int capacity; String details; boolean active; ArrayList<Event> approvedEvents` | `approvedEvents` is **kept sorted** by key `date + " " + startTime`; only `Approved` events live in it; not saved to file (rebuilt at startup) |
| `Blackout` | `int id; String date, reason` |  |
| `StatusLog` | `int requestId; String timestamp, oldStatus, newStatus; int actorId; String remarks` | one row per status change, never removed |
| `Setting` | `String key, value` | §4 |
| `Decision` | `int requestId; String type ("Approved"/"Rejected"); int eventId; ArrayList<Integer> autoRejectedIds` | stack entry |

Every model class has a full constructor, getters, setters only where values change, and `String toFileLine()` (fields joined by `;`).

## 3. Data Files (`data/*.txt`)

**Format**: one record per line, fields separated by **`;`**; no field is ever empty (write `none`); text input may never contain `;` (§7). Read with `line.split(";")` + `Integer.parseInt`. **Always rewrite a whole file from its ArrayList after every successful change.** Only `DataManager` touches files.

```java
// Read: missing file -> create it; malformed line -> skip with a warning, never crash
try {
    BufferedReader br = new BufferedReader(new FileReader("data/venues.txt"));
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
    saveVenues();   // file did not exist yet: create it (empty)
}
```

| File | Fields (in order) |
| --- | --- |
| `users.txt` | id;role;idNumber;name;email;passwordHash;type;organization;active — role `REQUESTER`/`ADMIN` |
| `venues.txt` | id;name;capacity;details;active |
| `requests.txt` | id;requesterId;title;organizer;organization;purpose;attendees;venueId;date;startTime;endTime;status;rejectReason;submittedAt;queueNumber |
| `events.txt` | id;requestId;title;organizer;organization;venueId;date;startTime;endTime;status;cancelReason |
| `status_log.txt` | requestId;timestamp;oldStatus;newStatus;actorId;remarks |
| `blackouts.txt` | id;date;reason |
| `settings.txt` | key;value |

- **IDs**: one global counter `NEXT_ID` in `settings.txt`; `DataManager.generateId()` returns it, adds 1, saves. IDs only ever grow.
- **Master lists are sorted by id** because new records are always appended and never removed (undo-of-approval is the one exception and removes the newest event, so order still holds). **Never reorder a master list** — sort a copy: `new ArrayList<>(list)`. This lets us find any record by id with Binary Search (§5.4).
- **Startup order**: load settings → users → venues → events (insert each `Approved` event into its venue's sorted list via Binary Search) → requests → status log → blackouts → build the queue.
- **Queue persistence**: each Request has `queueNumber` (set to `generateId()` on submit and again on defer). At startup, copy all `Pending` requests, Merge Sort them by `"QUEUE"`, enqueue in that order. This restores FIFO order and deferrals between sessions.
- **First run**: seed one Admin — email `admin` + `EMAIL_DOMAIN`, password `Admin123`, idNumber `none`; write default settings (§4).
- **Timestamp**: `LocalDate.now().toString() + " " + hh:mm` built from `LocalTime.now()`.

## 4. Settings (`settings.txt`) — placeholders, all Admin-editable

Marked **\[PLACEHOLDER\]** = the group has not confirmed the value; keep it as a setting so it can be changed without editing code.

| Key | Default | Allowed range when Admin edits |
| --- | --- | --- |
| `OPEN_TIME` / `CLOSE_TIME` | 07:00 / 18:00 | open \< close |
| `MIN_DURATION_MIN` / `MAX_DURATION_MIN` | 60 / 480 | 15–720, min ≤ max |
| `MIN_LEAD_DAYS` | 7 | 0–60 |
| `MAX_ADVANCE_DAYS` | 180 | 30–730 |
| `BUFFER_MIN` | 30 | 0–120 |
| `MAX_PENDING` | 3 | 1–10 |
| `MIN_PASSWORD` | 8 | 6–20 |
| `ID_DIGITS` **\[PLACEHOLDER\]** | 8 (ID number = exactly this many digits) | 4–12 |
| `EMAIL_DOMAIN` **\[PLACEHOLDER\]** | `@dlsl.edu.ph` | must start with `@` |
| `YEAR_START` / `YEAR_END` **\[PLACEHOLDER\]** | 2026-08-01 / 2027-07-31 | valid dates, start \< end |
| `NEXT_ID` | 1 | system only |
| Helpers `DataManager.getSettingInt("BUFFER_MIN")` / `getSettingString(...)` read these; **no limit is ever hard-coded elsewhere.** |  |  |

## 5. Data Structures & Algorithms (self-written)

**5.1 `RequestQueue` (FIFO, linked nodes)** — fields `QueueNode front, back; int size`. `QueueNode` has `Request data; QueueNode next`. Methods: `enqueue(Request)` (back, O(1)), `dequeue()` (front, O(1), null if empty), `peek()`, `isEmpty()`, `size()`, `remove(int requestId)` (walk the list, unlink; used for auto-reject/cancel), `insertInOrder(Request)` (insert before the first node whose `queueNumber` is larger; used by undo), `toArrayList()` (for display).

```java
public void enqueue(Request r) {
    QueueNode node = new QueueNode(r);
    if (back == null) {
        front = node;
        back = node;
    } else {
        back.setNext(node);
        back = node;
    }
    size++;
}
```

The queue holds the **same Request objects** as the master list, so edits show up everywhere.

**5.2 `DecisionStack` (LIFO)** — backed by `ArrayList<Decision>`: `push` = `add`, `pop` = `remove(size-1)`, `peek`, `isEmpty`, `clear`. Holds Admin approve/reject decisions of the **current run only** (never saved). **Cleared whenever anything else changes requests or events** (Requester edits/cancels, Admin reschedules/cancels an event, account deactivation), so Undo can never reverse stale data.

**5.3 `MergeSorter`** — `static void mergeSort(ArrayList<BookingRecord> list, String mode)`; recursive split in halves, merge with a temp list; on a tie take the **left** item (this makes it stable). Compare `a.getSortKey(mode).compareToIgnoreCase(b.getSortKey(mode)) <= 0`.

| mode | `getSortKey` returns |
| --- | --- |
| `"DATETIME"` | `date + " " + startTime` (zero-padded, so text order = time order) |
| `"VENUE"` | venue name (looked up by `venueId`) |
| `"ORGANIZATION"` | `organization` |
| `"STATUS"` | `status` |
| `"QUEUE"` | `String.format("%09d", queueNumber)` (Requests only) |
| To sort by venue/organization/status **first sort by `"DATETIME"`, then by the chosen mode** — stability keeps same-group items in time order. |  |

```java
private static void mergeSort(ArrayList<BookingRecord> list, int left, int right, String mode) {
    if (left >= right) {
        return;
    }
    int mid = (left + right) / 2;
    mergeSort(list, left, mid, mode);
    mergeSort(list, mid + 1, right, mode);
    merge(list, left, mid, right, mode);
}
```

Used for: Admin/Requester/Guest result lists, calendar views, rebuilding the queue at startup.

**5.4 `SearchHelper`**

- **Binary Search** (all sorted lists): `static int lowerBound(ArrayList<Event> list, String key)` returns the first index whose `date + " " + startTime` is `>= key` (or `list.size()` if none). Used for (a) the insert position of a new event, (b) conflict detection, (c) finding the first event on a date. `static int findById(ArrayList<...> list, int id)` is a classic binary search on the id-sorted master lists (returns -1 if absent) — write one version per list type.
- **Linear Search**: `static ArrayList<BookingRecord> searchKeyword(ArrayList<BookingRecord> list, String keyword)` — loop; keep a record if `title.toLowerCase().contains(keyword.toLowerCase())` or the same for `organizer`.

## 6. Date, Time & Conflict Logic

**`DateHelper`** (all static; dates `YYYY-MM-DD`, times `HH:MM` 24-hour; prompts always show the format): `isLeapYear(y)`, `daysInMonth(y,m)`, `isValidDate(String)`, `isValidTime(String)`, `toMinutes("13:05")` → 785, `toTimeString(785)` → `"13:05"`, `toDayNumber(String date)` (days since 2000-01-01 — count whole years then months with loops), `fromDayNumber(int)`, `dayOfWeek(String date)` (2000-01-01 was a Saturday; use `dayNumber % 7`), `today()` = `LocalDate.now().toString()`, `nowMinutes()`.

- **Lead time** = `toDayNumber(date) − toDayNumber(today())`.
- **Week view** = Sunday–Saturday around the chosen date (`startDayNumber = dayNumber − dayOfWeek`).

**Buffer rule**: two events at the same venue/date must have a gap of **at least `BUFFER_MIN`** between one's end and the next one's start (one buffer between them, not two).

**Conflict check** `RequestManager.hasConflict(venueId, date, startMin, endMin, ignoreEventId)`:

1. `list = venue.approvedEvents` (temporarily skip `ignoreEventId` when rescheduling).
2. `idx = SearchHelper.lowerBound(list, date + " " + startTime)`.
3. `prev = list[idx-1]` (if exists and `prev.date` equals `date`): conflict if `startMin < prev.endMin + BUFFER`.
4. `next = list[idx]` (if exists and `next.date` equals `date`): conflict if `endMin + BUFFER > next.startMin`. Approved events never overlap each other, so checking only these two neighbours is enough. Return the conflicting event so the message can name it.

**Next free slot** `RequestManager.suggestSlot(venueId, date, startMin, endMin)` (same duration, same date, same venue):

1. Candidate starts = `OPEN_TIME` plus, for each approved event that day (from `lowerBound(date + " 00:00")` while date equals), `event.endMin + BUFFER`.
2. A candidate **fits** if `candidate + duration <= CLOSE_TIME` and no event starts before `candidate + duration + BUFFER` unless it ended `BUFFER` before `candidate`.
3. Pick the fitting candidate **closest to the requested start** (earlier wins a tie). Print `10:30-12:15`, or "No free slot on this date at this venue."

**On approval, auto-reject**: after adding the event, loop the queue; every Pending request with the same venue and date that now conflicts (use the same neighbour rule against the new event) → status `Rejected`, `rejectReason = "Conflicts with an approved event."`, `queue.remove(id)`, status-log row, and its id goes into `Decision.autoRejectedIds`. Admin reschedule uses the same step.

## 7. Validation Rules (every prompt: re-ask on error; accept `0` or `back` to cancel; use try/catch like `getValidInt` in the earlier projects)

| Input | Rule |
| --- | --- |
| Any text | not blank; no leading/trailing spaces; never contains `;`; length limits below |
| Name | 2–40; letters, spaces, hyphens, periods |
| Title / organizer / organization | 3–60 / 2–40 / 2–40; letters, digits, spaces, `. , - ' & ( )` |
| Purpose | 5–150 chars, same character set |
| Reasons / remarks (reject, cancel) | 5–100 chars, not blank |
| ID number | exactly `ID_DIGITS` digits; unique |
| Email | `[A-Za-z0-9._]+` + `EMAIL_DOMAIN`; unique |
| Password | ≥ `MIN_PASSWORD` chars, no spaces, not equal to the ID number |
| Menu choice | integer within the menu range; otherwise message + re-ask |
| Date | real date (month 1–12, valid day, leap years), not in the past |
| Time | `HH:MM`, 00:00–23:59, inside `OPEN_TIME`–`CLOSE_TIME`; end > start; duration within `MIN_DURATION_MIN`–`MAX_DURATION_MIN` |
| Attendees | whole number ≥ 1 and ≤ venue capacity |
| Venue capacity | whole number 1–5000 |
| Venue | must exist and be `active` |

**Request-submission checks, in this order** (first failure stops with its message):

1. Requester active, and Pending count \< `MAX_PENDING`.
2. Date valid; lead time ≥ `MIN_LEAD_DAYS`; ≤ `MAX_ADVANCE_DAYS` away; inside `YEAR_START`–`YEAR_END`; not a blackout date.
3. Times valid, inside operating hours, duration in range.
4. Venue exists/active; attendees ≤ capacity.
5. Not a duplicate: the same requester has no Pending/Approved request with the same venue, date, start and end.
6. No conflict with an approved event (§6). On conflict print the conflicting event **and** the suggested free slot, then offer to re-enter. Edit of a Pending request re-runs checks 2–6 (ignoring itself in check 5); the Pending limit is not re-checked.

**Status rules**: only `Pending` can be edited/approved/rejected/deferred. `Rejected` and `Cancelled` are final (the Requester submits a new request). Ask `Y/N` confirmation before every cancel, reject, remove, or deactivate. Nothing is ever hard-deleted — "delete" = status change or `active=false`. Every status change appends a `StatusLog` row (remarks = reason or `none`). **Admin-only guards**: venue can't be deactivated if it has an `Approved` event dated today or later; a blackout can't be added on a date with an `Approved` event; Admin reschedule must pass §6 conflict, blackout, hours, duration and not-in-the-past checks (lead time is not enforced for Admin). **Stability**: a missing file is created at startup; malformed lines are skipped with a warning; save after every successful change; each role only reaches its own menu.

## 8. Menus & Flows

**Guest**: 1 Calendar (day/week/month/venue) · 2 Event details · 3 Search & filter · 4 Login · 5 Register · 0 Exit. **Requester**: 1–3 as Guest · 4 Submit request · 5 My requests (status, history, remarks) · 6 Edit a Pending request · 7 Cancel a request (Pending or Approved, event date still in the future) · 8 Edit profile · 9 Deactivate account · 0 Logout. **Admin**: 1 Review queue · 2 Undo last decision · 3 View/search/filter/sort all requests and events · 4 Reschedule an event · 5 Cancel an event · 6 Venues (add/list/edit/remove) · 7 Blackout dates (add/list/edit/remove) · 8 Settings · 9 Deactivate a user · 10 Summary report · 11 Calendar views · 0 Logout.

- **Register**: name, ID number, email, password (confirm), type (menu), organization → validate → hash → append (`role=REQUESTER`) → back to Guest menu.
- **Login**: email + password; inactive account → "Account is deactivated."; wrong → message, retry.
- **Submit request**: prompts for title, organizer (default = profile name), purpose, attendees, venue (numbered list of active venues with capacity), date, start, end → §7 checks → create `Request` (`Pending`, `queueNumber = generateId()`), enqueue, log `none → Pending`.
- **Review queue**: show the **front** request (details + venue + position in queue). Choose: **Approve** → conflict re-check (must still pass) → create `Event`, insert into the venue's sorted list at `lowerBound`, request → `Approved`, auto-reject conflicts (§6), push a `Decision`. **Reject** → reason required → `Rejected`, push a `Decision`. **Defer** → `dequeue`, new `queueNumber`, `enqueue` (if only one request, say so; nothing changes). **Back**. Dequeue happens on approve/reject.
- **Undo** (pop the top `Decision`; if the stack is empty say "Nothing to undo"):
  - *Approved*: remove the event from the venue's sorted list and the events list; request → `Pending`; `insertInOrder` it back into the queue; each auto-rejected request → `Pending` and `insertInOrder` too; log "Decision undone".
  - *Rejected*: request → `Pending`, `insertInOrder`.
- **Cancel**: Pending → `Cancelled` + `queue.remove`. Approved (Requester: event date after today; Admin: any upcoming) → event `Cancelled` (store reason), removed from the venue's sorted list, request → `Cancelled`. Admin cancel needs a reason. Clears the DecisionStack.
- **Reschedule (Admin)**: pick an `Approved` event → new venue/date/start/end → §6 conflict check ignoring itself → remove from old position, update, re-insert at the new `lowerBound`; update the linked request's fields; auto-reject new conflicts; log "Rescheduled from … to …". Clears the DecisionStack.
- **Deactivate user** (self or Admin): `active=false`; the user's `Pending` requests → `Cancelled` ("Account deactivated"); Approved events stay. Cannot deactivate the last active Admin.
- **Search & filter**: optional keyword (Linear Search), venue, date range, organization/department, status (Admin sees all statuses; Guest/Requester calendar sees `Approved` events only) → combine with loops → Merge Sort by the user's choice (date/time, venue, organization, status) → print.
- **Calendar views** (events only): *Day* – for every active venue, `lowerBound(date + " 00:00")` and print while the date matches, then Merge Sort the collected list by time. *Week* – 7 days from Sunday. *Month* – each day with events. *By venue* – that venue's `approvedEvents` already sorted; print from today onward.
- **Summary report**: events per venue and per organization (count `Approved` events; tally with two parallel `ArrayList`s — names and counts), and **approval rate** = `Approved ÷ (Approved + Rejected) × 100`, one decimal; `Pending` and `Cancelled` are excluded. Say "No decided requests yet" when the divisor is 0.
- **Settings**: list §4 keys; Admin edits one at a time within its allowed range; `NEXT_ID` hidden.

## 9. Display (use `printf` fixed widths like `Inventory.viewAllVehicles()`)

- Menus: `=== TITLE ===`, `1 - Option`, prompt `--> Option: `.
- Event/request table: ID, Title (`%-25.25s`), Organizer, Venue, Date, Time (`08:00-10:00`), Status.
- Day view: header `=== 2026-11-03 (Tuesday) ===`, one line per event, "No events" if empty.
- Conflict message: `Conflicts with: <title> at <venue>, 08:00-10:00 (30-minute buffer applies). Next free slot: 10:30-12:15.`

## 10. Team Folders, AI Protocol & Git

### 10.1 Folders

| Folder | Owner | Files (one class per file) |
| --- | --- | --- |
| `src/titong_model-data/` | Titong | `BookingRecord`, `Request`, `Event`, `Venue`, `Blackout`, `StatusLog`, `Setting`, `DataManager` |
| `src/barbin_queue-stack/` | Barbin | `QueueNode`, `RequestQueue`, `Decision`, `DecisionStack` |
| `src/deleon_sorting-searching/` | De Leon | `MergeSorter`, `SearchHelper` |
| `src/inandan_requests-dates/` | Inandan | `DateHelper`, `RequestManager` |
| `src/silang_menus-calendar/` | Silang | `Main`, `InputHelper`, `User`, `Guest`, `Requester`, `Admin`, `CalendarManager`, `ReportManager` |

### 10.2 Using this guide with an AI

**Human prompt** (attach this file, fill in, paste):

> I am **Silang**. My folder is **`silang_menus-calendar`**. Follow the guide: build my folder step by step (Step 0 first), then give me the separate GitHub commit-and-push guide.

**AI protocol** — when a member gives a name and a folder (or only a name: look the folder up in §10.1):

1. Find the row in §10.1. Its files are the **only** files you may create or edit. Never touch another member's folder. If the name and folder do not match §10.1, stop and ask.
2. Obey §0 (Skill-Level Rules) and every rule in this guide.
3. For anything outside the folder, code against the exact signatures in §10.3. Never rewrite a teammate's code and never invent different signatures.
4. Work in the order of that folder's steps in §10.4 (Step 0 first). If the member names specific steps, do only those.
5. Your output must contain: (a) every file with its full path; (b) the separate Git guide described in §10.5; (c) a short list of anything unclear that a teammate must confirm.

### 10.3 Cross-Folder Contract (exact signatures — never change without every owner agreeing)

Constructors take every field in the §2 table **in order** (a subclass: parent fields first); getters `getX()` (booleans `isX()`); setters `setX(v)`; every model has `String toFileLine()`. `BookingRecord` also has `String getSortKey(String mode)`, `int getStartMinutes()`, `int getEndMinutes()`.

| Class (owner) | Public signatures others may call (all `static` unless noted) |
| --- | --- |
| `DataManager` (Titong) | `void loadAll()` · `ArrayList<User> getUsers()` · `getVenues()` · `getEvents()` · `getRequests()` · `getStatusLogs()` · `getBlackouts()` · `getSettings()` (each returns its `ArrayList<...>`) · one `saveX()` per list · `int getSettingInt(String key)` · `String getSettingString(String key)` · `void setSetting(String key, String value)` · `int generateId()` · `RequestQueue getQueue()` · `DecisionStack getDecisionStack()` · `void addStatusLog(int requestId, String oldStatus, String newStatus, int actorId, String remarks)` · `String timestamp()` · `Venue findVenueById(int id)` · `Request findRequestById(int id)` · `Event findEventById(int id)` · `User findUserById(int id)` · `User findUserByEmail(String email)` (find methods return `null` if absent) |
| `RequestQueue` (Barbin; instance) | `void enqueue(Request r)` · `Request dequeue()` · `Request peek()` · `boolean isEmpty()` · `int size()` · `boolean remove(int requestId)` · `void insertInOrder(Request r)` · `ArrayList<Request> toArrayList()` |
| `DecisionStack` (Barbin; instance) · `Decision` | `void push(Decision d)` · `Decision pop()` · `Decision peek()` · `boolean isEmpty()` · `void clear()` · `Decision(int requestId, String type, int eventId)` · `void addAutoRejected(int requestId)` · `ArrayList<Integer> getAutoRejectedIds()` |
| `MergeSorter` (De Leon) | `void mergeSort(ArrayList<BookingRecord> list, String mode)` |
| `SearchHelper` (De Leon) | `int lowerBound(ArrayList<Event> list, String key)` · `int findRequestIndex(ArrayList<Request> list, int id)` · `findEventIndex(...)` · `findVenueIndex(...)` · `findUserIndex(...)` (binary search by id, `-1` if absent) · `ArrayList<BookingRecord> searchKeyword(ArrayList<BookingRecord> list, String keyword)` |
| `DateHelper` (Inandan) | `boolean isLeapYear(int y)` · `int daysInMonth(int y, int m)` · `boolean isValidDate(String d)` · `boolean isValidTime(String t)` · `int toMinutes(String t)` · `String toTimeString(int min)` · `int toDayNumber(String d)` · `String fromDayNumber(int n)` · `int dayOfWeek(String d)` (0 = Sunday) · `String today()` · `int nowMinutes()` |
| `RequestManager` (Inandan; prompts and saves by itself) | `Event findConflict(int venueId, String date, int startMin, int endMin, int ignoreEventId)` (null if none) · `String suggestSlot(int venueId, String date, int startMin, int endMin)` · `void submitRequest(Requester r)` · `void editRequest(Requester r)` · `void cancelRequest(User actor)` · `void reviewQueue(Admin a)` · `void undoLastDecision(Admin a)` · `void rescheduleEvent(Admin a)` · `void cancelEvent(Admin a)` · `void deactivateUser(User actor)` |
| `InputHelper` (Silang) | `int getValidInt(String prompt, int min, int max)` · `String getValidName(...)` · `String getValidText(String prompt, int minLen, int maxLen)` · `String getValidEmail(String prompt)` · `String getValidPassword(String prompt)` · `String getValidDate(String prompt)` · `String getValidTime(String prompt)` · `boolean getYesNo(String prompt)` · `String hashPassword(String plain)` |
| `CalendarManager` / `ReportManager` (Silang) | `void showCalendarMenu()` · `void showEventDetails()` · `void searchAndFilter(boolean adminView)` · `void viewAllRecords()` (Admin) · `void printSummaryReport()` |

Menu-owned work (Silang): register, login, edit profile, and the venue / blackout / settings CRUD inside `Admin`.

### 10.4 Commit Steps per Folder (each step = at least one commit)

**Step 0 for everyone = a stub commit**: every file in your folder with the exact public signatures from §10.3, bodies empty or returning a dummy value, compiling. Merge it to `main` first so teammates can compile against you.

| Folder | Steps after Step 0 |
| --- | --- |
| `titong_model-data` | 1 `BookingRecord`, `Request` · 2 `Event`, `Venue` · 3 `Blackout`, `StatusLog`, `Setting` · 4 `getSortKey` for every mode · 5 `DataManager`: settings, defaults, `generateId` · 6 users load/save + seeded Admin · 7 venues, blackouts load/save · 8 requests, events load/save + venue sorted lists + queue rebuild · 9 status log, `timestamp`, `find...` methods, `loadAll` |
| `barbin_queue-stack` | 1 `QueueNode` · 2 `RequestQueue`: `enqueue`, `dequeue`, `peek`, `isEmpty`, `size` · 3 `remove`, `toArrayList` · 4 `insertInOrder` · 5 `Decision` · 6 `DecisionStack` |
| `deleon_sorting-searching` | 1 `lowerBound` · 2 the four `find...Index` binary searches · 3 `searchKeyword` · 4 `MergeSorter` merge step · 5 `mergeSort` recursion + all sort modes |
| `inandan_requests-dates` | 1 `DateHelper` leap/days/valid · 2 times + `today`/`nowMinutes` · 3 day numbers, weekday · 4 `findConflict` · 5 `suggestSlot` · 6 `submitRequest` · 7 `editRequest`, `cancelRequest` · 8 `reviewQueue`: approve + auto-reject · 9 reject + defer · 10 `undoLastDecision` · 11 `rescheduleEvent`, `cancelEvent` · 12 `deactivateUser` |
| `silang_menus-calendar` | 1 `InputHelper` basics (`Scanner`, `getValidInt`) · 2 rest of `InputHelper` · 3 `User`, `Guest` shell · 4 register · 5 login · 6 `Main` loop · 7 `CalendarManager` day + week views · 8 month + venue views, event details · 9 `searchAndFilter` · 10 `Requester` menu + edit profile · 11 `Admin` venues + blackouts CRUD · 12 `Admin` settings, deactivate user, `viewAllRecords` · 13 `ReportManager` |

### 10.5 GitHub Guide — MANDATORY in every output

Progress must show on GitHub in **small increments, not one big commit or push**. Every time the AI/developer produces code for a member, it must also produce a **separate** file named `GIT_STEPS_<member>.md` (saved inside that member's own folder and also shown in the reply). It must contain (examples below use one member's names — substitute the actual member, folder, and branch):

1. **Setup**: `git clone <repo-url>`, then `git checkout -b feature/silang-menus-calendar`.
2. **One section per step in §10.4** (only the steps that were built), each with: the files; the compile check; the exact `git add <file paths>`; `git commit -m "<folder>: <what changed>"` (example: `silang_menus-calendar: add Step 1 classes`); `git push -u origin feature/silang-menus-calendar` the first time and `git push` after; and a line "On GitHub you should see: …".
3. **Milestone merges**: after Step 0 and then after every 3 steps, open a Pull Request into `main`; a teammate reviews and merges it; then everyone runs `git checkout main`, `git pull origin main`, `git checkout feature/silang-menus-calendar`, `git merge main`.
4. **Fixes**: if a push is rejected, run `git pull origin feature/silang-menus-calendar` and push again. Never `git push --force`.

**Hard rules the Git guide must state and the code must respect**

- At least **one commit per step** in §10.4 — never fewer commits than steps; each commit touches **at most 3 files**, all inside the member's own folder (the Git guide file itself is committed with the last step).
- Compile before every commit (`javac -d out src/titong_model-data/*.java src/barbin_queue-stack/*.java src/deleon_sorting-searching/*.java src/inandan_requests-dates/*.java src/silang_menus-calendar/*.java`); never commit code that does not compile.
- Push after **every** commit.
- Never use `git add .` or `git add -A`; add exact paths only.
- Never commit `out/` or the runtime files in `data/` (they are created when the program runs).
- Commit messages start with the folder name, then a short description.

### 10.6 One-Time Repo Setup (done once by Titong, in its own commits)

```
SchoolCalendar/
  src/
    titong_model-data/
    barbin_queue-stack/
    deleon_sorting-searching/
    inandan_requests-dates/
    silang_menus-calendar/
  data/.gitkeep
  .gitignore
  .vscode/settings.json
  README.md
```

Each folder above is a **source root, not a Java package** — all classes stay in the default package, so there are no `package` or `import` lines for our own classes. Without the setup below, an IDE may report "package does not match folder".

- **VS Code** — commit `.vscode/settings.json`:

```json
{
  "java.project.sourcePaths": [
    "src/titong_model-data",
    "src/barbin_queue-stack",
    "src/deleon_sorting-searching",
    "src/inandan_requests-dates",
    "src/silang_menus-calendar"
  ],
  "java.project.outputPath": "out"
}
```

- **IntelliJ** — right-click each member folder → *Mark Directory as* → *Sources Root*.
- **Terminal** (project root): compile `javac -d out src/titong_model-data/*.java src/barbin_queue-stack/*.java src/deleon_sorting-searching/*.java src/inandan_requests-dates/*.java src/silang_menus-calendar/*.java` · run `java -cp out Main`.
- `.gitignore`: `out/`, `data/*.txt`, `*.class`, `.idea/`, `*.iml`. Add an empty `.gitkeep` in every empty folder so Git tracks it.

## 11. Merge & Test Order

1. Everyone merges **Step 0** first (the project compiles with stubs).
2. Next: `titong_model-data` through Step 4, `barbin_queue-stack`, `deleon_sorting-searching` (others depend on them).
3. Then `DataManager` (Steps 5–9), `DateHelper`, `InputHelper`.
4. Then `RequestManager` and the menus. A feature is testable once the folders it calls are merged (submitting a request needs `DataManager`, `DateHelper`, `SearchHelper`, `RequestQueue`, `RequestManager`, `Requester`).
5. `ReportManager` last, then a full test pass against §7 (bad dates, past dates, blackout, capacity, limit, duplicate, buffer edges, undo after auto-reject).
