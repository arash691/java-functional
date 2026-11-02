# Frontend Developer → Senior Java Backend Engineer

**Pace:** ~10 h/week · **Length:** ~60 weeks · **Spine project:** ShopFlow (see `shopflow-backlog.md`) · **Bridge track:** Reality Lab (see `reality-lab/`)

Topic coverage is cross-checked against the AlgoMaster Java, Spring, Backend, SQL, DevOps and Kubernetes roadmaps. This plan adds what they lack: sequencing, gates, JVM and concurrency depth, OOD, FP, distributed systems, architecture, operations, and the theory-to-practice gap.

## Detailed lesson plans
Session-by-session plans with reading references live in `lessons/` (start with `lessons/README.md`). Each week has four sessions: Learn (3 h), Build (3 h), Lab (2 h), Review + Write (2 h). The checklists below are the summary; the lesson files are the source of truth.

## How to use this document
1. Each week: tick the checklist, do 2 Reality Lab items, work ShopFlow tickets.
2. Every 4th week is a consolidation + gate week. No new material.
3. A phase is finished only when its **Gate** is passed and reviewed by a mentor or peer against the rubric.
4. Skip rule: if she passes the Phase 0 entry quiz for a topic, skip it.

## Weekly rhythm (10 h)
| Time | Activity |
|---|---|
| 3 h | Learn (book, course, docs) |
| 3 h | Build on ShopFlow |
| 2 h | Reality Lab: reproduce 2 catalog items, write ledger entries |
| 1 h | Review: flashcards, refactor, read others' code |
| 1 h | Write: ADR, ledger entry, blog draft |

Monthly: 60-minute mentor review, one code-review swap, one public postmortem read, and a mock interview (5 random ledger entries + 1 system design).

## Timeline
| Phase | Weeks | Theme | Gate |
|---|---|---|---|
| 0 | 1–2 | Bridge and setup | Draw a request's life, browser → DB |
| 1 | 3–9 | Java language, test-first | Idiomatic code review |
| 1.5 | M1–M8 | Modeling: classes, OOD, domain concepts (party, money, time, history), 10 practical domains, data modeling, ERD | Timed model of an unseen brief: class diagram + ERD + DDL |
| 2 | 10–15 | First Spring Boot vertical slice + React client | One-command run by a teammate |
| 3 | 16–20 | OOD, patterns, FP, clean architecture | Smelly module refactor with ADRs |
| 4 | 21–27 | Databases in depth | Fix a lost update three ways |
| 5 | 28–32 | JVM and concurrency | Diagnose labs from dumps |
| 6 | 33–38 | Security, APIs, messaging | Pentest checklist + replay test |
| 7 | 39–46 | Distributed systems, architecture | Mock system design + design doc |
| 8 | 47–55 | Cloud-native delivery and operations | PR → prod in <15 min with rollback |
| 9 | 56–60 | Real-world engineering, senior behaviours | Game-day + senior checklist |

---

## Phase 0 — Bridge and setup (weeks 1–2)
**Goal:** working environment, backend mental model, ShopFlow defined.

**Week 1**
- [ ] Entry quiz: HTTP, TCP/DNS/TLS, Git, Linux shell, async model (skip what you pass)
- [ ] Install JDK 25 LTS (SDKMAN; 21 also works), IntelliJ, Docker, Postgres container
- [ ] Create the GitHub repo with branch protection and PR template
- [ ] Map Node concepts to JVM: event loop vs threads, npm vs Maven

**Week 2**
- [ ] Read the ShopFlow brief, copy the first 10 tickets to a board (requirements and glossary come after the modeling block)
- [ ] Linux shell basics: processes, files, permissions, `curl`, `ss`, `grep`
- [ ] Trace a request with browser devtools, `curl -v`, and DNS lookup
- [ ] Reality Lab setup: create the `reality-lab` repo from `reality-lab/` template

**Gate:** whiteboard a request from browser to DB and back, naming each hop and failure point.

## Phase 1 — Java language, test-first (weeks 3–9)
**Goal:** idiomatic Java with tests, not TypeScript in Java.

- **W3:** syntax, types, control flow, methods; Maven project; first JUnit 5 test
- **W4:** classes, encapsulation, constructors, static/final, `toString`/`equals`/`hashCode`; records
- **W5:** inheritance, interfaces, abstract classes, polymorphism, enums, sealed types
- **W6:** collections (List/Set/Map/Queue), complexity, iterators, generics, `Comparable` vs `Comparator`
- **W7:** exceptions (checked vs unchecked, strategy), try-with-resources, I/O and NIO, strings and `StringBuilder`
- **W8:** money and time: `BigDecimal`, `java.time`, UTC, time zones; immutability
- **W9:** consolidation + gate. Logging with SLF4J/Logback, debugger, AssertJ

**Daily drill:** Exercism Java track; ~40 LeetCode problems on core patterns over the phase.
**Build:** CLI inventory engine for ShopFlow, test-first.
**Reality Lab focus:** category D (JS→Java gotchas), `equals`/`hashCode` bugs, floating point, `Integer` cache.
**Gate:** idiomatic code review with no "this is TypeScript" comments; ≥80% meaningful coverage.

## Phase 1.5 — Modeling studio (weeks M1–M8, between weeks 9 and 10)
**Goal:** learn to model before building: classes first, then OOD, then the concepts every business system shares (people, roles, organizations, money, time, history, transactions), then practice on 10 practical domains, then data modeling. Work from `modeling-lab/DOMAINS.md`, score with `modeling-lab/RUBRIC.md`. Lessons: `lessons/modeling/`.

- **M1:** model classes by practice, no theory first (small warm-ups: Library, Parking lot)
- **M2:** learn OOD: encapsulation, composition, SRP/OCP/DIP, value vs entity, UML class diagrams; refactor your M1 models
- **M3:** domain concepts I: person vs user vs employee vs customer, party, role as a relationship over time, organization and org units, membership, labor
- **M4:** domain concepts II: money, quantity, time and intervals, temporality (valid time vs record time), history and audit, business transactions, ledger entries, reversal
- **M5:** model 5 practical domains as classes: retail catalog and pricing, orders/payments/refunds, bank accounts, double-entry ledger, booking
- **M6:** model 5 more: subscription billing, HR and payroll, identity/access/organizations, inventory, insurance policy; pattern cheat sheet; timed checkpoint
- **M7:** relational and data modeling: PostgreSQL, SQL basics, keys, constraints, ERD, normalization, effective-dated and append-only tables, class-to-table mapping, database transaction basics
- **M8:** model the data for the domains and the ShopFlow capstone; gate on an unseen brief
**Gate:** 60-minute timed model of a new brief (class diagram + ERD + DDL), at least 13/16 on both rubrics, plus a recorded walkthrough.

## Phase 2 — First vertical slice with Spring Boot (weeks 10–15)
**Goal:** a working, tested, containerized REST service with a React client.

- **W10:** Spring Initializr, IoC/DI, beans, config, profiles
- **W11:** Spring MVC REST, validation, RFC 7807 errors, DTOs (MapStruct)
- **W12:** Postgres, Flyway, JPA entities and relationships, repositories
- **W13:** OpenAPI contract-first; generate the TypeScript client; build the React page
- **W14:** tests: `@WebMvcTest`, `@DataJpaTest`, Testcontainers
- **W15:** consolidation + gate; Docker Compose for the full slice

**Build:** Catalog + Cart + Orders monolith with UI.
**Reality Lab focus:** categories A and B (proxies, `@Transactional`, N+1, OSIV, Jackson, timeouts).
**Gate:** teammate clones, runs one command, exercises the full slice.

## Phase 3 — OOD, patterns, FP, clean architecture (weeks 16–20)
- **W16:** SOLID, coupling and cohesion, UML class/sequence diagrams
- **W17:** patterns in ShopFlow: Strategy (pricing), State (order), Adapter (payment), Observer (domain events), Builder, Factory, Decorator
- **W18:** FP: streams, `Optional`, immutability, composition, errors-as-values; compare with TS habits
- **W19:** hexagonal architecture refactor; ArchUnit rules; OOD kata (parking lot, elevator, rate limiter, library)
- **W20:** consolidation + gate

**Gate:** refactor a deliberately smelly module; every move recorded in an ADR.

## Phase 4 — Databases in depth (weeks 21–27)
- **W21:** SQL mastery: joins, window functions, CTEs, aggregations
- **W22:** modeling: normalization, ERD, constraints; integrity as correctness tool
- **W23:** indexes and `EXPLAIN (ANALYZE, BUFFERS)`; slow query on 5M rows
- **W24:** transactions, ACID, isolation anomalies, MVCC, locks, deadlocks
- **W25:** Spring transactions: propagation, locking strategies, idempotent writes; stock reservation under concurrency
- **W26:** Redis, MongoDB, DynamoDB/Cassandra modeling, Elasticsearch search; replication, sharding, CAP/PACELC
- **W27:** consolidation + gate; zero-downtime migrations (expand/contract)

**Reality Lab focus:** categories B and C.
**Gate:** fix a lost update three ways (optimistic, pessimistic, atomic SQL) and defend the trade-offs.

## Phase 5 — JVM and concurrency (weeks 28–32)
- **W28:** JVM memory, JIT, GC; Java Memory Model, `happens-before`
- **W29:** threads, executors, `CompletableFuture`, locks, atomics, concurrent collections
- **W30:** virtual threads, structured concurrency; WebFlux awareness
- **W31:** profiling: JFR, async-profiler, MAT; load testing with k6; latency percentiles
- **W32:** consolidation + gate

**Build:** rate limiter and bulk-import pipeline.
**Break-it labs:** memory leak, thread-pool exhaustion, GC pause.
**Gate:** diagnose each lab from dumps and metrics before reading the code.

## Phase 6 — Security, APIs and messaging (weeks 33–38)
- **W33:** Spring Security, JWT, OAuth2/OIDC with Keycloak, RBAC
- **W34:** OWASP Top 10 hands-on with ZAP; secrets; dependency scanning, SBOM
- **W35:** API design: idempotency keys, pagination, versioning, rate limits, webhooks; gRPC/GraphQL trade-offs
- **W36:** Kafka and RabbitMQ; delivery semantics; outbox
- **W37:** Spring Batch, schedulers, ShedLock; WebSockets/SSE
- **W38:** consolidation + gate

**Build:** async order processing, notifications, payment webhooks with signature check and idempotency.
**Gate:** self-run pentest checklist passes; a replay test shows no duplicate effects.

## Phase 7 — Distributed systems and architecture (weeks 39–46)
- **W39–40:** theory: consistency models, Raft idea, clocks, failure modes (MIT 6.824, DDIA ch. 5–9)
- **W41:** saga, CQRS, event sourcing (and when not), outbox/CDC
- **W42:** resilience: timeouts, retries with jitter, circuit breaker, bulkhead
- **W43:** DDD: bounded contexts, aggregates; modular monolith; ADRs
- **W44–45:** extract Inventory, Payments, Notifications; saga for checkout; Toxiproxy partition tests
- **W46:** consolidation + gate

**System design practice (one per week alongside):** URL shortener, feed, chat, notifications, payments, ride-hailing, rate limiter, search, file storage, metrics.
**Gate:** 45-minute mock system design + a written design doc reviewed by a peer.

## Phase 8 — Cloud-native delivery and operations (weeks 47–55)
- **W47:** Docker: multi-stage, Jib/Buildpacks, non-root, scanning; Compose
- **W48:** CI/CD: GitHub Actions pipeline with test, coverage/mutation gate, scan, publish
- **W49–50:** Kubernetes: pods, deployments, services, ingress, config, probes, limits, HPA (kind)
- **W51:** Helm/Kustomize, ArgoCD GitOps; canary/blue-green; feature flags
- **W52:** Terraform + AWS core (IAM, VPC, EKS/ECS, RDS, S3, SQS, Secrets Manager); cost awareness
- **W53:** observability: Micrometer, Prometheus, Grafana, OpenTelemetry, Loki; SLOs and alerts
- **W54:** reliability: graceful shutdown, DB failover drill, backup and restore test, chaos basics
- **W55:** consolidation + gate

**Break-it labs:** bad deploy with rollback, pod OOMKilled, DB failover, secret rotation.
**Gate:** one-line change from PR to production in <15 min, with verified rollback and visible telemetry.

## Phase 9 — Real-world engineering and senior behaviours (weeks 56–60)
- **W56:** legacy: read a Java 8/Spring 4 codebase; upgrade Boot 2 → 3 (`javax` → `jakarta`); characterization tests; strangler pattern
- **W57:** read OSS (Spring MVC, Hikari, Kafka client); submit one real PR or doc fix
- **W58:** behaviours: design docs/RFCs, estimation and slicing, review that teaches, tech-debt negotiation, on-call
- **W59:** interview readiness: Java/Spring/concurrency Q&A, SQL, system design, STAR stories
- **W60:** game-day capstone: a teammate injects 3 faults; detect, mitigate, write the postmortem

## Senior exit checklist
1. Diagnose JVM memory, GC and thread problems from dumps.
2. Design schemas and tune queries from execution plans; explain isolation anomalies.
3. Choose monolith vs services and sync vs async, with written ADRs.
4. Services are secure, resilient, idempotent and observable by default.
5. Own a test strategy: unit, slice, Testcontainers, contract, load.
6. Ship via CI/CD to Kubernetes, operate against SLOs, write postmortems.
7. Run a design review and a system design session; write a design doc.
8. Review others' code, including AI-generated code, with specific actionable comments.
9. Ledger of ~100 reproduced production-style problems, each explainable in one minute; debug an unseen fault using evidence.

## Evidence to leave with
Public ShopFlow repo · 8–10 ADRs · 3 design docs · 4 write-ups · `LEDGER.md` · one OSS contribution.

## Resources
- **Books:** *Effective Java*, *Java Concurrency in Practice*, *Clean Architecture*, *Refactoring*, *Head First Design Patterns*, *DDIA*, *Domain-Driven Design Distilled*, *Building Microservices*, *Release It!*, *SRE* (selected chapters), *The Pragmatic Programmer*
- **Docs/courses:** AlgoMaster roadmaps (checklist), Spring guides, Baeldung, Use The Index Luke, Kubernetes docs, MIT 6.824, Exercism Java, Testcontainers guides, Martin Fowler's site
- **Practice:** LeetCode, ShopFlow tickets, system-design write-ups, game-days

## Progress tracker
| Phase | Started | Gate passed | Reviewer | Notes |
|---|---|---|---|---|
| 0 | | | | |
| 1 | | | | |
| 2 | | | | |
| 3 | | | | |
| 4 | | | | |
| 5 | | | | |
| 6 | | | | |
| 7 | | | | |
| 8 | | | | |
| 9 | | | | |
