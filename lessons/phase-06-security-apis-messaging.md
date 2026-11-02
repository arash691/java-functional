# Phase 6: Security, APIs and Messaging (Weeks 33–38)

Goal: secure by default, APIs that survive retries and duplicates, and asynchronous flows that don't lose or double-apply work.

Setup once: Keycloak in Docker Compose, OWASP ZAP, Kafka (KRaft mode) in Compose, Redis, Toxiproxy.

Conventions for this phase:
- Keep everything in one `docker-compose.yml` profile (`security`, `messaging`) so a single `docker compose --profile messaging up -d` starts the dependencies.
- Create `reality-lab/phase6` for lab code; ledger entries go in `reality-lab/LEDGER.md`.
- Secrets in compose files are dev-only. Never reuse them anywhere else.

---

## Week 33 — Authentication and authorization
**Outcome:** can configure Spring Security with OIDC and explain every step of the flow.
**Before you start:** Docker running; ShopFlow API and React app start locally; port 8081 free for Keycloak.
**If stuck:** decode your token at the command line (see tips) and compare `iss`, `aud`, `exp` and roles with what the API expects.

#### S1 Learn (3h): OAuth2, OIDC, JWT and Spring Security
1. (30m) Read oauth.com (Aaron Parecki): roles, client types, and the authorization code flow. Write a table: grant type → when to use (auth code + PKCE for browser/mobile apps, client credentials for service-to-service).
2. (25m) Read the PKCE section. Redraw the sequence on paper: browser → authorization endpoint (code_challenge) → redirect with code → token endpoint (code_verifier) → tokens → API call. Mark where each secret/verifier lives.
3. (25m) Read about JWT structure and RFC 8725 (JWT BCP). Make a checklist of what a resource server must validate: signature + algorithm allow-list, `iss`, `aud`, `exp`/`nbf`, and token type.
4. (30m) Skim RFC 9700 (OAuth 2.0 Security BCP). Note five rules (no implicit flow, PKCE everywhere, exact redirect URI matching, short-lived access tokens, refresh token handling).
5. (35m) Spring Security reference: "Architecture" (filter chain, `SecurityFilterChain`) and "OAuth2 Resource Server" (JWT). Write what happens to a request from `FilterChainProxy` to your controller.
6. (25m) "Method Security" (`@PreAuthorize`) and RBAC vs ABAC. Note CSRF: needed for cookie-session apps, usually disabled for stateless bearer-token APIs and why.
7. (10m) Flashcards: PKCE, `aud`, `iss`, 401 vs 403.

**Tips and common problems**
- Confusing authentication with authorization → AuthN says who you are, AuthZ says what you may do → keep two columns in your notes.
- Decoding a token to inspect it: `echo '<payload-part>' | tr '_-' '/+' | base64 -d` (add `=` padding if it complains); never paste real production tokens into online tools.
- Overreading RFCs → read the summaries and checklists; the goal is a validation checklist.

**Done when**
- [ ] The PKCE sequence drawn from memory is correct.
- [ ] You have a JWT validation checklist.

#### S2 Build (3h): SF-26 Keycloak + resource server + React login
1. (20m) Add Keycloak to Compose, for example (verify image tag and env names against current Keycloak docs):
   ```yaml
   keycloak:
     image: quay.io/keycloak/keycloak:latest
     command: start-dev --import-realm
     environment:
       KC_BOOTSTRAP_ADMIN_USERNAME: admin
       KC_BOOTSTRAP_ADMIN_PASSWORD: admin
       KC_HTTP_PORT: 8081
       KC_HOSTNAME_STRICT: "false"
     ports: ["8081:8081"]
     volumes: ["./keycloak:/opt/keycloak/data/import"]
   ```
2. (30m) In the admin console create realm `shopflow`; clients `shopflow-web` (public, standard flow, PKCE S256, exact redirect URI `http://localhost:5173/*` pattern as needed) and `shopflow-api`; realm roles `customer`, `admin`; two users. Export the realm to `keycloak/shopflow-realm.json` so it imports on startup.
3. (25m) API: add `spring-boot-starter-oauth2-resource-server`; set `spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081/realms/shopflow`; define a `SecurityFilterChain` (stateless, `authorizeHttpRequests`, `oauth2ResourceServer(jwt)`). Add a `JwtAuthenticationConverter` that maps `realm_access.roles` to `ROLE_*` authorities.
4. (30m) React: sign in with `oidc-client-ts` (or Keycloak JS adapter) using authorization code + PKCE; attach `Authorization: Bearer` in the generated client; handle token expiry.
5. (30m) Method security: enable `@EnableMethodSecurity`; add `@PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#id, authentication)")` to order reads; implement `OrderSecurity.isOwner` by comparing the token `sub` with the order's `customerId`.
6. (35m) Tests: use `SecurityMockMvcRequestPostProcessors.jwt()` from spring-security-test to cover (a) no token → 401, (b) customer reads own order → 200, (c) customer reads someone else's → 403/404, (d) admin reads any → 200.
7. (10m) Open PR with realm JSON, compose, API and UI changes.

**Tips and common problems**
- `Invalid parameter: redirect_uri` in Keycloak → redirect URIs must match the client config exactly (scheme, host, port, path) → fix in the client settings and re-export the realm.
- API returns 401 with "iss claim is not valid" → token `iss` is `http://localhost:8081/...` but the API in Docker resolves the issuer as `http://keycloak:8081/...` → use the same hostname everywhere (`KC_HOSTNAME`) or use `jwk-set-uri` for key fetch and a fixed `issuer`.
- Browser calls fail with a CORS error only when sending the token → preflight `OPTIONS` doesn't carry `Authorization` and is blocked by the security chain → enable `http.cors(...)` with a `CorsConfigurationSource` before authorization rules.
- Roles missing → Keycloak puts realm roles under `realm_access.roles`, not `scope` → map them in the converter.
- Clock skew makes new tokens look "not yet valid" → sync clocks (WSL/VM drift) or allow small skew.

**Done when**
- [ ] `docker compose up` brings Keycloak with the realm imported.
- [ ] Browser login works and the API rejects calls without a valid token.
- [ ] The four authorization tests pass.

#### S3 Lab (2h): JWT misconfiguration drills and IDOR (extras)
1. (10m) Create a test class `AuthDrillsIT` that starts the API and has helper methods to call endpoints with a custom `Authorization` header.
2. (25m) **alg=none token.**
   - Craft a token (header `{"alg":"none"}`, payload with admin role, `exp` far future, empty signature):
   ```bash
   h=$(printf '{"alg":"none","typ":"JWT"}' | base64 | tr -d '=\n' | tr '/+' '_-')
   p=$(printf '{"sub":"attacker","iss":"http://localhost:8081/realms/shopflow","exp":9999999999,"realm_access":{"roles":["admin"]}}' | base64 | tr -d '=\n' | tr '/+' '_-')
   curl -i -H "Authorization: Bearer $h.$p." localhost:8080/api/admin/orders
   ```
   - Failing test: expect 401. Observe the response and the log line from the JWT decoder (it should reject; if it accepts, you misconfigured a custom decoder).
   - Explain: the server must pin allowed algorithms and always verify signatures; `none` is an attack vector in naive libraries.
   - Fix: keep the Spring `JwtDecoder` default (RS256 from JWKS) and never write a custom decoder that skips verification.
3. (25m) **Missing `aud` check.**
   - Get a valid token from a different client in the same realm (e.g., a `reporting` client). Call the ShopFlow API with it.
   - Failing test: expect 401/403; observe 200 if only `iss` and signature are validated.
   - Explain: signature and issuer prove who issued the token, not that it was meant for your API.
   - Fix: add a `JwtClaimValidator<List<String>>("aud", ...)` combined with `JwtValidators.createDefaultWithIssuer(...)` on the `NimbusJwtDecoder`. Regression test for wrong audience.
4. (20m) **Long-lived access token / no revocation.**
   - In Keycloak set access token lifespan to 1 day and observe that logout/disable does not stop the token being accepted by the API.
   - Explain: stateless JWTs are valid until `exp`; revocation needs short lifetimes + refresh tokens, or introspection/deny-lists.
   - Fix: set 5 minutes, use refresh tokens in the SPA. Write down your policy.
5. (25m) **IDOR (insecure direct object reference).**
   - Failing test: customer A calls `GET /orders/{idOfB}`; expect 403/404, observe 200 when only role checks are present.
   - Explain: role checks don't establish ownership; authorization must consider the specific resource.
   - Fix: `@PreAuthorize` with ownership check or repository query scoped by `customerId`. Regression: test passes.
6. (15m) Ledger entries (extras) for all four.

**Tips and common problems**
- The `alg=none` call returns 401 for the wrong reason (malformed token) → check the logs for the exact error and craft the token carefully, including the trailing dot.
- `base64` flags differ between macOS and Linux → use `tr -d '\n'` and base64url translation as shown; verify with a decoder.
- Test uses mock JWTs and never hits real validation → for these drills run an integration test with the real decoder and a real token from Keycloak (Testcontainers Keycloak or your Compose instance).

**Done when**
- [ ] Four failing-then-passing tests exist.
- [ ] Each ledger entry names the exact config line that caused/fixed the issue.

#### S4 Review + Write (2h): ADR-014
1. (40m) Write ADR-014 "AuthN/AuthZ: OIDC with Keycloak, roles and ownership checks": context, options (sessions vs JWT, build vs buy), decision, consequences (token lifetime, revocation, role model).
2. (30m) Create an auth flow diagram (sequence) and a "JWT validation checklist" page in `docs/security/`.
3. (20m) Flashcards: grant types and when to use each; the claims to validate; 401 vs 403.
4. (15m) Finish ledger entries; say each answer in ≤ 1 minute.
5. (10m) PR.
6. (5m) Retro.

**Done when**
- [ ] ADR-014 merged; diagram and checklist exist.

**Self-check:** Why PKCE? What must a resource server validate in a JWT? Why is hiding the button in React not authorization?
<details><summary>Answers</summary>

- PKCE binds the authorization code to the client that started the flow, so an intercepted code can't be redeemed by an attacker (essential for public clients like SPAs).
- Signature (with an allow-listed algorithm), issuer, audience, expiry/not-before, and that it's an access token for this API.
- The browser is under the user's control; the server must enforce permissions on every request.
</details>

**Output:** SF-26, ADR-014, security tests.

---

## Week 34 — Application security
**Outcome:** attacks her own app and fixes real findings.
**Before you start:** the app runs in Docker Compose with authentication on; you have a test user token for scans.
**If stuck:** reproduce each attack with `curl` before writing a test; then fix.

#### S1 Learn (3h): OWASP Top 10, secrets and supply chain
1. (45m) Read the OWASP Top 10 categories. For each write one Java/Spring/React example of how it shows up in ShopFlow (injection, broken access control, crypto failures, SSRF, vulnerable components, logging failures, etc.).
2. (45m) OWASP Cheat Sheet Series: SQL Injection Prevention, Input Validation, Logging, Secrets Management. Write the "rules" from each in ≤ 5 bullets.
3. (15m) Skim OWASP ASVS: look at level 1 requirement headings; pick 10 to use as a checklist.
4. (30m) Supply chain: OWASP Dependency-Check or Trivy docs; what an SBOM (CycloneDX) is and how it helps respond to a new CVE.
5. (20m) Secrets management: never in git or images; env vars vs mounted secrets vs a secret store; rotation.
6. (25m) Build a table: threat → ShopFlow feature → control → test.

**Tips and common problems**
- Trying to memorise the Top 10 list → map each item to code you wrote → that's what sticks.
- Thinking dependency scanners find everything → they only know published CVEs; unused dependencies and custom code issues remain.

**Done when**
- [ ] A threat → control → test table exists.
- [ ] You can explain what an SBOM is for.

#### S2 Build (3h): SF-27 ZAP scan and fixes
1. (20m) Start the stack. Run a ZAP baseline scan with Docker (verify the current image name and options in the ZAP docs):
   `docker run --rm -t -v "$(pwd)":/zap/wrk:rw ghcr.io/zaproxy/zaproxy:stable zap-baseline.py -t http://host.docker.internal:8080 -r zap-baseline.html`
2. (20m) Read the HTML report; list alerts by risk. Note which are false positives and why.
3. (40m) Set up an authenticated active scan with ZAP's Automation Framework (YAML plan) using a static `Authorization: Bearer` header or a script-based auth (verify format in the ZAP docs). Run it against a **local** instance only.
4. (20m) Triage in `docs/security/zap-findings.md`: finding, risk, affected endpoint, decision (fix / accept / false positive), owner.
5. (60m) Fix at least five findings, each with a test: for example security headers (`X-Content-Type-Options`, `Strict-Transport-Security`, a CSP for the React app), cookie flags, error responses leaking stack traces, missing rate limits on login, directory/Actuator exposure.
6. (15m) Add `cyclonedx-maven-plugin` (`mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom`) and a dependency scan (`trivy fs .` or OWASP Dependency-Check); store reports as build outputs.
7. (5m) PR.

**Tips and common problems**
- ZAP can't reach the app → `localhost` inside the container isn't your host → use `host.docker.internal` (macOS/Windows) or the Compose network name.
- Active scan fills the DB with junk or locks accounts → run only against a disposable local environment and reset data afterwards; never scan production or shared environments.
- ZAP reports everything as unauthenticated 401 → authentication isn't configured in the scan context → add the header or auth script and verify by checking a protected URL appears in the spider results.
- Header fixes don't show up → a reverse proxy/dev server overrides them → test at the layer users hit.

**Done when**
- [ ] `zap-findings.md` lists every alert with a decision.
- [ ] At least five fixes merged with tests.
- [ ] A command generates the SBOM and a dependency report.

#### S3 Lab (2h): extras — injection, XSS, SSRF, leaked secret
1. (5m) Create `reality-lab/phase6/sec-drills` with a tiny Spring Boot app and a React page for the web drill.
2. (30m) **SQL injection (raw JDBC).**
   ```java
   var rs = stmt.executeQuery("SELECT * FROM products WHERE name = '" + name + "'");
   ```
   - Failing test: call `/search?name=' OR '1'='1` and assert only matching products return (you get all rows).
   - Observe the executed SQL (enable statement logging).
   - Explain: user input becomes part of the SQL grammar; parameter binding keeps data separate from code.
   - Fix: `PreparedStatement` with `?` or Spring Data parameters. Regression test with the payload.
3. (25m) **Stored XSS in React.**
   ```jsx
   <div dangerouslySetInnerHTML={{ __html: review.text }} />
   ```
   - Failing test: store `<img src=x onerror=alert(1)>` and render; assert no script executes (use Playwright/Testing Library with a spy on `window.alert`).
   - Explain: unescaped HTML from users runs as script in other users' browsers.
   - Fix: render text normally (React escapes by default) or sanitise with DOMPurify; add a CSP. Regression test.
4. (30m) **SSRF.**
   ```java
   byte[] fetch(String url) { return restClient.get().uri(url).retrieve().body(byte[].class); }
   ```
   - Failing test: call the import endpoint with `http://localhost:8080/actuator/env` (and the cloud metadata address `http://169.254.169.254/` in a safe local mock) and assert rejection.
   - Explain: the server can reach networks the attacker can't; user-controlled URLs turn it into a proxy.
   - Fix: allowlist hosts, resolve DNS and block private/loopback/link-local ranges, disable redirects, set timeouts. Regression tests for each blocked range.
5. (20m) **Leaked secret in git.**
   - Commit a fake key to a scratch repo, then remove it with `git filter-repo --path secrets.txt --invert-paths` (verify command in its docs).
   - Explain: deleting in a later commit does not remove it from history; anyone who cloned has it; rotation is the real fix.
   - Write a rotation checklist and add a secret scanner (e.g., gitleaks) as a pre-commit/CI step (verify tool docs).
6. (10m) Ledger drafts for all four.

**Tips and common problems**
- XSS test is flaky → await DOM updates properly; check that the handler would run (use `onerror` with a global flag, not only `alert`).
- SSRF fix is bypassed with a redirect or DNS rebinding → validate the resolved IP at connect time and disable following redirects.
- `filter-repo` rewrites history → work on a throw-away clone; never run it on a shared repo without coordination.

**Done when**
- [ ] Four attack tests exist and pass after the fix.
- [ ] The rotation checklist is written.

#### S4 Review + Write (2h): report and checklist
1. (45m) Finalise the security findings report (`docs/security/report.md`): scope, method, findings table, fixes, residual risk.
2. (30m) Write a PR review security checklist (authz on every endpoint, input validation, no secrets, safe logging, dependency changes reviewed, error responses) and add it to the PR template.
3. (15m) Flashcards: injection, XSS, SSRF, IDOR, SBOM.
4. (15m) Finish ledger extras.
5. (10m) PR.
6. (5m) Retro.

**Done when**
- [ ] The checklist is part of the PR template.

**Self-check:** Why does parameterization stop SQL injection? Why is deleting a leaked secret from git history not enough? What is an SBOM for?
<details><summary>Answers</summary>

- Parameters are sent separately from the SQL text, so input is treated as data and can't change the query structure.
- Anyone who cloned or forked already has it, caches may keep it; the secret must be rotated/revoked.
- It lists components and versions in your software so you can quickly find affected systems when a new vulnerability is published.
</details>

**Output:** SF-27 report and fixes, SBOM in CI script, PR security checklist.

---

## Week 35 — API design for failure: idempotency, timeouts, webhooks
**Outcome:** APIs and clients that behave when networks and callers misbehave.
**Before you start:** have WireMock (or a tiny upstream stub) and Toxiproxy available; ShopFlow checkout endpoint exists.
**If stuck:** log every outbound call with duration and status; most failure bugs are visible there.

#### S1 Learn (3h): idempotency, webhooks, clients and protocols
1. (40m) Read Stripe docs "Idempotent requests" and "Webhooks" (signatures, retries, ordering). Write the rules: key scope, storing the first response, same key + different payload, expiry.
2. (20m) RFC 9110 sections on method semantics (safe, idempotent) and retries. List which methods are idempotent and what that implies for client retries.
3. (25m) Google API Design Guide: pagination, filtering, versioning. Note cursor vs offset, and a deprecation approach.
4. (15m) Microsoft REST API Guidelines: error format, long-running operations, throttling (429 + `Retry-After`).
5. (30m) Spring `RestClient` and HTTP interfaces: set connect and read timeouts, connection pool basics, retry placement.
6. (25m) Resilience4j docs (Retry, RateLimiter): what to retry, backoff, jitter.
7. (20m) grpc.io "Introduction" and graphql.org "Learn": when each fits (internal service-to-service vs flexible client queries); note trade-offs.
8. (5m) Write your retry policy draft.

**Tips and common problems**
- "POST is never retryable" → it can be, with an idempotency key → write the exact conditions.
- Retrying inside every layer → multiplies traffic → decide one layer (usually the edge client) owns retries.

**Done when**
- [ ] You can state the idempotency-key algorithm in 5 steps.
- [ ] A draft retry policy table exists.

#### S2 Build (3h): SF-28 idempotent checkout and SF-30 payment webhook
1. (20m) Flyway migration:
   ```sql
   CREATE TABLE idempotency_key (
     key text NOT NULL, user_id text NOT NULL, request_hash text NOT NULL,
     status text NOT NULL, response_status int, response_body jsonb,
     created_at timestamptz NOT NULL DEFAULT now(),
     PRIMARY KEY (user_id, key));
   ```
2. (40m) Implement the checkout flow: read `Idempotency-Key`; insert a row with status `IN_PROGRESS` (unique constraint arbitrates concurrent requests); do the work; update the row with the response. Same key + same hash → return the stored response; same key + different hash → `422`; in progress → `409`.
3. (30m) Concurrency test: 20 parallel POSTs with the same key → exactly one order in the DB and 20 identical responses (or 1 created + others stored/409).
4. (20m) Webhook endpoint `/webhooks/payments`: read the **raw body bytes**, compute HMAC-SHA256 with the shared secret, compare in constant time (`MessageDigest.isEqual`), reject timestamps older than 5 minutes.
5. (30m) Dedupe: table `webhook_event(event_id primary key)`; insert first; if duplicate, return 200 and skip processing.
6. (30m) Tests: bad signature → 401; replayed old timestamp → 400; duplicate event → 200 with no second effect; valid event updates the payment.
7. (10m) PR.

**Tips and common problems**
- Signature never matches → you verified a re-serialised JSON instead of the raw body → read the body as bytes before any JSON mapping (e.g., a `@RequestBody byte[]`).
- Two parallel requests both pass the check → you "select then insert" → rely on the unique constraint and handle the violation instead.
- Key stored but the work crashed mid-way → rows stuck `IN_PROGRESS` → add a timeout/recovery rule or make the work itself atomic with the key insert.

**Done when**
- [ ] The parallel test proves one order for one key.
- [ ] Webhook tests cover signature, replay, duplicate.

#### S3 Lab (2h): RL-43 timeouts, RL-44 pool exhaustion, RL-45 retrying POST
1. (10m) Create a lab with a stub upstream (WireMock or a small controller) and a `RestClient` bean without timeouts.
2. (30m) **RL-43 missing outbound timeout.**
   ```java
   // upstream: WireMock stub with .withFixedDelay(600_000)
   String r = restClient.get().uri("http://localhost:9999/slow").retrieve().body(String.class);
   ```
   - Failing test: assert the call fails within 3 s (`assertTimeoutPreemptively`); it hangs.
   - Observe: `jcmd <pid> Thread.print` — the Tomcat thread `http-nio-8080-exec-N` is RUNNABLE/WAITING in socket read frames; repeat requests until all request threads are stuck and the app stops answering `/actuator/health`.
   - Explain: sockets have no read timeout by default, so a hung peer holds your thread forever.
   - Fix: configure connect/read timeouts (e.g., build the `RestClient` with `JdkClientHttpRequestFactory` and a `HttpClient` with `connectTimeout`, plus read timeout; or Boot's HTTP client properties; verify names for your Boot version). Regression: call fails fast with a clear exception.
3. (30m) **RL-44 HTTP connection-pool exhaustion.**
   - Configure Apache HttpClient with `maxConnTotal=5` (or similar); upstream replies in 2 s; fire 50 concurrent calls.
   - Observe: errors like "Timeout waiting for connection from pool"; metrics show leased = max and pending > 0.
   - Explain: slow upstream holds connections; callers queue for a pool slot.
   - Fix: timeouts shorter than the slow response, bulkhead concurrency per dependency (Resilience4j `Bulkhead`), right-size pool. Regression test with pool metrics.
4. (35m) **RL-45 retrying a non-idempotent POST.**
   ```java
   retryTemplate.execute(ctx -> restClient.post().uri("/payments").body(charge).retrieve().toBodilessEntity());
   ```
   - Stub the upstream to apply the charge and then time out the response; count charges on the stub.
   - Failing test: assert exactly one charge; observe 2–3.
   - Explain: after a timeout you don't know whether the server acted; retrying a non-idempotent call can duplicate effects.
   - Fix: send an `Idempotency-Key` per logical payment (same on each retry) and make the upstream honour it; retry only keyed or idempotent calls. Regression: one charge recorded.
5. (15m) Ledger entries for all three.

**Tips and common problems**
- Timeout set on the wrong layer (e.g., only connect) → read timeout still missing → set both and an overall deadline.
- WireMock delay doesn't hold → use `withFixedDelay` or a `ChunkedDribbleDelay` and confirm in the admin UI.
- Retry also triggers on 4xx → restrict retry to timeouts/5xx/connection errors.

**Done when**
- [ ] Each lab has failing and passing tests.
- [ ] The ledger includes the thread-dump excerpt for RL-43.

#### S4 Review + Write (2h): ADR-015
1. (40m) Write ADR-015 "Idempotency and retry policy: who retries, what, how often" with a table (operation, idempotent?, key required?, max attempts, backoff+jitter, timeout).
2. (30m) Finish ledger entries; practise aloud.
3. (15m) Flashcards: idempotent methods, 409 vs 422, why raw body for HMAC.
4. (20m) Update the API conventions (ADR-004) with idempotency, 429 and `Retry-After`.
5. (10m) PR.
6. (5m) Retro.

**Done when**
- [ ] ADR-015 merged and linked from ADR-004.

**Self-check:** How does an idempotency key survive a client crash? Why do webhooks need replay protection? Why must every outbound call have a timeout?
<details><summary>Answers</summary>

- The client persists or deterministically regenerates the key per logical operation and sends the same key on retry; the server stores the first result keyed by it.
- Anyone who captures a signed request can resend it; timestamps and event-ID dedupe prevent re-processing.
- Without a timeout a hung dependency holds threads/connections indefinitely and cascades into an outage.
</details>

**Output:** SF-28, SF-30, ADR-015, RL-43/44/45 entries.

---

## Week 36 — Kafka fundamentals and the outbox
**Outcome:** understands Kafka's model and ships events without the dual-write bug.
**Before you start:** Docker has at least 4 GB RAM; `kcat` or Kafka CLI tools are available (inside the broker container are fine).
**If stuck:** use `kafka-topics --describe` and `kafka-consumer-groups --describe` first; most issues are visible there.

#### S1 Learn (3h): Kafka design and the outbox pattern
1. (45m) Kafka documentation "Introduction" and "Design": topics, partitions, offsets, replication, retention and compaction, producer acks, consumer groups. Draw a 3-partition topic with 2 consumers and label offsets.
2. (45m) *Kafka: The Definitive Guide* (2nd ed.), producers and consumers chapters: batching, `acks`, `enable.idempotence`, partitioner and keys, commit strategies.
3. (30m) A Confluent Developer course intro (developer.confluent.io): hands-on feel for the CLI tools.
4. (30m) DDIA ch. 11 (log-based messaging): ordering, replay, why logs vs queues. Write: what ordering does Kafka guarantee.
5. (15m) microservices.io "Transactional outbox": list the failure it removes and what duplicates it still allows.
6. (15m) Spring for Apache Kafka reference: `KafkaTemplate`, `@KafkaListener`, ack modes.

**Tips and common problems**
- Believing Kafka orders globally → only within a partition → choose keys deliberately (e.g., `orderId`).
- Treating "exactly-once" as a free feature → it's scoped (producer idempotence/transactions) and doesn't cover external side effects → design consumers to be idempotent anyway.

**Done when**
- [ ] A labelled diagram of partitions, offsets and a consumer group exists.
- [ ] You can state the ordering guarantee and the outbox's purpose.

#### S2 Build (3h): SF-29 Kafka order events with outbox
1. (15m) Add Kafka (KRaft) to Compose (verify image and env names against current Kafka docs; advertised listeners must be reachable from both host and containers):
   ```yaml
   kafka:
     image: apache/kafka:latest
     ports: ["9092:9092"]
     environment:
       KAFKA_NODE_ID: 1
       KAFKA_PROCESS_ROLES: broker,controller
       KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
       KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
       KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
       KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
       KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
       KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
   ```
2. (20m) Migration for the outbox: `outbox(id bigserial primary key, aggregate_id text, type text, payload jsonb, created_at timestamptz default now(), published_at timestamptz)`.
3. (30m) In `PlaceOrderService` (`@Transactional`) save the order and insert an `OrderPlaced` outbox row in the same transaction.
4. (45m) Relay: a scheduled job that selects unsent rows `ORDER BY id LIMIT 100 FOR UPDATE SKIP LOCKED`, publishes with `kafkaTemplate.send(topic, aggregateId, payload).get()`, then sets `published_at`. Producer config: `acks=all`, `enable.idempotence=true`, key = `aggregate_id`.
5. (35m) Consumer module: `@KafkaListener` updates a read model; record handled event IDs in `processed_event` (unique) inside the same DB transaction as the effect.
6. (25m) Crash test: stop the relay between `send` and the DB update (e.g., throw a test-only exception); restart; verify the event is delivered again but the consumer applies it once.
7. (10m) PR.

**Tips and common problems**
- Consumer on the host can't connect or gets weird hostnames → advertised listeners point to a name the client can't resolve → use two listeners (internal and external) or `localhost` when everything runs on the host.
- Topic missing → auto-create is off → create it: `docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic orders --partitions 3` (verify path).
- Relay publishes in a loop forever → `published_at` update isn't committed → check transaction boundaries.
- Re-using a consumer group id between experiments → old committed offsets make the consumer skip messages → use a fresh group id or reset offsets with `kafka-consumer-groups --reset-offsets`.

**Done when**
- [ ] Placing an order produces exactly one outbox row and one Kafka event.
- [ ] The crash test shows redelivery with no duplicate effect.

#### S3 Lab (2h): RL-85 dual-write, RL-86 duplicate delivery
1. (10m) Set up a lab service with an `orders` table, a `KafkaTemplate`, and a flag to simulate a crash.
2. (45m) **RL-85 dual-write problem.**
   ```java
   @Transactional void place(Order o) { repo.save(o); }       // commit
   void handle(Order o) { place(o); if (crash) Runtime.getRuntime().halt(1); kafka.send("orders", o.id(), json(o)); }
   ```
   - Failing test/script: place an order with `crash=true`, then assert the event exists on the topic.
   - Observe: row in Postgres (`select * from orders`) but nothing from `kafka-console-consumer.sh --topic orders --from-beginning`. Also try the reverse order (send first, DB fails) to see a ghost event.
   - Explain: two systems can't be updated atomically without a protocol; a crash between writes leaves them inconsistent.
   - Fix: outbox — write the event in the same DB transaction and publish asynchronously. Regression: repeat the crash scenario and assert the event is eventually published.
3. (45m) **RL-86 duplicate delivery.**
   ```java
   @KafkaListener(topics="orders", groupId="lab") void on(String msg, Acknowledgment ack) { apply(msg); if (crash) halt(1); ack.acknowledge(); }
   ```
   (`enable.auto.commit=false`, `ack-mode=manual`)
   - Failing test: after restart, assert the effect applied exactly once.
   - Observe: `kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group lab` — `CURRENT-OFFSET` didn't advance, `LAG` > 0 after the crash; on restart the same message is processed again.
   - Explain: at-least-once delivery — commit happens after processing, so a crash in between redelivers.
   - Fix: idempotent consumer — unique `processed_event(event_id)` inserted in the same transaction as the side effect (or business-level idempotency). Regression test: send the message twice, assert one effect.
4. (20m) Ledger entries for both with the CLI output pasted.

**Tips and common problems**
- Offsets look stuck → you're describing a different group id → confirm the `groupId` in code matches the CLI.
- Crash simulation is flaky → `halt(1)` prevents shutdown hooks (good) but must run after the DB commit and before ack → place it carefully and verify with logs.
- Messages "disappear" on restart → the consumer starts at `latest` for a new group → set `auto.offset.reset=earliest` for the lab.

**Done when**
- [ ] Both labs have a reproduced failure with CLI evidence and a passing regression test.

#### S4 Review + Write (2h): ADR-016 and flow diagram
1. (35m) Write ADR-016 "Outbox, not dual-write": context, options (dual write, 2PC, outbox + relay, CDC with Debezium), decision, consequences (at-least-once, relay lag, cleanup of old rows).
2. (30m) Draw the outbox flow with failure points numbered (crash after commit, relay crash after send, consumer crash before commit) and what each yields.
3. (25m) Finish ledger entries and rehearse aloud.
4. (15m) Flashcards: `acks`, idempotent producer, partition key, offset commit, at-least-once.
5. (10m) PR.
6. (5m) Retro.

**Done when**
- [ ] ADR-016 merged and the diagram is in `docs/`.

**Self-check:** What ordering does Kafka guarantee? Why isn't "exactly-once" free? What failure does the outbox remove?
<details><summary>Answers</summary>

- Order is guaranteed within a single partition (so use a consistent key); not across partitions.
- Exactly-once semantics apply to Kafka-to-Kafka processing with transactions/idempotence; external side effects still need idempotent handling, and it adds complexity/latency.
- The dual-write inconsistency: the DB change and the event are committed atomically in one transaction.
</details>

**Output:** SF-29, ADR-016, RL-85/86 entries.

---

## Week 37 — Kafka failure modes, background work and real-time updates
**Outcome:** operates consumers safely and handles background jobs and live updates.
**Before you start:** the outbox flow from Week 36 works; Postgres has a `jobs` table for RL-76 (create it in the lab).
**If stuck:** look at consumer group state (`--describe --state`) and the consumer's poll logs before changing configs.

#### S1 Learn (3h): error handling, batch, scheduling, SSE
1. (50m) Spring Kafka reference "Error Handling" and "Non-Blocking Retries" (`@RetryableTopic`, `DefaultErrorHandler`, `DeadLetterPublishingRecoverer`, `ErrorHandlingDeserializer`). Write the decision: what is retried inline, what goes to retry topics, what goes to the DLT.
2. (30m) Kafka consumer configs: `max.poll.interval.ms`, `max.poll.records`, `session.timeout.ms`, `heartbeat.interval.ms`. Write the causes of a rebalance (join/leave, missed poll, session timeout).
3. (30m) Spring Batch reference (introduction): job, step, chunk, reader/processor/writer, restartability. Write when you'd use it vs a simple scheduled job.
4. (20m) ShedLock README: why `@Scheduled` runs on every instance and how a lock fixes it.
5. (25m) MDN "Server-sent events" and WebSocket overview: compare one-way push vs bidirectional; proxy/timeouts; reconnection with `EventSource`.
6. (15m) SLF4J MDC manual: what MDC is, and why executor threads don't inherit it.
7. (10m) Write the "failure modes" cheat sheet.

**Tips and common problems**
- Using unlimited inline retries → blocks the partition → use retry topics with delays for non-blocking retries.
- "Our DLQ is full of junk" → no replay/tooling → plan the replay tool this week (see S2).

**Done when**
- [ ] A cheat sheet lists each failure mode with its signal and its fix.

#### S2 Build (3h): notifications consumer with retries, DLQ, replay and SSE
1. (25m) Notifications consumer using non-blocking retries:
   ```java
   @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2.0))
   @KafkaListener(topics = "orders") void on(OrderPlaced e) { notifier.send(e); }
   @DltHandler void dlt(OrderPlaced e, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String why) { log.error("DLT {} {}", e, why); }
   ```
2. (30m) Test: make `notifier` fail twice then succeed → message processed after retries; make it always fail → lands in the DLT with exception headers.
3. (40m) Replay tool: a small CLI/script that reads from the DLT, filters by id/time, republishes to the original topic (with a header `x-replayed=true`), and records what it did. Keep it idempotent-safe.
4. (30m) SSE endpoint: `GET /orders/{id}/events` returning `SseEmitter` (with timeout and heartbeat).
5. (30m) Bridge: a registry of emitters by order id; the Kafka consumer pushes status updates to the registered emitters (note: in-memory only works on a single instance — write the limitation).
6. (15m) React: use `EventSource` to show live order status; handle reconnect.
7. (10m) PR.

**Tips and common problems**
- Everything lands in the DLT immediately → deserialization errors → wrap deserializers with `ErrorHandlingDeserializer` and configure the delegate.
- SSE disconnects after ~30–60 s → proxy/idle timeout → send periodic comments (`: ping`) and set the right timeouts.
- SSE emitters leak → not removed on completion/timeout/error → register `onCompletion`, `onTimeout`, `onError` to remove.

**Done when**
- [ ] Retry-then-success and retry-then-DLT tests pass.
- [ ] Replay moves a DLT message back and it is processed.
- [ ] Live status updates appear in the UI.

#### S3 Lab (2h): RL-87 rebalance, RL-88 lag, RL-89 poison message, RL-76 SKIP LOCKED, RL-23 MDC
1. (5m) Lab setup: a Spring Boot app with a Kafka listener; scripts to produce N messages: `kafka-console-producer.sh --bootstrap-server localhost:9092 --topic lab < messages.txt`.
2. (20m) **RL-87 rebalance storm.**
   ```java
   // spring.kafka.consumer.properties.max.poll.interval.ms=5000
   @KafkaListener(topics="lab", groupId="g87") void on(String m) throws Exception { Thread.sleep(8000); }
   ```
   - Failing test/script: all messages processed once without the consumer leaving the group (it doesn't).
   - Observe: log line "consumer poll timeout has expired" / "Member ... sending LeaveGroup"; `kafka-consumer-groups.sh --describe --group g87 --state` showing `PreparingRebalance`/`CompletingRebalance` repeatedly; duplicates appear.
   - Explain: processing time between polls exceeded `max.poll.interval.ms`, so the broker assumed the consumer died and rebalanced.
   - Fix: lower `max.poll.records`, raise the interval to fit worst-case processing, or offload work with manual ack/pause-resume. Regression: stable group state.
3. (15m) **RL-88 lag growth.**
   - Produce ~100 msg/s (`kafka-producer-perf-test.sh` with `--throughput 100`, verify tool name) to a consumer that handles ~20 msg/s.
   - Observe: `kafka-consumer-groups.sh --describe --group g88` — `LAG` rising each run; Micrometer `kafka.consumer.fetch.manager.records.lag.max` (verify metric name).
   - Explain: arrival rate exceeds service rate; lag is the queue length.
   - Fix: scale consumers up to partition count, speed processing, add partitions (keys!). Alert on lag growth rate, not just absolute lag.
4. (20m) **RL-89 poison message.**
   - Produce `not-json` to a topic where the consumer deserialises JSON.
   - Failing test: valid messages after the bad one are still processed. Observe the partition stuck: same offset retried, `LAG` constant, logs repeating the same exception.
   - Explain: with default error handling the failing record blocks the partition.
   - Fix: `ErrorHandlingDeserializer` + `DefaultErrorHandler` with `DeadLetterPublishingRecoverer` (and a bounded `BackOff`). Regression: the bad record goes to the `.DLT` topic and later messages flow.
5. (25m) **RL-76 `FOR UPDATE SKIP LOCKED` queue.**
   ```sql
   SELECT id FROM jobs WHERE status='NEW' ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED;
   ```
   - Failing test: 4 worker threads each claim jobs; assert no job processed twice and all processed (naive `SELECT ... LIMIT 1` then `UPDATE` gives duplicates; plain `FOR UPDATE` serialises workers).
   - Observe: with `SKIP LOCKED` workers claim different rows concurrently (check `pg_locks`, throughput).
   - Explain: `SKIP LOCKED` lets a worker skip rows other transactions have locked.
   - Fix/regression: use it in a claim transaction; keep the transaction open only while marking `IN_PROGRESS` (then process outside), with a timeout/reaper for dead workers.
6. (20m) **RL-23 MDC lost across threads.**
   ```java
   MDC.put("traceId", "abc");
   executor.submit(() -> log.info("work"));   // log line has no traceId
   ```
   - Failing test: capture log output (Logback `ListAppender`) and assert the `traceId` is present.
   - Explain: MDC is thread-local; pool threads don't inherit it.
   - Fix: a `TaskDecorator` (Spring `ThreadPoolTaskExecutor.setTaskDecorator`) that copies `MDC.getCopyOfContextMap()` into the task and restores/clears after.
7. (15m) Ledger entries (five).

**Tips and common problems**
- Group stays in `Empty` → no active consumers (app crashed) or wrong group id → check app logs first.
- `kafka-consumer-groups.sh` shows no lag but app seems stuck → the consumer is paused/long-processing and committed earlier offsets → look at the poll loop timing.
- SKIP LOCKED test passes accidentally → too few jobs/workers → use 1000 jobs and 8 workers and verify duplicates via a unique processed table.
- Reusing a group id leaves committed offsets → reset with `--reset-offsets --to-earliest --execute` while consumers are stopped.

**Done when**
- [ ] Each of the five items has an observed failure and a regression test or measurable result.
- [ ] You can recite the rebalance causes.

#### S4 Review + Write (2h): runbook and consolidation
1. (40m) Write the runbook "consumer lag is growing": symptom, dashboards, checks (`--describe`, processing time, errors, rebalances), mitigations (scale, pause, skip with DLT), escalation.
2. (40m) Finish five ledger entries; rehearse aloud.
3. (15m) Flashcards: rebalance causes, DLT vs retry topic, SKIP LOCKED, MDC.
4. (15m) Draw the retry-topic flow (main → retry-1 → retry-2 → DLT).
5. (5m) PR.
6. (5m) Retro.

**Done when**
- [ ] Runbook and diagram are in `docs/runbooks/`.

**Self-check:** What triggers a rebalance? How do you handle a message that always fails? Why does `@Scheduled` run on every instance?
<details><summary>Answers</summary>

- Members joining/leaving, missing `max.poll.interval.ms` or session timeout, partition changes.
- Bounded retries (ideally non-blocking), then route to a dead-letter topic with context, alert on it and provide a replay tool after fixing the cause.
- Each application instance has its own scheduler; without a distributed lock all instances execute the job.
</details>

**Output:** notifications consumer, DLQ replay, SSE, runbook, RL-23/76/87/88/89 entries.

---

## Week 38 — Caching and Phase 6 gate
**Outcome:** caches safely; passes the gate.
**Before you start:** Redis in Compose (`redis:7`); the catalog read endpoint has a k6 baseline from Week 31.
**If stuck:** measure first (hit ratio, latency), then change one thing at a time.

#### S1 Learn (3h): caching theory and tools
1. (35m) Spring Framework reference "Cache Abstraction": `@Cacheable`, `@CacheEvict`, `@CachePut`, key generation, conditions, proxy limits.
2. (30m) Redis docs on caching patterns: cache-aside, TTLs, eviction policies.
3. (30m) Stampede strategies: request coalescing (single flight), probabilistic early refresh, lock with timeout, TTL jitter. Write which you'd use for ShopFlow's catalog.
4. (30m) MDN "HTTP caching": `Cache-Control`, `ETag`, `If-None-Match`, `304`, `stale-while-revalidate`.
5. (15m) DDIA ch. 1 (scalability and load parameters) for what to measure.
6. (30m) List ShopFlow reads: cacheability, staleness tolerance and invalidation trigger for each.
7. (10m) Flashcards.

**Tips and common problems**
- Caching everything → stale data bugs → cache only hot, read-mostly data; define max staleness per item.
- Putting entities in the cache → serialization and lazy-load problems → cache DTOs.

**Done when**
- [ ] A table of what to cache, TTL, invalidation trigger exists.

#### S2 Build (3h): Redis cache, invalidation, ETag
1. (15m) Add `spring-boot-starter-data-redis` and `spring-boot-starter-cache`; add Redis to Compose; `@EnableCaching`.
2. (30m) Cache catalog reads: `@Cacheable(cacheNames="product", key="#id")` on the service method; configure `RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10))` with a JSON serializer for DTOs.
3. (30m) Invalidate on change: `@CacheEvict` on update/delete, plus evict on the `ProductUpdated` Kafka event so other instances evict as well.
4. (25m) HTTP caching: return `ETag` (e.g., `ShallowEtagHeaderFilter` or `ResponseEntity.eTag`) and `Cache-Control: max-age=60`; test that `If-None-Match` returns `304`.
5. (40m) k6 before/after: rerun the catalog scenario (open model) and compare p95/p99 and DB queries per request.
6. (30m) Expose hit/miss metrics (`cache.gets{result=hit|miss}` via Micrometer, verify names) and build a small dashboard/query.
7. (10m) PR.

**Tips and common problems**
- Cache seems unused → call is a self-invocation or the method is private → the proxy never intercepts; move it to another bean.
- `ClassCastException`/deserialization errors after deploy → cache stored an old class shape → version cache keys or flush on deploy.
- Local tests pass because the cache is a no-op → a `NoOpCacheManager` or missing `@EnableCaching` → assert via hit metrics.

**Done when**
- [ ] k6 shows lower p95 and fewer DB queries.
- [ ] Update → read returns fresh data (test).
- [ ] `304` verified.

#### S3 Lab (2h): RL-36 `@Cacheable` bypass, RL-90 stampede, RL-91 stale invalidation
1. (10m) Create a lab service with a `ProductService`, Redis, and a slow repository (`sleep(200)` per DB call).
2. (25m) **RL-36 `@Cacheable` bypassed by self-invocation.**
   ```java
   @Service class ProductService {
     public Product details(long id) { return load(id); }          // self-invocation
     @Cacheable("product") public Product load(long id) { return repo.find(id); }
   }
   ```
   - Failing test: call `details(1)` twice and assert the repository was hit once (it is hit twice).
   - Observe: repository counter, cache metrics showing no gets.
   - Explain: Spring caches via a proxy; calls from inside the class don't pass through it.
   - Fix: move `load` to another bean (or inject self, or use AspectJ mode). Regression: counter is 1.
3. (40m) **RL-90 stampede on hot key expiry.**
   - Set TTL to 2 s; use k6 or a thread pool to send 200 concurrent requests for the same key right after expiry.
   - Failing test: assert the repository is called once per expiry (it's called many times).
   - Observe: DB call counter/spike; latency spike at the expiry moments; `pg_stat_activity` showing many identical queries.
   - Explain: when the key expires, every concurrent miss recomputes.
   - Fix: `@Cacheable(sync = true)` (per-instance) or a Redis lock/single-flight across instances; add TTL jitter; optionally refresh-ahead. Regression: one load per expiry per instance.
4. (30m) **RL-91 stale cache after update path forgot eviction.**
   - Add a second update path (e.g., bulk price update via SQL or an admin endpoint) that doesn't evict.
   - Failing test: update price, then read; assert the new price (old one is returned).
   - Explain: caches must be invalidated by every write path; missed paths cause stale data until TTL.
   - Fix: centralise writes behind one service, evict on domain events, set sensible TTLs as a safety net. Regression test for each write path.
5. (15m) Ledger entries (three).

**Tips and common problems**
- `sync=true` doesn't help across instances → it only locks per JVM → use a distributed lock or accept N loads (N = instances).
- Redis TTL seems ignored → serializer or cache config not applied to the named cache → check per-cache configuration.
- Race: evict happens before DB commit and a read repopulates the old value → evict after commit (`@TransactionalEventListener(phase=AFTER_COMMIT)`).

**Done when**
- [ ] All three labs have failing and passing tests with counters as evidence.

#### S4 Review + Write (2h): Phase 6 gate
1. (35m) Self-run pentest checklist: go through your OWASP table against ShopFlow; for each item show the test or scan result; record gaps.
2. (30m) Replay test: script sending the same checkout request, the same webhook, and the same Kafka message three times each (`kafka-console-producer.sh` for the message). Assert in the DB: exactly one order, one payment effect, one notification.
3. (30m) Crash test: kill the outbox relay (and separately the consumer) mid-flow with `docker compose kill`; restart; assert no lost and no double-applied events.
4. (15m) Write gate notes and fill the tracker; tag `phase-6`.
5. (10m) Finish ledger and retro.

**Gate rubric (pass all):**
- Self-run pentest checklist (OWASP Top 10 against ShopFlow) with fixes.
- Replay test: replay the same checkout request, webhook and Kafka message 3 times; exactly one effect.
- Kill the outbox relay and consumer mid-flow; no loss, no double apply.
- Ledger has RL-23/36/43–45/76/85–91 explained aloud.

**Tips and common problems**
- Replay test flaky → eventual consistency; poll with a timeout instead of fixed sleeps.
- Crash test leaves partial state from earlier runs → reset DB and topics between runs (`docker compose down -v`).

**Done when**
- [ ] The three gate checks are scripted and repeatable.
- [ ] `phase-6` tag created; tracker updated.

**Self-check:** Which write paths must evict a cache entry? Why can `@Cacheable(sync=true)` be insufficient? What does a 304 save?
<details><summary>Answers</summary>

- Every path that changes the underlying data (API, batch, admin tools, events from other services), or the cache needs a TTL safety net.
- It only coordinates within one JVM; with multiple instances each can still recompute on a miss.
- The response body transfer and, usually, server-side rendering/serialisation work; the client reuses its cached copy.
</details>

**Output:** `phase-6` tag, ADRs 014–016.
