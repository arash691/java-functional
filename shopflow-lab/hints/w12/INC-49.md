# INC-49 hints: LazyInitializationException

<details><summary>Level 1</summary>

Loading the order works. The error comes a moment later, when the code touches the items.
</details>

<details><summary>Level 2</summary>

Look at the stack trace: which line triggers it, and is that code running inside a transaction/session?
</details>

<details><summary>Level 3</summary>

`orders.findById(id)` runs in its own short transaction (inside the repository). When it returns, the session is closed, and `order.getItems()` is only a placeholder.
</details>

<details><summary>Level 4</summary>

Search: "LazyInitializationException no session Spring `@Transactional` service layer". Also why "just make it EAGER" is the wrong answer.
</details>

<details><summary>Level 5</summary>

Make the whole operation (load + map to a view) one transaction: `@Transactional(readOnly = true)` on `describe`.
</details>
