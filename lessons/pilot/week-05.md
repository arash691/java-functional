# Week 5: Inheritance, interfaces, enums and sealed types

**Mode:** A → B (build-along first, then two incidents)  **Release:** v0.1 CLI
**Outcome:** you can model "a thing with variants" in three ways (interface or abstract class, enum, sealed types), and you have found and fixed two classic design bugs: a mutable `HashMap` key and a subclass that breaks its parent's promises.

**Before you start**
- Weeks 3 and 4 done (you can write a class, a record, and run a test).
- Open a terminal in the root of `shopflow-lab`. Everything below is run from there.
- Read the red/green rule once more: **red tests are the starting point**. At the start of this week about 21 of the 53 tests in the week are already green (guards and "forbidden" cases that a stub passes by accident). Don't be fooled by a green test: check what it proves.

**Repo:** `shopflow-lab`, module `core`, package `shopflow.w05`, tag `@Tag("w05")`
Run all: `scripts/run-week.sh w05`   One class: `./mvnw -q -pl core test -Dtest=OrderStatusTest -Dsurefire.failIfNoSpecifiedTests=false`
Hints: `hints/w05/` (INC-05-1, INC-05-2, SF-03)   Solutions: `solutions/w05/` (only after your tests are green)

---

## S1 Try it (3h): interface, abstract class, enum, sealed type (mode A)

You know the TypeScript versions of these ideas (`interface`, `enum`, union types with a `type` field). Java has stronger, compiler-checked versions. Today you meet each one by running a small example, changing it, and then writing your own.

1. (15m) **Run the worked example.** Predict first: write on paper what `NotifierDemo` prints (4 lines). Then compile and run:
   - `./mvnw -q -pl core compile`
   - `java -cp core/target/classes shopflow.w05.example.NotifierDemo`
   Compare with your prediction. Which line surprised you, and why? (Hint: look at the SMS length rule.)
2. (25m) **Read and change it.** Open the five files in `core/src/main/java/shopflow/w05/example/`. Answer in your notes: (a) Which type is the *interface*, which the *abstract class*, which the *concrete classes*? (b) Which method is the "template" and which are the "hooks"? (c) What does `final` on `send` prevent? Then change things:
   - Add `PushNotifier extends BaseNotifier` (channel `"push"`, format `"🔔 " + message`) and add it to the list in `NotifierDemo`. Run again.
   - Change `SmsNotifier` to truncate at 10 characters. Run `scripts/run-week.sh w05` and look at which `WorkedExampleTest` test goes red. Is the failure message good enough to tell you why? Revert the change.
3. (20m) **Just-in-time reading.** You have now used all three. Read for the *why*: Java Tutorials, "Interfaces and Inheritance" (overriding, `super`, abstract classes, default methods), and **EJ Item 18** (favor composition over inheritance) and **Item 20** (prefer interfaces to abstract classes). Write two sentences: when would you pick an interface, when an abstract class, and what is the danger of a long inheritance chain?
4. (10m) **Break it on purpose.** In `SmsNotifier`, delete the `format` method. Read the compiler error. Then in `BaseNotifier` remove `final` from `send`, override `send` in `SmsNotifier` to return `"hacked"`, and run the demo. What did `final` protect? Put everything back (`git checkout -- core/src/main/java/shopflow/w05/example`).
5. (30m) **EX-05-01: enum with behaviour.** Open `OrderStatus.java` and `OrderStatusTest.java`.
   - Read the test names first; they describe the rules. Draw the allowed transitions on paper (the diagram in the Javadoc is your check).
   - Implement `allowedNext()` first. **Gotcha:** you cannot write `CREATED(PAID, CANCELLED)` with constants as arguments, because `PAID` is declared later (illegal forward reference). Use a `switch (this)` expression that returns an `EnumSet` (see the Tips).
   - Then `canTransitionTo` (one line using `allowedNext()`), then `isTerminal` (one line).
   - Run `./mvnw -q -pl core test -Dtest=OrderStatusTest -Dsurefire.failIfNoSpecifiedTests=false`. Aim for green. Return an unmodifiable set so callers can't change the lifecycle.
6. (30m) **EX-05-02: sealed interface and pattern-matching switch.** Open `PaymentResult.java` (complete) and `PaymentMessages.java` (stubs).
   - Read **JEP 409** (sealed classes) summary and the "pattern matching for switch" section of **JEP 441**. Write what "exhaustive" means in one sentence.
   - Implement `describe` and `isFinal` with `switch (result) { case Approved a -> ...; case Declined d -> ...; case Pending p -> ...; }`. No `default` branch.
   - **Break it:** add a fourth record `Failed(String error)` to `PaymentResult`. Read the compile error in `PaymentMessages`. This is the point: the compiler tells you every place that must handle the new variant. Remove `Failed` again.
7. (40m) **EX-05-03: abstract class with a template method.** Open `ShippingMethod.java` and the three subclasses.
   - In `ShippingMethod.quote`, validate the weight (throw `IllegalArgumentException` for `<= 0`), call `costCents`, and format `"Name: euros.cents"` with `String.format("%s: %d.%02d", ...)`.
   - `StandardShipping`: 4.00 plus 1.00 per *started* kilogram. Integer maths: `(weightGrams + 999) / 1000` started kilos.
   - `ExpressShipping`: twice the standard price plus 3.00. **Reuse** a `StandardShipping` instance (composition) rather than copying the formula. `costCents` is `protected`, and both classes are in the same package, so you can call it.
   - `PickupShipping`: 0.
   - Run `-Dtest=ShippingMethodTest`. If a polymorphism test fails, check you used `name()` and not a hard-coded string.
8. (10m) **Check and commit.** Run `scripts/run-week.sh w05`. The S1 test classes (`WorkedExampleTest`, `OrderStatusTest`, `PaymentMessagesTest`, `ShippingMethodTest`) must be green; `OrderTest` and the two `INC` tests are still red. Commit: `git add -A && git commit -m "w05 S1: OrderStatus, PaymentMessages, ShippingMethod"`.

**Tips and common problems**
- `illegal forward reference` when you write `CREATED(PAID)` → enum constants are initialized in order → compute the set in a method with `switch (this)` or use a static initializer block.
- `the switch expression does not cover all possible input values` → you forgot a case, or the sealed `permits` list changed → add the case; do not add `default` (it would hide future variants).
- `ShippingMethod.quote` is `final` and your IDE says "cannot override" → that is the point; put your logic in `costCents`.
- `OrderStatusTest` shows 11 of its 18 tests green at the start → the stub returns `false`, which passes the "forbidden" cases by accident → trust only the `allowed` tests until the whole class is green.
- `Cannot resolve symbol PaymentResult.Approved` → import `shopflow.w05.PaymentResult.Approved` or refer to it as `PaymentResult.Approved`.

**Done when**
- [ ] `OrderStatusTest`, `PaymentMessagesTest`, `ShippingMethodTest` and `WorkedExampleTest` are green.
- [ ] You can say, without looking, the difference between an interface, an abstract class, an enum and a sealed type, in one sentence each.
- [ ] You saw the compiler error when a new `PaymentResult` variant is added, and know why it is useful.
- [ ] Work committed.

---

## S2 Build (3h): SF-03, the order lifecycle (mode A, tests first)

**Ticket SF-03 (continued):** *An order moves through CREATED, PAID, SHIPPED, DELIVERED and can be cancelled before shipping. Keep a history of every change. Reject illegal moves with a clear message.* The acceptance tests are in `OrderTest.java`.

1. (15m) **Read the tests first.** Open `OrderTest.java` and, without coding, list the rules in your own words (aim for 8 to 10 bullets). Check: what happens on a declined payment? on a pending one? on a payment for an order that is already PAID? what is the exact error message for an illegal move?
2. (10m) **Run and look.** `./mvnw -q -pl core test -Dtest='OrderTest*' -Dsurefire.failIfNoSpecifiedTests=false`. Expect most tests to fail with `UnsupportedOperationException: TODO SF-03` (the nested classes are reported as `OrderTest$...`). Note how many are red.
3. (25m) **One method to rule them all.** Add a private `transitionTo(OrderStatus next)` in `Order`: if `!status.canTransitionTo(next)` throw `IllegalStateException("Cannot go from " + status + " to " + next)`; otherwise append `new StatusChange(status, next)` to `history` and update `status`. Then write `ship()`, `deliver()` and `cancel()` as one-liners. Run the `Fulfilment` and `Cancellation` nested tests. Commit when that group is green.
4. (30m) **Payments with a sealed switch.** In `applyPayment`: first reject (with `IllegalStateException`) when the status is not `CREATED`; then `switch (result)`: `Approved` → `transitionTo(PAID)` and remember the transaction id; `Declined` → increment `failedPayments`; `Pending` → do nothing. No `default`. Run the `Payment` nested tests.
5. (20m) **History is read-only.** The accessor already returns an unmodifiable view; run `historyIsReadOnly` and check you understand *why* it is green (look at `Collections.unmodifiableList`). Then try `o.history().add(...)` yourself in a scratch test and read the exception.
6. (30m) **Refactor while green.** Remove duplication, rename for clarity, check method order (public first, private last). Reread the Javadoc of `OrderStatus` and update it if your implementation differs. Run all `w05` tests for the S1/S2 classes.
7. (30m) **Your own edge cases.** Add three tests in `OrderTest` (in a new `@Nested` class `MyEdgeCases`): (a) Declined, Declined, then Approved still works and `failedPaymentAttempts()` is 2; (b) Pending then Approved works; (c) deliver after cancel throws. Make them green (they should pass already; if not, you found a bug).
8. (20m) **Ship it.** Commit with a message that mentions SF-03. Write a three-line PR description: what changed, how it was tested, one risk. Save it in `~/shopflow-notes/pr-sf03.md`.

**Tips and common problems**
- `IllegalStateException` message differs by one character → the tests use the exact text → copy the format `"Cannot go from X to Y"`, no trailing period.
- `history()` test fails after you changed it to return the field → return `Collections.unmodifiableList(history)` (or `List.copyOf`).
- Switch complains it needs a `default` → you used the wrong static type (e.g. `Object`) → the parameter must be `PaymentResult`.
- `Order` tests all fail with `UnsupportedOperationException` from `OrderStatus` → `OrderStatus` from S1 is not finished → go back to S1 step 5.
- Don't put the "only while CREATED" check inside `transitionTo`; a declined payment must be rejected on a PAID order too, even though it makes no transition.

**Done when**
- [ ] All tests in `OrderTest` are green, plus your three extra tests.
- [ ] `Order` has exactly one place that changes `status`.
- [ ] A commit and a PR description exist.

---

## S3 Incident (2h): two bugs that look like features (mode B)

### Incident 1 (60 min): INC-05-1, "Loyalty points disappear when a customer is renamed" (RL-05)

> **Support ticket #4821.** *"Sara changed her last name on her profile. Her 120 loyalty points show as 0 now. Finance says the total points issued didn't change. Please fix and tell us whether other customers are affected."*

1. (10m) **PREDICT.** Open `loyalty/LoyaltyLedger.java` and `Customer.java` (read only, do not run yet). Write your answers in your notes:
   1) What kind of map is the ledger, and what is the key type?
   2) Which fields feed `Customer.hashCode()`?
   3) After `rename`, will `points.get(customer)` look in the same bucket as before?
   4) If you add 20 points after the rename, how many entries will the map hold?
   <details><summary>Answers</summary>

   1) A `HashMap<Customer,Integer>`; the `Customer` object itself is the key. 2) `id` and `name`. 3) No: `hashCode` changed, so the lookup goes to another bucket and the old entry is unreachable. 4) Two (the old, orphaned one and a new one).
   </details>
2. (5m) **REPRODUCE.** `./mvnw -q -pl core test -Dtest=LoyaltyLedgerTest -Dsurefire.failIfNoSpecifiedTests=false`. Expect `pointsSurviveRename` to fail with `[points after rename] expected: 120 but was: 0`, and `pointsAfterRenameAccumulate` to fail with `[number of accounts] expected: 1 but was: 2`. The `GUARD` tests and the `CLUE` test (`totalIsStillThere`) are green. Read the `CLUE` test: what does it tell you?
3. (15m) **INVESTIGATE.** Put a breakpoint at the first line of `pointsOf`. Debug `pointsSurviveRename`. Before and after the `rename` call, evaluate `sara.hashCode()`. In the Variables view find `ledger.points` → `table` and look at which bucket index holds the entry. Write down the two hash codes. If you are stuck for more than 10 minutes, open `hints/w05/INC-05-1.md` one level at a time.
4. (15m) **STUDY, now that you have seen it.** Read **EJ Item 10** (obey the general contract when overriding `equals`) and **Item 11** (always override `hashCode` when you override `equals`), and the contract in the Javadoc of `Object.hashCode()`. Answer from the text: (a) What does the contract say must hold *as long as the fields used in equals do not change*? (b) What happens to a `HashMap` entry if its key's hash changes after insertion? (c) Which fields of `Customer` identify a customer for the lifetime of the business?
5. (5m) **FIX.** Change `Customer.equals/hashCode` so they use only the immutable identity (`id`). (Alternative that also works: make the ledger a `Map<String,Integer>` keyed by id.) Keep the change small.
6. (5m) **PROVE.** Rerun `LoyaltyLedgerTest`: all five green. Also run all of `w05` to make sure you broke nothing. Now add your own regression test: two customers with the same name but different ids must not be equal.
7. (5m) **GENERALIZE.** Write the ledger entry `RL-05` in `~/shopflow-notes/ledger.md` using the template in `reality-lab/LEDGER.md`. One-minute answer: *"A `HashMap` finds a key by its hash code. If a field used in `hashCode` changes after the key was inserted, the entry is stored under the old hash and lookups use the new one, so the value seems to vanish. Use immutable fields for `equals`/`hashCode`, or key by an immutable id."*

### Incident 2 (60 min): INC-05-2, "Gift cubes are charged for the wrong volume" (Liskov extra)

> **Ops alert.** *"Shipping cost for gift cubes is 4.5x too high when the packer resizes the box. Normal boxes are fine. Also: the original box in the packing list changes after we ask for a resized quote."*

1. (10m) **PREDICT.** Read `packaging/Box.java`, `Boxes.java` (including the nested `Cube`) and `Packaging.java`. Write answers:
   1) What are the three field values of a cube of side 10 after `setWidth(10); setHeight(20); setDepth(30)`?
   2) What does `resized` return for a plain `Box`: the same object or a new one?
   3) Which assumption does the code in `Packaging.resized` make about `setWidth`?
   <details><summary>Answers</summary>

   1) All three are 30: every setter on `Cube` sets all sides, so the last call wins (volume 27,000). 2) The same object, mutated, so the caller's box changes too. 3) That `setWidth` changes only the width.
   </details>
2. (5m) **REPRODUCE.** `-Dtest=PackagingTest`. Expect: `resizedCubeUsesRequestedDimensions` fails with `[volume of 10x20x30] expected: 6000 but was: 27000`, `resizedCubeIsNotACube` fails, `originalIsUntouched` fails with `[original volume] expected: 6 but was: 729`. The two `GUARD` tests are green.
3. (15m) **INVESTIGATE.** On paper, trace the three setter calls on a `Cube(10)` and write the three fields after each call. Then confirm in the debugger by stepping through `resizedCubeUsesRequestedDimensions`. Note a second problem the test `originalIsUntouched` shows (mutation of an argument). Hints: `hints/w05/INC-05-2.md`.
4. (15m) **STUDY.** Search for "Liskov substitution principle" and "Square Rectangle problem" and read one good explanation. Read **EJ Item 17** (minimize mutability) and re-read **Item 18**. Answer: (a) State the Liskov principle in one sentence. (b) Why is `Cube extends Box` a violation even though a cube *is* a box in geometry? (c) Which two rules from EJ would have prevented this bug?
5. (5m) **FIX.** Make `Box` immutable (final fields, no setters). `Packaging.resized` returns a new box. Remove the `Cube` subclass: `Boxes.cube(side)` just returns `Boxes.of(side, side, side)`. Keep the public methods the tests use (`volume`, `isCube`).
6. (5m) **PROVE.** `PackagingTest` green (5 tests); `w05` all green except nothing else should be red.
7. (5m) **GENERALIZE.** Ledger entry for "LSP: subclass breaks parent's contract (extra)". One-minute answer: *"A subclass must be usable anywhere its parent is, without surprising the caller. A mutable `Cube` that changes three fields on every setter breaks callers that assume setters are independent. Prefer immutability and composition over inheritance."*

**Tips and common problems**
- Breakpoint never hits → you ran the test with `Run` instead of `Debug`, or you are running through Maven → debug the test method from the IDE gutter icon.
- You changed `equals` but the test is still red → you forgot `hashCode`, or `hashCode` still uses `name` → both must use the same fields.
- After removing `Cube`, `Boxes.java` has an unused import / compile error → delete the nested class completely; no one else references it.
- You are tempted to "fix" the test → the tests describe the required behaviour, do not edit them (add new ones).

**Done when**
- [ ] `LoyaltyLedgerTest` (5 tests) and `PackagingTest` (5 tests) are green; `scripts/run-week.sh w05` is fully green.
- [ ] Two ledger entries written, each with symptom, cause, tool, fix, prevention and the one-minute answer.
- [ ] You can explain both bugs aloud in under a minute each.

---

## S4 Consolidate (2h)

1. (25m) **Ledger and notes.** Finish the two ledger entries: for each, fill *Symptom, Why, Reproduce, Observe with, Fix, Prevent, One-minute answer, Confidence (1 to 5)*. Under "Prevent", write a concrete safeguard (e.g. "make key classes immutable", "a test that renames a customer and re-reads points").
2. (20m) **Flashcards.** Create five cards (question front, answer back) from today. Suggested: (1) What does `final` on a method do? (2) Why can't enum constants reference later constants in their constructor arguments? (3) What does `sealed` give you that an enum does not? (4) What must a `hashCode` implementation not depend on? (5) State the Liskov principle.
3. (25m) **Decision note.** Write `~/shopflow-notes/enum-vs-sealed-vs-interface.md`: for each of the three, give a definition, three real ShopFlow examples (e.g. order status, payment result, notifier channels), and "when I would NOT use it".
4. (20m) **Compare two styles.** In a scratch file, write `describe(PaymentResult)` the old way (an `if (r instanceof Approved) ... else if ...` chain) and compare it with your `switch`. Add a fourth variant to both; which one tells you about the missing case at compile time?
5. (15m) **Self-check.** Answer aloud, then open the answers.
   1) Why is a `HashMap` key with a mutable `hashCode` dangerous? 2) What is a template method? 3) Why did the sealed `switch` need no `default`? 4) Why is `Cube extends Box` a bad idea here? 5) When is composition better than inheritance?
   <details><summary>Answers</summary>

   1) The entry is stored by the old hash, lookups use the new one, so the entry becomes unreachable (a silent leak). 2) A `final` method in a base class that fixes the algorithm and calls abstract "hook" methods that subclasses fill in. 3) The compiler knows the full list of permitted subtypes, so it can check every case is covered; adding a variant makes it a compile error. 4) A cube can't honour the "setters are independent" contract of its parent. 5) When the relationship is "uses" rather than "is a", or the parent is mutable or likely to change: you reuse behaviour without inheriting its promises.
   </details>
6. (10m) **Retro.** Three lines in `~/shopflow-notes/retro.md`: what worked, what blocked me (and for how long), what I change next week. Tick Week 5 in your progress tracker.
7. (5m) **Commit and tag.** `git add -A && git commit -m "w05 done" && git tag w05-done`.

**Done when**
- [ ] Two complete ledger entries and five flashcards exist.
- [ ] The decision note is written.
- [ ] You answered at least 4 of 5 self-check questions without looking.

**Output this week:** `OrderStatus`, `PaymentMessages`, `ShippingMethod` family, `Order` (SF-03), fixed `Customer` and `Box`, two ledger entries, the decision note, tag `w05-done`.

**If stuck:** for sealed types re-read the examples in JEP 409; for equals/hashCode re-read EJ Items 10 and 11; the debugger's Variables view is the fastest way to see a changing hash code.
