# Week 2: Shell, signals, Git rescue and defining ShopFlow

**Mode:** A (build-along), with two Git incidents (B-lite)  **Release:** v0.1 (project definition)
**Outcome:** you are comfortable in a terminal, you know why a process stops politely or abruptly, you can recover "lost" Git work, and ShopFlow has requirements, a glossary, ten tickets and its first decision record (ADR-001).

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

## S2 Build (3h): define ShopFlow (requirements, glossary, tickets)
Today's ticket: **SF-DOC "Define what we are building."** No code. Writing things down now saves arguments later, and a vague ticket is the most common cause of wasted work.

1. (20m) **Read about shared language.** *Domain-Driven Design Distilled* (DDD-D), ch. 1–2, or any short article on "ubiquitous language". Write one sentence: why must "order" mean exactly one thing in the code, the database and the conversations?
2. (40m) **Requirements.** Create `docs/requirements.md` with these sections: *Vision* (2 sentences), *Actors* (Customer, Admin, Warehouse operator), *Use cases* (browse catalog, manage cart, place order, pay, track order, manage stock), *Non-goals* (for now: returns, marketplace sellers, multi-warehouse), *Quality goals* (be concrete: "checkout answers in under 500 ms at the 95th percentile", "no overselling", "no lost orders"). Under every use case write 2–3 lines of the happy path and 1–2 failure cases (payment declined, item out of stock).
3. (40m) **Glossary.** Create `docs/glossary.md` with a table: Term, Meaning, Example, Not to be confused with. Include: Product, SKU, Category, Cart, Cart line, Order, Order line, Payment, Reservation (stock held for an order), Inventory, Customer. Rule: one term, one meaning. If you wrote "item" anywhere, decide whether it means a Product, a Cart line or an Order line and fix it.
4. (50m) **First ten tickets.** Open `shopflow-backlog.md` and create GitHub Issues for SF-01 to SF-10 (keep the numbers in the title). For each, write acceptance criteria as bullets in the form *Given … When … Then …*. Example for SF-01: *Given an empty inventory, when I add 5 of ABC-0001, then stock of ABC-0001 is 5.* Add labels `phase-1` or `phase-2`. Keep each ticket small enough to finish in one session.
5. (20m) **Board and templates.** Create a GitHub Project (Kanban) with columns Backlog, In progress, In review, Done. Add the ten issues. Add an issue template `.github/ISSUE_TEMPLATE/ticket.md` with: Goal, Acceptance criteria, Out of scope, Notes.
6. (10m) **Ambiguity pass.** Search your documents for words like *fast, easy, nice, soon, appropriate, etc., and so on, flexible*. Replace each with something testable or delete it.

**Tips and common problems**
- You cannot decide what a term means → write both meanings down, pick the one you need for ShopFlow, and note the other under "Not to be confused with".
- Tickets become huge → split along user-visible behaviour, not along layers ("add stock" and "remove stock", not "do the database part").
- Acceptance criteria describe implementation ("use a HashMap") → rewrite as observable behaviour.
- You feel it's "just documentation" → a good test is: could someone else implement SF-01 from the ticket alone?

**Done when**
- [ ] `docs/requirements.md` and `docs/glossary.md` are committed and contain no vague words from step 6.
- [ ] Ten GitHub issues exist with Given/When/Then criteria and are on the board.
- [ ] Issue and PR templates are in the repo.

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
2. (25m) **Flashcards.** 10 cards from: SIGTERM vs SIGKILL, what a branch is, `reset --soft/--mixed/--hard`, reflog, rebase vs merge, bisect idea, what exit code 0 means, why tickets need testable criteria, "one term one meaning", what ADR stands for.
3. (25m) **Self-check.** Answer aloud, then open the answers.
   1) What is the difference between SIGTERM and SIGKILL, and why does a deployment tool send SIGTERM first? 2) How do you find a commit you lost with `reset --hard`? 3) Why can `git bisect run` find a bug faster than reading the diff? 4) Why is "the checkout should be fast" a bad acceptance criterion? 5) When is rebase a bad idea?
   <details><summary>Answers</summary>

   1) SIGTERM can be handled (the app can finish requests and clean up); SIGKILL cannot be caught. Tools send SIGTERM, wait a grace period, then SIGKILL. 2) `git reflog`, find the hash, `git branch rescue <hash>` or `git reset --hard <hash>`. 3) It does a binary search over history: about log2(N) test runs instead of reading N diffs. 4) It cannot be tested; replace it with "p95 under 500 ms at 50 requests per second". 5) On commits that others already pulled (it rewrites history); on shared branches.
   </details>
4. (20m) **Ledger and commit.** Commit all docs, scripts and the ledger. Open a PR titled "Week 2: define ShopFlow", review your diff, merge.
5. (10m) **Retro** in `retro.md`: what worked, what blocked me, what I change. Note whether any ticket took longer to write than expected, and why.
6. (10m) **Preview Week 3.** Open `core/src/main/java/shopflow/w03/WorkedExample.java` and read it once. Next week you start writing Java.

**Output:** `docs/requirements.md`, `docs/glossary.md`, ten GitHub issues on a board, issue and PR templates, `docs/adr/001-...md`, two ledger entries, git safety cheat sheet, 10 flashcards.

**If stuck:** for Git, `git status` and `git log --oneline --graph --all` answer most questions; for the shell, `man <command>` or `<command> --help`; otherwise search the first line of the error message.
