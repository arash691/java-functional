# M4: Money, time, history and transactions (the "when and how much" of every business system)

**Mode:** A (build-along), bug-driven  **Block:** Modeling (M4 of M1–M8)
**Outcome:** you can model money without losing a cent, choose the right time type for a fact, record what was true *when* (effective dating, valid time vs record time), decide between snapshot and reference, and explain a business transaction, a ledger entry and a reversal. You have extended your `concepts-lib` with `Money`, `Period`, `TemporalHistory` and a balanced `Ledger`.

**Before you start**
- M3 done: `concepts-lib/` exists with tags `m3-start`, `m3-lib`. Weeks 4 and 8 (BigDecimal, `java.time`) are fresh in your mind; if not, skim your notes.
- Practice workspace: `~/shopflow-notes/modeling/concepts/`, notes in `notes/m4.md`. Rubric: `modeling-lab/RUBRIC.md` (C rows; C7 now asks for Money, Interval and history instead of `double` and loose dates).
- Reading (by title, just in time): Fowler's "Money" pattern and the *Patterns of Enterprise Application Architecture* entry on Money (verify against current books/site); Fowler articles "Temporal Patterns" and "Bitemporal History" on martinfowler.com (verify the titles against the current site); *Effective Java* Items 17, 54 and 62; the `java.time` Javadoc for `LocalDate`, `Instant`, `ZonedDateTime`, `Period`, `Duration`; search the title "Falsehoods programmers believe about time"; *Analysis Patterns* (Fowler), the chapters on accounting and on Quantity.
- No database yet. Forward notes show where the same ideas become columns (M7/M8). SQL snippets are previews only.

---

## S1 Try it (3h): bugs first, then the model

1. (10m) **Guess.** One line each, no search: what is the difference between a price, a cost, an amount and a balance? What is an instant, a local date, a business date?
2. (25m) **Money bugs, predict first.** Write the expected output of each line in a comment, then run it in `s1/MoneyBugs.java`:
   ```java
   System.out.println(0.1 + 0.2);                                   // ?
   System.out.println(new BigDecimal(0.1));                         // ?
   System.out.println(new BigDecimal("100.00").divide(new BigDecimal("3"), 2, RoundingMode.HALF_EVEN).multiply(new BigDecimal("3"))); // ?
   System.out.println(new BigDecimal("1.0").equals(new BigDecimal("1.00")));  // ?
   System.out.println(new BigDecimal("1.0").compareTo(new BigDecimal("1.00"))); // ?
   ```
   Then answer: (a) why is the third line not 100.00? (b) which of `equals` and `compareTo` should a `Money` use? (c) what does "3 people split 100.00" need? Read **EJ Item 60** (avoid float and double for exact answers).
3. (25m) **Money is amount plus currency.** Predict what is wrong with `BigDecimal total` in a method `Money add(BigDecimal a, BigDecimal b)` when one side is euros and one is yen. Sketch (do not compile yet):
   ```java
   record Money(BigDecimal amount, Currency currency) {
       Money { Objects.requireNonNull(amount); Objects.requireNonNull(currency);
               amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN); }
       Money plus(Money o) { requireSame(o); return new Money(amount.add(o.amount), currency); }
   }
   ```
   Decide: why `java.util.Currency` default fraction digits (JPY has 0, KWD has 3)? Why must `plus` reject different currencies instead of converting silently? Write down the **three kinds of price words**: *price* (what a seller asks), *cost* (what it costs the seller), *amount* (a number on a transaction line). Also note: tax inclusive vs exclusive must be a labelled fact, never guessed; negative amounts mean credits/refunds, so decide a sign convention and keep it.
4. (15m) **Quantity and unit.** `10 kg` and `10 pieces` are not both "10". Write a 6-line `Quantity(BigDecimal value, Unit unit)` idea in notes and one reason to forbid `Quantity.plus` across units. Currency conversion is *data with a date*: `ExchangeRate(from, to, rate, validOn)`; write why you never store a converted amount without the rate and date that produced it.
5. (30m) **Time types, decide before you read.** For each fact choose one of: `Instant`, `LocalDate`, `LocalDateTime`, `ZonedDateTime`, `LocalTime`, `Duration`, `Period`, `YearMonth`: (a) the moment a payment was received; (b) a person's birthday; (c) a meeting at 09:00 in Berlin next Monday; (d) store opening hours; (e) invoice for the month of March; (f) a 30-day trial; (g) the business date on which a bank posts a transfer.
   <details><summary>Answers</summary>

   a) `Instant` (an exact moment, store in UTC). b) `LocalDate`. c) `ZonedDateTime` (or a `LocalDateTime` + zone id), because rules for DST can move the instant: you must keep the **zone**. d) `LocalTime` per weekday. e) `YearMonth`. f) `Period.ofDays(30)` or an end `LocalDate`; decide inclusive or exclusive. g) a **business date** (`LocalDate`) that is *not* the instant the request arrived.
   </details>
   Then **predict**, run, and note: `LocalDate.of(2025,1,31).plusMonths(1)`; `ZonedDateTime` at 02:30 on the spring-forward day in `Europe/Berlin`; `Duration.ofDays(1)` vs `Period.ofDays(1)` across DST. Read "Falsehoods programmers believe about time" and keep five items.
6. (25m) **Intervals.** Rule: use **half-open** intervals `[from, to)`: the start is included, the end is excluded. Draw [1 Jan, 1 Feb) and [1 Feb, 1 Mar) on a line: they touch but do not overlap, and a day belongs to exactly one. With closed ends (31 Jan, 1 Feb) you get overlap bugs and "does Feb 1 belong to both?". Overlap test: `a.from < b.to && b.from < a.to` (with open ends as infinity). Write three cases where a missing `to` (open-ended) is the right model (current salary, active price, subscription).
7. (30m) **Temporality.** Imagine Sara's salary: 4000 from 1 January, raised to 4500 from 1 July. (1) On 15 August HR learns the raise was *meant* from 1 May. (2) Payroll for May has already been paid. Decide: what are the two different clocks? **Valid time**: when the fact is true in the real world ("salary 4500 from 1 May"). **Record (transaction) time**: when the system learned or stored it ("recorded on 15 August"). A model with only valid time answers "what is true"; with both (bitemporal) it also answers "what did we believe on 1 June when we ran payroll?". Write the method signatures you would want: `salaryAt(LocalDate validOn)` and, only if needed, `salaryAt(LocalDate validOn, Instant knownAt)`. Read Fowler's "Bitemporal History" (title; skim). Rule of thumb: effective dating everywhere things change (price, salary, role, address); bitemporal only where corrections must not rewrite the past you reported (insurance, payroll, finance, regulation).
   Wrong first: `Employee.salary` overwritten on every change. Better: `Effective<Money>(value, validFrom, validTo)` kept in a `TemporalHistory<Money>`.
   Preview (columns, M7/M8): `salary(person_id, amount, currency, valid_from, valid_to, recorded_at)`.
8. (20m) **History and snapshot vs reference.** The order line must show the price at order time, even if the product price changes tomorrow. Two options: *reference* (store `productId`, look up the price: old orders change when prices change, wrong) or *snapshot* (copy name and price onto the line). Decide for each: address on an invoice (snapshot), product category on a report (reference), customer name on a contract (snapshot), the product of a cart line (reference, still open). Also decide **end-dating vs soft delete vs hard delete** for a retired price: end-dating keeps the fact and its period; soft delete flags the record; hard delete destroys history. Write the rule you will use: *keep facts that explain money or decisions*.

**Tips and common problems**
- You use `double` "just for display" → every conversion is a chance to drift; format at the edge only.
- `equals` on `BigDecimal` says 2.0 ≠ 2.00 → normalize scale in the constructor (as in step 3) so `Money` equality is meaningful.
- You store local times as UTC "to be safe" → for a meeting at 09:00 Berlin you lose the intent after a DST change; keep the zone.
- Overlap bugs at month boundaries → half-open intervals; draw before coding.

**Done when**
- [ ] `notes/m4.md` has your predictions for steps 2 and 5 with the real outputs.
- [ ] You can state half-open intervals, valid vs record time and snapshot vs reference in one sentence each.

---

## S2 Build (3h): Money, Period, history and a ledger

Test-first in `concepts-lib/`, package `concepts.money`, `concepts.time`, `concepts.ledger`. The scenario lists are the spec.

1. (10m) **Setup.** Create the packages; tag `m4-start`. Upgrade `TimePeriod` from M3 or write `Period` here and use it in both places.
2. (35m) **Money.** Scenarios: construction normalizes scale (`new Money("10", EUR)` equals `new Money("10.00", EUR)`); `plus`/`minus` with equal currency; mixed currency throws; `times(BigDecimal)` rounds half-even to currency digits; `isNegative()`; `negate()`; compare via `compareTo` only on equal currency; JPY has 0 digits.
3. (30m) **Allocate.** `List<Money> allocate(int... ratios)` and `splitEvenly(int parts)`: allocate 100.00 EUR in 3 gives [33.34, 33.33, 33.33] (sum is exactly 100.00); allocate 0.05 EUR in 2 gives [0.03, 0.02]; ratios [70, 30] of 0.05 give [0.04, 0.01]. Rule: nobody loses a cent; the remainder is handed out one minor unit at a time from the first part. (Decide and document the rule in a comment.)
4. (25m) **Period.** `LocalDate` half-open: `contains`, `overlaps`, `isOpenEnded`, `intersection(other)` returning `Optional<Period>`, `daysBetween` (and what happens for open-ended). Scenarios from S1 step 6, plus: [1 Jan, 1 Feb) intersects [15 Jan, null) at [15 Jan, 1 Feb).
5. (35m) **TemporalHistory.** `record Effective<T>(T value, Period valid)`; `TemporalHistory<T>` with `valueAt(LocalDate)` returning `Optional<T>`, `changeFrom(LocalDate d, T newValue)` (ends the open period at `d` and starts a new one at `d`), `current()`, `periods()`. Scenarios: salary 4000 from 1 Jan, `changeFrom(1 Jul, 4500)`, `valueAt(15 May)` is 4000; a change before the first period is rejected; a backdated correction `correct(Period, T)` that replaces what was in force, with the old versions kept in a `List<Correction>` including `recordedAt` (an `Instant` from an injected `Clock`); no gaps or overlaps inside a history after any operation (write a property-style loop test with several changes).
6. (35m) **Ledger.** `Entry(id, LocalDate bookingDate, String description, List<Line> lines)`; `Line(AccountId account, Money amount)` with positive = debit, negative = credit (state the convention). Invariants in the constructor: at least two lines, one currency per entry (decide), **sum of lines is zero**. `Ledger.post(Entry)` appends; no `update`, no `delete`. `balance(account)` is *derived* by summing entry lines (decide: stored or derived; derived first). `reverse(entryId, LocalDate on)` posts a new entry with negated lines and a reference to the original; the original stays. Scenarios: a sale of 119.00 as debit receivable 119, credit revenue 100, credit tax 19; a reversal returns every balance to zero; posting an unbalanced entry throws; posting the same idempotency key twice does not duplicate (add an `idempotencyKey` field and a test).
7. (10m) **Commit and score.** RUBRIC C1 to C8, tag `m4-lib`.

**Tips and common problems**
- Allocation gives 99.99 → you rounded each share separately; compute floor shares then distribute the remainder.
- History tests are brittle → build with a helper `history(4000, "2025-01-01").thenFrom("2025-07-01", 4500)`.
- You want `Ledger.delete` "for corrections" → the answer is reversal; if the rule feels hard, read Fowler's *Analysis Patterns* chapter on accounting again.
- Entry totals fail with floating error → you used `double` in a test; use string constructors.

**Done when**
- [ ] All scenarios are green tests; there is no `double` in the library.
- [ ] No ledger update/delete method exists; reversal works.
- [ ] Tags `m4-start`, `m4-lib` exist; score recorded.

---

## S3 Practice (2h): decide on three mini cases

For each: write what must be answerable, then choose type, period handling and snapshot/reference. Class diagram plus a table of decisions.

1. (10m) Write the five "what was true when" questions you can now ask of any system (price on a date, salary on a date, owner on a date, status on a date, what did we know on a date).
2. (30m) **Salary history with a backdated correction.** 4000 from 1 Jan, 4500 from 1 Jul. On 15 Aug HR corrects: the raise was from 1 May. Payroll already ran for May, June and July. Model it; answer: what was May's gross pay as paid? What should it have been? What is the difference to pay now, and in which month does it show up? Use `TemporalHistory` and a correction record. When is bitemporal necessary here?
3. (30m) **Price list with a promotion.** Product P costs 20.00 until 30 June and 22.00 from 1 July; a promotion of 10% runs 20 June to 5 July inclusive. Decide: what is the interval for the promotion in half-open form? What is the price on 25 June, 1 July, 6 July? Which prices are stored and which computed? Which is snapshotted on the order line?
4. (30m) **A refund.** A customer paid 119.00 (100 net, 19 tax) for an order; two weeks later 40% of the goods are returned. Model the refund as new ledger entries (not an edit of the original), including the tax. Allocate rounding. What happens to the order's status history? Is the original payment changed?
5. (20m) **Review.** Fill this table for the three cases: *fact | type | stored or derived | period | snapshot or reference | edited, end-dated or reversed*. Score C7 and C4 for each.

<details><summary>Reference thoughts, open after your decisions</summary>

Salary: two clocks; payroll for May used 4000 because that was known; the correction is a new payment for the difference (4500 - 4000 = 500 for May and 500 for June, and 0 for July, which was already 4500), booked in August; the original payslips stay unchanged. Promotion: [20 Jun, 6 Jul); on 25 Jun price 20.00 minus 10% = 18.00; 1 Jul 22.00 minus 10% = 19.80; 6 Jul 22.00. Store base price history and promotion periods; compute the final price; snapshot the final unit price, the base price and the promotion id on the order line. Refund: reverse proportionally with allocation (40% of 100 = 40.00 net, 7.60 tax), post new entries linked to the original; the order gets a `PARTIALLY_REFUNDED` status entry with date and reason.
</details>

**Tips and common problems**
- You overwrite the order total on refund → the order total is a fact at order time; refunds are new facts.
- You store both "current price" and "price history" and they disagree → keep one source (history) and derive the current value.

**Done when**
- [ ] Three decision tables and diagrams are saved; the answers to the numeric questions are written.

---

## S4 Consolidate (2h)

1. (35m) **Glossary v2.** In `notes/glossary-m4.md`, one line each with "not to be confused with": money, currency, minor unit, rate, instant, local date, business date, interval (half-open), effective date, valid time, record time, snapshot, ledger, journal entry, reversal, idempotency key. Link terms to your M3 glossary where they interact (for example "assignment period").
2. (20m) **Ledger entries (learning log).** Add three entries to `~/shopflow-notes/ledger.md`: "allocate keeps the cents because...", "half-open intervals because...", "reversal instead of edit because...". Say each aloud in under a minute.
3. (20m) **Flashcards.** (1) Why not `double` for money? (2) Instant vs local date vs business date? (3) Valid time vs record time? (4) Snapshot vs reference? (5) Why reversal instead of editing a ledger entry?
4. (25m) **Self-check.** Answer aloud first. 1) What does `allocate(100.00, 3)` return and why? 2) What is wrong with closed intervals for months? 3) When do you need bitemporal data? 4) What is the difference between a business transaction and a database transaction?
   <details><summary>Answers</summary>

   1) 33.34, 33.33, 33.33: the remainder cent goes to the first part so the sum is exact. 2) The end day is claimed by two periods; half-open intervals give each day one owner. 3) When corrections must not rewrite what you reported or decided earlier: payroll, insurance, finance, regulation. 4) A business transaction is a fact with date, amount and parties (a sale, a transfer). A database transaction is a unit of work that either fully happens or not at all (ACID); M7 introduces it and a business transaction usually needs one.
   </details>
5. (10m) **Retro** in `retro.md`: what worked, what blocked me, what I change. Which of the concepts would have saved you in a past project?
6. (10m) **Preview M5.** Read domains 1 to 5 in `modeling-lab/DOMAINS.md`. For each, write the concept names you will reuse (for example "Money, effective dating"). Next week you model them as classes.

**Forward note (M7/M8):** money becomes `NUMERIC(19,4)` plus `currency` (never float); periods become `valid_from DATE`, `valid_to DATE` (or a Postgres range with an exclusion constraint, learned in M7); ledger entries become append-only rows with no `UPDATE`; business transactions use a database transaction around their rows. Preview: `CHECK (valid_to IS NULL OR valid_to > valid_from)`.

**Output:** `concepts-lib/` extended (`Money`, `Period`, `TemporalHistory`, `Ledger`; tags `m4-start`, `m4-lib`), three decision tables, glossary v2, 3 ledger entries, 5 flashcards, retro, a short list of concepts for each M5 domain.

**If stuck:** do Money and Period first and leave the ledger for the S3 hour; use the 15-minute routine in `lessons/README.md`. Commands and book chapter names above are starting points; verify against current docs.
