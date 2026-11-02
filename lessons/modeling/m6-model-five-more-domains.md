# Week M6: Model five more domains, pass the class-modeling checkpoint, write the pattern cheat sheet

**Mode:** C (design-it, practice)  **Release:** none (modeling block, before Spring)
**Outcome:** you have class models for all ten practice domains, you can name the structures that keep coming back, and you have a one-page pattern cheat sheet. A timed checkpoint on two unseen briefs (45 minutes each, rubric 12 or more) shows you can do this without help.

**Before you start**
- M5 is done: five domains scored, `modeling/scores.md`, `concepts-map.md` and a first `patterns.md` exist.
- Open `modeling-lab/DOMAINS.md` (sections 6–10) and `modeling-lab/RUBRIC.md`.
- This week the loops are fast: Billing 70 min, HR 70, Identity 60, Inventory 50, Insurance 55. Use a visible timer. Over budget means stop and score what you have.
- Keep reusing the library in `src/main/java/concepts/` (`Money`, `Period`, `Effective<T>`, `Party`, `PartyRole`, `OrgUnit`, `LedgerEntry` or your names). If a domain needs a block you do not have yet (for example `Quantity`), add it to `concepts/` with a test, not inside the domain.

**Practice workspace:** the Maven project `~/shopflow-notes/modeling/` from M5: package `domains.<name>`, notes folder `<name>/` with `model.md` and `score.md`.
**Rubric:** `modeling-lab/RUBRIC.md`, rows C1–C8 (class model, total 16)
**Repo:** no starter code, no hints folder. Reference models are in `<details>` blocks; open them after you have scored your own.

## The loop (shortened as you go)
| Step | What | Billing | HR | Identity | Inventory | Insurance |
|---|---|---|---|---|---|---|
| 1 | Brief + nouns/verbs | 8m | 8m | 7m | 6m | 7m |
| 2 | **Ask** assumptions | 4m | 4m | 3m | 3m | 3m |
| 3 | **Twist** + building blocks | 5m | 5m | 5m | 4m | 5m |
| 4 | Mermaid `classDiagram` | 13m | 13m | 12m | 10m | 12m |
| 5 | Skeleton + 3 scenario tests | 30m | 30m | 25m | 20m | 20m |
| 6 | Score + one improvement | 10m | 10m | 8m | 7m | 8m |
| | Total | 70m | 70m | 60m | 50m | 55m |

---

## S1 Try it (3h): Subscription billing and HR/payroll
1. (10m) **Warm-up: "one month later".** On paper: what is 31 January plus one month? What is 29 February plus one year? Decide a rule for billing that keeps the customer's anchor day when the month is shorter, and write it as one sentence. Then write which of "employee" and "person" is the thing that exists independently, and which is a role during a period.

**Domain 6: Subscription billing and invoicing** (`modeling-lab/DOMAINS.md`, section 6; folder `billing/`)

2. (8m) Brief, nouns and verbs.
3. (4m) **Ask** assumptions. Suggested: an upgrade takes effect immediately with proration; invoices are immutable after issue; a downgrade takes effect at the next cycle.
4. (5m) **Twist:** "monthly" has no fixed length, and proration needs exact rules and rounding. Which building blocks apply (`Money` with allocation, `Period`, status history)?
5. (13m) Mermaid `classDiagram`.
6. (30m) Java skeleton plus 3 scenario tests: (a) a monthly subscription anchored on 31 January is billed on 28 February (or 29), then on 31 March; (b) a mid-cycle upgrade produces an invoice with a credit line for the unused old plan and a charge line for the new plan, and the totals match your hand calculation; (c) an issued invoice cannot be changed, and a correction is a new credit-note invoice.
7. (10m) Score with the rubric; write one improvement.

**Domain 7: HR and payroll** (section 7; folder `hr/`)

8. (8m) Brief, nouns and verbs.
9. (4m) **Ask** assumptions. Suggested: an employee has one primary assignment at a time; the manager is an employment, and a vacated manager position is empty until filled.
10. (5m) **Twist:** "employee" is a role a person plays in a period; salary, department and manager are effective-dated. Which building blocks apply (`Party`, `PartyRole`, `OrgUnit`, `Effective<T>`)?
11. (13m) Mermaid `classDiagram`; show the org unit tree.
12. (30m) Java skeleton plus 3 scenario tests: (a) an employee moves from team A to team B on 1 July → the assignment history shows two non-overlapping periods and `unitOn(date)` answers both sides of the boundary; (b) a raise effective 1 August does not change the July payslip; (c) a person is rehired after leaving: still one `Person`, two employments; two simultaneous primary assignments are rejected.
13. (10m) Score with the rubric; write one improvement.
14. (30m) **Review questions, aloud** (both domains). Answer, then open the answers.

**Review questions**
1. Billing: what breaks if a subscription stores `nextBillingDate` as "today + 30 days"?
2. Billing: what breaks if an invoice line is edited after sending?
3. Billing: what breaks if proration is computed with `double`, or each line is rounded alone?
4. HR: what breaks if `Employee` is a subclass of `Person`?
5. HR: what breaks if salary is a field on `Employee`?
6. HR: what breaks if the manager is a reference to the manager's `Employee` row and that person leaves?
<details><summary>Answers</summary>

1. The cycle drifts (30 days is not a month), and it breaks on month ends. Keep an anchor day and compute each cycle from it.
2. The customer's document and your records disagree. Invoices are immutable; corrections are credit notes (the same idea as ledger reversal).
3. Totals drift by cents and may not match the sum of lines. Use `BigDecimal`-based `Money`, one rounding rule, and allocation (spread the remainder) so lines add up exactly.
4. A person can be an employee, a contractor and a customer at once, and stop being an employee. Role as a relationship over time (composition), not inheritance.
5. You lose salary history, back-dated raises and "what did we pay in March". Use `Effective<Money>` or a salary-term list with periods.
6. A dangling reference or silently wrong reporting line. Model the manager link as an assignment with a period; a vacancy is a valid state.
</details>

**Reference models (open after scoring)**
<details><summary>Subscription billing and invoicing: expected classes</summary>

`Plan` (name, interval, unit price `Money`), `Subscription` (customer `PartyId`, plan, seats, anchor date, `SubscriptionStatus`, `StatusChange` history), `BillingCycle` (`Period`), `Invoice` (immutable once issued, lines, status), `InvoiceLine` (description, `Money`, kind: PLAN, PRORATION, CREDIT, TAX), `ProrationCalculator`, `CreditNote`, `Money`. Key decisions: anchor day plus `plusMonths` from the anchor (never from the previous due date); invoices are immutable; proration uses `Money` allocation.
</details>
<details><summary>HR and payroll: expected classes</summary>

`Person` (party), `Employment` (person, employer, `Period`, status), `Assignment` (employment, `OrgUnit`, position title, `Period`, primary flag, manager link), `OrgUnit` (name, parent), `SalaryTerm` as `Effective<Money>` list on the employment, `TimeEntry` (employment, date, hours), `Payslip` (employment, month, lines, `Money`). Key decisions: employment is a role of a person; assignments and salaries are effective-dated and checked for overlap; payslip stores what was paid (a snapshot).
</details>

**Tips and common problems**
- Billing test fails in February → adding `plusMonths(1)` to the previous due date shifts the anchor → compute from the anchor each time.
- HR model has `Manager extends Employee` → inheritance for a role → manager is a position or assignment link.
- Two overlapping periods are accepted → no overlap check → put the check next to the list of periods, in one method, and test it.
- You are tempted to model payroll tax rules → out of scope → only the structures: employment, assignment, salary, time entry, payslip.

**Done when**
- [ ] `billing/` and `hr/` each have `model.md`, diagram, code, 3 green scenario tests and a score.
- [ ] Both Twists are explained in one sentence each.
- [ ] You answered the six review questions aloud before opening the answers.

---

## S2 Build (3h): Identity and access, Inventory, Insurance policy
Three domains, each faster than the one before. Scope the skeleton to the Twist.

1. (5m) **Warm-up: three words.** On paper, define "user", "person" and "account" in one line each and say which of them can exist without the others. Keep it; you will test it in Identity.

**Domain 8: Identity, access and organizations** (section 8; folder `identity/`)

2. (7m) Brief, nouns and verbs.
3. (3m) **Ask** assumptions. Suggested: roles are fixed (admin, member, billing); a user can have several memberships; an invitation is for an email address.
4. (5m) **Twist:** the *membership* (user in organization) has the role, not the user. History must survive deleting a user. Which building blocks apply (`Party`, `PartyRole`, `Period`)?
5. (12m) Mermaid `classDiagram`.
6. (25m) Java skeleton plus 3 scenario tests: (a) a user is admin in organization A and member in B; a permission check for B uses the B role; (b) an invitation sent to an unknown email is accepted by a new account and creates a membership; (c) removing a user keeps the audit events readable (they show the actor's name at the time).
7. (8m) Score with the rubric; write one improvement.

**Domain 9: Inventory and warehouse** (section 9; folder `inventory/`)

8. (6m) Brief, nouns and verbs.
9. (3m) **Ask** assumptions. Suggested: stock is derived from movements per lot and location; a count difference creates an adjustment movement.
10. (4m) **Twist:** stock level as an updated number vs the sum of movements; reserved is not available. Which building blocks apply (`Quantity`, movement like a ledger entry)?
11. (10m) Mermaid `classDiagram`.
12. (20m) Minimal skeleton plus 3 scenario tests: (a) receive 100 units, then `onHand` is 100; (b) reserve 30 → `available` 70, `onHand` 100, and reserving 80 more is rejected; (c) picking takes the lot that expires first (FEFO), and stock never goes negative.
13. (7m) Score and one improvement.

**Domain 10: Insurance policy (long-lived contract)** (section 10; folder `insurance/`)

14. (7m) Brief, nouns and verbs.
15. (3m) **Ask** assumptions. Suggested: one policyholder, several insured objects; mistakes are corrected by a new record, not an overwrite.
16. (5m) **Twist:** two clocks, valid time (when it was true in the world) and recorded time (when the system knew it). Which building blocks apply (`Effective<T>`, `PartyRole`, `Money`, status history)?
17. (12m) Mermaid `classDiagram`; mark the two time fields on `PolicyVersion`.
18. (20m) Java skeleton plus 3 scenario tests: (a) `coverageOn(date)` returns the coverage valid on that date; (b) an endorsement mid-term creates a new version and the old one stays queryable; (c) a claim with event date 10 March is judged against the policy as it was on 10 March, even after an endorsement on 1 April, and a correction recorded later shows up only when asking "as known on" a later date.
19. (8m) Score and one improvement.
20. (10m) **Review questions, aloud.** Answer, then open the answers.

**Review questions**
1. Identity: what breaks if `User` has a `role` field?
2. Identity: what breaks if audit events point to a `User` row that is deleted?
3. Inventory: what breaks if `stock` is an integer you increment and decrement?
4. Inventory: what breaks if reservation and picking both just subtract from the same number?
5. Insurance: what breaks if an endorsement overwrites the coverage fields?
6. Insurance: what is the difference between valid time and recorded time, in one sentence?
<details><summary>Answers</summary>

1. A person is admin in one company and member in another; one field cannot say that. Put the role on the membership.
2. Foreign-key or null problems and a lost history. Keep audit events immutable with a copy of the actor's id and display name; deleting a user means deactivating or anonymizing.
3. No history, no explanation of a wrong number, and two concurrent updates lose one. Derive the level from movements (or cache it and verify it).
4. You cannot tell what is promised and what left the building. Keep reservations and movements separate: `available = onHand - reserved`.
5. A claim for an old event is judged against today's contract. Keep versions with periods.
6. Valid time is when the fact was true in reality; recorded time is when our system learned (stored) it.
</details>

**Reference models (open after scoring)**
<details><summary>Identity, access and organizations: expected classes</summary>

`Person` (optional, may be unknown at invite time), `Account` (login, email, status), `Organization`, `Membership` (account, organization, `Role`, `Period`, status), `Role` (admin, member, billing) with `Permission`s, `Invitation` (email, organization, role, status, expiry), `AuditEvent` (actor id and name copied, action, target, at). Key decisions: the role lives on the membership; audit events are append-only snapshots; suspension and removal are statuses, not deletes.
</details>
<details><summary>Inventory and warehouse: expected classes</summary>

`Sku`, `Location` (parent), `Lot` (sku, expiry), `Quantity` (amount, unit), `StockMovement` (sku, lot, from, to, `Quantity`, type, at), `Reservation` (reference, sku, `Quantity`, expiry), `StockLedger` (derives `onHand` and `available`, picks FEFO). Key decisions: stock derived from movements; a count difference is an adjustment movement; reservation is separate from movement; quantities never mix units.
</details>
<details><summary>Insurance policy: expected classes</summary>

`Policy` (number, policyholder `PartyId`), `PolicyVersion` (coverage, premium `Money`, valid `Period`, `recordedAt`, replaces), `Coverage`, `InsuredObject`, `PartyRole` (POLICYHOLDER, INSURED, BENEFICIARY), `Claim` (event date, filed at, policy number), `Endorsement`, `StatusChange`. Key decisions: versions are immutable; `versionAsOf(validDate, knownAt)` answers both clocks; claims look up the version by event date.
</details>

**Tips and common problems**
- Identity test needs the same email in two organizations → you made email the key of a membership → key the membership by account and organization.
- Inventory can go negative in a test → the check is in the caller → move the check into `StockLedger` (C4).
- Insurance model has only `validFrom` and `validTo` → one clock → add `recordedAt` and decide how corrections are written (append, never overwrite).
- You skip "the Twist" because the loop is short → the diagram will be wrong again → never skip step 3.

**Done when**
- [ ] `identity/`, `inventory/` and `insurance/` each have `model.md`, diagram, code, 3 green scenario tests and a score.
- [ ] All three Twists are explained in one sentence.
- [ ] You answered the six review questions aloud before opening the answers.

---

## S3 Practice (2h): the class-modeling checkpoint
Two unseen briefs, 45 minutes each, with a timer and no notes except your pattern list and the rubric. The goal is a model you would defend, not a perfect one.

1. (5m) **Prepare.** Close the reference models and your old models. Create `checkpoint/loan/` and `checkpoint/expense/`. Start a timer you can see. Open `patterns.md` and `RUBRIC.md` only.
2. (45m) **Model A: Loan origination.** In `checkpoint/loan/model.md` do the whole loop yourself (nouns and verbs, assumptions, Twist, Mermaid, skeleton, 3 tests, score) inside the time.
   > *Brief:* A lender offers personal loans. An applicant (a person, sometimes with a co-applicant) submits an application for an amount and a term. An underwriter reviews it and approves, rejects or asks for more information; every decision is recorded with the reason. An approved application becomes a loan with an interest rate that is valid from the signing date. The loan has a repayment schedule of monthly instalments (principal and interest); payments received are applied to the oldest unpaid instalment first. Late payments add a fee.
   > **Ask:** Can the rate change during the loan? Can an application be reopened after a rejection? Is a partial payment accepted?
   > **Twist:** the schedule must add up exactly to the amount plus interest (rounding and the last instalment), and the decision history must be kept.
3. (45m) **Model B: Expense reimbursement.** Same loop in `checkpoint/expense/` inside the time.
   > *Brief:* Employees submit expense reports made of lines (date, category, amount, currency, receipt). A report is approved by the employee's manager, and above a limit also by finance. Lines in a foreign currency are converted at the rate valid on the expense date. Approved reports are paid in the next payroll run. A report can be returned for changes and resubmitted; the history of who did what is kept.
   > **Ask:** Who approves when the manager is on leave? Can a line be rejected while the report is accepted?
   > **Twist:** the approval chain depends on the *organization at the time* (manager changes), and the converted amount must not change when rates change later.
4. (10m) **Record a walkthrough.** Record yourself for 3 minutes explaining one of the two models: classes, relationships, the Twist, one decision you would change. Listen back once and note where you hesitated.
5. (15m) **Score and compare.** Score both models with `RUBRIC.md` (C1–C8). Target: 12 or more each. Then read the hints below and check which decisions you missed.
   <details><summary>What a good answer contains</summary>

   **Loan:** `Applicant` and `CoApplicant` as party roles on the `Application`; `Application` with `Decision` history (decision, reason, by, at); `Loan` with `Effective<Rate>` (rate with a period); `Schedule` of `Instalment` (due date, principal, interest) whose sum equals the total exactly (last instalment takes the rounding remainder: `Money` allocation); `Payment` applied to the oldest unpaid instalment; `Fee`. Twist handled by allocation and an append-only decision list.

   **Expense:** `Employee` (party role), `ExpenseReport` with `StatusChange` history, `ExpenseLine` (original amount and currency, converted amount, rate used and its date: a snapshot), `ApprovalStep` (approver, outcome, at) where the approver is resolved from the org unit tree *as of the report date*, `ExchangeRate` as `Effective<BigDecimal>`, `Reimbursement` (a payment fact).
   </details>

**Tips and common problems**
- You ran out of time without tests → you drew too long → keep the diagram to 10 minutes and write the three tests first (they fix the design).
- You reached for inheritance (`Manager extends Employee`) → role as a type → party-role with a period.
- A `double` appeared → rounding → `Money` and allocation.
- The score is below 12 → find the lowest row and fix only that row for 10 minutes; note it for S4.

**Done when**
- [ ] Both checkpoint models exist with a diagram, code and 3 green tests.
- [ ] Both are scored at 12 or more (or you wrote the lowest row and a fix).
- [ ] A 3-minute recording exists and you listened to it once.

---

## S4 Consolidate (2h)
1. (30m) **The pattern cheat sheet.** Create `modeling/pattern-cheat-sheet.md` (one page). Start from your `patterns.md` and complete this table with *your own* words and a link to the domain where you used it:

   | Pattern | Use when | How I model it | Typical trap |
   |---|---|---|---|
   | State lifecycle (+ history) | a thing moves through stages | enum that knows legal transitions, plus a list of changes | plain `String`, no history |
   | Money | any amount of currency | value object with currency and `BigDecimal`, allocation | `double`, mixed currencies |
   | Time range / effective dating | a fact true from-to | half-open `Period`, `Effective<T>` | overlaps, closed ends, two loose dates |
   | Party and role | a person or organization does different jobs | one `Party`, roles as relationships with periods | `Employee extends Person` |
   | Snapshot | the past must not change | copy the value at the moment of the event | referencing live data |
   | Ledger / movement | quantities or money that change | append-only movements, derive the balance | updating a number in place |
   | Hold / reservation | a thing is promised, not yet used | separate record with expiry | subtracting from the same number |
   | Hierarchy / tree | org units, categories, locations | parent reference, queries as of a date | cycles, no root |
   | Versioning (two clocks) | long-lived contracts, corrections | version with valid and recorded time | overwrite on change |
2. (15m) **Ledger entries.** For the five domains of this week and the two checkpoint models write one short entry each (seven in total) in `reality-lab/LEDGER.md` format.
3. (15m) **Flashcards.** Add these five and write five of your own:
   - Q: Why is a role a relationship and not a subclass? A: A person can have many roles that start and end; a subclass is fixed at creation.
   - Q: What is the difference between valid time and recorded time? A: When it was true in the world vs when the system learned it.
   - Q: Why do invoices get credit notes instead of edits? A: Issued documents are facts; corrections are new facts (the ledger idea).
   - Q: Why derive stock from movements? A: Every number can be explained; a counter hides history and loses concurrent updates.
   - Q: How do you split an amount exactly across lines? A: Allocate the remainder (smallest unit) so the parts add up to the total.
4. (20m) **Self-check, aloud.** Answer, then open:
   1. Name three domains that used a status lifecycle with history, and say which fields the history stores.
   2. Where did the party-role idea show up in domains 7, 8 and 10?
   3. Why does the checkpoint ask for a 3-minute walkthrough?
   4. Which pattern did you almost forget in the checkpoint?
   <details><summary>Answers</summary>

   1. For example Orders (status change: from, to, at, by), Billing (subscription status), Insurance (policy status), Expense (report approvals). History stores what changed, when and by whom.
   2. HR: person with employments and assignments; Identity: account with memberships and roles per organization; Insurance: policyholder, insured, beneficiary as roles on a policy. The person stays one, the roles come and go.
   3. If you cannot explain it simply, you do not own the model yet; explaining shows where decisions are unclear.
   4. Open answer. Add it to the first line of the cheat sheet.
   </details>
5. (25m) **Score table and weakest row.** Update `modeling/scores.md` with all ten practice domains and the two checkpoint models (columns C1–C8, total). Find the row with the most 0s and 1s, redo it on your lowest-scoring model for 15 minutes, re-score.
6. (15m) **Retro, commit and preview M7.** Three lines in `retro.md` (what worked, what blocked me and for how long, what I change in M7). Commit the workspace. Open `modeling-lab/DOMAINS.md` and note for each domain one thing you will have to store in a database; M7 starts there.

**Output:** ten domain folders and two checkpoint models with diagrams, code, passing tests and scores; `scores.md`; `pattern-cheat-sheet.md`; 7 ledger entries; 10 flashcards; a 3-minute recording; retro.

**If stuck:** reread the matching M2, M3 or M4 step, then compare only the one decision you are stuck on with the reference model in `<details>`. In M7 you learn the database side of the same ideas, and in M8 you model the data of the same domains.
