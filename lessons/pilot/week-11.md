# Week 11: REST, validation and contract-first APIs

**Mode:** A→B   **Release:** v0.2 Spring slice
**Outcome:** You can design an HTTP API on paper (OpenAPI), implement it with Spring MVC (status codes, validation, `ProblemDetail` errors, paging), and diagnose two classic "works on my machine" API bugs: dates and money in JSON, and configuration that differs per environment.

**Before you start**
- Week 10 done (at least S1 and S2). No database this week: products live in memory. Docker is not needed.
- Run the week once: `scripts/run-week.sh w11 api`. Expect **34 tests, 26 red, 8 green** (one worked example plus guards).
- You have Node installed (from your frontend work): it is used in INC-39/40 to see what a browser really does with the JSON.
- Run the app for this week: `./mvnw -q -pl api spring-boot:run -Dspring-boot.run.main-class=shopflow.api.w11.W11App`

**Repo:** `shopflow-lab`, module `api`, package `shopflow.api.w11`, tag `@Tag("w11")`
- Run all: `scripts/run-week.sh w11 api` · one class: `scripts/run-week.sh w11 api Ex1GetProductTest` · nested classes: `'Inc38*'`
- Hints: `hints/w11/` · Solutions: `solutions/w11/` (only after green)
- Settings: `api/src/main/resources/shopflow-w11.yml` · the contract: `api/src/main/resources/openapi/catalog.openapi.yaml`

---

## S1 Try it (3h): mode A. Your first REST endpoints

Goal: read an OpenAPI description, then make three endpoint behaviours green: get one, create one, reject bad input.

1. (15m) **Orient and baseline.** Open `shopflow.api.w11.catalog` (controller, service, an in-memory repository, request/response records, two exception classes, and an `ApiExceptionHandler` with nothing in it yet). Run `scripts/run-week.sh w11 api PingControllerTest`: green. This is the **worked example**; the rest of the week is red.
2. (25m) **Worked example and primer.**
   - Read `ping/PingController` and `PingControllerTest` (a "web slice" test: only the web layer is started, which is why it runs in a fraction of a second).
   - **Predict, then change:** make the controller return `Map.of("status", "up")`; predict what happens to the test; run it; revert.
   - Read: MDN *HTTP request methods* and *HTTP response status codes* (200, 201, 204, 400, 404, 409, 500 only) and Spring MVC reference *Web on Servlet Stack > Annotated Controllers* (`@RestController`, `@RequestMapping`, `@PathVariable`, `@RequestBody`, `ResponseEntity`). Write from memory: which methods are idempotent, and when is 201 used instead of 200?
3. (20m) **Read a spec.** Open `catalog.openapi.yaml`. It contains one finished operation (`GET /api/products/{id}`) and TODOs. Paste it into the Swagger Editor (editor.swagger.io) to see it rendered. Identify in your notes: path, operation, parameters, responses (and which media type each uses), `$ref` and `components/schemas`. Read the first pages of the OpenAPI Specification (spec.openapis.org, "Paths Object", "Responses Object") as needed.
4. (30m) **EX-11-01: GET one product + the 404.**
   - Read `Ex1GetProductTest`. **Predict:** what status does the controller return now? Run `Ex1GetProductTest` and check (501).
   - In `ProductController.get` return `service.get(id)` (change the return type from `ResponseEntity<?>` to `ProductResponse`). Run: `found` is green, `notFound` still red.
   - In `ApiExceptionHandler` add `@ExceptionHandler(ProductNotFoundException.class)` returning a `ProblemDetail` with status 404, **title** "Product not found" and the exception message as detail.
   - Read Spring MVC reference *Error Responses* and RFC 9457 (Problem Details), sections on the standard members `type`, `title`, `status`, `detail`. Check with the app running: `curl -i localhost:8080/api/products/999` → `Content-Type: application/problem+json`.
5. (30m) **EX-11-02: POST creates → 201 + Location.**
   - Run `Ex2CreateProductTest` (red: 501). Implement `create`: call the service, build the `Location` header with `ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")...`, return `ResponseEntity.created(location).body(created)`. Remember `@RequestBody` is already on the parameter.
   - Try it for real: `curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/json' -d '{"sku":"NEW-1","name":"Eraser","price":2.50}'` then `curl -i <the Location value>`.
6. (40m) **EX-11-03: validation and a useful 400.**
   - Run `Ex3ValidationTest` (4 red, the malformed-JSON guard is green: Spring already handles unreadable bodies).
   - Add Bean Validation annotations to `ProductRequest` (rules in its javadoc) and `@Valid` on the controller parameter. Run again: now the status is 400 but `errors[*].field` is missing.
   - In `ApiExceptionHandler` override `handleMethodArgumentNotValid` (it comes from `ResponseEntityExceptionHandler`): take `ex.getBody()`, set title "Validation failed", add property `errors` = list of `{field, message}` from `ex.getBindingResult().getFieldErrors()`. Stuck at 10 minutes? `hints/w11/EX-11-03.md`.
   - Check by hand: `curl -s -X POST localhost:8080/api/products -H 'Content-Type: application/json' -d '{"sku":"ab","name":"","price":-1}'` → JSON with `status:400` and an `errors` array.
7. (20m) **Close the loop.** Run `scripts/run-week.sh w11 api 'Ex*'` (all 3 classes green, 10 tests) and `PingControllerTest`. Commit each exercise separately. In your notes list: 3 things Spring did for you that you did not write.

**Tips and common problems**
- `Whitelabel`/HTML error page instead of JSON → the exception is not handled by your advice or the path is wrong → check `@RestControllerAdvice` on `ApiExceptionHandler` and run with `curl -i`.
- `HttpMessageNotReadableException` on `curl` → shell quoting broke the JSON → use single quotes outside and double quotes inside, as in the examples.
- Test for 404 passes but `Content-Type` check fails → you returned `ResponseEntity.notFound()`; return a `ProblemDetail` so Spring writes `application/problem+json`.
- Your tutorial uses `@WebMvcTest` from `org.springframework.boot.test.autoconfigure.web.servlet` → **Spring Boot 4 moved it** to `org.springframework.boot.webmvc.test.autoconfigure` (`@AutoConfigureMockMvc` too), and the test starter is `spring-boot-starter-webmvc-test`.
- Your tutorial imports `com.fasterxml.jackson.databind.ObjectMapper` → **Boot 4 uses Jackson 3** (`tools.jackson.databind.json.JsonMapper`). The annotations (`@JsonFormat`, `@JsonProperty`) are still in `com.fasterxml.jackson.annotation`.
- `errors` has the right fields but in an unexpected order → the test uses `hasItems`, so order does not matter; check spelling of `field`.

**Done when**
- [ ] `PingControllerTest` and `Ex1GetProductTest`, `Ex2CreateProductTest`, `Ex3ValidationTest` are green.
- [ ] You called all three endpoints with `curl` and saw the `application/problem+json` bodies.
- [ ] Notes: answers to the idempotency/201 question and the "3 things Spring did for me".

---

## S2 Build (3h): SF-07 Catalog API, spec first

Goal: write the contract first, then complete the API until the contract test and the behaviour tests agree.

Acceptance tests: `Sf07SpecContractTest` (6) and `Sf07CatalogApiTest` (9). Hints: `hints/w11/SF-07.md`.

1. (10m) **Read the tests as a requirements document.** List the demanded status codes per operation. Run `scripts/run-week.sh w11 api 'Sf07*'`: spec test 5 red, API test 9 red.
2. (45m) **Write the contract first.** Extend `catalog.openapi.yaml`: `GET`/`POST` on `/api/products` (with `page` and `size` query parameters, responses 200 / 201 + `Location` header / 400 / 409), `PUT`/`DELETE` on `/api/products/{id}` (200/400/404/409 and 204/404), schemas `ProductRequest`, `ProductResponse`, a page schema and `Problem` (with the `errors` array). Every error response uses `application/problem+json`. Validate the YAML in the Swagger Editor, then run `Sf07SpecContractTest` until all 6 pass. The failure messages name exactly what is missing.
3. (30m) **PUT and DELETE.** Implement them in `ProductController` (the service methods exist). `update` returns the updated product (200); `delete` returns 204. Missing id → 404 through the handler you already have. Run `Sf07CatalogApiTest` for `update`, `updateUnknown`, `deleteRemovesProduct`, `deleteUnknown`.
4. (25m) **409 on duplicate SKU.** The service throws `DuplicateSkuException`. Add the handler: status 409, title "Duplicate SKU". Run `duplicateOnCreate` and `duplicateOnUpdate`. Add `@Valid` to `update` if not yet done (`updateInvalid`).
5. (30m) **Paging.** Implement `list(page, size)` in the controller. `size` defaults to `shopflow.catalog.page-size` (10), already handled in `ProductService.list`. Run `defaultPage` and `explicitPage`. By hand: create 12 products with a shell loop and call `curl 'localhost:8080/api/products?page=1&size=5'`:
   `for i in $(seq 3 14); do curl -s -X POST localhost:8080/api/products -H 'Content-Type: application/json' -d "{\"sku\":\"SKU-$i\",\"name\":\"P$i\",\"price\":1.00}" >/dev/null; done`
6. (25m) **Spec versus implementation, by hand.** Go through every operation in the spec and call it with `curl -i`. For each, tick: status code as documented? media type as documented? If they disagree, decide which side is wrong. The spec is the contract: change the code unless the spec itself was a mistake. Write down one discrepancy you found (there is usually one).
7. (15m) **Close the loop.** `scripts/run-week.sh w11 api 'Ex*,Sf07*'` is green (25 tests). Commit `SF-07 catalog API (spec first)`.

**Tips and common problems**
- Swagger Editor says "bad indentation of a mapping entry" → YAML is indentation-sensitive; use spaces only, 2 per level.
- The spec test fails on `application/problem+json` → the media type key must be exactly that string, under `content`, inside each error response.
- Your list endpoint returns everything → remember `skip(page * size).limit(size)`; the service does it, the controller must pass `page` and `size` through.
- `page` and `size` come as `null` and cause a NullPointerException → declare the parameters as `Integer` with `required = false` (as in the stub), not `int`.
- Duplicate SKU test returns 500 → the exception is not mapped; mapping goes in the same `@RestControllerAdvice`.

**Done when**
- [ ] `Sf07SpecContractTest` (6/6) and `Sf07CatalogApiTest` (9/9) are green.
- [ ] You can show a table "operation / status codes" that matches both the spec and your `curl` calls.
- [ ] You wrote down one spec-vs-code discrepancy and how you resolved it.

---

## S3 Incident (2h): mode B. Dates, cents and environments

Hints: `hints/w11/INC-39-40.md`, `hints/w11/INC-38.md` (open one level at a time).

### INC-39/40 (RL-39, RL-40): "Wrong day, lost cents" (≈55m)
> **Support ticket #5120:** "Customers in Tokyo see order 1 as created *yesterday*. And the invoice total of our biggest customer shows as 12345678901234568 instead of 12345678901234567.89. Also normal totals like 19.90 appear as 19.9 in the app."

1. **PREDICT** (5m). In your notes:
   1. The server clock says 2026-11-02 23:30 UTC. What will a browser in Tokyo show for `"2026-11-02T23:30:00"` (no zone)? And for `"2026-11-02T23:30:00Z"`?
   2. What does JavaScript do with the JSON number `12345678901234567.89`?
   3. What does the JSON number `19.90` become in JavaScript?
   4. How would you transport a money amount so that no client can round it?

   <details><summary>Answers</summary>

   1. Without a zone, JavaScript treats it as the *viewer's local* time: 23:30 on Nov 2 in Tokyo, which is a different instant (14:30 UTC) than intended. With `Z` it is an exact instant: 08:30 on Nov 3 in Tokyo (UTC+9).
   2. It becomes the nearest double: `12345678901234568`. Numbers above 2^53 lose digits.
   3. `19.9`: a JS number has no trailing zeros, so the scale is lost.
   4. As a string (`"19.90"`), converted to a decimal type on the client.
   </details>
2. **REPRODUCE** (5m). `scripts/run-week.sh w11 api Inc39MoneyAndDatesTest`. Expected: `basics` green; `createdAtHasZone` red (`expected:<2026-11-02T23:30:00Z> but was:<2026-11-02T23:30:00>`); `totalAsString` and `largeTotal` red (the raw JSON contains `"total":19.90`).
3. **INVESTIGATE** (15m).
   - Start the app and look at the raw response: `curl -s localhost:8080/api/order-summaries/2`.
   - See what a browser does: `node -e 'console.log(JSON.parse(process.argv[1]).total)' '{"total":12345678901234567.89}'` prints `12345678901234568`. And: `TZ=Asia/Tokyo node -e 'console.log(new Date("2026-11-02T23:30:00").toString()); console.log(new Date("2026-11-02T23:30:00Z").toString())'`.
   - Signal: **two different instants** for the same string. Write down which part of the response is ambiguous. Stuck at 10 minutes? Open hint level 1-2.
4. **STUDY** (10m). Read: MDN `Number.MAX_SAFE_INTEGER`; ISO 8601 / RFC 3339 timestamps (why "Z"); the Java docs for `Instant` vs `LocalDateTime`; your own Phase 1 "time rules" note. Answer: (a) what does `LocalDateTime` *not* know? (b) why is "money as a string" common in payment APIs (Stripe uses integer minor units, others use strings)? (c) what are the two safe encodings of money in JSON?
5. **FIX** (10m). In `OrderSummaryResponse` use `Instant` for `createdAt` (and `Instant.now(clock)` in the controller). For `total` keep `BigDecimal` but serialize it as a JSON string with `@JsonFormat(shape = JsonFormat.Shape.STRING)` (import `com.fasterxml.jackson.annotation.JsonFormat`, which is unchanged in Jackson 3).
6. **PROVE** (5m). `Inc39MoneyAndDatesTest` green (4/4). Re-run the Node commands against the fixed endpoint and write what changed.
7. **GENERALIZE** (5m). Ledger entries **RL-39** (zone-less timestamps) and **RL-40** (money as JSON numbers) with one-minute answers.

**Tips and common problems**
- Your test passes with `jsonPath("$.total").value("19.90")` but a real client still gets a number → Spring's JsonPath helper coerces numbers to strings and hides the problem; the lab test looks at the raw JSON on purpose. Check the raw body whenever the exact JSON type matters.
- The date field now ends with `Z` but your clock bean returns a local zone → the app's `Clock` is `Clock.systemUTC()`; keep it injected so tests can fix it.
- `@JsonFormat` has no effect → you imported another `JsonFormat` or put it on the wrong element; on a record put it directly in front of the component.

### INC-38 (RL-38): "Production shows 10 products per page, not 50" (≈55m)
> **Ticket #5188:** "We set the page size for production to 50 in the configuration repo. Production still shows 10. Ops start the container with SPRING_PROFILES_ACTIVE=production. The same config works on the QA server."

1. **PREDICT** (5m).
   1. Which of these wins if all are set: a profile-specific yaml value, `application.yml`, an environment variable, a command-line argument?
   2. If a profile document is never activated, what do you see in the logs and in `/actuator/env`?
   3. Does a typo in a profile name cause an error at startup?

   <details><summary>Answers</summary>

   1. Command line > environment variables > profile-specific files > `application.yml` > defaults (the exact full ladder is in the Boot reference; when two sources set the same key the higher one wins).
   2. The log line "The following 1 profile is active: production"; the `env` endpoint lists no property source for that document. Nothing tells you a profile document exists but did not match.
   3. No. A profile that no document refers to is simply ignored.
   </details>
2. **REPRODUCE** (5m). `scripts/run-week.sh w11 api 'Inc38*'`. Expected: `productionSize` red (`expected: 50` but was 10); the default-size, explicit-override and `/actuator/env` tests are green guards.
3. **INVESTIGATE** (15m). Ask Spring where a value comes from. Locally only (never in production: it exposes configuration):
   `SPRING_PROFILES_ACTIVE=production ./mvnw -q -pl api spring-boot:run -Dspring-boot.run.main-class=shopflow.api.w11.W11App -Dspring-boot.run.arguments="--management.endpoints.web.exposure.include=env --management.endpoint.env.show-values=ALWAYS"`
   then `curl -s localhost:8080/actuator/env/shopflow.catalog.page-size` and `curl -s localhost:8080/actuator/env | head -c 600` (look at `activeProfiles`).
   Signal: `activeProfiles: ["production"]`, yet the winning source is `shopflow-w11.yml ... (document #0)` with value `10`: the second document (the one meant for production) never appears in the list of sources. Compare the active profile name with the name in `shopflow-w11.yml`. Hint ladder: `hints/w11/INC-38.md`.
4. **STUDY** (10m). Spring Boot reference *Externalized Configuration*: the list of property sources in order of precedence, *Profile Specific Files*, and *Activation Properties* (`spring.config.activate.on-profile`). Answer: where do environment variables rank? Where do `@SpringBootTest(properties=...)` values rank? Why does `Override.explicitWins` (guard) pass?
5. **FIX** (5m). Make the profile name in `shopflow-w11.yml` match the one used by ops. Run `Inc38*` → 4/4 green.
6. **PROVE + GENERALIZE** (25m). Add a test of your own: with profile `production` and the property `shopflow.catalog.page-size=7`, the value is 7. Ledger entry **RL-38** that includes the precedence ladder in your own words and the rule "when a value is wrong, ask `/actuator/env` where it came from".

**Tips and common problems**
- `/actuator/env` returns 404 → it is not exposed by default; use the two `--management...` arguments above (locally only).
- Values show as `******` → Boot hides values; `management.endpoint.env.show-values=ALWAYS` shows them (again: local only).
- The yaml document separator must be exactly `---` on its own line; a wrong indentation after it silently creates a different key.

**Wrap-up** (10m). Run `scripts/run-week.sh w11 api` → 34 green. Write which hint levels you used.

**Done when**
- [ ] `Inc39MoneyAndDatesTest` (4/4) and `Inc38ConfigPrecedenceTest` (4/4) are green.
- [ ] Ledger entries for RL-38, RL-39, RL-40.
- [ ] You can explain, with the Node output, why money should travel as a string.

---

## S4 Consolidate (2h)

1. (30m) **Ledger and recall.** Complete the three ledger entries (RL-38/39/40) and record the one-minute answers. Listen to one recording and critique it.
2. (30m) **ADR-004 "API conventions".** Create `docs/adr/ADR-004-api-conventions.md` and decide, with reasons: resource naming (plural nouns, kebab-case paths), status codes per operation, error format (RFC 9457 `application/problem+json` with an `errors` array for validation), paging (`page`, `size`, defaults from configuration), **money as strings**, **instants as ISO-8601 UTC with `Z`**, and no entities in responses. Add a line for what you would change when the first client complains.
3. (15m) **Flashcards.** Add:
   1. Which HTTP methods are idempotent and why does it matter for retries?
   2. When do you return 201 and what goes in `Location`?
   3. What does `application/problem+json` contain?
   4. Why `Instant` and not `LocalDateTime` in an API?
   5. In what order does Spring Boot resolve configuration sources?
4. (20m) **Self-check.** Answer aloud first.
   1. Why write the OpenAPI spec before the code?
   2. A client sends `{"price": 1.234}`: what do you answer and with which status?
   3. A value in production is not what the yaml says. What is your first move?
   4. Why does a test that asserts `jsonPath("$.total").value("19.90")` sometimes not prove the JSON type?

   <details><summary>Answers</summary>

   1. The contract can be reviewed and used (mocks, generated clients) before the implementation exists; it prevents the code from defining the API by accident.
   2. 400 with a problem body that names the field `price` (Bean Validation `@Digits`).
   3. Ask the application where the value came from (`/actuator/env`, in a safe environment) and check the active profiles.
   4. JsonPath helpers coerce a JSON number to the expected type; assert on the raw body when the JSON type matters.
   </details>
5. (15m) **Compare with the reference** (only now): `diff -ru api/src/main solutions/w11/api/src/main | less`. Note one difference and whether you agree.
6. (10m) **Retro** in `retro.md`.

**Done when**
- [ ] ADR-004 committed; flashcards added; retro written.
- [ ] `scripts/run-week.sh w11 api` is fully green and committed.

**Output of the week:** `catalog.openapi.yaml`, SF-07 PR, three EX commits, ADR-004, ledger entries RL-38/39/40, notes.

**Next week preview:** the catalog moves from memory to Postgres (JPA, Flyway, Testcontainers). Start Docker Desktop before you begin.

**If stuck:** re-read the failing assertion's *expected vs actual* line first; for configuration puzzles use `/actuator/env`; for JSON puzzles use `curl -i` and look at the raw body.
