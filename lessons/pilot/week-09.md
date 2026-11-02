# Week 9: Logging, debugging, Maven, and the Phase 1 gate

**Mode:** A→B (build-along for logging, debugger and Maven; a review challenge; one incident)   **Release:** v0.1 CLI (gate)
**Outcome:** you can log safely, debug with conditions instead of print statements, explain the Maven lifecycle, and you pass the Phase 1 gate by finding eight planted problems that come from weeks 3–8.
**Before you start:** the `shopflow-lab` repo is cloned. JDK 21 or newer. This week is standalone: you do not need your earlier weeks' solutions, but the gate review in S4 will look at your own Phase 1 code, so have it in a branch you can show.

**Repo:** `shopflow-lab`, module `core`, packages `shopflow.w09`, `shopflow.w09.review`, `shopflow.w09.incident`, `shopflow.w09.debug`, tag `@Tag("w09")`
**Run:** `scripts/run-week.sh w09` (from the `shopflow-lab` folder)
**Hints:** `hints/w09/` (five levels each, open one at a time)
**Solutions:** `solutions/w09/` (peek only AFTER your tests are green)

> Expect 20 of 30 tests red on the first run. The review challenge, the incident and the debugger exercise hold most of them.

---

## S1 Try it (3h): mode A

You will learn: the SLF4J/Logback split, log levels, parameterised messages, MDC, the five debugger moves you will use for the rest of your career, and the Maven lifecycle.

**TypeScript comparison:** `console.log` is both the API and the printer. In Java, **SLF4J** is the API you call (like a `logger` interface) and **Logback** is the engine that decides what to print, where and in what format. You can swap the engine without touching your code.

1. (15m) **Baseline.** Run `scripts/run-week.sh w09`. Note which classes are fully green (none yet except the guards). Open `LoggingExercisesTest` and read the `@DisplayName`s.
2. (20m) **Primer (read, then close it).** Read the SLF4J manual "Getting started" and its FAQ entry about *parameterized logging*, then the Logback manual's "Configuration" chapter (appenders, encoders, pattern layout, logger levels). Write in `notes/w09.md` from memory: the five levels in order (TRACE, DEBUG, INFO, WARN, ERROR), what each is for, and why `log.info("x=" + x)` is worse than `log.info("x={}", x)`.
3. (20m) **Worked example.** Open `LoggingTour.java`. Predict, in a comment, which of the five level lines will print with Logback's default (no config file). Run `main`. Then notice the last block: the exception is the *last* argument and is not a placeholder; look at the stack trace it printed.
4. (25m) **Your own `logback.xml`.** Create `core/src/main/resources/logback.xml`:
   ```xml
   <configuration>
     <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
       <encoder>
         <pattern>%d{HH:mm:ss.SSS} %-5level [%X{requestId}] %logger{20} - %msg%n</pattern>
       </encoder>
     </appender>
     <logger name="shopflow" level="DEBUG"/>
     <root level="INFO"><appender-ref ref="CONSOLE"/></root>
   </configuration>
   ```
   Re-run `LoggingTour.main`. Change `shopflow` to `WARN` and re-run: which lines disappear? (Test classes in this repo force their own levels, so this file will not break any test.)
5. (20m) **EX-09-01 and EX-09-02** (`OrderLogger.logPlaced`, `StockAlerts.check`). Make the tests green one by one. The test checks the *template* (`Order placed: id={} cents={}`) and the argument array, not just the final text: that is how it detects string concatenation.
6. (20m) **EX-09-03 and EX-09-04** (`RequestScope.run`, `FailureLogger.logFailure`). For MDC think about "what if the action throws?". For the failure logger the exception goes **last**, after all placeholders. If stuck on MDC: `hints/w09/EX-09-03.md`.
7. (25m) **Debugger drill.** Set a normal breakpoint in `LoggingTour.main` on the `log.info("Order placed...")` line and use *Debug* (not *Run*). Practise these five moves: Step Over, Step Into, Step Out, Evaluate Expression (evaluate `cents * 2`), and Watch (add `orderId`). Then open `BulkPricer.lineCents`, set a breakpoint and step into `unitCents(index)` from a call you start by running `BulkPricerTest.smallOrder` in debug mode. Write down which move you used for which purpose.
8. (35m) **Maven lifecycle.** From `shopflow-lab` run, one at a time, and note what each prints and what is new in `core/target`: `./mvnw -pl core validate`, `./mvnw -pl core compile`, `./mvnw -pl core test -Dgroups=w08 -Dtest=TimeTourTest -Dsurefire.failIfNoSpecifiedTests=false` (a small green test from week 8), `./mvnw -pl core package -DskipTests`. Then `./mvnw -pl core dependency:tree` and find `logback-classic`, `slf4j-api` and the test-scoped `junit-jupiter`. Run `./mvnw -pl core help:effective-pom` and find the `maven.compiler.release` property. Finally write from memory the default phase order `validate → compile → test → package → verify → install → deploy` and what runs unit tests (surefire, bound to `test`).

**Tips and common problems**
- `SLF4J: No SLF4J providers were found` → only the API is on the classpath → add `logback-classic` (it is already in this module's `pom.xml`).
- Two logging backends on the classpath ("multiple SLF4J providers") → another dependency pulled in a second engine → find it with `dependency:tree` and exclude one.
- Breakpoint is ignored → you pressed *Run*, not *Debug* → click the bug icon, or right-click the test and choose *Debug*.
- Pattern shows `[]` for the request id → MDC is empty at that moment → expected outside a request; it fills in once `RequestScope.run` works.
- `help:effective-pom` or `dependency:tree` needs a plugin download the first time → it is not an error → let it finish (needs network).

**Done when**
- [ ] `LoggingExercisesTest` is fully green
- [ ] your `logback.xml` is committed and `LoggingTour` output changes when you change levels
- [ ] `notes/w09.md` lists the five debugger moves, the five levels and the phase order

---

## S2 Build (3h): the Phase 1 code review challenge

You are the reviewer. The package `shopflow.w09.review` is about 150 lines of ShopFlow code written by a "teammate". It compiles, it looks reasonable, and it hides eight problems that come from weeks 3–8. This is a dry run for the gate.

1. (10m) **Rules.** Do not run the tests yet. Create `review-notes.md`. You will write each finding as: `File:line, problem, why it is a problem, suggested fix`.
2. (45m) **Read-only review pass.** Read every file in `core/src/main/java/shopflow/w09/review/` slowly (`Sku`, `Customer`, `LoyaltyPoints`, `Cart`, `Checkout`, `PriceListException`). Look at each class through these lenses: money arithmetic, time and clocks, exceptions (including empty `catch`), `equals`/`hashCode`, mutable objects used as hash keys, collections handed out or modified while iterating, boxed numbers compared with `==`. Aim for eight findings. Stop at 45 minutes even if you have fewer.
3. (10m) **Check your score.** Run `./mvnw -q -pl core test -Dgroups=w09 -Dtest=CodeReviewChallengeTest -Dsurefire.failIfNoSpecifiedTests=false`. Each failing test whose display name starts with `ISSUE:` is one planted problem. Count how many you had already written down. Tests starting with `guard:` should be green and must stay green.
4. (15m) **Study the misses.** For every issue you missed, re-read the exact lines and write what you overlooked. If you want a nudge, open `hints/w09/REVIEW-09.md` one level at a time.
5. (80m) **Fix them, one at a time (about 10 minutes each).** For each issue: run only its nested test class (IntelliJ: click the gutter arrow next to the nested class; command line: `-Dtest='CodeReviewChallengeTest*'` runs all of them), make the smallest correct fix, then commit with a message that names the issue.
6. (10m) **Map each issue to the week where you learned it.** Open the answers only after the fixes are done. In `review-notes.md` add a third column: *how could this have been caught automatically?* (a test, a compiler warning, SpotBugs, code review).
   <details><summary>Issue → week table (open after all eight are fixed)</summary>

   | # | Problem | Learned in |
   |---|---|---|
   | 1 | `double` arithmetic for money in `Checkout.total` | week 4 |
   | 2 | `LocalDateTime.now()` ignores the injected `Clock` and the zone in `Checkout.receipt` | week 8 |
   | 3 | Empty `catch (Exception)` swallows read and parse errors in `loadPriceList` | week 7 |
   | 4 | `Sku` overrides `equals` but not `hashCode` | week 4 |
   | 5 | `Customer` equality includes the mutable `name`, so a rename loses the map entry | week 5 |
   | 6 | `Cart.lines()` returns the internal mutable list | week 4 (defensive copying), week 6 |
   | 7 | `removeEmptyLines` removes while iterating (`ConcurrentModificationException`) | week 6 |
   | 8 | Boxed `Integer` compared with `==` in `sameQuantity` (fails above 127) | week 3 |
   </details>
7. (10m) **Wrap up.** Run `scripts/run-week.sh w09` for the whole week: only the incident and debugger classes should still be red. Open a PR titled "Phase 1 review challenge: 8 findings fixed" with the table in the description.

**Tips and common problems**
- You found only 3–4 issues in the read-only pass → normal on a first try → reviewing is a skill; repeat the pass with a different lens each time (one lens per read).
- A fix for issue 7 makes the test pass but you used `Iterator` by hand → fine, but `removeIf` is the idiomatic Java 8+ way.
- After fixing issue 5 the `Customer` still compares names somewhere → check `equals` and `hashCode` use the same fields and only immutable ones.
- After fixing issue 6 `Checkout` or another class mutates the list through `lines()` → they must use the cart's own methods instead.
- Issue 1: `new BigDecimal(0.1)` is not the same as `BigDecimal.valueOf(0.1)` or `new BigDecimal("0.10")` → build prices from strings or from integers.

**Done when**
- [ ] all `CodeReviewChallengeTest` tests are green, including the `guard:` tests
- [ ] `review-notes.md` has your original findings, the ones you missed, and the automatic-detection column
- [ ] PR opened

---

## S3 Incident (2h): mode B, one incident plus a debugger exercise

### INC-09-A (RL-25): "A customer's login token showed up in our log search"

> **From:** Priya (security) **Priority:** critical
> While investigating an unrelated bug I searched our log platform for "Bearer" and found real customer tokens, plus what looks like a password. Logs are shipped to a third-party SaaS and retained for 90 days. Please find out how this got in there and stop it. Don't just delete the old logs: tell me how we prevent it.

1. (10m) **PREDICT.** In `notes/w09.md`, before running anything:
   1. Which headers or fields of an HTTP request might contain secrets?
   2. What does `log.info("Incoming request: {}", request)` print when `request` is a Java `record`?
   3. Which is safer: removing known secrets from the text (blocklist) or logging only known-safe fields (allowlist)? Why?
   <details><summary>Answers</summary>

   1. `Authorization`, `Cookie`/`Set-Cookie`, API-key headers, and bodies containing passwords, card numbers or personal data.
   2. The record's generated `toString()`, which prints *every* component, including all headers and the whole body.
   3. An allowlist. A blocklist fails open: any new secret you did not think of is logged by default; an allowlist fails closed.
   </details>
2. (10m) **REPRODUCE.** `./mvnw -q -pl core test -Dgroups=w09 -Dtest=RequestLoggerTest -Dsurefire.failIfNoSpecifiedTests=false`. Expected: three red tests (`noBearerToken`, `noSessionCookie`, `noPassword`) and two green guards (`mentionsMethodAndPath`, `keepsTheRequestId`). In the failure message read the whole logged line: it contains `Authorization=Bearer abc123`, `session=s3cr3t` and `"password":"hunter2"`.
3. (15m) **INVESTIGATE.** Open `RequestLogger` (one line) and `HttpRequest` (a record). Run `new HttpRequest(...).toString()` in `jshell` or a scratch `main` to see exactly what is printed. The test captures log output with Logback's `ListAppender` (see `LogCapture`): read that helper, it is a pattern you will reuse. Hints: `hints/w09/INC-09-A.md`.
4. (10m) **STUDY.** Read the OWASP Logging Cheat Sheet, the part about *data to exclude*. Answer in notes: (a) name four categories of data that must never be logged. (b) Why is redacting only at the log call site fragile? (c) Where else could the same secret leak (think: exception messages, URLs, metrics tags)?
   <details><summary>Answers</summary>

   (a) Credentials/passwords, session ids and tokens, payment card data, personal data without a legal need. (b) Anyone adding a new log call can forget it; central allowlisting helpers or logging filters are safer. (c) Query strings in URLs, exception messages that echo input, tracing/metrics tags, error pages and crash dumps.
   </details>
5. (20m) **FIX and PROVE.** Change `RequestLogger` so it logs only: method, path, a fixed allowlist of safe headers (`X-Request-Id`, `Content-Type`, `Accept`) and the body *length* (never the body). Keep parameterised logging. Run `RequestLoggerTest`: five green. Add one more test of your own for a header named `x-api-key` (lowercase) to prove the allowlist is case-insensitive.
6. (10m) **GENERALIZE.** Ledger entry RL-25. In "Prevent" write: allowlist logging, a logging review checklist item, and (optionally) a test like this one for every request-logging class. In the one-minute answer include what you would tell the security team about already-leaked tokens: rotate/revoke them, because deleting logs does not un-leak them.

### DBG-09 (extra): "Finance says the grand total is wrong, and nobody knows which line"

> **From:** Finance. The total for the 1,000-line bulk catalogue is a few euros too high compared with our spreadsheet. We can't tell which lines differ. Dev: please don't add print statements to production code.

1. (10m) **PREDICT.** (a) The totals differ by 6,360 cents. Is it more likely one huge line is wrong or several lines are wrong by a small amount each? (b) What is the quickest way to find the *first* line where two implementations disagree? <details><summary>Answers</summary>(a) Several lines with the same small error is most plausible: the logic has a rule that is off at a boundary. (b) Compare the two implementations line by line and stop at the first difference: a conditional breakpoint does exactly that.</details>
2. (5m) **REPRODUCE.** Run `BulkPricerTest`: `smallOrder` is green, `grandTotalMatchesReference` is red with two different numbers.
3. (20m) **INVESTIGATE.** Using only the debugger:
   1. Set a breakpoint inside the loop in `BulkPricer.totalCents()` (the `total += lineCents(i)` line).
   2. Right-click the red dot and set the condition `BulkPricer.lineCents(i) != ReferencePricer.lineCents(i)`.
   3. Debug `BulkPricerTest.grandTotalMatchesReference`. The program stops at the first mismatching `i`.
   4. Use Evaluate Expression on `BulkPricer.quantity(i)` and `ReferencePricer.lineCents(i)`. What quantity do the mismatching lines have? Resume (F9) three more times to see whether the pattern holds.
   Hints (after 10 minutes): `hints/w09/DBG-09.md`.
4. (5m) **FIX and PROVE.** The Javadoc says "100 units or more". Fix the boundary in `BulkPricer.lineCents` and re-run the test.
5. (5m) **GENERALIZE.** Add a boundary test of your own for quantities 99, 100 and 101 against `ReferencePricer`. In `notes/w09.md` write the technique in one line: "Conditional breakpoint on `mine != oracle` finds the first disagreement without touching the code."

**Tips and common problems**
- The conditional breakpoint evaluates but never stops → the condition is false for all `i` because you already fixed the bug → undo the fix, or use the starter checkout.
- Debugging makes the test very slow → the condition is evaluated 1,000 times → normal for this size; for hot loops use "log evaluated expression" breakpoints instead of stopping.
- Your fix for INC-09-A also removed `X-Request-Id` → the guard `keepsTheRequestId` goes red → good: the guards exist to catch over-correction.
- `ListAppender` shows no events → the logger name does not match or the level is too high → use `LogCapture` (it forces DEBUG for the class under test).

**Done when**
- [ ] `RequestLoggerTest` (plus your extra test) green; `BulkPricerTest` green; your boundary test added
- [ ] ledger entry RL-25 written
- [ ] you can explain, in a minute, why allowlists beat blocklists

---

## S4 Consolidate (2h): the Phase 1 gate

**Phase 1 gate rubric (pass all):**
- No `double` for money, no `LocalDateTime` for instants, no swallowed exceptions.
- Every public value type has a correct `equals`/`hashCode`/`toString`.
- Tests are meaningful (they check behaviour, not just coverage).
- You can explain every ledger entry in under one minute.

1. (15m) **Ledger.** Finish RL-25 and count your entries so far (Phase 1 should have about 10, from RL-01 to RL-09, RL-25 and the extras). Rehearse three at random out loud, in under a minute each.
2. (30m) **Self-audit of your own Phase 1 code.** In your own project (the ShopFlow code you wrote for the S2 tickets of weeks 3–8), run:
   `grep -rn "double\|float" src/main` (money?), `grep -rn "LocalDateTime" src/main` (events?), `grep -rn "catch (Exception\|catch (Throwable" src/main` (swallowed?), and for each class with `equals` check `hashCode`. Write each hit as "fixed" or "justified". Optional: add the JaCoCo Maven plugin to your pom (look up the current version on Maven Central; verify against current docs), run `./mvnw verify`, open `target/site/jacoco/index.html` and look at behaviour that has no test. Coverage is a floor, not a goal.
3. (40m) **Gate review.** With a mentor or peer: they read your code against Effective Java items (10, 11, 17, 18, 49, 50, 69–77) and ask "is any of this TypeScript written in Java?". They pick three ledger entries for you to explain.
   **No reviewer yet?** Do this instead: record yourself (screen + voice, 15–20 minutes) walking through your five most important classes and two ledger entries. Watch the recording the next day and score it against the rubric above. For every "um, I'm not sure" moment, write a follow-up note and ask a mentor or an AI agent to quiz you on it (ask it to ask questions, not to give answers). Record a 5-minute re-take of the weakest part.
4. (15m) **Fix review findings.** Create tickets for anything you cannot fix now; fix the rest.
5. (10m) **Tag and track.** Update the progress tracker in `java-backend-roadmap.md` (Phase 1: gate passed, date, reviewer or "recording"). Then `git tag phase-1` and push the tag.
6. (10m) **Retro and Phase 2 preview.** Three lines in `retro.md`. Then read the headings of week 10 and install Docker if it is not installed: Phase 2 starts Spring Boot with Postgres in a container.

**Self-check:** answer aloud, then open the answers.
1. Which Maven phase runs unit tests, and which plugin does it? 2. Why is `log.info("x=" + x)` worse than `log.info("x={}", x)`? 3. What must never appear in logs? 4. Name the five debugger moves and one use for each. 5. What technique finds the first disagreement between two implementations without editing code?
<details><summary>Answers</summary>

1. `test`, run by the surefire plugin. 2. The concatenation builds the string even if the level is disabled, and the parameterised form keeps a stable template that tools can group by. 3. Secrets, tokens, passwords, session ids, card data and unneeded personal data. 4. Step Over (skip a call), Step Into (follow a call), Step Out (leave the method), Evaluate Expression (inspect or compute a value), Watch (track a value as you step). 5. A conditional breakpoint comparing the two results.
</details>

**Flashcards to add:** the eight review issues (Q: "what is wrong with `Integer == Integer` above 127?"), the MDC cleanup rule, "allowlist beats blocklist", "`final` does not make an object immutable".

**Done when**
- [ ] gate rubric all ticked (by a reviewer, or by you against the recording)
- [ ] tag `phase-1` pushed and tracker updated
- [ ] retro written

**Output:** `phase-1` tag, gate notes, `review-notes.md`, `logback.xml`, ledger entries (RL-25 and the earlier ones), `notes/w09.md`, retro.

**If stuck:** SLF4J manual "Getting started" for logging setup; the IntelliJ debugger docs for conditional breakpoints; the Maven "Introduction to the Build Lifecycle" page for phases.
