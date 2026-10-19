# GIT_STEPS_barbin.md

**Member:** Barbin · **Folder:** `barbin_queue-stack` · **Branch:** `feature/barbin-queue-stack`

This is the **short version**: all of your folder (every step of §10.4, Steps 0 to 5) is already built, so you push it in 2 commits. One commit is not possible, because a commit may touch at most 3 files and you have 5 (4 classes plus this guide).

## 1. Setup

```
git clone <repo-url>
cd <the folder git just created>
git checkout -b feature/barbin-queue-stack
```

Make sure the repo already has Titong's setup (`.gitignore`, `.vscode/settings.json`). If you cloned before he merged it, run `git pull origin main` first.

## 2. Put the files in place

Copy these 5 files into `src/barbin_queue-stack/` (create the folder if it does not exist yet):

- `QueueNode.java`, `RequestQueue.java`, `Decision.java`, `DecisionStack.java`
- `GIT_STEPS_barbin.md`

Touch nothing in anyone else's folder.

## 3. Check before each commit

```
git pull origin main
```

Open the files in VS Code and look at View → Problems. `Decision.java` and `DecisionStack.java` depend only on each other. `QueueNode.java` and `RequestQueue.java` use `Request`, which is Titong's, so they show red until his `Request.java` is in `main`. After it is merged, `git pull origin main` again and the red errors disappear. Any other red error is a real problem: fix it before committing.

## 4. Commit 1 of 2: queue and Decision

```
git add src/barbin_queue-stack/QueueNode.java src/barbin_queue-stack/RequestQueue.java src/barbin_queue-stack/Decision.java
git commit -m "barbin_queue-stack: add QueueNode, RequestQueue and Decision"
git push -u origin feature/barbin-queue-stack
```

**On GitHub you should see:** the branch `feature/barbin-queue-stack` with one commit and 3 files in `src/barbin_queue-stack/`.

## 5. Commit 2 of 2: DecisionStack and this guide

```
git add src/barbin_queue-stack/DecisionStack.java src/barbin_queue-stack/GIT_STEPS_barbin.md
git commit -m "barbin_queue-stack: add DecisionStack and Git guide"
git push
```

**On GitHub you should see:** 2 commits and 5 files in `src/barbin_queue-stack/`.

Never use `git add .` or `git add -A`. Titong's `DataManager` creates your `RequestQueue` and `DecisionStack`, so push soon.

## 6. Merge into `main`

1. On GitHub click **Compare & pull request** (base `main`, compare `feature/barbin-queue-stack`).
2. A teammate reviews and merges it. Do not merge your own PR.
3. Then everyone runs:

```
git checkout main
git pull origin main
git checkout feature/barbin-queue-stack
git merge main
```

## 7. Fixes

- **Push rejected?** Run `git pull origin feature/barbin-queue-stack` and push again. Never `git push --force`.
- **Wrong file added?** Run `git status` and only `git add` the exact paths shown above.

## Note about the guide's commit rules

§10.5 says there must be at least one commit per step (6 commits for Steps 0 to 5). These 2 commits meet the "at most 3 files, all in your own folder" rule but not the "one commit per step" rule. If your team checks commit history, tell me and I will split the files into step-by-step commits.
