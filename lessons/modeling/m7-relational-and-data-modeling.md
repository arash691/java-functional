# Week M7: Relational and data modeling (PostgreSQL, first-hand)

**Mode:** A (build-along), with a normalization clinic and temporal data in S3   **Release:** none (modeling block M1–M8, between weeks 9 and 10)
**Outcome:** you can create tables in PostgreSQL, write basic SQL, explain keys and constraints, draw an ERD with cardinality, normalize a table to 3NF, turn a class diagram into tables, and store people, roles, money, history and time the way real business systems do (append-only ledger, effective-dated roles, valid time vs recorded time, a database transaction).
**Before you start**
- Docker works (Week 1). You finished M1–M6, so you have class models for the practical domains from M5 and M6, including **Bank accounts** (domain 3) and **General ledger** (domain 4), in `~/shopflow-notes/modeling/<domain>/`. You also know the M3 and M4 building blocks (party, role, organization unit, money, time interval, effective dating, history).
- You have never used a database before. That is fine; this week starts at zero.
- Indexes, query plans, isolation levels and locking are **Phase 4** (weeks 21–27). Here we need tables, constraints, queries and the basic idea of a transaction (`BEGIN`, `COMMIT`, `ROLLBACK`).

**Practice workspace:** `~/shopflow-notes/modeling/` (one folder per domain; this week: `bank/sql/`; the normalization and time experiments run in a second database, `sandbox`)
**Rubric:** `modeling-lab/RUBRIC.md`, rows D1–D8 (score the bank data model in S2)
**Domains:** `modeling-lab/DOMAINS.md` (Bank accounts, domain 3; General ledger, domain 4)
**Repo:** none. This week uses a throwaway Postgres container, no `shopflow-lab` tests. The "tests" are the error messages the database gives you and the expected results listed below.

> A database that rejects bad data is doing its job. An error message in this week is a lesson, not a failure.

---

## S1 Try it (3h): your first database (mode A)
You know objects. A table is a set of rows of the same shape, a row is one fact-bundle, and the database enforces rules on them. Predict first, then run.

1. (15m) **Start Postgres.** Throwaway local database, password from an environment variable, data in a named volume:
   ```bash
   export POSTGRES_PASSWORD=local-dev-only
   docker run --name shopflow-pg -e POSTGRES_PASSWORD -e POSTGRES_DB=modeling \
     -p 5432:5432 -v shopflow_pgdata:/var/lib/postgresql/data -d postgres:17
   docker logs shopflow-pg 2>&1 | tail -3     # look for: database system is ready to accept connections
   ```
   (If Week 1 left a container with the same name: `docker ps -a`, then `docker start shopflow-pg`. If port 5432 is busy, use `-p 5433:5432`. Newer image tags may change the data directory; stay on `postgres:17` for now, verify against current docs.)
2. (10m) **Meet psql.** Add two shell aliases to `~/.zshrc` (then `source ~/.zshrc`):
   ```bash
   alias pgi='docker exec -it shopflow-pg psql -U postgres -d modeling'
   alias pg='docker exec -i shopflow-pg psql -U postgres -d modeling -v ON_ERROR_STOP=1'
   ```
   Run `pgi` and try: `SELECT version();`, `\l` (databases), `\dt` (tables, empty now), `\?` (help), `\q` (quit). Statements end with `;`. Commands starting with `\` are psql, not SQL.
3. (30m) **A tiny schema, by hand.** Predict: how many rows will each INSERT report? Then run in `pgi`:
   ```sql
   CREATE TABLE owner (
     id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     name  TEXT NOT NULL,
     email TEXT NOT NULL UNIQUE
   );
   CREATE TABLE pet (
     id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     owner_id BIGINT NOT NULL REFERENCES owner(id),
     name     TEXT NOT NULL,
     species  TEXT NOT NULL CHECK (species IN ('cat','dog','bird')),
     born_on  DATE
   );
   INSERT INTO owner(name, email) VALUES ('Sara','sara@example.com'), ('Omid','omid@example.com');
   INSERT INTO pet(owner_id, name, species, born_on)
   VALUES (1,'Miso','cat','2021-04-02'), (1,'Rex','dog',NULL), (2,'Kiwi','bird','2023-09-15');
   ```
   Expected: `INSERT 0 2` and `INSERT 0 3`. Run `\d pet` and read the output. Write in your notes one sentence each: **table**, **row**, **column**, **primary key** (identifies a row, unique, never null), **foreign key** (a column whose value must exist in another table's key), **identity column** (the database hands out ids).
4. (30m) **Break it on purpose.** For each statement write the error you expect *before* running it, then compare. One of them succeeds.
   1. `INSERT INTO owner(name,email) VALUES ('Sara2','sara@example.com');`
   2. `INSERT INTO pet(owner_id,name,species) VALUES (99,'Ghost','cat');`
   3. `INSERT INTO pet(owner_id,name,species) VALUES (1,'Nemo','fish');`
   4. `INSERT INTO pet(owner_id,name,species) VALUES (1,NULL,'cat');`
   5. `DELETE FROM owner WHERE id = 1;`
   6. `UPDATE pet SET species = 'dog' WHERE name = 'Miso';`
   7. `INSERT INTO pet(id,owner_id,name,species) VALUES (10,1,'Zed','cat');`
   <details><summary>Answers</summary>

   1. `ERROR:  duplicate key value violates unique constraint "owner_email_key"`
   2. `ERROR:  insert or update on table "pet" violates foreign key constraint "pet_owner_id_fkey"`
   3. `ERROR:  new row for relation "pet" violates check constraint "pet_species_check"`
   4. `ERROR:  null value in column "name" of relation "pet" violates not-null constraint`
   5. `ERROR:  update or delete on table "owner" violates foreign key constraint "pet_owner_id_fkey" on table "pet"` (Sara still has pets; the database refuses to orphan them)
   6. `UPDATE 1`. No error: the database does not know that Miso being a dog is silly. It only enforces rules you wrote down.
   7. `ERROR:  cannot insert a non-DEFAULT value into column "id"` (identity is `GENERATED ALWAYS`)
   </details>
5. (25m) **Read and change rows.** Run these, predicting the row count first:
   ```sql
   SELECT * FROM pet WHERE species = 'cat';
   SELECT name, born_on FROM pet WHERE born_on IS NULL;     -- not "= NULL", see Tips
   SELECT name FROM pet ORDER BY name DESC LIMIT 2;
   UPDATE pet SET born_on = DATE '2020-01-01' WHERE name = 'Rex';   -- UPDATE 1
   BEGIN;
   UPDATE pet SET name = 'X';          -- no WHERE! how many rows?
   SELECT name FROM pet;
   ROLLBACK;
   SELECT name FROM pet;               -- back to normal
   ```
   `UPDATE 3` for the forgotten `WHERE`, then `ROLLBACK` undoes it. `BEGIN … ROLLBACK` is a **transaction** (several statements as one unit). You use one for a real transfer in S2; isolation and locking come in Phase 4. Use `BEGIN` whenever you run a risky `UPDATE` or `DELETE`.
6. (30m) **JOIN.** A join glues rows from two tables where a condition matches. Predict, then run:
   ```sql
   SELECT o.name AS owner, p.name AS pet
   FROM owner o JOIN pet p ON p.owner_id = o.id
   ORDER BY o.name, p.name;
   ```
   Expected: `Omid | Kiwi`, `Sara | Miso`, `Sara | Rex`. Now `INSERT INTO owner(name,email) VALUES ('Lena','lena@example.com');` and rerun it. Predict: is Lena listed? (No: `JOIN` keeps only matches.) Change `JOIN` to `LEFT JOIN`: Lena appears with an empty (`NULL`) pet. Write down when you want each.
7. (15m) **GROUP BY.** Predict the three result rows, then run:
   ```sql
   SELECT o.name, count(p.id) AS pets
   FROM owner o LEFT JOIN pet p ON p.owner_id = o.id
   GROUP BY o.name
   ORDER BY pets DESC, o.name;
   ```
   Expected: `Sara 2`, `Omid 1`, `Lena 0`. Change `count(p.id)` to `count(*)`: Lena now shows `1`. Why? (`count(*)` counts the row, `count(column)` skips NULLs.) Add `HAVING count(p.id) >= 1` and note what it filters (groups, after grouping; `WHERE` filters rows before grouping).
8. (15m) **Read with questions.** In the PostgreSQL documentation, the chapter *Data Definition*, section *Constraints* (not-null, unique, primary keys, foreign keys), and the first page of *Data Types → Numeric Types* and *Date/Time Types*. Answer: (a) Why is `NUMERIC` and not `REAL` for money? (b) What is the difference between `DATE`, `TIMESTAMP` and `TIMESTAMPTZ`? (c) What does `ON DELETE CASCADE` do on a foreign key, and why is the default (no cascade) safer?
9. (10m) **Money and time in five statements.** Predict the output, then run:
   ```sql
   SELECT 0.1::float8 + 0.2::float8 AS float, 0.1::numeric + 0.2::numeric AS exact;             -- 0.30000000000000004 | 0.3
   SELECT TIMESTAMPTZ '2025-03-09 23:30+00' AT TIME ZONE 'Asia/Tokyo';                          -- 2025-03-10 08:30:00
   SELECT (TIMESTAMPTZ '2025-03-09 23:30+00' AT TIME ZONE 'Asia/Tokyo')::date;                 -- 2025-03-10
   SELECT DATE '2025-01-31' + INTERVAL '1 month';                                              -- 2025-02-28 00:00:00
   SELECT DATE '2025-03-10' - DATE '2025-03-01';                                              -- 9 (an integer number of days)
   ```
   Write three sentences: why floats are wrong for money; why "which day is it" depends on a time zone (an instant is one moment; a calendar day belongs to a place); why "plus one month" has no fixed length.

**Tips and common problems**
- `WHERE born_on = NULL` returns nothing → `NULL` means "unknown", and nothing equals unknown → use `IS NULL` / `IS NOT NULL`.
- `ERROR: relation "pet" does not exist` → wrong database, or a typo → `\dt`, and check the prompt shows `modeling=#`.
- `psql: connection refused` or the container is not running → `docker ps`, `docker start shopflow-pg`.
- A table named `order` or `user` fails with a syntax error → reserved words → use `customer_order`, `app_user`, or avoid them.
- You pasted several statements and one failed halfway → in `pgi` earlier ones already ran → use `pg < file.sql` with `ON_ERROR_STOP` (alias above) and drop/recreate: `DROP TABLE pet, owner;`.

**Done when**
- [ ] Your notes have the seven predictions with the real error texts next to them.
- [ ] You can say in one sentence each: `JOIN` vs `LEFT JOIN`, `WHERE` vs `HAVING`, `count(*)` vs `count(col)`.
- [ ] You answered the three questions from the documentation.
- [ ] You ran the five money/time statements and wrote the three sentences.

---

## S2 Build (3h): a bank as tables, with an append-only ledger
Today you translate the **Bank accounts** class model (domain 3, M5) into a real schema, load data, answer questions, and meet the database transaction. The ledger idea (domain 4) is built in: money is never stored as "the number on the account", it is the sum of movements.

1. (10m) **Re-open the Bank class model** (and the ledger sketch from M5). List the classes, the relationships with multiplicity and the rules from the brief in `modeling-lab/DOMAINS.md`. Write down what the class model has that a database cannot hold (methods such as `account.withdraw(x)`).
2. (25m) **Mapping rules and design decisions.** Read this table and the list below it once, slowly; keep them open while you work.

   | In the class model | In the data model |
   |---|---|
   | Class with identity (Customer, Account, Transfer) | A table, one row per object |
   | Attribute | A column with a type and, where the brief says so, `NOT NULL`, `UNIQUE`, `CHECK` |
   | `Money` value object (M4) | Two columns: `amount NUMERIC(19,4)` and `currency CHAR(3)`; a separate table only if shared or historical |
   | `Party` = Person or Organization (M3) | `person` and `organization` tables (or one `party` table with a `kind` column); an account owner points at the party, not at "customer" |
   | Role / membership with a validity period (M3) | An association table with its own columns: `role`, `valid_from`, `valid_to` (see S3 step 5) |
   | `DateRange` / `Interval` (M4) | `valid_from` + `valid_to` (half-open: `from` included, `to` excluded, `NULL` = open end) or a Postgres range type |
   | Enum | `TEXT` + `CHECK (x IN (...))` (simple and readable) or a Postgres `ENUM` type (harder to change) |
   | 1:N association | A foreign key column on the "many" side (`entry.transfer_id`) |
   | M:N association | A **join table** with two foreign keys; extra attributes of the relationship live there (`account_owner.role`). When it has its own life, it is a real entity (`transfer`) |
   | 1:1 | Foreign key + `UNIQUE`, or the same primary key in both tables |
   | List/collection inside an object | A child table with a foreign key (and an order column if order matters) |
   | Inheritance | (a) **single table** with a `type` column and nullable specifics; (b) **table per class** sharing a primary key; (c) table per concrete class. Prefer (a) for small hierarchies, (b) when subtypes differ a lot |
   | Method, behaviour | No equivalent. Rules the database can express become constraints; the rest stays in code |
   | Derived value (`balance`, `isOverdue`) | Normally not stored; computed by a query |
   | Status with a past (`ACTIVE` then `FROZEN`) | A history table: one row per change with `valid_from` |

   **Decisions to make consciously** (each is a rubric row):
   - **Surrogate vs natural key.** Use a surrogate `id BIGINT GENERATED ALWAYS AS IDENTITY` as the primary key (stable, small). Keep the natural identifier (email, account number) as `UNIQUE NOT NULL`.
   - **Money.** `NUMERIC(19,4)` plus a currency code is exact and flexible (interest, exchange rates, splitting). The alternative is **amount in minor units** (`BIGINT` cents): exact, compact and fast, but you must know each currency's decimals (JPY 0, EUR 2, KWD 3), divide for display, and rates or splits become awkward. Choose one per system and write it down. Never `REAL`/`DOUBLE PRECISION`. This lesson uses `NUMERIC(19,4)`.
   - **Time types.** `TIMESTAMPTZ` for an **instant** (when did it happen). `DATE` for a **calendar fact with no time of day** (birthday, price valid from). A **business date** (the accounting day, "the 5th of March in the bank's own zone") is a `DATE` you store on purpose; do not derive it from an instant with `::date`, because the result depends on the session time zone.
   - **Snapshot vs reference.** A snapshot copies a value that must not change when its source changes (the price on an order line). A reference points at the current value. A snapshot is a deliberate duplicate; label it in a comment.
   - **History.** If the past matters ("what was the status on Tuesday?"), insert a row per change into a history table instead of overwriting.
   - **Append-only.** A ledger row is never updated or deleted. A mistake is corrected with a **reversal**: a new transfer whose entries have the opposite signs, linked to the original. The past stays explainable.
   - **Soft delete** (`deleted_at` column) keeps rows but every query must remember to filter, and unique rules get awkward. Do not use it unless the brief demands it; prefer status values, a history table or an archive table.
3. (35m) **Write the schema** in `~/shopflow-notes/modeling/bank/sql/V1__bank_schema.sql`. Tables: `customer`, `account`, `account_owner`, `transfer`, `entry`, `account_status_history`. Rules from the brief to enforce *in the database*: unique email and account number; an account kind from a fixed list; the currency is three capital letters; the overdraft limit is not negative; an owner role from a fixed list and one row per (account, customer); an entry amount is never zero; a transfer can be reversed at most once. Write it yourself first, run it with `pg < V1__bank_schema.sql`, and fix errors one at a time.
   <details><summary>Reference schema (compare after you wrote yours)</summary>

   ```sql
   CREATE TABLE customer (
       id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
       email     TEXT NOT NULL UNIQUE,
       full_name TEXT NOT NULL,
       born_on   DATE
   );
   CREATE TABLE account (
       id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
       number          TEXT NOT NULL UNIQUE,
       kind            TEXT NOT NULL CHECK (kind IN ('CURRENT','SAVINGS','INTERNAL')),
       currency        CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
       overdraft_limit NUMERIC(19,4) NOT NULL DEFAULT 0 CHECK (overdraft_limit >= 0),
       opened_on       DATE NOT NULL
   );
   CREATE TABLE account_owner (
       account_id  BIGINT NOT NULL REFERENCES account(id),
       customer_id BIGINT NOT NULL REFERENCES customer(id),
       role        TEXT NOT NULL CHECK (role IN ('OWNER','CO_OWNER')),
       PRIMARY KEY (account_id, customer_id)
   );
   -- One business transaction: groups the entries that must happen together.
   CREATE TABLE transfer (
       id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
       booked_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
       memo                 TEXT,
       reverses_transfer_id BIGINT UNIQUE REFERENCES transfer(id)
   );
   -- Append-only ledger lines. Positive = money in, negative = money out.
   -- The time lives on the transfer (one fact, one place).
   CREATE TABLE entry (
       id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
       transfer_id BIGINT NOT NULL REFERENCES transfer(id),
       account_id  BIGINT NOT NULL REFERENCES account(id),
       amount      NUMERIC(19,4) NOT NULL CHECK (amount <> 0)
   );
   CREATE TABLE account_status_history (
       account_id BIGINT NOT NULL REFERENCES account(id),
       status     TEXT NOT NULL CHECK (status IN ('ACTIVE','FROZEN','CLOSED')),
       valid_from DATE NOT NULL,
       PRIMARY KEY (account_id, valid_from)
   );
   ```
   Out of scope today: transfers between currencies (an exchange rate and two currencies on one transfer).
   </details>
4. (10m) **Draw the ERD** in Mermaid (render it in the IntelliJ Markdown preview or GitHub). Crow's-foot symbols: `||` exactly one, `o|` zero or one, `}o` zero or many, `}|` one or many.
   ```mermaid
   erDiagram
       CUSTOMER ||--o{ ACCOUNT_OWNER : holds
       ACCOUNT ||--o{ ACCOUNT_OWNER : "is held by"
       ACCOUNT ||--o{ ENTRY : receives
       TRANSFER ||--|{ ENTRY : "consists of"
       TRANSFER |o--o| TRANSFER : reverses
       ACCOUNT ||--o{ ACCOUNT_STATUS_HISTORY : has
       ACCOUNT {
           bigint id PK
           text number UK
       }
   ```
   Finish it for all six tables, with PK/FK/UK marks. Save it as `bank/erd.md`.
5. (10m) **Sample data** in `bank/sql/seed.sql`, so everyone's answers match. Run it with `pg < seed.sql`. (Run `pg < V1__bank_schema.sql` first on an empty database.)
   ```sql
   INSERT INTO customer(email, full_name, born_on) VALUES
     ('sara@example.com','Sara','1990-05-12'), ('omid@example.com','Omid','1988-11-30'), ('lena@example.com','Lena','1995-02-02');
   INSERT INTO account(number, kind, currency, overdraft_limit, opened_on) VALUES
     ('ACC-001','CURRENT','EUR',500,'2025-03-01'), ('ACC-002','SAVINGS','EUR',0,'2025-03-03'),
     ('ACC-003','CURRENT','EUR',0,'2025-03-01'),   ('ACC-004','CURRENT','EUR',0,'2025-03-02'),
     ('CASH','INTERNAL','EUR',0,'2025-01-01');        -- the bank's own cash account (id 5)
   INSERT INTO account_owner(account_id, customer_id, role) VALUES
     (1,1,'OWNER'), (2,1,'OWNER'), (3,2,'OWNER'), (4,3,'OWNER'), (4,2,'CO_OWNER');
   INSERT INTO transfer(booked_at, memo, reverses_transfer_id) VALUES
     ('2025-03-01 09:00+00','deposit Sara',NULL),   -- 1
     ('2025-03-01 10:00+00','deposit Omid',NULL),   -- 2
     ('2025-03-02 11:00+00','deposit Lena',NULL),   -- 3
     ('2025-03-03 12:00+00','to savings',NULL),     -- 4
     ('2025-03-05 13:00+00','rent share',NULL),     -- 5
     ('2025-03-06 14:00+00','wrong amount',NULL),   -- 6
     ('2025-03-07 08:30+00','reversal of 6',6),     -- 7
     ('2025-03-08 16:45+00','refund',NULL);         -- 8
   INSERT INTO entry(transfer_id, account_id, amount) VALUES
     (1,5,-1000.00),(1,1,1000.00),  (2,5,-300.00),(2,3,300.00),  (3,5,-200.00),(3,4,200.00),
     (4,1,-400.00),(4,2,400.00),    (5,3,-150.00),(5,4,150.00),  (6,1,-75.50),(6,3,75.50),
     (7,1,75.50),(7,3,-75.50),      (8,4,-100.00),(8,1,100.00);
   INSERT INTO account_status_history(account_id, status, valid_from) VALUES
     (1,'ACTIVE','2025-03-01'), (2,'ACTIVE','2025-03-03'), (2,'FROZEN','2025-03-08'),
     (3,'ACTIVE','2025-03-01'), (4,'ACTIVE','2025-03-02');
   ```
6. (30m) **Answer five questions with SQL.** "Today" is fixed at `2025-03-10`. Amounts print with four decimals (`NUMERIC(19,4)`); use `round(x, 2)` when you want two. Write each query yourself first.
   1. What is the balance of each customer account (skip the internal cash account)?
   2. Show the statement of `ACC-001`: date, memo, amount and the running balance after each line.
   3. Who holds `ACC-004`, with which role?
   4. Which transfers were reversed, and by which transfer?
   5. What was the balance of `ACC-001` at the end of 6 March 2025, as the bank would have shown it that evening?
   <details><summary>Expected results and one possible query each</summary>

   1. `ACC-001 700.00`, `ACC-002 400.00`, `ACC-003 150.00`, `ACC-004 250.00`.
      `SELECT a.number, round(sum(e.amount), 2) AS balance FROM account a JOIN entry e ON e.account_id = a.id WHERE a.kind <> 'INTERNAL' GROUP BY a.number ORDER BY a.number;`
   2. Five rows: `2025-03-01 deposit Sara 1000 → 1000`; `2025-03-03 to savings -400 → 600`; `2025-03-06 wrong amount -75.5 → 524.5`; `2025-03-07 reversal of 6 75.5 → 600`; `2025-03-08 refund 100 → 700`.
      `SELECT t.booked_at::date AS day, t.memo, e.amount, sum(e.amount) OVER (ORDER BY t.booked_at, e.id) AS balance FROM entry e JOIN transfer t ON t.id = e.transfer_id WHERE e.account_id = 1 ORDER BY t.booked_at, e.id;` (a *window function* with a running total; the `::date` result assumes the session time zone is UTC, as in the Docker image)
   3. `Lena OWNER`, `Omid CO_OWNER`.
      `SELECT c.full_name, ao.role FROM account_owner ao JOIN customer c ON c.id = ao.customer_id JOIN account a ON a.id = ao.account_id WHERE a.number = 'ACC-004' ORDER BY ao.role;`
   4. `6 | 7` (original 6, reversal 7).
      `SELECT r.reverses_transfer_id AS original, r.id AS reversal FROM transfer r WHERE r.reverses_transfer_id IS NOT NULL;`
   5. `524.50` (the later reversal does not rewrite what was true that evening).
      `SELECT round(sum(e.amount), 2) FROM entry e JOIN transfer t ON t.id = e.transfer_id WHERE e.account_id = 1 AND t.booked_at < TIMESTAMPTZ '2025-03-07 00:00+00';`
   </details>
7. (25m) **Constraints and the transaction.**
   - **Break your own schema.** Each statement below must fail (predict the error first); wrap each in `BEGIN; … ROLLBACK;` if it succeeds by accident.
     1. `INSERT INTO entry(transfer_id, account_id, amount) VALUES (1, 1, 0);` → `violates check constraint "entry_amount_check"`
     2. `INSERT INTO entry(transfer_id, account_id, amount) VALUES (1, 99, 10);` → `violates foreign key constraint "entry_account_id_fkey"`
     3. `INSERT INTO account_owner VALUES (4, 3, 'BOSS');` → `violates check constraint "account_owner_role_check"`
     4. `INSERT INTO account_owner VALUES (4, 3, 'OWNER');` → `duplicate key value violates unique constraint "account_owner_pkey"`
     5. `INSERT INTO transfer(memo, reverses_transfer_id) VALUES ('again', 6);` → `duplicate key … "transfer_reverses_transfer_id_key"` (a transfer can be reversed once)
   - **The one that succeeds.** `BEGIN; UPDATE entry SET amount = 1 WHERE id = 1; SELECT amount FROM entry WHERE id = 1; ROLLBACK;` → `UPDATE 1`. The database lets anyone rewrite history. "Append-only" is a rule you enforce by discipline in code and by permissions (`REVOKE UPDATE, DELETE ON entry FROM <app role>`; roles are a Phase 6 topic; verify against current docs).
   - **A transfer is a transaction.** A *database transaction* groups statements so that they all happen or none does. The four properties, in one line each: **Atomicity** (all or nothing), **Consistency** (constraints hold after commit), **Isolation** (concurrent transactions do not see each other's half-done work; the details are Phase 4), **Durability** (committed data survives a crash). Run it:
     ```sql
     BEGIN;
     INSERT INTO transfer(memo) VALUES ('test transfer') RETURNING id;          -- remember the id, e.g. 9
     INSERT INTO entry(transfer_id, account_id, amount) VALUES (9, 1, -50.00);  -- debit only, the credit is "forgotten"
     SELECT transfer_id, sum(amount) FROM entry GROUP BY transfer_id HAVING sum(amount) <> 0;   -- shows transfer 9 with -50.00
     ROLLBACK;
     SELECT count(*) FROM entry WHERE transfer_id = 9;                          -- 0: nothing was kept
     ```
     Then do it right: the same, with the second line (`(9, 2, 50.00)`) added, end with `COMMIT`, and run the health query again (0 rows). Delete is not available to you in production, so afterwards reverse your own test transfer with a reversal transfer, or `DROP` and reload the seed (playground only).
   - **What the schema cannot do.** "A transfer's entries add up to zero" and "an account must not go below `-overdraft_limit`" depend on *other rows*, so a plain `CHECK` cannot see them. They live in application code inside one transaction (and later, in deferred triggers or locking; Phase 4).
8. (20m) **Status history, "as of" a date.** Run `SELECT status FROM account_status_history WHERE account_id = 2 AND valid_from <= DATE '2025-03-05' ORDER BY valid_from DESC LIMIT 1;` → `ACTIVE`. Change the date to `2025-03-10` → `FROZEN`. Write the query "all accounts that are frozen on 2025-03-10" (expected: `ACC-002`) using the same idea (the latest row with `valid_from <=` the date, per account; hint: `DISTINCT ON (account_id) … ORDER BY account_id, valid_from DESC`). In your notes answer: why is a `status` column on `account` not enough to answer "was this account frozen when the transfer was booked?"
9. (15m) **Class model vs data model, and score.** Fill this table in your notes in your own words, with a bank example in each cell:

   | | Class model | Data model |
   |---|---|---|
   | Describes | behaviour and rules in one process | facts that must stay true for years, across programs |
   | Identity | the object's reference in memory | a primary key |
   | Relationships | a field that points to an object (navigation) | a foreign key, followed with a `JOIN` |
   | Derived values | methods | queries (or deliberate copies) |
   | Change over time | the object is mutated | history rows, snapshots, reversals, or overwrite on purpose |

   Then score your schema on **D1–D8** (`modeling-lab/RUBRIC.md`) and write the lowest row and one fix.

**Tips and common problems**
- `ERROR: relation "customer" already exists` when rerunning → the script is not idempotent → in the playground, `DROP SCHEMA public CASCADE; CREATE SCHEMA public;` before rerunning (never do this to a database you care about).
- The seed fails on `transfer` with a foreign key error → rows 6 and 7 are in the wrong order, or you inserted in two statements with the wrong id → the reversal row must come after the row it points to, and ids are handed out in insert order.
- Query 1 includes `CASH -1500.00` → you forgot to exclude the internal account → filter on `kind`.
- Query 2 shows the wrong dates → the session time zone is not UTC → `SHOW TIME ZONE;` and `SET TIME ZONE 'UTC';` before the query.
- Balances are off by the reversal → you joined `transfer` twice or filtered out reversals → reversals are normal rows and must be counted.
- You added `balance` to `account` "for speed" → two sources of truth that can drift → derive it, or call it a cache and say how it is checked (rubric D1).

**Done when**
- [ ] `V1__bank_schema.sql` and `seed.sql` run cleanly on an empty database with `pg < file`.
- [ ] `erd.md` has all six tables and relationships with cardinality.
- [ ] Your five queries return the expected results, and the "frozen on 2025-03-10" query returns `ACC-002`.
- [ ] The five break attempts fail with constraint errors, and you can say why the `UPDATE` succeeded.
- [ ] You ran the half-transfer in a transaction, saw it in the health query, and rolled it back.
- [ ] D1–D8 score written, with the lowest row named.

---

## S3 Practice (2h): normalization clinic, people and organizations, two kinds of time
Use a second database so S2's bank stays intact. Add these helpers to `~/.zshrc` and `source` it (M8 uses them too):
```bash
pgdb()    { docker exec -i shopflow-pg psql -U postgres -d "$1" -v ON_ERROR_STOP=1; }
resetdb() { docker exec shopflow-pg dropdb -U postgres --if-exists "$1" && docker exec shopflow-pg createdb -U postgres "$1"; }
```
1. (10m) **Meet the spreadsheet.** Run `resetdb sandbox`, then paste into `docker exec -it shopflow-pg psql -U postgres -d sandbox`. Someone kept orders in one table:
   ```sql
   CREATE TABLE order_sheet (
     order_no INT, line_no INT, order_date DATE,
     customer_email TEXT, customer_name TEXT, customer_city TEXT, city_country TEXT,
     sku TEXT, product_name TEXT, unit_price NUMERIC(8,2), qty INT,
     PRIMARY KEY (order_no, line_no));
   INSERT INTO order_sheet VALUES
   (1001,1,'2025-03-01','sara@example.com','Sara','Berlin','Germany','KB-1','Keyboard',49.90,1),
   (1001,2,'2025-03-01','sara@example.com','Sara','Berlin','Germany','MS-2','Mouse',19.90,2),
   (1002,1,'2025-03-02','omid@example.com','Omid','Lisbon','Portugal','KB-1','Keyboard',49.90,1),
   (1003,1,'2025-03-02','sara@example.com','Sara','Berlin','Germany','MS-2','Mouse',19.90,1),
   (1003,2,'2025-03-02','sara@example.com','Sara','Berlin','Germany','KB-1','Keyboard',49.90,3),
   (1004,1,'2025-03-04','lena@example.com','Lena','Lisbon','Portugal','MS-2','Mouse',19.90,5);
   ```
2. (15m) **Cause the three anomalies** (use `BEGIN … ROLLBACK` or re-create the table between tries). Predict each result first.
   - **Update anomaly.** Sara moves: `UPDATE order_sheet SET customer_city='Hamburg' WHERE order_no=1001;` then `SELECT customer_city, count(*) FROM order_sheet WHERE customer_email='sara@example.com' GROUP BY 1;` → `Berlin 2`, `Hamburg 2`. Which city is Sara's? The table now contradicts itself.
   - **Insert anomaly.** `INSERT INTO order_sheet(sku, product_name, unit_price) VALUES ('MN-3','Monitor',199.00);` → `null value in column "order_no" … violates not-null constraint`. You cannot store a product without an order.
   - **Delete anomaly.** `DELETE FROM order_sheet WHERE order_no=1004;` then `SELECT count(*) FROM order_sheet WHERE customer_email='lena@example.com';` → `0`. Lena as a customer vanished with her order.
3. (25m) **Decompose, step by step.** The key is `(order_no, line_no)`. List dependencies in your notes ("X determines Y"): `order_no → order_date, customer_email`; `customer_email → customer_name, customer_city`; `customer_city → city_country` (city names unique in this exercise); `sku → product_name, unit_price`.
   - **1NF:** every cell holds one atomic value and the table has a key (already true).
   - **2NF:** no column depends on *part* of the key. Split out orders and products.
   - **3NF:** no column depends on a non-key column. `customer_city → city_country` hangs off `customer_email`. Split out customers and cities.
   Create `product`, `city`, `customer`, `customer_order`, `order_line` with keys and foreign keys, and `INSERT … SELECT DISTINCT` data from `order_sheet`. **Decide and justify:** `unit_price` is per *line* in the sheet. Does the price belong to the product or to the order line? (When the keyboard price changes next month, should old orders change?)
   <details><summary>One reasonable result</summary>

   `product(sku PK, name, current_price)`; `city(name PK, country)`; `customer(id PK, email UNIQUE, name, city FK)`; `customer_order(order_no PK, order_date, customer_id FK)`; `order_line(order_no FK, line_no, sku FK, qty, unit_price, PK(order_no, line_no))`. `order_line.unit_price` is a **snapshot** (the price paid), `product.current_price` is today's price. They look like duplicates but they are different facts. You meet this decision again in M8 and in ShopFlow.
   </details>
4. (5m) **Prove nothing was lost.** Rebuild the original shape with joins and compare with `EXCEPT` in both directions; both must return 0 rows and the join must return 6 rows:
   ```sql
   SELECT ol.order_no, ol.line_no, o.order_date, c.email, c.name, c.city, ci.country, p.sku, p.name, ol.unit_price, ol.qty
   FROM order_line ol JOIN customer_order o USING (order_no) JOIN customer c ON c.id=o.customer_id
   JOIN city ci ON ci.name=c.city JOIN product p USING (sku)
   EXCEPT
   SELECT * FROM order_sheet;   -- then swap the two halves
   ```
5. (20m) **People, roles and organization units (the M3 concepts as tables).** In `sandbox`. First enable the extension that lets a plain column take part in an exclusion constraint (verify against current docs): `CREATE EXTENSION IF NOT EXISTS btree_gist;`
   ```sql
   CREATE TABLE person   (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, full_name TEXT NOT NULL, born_on DATE);
   CREATE TABLE org_unit (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, name TEXT NOT NULL,
                          parent_id BIGINT REFERENCES org_unit(id));              -- NULL parent = the root
   -- "Employee" is not a kind of person: it is a role a person plays in a unit during a period.
   CREATE TABLE employment (
     id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     person_id   BIGINT NOT NULL REFERENCES person(id),
     org_unit_id BIGINT NOT NULL REFERENCES org_unit(id),
     job_title   TEXT   NOT NULL,
     valid_from  DATE   NOT NULL,                       -- included
     valid_to    DATE,                                  -- excluded; NULL = still true
     CHECK (valid_to IS NULL OR valid_to > valid_from),
     EXCLUDE USING gist (person_id WITH =, daterange(valid_from, valid_to, '[)') WITH &&)   -- no overlapping periods per person
   );
   INSERT INTO person(full_name) VALUES ('Sara'),('Omid'),('Lena');
   INSERT INTO org_unit(name, parent_id) VALUES ('Company',NULL),('Engineering',1),('Platform Team',2),('Sales',1);
   INSERT INTO employment(person_id, org_unit_id, job_title, valid_from, valid_to) VALUES
     (1,3,'Engineer','2023-01-01','2024-07-01'), (1,3,'Tech Lead','2024-07-01',NULL),
     (2,4,'Account Manager','2022-05-01','2025-01-01'), (2,2,'Engineer','2025-01-01',NULL),
     (3,4,'Sales Rep','2024-03-01',NULL);
   ```
   - Predict, then run: `INSERT INTO employment(person_id, org_unit_id, job_title, valid_from, valid_to) VALUES (1,4,'Advisor','2024-06-01','2024-08-01');` → `conflicting key value violates exclusion constraint` (Sara already has a role then). Omid's two rows *touch* at 2025-01-01 and are accepted, because the range end is excluded (half-open).
   - **Who worked where on 2024-12-01?** `SELECT p.full_name, u.name AS unit, e.job_title FROM employment e JOIN person p ON p.id=e.person_id JOIN org_unit u ON u.id=e.org_unit_id WHERE e.valid_from <= DATE '2024-12-01' AND (e.valid_to IS NULL OR e.valid_to > DATE '2024-12-01') ORDER BY p.full_name;` → `Lena | Sales | Sales Rep`, `Omid | Sales | Account Manager`, `Sara | Platform Team | Tech Lead`.
   - **The unit tree with a recursive query** (a preview; trees get a proper treatment in Phase 4): 
     ```sql
     WITH RECURSIVE tree AS (
       SELECT id, name, 0 AS depth FROM org_unit WHERE parent_id IS NULL
       UNION ALL
       SELECT u.id, u.name, t.depth + 1 FROM org_unit u JOIN tree t ON u.parent_id = t.id)
     SELECT name, depth FROM tree ORDER BY depth, name;
     ```
     → `Company 0`, `Engineering 1`, `Sales 1`, `Platform Team 2`. Start from `Engineering` instead (`WHERE name = 'Engineering'`) and use it to answer: who works *anywhere under Engineering* on 2025-03-10? Expected: `Omid | Engineer`, `Sara | Tech Lead`.
   - Write in your notes: why is one `person` table with a separate `employment` table better than `employee` and `customer` tables that both hold name and birth date?
6. (15m) **Two kinds of time.** A price is true in the real world *from a date*, and the system *learns it at some moment*. These are different facts (valid time and recorded time), and corrections must not erase what we believed before.
   ```sql
   CREATE TABLE price (
     sku         TEXT NOT NULL,
     currency    CHAR(3) NOT NULL,
     amount      NUMERIC(19,4) NOT NULL CHECK (amount >= 0),
     valid_from  DATE NOT NULL,                              -- when it becomes true in the real world
     recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),         -- when the system learned it
     PRIMARY KEY (sku, currency, valid_from, recorded_at));
   INSERT INTO price VALUES
     ('KB-1','EUR',49.90,'2025-01-01','2024-12-15 10:00+00'),
     ('KB-1','EUR',54.90,'2025-03-01','2025-02-20 09:00+00'),   -- a planned price rise
     ('KB-1','EUR',52.90,'2025-03-01','2025-03-05 12:00+00');   -- correction made on 5 March: the rise is only 52.90
   ```
   The query "price on a given day, as known at a given moment":
   ```sql
   SELECT amount FROM price WHERE sku='KB-1' AND currency='EUR'
     AND valid_from <= DATE '2025-03-02' AND recorded_at <= TIMESTAMPTZ '2025-03-10 00:00+00'
   ORDER BY valid_from DESC, recorded_at DESC LIMIT 1;
   ```
   Predict, then run: as known today → `52.9000`. Change the last timestamp to `2025-03-01 12:00+00` ("what did we think on 1 March?") → `54.9000`. Change the date to `2025-02-01` → `49.9000`. This is **bitemporal** data in plain words: two time columns, no `UPDATE`, a correction is a new row. Write one sentence on when you need this (invoices, insurance claims, audits: "what did we know then?") and when plain `valid_from` is enough.
7. (30m) **Six SQL exercises** on the bank database from S2 (`pgi`, database `modeling`). Write each query before opening the answer.
   1. List the customers, alphabetical.
   2. How many accounts does each customer hold (owner or co-owner), most first, then by name?
   3. How many transfers touched each customer account (not the internal cash account)?
   4. What is the total balance of each customer across the accounts they hold (a joint account counts for each holder), highest first?
   5. Which customer accounts have never sent money (no negative entry)?
   6. Which transfer moved the most money (sum of its positive entries), and how much?
   <details><summary>Expected results (compare yours)</summary>

   1. `Lena`, `Omid`, `Sara`.
   2. `Omid 2`, `Sara 2`, `Lena 1`.
   3. `ACC-001 5`, `ACC-002 1`, `ACC-003 4`, `ACC-004 3` (`count(DISTINCT e.transfer_id)`).
   4. `Sara 1100.00`, `Omid 400.00` (150 + 250), `Lena 250.00`.
   5. `ACC-002` (anti-join: `NOT EXISTS (SELECT 1 FROM entry e WHERE e.account_id = a.id AND e.amount < 0)`, and `kind <> 'INTERNAL'`).
   6. `1 | deposit Sara | 1000.00` (`sum(e.amount) FILTER (WHERE e.amount > 0)`, `ORDER BY … DESC LIMIT 1`).
   </details>

**Tips and common problems**
- Your `EXCEPT` returns rows → a column order or a type differs, or `customer_city` does not match a `city` row → compare single columns.
- You cannot decide "2NF or 3NF" → the label does not matter, the question does: "does this column describe the whole key, or something else?" Move it to the table of that something else.
- `ERROR: data type … has no default operator class for access method "gist"` → the `btree_gist` extension is missing → `CREATE EXTENSION btree_gist;` (superuser; fine in the container; verify against current docs).
- The recursive query loops forever → a unit is its own ancestor in your data → fix the data; in real systems add a depth limit while debugging.
- Exercise 4 double counts → you joined the entries once per owner row → join accounts to owners, then sum balances computed per account (a subquery) or sum distinct entries carefully.
- `GROUP BY` error "column must appear in the GROUP BY clause" → add it to `GROUP BY` or wrap it in an aggregate.

**Done when**
- [ ] You caused and observed all three anomalies, in your notes with the output.
- [ ] Five normalized tables load and the `EXCEPT` check returns 0 rows both ways.
- [ ] The overlapping employment is rejected, the "who worked where" query and the tree query return the expected rows.
- [ ] You ran the three `price` queries and wrote the one-sentence summary of valid time vs recorded time.
- [ ] 5 of 6 SQL exercises match the expected results; the rest are in your ledger as "open".

---

## S4 Consolidate (2h)
1. (15m) **Ledger.** Two entries in `LEDGER.md` format (use the heading "M7" in place of a catalog ID): "Update anomaly in a flat table: root cause and fix" and "A rule the database cannot enforce (a transfer must add up to zero, an overdraft limit): where it must live". Add the one-minute spoken answer to each.
2. (15m) **Flashcards (7).** Add these to Anki and add two of your own:
   - Q: Primary key vs unique key? A: Both are unique; a table has one primary key, never null; it identifies the row. Unique keys can be several (email, account number).
   - Q: How do you model M:N with extra data? A: A join table with both foreign keys and the extra columns (`account_owner.role`, `employment.valid_from`).
   - Q: 1NF/2NF/3NF in one line each? A: atomic values and a key / nothing depends on part of the key / nothing depends on a non-key column.
   - Q: `NUMERIC` vs `REAL` for money; `DATE` vs `TIMESTAMPTZ`? A: exact vs approximate; calendar day vs an instant in time.
   - Q: Why snapshot a price on an order line? A: because the past must not change when the current price does.
   - Q: Valid time vs recorded time? A: when it is true in the real world vs when the system learned it; corrections are new rows, not updates.
   - Q: Why reversal rows instead of `UPDATE`/`DELETE` in a ledger? A: history stays explainable and audits work; the correction is itself a fact.
3. (25m) **Self-check.** Answer aloud, without notes, then open the answers.
   1. Why does `LEFT JOIN … count(*)` give 1 for a customer with no accounts?
   2. What is the difference between a foreign key and a `JOIN`?
   3. The brief says "an account may not go below its overdraft limit". Can a constraint enforce it? What would you do?
   4. When is it correct to store the same value twice?
   5. A person was an engineer until June and a manager from July. How do you store it, and how do you ask "what was she on 1 May"?
   6. What does a database transaction guarantee for a transfer, and what does it not guarantee by itself?
   <details><summary>Answers</summary>

   1. The left join produces one row (with NULLs) for that customer and `count(*)` counts rows; `count(account_id)` skips NULL and gives 0.
   2. A foreign key is a rule stored in the schema (the value must exist in the other table); a `JOIN` is a way to combine rows when you query. Foreign keys make joins safe, they do not perform them.
   3. Not with a simple column constraint, because it depends on other rows. Enforce it in application code inside a transaction (and possibly with locking or a trigger; Phase 4 covers this).
   4. When the two values are different facts: a snapshot (price paid vs current price), or a deliberately denormalized copy for speed that you document and keep in sync.
   5. Two rows in an effective-dated table (`valid_from`, `valid_to`, half-open, no overlap enforced by an exclusion constraint). Ask: `valid_from <= date AND (valid_to IS NULL OR valid_to > date)`.
   6. Atomicity (both entries or neither), consistency with declared constraints, isolation from other transactions, durability after commit. It does not know that debits must equal credits or that the balance stays above the limit unless your code or a constraint checks it.
   </details>
   If you got fewer than 4 of 6, redo S1 steps 6–7 or S3 step 3 next week.
4. (30m) **Retrieval test.** Close everything. On paper, draw the bank ERD from memory (tables, keys, cardinalities) and write the `entry` and `account_status_history` DDL. Compare with `erd.md` and note every difference.
5. (25m) **One-page cheat sheet** `~/shopflow-notes/modeling/mapping-cheatsheet.md`: the mapping table from S2 step 2, the decisions list (keys, money, time types, snapshot, history, append-only, soft delete) and the S3 patterns (role with validity period and exclusion constraint, tree with `parent_id`, valid time + recorded time), each with one example from today. You will use it in M8. Books to browse later (titles only): *SQL Antipatterns* (Karwin), *Database Design for Mere Mortals*. For indexes, *Use The Index, Luke* waits until Phase 4.
6. (10m) **Retro** in `retro.md`: what worked, what blocked me, what I'll change.

**Done when**
- [ ] Two ledger entries, seven or more flashcards, retro written.
- [ ] You scored 4 of 6 on the self-check or booked the redo.
- [ ] The cheat sheet exists.

**Output:** `bank/sql/V1__bank_schema.sql`, `seed.sql`, `erd.md`, the normalized `order_sheet` tables, the `employment` and `price` experiments, ledger entries, flashcards, cheat sheet.

**If stuck:** use the 15-minute routine in `lessons/README.md`; for syntax, the PostgreSQL documentation chapters *The SQL Language* and *Data Definition* are the primary source.
