# Learner Guide

Read this first. It is a one-page version of how to take the course. Details live in `lessons/README.md`.

## 1. What you are doing
Over about 60 weeks at about 10 hours a week, you go from frontend developer to senior Java backend engineer. You learn by making failing tests pass in `shopflow-lab`, and by building one project, ShopFlow, step by step. Reading supports the code. It does not replace it.

## 2. Before day 1 (30 minutes)
- [ ] A laptop with admin rights, a GitHub account and 10 GB free disk.
- [ ] Block four fixed slots in your calendar each week (3h, 3h, 2h, 2h). Fixed slots matter more than long ones.
- [ ] Create `~/shopflow-notes/` with `ledger.md` (copy `reality-lab/LEDGER.md`) and `retro.md`.
- [ ] Fork or clone this repo, and keep your work on your own branches.
- [ ] Install the JDK, IntelliJ IDEA Community and Docker Desktop when Week 1 S2 tells you to.
- [ ] Find a mentor or study buddy for the monthly review, if you can.

## 3. Your first hour
1. Read `lessons/README.md` (the session formats and the stuck routine).
2. Open `lessons/pilot/week-01.md` and start S1.
3. When the JDK is installed, run `shopflow-lab/scripts/run-week.sh w00`. It should be red. Fix `Hello.java` and run it again. That red-to-green loop is the whole course.

## 4. Every week
| Session | Time | You do |
|---|---|---|
| S1 Try it | 3h | Run the code, change one thing, predict the result, then break it on purpose. |
| S2 Build | 3h | Work on ShopFlow tickets. Use one branch and one PR per ticket. |
| S3 Incident | 2h | Predict, reproduce with a red test, investigate with a tool, fix, then prove it. |
| S4 Consolidate | 2h | Do flashcards, write the ledger entry, answer the self-check aloud, and write a 3-line retro. |

Run a week's tests with `scripts/run-week.sh wNN` (for Spring weeks: `scripts/run-week.sh w10 api`).

## 5. Do
- Follow the steps in order. Treat time boxes as budgets, not targets.
- Predict the result before you run anything.
- Type and run every step yourself. Don't copy-paste.
- Keep the failing test. It is proof the problem exists.
- Commit after every green group, in small commits.
- Write the root cause in one sentence before you fix anything.
- Write ledger entries in your own words, and turn them into flashcards.
- Check commands, versions and links against current docs. Tools change.

## 6. Don't
- Don't edit tests to make them pass, unless a lesson tells you to write your own.
- Don't read for more than 30 minutes without touching code.
- Don't open `solutions/wNN/` before your tests are green.
- Don't ask an AI to write the solution before you've tried. Ask it for hypotheses, reviews and quizzes.
- Don't leave cloud resources running. Destroy them and set a budget alert.
- Don't skip S4. It is where learning becomes memory.

## 7. When you are stuck
1. **Spend 5 minutes** re-reading the error and searching its first line.
2. **Spend 5 minutes** shrinking the problem to a tiny repro.
3. **Spend 5 minutes** asking an AI: "Goal, what I tried, exact error, expected result. Give hypotheses and how to test them. Don't write the full solution."
4. **Still stuck?** Open the next hint level (`hints/wNN/<id>.md`, one level at a time). Log the problem as "open" in the ledger and move on.

## 8. Am I done with the week?
- Every "Done when" box is ticked. If not, carry the remainder into next week's S4.
- You can answer the self-check questions aloud without notes. If you get fewer than 3 of 4, repeat the missed S1 reading.
- There is a concrete artifact in git (code, test, ADR or ledger entry).
- The `retro.md` has three lines: what worked, what blocked me, what I'll change. If the same blocker appears twice, fix its cause first.

## 9. Monthly and per phase
- Once a month, record a one-minute spoken explanation of a topic, and listen back.
- Phase gates need an outside reviewer. Until you have one, record yourself and compare against the rubric in `java-backend-roadmap.md`.
- If you fall behind, drop S3 extras first, never S4. Slipping two weeks is normal. Stopping is the only failure.

## 10. Where things are
| Need | File |
|---|---|
| Big picture, phases, gates | `java-backend-roadmap.md` |
| Weekly lessons | `lessons/pilot/week-NN.md` (1–12), `lessons/phase-*.md` (13–60) |
| Tickets | `shopflow-backlog.md` |
| Production-style problems | `reality-lab/CATALOG.md` |
| Practice code, hints and solutions | `shopflow-lab/` |
