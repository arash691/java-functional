# Week 10: Spring core: beans, configuration, profiles

**Mode:** A→B   **Release:** v0.2 Spring slice
**Outcome:** You can wire a Spring Boot application with constructor injection, typed configuration and profiles, and you can explain two classic startup/runtime bugs (shared singleton state, circular dependencies) from evidence.

**Before you start**
- Phase 1 done (Java and tests are comfortable). JDK 21+ (25 recommended). No database is needed this week. Docker is not needed yet.
- `cd shopflow-lab` and check the toolchain: `./mvnw -v` (the first run downloads Maven and all dependencies: allow 3-10 minutes).
- Run the whole week once to see the starting point: `scripts/run-week.sh w10 api`. Expect **30 tests, 25 red, 5 green** (the green ones are guards). Red is the starting point, not an error.
- Create `notes/week-10.md` in your own notes folder. You will write predictions and answers in it.

**Repo:** `shopflow-lab`, module `api`, package `shopflow.api.w10`, tag `@Tag("w10")`
- Run all of the week: `scripts/run-week.sh w10 api`
- Run one test class: `scripts/run-week.sh w10 api Ex1TaxTest` (nested classes: `'Sf06ShopApiTest*'`)
- Hints: `hints/w10/` (open one level at a time, after 10 minutes of trying)
- Solutions: `solutions/w10/` (only AFTER your tests are green)
- Run the app: `./mvnw -q -pl api spring-boot:run -Dspring-boot.run.main-class=shopflow.api.w10.W10App`

---

## S1 Try it (3h): mode A. Wire a Spring application

Goal: see Spring run, then make four small red tests green: a bean with constructor injection, typed configuration, profiles, and a health indicator.

1. (15m) **Orient.**
   - Open the module in your IDE: `api/src/main/java/shopflow/api/w10/` has packages `tax`, `config`, `shipping`, `health`, `pricing`, `shop`, `cart`, `discount`. `api/src/main/resources/shopflow-w10.yml` holds this week's settings.
   - Why a file called `shopflow-w10.yml`? The lab repo hosts several weeks in one module, so each week has its own settings file, pulled in by `application.yml` through `spring.config.import`. In a real project this would all be in `application.yml`.
   - Run `scripts/run-week.sh w10 api` and open `api/target/surefire-reports`. Find the red tests and read the first red message in the console.
2. (25m) **Worked example: run the app.**
   - **Predict first** (write in your notes): what will `GET /actuator/health` return? What will `GET /api/shop` return?
   - Start it: `./mvnw -q -pl api spring-boot:run -Dspring-boot.run.main-class=shopflow.api.w10.W10App`. In another terminal: `curl -s localhost:8080/actuator/health` and `curl -i localhost:8080/api/shop`.
   - Expected: health `{"groups":["liveness","readiness"],"status":"UP"}` and `/api/shop` answers `501` with an empty body (a stub: you will build it in S2). Stop the app with Ctrl-C.
   - Read `W10App` and `common/NoDbApp` (the javadoc explains why the database auto-configuration is switched off this week).
3. (25m) **Primer: what is a bean?** Read, then close the page and write 3 bullets from memory.
   - Spring Framework reference: *Core Technologies > The IoC Container > Introduction to the Spring IoC Container and Beans*, and *Dependency Injection > Constructor-based Dependency Injection*.
   - Questions to answer in your notes: What is a bean? Who creates it? What is the difference between creating `new TaxCalculator(...)` yourself and letting Spring do it? Why is constructor injection preferred over field injection?
4. (30m) **EX-10-01: a bean with constructor injection.**
   - Read `Ex1TaxTest`. **Predict:** which of its 5 tests fail right now, and why? Write it down, then run `scripts/run-week.sh w10 api Ex1TaxTest` (expect 4 failing).
   - In `tax/FlatTaxPolicy` return `0.19` for `DE`, `0.20` for `FR`, `0` otherwise (a `Map.of(...)` is enough).
   - In `tax/TaxCalculator` replace the no-argument constructor with `TaxCalculator(TaxPolicy policy)`, and compute `net * rate` rounded to 2 decimals with `RoundingMode.HALF_UP`.
   - Run the test class until green. Commit: `git add -A && git commit -m "EX-10-01 tax calculator"`.
5. (25m) **EX-10-02: typed configuration.**
   - Read: Spring Boot reference, *Externalized Configuration > Type-safe Configuration Properties* (just the intro and `@ConfigurationProperties` / constructor binding with records).
   - Run `scripts/run-week.sh w10 api Ex2ShopPropertiesTest`. Read the assertion message of `registered`: it tells you what is missing.
   - Register `ShopProperties`: add `@ConfigurationPropertiesScan` to `W10App`. Run again: 3 green.
   - **Break it on purpose:** in `shopflow-w10.yml` change `free-shipping-threshold: 50.00` to `abc`, start the app, read the "APPLICATION FAILED TO START" report, then put `50.00` back.
6. (35m) **EX-10-03: profiles.**
   - Read `Ex3ShippingProfilesTest`. It builds a tiny context that scans only the `shipping` package, so a mistake here cannot break other tests (this is why `StandardShipping` and `ExpressShipping` have no annotations yet).
   - **Predict:** what does the test say when neither class is a bean? Run it and compare.
   - Make `StandardShipping` a bean that is active when the profile `express` is **not** active, and take `ShopProperties` in its constructor so it can return `0.00` from the threshold on. Make `ExpressShipping` a bean that is active **only** for profile `express`.
   - Stuck after 10 minutes? Open `hints/w10/EX-10-03.md` level 1.
   - **Break it on purpose:** remove `@Profile` from `ExpressShipping`, run the test, read the failure ("expected a single bean but found 2"), then restore.
7. (25m) **EX-10-04: Actuator health with a custom indicator.**
   - Run `Ex4HealthTest`. The first test passes, the second does not: `components` is missing.
   - Make `ShopHealthIndicator` a bean (`@Component("shop")`), give it `ShopProperties`, and return `Health.up().withDetail("name", shop.name()).build()`.
   - The component still will not show: Actuator hides details by default. In `shopflow-w10.yml` add `management.endpoint.health.show-details: always`. Run the test: green.
   - Note: showing details publicly is a security decision. In production you would restrict it (`when-authorized`).

**Tips and common problems**
- `./mvnw: Permission denied` → the execute bit was lost → `chmod +x mvnw`.
- The first Maven run seems stuck → it is downloading Maven and ~200 MB of dependencies → wait; later runs take seconds.
- `Port 8080 was already in use` when starting the app → another app uses it → `lsof -i :8080`, stop it, or add `-Dspring-boot.run.arguments=--server.port=8081`.
- Your IDE marks `org.springframework.boot.health.contributor.HealthIndicator` or `@AutoConfigureMockMvc` as unknown when you copy from an older tutorial → **Spring Boot 4 split Boot into modules and moved packages**. Boot 3 tutorials use `org.springframework.boot.actuate.health.HealthIndicator` and `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc`. Use the imports already present in the lab's classes.
- `No qualifying bean of type ShopProperties` → the properties record is not registered → `@ConfigurationPropertiesScan` (or `@EnableConfigurationProperties(ShopProperties.class)`).
- You changed `shopflow-w10.yml` but the test still sees old values → the module was not rebuilt, or the test overrides the key with `properties = ...` (Ex2 does that for `name`).
- `Ex3` is green on the default test but red on `express` → in the second test the profile is activated by the property `spring.profiles.active=express`; check your `@Profile("express")` spelling.

**Done when**
- [ ] `scripts/run-week.sh w10 api Ex1TaxTest` is green (5/5), and also `Ex2ShopPropertiesTest`, `Ex3ShippingProfilesTest`, `Ex4HealthTest`.
- [ ] You can start the app and `curl localhost:8080/actuator/health` shows the `shop` component with the name.
- [ ] Your notes contain the 3 bullets about beans and your predictions for steps 2, 4 and 6.
- [ ] Four commits, one per exercise.

---

## S2 Build (3h): SF-06 Bootstrap service

Goal: the first real endpoints: shop info and a price quote that combines tax, shipping and configuration.

Acceptance tests: `Sf06ShopApiTest` (7 tests) and `Sf06ShopPropertiesValidationTest` (4 tests). Hints: `hints/w10/SF-06.md`.

1. (10m) **Read the tests before the code.** Open both test classes. In your notes list the behaviours they demand (shop info; quote with shipping; free shipping from 50.00; unknown SKU gives 400; empty items and zero quantity give 400; express profile ships at 15.00; invalid configuration stops startup). Run `scripts/run-week.sh w10 api 'Sf06*'` and see the starting state.
2. (25m) **`GET /api/shop`.** In `ShopController` inject `ShopProperties` through the constructor and return `Map.of("name", ..., "currency", ...)`. Run `'Sf06ShopApiTest*'` and watch `shopInfo` turn green. Then `curl localhost:8080/api/shop` against the running app.
3. (50m) **The pricing service.** In `PricingService` take `PriceList`, `TaxCalculator` and `ShippingPolicy` in the constructor. Compute: subtotal = Σ price × quantity (scale 2); tax; shipping; total. Unknown SKU → throw `UnknownSkuException`.
   - Work with the smallest test first: a quote for one item to `US` (no tax). Then add DE tax, then shipping, then the threshold.
   - If every test in the class suddenly fails with "No qualifying bean of type ShippingPolicy", finish EX-10-03 first.
4. (25m) **The controller and error handling.** In `PricingController` call the service, put `@Valid` on the `@RequestBody`, and add an `@ExceptionHandler(UnknownSkuException.class)` that returns a `ProblemDetail` with status 400. Run the Default nested class: all green except possibly validation, which `@Valid` already handles (`QuoteRequest` has the constraints).
5. (30m) **The express profile in a real run.**
   - Run the `Express` nested test. Then start the app with the profile: `./mvnw -q -pl api spring-boot:run -Dspring-boot.run.main-class=shopflow.api.w10.W10App -Dspring-boot.run.profiles=express`.
   - Call the quote twice, once with and once without the profile, and compare the `shipping` value:
     `curl -s -X POST localhost:8080/api/pricing/quote -H 'Content-Type: application/json' -d '{"items":[{"sku":"SKU-3","quantity":2}],"country":"US"}'`
     Expected: `{"subtotal":50.00,"tax":0.00,"shipping":0.00,"total":50.00}` normally, `shipping":15.00` with `express`.
6. (25m) **Fail fast on bad configuration.** Make `ShopProperties` `@Validated` with constraints: name not blank, currency exactly three capital letters, threshold not negative and not null. Run `Sf06ShopPropertiesValidationTest` until green. Then set `currency: eur` in `shopflow-w10.yml`, start the app, read the startup report, restore the value.
7. (15m) **Close the loop.** Run all non-incident tests: `scripts/run-week.sh w10 api 'Ex*,Sf06*'`. Commit: `SF-06 shop info and pricing quote`. Open a PR (or write a PR description in your notes): what changed, how tested.

**Tips and common problems**
- `400` expected but the test sees `500` → an exception escapes → check that the `@ExceptionHandler` is in the controller class and returns a `ProblemDetail`.
- `JSON path "$.subtotal" expected:<25.0> but was:<25.00>`-style mismatch does not occur here (`jsonPath().value(25.00)` compares numbers), but a missing `setScale(2)` can still give `20` instead of `20.00` in `curl` output → set the scale explicitly.
- `HttpMessageNotReadableException` on the quote → the JSON body is malformed or has the wrong field names → compare with the `curl` example.
- Constructor with three parameters and Spring says "No default constructor" → you have two constructors, or an old no-arg one is still there → keep exactly one public constructor.
- The validation test `valid()` passes already but the others fail → the constraints are missing, or `@Validated` is missing from the record.

**Done when**
- [ ] `scripts/run-week.sh w10 api 'Sf06*'` is green (11/11).
- [ ] You ran the app with and without profile `express` and saw both shipping values.
- [ ] Bad `currency` in the yaml stops the app at startup with a clear message.
- [ ] One commit and one PR description for SF-06.

---

## S3 Incident (2h): mode B. Two bugs you did not write

You inherit two tickets. Open `hints/w10/INC-33.md` and `INC-34.md` only one level at a time.

### INC-33 (RL-33): "Customers see each other's cart totals"  (≈60m)
> **Support ticket #4417:** "Alice says her cart total (45.00) includes items she never added. Bob's cart looks fine. It only seems to happen when two people shop at around the same time. Can we get this fixed before the weekend sale?"

1. **PREDICT** (5m). Before running anything, answer in your notes:
   1. How many `CartService` objects does Spring create by default?
   2. Alice adds 2 × SKU-1 (10.00 each). Bob adds 1 × SKU-2 (5.00). Alice asks for her total. What does the current code return?
   3. Where is the cart data stored?
   4. Would the bug also happen if requests never ran at the same time?

   <details><summary>Answers</summary>

   1. One (singleton scope: one instance per application context).
   2. 25.00 (Alice sees Bob's item).
   3. In the field `lines` of the single shared `CartService`.
   4. Yes. It is a design bug (state in a shared object); threads only add more problems on top.
   </details>
2. **REPRODUCE** (5m). `scripts/run-week.sh w10 api Inc33SharedCartTest`. Expected red excerpt:
   `JSON path "$.total" expected:<20.0> but was:<25.0>` (tests `twoCustomers` and `emptyCart` fail; `singleCustomer` is the green guard).
3. **INVESTIGATE** (15m). Use the debugger: put a breakpoint on the first line of `CartService.addItem`, run `twoCustomers` in debug mode, and look at `this` on both calls: the same object id. Then look at the `lines` field in the Variables panel after Bob's call. Optional: start the app, run the two `curl` calls for different customers and watch `total` for the first one change:
   `curl -s -X POST localhost:8080/api/carts/alice/items -H 'Content-Type: application/json' -d '{"sku":"SKU-1","quantity":2}'` and the same for `bob`, then `curl localhost:8080/api/carts/alice/total`.
   Signal to look for: **the same `identityHashCode` for different customers**. Stuck at 10 minutes? Open hint level 1, then 2.
4. **STUDY** (10m). Read Spring Framework reference *Core Technologies > Bean Scopes > The Singleton Scope* (and skim *The Prototype Scope* and *Request, Session...*). Answer:
   1. What does "singleton" mean in Spring compared with the GoF pattern?
   2. When would you use request scope? Why is it usually *not* the right fix here?
   3. Which of these fixes is best: (a) `@Scope("prototype")` on `CartService`, (b) key the data by customer, (c) `synchronized` methods? Why?
5. **FIX** (10m). Replace the shared list by `Map<String, List<CartLine>>` keyed by `customerId` (use a thread-safe map such as `ConcurrentHashMap`, and a thread-safe list per customer). `total(customerId)` must use only that customer's lines. Keep `reset()` working.
6. **PROVE** (5m). `Inc33SharedCartTest` green (3/3). Add one regression test of your own with three customers. Optional extra: write a test with 50 threads adding to the same customer and see whether your map/list choice is safe.
7. **GENERALIZE** (10m). Write the ledger entry for **RL-33** in `reality-lab/LEDGER.md` (template there) and say the one-minute answer aloud: *"Spring beans are singletons by default, so any per-user or per-request data in a field is shared by everyone..."*

**Tips and common problems**
- The tests share one application context, so the singleton keeps state across tests → that is why `CartService` has a `reset()` hook used by `@BeforeEach`; do not remove it.
- Your fix makes `twoCustomers` green but `emptyCart` red → `total` for a customer with no entry must return `0.00` (`getOrDefault`), not throw.
- You wrapped the methods in `synchronized` and the test is still red → locking makes access safe, not correct: the problem is the data model, not mutual exclusion.

### INC-34 (RL-34): "The app does not start after the discount feature" (≈50m)
> **Alert from CI:** "main is red: ApplicationContext fails to load after PR #212 (Discounts). The feature works in the author's unit tests."

1. **PREDICT** (5m).
   1. Does it fail at startup or on the first call of the discount code?
   2. Which beans does Spring try to create and in which order does the problem appear?
   3. If you changed one class to setter injection, would the cycle be fixed or hidden?
   4. What does a cycle tell you about the design?

   <details><summary>Answers</summary>

   1. At startup, when the context creates the beans.
   2. `cartPricing` needs `discountService`, which needs `cartPricing`, which is still "currently in creation".
   3. Hidden. Spring could resolve it with an early reference if circular references were allowed, but Spring Boot forbids them by default (`spring.main.allow-circular-references=false`), and the cycle remains a design smell.
   4. Two classes know too much about each other; a responsibility is in the wrong place.
   </details>
2. **REPRODUCE** (5m). `scripts/run-week.sh w10 api Inc34CircularDependencyTest`. Expected: `starts` and `discountForLargeOrders` red with
   `BeanCurrentlyInCreationException: Error creating bean with name 'cartPricing': Requested bean is currently in creation: Is there an unresolvable circular reference...`; `smallOrders` is the green guard.
3. **INVESTIGATE** (10m). Read the failure text from the bottom ("Caused by") up. Write the cycle as arrows in your notes. Open `CartPricing` and `DiscountService` and find the exact method call that forces each dependency. Hint ladder: `hints/w10/INC-34.md`.
4. **STUDY** (10m). Spring Framework reference *Dependency Injection > Circular dependencies* (inside "Constructor-based or setter-based DI"), and the Spring Boot property `spring.main.allow-circular-references`. Answer: why is allowing circular references a bad fix? What are three ways to break a cycle (move logic, extract a third class, pass data as parameters)?
5. **FIX** (10m). Break the cycle by moving the "large order" rule into `DiscountService` so `DiscountService` no longer needs `CartPricing`. Remove the now-unused `isLargeOrder` from `CartPricing`.
6. **PROVE + GENERALIZE** (20m). `Inc34CircularDependencyTest` green (3/3). Ledger entry **RL-34** and the one-minute answer: *"A constructor cycle can never be created; Boot forbids even setter cycles; the fix is to change the design, not the injection style."*

**Tips and common problems**
- Your fix is `@Lazy` on a constructor parameter and the test is green → that is a workaround that hides the design problem; the lab accepts it, but write in the ledger why the redesign is better.
- The test says "Unsatisfied dependency expressed through constructor parameter 0" without the word "circular" → the cause is further down in the "Caused by" chain; read to the end.

**Wrap-up** (10m). Run the whole week: `scripts/run-week.sh w10 api` → all 30 green. If a hint ladder was needed, write which level in your notes.

**Done when**
- [ ] `Inc33SharedCartTest` and `Inc34CircularDependencyTest` are green.
- [ ] Two ledger entries (RL-33, RL-34), each with a one-minute spoken answer.
- [ ] Notes record which hint levels you used.

---

## S4 Consolidate (2h)

1. (30m) **Ledger and recall.** Finish `reality-lab/LEDGER.md` entries for RL-33 and RL-34 (symptom, why, reproduce, observe with, fix, prevent, one-minute answer, confidence 1-5). Record yourself saying both answers; listen once.
2. (25m) **ADR-003 "Constructor injection only; no field injection".** Create `docs/adr/ADR-003-constructor-injection.md` with *Context* (what you saw in INC-33/34 and EX-10-01), *Decision*, *Consequences* (testability, immutability, visible cycles; slightly more constructor code), and *How we enforce it* (code review rule now; ArchUnit rule in Phase 3).
3. (20m) **Flashcards.** Add these to your deck (write your own answers first):
   1. What is a Spring bean, and what is its default scope?
   2. Why constructor injection over field injection?
   3. What does `@ConfigurationProperties` give you over `@Value`?
   4. How do `@Profile("!x")` and `spring.profiles.active` interact?
   5. What does Boot do about circular dependencies by default, and why?
4. (20m) **Self-check.** Answer aloud before opening the answers.
   1. Why can a singleton bean not safely hold per-request data in a field?
   2. How do you make an application refuse to start with a bad currency code?
   3. Where in the failure message do you find the beans on a circular dependency?
   4. Why does the default health endpoint not show the `shop` component details?

   <details><summary>Answers</summary>

   1. Because one instance serves all requests/threads; the data is shared and also needs thread-safety.
   2. Bind the settings with `@ConfigurationProperties` and add `@Validated` constraints; binding failure stops startup.
   3. In the chain of "Caused by" messages (the bean names appear in each level).
   4. Details are hidden for security; `management.endpoint.health.show-details` must be set.
   </details>
   If you missed two or more, re-read the matching part of S1 (steps 3, 5 or 7) in next week's S4.
5. (15m) **Compare with the reference.** Now (and only now) compare: `diff -ru api/src/main solutions/w10/api/src/main | less`. Note one thing the solution does differently and decide whether you agree.
6. (10m) **Retro** in `retro.md`: what worked, what blocked me (and how long), what I'll change next week.

**Done when**
- [ ] ADR-003 committed; 5 flashcards added; retro written.
- [ ] `scripts/run-week.sh w10 api` is fully green and committed.

**Output of the week:** SF-06 PR, 4 EX commits, ADR-003, ledger entries RL-33 and RL-34, notes file.

**If stuck:** search the exact first line of the error plus "Spring Boot 4"; for any Boot 3 snippet, check the package names first.
