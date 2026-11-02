# Week M5: Model five practical domains (classes)

**Mode:** C (design-it, practice)  **Release:** none (modeling block, before Spring)
**Outcome:** you can turn a one-page brief of a real business system into a small class model that reuses your M3 and M4 building blocks, with a Mermaid diagram and three passing scenario tests, and score it honestly. Five domains are done: Retail catalog and pricing, Orders/payments/refunds, Bank accounts and transfers, General ledger, Booking and reservations.

**Before you start**
- You finished M1 and M2 (classes, OOD), M3 (parties, roles, organizations) and M4 (money, time, effective dating, history, ledger entries). You have your library from those weeks, for example `Money`, `Period`, `Effective<T>`, `Party`, `PartyRole`, `OrgUnit`, `LedgerEntry` under `~/shopflow-notes/modeling/concepts/`. Your class names may differ; use yours.
- Have `modeling-lab/DOMAINS.md` and `modeling-lab/RUBRIC.md` open. You will use both every session.
- Make sure you can render Mermaid (IntelliJ Markdown preview, or any Mermaid preview; verify against current docs).

**Practice workspace:** one Maven project at `~/shopflow-notes/modeling/` (set up in S1). Your M3/M4 classes live in package `concepts`; each domain gets package `domains.<name>` plus a notes folder `~/shopflow-notes/modeling/<name>/` with `model.md` (assumptions, diagram) and `score.md`. Java 21+, JUnit and AssertJ.
**Rubric:** `modeling-lab/RUBRIC.md`, rows C1–C8 (class model, total 16). Row C7 now also asks: did you *use* your building blocks?
**Repo:** no starter code, no hints folder. The reference model for each domain is in a `<details>` block. Open it only after you have scored your own model.

> There is no red test to start from. Your own scenario tests are the proof. Write them before you trust a class.

## The loop (same for every domain, shorter each time)
| Step | What | D1 | D2 | D3 | D4 | D5 |
|---|---|---|---|---|---|---|
| 1 | Read the brief, list nouns and verbs | 10m | 10m | 10m | 7m | 7m |
| 2 | Answer the **Ask** questions as written assumptions | 5m | 5m | 5m | 3m | 3m |
| 3 | Decide the **Twist** and which building blocks apply | 5m | 5m | 5m | 5m | 5m |
| 4 | Mermaid `classDiagram` | 20m | 20m | 20m | 10m | 10m |
| 5 | Java skeleton with behaviour, plus 3 scenario tests (happy path, rule violation, edge case) | 35m | 35m | 30m | 20m | 20m |
| 6 | Score with the rubric, write one improvement | 10m | 10m | 10m | 10m | 10m |
| | Total | 85m | 85m | 80m | 55m | 55m |

For D4 and D5 keep the skeleton minimal: only the classes that carry the Twist and the three tests. Over budget means stop and score what you have.

---

## S1 Try it (3h): how real systems differ from toys, then Retail catalog and pricing
1. (15m) **Orientation: toys vs real systems.** Write four short answers in `orientation.md`, then open the answers below. (a) In a Library or Parking lot, what happens when a rule changes next year? (b) In a shop, what must you still be able to tell about an order from three years ago? (c) Why is a price never "just a number"? (d) Which people and organizations can appear in one business process?
   <details><summary>Answers</summary>

   (a) You change the code or a constant and nobody cares about the past. (b) What was bought, at which price, who paid, what happened in which order, who did what. Real systems answer questions about *the past*. (c) It depends on currency, date, customer group, promotion; and it belongs to a period of time. (d) Many actors with several roles each: customers, staff, suppliers, systems. Rules also change, so models keep dates and history by default.
   </details>
2. (20m) **Concept map.** Create `concepts-map.md` with this table. In the last column write your *prediction* of which of this week's domains (1 Retail pricing, 2 Orders, 3 Bank, 4 Ledger, 5 Booking) need it. Fill the "class" column from your own library. You will check the predictions in S4.

   | Concept (from) | Your class | Needed in domain(s)? |
   |---|---|---|
   | Money and currency (M4) | | |
   | Time interval, half-open (M4) | | |
   | Effective dating / valid from-to (M4) | | |
   | Snapshot vs reference (M4) | | |
   | Status lifecycle with history (M4) | | |
   | Ledger entry, reversal not delete (M4) | | |
   | Party and role (M3) | | |
   | Organization / hierarchy (M3) | | |
3. (15m) **Set up the workspace.** Create the project `~/shopflow-notes/modeling/` (copy the `pom.xml` setup from `shopflow-lab/core`, Java 21, JUnit, AssertJ). Put your M3/M4 library (the `concepts-lib/` project from `~/shopflow-notes/modeling/concepts/`) in `src/main/java/concepts/` (copy its packages `concepts.money`, `concepts.time`, `concepts.ledger` and the party classes; retire the old copy; from now on fix bugs in one place only). Add `src/main/java/domains/` and `src/test/java/domains/`. Run `mvn -q test` (or `./mvnw` from the lab) once to see it green.

**Domain 1: Retail catalog and pricing** (`modeling-lab/DOMAINS.md`, section 1; folder `retail/`)

4. (10m) Brief, nouns and verbs in `retail/model.md`.
5. (5m) **Ask** assumptions: where does the price live (variant, price list)? Do promotions stack? Tax in or out? Suggested: price per variant, per currency, in a price list; promotions do not stack; prices without tax.
6. (5m) **Twist:** "What is the price?" always depends on a date. Write the sentence "my model answers `priceOn(variant, currency, date)` by ...". Which building blocks apply (Money, Period, Effective)?
7. (20m) Mermaid `classDiagram`. Include the category tree as a self-relation with multiplicity.
8. (35m) Java skeleton plus 3 scenario tests, for example: (a) a price valid from 1 March returns the old price on 28 Feb and the new on 1 March; (b) adding a second price for the same variant and currency with an overlapping period is rejected; (c) a 20% promotion applies on its last day and not on the day after (half-open end).
9. (10m) Score with the rubric (C1–C8); write one improvement.
10. (25m) **Review questions, aloud.** Answer, then open.
11. (20m) **Start `patterns.md`.** List every structure used twice or more so far (half-open period, `Money`, effective-dated value, snapshot). One line each: where it appeared. You finish this list in M6.

**Review questions**
1. What breaks if the price is a `price` field on `Product`?
2. What breaks if a promotion is stored as "discount = 20" with no dates?
3. What breaks if an order later needs "the price the customer saw"?
<details><summary>Answers</summary>

1. You lose history (what did it cost last month?), per-currency prices and scheduled changes. Move prices into entries that carry a `Period`; `Product` asks the price list.
2. Someone must remember to remove it, and past reports are wrong. A promotion has its own `Period` and a rule object (`PercentOff`, `AmountOff`).
3. A quote must be stored with the order as a snapshot (the amount, the date, the promotion applied); you cannot recompute it from live data. This leads straight into domain 2.
</details>

**Reference model (open only after scoring)**
<details><summary>Retail catalog and pricing: expected classes</summary>

`Product`, `Variant` (sku, attributes), `Category` (optional parent), `PriceList` (currency), `PriceEntry` (variant, `Money`, `Period`; or an `Effective<Money>`), `Promotion` with `DiscountRule` (`PercentOff`, `AmountOff`), `PriceQuote` (base, discount, final, asOf date). Key decisions: the price is always answered *as of a date*; overlap of entries is rejected in one place; the quote is a value you can store later; `Money` never a `double`.
</details>

**Tips and common problems**
- Price lookup returns the wrong row on the change day → closed period → use the half-open period from M4 (start included, end excluded).
- `Product` has a `currentPrice()` that mutates a field → you destroyed history → store entries, derive the current price.
- No test for "no price exists on this date" → a `null` leaks → decide: exception or `Optional`, and test it.
- A step runs over twice its budget → stop, write the unknown in `questions.md`, return in S4.

**Done when**
- [ ] The project builds and `concepts-map.md` has your predictions.
- [ ] `retail/` has `model.md`, a diagram that matches the code, 3 green scenario tests and a score.
- [ ] You answered the review questions aloud before opening the answers.
- [ ] `patterns.md` has at least 4 entries.

---

## S2 Build (3h): Orders, payments and refunds; Bank accounts and transfers
Two domains where money moves. Do not skip step 3 of each loop: deciding the Twist before the diagram is the habit you are training.

1. (5m) **Warm-up: facts vs edits.** Write on paper: when a customer pays, do you change the order or do you add something? Decide "payments and refunds are new facts, the order is not edited back" and write why you would trust it in an audit.

**Domain 2: Orders, payments and refunds** (section 2; folder `orders/`)

2. (10m) Read the brief; list nouns and verbs in `orders/model.md`.
3. (5m) Answer **Ask** (one payment or many attempts? refund above paid? partial shipments?). Suggested: many attempts per order, a refund never exceeds what was paid, one shipment.
4. (5m) Decide the **Twist:** attempts and refunds are new records; the order total must be explainable later. Which fields on `OrderLine` are snapshots?
5. (20m) Mermaid `classDiagram`; put the allowed status transitions in a note.
6. (35m) Java skeleton plus 3 scenario tests: (a) place an order, pay it → status `PAID`, total equals the sum of lines; (b) a declined attempt leaves the order `NEW`, is recorded, and a second attempt can succeed; (c) a refund larger than the paid amount is rejected, while two partial refunds adding up to the total work.
7. (10m) Score with the rubric; write one improvement.

**Domain 3: Bank accounts and transfers** (section 3; folder `bank/`)

8. (10m) Read the brief; list nouns and verbs in `bank/model.md`.
9. (5m) Answer **Ask** (who may act on a joint account? is a pending transfer in the balance?). Suggested: one currency per account; owners are parties with a role on the account; pending transfers are not part of the balance.
10. (5m) Decide the **Twist:** a transfer touches two accounts and never half-happens; the balance is stored or derived (write the trade-off in two lines).
11. (20m) Mermaid `classDiagram`.
12. (30m) Java skeleton plus 3 scenario tests: (a) transfer 30 from A to B → balances move by 30 and two movements are recorded; (b) a savings account refuses a transfer that would take it below zero and neither balance changes; (c) transfer to the same account is rejected, and a transfer of exactly the full balance works.
13. (10m) Score with the rubric; write one improvement.
14. (10m) **Review questions, aloud** (both domains). Answer, then open.

**Review questions**
1. Orders: what breaks if `Order` has a single `paid` boolean?
2. Orders: what breaks if the order stores only `productId` and reads the name and price live?
3. Bank: what breaks if two transfers run at the same time on one account?
4. Bank: what breaks if the bank adds a second currency, or a co-owner who joins in March?
<details><summary>Answers</summary>

1. A declined attempt, a timeout, a partial refund and a second attempt cannot be expressed. Model `PaymentAttempt` records and derive "paid" from them; keep `OrderStatus` changes in a history list.
2. History changes silently: old orders show new names and prices. Copy sku, name and unit price into the line (snapshot); keep the `productId` as a reference only for navigation.
3. The check "enough money?" and the debit are two steps, so both can pass before either debit happens: a lost update. Note the risk in your assumptions; you study it in Phases 4 and 5.
4. `Money` must carry its currency and adding different currencies must fail loudly. A co-owner is a *role on the account with a start date*, not a flag on `Customer`: reuse the party-role idea from M3.
</details>

**Reference models (open after scoring)**
<details><summary>Orders, payments and refunds: expected classes</summary>

`Order`, `OrderLine` (sku, name, unit `Money`, quantity: copied), `OrderStatus` (NEW, PAID, SHIPPED, DELIVERED, CANCELLED, REFUNDED) that knows legal transitions, `StatusChange` (from, to, at, by), `Payment` holding `PaymentAttempt`s (amount, status, provider reference, at), `Refund` (attempt, amount, at, reason), `PartyId` for the customer, `Money`. Key decisions: attempts and refunds are append-only; "amount paid" and "amount refundable" are derived; the status history is kept.
</details>
<details><summary>Bank accounts and transfers: expected classes</summary>

`Account` (id, type, currency, `OverdraftPolicy`), `AccountParty` (party, role OWNER or CO_OWNER, `Period`), `Movement` (immutable: account, signed `Money`, at, reason), `Transfer` (from, to, amount; creates two movements together), `Statement` (account, `YearMonth`, movements), `InsufficientFundsException`. Key decisions: movements are append-only; a transfer creates both movements or none; balance is derived from movements (or cached with a test that checks equality); owners are party roles with periods.
</details>

**Tips and common problems**
- `double` in a field → rounding errors → `Money` with `BigDecimal` and a currency.
- Transfer debits A, then throws before crediting B → half-done state → validate everything first, then change both; test it.
- `OrderService` has all the rules → anemic model (C3) → move `refund(...)` and `pay(...)` rules onto `Order` / `Payment`.
- Statement for a month contains a movement from the next month's first second → closed range → `[start, nextMonthStart)`.
- Tests need real time → inject a `Clock` or pass the instant (Week 8).

**Done when**
- [ ] `orders/` and `bank/` each have `model.md`, diagram, code, 3 green scenario tests and a score.
- [ ] Both Twists have a sentence explaining the decision, and the balance trade-off is written in two lines.
- [ ] You applied one improvement for each model.
- [ ] You answered the four review questions aloud before opening the answers.

---

## S3 Practice (2h): General ledger and Booking, faster
Both loops are shorter now. Scope the skeleton to the Twist: only the classes that carry the rule, plus the three tests.

**Domain 4: General ledger (double-entry)** (section 4; folder `ledger/`)

1. (7m) Brief, nouns and verbs in `ledger/model.md`.
2. (3m) **Ask** assumptions. Suggested: one currency, accounts form a flat chart with a type, a closed period rejects posting.
3. (5m) **Twist:** the invariant "debits = credits" belongs to the *entry*, entries are never edited, a mistake is fixed by a reversal. Which building blocks apply (Money, Ledger entry from M4)?
4. (10m) Mermaid `classDiagram`.
5. (20m) Minimal skeleton plus 3 scenario tests: (a) a balanced entry posts and the trial balance of all accounts sums to zero; (b) an entry that does not balance cannot even be constructed; (c) reversing an entry creates a new entry and nets the accounts to zero, and posting into a closed period is rejected.
6. (10m) Score and one improvement.

**Domain 5: Booking and reservations** (section 5; folder `booking/`)

7. (7m) Brief, nouns and verbs in `booking/model.md`.
8. (3m) **Ask** assumptions. Suggested: book a specific resource, resource has a time zone, no overbooking, free cancellation until 48 h before start.
9. (5m) **Twist:** overlapping ranges, and the price and cancellation policy at booking time are copied. Decide: are times stored as `Instant` or as local date plus zone? Say why.
10. (10m) Mermaid `classDiagram`.
11. (20m) Minimal skeleton plus 3 scenario tests: (a) booking a free resource works; (b) an overlapping booking is rejected but one that starts exactly when the other ends works; (c) cancelling 3 days before start is free, 1 day before costs the fee, and changing the rate plan afterwards does not change the existing booking.
12. (10m) Score and one improvement.
13. (10m) **Review questions, aloud.** Answer, then open.

**Review questions**
1. Ledger: what breaks if you allow `update` on a posted entry?
2. Ledger: where does "debits equal credits" belong: on the line, the entry or the account?
3. Booking: what breaks if you store `LocalDateTime` for start and end?
4. Booking: what breaks if the cancellation fee is read from the live policy at cancel time?
<details><summary>Answers</summary>

1. History is rewritten and the audit trail is gone. Keep entries immutable; correct with a reversal plus a new entry.
2. On the entry: a single line can never be balanced alone. The constructor of the entry checks the sum, so an unbalanced entry cannot exist.
3. Without a zone, "18:00" means different instants for different people, and daylight-saving changes break durations. Store `Instant` (or `ZonedDateTime`) and the resource's `ZoneId`.
4. Policy changes alter old promises. Copy the policy terms (or a policy version id) into the booking at creation.
</details>

**Reference models (open after scoring)**
<details><summary>General ledger: expected classes</summary>

`Account` (code, name, `AccountType`), `JournalEntry` (id, booking date, description, lines, status), `JournalLine` (account, `Side` DEBIT or CREDIT, `Money`), `AccountingPeriod` (`YearMonth`, open or closed), `Ledger` (posting, trial balance, reversing), `UnbalancedEntryException`. Key decisions: entry validates balance in its constructor; reversal is a new entry that references the original; closed periods reject posting.
</details>
<details><summary>Booking and reservations: expected classes</summary>

`Resource` (type, `ZoneId`), `ResourceType`, `Booking` (resource, customer `PartyId`, `Period` in instants, status, price snapshot, cancellation terms snapshot), `RatePlan`, `CancellationPolicy` (`feeFor(Booking, Instant cancelAt)`), `BookingStatus`, `Period` (half-open, `overlaps`), `Money`, `AvailabilityCalendar`. Key decisions: booking pricing and policy are copied; overlap is one method on `Period`; the calendar rejects double-booking.
</details>

**Tips and common problems**
- Unbalanced entries slip through because the check lives in `Ledger.post` → anyone can build an invalid entry → check in the entry's constructor (C4).
- Overlap returns true when one booking ends as the next begins → closed range → half-open (`start < otherEnd && otherStart < end`).
- The cancellation test is flaky → real `Instant.now()` → pass the instant or a `Clock`.
- You are over time on D4 or D5 → the skeleton grew → delete everything the three tests do not need.

**Done when**
- [ ] `ledger/` and `booking/` each have `model.md`, diagram, code, 3 green scenario tests and a score.
- [ ] Both Twists are explained in one sentence each.
- [ ] You answered the four review questions aloud before opening the answers.

---

## S4 Consolidate (2h)
1. (20m) **Ledger entries.** For all five domains write one entry each in `reality-lab/LEDGER.md` format, shortened: "What I modeled, the Twist, how I decided, what I would do differently."
2. (20m) **Flashcards.** Add these five and write five of your own:
   - Q: Why is a price never "just a number"? A: It depends on currency and date, and it belongs to a period.
   - Q: Why copy price and name into an order line? A: Catalog data changes; the order must remember what was agreed.
   - Q: Why are payments and refunds records and not edits? A: They are facts that happened; edits destroy the audit trail.
   - Q: How do you correct a posted ledger entry? A: Reverse it with a new entry and post the right one; never edit or delete.
   - Q: How do you avoid overlap bugs in ranges? A: Half-open ranges; `start < otherEnd && otherStart < end`.
3. (25m) **Self-check, aloud.** Answer, then open:
   1. Pick one of your classes. Describe it in one sentence without "and".
   2. Give one place from this week where a snapshot was better than a reference.
   3. Which building blocks (Money, Period, Effective, Party/Role, Ledger entry) did you reuse, and where did you almost reinvent one?
   4. Which rubric row did you score lowest across the five domains?
   <details><summary>Answers</summary>

   1. Example: "`Period` represents a half-open span of time and answers whether two spans overlap." If you need "and" for two different jobs, split the class.
   2. The order line (name and price), or the booking's price and cancellation terms. A reference to live data changes later and rewrites history.
   3. Open answer. Check the table in `concepts-map.md` against what you really used: Money in all five, Period in 1, 4 and 5, effective dating in 1, status history in 2 and 5, ledger entry in 3 and 4, party-role in 3.
   4. Open answer. If it is the same row three times, it is a gap: reread the matching M2, M3 or M4 step.
   </details>
4. (15m) **Score table.** Create `modeling/scores.md`: one row per domain, columns C1–C8, total. Sort by total. Check your predictions in `concepts-map.md` and correct them.
5. (20m) **Fix the weakest row** in the lowest-scoring model. Re-score it.
6. (10m) **Update `patterns.md`.** Add the new structures: status lifecycle with history, ledger/movement, party-role on an account. One line each.
7. (10m) **Retro and commit.** Three lines in `retro.md` (what worked, what blocked me and for how long, what I change in M6). Commit the workspace, one commit per domain folder.

**Output:** five domain folders with diagrams, code, passing scenario tests and scores; `concepts-map.md`; `scores.md`; `patterns.md`; 5 ledger entries; 10 flashcards; retro.

**If stuck:** reread the matching M2, M3 or M4 step, then look at one reference model at a time and compare only the single decision you are stuck on. In M7 and M8 you will model the same domains as data.
