> **Superseded for weeks 1–12 (pilot).** The project-first version of these weeks is in `pilot/week-01.md` … `pilot/week-12.md`, backed by the `shopflow-lab` repo. This file is kept for reference; the Phase 1 gate rubric and the full reference lists still apply.

# Phase 2: First Spring Boot Vertical Slice (Weeks 10–15)

Goal: a tested, containerized REST service with a React client; first contact with Spring's proxies and JPA's hidden behaviour.

Format reminder: numbered, time-boxed steps; **Done when** is the only definition of finished; use the "Stuck? The 15-minute routine" in `README.md` when a step runs over by 2x. Ledger = `reality-lab/LEDGER.md`.

> **Version note:** commands and annotation names below target Spring Boot 3.4+/4.x with Hibernate 6.x and JDK 21/25. Where a name changed between versions it is marked "(verify against current docs)".

---

## Week 10 — Spring core: IoC, DI, configuration
**Outcome:** understands what Spring does at startup and why.
**Before you start:** Phase 1 tagged; JDK, Maven wrapper (`./mvnw`), Docker and IntelliJ working; GitHub repo `shopflow` created with PR template.

### S1 Learn (3h): the container, beans and configuration
1. (20m) Read Spring Framework reference, "The IoC Container": the introduction and "Container Overview". Write 3 bullets from memory: what is a bean, what is the `ApplicationContext`, what is configuration metadata.
2. (30m) Read "Dependencies" (dependency injection: constructor vs setter). Note why the reference itself recommends constructor injection for mandatory dependencies.
3. (25m) Read "Bean Scopes" and "Customizing the Nature of a Bean" (lifecycle callbacks: `@PostConstruct`, `@PreDestroy`). Write down the default scope and one consequence of it.
4. (30m) Read Spring Boot reference "Externalized Configuration" (focus on the precedence list and `@ConfigurationProperties`) and "Profiles".
5. (30m) Do the Spring Guide "Building an Application with Spring Boot". **Type every line yourself**; run it; add one extra bean and inject it by constructor.
6. (25m) Read the Boot reference intro on auto-configuration and `@SpringBootApplication`. Open the annotation source in the IDE (Ctrl/Cmd+click) and list the three annotations it combines.
7. (20m) Close everything. On paper write: (a) what happens from `main()` to "Started ShopflowApplication", (b) the three annotations, (c) the config precedence order (top 5). Compare with the docs and fix gaps.

**Tips and common problems**
- Guide code doesn't compile → it targets an older Boot/JDK → use the project's current Boot version; only the concepts matter.
- You can't tell where a bean came from → IDE gutter icons on `@Bean`/`@Autowired`, or run with `--debug` and read the conditions report.
- Config isn't picked up → wrong file name/location or profile not active → use `application.yml` in `src/main/resources` and check `spring.profiles.active`.
- Reading stalls → switch to the "typed" step (5) earlier; don't read longer than 30 minutes without code.

**Done when**
- [ ] The guide app runs and your extra bean is injected by constructor.
- [ ] You wrote the three `@SpringBootApplication` annotations and the precedence list from memory.

### S2 Build (3h): SF-06 Bootstrap service
1. (15m) Go to start.spring.io: Project Maven, Language Java, latest stable Boot, Java 25 (or 21), Packaging Jar. Group `io.shopflow`, artifact `shopflow`. Dependencies: Spring Web, Validation, Spring Data JPA, PostgreSQL Driver, Flyway Migration, Spring Boot Actuator, Testcontainers, Docker Compose Support. Download, unzip into the repo, open in IntelliJ, create branch `sf-06-bootstrap`.
2. (25m) Create `compose.yaml` in the project root:
   ```yaml
   services:
     db:
       image: postgres:17
       environment:
         POSTGRES_DB: shopflow
         POSTGRES_USER: shopflow
         POSTGRES_PASSWORD: shopflow
       ports: ["5432"]
   ```
   Boot's Docker Compose support starts it on `spring-boot:run` and wires the datasource automatically. Run `./mvnw spring-boot:run` and watch the log for the container starting.
3. (25m) Add `src/main/resources/db/migration/V1__baseline.sql` (a comment and `SELECT 1;`). In `application.yml` set `spring.application.name: shopflow`, `management.endpoints.web.exposure.include: health,info`, `spring.jpa.hibernate.ddl-auto: validate`.
4. (30m) **Test first:** write `ShopflowPropertiesTest` asserting that `shopflow.currency` defaults to `EUR` and rejects blank. Then create `ShopflowProperties`:
   ```java
   @ConfigurationProperties(prefix = "shopflow")
   @Validated
   public record ShopflowProperties(@NotBlank String currency, @Positive int defaultPageSize) {}
   ```
   Register with `@ConfigurationPropertiesScan` on the application class. Make the test pass by providing values in `application.yml`.
5. (30m) Run the app. `curl -s localhost:8080/actuator/health` must return `{"status":"UP"}`. Stop the DB container (`docker compose stop db`) and call again: expect `DOWN`. Restart it.
6. (35m) Add the context test with Testcontainers:
   ```java
   @SpringBootTest
   @Testcontainers
   class ShopflowApplicationTests {
       @Container @ServiceConnection
       static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
       @Test void contextLoads() {}
   }
   ```
   (Testcontainers 2.x moved/renamed some classes; use the import your IDE offers and check the Testcontainers guide, verify against current docs.) Run `./mvnw test`.
7. (20m) Commit in small logical commits, open a PR with the template, review your own diff line by line, merge.

**Tips and common problems**
- `error: release version 25 not supported` → JDK on PATH differs from the pom → `sdk use java <version>` and align `java.version` in `pom.xml`.
- `Could not find a valid Docker environment` → Docker Desktop not running or socket not found → start Docker; on Linux/macOS check `docker ps` works in the same shell.
- `Failed to determine a suitable driver class` → no datasource available (Compose support off or Docker down) → start the DB or check Compose support dependency.
- `Port 5432 already in use` → local Postgres running → the `"5432"` mapping above picks a random host port; don't hard-code `5432:5432`.
- Flyway: `Found non-empty schema(s) but no schema history table` → pointing at a DB with existing tables → use a fresh container/volume.

**Done when**
- [ ] `./mvnw spring-boot:run` starts the app and DB automatically; `/actuator/health` is `UP`.
- [ ] `./mvnw test` is green including the Testcontainers context test and the properties test.
- [ ] PR merged to `main`.

### S3 Lab (2h): RL-33 singleton state, RL-34 circular dependency
1. (10m) In `reality-lab/` create a Boot module (or package) `spring-lab`. Copy the Initializr dependencies you need (Web, Actuator).
2. (15m) **RL-33, failing test first.** Controller with shared state:
   ```java
   @RestController
   class WhoAmIController {
       private String current; // BUG: singleton shared by all requests
       @GetMapping("/whoami")
       String whoami(@RequestParam String name) throws InterruptedException {
           current = name; Thread.sleep(20); return current;
       }
   }
   ```
   Test: with `MockMvc`, fire 50 requests from an `ExecutorService` with different names, collect `(name, response)` pairs, and assert each response equals its name. It should fail.
3. (15m) Run it a few times. **Observe:** some responses contain another request's name; print `Thread.currentThread().getName()` to see concurrent Tomcat/test threads sharing one controller instance.
4. (10m) Explain in 3 sentences: default scope is singleton, one instance serves all threads, so fields written per request race.
5. (15m) Fix: remove the field; use the method parameter/local variable. Keep the test as the regression test. Note why `@Scope("request")` or `ThreadLocal` are worse fixes.
6. (15m) **RL-34.** Create `@Service class A { A(B b){} }` and `@Service class B { B(A a){} }`. Start the app. **Observe** the failure analysis: "The dependencies of some of the beans in the application context form a cycle", with the cycle drawn. Note that Boot prohibits circular references by default; `spring.main.allow-circular-references=true` exists but hides the design flaw.
7. (20m) Fix by redesign: extract the shared behaviour into a third class `C` that both depend on, or replace one direction with an event. Add a test that `@SpringBootTest` context loads.
8. (20m) Draft ledger entries RL-33 and RL-34 using the template (symptom, why, reproduce, observe, fix, prevent, 1-minute answer).

**Tips and common problems**
- RL-33 test passes by luck → increase threads/iterations and the sleep; run `@RepeatedTest(10)`.
- MockMvc runs requests in the calling thread, so no real concurrency → the concurrency comes from your executor calling `mockMvc.perform` from many threads.
- Cycle error mentions field injection → you used `@Autowired` fields; constructor injection makes the cycle fail fast, which is what you want.
- Setter injection "fixes" cycles at the cost of hiding the design problem → don't.

**Done when**
- [ ] RL-33 test failed before the fix and passes after, repeated 10 times.
- [ ] You saw the cycle failure analysis and removed the cycle without `allow-circular-references`.
- [ ] Two ledger drafts exist.

### S4 Review + Write (2h)
1. (30m) Finish ledger entries RL-33, RL-34; say each aloud in under a minute and time yourself.
2. (30m) Write ADR-003 "Constructor injection only; no field injection" (context, decision, alternatives considered, consequences). Use the ADR template from adr.github.io.
3. (30m) Draw the startup sequence in Mermaid: `main` → `SpringApplication.run` → environment/profiles → create `ApplicationContext` → component scan and bean definitions → auto-configuration → instantiate singletons → runners → started. Put it in `docs/startup-sequence.md`.
4. (15m) Create 6 flashcards (scope default, three annotations, config precedence, constructor injection benefits, cycle cause, `@ConfigurationProperties` use).
5. (15m) Answer the self-check below, then write your weekly retro.

**Self-check:** Why is constructor injection preferred? What is a bean's default scope and what does it imply for state? What does `@SpringBootApplication` combine?
<details><summary>Answers</summary>

- Constructor injection makes dependencies explicit and mandatory, allows `final` fields (immutability), works without Spring in unit tests, and exposes cycles at startup.
- Default scope is singleton: one shared instance per container, so per-request mutable state in fields is a thread-safety bug.
- `@SpringBootConfiguration` (a `@Configuration`) + `@EnableAutoConfiguration` + `@ComponentScan`.
</details>

**Output:** SF-06 merged; ADR-003; RL-33/34 entries.
**If stuck:** the Spring Boot reference "Getting Started" plus `--debug` output answers most startup failures; read the first "Caused by" line.

---

## Week 11 — REST, validation and contract-first API
**Outcome:** writes an API spec first, then implements it with proper errors.
**Before you start:** SF-06 merged and app runs; open editor.swagger.io (or an OpenAPI plugin in IntelliJ); skim ADR-002 (error strategy) from Phase 1.

### S1 Learn (3h): HTTP semantics, MVC, validation, ProblemDetail, OpenAPI
1. (25m) Read MDN "HTTP request methods" and "HTTP response status codes". Afterwards build a table from memory: GET/POST/PUT/PATCH/DELETE vs safe, idempotent, typical success code.
2. (25m) Read Spring MVC reference "Annotated Controllers": `@RestController`, `@RequestMapping`, `@PathVariable`, `@RequestParam`, `@RequestBody`, `ResponseEntity`. Write a 10-line controller in a scratch project.
3. (25m) Read Spring MVC "Validation" and the Bean Validation constraints list (`@NotBlank`, `@Size`, `@Positive`, `@Pattern`). Note `@Valid` on a body vs `@Validated` on a class for method parameters.
4. (30m) Read Spring MVC "Error Responses" (`ProblemDetail`, `ResponseEntityExceptionHandler`, `@RestControllerAdvice`) and RFC 9457 (members `type`, `title`, `status`, `detail`, `instance`; extension members).
5. (35m) Read the OpenAPI Specification overview on spec.openapis.org: `openapi`, `info`, `paths`, `components/schemas`, `parameters`, `responses`. In the Swagger Editor, load the example and add one endpoint.
6. (25m) Hand-write a minimal spec for `GET /products/{id}` returning `200 Product` and `404 application/problem+json`. Fix every editor warning.
7. (15m) From memory write a status-code decision list: create → ?, validation failure → ?, missing → ?, duplicate SKU → ?, unauthorised → ?. Check against MDN.

**Tips and common problems**
- Confusing 400 and 422 → both appear in the wild; ShopFlow convention is 400 for malformed/invalid input unless ADR-004 says otherwise. Decide and write it down.
- `@Valid` seems ignored → constraints on nested objects need `@Valid` on the field; on query params need `@Validated` on the class.
- Swagger Editor shows `$ref` errors → indentation or wrong `#/components/schemas/...` path.
- Spec feels like busywork → it is the contract the React client will be generated from in Week 13.

**Done when**
- [ ] Method/status table written from memory and corrected.
- [ ] A valid `GET /products/{id}` spec with 200 and 404 problem+json exists.

### S2 Build (3h): SF-07 Catalog API, spec first
1. (30m) Create `src/main/resources/openapi/catalog.openapi.yaml` in the editor: `GET/POST /products`, `GET/PUT/DELETE /products/{id}`; schemas `Product`, `ProductRequest`, `ProblemDetail`; query params `page`, `size`; money as a string (`pattern: '^\d+(\.\d{1,4})?$'`) plus `currency`. No editor errors.
2. (15m) Add `springdoc-openapi-starter-webmvc-ui` (pick the version compatible with your Boot, per springdoc's compatibility table; verify against current docs). It is only used to compare the running API with your hand-written spec.
3. (25m) **Test first:** `ProductControllerTest` with `@WebMvcTest(ProductController.class)` and a mocked `ProductService` (`@MockitoBean` in Spring 6.2+/Boot 3.4+; `@MockBean` in older versions). Cases: valid POST → `201` + `Location`; invalid POST (blank name, malformed price) → `400` with `application/problem+json` and an `errors` member; unknown id → `404` problem+json. Run: all red.
4. (35m) Create request/response record DTOs with constraints, e.g.:
   ```java
   public record ProductRequest(
       @NotBlank String sku, @NotBlank String name,
       @NotNull @Pattern(regexp = "^\\d+(\\.\\d{1,4})?$") String price,
       @NotBlank @Size(min = 3, max = 3) String currency) {}
   ```
   Add `ProductController` with `@Valid @RequestBody`, and an in-memory `ProductService` for now (JPA arrives next week).
5. (30m) `GlobalExceptionHandler extends ResponseEntityExceptionHandler` with `@RestControllerAdvice`: map `ProductNotFoundException` → `ProblemDetail` 404; override `handleMethodArgumentNotValid` to add an `errors` property (field → message). Consider `spring.mvc.problemdetails.enabled=true` (verify against current docs).
6. (25m) Run the app and `curl -i` each case; open `/v3/api-docs` and compare paths, status codes and schemas with your hand-written YAML. List differences; fix code (not the spec) unless the spec was wrong.
7. (20m) Commit, PR, self-review, merge.

**Tips and common problems**
- `415 Unsupported Media Type` → missing `Content-Type: application/json` in curl → add `-H 'Content-Type: application/json'`.
- Unknown route returns an HTML Whitelabel page instead of problem+json → handle `NoResourceFoundException` in your advice or enable ProblemDetail support.
- `NoSuchMethodError` from springdoc → springdoc version doesn't match Boot → use the matching version from its compatibility table.
- `@MockBean`/`@MockitoBean` not found → wrong annotation for your version; check the Boot reference "Testing".
- Tests pass but curl fails → test used a mocked service; add a smoke test later with the real one (Week 14).

**Done when**
- [ ] The three `@WebMvcTest` cases pass.
- [ ] `curl` outputs for 201, 400, 404 match the YAML.
- [ ] A diff of YAML vs `/v3/api-docs` is recorded in the PR description.

### S3 Lab (2h): RL-38 property precedence, RL-39 date/time, RL-40 BigDecimal
1. (15m) **RL-38, failing test first.** In `spring-lab`: `application.yml` has `shopflow.currency: EUR`, `application-dev.yml` has `USD`. Write down your prediction for each launch below, then write a test expecting the "wrong" answer for at least one so it fails.
2. (15m) Observe: `./mvnw spring-boot:run` (→?), `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run` (→?), same with `SHOPFLOW_CURRENCY=GBP` (→?), then `--shopflow.currency=JPY`. Expose `/actuator/env/shopflow.currency` (`management.endpoints.web.exposure.include=env`, and `management.endpoint.env.show-values=ALWAYS`; verify against current docs) and read `propertySources` in precedence order. Only expose `env` locally.
3. (10m) Explain in 3 sentences: sources are layered; command line > env vars > profile files > base file; the first match wins.
4. (20m) **RL-39.** Create `record Event(LocalDateTime local, Instant instant) {}` and a test serializing it with the app's `ObjectMapper`. Run the JVM with `-Duser.timezone=UTC` then `-Duser.timezone=Asia/Tokyo` and compare outputs for `LocalDateTime.now()` and `Instant.now()`. **Observe:** the `LocalDateTime` JSON has no offset (ambiguous across zones). Check that dates are not written as numeric timestamps (`spring.jackson.serialization.write-dates-as-timestamps`, default false in Boot; the Jackson major version in Boot 4 changed some property names, verify against current docs).
5. (20m) **RL-40.** Serialize `new BigDecimal("12345678901234567.89")` as a JSON number, then run:
   `node -e 'console.log(JSON.parse("{\"p\":12345678901234567.89}").p)'`. **Observe:** it prints `12345678901234568`. Fix: represent money as a string (`"12345678901234567.89"`) as ADR-004 will state. Add a test asserting the price field is a JSON string.
6. (20m) Turn each experiment into a regression test (property binding, UTC instants in output, money as string).
7. (20m) Draft ledger entries for RL-38, 39, 40.

**Tips and common problems**
- `/actuator/env` shows `******` → values are sanitized by default → set `show-values` locally only.
- Time zone test is flaky → set the zone in the test or JVM args, not by editing the system clock; restore after.
- `LocalDateTime` seems fine in your zone → test in a second zone; the bug is invisible on one machine.
- JSON numbers look right in Java → the loss happens in the JS parser, so verify in Node.

**Done when**
- [ ] You predicted and then verified the property winner in 4 launch modes.
- [ ] You reproduced the `12345678901234568` precision loss in Node.
- [ ] Three regression tests and three ledger drafts exist.

### S4 Review + Write (2h)
1. (30m) Finish ledger entries RL-38/39/40 (10 minutes each) and say them aloud.
2. (45m) Read Google API Design Guide sections on resource names and standard methods (cloud.google.com/apis/design). Write ADR-004 "API conventions": plural kebab-case paths, camelCase JSON, pagination parameters, error format (RFC 9457 + `errors`), money as string + currency, ISO-8601 UTC timestamps, IDs as strings.
3. (20m) Review the SF-07 code against ADR-004 and fix inconsistencies.
4. (15m) Make 6 flashcards (idempotent methods, 201+Location, ProblemDetail members, precedence, `Instant` vs `LocalDateTime`, money as string).
5. (10m) Self-check, then weekly retro.

**Self-check:** Which methods are idempotent? Why money-as-string in JSON? What is the benefit of designing the spec before code?
<details><summary>Answers</summary>

- GET, PUT, DELETE (and HEAD/OPTIONS) are idempotent; POST and PATCH are not by default.
- JSON numbers are parsed as IEEE-754 doubles by JavaScript, so large or high-precision decimals lose digits; a string preserves the exact value (and travels with a currency code).
- The contract is reviewed before code exists, frontend and backend can work in parallel, clients and mocks can be generated, and drift can be detected automatically.
</details>

**Output:** `catalog.openapi.yaml`, Catalog API, ADR-004, RL-38/39/40 entries.
**If stuck:** compare your YAML with `/v3/api-docs`; differences usually reveal the mistake.

---

## Week 12 — Postgres, Flyway and JPA basics
**Outcome:** persists the domain properly and sees the SQL Hibernate generates.
**Before you start:** SF-07 merged; Docker Compose Postgres works; `psql` or DBeaver connected (`docker compose exec db psql -U shopflow shopflow`).

### S1 Learn (3h): mapping, repositories, Flyway
1. (25m) Read Spring Data JPA reference "Core concepts" and "Query methods" (derived query keywords, `@Query`). Write 5 derived-query method names for ShopFlow (`findBySku`, `findByStatusAndCreatedAtAfter`...).
2. (30m) Read Hibernate ORM User Guide "Entity" (`@Entity`, `@Id`, identifier generation). Note sequence vs identity.
3. (30m) Read "Associations" (`@ManyToOne`, `@OneToMany(mappedBy)`, helper methods keeping both sides in sync). Then read Vlad Mihalcea, "The best way to map a @OneToMany relationship".
4. (25m) Read "Fetching": lazy vs eager defaults. Memorise: `@ManyToOne` and `@OneToOne` default EAGER, `@OneToMany` and `@ManyToMany` default LAZY. Know what a proxy is.
5. (25m) Read Flyway docs "Concepts": versioned migrations (`V1__name.sql`), checksums, schema history table, "never edit an applied migration".
6. (20m) In a scratch Boot project, enable SQL logging: `logging.level.org.hibernate.SQL=DEBUG` and `logging.level.org.hibernate.orm.jdbc.bind=TRACE` (verify against current docs). Save one entity and read the SQL.
7. (25m) On paper: ERD for Product, Cart, CartItem, Order, OrderItem. Predict the SQL for "load all orders and print item counts" before running anything.

**Tips and common problems**
- Table named `order` fails → `ORDER` is a reserved word → name it `orders`.
- Bidirectional mapping confuses → the side with `mappedBy` is not the owner; update both sides in a helper method.
- `ddl-auto=update` "just works" → it also makes schema drift invisible → use `validate` with Flyway.

**Done when**
- [ ] You wrote the default-fetch table from memory.
- [ ] Your ERD and predicted SQL are saved to compare in S3.

### S2 Build (3h): SF-08 Cart, SF-09 Orders
1. (20m) Finalize the schema on paper: `product(id, sku unique, name, price_amount numeric(19,4), currency char(3))`, `cart`, `cart_item`, `orders`, `order_item(order_id, product_id, quantity, unit_price_amount, currency)`.
2. (25m) Write `V2__catalog.sql` and `V3__cart_orders.sql`. Run the app, then `select * from flyway_schema_history;` in psql. Keep `ddl-auto=validate`.
3. (30m) **Test first:** `@DataJpaTest` with `@AutoConfigureTestDatabase(replace = NONE)` and a Testcontainers Postgres: save a `Product`, `findBySku` returns it, a duplicate SKU throws `DataIntegrityViolationException`. Implement `Product` entity and `ProductRepository`; replace the in-memory service.
4. (35m) Entities `Cart`, `CartItem`, `Order`, `OrderItem`: protected no-arg constructors, `@ManyToOne(fetch = LAZY)`, `Order.addItem(...)` helper, money as amount + currency. Avoid Lombok in this project for now.
5. (30m) Services (`CartService`, `OrderService.placeOrder(cartId)`) and MapStruct mappers entity ↔ DTO (add MapStruct dependency and annotation processor).
6. (30m) Update the OpenAPI YAML first for Cart and Orders endpoints, then write controllers; add tests for place-order happy path.
7. (10m) One PR per ticket (SF-08, SF-09).

**Tips and common problems**
- `Flyway: Migration checksum mismatch` → you edited an applied migration → in dev run `docker compose down -v` (drops the DB volume) or Flyway repair; in production never edit applied migrations, add a new one.
- `Schema-validation: missing column` → entity doesn't match migration → fix entity or add a new migration.
- `@DataJpaTest` uses H2 and fails on Postgres SQL → missing `replace = NONE`.
- Infinite recursion in JSON or `toString()` → bidirectional relations on entities → never serialize entities (Week 12 lab and ADR-005).
- MapStruct produces empty mappers → annotation processor not configured; if you use Lombok later, order matters (Lombok, `lombok-mapstruct-binding`, MapStruct).

**Done when**
- [ ] `flyway_schema_history` shows V1–V3 success.
- [ ] Repository and place-order tests are green on Postgres via Testcontainers.
- [ ] No entity is returned from any controller.

### S3 Lab (2h): RL-48 N+1, RL-49 lazy init, RL-42 entities in JSON
1. (15m) **RL-48, failing test first.** Seed 50 orders × 5 items in a `@DataJpaTest`/`@SpringBootTest`, `em.flush(); em.clear();`. Enable Hibernate statistics (`spring.jpa.properties.hibernate.generate_statistics=true`). Test:
   ```java
   Statistics stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
   stats.clear();
   orders.findAll().forEach(o -> o.getItems().size());
   assertThat(stats.getPrepareStatementCount()).isLessThanOrEqualTo(2);
   ```
   It should fail with ~51 statements.
2. (15m) Run and **observe** the SQL log: one `select ... from orders` followed by 50 `select ... from order_item where order_id=?`. Compare with the SQL you predicted in S1.
3. (10m) Explain in 3 sentences: lazy collection loads per parent on first access, giving 1 + N queries.
4. (10m) Fix with `@EntityGraph(attributePaths = "items")` on a repository method (or `join fetch`). Re-run: statement count ≤ 2. Note: `join fetch` + pagination has its own trap (Week 23).
5. (20m) **RL-49.** Set `spring.jpa.open-in-view=false`. Call a service method (not `@Transactional`) that returns an `Order`, then access `getItems()` in the test. **Observe** `LazyInitializationException: ... could not initialize proxy - no Session`. Fix: map to a DTO inside a `@Transactional(readOnly = true)` service method.
6. (20m) **RL-42.** Make a controller return the `Order` entity directly. Hit the endpoint. **Observe** a serialization failure (lazy proxy / `ByteBuddyInterceptor`) or infinite recursion. Fix: return the DTO; add a test asserting the response has no entity-only fields.
7. (10m) Convert each experiment into a regression test.
8. (20m) Draft ledger entries for RL-48, 49, 42.

**Tips and common problems**
- Statement count is 0 or 1 → entities are still in the persistence context from seeding → `em.flush(); em.clear();` before measuring.
- Test is `@Transactional` and never throws LazyInit → the session stays open for the whole test → use a non-transactional test for RL-49.
- `datasource-proxy` is a fine alternative to statistics (add its dependency; verify against current docs).
- Don't fix N+1 with EAGER; see RL-50 in Week 15.

**Done when**
- [ ] RL-48 failed with ~51 statements and passes with ≤2 using an entity graph.
- [ ] RL-49 and RL-42 reproduced and fixed with DTO mapping inside a transaction.
- [ ] Three ledger drafts exist.

### S4 Review + Write (2h)
1. (35m) Finish ledger entries RL-48/49/42 and speak each in under a minute.
2. (30m) Create `docs/erd.md` with a Mermaid `erDiagram` for the schema.
3. (25m) Write ADR-005 "No entities in API responses" (alternatives: records straight from projections, entities with `@JsonIgnore`).
4. (15m) Flashcards: fetch defaults, N+1 detect/fix, `mappedBy`, `ddl-auto` values, Flyway rules.
5. (15m) Self-check, then weekly retro.

**Self-check:** Why is `EAGER` rarely the answer? What does `spring.jpa.hibernate.ddl-auto=update` risk? How do you see the SQL being run?
<details><summary>Answers</summary>

- EAGER loads data on every access path whether needed or not, can't be switched off per query, and often multiplies queries (N+1) or joins; prefer LAZY plus explicit fetching per use case.
- `update` mutates the schema from entities, can't handle renames/drops, bypasses migrations, and causes drift between environments; use `validate` or `none` with Flyway.
- `logging.level.org.hibernate.SQL=DEBUG` (and bind logging), a datasource proxy, or Hibernate statistics.
</details>

**Output:** schema migrations, repositories, ERD, ADR-005, RL-48/49/42 entries.
**If stuck:** turn on SQL logging first; most JPA mysteries disappear when you read the actual SQL.

---

## Week 13 — Typed client and React UI
**Outcome:** a frontend consuming a generated client; contract-first from the consumer side.
**Before you start:** SF-08/09 merged; OpenAPI YAML covers catalog, cart and orders; Node LTS and npm/pnpm installed.

### S1 Learn (3h): CORS, generated clients, PATCH, breaking changes
1. (30m) Read MDN "Cross-Origin Resource Sharing (CORS)": simple vs preflighted requests, `Access-Control-Allow-*` headers. Draw the preflight sequence from memory afterwards.
2. (20m) Read Spring MVC "CORS" (`@CrossOrigin`, `CorsRegistry` in `WebMvcConfigurer`).
3. (35m) Read the docs of two generators: OpenAPI Generator and a TypeScript-first one (`openapi-typescript` + `openapi-fetch`, or `orval`). Choose one and write why in two lines.
4. (25m) Read RFC 7396 (JSON Merge Patch): `null` removes, absent leaves unchanged. Skim RFC 6902 (JSON Patch) for contrast.
5. (30m) Write from memory a table "breaking vs non-breaking API changes" (remove/rename field, change type, new required field, new optional field, narrow enum, widen enum, change status code). Compare with Stripe's API versioning/changelog docs.
6. (25m) Read TanStack Query "Quick Start", `useQuery`, `useMutation`, query invalidation.
7. (15m) Notes: when would the generated client catch a mistake the compiler wouldn't otherwise?

**Tips and common problems**
- Generated code is huge → treat it as build output; commit only if the team decides so (ADR), otherwise generate in CI.
- You blame CORS for everything → check the network tab: was the request sent, and what did the preflight return?

**Done when**
- [ ] Preflight sequence drawn; generator choice written down.
- [ ] Breaking-change table drafted.

### S2 Build (3h): SF-10 React UI on the generated client
1. (20m) `npm create vite@latest web -- --template react-ts` inside the repo; install `@tanstack/react-query` and your generator (`npm i -D openapi-typescript`, `npm i openapi-fetch`, or the orval equivalent).
2. (20m) Add a script such as `"gen:api": "openapi-typescript ../src/main/resources/openapi/catalog.openapi.yaml -o src/api/schema.d.ts"` (check the CLI for your installed version; verify against current docs). Run it; open the generated types and find `Product`.
3. (20m) In `vite.config.ts` proxy `/api` → `http://localhost:8080` so dev works without CORS. Start both apps and confirm a call works.
4. (45m) Build pages: `ProductList` (`useQuery`), `Cart` (add/remove/update mutations), `PlaceOrder` (`useMutation` → POST `/orders`, then invalidate cart and orders queries). Use only the generated client and types; no hand-written URLs.
5. (25m) Error handling: display ProblemDetail `title`/`detail` and per-field `errors` next to the inputs.
6. (30m) Create `scripts/check-api-drift.sh`: regenerate, then `git diff --exit-code web/src/api`. Run it clean (passes). Change the spec (add a required field), run again (fails), fix, pass.
7. (20m) PR with a screenshot or short screen recording of the happy path and a validation error.

**Tips and common problems**
- Browser console says CORS error but the request reached the server → preflight rejected → use the proxy for dev, or allow origin `http://localhost:5173`, methods and headers in `CorsRegistry`.
- `allowedOrigins("*")` with credentials fails → browsers forbid it → list explicit origins.
- Types show `string` where you expected a number → money is a string by ADR-004; that's intentional.
- Query doesn't refresh after mutation → missing `invalidateQueries` with the correct key.
- Dev double fetch → React StrictMode runs effects twice in development only.

**Done when**
- [ ] You can browse products, add to cart, and place an order in the UI.
- [ ] `check-api-drift.sh` passes on a clean tree and fails when the spec changes without regeneration.
- [ ] No hand-written API URLs in the frontend (`grep -R "fetch(" web/src` is empty or only in the client wrapper).

### S3 Lab (2h): RL-41 PATCH null vs absent, RL-32 Open-Session-in-View
1. (15m) **RL-41, failing test first.** `PATCH /products/{id}` with a DTO `record ProductPatch(String description)`. Tests: body `{"description": null}` must clear the description; body `{}` must leave it unchanged. With a plain DTO both give `null`, so one test fails.
2. (10m) Run and observe: the code can't tell "absent" from "null".
3. (10m) Explain in 3 sentences: Jackson maps both to `null` in a record; JSON Merge Patch needs to distinguish them.
4. (25m) Fix: accept `application/merge-patch+json` into a `JsonNode` and apply field by field:
   ```java
   @PatchMapping(path = "/{id}", consumes = "application/merge-patch+json")
   ProductResponse patch(@PathVariable UUID id, @RequestBody JsonNode patch) {
       return service.patch(id, patch); // if (patch.has("description")) set to patch.get("description").isNull() ? null : text
   }
   ```
   (Alternative: `JsonNullable` from `jackson-databind-nullable`; verify against current docs.)
5. (10m) Three regression tests: null clears, absent keeps, value sets.
6. (10m) **RL-32.** With `open-in-view` left at the default (true), note the startup WARN about it. Make a controller that returns lazily loaded data (entity or mapper called in the controller) and log SQL.
7. (20m) **Observe** queries firing during JSON writing/controller code outside any service transaction, holding a DB connection for the entire request. Set `spring.jpa.open-in-view=false`, watch it break with `LazyInitializationException`, then fix by fetching what you need in the service (`@EntityGraph`/DTO).
8. (20m) Draft ledger entries for RL-41 and RL-32.

**Tips and common problems**
- Spring rejects `application/merge-patch+json` with 415 → `consumes` didn't match the client header → send `Content-Type: application/merge-patch+json`.
- OSIV off breaks existing pages → that's the point: each break is a hidden query now made explicit.

**Done when**
- [ ] The three PATCH cases pass.
- [ ] `open-in-view=false` is set in `application.yml` and the app still works.
- [ ] Two ledger drafts exist.

### S4 Review + Write (2h)
1. (30m) Finish ledger entries RL-41 and RL-32, aloud.
2. (45m) Write `docs/api-breaking-changes.md`: table of change type → breaking? → how to ship safely (expand/contract, deprecation header, version bump). Use the Stripe API changelog for real examples.
3. (20m) Add the drift script and generation steps to the README.
4. (15m) Flashcards: preflight, merge-patch semantics, breaking changes, OSIV.
5. (10m) Self-check, then retro.

**Self-check:** Which API changes are breaking? What does CORS protect, and what does it not? Why disable OSIV?
<details><summary>Answers</summary>

- Breaking: removing/renaming fields, changing types, making optional fields required, narrowing accepted values, changing semantics or status codes. Non-breaking: adding optional fields/endpoints (clients must tolerate unknown fields).
- CORS lets browsers block cross-origin pages from reading responses; it doesn't protect the server from direct requests (curl, servers) and doesn't stop simple cross-site requests from causing side effects (that is CSRF's area).
- OSIV keeps a session and a DB connection open for the whole request and hides lazy-loading queries in the web layer; disabling it makes data access explicit and avoids connection-pool starvation.
</details>

**Output:** React slice, generated client, drift check, breaking-change checklist.
**If stuck:** open the browser network tab first; read the request, response and preflight before changing code.

---

## Week 14 — Testing the slice
**Outcome:** a test pyramid appropriate for Spring apps.
**Before you start:** Docker running (Testcontainers needs it); SF-10 merged; know your current test count (`./mvnw test`).

### S1 Learn (3h): test pyramid, slices, Testcontainers, Mockito
1. (30m) Read Ham Vocke / Martin Fowler, "The Practical Test Pyramid". Draw the pyramid and write one ShopFlow example per level.
2. (30m) Read Spring Boot reference "Testing": `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`, `@JsonTest`. Make a table: annotation → what it loads → what you must mock.
3. (30m) Read Testcontainers guides (Spring Boot + Postgres, `@ServiceConnection`, container reuse). Note how to share one container across tests.
4. (30m) Read Mockito docs: `mock`, `when/thenReturn`, `verify`, `ArgumentCaptor`. Read about when mocks hurt: mocking what you don't own, verifying implementation details.
5. (25m) Read about test data builders / object mothers; write a `ProductBuilder` and an `OrderBuilder` in a scratch file.
6. (20m) Recall: write the slice table again from memory; fix errors.
7. (15m) Plan the test inventory for ShopFlow: what is missing at each level?

**Tips and common problems**
- Boot test annotations differ by version (`@MockBean` vs `@MockitoBean`) → follow the reference for your Boot version.
- "Which level should this test be?" → the lowest level that can fail for the reason you care about.

**Done when**
- [ ] Slice table from memory is correct.
- [ ] A written list of missing tests per level exists.

### S2 Build (3h): SF-11 tests for the slice
1. (15m) Create `docs/testing.md` skeleton (levels, tools, naming, where each lives). Run `./mvnw test` and note total time and test count.
2. (35m) Add a `Clock` bean (`Clock.systemUTC()`) and inject it into `OrderService`; replace `Instant.now()` with `Instant.now(clock)`. **Test first** with `Clock.fixed(...)` asserting `placedAt`.
3. (35m) `@WebMvcTest(OrderController.class)`: valid create, validation error (problem+json), not found, with `jsonPath` assertions and mocked service.
4. (30m) `@DataJpaTest` (Testcontainers): repository query tests and the unique constraint.
5. (45m) One `@SpringBootTest(webEnvironment = RANDOM_PORT)` with Testcontainers: create product → add to cart → place order via HTTP → assert response and DB state. Use `TestRestTemplate`, `WebTestClient` or `RestTestClient` depending on your Boot version (verify against current docs).
6. (10m) Run `./mvnw verify`; record the time.
7. (10m) PR.

**Tips and common problems**
- `Could not find a valid Docker environment` → Docker not running → start it; check `DOCKER_HOST` if using Colima/Rancher.
- Containers start for every test class and slow everything → share a static container or use a base class / singleton pattern.
- `@WebMvcTest` fails with `No qualifying bean` → a controller dependency isn't mocked → add `@MockitoBean` for it.
- Tests affect each other → data from earlier tests; clean tables in `@BeforeEach` (rollback does not work across HTTP calls in `RANDOM_PORT`).
- Time-based assertions flake → inject `Clock`.

**Done when**
- [ ] Controller slice, repository slice and one end-to-end flow are green.
- [ ] `Clock` is injected and used in a deterministic test.
- [ ] `docs/testing.md` skeleton exists.

### S3 Lab (2h): RL-26 self-invocation, RL-28 checked exceptions
1. (15m) **RL-26, failing test first.**
   ```java
   @Service
   class PaymentService {
       public boolean outer() { return inner(); }          // self-invocation
       @Transactional
       public boolean inner() {
           return TransactionSynchronizationManager.isActualTransactionActive();
       }
   }
   ```
   Test: `assertThat(service.outer()).isTrue()`. It fails (`false`). The test class must NOT be `@Transactional`.
2. (10m) Run and **observe** with `logging.level.org.springframework.transaction.interceptor=TRACE` and `logging.level.org.springframework.orm.jpa=DEBUG`: calling `service.inner()` directly logs "Getting transaction for"; calling `outer()` does not.
3. (15m) Print `service.getClass()` and `AopUtils.isAopProxy(service)`: you hold a CGLIB proxy (`$$SpringCGLIB$$`). Explain in 3 sentences: the proxy applies transactions to calls entering from outside; `this.inner()` bypasses the proxy.
4. (15m) Fix: move `inner()` into another bean and inject it (or use `TransactionTemplate`). Test goes green; keep it.
5. (15m) **RL-28, failing test first.**
   ```java
   @Transactional
   public void saveThenFail(Product p) throws Exception {
       repo.save(p);
       throw new Exception("boom");
   }
   ```
   Test expects `repo.count() == 0` after the exception. It fails: the row is committed.
6. (10m) **Observe** the log "Committing JPA transaction" despite the exception. Explain: default rollback is only for unchecked exceptions and errors.
7. (20m) Fix with `@Transactional(rollbackFor = Exception.class)` or, better, unchecked domain exceptions (per ADR-002). Regression test for both.
8. (20m) Draft ledger entries for RL-26 and RL-28.

**Tips and common problems**
- Test passes unexpectedly → your test is `@Transactional`, so everything runs in the test's transaction → remove it for these labs.
- `inner()` is `private` or `final` → different problem (RL-27, Week 15).
- SQL log shows insert but no commit/rollback lines → increase transaction log levels as above.

**Done when**
- [ ] You saw `$$SpringCGLIB$$` in the class name and understand why self-calls skip it.
- [ ] Both bugs reproduced, fixed and covered by regression tests.
- [ ] Two ledger drafts exist.

### S4 Review + Write (2h)
1. (45m) Complete `docs/testing.md`: levels, what each level proves, when to mock, how to run, how long, flakiness policy.
2. (30m) Finish ledger entries RL-26 and RL-28, aloud.
3. (20m) Flashcards: slice annotations table, proxy rules, rollback rules.
4. (15m) Self-check.
5. (10m) Weekly retro.

**Self-check:** What does `@WebMvcTest` load? When is mocking a design smell? Why does a transactional proxy miss self-calls?
<details><summary>Answers</summary>

- The MVC slice only: controllers, controller advice, converters, filters, Jackson config; not services or repositories (mock them).
- When you mock value types or your own collaborators in a way that just repeats the implementation, or need many mocks for one test: it signals tight coupling and brittle tests.
- Spring wraps the bean in a proxy; only calls through the proxy get advice; a call via `this` goes straight to the target.
</details>

**Output:** tests, `docs/testing.md`, RL-26/28 entries.
**If stuck:** print the bean's class and the transaction log lines; they tell you whether the proxy was involved.

---

## Week 15 — Consolidation and Phase 2 gate
**Outcome:** one-command run by someone else.
**Before you start:** SF-06 to SF-11 merged; find a peer or spare machine/VM for the clean-room test.

### S1 Learn (3h): Docker for Java apps, Compose, Actuator
1. (30m) Read Docker docs "Get started": images, containers, layers, Dockerfile basics. Run a hello container.
2. (25m) Read Dockerfile best practices relevant to Java: multi-stage builds, `.dockerignore`, layer ordering (full treatment in Phase 8).
3. (30m) Read Docker docs "Compose": services, networks, volumes, environment, `depends_on` with `condition: service_healthy`, healthchecks.
4. (30m) Read Spring Boot Actuator reference: endpoints `/beans`, `/env`, `/conditions`, `/mappings`, `/configprops`, `/loggers`; how to expose and secure them.
5. (25m) Read Boot "Docker Compose Support" again and decide: dev uses Boot's integration; the demo stack uses your own full `compose.yaml`.
6. (20m) With the app running, call each Actuator endpoint; write one thing you learned from each.
7. (20m) Draft the README outline: prerequisites, run, test, URLs, troubleshooting.

**Tips and common problems**
- Actuator endpoints return 404 → not exposed → `management.endpoints.web.exposure.include`.
- Never expose `env`/`heapdump` publicly; this is local diagnosis only.

**Done when**
- [ ] You called six Actuator endpoints and noted a finding for each.
- [ ] README outline exists.

### S2 Build (3h): SF-12 one-command run
1. (25m) `Dockerfile` for the API (multi-stage: Maven build image → `eclipse-temurin` JRE image) and `.dockerignore` (`target/`, `.git`, `web/node_modules`).
2. (20m) `web/Dockerfile`: build with Node, serve static files with nginx; `nginx.conf` proxies `/api` to the `api` service.
3. (35m) Root `compose.yaml` with `db` (healthcheck `pg_isready`), `api` (`depends_on: db: condition: service_healthy`, env `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/shopflow`, `SPRING_DOCKER_COMPOSE_ENABLED=false`), `web` (port 8081).
4. (25m) Seed data: add a `demo` profile with `spring.flyway.locations=classpath:db/migration,classpath:db/seed` and a `db/seed/V1000__demo_data.sql` (or repeatable migration); enable the profile in compose.
5. (20m) `docker compose up --build`; fix issues until `http://localhost:8081` shows seeded products.
6. (25m) Write the README (prerequisites, `docker compose up --build`, how to run tests, URLs, how to reset with `docker compose down -v`, common errors).
7. (30m) Clean-room test: `git clone` into a fresh directory (or have a peer/VM follow only the README). Time it; fix every gap you or the peer hit.

**Tips and common problems**
- API can't reach DB → used `localhost` inside the container → use the service name `db`.
- API starts before DB is ready → missing healthcheck/condition.
- Port conflicts (5432/8080/8081) → change host ports in compose.
- Apple Silicon image warnings → pin platform-compatible images or add `platform:`.
- Stale data/schema → `docker compose down -v`; stale image → `--build`.
- Flyway complains about seed ordering → give seed migrations a higher version than schema ones.

**Done when**
- [ ] `docker compose up --build` from a clean clone serves the UI with seeded data.
- [ ] A second person followed only the README successfully (or you did on a clean VM).
- [ ] PR for SF-12 merged.

### S3 Lab (2h): RL-27 proxy rules, RL-37 auto-config, RL-50 EAGER fallout
1. (15m) **RL-27, failing test first.** A service whose public method calls a `private @Transactional` method and another `final @Transactional` method; assert `isActualTransactionActive()` is true inside each. Both fail.
2. (10m) **Observe**: `AopUtils.isCglibProxy(service)`; `private` methods are never intercepted and `final` methods cannot be overridden by a CGLIB subclass, so advice is skipped (Spring 6 can proxy protected/package-private methods with class-based proxies but never private or final).
3. (10m) Explain in 3 sentences; compare JDK interface proxies (only interface methods) and Boot's default class-based proxies.
4. (10m) Fix: make the methods public and non-final on a separate bean. Regression test.
5. (15m) **RL-37.** Introduce a typo in `spring.datasource.ulr` (or remove the Flyway dependency) and run with `--debug`. In the "CONDITIONS EVALUATION REPORT" find `DataSourceAutoConfiguration` under Negative matches and read the reason.
6. (10m) Fix it; then do the same via `/actuator/conditions` on a healthy app and find why a bean was (not) created.
7. (15m) **RL-50.** Change `Order.items` to `fetch = EAGER` and keep `OrderItem.product` as the default EAGER `@ManyToOne`. Load a page of orders with statistics on. **Observe** the statement count and join/cascade of queries even for endpoints that don't need items.
8. (10m) Explain and revert to LAZY with `@EntityGraph` on the queries that need the data.
9. (25m) Draft ledger entries for RL-27, 37, 50 and review all Phase 2 entries.

**Tips and common problems**
- The `private` case "works" in your test → you called it from a `@Transactional` test method → keep tests non-transactional.
- `--debug` output is huge → search for "Negative matches" and the bean name.

**Done when**
- [ ] RL-27, RL-37, RL-50 reproduced with evidence (log or statistics).
- [ ] All three have ledger drafts.

### S4 Review + Write (2h)
1. (30m) Gate review: walk through the rubric below with evidence for each line; fix anything missing.
2. (30m) Record yourself explaining the 13 ledger items required by the gate (≤1 minute each); listen back and note unclear ones.
3. (20m) Update the progress tracker in `../java-backend-roadmap.md`.
4. (15m) Tag: `git tag phase-2 && git push --tags`.
5. (15m) Retro for the phase and plan Phase 3 (Week 16 S1 reading).
6. (10m) Answer the self-check.

**Self-check:** What does `depends_on` guarantee in Compose? Why disable Boot's Compose support inside the container? What does `/actuator/conditions` show?
<details><summary>Answers</summary>

- Start order only; to wait for readiness use a healthcheck with `condition: service_healthy`.
- The container already gets its datasource from the compose file; Boot's integration would try to start/manage Compose from inside the container.
- For each auto-configuration, whether its conditions matched and why not.
</details>

**Gate rubric (pass all):**
- Clean clone → `docker compose up` → UI works, with seeded data.
- OpenAPI spec and generated client are in sync (CI-style check passes).
- Slice and integration tests pass; no entities in API; no OSIV.
- Ledger has RL-26/27/28/32/33/34/37/39/40/41/48/49/50 explained aloud.

**Output:** `phase-2` tag, gate notes.
**If stuck:** run `docker compose logs <service>` for the failing service and read the first exception.
