# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

**School Calendar Year Booking System**: a Java terminal program in which students, faculty and staff request a venue, date and time for a school event. The Admin approves or rejects each request, and approved events appear in the school calendar. This is a Data Structures final project for Group 1 · Section C2B (Titong, Barbin, De Leon, Inandan, Silang). The GitHub repo is `jacobtitong/final-project-datastrc`.

The authoritative spec is `School-Calendar-Year-Booking-System-Implementation-Guide.md`. It is **not committed here**. In Barbin's workspace it sits one level up (`../`). Section numbers below (§) refer to it. If it is available, read the relevant sections before writing code. Never change a schema or a cross-folder signature; only extend.

## Build & run

There is no build tool, no linter and no test suite. The team runs the program from VS Code: open `src/silang_menus-calendar/Main.java` and press Run. `.vscode/settings.json` marks each member folder as a **source root**, so all classes are in the default package. Do not add `package` lines.

Command-line equivalent, from the repo root:

```bash
javac -d out $(find src -name '*.java')
```

```bash
java -cp out Main
```

- The program creates seven `;`-separated `.txt` files in the **current working directory**. They are git-ignored, as are `bin/` and `out/`. For throwaway runs, `cd` to a scratch directory and use `java -cp <repo>/out Main` so the real data stays untouched.
- `Scanner` reads stdin, so you can smoke-test by piping input, e.g. `printf '0\n' | java -cp out Main` opens the Guest menu and exits.
- On the first run the program seeds an Admin with email `admin` + `EMAIL_DOMAIN` (`admin@dlsl.edu.ph`), password `Admin123`, and the default settings from §4.

## Architecture

One folder per member under `src/`. **A member edits only their own folder.** Code against the cross-folder signatures in §10.3 and never rewrite a teammate's file.

| Folder | Owner | Contents |
| --- | --- | --- |
| `titong_model-data` | Titong | Entities (`BookingRecord` ← `Request`, `Event`; `Venue`, `Blackout`, `StatusLog`, `Setting`) and `DataManager` |
| `barbin_queue-stack` | Barbin | `QueueNode`/`RequestQueue` (linked-list FIFO) and `Decision`/`DecisionStack` (an `ArrayList` used as a LIFO) |
| `deleon_sorting-searching` | De Leon | `MergeSorter` (recursive and stable) and `SearchHelper` (`lowerBound` and `findEventIndex` binary searches, linear `searchKeyword`) |
| `inandan_requests-dates` | Inandan | `DateHelper` (hand-written date math, day numbers from 2000-01-01) and `RequestManager` (submit, edit, cancel, review queue, undo, reschedule, conflict check, slot suggestion; it prompts and saves by itself) |
| `silang_menus-calendar` | Silang | `Main`, `InputHelper`, `User` ← `Guest`/`Requester`/`Admin`, `CalendarManager`, `ReportManager` |

`src/do-not-delete/` is a placeholder; leave it alone.

**Control flow:** `Main` calls `DataManager.loadAll()`, then runs `User current = new Guest(); while (current != null) { current = current.displayMenu(); }`. Each role's `displayMenu()` runs its own menu loop and returns the next user: logout returns a new `Guest`, login returns the logged-in user, and Exit returns `null`. Role menus delegate the request and event workflows to `RequestManager`, and calendar, search and report screens to `CalendarManager`/`ReportManager`.

**`DataManager` is the static hub.** It holds every master `ArrayList`, the single `RequestQueue` and the single `DecisionStack`, the settings, `generateId()` (one global `NEXT_ID` counter), and the `find...ById` lookups. It is the **only** class that reads or writes files. Startup order: settings → users → venues → events → requests → status log → blackouts → queue rebuild.

Invariants that span several files:
- The queue holds the **same `Request` objects** as the master list. It is rebuilt from `Pending` requests in file order at startup and is never saved, so a Defer lasts only until the program closes.
- Each `Venue.approvedEvents` list holds `Approved` events only, **sorted by `date + " " + startTime`**, and is rebuilt at startup. Inserts, conflict checks and day views use `SearchHelper.lowerBound`. A conflict check only needs the two neighbours, with the `BUFFER_MIN` gap between them (§6).
- Master lists stay sorted by id, because records are only appended. Never reorder them; sort a copy (`new ArrayList<>(list)`). For a sort by venue, organization or status, sort by `"DATETIME"` first and rely on stability.
- `DecisionStack` covers the current run only. It must be **cleared** whenever anything other than approve/reject changes requests or events: edits, cancels, reschedules, deactivation.
- File format: one record per line, fields joined by `;`. No field is ever empty (`none` is written instead), and user text may never contain `;`. A whole file is rewritten after every successful change. Malformed lines are skipped with a warning.
- Every status change appends a `StatusLog` row. Nothing is hard-deleted: "delete" means a status change or `active=false`.
- All limits come from `settings.txt` through `DataManager.getSettingInt/getSettingString`. Never hard-code them.

## Coding rules (§0: these override "better" practices)

The code must look like the team's earlier beginner projects. Use plain classes, `Scanner`, static helpers, `while` menu loops with `switch`, and `try/catch` around parsing. Validation helpers loop until the input is valid, print errors starting with `Error: `, and treat `back` as cancel (`null` for text, `-1` for numbers). Tables use `printf` with fixed widths. Comments are rare and one line long. Build only what the spec states.

**Forbidden:** packages, nested/inner/anonymous classes, `record`, `enum`, `interface`, lambdas, streams, method references, `Optional`, `var`, switch expressions, `HashMap`/`Set`/`LinkedList`, any `java.util` queue or stack, `Collections.sort`/`Arrays.sort`/`Comparator`/`binarySearch`, recursion outside Merge Sort, try-with-resources, `Files`/NIO, and `java.time` other than `LocalDate.now().toString()` and `LocalTime.now()`. The queue, the stack, the sorting and the searching must stay self-written.

## Git workflow (§10.4–10.5)

- Branch `feature/<member>-<folder-kind>` (e.g. `feature/barbin-queue-stack`) and merge into `main` through a PR that a teammate reviews.
- At least one commit per step listed in §10.4, **at most 3 files per commit**, all inside the member's own folder. Push after every commit.
- Commit messages start with the folder name, e.g. `barbin_queue-stack: implement Step 2 FIFO operations`.
- Stage exact paths only: never `git add .` or `git add -A`. Never `git push --force`; if a push is rejected, run `git pull origin <branch>` and push again.
- Never commit `*.txt`, `bin/`, `out/`, or `.DS_Store`. `.DS_Store` is not in `.gitignore`, so it shows as untracked.
- When producing code for a member, also produce `GIT_STEPS_<member>.md` inside that member's folder (§10.5).
