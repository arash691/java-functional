# INC-33 hints: customers see each other's cart totals

Open ONE level at a time, only after 10 minutes of real trying.

<details><summary>Level 1: the symptom, restated</summary>

Alice's total contains Bob's items. It is wrong even when the two requests do not run at the same time, so this is not mainly a "threads" problem.
</details>

<details><summary>Level 2: where to look</summary>

Open `CartService`. Ask: how many `CartService` objects exist while the application runs? Then ask: which fields does it have, and who can see them?
</details>

<details><summary>Level 3: which tool</summary>

Put a breakpoint (or a `System.out.println(System.identityHashCode(this))`) in `CartService.addItem`. Call the endpoint for two different customers. Compare the identity of `this` for the two calls.
</details>

<details><summary>Level 4: the concept to search for</summary>

"Spring bean scopes singleton" and "stateless service". Search the Spring reference for "The Singleton Scope".
</details>

<details><summary>Level 5: the shape of the fix</summary>

The bean may keep collaborators (like `PriceList`) in fields, but it must not keep per-customer data in a single shared field. Keep the lines per customer, for example in a `Map<String, List<CartLine>>` keyed by `customerId`, and read the right entry in `total`. Use a thread-safe map because the bean is shared by all request threads. (In a real system this state lives in a database. Week 12 does exactly that.)
</details>
