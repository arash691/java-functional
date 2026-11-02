# Week 8: Time, ISO-8601 and shared mutable state

**Mode:** A→B (build-along for `java.time`, then two incidents)   **Release:** v0.1 CLI
**Outcome:** you can model time correctly (Instant at rest, zone at the edge, injected Clock) and you have been bitten, on purpose, by the two classic time bugs.
**Before you start:** the `shopflow-lab` repo is cloned and `scripts/run-week.sh w00` works (week 1; it is fine if `HelloTest` is still red or already green). JDK 21 or newer. Weeks 3–7 are not required for the exercises: this week is standalone.

**Repo:** `shopflow-lab`, module `core`, package `shopflow.w08`, tag `@Tag("w08")`
**Run:** `scripts/run-week.sh w08` (from the `shopflow-lab` folder)
**Hints:** `hints/w08/` (five levels each, open one at a time)
**Solutions:** `solutions/w08/` (peek only AFTER your tests are green)

> Red tests are the starting point, not an error. Expect about 34 of 40 tests red on the first run. They turn green as you work.

---

## S1 Try it (3h): mode A

You will learn five types and one habit: `Instant`, `LocalDate`/`LocalDateTime`, `ZonedDateTime`, `Duration`/`Period`, and "inject a `Clock`".

**TypeScript comparison (only the parts that cause mistakes):**

| In JS/TS | In Java | Careful because |
|---|---|---|
| `Date` (a point in time, shows in the local zone) | `Instant` (a point in time, no zone at all) | `Date` mixes the two ideas; Java separates them |
| `new Date(2025, 2, 9)` (months from 0) | `LocalDate.of(2025, 3, 9)` (months from 1) | no off-by-one month surprise in `java.time` |
| `Date.now()` anywhere | `Instant.now(clock)` | the clock is a parameter, so tests are deterministic |
| `date.toISOString()` | `Instant.toString()` / `DateTimeFormatter.ISO_INSTANT` | both give `2025-03-09T07:00:00Z` |

1. (15m) **Baseline.** From `shopflow-lab` run `scripts/run-week.sh w08`. Skim the output: find the one test class that is fully green (`TimeTourTest`) and note how many tests fail. Open `TimeExercisesTest` and read only the `@DisplayName` titles.
2. (20m) **Primer (read, then close it).** Read the Java Tutorials "Date Time" trail: "Overview" and "Standard Calendar" pages (`Instant`, `LocalDate`, `LocalDateTime`, `ZonedDateTime`, `Duration`, `Period`). Then write from memory, in `notes/w08.md`, one sentence for each type: "is it a point on the timeline? does it have a zone?".
3. (20m) **Worked example.** Open `TimeTour.java`. Before running, write in a comment your prediction of the line "02:30 in New York". Run `main`. Then change the instant to `2025-11-02T05:30:00Z` and the zone list to include `Europe/London`; predict again, run again.
4. (25m) **EX-08-01 and EX-08-02** (`TimeExercises.fromIsoUtc`, `toIsoUtc`, `calendarDayOf`). Work test by test: after each change run `scripts/run-week.sh w08` and watch the count of red tests drop.
5. (30m) **EX-08-03 DST gap and overlap** (`resolve`, `laterOverlap`). First read the four test names. Predict: what is `LocalDateTime.of(2025, 3, 9, 2, 30)` in New York? Then implement. If stuck for 10 minutes: `hints/w08/EX-08-03.md`.
6. (25m) **EX-08-04 "a day later" has two meanings** (`sameLocalTimeTomorrow`, `exactly24HoursLater`). Draw the timeline of 2025-03-08 12:00 New York to the next noon on paper: how many hours long is that day? Then implement and read the `.as(...)` messages in the tests.
7. (20m) **EX-08-05 inject a Clock** (`greeting`). Use `clock.instant()` and the `zone` argument. Do not call `Instant.now()` or `LocalTime.now()` anywhere. Notice the last test: the clock's own zone must be ignored.
8. (15m) **EX-08-06 human formatting** (`format`). Use `DateTimeFormatter.ofPattern("d MMM uuuu HH:mm", locale).withZone(zone)`.
9. (10m) **Falsehoods.** Search the title "Falsehoods programmers believe about time" and write five items in `notes/w08.md` that you would have believed yesterday.

**Tips and common problems**
- `DateTimeException: Unsupported field: InstantSeconds` or `YearOfEra` → you formatted an `Instant` with a formatter that has no zone → add `.withZone(zone)`.
- Using `yyyy` and getting odd years near New Year → `yyyy` is "year of era" and `uuuu` is the proper year → prefer `uuuu`, which is also what the lesson patterns use.
- The French test fails with an unexpected abbreviation → you used `Locale.getDefault()` instead of the `locale` argument → pass `locale` explicitly (this is the same disease as week 8's incident: hidden defaults).
- IntelliJ shows no gutter arrow next to `main` → the module was not imported as Maven → "Maven" tool window → Reload All Maven Projects.
- Tests keep re-running everything → you ran `./mvnw test` without the tag → use `scripts/run-week.sh w08`.

**Done when**
- [ ] `TimeExercisesTest` is fully green
- [ ] `notes/w08.md` has one sentence per type and five "falsehoods"
- [ ] you can say, without looking, why 2025-03-09 in New York is 23 hours long

---

## S2 Build (3h): SF-05 order timestamps

**Ticket SF-05:** orders must store *when* they were placed as a point in time, show it in the customer's zone, promise delivery in 24 hours, and be saved as JSON with an ISO-8601 UTC string. The acceptance tests are in `OrderServiceTest`; the production stubs are `Order`, `OrderService` and `OrderJson`.

1. (20m) Read `OrderServiceTest` top to bottom without running it. In `notes/w08.md` write, in your own words, the requirements it expresses (11 tests). Note the `MutableClock` helper: it is a test double you can move by hand.
2. (25m) **Placing.** Make the three `placing an order` tests green. One line of production code should do most of the work.
3. (30m) **Displaying.** Make the three `displaying in the user's zone` tests green. Use a single static `DateTimeFormatter` (it is immutable, so sharing it is safe; compare with this week's S3). Pattern `uuuu-MM-dd HH:mm:ss xxx`.
4. (30m) **Promise.** Make the three `delivery promise` tests green. Two different "tomorrow" semantics are tested; decide for each which of `Instant.plus(Duration)` or `ZonedDateTime.plusDays` expresses it. If stuck: `hints/w08/SF-05.md`.
5. (35m) **JSON.** Make the two `persistence as JSON` tests green. You need an `ObjectMapper` with `JavaTimeModule` registered. Run the test first without configuring anything and read what Jackson writes for an `Instant`.
6. (20m) **Refactor.** All 11 tests green. Extract constants, add Javadoc to `OrderService`, and run `grep -rn "now()" core/src/main/java/shopflow/w08/` to prove that no `now()` call exists without a clock (the ticket's rule).
7. (20m) **Commit and PR.** One commit per green group (placing, displaying, promise, JSON). Write the PR description for SF-05: what changed, how it was tested, and one sentence on the DST decision.

**Tips and common problems**
- Jackson writes `"placedAt":1741503600.000000000` → by default dates are numbers → disable `SerializationFeature.WRITE_DATES_AS_TIMESTAMPS`.
- `InvalidDefinitionException: Java 8 date/time type java.time.Instant not supported by default` → `JavaTimeModule` is not registered → `registerModule(new JavaTimeModule())`.
- Display test off by one hour → you formatted the `Instant` in UTC or used `LocalDateTime` → convert with `atZone(userZone)` first.
- The "promise" tests pass in UTC but not in New York → you used `plusDays` on a UTC `ZonedDateTime` → the zone decides what a calendar day is.
- `OrderServiceTest` compiles but `OrderJson` throws `UnsupportedOperationException` → you have not replaced the TODO yet; that is expected.

**Done when**
- [ ] all 11 `OrderServiceTest` tests green
- [ ] `grep -rn "now()" core/src/main/java/shopflow/w08/` finds only comments (for example in `TimeExercises`), no call in `OrderService`
- [ ] PR text for SF-05 written

---

## S3 Incident (2h): mode B, two tickets (RL-06 and RL-07)

### INC-08-A (RL-06): "The daily sales report is wrong in the Tokyo data centre"

> **From:** Nadia (finance) **Priority:** high
> We moved the nightly job to the new Tokyo server. The report for our New York shop now shows some evening orders on the NEXT day, and the totals per day no longer match the till. On the old server in New York it was right. Same code, same data. What changed?

1. (10m) **PREDICT.** Write answers in `notes/w08.md` before running anything.
   1. The shop is in New York. An order arrives at `2025-03-09T23:30:00Z`. On which calendar day should the report count it?
   2. If code asks "which day is it?" without naming a zone, whose zone does Java use?
   3. A `LocalDate` has no zone. So where could a hidden zone come from?
   <details><summary>Answers</summary>

   1. March 9 (19:30 New York time).
   2. The JVM's default time zone (`ZoneId.systemDefault()`, from the OS or `-Duser.timezone`).
   3. From a conversion somewhere that used the default zone implicitly.
   </details>
2. (10m) **REPRODUCE.**
   `./mvnw -q -pl core test -Dgroups=w08 -Dtest=DailySalesReportTest -Dsurefire.failIfNoSpecifiedTests=false`
   Expected: four failures named `sameReportOnAnyServer`, one for each server zone, with a message like `[report computed on a server whose default zone is Asia/Tokyo]` and a different map than `{2025-03-09=1500, 2025-03-10=200}`. `worksWhenTheServerRunsInTheShopZone` stays green: that is why nobody noticed on the old server.
3. (15m) **INVESTIGATE.**
   - Run `ZoneProbe.main` twice with VM options `-Duser.timezone=America/New_York` and `-Duser.timezone=Asia/Tokyo` (IntelliJ: Run → Edit Configurations → VM options). Note the difference in the output. Also read the `Charset.defaultCharset()` line: since JDK 18 the default charset is UTF-8 on every OS, but older JVMs differed, which is the same kind of hidden dependency.
   - Debug the Tokyo test case. Put a breakpoint in `DailySalesReport.dayOf`, step INTO `toLocalDate()` and find the zone it consults.
   - Hint ladder: `hints/w08/INC-08-A.md` (open one level at a time after 10 minutes).
4. (10m) **STUDY** (now that you have seen the failure). Read the Javadoc of `java.time.ZoneId#systemDefault` and `java.sql.Date#toLocalDate`. Answer in notes: (a) which method takes the shop zone? (b) why is the bug invisible on a developer laptop? (c) what is the single rule that prevents this class of bug?
   <details><summary>Answers</summary>

   (a) None: `dayOf` ignores the `shopZone` field. (b) The laptop's default zone happens to equal the shop's zone. (c) Never rely on defaults: pass the zone (and charset, locale) explicitly.
   </details>
5. (10m) **FIX and PROVE.** Compute the day from the instant and the shop zone in `dayOf`. Run the class: all five tests green, including the guard. Then run the whole week: no new red tests.
6. (5m) **GENERALIZE.** Write the ledger entry RL-06 using the `reality-lab/LEDGER.md` template: Symptom, Why, Reproduce (the test name), Observe with (debugger + `ZoneProbe`), Fix, Prevent (explicit zone parameter; the parameterised test over server zones), 1-minute answer.

### INC-08-B (RL-07): "Some orders from the nightly import land on the wrong date"

> **From:** Omar (support) **Priority:** medium
> The nightly import file has the right dates, but after the import a handful of orders show the wrong date, and it is a different handful every night. Sometimes the import log has odd errors like `NumberFormatException: For input string: ""`. Re-running a single order always works.

1. (10m) **PREDICT.**
   1. `private static final SimpleDateFormat FORMAT` is `final`. Does that make it safe to share between threads?
   2. If two threads parse at the same moment with one shared parser object, what do you expect to happen?
   3. Will a failing test fail on every run? Why might it not?
   <details><summary>Answers</summary>

   1. No. `final` freezes the reference, not the object's internal state.
   2. The parser keeps working state inside the object (a calendar and buffers). Two threads overwrite each other's state, so you get wrong dates or exceptions.
   3. Usually yes with many threads, but a race is probabilistic; the test uses 8 threads and 20,000 iterations to make it very likely.
   </details>
2. (10m) **REPRODUCE.** `./mvnw -q -pl core test -Dgroups=w08 -Dtest=OrderDateParserTest -Dsurefire.failIfNoSpecifiedTests=false`. Expected: `parsingIsCorrectUnderConcurrency` fails with `wrong or failed parses out of 160000 (first problem: ...)`. The two guard tests (single-threaded, garbage input) stay green. Run it 3 times; the first-problem text changes between runs.
3. (15m) **INVESTIGATE.** There is no blocking, so a thread dump will not show anything. Read the failure's "first problem" text. Open the `SimpleDateFormat` Javadoc, section "Synchronization". Hints: `hints/w08/INC-08-B.md`.
4. (10m) **STUDY.** Read Effective Java Item 17 ("Minimize mutability"). Answer: (a) why are immutable objects automatically thread-safe? (b) name two immutable `java.time` types and one mutable legacy date class.
   <details><summary>Answers</summary>

   (a) Nothing can change after construction, so there are no races to protect. (b) `LocalDate`, `Instant`, `DateTimeFormatter`; mutable: `java.util.Date`, `Calendar`, `SimpleDateFormat`.
   </details>
5. (10m) **FIX and PROVE.** Replace the shared `SimpleDateFormat` with `LocalDate.parse`, keeping the `IllegalArgumentException` contract (the garbage-input guard must stay green). Run `OrderDateParserTest` 10 times in a row (a shell loop is fine) and confirm it is never red.
6. (5m) **GENERALIZE.** Ledger entry RL-07. In "Prevent" write the rule: "legacy formatters (`SimpleDateFormat`, `DecimalFormat`) are not thread-safe; use `java.time` formatters or keep one per thread". One-minute answer rehearsed once aloud.

**Tips and common problems**
- Your RL-06 fix passes in the IDE but fails in Maven (or the other way around) → a test left a changed default zone behind → the tests restore it in `@AfterEach`; if you add your own zone tests, copy that pattern.
- You cannot reproduce RL-07 on a very small machine → few cores, few interleavings → keep the test as it is; it uses a barrier so all threads start together. Increase `iterationsPerThread` temporarily if needed (not in the committed test).
- "Fixing" RL-07 by adding `synchronized` → it makes the bug disappear but keeps shared mutable state and a lock → prefer the immutable formatter; mention the trade-off in your ledger entry.
- The debugger does not step into JDK classes → "Do not step into classes" filters are on → IntelliJ: Settings → Build, Execution, Deployment → Debugger → Stepping → disable the `java.*` filter, or use Force Step Into.

**Done when**
- [ ] `DailySalesReportTest` fully green and `OrderDateParserTest` green 10 times in a row
- [ ] ledger entries RL-06 and RL-07 written
- [ ] you explained both in under one minute each, out loud

---

## S4 Consolidate (2h)

1. (15m) **Ledger.** Finish and commit RL-06 and RL-07 (template: `reality-lab/LEDGER.md`).
2. (35m) **Write `docs/time-rules.md`** for ShopFlow in your own words. Skeleton to complete (each rule needs one sentence of "why" and one example from this week):
   1. At rest and over the wire: UTC, `Instant`, ISO-8601 with `Z`.
   2. Zones only at the edges: convert to the user's or shop's zone when displaying or when defining "a day".
   3. Never `LocalDateTime` for an event (an order, a payment); it has no zone and no instant.
   4. Inject a `Clock`; never call `now()` without one.
   5. Durations of elapsed time use `Duration`; calendar arithmetic uses `plusDays` on a `ZonedDateTime`.
   6. Never rely on JVM defaults for zone, charset or locale; pass them.
   7. No `SimpleDateFormat`/`java.util.Date` in new code; convert at the boundary with `toInstant()`.
3. (20m) **Grep sweep** over your own `core/src/main/java` code from earlier weeks and this week: `grep -rn "LocalDateTime.now\|new Date()\|SimpleDateFormat\|systemDefault\|\.now()" core/src/main/java`. For each hit decide: fix, or justify in a comment (demos like `TimeTour` and `ZoneProbe` are justified).
4. (15m) **Flashcards.** Put these in your flashcard tool and add your own five:
   - Q: Why is `LocalDateTime` wrong for "when was the order placed"? A: No zone/offset, so it is not a point in time.
   - Q: How many hours is 2025-03-09 in New York? A: 23 (spring forward).
   - Q: `plusDays(1)` vs `plus(Duration.ofHours(24))` across DST? A: Calendar day vs exactly 24 elapsed hours; they can differ by an hour.
   - Q: Why inject a `Clock`? A: Deterministic tests; no hidden global state.
   - Q: Why is a shared `SimpleDateFormat` dangerous? A: It is mutable; concurrent use corrupts its state.
5. (15m) **Self-check.** Answer aloud, then open the answers:
   1. Which type represents an instant on the timeline? 2. Why inject a `Clock`? 3. What breaks at a DST boundary? 4. Why did INC-08-A look fine on the old server?
   <details><summary>Answers</summary>

   1. `Instant` (or an `OffsetDateTime`/`ZonedDateTime`, which carry an offset). 2. Time becomes a parameter, so tests control it and there is no hidden dependency on the real clock. 3. Local times can be skipped (gap) or occur twice (overlap), and "one day" is not always 24 hours. 4. The old server's default zone equalled the shop's zone, so the hidden dependency on the default never showed.
   </details>
6. (10m) **Retro** (`retro.md`, three lines): what worked, what blocked me and for how long, what I change next week.
7. (10m) **Wrap up.** `git status`, commit, push. Check the output list below.

**Done when**
- [ ] `docs/time-rules.md`, grep sweep notes and flashcards exist
- [ ] retro written

**Output:** SF-05 (PR), `docs/time-rules.md`, ledger entries RL-06 and RL-07, `notes/w08.md`, retro.

**If stuck:** Java Tutorials "Date Time" for the types; search "falsehoods programmers believe about time"; the JDK Javadoc for `ZonedDateTime` explains gaps and overlaps precisely.
