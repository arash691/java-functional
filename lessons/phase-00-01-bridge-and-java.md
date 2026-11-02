> **Superseded for weeks 1–12 (pilot).** The project-first version of these weeks is in `pilot/week-01.md` … `pilot/week-12.md`, backed by the `shopflow-lab` repo. This file is kept for reference; the Phase 1 gate rubric and the full reference lists still apply.

# Phase 0–1: Bridge and Java Language (Weeks 1–9)

Goal: working environment, a backend mental model, and idiomatic, tested Java.

Session format and the "Stuck?" routine are defined in `README.md`. Commands are starting points: if one fails, read the error and the tool's current docs.

---

## Week 1 — Backend mental model and setup
**Outcome:** can trace a request end to end and has a working toolchain.
**Before you start:** a laptop with admin rights, a GitHub account, Docker Desktop (or another Docker engine) installed.
**If stuck:** search "what happens when you type a URL" for the big picture; re-read HPBN ch. 2 for anything about handshakes.

#### S1 Learn (3h): the request's life and Node vs JVM
1. (45m) Take your own entry quiz. Write 12 one-line questions (3 each on HTTP, DNS/TCP/TLS, Git/shell, async). Answer them from memory with no searching, then mark each green/yellow/red. Green topics are skipped from now on.
2. (50m) Read HPBN ch. 1 (latency and bandwidth) and ch. 2 (TCP). Close the book and write from memory: what is RTT, why does a new TCP connection cost one round trip, what is slow start.
3. (30m) Read HPBN ch. 4 (TLS). From memory, list the extra round trips TLS adds and what session resumption saves.
4. (25m) Skim MDN "HTTP" guide: overview, messages, methods, status codes. Write the 5 status codes you will return most from a backend (200, 201, 400, 404, 500) and one sentence each.
5. (20m) Watch Philip Roberts, *What the heck is the event loop anyway?* at 1.25x speed. Then write 5 lines: how Node handles 1,000 concurrent requests (one thread, callbacks).
6. (10m) Write the JVM contrast in 5 lines: a servlet container gives each request a thread from a pool; blocking calls block that thread; hence pool sizing matters (revisited in Phase 5).

**Tips and common problems**
- "I know HTTP from the browser" → you know the client side; the server side adds connection pooling, timeouts and keep-alive → focus on what the server must do when a client is slow or gone.
- Reading HPBN straight through takes hours → it is long → read only the listed chapters and stop at the time box.
- Mixing up latency and bandwidth → both are measured in "speed" colloquially → remember: latency is time for one bit, bandwidth is bits per second.

**Done when**
- [ ] Quiz written, answered and colour-coded
- [ ] 3 short from-memory write-ups (TCP, TLS, event loop vs threads) saved in `notes/week-01.md`

#### S2 Build (3h): toolchain and first Maven project
1. (25m) Install SDKMAN: `curl -s "https://get.sdkman.io" | bash`, open a new terminal, run `sdk version`. Run `sdk list java`, pick a Temurin build of 25 (LTS), run `sdk install java <identifier>` (identifiers vary; verify against current docs). Check `java -version` and `javac -version`.
2. (20m) Install IntelliJ IDEA (Community or Ultimate). Open Settings → Project Structure and make sure the SDK points to the SDKMAN JDK, not an old system JDK.
3. (20m) Start Postgres: `docker run --name shopflow-pg -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:17` (use a supported major version). Verify with `docker exec -it shopflow-pg psql -U postgres -c "select version();"`.
4. (30m) Create the GitHub repo `shopflow`. Add `.gitignore` (Java + IntelliJ + Maven `target/`), a `README.md`, and `.github/pull_request_template.md` with sections: What, Why, How tested, Risks. In repo settings, protect `main`: require a pull request before merging and require status checks (checks come later).
5. (45m) Generate a project: `mvn archetype:generate -DgroupId=com.shopflow -DartifactId=hello -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false` (verify archetype version against current docs). Open `pom.xml`, set `<maven.compiler.release>` to your JDK major (25 or 21).
6. (25m) Run from the CLI: `mvn -q compile exec:java` is not available by default, so run `mvn package` and then `java -cp target/classes com.shopflow.App`. Then run the same class from IntelliJ via the gutter run icon.
7. (15m) Work through branch → commit → push → PR → merge on a trivial README change, using the PR template.

**Tips and common problems**
- `mvn: command not found` → Maven isn't installed (SDKMAN doesn't install it by default) → `sdk install maven`.
- `invalid target release: 25` → Maven's compiler plugin or JDK doesn't match → check `mvn -v` shows the same JDK as `java -version`; upgrade `maven-compiler-plugin`.
- `port is already allocated` on 5432 → a local Postgres already uses it → map another host port, e.g. `-p 5433:5432`.
- Push rejected on `main` → branch protection is working → create a branch and open a PR.

**Done when**
- [ ] `java -version`, `mvn -v` and `docker ps` show expected output
- [ ] `psql` inside the container returns the Postgres version
- [ ] First PR merged using the template

#### S3 Lab (2h): `reality-lab` repo and tracing a request
1. (15m) Create the `reality-lab` repo. Copy `LEDGER.md` and `CATALOG.md` into it. Commit.
2. (25m) Run `curl -v https://example.com 2>&1 | head -60`. Annotate each section: DNS/connect lines (`*   Trying`), TLS handshake lines, request headers (`>`), response headers (`<`). Save the annotated text in `notes/trace-curl.md`.
3. (20m) Run `dig example.com` and `dig example.com +trace` (verify flags against `man dig`). Identify the A record, TTL, and which servers answered at each level.
4. (25m) Run `openssl s_client -connect example.com:443 -servername example.com </dev/null`. Find the certificate subject, issuer, expiry (`notAfter`) and the negotiated protocol (TLS 1.2/1.3). Note it: you will use this for RL-94 in Phase 8.
5. (20m) Time the phases: `curl -o /dev/null -s -w "dns=%{time_namelookup} connect=%{time_connect} tls=%{time_appconnect} ttfb=%{time_starttransfer} total=%{time_total}\n" https://example.com`. Run it five times; explain why `dns` drops after the first run.
6. (15m) Explain the mechanism in 3 sentences (DNS caching, TCP then TLS before HTTP, TTFB includes server time), and write it up as the first ledger entry "RL-00 Where does request time go" using the template.

**Tips and common problems**
- `dig: command not found` → minimal installs lack dnsutils → install `bind-utils` / `dnsutils` (or use `nslookup`).
- `curl` shows HTTP/2 lines you don't expect → curl negotiates HTTP/2 via ALPN → add `--http1.1` to compare.
- Output too long to read → pipe to a file and open in the editor, annotate with comments.

**Done when**
- [ ] Annotated curl, dig and openssl outputs saved
- [ ] Timing breakdown run five times with a one-paragraph explanation
- [ ] Ledger entry RL-00 committed

#### S4 Review + Write (2h): Node↔JVM table and request-flow diagram
1. (30m) Draw the request flow on paper (browser → DNS → TCP/TLS → load balancer → app → DB → back). Mark five places it can hang or fail (DNS failure, connect timeout, TLS expiry, slow app, DB lock).
2. (30m) Write the Node↔JVM mapping table in `notes/node-to-jvm.md`: npm↔Maven, `package.json`↔`pom.xml`, `node_modules`↔`~/.m2`, event loop↔thread pool, `async/await`↔`CompletableFuture`, TS types↔Java types (erased generics), `console.log`↔SLF4J.
3. (30m) Recreate the diagram in Mermaid or draw.io and commit it to `docs/request-flow`.
4. (20m) Create 10 flashcards (Anki or text file) from today's material.
5. (10m) Weekly retro in `retro.md` (see README).

**Tips and common problems**
- Table feels shallow → it is a first pass → add rows each week as new surprises appear.
- Diagram tool takes too long → perfectionism → hand-draw and photo it.

**Done when**
- [ ] Diagram, table, 10 flashcards and retro committed

**Self-check:** What happens between typing a URL and the first byte of the response? Why does TLS add round trips? Where could a request hang?
<details><summary>Answers</summary>

DNS lookup (cached or recursive), TCP handshake (1 RTT), TLS handshake (1 RTT on TLS 1.3, 2 on 1.2), then the HTTP request travels through load balancer to app, possibly to the DB, and back; the first byte arrives after server processing. TLS negotiates keys and authenticates the server before any application data can be encrypted, which requires message exchanges. A request can hang in DNS, connect, TLS, waiting on a saturated server thread pool, a slow DB query, or a lock.
</details>

**Output:** repo with hello-world, `reality-lab` repo, request-flow diagram.

---

## Week 2 — Linux, shell and ShopFlow definition
**Outcome:** comfortable in a shell; ShopFlow scoped with a glossary and first tickets.
**Before you start:** Week 1 complete (shell, Docker, repo).
**If stuck:** `man <command>` first, then Julia Evans' *Bite Size Linux*; for Git messes, `git reflog` is your undo.

#### S1 Learn (3h): shell, processes, signals, branching
1. (50m) MIT Missing Semester: "The Shell". Type along with every example in a scratch directory.
2. (40m) MIT Missing Semester: "Shell Tools and Scripting". Write a script `findport.sh` that takes a port and prints the process using it (`lsof -i :PORT` or `ss -ltnp`).
3. (30m) Read Julia Evans, *Bite Size Linux*: processes and signals. From memory, write the difference between `SIGTERM`(15), `SIGKILL`(9) and `SIGINT`(2), and which one Kubernetes sends first.
4. (30m) MIT Missing Semester: "Version Control (Git)". Then run `git log --graph --oneline --all` on your repo and explain each line.
5. (30m) Read the trunk-based development overview (trunkbaseddevelopment.com) and Atlassian's gitflow comparison. Write 5 lines: which would you pick for a small team shipping daily, and why.

**Tips and common problems**
- `Permission denied` on a script → no execute bit → `chmod +x script.sh`.
- Exit codes confuse you → `$?` holds the last command's code → run `false; echo $?` and `true; echo $?`.
- macOS vs Linux flags differ (`ss` missing on macOS) → BSD vs GNU tools → use `lsof -i :PORT` on macOS.

**Done when**
- [ ] `findport.sh` works for a running process
- [ ] SIGTERM vs SIGKILL explained in `notes/week-02.md`
- [ ] Branching choice written in 5 lines

#### S2 Build (3h): ShopFlow requirements and tickets
1. (30m) Write `docs/requirements.md`: actors (customer, admin, warehouse), 8 use cases, non-goals (no real payments, no multi-currency in v1).
2. (30m) Read DDD-D ch. 1–2 (ubiquitous language). Then write `docs/glossary.md` with Product, SKU, Cart, Order, OrderLine, Payment, Reservation, Stock. One-sentence definitions, no synonyms.
3. (20m) Add three "invariants" per term (e.g. stock never negative; an order has at least one line; SKU unique).
4. (60m) Create the first 10 tickets from `../shopflow-backlog.md` (SF-01 to SF-05 plus the next five) in GitHub Issues, each with acceptance criteria copied and sharpened.
5. (30m) Add labels (`phase-1`, `phase-2`, `lab`) and a milestone per phase.
6. (10m) Open a PR with these docs; self-review it using the PR template.

**Tips and common problems**
- Requirements balloon → no scope limit → write non-goals first and defend them.
- Glossary words overlap ("Item" vs "Product") → naming isn't settled → pick one and add "not to be called X".

**Done when**
- [ ] Requirements, glossary and 10 issues exist in the repo and are merged or open

#### S3 Lab (2h): shell and Git drills
1. (20m) Start a trivial server (`python3 -m http.server 8080`). Find it with your `findport.sh`, send `kill -TERM <pid>`, confirm it exits. Start again and use `kill -KILL`; note it gives no chance to clean up.
2. (20m) Write a tiny script that traps `SIGTERM` and prints "cleaning up": `trap 'echo cleaning up; exit 0' TERM; sleep 1000 &` then `wait`. Send `kill -TERM`, see the message. Note: this is the shutdown behaviour a Java app needs later (RL-47).
3. (25m) Tail a log with filters: generate lines with `while true; do echo "$(date) INFO ok"; sleep 1; done > app.log &`, then `tail -f app.log | grep --line-buffered ERROR`. Append an ERROR line and see it.
4. (30m) Git rebase drill: create three messy commits on a branch, run `git rebase -i main`, squash two, reword one. Use `git reflog` to undo a bad rebase.
5. (20m) `git bisect`: in a throwaway repo make 10 commits where commit 6 breaks a test script; run `git bisect start`, `bad`, `good <hash>` and `git bisect run ./test.sh`.
6. (5m) Write the ledger entry for "signals and graceful shutdown".

**Tips and common problems**
- `grep` shows nothing live → output buffering → use `--line-buffered`.
- Rebase conflicts → overlapping edits → resolve, `git add`, `git rebase --continue`; abort with `git rebase --abort`.
- Lost commits after a rebase → they are still in reflog → `git reflog`, then `git reset --hard <hash>`.

**Done when**
- [ ] Trap script demonstrates graceful handling
- [ ] You recovered a "lost" commit with reflog
- [ ] Bisect found the bad commit automatically

#### S4 Review + Write (2h): ADR-001 and recall
1. (30m) Write ADR-001 "Why ShopFlow, why this tech stack" using the adr.github.io template (context, decision, consequences, alternatives).
2. (30m) Rehearse out loud the one-minute answer for "SIGTERM vs SIGKILL and why it matters for deployments". Record it on your phone.
3. (30m) Make 10 flashcards from Missing Semester notes.
4. (20m) Clean up tickets: assign Phase 1 tickets to weeks.
5. (10m) Retro.

**Tips and common problems**
- ADR is vague → "decision" lacks a reason → add one rejected alternative with why.

**Done when**
- [ ] ADR-001 merged; recording made; retro written

**Self-check:** Which signal lets an app shut down gracefully? What is the difference between a process and a thread? When do you rebase and when do you merge?
<details><summary>Answers</summary>

SIGTERM (it can be handled; SIGKILL cannot). A process has its own address space; threads share a process's memory and run within it. Rebase local, unpublished work to keep history linear; merge (or squash-merge) when integrating shared branches you must not rewrite.
</details>

**Output:** `docs/requirements.md`, glossary, ADR-001, 10 tickets.

---

## Week 3 — Java syntax, types and testing basics
**Outcome:** can write and test small Java programs.
**Before you start:** JDK and Maven working (Week 1).
**If stuck:** search the exact compiler message plus "java"; re-read dev.java language basics.

#### S1 Learn (3h): language fundamentals
1. (40m) dev.java "Learn" path: variables, primitive types, operators. In IntelliJ create `Basics.java` and re-type each example; run them.
2. (30m) Java Tutorials "Learning the Java Language": control flow (if, switch expressions, loops, enhanced for). Write one `switch` expression returning a value.
3. (30m) Methods, parameters, overloading. Prove pass-by-value: write a method that reassigns its parameter and one that mutates an object; print before/after.
4. (25m) Classes, packages and `main`. Create two packages and import across them.
5. (25m) Compiler vs JVM: run `javac Hello.java`, then `java Hello`, then `javap -c Hello` and find the `invokevirtual` for `println`.
6. (30m) Close all tabs. From memory, write a one-page summary of "what is different from TypeScript" (static types, no truthy/falsy, `==` on references, no `undefined`, checked at compile time).

**Tips and common problems**
- `class X is public, should be declared in a file named X.java` → filename must match the public class → rename file or class.
- `incompatible types: possible lossy conversion` → narrowing needs a cast → decide if the cast is really intended.
- Using `==` on strings "works" sometimes → string pool → always use `.equals`.

**Done when**
- [ ] `Basics.java` compiles and runs; pass-by-value demo prints expected output
- [ ] One-page TS-vs-Java summary in notes

#### S2 Build (3h): SF-01 skeleton with tests
1. (20m) Create module `inventory-core`: `mvn archetype:generate ... -DartifactId=inventory-core` (as in Week 1). Replace the generated JUnit 4 dependency with JUnit Jupiter and AssertJ in `pom.xml`; ensure `maven-surefire-plugin` is a recent version.
2. (15m) Add `src/test/java/.../InventoryTest.java` with one failing test: `new Inventory().stockOf("SKU-1")` returns 0.
3. (10m) Run `mvn test` and see it fail (red) because `Inventory` doesn't exist.
4. (30m) Implement `Inventory` with `add(String sku, int qty)`, `remove(String sku, int qty)`, `stockOf(String sku)` backed by a `HashMap<String,Integer>`. Make the first test green.
5. (45m) Write tests first for: add accumulates; remove below zero throws `IllegalArgumentException`; adding a negative quantity throws; unknown SKU returns 0.
6. (35m) Convert 3 tests to one `@ParameterizedTest` with `@CsvSource`. Read the JUnit 5 User Guide sections "Writing Tests" and "Parameterized Tests" first for 20 minutes of this block.
7. (25m) Commit on branch `sf-01-inventory`, open a PR, self-review the diff.

**Tips and common problems**
- Tests don't run (`Tests run: 0`) → old surefire with JUnit 5 → set surefire/failsafe ≥ 3.x in the `pom`.
- `package org.junit.jupiter.api does not exist` → missing dependency scope → add `junit-jupiter` with `<scope>test</scope>`.
- AssertJ import clashes → static import `org.assertj.core.api.Assertions.assertThat` only.
- IntelliJ runs tests but Maven doesn't (or vice versa) → different config → trust `mvn test` as the source of truth.

**Done when**
- [ ] `mvn test` is green with at least 6 tests
- [ ] SF-01 PR open with the red-then-green commit history visible

#### S3 Lab (2h): RL-01, RL-02
1. (15m) Write failing test for RL-01 in `reality-lab`:
```java
@Test void integerCache() {
    Integer a = 127, b = 127, c = 128, d = 128;
    assertThat(a == b).isTrue();
    assertThat(c == d).isTrue(); // expected to FAIL: shows == compares references
}
```
2. (10m) Run `mvn test`; observe the failure on `c == d`.
3. (20m) Observe with `javap -c`: find `Integer.valueOf` in the bytecode of the boxing line. Read the `IntegerCache` range (-128 to 127 by default) in the JDK docs for `Integer.valueOf`.
4. (10m) Explain in 3 sentences: boxing calls `valueOf`, which caches small values, so `==` compares object identity.
5. (10m) Fix and regress: replace `==` with `.equals` (or `intValue`) and change the test to assert `equals`.
6. (20m) RL-02: write a failing test that `Integer.MAX_VALUE + 1` is positive. Run; observe wraparound to `-2147483648`.
7. (15m) Fix with `Math.addExact` expecting `ArithmeticException`; add the regression test. Record where a quantity addition in `Inventory.add` should use `addExact`.
8. (20m) Write ledger entries RL-01 and RL-02 using `LEDGER.md` template, including the one-minute answer.

**Tips and common problems**
- Test passes unexpectedly for `c == d` → compiler/JVM flag changed the cache or you used `new Integer` → use autoboxing and don't pass `-XX:AutoBoxCacheMax`.
- Confusion on `+` overflow in `long` vs `int` → literal types → use `1L` where needed.
- Don't apply `addExact` blindly → cost vs benefit → use it for domain quantities.

**Done when**
- [ ] Both labs have a red test, an explanation and a green regression test
- [ ] Two ledger entries committed

#### S4 Review + Write (2h)
1. (30m) Write flashcards: Java vs TS differences (10 cards).
2. (30m) Re-read the `Inventory` tests; delete any test that only repeats another; add one boundary test (quantity 0).
3. (30m) Rehearse out loud RL-01 and RL-02 answers (under a minute each).
4. (20m) Update `notes/node-to-jvm.md` with three new rows.
5. (10m) Retro.

**Tips and common problems**
- Flashcards feel boring → passive → write a question with a code snippet that must be predicted.

**Done when**
- [ ] 10 new flashcards, ledger answers rehearsed, retro written

**Self-check:** Why does `Integer a=127,b=127; a==b` differ from 128? Is Java pass-by-value or by-reference? What does `javac` produce, and who runs it?
<details><summary>Answers</summary>

Boxing uses `Integer.valueOf`, which caches −128..127, so those are the same object; 128 creates new objects and `==` compares identities. Pass-by-value: object references are passed by value (a copy of the reference). `javac` produces bytecode (`.class`), which the JVM interprets/JIT-compiles.
</details>

**Output:** SF-01 skeleton merged with tests; 2 ledger entries.

---

## Week 4 — Classes, immutability and value objects
**Outcome:** can design small immutable value types correctly.
**Before you start:** SF-01 merged.
**If stuck:** re-read EJ Item 10 and Item 11; use `java.util.Objects.equals/hash`.

#### S1 Learn (3h): object basics and contracts
1. (30m) Read EJ Items 1 (static factories) and 2 (builder). From memory, write when a static factory beats a constructor.
2. (40m) Read EJ Items 10 and 11 (`equals`, `hashCode`). Write the five properties of `equals` and the hashCode rule in your own words.
3. (20m) Read EJ Items 12 (toString), 15 (minimize accessibility) and 17 (minimize mutability). Make a checklist of the five rules for immutable classes.
4. (20m) Read EJ Item 50 (defensive copies). Write an example where a caller mutates a `Date`/`List` passed in.
5. (30m) dev.java on records. Convert a small class to a record; note what is generated (accessors, `equals`, `hashCode`, `toString`).
6. (30m) Java docs: `BigDecimal` (scale, precision, `RoundingMode`) and `Currency`. Note `equals` compares scale, `compareTo` does not.
7. (10m) Write one-sentence summaries of each item.

**Tips and common problems**
- `BigDecimal("1.0").equals(new BigDecimal("1.00"))` is false → scale is part of equality → use `compareTo` or normalise scale.
- Records can't extend classes → they implicitly extend `java.lang.Record` → use composition or interfaces.
- Mutable fields in records (lists) are still mutable → shallow immutability → copy with `List.copyOf` in the compact constructor.

**Done when**
- [ ] Notes with the equals/hashCode rules and immutability checklist

#### S2 Build (3h): SF-02 `Money` and SF-03 catalog model
1. (30m) Write failing tests for `Money`: equal when same amount and currency; add/subtract; reject negative amounts if you decide so; reject mismatched currencies.
2. (40m) Implement `Money` as a record `Money(BigDecimal amount, Currency currency)` with a compact constructor that normalises scale (`amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN)`) and null checks. Add `plus`, `minus`, `times(int)`.
3. (20m) Add `Money.of("10.00", "EUR")` static factory.
4. (30m) SF-03: model `Product` (record with `Sku`, `name`, `Money price`, `Category` enum). Write a `Sku` record that validates format.
5. (30m) Tests for equality and invalid input of `Product`/`Sku`.
6. (20m) Mutation check: temporarily break `equals` (a record: change a component) and confirm a test fails.
7. (10m) Open PRs SF-02 and SF-03 separately.

**Tips and common problems**
- `new BigDecimal(0.1)` used in a test → binary float input → use string constructor.
- Division by `Money.times` produces non-terminating decimal → `ArithmeticException` → pass a scale and `RoundingMode`.
- PR too big → mixed concerns → one ticket, one PR.

**Done when**
- [ ] `Money`, `Sku`, `Product` implemented with passing tests
- [ ] Two PRs open or merged

#### S3 Lab (2h): RL-03, RL-04
1. (15m) RL-03 failing test:
```java
@Test void floatMoney() {
    double sum = 0;
    for (int i = 0; i < 10; i++) sum += 0.1;
    assertThat(sum).isEqualTo(1.0); // expected to FAIL: 0.9999999999999999
}
```
2. (10m) Run it and note the printed actual value.
3. (15m) Explain: binary floating point cannot represent 0.1 exactly; sums accumulate rounding error. Print `new BigDecimal(0.1)` to see the exact binary value.
4. (10m) Fix with `BigDecimal` sums via `Money`; add the regression test.
5. (20m) RL-04: write a class `BadSku` with `equals` only. Test that `new HashSet<>(List.of(a)).contains(b)` is true for equal objects; expect it to FAIL.
6. (15m) Observe: print `a.hashCode()` and `b.hashCode()` (identity hashes differ); `HashSet` looks in the wrong bucket.
7. (15m) Fix by adding `hashCode` with `Objects.hash`; add the regression; also test records get both for free.
8. (20m) Write ledger entries RL-03, RL-04.

**Tips and common problems**
- Test passes by luck on `contains` → collision into same bucket → run the test many times or insert more elements.
- IntelliJ can generate `equals/hashCode` → use it but review for `getClass` vs `instanceof` choice.

**Done when**
- [ ] Both labs red→explained→green
- [ ] Two ledger entries committed

#### S4 Review + Write (2h)
1. (30m) Refactor: are any classes still mutable without reason? Make them immutable.
2. (30m) Write notes: "equals/hashCode contract rules" and "when a record is wrong" (identity entities, many optional fields).
3. (30m) Rehearse RL-03/04 answers aloud.
4. (20m) Create 10 flashcards (BigDecimal pitfalls, record features).
5. (10m) Retro.

**Tips and common problems**
- Overusing records for JPA entities → records can't be proxied → keep entities as classes later.

**Done when**
- [ ] Notes and flashcards done; PRs merged

**Self-check:** What does the `equals`/`hashCode` contract require? Why is `new BigDecimal(0.1)` different from `new BigDecimal("0.1")`? When is a record the wrong choice?
<details><summary>Answers</summary>

Equal objects must have equal hash codes; `equals` must be reflexive, symmetric, transitive, consistent and false for null. The double literal 0.1 is already inexact, so the first captures that error; the string constructor is exact. A record is wrong for mutable entities with identity, for JPA entities needing proxies, or when you need inheritance.
</details>

**Output:** SF-02, SF-03 PRs; RL-03, RL-04 entries.

---

## Week 5 — Inheritance, interfaces, enums, sealed types
**Outcome:** knows when to compose, when to inherit, and how to model closed hierarchies.
**Before you start:** `Money` and `Product` merged.
**If stuck:** re-read JEP 409 examples; the compiler's exhaustiveness errors are the guide.

#### S1 Learn (3h)
1. (40m) Java Tutorials "Interfaces and Inheritance": overriding, `super`, abstract classes, default methods. Type the examples.
2. (30m) Read EJ Item 18 (favor composition over inheritance). Write the "fragile base class" problem from memory.
3. (20m) Read EJ Items 19 and 20. Note when an interface plus abstract skeleton is better.
4. (30m) Read EJ Items 34, 35, 36 (enums). Write two enum behaviours you could use for `OrderStatus`.
5. (30m) Read JEP 409 (sealed classes) and JEP 441 (pattern matching for switch). Write a sealed `Shape` with a pattern `switch` and see the compiler force exhaustiveness.
6. (30m) HFDP ch. 1 (Strategy intro). Summarise Strategy in 5 lines.

**Tips and common problems**
- `switch` not exhaustive → missing permitted subtype → add the case; avoid `default` so new subtypes break compilation.
- Calling overridable method in a constructor → subclass not initialised → avoid it (EJ Item 19).
- Interface default method conflicts → diamond inheritance → override and call `A.super.m()`.

**Done when**
- [ ] Sealed `Shape` demo; notes on composition vs inheritance

#### S2 Build (3h): OrderStatus and PaymentResult
1. (30m) Write tests for `OrderStatus` transitions: allowed (`NEW→PAID→SHIPPED`), forbidden (`SHIPPED→NEW`).
2. (40m) Implement `enum OrderStatus` with `EnumSet<OrderStatus> next()` and `canMoveTo(OrderStatus)`.
3. (30m) Write tests for `PaymentResult`: construction of `Approved(txId)`, `Declined(reason)`, `Pending(retryAfter)`.
4. (40m) Implement `sealed interface PaymentResult permits Approved, Declined, Pending` with records.
5. (30m) Add `String describe(PaymentResult r)` using a pattern `switch` with record deconstruction (verify syntax for your JDK).
6. (10m) Add a new permitted subtype temporarily and watch compilation fail where `describe` is not updated; revert.

**Tips and common problems**
- Using enum `ordinal()` in logic → breaks when reordered → use explicit fields (EJ Item 35).
- Records in `permits` must be in the same package/module unless nested → place them as nested types or same file.
- Forgetting `final` on non-sealed branches → compile error → mark records/final classes.

**Done when**
- [ ] Transition and pattern-match tests pass
- [ ] Compile-time exhaustiveness demonstrated

#### S3 Lab (2h): RL-05 and an LSP extra
1. (15m) RL-05 failing test:
```java
@Test void mutableKey() {
    var key = new StringBuilder("a");
    Map<StringBuilder,String> map = new HashMap<>();
    map.put(key, "v");
    key.append("b");
    assertThat(map.get(key)).isEqualTo("v"); // FAILS: null
}
```
(`StringBuilder` doesn't override `equals`, so use your own `MutableKey` class with `equals/hashCode` on a mutable field to show the real problem.)
2. (10m) Run; observe `null`.
3. (15m) Observe: print `key.hashCode()` before and after mutation; inspect `HashMap` bucket by debugger (field `table`).
4. (10m) Explain: the entry was stored in the bucket of the old hash; lookups use the new hash.
5. (10m) Fix: make the key immutable (record) and add the regression test.
6. (30m) Extra: write `Rectangle` and `Square extends Rectangle` with `setWidth` and a test `area == w*h` after `setWidth(5); setHeight(4)`. Watch it fail. Fix by making them separate immutable types behind a `Shape` interface.
7. (30m) Ledger entry RL-05 and a note for the LSP extra.

**Tips and common problems**
- Debugger can't see `table` → JDK internals hidden → use IntelliJ's "Evaluate Expression" or reflective peek (`--add-opens` needed; skip if blocked).
- Test accidentally passes because the key lands in the same bucket → rare; try several values.

**Done when**
- [ ] RL-05 red→green; LSP extra written
- [ ] Ledger entry committed

#### S4 Review + Write (2h)
1. (40m) Write "enum vs sealed vs interface" decision note with 3 examples each.
2. (30m) Refactor any `if/else` on type in your code to a `switch` pattern.
3. (20m) Rehearse RL-05 answer aloud.
4. (20m) 10 flashcards (sealed rules, Strategy).
5. (10m) Retro.

**Tips and common problems**
- Overengineering with sealed hierarchies → not every type needs it → use it for closed sets of cases only.

**Done when**
- [ ] Decision note, flashcards, retro committed

**Self-check:** Why prefer composition? What does `sealed` give you that an enum does not? How does the compiler help with exhaustiveness?
<details><summary>Answers</summary>

Inheritance couples subclass to implementation details of the parent; composition delegates through a stable interface. Sealed types let each case carry different data, while enum constants share one shape. A pattern `switch` over a sealed type without `default` must cover every permitted subtype or compilation fails.
</details>

**Output:** OrderStatus + PaymentResult with tests; RL-05 entry.

---

## Week 6 — Collections and generics
**Outcome:** picks the right collection with complexity reasoning and uses generics safely.
**Before you start:** JMH quickstart bookmarked (openjdk.org/projects/code-tools/jmh).
**If stuck:** the Javadoc table of each collection's complexity; EJ Items 28–31.

#### S1 Learn (3h)
1. (50m) Java Tutorials "Collections" trail: interfaces and implementations. Build a table of List/Set/Map/Queue/Deque with implementations and Big-O of add/get/remove/contains.
2. (20m) Write from memory the Big-O of `ArrayList.get`, `LinkedList.get`, `HashMap.get`, `TreeMap.get`, `PriorityQueue.poll`.
3. (30m) `Comparable` vs `Comparator`: implement both for `Product`; use `Comparator.comparing(...).thenComparing(...)`.
4. (30m) Java Tutorials "Generics": bounded types, wildcards. Read EJ Items 28, 29, 30, 31 (PECS).
5. (20m) Read EJ Items 54 (return empty collections) and 64 (refer by interface).
6. (30m) Type erasure: write `List<String>` and `List<Integer>` overloads that clash; read the compiler error.

**Tips and common problems**
- `List.of(...)` throws `UnsupportedOperationException` on add → immutable list → copy into `new ArrayList<>(...)`.
- `Arrays.asList` has fixed size → backed by array → same fix.
- `TreeMap`/`TreeSet` use `compareTo`, not `equals` → inconsistent comparators lose elements → keep them consistent.
- `PriorityQueue` iteration isn't sorted → only head is guaranteed → use `poll`.

**Done when**
- [ ] Collections table and PECS note in notes

#### S2 Build (3h)
1. (30m) Tests first for `Inventory` extension: `lowStock(threshold)` returns SKUs ascending by stock.
2. (30m) Replace the map with `Map<Sku,Integer>`; implement low-stock queue with `PriorityQueue<StockEntry>` and a `Comparator`.
3. (30m) Add `List<Product> sortedByPrice()` and `sortedByNameThenPrice()` using comparators.
4. (30m) Return unmodifiable views (`List.copyOf`) from public methods; test that callers can't mutate internals.
5. (20m) Tests for ordering ties and empty inventory.
6. (20m) Refactor to depend on interfaces (`Map`, `List`) in signatures.
7. (20m) PR with updates.

**Tips and common problems**
- Returning internal collection → leaks mutation → return copies/unmodifiable views.
- Comparator with subtraction `a-b` → overflow → use `Integer.compare`.
- Sorting nulls → NPE → use `Comparator.nullsFirst`.

**Done when**
- [ ] Ordering and encapsulation tests green

#### S3 Lab (2h): RL-08 and a benchmark
1. (15m) RL-08 failing test:
```java
@Test void cme() {
    var list = new ArrayList<>(List.of("a","b","c","d"));
    assertThatCode(() -> { for (String s : list) if (s.equals("b")) list.remove(s); })
        .doesNotThrowAnyException(); // FAILS: ConcurrentModificationException
}
```
2. (10m) Run; read the stack trace and locate `modCount` checks in `ArrayList.Itr`.
3. (10m) Explain: iterators detect structural modification via `modCount` and fail fast.
4. (15m) Fix with `list.removeIf(...)` and with `Iterator.remove()`; add tests.
5. (10m) Observe the quirk: removing the second-to-last element does *not* throw. Write a test showing it so you remember the bug can hide.
6. (40m) Extra: JMH benchmark of `ArrayList` vs `LinkedList` for iteration and `add(0, x)`. Use the JMH Maven archetype (verify current command) and run with 2 forks, 5 iterations.
7. (20m) Ledger entry RL-08 and record the benchmark results.

**Tips and common problems**
- JMH results noisy → laptop throttling/other apps → close apps, plug in, more iterations.
- Benchmark optimized away → dead code elimination → return the result or use `Blackhole`.

**Done when**
- [ ] RL-08 red→green; benchmark results saved

#### S4 Review + Write (2h)
1. (40m) One-page collections cheat sheet (when to use which, complexity).
2. (30m) Review generics usage in your code; remove raw types.
3. (20m) Rehearse RL-08 answer.
4. (20m) 10 flashcards.
5. (10m) Retro.

**Done when**
- [ ] Cheat sheet committed; flashcards created

**Self-check:** Why does `HashMap` degrade with bad `hashCode`? What is type erasure? When is `LinkedList` actually appropriate?
<details><summary>Answers</summary>

Many keys collide into the same bucket and lookups become linear (or tree-based O(log n) in newer JDKs for buckets with comparable keys). Generic type parameters are erased at compile time, leaving casts and no runtime type arguments. Rarely: mostly as a `Deque` when removing from both ends with an iterator in place; `ArrayDeque` is usually better.
</details>

**Output:** inventory updated; cheat sheet; RL-08 entry.

---

## Week 7 — Exceptions, I/O and strings
**Outcome:** designs an error-handling strategy and reads/writes files safely.
**Before you start:** Jackson dependency picked (check the version used by your Spring Boot target).
**If stuck:** EJ Items 69–77; JUnit `@TempDir` docs.

#### S1 Learn (3h)
1. (50m) Read EJ Items 69–77 in order; after each, write a one-line rule.
2. (20m) Read EJ Item 9 (try-with-resources). Write an example with two resources and explain close order and suppressed exceptions.
3. (35m) Java Tutorials "Essential Classes: Exceptions" — type the examples.
4. (35m) Java Tutorials "Basic I/O" and `java.nio.file` (`Path`, `Files.readString`, `Files.writeString`, `Files.newBufferedReader`).
5. (20m) Strings: immutability, `StringBuilder`, text blocks. Always pass `StandardCharsets.UTF_8` explicitly.
6. (20m) Write your draft error strategy: when checked, when unchecked, how errors reach the API.

**Tips and common problems**
- `FileNotFoundException` vs `NoSuchFileException` → java.io vs NIO → know both.
- Garbled characters → default charset assumptions → always specify UTF-8.
- Catching `Exception` hides bugs → too broad → catch the narrowest type.

**Done when**
- [ ] 9 rules written; draft strategy saved

#### S2 Build (3h): SF-04 persistence to file
1. (20m) Write tests first using `@TempDir`: save then load round-trips an inventory.
2. (40m) Add Jackson (`jackson-databind`); implement `InventoryRepository` with `save(Path)` and `load(Path)` (JSON).
3. (30m) Test missing file → throws `InventoryStorageException` (unchecked domain exception with cause and path).
4. (30m) Test corrupt JSON → same exception with a useful message.
5. (30m) Write atomically: write to `tmp` then `Files.move(..., ATOMIC_MOVE)` (verify filesystem support).
6. (20m) Make sure streams are closed via try-with-resources.
7. (10m) PR for SF-04.

**Tips and common problems**
- `@TempDir` not injected → JUnit 4 import → use `org.junit.jupiter.api.io.TempDir`.
- Jackson can't deserialize records/immutables → missing module/constructor info → verify configuration for your Jackson version.
- Exceptions lose root cause → not passed to constructor → always pass `cause`.

**Done when**
- [ ] Round-trip, missing and corrupt-file tests green

#### S3 Lab (2h): RL-09 and a leak extra
1. (15m) RL-09 failing test: a method catches `Exception` and returns `Optional.empty()`. Assert the logged cause is retained:
```java
try { Files.readString(Path.of("nope")); } catch (Exception e) { log.error("failed"); return null; }
```
Write a test using a log-capturing appender (Logback `ListAppender`) asserting the stack trace exists; it FAILS.
2. (10m) Run; observe the log lacks the exception.
3. (10m) Explain: swallowing or logging without the throwable destroys the cause and stack.
4. (15m) Fix: wrap `throw new InventoryStorageException(path, e)` and `log.error("...", e)`.
5. (20m) Regression test verifies the cause chain via `getCause()`.
6. (30m) Extra: a test `TrackingInputStream` whose `close()` flips a flag; show a leak when forgetting to close, then fix with try-with-resources.
7. (20m) Ledger entry RL-09.

**Tips and common problems**
- Logging the message string only (`e.getMessage()`) → stack lost → pass the exception as last argument.
- Logging and rethrowing → duplicate noise → log once at the boundary.

**Done when**
- [ ] RL-09 and leak extra red→green; ledger entry committed

#### S4 Review + Write (2h)
1. (50m) Write ADR-002 "Error handling strategy" (checked vs unchecked, translation at boundaries, how errors map to HTTP later).
2. (25m) Review SF-04 PR; check for broad catches.
3. (20m) Rehearse RL-09 answer.
4. (15m) 10 flashcards.
5. (10m) Retro.

**Done when**
- [ ] ADR-002 merged

**Self-check:** When should an exception be checked? What does try-with-resources guarantee? Why is `catch (Exception e) {}` dangerous?
<details><summary>Answers</summary>

When the caller can reasonably recover and you want to force them to consider it; most failures are better unchecked. Resources are closed in reverse order even on exceptions, with close failures added as suppressed. It hides bugs and failures, removing the cause and leaving the system in an unknown state.
</details>

**Output:** SF-04, ADR-002, RL-09 entry.

---

## Week 8 — Time, money and thread-safety basics
**Outcome:** can't be bitten by dates, zones, or shared formatters.
**Before you start:** `Money` and persistence exist.
**If stuck:** Java Tutorials "Date Time"; search "falsehoods programmers believe about time".

#### S1 Learn (3h)
1. (50m) Java Tutorials "Date Time": `Instant`, `LocalDate`, `LocalDateTime`, `ZonedDateTime`, `Duration`, `Period`. Type the examples.
2. (30m) Read "Falsehoods programmers believe about time" (search the title) and list five.
3. (30m) Study `Clock`: `Clock.systemUTC()`, `Clock.fixed(...)`. Write why injecting `Clock` helps tests.
4. (30m) DST: create a `ZonedDateTime` of `2025-03-09T02:30` in `America/New_York` (a gap) and `2025-11-02T01:30` (an overlap). Print results.
5. (20m) Re-read EJ Item 17: why immutability gives thread safety.
6. (20m) Formatting: `DateTimeFormatter.ISO_INSTANT`, locale-aware formats.

**Tips and common problems**
- `LocalDateTime` used for events → no zone/offset → store `Instant` or `OffsetDateTime`.
- `Date`/`Calendar` appear in old code → legacy API → convert at the edge with `toInstant()`.
- DST gap silently shifted → resolved by the zone rules → test the edge explicitly.

**Done when**
- [ ] DST gap and overlap results explained in notes

#### S2 Build (3h): SF-05 order timestamps
1. (20m) Write tests first with `Clock.fixed`: order `placedAt` equals the clock's instant.
2. (30m) Add `Instant placedAt` to `Order`; inject `Clock` in the service.
3. (40m) Add `String displayFor(Order, ZoneId)` formatting in the user zone.
4. (40m) Test DST: orders placed at `2025-03-09T06:59:59Z` and `07:00:00Z` for `America/New_York` show 01:59:59 and 03:00:00.
5. (20m) Test a "delivered within 24h" rule across a DST change: use `Duration`, not `Period` or days.
6. (20m) Persist `placedAt` as ISO-8601 UTC in the JSON file.
7. (10m) PR for SF-05.

**Tips and common problems**
- Tests depend on the current time → flaky → inject a clock everywhere.
- Using `LocalDate.now()` in code → hidden dependency → pass `Clock`.

**Done when**
- [ ] DST tests green; no `now()` calls without a clock

#### S3 Lab (2h): RL-06, RL-07
1. (15m) RL-06: write a main that prints `ZoneId.systemDefault()`, `Charset.defaultCharset()` and a formatted `LocalDateTime.now()`. Run with `-Duser.timezone=America/New_York`, then `-Duser.timezone=Asia/Tokyo`.
2. (15m) Observe the differences. Also try `-Dfile.encoding=ISO-8859-1` and write a UTF-8 text with accents; note JDK 18+ defaults to UTF-8, so the old default is only reproducible by setting the flag (verify against current docs).
3. (10m) Explain: default zone and charset come from the JVM environment, making behaviour machine-dependent.
4. (10m) Fix: never rely on defaults; pass `ZoneId` and charset explicitly; add a test failing under a different `-Duser.timezone` that now passes.
5. (30m) RL-07: failing test with shared `SimpleDateFormat`:
```java
var f = new SimpleDateFormat("yyyy-MM-dd");
var pool = Executors.newFixedThreadPool(16);
// submit 10_000 tasks calling f.parse("2025-03-09"); collect exceptions/wrong results
```
6. (15m) Run; observe `NumberFormatException` or wrong dates intermittently.
7. (10m) Explain: the formatter keeps mutable internal state (calendar), shared across threads.
8. (15m) Fix with immutable `DateTimeFormatter`; add the regression test. Ledger entries RL-06, RL-07.

**Tips and common problems**
- Can't reproduce the race → low thread count → increase tasks and threads, use a `CountDownLatch` start gate.
- `-D` flags not applied in IntelliJ → wrong run config → add VM options in the run configuration.

**Done when**
- [ ] Both labs red→green; ledger entries committed

#### S4 Review + Write (2h)
1. (50m) Write `docs/time-rules.md` (UTC at rest, ISO-8601 at API, never `LocalDateTime` for events, inject `Clock`).
2. (25m) Grep the codebase for `LocalDateTime`, `new Date()`, `now()`; fix or justify.
3. (20m) Rehearse RL-06 and RL-07 answers.
4. (15m) 10 flashcards.
5. (10m) Retro.

**Done when**
- [ ] Time rules doc merged; grep clean

**Self-check:** Which type represents an instant on the timeline? Why inject a `Clock`? What breaks at a DST boundary?
<details><summary>Answers</summary>

`Instant` (or `OffsetDateTime`/`ZonedDateTime`). It makes time deterministic in tests. Local times may not exist (gap) or occur twice (overlap), and "one day" is not always 24 hours.
</details>

**Output:** SF-05, "time rules" doc, RL-06/RL-07 entries.

---

## Week 9 — Consolidation and Phase 1 gate
**Outcome:** passes the gate review.
**Before you start:** all Phase 1 PRs merged.
**If stuck:** SLF4J manual "Getting started"; IntelliJ debugger docs for breakpoints.

#### S1 Learn (3h): logging, debugging, Maven lifecycle
1. (40m) SLF4J manual: API, parameterised messages, levels. Logback manual (configuration): create `logback.xml` with console appender and pattern including `%X{requestId}`.
2. (30m) Debugger: set a breakpoint, step over/into/out, conditional breakpoint (`sku.equals("SKU-9")`), evaluate expression, watch.
3. (40m) Maven "Introduction to the Build Lifecycle": phases (`validate`…`deploy`) and plugin goals bound to them; run `mvn help:effective-pom` and read a section.
4. (30m) Dependency management: `mvn dependency:tree`, scopes, `dependencyManagement`.
5. (20m) Google Java Style Guide skim: naming, imports.
6. (20m) Write from memory the order of Maven phases from `validate` to `install`.

**Tips and common problems**
- `SLF4J: No SLF4J providers were found` → only the API is on the classpath → add `logback-classic`.
- Duplicate logging bindings warning → two backends → exclude one.
- Breakpoints ignored → running without debug → use the Debug button, not Run.

**Done when**
- [ ] `logback.xml` works; conditional breakpoint used once

#### S2 Build (3h): gate prep
1. (30m) Add JaCoCo plugin (`prepare-agent` and `report`) and read the HTML report under `target/site/jacoco`.
2. (40m) Find uncovered lines; write meaningful tests for behaviour, not trivial getters.
3. (30m) Add Spotless (or Checkstyle) with Google style; run `mvn spotless:apply`.
4. (30m) Add SpotBugs; run and triage findings (fix or document false positives).
5. (30m) Self-audit against the gate: grep for `double` in money code, `LocalDateTime`, empty catch blocks.
6. (20m) Add logging at meaningful points (parameterised, no secrets).

**Tips and common problems**
- JaCoCo shows 0% → agent not attached (surefire argLine overwritten) → ensure `@{argLine}` is used if you set one.
- Spotless fails CI-style → formatting drift → run `apply` before commit.
- Chasing 100% coverage → vanity → aim for meaningful tests, ≥80% as a floor.

**Done when**
- [ ] Coverage ≥80% with meaningful tests, formatter and SpotBugs clean

#### S3 Lab (2h): RL-25 and a debugger extra
1. (15m) RL-25 failing test: log a request containing `Authorization: Bearer abc123`; assert captured logs don't contain `abc123` (use a `ListAppender`). It FAILS.
2. (10m) Run; observe the token in the output.
3. (10m) Explain: logs are plain text sinks, often shipped widely; secrets and PII must never enter them.
4. (25m) Fix: add a `redact(String)` helper masking tokens and sensitive keys; apply at logging call sites; add a regression test.
5. (10m) Read the OWASP Logging Cheat Sheet; list what not to log.
6. (35m) Extra: find a seeded bug using only a debugger: add a deliberate off-by-one in `lowStock`, then find it with a conditional breakpoint without adding prints.
7. (15m) Ledger entry RL-25.

**Tips and common problems**
- Redaction regex misses variants → incomplete patterns → combine key-based and pattern-based redaction.
- Debugging changes timing of concurrent code → heisenbugs → use logs for those.

**Done when**
- [ ] RL-25 red→green; debugger extra done; ledger entry committed

#### S4 Review + Write (2h): gate review
1. (50m) Gate review with mentor/peer: they read the `inventory-core` code against EJ items and ask "is any of this TypeScript in Java?". If no reviewer: record yourself walking through the code, then review the recording.
2. (30m) Fix review findings.
3. (15m) Update progress tracker in `java-backend-roadmap.md`; tag `phase-1`.
4. (15m) Retro and a plan for Phase 2.
5. (10m) Count ledger entries and rehearse three aloud.

**Gate rubric (pass all):**
- No `double` for money, no `LocalDateTime` for instants, no swallowed exceptions.
- Every public value type has a correct `equals/hashCode/toString`.
- Tests are meaningful (behaviour, not coverage padding).
- She can explain every ledger entry in under one minute.

**Done when**
- [ ] Gate rubric all ticked; tag pushed

**Self-check:** Which Maven phase runs unit tests? Why is `log.info("x=" + x)` worse than `log.info("x={}", x)`? What must never appear in logs?
<details><summary>Answers</summary>

`test` (via surefire). The parameterised form avoids building the string when the level is disabled and keeps messages structured. Secrets, tokens, passwords, and unnecessary PII.
</details>

**Output:** Phase 1 tag in git, gate notes, 9–10 ledger entries.
