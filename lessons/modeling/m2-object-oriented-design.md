# M2: Object-oriented design, learned on your own code

**Mode:** A → B (build-along concepts, then diagnose and fix smells in your own M1 models)  **Block:** Modeling (M2 of M1–M8)
**Outcome:** you can explain in plain words, and apply to code, encapsulation, abstraction, composition over inheritance, polymorphism through interfaces, cohesion and coupling, tell-don't-ask, SRP/OCP/DIP, and entity versus value object. You can read and draw a UML class diagram, and you have refactored your M1 Library and Parking lot models with a measurable score gain.
**Before you start**
- M1 done: tags `m1-library-before`, `m1-parking-before` exist in `~/shopflow-notes/modeling/`, with rubric scores and a tally of naive failures in `notes/m1.md`.
- Open `notes/questions.md` from M1. Many of your open questions are answered this week.
- Classes still live in memory only. Database design is taught in M7 and M8; if you start thinking about tables, write the thought in `notes/questions.md` and continue.
- Practice workspace: `~/shopflow-notes/modeling/` (same folders). Create `library-v2/` and `parking-v2/` by copying the tagged versions; do the refactor there so the originals stay untouched.
- Rubric: `modeling-lab/RUBRIC.md` (C rows). Diagrams: Mermaid `classDiagram` blocks inside `.md` files (renders on GitHub and in IntelliJ's Markdown preview; verify against current docs).
- Reading (by title; just in time inside the steps): *Effective Java* (EJ) items 10, 11, 15–18, 64; *Head First Design Patterns* (intro chapter on strategy); *UML Distilled* (class diagrams chapter); *Clean Architecture* (chapters on SRP, OCP, DIP); *Refactoring* (Fowler), "Replace Conditional with Polymorphism" and "Move Method" entries.

---

## S1 Try it (3h): the core ideas, each with a tiny exercise
Pattern for every step: **guess → small code → read → reconcile.** Write your guess in `notes/m2.md` first.

1. (10m) **Guess.** One line each, from your own understanding, no search: encapsulation, abstraction, polymorphism, coupling, cohesion. Keep this; you will grade yourself at the end of S4.
2. (30m) **Encapsulation and invariants.** Create `s1/Account.java`:
   ```java
   public class Account { public long balanceCents; public String owner; }
   ```
   **Predict:** list three ways a caller can put this object in a state that makes no sense. Then write a scenario that does one of them (`acc.balanceCents = -50`). Now rewrite so it is impossible:
   ```java
   public final class Account {
       private final String owner;
       private long balanceCents;
       public Account(String owner, long openingCents) {
           if (owner == null || owner.isBlank()) throw new IllegalArgumentException("owner");
           if (openingCents < 0) throw new IllegalArgumentException("opening balance");
           this.owner = owner; this.balanceCents = openingCents;
       }
       public void deposit(long cents) { require(cents > 0); balanceCents += cents; }
       public void withdraw(long cents) {
           require(cents > 0);
           if (cents > balanceCents) throw new IllegalStateException("insufficient funds");
           balanceCents -= cents;
       }
       public long balanceCents() { return balanceCents; }
       private static void require(boolean ok) { if (!ok) throw new IllegalArgumentException(); }
   }
   ```
   Read **EJ Items 15 and 16** (minimize accessibility; use accessors, not public fields) and **Item 17** (minimize mutability). Write: an *invariant* is a statement that is always true for an object ("balance is never negative"); who is responsible for keeping it true?
3. (30m) **Tell, don't ask.** Predict what is wrong with this caller code, then rewrite it:
   ```java
   if (account.getBalance() >= amount) { account.setBalance(account.getBalance() - amount); }
   else { throw new IllegalStateException(); }
   ```
   Problems: the rule is outside the object, and two callers can each re-implement it differently. Fix: `account.withdraw(amount)`. **Hunt:** open `library-v2/` and find three places where code *asks* an object for data to decide something that the object could decide itself. Write the file and line in your notes (you will fix them in S2).
4. (35m) **Cohesion, coupling, SRP.** Define in your notes: *cohesion* = how closely the parts of one class belong together; *coupling* = how much one class knows about another's insides. Aim: high cohesion, low coupling. Read the SRP chapter in *Clean Architecture* ("a module has one reason to change"). Exercise: split this class so that each part has one reason to change, then write what changes in each when (a) the report format becomes CSV, (b) the tax rate changes:
   ```java
   class InvoiceService {
       double total(List<Line> lines) { /* sums, applies tax */ }
       String toHtml(Invoice i) { /* formatting */ }
       void save(Invoice i) { /* writes a file */ }
   }
   ```
   Expected shape: `Invoice` (knows its total), `InvoiceHtmlFormat`, `InvoiceFile`. Notice that "SRP" is about *reasons to change*, not "one method".
5. (35m) **Composition over inheritance: the trap.** Read **EJ Item 18** (favor composition over inheritance). Concrete example. A shop wants discounted orders:
   ```java
   class Order { Money total() { ... } }
   class DiscountedOrder extends Order { ... }
   ```
   Then the shop adds gift-wrap, express shipping and loyalty points. **Predict:** if each of those 3 features is a subclass and they can be combined, how many classes do you need? (Answer: up to 2³ = 8.) Replace it with a field:
   ```java
   interface PricingRule { Money apply(Money subtotal); }
   final class Order {
       private final List<PricingRule> rules;
       Money total() { Money m = subtotal(); for (PricingRule r : rules) m = r.apply(m); return m; }
   }
   ```
   Rule of thumb: inherit only when it is truly *is-a* and the subclass honours every promise of the parent (the Liskov rule you met in week 5); otherwise hold the other object and delegate. An order *has* pricing rules; it *is not* a kind of discount.
6. (30m) **Polymorphism through interfaces, OCP, DIP.** Take your `parking-v2/` fee calculation. **Predict:** the owner wants "weekends are free" (your M1 prediction). Which files change today? Then introduce:
   ```java
   interface FeePolicy { Money feeFor(Duration stay, LocalDateTime exit); }
   ```
   with `HourlyWithDailyCap implements FeePolicy` and `WeekendFree implements FeePolicy` (wraps another policy). `ParkingLot` receives a `FeePolicy` in its constructor and calls it. Define: **OCP** (open for extension, closed for modification): adding `WeekendFree` edits no existing class except the line that builds the lot. **DIP**: `ParkingLot` depends on the abstraction `FeePolicy`, not on a concrete fee class. Count the files that changed this time.
7. (10m) **Notes.** Update `notes/m2.md`: write one correction to your step-1 guesses and one open question.

**Tips and common problems**
- `cannot find symbol Money` → you did not carry your week-4 class over → copy it into the folder or use `long cents` and say so.
- You made an interface for everything → an interface pays off when there are two or more real variants, or when you want to hide a decision that may change → ask "what second implementation do I expect?"
- Splitting classes creates tiny classes with no logic → you split by method instead of by reason to change → ask which part would change for a different reason.
- Inheritance feels easier → it is, today; composition is cheaper over a year. Keep inheritance for real *is-a*.

**Done when**
- [ ] `s1/Account.java` cannot be put in an invalid state, with a scenario proving the rejection.
- [ ] The invoice split and the `FeePolicy` change are in `parking-v2/` and scenarios pass.
- [ ] `notes/m2.md` has your step-1 guesses, a correction and three "asking" spots in the Library.

---

## S2 Build (3h): UML in plain words, entity vs value, and refactoring the Library
1. (15m) **Read a diagram before reading its legend.** Paste into a `.md` file and preview it. **Predict** what each line means, then check the legend below.
   ```mermaid
   classDiagram
     class Member { -String name  +borrow(Copy) }
     class Loan { -LocalDate due }
     class Copy { -String barcode }
     class Title { -String name }
     Member "1" --> "0..5" Loan : has active
     Loan "*" --> "1" Copy : of
     Copy "*" --> "1" Title : is a copy of
     Library "1" *-- "*" Copy : owns
     Fee <|-- HourlyFee
     Fee <|-- DailyCapFee
     ParkingLot ..> Fee : uses
   ```
   Legend: `-->` association ("knows"); `*--` composition (part dies with the whole); `o--` aggregation (whole groups parts that can live alone; use rarely); `<|--` inheritance/implementation; `..>` dependency (uses briefly). Numbers are **multiplicity**: `1`, `0..1`, `*` (many), `0..5`.
2. (25m) **Read** the class diagram chapter of *UML Distilled*. Then draw (Mermaid) the coffee-shop model from M1 S1 with multiplicities: how many lines in an order (1..*), how many extras on a line (0..*). Rule: arrows carry a direction only if code navigates that way; label the line with the verb.
3. (20m) **Entity or value?** Two questions. (a) If two objects have identical attributes, are they the *same thing*? If yes → **value** (compare by content). (b) Does it keep its identity while its attributes change? If yes → **entity** (compare by id). Classify without help, then open the answers: `Money`, `Member`, `Copy`, `Title`, `Address`, `DateRange`, `ParkingTicket`, `ParkingSpot`.
   <details><summary>Answers</summary>

   Value: `Money`, `Address` (two addresses with the same text are equal), `DateRange`. Entity: `Member`, `Copy` (two copies of the same title are different physical things), `Title` (identified by ISBN, say), `ParkingSpot` (spot 3A stays spot 3A). `ParkingTicket` is an entity (its number identifies it). Consequence in Java: values are immutable and use `equals`/`hashCode` over all fields (records, **EJ Items 10, 11**); entities compare by id and may change state.
   </details>
4. (15m) **Reverse-engineer the "before".** From `library/` (tag `m1-library-before`) draw the class diagram of what the code *actually* does (not what you intended). Save as `diagrams/library-before.md`. Mark classes with a lot of arrows to them.
5. (75m) **Refactor `library-v2/` in small commits** (run scenarios after each; tag each as `m2-library-stepN`). Work through the list in order and stop where time runs out:
   1. (10m) Remove public fields and setters; add constructors that enforce invariants.
   2. (15m) For each "ask" spot from S1 step 3, move the decision into the object (`member.borrow(copy)`, `copy.isAvailable()`).
   3. (10m) Replace `int status` or `String` codes with an enum (`LoanState`).
   4. (15m) Put the "max 5 loans" rule in one method; delete all other copies.
   5. (15m) Pull late-fee calculation behind `interface FinePolicy { Money fineFor(Loan loan, LocalDate returned); }`.
   6. (10m) Make `Title` and `Copy` different classes if they were one, and make value-like things (`Isbn`, `Money`, a date range) immutable.
6. (30m) **Draw the "after"** in `diagrams/library-after.md` and write 5 lines: what moved, what got simpler, what got more complicated.

**Tips and common problems**
- Mermaid shows a syntax error → a `:` or generic `<>` in a member → remove generics from the diagram text; use `List~Loan~` style (verify against current docs).
- Scenarios break after step 2 → expected; fix the scenario only if it reached into internals, never to hide a rule.
- You are unsure if a relationship is composition or association → ask "if I delete the whole, must the part disappear?" Yes → composition; no → association.
- You add `getX()` for every field "just in case" → expose behaviour (`isOverdue()`), not data.

**Done when**
- [ ] `diagrams/library-before.md` and `diagrams/library-after.md` exist with multiplicities.
- [ ] `library-v2/` scenarios pass with at least 4 refactor commits or tags.
- [ ] The entity/value table is in your notes, with a stated `equals` rule for each.

---

## S3 Practice (2h): smell clinic, then the Parking lot
1. (30m) **Smell clinic.** For each snippet, write in your notes: the smell, why it hurts, the fix in one sentence. Only then open the answers.
   - **A**
     ```java
     class OrderManager {
         double total(Order o) {
             double sum = 0;
             for (Line l : o.getLines()) sum += l.getQty() * l.getPrice();
             if (o.getType().equals("GIFT")) sum += 3;
             else if (o.getType().equals("EXPRESS")) sum += 8;
             return sum;
         }
     }
     ```
   - **B**
     ```java
     abstract class Vehicle { String plate; }
     class Car extends Vehicle { } class Truck extends Vehicle { } class Bike extends Vehicle { }
     class ParkingLot {
         Spot find(Vehicle v) {
             if (v instanceof Car) return freeSpot(SpotSize.MEDIUM);
             else if (v instanceof Truck) return freeSpot(SpotSize.LARGE);
             else return freeSpot(SpotSize.SMALL);
         }
     }
     ```
   - **C**
     ```java
     class Member { public List<Loan> loans = new ArrayList<>(); public int status; /* 0 ok, 1 blocked, 2 closed */ }
     // somewhere else
     if (member.status == 0 && member.loans.size() < 5) member.loans.add(new Loan(copy));
     ```
   <details><summary>Answers</summary>

   **A**: anemic order plus a `*Manager` (logic away from the data), `double` for money, type codes with an `if` chain (adding a type edits this method; violates OCP). Fix: `Order.total()` using a `PricingRule` or `ShippingOption` per type; `Money` instead of `double`.
   **B**: `instanceof` chain, and the subclasses carry no behaviour, so inheritance buys nothing. Fix: a `SpotSize requiredSize()` on the vehicle (or an enum field `VehicleKind` with a size), then `lot.find(v.requiredSize())`; no `instanceof`.
   **C**: public mutable list, a magic-number status, and the borrowing rule written outside `Member` (copied wherever someone borrows). Fix: private list, `enum MemberStatus`, `member.borrow(copy)` that checks status and limit and returns the `Loan`.
   </details>
2. (50m) **Refactor `parking-v2/`.** Apply the same recipe in order: (10m) before-diagram from code; (35m) private fields and invariants, one place for each rule, `FeePolicy` (done in S1), `SpotSize` and `VehicleKind` instead of `instanceof`, `Ticket` as an entity with an id; (5m) after-diagram in `diagrams/parking-after.md`. Scenarios must stay green.
3. (25m) **Re-score.** Score `library-v2/` and `parking-v2/` on C1–C8 and put the new numbers next to the M1 numbers:
   | Domain | M1 total | M2 total | Rows that moved |
   |---|---|---|---|
   | Library | _ /16 | _ /16 | |
   | Parking | _ /16 | _ /16 | |
   Expect +3 to +6 in total. If a row did not move, write why.
4. (15m) **Letter to M5.** Write 8 lines: what you will now do differently at the start of the first practical domain (M3 and M4 teach the building blocks first) (for example "I will write the 3 scenarios first and the invariant list second").

**Tips and common problems**
- You cannot see the smell in a snippet → read it as the caller: "if I add a new type, what do I edit?" → each place you edit is a smell.
- The refactor takes 3x longer → you are rewriting instead of moving → change one thing, run scenarios, commit.
- Scores did not improve → your scenarios may not exercise rules (C8) or the rules are still in two places (C3) → check these two first.

**Done when**
- [ ] Smell clinic answers written before reading the reveal (be honest about the ones you missed).
- [ ] `parking-v2/` scenarios pass; `diagrams/parking-after.md` exists.
- [ ] The before/after score table is filled in.

---

## S4 Consolidate (2h)
1. (20m) **Ledger.** Two entries: (a) "Type code with `if` chain" (symptom: touching many places to add a variant; fix: polymorphism/strategy; evidence: your `FeePolicy` change), (b) "Inheritance for code reuse" (symptom: class explosion; fix: composition).
2. (20m) **Flashcards (5).**
   1) Tell, don't ask? → Give an object the job instead of reading its data and deciding outside.
   2) When is inheritance right? → True is-a, subclass keeps every parent promise.
   3) OCP in one line? → Add behaviour by adding code, not by editing existing, tested code.
   4) Entity vs value? → Entity: same identity over changing state. Value: equal if all fields equal, immutable.
   5) Composition vs aggregation? → Composition: the part cannot outlive the whole. Aggregation: the whole only groups parts.
3. (25m) **Self-check (answer out loud, then open).**
   1. Why is a public field worse than a getter, if both expose data?
   2. Give an example where inheritance is fine and one where composition is better.
   3. What does DIP mean for `ParkingLot` and `FeePolicy`?
   4. Draw (in words) the multiplicity of "a member has at most 5 active loans, a loan is for exactly one copy".
   5. Is `Address` an entity or a value? What follows for `equals`?
   <details><summary>Answers</summary>

   1. A field cannot enforce any rule, and every caller depends on the field forever; a method can validate, change representation, or compute. A bare getter is also weak; the best fix is exposing behaviour.
   2. Fine: `ArrayList` is a `List` (honours the whole contract). Better composition: discounts, shipping and gift options on an order.
   3. `ParkingLot` depends on the interface; concrete fee classes depend on it too; high-level code does not know low-level details and you can swap policies without editing the lot.
   4. `Member 1 → 0..5 Loan`, `Loan * → 1 Copy`.
   5. Usually a value: equal if all fields are equal, immutable. It would become an entity only if the business tracks it by identity (for example a "saved address" record with an id); that is a modeling decision you write down.
   </details>
   Score: fewer than 4 of 5 → reread the S1 step you missed and retry next S4.
4. (40m) **Preview for M5, diagram only.** New brief (not part of the practice domains): *A bike-rental station has bikes of two types (city, e-bike). A user unlocks a bike with a card, rides, and returns it to any station with a free dock. Rides under 30 minutes are free for members; others pay per started 15 minutes. E-bikes have a battery level and cannot be unlocked below 10%.* Do only: nouns/verbs/rules (10m), 3 scenarios in words (5m), class diagram with multiplicities (15m), list of invariants (10m). No code. Save as `diagrams/bike-rental.md`; you will not get a solution, compare it to your models in M5.
5. (10m) **Grade your step-1 guesses.** Reread `notes/m2.md` step 1 and rewrite each definition correctly in one line.
6. (5m) **Retro** in `retro.md`: what worked, what blocked me, what I will change next week. Commit and tag `m2-done`.

**Tips and common problems**
- The preview takes longer than 40 minutes → stop at the invariant list; the point is the habit of listing invariants.
- Definitions still sound vague → say each with a concrete class from your code ("`Account` hides `balanceCents` so no one can set a negative value").

**Done when**
- [ ] Two ledger entries, five flashcards, self-check score 4/5 or better.
- [ ] `diagrams/bike-rental.md` exists; step-1 definitions corrected.
- [ ] Retro lines written; tag `m2-done` exists.

**Output:** `library-v2/`, `parking-v2/`, `diagrams/` (library before/after, parking after, bike rental), before/after rubric table, 2 ledger entries, 5 flashcards.
**Next:** M3 and M4 teach the building blocks of real business domains (people, roles, organizations, money, time, history). M5 and M6 apply everything to ten practical domains (retail, orders, banking, ledger, booking, billing, HR, access, inventory, insurance). M7 and M8 teach how to store them (data modeling and ERD).
**If stuck:** do only steps 2–3 of S1 on your own Library: remove public fields, then move one rule into the object. Everything else builds on that habit.
