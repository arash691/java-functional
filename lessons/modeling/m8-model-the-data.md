# Week M8: Model the data for the practical domains, then the gate

**Mode:** C (design-it), with a gate in S4   **Release:** none (end of the modeling block; weeks 10–12 follow)
**Outcome:** you can turn a business brief into a class model **and** a relational model (ERD + DDL + queries) in about 45 minutes, handle money, time, history and roles on purpose, justify the hard decisions, and explain what changed between the two models.
**Before you start**
- M7 is done: the `shopflow-pg` container runs, you have the aliases `pg` and `pgi`, and your bank schema from M7 works.
- You have class models for the ten practical domains from M5 and M6 in `~/shopflow-notes/modeling/<domain>/`, and your M3/M4 building blocks (party, role, org unit, `Money`, interval, effective dating, history).
- Helpers that run SQL in any database and reset one (skip if you added them in M7 S3; otherwise add to `~/.zshrc`, then `source` it):
  ```bash
  pgdb()    { docker exec -i shopflow-pg psql -U postgres -d "$1" -v ON_ERROR_STOP=1; }
  resetdb() { docker exec shopflow-pg dropdb -U postgres --if-exists "$1" && docker exec shopflow-pg createdb -U postgres "$1"; }
  ```
  Usage: `resetdb ledger` then `pgdb ledger < V1__ledger_schema.sql`.
- Time-range rules use the Postgres extension `btree_gist` (`CREATE EXTENSION IF NOT EXISTS btree_gist;`, verify against current docs) for exclusion constraints, as in M7 S3.

**Practice workspace:** `~/shopflow-notes/modeling/<domain>/` with `erd.md`, `mapping.md`, `score.md` and `sql/V1__<domain>_schema.sql`, `sql/seed.sql`, `sql/queries.sql`. (The `V1__` name copies Flyway's convention; you will meet the tool itself in Week 12.)
**Rubric:** `modeling-lab/RUBRIC.md`, rows D1–D8 for data models and C1–C8 for class models
**Domains:** `modeling-lab/DOMAINS.md`

> Speed is the point of this week. Domain 4 is slow. Domain 8 should feel routine. If it does not, that is information.

**What you must complete.** Eight of the ten domains in full (class model → ERD → DDL → sample data → five questions as SQL): **3 Bank, 4 Ledger, 2 Orders, 7 HR (temporal), 10 Insurance (bitemporal)** are mandatory, plus **5 Booking, 1 Retail pricing, 8 Identity**. Domains **6 Billing** and **9 Inventory** may be diagram-only. Then the ShopFlow capstone and the gate.

---

## The domain loop
Every domain follows the same loop. Budgets shrink as you improve; the table lists three speeds.

| Step | What you produce | Slow (80m) | Medium (45m) | Sprint (35m) |
|---|---|---|---|---|
| a | Re-read the brief and your class model; underline the **Twist** and the **Concepts** line | 5 | 3 | 2 |
| b | List entities and the facts each one holds; write your assumptions for the **Ask** questions | 10 | 5 | 4 |
| c | **Twist decision for data:** 3 lines on what you store, what you derive, what you snapshot, and which facts have a validity period | 8 | 4 | 3 |
| d | `erd.md` (Mermaid `erDiagram`, keys and cardinality) | 12 | 7 | 6 |
| e | `V1__<domain>_schema.sql` with keys, `NOT NULL`, `UNIQUE`, `CHECK`, foreign keys (and an exclusion constraint where periods must not overlap) | 20 | 12 | 10 |
| f | `seed.sql`: 2–4 rows per table | 8 | 4 | 3 |
| g | `queries.sql`: **five questions you write yourself**, each answered by a query you ran. Mix: one 3-table join, one aggregate, one "who has none" (anti-join), one **about time ("as of a date")**, one about the Twist | 12 | 8 | 6 |
| h | Score D1–D8 in `score.md`; fill `mapping.md` (what changed between the class model and the table model, and why) | 5 | 2 | 1 |

Rules: keep each model to 5–9 tables. Money is `NUMERIC(19,4)` with a currency code, instants are `TIMESTAMPTZ`, calendar facts are `DATE`, snapshots are labelled in a comment. If a rule can be a constraint, make it one. Run the schema on an empty database every time (`resetdb`), so it truly works.

`mapping.md` template (3 columns, at least 4 rows per domain):

| Class model | Data model | Why different |
|---|---|---|
| `Account.balance()` method | no column; `SUM(entry.amount)` | derived value, not a stored fact |
| `Role` with `DateRange` | `valid_from` / `valid_to` + exclusion constraint | the period becomes columns; "no overlap" becomes a constraint |

**Twist guide.** Decide your own approach first, then open this to compare. Different valid answers are fine if you can defend them.
<details><summary>Hints per domain (open after step c)</summary>

- **1 Retail pricing:** `product` and `variant`; `price_list` or a `price` table keyed by (variant, currency, `valid_from`); promotions with `starts_on`/`ends_on` and a rule type; "price on a date" is a query with `valid_from <= d` ordered descending. Money `NUMERIC(19,4)` + currency.
- **2 Orders:** `order_line` copies product name and unit price (snapshot); `order_status_history` (one row per change); `payment` rows per attempt (never overwrite a failed one); `refund` as its own rows linked to the payment; the order total is derived or stored with a documented check. The table is `customer_order`, not `order`.
- **3 Bank:** movements (ledger entries) with sign, balance derived; a transfer is a business transaction grouping two entries, written in one database transaction; `CHECK (amount <> 0)`; currency `CHAR(3)`; joint account via an association table with a role.
- **4 Ledger:** `journal_entry` (the unit that must balance) and `journal_line` (account, debit or credit amount, both ≥ 0); chart of accounts with `parent_id`; `accounting_period` with a closed flag; corrections are reversing entries (`reverses_entry_id`). "Debits = credits" cannot be a plain `CHECK`: say where it lives.
- **5 Booking:** `resource_type` vs `resource`; a booking has a time range stored as `starts_at`/`ends_at` (`TIMESTAMPTZ`, half-open) or a `tstzrange`; double-booking is an exclusion constraint on (resource, range) (verify against current docs); price and cancellation policy are snapshots on the booking; store the resource's time zone.
- **6 Billing (diagram-only is allowed):** `plan`, `subscription` (with `anchor_day`), `invoice` and `invoice_line` (immutable once issued, amounts as snapshots), `payment_attempt`; proration lines are normal invoice lines with a description.
- **7 HR:** `person` once; `employment` with `valid_from`/`valid_to` and an exclusion constraint per person; `org_unit` with `parent_id`; `salary` effective-dated per employment; `timesheet_entry` for labor (person, date, hours), separate from the contract. "Employee" is a row in `employment`, not a kind of person.
- **8 Identity:** `person` and `user_account` are different tables (a person may have two accounts); `organization`; `membership` (user, organization, role, `valid_from`/`valid_to`) as an association with its own data; `role` and `permission` with a join table; `audit_log` that stores the user id and a name snapshot so deleting a user keeps the log meaningful.
- **9 Inventory (diagram-only is allowed):** `stock_movement` (product, location, lot, signed quantity, reason) as the source of truth; stock level derived or held in a cache table with a stated check; `location` tree; `lot` with expiry; reservations as rows with `expires_at`.
- **10 Insurance:** `policy` (stable identity) and `policy_version` with `valid_from`/`valid_to` (real-world period) **and** `recorded_at` (when the system learned it); a claim stores the loss date and is judged against "the version valid on the loss date as known on the decision date". Parties (policyholder, insured) via a role table. Never `UPDATE` a version; add a new row.
</details>

---

## S1 Try it (3h): score the bank, then the ledger and orders
1. (20m) **Score the M7 bank schema** on D1–D8 and write `bank/score.md`. Take the two lowest rows and fix the schema now. Add `bank/mapping.md` with at least four rows (the joint-account association, balance derived, status history, reversal). This counts as domain 3.
2. (10m) **Read the loop above** once, then set up folders for `ledger` and `orders` with the file names from the workspace line.
3. (80m) **Domain 4, General ledger, slow column.** Follow steps a–h exactly. Do not skip the Twist decision (c): where does "debits = credits" live (constraint impossible, so: code in one transaction, plus a health query you keep in `queries.sql`)? How is an entry corrected, and how is a closed period protected? Include the "as of a date" question: the trial balance (sum per account) at the end of a given month.
4. (70m) **Domain 2, Orders, payments and refunds** (a little faster: e 17, g 10). The trap is facts you are tempted to overwrite: a failed payment, a refund, a status. Write the question "what was the order's status on the 3rd?" as one of your five, and the question "how much was refunded per order?" as another.

**Tips and common problems**
- Your ledger stores one amount column with a sign on lines *and* debit/credit columns → two representations of one fact → choose one (signed amounts or debit/credit pair) and write why.
- You update an order's `status` and total in place → the past is lost → insert history rows and derive the current status from the latest.
- Your ERD has 14 tables → you are modeling the whole company → delete everything the brief does not mention.
- Time range queries are wrong → ranges must be half-open: `start < other_end AND end > other_start` → test with ranges that touch at the border.
- `CHECK` cannot see other rows → "debits equal credits" and "no double booking" cannot be plain `CHECK`s → say which are code-in-a-transaction, exclusion constraints, or triggers (later).

**Done when**
- [ ] `bank`, `ledger` and `orders` each have an ERD, runnable schema, seed data, five queries with results, score and mapping notes.
- [ ] Each of the three scores at least 11/16, or the lowest row is fixed or explained.
- [ ] The ledger folder has a "health query" listing unbalanced entries (empty result on your seed data) and an "as of a date" trial-balance query.

---

## S2 Build (3h): four domains at medium speed, with time in every one
1. (45m) **Domain 7, HR and payroll** (medium column). Twist: "employee" is a role over a period, not a kind of person. Must have: `person`, `org_unit` tree, `employment` with an exclusion constraint against overlapping periods, `salary` with effective dates, `timesheet_entry`. One of your five questions is the recursive tree query ("everyone under Engineering on a given date").
2. (45m) **Domain 10, Insurance policy** (medium column). Twist: two clocks. Must have: policy versions with `valid_from`/`valid_to` and `recorded_at`; a claim with a loss date. Write the query "which coverage applied to this claim, as the insurer knew it on the day of the decision", then insert a backdated correction and show that the answer for the old decision date does not change.
3. (45m) **Domain 5, Booking and reservations** (medium column). Twist: two overlapping ranges are never allowed, and the price and cancellation policy of the day of booking must survive later changes. Try the double booking on purpose and capture the error text.
4. (45m) **Domain 1, Retail catalog and pricing** (medium column). Twist: a price is always "for a date and a currency". Must have: effective-dated prices, a promotion with start and end, and the query "what did product X cost on date D, with and without the promotion".

**Tips and common problems**
- You overwrite a price, salary or version "to keep it simple" → the past is gone and cannot be queried → insert a new row with a new `valid_from`; history is the default.
- You use `<=` on both ends of a period → touching periods overlap → use half-open `[from, to)` and `NULL` for open end.
- You keep a `current` flag next to the dates → two sources of truth → derive "current" from the dates.
- You model "employee" and "customer" as two person tables → one fact stored twice → one `person`, roles as rows.
- You are over time on domain 1 of the session → draw ERD and DDL only for the core 5 tables; rank the rest as "later".

**Done when**
- [ ] Four more domains with full file sets (that is 7 in total with S1).
- [ ] Each of them at least 11/16, with the Twist decision written and D7 (history and time) scored at 2, or fixed.
- [ ] HR and Booking each reject an overlapping period with an exclusion constraint (error text saved in `notes`); Insurance answers an "as known on" question.
- [ ] Each `mapping.md` names at least one thing that is a method in the class model and a query or constraint here.

---

## S3 Practice (2h): identity, two sketches, and ShopFlow
1. (35m) **Domain 8, Identity, access and organizations**, sprint column (35m). Twist: the role belongs to the *membership* (user in organization), and deleting a user must not break the audit log.
2. (12m) **Domain 6, Subscription billing, diagram-only.** Nouns (3m), Twist decision (3m), `erd.md` (6m). No DDL. Decide: what is stored on an issued invoice line, and why it is never recomputed.
3. (12m) **Domain 9, Inventory and warehouse, diagram-only.** Same 12-minute split. Decide: stock level as stored number or as the sum of movements, and why "reserved" is not "available".
4. (45m) **ShopFlow capstone** (the brief at the bottom of `modeling-lab/DOMAINS.md`; it combines domains 1, 2 and 9 in one small model):
   - (10m) Class diagram in Mermaid (`shopflow/class-diagram.md`) and 3 scenarios traced by hand (add to cart, place order, payment fails).
   - (10m) `shopflow/erd.md`.
   - (18m) `shopflow/sql/V1__shopflow_schema.sql`. Required decisions: the order line **snapshots** product name and price; stock cannot go negative (`CHECK`); order status values are a fixed list with a status history; a payment belongs to an order and may fail; money is `NUMERIC(19,4)` plus currency.
   - (7m) **Bridge to Week 12.** In `shopflow/bridge-w12.md` predict, before Week 12: (a) which tables hold an order and its items; (b) which table has the foreign key; (c) whether the order item copies the price; (d) why the table cannot be named `order`. In Week 12 you will open `V3__create_orders.sql`, `OrderEntity` and `OrderItemEntity`, draw the ERD, and compare it with yours. Differences are lessons, not errors.
   <details><summary>A good ShopFlow model usually has</summary>

   Category, product, customer, cart and cart line, `customer_order`, order line (snapshot), payment, and a status history; stock either a column on `product` with `CHECK (stock >= 0)` or a movement table. About 8 tables. The order table is named `customer_order` because `order` is a reserved word.
   </details>
5. (16m) **Flashcards (5) and self-check.** Flashcards: (1) the Twist of three domains and your answers, (2) derived vs stored, (3) snapshot vs reference, (4) valid time vs recorded time, (5) why `CHECK` cannot enforce "no overlapping bookings" but an exclusion constraint can. Self-check, aloud:
   1. Why is storing a balance dangerous?
   2. How do you model one person who is an employee, a customer and a user?
   3. How can the database prevent double-booking a room?
   4. Name two places where your class model and your tables differ most.
   <details><summary>Answers</summary>

   1. It can drift from the movements that produced it (two sources of truth). Store movements, derive the balance, or keep a stored balance only as a documented, verified cache updated in the same transaction.
   2. One `person` row; employment, customer and user account are separate rows (roles/relationships) that point at it, each with its own validity period where it matters.
   3. For fixed slots, `UNIQUE (resource_id, starts_at)`. For flexible ranges, an exclusion constraint on the resource and the time range (verify against current docs). Otherwise code with a transaction.
   4. For example: derived values (methods become queries), collections (become child tables), inheritance (single table or shared key), enum (text + `CHECK`), the period value object (becomes two columns and maybe a constraint), behaviour (absent from tables).
   </details>

**Extra (not timed): ADR "why we snapshot price on order lines".** Create `shopflow/adr-001-price-snapshot.md` with *Status*, *Context* (prices change; old invoices must not), *Options* (reference `product.price`; copy into `order_line.unit_price`; price-history table with valid ranges), *Decision*, *Consequences* (duplicated data on purpose; refunds and reports use the snapshot; extra column to keep in sync with currency).

**Tips and common problems**
- The capstone grows to 15 tables → you added coupons, reviews and shipping → stick to the brief; list extras under "later".
- You cannot decide cart: table or session only? → either is valid; write the choice and the reason (a cart table survives a closed browser; a session cart is simpler).
- You skip the diagram-only domains because they feel unfinished → they are the point of practice at speed → set a timer and stop when it rings.

**Done when**
- [ ] 8 domains are complete (Bank, Ledger, Orders, HR, Insurance, Booking, Retail pricing, Identity) and 2 have ERDs only (Billing, Inventory).
- [ ] ShopFlow has class diagram, ERD, schema and `bridge-w12.md`.
- [ ] Five flashcards exist; self-check at least 3 of 4.

---

## S4 Gate (2h): unseen brief, timed
This is the exit check for the modeling block. No notes from earlier domains; the mapping cheat sheet from M7 is allowed.

1. (5m) **Prep.** Create `~/shopflow-notes/modeling/gate/`, start `resetdb gate`, silence notifications, set a 60-minute timer.
2. (60m) **Exam** on the brief below. Produce: a Mermaid class diagram (`class.md`, with three scenarios traced), `erd.md`, `sql/V1__gate_schema.sql`, and `sql/queries.sql` with the five questions answered on a few rows of sample data. Suggested split: read and assumptions 5, class diagram 15, ERD 10, DDL 15, data and queries 13, final check 2.

   > **Marketplace payouts.** An online marketplace lets independent sellers sell to buyers. A buyer pays the marketplace; the marketplace keeps a commission (a percentage that depends on the seller's plan and can change) and pays the rest to the seller once a week, in the seller's currency. A buyer can get a refund within 30 days; refunds are taken from the seller's next payout, and a seller's balance can be negative. A seller can be a person or a company with several staff members who may view payouts. Sellers can be suspended; suspended sellers receive no payouts until they are reinstated.
   > **Ask:** Is the commission taken per sale or per payout? Which exchange rate is used when currencies differ?
   > **Twist:** money flows are immutable facts (sale, commission, refund, payout) from which the seller balance is derived; the commission rule that applied at the time of each sale must stay explainable after it changes.
   > **Questions:** (1) What is the current balance of each seller? (2) Which sales were made under the old commission rate, and what was the commission? (3) Show every movement that made up one payout. (4) Which sellers had a negative balance at the end of last month? (5) Which staff members can view payouts of company X, and since when?
3. (10m) **Score yourself** on both rubrics. Write `gate/score.md` with each row C1–C8 and D1–D8. For the gate, C8 means: three scenarios traced on the class diagram (happy path, a rule violation, an edge case). **Pass = at least 13/16 on both.**
4. (8m) **Record a 5-minute walkthrough** (phone or screen recorder): the brief in one sentence, three class decisions, three data decisions, how you handled the Twist, one thing you would change, one question you would ask the client.
5. (15m) **Before and after.** Open your first class model from M1 (saved in `~/shopflow-notes/modeling/`). In `gate/before-after.md` write 10 lines: what you named differently, what you would now put in a method instead of a field, which relationships you would now draw, how you would now treat money, time and history, and what you could not do then (a database) that you can now.
6. (8m) **Listen back** to the recording once, at normal speed. Write two things you hesitated on; they are the first flashcards of next week.
7. (14m) **Ledger, retro, output.** One ledger entry ("the hardest decision in the gate and why"), the retro lines, and the output list below.

**Below 13 on either rubric?** Do not move on yet. Take the **Event ticketing** retake brief in next week's S4 (60 minutes again): *An organizer creates events with several ticket categories (standard, VIP) at different prices and limited quantities. Buyers purchase tickets, can transfer a ticket to another person, and can get a refund until 7 days before the event. Entry staff scan tickets at the door; a ticket can be scanned once. Twist: the price paid and the refund rules at purchase time; a ticket changes owner but keeps its history.*

**Tips and common problems**
- You run out of time in the DDL → write the three core tables fully and list the rest as comments; time is part of the skill → next time cut the class diagram detail first.
- You keep redrawing the ERD → perfectionism → the diagram's job is to expose decisions, not to be pretty.
- The queries fail on empty data → add sample data first (2 rows per table is enough).
- You split a payment with `/ 3` and lose a cent → money allocation needs an explicit remainder rule (largest remainder, last installment) → say which one you chose.
- You do not know what to say in the recording → follow the six items in step 4 literally.

**Done when**
- [ ] `gate/` has class diagram, ERD, schema, queries, score, recording and before-after note.
- [ ] Both rubric scores are at least 13/16, or the retake is booked.
- [ ] You can state the Twist and your solution in 30 seconds.

**Output:** ten domain folders (eight complete, two ERD-only), `shopflow/` capstone with `bridge-w12.md`, optional `adr-001-price-snapshot.md`, the gate folder, five flashcards plus two from the listen-back, ledger entry, retro.

**What comes next:** Week 10 starts Spring. Week 12 reuses what you built here: you will read an order schema written by someone else and compare it with your `bridge-w12.md` predictions, then map tables to Java classes with JPA.

**If stuck:** use the 15-minute routine in `lessons/README.md`. For syntax questions, the PostgreSQL documentation (*Data Definition*, *Queries*) is the primary source.
