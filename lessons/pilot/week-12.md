# Week 12: Postgres, Flyway and JPA

**Mode:** A→B   **Release:** v0.2 Spring slice
**Outcome:** You can persist a domain model in PostgreSQL with Flyway migrations, JPA entities and Spring Data repositories, see the SQL Hibernate generates, and fix the three classic JPA production bugs: N+1 queries, `LazyInitializationException`, and entities leaking into JSON.

**Before you start**
- Complete the modeling block (M1–M8, `lessons/modeling/`): this week assumes you can read an ERD and basic SQL.
- Weeks 10 and 11 done. **Docker Desktop must be running** (`docker ps` must answer). Tests start a real PostgreSQL 17 in Docker with Testcontainers. The first run pulls the image (`postgres:17-alpine`, ~100 MB), so it is slow once.
- Run the week once: `scripts/run-week.sh w12 api`. Expect **27 tests: 23 red, 4 green** (worked example plus guards). Red tests here are mostly `UnsupportedOperationException: TODO ...`: those are stubs for you to fill.
- Tools: a SQL client is optional; the lesson uses `docker exec` + `psql` (see S1 step 2).

**Repo:** `shopflow-lab`, module `api`, package `shopflow.api.w12`, tag `@Tag("w12")`
- Run all: `scripts/run-week.sh w12 api` · one class: `scripts/run-week.sh w12 api Ex1ProductQueriesTest`
- Hints: `hints/w12/` · Solutions: `solutions/w12/` (only after green)
- Migrations live in `api/src/main/resources/db/migration`: `V1` (product) and `V3` (orders) are provided; **you** write `V2` (S1) and `V4` (S2). Flyway orders by version number, not by file date.

---

## S1 Try it (3h): mode A. A table, an entity, a repository

Goal: understand the path from SQL migration to entity to repository to test, then make two exercises green.

1. (15m) **Orient and first run.**
   - Start Docker Desktop. Run `scripts/run-week.sh w12 api ProductRepositoryExampleTest` (the worked example). The first run takes minutes (image pull); watch the console for `Container is started`, then Flyway lines: `Migrating schema "public" to version "1 - create product"` and `... "3 - create orders"`.
   - Read `AbstractPostgresTest`: one container for the whole test run, `@ServiceConnection` tells Spring Boot to use it as the `DataSource`, and a `@BeforeEach` empties the tables.
2. (30m) **Worked example, line by line.**
   - Open side by side: `V1__create_product.sql`, `ProductEntity`, `ProductRepository`, `ProductRepositoryExampleTest`. Draw arrows between each column and its field.
   - **Predict:** which SQL will Hibernate run for `repository.save(...)`? Then see it: in `AbstractPostgresTest` add `"logging.level.org.hibernate.SQL=DEBUG"` to the `properties` array of `@SpringBootTest`, run the example, and find the `insert into product (...)` line. (Remove it again later, or keep it while learning.)
   - **Look inside the database.** Put a breakpoint on the last line of the example test and debug it. In a terminal: `docker ps` (copy the container id of `postgres:17-alpine`), then `docker exec -it <id> psql -U test -d test`. Try `\d product`, `select * from product;`, `select version, description, success from flyway_schema_history;`. Finish with `\q` and resume the test.
3. (25m) **Primer.** Read and take notes (3 bullets each):
   - Spring Data JPA reference *Query Methods*: how method names become queries (`findBy`, `Containing`, `IgnoreCase`, `OrderBy`).
   - Flyway documentation *Concepts > Migrations* (versioned migrations, naming `V2__description.sql`, checksums, "never edit an applied migration").
   - Hibernate ORM User Guide, the introduction to *Entity types* and `@Id` generation (`IDENTITY` vs sequences: for now just know both exist).
4. (30m) **EX-12-01: query methods.**
   - Read `Ex1ProductQueriesTest`; run it: 4 errors `TODO EX-12-01`.
   - Add to `ProductRepository`: `Optional<ProductEntity> findBySku(String sku)` and a method that finds by name containing a fragment, ignoring case, ordered by name ascending (let the method name say so).
   - Implement `ProductCatalog.findBySku` and `searchByName`, mapping `ProductEntity` to `ProductView`. The entity has no description yet, so use `null` for it for now. Run until 4/4. With SQL logging on, read the `like ... escape` statement Hibernate generated for the case-insensitive search.
5. (40m) **EX-12-02: a schema change, the right way.**
   - Read `Ex2ProductDescriptionTest`. It demands a nullable `description` column (max 500 characters).
   - **First do it wrong on purpose.** Add a `description` field to `ProductEntity` (with a getter and a constructor taking it) but write **no migration**. Implement `ProductCatalog.create` and run the test. **Predict** the error, then read it: a `PSQLException: ERROR: column "description" of relation "product" does not exist` (or a similar message).
   - Now fix it properly: create `api/src/main/resources/db/migration/V2__product_description.sql` with the `alter table` statement. Do **not** edit `V1`. Run again.
   - Update `ProductCatalog.toView` to pass the real description. Run `Ex2ProductDescriptionTest` and `Ex1ProductQueriesTest` (both green).
   - Question for your notes: why does Flyway refuse to start if you edit `V1__...` after it was applied? (Look at the `checksum` column in `flyway_schema_history`.)
6. (25m) **Relationships, by reading.** Open `V3__create_orders.sql`, `OrderEntity`, `OrderItemEntity`. You drew ERDs in M7/M8, so first predict the tables and keys from `OrderEntity`, then compare with the SQL. Draw the ERD: `customer_order 1 --- * order_item * --- 1 product`. In your notes answer: which side *owns* the relationship (the one with the foreign key, `@JoinColumn`)? What does `mappedBy` mean? What do `cascade = ALL` and `orphanRemoval = true` do to items? Why are the `@ManyToOne` fetches `LAZY`? Which table name is not `order`, and why?
7. (15m) **Close the loop.** `scripts/run-week.sh w12 api 'Ex*'` is green (7 tests) and the example stays green. Commit each exercise. Remove the debug breakpoint.

**Tips and common problems**
- `Could not find a valid Docker environment` or `Cannot connect to the Docker daemon` → Docker Desktop is not running → start it and wait until `docker ps` works. On Linux/Colima set `DOCKER_HOST` accordingly. Restart the test run.
- `Ryuk` / "Could not start container Ryuk" errors on corporate networks or rootless Docker → the reaper container cannot start → try `TESTCONTAINERS_RYUK_DISABLED=true` in your environment for local runs (containers then remain after the run: `docker ps` and `docker rm -f` them yourself).
- `Flyway: Detected failed migration` or `Validate failed: Migration checksum mismatch` → you edited an applied migration; the Testcontainers database is fresh each run, so this only happens against a long-lived database → never edit applied migrations, add a new one.
- Test output is flooded with logs → set `logging.level.root=WARN` in the test properties, keep only `org.hibernate.SQL=DEBUG`.
- Spring Boot 4 specifics: the Flyway starter is `spring-boot-starter-flyway` and the PostgreSQL support module `flyway-database-postgresql` (both already in the pom). Testcontainers 2 renamed classes: `org.testcontainers.postgresql.PostgreSQLContainer` (not generic anymore). Old tutorials show `PostgreSQLContainer<?>` from `org.testcontainers.containers`.
- The test for the column name fails even though you wrote the migration → the file name must be `V2__product_description.sql` (capital V, two underscores) in `db/migration`.

**Done when**
- [ ] `Ex1ProductQueriesTest` (4) and `Ex2ProductDescriptionTest` (3) are green; the worked example is still green.
- [ ] You saw an `insert` and a `select ... like` statement in the log, and you looked at `flyway_schema_history` with `psql`.
- [ ] Notes contain the ERD and the answers about `mappedBy`, `cascade`, `LAZY`.

---

## S2 Build (3h): SF-08 Cart and SF-09 Orders

Goal: persist a customer's cart and turn it into an order, transactionally.

Acceptance tests: `Sf08CartTest` (7) and `Sf09PlaceOrderTest` (5). Hints: `hints/w12/SF-08.md`.

**Database contract the tests use:** table `cart` (one row per customer, `customer_id` unique) and table `cart_item` (one row per SKU in a cart). Everything else is your design.

1. (10m) **Read the tests as requirements.** List the rules: merge same SKU, quantity must be positive, unknown SKU, separate carts per customer, price snapshot on the order, empty cart error, the cart is empty after ordering. Run `scripts/run-week.sh w12 api 'Sf0*'` for the starting state.
2. (30m) **Schema and entities.** Write `V4__create_cart.sql` (tables `cart` and `cart_item`, with foreign keys, `quantity > 0`, and `unique (cart_id, product_id)`). Create `CartEntity`, `CartItemEntity` and `CartRepository` (with `findByCustomerId`) in package `shopflow.api.w12.cart`. Model them like `OrderEntity` / `OrderItemEntity`.
3. (45m) **`CartService`.** Implement `addItem`, `getCart`, `clear`:
   - `addItem`: reject non-positive quantity with `IllegalArgumentException` *before* touching the database; find the product by SKU or throw `UnknownProductException`; find or create the customer's cart; increase the quantity if the SKU is already in it, else add a new item.
   - `getCart`: lines with `unitPrice` (current product price) and `lineTotal`; total with scale 2; an unknown customer has an empty cart with total `0.00`.
   - `clear`: remove all items.
   - Put `@Transactional` on the methods. Work test by test: `emptyCart`, `addItems`, `sameSkuMerges`, then the rest. Stuck at 10 minutes? `hints/w12/SF-08.md`.
4. (45m) **`OrderService.placeOrder`** (read its javadoc). Inject the cart repository and the order repository. Steps inside one transaction: load the customer's cart (or `EmptyCartException`), create `OrderEntity(customerId, NEW, Instant.now())`, add one `OrderItemEntity` per cart item with `unitPrice` = product price **now** (a snapshot), save the order, empty the cart, return an `OrderView`. `OrderQueryService.toView` is private: write your own small mapping method (or move it to a shared place) rather than injecting the query service. Run `Sf09PlaceOrderTest` until 5/5.
5. (20m) **Look at the SQL of an order.** Turn on SQL logging, run `Sf09PlaceOrderTest.placeOrder`, and count the statements from `placeOrder`. Write in your notes: which statements are selects, inserts, deletes? Which one surprised you?
6. (20m) **What could go wrong? (thinking, no code).** Two requests place an order for the same customer at the same moment. Walk through your code and write down what could happen (two orders from one cart? a failed insert?). Note one idea to prevent it (unique constraint, locking). You will do this properly in Phase 4.
7. (10m) **Close the loop.** `scripts/run-week.sh w12 api 'Ex*,Sf0*'` is green (19 tests). Commit `SF-08 persistent cart` and `SF-09 place order`.

**Tips and common problems**
- `LazyInitializationException` in `getCart` → you mapped lazy associations outside a transaction → mark the service method `@Transactional(readOnly = true)`. (This is exactly INC-49 in miniature: expect to meet it again.)
- `could not execute statement ... violates foreign key constraint` → you referenced a product that is not in the table, or deleted rows in the wrong order; the test cleanup uses `truncate ... cascade`.
- `duplicate key value violates unique constraint "cart_customer_id_key"` → you created a second cart for the same customer → look up before creating.
- `table "cart" does not exist` in a test failure → `V4` is not found: check the folder and file name, then rebuild.
- Cart total prints `25` instead of `25.00` → the assertion uses `isEqualByComparingTo`, so it passes; but keep scale 2 for money anyway.
- Entities and views are different things: do not return `CartEntity` from `CartService` (the tests would still pass, but INC-42 shows why not).

**Done when**
- [ ] `Sf08CartTest` (7/7) and `Sf09PlaceOrderTest` (5/5) are green.
- [ ] `V4__create_cart.sql` is committed and `flyway_schema_history` would list versions 1-4.
- [ ] Notes: the statement count/shape for `placeOrder` and your "two requests at once" analysis.

---

## S3 Incident (2h): mode B. Three JPA bugs you did not write

Do them in this order: INC-48, INC-49, INC-42 (INC-42 relies on INC-49). Hints: `hints/w12/`. Seed data comes from the test helper `OrderTestData`.

### INC-48 (RL-48): "The back-office order list is slow" (≈45m)
> **Ticket #6002:** "Opening Orders in the back office takes seconds and the database CPU spikes. It was fine when we had 10 orders. Now we have thousands (we only look at the first 20 in QA)."

1. **PREDICT** (5m).
   1. The list shows 20 orders with 3 items each. How many SQL statements would you expect for a well-written version? How many does the code (`orders.findAll()` then `order.getItems()` and `item.getProduct()` for each) probably run?
   2. Which of these is a fix: (a) make associations `EAGER`, (b) fetch join, (c) add an index, (d) cache?
   3. Why is it invisible in a test with 2 orders?

   <details><summary>Answers</summary>

   1. A well-written version needs 1 (orders + items + products in one join) or 2. The naive one runs 1 for orders, then 1 per order for its items (20), then one per distinct product (3): **24**.
   2. (b). `EAGER` often makes it worse and applies everywhere; an index does not remove the extra round trips; a cache hides it.
   3. With 2 orders the extra statements are a handful: fast enough to go unnoticed. The cost grows linearly with N.
   </details>
2. **REPRODUCE** (5m). `scripts/run-week.sh w12 api Inc48NPlusOneTest`. Expected: `correctContent` green (guard); `fewStatements` red: `[SQL statements for listing 20 orders (1 + N means N+1)] Expecting actual: 24L to be less than or equal to: 2L`.
3. **INVESTIGATE** (15m). Add `"logging.level.org.hibernate.SQL=DEBUG"` to `AbstractPostgresTest`'s properties and run again. In the console find one `select ... from customer_order`, then `select ... from order_item where order_id=?` repeated 20 times with different ids, then `select ... from product where id=?` three times. Signal: **the same statement repeated with different parameters**. Hint ladder: `hints/w12/INC-48.md`.
4. **STUDY** (5m). Hibernate ORM User Guide *Fetching* (lazy vs eager, fetch joins) and Vlad Mihalcea's article "N+1 query problem with JPA and Hibernate". Answer: what are the two common fixes, and what is the catch of fetching a collection together with pagination?
5. **FIX** (10m). In `OrderRepository` add a `@Query` that loads orders with their items and each item's product using `join fetch`; call it from `OrderQueryService.listWithItems`. Run `Inc48NPlusOneTest`: 2/2 green.
6. **PROVE + GENERALIZE** (5m). Repeat with 100 orders (change the seed in a copy of the test): the statement count must not change. Ledger **RL-48**: "fix = fetch what you will use, in one query; assert the statement count in a test."

### INC-49 (RL-49): "Opening one order fails with LazyInitializationException" (≈30m)
> **Ticket #6013:** "Clicking an order in the back office gives a 500. Log: `LazyInitializationException: Cannot lazily initialize collection of role '...OrderEntity.items' (no session)`."

1. **PREDICT** (5m). (a) The order *loads* fine: why does the error appear a moment later? (b) What is "the session"? (c) Why does the same code work inside `listWithItems`? (d) Is `EAGER` the right fix?

   <details><summary>Answers</summary>

   (a) `findById` returns the order but its `items` is an uninitialised placeholder; touching it needs the database. (b) The Hibernate session, which lives as long as the transaction. (c) That method is `@Transactional`, so the session stays open while the items are mapped. (d) No: it hides the cost on every load and does not solve where the session boundary should be.
   </details>
2. **REPRODUCE** (5m). `scripts/run-week.sh w12 api Inc49LazyInitializationTest`. Expected: `describe` red with `LazyInitializationException ... (no session)`; `unknown` is the green guard.
3. **INVESTIGATE** (5m). Read the stack trace: it points into `toView` in `OrderQueryService`. Compare the two public methods: one has `@Transactional`, the other does not. Hint: `hints/w12/INC-49.md`.
4. **STUDY** (5m). Spring Framework reference *Data Access > Transaction Management > Declarative* (what `@Transactional` on a service method means) and the Hibernate User Guide on *LazyInitializationException* / sessions. Answer: where should the transaction boundary be, and why is "map to a DTO inside the transaction" the standard answer?
5. **FIX** (5m). Put `@Transactional(readOnly = true)` on `describe`. `Inc49LazyInitializationTest` 2/2 green.
6. **PROVE + GENERALIZE** (5m). Ledger **RL-49**: symptom, cause (session closed), the three non-fixes (EAGER, open-session-in-view, `Hibernate.initialize` everywhere) and why the transaction at the service boundary is the fix.

### INC-42 (RL-42): "GET /api/orders/{id} returns 500" (≈35m)
> **Ticket #6020:** "The orders API returns 500 for real orders. QA only tested with an unknown id (404), which works."

1. **PREDICT** (5m). (a) What does `OrderController.get` return? (b) What will Jackson do with a JPA entity that has a lazy collection, a back-reference (`item.order`) and lazy products? (c) What status does the client see?

   <details><summary>Answers</summary>

   (a) The entity itself. (b) It walks every getter: the lazy collection fails (no session, open-session-in-view is off), and even if loaded, `order -> items -> order -> ...` is a cycle. (c) 500, because writing the response fails after the controller returned.
   </details>
2. **REPRODUCE** (5m). `scripts/run-week.sh w12 api Inc42EntitySerializationTest`. Expected: `ok` and `noCycles` red with `Status expected:<200> but was:<500>`; `unknown` is the green guard. (Fix INC-49 first.)
3. **INVESTIGATE** (10m). In the console find `Resolved [HttpMessageNotWritableException: Could not write JSON: Cannot lazily initialize collection of role '...OrderEntity.items'...]`. Signal: the exception is thrown *while writing the response*, not in your code. Hint: `hints/w12/INC-42.md`.
4. **STUDY** (5m). Search "do not expose JPA entities in REST API" and read two arguments for DTOs (decoupling, lazy loading, security, cycles). Answer: what else could leak from an entity (internal fields, password hashes)?
5. **FIX** (5m). `OrderController.get` should return `OrderView` from `OrderQueryService.describe(id)`; the repository dependency goes away.
6. **PROVE + GENERALIZE** (15m). `Inc42EntitySerializationTest` 3/3 green. Ledger **RL-42** and the one-minute answer. Next week's S3 will turn open-session-in-view on to show a second, quieter way this same code can go wrong.

**Wrap-up** (10m). `scripts/run-week.sh w12 api` → all 27 green. Write which hints you needed.

**Tips and common problems**
- `join fetch` returns duplicate parent rows in your own variant → add `distinct` (Hibernate 6+ de-duplicates entities anyway, but the SQL returns more rows).
- `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory` appears → you combined `join fetch` of a collection with paging; that is why paging with collection fetches needs a different pattern (two queries, or batch size).
- Statement count still above 2 → the product association is still loaded lazily per item: fetch it in the same query (`join fetch i.product`) or the count includes it.
- `describe` is green in your test but fails in the controller test → the controller is still using the repository directly (INC-42).

**Done when**
- [ ] All of `Inc48NPlusOneTest`, `Inc49LazyInitializationTest`, `Inc42EntitySerializationTest` are green.
- [ ] Ledger entries RL-48, RL-49, RL-42, each with a one-minute spoken answer.

---

## S4 Consolidate (2h)

1. (30m) **Ledger and recall.** Finish the three ledger entries; for RL-48 include the SQL log excerpt (before and after). Record the three one-minute answers and listen to one.
2. (25m) **ADR-005 "No entities in API responses".** Create `docs/adr/ADR-005-no-entities-in-api.md`: *Context* (INC-42), *Decision* (controllers return DTOs/views, mapped inside a transactional service), *Consequences* (more mapping code; the API is independent of the schema; lazy-loading problems cannot reach JSON), *How we enforce it* (review rule; an ArchUnit rule in Phase 3).
3. (15m) **Flashcards.** Add:
   1. What is the N+1 problem and how do you detect it in a test?
   2. Why does `LazyInitializationException` happen and what is the right fix?
   3. Why never edit an applied Flyway migration?
   4. What does `mappedBy` mean?
   5. Why return DTOs instead of entities?
4. (20m) **Self-check.** Answer aloud first.
   1. 20 orders × 3 items: what is the statement count of the N+1 version and of the fixed one?
   2. Which annotation sets the transaction boundary for `describe` and why does mapping to a view belong inside it?
   3. A teammate "fixes" LazyInitializationException by switching to `EAGER`. What do you say?
   4. How does the test know the number of SQL statements?

   <details><summary>Answers</summary>

   1. 24 (1 + 20 + 3) versus 1 (or at most 2).
   2. `@Transactional` on the service method; lazy associations can only be read while the session is open.
   3. It trades one bug for a bigger one: every load fetches the data whether needed or not and N+1 can get worse; fix the query and the transaction boundary instead.
   4. Hibernate statistics (`generate_statistics=true`): `getPrepareStatementCount()` after `clear()`.
   </details>
5. (20m) **Compare with the reference** (only now): `diff -ru api/src/main solutions/w12/api/src/main | less`. Note one difference and whether you agree.
6. (10m) **Retro** in `retro.md`. Also write what you want the pilot's author to change about weeks 10-12 (what was unclear, what was too slow).

**Done when**
- [ ] ADR-005 committed; flashcards added; retro written.
- [ ] `scripts/run-week.sh w12 api` fully green and committed. Stop leftover containers if any: `docker ps`.

**Output of the week:** `V2` and `V4` migrations, EX-12-01/02 commits, SF-08 and SF-09 PRs, ADR-005, ledger entries RL-42/48/49, retro with feedback.

**If stuck:** turn on SQL logging first: most JPA puzzles are solved by reading the statements Hibernate actually runs.
