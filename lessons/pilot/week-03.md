# Week 3: Java syntax and types for a TypeScript developer

**Mode:** A → B (build-along, then two incidents)  **Release:** v0.1 (CLI inventory engine)
**Outcome:** you can read and write basic Java (types, methods, loops, `switch`, strings), you wrote and passed your first tests, and you found and fixed two classic numeric bugs (`Integer ==`, integer overflow).

**Before you start**
- Weeks 1–2 are finished: `java -version`/`javac -version` work, `scripts/run-week.sh w00` is green.
- Run `scripts/run-week.sh w03` once. A wall of red is expected. Pick the week's goal: turn it green, one test class at a time.
- To run a single test class: `./mvnw -q -pl core test -Dgroups=w03 -Dtest=PricingTest -Dsurefire.failIfNoSpecifiedTests=false`

**Repo:** `shopflow-lab`, module `core`, package `shopflow.w03`, tag `@Tag("w03")`
Run: `scripts/run-week.sh w03`   Hints: `hints/w03/`   Solutions (peek only after your tests are green): `solutions/w03/`

---

## S1 Try it (3h): Java basics by running and changing code (mode A)
You already know variables, functions, loops and types from TypeScript. Only the *differences* are new; they are where mistakes happen.

| TypeScript | Java |
|---|---|
| `const n: number = 5` (one number type) | `int n = 5;` (also `long`, `double`, ... each with fixed size) |
| `let s: string = "a"` | `String s = "a";` (capital S, an object, immutable) |
| `x === y` for values | `==` compares primitives by value but objects by **identity**; use `.equals()` for values |
| `null` and `undefined` | `null` only, and primitives (`int`) can never be null |
| Types erased at runtime | Types checked at compile time, **must** be declared (`var` allows inference for locals) |
| Functions anywhere | Methods always live in a class; `static` methods belong to the class |
| `switch` falls through | `switch` expressions with `->` produce a value and don't fall through |

1. (15m) **Orientation.** Open `core/src/main/java/shopflow/w03/` in IntelliJ. List the files. Open `WorkedExample.java`. Notice: one class per file, the file name equals the class name, `package` on the first line, `main` is the entry point.
2. (20m) **Worked example: predict, then run.** Without running, write down what `WorkedExample.main` prints (in a comment or your notes). Then run: `java core/src/main/java/shopflow/w03/WorkedExample.java`. Compare. Run its tests: `./mvnw -q -pl core test -Dgroups=w03 -Dtest=WorkedExampleTest -Dsurefire.failIfNoSpecifiedTests=false` (they are already green).
3. (15m) **Change one thing at a time.** In `WorkedExample.java`: (a) change `stock` to 0, 4 and 5 and re-run; (b) add a branch that returns `"NEGATIVE"` for `stock < 0`; (c) change `int stock = 3;` to `int stock = "three";` and read the compiler error; undo it. Write down the exact message and how it compares with the TS error for the same mistake.
4. (30m) **Read just enough.** dev.java *Learn* → "Language Basics" (or the Java Tutorials trail "Learning the Java Language", the chapters *Variables*, *Operators*, *Control Flow Statements*). Targeted questions to answer in your notes: (a) the eight primitive types and their sizes; (b) what is integer division (`7 / 2`) and what is `7 % 2`; (c) what `var` does; (d) how a `switch` expression differs from a `switch` statement; (e) why `String` has `.equals` and what `==` would compare.
5. (30m) **EX-03-01 Pricing** (`Pricing.java`). Work test by test using `PricingTest`: first `lineTotalCents`, then `applyDiscountPercent` (read the test that explains integer division), then the two `format` overloads. Each time you finish one, run the single test class and watch another group turn green.
6. (30m) **EX-03-02 Sku** (`Sku.java`, the hardest one). Implement `isValid` with a loop and `charAt`, **without regular expressions**. Check `null` and the length first. Then `prefix` and `number`. Hint ladder: `hints/w03/EX-03-02.md` (open one level at a time after 10 minutes).
7. (20m) **EX-03-03 Shipping** (`Shipping.java`). Use a `switch` **expression** with `->` for the zone, and an `if`/`else` chain (or nested ternary) for the weight tiers. The unknown zone must throw `IllegalArgumentException` with the zone in the message.
8. (10m) **EX-03-04 Pass-by-value quiz** (`PassByValueQuiz.java`). Read `Scenarios.java` **without running it** and fill the four numbers in `predictions()`. Run `PassByValueQuizTest`. If it is red, read the message, find which of your four beliefs was wrong, and write down why.
9. (10m) **EX-03-05 Your first test** (`YourFirstTest.java`). Replace the `fail(...)` line with a real assertion using AssertJ (`assertThat(Greeter.shout("sara")).isEqualTo("SARA!");`) and add a second test of your own (for example an empty name, or a name with spaces).

**Tips and common problems**
- `cannot find symbol` → typo, missing `import`, or the file/class names don't match → read the line number the compiler gives.
- `incompatible types: possible lossy conversion from double to int` → Java won't narrow silently → decide on the type or cast explicitly, and think about what you lose.
- `StringIndexOutOfBoundsException` in `Sku` → you called `charAt(i)` on a shorter string → check the length first.
- Your `Pricing.format(5)` gives `0.5` → you forgot to pad the cents with a zero.
- `String s1 == s2` is sometimes true → string literals are interned; never compare strings with `==`.
- A red test says `UnsupportedOperationException: TODO EX-03-0x` → that method is still the stub; replace the `throw`.
- IntelliJ shows red squiggles while Maven compiles fine (or vice versa) → reimport: *Maven* tool window → *Reload All Maven Projects*.

**Done when**
- [ ] `PricingTest`, `SkuTest`, `ShippingTest`, `PassByValueQuizTest`, `YourFirstTest` are all green.
- [ ] You wrote a test of your own that fails first (change the expected value, see the message, change back) and then passes.
- [ ] Your notes contain the TS-vs-Java differences that surprised you.

---

## S2 Build (3h): SF-01 inventory core, part 1
Ticket **SF-01**: *As a warehouse operator I can add and remove stock for a SKU, and ask how much I have.* The acceptance tests are in `InventoryTest.java` and are red.

1. (15m) **Read the tests first.** Open `InventoryTest.java` from top to bottom. In your notes list every behaviour in plain words (for example: "unknown SKU has stock 0", "removing more than available throws IllegalStateException that names the SKU, and doesn't change anything").
2. (20m) **HashMap in 5 lines.** In a scratch class with a `main` (not committed) try: `Map<String,Integer> m = new HashMap<>(); m.put("a", 1); m.getOrDefault("b", 0); m.merge("a", 5, Integer::sum); m.remove("a"); m.size();` Print the results. Predict each one first. Note that the key and value types are *objects* (`Integer`, not `int`): Java boxes them automatically.
3. (15m) **First greens.** Implement `stockOf` and `distinctSkus` with a `Map<String,Integer>` field. Run `InventoryTest`: the `Empty` group should be green.
4. (25m) **add.** Implement `add` with validation (`IllegalArgumentException` for null/blank SKU and quantity <= 0). Use `merge` to accumulate. Greens: the `Add` group.
5. (30m) **remove.** Implement `remove`: validate arguments; compute the current stock; if you ask for more than available throw `IllegalStateException` with a message that contains the SKU **before** changing anything; remove the map entry when stock reaches zero so `distinctSkus` stays correct.
6. (20m) **Refactor on green.** Extract private helpers `requireSku` and `requirePositive`. Rename anything unclear. Run the whole week (`scripts/run-week.sh w03`): Inventory must be fully green.
7. (20m) **Two tests of your own.** Add to a new class `InventoryExtraTest` (tag `w03`): for example "add after remove to zero starts from zero again", "two inventories do not share stock". Make each fail once on purpose (wrong expected value) to confirm the test can fail.
8. (15m) **Branch, commit, PR.** `git switch -c sf-01-inventory`; commit in small steps (one per green group); push; open a PR with *What / Why / How I tested it*; read your diff as a reviewer; merge.
9. (20m) **Compare, then learn.** Only now open `solutions/w03/core/src/main/java/shopflow/w03/Inventory.java`. Write down three differences from your version and decide for each: better, worse, or just different.

**Tips and common problems**
- `NullPointerException` on `stock.get(sku)` assigned to an `int` → `get` returns `null` for missing keys and unboxing fails → use `getOrDefault(sku, 0)`.
- `distinctSkus()` counts SKUs with stock 0 → remove the entry at zero, or count only values > 0.
- The failed-remove test complains that stock changed → you subtracted before checking; check first.
- You compare `Integer` values with `==` → works for small numbers and breaks later; use `.equals` or unbox (this is next session's incident).
- Tests pass in isolation but `InventoryExtraTest` fails when run together → shared state: create a new `Inventory` per test (`@BeforeEach`).

**Done when**
- [ ] All of `InventoryTest` is green.
- [ ] You have at least two extra tests that you saw fail first.
- [ ] A merged PR for SF-01 part 1 exists, with small commits.

---

## S3 Incident (2h): two numeric incidents (mode B)
**INC-03-01 ticket (support):** *"The nightly duplicate-cart job flags carts as different even though the customer has the same number of items in each. It only happens for big orders. Small carts are fine."* (Reality Lab RL-01)
**INC-03-02 ticket (finance):** *"Order #88213 shows a total of -1,794,967.296 euros. The customer ordered 10,000 units at 2,500.00."* (Reality Lab RL-02)

1. (10m) **PREDICT** (write answers before running anything):
   - A) `Integer a = 127, b = 127;` what is `a == b`? What if both are 128? B) What is `Integer.MAX_VALUE`? What is `Integer.MAX_VALUE + 1`? C) What is `250_000 * 10_000` when both are `int`? D) What would you rather have happen when a total does not fit: a wrong number or an exception?
   <details><summary>Answers</summary>

   A) `true` for 127; `false` for 128, because small boxed values come from a cache while larger ones are new objects, and `==` compares identity. B) 2147483647; adding 1 wraps around to -2147483648. C) The mathematical result is 2,500,000,000, which does not fit, so it wraps to -1,794,967,296 (matching the ticket). D) An exception: a loud failure is better than a silently wrong total.
   </details>
2. (10m) **REPRODUCE INC-03-01.** Run `./mvnw -q -pl core test -Dgroups=w03 -Dtest=LineCheckerIncidentTest -Dsurefire.failIfNoSpecifiedTests=false`. Expected: 2 failures (`quantity128`, `quantity1000`) and 3 passing tests (the guards). Read the failure message: `two lines with quantity 128 Expecting value to be true but was false`.
3. (15m) **INVESTIGATE.** Open `LineChecker.sameQuantity` and `CartLine`. What type is `quantity`? In a scratch `main` print `Integer.valueOf(127) == Integer.valueOf(127)` and the same for 128. In IntelliJ put a breakpoint in `sameQuantity`, debug the failing test, and compare the object ids of `a.quantity()` and `b.quantity()`. Hint ladder: `hints/w03/INC-03-01.md` (one level at a time).
4. (10m) **STUDY** (targeted). *Effective Java* Item 61 "Prefer primitive types to boxed primitives", and the Javadoc of `Integer.valueOf(int)` (find the sentence about the cached range). Answer: (a) which range is cached; (b) three reasons to avoid boxed types in fields; (c) why does `CartLine` use `Integer` and is that reason good enough?
5. (10m) **FIX and PROVE INC-03-01.** Compare the values, not the references. Decide how you want to treat a `null` quantity and write that decision as a comment. Run the class again: all five tests must be green, including the guards.
6. (10m) **REPRODUCE INC-03-02.** Run `BulkOrderIncidentTest` the same way. Expected: 2 failures (`multiplicationOverflowIsDetected`, `sumOverflowIsDetected`), 2 green guards. Note the message `Expecting code to raise a throwable`.
7. (15m) **INVESTIGATE.** In a scratch `main` print `Integer.MAX_VALUE`, `Integer.MAX_VALUE + 1`, `250_000 * 10_000`, and `250_000L * 10_000`. Which result matches the ticket? Hint ladder: `hints/w03/INC-03-02.md`.
8. (10m) **STUDY.** The Javadoc of `Math.multiplyExact(int,int)` and `Math.addExact(int,int)`; and the Java Tutorial page "Primitive Data Types" (ranges). Answer: (a) why does `int` wrap instead of failing? (b) what do the `*Exact` methods do instead? (c) when would `long` be the better design than an exception?
9. (10m) **FIX and PROVE INC-03-02.** Use the exact methods. Run the class: all four green. Then think (and write two lines): would you prefer to switch the API to `long`? What breaks if a later total can exceed `long` too?
10. (20m) **GENERALIZE.** Write ledger entries for RL-01 and RL-02 (Symptom, Why, Reproduce, Observe with, Fix, Prevent, 1-minute answer). Under *Prevent* add a concrete guard: for RL-01 "use `int` for fields unless null is meaningful, compare boxed values with `equals`"; for RL-02 "use `*Exact` or `BigDecimal` for money-like arithmetic". Record yourself saying each answer in under a minute.

**Tips and common problems**
- The 127 test passes and 128 fails and you think the test is flaky → it isn't; the cache boundary is the whole point.
- IntelliJ shows `==` between `Integer`s with a warning → read it, it is exactly this bug.
- You changed `Integer` to `int` in `CartLine`, tests still red → the record is built from `int` literals anyway; the comparison inside `LineChecker` is what must change (but changing the record type is a valid fix if you decide `null` should not exist).
- `Math.multiplyExact` throws for `Integer.MAX_VALUE * 1`? → it should not; if it does you passed the wrong arguments.
- You "fixed" overflow by casting to `long` and the test still expects an exception → read the test: the requirement is to fail loudly.

**Done when**
- [ ] `LineCheckerIncidentTest` and `BulkOrderIncidentTest` are fully green.
- [ ] Two ledger entries exist and you can explain each in under a minute without reading.
- [ ] You can state the cached range of `Integer` and the value of `Integer.MAX_VALUE`.

---

## S4 Consolidate (2h)
1. (25m) **Flashcards.** 10 cards: RL-01, RL-02, `==` vs `equals`, int division, `%`, what `var` is, pass-by-value in one sentence, `switch` expression vs statement, `String` immutability, what `final` means on a variable.
2. (25m) **Self-check.** Answer aloud, then open the answers.
   1) Why does `new Integer(5) == new Integer(5)` (or two large boxed values) give `false`? 2) What does `7 / 2` give and what does `7 / 2.0` give? 3) A method receives an `int[]` and assigns `a = new int[]{9}`: does the caller's array change? 4) Why is `Math.multiplyExact` better than `*` for totals? 5) What is the difference between `IllegalArgumentException` and `IllegalStateException`?
   <details><summary>Answers</summary>

   1) `==` on objects compares identity and each boxed value is a separate object (outside the small cache). 2) `3` (integer division) and `3.5`. 3) No: the parameter is a copy of the reference; assigning to it only changes the local copy. 4) It throws instead of wrapping around. 5) Illegal argument: the caller passed something bad; illegal state: the object is not in a state where the call makes sense (for example removing more stock than exists).
   </details>
3. (30m) **Write a one-page "Java for TS developers" cheat sheet** in `docs/java-for-ts-devs.md`: the table from S1 extended with everything you learned (primitive sizes, integer division, `==`/`equals`, `String` immutability, no truthy/falsy conditions, `switch` expression syntax, exceptions you used).
4. (20m) **Refactor pass.** Re-read your code for naming and duplication; make helper methods `private`; make constants `static final`; run `scripts/run-week.sh w03` and confirm everything is green. Commit.
5. (10m) **Retro** in `retro.md`: what worked, what blocked me (and for how long), what I change. Which exercise was hardest and why?
6. (10m) **Preview week 4.** Open `core/src/main/java/shopflow/w04/Rectangle.java` and `RectangleTest.java`. Predict what `scaledBy(2)` does to the original object. Next week you design classes.

**Output:** green `w03` suite, SF-01 PR merged, two ledger entries, 10 flashcards, `docs/java-for-ts-devs.md`.

**If stuck:** read the failing test name and message aloud, then open the matching hint ladder one level at a time. If a compiler message is cryptic, copy its first line into a search with the word "Java".
