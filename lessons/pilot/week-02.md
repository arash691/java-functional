# Week 2: Shell, signals, Git rescue and meeting ShopFlow

**Mode:** A (build-along), with two Git incidents (B-lite)  **Release:** v0.1 (project definition)
**Outcome:** you are comfortable in a terminal, you know why a process stops politely or abruptly, you can recover "lost" Git work, and ShopFlow is introduced with a one-page brief, ten tickets on a board and its first decision record (ADR-001). Requirements and a glossary come later, after the modeling block.

**Before you start**
- Week 1 is finished: JDK works, your `shopflow-lab` repo is on GitHub, `scripts/env-doctor.sh` exists.
- Have two terminal windows open side by side.

**Repo:** `shopflow-lab`, scripts only this week: `scripts/w02-git-incident.sh`, `scripts/w02-bisect-demo.sh`, `core/src/main/java/shopflow/w00/ShutdownDemo.java`
No test tag this week. Hints: inside the incident steps. Solutions: `git reflog` is your solution.

---

## S1 Try it (3h): the shell, signals and Git (mode A)
1. (15m) **Shell warm-up.** Read the Missing Semester lecture "The Shell" (missing.csail.mit.edu). In `shopflow-lab` run and note the output of: `pwd`, `ls -la`, `cat pom.xml | head -20`, `grep -rn "Tag(\"w03\")" core/src/test | head`, `find core -name "*.java" | wc -l`. Predict the last number before you run it.
2. (25m) **Pipes and exit codes.** (a) Count how many test classes are tagged `w03`: `grep -rl '@Tag("w03")' core/src/test | wc -l`. (b) Find the five largest files in the repo: `find . -type f -not -path "./.git/*" -not -path "*/target/*" -exec wc -c {} + | sort -n | tail -6`. (c) Run `false; echo $?` then `true; echo $?`; explain what `0` means. (d) Run `./mvnw -q -pl core test -Dgroups=w00; echo "exit=$?"` and confirm that a green build exits 0 and a red one exits non-zero. This is how CI decides pass/fail.
3. (30m) **Processes.** Start the server: `java core/src/main/java/shopflow/w00/TinyServer.java`. In another terminal: `ps aux | grep TinyServer`, `pgrep -f TinyServer`, `top -pid $(pgrep -f TinyServer)` (macOS; on Linux `top -p`). Find the PID, memory and CPU. Stop it with `kill <PID>`. Read `man kill` (just the first screen) and list the signals shown by `kill -l`.
4. (30m) **SIGTERM vs SIGKILL, with Java.** Predict: which of `kill -TERM` and `kill -9` lets a program clean up? Run `java core/src/main/java/shopflow/w00/ShutdownDemo.java`, note the PID it prints, and from another terminal run `kill -TERM <pid>`. You should see `[hook] SIGTERM received...`. Start it again and run `kill -9 <pid>`: no hook message. Read `ShutdownDemo.java`: find where the hook is registered. Write down why this matters for deploys (the orchestrator sends SIGTERM and waits; if you ignore it, it sends SIGKILL).
5. (30m) **Git mental model.** Read the Missing Semester lecture "Version Control (Git)" (the data model part). Then do the first levels of *Learn Git Branching* (learngitbranching.js.org): "Introduction Sequence" and the "Rebase Introduction" and "Moving work around" levels. Write in your notes, in one line each: commit, branch (a movable pointer), HEAD, `merge`, `rebase`.
6. (30m) **Bisect, by hand.** Run `scripts/w02-bisect-demo.sh setup`. It creates `/tmp/w02-bisect` with 16 commits, one of which breaks `./test.sh`. In that repo run `git bisect start`, `git bisect bad`, `git bisect good $(git rev-list --max-parents=0 HEAD)`, then answer `git bisect good` or `bad` by running `./test.sh; echo $?` at each step (about 4 steps). Then redo it automatically with `git bisect reset` followed by `git bisect run ./test.sh` after marking good and bad. Check the answer with `scripts/w02-bisect-demo.sh reveal`.
7. (20m) **Branching strategies.** Read the introduction on trunkbaseddevelopment.com and any comparison of "git flow vs trunk-based" (for example from Atlassian's Git tutorials). Write 5 lines: how each works, what each costs, and which fits a one-person project with CI.

**Tips and common problems**
- `kill: (12345) - No such process` → the program already exited or you copied the wrong number → use `pgrep -f ShutdownDemo` again.
- The hook message does not appear on Windows → signals work differently there (use Git Bash/WSL for this exercise).
- In `git bisect`, you answer from the state of the checked-out commit, not from the history → always run the test first, then mark.
- `bisect run` prints "first bad commit" → copy the hash; `git bisect reset` returns you to the branch tip.
- `top` options differ between macOS and Linux → `man top` or use `htop` if installed.

**Done when**
- [ ] You can start, find (PID) and stop a process with `ps`/`pgrep` and `kill`.
- [ ] You have seen the shutdown hook fire for SIGTERM and not for SIGKILL.
- [ ] You found the culprit commit with `git bisect run`.
- [ ] Your notes contain the 5-line branching strategy comparison.

---

## S2 Build (3h): meet ShopFlow and set up your board
Today's ticket: **SF-DOC "Know what we are building and organise the work."** No code and no design. You are not asked to write requirements or a glossary yet: you have not learned class or database modeling, so those documents come later (Phase 3), when you can do them well.

1. (20m) **Read the brief.** Read `shopflow-brief.md` (one page). In `notes/week2.md` write 3 sentences in your own words: what ShopFlow is, who uses it, what it will become.
2. (30m) **Skim the backlog.** Read `shopflow-backlog.md` from SF-01 to SF-12. For every word you do not understand (inventory? DTO? Flyway?), add a line to `notes/questions.md`. Do not look anything up now. Most of these questions are answered in weeks 3 to 12 and the modeling block (M1 to M8); tick them off as you meet them.
3. (50m) **Create the first ten issues.** In your GitHub repo create issues for SF-01 to SF-10. Copy the title and the acceptance text from the backlog as written (keep the number in the title). Add the labels `phase-1` or `phase-2`. Do not rewrite or extend the criteria: writing good acceptance criteria is a Phase 3 skill.
4. (30m) **Board and templates.** Create a GitHub Project (Kanban) with columns Backlog, In progress, In review, Done and add the ten issues. Add an issue template `.github/ISSUE_TEMPLATE/ticket.md` with the headings Goal, Acceptance criteria, Out of scope, Notes, and a PR template with Summary, Tests, Notes.
5. (20m) **First naive guess (a baseline).** Pick SF-01 (inventory) and SF-03 (product catalog). On paper, list the *nouns* you think the code will need and draw boxes with lines between them. Do not worry about being right. Keep the sheet: you will compare it with what you can do after the modeling block (week M8).
6. (30m) **Explain it aloud.** Record 2 minutes explaining ShopFlow to a friend who is not technical, without jargon. Listen back once and note where you hesitated.

**Tips and common problems**
- You feel you should write more design now → resist: designing before learning modeling produces documents you will throw away. The baseline sheet is enough.
- Backlog wording looks odd (`DTO`, `Testcontainers`) → write it in `notes/questions.md` and move on; nobody expects you to understand it yet.
- Tickets become long when copied → leave them as in the backlog; an issue can be short.

**Done when**
- [ ] `notes/week2.md` has your 3-sentence summary and `notes/questions.md` has at least 8 unfamiliar terms.
- [ ] Ten GitHub issues exist and are on the board.
- [ ] Issue and PR templates are in the repo.
- [ ] You kept your baseline sheet of nouns and boxes for SF-01 and SF-03.

---

## S3 Incident (2h): the wrong branch and the lost commit (mode B-lite)
**INC-02-01 ticket:** *"I wrote the 10% discount rule and committed it on `main`. It should have been on a branch called `feature/discount`, and `main` must go back to how it was."*
**INC-02-02 ticket:** *"I ran `git reset --hard HEAD~1` to undo something, and my commit 'Add stock reservation' disappeared. Please get it back."*

1. (10m) **PREDICT** (answers first, then open):
   - A) After `git reset --hard HEAD~1`, is the commit gone from the disk immediately? B) What is a branch, physically? C) If you create a branch at the current commit and then move `main` back one commit, what happens to the commit? D) Where could you find the previous position of `HEAD`?
   <details><summary>Answers</summary>

   A) No. The commit object stays in the repository until garbage collection (weeks later by default); only the branch pointer moved. B) A small file containing a commit hash. C) The commit is kept alive by the new branch, so nothing is lost. D) In the reflog: `git reflog`.
   </details>
2. (5m) **Create the broken repos:** `scripts/w02-git-incident.sh setup`. It prints two paths under `/tmp/w02-git-incident/`.
3. (20m) **INVESTIGATE INC-02-01.** `cd /tmp/w02-git-incident/wrong-branch`. Run `git status`, `git log --oneline --graph --all`. Draw the current picture of `main` and `HEAD`. Do NOT fix anything yet. Hint ladder: (1) the commit exists and is only on the wrong branch; (2) look at `git branch`; (3) tools: `git branch <name>` creates a pointer at HEAD, `git reset` moves the current branch pointer; (4) search "git move commit to new branch"; (5) the fix is two commands, and the order matters.
4. (15m) **STUDY** (targeted, now that you saw the problem). *Pro Git* book: ch. 3.1 "Branches in a Nutshell" and the `git reset` explanation in ch. 7.7 "Reset Demystified" (read the `--soft`, `--mixed`, `--hard` summary). Answer: (a) Which of the three modes changes the working directory? (b) Why is it safe to run `git reset --hard HEAD~1` *after* you created a branch that points at the commit?
5. (10m) **FIX and PROVE INC-02-01.** In the repo: `git branch feature/discount`, then `git reset --hard HEAD~1`. Verify with `git log --oneline --all --graph`. Then run `scripts/w02-git-incident.sh check`: INC-02-01 must say PASS.
6. (10m) **INVESTIGATE INC-02-02.** `cd /tmp/w02-git-incident/lost-commit`; `git log --oneline` (the commit is not listed); now `git reflog` and find the line `commit: Add stock reservation`. Note its hash and its `HEAD@{n}` name.
7. (15m) **STUDY.** *Pro Git* ch. 10.7 "Maintenance and Data Recovery" (the reflog section). Answer: (a) How long are reflog entries kept by default? (b) Name one situation where reflog cannot help (hint: changes that were never committed, or work in another clone).
8. (10m) **FIX and PROVE INC-02-02.** Either `git reset --hard HEAD@{1}` or, safer, `git branch rescue <hash>` followed by `git merge rescue`. Run `scripts/w02-git-incident.sh check`: both lines must say PASS. If you want to start again: `scripts/w02-git-incident.sh reset` then `setup`.
9. (15m) **GENERALIZE.** Write two ledger entries (INC-02-01, INC-02-02) and a 6-line "git safety rules" cheat sheet, for example: *before any `reset --hard`, run `git branch backup`; use `git stash` for unfinished work; `git reflog` first when something vanished; never force-push shared branches.*
10. (10m) **Practise the other direction.** In `/tmp/w02-git-incident/wrong-branch` create two small commits on a branch, then use `git rebase -i HEAD~2` to squash them into one with a good message. Then cause a conflict on purpose (edit the same line on two branches) and resolve it. Write down which markers (`<<<<<<<`, `=======`, `>>>>>>>`) mean what.

**Tips and common problems**
- `git reset --hard HEAD~1` prints "HEAD is now at ..." and you panic → that line is the new position; the old commit still exists in the reflog.
- `reflog` shows `HEAD@{1}` but resetting to it also changes files you edited since → commit or stash local changes before resetting.
- `check` says FAIL after you fixed one repo → it checks both; run `git status` in each (the tree must be clean).
- You edited the wrong repo → paths are under `/tmp/w02-git-incident/`; `pwd` before running commands.
- Git opens an editor (Vim) for the rebase → `:wq` to save, or set `git config --global core.editor "code --wait"`.

**Done when**
- [ ] `scripts/w02-git-incident.sh check` prints PASS twice.
- [ ] Two ledger entries are written, each explainable in a minute.
- [ ] You squashed commits with interactive rebase and resolved one conflict.
- [ ] Your git safety cheat sheet is committed.

---

## S4 Consolidate (2h)
1. (30m) **Write ADR-001.** Create `docs/adr/001-branching-and-stack.md` with: *Context* (solo learner, CI later, small team simulated), *Options* (trunk-based with short branches, git flow, long-lived feature branches), *Decision* (your choice, for example trunk-based with short-lived branches and PRs), *Consequences* (what gets easier, what gets harder). Add a second section "Tech stack for now": Java 25 (or 21), Maven, JUnit 6 + AssertJ, Postgres later, and why.
2. (25m) **Flashcards.** 10 cards from: SIGTERM vs SIGKILL, what a branch is, `reset --soft/--mixed/--hard`, reflog, rebase vs merge, bisect idea, what exit code 0 means, what a backlog ticket is, why we keep a board, what ADR stands for.
3. (25m) **Self-check.** Answer aloud, then open the answers.
   1) What is the difference between SIGTERM and SIGKILL, and why does a deployment tool send SIGTERM first? 2) How do you find a commit you lost with `reset --hard`? 3) Why can `git bisect run` find a bug faster than reading the diff? 4) What is an ADR and why write one before you need it? 5) When is rebase a bad idea?
   <details><summary>Answers</summary>

   1) SIGTERM can be handled (the app can finish requests and clean up); SIGKILL cannot be caught. Tools send SIGTERM, wait a grace period, then SIGKILL. 2) `git reflog`, find the hash, `git branch rescue <hash>` or `git reset --hard <hash>`. 3) It does a binary search over history: about log2(N) test runs instead of reading N diffs. 4) An Architecture Decision Record: a short note of context, options, decision and consequences, so that later you (or a teammate) know why a choice was made and can revisit it deliberately. 5) On commits that others already pulled (it rewrites history); on shared branches.
   </details>
4. (20m) **Ledger and commit.** Commit all docs, scripts and the ledger. Open a PR titled "Week 2: define ShopFlow", review your diff, merge.
5. (10m) **Retro** in `retro.md`: what worked, what blocked me, what I change. Note which backlog terms you most want answered.
6. (10m) **Preview Week 3.** Open `core/src/main/java/shopflow/w03/WorkedExample.java` and read it once. Next week you start writing Java.

**Output:** `notes/week2.md`, `notes/questions.md`, baseline sketch, ten GitHub issues on a board, issue and PR templates, `docs/adr/001-...md`, two ledger entries, git safety cheat sheet, 10 flashcards.

**If stuck:** for Git, `git status` and `git log --oneline --graph --all` answer most questions; for the shell, `man <command>` or `<command> --help`; otherwise search the first line of the error message.
