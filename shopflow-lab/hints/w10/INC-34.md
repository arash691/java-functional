# INC-34 hints: the app does not start after the discount feature

<details><summary>Level 1: the symptom, restated</summary>

The context fails to start. The message ends with "Is there an unresolvable circular reference?".
</details>

<details><summary>Level 2: where to look</summary>

Read the failure from the bottom ("Caused by") upwards. It names the beans on the cycle. Then open both classes and look at their constructors.
</details>

<details><summary>Level 3: which tool</summary>

`assertThat(ctx).hasNotFailed()` prints the whole startup failure. Draw the arrows: `A needs B`, `B needs A`.
</details>

<details><summary>Level 4: the concept to search for</summary>

"Spring circular dependency constructor injection" and "break a dependency cycle". Also read what `spring.main.allow-circular-references` does, and why you should NOT turn it on.
</details>

<details><summary>Level 5: the shape of the fix</summary>

Ask why `DiscountService` needs `CartPricing`. It only asks "is this a large order?". That rule belongs in the discount logic. Move it into `DiscountService` (or a third small class both use) so the arrows only point one way: `CartPricing -> DiscountService`.
</details>
