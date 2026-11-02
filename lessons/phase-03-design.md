# Phase 3: OOD, Patterns, FP and Clean Architecture (Weeks 16–20)

Goal: design code that survives change; recognize patterns in frameworks and in her own code. The catalog has no items tagged for this phase, so S3 uses **extra** design labs. Log them in `DESIGN_LAB.md` in the `reality-lab` repo (same discipline as the ledger: what you did, what you observed, what you learned).

Format reminder: numbered, time-boxed steps; **Done when** is the only definition of finished; use the "Stuck? The 15-minute routine" in `README.md` when a step runs over by 2x.

---

## Week 16 — SOLID, coupling and cohesion
**Outcome:** can name the design problem in a piece of code and justify a fix.
**Before you start:** Phase 2 tagged; ShopFlow has at least 15 commits of history; Mermaid or PlantUML available in your editor.

### S1 Learn (3h): principles, smells, diagrams
1. (40m) Read CA Part III chapters on SRP and OCP. After each, close the book and write the principle in one sentence plus a Java example from your own Phase 1–2 code.
2. (30m) Read the CA chapters on LSP, ISP and DIP. Write one violation example for each.
3. (25m) Read Martin Fowler's bliki entry on "Tell Don't Ask" and any short piece on the Law of Demeter. Write: what is coupling, what is cohesion, one example of each in ShopFlow.
4. (35m) Read Refactoring ch. 3 "Bad Smells in Code". As you read, tick which smells exist in your code (Long Method, Large Class, Feature Envy, Primitive Obsession, Data Clumps, Switch Statements).
5. (25m) Read Refactoring.guru "Code Smells" for concrete examples of 5 smells; open the Java snippets.
6. (25m) Read PlantUML or Mermaid docs for class and sequence diagrams. Draw a class diagram of `Order` and `OrderItem` and a sequence diagram of "place order" in Mermaid.

**Tips and common problems**
- Principles feel abstract → always attach each to a concrete code smell: SRP ↔ "this class changes for two reasons", OCP ↔ "adding a case means editing a switch", LSP ↔ "a subclass throws or ignores the parent's contract".
- Don't memorise definitions; practise pointing at code.

**Done when**
- [ ] Five SOLID sentences with your own examples written without looking.
- [ ] A smell checklist with ShopFlow examples started.
- [ ] Two Mermaid diagrams render.

### S2 Build (3h): audit and fix smells in ShopFlow
1. (30m) Audit the Orders module. Use IntelliJ **Analyze → Inspect Code** and `wc -l` to find long files. Create `docs/smell-audit.md` listing file, smell, why it matters, proposed refactoring.
2. (20m) Create 10 GitHub issues from the audit (title "Smell: …", location, proposed fix, risk, test needed).
3. (30m) Pick the 3 easiest. For each, write a **characterization test** that asserts current behaviour and goes green before touching the code.
4. (60m) Refactor the 3 smells in small commits using IDE refactorings (Extract Method, Introduce Parameter Object, Replace Magic Number with Constant, Move Method). Run the tests after every commit.
5. (25m) Write `docs/orders-class-diagram.md` with the post-refactor class diagram (Mermaid).
6. (15m) PR with before/after notes.

**Tips and common problems**
- "I can't test this" → the code does too much; extract the logic into a pure method first, then test it.
- Commits mix refactoring and behaviour changes → split them; a refactoring commit must not change behaviour.
- Overreach (fixing 10 things) → time-box; log the rest as issues.
- IDE rename/extract breaks reflection-based bits (JPA field names, JSON) → run the integration tests after moves.

**Done when**
- [ ] 10 smell issues exist and 3 are fixed with tests.
- [ ] All tests green after every commit (`git rebase -x ./mvnw test` or manual checks).
- [ ] Class diagram committed.

### S3 Lab (2h): extra labs — hotspots (SRP signal) and an OCP violation
1. (10m) **Hotspots.** In the ShopFlow repo run:
   `git log --since="6 months ago" --name-only --pretty=format: | grep -v '^$' | sort | uniq -c | sort -rn | head -20`
   Note the most-changed files.
2. (20m) **Co-change.** Save and run this with `python3 -I cochange.py` (kept outside the repo):
   ```python
   import subprocess, itertools, collections
   out = subprocess.run(["git","log","--name-only","--pretty=format:--"],capture_output=True,text=True).stdout
   pairs = collections.Counter()
   for commit in out.split("--"):
       files = sorted({f for f in commit.splitlines() if f.strip().endswith(".java")})
       if len(files) <= 12:
           pairs.update(itertools.combinations(files, 2))
   for (a,b),n in pairs.most_common(10): print(n, a, b)
   ```
   Run it from the repo root (the `-I` flag avoids importing stray local modules).
3. (15m) Interpret: files that always change together but live far apart signal coupling; one file that changes for many reasons signals an SRP violation. Write 3 hypotheses in `DESIGN_LAB.md`.
4. (15m) **OCP failing test first.** Create `DiscountCalculator.apply(String type, Money amount)` with a `switch` on `"STUDENT"`, `"VIP"`. Write a test for a new type `"BLACK_FRIDAY"` expecting a 25% discount **without modifying `DiscountCalculator`**, by registering a new `Discount` object. It cannot compile or pass: that is the OCP violation.
5. (25m) Refactor to `interface Discount { boolean supports(String type); Money apply(Money m); }` and `DiscountCalculator(List<Discount>)`. Add `BlackFridayDiscount` as a new class; the calculator is untouched. Test is green.
6. (15m) Regression tests for existing types; add a test proving unknown types throw a clear exception.
7. (20m) Write the lab log: what the hotspot data told you, what the OCP refactor cost and bought.

**Tips and common problems**
- Co-change script prints nothing → repository too young; lower the date filter or run it on any bigger open-source repo you cloned (in a separate directory).
- Huge commits dominate pairs → the script ignores commits touching more than 12 Java files.
- Don't treat hotspots as proof; they are hints to investigate.

**Done when**
- [ ] You identified at least one hotspot and wrote a hypothesis for why it changes so often.
- [ ] The OCP test failed (compile error counts) before the refactor and passes after.
- [ ] `DESIGN_LAB.md` has both entries.

### S4 Review + Write (2h)
1. (50m) Write one paragraph per SOLID principle (10m each) in your own words with a ShopFlow example. Read aloud to check it makes sense.
2. (25m) Create 15 flashcards: smell → symptom → typical refactoring.
3. (20m) Review the diffs of your 3 refactors; are the commit messages specific?
4. (15m) Retro.
5. (10m) Answer the self-check.

**Self-check:** Which principle does a large `switch` on type usually violate? What is feature envy? How does DIP relate to dependency injection?
<details><summary>Answers</summary>

- Open/Closed (adding a type means editing the switch); often also a missed use of polymorphism.
- A method that uses another class's data more than its own; move it to that class.
- DIP is the principle (depend on abstractions, with high-level policy owning the interface); dependency injection is a technique for supplying the concrete implementation.
</details>

**Output:** smell tickets, three refactors, class diagram.
**If stuck:** reduce the smell to the smallest code example and describe it in one sentence; if you can't, you haven't understood the smell yet.

---

## Week 17 — Design patterns in real use
**Outcome:** applies Strategy, State, Adapter and friends where they pay off, and recognizes them in Spring.
**Before you start:** SF-13, SF-14, SF-15 tickets created; ADR-006 template ready.

### S1 Learn (3h): the working set of patterns
1. (30m) Read HFDP chapters Strategy and Observer (match by title). For each, redraw the class diagram from memory and map it to a ShopFlow need (pricing, order events).
2. (30m) Read HFDP chapters Decorator and Factory (Factory Method/Abstract Factory). Note where Java's `InputStream` wrappers and Spring factories use them.
3. (25m) Read HFDP chapters Adapter and Template Method. Think of a payment provider SDK for Adapter and a base class workflow for Template.
4. (25m) Read HFDP chapters State and Proxy. Write the difference between State and Strategy in two sentences.
5. (25m) Skim Refactoring.guru "Design Patterns": Builder, Singleton, Command, Facade. Mark the one you've already used in Phase 1–2.
6. (25m) Hunt in Spring source (GitHub): `JdbcTemplate` (Template), `SimpleApplicationEventMulticaster` (Observer), AOP `ProxyFactory`/CGLIB (Proxy), `HandlerAdapter` (Adapter). Write class names and the pattern for each.
7. (20m) Write five signs a pattern is overkill (single implementation, no variation expected, adds layers without tests...).

**Tips and common problems**
- Pattern-itis → start from a smell or a variation point, never from "let's use Strategy".
- HFDP examples feel dated → focus on the structure, not the domain.

**Done when**
- [ ] State vs Strategy explained in two sentences.
- [ ] Table "pattern → where I saw it in Spring" with 4 rows.

### S2 Build (3h): SF-13 pricing, SF-14 order state, SF-15 payment adapter
1. (15m) Sketch interfaces on paper: `PricingRule`, `OrderState`, `PaymentGateway`.
2. (50m) **SF-13 test first:** parameterized tests for `PercentageDiscount`, `FixedDiscount`, `BuyXGetYFree` applied to a cart, expected totals as `Money`. Implement `interface PricingRule { Money apply(Cart cart); }` and `PricingEngine(List<PricingRule>)` that applies rules in a defined order.
3. (45m) **SF-14:** tests for every allowed transition (CREATED→PAID→SHIPPED→DELIVERED, CREATED/PAID→CANCELLED) and illegal ones throwing a domain exception. Implement State (enum with behaviour or sealed interface); `Order.pay()` delegates to the state.
4. (40m) **SF-15:** `PaymentGateway` port (interface, in the application/domain layer) returning the sealed `PaymentResult` from Phase 1; `FakePaymentGateway` adapter, deterministic (e.g., declines amounts ending in `.13`). Wire into `OrderService`.
5. (20m) Spring wiring (`List<PricingRule>` injection, `@Order` for rule order), then run an integration test.
6. (10m) One PR per ticket.

**Tips and common problems**
- Injecting `List<PricingRule>` gives an unexpected order → add `@Order` or an explicit ordered list.
- Persisting State objects with JPA is awkward → persist the status as an enum/string and map to behaviour in the domain.
- Fake gateway uses randomness → tests flake; make it deterministic.
- State machine with `if` ladders sneaks back → keep transitions in one place (the state objects).

**Done when**
- [ ] Each strategy and each transition has a test; illegal transitions are rejected.
- [ ] Adding a new pricing rule requires only a new class (verify by adding one).
- [ ] Payment fake works through the port; no vendor types in the domain.

### S3 Lab (2h): extra labs — remove an if/else chain two ways; read Observer in Spring
1. (10m) **Setup.** Write `ShippingCostCalculator.cost(Region, Order)` with a 6-branch `if/else` chain and a table-driven test covering all branches (characterization).
2. (25m) **Version A:** lookup map: `Map<Region, Function<Order, Money>>` (use `EnumMap`). Tests stay green.
3. (25m) **Version B:** `interface ShippingStrategy` with one class per region; resolved via a `Map<Region, ShippingStrategy>`.
4. (15m) Compare in a table: lines of code, effort to add a region, testability, readability. Decide and note when each wins (map for tiny logic, classes when logic grows or needs dependencies).
5. (10m) **Observer in a framework.** Open `SimpleApplicationEventMulticaster` in Spring's source (via the IDE: navigate from `ApplicationEventPublisher`).
6. (25m) Read `multicastEvent`: how listeners are found (`getApplicationListeners`), how they're invoked (sync by default, executor optional), how errors are handled. Write a 10-line summary.
7. (10m) Write a 20-line hand-made `EventBus` (`subscribe`, `publish`) with a test.

**Tips and common problems**
- Both versions look equal in a tiny example → that is the finding; record when you'd choose classes.
- Source navigation is overwhelming → set a breakpoint in `multicastEvent` and step through one `publishEvent` call.

**Done when**
- [ ] Both refactors keep all characterization tests green.
- [ ] Comparison table written with a decision rule.
- [ ] Observer summary written from reading the source.

### S4 Review + Write (2h)
1. (45m) Write ADR-006 "Pricing rules are strategies; new rule = new class only" (context, decision, consequences, alternatives: map of functions, switch).
2. (45m) Build a one-page cheat sheet: pattern, problem it solves, ShopFlow example, Spring example, smell that hints you need it.
3. (15m) Flashcards (pattern ↔ problem).
4. (15m) Retro and self-check.

**Self-check:** When is State better than an enum + switch? Why is a Spring singleton bean not the Singleton pattern? What does Adapter buy you in testing?
<details><summary>Answers</summary>

- When behaviour differs per state and transition rules grow; it keeps rules in one place instead of scattered switches. An enum with per-constant behaviour is a good middle ground.
- A Spring singleton is one instance per container, injected; the Singleton pattern enforces one instance via a global access point and private constructor.
- It isolates a vendor/external API behind your own interface so tests use a fake without network access, and a vendor change touches one class.
</details>

**Output:** SF-13, SF-14, SF-15, ADR-006.
**If stuck:** write the failing test for the next rule/transition first; the test shows the interface you need.

---

## Week 18 — Functional programming in Java
**Outcome:** uses streams, `Optional` and immutability well; models errors as values where it helps.
**Before you start:** Phase 1 `Money` record available; decide whether to add jqwik (property tests) or use parameterized tests.

### S1 Learn (3h): lambdas, streams, Optional, errors as values
1. (30m) Read EJ Items 42, 43, 44 (lambdas over anonymous classes, method references, standard functional interfaces). Write one example of each from ShopFlow.
2. (35m) Read EJ Items 45–48 (streams judiciously, side-effect-free functions, collections as return types, parallel streams). Write the rule of thumb for when to prefer a loop.
3. (20m) Read EJ Item 55 (`Optional` judiciously). Write 4 rules for using it.
4. (35m) Work through *Modern Java in Action* chapters on streams and collectors (`map/filter/reduce`, `groupingBy`, `partitioningBy`). Code every example in a scratch project.
5. (25m) Translate one TS pipeline you wrote (`map/filter/reduce`) to Java streams. List differences (laziness, one-shot, checked exceptions, no async).
6. (20m) Read about sealed `Result<T,E>` (Ok/Err) and skim Vavr `Either`/`Try` docs for comparison. Note Java's FP limits (no TCO, checked exceptions in lambdas).
7. (15m) Recall: what makes a function pure; why immutability helps.

**Tips and common problems**
- "Effectively final" compile error in lambdas → you mutate a captured variable → use a reduction or collector instead.
- `Collectors.toMap` throws `IllegalStateException: Duplicate key` → provide a merge function.
- Stream reused → "stream has already been operated upon or closed" → create a new stream.
- Checked exceptions in lambdas don't compile → wrap in a helper or restructure with `Result`.

**Done when**
- [ ] Optional rules and stream-vs-loop rule written.
- [ ] TS vs Java stream differences list.

### S2 Build (3h): pure pricing core and Result
1. (20m) List impure code in order pricing (uses `Instant.now()`, mutates entities, static state). Mark what to isolate.
2. (40m) **Test first:** table-driven tests for `subtotal(List<Line>)`, `tax(Money, TaxRate)`, `discount(Money, Rule)`.
3. (30m) Implement them as pure functions (static or small records), compose with `Function.andThen`/`compose`; the service just wires inputs.
4. (35m) Create `sealed interface Result<T,E> permits Ok, Err` with `map`, `flatMap`, `fold`. Use it in `OrderValidator.validate(cmd)` returning `Result<ValidOrder, List<Violation>>`; test both branches.
5. (30m) Sales report with `Collectors.groupingBy(Category, Collectors.reducing(Money.ZERO, Line::total, Money::add))`.
6. (20m) Add property tests with jqwik (verify dependency and syntax in its user guide) or parameterized tests: total is never negative; discount never exceeds subtotal; splitting an order doesn't change the sum.
7. (5m) PR.

**Tips and common problems**
- `reduce` with an identity that isn't neutral gives wrong totals → use `Money.ZERO` that really is neutral.
- `groupingBy` returns a `HashMap` with no stable order → sort before comparing in tests or use `TreeMap`.
- `Result` leaks everywhere → keep it in the domain/application; translate to exceptions/HTTP at the boundary (ADR-007).
- Property test failures show huge inputs → use shrinking output and add the minimal case as a regular test.

**Done when**
- [ ] Pricing functions are pure and tested; no clock or static state inside.
- [ ] `Result` is used by one validator with tests for ok and error.
- [ ] The report returns correct totals per category.

### S3 Lab (2h): extra labs — parallel stream side effects; Optional misuse
1. (15m) **Failing test first.**
   ```java
   @RepeatedTest(50)
   void collectsAll() {
       List<Integer> out = new ArrayList<>();
       IntStream.range(0, 10_000).parallel().forEach(out::add); // BUG
       assertThat(out).hasSize(10_000);
   }
   ```
2. (10m) Run: expect wrong sizes and sometimes `ArrayIndexOutOfBoundsException`.
3. (10m) Explain in 3 sentences: `ArrayList` isn't thread-safe; `forEach` on a parallel stream runs on multiple threads; side-effects on shared state race.
4. (15m) Fix with `.boxed().toList()` / `collect(Collectors.toList())`; also try `forEachOrdered` and note the cost. Keep the repeated test as regression.
5. (10m) **Optional misuse.** Write a class with: an `Optional` field, an `Optional` parameter, `opt.get()` without check, `Optional.of(null)`, and a method returning `null` for `Optional`.
6. (25m) Write tests showing `NoSuchElementException`/`NullPointerException`; fix each (return `Optional` only from methods, use `orElseThrow`, `Optional.ofNullable`). Enable IntelliJ inspections "Optional used as field or parameter type" and check they flag the bad code.
7. (15m) Write a review checklist for streams/Optional (8 bullets).
8. (20m) Record both labs in `DESIGN_LAB.md`.

**Tips and common problems**
- The parallel test sometimes passes → that is exactly why it's dangerous; repeat it.
- Don't "fix" with `Collections.synchronizedList`; the real issue is the side-effecting design.

**Done when**
- [ ] The parallel test fails repeatedly before the fix and passes 50/50 after.
- [ ] All five Optional smells reproduced and fixed.
- [ ] A review checklist exists.

### S4 Review + Write (2h)
1. (50m) Write "FP in Java: what I'd use, what I'd avoid", comparing to your TS habits (immutability, array methods, Promises vs `CompletableFuture`, error handling).
2. (45m) Write ADR-007 "Error model: exceptions at boundaries, Result inside the domain" with consequences (mapping to HTTP via ProblemDetail).
3. (15m) Flashcards (purity, Optional rules, collectors).
4. (10m) Retro and self-check.

**Self-check:** What makes a function pure? Why is `Optional` not for fields? When do streams hurt readability?
<details><summary>Answers</summary>

- Same inputs always give the same output and there are no side effects (no I/O, mutation, clock, randomness).
- `Optional` is designed as a return type; as a field it isn't serializable, adds an object per field and brings no benefit over a clear null contract; as a parameter it complicates callers.
- Deeply nested collectors, side-effecting lambdas, checked exceptions, or when a simple loop is shorter and easier to debug.
</details>

**Output:** refactored domain calculations, `Result` type, ADR-007.
**If stuck:** write the same thing as a for-loop first, then convert; if the stream version is harder to read, keep the loop.

---

## Week 19 — Hexagonal architecture and OOD katas
**Outcome:** the domain is independent of Spring/JPA; can solve a classic OOD prompt.
**Before you start:** SF-13–15 merged; add the ArchUnit dependency (`com.tngtech.archunit:archunit-junit5`, verify against current docs); clean working tree.

### S1 Learn (3h): ports, adapters, dependency rule
1. (35m) Read CA Part V (Architecture): the chapters on boundaries and the dependency rule. Write the rule in one sentence and draw concentric circles with ShopFlow parts.
2. (25m) Read Alistair Cockburn, "Hexagonal Architecture". List driving (inbound) vs driven (outbound) ports for ShopFlow.
3. (35m) Read the Hombergs book chapters on ports/adapters in Spring and use-case classes. Note how it handles entities vs JPA entities.
4. (25m) Read Fowler's bliki "AnemicDomainModel". Decide where ShopFlow logic currently lives and what is anemic.
5. (30m) Read the ArchUnit user guide: `noClasses().that().resideInAPackage(...)`, `layeredArchitecture()`. Write one rule on paper.
6. (30m) Design the target package layout (`order.domain`, `order.application` with `port.in/port.out`, `order.adapters.in.web`, `order.adapters.out.persistence`) and list classes to move.

**Tips and common problems**
- Hexagonal can become ceremony → apply it to the Order aggregate first; decide for other modules in ADR-008.
- Package-by-layer vs package-by-feature: prefer feature at the top level, layers inside.

**Done when**
- [ ] Dependency rule and port lists written.
- [ ] Target package layout and move list drafted.

### S2 Build (3h): SF-16 domain events, SF-17 hexagonal refactor
1. (20m) Create branch `sf-17-hexagonal`. Move packages with IntelliJ **Refactor → Move** (it updates imports).
2. (40m) **SF-16 test first:** placing an order publishes `OrderPlaced(orderId, total, at)` and `LoyaltyPointsListener` reacts. Aggregate collects events (`List<DomainEvent>` + `pullEvents()`); the use case publishes them through a port; Spring adapter uses `ApplicationEventPublisher`. Consider `@TransactionalEventListener(phase = AFTER_COMMIT)` for listeners.
3. (50m) **SF-17:** plain Java `Order` aggregate in `domain`; `OrderJpaEntity` + mapper in `adapters.out.persistence`; `OrderRepository` as an outbound port in `application`. Keep the HTTP adapter calling a use-case interface.
4. (30m) ArchUnit tests: domain must not depend on `org.springframework..` or `jakarta.persistence..`; adapters must not depend on each other; application must not depend on adapters.
5. (25m) Fix violations until the ArchUnit tests are green (expect to introduce a few small interfaces).
6. (15m) PR with a before/after package tree in the description.

**Tips and common problems**
- Component scan or `@EntityScan` stops finding classes after moving packages → adjust scan base packages.
- Duplicate mapping code feels wasteful → that is the cost recorded in ADR-008; keep mappers small and tested.
- `@Transactional` on the use case class means Spring in the application layer → accept it consciously (document) or wrap with your own annotation/port.
- ArchUnit `no classes found` → wrong package string or imports not scanned → use `@AnalyzeClasses(packages = "io.shopflow")`.
- `@TransactionalEventListener` doesn't fire → no active transaction when the event is published.

**Done when**
- [ ] `OrderPlaced` event is published and handled; tested.
- [ ] Domain package has no Spring/JPA imports; ArchUnit tests prove it.
- [ ] All previous tests still green.

### S3 Lab (2h): extra lab — parking lot OOD kata
1. (15m) Write requirements and 8 clarifying questions (levels, spot types, vehicle types, entry/exit, tickets, fees, payment, capacity).
2. (20m) CRC cards (class, responsibility, collaborators) from nouns/verbs.
3. (20m) Mermaid class diagram; mark where Strategy (fee calculation) and Factory (spot creation) fit.
4. (15m) Mermaid sequence diagram: vehicle enters → ticket issued → exits → fee paid.
5. (40m) TDD skeleton in `katas/parking-lot`: park returns a `Ticket`; full lot rejects; exit calculates fee by hours; different spot sizes. Red-green-refactor.
6. (10m) Self-critique against SOLID; note one thing you'd do differently.

**Tips and common problems**
- You jump into code → resist: the design conversation (requirements, entities) is the exercise.
- Over-modelling → keep to what the requirements need.
- Time check: if the diagram takes more than 20 minutes, simplify.

**Done when**
- [ ] Diagrams and tests exist; at least 5 behaviours covered by tests.
- [ ] A short critique is written in `DESIGN_LAB.md`.

### S4 Review + Write (2h)
1. (50m) Write ADR-008 "Hexagonal structure; costs and benefits". Record the cost honestly: more mapping, more classes, indirection.
2. (25m) Update the README architecture section with the package diagram.
3. (20m) Flashcards: dependency rule, ports vs adapters, anemic vs rich.
4. (15m) Retro and self-check.
5. (10m) Tidy the kata repo structure.

**Self-check:** What is the dependency rule? What does hexagonal cost you? How would you swap Postgres for another store?
<details><summary>Answers</summary>

- Source-code dependencies point inward, toward the domain; inner layers know nothing of outer ones.
- Extra classes and mapping code, indirection, and the discipline to keep boundaries; benefits show with change and testing.
- Write a new persistence adapter implementing the same outbound port and contract-test it; domain and use cases remain unchanged.
</details>

**Output:** SF-16, SF-17, ArchUnit rules, ADR-008, parking-lot design.
**If stuck:** start from the ArchUnit failure message; it names the exact illegal dependency to cut.

---

## Week 20 — Consolidation and Phase 3 gate
**Outcome:** passes the refactoring gate.
**Before you start:** schedule a peer/mentor for S4; clone Emily Bache's Gilded Rose Refactoring Kata (Java) into a separate directory.

### S1 Learn (3h): refactoring technique and golden master
1. (40m) Read Refactoring ch. 1 (the opening example). Note each small step and the test run between them.
2. (30m) Read Refactoring ch. 2–4 (principles, bad smells recap, building tests). Write the rules: refactoring never changes behaviour; test before; small steps.
3. (20m) Read the Gilded Rose kata README (requirements and the "don't touch Item" rule).
4. (30m) Learn the golden master technique: record outputs of the legacy code for many inputs and assert unchanged (ApprovalTests library, or write your own text snapshot). Read ApprovalTests docs for Java (verify against current docs).
5. (30m) Run the kata; write a golden master test over combinations of name, sellIn, quality for 30 days.
6. (30m) Plan refactoring moves (not code yet): extract per-item-type methods, replace conditional with polymorphism, remove nested ifs. Order them by risk.

**Tips and common problems**
- Golden master too small → misses branches; check branch coverage in the IDE and add inputs until all branches are hit.
- Temptation to rewrite from scratch → forbidden: only behaviour-preserving moves.

**Done when**
- [ ] Golden master test passes and covers every branch of `updateQuality`.
- [ ] Ordered refactoring plan written.

### S2 Build (3h): gate task
1. (10m) Create branch `gate/gilded-rose`.
2. (20m) Confirm the golden master is green and committed.
3. (60m) Refactor in at least 12 small commits. Message format: `refactor: <move> — <reason>`. Run tests after each.
4. (15m) Add the new feature "Conjured items degrade twice as fast" using TDD after the refactor, and note how easy it was compared to before.
5. (20m) Write `docs/refactoring-log-gilded-rose.md`: each major move, the smell/principle behind it, and why it was safe.
6. (45m) Apply the same method to one remaining smelly ShopFlow module from Week 16: characterization tests → small commits → short log.
7. (10m) PRs.

**Tips and common problems**
- Tests start failing mid-way → revert the last commit (`git revert` or `git reset --hard HEAD~1` on your branch) and take a smaller step.
- Commit history becomes noisy → use `git add -p` to keep commits single-purpose.
- Kata forbids altering `Item` → wrap it instead of changing it.

**Done when**
- [ ] ≥12 refactoring commits with reasons; all green.
- [ ] Conjured feature implemented test-first.
- [ ] A ShopFlow module refactored with its own log.

### S3 Lab (2h): extra lab — timed elevator OOD
1. (5m) Set a 45-minute timer. Pretend you're in an interview.
2. (10m) Clarify requirements aloud and write assumptions (floors, number of elevators, capacity, inside vs outside requests, scheduling goal).
3. (10m) Identify entities and responsibilities.
4. (10m) Draw the main flow and class diagram (Mermaid or paper).
5. (10m) Choose patterns (State for elevator movement, Strategy for scheduling, Observer for floor displays) and justify each in one line.
6. (15m) Debrief: critique against SOLID and a checklist (requirements clarified? edge cases? extensibility?).
7. (25m) Code a skeleton of the elevator state machine with tests (idle, moving up/down, doors open).
8. (20m) List what you missed and redo the weakest part (e.g., scheduling algorithm).
9. (15m) Write the entry in `DESIGN_LAB.md`.

**Tips and common problems**
- Talking while drawing is harder than it sounds → practise out loud; record yourself.
- Don't chase the perfect scheduling algorithm; a clear, extensible interface wins.

**Done when**
- [ ] Design produced within 45 minutes and critiqued.
- [ ] State machine tests green.

### S4 Review + Write (2h)
1. (40m) Gate review with the peer/mentor: they pick two Gilded Rose commits and two ShopFlow commits; you explain the move, the reason and why it was safe.
2. (25m) Record feedback and address the top two items.
3. (15m) Check ADRs 006–008 are complete (context, decision, consequences, alternatives).
4. (15m) Update the progress tracker and tag: `git tag phase-3 && git push --tags`.
5. (15m) Phase retro; plan Phase 4 setup (Postgres with `pg_stat_statements`).
6. (10m) Answer the self-check.

**Self-check:** What is a characterization test? Why refactor in small steps? When would you not refactor now?
<details><summary>Answers</summary>

- A test that captures what the code currently does (even if odd), so you can change structure safely.
- Small steps keep the system green and make failures easy to localise and revert.
- When there are no tests and no time to add them, when the code is about to be deleted, or when a deadline requires a fix-first approach with a ticket for follow-up.
</details>

**Gate rubric (pass all):**
- Refactor done in small green steps with a clear commit history.
- Each design move has a reason tied to a principle or smell, not "it looked cleaner".
- Domain layer has no framework imports (ArchUnit proves it).
- She can say when she would not use a pattern.

**Output:** `phase-3` tag, ADRs 006–008 complete.
**If stuck:** take a smaller step, or revert and re-run the golden master; green first, clever later.
