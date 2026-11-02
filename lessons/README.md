# Lesson Plans: How to Use

Companion to `../java-backend-roadmap.md`. One file per phase, with a plan for every session of every week.

| File | Weeks |
|---|---|
| `phase-00-01-bridge-and-java.md` | 1–9 |
| `phase-02-spring-boot-slice.md` | 10–15 |
| `phase-03-design.md` | 16–20 |
| `phase-04-databases.md` | 21–27 |
| `phase-05-jvm-concurrency.md` | 28–32 |
| `phase-06-security-apis-messaging.md` | 33–38 |
| `phase-07-distributed-architecture.md` | 39–46 |
| `phase-08-cloud-native.md` | 47–55 |
| `phase-09-real-world.md` | 56–60 |

## The four sessions of a week (10 h)
| Session | Time | Purpose | Rule |
|---|---|---|---|
| **S1 Learn** | 3 h | Concepts from reading, docs and one talk/video | Read actively: after each section, close the source and write 3 bullet points from memory |
| **S2 Build** | 3 h | Apply it to ShopFlow tickets (`../shopflow-backlog.md`) | One branch per ticket, one PR per ticket, tests included |
| **S3 Lab** | 2 h | Reproduce Reality Lab items (`../reality-lab/CATALOG.md`) | Failing test first, then observe with the named tool, then fix |
| **S4 Review + Write** | 2 h | Flashcards, refactor, ledger entry, ADR or note | Say each ledger answer aloud in under a minute |

Items marked **extra** are additional exercises that are not in the catalog.

## Each week also ends with
- **Self-check questions.** If she can't answer one without notes, repeat that part next week's S4.
- **Output.** A concrete artifact in git (code, test, ADR, ledger entry).

## Version policy (check before starting; versions move)
- **JDK:** 25 LTS is current; 21 LTS is still widely deployed. Use 25 for new work and expect 17/21 in employers' codebases. Features introduced after 17 are flagged where they matter.
- **Spring:** Spring Boot 4.x / Spring Framework 7 is current. Many production systems run Boot 3.x, and some run 2.x. Learn on the newest, and read the migration guides (Phase 9 covers upgrades). When a tutorial shows older annotations or packages, check the release notes before assuming it's wrong.
- **PostgreSQL:** use a currently supported major version (17 or later).
- **Links:** references are given by stable site or title rather than deep URLs. Search the title if a page moved.

## Core library (buy or borrow; chapters are cited in the weekly plans)
| Short name | Full title |
|---|---|
| **EJ** | *Effective Java*, 3rd ed., Joshua Bloch |
| **JCIP** | *Java Concurrency in Practice*, Goetz et al. |
| **DDIA** | *Designing Data-Intensive Applications*, Martin Kleppmann (chapter numbers follow the 1st edition; the 2nd edition reorganizes them, so match by title) |
| **CA** | *Clean Architecture*, Robert C. Martin |
| **Refactoring** | *Refactoring*, 2nd ed., Martin Fowler |
| **HFDP** | *Head First Design Patterns*, 2nd ed. |
| **BM** | *Building Microservices*, 2nd ed., Sam Newman |
| **RI** | *Release It!*, 2nd ed., Michael Nygard |
| **SRE** | *Site Reliability Engineering* (free at sre.google/books), plus the *SRE Workbook* |
| **DDD-D** | *Domain-Driven Design Distilled*, Vaughn Vernon |
| **HPBN** | *High Performance Browser Networking*, Ilya Grigorik (free online) |
| **UTI** | *Use The Index, Luke* (free online, use-the-index-luke.com) |

## Free sites and courses used repeatedly
- **dev.java** and the official Java Tutorials (Oracle): language and API learning
- **Spring reference docs** (docs.spring.io): Framework, Boot, Data, Security. Treat these as the primary source.
- **Spring Guides** (spring.io/guides) and **Spring Academy** (spring.academy)
- **Baeldung**: practical how-to articles. Cross-check against official docs.
- **Vlad Mihalcea's blog** and **Thorben Janssen's blog**: JPA/Hibernate internals
- **PostgreSQL documentation** (postgresql.org/docs): chapters "Concurrency Control", "Performance Tips", "Backup and Restore"
- **MIT Missing Semester** (missing.csail.mit.edu): shell, Git, debugging, profiling
- **MIT 6.5840 (formerly 6.824) Distributed Systems**: lectures on YouTube
- **CMU 15-445 Database Systems** (Andy Pavlo): lectures on YouTube
- **Martin Fowler's site** (martinfowler.com): patterns and architecture articles
- **microservices.io** (Chris Richardson): saga, outbox and related patterns
- **OWASP** Top 10 and Cheat Sheet Series
- **Brendan Gregg's site**: USE method, performance analysis
- **Aleksey Shipilev's site**: JVM internals ("JVM Anatomy Quarks", "Java Memory Model Pragmatics")
- **Julia Evans (jvns.ca)**: approachable zines on networking, Linux, debugging
- **AlgoMaster roadmaps** (algomaster.io/roadmaps): topic checklists for Java, Spring, SQL, DevOps, Kubernetes

## Project-first learning (pilot: weeks 1–12 in `pilot/`)
The pilot replaces "read first, practise later" with a feedback loop in the `shopflow-lab` repo. Every concept uses one of three modes:

| Mode | Use when | Loop |
|---|---|---|
| **A. Build-along** | New concept, she can't yet predict what code does | Run → read → modify one thing → predict → break it on purpose. Tiny exercises with tests that go green one by one. |
| **B. Break-it (Incident)** | She knows normal behaviour, not failure modes | Ticket → **Predict** → Reproduce (red test) → Investigate (tool) → Study (just-in-time reading) → Fix → Prove (green + regression) → Generalize (ledger) |
| **C. Design-it** | Judgment topics with several valid answers | Build the naive version, pressure-test it (load, failures), redesign, record an ADR |

Mode choice rule: show a small snippet and ask three prediction questions. Two or more correct → mode B; otherwise mode A first.

In the pilot each week has four sessions: **S1 Try it** (mode A), **S2 Build** (tickets with acceptance tests already in the repo), **S3 Incident** (mode B), **S4 Consolidate**. Run a week's tests with `shopflow-lab/scripts/run-week.sh wNN [module]`. Red tests are the starting point, not an error. Hints come in five levels (`hints/wNN/`), opened one at a time. Solutions (`solutions/wNN/`) are for comparison after the tests are green.

Authoring rules for these files: `pilot/AUTHORING.md`. The phase files for weeks 13 onward still use the older reading-first format until the pilot has been trialled.

## Self-study playbook

### The session format (used in every phase file)
Each session is written as numbered, time-boxed steps so nothing is left to guess:

```
#### S2 Build (3h): <focus>
1. (15m) <concrete action with the exact command or file to create>
2. (60m) <next action>
...
**Tips and common problems**
- <symptom you will probably hit> → <why> → <what to do>
**Done when**
- [ ] <observable check: test is green, command prints X, file exists>
```

Rules for every session:
- **Do the steps in order.** The order is the lesson.
- **Time boxes are budgets, not targets.** If a step goes over by 2x, stop and use the "Stuck?" routine below.
- **"Done when" is the only definition of finished.** If a box can't be ticked, the session isn't done; carry the remainder into the next S4.
- **Commands are starting points.** Tool versions change; when a command fails, read the error and the tool's current docs before assuming you made a mistake.

### Stuck? The 15-minute routine
1. (5m) Re-read the error message slowly and copy its **first** line into a search.
2. (5m) Shrink the problem: a 20-line project, one test, one table. If the small version works, bisect toward the big one.
3. (5m) Ask an AI agent, but in this form: *"Here is my goal, what I tried, the exact error, and what I expect. Give me hypotheses and how to test each. Do not write the full solution."*
4. Still stuck after 15 minutes: write the question in the ledger as "open", skip to the next step, and ask a mentor or peer at the monthly review.

### Using an AI agent without losing the learning
- **Allowed:** explain a concept, give hypotheses, review your code, generate test data, explain a stack trace, quiz you.
- **Not allowed before you try:** writing the solution of a Build or Lab step. Attempt first, then ask for a review.
- **Always verify:** run the code, read the docs link, and write what you learned in your own words in the ledger.

### Self-checking without a teacher
- Every week ends with **self-check questions and answers hidden in a `<details>` block**. Answer out loud before opening it.
- If you got fewer than 3 of 4 right, redo the S1 reading you missed, then retry the questions in the next S4.
- Phase gates need an outside reviewer. Until you have one, record yourself explaining the gate tasks and review the recording against the rubric.

### Weekly retro (10 minutes, end of S4)
Write three lines in `retro.md`: **what worked**, **what blocked me (and for how long)**, **what I'll change next week**. If the same blocker appears twice, fix the cause (tool, habit, gap in knowledge) before moving on.

### Common beginner traps (applies everywhere)
- Copy-pasting from tutorials without typing and running each step.
- Reading for 3 hours with no code. Follow the 3-and-3 rule: no more than 30 minutes reading before touching code.
- Skipping the failing test in labs. The red test is the proof the problem exists.
- Fixing symptoms. Always write the root cause in one sentence before fixing.
- Leaving environments running. Cloud labs: always destroy, and keep a budget alert.

## Study technique
1. **Retrieval over rereading.** Flashcards (Anki) for every ledger entry and every self-check question.
2. **Build the smallest repro.** If an idea can't be shown in 30 lines, it's not understood yet.
3. **Explain aloud.** One-minute answers, recorded once a month and listened back to.
4. **Never skip the failing test.** Writing the failure is the learning.
