# Code review challenge: hint ladder

There are eight problems in `shopflow.w09.review` (production code only). Open ONE level at a time.

<details><summary>Level 1: how to start</summary>

Do the read-only pass first (S2 step 2). Look at every class for: money arithmetic, time, empty `catch`, `equals` without `hashCode`, collection modification, boxed numbers, mutable data used as a hash key, data returned from a getter.
</details>

<details><summary>Level 2: which classes</summary>

`Checkout` has three problems, `Cart` has three, `Sku` has one and `Customer` has one.
</details>

<details><summary>Level 3: which weeks they come from</summary>

Money → week 4. Time → week 8. Exceptions → week 7. `hashCode` → week 4. Mutable key → week 5. Collections → week 6 (two of them) and week 3 (boxed `Integer`).
</details>

<details><summary>Level 4: use the failing test names</summary>

Each `ISSUE:` test name describes the required behaviour in one sentence. Pair every red test with the line of production code that breaks that sentence.
</details>

<details><summary>Level 5: fix outlines</summary>

1. Sum with `BigDecimal`, not `double`.
2. Use `clock.instant()`.
3. Catch `IOException | RuntimeException` and throw `PriceListException` with the cause.
4. Add `hashCode` consistent with `equals`.
5. Base `Customer` equality on the immutable `id` only.
6. Return `List.copyOf(lines)`.
7. `lines.removeIf(...)`.
8. Compare boxed `Integer`s with `equals`, or use `int`.
</details>
