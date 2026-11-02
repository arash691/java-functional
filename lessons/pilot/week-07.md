# Week 7: Exceptions, resources, files and strings

**Mode:** A → B (build-along first, then two incidents)  **Release:** v0.1 CLI
**Outcome:** you design an error-handling strategy (what is thrown, where it is translated, what is never swallowed), you read and write files safely with `java.nio.file`, and you have found two bugs that only appear when something goes wrong.

**Before you start**
- Week 6 done. Terminal in the root of `shopflow-lab`.
- Jackson (JSON) is already a dependency of `core`; you don't need to edit `pom.xml`.
- About 8 of the 46 tests this week are green at the start (worked example, guards). The rest are red.
- JavaScript comparison: there is no `try/catch` for *checked* exceptions in JS. In Java the compiler forces you to catch or declare them. The rules for when to use which are the main lesson of the week.

**Repo:** `shopflow-lab`, module `core`, package `shopflow.w07`, tag `@Tag("w07")`
Run all: `scripts/run-week.sh w07`   One class: `./mvnw -q -pl core test -Dtest=StockServiceTest -Dsurefire.failIfNoSpecifiedTests=false`
Hints: `hints/w07/` (INC-07-1, INC-07-2, SF-04)   Solutions: `solutions/w07/` (after your tests are green)

---

## S1 Try it (3h): exceptions, try-with-resources, NIO files, strings (mode A)

1. (15m) **Run the worked example.** Predict first: what do the three printed lines of `QuantityParser.main` show? Then:
   - `./mvnw -q -pl core compile`
   - `java -cp core/target/classes shopflow.w07.example.QuantityParser`
   Look at the third line (`cause: java.lang.NumberFormatException ...`). That is exception translation *with the cause kept*.
2. (25m) **Read, with questions.** Read **EJ Item 70** (checked exceptions for recoverable conditions, runtime exceptions for programming errors), **Item 72** (favor standard exceptions), **Item 73** (throw exceptions appropriate to the abstraction, i.e. translate), **Item 75** (include failure-capture information in detail messages). In your notes answer: (a) When would a caller reasonably *recover* from an exception? (b) Why does `GatewayException` (checked) get translated into `InventoryException` (unchecked) in the service layer? (c) What should a good message contain? Then re-open `example/` and find where each rule is applied.
3. (35m) **EX-07-01: custom exceptions and translation.**
   - Open `OutOfStockException` and `UnknownSkuException` (stubs) and `InventoryException`, `GatewayException`, `LegacyStockGateway` (complete).
   - Complete the two exceptions: store the data in fields, expose `sku()`, `requested()`, `available()`, and build the exact messages (`Out of stock: SKU-1 (requested 5, available 2)`, `Unknown SKU: X-9`).
   - Implement `StockService.available`: call the gateway; `-1` means unknown SKU; a `GatewayException` becomes `new InventoryException("Could not read stock for " + sku, e)`: **pass `e` as the cause**. Then `require`.
   - Run `-Dtest=StockServiceTest`. The test `translatesAndKeepsTheCause` checks `.hasCause(down)`: look at how it does it.
4. (30m) **EX-07-02: try-with-resources.** First the problem: in a scratch test write `Resources`-like code with `try { ... } finally { r.close(); }` where *both* the body and `close()` throw. Which exception do you see? (The close exception hides the real one.) Then read **EJ Item 9** (prefer try-with-resources). Implement `Resources.useResource` and `Resources.useTwo` with `try (TrackingResource a = ...; TrackingResource b = ...)`. Run `-Dtest=ResourcesTest`. Check the order in the log: resources close in reverse order of opening.
5. (30m) **EX-07-03: NIO file I/O.** Read the Java Tutorials "Basic I/O", the `java.nio.file` part (Path, Files), and the Javadoc of `Files.readAllLines`, `Files.write`, `Files.lines`, `Files.createDirectories`, `Files.copy`. Implement `FileOps`:
   - `writeLines`: create the parent directory if there is one, then `Files.write(file, lines, UTF_8)`.
   - `readLines`: `Files.readAllLines(file, UTF_8)`. A missing file already throws `NoSuchFileException`: don't catch it.
   - `countLinesContaining`: `Files.lines(...)` returns a *stream that must be closed*: use try-with-resources.
   - `copyUtf8`: `Files.copy(from, to, REPLACE_EXISTING)`.
   - Run `-Dtest=FileOpsTest`. The tests use JUnit's `@TempDir`: a fresh temporary folder per test, deleted afterwards.
6. (30m) **EX-07-04: strings, and a measurement.** Read the Javadoc intro of `String` (immutability) and `StringBuilder`. Implement `joinSkus` (a `StringBuilder`, no `+=` in the loop), `maskCardNumber` (`"*".repeat(...)` plus the last four characters) and `slugify` (lowercase, `replaceAll("[^a-z0-9]+", "-")`, then strip leading/trailing dashes). Run `-Dtest=Strings101Test`. Then measure: in a scratch test build a 100,000-element string with `s += "x"` and with a `StringBuilder`, timing both with `System.nanoTime()`. Write the two numbers and explain the difference in one sentence.
7. (15m) **Check and commit.** `scripts/run-week.sh w07`: the S1 classes (`WorkedExampleTest`, `StockServiceTest`, `ResourcesTest`, `FileOpsTest`, `Strings101Test`) are green; `InventoryStoreTest`, `ImportServiceTest`, `LineCounterTest` remain red. `git add -A && git commit -m "w07 S1: exceptions, resources, NIO, strings"`.

**Tips and common problems**
- `unreported exception java.io.IOException; must be caught or declared to be thrown` → checked exception → either handle it (translate/recover) or declare `throws`; for `FileOps` the signatures already declare it.
- `NoSuchFileException` vs `FileNotFoundException` → NIO throws the former, the old `java.io` classes the latter → this repo uses NIO; the tests expect `NoSuchFileException`.
- Accented characters turn into `?` or garbage → you used the platform default charset → always pass `StandardCharsets.UTF_8` (the older API `FileReader` uses the default).
- `Files.lines` leaks a handle (a warning on Windows when `@TempDir` cannot delete) → you didn't close the stream → try-with-resources.
- `ResourcesTest` fails with "expected [close:b, close:a]" → you declared the resources in separate nested `try` blocks in the wrong order, or closed manually → one `try ( A a = ...; B b = ...)` header closes in reverse order.

**Done when**
- [ ] The five S1 test classes are green.
- [ ] You can explain why the `finally` version hides the real exception and try-with-resources does not.
- [ ] You measured string concatenation vs `StringBuilder` and noted both numbers.
- [ ] Work committed.

---

## S2 Build (3h): SF-04, saving and loading the inventory (mode A, tests first)

**Ticket SF-04:** *Persist the stock map as JSON in a file so it survives restarts. A missing or corrupted file must give a clear error naming the file, and the original cause must not be lost.* Acceptance tests: `InventoryStoreTest.java`.

File format: `{"version":1,"stock":{"A-1":5,"B-2":0}}`, keys sorted, UTF-8.

1. (15m) **Read the tests first.** List the rules, including the six `Loading` failure cases (missing, invalid JSON, empty, no `stock`, negative quantity, version 2) and the exact words each message must contain (`not found`, `corrupt`, `unsupported version`).
2. (10m) **Run and look.** `-Dtest='InventoryStoreTest*'`. Expect everything to fail with `UnsupportedOperationException: TODO SF-04`.
3. (20m) **Jackson primer.** In a throwaway `ScratchTest.java` (tag `w07`; delete before committing) experiment: create `new ObjectMapper()`, write `Map.of("A", 1)` with `writeValueAsString`, read a string back with `readValue(json, Map.class)`, and feed it three bad inputs: `"{not json"`, `""`, `"[]"`. Print `e.getClass().getName()` for each. Note that they are all subclasses of `JsonProcessingException`. Define a private `record StoreFile(Integer version, Map<String,Integer> stock)` and try reading `{"version":1,"stock":{"A":1}}` into it.
4. (30m) **`save`.** Create parent directories, write `new StoreFile(1, new TreeMap<>(stock))` as JSON with a shared `ObjectMapper` (a `private static final` field), `Files.writeString` (UTF-8 is the default for this method). Wrap `IOException` in `StoreException("Could not write store file: " + file, e)`. Run the `Saving` tests that apply.
5. (35m) **`load`, part 1: read and parse.** Three small steps in one method: (1) `Files.readString(file)`; catch `NoSuchFileException` and throw `StoreException("Store file not found: " + file, e)`; catch other `IOException`s with a generic message. (2) `mapper.readValue(json, StoreFile.class)`; catch `JsonProcessingException` and throw `StoreException("Store file is corrupt: " + file, e)`. Run `missingFile`, `invalidJson`, `emptyFile`.
6. (25m) **`load`, part 2: validate.** After parsing: a `null` result or `version != 1` → `"Store file has unsupported version <n>: <file>"`. A `null` `stock` map, or any `null` or negative quantity → `"Store file is corrupt: <file>"`. Run the remaining `Loading` tests.
7. (25m) **Review your error handling.** Check each `catch` block: (a) Is the original exception passed as the cause? (b) Does the message contain the file path? (c) Does any block catch `Exception` or `Throwable`? (d) Is anything logged *and* rethrown (don't do both)? Fix what you find. Then study why `ioProblemsAreTranslated` is green: what actually fails when a regular file is used as a directory?
8. (20m) **Ship it.** Run `-Dtest=InventoryStoreTest`. Commit with SF-04 in the message, write a PR description (`~/shopflow-notes/pr-sf04.md`) listing the six failure modes and how each is reported. Stretch (discuss in notes, optional to code): write to a temporary file and `Files.move(..., ATOMIC_MOVE)` so a crash during `save` cannot leave a half-written store.

**Tips and common problems**
- `UnrecognizedPropertyException` → your JSON has a field the record doesn't → keep the record exactly `(version, stock)`.
- `MismatchedInputException: No content to map due to end-of-input` for the empty file → expected: it is a `JsonProcessingException`; catch it and report "corrupt".
- Keys are not sorted in the output → you passed the original `Map.of(...)` → copy into a `TreeMap` first.
- `hasMessageContaining("corrupt")` fails with "Corrupt" → case-sensitive match → lowercase in the message.
- `ioProblemsAreTranslated` fails with a raw `IOException` or `FileSystemException` leaking → your `save` doesn't catch `IOException` around `createDirectories` too.

**Done when**
- [ ] All `InventoryStoreTest` tests are green.
- [ ] Every `catch` in `InventoryStore` keeps the cause and names the file.
- [ ] No `catch (Exception e)` and no empty `catch` in your code.
- [ ] Commit and PR description exist.

---

## S3 Incident (2h): the silent success and the leaking handle (mode B)

### Incident 1 (60 min): INC-07-1, "The import says success but nothing was saved" (RL-09)

> **Ticket #5107, from Finance.** *"The nightly stock import reported `success: true, imported: 2` but the stock file is missing, so the morning stock levels are stale and we oversold 14 items. Nobody was alerted. Please make this impossible to miss."*

1. (10m) **PREDICT.** Read `importer/ImportService.java`. Don't run anything. Write answers:
   1) What happens if `Files.write(target, ...)` throws `NoSuchFileException` because the folder is missing?
   2) What does the method return in that case?
   3) Which `catch` is the "right" one (the one that handles a *bad line*) and which is the dangerous one?
   4) What would be lost if you only added `e.printStackTrace()` inside the dangerous `catch`?
   <details><summary>Answers</summary>

   1) The outer `catch (Exception e)` catches it and does nothing. 2) `ImportResult(true, stock.size(), skipped)`: success. 3) The inner `catch (NumberFormatException | ArrayIndexOutOfBoundsException)` is a valid, narrow handler for a bad line. The outer `catch (Exception e)` with an empty body is dangerous. 4) The caller still gets "success"; the information would be only in a log nobody reads. The caller must be told.
   </details>
2. (5m) **REPRODUCE.** `-Dtest=ImportServiceTest`. Expect `targetDirectoryMissing` and `inputFileMissing` to fail with `Expecting code to raise a throwable.`: nothing was thrown. The two `GUARD` tests (`happyPath`, `badLinesAreSkipped`) are green.
3. (15m) **INVESTIGATE.** Temporarily add `e.printStackTrace();` inside the outer `catch` and rerun the two failing tests: read which exception and which line. Remove the print again afterwards. Then trace what each failing test sees from the outside. Hints: `hints/w07/INC-07-1.md`.
4. (15m) **STUDY.** Re-read **EJ Item 77** (don't ignore exceptions) and **Item 73** (translate). Read the Javadoc of `Files.write` and `NoSuchFileException`. Answer: (a) Why is `catch (Exception e)` almost always wrong? (b) Which exceptions are *expected* here and which are *failures*? (c) Why must the caller, not the logs, learn about the failure?
5. (5m) **FIX.** Keep the inner catch for bad lines. Replace the outer `catch (Exception e) { }` with `catch (IOException e)` that throws `new ImportException("Could not import " + csv + " into " + target, e)`.
6. (5m) **PROVE.** `ImportServiceTest` green (4 tests). Add a regression test of your own: if the *input* is fine and the *target* is a directory, `ImportException` is thrown.
7. (5m) **GENERALIZE.** Ledger entry RL-09. One-minute answer: *"An empty `catch (Exception)` hides real failures and turns them into fake success. Catch only the exceptions you can handle, translate the rest into a domain exception with the original as the cause, and let the caller decide."*

### Incident 2 (60 min): INC-07-2, "File handles pile up on report failures" (resource-leak extra)

> **Ops alert.** *"The report service dies after a few days with `Too many open files`. It happens faster on days when the report disk has errors."*

1. (10m) **PREDICT.** Read `reports/LineCounter.java` and `InputStreamOpener.java`. Write answers:
   1) On the normal path, who closes the stream?
   2) If `readLine()` throws an `IOException` half-way, is `reader.close()` reached?
   3) How many handles leak if the disk fails on 1,000 reports?
   <details><summary>Answers</summary>

   1) `reader.close()` at the end of the method closes the reader, which closes the underlying stream. 2) No. The exception jumps to the `catch`, skipping the `close()` line. 3) 1,000: each failed run leaves one stream open until garbage collection (not guaranteed), eventually hitting the OS limit.
   </details>
2. (5m) **REPRODUCE.** `-Dtest=LineCounterTest`. Expect `closedWhenReadingFails` to fail with `[stream closed after the failure] Expecting value to be true but was false`. Three `GUARD` tests are green (count and close on success, empty file, failing to open). Look at `TrackingInputStream` in the test folder: how does it simulate a disk failure after 4 bytes?
3. (15m) **INVESTIGATE.** Put a breakpoint on the `catch` line in `countLines` and debug `closedWhenReadingFails`. When the breakpoint hits, inspect `stream`: `closed` is still `false`. Step forward: the method exits through `UncheckedIOException` without ever closing. Hints: `hints/w07/INC-07-2.md`.
4. (15m) **STUDY.** Re-read **EJ Item 9** (prefer try-with-resources to try-finally). Read the Java Tutorials "The try-with-resources Statement". Answer: (a) What does try-with-resources guarantee on both normal and exceptional exit? (b) What happens to an exception thrown by `close()` if the body already threw (you saw this in S1)? (c) Which objects need closing (files, sockets, streams, DB connections, `Files.lines`)?
5. (5m) **FIX.** Declare the `BufferedReader` in a try-with-resources header: `try (BufferedReader reader = new BufferedReader(...)) { ... }` and remove the manual `close()`. Keep the `catch (IOException e)` for translation.
6. (5m) **PROVE.** `LineCounterTest` green (4 tests). Run the whole week: `scripts/run-week.sh w07`.
7. (5m) **GENERALIZE.** Ledger entry "Resource leak on the failure path (extra)". One-minute answer: *"Closing a resource at the end of a method isn't enough: an exception skips it. Use try-with-resources so the resource is closed on every path; a failure while closing is attached as a suppressed exception instead of hiding the original one."*

**Tips and common problems**
- Test passes but you still see the problem in your head → check the *failure* path, not only the success path: that is the whole point of this incident.
- `Resource leak` warning in the IDE on `Files.lines`/`newBufferedReader` → real warning; wrap in try-with-resources.
- `UncheckedIOException` message is "java.io.IOException: disk error" → the wrapper's message includes the cause's text; the test uses `hasMessageContaining("disk error")`.
- You moved `opener.open(file)` out of the `try` header and it leaks if the `BufferedReader` constructor fails → put every resource that needs closing in the header.

**Done when**
- [ ] `ImportServiceTest` (4) and `LineCounterTest` (4) are green; `scripts/run-week.sh w07` fully green.
- [ ] Two ledger entries written and rehearsed aloud.
- [ ] You can say why "success" returned from a failed import is worse than a crash.

---

## S4 Consolidate (2h)

1. (25m) **Ledger.** Complete the entries RL-09 and the resource-leak extra (*Symptom, Why, Reproduce, Observe with, Fix, Prevent, One-minute answer, Confidence*). "Prevent" ideas: a team rule "no empty catch", an IDE inspection, a test for every failure path.
2. (25m) **Write ADR-002: Error-handling strategy for ShopFlow.** Put it in `docs/adr/002-error-handling.md` in your own repo or notes folder. Use this template and fill every section in your own words:
   ```
   # ADR-002: Error-handling strategy
   Status: accepted      Date: <today>
   ## Context
   <what problem are we solving: inconsistent errors, swallowed exceptions>
   ## Decision
   1. Programming errors (bad arguments, illegal state) -> unchecked standard exceptions.
   2. Recoverable external failures at a boundary -> translated into a domain exception (unchecked, extends InventoryException/StoreException/ImportException) at the layer boundary.
   3. Always keep the original exception as the cause.
   4. Messages name the thing that failed (sku, file path) and the numbers involved.
   5. Never write an empty catch or catch (Exception) without rethrowing; log OR throw, not both.
   6. Resources are always opened with try-with-resources.
   ## Consequences
   <good: callers get uniform errors; cost: more small exception classes>
   ## Alternatives considered
   <checked exceptions everywhere; error codes/Result types (we revisit in week 18)>
   ```
3. (15m) **Flashcards.** Five cards. Suggested: (1) Checked vs unchecked: when each? (2) What is exception translation and why keep the cause? (3) What does try-with-resources guarantee? (4) What is a suppressed exception? (5) Why pass an explicit charset?
4. (20m) **Sweep your code.** Search weeks 3 to 7 in your solution folder: `grep -rn "catch (" core/src/main/java/shopflow/w0{3,4,5,6,7}` and `grep -rn "printStackTrace\|FileInputStream\|FileReader" core/src/main/java`. For each hit decide against ADR-002: keep, change, or comment why. Fix at least one and write what you changed.
5. (15m) **Self-check.** Answer aloud, then open the answers.
   1) Why is `catch (Exception e) {}` dangerous? 2) What does the `cause` argument give a future debugger? 3) How are resources closed when you open two in one try header? 4) What does `Files.readAllLines` throw for a missing file? 5) Why `StringBuilder` instead of `+=` in a loop?
   <details><summary>Answers</summary>

   1) It hides real failures and can turn them into fake success. 2) The original stack trace and type, so the root cause is visible even after translation. 3) In reverse order of opening, even if the body or an earlier close fails. 4) `NoSuchFileException` (a subclass of `IOException`). 5) Strings are immutable: each `+=` copies the whole string, so the loop is O(n²); `StringBuilder` appends in amortised O(1).
   </details>
6. (10m) **Retro.** Three lines in `~/shopflow-notes/retro.md`. Tick Week 7 in your tracker.
7. (10m) **Commit, tag, preview.** `git add -A && git commit -m "w07 done" && git tag w07-done`. Skim next week's title (time and money bugs): write one guess at what can go wrong when a server runs in a different time zone from your laptop.

**Done when**
- [ ] ADR-002 written; two ledger entries; five flashcards.
- [ ] You applied ADR-002 to at least one place in your earlier code.
- [ ] 4 of 5 self-check questions answered without looking.

**Output this week:** exception classes and `StockService`, `Resources`, `FileOps`, `Strings101`, `InventoryStore` (SF-04), fixed `ImportService` and `LineCounter`, ADR-002, two ledger entries, tag `w07-done`.

**If stuck:** EJ Items 69 to 77 (the exceptions chapter); the Javadoc of `java.nio.file.Files`; for Jackson errors read the exception class name first: it tells you which stage failed.
