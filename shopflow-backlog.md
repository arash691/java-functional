# ShopFlow Backlog

ShopFlow is an e-commerce / order-management system. It starts as a CLI, becomes a Spring Boot monolith with a React client, then a few cloud-native services. Work each ticket on a branch, open a PR, review it yourself (or with a peer), and record decisions as ADRs.

Format: **ID — Title** · Phase · Acceptance criteria

## Phase 1 — Java
- **SF-01 Inventory core (CLI)** · P1 · Add/remove/query stock; unit tests ≥80%; no static mutable state
- **SF-02 Money type** · P1 · `Money` value object with `BigDecimal` and currency; rounding rules tested; no `double` anywhere
- **SF-03 Product catalog model** · P1 · Records/enums for product, category, price; `equals`/`hashCode` tests
- **SF-04 Persistence to file** · P1 · Save/load with NIO; handles corrupt file with a clear exception
- **SF-05 Order timestamps** · P1 · Store UTC, display in user zone; DST test cases

## Phase 2 — Spring Boot slice
- **SF-06 Bootstrap service** · P2 · Boot 3, Postgres via Docker Compose, Flyway V1, health endpoint
- **SF-07 Catalog API** · P2 · CRUD + validation + RFC 7807 errors; OpenAPI spec written first
- **SF-08 Cart API** · P2 · Add/remove/update items; DTO mapping with MapStruct
- **SF-09 Orders API** · P2 · Create order from cart; status lifecycle
- **SF-10 TS client + React UI** · P2 · Generated client; product list, cart, order pages
- **SF-11 Tests** · P2 · Slice tests + Testcontainers integration; CI-green locally
- **SF-12 One-command run** · P2 · `docker compose up` brings up DB, API and UI

## Phase 3 — Design
- **SF-13 Pricing strategies** · P3 · Strategy for discounts; adding a rule needs no `if` chain edits
- **SF-14 Order state machine** · P3 · State pattern; illegal transitions rejected; tests per transition
- **SF-15 Payment adapter port** · P3 · Port/adapter with a fake gateway
- **SF-16 Domain events** · P3 · `OrderPlaced` event with in-process listeners
- **SF-17 Hexagonal refactor** · P3 · Domain has no Spring/JPA imports; ArchUnit rule enforces it

## Phase 4 — Data
- **SF-18 Inventory reservation** · P4 · No overselling under 100 concurrent requests (test proves it)
- **SF-19 Order history queries** · P4 · Keyset pagination, indexed; `EXPLAIN` attached to the PR
- **SF-20 5M-row seed and slow query fix** · P4 · Before/after timings and plan documented
- **SF-21 Catalog search** · P4 · Elasticsearch index with sync strategy and a stale-data note
- **SF-22 Zero-downtime migration** · P4 · Rename a column via expand/contract in 3 releases

## Phase 5 — JVM
- **SF-23 Rate limiter** · P5 · Token bucket, thread-safe, benchmarked
- **SF-24 Bulk import** · P5 · 1M-row CSV import with bounded memory and backpressure
- **SF-25 Load test baseline** · P5 · k6 script, p50/p95/p99 report committed

## Phase 6 — Security and messaging
- **SF-26 Auth with Keycloak** · P6 · OIDC login; roles for customer/admin; method-level security
- **SF-27 Security review** · P6 · ZAP scan report and fixes for findings
- **SF-28 Idempotent checkout** · P6 · Idempotency key; double-submit creates one order
- **SF-29 Kafka order events with outbox** · P6 · No lost or duplicate events after a crash test
- **SF-30 Payment webhook** · P6 · Signature verification, replay safe

## Phase 7 — Distributed
- **SF-31 Extract Inventory service** · P7 · Own DB, API contract, ADR on why
- **SF-32 Extract Payments service** · P7 · Timeouts, retries with jitter, circuit breaker
- **SF-33 Checkout saga** · P7 · Compensation on payment failure; chaos test with Toxiproxy
- **SF-34 Notifications service** · P7 · Idempotent consumer, DLQ with replay tooling
- **SF-35 Design docs** · P7 · Three system design docs (feed, payments, search) with capacity estimates

## Phase 8 — Delivery and ops
- **SF-36 Container images** · P8 · Non-root, scanned, <250 MB, graceful shutdown verified
- **SF-37 CI pipeline** · P8 · Build, test, coverage gate, vulnerability scan, publish
- **SF-38 Kubernetes manifests/Helm** · P8 · Probes, limits, HPA, config and secrets
- **SF-39 GitOps deploy** · P8 · ArgoCD syncs from repo; canary rollout with automatic rollback
- **SF-40 Observability** · P8 · Metrics, traces, logs correlated by trace ID; 3 SLO alerts with runbooks
- **SF-41 Terraform + cloud** · P8 · Reproducible environment, cost estimate documented
- **SF-42 DB failover and restore drill** · P8 · Measured RTO/RPO written up

## Phase 9 — Reality
- **SF-43 Legacy upgrade** · P9 · Boot 2 → 3 on a provided legacy sample with characterization tests
- **SF-44 OSS contribution** · P9 · One merged or submitted PR with context
- **SF-45 Game-day** · P9 · Three injected faults found from telemetry; postmortem written
