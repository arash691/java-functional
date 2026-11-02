# Phase 9: Real-World Engineering and Senior Behaviours (Weeks 56–60)

Goal: handle messy code, collaborate like a senior, show evidence, and pass a game-day.

Conventions: steps are time-boxed and sum to the session length (3h/3h/2h/2h). Commands marked *(verify against current docs)* depend on versions. Keep `phase9-notes.md` for "write from memory" steps. This phase needs **a peer or mentor** for weeks 58–60; line them up now (see "If no peer is available" in each week).

---

## Week 56 — Legacy code and upgrades
**Outcome:** can safely change code she did not write and upgrade a framework version.
**Before you start:** JDK 17 and your current JDK installed side by side (SDKMAN `sdk use java`); Git; the older Spring Boot 2.x sample (an old Spring PetClinic release or an internal sample) cloned into `legacy-sample/`.

### S1 Learn (3h): seams, characterization tests, upgrade strategy
1. (60m) Read Feathers, *Working Effectively with Legacy Code*: the chapters on seams and on characterization tests. Write definitions of: seam, enabling point, characterization test, sprout method, wrap method. Add one example of each from your own ShopFlow code.
2. (45m) Read the Spring Boot migration guides (GitHub wiki) for the jump from 2.7 → 3.0 and 3.x → 4.0. Make a table: area (Jakarta namespace, config property renames, removed APIs, Hibernate, Jackson, Security), breaking change, fix.
3. (30m) Read OpenRewrite docs: what a recipe is, the Maven plugin, the Spring Boot upgrade recipes, and how to dry-run (`rewrite:dryRun`) *(verify against current docs)*.
4. (30m) List the typical breaks in your notes: `javax.*` → `jakarta.*`, removed/renamed `application.properties` keys, Spring Security `WebSecurityConfigurerAdapter` removal, deprecated Hibernate APIs, Jackson major version changes, dependency version alignment.
5. (15m) Write your upgrade plan: baseline green build → characterization tests → upgrade JDK → upgrade Boot one major step at a time → rebuild → run tests → record each break.

**Tips and common problems**
- Reading Feathers feels slow → read only seams and characterization chapters now; the rest is a reference.
- Migration guides are long → search the page for the starter/library names in your `pom.xml` only.

**Done when**
- [ ] You have the breaks table and an upgrade plan with steps in order.
- [ ] You can explain why characterization tests describe current behaviour even if it's wrong.

### S2 Build (3h): SF-43 legacy upgrade
1. (20m) `cd legacy-sample && ./mvnw -q verify` (or `mvn`). If it doesn't build, fix only what is needed (JDK version, missing repo). Commit "baseline green" on a branch `upgrade/boot-3`.
2. (45m) Characterization tests: pick 3 behaviours at the boundaries (HTTP endpoints via MockMvc, a repository query, a service method). Write tests that assert what the code does today. Run; commit.
3. (20m) Dry-run OpenRewrite:
```
./mvnw -U org.openrewrite.maven:rewrite-maven-plugin:dryRun \
  -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:RELEASE \
  -Drewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0
```
   *(verify recipe names and coordinates against current docs)*. Read `target/rewrite/rewrite.patch`; then run `rewrite:run`.
4. (60m) Fix the remaining breaks one at a time: compile errors first, then test failures, then startup errors. After each fix, run tests and commit with a message like `fix: replace WebSecurityConfigurerAdapter with SecurityFilterChain bean`. Record each break in `docs/upgrade-log.md`: symptom, cause, fix.
5. (25m) Continue to the next major (Boot 4) following the guide; accept that it may take time. Record what is new.
6. (10m) Run the full test suite and start the app. Compare behaviour with the baseline using the characterization tests.

**Tips and common problems**
- Hundreds of compile errors after the Jakarta change → imports changed → let OpenRewrite do it, then fix leftovers.
- App starts but behaves differently → a default changed (for example trailing slash matching, property binding) → rely on characterization tests and the migration guide's "behavior changes" sections.
- `ClassNotFoundException` for a third-party library → incompatible version → check the library's compatibility matrix before bumping.

**Done when**
- [ ] The upgraded app builds, characterization tests pass, and it starts.
- [ ] `docs/upgrade-log.md` lists each break with cause and fix.

### S3 Lab (2h): RL-108 characterization tests, RL-109 Boot 2 → 3 failure log
1. (10m) Pick one untested legacy class with branching logic (a service or a utility with at least 5 paths).
2. (30m) **RL-108 golden master:** write a test that calls the class with a wide set of inputs (generate 50–200 combinations) and writes outputs to an approval file; commit the file as the baseline. Re-run: the test should compare against the file.
3. (25m) Refactor the class safely: extract a method or rename, run tests after each tiny change. If the output file changes, you changed behaviour; revert or decide consciously.
4. (10m) **Sanity check:** deliberately introduce a bug (change a `>` to `>=`). **Observe:** the golden-master test fails. Revert. This proves the tests can catch change.
5. (35m) **RL-109:** with the sample on Boot 2.x, switch the parent version to 3.x without any other changes. Run `./mvnw -q compile`; copy the first 30 errors into `docs/upgrade-failures.md`; categorize them (namespace, removed API, dependency, config). Then fix by category and record time spent per category.
6. (10m) Ledger entries RL-108 and RL-109.

**Tips and common problems**
- Golden file differs on every run → output includes timestamps/random → inject a fixed `Clock` and seed.
- Tests pass but you can't trust them → test the tests (step 4).

**Done when**
- [ ] A bug introduced on purpose fails the characterization test.
- [ ] You have a categorized list of upgrade failures with a fix for each category.

### S4 Review + Write (2h)
1. (45m) Write `docs/upgrade-playbook.md`: pre-checks, branch strategy, characterization tests first, recipe dry-run, order of fixes, rollback plan, how to communicate to the team.
2. (25m) Ledger answers for RL-108 and RL-109 aloud.
3. (15m) Flashcards: seam types, sprout vs wrap, golden master, namespace rename.
4. (20m) Self-check out loud, then check answers.
5. (15m) Retro; merge SF-43 notes.

**Self-check:** What is a seam? Why write tests that capture current behaviour even if it's wrong? Why upgrade in steps rather than jumping versions?
<details><summary>Answers</summary>

- A place where you can change behaviour without editing the code at that place (for example by substituting a dependency or overriding a method), which lets you get code under test.
- Because you need a safety net to prove a refactor or upgrade did not change behaviour; fixing "wrong" behaviour is a separate, deliberate step.
- Each major version has a migration guide and deprecations that are removed next; stepping isolates breaks and keeps each diff understandable.
</details>

**Output:** SF-43 upgraded project, playbook, RL-108/109 entries.
**If stuck:** compile errors first; fix the first error in a file, rebuild, and don't try to fix all 200 at once.

---

## Week 57 — Reading open source and contributing
**Outcome:** can navigate a large codebase and has one real contribution.
**Before you start:** a GitHub account with SSH keys; IDE can download sources; a tiny Spring Boot app with one `@Transactional` service method.

### S1 Learn (3h): how to read a big codebase
1. (25m) Read opensource.guide "How to contribute to open source". Write the 5 steps in your notes.
2. (20m) Write the reading method on a card: start from an entry point, set a breakpoint, step in with the debugger, read the call stack, take notes, draw as you go.
3. (45m) Trace a request through Spring MVC: set breakpoints at `DispatcherServlet#doDispatch`, then step through handler mapping, `HandlerAdapter#handle`, argument resolvers, return value handlers. Write the sequence as a list.
4. (45m) Trace a transaction: breakpoint in `TransactionInterceptor#invoke`, step into `TransactionAspectSupport#invokeWithinTransaction`. Note where the transaction manager starts and commits.
5. (30m) Trace a connection: breakpoint in `HikariPool#getConnection`; follow `ConcurrentBag`. Note what happens when the pool is exhausted (timeout).
6. (15m) Skim the Kafka consumer `poll` loop (`KafkaConsumer#poll`), noting where heartbeats and rebalances are triggered.

**Tips and common problems**
- IDE shows decompiled code → "Download sources" for the dependency.
- Debugger stops in many unrelated frames → use conditional breakpoints or set a class filter in the step settings.
- You get lost in 30 files → follow one value (for example the HTTP request object) and ignore the rest.

**Done when**
- [ ] You can list the main steps for MVC, transactions and Hikari acquisition from memory.

### S2 Build (3h): SF-44 first contribution
1. (30m) **Choose a project and issue** with these criteria: (a) a library ShopFlow actually uses; (b) recent activity (commits or merged PRs in the last month); (c) has CONTRIBUTING and a code of conduct; (d) issue labelled `good first issue`, `help wanted` or documentation; (e) small scope you can finish in a session; (f) not claimed by someone else. If nothing matches, fix a typo or unclear doc in the library you read this week. Write the issue link in `phase9-notes.md`.
2. (20m) Read CONTRIBUTING fully: branch naming, commit message format, tests, DCO or CLA requirements, formatting. Comment on the issue to say you're working on it.
3. (30m) Fork, clone, build and run the tests locally (`./mvnw verify`, `./gradlew test`). If the build fails on a clean checkout, read the README for prerequisites.
4. (60m) Reproduce the problem with a failing test, then fix it. Keep the change minimal and match the surrounding style.
5. (25m) Format code with the project's tools; commit with the required format; sign off if needed (`git commit -s`).
6. (15m) Open the PR using the template; link the issue; describe what, why, and how you tested. Respond politely to review comments within a day.

**Tips and common problems**
- PR rejected or silent → maintainers busy or scope wrong → be patient, ping once after a week, and treat feedback as free review.
- Build requires a huge setup → pick a smaller project or a docs-only contribution.
- You can't reproduce the issue → ask a clarifying question on the issue instead of guessing.

**Done when**
- [ ] A PR is open (merged is better) with a failing-then-passing test or a docs fix.

### S3 Lab (2h): sequence diagram from source and debugger
1. (10m) Prepare the tiny app with a `@Transactional` service method and an unrelated method that calls it from the same class.
2. (30m) Set breakpoints in `TransactionInterceptor#invoke`, `TransactionAspectSupport#invokeWithinTransaction`, `createTransactionIfNecessary`, `JpaTransactionManager#doBegin`, and `commitTransactionAfterReturning`. Call the method through a controller.
3. (30m) At each stop, copy the top 6 stack frames into notes. Note the proxy class name (`$$SpringCGLIB$$`) in the stack.
4. (30m) Draw the sequence diagram (Mermaid): Controller → Proxy → TransactionInterceptor → TransactionManager → Target → commit.
5. (20m) Now call the method via self-invocation. **Observe:** the interceptor breakpoint never hits; explain in 3 sentences. This connects to RL-26.

**Tips and common problems**
- Breakpoint doesn't trigger → class not on the path or AOP not applied → check bean type with a debugger watch `this.getClass()`.
- Too many frames → use "Hide frames from libraries" except Spring ones.

**Done when**
- [ ] Diagram matches the debugger stack and you have shown the self-invocation case.

### S4 Review + Write (2h)
1. (20m) Outline the blog post "What `@Transactional` really does, from the source": problem, call path, proxy, pitfalls, how to verify.
2. (60m) Draft it using your diagram and stack traces. Include a minimal reproduction.
3. (20m) Edit: cut anything you can't prove from the debugger; add code blocks.
4. (10m) Publish (or save as a draft in the repo) and share with a peer.
5. (10m) Retro.

**Self-check:** Where does a Spring MVC request enter the framework? How do you find the code path for a behaviour you don't understand?
<details><summary>Answers</summary>

- The servlet container calls `DispatcherServlet` (`doDispatch`), which finds a handler via `HandlerMapping`, invokes it via a `HandlerAdapter`, then handles the return value or exception.
- Set a breakpoint at the closest entry point, step into with a debugger, read the call stack, and follow one object; read tests in the library as executable documentation.
</details>

**Output:** SF-44 PR link, sequence diagram, blog post.
**If stuck:** ask in the issue thread with a concrete question and your reproduction; maintainers respond better to specifics.

---

## Week 58 — Senior behaviours
**Outcome:** communicates, reviews, estimates and decides like a senior.
**Before you start:** access to three PRs to review (from a peer, an open-source project, or your old ShopFlow PRs). **If no peer is available:** review three merged PRs in an open-source project you use, commenting in a local file.

### S1 Learn (3h): review, design docs, estimation, influence
1. (40m) Read Google Engineering Practices: "How to do a code review" (what to look for, speed, comments).
2. (30m) Read "The CL author's guide": small changes, descriptions, handling reviewer comments.
3. (35m) Read Malte Ubl, "Design Docs at Google". Note the sections and the purpose of non-goals.
4. (35m) Read selected chapters of *The Staff Engineer's Path* on communication, working across teams and technical decisions.
5. (25m) Read selected topics from *The Pragmatic Programmer* (for example estimating, good-enough software, tracer bullets).
6. (15m) Condense into a one-page "how I work" checklist: review rules, estimate in ranges, write down decisions, say what you don't know.

**Tips and common problems**
- Soft-skills reading feels vague → turn each idea into a concrete rule you could check in a PR or meeting.

**Done when**
- [ ] The one-page checklist exists and includes a comment taxonomy (`nit:`, `question:`, `suggestion:`, `blocking:`).

### S2 Build (3h): three reviews and one RFC
1. (15m) Choose three PRs of different sizes and risk (small bug fix, feature, refactor).
2. (25m) Review PR 1. Order: read description → tests first → design → code → naming. Write at least one praise comment, one question, one suggestion, and mark blockers clearly.
3. (25m) Review PR 2 the same way; look for missing tests, edge cases, security, transactions, error handling.
4. (25m) Review PR 3; add a summary comment: overall impression, top two issues, and what's good.
5. (50m) Write an RFC for a real ShopFlow change (for example "Introduce a read model for order history"): context, goals and non-goals, current state, options (at least three), trade-offs, recommendation, risks, rollout and rollback, open questions.
6. (25m) Revise the RFC with the strongest counter-argument you can think of; add an "alternatives considered" section.
7. (15m) Share it with a peer and ask for the three hardest questions.

**Tips and common problems**
- Comments feel harsh → rewrite as questions or suggestions with the reason ("Consider X because Y").
- Reviews take hours → time-box to 25 minutes, focus on correctness, tests, and design before style (let linters handle style).
- RFC has only one option → you decided before thinking → force two alternatives, including "do nothing".

**Done when**
- [ ] Three PRs have specific, actionable comments with clear blocking vs non-blocking labels.
- [ ] The RFC has options, trade-offs, a recommendation and a rollback plan.

### S3 Lab (2h): RL-110 review an AI-generated PR
1. (15m) Ask an AI agent to implement a ShopFlow feature (for example "add a bulk-discount endpoint with tests"). Commit its output unchanged to branch `lab/ai-pr` and open a draft PR.
2. (45m) Review using this checklist and note every finding: (a) does it compile and run? (b) hallucinated or deprecated APIs? (c) are tests meaningful (assertions, edge cases) or just coverage? (d) N+1 queries or missing indexes? (e) authorization check present? (f) input validation and error handling? (g) transaction boundaries? (h) swallowed exceptions or generic catch? (i) dependency versions and licences? (j) consistency with ADRs and project conventions? (k) logging of secrets/PII?
3. (25m) Run the tests, coverage, mutation tests and the security scan. Record the results.
4. (20m) Write review comments in the PR as you would for a human colleague. Find at least five real issues.
5. (15m) Turn your checklist into `docs/ai-pr-review-checklist.md`; add the ledger entry RL-110.

**Tips and common problems**
- Generated code looks plausible → trust nothing; run it and read each line that touches data or security.
- Can't find five issues → look at tests (are they just asserting `true`?), authorization, error paths and concurrency.

**Done when**
- [ ] At least five distinct issues found and written as review comments.
- [ ] Checklist saved in the repo.

### S4 Review + Write (2h)
1. (20m) Gather facts for a tech-debt case: incident count, hours lost, risk of the debt, estimated cost to fix, estimated saving.
2. (30m) Write a one-page proposal: problem in business terms, options, recommendation, cost/benefit, risks, ask.
3. (30m) Role-play with a peer as the product manager (or record 5 minutes of yourself): present, handle "why now?" and "can it wait?".
4. (20m) Review the recording against: clear ask, evidence, trade-offs, calm tone, no jargon.
5. (10m) Self-check out loud, then check answers.
6. (10m) Retro.

**Self-check:** What is the difference between blocking and non-blocking review comments? How do you estimate something unknown? When do you escalate?
<details><summary>Answers</summary>

- Blocking comments must be addressed before merge (bugs, security, design flaws); non-blocking ones (nits, suggestions) are optional or follow-up and are labelled so the author can prioritise.
- Give a range with assumptions, break the work into smaller known pieces, do a spike for the biggest unknown, and update the estimate as you learn.
- When you are blocked beyond your control, a risk threatens a commitment or safety, or a decision needs authority; escalate early with facts, options and a proposed decision.
</details>

**Output:** review comments, RFC, AI-review checklist, RL-110 entry.
**If no peer is available:** record yourself and use an AI agent as the "product manager" with an instruction to be sceptical; still review against the rubric.
**If stuck:** use the template headings and fill them in; structure unblocks writing.

---

## Week 59 — Interview readiness
**Outcome:** can pass a senior Java backend interview loop.
**Before you start:** schedule three mock interviewers for S3 now (peer, mentor, or fall back to a recorded self-mock with an AI interviewer using the prompt in S3).

### S1 Learn (3h): turn knowledge into answers
1. (45m) Export `LEDGER.md` to a Q&A deck (Anki or plain Markdown): question = symptom ("`@Transactional` doesn't roll back, why?"), answer = your one-minute answer.
2. (45m) Write crisp answers (5–8 lines each) for 15 core questions: `HashMap` internals and the `equals/hashCode` contract; Java Memory Model and `volatile`; `@Transactional` proxy behaviour; N+1 and fixes; isolation levels and anomalies; index usage and `EXPLAIN`; deadlocks; GC basics; thread pool sizing; virtual threads; idempotency; outbox; saga; circuit breaker; Kubernetes probes.
3. (30m) Review AlgoMaster Java, Spring and SQL roadmaps' interview-prep sections and note any theme you have not covered; study it now.
4. (40m) Write six STAR stories (Situation, Task, Action, Result, plus Reflection): conflict, failure, ambiguity, leadership/mentoring, production incident (use the game-day), delivering under pressure.
5. (20m) Write a system design template: requirements, estimates, API, data model, high-level design, deep dives, bottlenecks, trade-offs.

**Tips and common problems**
- Answers sound memorized → add an example from ShopFlow in each.
- STAR stories lack numbers → add scale, time and measurable result.

**Done when**
- [ ] 15 written answers, 6 STAR stories, and a design template exist.

### S2 Build (3h): portfolio and algorithms
1. (60m) Timed algorithms: 5 medium LeetCode problems in Java, 12 minutes each. Write after each: pattern used, complexity, what you missed.
2. (30m) Polish the ShopFlow README: purpose, architecture diagram, how to run in one command, tests, deployment, links to ADRs and design docs.
3. (20m) Add an ADR index (table: number, title, status, date) and a docs index.
4. (40m) Write a 5-minute demo script: show checkout, an alert firing, a trace, a canary rollback. Rehearse once with a timer.
5. (30m) Prepare a 10-minute talk outline: problem, architecture, three hard problems you solved with evidence, what you'd do differently.

**Tips and common problems**
- Stuck for 20 minutes on a problem → read the solution, then re-implement from scratch the next day.
- README is a wall of text → lead with a diagram and a 5-line quick start.

**Done when**
- [ ] Repo is presentable to a stranger in 3 minutes; demo script rehearsed.

### S3 Lab (2h): three mock interviews
Use this scoring rubric (1 = weak, 2 = partial, 3 = solid, 4 = strong) per mock.

| Mock | Dimensions |
|---|---|
| Java/Spring depth | Correctness, depth (why, not just what), trade-offs, debugging approach, communication |
| System design | Requirements, estimation, architecture, data model, failure modes, trade-offs, communication |
| Behavioural | Specific situation, ownership, result with numbers, reflection, concision |

1. (5m) Brief the interviewer: give the rubric, the question source (ledger, STAR list), and ask them to be strict and to stop you if you ramble.
2. (30m) **Mock 1, Java/Spring depth:** 20 minutes of questions (mix of ledger items, "how would you debug…" and a short whiteboard of a concurrency or transaction bug), 10 minutes of feedback.
3. (50m) **Mock 2, system design:** 40 minutes on an unseen prompt (for example a ticketing system), 10 minutes of feedback.
4. (30m) **Mock 3, behavioural:** 20 minutes using your STAR stories, 10 minutes of feedback.
5. (5m) Collect scores in `phase9-notes.md` with the top three improvements.

**If no interviewer is available:** record yourself and use an AI agent with a prompt such as: "You are a strict senior interviewer. Ask one question at a time, wait for my answer, probe weak points, and score using this rubric at the end. Do not give hints."

**Tips and common problems**
- You freeze on design prompts → follow the template, say your assumptions aloud, and start with requirements.
- Answers too long → practise 60-second versions, then expand only when probed.

**Done when**
- [ ] Three scored mocks with concrete improvement items.

### S4 Review + Write (2h)
1. (45m) Rewrite the weakest five answers from the mocks; re-record yourself answering each in under a minute.
2. (40m) Update your CV: each bullet follows action + scale + result (for example "Reduced checkout p95 from 900 ms to 300 ms by fixing N+1 queries and adding indexes").
3. (15m) Flashcards for items you missed in the mocks.
4. (10m) Self-check out loud, then check answers.
5. (10m) Retro.

**Self-check:** Pick five random ledger entries and answer each in one minute. Present the project in ten minutes.
<details><summary>Answers</summary>

- A good answer states the symptom, the mechanism, how you would confirm it with evidence, the fix, and how you would prevent it; compare yours with the ledger entry.
- A good talk covers the problem, the architecture, three hard problems with evidence (plans, dumps, traces), results, and what you'd change.
</details>

**Output:** polished repo README, CV, mock feedback notes.
**If stuck:** do less, better: pick three weak topics and write their answers; depth beats breadth.

---

## Week 60 — Game-day and final gate
**Outcome:** shows senior-level operation under pressure.
**Before you start:** a peer or mentor as **Injector** (ideally someone who has not seen the details of your deployments). The environment is the kind cluster with the full ShopFlow platform, never anything shared. Agree on an abort word. **If no peer is available:** randomize the faults yourself by writing three slips of paper, shuffling them, and setting a timer so the choice is not yours; fidelity is lower but still useful.

### S1 Learn (3h): preparation
1. (40m) Re-read SRE book ch. 14 (Managing Incidents). Note roles, severity levels and communication cadence.
2. (30m) Re-read your runbooks and dashboards; update anything unclear.
3. (40m) Write the "first five minutes" checklist: acknowledge, declare incident and severity, check dashboards (RED), check recent deploys, check dependencies, decide mitigate vs investigate, start a notes file with timestamps.
4. (30m) Prepare your toolbox: saved Grafana views, `kubectl` aliases, `psql` snippets (`pg_stat_activity`, `pg_stat_statements`), `jcmd` commands for heap/thread dumps, Kafka CLI for lag and consumer groups.
5. (40m) Injector prep (done by the peer, shared in a sealed note): choose three faults from the recipe list below; test each once; prepare rollback commands; define the expected visible symptoms.

**Fault recipe catalog (for the Injector)**
| Fault | How to inject | Expected symptom | Safe rollback |
|---|---|---|---|
| Slow query after bad migration | `DROP INDEX CONCURRENTLY idx_orders_customer_created;` | p95 up, DB CPU up, top query in `pg_stat_statements` doing seq scan | Recreate the index concurrently |
| Memory leak | Set env `LEAK_ENABLED=true` on a service (lab-only flag adding to a static list) | Heap grows, GC time up, eventually `OOMKilled` | Unset flag and restart; heap dump first |
| Kafka poison message | `echo 'not-json' \| kafka-console-producer --topic order-events ...` | Consumer lag grows, same offset retried, errors in logs | Skip via DLQ/seek, fix deserializer |
| Bad config | Change DB URL in a ConfigMap and restart one service | CrashLoopBackOff or readiness failing | Restore the ConfigMap from git |
| CPU throttling | Lower `limits.cpu` on Orders | Latency spikes, throttled metrics | Restore limit |
| Network latency | Toxiproxy latency on Payments | Timeouts, circuit breaker opens | Remove toxic |
| Disk fill | Fill a PVC with a file | Write errors | Delete file |

Rules for the Injector: one fault at a time; record the exact injection time; no hints for the first 10 minutes, then tiered hints (area → component → evidence); stop on the abort word.

**Done when**
- [ ] Checklist and toolbox ready; Injector has three tested faults and rollbacks.

**Tips and common problems**
- You over-prepare tools → the point is method; practise reading telemetry not memorizing commands.

### S2 Build (3h): SF-45 game-day
1. (10m) Briefing: scope (kind cluster only), roles (Responder, Injector, optional Scribe), abort word, communication file `gameday-log.md`. Baseline: confirm dashboards are green.
2. (50m) **Fault A.** Responder: acknowledge, declare severity, post an update every 10 minutes, mitigate first (rollback, scale, restart, flag off), preserve evidence (heap/thread dump, logs) before destroying it, then find root cause. Injector observes and takes notes.
3. (50m) **Fault B** with the same procedure; the Injector may overlap the cleanup of A with the start of B to create pressure.
4. (50m) **Fault C** with the same procedure.
5. (20m) Hot debrief: timeline of what the Responder did, what slowed them, what telemetry was missing.

**Tips and common problems**
- Jumping to root cause while users are impacted → mitigate first, then investigate.
- Changing many things at once → change one thing, observe, then decide.
- Lost track of time → keep timestamps in the log for detection, mitigation and resolution.

**Done when**
- [ ] Three faults detected and mitigated; `gameday-log.md` has timestamps and decisions.

### S3 Lab (2h): the postmortem
1. (15m) Build the timeline from `gameday-log.md`, dashboards (annotate times), Git history and logs (UTC).
2. (30m) Write Summary, Impact (who, how long, how much) and Detection (how and when it was noticed; was it an alert or a human?).
3. (30m) Root causes and contributing factors for each fault using "5 whys". Avoid individuals; describe system conditions.
4. (25m) Action items: each with owner, due date, and type (prevent, detect, mitigate). At least one per fault. Add "what went well/badly/where we got lucky".
5. (20m) Language review: remove blame, vagueness and passive hiding. Ask the Injector to read it.

**Postmortem template**
```
# Postmortem: <title>   Date: <UTC>   Authors: <names>   Status: draft/final
## Summary
## Impact
## Timeline (UTC)
## Detection
## Response
## Root causes and contributing factors
## What went well / badly / where we got lucky
## Action items (owner, due, type)
```

**Done when**
- [ ] Postmortem is blameless, specific, and has owned action items with dates.

### S4 Review + Write (2h)
1. (50m) Final review against the **senior exit checklist** in `../java-backend-roadmap.md`: for each of the nine items, link the evidence (file, PR, ledger entry, dashboard, recording). Mark any item without evidence as a gap.
2. (20m) Hold the final review with an external reviewer if possible, or record yourself walking through the nine items and the evidence.
3. (20m) Write the gap list and turn it into a six-month plan: specialization (for example performance engineering or data platforms), certifications (for example AWS or CKA), open-source involvement, and mentoring.
4. (20m) Tag `phase-9`, update the progress tracker, publish blog posts and links.
5. (10m) Final retro: what worked in the plan, what you would change for the next learner.

**Final gate rubric (pass all):**
- Three injected faults diagnosed from telemetry; mitigations are safe and communicated.
- Postmortem is blameless, specific, and has owned action items.
- The senior exit checklist has evidence for all nine items.
- Public artefacts exist: ShopFlow repo, ADRs, design docs, ledger, blog posts, one OSS PR.

**Self-check:** What do you do in the first five minutes of an incident? Why mitigate before finding the root cause? What makes a postmortem blameless?
<details><summary>Answers</summary>

- Acknowledge, declare the incident and severity, check user-facing dashboards and recent changes, decide on mitigation, and start a timestamped log.
- Users are being harmed now; rollbacks, restarts or flags restore service quickly, and root-cause analysis can continue with preserved evidence.
- It looks for system conditions that allowed the failure (tooling, process, missing guardrails) rather than naming a person as the cause, and it lists concrete, owned fixes.
</details>

**Output:** `phase-9` tag, game-day postmortem, final review notes.
**If stuck:** if the game-day goes badly, that is the data: write the postmortem anyway and repeat with new faults a week later.
