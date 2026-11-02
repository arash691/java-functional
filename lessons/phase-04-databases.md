# Phase 4: Databases in Depth (Weeks 21–27)

Goal: write correct, fast SQL; understand transactions and locking well enough to debug production; know when not to use Postgres.

> **Load note:** this phase has the most Reality Lab items (~35). Each is a small repro (≈25 min). Anything unfinished rolls into the Phase 5 and 6 consolidation weeks.

Session format and the "Stuck?" routine are defined in `README.md`. Follow steps in order; time boxes are budgets.

## Phase setup (do once, before Week 21, ~30 min)
1. Start a lab Postgres with the statistics extensions and helpful logging (verify flags against current docs):
   ```bash
   docker run -d --name pg-lab -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres:17 \
     -c shared_preload_libraries='pg_stat_statements,auto_explain' \
     -c pg_stat_statements.track=all -c track_io_timing=on \
     -c auto_explain.log_min_duration=200ms -c log_min_duration_statement=200 \
     -c log_lock_waits=on -c deadlock_timeout=1s
   ```
2. Connect and prepare:
   ```bash
   docker exec -it pg-lab psql -U postgres -c "CREATE DATABASE shopflow;"
   docker exec -it pg-lab psql -U postgres -d shopflow
   ```
   Inside psql: `CREATE EXTENSION pg_stat_statements;` then `\timing on`, `\x auto`, `\pset null '∅'` (makes NULLs visible).
3. Watch logs in another terminal: `docker logs -f pg-lab`.
4. psql survival kit: `\dt`, `\d table`, `\di+` (indexes with size), `\l`, `\q`, `\watch 2` (re-run last query every 2s), `\timing`.
5. Keep every script in the repo (`sql/`), not in your shell history.

---

## Week 21 — SQL mastery
**Outcome:** fluent in joins, windows, CTEs and aggregation.
**Before you start:** phase setup done; `shopflow` database exists. Create a scratch schema `scratch` for experiments: `CREATE SCHEMA scratch;`.

#### S1 Learn (3h): joins, NULL logic, windows, CTEs
1. (25m) Read PostgreSQL docs "The SQL Language" tutorial, sections on joins, aggregates and window functions intro. Type every example into psql (don't paste). Use the tutorial's `weather`/`cities` tables.
2. (30m) Read the "Queries" chapter: table expressions (join types), select lists, `WITH` queries. From memory (close the page), write a join-type table using `A(1,2,3)` and `B(2,3,4)`: result rows for INNER, LEFT, RIGHT, FULL, CROSS. Then check by running them.
3. (30m) NULL semantics. In psql run: `SELECT NULL = NULL, NULL IS NULL, NULL IS DISTINCT FROM NULL, 1 NOT IN (2, NULL), COALESCE(NULL, 5), NULLIF(5, 5);`. Write the 3-valued truth table for AND/OR/NOT from memory and compare with the docs.
4. (40m) Window functions: tutorial window section, then the window function list in the functions chapter. Using the tutorial `empsalary` table run `ROW_NUMBER`, `RANK`, `DENSE_RANK`, `LAG`, `SUM(...) OVER (ORDER BY ...)`, `PARTITION BY`. Predict the output before each run.
5. (30m) CTEs and recursive CTEs: generate 1..10 with `WITH RECURSIVE`, then build an org-chart traversal on a small `employees(id, manager_id)` table.
6. (25m) pgexercises.com: do 5 problems from "Joins and Subqueries" and 3 from "Aggregates". Skim AlgoMaster SQL roadmap and tick topics you haven't met.

**Tips and common problems**
- `LEFT JOIN` results silently become `INNER JOIN` → you filtered the right table in `WHERE` → move the condition into `ON`.
- `GROUP BY` error "column must appear in GROUP BY" → every non-aggregated selected column needs grouping (or use a window function).
- `COUNT(col)` ignores NULLs, `COUNT(*)` counts rows → check which one you meant.
- Window functions can't be used in `WHERE` → wrap in a CTE/subquery, then filter.

**Done when**
- [ ] You wrote the join-type table and the NULL truth table from memory, then verified them.
- [ ] You ran at least one `RANK` vs `DENSE_RANK` example and can explain the difference.
- [ ] 8 pgexercises problems completed.

#### S2 Build (3h): seed ShopFlow data and write 20 analytical queries
1. (20m) Create `sql/00_schema.sql` in the `shopflow` database (analytics schema, simplified): `customers(id, name, country, created_at timestamptz)`, `categories(id, name)`, `products(id, category_id, name, price numeric(12,2))`, `orders(id, customer_id, status, created_at timestamptz, total numeric(12,2))`, `order_items(order_id, product_id, quantity, unit_price)`. Add PKs and FKs.
2. (25m) Create `sql/01_seed.sql` with `generate_series`. Make results repeatable with `SELECT setseed(0.42);` at the top. Example for customers: `INSERT INTO customers SELECT g, 'Customer '||g, (ARRAY['DE','FR','US','IR','JP'])[1+floor(random()*5)::int], now() - (random()*730 || ' days')::interval FROM generate_series(1,1000) g;`. Seed 20 categories, 200 products, 100k orders (random customer, status, date in last 24 months), ~300k order items.
3. (15m) Sanity: `SELECT count(*)` for each table; `ANALYZE;`. Commit the schema and seed scripts.
4. (90m) Write 20 queries in `sql/analytics/q01_*.sql … q20_*.sql`, 4–5 per theme, each with a comment header stating the question:
   - Joins/aggregates: top 10 customers by spend; revenue by country; products never sold (use `NOT EXISTS`).
   - Window: running revenue per month; rank products within each category; each customer's previous order date (`LAG`) and days between orders.
   - CTE: customers whose last order was >90 days ago; monthly cohort retention (first-order month vs later activity).
   - Conditional: `CASE`-based revenue buckets; `FILTER (WHERE …)` aggregates.
   - Recursive: category tree (add `parent_id`) and total revenue per subtree.
   For each, save an "expected" check: a second file/comment with the row count and the first row's values (deterministic because of `setseed`).
5. (20m) Pick two queries written with subqueries and rewrite as CTEs (or vice versa); run both and compare `EXPLAIN` row estimates. Note which is clearer.
6. (10m) Commit and open a PR "SQL analytics pack".

**Tips and common problems**
- Different results on re-seed → `setseed` must be called in the same session before each `random()` use; re-run the whole seed script, not parts.
- `generate_series` with timestamps: `generate_series(ts1, ts2, interval '1 day')`. Casting `random()` results to int needs `floor()` first.
- Revenue doesn't match → you joined `orders` to `order_items` and then summed `orders.total` (fan-out duplicates). Sum at the correct grain.
- Seq scans everywhere are fine here; indexing comes in Week 23.

**Done when**
- [ ] `psql -f sql/00_schema.sql -f sql/01_seed.sql` rebuilds the data from zero.
- [ ] 20 query files exist, each with a documented expected row count.
- [ ] You can explain the fan-out trap with one example.

#### S3 Lab (2h): RL-80, RL-78, RL-79
1. (35m) **RL-80 `NOT IN` with a NULL**
   - Setup: `CREATE TABLE scratch.a(id int); CREATE TABLE scratch.b(id int); INSERT INTO scratch.a VALUES (1),(2),(3); INSERT INTO scratch.b VALUES (2),(NULL);`
   - Trigger: `SELECT * FROM scratch.a WHERE id NOT IN (SELECT id FROM scratch.b);` (expect 0 rows although 1 and 3 are not in b).
   - Observe: `EXPLAIN` shows a `Filter: (NOT (hashed SubPlan 1))`. Evaluate `1 NOT IN (2, NULL)` → NULL, so the row is dropped.
   - Fix: `WHERE NOT EXISTS (SELECT 1 FROM scratch.b WHERE b.id = a.id)` → returns 1 and 3.
   - Cause (one sentence): `x NOT IN (…NULL…)` is UNKNOWN, never true.
2. (35m) **RL-78 `timestamp` vs `timestamptz`**
   - Setup: `CREATE TABLE scratch.ev(id int, t_naive timestamp, t_tz timestamptz); SET timezone='UTC'; INSERT INTO scratch.ev VALUES (1, '2025-03-30 12:00', '2025-03-30 12:00');`
   - Trigger: `SET timezone='America/New_York'; SELECT * FROM scratch.ev;` then `SET timezone='Asia/Tehran'; SELECT * FROM scratch.ev;`.
   - Observe: `t_naive` always prints `12:00`; `t_tz` shifts with the session zone. Also run `SELECT t_tz AT TIME ZONE 'UTC' FROM scratch.ev;`.
   - Fix: use `timestamptz` for event times, store/compare in UTC, convert at the edge. In Java map to `Instant`/`OffsetDateTime`.
   - Cause: `timestamp` has no zone so it's an ambiguous wall-clock reading; `timestamptz` stores an absolute instant.
3. (40m) **RL-79 UUID v4 vs v7**
   - Setup: `CREATE TABLE scratch.t_v4(id uuid PRIMARY KEY DEFAULT gen_random_uuid(), pad text);` and `scratch.t_v7` with `DEFAULT uuidv7()` (native in PostgreSQL 18; on 17 generate v7 in the app or via an extension — verify against current docs).
   - Trigger: `\timing on`, then `INSERT INTO scratch.t_v4(pad) SELECT 'x' FROM generate_series(1,5000000);` and the same for `t_v7`. (If it's too slow, use 2M and note it.)
   - Observe: compare timings, `SELECT pg_size_pretty(pg_relation_size('scratch.t_v4_pkey')), pg_size_pretty(pg_relation_size('scratch.t_v7_pkey'));`, and cache behaviour with `EXPLAIN (ANALYZE, BUFFERS)` on a range scan.
   - Fix/decision: prefer time-ordered ids (v7) or bigint sequences for hot insert tables; keep v4 where unguessability matters and insert volume is small.
   - Cause: random keys hit random index pages (cache misses, page splits); time-ordered keys append to the right edge.
4. (10m) Write one line per lab in a scratch note (cause, signal, fix).

**Tips and common problems**
- psql in autocommit mode hides transactions → fine here, but remember it for Week 24.
- Inserting 5M rows may take minutes → run the two inserts one after the other and don't run other work at the same time.
- `SET timezone` is per session → reconnecting resets it.

**Done when**
- [ ] Three repros saved in `reality-lab/` with commands and observed output.
- [ ] You can state the one-sentence cause for each without looking.

#### S4 Review + Write (2h)
1. (30m) Write ledger entries RL-78, RL-79, RL-80 using the template in `reality-lab/LEDGER.md`.
2. (20m) Make 15 flashcards: join types, NULL logic, window frame defaults, `COUNT(*)` vs `COUNT(col)`, `UNION` vs `UNION ALL`.
3. (20m) Write `docs/sql-notes.md`: explain `COUNT(*)` vs `COUNT(col)` vs `COUNT(DISTINCT col)` with a 5-row example showing three different answers.
4. (30m) Retrieval practice: re-write three of your 20 queries from a blank file, no peeking. Compare with the originals.
5. (10m) Say each ledger answer aloud in under one minute.
6. (10m) Retro (3 lines in `retro.md`).

**Done when**
- [ ] 3 ledger entries committed; flashcards created; retro written.

**Self-check**
1. Why does `x NOT IN (…NULL…)` yield no rows?
2. What can a window function do that `GROUP BY` can't?
3. When would you use a recursive CTE?
4. Why is `timestamptz` the default choice for events?

<details><summary>Answers</summary>

1. The comparison with NULL is UNKNOWN, so `NOT IN` is never TRUE; use `NOT EXISTS`.
2. Compute per-row values over a set of rows (rank, running total, previous row) without collapsing rows.
3. Hierarchies/graphs of unknown depth (org charts, category trees) or generating series.
4. It stores an absolute instant, so it's unambiguous across zones and DST; `timestamp` is just a wall-clock reading.
</details>

**If stuck:** retry the join exercises on tiny tables of 3 rows; for `generate_series` issues test the expression alone with `SELECT` before using it in `INSERT`.
**Output:** schema/seed scripts, 20 analytic queries, RL-78/79/80 entries.

---

## Week 22 — Data modeling and JPA mapping depth
**Outcome:** schema encodes the rules; mapping choices are deliberate.
**Before you start:** the Phase 2 Spring Boot project runs with Postgres Testcontainers. Add to `application-test.yml`: `logging.level.org.hibernate.SQL=DEBUG` and `spring.jpa.properties.hibernate.generate_statistics=true`. Optional: add `datasource-proxy` (verify coordinates) to count statements in tests.

#### S1 Learn (3h): constraints, normalization, persistence context
1. (30m) Read PostgreSQL docs "Data Definition", constraints section (check, not-null, unique, primary key, foreign key, exclusion). Run an example of each in `scratch`, including a violating insert.
2. (25m) Read DDIA ch. 2 (relational vs document model, normalization discussion). From memory, explain 1NF, 2NF, 3NF/BCNF with a ShopFlow example (e.g., storing `product_name` in `order_items`).
3. (20m) Soft delete vs history table vs audit log. Search for two reputable articles; write a 5-row decision table (when each fits, cost, query impact).
4. (35m) Read Hibernate User Guide: "Persistence Context" and "Flushing". Draw the entity lifecycle (transient → managed → detached → removed) and mark which operations move between states.
5. (25m) Read Vlad Mihalcea on entity `equals/hashCode`. Summarize the 3 viable strategies (business key, constant hashCode with id equality, id-only with care) in your own words.
6. (25m) Read Vlad on `IDENTITY` vs `SEQUENCE` and batching; note `hibernate.jdbc.batch_size`, `order_inserts`, pooled optimizer, `allocationSize`.
7. (20m) Quiz yourself: close everything and answer the self-check questions below on paper.

**Tips and common problems**
- Confusing a unique *index* with a unique *constraint* → both enforce uniqueness in Postgres; constraints are visible to tooling and FKs.
- Reading about flush modes in the abstract won't stick → you'll watch it in the S3 lab; just capture the questions.

**Done when**
- [ ] Lifecycle diagram drawn and photographed into `docs/`.
- [ ] Decision table for soft delete vs history committed.

#### S2 Build (3h): constraints, audit trail and ID strategy
1. (20m) List ShopFlow invariants in `docs/invariants.md`: unique SKU, stock ≥ 0, price ≥ 0, quantity > 0, status in an allowed set, every order item references an order and product, one active reservation per (order, sku).
2. (50m) **Test first:** write a Testcontainers integration test per invariant asserting that a violating insert fails (`assertThatThrownBy(...).isInstanceOf(DataIntegrityViolationException.class)`). Run → red. Then write Flyway `V10__constraints.sql` with `ADD CONSTRAINT … CHECK/UNIQUE/FOREIGN KEY`. For existing data, use `NOT VALID` then `VALIDATE CONSTRAINT` (you'll use this again in Week 27). Run → green.
3. (30m) Audit columns: `created_at`, `updated_at` as `timestamptz NOT NULL DEFAULT now()`. Choose either JPA auditing (`@EnableJpaAuditing`, `@CreatedDate`) or a DB trigger; write one test proving `updated_at` changes on update.
4. (40m) Add `order_status_history(id, order_id, from_status, to_status, changed_at, changed_by)`. Write history rows in the same transaction as the status change. Test: a failed status change leaves no history row.
5. (30m) ID strategy: switch entities to a `SEQUENCE` generator with `allocationSize=50` and a DB sequence `INCREMENT BY 50` (they must match), or app-generated UUIDv7. Add a test inserting 1,000 rows and assert prepared-statement count is low with batching enabled (`spring.jpa.properties.hibernate.jdbc.batch_size=50`, `order_inserts=true`).
6. (10m) Commit; draft ADR-009 and ADR-010 bullet points.

**Tips and common problems**
- Flyway "checksum mismatch" → you edited an already-applied migration. Add a new migration instead; use `flyway repair` only on local throwaway DBs (verify against current docs).
- `duplicate key` or ID gaps after switching to sequences → `allocationSize` and DB `INCREMENT BY` don't match.
- Adding a CHECK to a populated table fails → clean the data, or add `NOT VALID` first and validate later.
- Batching "doesn't work" → `IDENTITY` ids disable it; check the generator and the logs.

**Done when**
- [ ] Every invariant has a failing-then-passing test.
- [ ] `order_status_history` written in the same transaction (test proves rollback).
- [ ] Batch insert test passes.

#### S3 Lab (2h): RL-53, RL-54, RL-55, RL-56, RL-57
Use a `@DataJpaTest`/`@SpringBootTest` with Testcontainers and SQL logging on. About 22 minutes each.
1. (22m) **RL-53 dirty checking**
   - Setup: load a `Product` by id inside `@Transactional`.
   - Trigger: call `setName("changed")` and do **not** call `save`; then `entityManager.flush()`.
   - Observe: an `UPDATE product …` appears in the SQL log.
   - Fix/lesson: managed entities are tracked; to avoid accidental writes use read-only transactions or DTOs for reads.
   - Cause: Hibernate compares the managed entity to its loaded snapshot at flush.
2. (22m) **RL-54 `persist` vs `merge`**
   - Setup: create an entity, save it, clear the persistence context (`em.clear()`), so it's detached.
   - Trigger: `em.persist(detached)` → exception "detached entity passed to persist". Then `repository.save(detached)` (merge path).
   - Observe: merge issues a `SELECT` then `UPDATE`, and returns a *different managed instance*; your detached variable is still detached.
   - Fix: always use the return value of `save`.
   - Cause: `persist` is for new entities; `merge` copies state onto a managed instance.
3. (22m) **RL-55 entity `equals/hashCode` with generated ID**
   - Setup: entity with `equals/hashCode` based on `id` (generated).
   - Trigger: `Set<Item> s = new HashSet<>(); Item i = new Item(); s.add(i); repo.save(i); s.contains(i)`.
   - Observe: `false` (hash changed after id assigned).
   - Fix: use a business key (e.g., SKU) or a stable strategy; assert the fix in a test.
   - Cause: `hashCode` changed while the object sat in a hash-based collection.
4. (22m) **RL-56 cascade and `orphanRemoval`**
   - Setup: `Order` with `@OneToMany(mappedBy="order", cascade=ALL)` items.
   - Trigger A: remove an item from `order.getItems()` without `orphanRemoval` → no `DELETE`. Trigger B: add `orphanRemoval=true` → a `DELETE` appears. Trigger C: accidentally put `CascadeType.REMOVE` on a `@ManyToOne` → deleting an item deletes its order.
   - Fix: cascade only from aggregate root to owned children; keep both sides in sync with helper methods.
   - Cause: removal from a collection only unlinks unless `orphanRemoval` is set.
5. (22m) **RL-57 `IDENTITY` disables batching**
   - Setup: two entities, one with `GenerationType.IDENTITY`, one with `SEQUENCE` and `allocationSize=50`; set `hibernate.jdbc.batch_size=50`, `order_inserts=true`.
   - Trigger: insert 10,000 of each in one transaction.
   - Observe: Hibernate statistics `getPrepareStatementCount()`/`getEntityInsertCount()` and wall time. For the pgJDBC driver also try `reWriteBatchedInserts=true` on the JDBC URL (verify).
   - Fix: sequences with a pooled allocation size.
   - Cause: with `IDENTITY` Hibernate must run each insert immediately to learn the id.
6. (10m) Write the cause/signal/fix line for each.

**Tips and common problems**
- A test annotated `@Transactional` rolls back and may never flush → call `em.flush()` explicitly to see SQL.
- `@DataJpaTest` replaces your DB with an embedded one by default → use `@AutoConfigureTestDatabase(replace = NONE)` with Testcontainers.
- Testcontainers startup is slow → use a shared static container or `.withReuse(true)` (verify config) to save time.

**Done when**
- [ ] Five failing/observed repros committed under `reality-lab/` with SQL log excerpts.

#### S4 Review + Write (2h)
1. (40m) Ledger entries RL-53 … RL-57 and RL-79 (carried from Week 21 if not done).
2. (20m) ADR-009 "Constraints live in the database as well as the code" (context, decision, consequences, rejected: validate only in Java).
3. (20m) ADR-010 "ID strategy" (sequence+pooled vs UUIDv7, with the measured numbers from RL-57 and RL-79).
4. (15m) Flashcards: entity states, `persist` vs `merge`, flush triggers.
5. (15m) Retrieval: on a blank page redraw the lifecycle diagram and annotate which of RL-53/54/56 happen where.
6. (10m) Retro.

**Done when**
- [ ] ADR-009 and ADR-010 merged; six ledger entries; diagram redrawn from memory.

**Self-check**
1. Why constraints in the DB and not only in Java?
2. What does flush do and when does it happen?
3. Why can `IDENTITY` hurt bulk inserts?
4. Why is id-based `hashCode` risky for new entities?

<details><summary>Answers</summary>

1. Multiple writers, bugs and manual SQL bypass Java; the DB is the last line of defense and also enables optimizer guarantees.
2. It synchronizes pending changes to the DB (SQL statements); on commit, before queries touching changed tables (auto mode), or on explicit `flush()`.
3. The insert must execute immediately to obtain the id, so Hibernate can't batch inserts.
4. The id is null/changes after persist, so the hash changes while the object is in a `HashSet`/`Map`.
</details>

**If stuck:** reduce to one entity and one test; turn on `logging.level.org.hibernate.SQL=DEBUG` and read exactly which statements run and in what order.
**Output:** migrations, ADR-009/010, RL-53–57 and RL-79 entries.

---

## Week 23 — Indexes and query tuning
**Outcome:** reads execution plans and makes slow queries fast with evidence.
**Before you start:** make sure `ANALYZE` has run and `pg_stat_statements` is enabled. Create `docs/perf/` for plans.

#### S1 Learn (3h): B-trees and reading plans
1. (35m) Read UTI "Anatomy of an Index". Draw a B-tree with 8 keys on paper and show how a lookup and a range scan traverse it.
2. (35m) Read UTI "The Where Clause" (equality, ranges, functions, `LIKE`) and the start of "Sorting and Grouping" and "Partial Results". Write down three rules for when an index is *not* used.
3. (30m) Read PostgreSQL docs "Indexes": index types, multicolumn indexes, ordering, combining, unique, expression, partial, index-only scans/covering (`INCLUDE`). Run one example of each on `scratch` tables.
4. (30m) Read PostgreSQL docs "Using EXPLAIN". Run `EXPLAIN (ANALYZE, BUFFERS)` on the doc's examples. Annotate each plan node: cost, estimated vs actual rows, loops, `Buffers: shared hit/read`.
5. (30m) Watch ~30 minutes of the CMU 15-445 lecture on tree indexes/B+ trees (and optionally the query execution lecture).
6. (20m) From memory write a "plan reading cheat sheet": Seq Scan, Index Scan, Index Only Scan, Bitmap Index/Heap Scan, Nested Loop, Hash Join, Merge Join, Sort, HashAggregate. For each: when chosen, what makes it slow.

**Tips and common problems**
- "My index isn't used" on a tiny table → the planner correctly prefers a seq scan for small tables; test with realistic volumes (millions of rows).
- Costs are not milliseconds → compare plans by actual time and buffers.
- `EXPLAIN ANALYZE` on `INSERT/UPDATE/DELETE` really executes → wrap in `BEGIN; … ROLLBACK;`.

**Done when**
- [ ] B-tree drawing and plan-node cheat sheet committed.

#### S2 Build (3h): SF-19 keyset pagination and SF-20 slow-query hunt
1. (30m) Create `sql/10_seed_5m.sql`: 5M orders with a skewed customer distribution, e.g. `customer_id = 1 + floor(power(random(),3) * 1000000)::int`, `status` from a weighted array, `created_at` over 2 years, `total` random. Then `ANALYZE orders;` and check `SELECT pg_size_pretty(pg_total_relation_size('orders'));`.
2. (25m) **Test first (SF-19):** write an integration test for `GET /customers/{id}/orders?after=<cursor>&limit=20` that fetches page 1 and page 2, inserts a new row between calls, and asserts no duplicates or gaps. Run → red.
3. (25m) Implement keyset: `WHERE customer_id = ? AND (created_at, id) < (?, ?) ORDER BY created_at DESC, id DESC LIMIT ?` with an opaque cursor. Green. Save the baseline plan in `docs/perf/sf19-before.txt` (no index yet).
4. (35m) Add `CREATE INDEX CONCURRENTLY idx_orders_customer_created ON orders (customer_id, created_at DESC, id DESC);` in a Flyway migration (non-transactional). Re-run `EXPLAIN (ANALYZE, BUFFERS)`; save `sf19-after.txt`. **RL-65:** compare `OFFSET 1000000 LIMIT 20` vs keyset at the same position; note times.
5. (30m) **SF-20 hunt:** write five candidate slow queries (by status+date range, by customer, top products last 30 days, count by status, search by partial email). Run each with `EXPLAIN (ANALYZE, BUFFERS)`; pick the three slowest; fix each with the right index or a rewrite. **RL-66:** measure `SELECT count(*) FROM orders`; try `reltuples` estimate, an index-only scan after `VACUUM`, and a counter table; record trade-offs.
6. (25m) JPA side: **RL-59** replace a list endpoint's entity fetch with an interface/DTO projection and compare SQL (columns selected) and time. **RL-51** reproduce `join fetch` + `Pageable` → Hibernate warns `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory`. **RL-52** fetch two `List` collections → `MultipleBagFetchException`/Cartesian rows. Fix with a two-query approach (page ids first, then fetch) or `@BatchSize`/`default_batch_fetch_size`.
7. (10m) Commit with the plan files attached to the PR description.

**Tips and common problems**
- Index not picked even at 5M rows → stale stats (run `ANALYZE`), or the query shape/cast prevents it (see S3).
- `CREATE INDEX CONCURRENTLY` inside Flyway's transaction fails → run the migration non-transactionally (Flyway per-script config `executeInTransaction=false`, verify against current docs).
- Keyset skips rows with equal timestamps → always add the id as a tie-breaker in both `ORDER BY` and the cursor.
- Comparing timings with cold cache → run each query twice; look at `Buffers: shared read` vs `hit`.

**Done when**
- [ ] SF-19 test is green and shows no duplicates/gaps.
- [ ] Before/after plans for three slow queries stored in `docs/perf/`.
- [ ] RL-51, RL-52, RL-59, RL-65, RL-66 observations written down.

#### S3 Lab (2h): RL-60 … RL-63
Use the 5M-row `orders` table (add a `users(id, email, phone varchar, …)` table with 1M rows for the cast and function labs). About 25 minutes each.
1. (25m) **RL-60 index ignored due to a type cast**
   - Setup: `CREATE INDEX ON orders (customer_id);` (column is `int`).
   - Trigger: `EXPLAIN (ANALYZE) SELECT * FROM orders WHERE customer_id = 42.0::numeric;`
   - Observe: `Seq Scan` with `Filter: ((customer_id)::numeric = 42.0)`. Same query with `= 42` uses the index.
   - Fix: pass parameters with the column's type (in JPA, check the Java parameter type, e.g., `BigDecimal` vs `Integer`).
   - Cause: Postgres casts the column, so the plain index can't be used.
2. (25m) **RL-61 function on the column**
   - Setup: `CREATE INDEX ON users (email);`
   - Trigger: `EXPLAIN (ANALYZE) SELECT * FROM users WHERE lower(email) = 'a@b.com';`
   - Observe: `Seq Scan` with `Filter: (lower(email) = …)`.
   - Fix: `CREATE INDEX ON users (lower(email));` and re-run: `Index Scan`. Also try `date_trunc('day', created_at) = …` vs a range predicate.
   - Cause: the index stores raw values, not the function result.
3. (25m) **RL-62 composite index column order**
   - Setup: `CREATE INDEX idx_ab ON orders (status, customer_id);`
   - Trigger: filter by `customer_id` alone; then by `status` alone; then both.
   - Observe: `customer_id` alone → poor (bitmap/seq scan or full index scan); `status` alone → low selectivity; both → good.
   - Fix: lead with the most useful equality column for your queries (usually higher selectivity, used alone); recreate in the right order or add a second index.
   - Cause: a B-tree is sorted by the first column first.
4. (25m) **RL-63 partial and covering indexes**
   - Setup: `CREATE INDEX idx_pending ON orders (created_at) WHERE status = 'PENDING';` and `CREATE INDEX idx_cust_cover ON orders (customer_id) INCLUDE (total);` then `VACUUM (ANALYZE) orders;`
   - Trigger: `SELECT created_at FROM orders WHERE status='PENDING' ORDER BY created_at LIMIT 100;` and `SELECT total FROM orders WHERE customer_id = 42;`
   - Observe: `Index Only Scan … Heap Fetches: 0` (after vacuum) for the covering case; the partial index is much smaller (`\di+`).
   - Fix/lesson: partial indexes for hot subsets, covering indexes to avoid heap visits.
   - Cause: the visibility map lets Postgres skip the heap for all-visible pages.
5. (20m) Record cause/signal/fix for each in a scratch note; commit scripts.

**Tips and common problems**
- Heap Fetches stays high → run `VACUUM` so the visibility map is updated.
- You can't see a difference → table too small or cached; use the 5M table and run twice.
- Adding too many indexes → each index slows writes; drop experimental ones (`DROP INDEX`) when done.

**Done when**
- [ ] Four repros with before/after plan excerpts saved.

#### S4 Review + Write (2h)
1. (40m) Ledger entries for RL-51, 52, 59, 60, 61, 62, 63, 65, 66.
2. (25m) Write `docs/perf/query-debugging.md`: slow query? check plan → stats (`ANALYZE`) → index (shape, order, cast, function) → rewrite → verify with buffers.
3. (15m) Flashcards: plan nodes, index rules, keyset vs offset.
4. (30m) Timed drill: ask a peer or generate three unseen slow queries on the 5M table; diagnose each in ≤ 10 minutes using only the checklist.
5. (10m) Retro.

**Done when**
- [ ] Debugging checklist committed; drill completed; nine ledger entries.

**Self-check**
1. Why does `WHERE lower(email)=…` skip a normal index?
2. How does column order matter in a composite index?
3. What does a large gap between estimated and actual rows suggest?
4. Why is keyset pagination faster than `OFFSET` at deep pages?

<details><summary>Answers</summary>

1. The index holds raw `email` values; the query needs `lower(email)`, so you need an expression index.
2. The tree is sorted by the first column, then the second; queries filtering only on later columns can't seek efficiently.
3. Stale or insufficient statistics (or correlated columns), leading to bad plan choices; run `ANALYZE`, consider extended statistics.
4. Keyset seeks directly to the position by key; `OFFSET` must read and discard all skipped rows.
</details>

**If stuck:** paste the plan into explain.depesz.com or pev2 (no secrets/data); compare estimated vs actual rows node by node, starting from the deepest node.
**Output:** SF-19, SF-20 with plans attached, nine ledger entries.

---

## Week 24 — Transactions, MVCC and isolation
**Outcome:** can predict anomalies and pick the right isolation or locking strategy.
**Before you start:** open **two psql terminals** (call them **A** and **B**) connected to `shopflow`. Check `SHOW transaction_isolation;` (default `read committed`). Create `scratch.counter(id int primary key, v int)`, `scratch.accounts(id int primary key, balance int)`, `scratch.doctors(name text primary key, on_call boolean)`. Never use autocommit tricks: always type `BEGIN;` and `COMMIT;` explicitly.

#### S1 Learn (3h): ACID, MVCC, isolation
1. (40m) Read DDIA ch. 7 (Transactions), first half: ACID, single- vs multi-object operations, weak isolation (read committed, snapshot isolation). Take notes of each anomaly with a one-line example.
2. (30m) Finish DDIA ch. 7: lost updates, write skew and phantoms, serializability (2PL, SSI).
3. (35m) Read PostgreSQL docs "Concurrency Control", "Transaction Isolation". From memory write a Postgres-specific table: isolation level × anomaly (note `READ UNCOMMITTED` behaves as `READ COMMITTED`; `REPEATABLE READ` is snapshot isolation).
4. (25m) Read "Explicit Locking", row-level locks: `FOR UPDATE`, `FOR NO KEY UPDATE`, `FOR SHARE`, `FOR KEY SHARE`, `NOWAIT`, `SKIP LOCKED`.
5. (30m) Watch ~30 minutes of the CMU 15-445 lecture on concurrency control / MVCC.
6. (20m) Skim Kleppmann's Hermitage results for PostgreSQL; list which anomalies each level allows.

**Tips and common problems**
- Mixing up "dirty read" and "non-repeatable read" → draw timelines with two lanes.
- Believing `SERIALIZABLE` means locks everywhere → Postgres uses SSI (optimistic detection, may abort).
- Notes get too long → keep a 1-page anomaly cheat sheet.

**Done when**
- [ ] One-page anomaly/isolation table made from memory, then corrected.

#### S2 Build (3h): three stock-reservation strategies, one shared test
1. (20m) Schema: `stock(sku text primary key, qty int not null check (qty >= 0), version int not null default 0)` and `reservation(id bigserial primary key, sku text, order_id bigint, qty int not null)`; seed `sku='A'`, `qty=50`.
2. (30m) **Test first:** JUnit test with `ExecutorService(100)` and a `CountDownLatch` start gate; 100 workers each try to reserve 1 unit. Assert exactly 50 succeed, final `qty = 0`, never negative. Run → red (no implementation). Set Hikari `maximum-pool-size` ≥ 100 or workers will be serialized by the pool.
3. (35m) Variant A optimistic: `@Version` column; retry on `ObjectOptimisticLockingFailureException` with a small bounded loop (max 10, jittered sleep).
4. (35m) Variant B pessimistic: `@Lock(LockModeType.PESSIMISTIC_WRITE)` on a repository method (`SELECT … FOR UPDATE`); keep the transaction short.
5. (25m) Variant C atomic SQL: `UPDATE stock SET qty = qty - :n WHERE sku = :sku AND qty >= :n` and check the affected row count (1 = success, 0 = sold out).
6. (25m) Compare: run each 20 times; record throughput, retry counts and failures in a table in `docs/perf/reservation.md`.
7. (10m) Commit the three variants on three branches and the comparison doc on main.

**Tips and common problems**
- Test passes by luck → run it 20 times in a loop; concurrency bugs are probabilistic.
- All variants look equally fast → pool size or CPU-bound test harness; use more workers and ensure the DB is the bottleneck.
- Putting `@Transactional` on the test method makes all workers share one transaction → don't; each worker must open its own transaction through the service.
- Optimistic retries inside the same transaction don't help → the retry must start a **new** transaction.

**Done when**
- [ ] One shared concurrency test, green for all three variants, 20 runs in a row.
- [ ] Comparison table with numbers committed.

#### S3 Lab (2h): RL-73, RL-74, RL-75, RL-77
Run these in two psql terminals A and B.
1. (30m) **RL-73 lost update under `READ COMMITTED`**
   - Setup: `INSERT INTO scratch.counter VALUES (1, 0);`
   - Steps in order: **A:** `BEGIN; SELECT v FROM scratch.counter WHERE id=1;` (0). **B:** `BEGIN; SELECT v FROM scratch.counter WHERE id=1;` (0). **A:** `UPDATE scratch.counter SET v = 1 WHERE id=1; COMMIT;` **B:** `UPDATE scratch.counter SET v = 1 WHERE id=1; COMMIT;`
   - Observe: final `v = 1`, though two increments were intended.
   - Fix variants: `SET v = v + 1` (atomic), `SELECT … FOR UPDATE`, or `@Version` check. Re-run with `UPDATE … SET v = v + 1`: result 2.
   - Cause: both transactions computed the new value from a stale read and overwrote each other.
2. (30m) **RL-74 write skew**
   - Setup: `INSERT INTO scratch.doctors VALUES ('alice', true), ('bob', true);`
   - Steps: **A:** `BEGIN ISOLATION LEVEL REPEATABLE READ; SELECT count(*) FROM scratch.doctors WHERE on_call;` (2). **B:** same. **A:** `UPDATE scratch.doctors SET on_call=false WHERE name='alice'; COMMIT;` **B:** `UPDATE scratch.doctors SET on_call=false WHERE name='bob'; COMMIT;`
   - Observe: zero doctors on call. Both commits succeed because they updated different rows.
   - Fix: repeat with `ISOLATION LEVEL SERIALIZABLE`: the second commit fails with `ERROR: could not serialize access due to read/write dependencies` (SQLSTATE 40001). Or lock the rows read (`FOR UPDATE`) or enforce via a constraint.
   - Cause: each transaction made a decision from a snapshot that the other invalidated.
3. (25m) **RL-75 serialization failure and retry**
   - Setup: wrap the RL-74 logic in a Java method using `Isolation.SERIALIZABLE`.
   - Trigger: run two threads concurrently (latch). One throws a `CannotSerializeTransactionException`/SQLState `40001`.
   - Fix: a bounded retry loop that re-runs the whole transaction (new connection/tx), with jitter; add a test that both operations eventually succeed or one is refused by business rule.
   - Cause: SSI aborts one transaction rather than allowing a non-serializable outcome.
4. (25m) **RL-77 deadlock from inconsistent lock order**
   - Setup: `INSERT INTO scratch.accounts VALUES (1,100),(2,100);`
   - Steps: **A:** `BEGIN; UPDATE scratch.accounts SET balance=balance-10 WHERE id=1;` **B:** `BEGIN; UPDATE scratch.accounts SET balance=balance-10 WHERE id=2;` **A:** `UPDATE scratch.accounts SET balance=balance+10 WHERE id=2;` (waits) **B:** `UPDATE scratch.accounts SET balance=balance+10 WHERE id=1;`
   - Observe: after about `deadlock_timeout` (1s) one session gets `ERROR: deadlock detected` with DETAIL listing both processes; also read it in `docker logs pg-lab`.
   - Fix: always lock in a fixed order (`SELECT … WHERE id IN (1,2) ORDER BY id FOR UPDATE`) before updating.
   - Cause: circular wait, each holding a lock the other needs.
5. (10m) Write cause/signal/fix lines.

**Tips and common problems**
- Nothing blocks → one terminal is in autocommit; check you typed `BEGIN`. After an error psql leaves the transaction aborted: run `ROLLBACK;`.
- Deadlock not detected → wait ≥ `deadlock_timeout`; check `SHOW deadlock_timeout;`.
- Seeing a different isolation level than expected → `SHOW transaction_isolation;` inside the transaction.
- Sessions stuck → `SELECT pid, state, wait_event_type, query FROM pg_stat_activity WHERE datname='shopflow';` then `SELECT pg_terminate_backend(<pid>);`.

**Done when**
- [ ] Each lab has a saved transcript with the exact order of statements and the observed error/result.

#### S4 Review + Write (2h)
1. (35m) Ledger entries RL-73, 74, 75, 77.
2. (25m) Draw the Postgres anomaly × isolation level table; add which fix prevents each in practice.
3. (20m) Redo RL-74 (write skew) on a blank terminal without notes; then explain it aloud in under a minute.
4. (15m) Flashcards: anomalies, lock modes, error codes `40001` and `40P01`.
5. (15m) Record yourself explaining "lost update: three ways to fix"; listen back and note filler or gaps.
6. (10m) Retro.

**Done when**
- [ ] Four ledger entries; redo succeeded without notes.

**Self-check**
1. What does `READ COMMITTED` guarantee and not guarantee?
2. Why does `SERIALIZABLE` require retries?
3. How do you prevent deadlocks by design?
4. Which anomaly does `REPEATABLE READ` in Postgres still allow?

<details><summary>Answers</summary>

1. Each statement sees only committed data as of its start; it does not prevent non-repeatable reads, lost updates or write skew across statements.
2. SSI detects dangerous dependencies and aborts one transaction; the client must retry the whole transaction.
3. Acquire locks in a consistent global order, keep transactions short, avoid user interaction inside them.
4. Write skew (and serialization anomalies), because it's snapshot isolation.
</details>

**If stuck:** write the timeline of A/B statements on paper first; reproduce with the smallest table; check `pg_locks` joined with `pg_stat_activity` for who waits on whom.
**Output:** three reservation variants with a shared concurrency test, RL-73/74/75/77 entries.

---

## Week 25 — Transactions in Spring and correctness under load
**Outcome:** controls transaction boundaries, locking and connection use deliberately.
**Before you start:** turn on `logging.level.org.springframework.transaction.interceptor=TRACE`, `logging.level.org.springframework.orm.jpa=DEBUG`, and expose Hikari metrics (`management.endpoints.web.exposure.include=metrics,health`; check `/actuator/metrics/hikaricp.connections.active`). Have WireMock available for slow upstream simulations.

#### S1 Learn (3h): Spring transactions, locking, pool sizing
1. (35m) Read Spring Framework "Transaction Management", declarative section: how the proxy works, `@Transactional` attributes, rollback rules. Write when proxies are bypassed (self-invocation, private methods).
2. (25m) Propagation: `REQUIRED`, `REQUIRES_NEW`, `NESTED`, `SUPPORTS`, `MANDATORY`, `NEVER`. For each write one realistic use and one trap.
3. (30m) Read Hibernate User Guide "Locking" (optimistic and pessimistic modes). Summarize `@Version` behavior and which exception you'll see.
4. (25m) Read HikariCP wiki "About Pool Sizing". Note that the formula is a starting point and the key idea is that a small pool + short transactions beats a huge pool.
5. (30m) Idempotent writes: unique constraints as guards, `INSERT … ON CONFLICT DO NOTHING/UPDATE`. Write two examples for ShopFlow reservations.
6. (35m) Hands-on: call three service methods (REQUIRED→REQUIRED, REQUIRED→REQUIRES_NEW, self-call) and read the TRACE logs to find "Getting transaction", "Suspending current transaction", "Completing transaction". Annotate each log line in a note.

**Tips and common problems**
- No transaction logs visible → the logger name is wrong or the method is not called through the proxy.
- `@Transactional` on a `private`/`final` method silently does nothing → make it public on a Spring bean and call it from another bean.
- Pool-sizing advice from blogs conflicts → trust measurements from your own load test (Week 26 lab).

**Done when**
- [ ] Annotated log excerpt showing suspend/resume of a transaction.
- [ ] Propagation table written.

#### S2 Build (3h): SF-18 inventory reservation
1. (20m) Write a short decision memo (`docs/reservation-decision.md`) from Week 24 numbers: atomic UPDATE for single-row counters; `FOR UPDATE` when several rows must change together; optimistic when contention is low.
2. (40m) Implement `ReservationService.reserve(orderId, sku, qty)` with `@Transactional` at the application-service boundary. **Test first:** extend the Week 24 concurrency test to call the service (100 concurrent requests, 50 units). Red → green.
3. (40m) Idempotency: `UNIQUE (order_id, sku)` on `reservation`; repeated `reserve` with the same order/sku returns the same reservation without decrementing twice. Test double call and concurrent double call.
4. (30m) Release path: cancelling an order restores stock exactly once (guard against double release). Test it.
5. (30m) Run the concurrency tests 20 times: `for i in $(seq 20); do mvn -q test -Dtest=ReservationConcurrencyTest || break; done`. Fix any flake before moving on.
6. (20m) Open the PR with the memo and test evidence.

**Tips and common problems**
- Idempotency check in Java (select then insert) races → rely on the unique constraint and handle `DuplicateKeyException`.
- `@Transactional` on a controller → keep transactions in application services so web concerns don't extend them.
- Test passes locally but not on CI → pool size/CPU differences; make the pool explicit in test config.

**Done when**
- [ ] 100 concurrent requests never oversell; 20 consecutive green runs.
- [ ] Duplicate reserve and duplicate release are no-ops.

#### S3 Lab (2h): RL-29, RL-30, RL-31, RL-58, RL-67
About 20 minutes each.
1. (20m) **RL-29 `REQUIRES_NEW` surprise**
   - Setup: `OrderService.place()` (`@Transactional`) calls `AuditService.record()` (`@Transactional(REQUIRES_NEW)`, a different bean) then throws.
   - Trigger: call `place()`.
   - Observe: the order row is rolled back but the audit row is committed; TRACE shows "Suspending current transaction" and two connections.
   - Fix/lesson: use `REQUIRES_NEW` only when independent commit is intended (audit/outbox-like); beware pool exhaustion (a second connection per call).
   - Cause: the inner transaction commits independently of the outer.
2. (20m) **RL-30 `readOnly=true` expectations**
   - Setup: a `@Transactional(readOnly = true)` method that modifies a managed entity, and another that runs a native `UPDATE`.
   - Trigger: call both.
   - Observe: the entity modification is not flushed (Hibernate flush mode manual) while a native write may raise `cannot execute UPDATE in a read-only transaction` on Postgres (behavior depends on versions; verify).
   - Fix: use `readOnly` for read paths only; it's an optimization hint, not a security guard.
   - Cause: Spring sets read-only hints (flush mode, JDBC read-only), they don't make the app safe.
3. (20m) **RL-31 remote call inside a transaction**
   - Setup: set `spring.datasource.hikari.maximum-pool-size=5`; a `@Transactional` method does a DB write then calls a WireMock endpoint with a 5 s delay.
   - Trigger: fire 10 parallel requests (k6 or a Java latch test).
   - Observe: `SELECT pid, state, now()-xact_start AS age, query FROM pg_stat_activity WHERE datname='shopflow';` shows `idle in transaction`; later requests fail with `Connection is not available, request timed out after …ms`; `hikaricp.connections.pending` rises.
   - Fix: do the remote call before or after the transaction (or use an outbox).
   - Cause: the transaction holds a pooled connection while waiting on the network.
4. (20m) **RL-58 `@Version` conflict and retry**
   - Setup: entity with `@Version`.
   - Trigger: two threads load the same row, both modify, both save.
   - Observe: the second gets `ObjectOptimisticLockingFailureException`; the SQL shows `… WHERE id=? AND version=?` updating 0 rows.
   - Fix: bounded retry in a new transaction, or return 409 to the client.
   - Cause: the version check detects the concurrent write.
5. (20m) **RL-67 idle in transaction blocks vacuum**
   - Steps: **A:** `BEGIN; SELECT * FROM orders LIMIT 1;` (leave open). **B:** `UPDATE orders SET total = total WHERE id <= 200000; VACUUM (VERBOSE) orders;`
   - Observe: VACUUM output says dead row versions "cannot be removed yet"; `SELECT pid, state, backend_xmin, xact_start FROM pg_stat_activity WHERE state LIKE 'idle in%';`
   - Fix: end the transaction; set `idle_in_transaction_session_timeout = '30s'`.
   - Cause: an old snapshot (xmin) prevents cleanup of dead tuples.
6. (20m) Wrap up: cause/signal/fix lines and commit scripts.

**Tips and common problems**
- RL-29 shows no second transaction → the call went through `this.` (same bean); move it to another bean.
- RL-31 no starvation → pool larger than the parallelism; set pool to 5 and parallelism to 10+.
- RL-67 still vacuums fine → the open transaction began after the updates; start A first.

**Done when**
- [ ] Five repros with logs/queries saved and one-sentence causes written.

#### S4 Review + Write (2h)
1. (35m) Ledger entries RL-29, 30, 31, 58, 67 (plus RL-51/52 if carried over).
2. (25m) ADR-011 "Transaction boundaries: application services; no remote calls inside transactions" (decision, consequences, outbox alternative).
3. (15m) A one-page transaction checklist for code review (boundaries, propagation, locks, retries, remote calls).
4. (15m) Flashcards: propagation modes, `@Version`, `idle in transaction`.
5. (20m) Redo RL-31 from scratch against a clock; target 10 minutes to reproduce and explain.
6. (10m) Retro.

**Done when**
- [ ] ADR-011 merged; checklist added to the PR template; five ledger entries.

**Self-check**
1. Why avoid remote calls inside a transaction?
2. When prefer optimistic locking over pessimistic?
3. How does pool size relate to DB `max_connections`?
4. What does `REQUIRES_NEW` cost?

<details><summary>Answers</summary>

1. The transaction holds a connection and locks while waiting on a slow, unreliable network, exhausting the pool and extending lock times.
2. Low contention and when conflicts can be retried cheaply; pessimistic when conflicts are frequent or retries are expensive.
3. The sum of all app instances' pool sizes must stay below `max_connections` (minus admin headroom); use a pooler (PgBouncer) for many instances.
4. A second connection and a separate commit; deadlock/exhaustion risk and partial-success semantics.
</details>

**If stuck:** watch `/actuator/metrics/hikaricp.connections.*` and `pg_stat_activity` side by side to see who holds connections.
**Output:** SF-18, ADR-011, RL-29/30/31/58/67 entries.

---

## Week 26 — NoSQL, search, scaling and database operations
**Outcome:** picks a data store by access pattern; understands replication and partitioning limits.
**Before you start:** have Docker ready for Redis (`docker run -d --name redis -p 6379:6379 redis:7`), OpenSearch/Elasticsearch single node for dev only (security disabled; verify image flags in current docs), and a second Postgres for replication (outline in S3 step 3).

#### S1 Learn (3h): choosing stores by access pattern
1. (30m) Redis docs "Data types": run `redis-cli` examples for strings, hashes, sets, sorted sets, streams. Build a top-10 leaderboard with `ZADD`/`ZREVRANGE`.
2. (25m) Redis patterns: cache with TTL, `SET key val NX EX 10` as a lock, `INCR` + `EXPIRE` as a rate limiter. Read why distributed locks are tricky (fencing tokens) and note the limits.
3. (30m) MongoDB docs "Data modeling": embedding vs referencing. Sketch an order document and list two queries that become awkward.
4. (30m) Access-pattern-first modeling: read the intro chapters of *The DynamoDB Book* (or the DynamoDB docs on key design). Model "orders by customer" and "orders by status/date" with partition/sort keys.
5. (30m) DDIA ch. 5 (replication: leader/follower, lag, read-your-writes) and ch. 6 (partitioning: key range vs hash, hot spots). Write two failure stories each (what goes wrong for users).
6. (20m) Elasticsearch/OpenSearch docs "Text analysis": analyzer = char filters + tokenizer + token filters; sketch an inverted index for three product names.
7. (15m) Write a comparison table: store × best for × weak at × ShopFlow use. Add CAP/PACELC in one paragraph from memory.

**Tips and common problems**
- Treating Redis as a durable primary store → default persistence settings may lose recent writes; decide per use case.
- NoSQL modeling by copying relational tables → design from queries instead.
- Infinite reading of replication theory → stop at the failure stories; the lab makes it concrete.

**Done when**
- [ ] Comparison table committed.
- [ ] You can explain read-your-writes and hot partitions in two sentences each.

#### S2 Build (3h): SF-21 catalog search
1. (20m) Start OpenSearch/Elasticsearch single node (dev). Verify: `curl -s localhost:9200`. Record the version.
2. (30m) Create the index mapping: `name` as `text` (with a standard analyzer), `sku` and `category` as `keyword`, `price` as `scaled_float`/`double`. Create it with `PUT /products` via curl; save mapping JSON in the repo.
3. (40m) **Test first:** Testcontainers integration test that indexes 3 products and expects a query for a misspelled term ("wireles headfone") to match with fuzziness. Implement the indexer + search service. Green.
4. (30m) Sync strategy v1: a `@TransactionalEventListener(phase = AFTER_COMMIT)` upserts the product document. Document in comments why this can drop updates (search down after commit).
5. (30m) Reindex command: read products from Postgres in batches of 1,000 and send `_bulk` requests; make it idempotent (use product id as document id). Test: delete the index, run reindex, search works.
6. (20m) Staleness experiment: stop the search container, update a product, restart, observe the old data. Write `docs/search-sync.md` describing the stale window and the later fix (outbox/CDC, Phase 6/7).
7. (10m) Commit and open the PR.

**Tips and common problems**
- Search returns nothing right after indexing → the index is near-real-time; call `_refresh` in tests (or use `refresh=wait_for`).
- Mapping conflicts on re-create → delete and rebuild index or use an alias for zero-downtime reindexing.
- Testcontainers for Elasticsearch needs more memory/ports → set JVM options in the container config (verify per image).

**Done when**
- [ ] Fuzzy search test is green; reindex command works from scratch.
- [ ] `docs/search-sync.md` states the failure window.

#### S3 Lab (2h): RL-46, RL-64, RL-81, RL-68, RL-69
About 20 minutes each (RL-81 may take longer; start it first).
1. (25m) **RL-81 replica lag and read-your-writes**
   - Setup (verify against current docs): run a primary with `-c wal_level=replica -c max_wal_senders=5 -c hot_standby=on`; create a replication role; start a second container, take a base backup with `pg_basebackup -h <primary> -U repl -D <dir> -R -X stream`, and start it as a standby. Confirm with `SELECT * FROM pg_stat_replication;` on the primary and `SELECT pg_is_in_recovery();` on the replica.
   - Trigger: on the replica run `SELECT pg_wal_replay_pause();`, write a row on the primary, read it on the replica (missing). Then `SELECT pg_wal_replay_resume();`.
   - Observe: `replay_lag` in `pg_stat_replication`; the row appears after resume.
   - Fix: read from the primary right after a write, sticky sessions for N seconds, or wait for the replica's LSN to pass the write's LSN.
   - Cause: replication is asynchronous.
2. (20m) **RL-46 Tomcat vs Hikari vs DB limits**
   - Setup: endpoint doing `SELECT pg_sleep(0.1)`; Hikari `maximum-pool-size=10`; Tomcat default threads (200).
   - Trigger: k6 with 100 virtual users for 60 s.
   - Observe: throughput caps near `pool/service time = 10 / 0.1 = 100 req/s`; `hikaricp.connections.pending` climbs; latency grows.
   - Fix: size the pool to the DB's capacity and shorten transactions; scaling the web threads doesn't help.
   - Cause: threads queue for connections (Little's law).
3. (20m) **RL-64 stale statistics**
   - Setup: `CREATE TABLE scratch.t(id serial, k int); ALTER TABLE scratch.t SET (autovacuum_enabled=false); INSERT INTO scratch.t(k) SELECT 1 FROM generate_series(1,1000); ANALYZE scratch.t;`
   - Trigger: `INSERT INTO scratch.t(k) SELECT 2 FROM generate_series(1,2000000);` then `EXPLAIN (ANALYZE) SELECT * FROM scratch.t WHERE k=2;`
   - Observe: estimated rows far below actual (e.g., `rows=1` vs millions); a poor plan shape.
   - Fix: `ANALYZE scratch.t;` and re-run: estimates match; plan flips.
   - Cause: the planner used outdated statistics.
4. (20m) **RL-68 bloat after heavy updates**
   - Setup: `CREATE TABLE scratch.b AS SELECT g AS id, 0 AS x FROM generate_series(1,1000000) g; ALTER TABLE scratch.b SET (autovacuum_enabled=false);`
   - Trigger: run `UPDATE scratch.b SET x = x + 1;` five times.
   - Observe: `SELECT pg_size_pretty(pg_relation_size('scratch.b')), n_dead_tup FROM pg_stat_user_tables WHERE relname='b';` and `CREATE EXTENSION pgstattuple; SELECT * FROM pgstattuple('scratch.b');` shows high `dead_tuple_percent`.
   - Fix: `VACUUM (VERBOSE) scratch.b;` (space reusable but file doesn't shrink); `VACUUM FULL` shrinks but takes an exclusive lock; tune autovacuum.
   - Cause: MVCC keeps old row versions until vacuum.
5. (15m) **RL-69 `pg_stat_statements` hunt**
   - Trigger: `SELECT pg_stat_statements_reset();` then run your k6/test load.
   - Observe: `SELECT calls, round(total_exec_time) AS total_ms, round(mean_exec_time::numeric,2) AS mean_ms, rows, left(query,80) FROM pg_stat_statements ORDER BY total_exec_time DESC LIMIT 10;`
   - Fix: pick the top query by total time and look at its plan.
   - Cause (lesson): total time = calls × mean; a fast query called a million times can dominate.
6. (20m) Wrap-up: cause/signal/fix notes; save scripts.

**Tips and common problems**
- Replica setup fails → empty data directory required for `pg_basebackup`; check `pg_hba.conf` replication entry and the replication role's permissions.
- `pg_stat_statements` view is missing → extension not created in this database or `shared_preload_libraries` not set; restart after changing it.
- Autovacuum keeps hiding bloat → remember you disabled it only on the scratch table.

**Done when**
- [ ] Five repros documented; replica lag observed via `pg_stat_replication`.

#### S4 Review + Write (2h)
1. (35m) Ledger entries RL-46, 64, 68, 69, 81.
2. (25m) ADR-012 "When to use Postgres, Redis, Elasticsearch in ShopFlow" (decision per data type with failure modes).
3. (20m) Explain aloud (record) read-your-writes and three ways to get it with replicas.
4. (15m) Flashcards: Redis structures, CAP/PACELC, replication terms.
5. (15m) Write a "Postgres health check" query pack (long transactions, bloat, top queries, replication lag) in `sql/ops/`.
6. (10m) Retro.

**Done when**
- [ ] ADR-012 merged; ops query pack committed; five ledger entries.

**Self-check**
1. Why can't a replica always serve a read-after-write?
2. What is the access-pattern-first modeling rule for DynamoDB?
3. What does CAP actually say?
4. Why can a fast query still dominate `pg_stat_statements`?

<details><summary>Answers</summary>

1. Replication is asynchronous, so the replica may not have applied the write yet.
2. Start from the queries and design keys to serve them; then denormalize/duplicate as needed.
3. During a network partition a system must choose between consistency and availability for operations that need both.
4. Total time = calls × mean time; very high call counts add up.
</details>

**If stuck:** for replication problems, check `docker logs` of both nodes and `pg_stat_replication`; if it takes more than an hour, use the pause/resume trick on a single node with a logical simulation (a second connection reading a stale value) and note the gap in the ledger.
**Output:** SF-21, ADR-012, RL-46/64/68/69/81 entries.

---

## Week 27 — Zero-downtime migrations and Phase 4 gate
**Outcome:** passes the gate; ships schema changes without locking production.
**Before you start:** the 5M-row `orders` table exists; two ShopFlow versions can run side by side (tag/build the current jar as `v1`). Keep two psql terminals A and B plus a third C for the lock queue demo.

#### S1 Learn (3h): lock levels and safe DDL
1. (30m) Read PostgreSQL docs "Explicit Locking", table-level locks (the conflict table). Highlight `ACCESS SHARE`, `SHARE`, `SHARE UPDATE EXCLUSIVE`, `ACCESS EXCLUSIVE`; list which common DDL takes which (see `ALTER TABLE` docs).
2. (25m) `lock_timeout` and `statement_timeout`: why every migration should set them (`SET lock_timeout = '3s';`) and be retried.
3. (30m) `CREATE INDEX CONCURRENTLY` docs: can't run inside a transaction; may leave an `INVALID` index on failure (find with `\d table`); how to drop and retry. Flyway: non-transactional migrations (verify the config option name in current Flyway docs).
4. (30m) Safe constraints: adding a column with a constant default is fast on modern Postgres; `NOT NULL` via `ADD CONSTRAINT … CHECK (col IS NOT NULL) NOT VALID`, then `VALIDATE CONSTRAINT`, then `SET NOT NULL`; foreign keys with `NOT VALID` + `VALIDATE` (verify version-specific behavior).
5. (30m) Read two reputable engineering posts on zero-downtime Postgres migrations (search the topic). Write the expand/contract steps for a column rename in your own words.
6. (25m) Flyway docs: versioned vs repeatable migrations, checksums, out-of-order, baseline.
7. (10m) Write a migration safety cheat sheet.

**Tips and common problems**
- "Tiny" DDL hanging production → it waited for a long transaction and queued everyone behind it; always use `lock_timeout`.
- Backfills in a single `UPDATE` hold locks and bloat the table → batch by id range and commit each batch.
- Don't trust one blog's version-specific claims → check release notes for your Postgres version.

**Done when**
- [ ] Cheat sheet committed listing safe vs unsafe operations.

#### S2 Build (3h): SF-22 rename `orders.total` → `orders.total_amount` in 3 releases
1. (15m) Write the plan in `docs/migrations.md`: **R1 expand** (add nullable `total_amount`, app writes both columns, reads old), **R2 migrate** (backfill, app reads new and still writes both), **R3 contract** (stop writing old; drop in a later release).
2. (30m) Migration V30 adds the column. Backfill script: loop in batches (e.g., 10,000 ids) with `UPDATE … WHERE id BETWEEN ? AND ? AND total_amount IS NULL`, committing each batch; add a verification query that counts mismatches.
3. (35m) Code changes per release (three tags `v1`, `v2`, `v3`) with tests: R1 dual-writes; R2 reads new; R3 stops writing old.
4. (40m) Compatibility harness: run old jar (`v1`) and new jar (`v2`) at the same time against one database while a script sends requests (k6 or `curl` loop) and records non-2xx responses. Perform the migrations during the run.
5. (30m) Full rehearsal start to finish; record failed requests (target: zero) and duration.
6. (20m) Write the runbook (order of operations, abort conditions, rollback per step) in `docs/migrations.md`.
7. (10m) Commit and open the PR.

**Tips and common problems**
- Old app breaks after adding a `NOT NULL` column → add nullable first, backfill, then constrain.
- Flyway checksum errors after editing → never edit applied migrations.
- A backfill runs for hours → measure per-batch time, limit batch size and add `pg_sleep` between batches if replicas lag.
- Two app versions use different JPA mappings → `ddl-auto` must be `none`/`validate`, never `update`.

**Done when**
- [ ] Rehearsal shows zero failed requests while migrating.
- [ ] Runbook describes each step and its rollback.

#### S3 Lab (2h): RL-70, RL-71, RL-72
1. (40m) **RL-70 lock queue behind `ALTER TABLE`**
   - Steps: **A:** `BEGIN; SELECT * FROM orders LIMIT 1;` (holds `ACCESS SHARE`, leave open). **B:** `ALTER TABLE orders ADD COLUMN lab_flag int;` (blocks, waits for `ACCESS EXCLUSIVE`). **C:** `SELECT * FROM orders LIMIT 1;` (also blocks, queued behind B!).
   - Observe: `SELECT pid, mode, granted, relation::regclass FROM pg_locks WHERE relation = 'orders'::regclass;` and `pg_stat_activity.wait_event_type = 'Lock'`. Reads are stuck although the DDL is "tiny".
   - Fix: run DDL with `SET lock_timeout = '2s';` and retry; kill/avoid long transactions; monitor `idle in transaction`.
   - Cause: lock requests queue in order; a waiting `ACCESS EXCLUSIVE` blocks later readers.
2. (35m) **RL-71 normal vs concurrent index build**
   - Setup: in **B** run a loop inserting rows continuously (`DO $$ BEGIN FOR i IN 1..200000 LOOP INSERT INTO orders(...) VALUES (...); COMMIT; END LOOP; END $$;` — procedure-style commit in `DO` blocks needs PG11+; or use a small script with `\watch 0.1`).
   - Trigger: **A:** `CREATE INDEX idx_lab ON orders (total);` and time the insert stalls; drop it; then `CREATE INDEX CONCURRENTLY idx_lab2 ON orders (total);`.
   - Observe: normal build blocks writes (`SHARE` lock) until done; concurrent build lets writes continue but takes longer. Cancel a concurrent build midway and check `\d orders` for an `INVALID` index; `DROP INDEX CONCURRENTLY` it.
   - Fix: use `CONCURRENTLY` in production migrations.
   - Cause: a normal build locks out writes for its duration.
3. (35m) **RL-72 safe NOT NULL / default change**
   - Trigger A: `ALTER TABLE orders ADD COLUMN flag boolean NOT NULL DEFAULT false;` and time it (fast on modern Postgres; verify on your version).
   - Trigger B: add a nullable `note_code int`, backfill, then time `ALTER TABLE orders ALTER COLUMN note_code SET NOT NULL;` (a full-table scan under `ACCESS EXCLUSIVE`).
   - Safe path: `ALTER TABLE orders ADD CONSTRAINT note_code_nn CHECK (note_code IS NOT NULL) NOT VALID; ALTER TABLE orders VALIDATE CONSTRAINT note_code_nn;` then `SET NOT NULL` (fast when a validated check exists on modern Postgres; verify), then drop the check.
   - Observe: lock mode held during each step in `pg_locks`; `VALIDATE` only takes a weaker lock.
   - Cause: constraint validation scans the table; do the scan with a lock that doesn't block writes.
4. (10m) Save transcripts and one-line conclusions.

**Tips and common problems**
- `CREATE INDEX CONCURRENTLY cannot run inside a transaction block` → run outside a transaction / mark migration non-transactional.
- Locks look clear in `pg_locks` but reads still hang → join with `pg_stat_activity` and follow `pg_blocking_pids(pid)`.
- Don't run these labs on a shared DB.

**Done when**
- [ ] Three transcripts with `pg_locks` evidence saved.

#### S4 Review + Write (2h): Phase 4 gate
1. (30m) **Gate task 1:** explain and demo a lost update and fix it three ways (optimistic, pessimistic, atomic SQL), defending trade-offs. Present to the reviewer or record yourself.
2. (30m) **Gate task 2:** diagnose two unseen slow queries within 10 minutes each using only plans and the checklist (reviewer provides, or use a peer-prepared set).
3. (20m) **Gate task 3:** demonstrate the expand/contract migration with zero failed requests.
4. (20m) Ledger audit: pick five random entries from RL-29–31 and RL-51–81 (excluding 76) and answer each in under a minute.
5. (10m) Update the progress tracker and tag `phase-4`.
6. (10m) Retro for the whole phase: top 3 gaps to carry into Phase 5.

**Gate rubric (pass all):**
- Fix a lost update three ways (optimistic, pessimistic, atomic SQL) and defend the trade-offs.
- Given a slow query and its plan, find the problem within 10 minutes.
- Demonstrate an expand/contract migration with zero failed requests.
- Ledger has RL-29–31, 51–81 (excluding 76) explained aloud.

**Done when**
- [ ] Rubric items all passed (or gaps recorded with a re-test date); `phase-4` tag pushed.

**Self-check**
1. Why must DDL use `lock_timeout`?
2. Why is `CREATE INDEX CONCURRENTLY` safer and what's its cost?
3. Why backfill in batches?
4. Why can old and new app versions run together during expand/contract?

<details><summary>Answers</summary>

1. A DDL statement waiting for a strong lock blocks every later query on the table; a timeout makes it fail fast and retry instead of causing an outage.
2. It doesn't block writes; it takes longer, uses more resources, can't run in a transaction and may leave an invalid index on failure.
3. Short transactions limit lock time, WAL bursts, bloat and replica lag.
4. Each release is compatible with both the old and the new schema shape (additive changes first, removals last).
</details>

**If stuck:** if a lab hangs, open a third terminal and inspect `pg_stat_activity` and `pg_blocking_pids`; terminate stuck sessions with `pg_terminate_backend`. For migration failures, reread the runbook step by step and test each step alone on a copy of the data.
**Output:** `phase-4` tag, gate notes, ADRs 009–012.
