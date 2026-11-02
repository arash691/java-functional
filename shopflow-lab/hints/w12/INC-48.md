# INC-48 hints: the order list is slow

<details><summary>Level 1</summary>

The database is working hard for a screen that shows 20 rows. Count the SQL statements one page view causes.
</details>

<details><summary>Level 2</summary>

Add `"logging.level.org.hibernate.SQL=DEBUG"` to the `properties` of `@SpringBootTest` in `AbstractPostgresTest`, run `Inc48NPlusOneTest`, and read the SQL. Look for one statement repeated with a different id each time.
</details>

<details><summary>Level 3</summary>

The pattern is "1 query for the list + N queries for a lazy association", the N+1 problem. The test also prints the real count (24 for 20 orders).
</details>

<details><summary>Level 4</summary>

Search: "Hibernate N+1 join fetch", "Spring Data JPA @EntityGraph". Read about the pagination caveat of fetching collections.
</details>

<details><summary>Level 5</summary>

Add to `OrderRepository` a query that loads orders together with their items and the items' products in one statement (`join fetch`), and call it from `listWithItems()`.
</details>
