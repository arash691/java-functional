# Week 6: Collections and generics

**Mode:** A → B (build-along first, then two incidents)  **Release:** v0.1 CLI
**Outcome:** you pick the right collection for a job and can justify it with Big-O *and* a measurement, you write simple generic methods, and you have fixed a `ConcurrentModificationException` and a "performance optimisation" that made things slower.

**Before you start**
- Week 5 done. Terminal in the root of `shopflow-lab`.
- Read the rule again: red tests are the starting point. About 8 of the 44 tests this week are already green (worked example, guards).
- JavaScript comparison you will use all week: `Array` ≈ `ArrayList`, `Set` ≈ `LinkedHashSet` (insertion order!), `Map` ≈ `LinkedHashMap`, but Java's `HashMap`/`HashSet` do **not** keep insertion order, and Java collections are typed.

**Repo:** `shopflow-lab`, module `core`, package `shopflow.w06`, tag `@Tag("w06")`
Run all: `scripts/run-week.sh w06`   One class: `./mvnw -q -pl core test -Dtest=Collections101Test -Dsurefire.failIfNoSpecifiedTests=false`
Hints: `hints/w06/` (EX-06-04, INC-06-1, INC-06-2)   Solutions: `solutions/w06/` (after your tests are green)

---

## S1 Try it (3h): the collections family, comparators, generics, and measuring Big-O (mode A)

1. (15m) **Run the worked example.** Predict the five printed lines of `CollectionsTour` first (what order will the set print? what will `pq.poll()` return twice? what does a `Deque` print after `addFirst("x"); addFirst("y"); addLast("z")`?). Then:
   - `./mvnw -q -pl core compile`
   - `java -cp core/target/classes shopflow.w06.example.CollectionsTour`
   Compare. Write down every surprise.
2. (25m) **Build the map of the family.** Read the Java Tutorials "Collections" trail, the "Interfaces" part (Collection, List, Set, Queue, Deque, Map), and skim "Implementations". Fill this table in your notes from memory after reading (then check the Javadoc): for `ArrayList`, `LinkedList`, `HashSet`, `LinkedHashSet`, `TreeSet`, `HashMap`, `LinkedHashMap`, `TreeMap`, `ArrayDeque`, `PriorityQueue`: *keeps order? allows duplicates? Big-O of add / get / contains / remove*.
3. (20m) **EX-06-01 (List, Set, Map).** Open `Collections101.java`. Implement `uniquePreserveOrder` (`LinkedHashSet`), `countBySku` (`Map.merge`), `groupByCategory` (`LinkedHashMap` + `computeIfAbsent`). Run `-Dtest=Collections101Test`.
4. (20m) **EX-06-02 (Deque).** Implement `RecentlyViewed`: a bounded "most recent first" list without duplicates. Use an `ArrayDeque`: `remove(sku)`, `addFirst(sku)`, and `removeLast()` when over capacity. `items()` must return a *copy*. Run `-Dtest=RecentlyViewedTest`.
5. (25m) **EX-06-03 (Comparable vs Comparator).** Read **EJ Item 14** (consider implementing `Comparable`) and the Javadoc of `Comparator.comparing` and `thenComparing`. Implement `Product.compareTo` (by SKU) and the three comparators in `ProductSorts`. Notes: compare names with `String.CASE_INSENSITIVE_ORDER`; never subtract ints to compare (overflow); `Comparator.comparingLong` avoids boxing. Run `-Dtest=ProductSortsTest`.
6. (35m) **EX-06-04 (generics).** First read **EJ Items 26, 28 and 31** (don't use raw types; prefer lists to arrays; use bounded wildcards: PECS = Producer Extends, Consumer Super) and the Java Tutorials "Generics" (bounded type parameters, wildcards). Then implement the three methods in `Generics101`. Order: `keyOfMaxValue`, `maxOf`, `copyAll`. The signatures are given. If a test does not compile, the compiler message names the type it could not infer. Run `-Dtest=Generics101Test`. Hints: `hints/w06/EX-06-04.md`.
7. (30m) **EX-06-05 (measure Big-O).** Predict, then measure:
   - Run `java -cp core/target/classes shopflow.w06.ComplexityLab`. Which of the four lines is slowest and by how much (typical result: `get(i)` on `LinkedList` over 50,000 items takes about a second, `HashSet.contains` takes fractions of a millisecond)? Write the ratio.
   - In `Lookup.java` implement `fastContains` so that it gives the same answers as `naiveContains` but at least 10x faster: build a `HashSet` once, then query it. Run `-Dtest=LookupTest`.
   - Write in your notes: why is `ArrayList.contains` O(n) and `HashSet.contains` O(1) on average?
8. (10m) **Check and commit.** `scripts/run-week.sh w06`: S1 classes green (`WorkedExampleTest`, `Collections101Test`, `RecentlyViewedTest`, `ProductSortsTest`, `Generics101Test`, `LookupTest`). `InventoryTest`, `PromotionCleanupTest`, `ReportsTest` stay red. `git add -A && git commit -m "w06 S1: collections, comparators, generics"`.

**Tips and common problems**
- Output order of a `HashSet`/`HashMap` differs from what you expect → they are unordered by design → use `LinkedHash*` when order matters, `Tree*` when sorted order matters.
- `incompatible types: List<Integer> cannot be converted to List<Number>` → generics are invariant → that is why `? extends` / `? super` exist (PECS).
- `LookupTest` says "fast must be at least 10x quicker" → you still scan the list for each query → build the `HashSet` once, outside the loop.
- `LookupTest` is flaky on a very busy machine → close other heavy programs and rerun; the margin is large (about 40x with a correct solution).
- `Collections.sort` complains "no suitable method" → `Product` must implement `Comparable<Product>` (it does), and `compareTo` must be consistent with your comparators' expectations.

**Done when**
- [ ] The six S1 test classes are green.
- [ ] Your table of collections (order, duplicates, Big-O) is in your notes.
- [ ] You measured at least two Big-O differences yourself and wrote the numbers down.
- [ ] Work committed.

---

## S2 Build (3h): SF-01, the inventory (mode A, tests first)

**Ticket SF-01 (extension):** *Inventory keeps stock by SKU, lists products that are running low, and offers sorted views of the catalogue. Callers must not be able to change the inventory behind our back.* Acceptance tests: `InventoryTest.java`.

1. (15m) **Read the tests first.** Open `InventoryTest.java` and list the rules in your own words. Check: what is the exact message when removing too much? is the low-stock threshold inclusive? how are ties ordered? what does `stockOf` return for an unknown SKU?
2. (10m) **Run and look.** `-Dtest='InventoryTest*'`. Expect 12 tests, all with `UnsupportedOperationException: TODO SF-01`.
3. (30m) **Data and registration.** Add two fields to `Inventory`: a `Map<String, Product>` (use `LinkedHashMap`) and a `Map<String, Integer>` for stock. Implement `register` (reject duplicates and negative stock) and `stockOf`. Run the `Stock` tests that now pass.
4. (30m) **Add and remove.** Implement `add` and `remove` with two small private helpers (`requireKnown(sku)`, `requirePositive(qty)`). On insufficient stock throw `IllegalStateException("Insufficient stock for T-1: have 2, need 3")` *without* changing the stock. Run the `Stock` nested tests.
5. (30m) **Low stock with a PriorityQueue.** In `lowStock(threshold)`: put the matching `StockEntry` values into a `PriorityQueue` with `Comparator.comparingInt(StockEntry::quantity).thenComparing(StockEntry::sku)`, then poll them into a list. Run the `LowStock` nested tests. (Why a queue and not `sort`? For a few entries it doesn't matter; a queue shines when you only need the *k* lowest. Write one sentence on that.)
6. (25m) **Sorted views and a read-only set.** `productsSortedByPrice()` should use your `ProductSorts.byPrice()` plus a tie-breaker by SKU; `productsSortedByNameThenPrice()` uses `ProductSorts.byNameThenPrice()`. Both return a *new* list. `skus()` returns `Collections.unmodifiableSet(...)` of the key set. Run all of `InventoryTest`.
7. (20m) **Defensive thinking.** In a scratch test, check what happens when a caller modifies the list returned from `productsSortedByPrice()` (must not affect the inventory). Then check what an `unmodifiableSet` view does when the inventory registers a new product *after* you got the view (it reflects it: a view, not a copy). Decide if that is acceptable and write the decision in `~/shopflow-notes/inventory-notes.md`.
8. (20m) **Ship it.** Run everything for `w06` that you have completed. Commit with SF-01 in the message and write a three-line PR description in `~/shopflow-notes/pr-sf01.md`.

**Tips and common problems**
- `NullPointerException` in `remove` → `stock.get(sku)` returns `null` for an unknown SKU and auto-unboxing fails → check `requireKnown` first.
- `lowStock` order wrong when quantities tie → add `.thenComparing(StockEntry::sku)`.
- `skus()` test fails with the wrong exception → returning `Set.copyOf(...)` throws `UnsupportedOperationException` too, but a mutable copy would not; make sure your return is unmodifiable.
- `PriorityQueue.toString()` / iteration order looks unsorted → only `poll()` is ordered, not iteration → always drain with `poll()`.

**Done when**
- [ ] All 12 `InventoryTest` tests are green.
- [ ] The inventory never exposes its internal collections for modification.
- [ ] Commit and PR description exist.

---

## S3 Incident (2h): the cleanup that crashes and the optimisation that hurt (mode B)

### Incident 1 (60 min): INC-06-1, "The nightly promotion cleanup job crashes" (RL-08)

> **On-call alert 02:14.** *"`PromotionCleanup` failed with `ConcurrentModificationException`. It ran fine all last week. Also, yesterday finance noticed an expired promotion code was still being accepted even though the job reported success."*

1. (10m) **PREDICT.** Read `promo/PromotionCleanup.java` and `Promotion.java`. Don't run anything. Write answers:
   1) What does a for-each loop use under the hood to walk a list?
   2) For `[A, B(expired), C, D(expired), E]`, what happens right after `B` is removed?
   3) For `[A, B(expired), C(expired)]` (the expired items are the last two), will the loop throw? Which promotions remain?
   <details><summary>Answers</summary>

   1) An `Iterator`. 2) The list's structure changed (`modCount` changed) and the iterator's next `next()` call throws `ConcurrentModificationException`. 3) No exception: after removing `B`, the list has size 2 and the iterator's cursor is 2, so `hasNext()` is false and the loop ends before it looks at `C`. `C` is still in the list, expired, silently.
   </details>
2. (5m) **REPRODUCE.** `-Dtest=PromotionCleanupTest`. Expect `removesFromTheMiddle` and `removesAll` to fail with `ConcurrentModificationException`, and `secondToLastQuirk` to fail with an *assertion* (`[promotions left]`) rather than an exception: the dangerous kind. The two `GUARD` tests are green.
3. (15m) **INVESTIGATE.** Read the stack trace of `removesFromTheMiddle`: find the frame in `ArrayList$Itr.checkForComodification`. Debug `secondToLastQuirk`: step through the loop, watch `promotions.size()` and (via the debugger's variable tree) the iterator's `cursor` field. Hints: `hints/w06/INC-06-1.md`.
4. (15m) **STUDY.** Read the Javadoc of `ConcurrentModificationException` and `Iterator.remove`, and of `Collection.removeIf`. Answer: (a) What does "fail-fast" mean here and is it guaranteed? (b) Which three ways exist to remove elements while traversing? (c) Why is the quirk in step 1 question 3 dangerous in production?
5. (5m) **FIX.** Replace the loop body with `promotions.removeIf(...)` and compute the count from the size before and after.
6. (5m) **PROVE.** `PromotionCleanupTest` green (5 tests). Add one regression test of your own: a list where *every other* promotion is expired.
7. (5m) **GENERALIZE.** Ledger entry RL-08. One-minute answer: *"Structural changes to an `ArrayList` during iteration make the iterator throw `ConcurrentModificationException` on its next call. Removing the second-to-last element hides the problem because `hasNext()` returns false first, so an item is skipped silently. Use `removeIf` or `Iterator.remove`."*

### Incident 2 (60 min): INC-06-2, "The optimised report got slower" (extra)

> **Perf ticket.** *"We switched the order-amounts list to a `LinkedList` last sprint because 'we append a lot'. The nightly totals job used to take milliseconds; now it takes over a second at 60,000 rows and grows much faster than the data."*

1. (10m) **PREDICT.** Read `report/Reports.java`. Write answers:
   1) What does `LinkedList.get(30000)` have to do?
   2) How many "hops" does the whole `sumAll` loop make for n = 60,000, roughly (use n²/4)?
   3) If you double the rows, how does the time change?
   <details><summary>Answers</summary>

   1) Walk from the nearer end node by node (up to n/2 hops). 2) About 60,000²/4 ≈ 900 million. 3) About four times as long (quadratic).
   </details>
2. (5m) **REPRODUCE.** `-Dtest=ReportsTest`. Expect `sumIsFast` to fail with `sumAll took ~1400 ms` against a limit of 150 ms (your number will vary, but it should be seconds-level on a slow laptop). `sumIsCorrect` is green: the code is *correct*, just slow.
3. (15m) **INVESTIGATE.** Re-run `ComplexityLab` and find the two lines for `get(i)`. Then measure `sumAll` with your own `System.nanoTime` in a scratch `main` for n = 20,000, 40,000, 80,000 and write the three times. Do they grow linearly or quadratically? Hints: `hints/w06/INC-06-2.md`.
4. (15m) **STUDY.** Read the Javadoc of `LinkedList` ("Doubly-linked list implementation… operations that index into the list will traverse the list…") and **EJ Item 64** (refer to objects by their interfaces) — notice how code that only talks to `List` could switch implementations in one line. Answer: (a) Why is a linked list rarely faster than an array list in practice, even for inserts? (b) What is the one case where `LinkedList` fits? (c) Why is "we append a lot" not a reason to use it?
5. (5m) **FIX.** Return an `ArrayList` from `newAmountsList()`. Optionally also iterate with a for-each in `sumAll` so it is fast for any `List`.
6. (5m) **PROVE.** `ReportsTest` green. Run it three times to check it is stable.
7. (5m) **GENERALIZE.** Ledger entry "ArrayList vs LinkedList (extra)". One-minute answer: *"Indexed access on a `LinkedList` is O(n), so a loop using `get(i)` is O(n²). Measure before optimising; default to `ArrayList`; if you must keep a `LinkedList`, iterate with the iterator instead of indexing."*

**Tips and common problems**
- Timing test is green on your fast machine before the fix → raise `60_000` temporarily to `120_000` in a scratch copy to see the difference, but don't commit changes to the test.
- "I used `LinkedList` iterator and it is fast" → correct, that is fix option 2; both are valid; say which one you prefer and why.
- `ConcurrentModificationException` still thrown after the fix → another loop elsewhere still removes inside a for-each; search with `grep -rn "remove(" core/src/main/java/shopflow/w06/promo`.
- You can't reproduce the "second-to-last" case → you must use a list where the last **two** items are expired.

**Done when**
- [ ] `PromotionCleanupTest` (5) and `ReportsTest` (2) green; `scripts/run-week.sh w06` fully green.
- [ ] Two ledger entries written and rehearsed aloud.
- [ ] You can explain why removing the second-to-last element hides the bug.

---

## S4 Consolidate (2h)

1. (25m) **Ledger.** Complete both entries (*Symptom, Why, Reproduce, Observe with, Fix, Prevent, One-minute answer, Confidence*). "Prevent": a lint/review rule ("no remove() inside for-each"), a test, a default ("ArrayList unless proven otherwise").
2. (30m) **Cheat sheet.** One page `~/shopflow-notes/collections-cheatsheet.md`: a table with the 10 collections from S1 step 2 (ordered? duplicates? add/get/contains/remove complexity), plus a "which would I pick for…" list: unique SKUs in insertion order; lookup by SKU; top-5 lowest stock; undo history; sorted price list; FIFO queue of jobs.
3. (15m) **Flashcards.** Five cards. Suggested: (1) HashMap vs LinkedHashMap vs TreeMap. (2) What does PECS stand for? (3) Why `ArrayDeque` over `Stack`/`LinkedList` for a stack? (4) What does `removeIf` do that a for-each remove cannot? (5) Why must `compareTo` not subtract integers?
4. (20m) **Interface thinking.** Open your `Inventory` and `Reports`. Find every place a variable or return type is a concrete class (`ArrayList`, `HashMap`). Change declarations to interfaces (`List`, `Map`) where it is safe (EJ Item 64). Run `scripts/run-week.sh w06`.
5. (15m) **Self-check.** Answer aloud, then open the answers.
   1) What is the average cost of `HashSet.contains`? 2) Why does a for-each with `list.remove` fail? 3) What is `Comparator.thenComparing` for? 4) Why is `List<Integer>` not a `List<Number>`? 5) When is a `PriorityQueue` better than sorting?
   <details><summary>Answers</summary>

   1) O(1) on average (O(n) worst case with many collisions). 2) The iterator detects structural change and throws; and in one edge case it silently skips an element. 3) Tie-breaking: apply a second comparator when the first says "equal". 4) Generics are invariant: a `List<Integer>` accepts only Integers, and treating it as `List<Number>` would let you add a Double. 5) When you repeatedly need the smallest/largest element or only the top *k*, without sorting everything.
   </details>
6. (10m) **ADR note (optional but recommended).** Write ADR-002a, a three-line decision: "Return unmodifiable views/copies from public methods", and why.
7. (5m) **Retro and tag.** Retro lines; tick Week 6; `git add -A && git commit -m "w06 done" && git tag w06-done`.

**Done when**
- [ ] Cheat sheet, five flashcards and two ledger entries exist.
- [ ] Declared types in the week's code are interfaces where sensible.
- [ ] 4 of 5 self-check questions answered without looking.

**Output this week:** `Collections101`, `RecentlyViewed`, `ProductSorts`, `Generics101`, `Lookup`, `Inventory` (SF-01), fixes for `PromotionCleanup` and `Reports`, collections cheat sheet, two ledger entries, tag `w06-done`.

**If stuck:** Javadoc of the class (the first paragraph tells you the complexity and ordering); for generics, hover over the call in the IDE to see the inferred type parameters.
