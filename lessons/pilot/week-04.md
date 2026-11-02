# Week 4: Classes, immutability and money

**Mode:** A → B (build-along, then two incidents)  **Release:** v0.1 (value objects for the CLI)
**Outcome:** you can design small immutable classes and records from tests, you built `Money` and `Product`, and you found and fixed two production-style bugs: a missing `hashCode` and `double` money.

**Before you start**
- Week 3 is finished (`scripts/run-week.sh w03` is green).
- Run `scripts/run-week.sh w04` once and read the names of the failing tests. They are the specification for the week.
- Single test class: `./mvnw -q -pl core test -Dgroups=w04 -Dtest=CustomerTest -Dsurefire.failIfNoSpecifiedTests=false`

**Repo:** `shopflow-lab`, module `core`, package `shopflow.w04`, tag `@Tag("w04")`
Run: `scripts/run-week.sh w04`   Hints: `hints/w04/`   Solutions (peek only after green): `solutions/w04/`

---

## S1 Try it (3h): designing classes from tests (mode A)
Today's idea: **an object should be valid from the moment it exists, and it should not change behind your back.** You'll practise that on four small types.

1. (10m) **Read the worked example.** Open `Rectangle.java` and `RectangleTest.java`. Predict: what do `new Rectangle(2,3).scaledBy(2).area()` and the original's `area()` return? Run `RectangleTest` (green) and confirm. Note: `private final` fields, validation in the constructor, no setters, "changing" returns a **new** object.
2. (25m) **Read just enough.** *Effective Java* (EJ) Item 15 "Minimize the accessibility of classes and members", Item 17 "Minimize mutability" (the five rules in the first pages), and the dev.java page on records. Answer: (a) the five rules that make a class immutable; (b) what a record generates for you; (c) why `final` fields are useful for thread-safety (a preview for Phase 5).
3. (35m) **EX-04-01 Customer** (`Customer.java`). Make `CustomerTest` green: private final fields, constructor validation (name not blank; email has exactly one `@` with text before and after), `displayName()`, and `withEmail(...)` that returns a **new** `Customer`. Run the tests after each method.
4. (25m) **EX-04-02 Quantity** (`Quantity.java`). Private constructor + static factory `of(int)`. Read EJ Item 1 "Consider static factory methods instead of constructors" (first pages). Make `QuantityTest` green. Notice `plus` returns a new object.
5. (35m) **EX-04-03 Address record** (`Address.java`). The record already has its components. Add a **compact constructor** with the validation (`countryCode` is exactly two uppercase letters; street and city not blank) and implement `withCity`. Note what you did *not* have to write (equals, hashCode, toString, accessors). Make `AddressTest` green.
6. (35m) **EX-04-04 Basket** (`Basket.java`, the hardest). Make `BasketTest` green by copying on the way in and not leaking the internal list on the way out. Read EJ Item 50 "Make defensive copies when needed" first. Hint ladder: `hints/w04/EX-04-04.md` (one level at a time after 10 minutes).
7. (15m) **Break it on purpose.** Temporarily change `Basket` back to storing the caller's list directly and run `BasketTest`. Which test notices? Restore the fix. Then in `Customer` temporarily add a `setEmail` method and write a one-line test that shows why `withEmail` is safer when a `Customer` is shared between two carts. Delete the experiment.

**Tips and common problems**
- `variable name might not have been initialized` → a `final` field is not assigned on every constructor path → assign it once, after validation.
- A compact record constructor assigns fields manually → you must not; in a compact constructor the fields are assigned automatically at the end (you can reassign the *parameters* before that).
- `cannot assign a value to final variable` → you tried to change a `final` field in `withEmail` → create a new object instead.
- Your email validator accepts `a@b@c.com` → check `indexOf('@') == lastIndexOf('@')`.
- `List.copyOf(null)` throws `NullPointerException` → that is what the test expects; don't catch it.
- Parameterized tests show `[1]`, `[2]` → open the failure report to see which argument failed; the `@ValueSource` list tells you.

**Done when**
- [ ] `RectangleTest`, `CustomerTest`, `QuantityTest`, `AddressTest`, `BasketTest` are green.
- [ ] You can say, without notes, the rules that make a class immutable.
- [ ] You know which Basket test fails when the constructor does not copy.

---

## S2 Build (3h): SF-02 Money and SF-03 Product
Tickets **SF-02** *Money value object* and **SF-03** *Product catalog model*. Acceptance tests: `MoneyTest` and `ProductTest` (red).

1. (15m) **Read the tests and the pattern.** Read `MoneyTest` fully and list the rules (currency code shape, scale 2, rounding, mismatch rejection, equality ignores written form, `toString`). Read Martin Fowler's *Money* pattern in *Patterns of Enterprise Application Architecture* (the catalog page, 10 min).
2. (15m) **BigDecimal primer.** In a scratch `main` (not committed), predict then print: `new BigDecimal(0.1)` vs `new BigDecimal("0.1")`; `new BigDecimal("1.5").equals(new BigDecimal("1.50"))` vs `compareTo(...) == 0`; `new BigDecimal("2.675").setScale(2, RoundingMode.HALF_EVEN)` and `HALF_UP`. Write one sentence for each surprise.
3. (30m) **Money: creation.** Implement the private constructor, `of(String, String)`, `zero`, `amount()`, `currency()`. Validate the currency code (three uppercase letters, `IllegalArgumentException` otherwise); reject null/invalid amounts; always store scale 2 with `RoundingMode.HALF_EVEN`. Run `MoneyTest$Creation` tests (`-Dtest='MoneyTest*'`).
4. (30m) **Money: arithmetic.** `add`, `subtract`, `multiply(int)`, `isNegative`, `isPositive`. A currency mismatch throws `IllegalArgumentException` whose message names **both** currencies. Operands never change.
5. (25m) **Money: equality and printing.** Implement `equals` (same amount and same currency) and `toString` (`19.99 EUR`). Leave a comment `// hashCode? decide in session 3`. **Do not write `hashCode` yet**: you will find out why in the incident. Run `MoneyTest`: everything should be green.
6. (10m) **Commit.** Branch `sf-02-money`, commit, push, open a PR, review, merge.
7. (30m) **SF-03 Product.** The record `Product` and the enum `Category` already exist. Add a compact constructor: sku and name not blank; category not null; price not null and `isPositive()`. Implement `withPrice`. Make `ProductTest` green.
8. (15m) **Refactor on green.** If you repeat the "not blank" check, extract a private static helper. Re-run `scripts/run-week.sh w04` (the incident tests will still be red; that is next session).
9. (10m) **PR.** Branch `sf-03-product`, PR with *What/Why/How tested*, merge.

**Tips and common problems**
- `Money.of("1.5","EUR")` is not equal to `Money.of("1.50","EUR")` → you did not normalise the scale before storing → `setScale(2, HALF_EVEN)` in the constructor.
- A rounding test fails for `0.025` → you used `HALF_UP` (gives 0.03) → the requirement is banker's rounding `HALF_EVEN` (gives 0.02).
- `NumberFormatException` leaks from `of("abc","EUR")` → it is a subclass of `IllegalArgumentException`, so the test passes; but a **null** amount throws `NullPointerException` in `new BigDecimal(null)` → check null first.
- `Product` accepts a zero price → `isPositive` returns true only for > 0; use it.
- A test constructs `Money.of("-1.00","EUR")` and fails with an exception in the test itself → `of` must allow negative amounts (subtract needs them); only `Product` rejects them.

**Done when**
- [ ] `MoneyTest` and `ProductTest` are green.
- [ ] `Money` has no `hashCode` yet (intentional) and a comment explains why.
- [ ] Two PRs (SF-02, SF-03) merged.

---

## S3 Incident (2h): the cart that eats products and the invoice that lies (mode B)
**INC-04-01 ticket (support):** *"Customers say that when they add the same product twice, the cart shows two lines. Others say a product they just added is 'not in the cart' and can't be removed. The code that decides whether two products are the same looks correct."* (Reality Lab RL-04)
**INC-04-02 ticket (finance):** *"Invoice totals are sometimes one cent below the sum of the lines. Ten items of 0.10 do not add up to 1.00. We've undercharged customers."* (Reality Lab RL-03)

1. (10m) **PREDICT** (write answers first, then open):
   - A) A class overrides `equals` (same SKU means equal) but not `hashCode`. You put one instance in a `HashSet` and then ask `contains` with another instance with the same SKU. What happens, and why? B) What does `0.1 + 0.2 == 0.3` evaluate to in Java? C) What does `(long) (0.29 * 100)` give? D) Which type would you use for money amounts?
   <details><summary>Answers</summary>

   A) `contains` is probably `false`: the set first computes the hash code, which defaults to a per-object identity value, so the two instances land in different buckets and `equals` is never called. B) `false` (0.30000000000000004). C) `28`, because `0.29 * 100` is `28.999999999999996` and the cast truncates. D) `BigDecimal` (or integer cents, or a dedicated `Money` type).
   </details>
2. (10m) **REPRODUCE INC-04-01.** Run `CartIncidentTest`: `./mvnw -q -pl core test -Dgroups=w04 -Dtest=CartIncidentTest -Dsurefire.failIfNoSpecifiedTests=false`. Expected: 3 failures (`sameProductLoadedTwice`, `containsFindsEqualProduct`, `removeWorksForEqualProduct`) and 3 passes (guards). Notice that the guard `equalsIsSkuBased` is green: `equals` itself is correct.
3. (15m) **INVESTIGATE.** In IntelliJ debug `containsFindsEqualProduct`. Step into `HashSet.contains` (or just inspect): evaluate `listingA.hashCode()` and `listingB.hashCode()`: they differ. In a scratch `main` print them for two same-SKU listings. Hint ladder: `hints/w04/INC-04-01.md`. Ask yourself: if the hash codes differ, will the set ever call `equals`?
4. (15m) **STUDY** (targeted). *Effective Java* Item 10 "Obey the general contract when overriding equals" (the list of properties) and Item 11 "Always override hashCode when you override equals" (the contract box). Answer: (a) the three rules of the `hashCode` contract; (b) how `HashMap` uses hash code then equals to find a key; (c) why generated `hashCode` should use exactly the fields used by `equals`.
5. (10m) **FIX and PROVE INC-04-01.** Add `hashCode` to `ProductListing`. Run `CartIncidentTest`: all six green. Then do the **follow-up**: run `MoneyInSetTest`. Your own `Money` from S2 has the same hole. Add `hashCode` to `Money` (same fields as `equals`), and make `MoneyInSetTest` green.
6. (10m) **REPRODUCE INC-04-02.** Run `InvoiceIncidentTest`. Expected: `tenTimesTenCents` and four `singleLine` cases red; the four `roundPricesAreFine` guards green. Read one message, for example `expected: 29 but was: 28`.
7. (15m) **INVESTIGATE.** In a scratch `main` print: `0.29 * 100`, `0.1 + 0.1 + 0.1`, `(long) (0.29 * 100)`, `new BigDecimal("0.29").multiply(new BigDecimal("100"))`, and `new BigDecimal(0.29)` (look at all those digits). Find the `double` in `Invoice`. Hint ladder: `hints/w04/INC-04-02.md`.
8. (10m) **STUDY.** *Effective Java* Item 60 "Avoid float and double if exact answers are required" and the Javadoc warning on the `BigDecimal(double)` constructor. Answer: (a) why can `0.1` not be represented exactly in binary; (b) why does casting to `long` make a tiny error into a whole cent; (c) when are integer cents an acceptable alternative.
9. (10m) **FIX and PROVE INC-04-02.** Keep the running total as a `BigDecimal` and never convert to `double`. `totalCents` should move the decimal point and round with an explicit `RoundingMode`. Run `InvoiceIncidentTest`: all nine green.
10. (15m) **GENERALIZE.** Write ledger entries for RL-04 and RL-03. Under *Prevent* write concrete rules: "when I override `equals`, I override `hashCode` in the same commit and add a test that puts the object in a `HashSet`", "money is never `double`". Record yourself explaining each in under a minute.

**Tips and common problems**
- After adding `hashCode` the test still fails → your `hashCode` uses a field that `equals` doesn't (for example the title) → both must use the same fields (here the SKU only).
- IntelliJ's generator offers many options → *Generate → equals() and hashCode()*, choose "java.util.Objects" and select only the fields that define identity.
- `hashCode` returns the same value for every instance (`return 1;`) → correct but slow; fine for the exercise, never in production.
- You "fixed" `Invoice` by `Math.round(total * 100)` → hides some cases, not all; the test with ten lines and the exact `total()` comparison still catches the design problem.
- `ArithmeticException: Rounding necessary` from `longValueExact()` → the `BigDecimal` still has fractions of a cent → call `setScale(0, RoundingMode...)` first.

**Done when**
- [ ] `CartIncidentTest`, `MoneyInSetTest` and `InvoiceIncidentTest` are fully green.
- [ ] Two ledger entries exist, each explainable in under a minute.
- [ ] `Money` has `equals`, `hashCode`, `toString` that agree with each other.

---

## S4 Consolidate (2h)
1. (25m) **Flashcards.** 10 cards: RL-04, RL-03, the `hashCode` contract, "when do I override `equals`", immutable-class rules, defensive copy (in and out), static factory benefits, record vs class, `BigDecimal(String)` vs `BigDecimal(double)`, `HALF_EVEN`.
2. (25m) **Self-check.** Answer aloud, then open the answers.
   1) Why did the cart show the same product twice, even though `equals` was right? 2) What do records generate and what must you still write? 3) How do you make a class holding a `List` safe against outside changes? 4) Why store `Money` with a fixed scale? 5) Why is `Money.equals` using `BigDecimal.equals` safe only after normalising the scale?
   <details><summary>Answers</summary>

   1) `HashSet` uses `hashCode` first; the default hash code is per object, so equal products landed in different buckets and `equals` was never asked. 2) Constructor, accessors, `equals`, `hashCode`, `toString`; you write validation (compact constructor) and extra methods. 3) Copy on the way in (`List.copyOf`) and return an unmodifiable view or copy on the way out. 4) So that equal values have the same representation (`1.5` and `1.50`), which makes `equals`/`hashCode` and comparisons predictable. 5) `BigDecimal.equals` also compares scale, so `1.5` and `1.50` are different unless both are normalised to scale 2.
   </details>
3. (30m) **Write a decision note** `docs/decisions/money.md` (ADR style): *Context* (invoice off by a cent), *Decision* (`Money` value object with `BigDecimal`, scale 2, `HALF_EVEN`, currency code), *Alternatives* (integer cents, `double` — rejected because...), *Consequences* (every API that carries money uses `Money`, never `double`).
4. (20m) **Refactor and full run.** Review all week 4 code for naming and duplicate validation. Run both weeks together: `./mvnw -q -pl core test -Dgroups=w03,w04`. Everything must be green. Commit and merge a PR "Week 4 wrap-up".
5. (10m) **Retro** in `retro.md`: what worked, what blocked me (and for how long), what I change. Which of the two incidents did you find faster, and what clue led you there?
6. (10m) **Preview week 5.** Read `OrderStatus` ideas in `shopflow-backlog.md` (SF-14). Next week: inheritance, interfaces, enums and sealed types.

**Output:** green `w04` suite, SF-02 and SF-03 PRs merged, two ledger entries, `docs/decisions/money.md`, 10 flashcards.

**If stuck:** for the class-design exercises, reread the test names as requirements; for the incidents, use the hint ladders one level at a time. If you are stuck in a debugger, print the values in a scratch `main` instead and compare.
