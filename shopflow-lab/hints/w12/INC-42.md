# INC-42 hints: 500 when returning an order

<details><summary>Level 1</summary>

The service works (INC-49 fixed). The web endpoint still returns 500.
</details>

<details><summary>Level 2</summary>

Read the log line `Resolved [HttpMessageNotWritableException: Could not write JSON: ...]`. What does the controller return?
</details>

<details><summary>Level 3</summary>

The controller returns the JPA entity. Jackson walks its getters, including lazy collections (and later the back-reference from item to order).
</details>

<details><summary>Level 4</summary>

Search: "don't expose JPA entities in REST API DTO". It is a rule of this project (ADR-005).
</details>

<details><summary>Level 5</summary>

Return the `OrderView` from `OrderQueryService.describe(id)` instead of the entity, and drop the repository from the controller.
</details>
