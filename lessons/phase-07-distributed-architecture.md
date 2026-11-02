# Phase 7: Distributed Systems and Architecture (Weeks 39–46)

Goal: reason about what fails in distributed systems, choose architecture with trade-offs written down, and split a monolith only for good reasons.

Every week also has a **system design practice** item: a two-page design doc in `docs/design/` with requirements, capacity estimates, API, data model, architecture, failure modes and trade-offs. The full template and the self-review rubric are in **Week 39, S4**. Weeks 40–45 use the short-form timebox. Week 46 returns to the full form.

How to read this file: follow the numbered steps in order (see `README.md`, "The session format"). Times are budgets. "Done when" is the only definition of finished.

Setup once for the phase:
- Toxiproxy in Compose (image `ghcr.io/shopify/toxiproxy`, API on port 8474; verify against current docs) and the `toxiproxy-cli` binary.
- A scratch notebook file `docs/design/_estimates.md` for arithmetic you reuse.
- A Mermaid-capable markdown viewer for sequence diagrams (IntelliJ plugin or GitHub rendering).

---

## Week 39 — Distributed systems fundamentals I: failure, time, replication
**Outcome:** can explain why distributed systems are hard.
**Before you start:** Phase 6 gate passed; ShopFlow runs with Compose (Postgres, Kafka, Redis). Create `labs/week-39/` and `docs/design/`.

### S1 Learn (3h): failure, clocks, replication
1. (25m) Search for "Fallacies of Distributed Computing" (Deutsch). Read the 8 fallacies. In a table, give each one a ShopFlow example (e.g. "the network is reliable" → Orders calls Payments over HTTP; what if the response is lost after the charge?).
2. (50m) DDIA ch. 8 "The Trouble with Distributed Systems": faults and partial failures, unreliable networks, timeouts, unreliable clocks. After each section close the book and write 3 bullets from memory.
3. (30m) Lamport, "Time, Clocks, and the Ordering of Events in a Distributed System": sections on the happened-before relation and logical clocks. Exercise: draw three processes A, B, C with 4 events each and 3 messages; assign Lamport timestamps by hand; check that if `a → b` then `L(a) < L(b)` and find a pair where the converse fails.
4. (50m) DDIA ch. 5 "Replication": single-leader (sync vs async followers, failover), replication lag problems (read-your-writes, monotonic reads, consistent prefix), multi-leader conflict, leaderless quorums (just the idea; details next week). Make a table: model → who accepts writes → what can go wrong.
5. (25m) MIT 6.5840 lecture 1 (introduction); lectures 2–4 (RPC, primary-backup) are optional extras for the commute. Note the lecture's failure vocabulary (fail-stop, network partition).

**Tips and common problems**
- Reading Lamport's paper feels abstract → the key idea is only "order by causality, not by wall clock"; do the hand exercise and skip proofs.
- "Why can't I tell dead from slow?" feels like a trick question → it is the whole point: a timeout is a guess. Write that sentence in your notes.
- DDIA chapters are long → do not read cover to cover in one go; use the 3-bullets-from-memory rule after each section.

**Done when**
- [ ] A fallacy table with 8 ShopFlow examples exists in `docs/design/fallacies.md`.
- [ ] Lamport timestamp exercise is photographed or committed.
- [ ] You can explain read-your-writes and monotonic reads aloud in under a minute each.

### S2 Build (3h): toy replicated key-value store
1. (15m) Create module `labs/week-39/toy-kv` (plain Java, JUnit 5). Design on paper: one leader, two followers; leader accepts `put(key, value)`; every write gets a monotonically increasing `version`; followers apply `replicate(key, value, version)` and ignore versions not newer than what they have.
2. (30m) Tests first: (a) leader write is readable on leader; (b) with a perfect network a follower converges after `replicate`; (c) with a dropped message the follower stays behind; (d) applying the same message twice is harmless. Use an interface `Network` with `SimulatedNetwork` (seeded `Random`, drop probability, delay range) so tests are deterministic.
3. (40m) Implement `Node` (`ConcurrentHashMap<String, Versioned>`), `Leader` (assigns versions, replicates asynchronously through `Network`) and `Follower`. Make tests (a), (b), (d) green.
4. (30m) Make test (c) green and add a failing test "follower catches up after the drop" → implement simple retry with the follower reporting its highest version (anti-entropy sketch). Keep it small.
5. (30m) Add a thin HTTP layer (JDK `HttpServer` or Spring Boot) so the three nodes can run as three processes on ports 9001–9003 (verify the demo with `curl`).
6. (25m) Read-after-write experiment: script 100 iterations of "write on leader, immediately read from follower"; count stale reads at drop 0%, 30%, and delay up to 300 ms. Record the numbers in `labs/week-39/README.md` with three possible fixes (read from leader, sticky session, carry the version token).
7. (10m) Commit and open a PR.

**Tips and common problems**
- Flaky tests → real threads and sleeps → use a single-threaded scheduler or a controllable clock in `SimulatedNetwork` and assert on state after "advancing time".
- Followers diverge forever → you drop messages and never retry → implement step 4 before declaring the demo done.
- Version collisions after a leader restart → versions kept in memory → note this limitation in the README; do not solve it now.

**Done when**
- [ ] All tests pass, including the drop and duplicate cases.
- [ ] A table of stale-read counts at three network settings is committed.
- [ ] `curl` against the three ports shows leader and follower values converging.

### S3 Lab (2h): extra — Toxiproxy between ShopFlow components
**Setup:** add Toxiproxy to Compose. Create a proxy so the API connects to Postgres via Toxiproxy.
1. (15m) Compose: add the `toxiproxy` service, expose 8474 (API) and 15432. Create the proxy: `toxiproxy-cli create --listen 0.0.0.0:15432 --upstream postgres:5432 shopflow_pg` (verify against current docs). Confirm with `toxiproxy-cli list`.
2. (10m) Point the ShopFlow datasource at `toxiproxy:15432`. Run a baseline: 20 requests to `GET /api/orders`, record p50 and p95 with `curl -w` in a loop or a tiny k6 script.
3. (45m) Run the experiments below one at a time. After each, remove the toxic (`toxiproxy-cli toxic delete ...`, verify) and note recovery. For each, fill a row: toxic, symptom seen by the client, log lines, whether threads/connections pile up (`jcmd <pid> Thread.print` or Actuator metrics), time to recover.
   - latency 500 ms: `toxiproxy-cli toxic add -t latency -a latency=500 shopflow_pg`
   - latency 500 ms with jitter 400
   - bandwidth limited to a few KB/s
   - `timeout` toxic with timeout=0 (connection accepted, no data: the "hang" case)
   - `reset_peer` (connection resets)
   - disable the proxy entirely (`toxiproxy-cli toggle shopflow_pg`)
4. (20m) Look specifically at the Hikari pool: which setting decided how long a request waited before failing (`connectionTimeout`, default 30 s)? Reproduce one request that fails only because the pool is exhausted, not because the DB is down.
5. (20m) Map each observed failure to a fallacy from S1.
6. (10m) Create ledger entry `X-39 Toxiproxy failure catalogue` with the table.

**Tips and common problems**
- Toxic seems to have no effect → app still uses the direct DB port or an existing pooled connection → restart the app after switching the URL; check `toxiproxy-cli inspect shopflow_pg` (verify).
- Everything times out at once → latency toxic on both upstream and downstream directions → add one toxic and set its stream (`-u`/`-d`) deliberately (verify flag names).
- Port conflicts → Compose publishes 15432 and your local Postgres uses it → change the listen port consistently in Compose and datasource URL.

**Done when**
- [ ] A table of 6 failure experiments with symptom, log evidence and recovery behaviour.
- [ ] You can explain why the "hang" case is worse than the "reset" case for a thread-per-request app.

### S4 Review + Write (2h): system design 1 — URL shortener (full template)

**The design-doc template** (copy to `docs/design/TEMPLATE.md`; every doc uses it):
```
# Design: <system>
1. Problem and scope
   - Functional requirements (3-6 bullets, user-visible)
   - Non-functional requirements (availability, latency target, consistency, durability)
   - Out of scope
2. Estimates (show the arithmetic)
   - Users, requests/day -> QPS average and peak (peak ~ 2-3x)
   - Read:write ratio
   - Storage: records x bytes x retention (+ replication factor)
   - Bandwidth: QPS x payload size
3. API (endpoints, request/response, idempotency, pagination, error model)
4. Data model (tables or key patterns, keys, indexes, access patterns served)
5. Architecture (diagram + one request flow per main use case + components and why)
6. Deep dive (1-2 hardest parts, e.g. ID generation, hot keys, ordering)
7. Failure modes (what breaks, how detected, what the user sees, recovery)
8. Trade-offs and rejected alternatives (what you chose NOT to do and why)
9. Open questions and risks
```
**Self-review rubric** (score yes/no; a doc passes with at least 9 of 10):
1. Every requirement is testable (a number or a behaviour).
2. Estimates show arithmetic and state assumptions.
3. Peak vs average is considered.
4. API covers errors and retries (idempotency where needed).
5. Data model serves each listed access pattern.
6. Diagram plus at least one request flow is present.
7. One deep dive explains a real design decision.
8. At least 3 failure modes include detection and recovery.
9. At least 2 alternatives are rejected with reasons.
10. Nothing is designed for a scale that the estimates do not justify.

**Full-form timebox:** requirements 10m, estimates 10m, API+data 15m, architecture 25m, failure modes+trade-offs 15m, self-review 15m.

Steps for this session:
1. (10m) Read the template and rubric. Read "Design Docs at Google" (Malte Ubl) for the idea of a short doc that records decisions. Skim the URL shortener chapter in Alex Xu vol. 1 only for the problem statement; do not read the solution yet.
2. (10m) Requirements: create short link, redirect, custom alias optional, expiry optional; latency target for redirects; availability goal; out of scope (analytics).
3. (10m) Estimates, worked example: 100M new URLs/month → 100,000,000 / (30 × 86,400) ≈ 39 writes/s (say 40). Read:write 100:1 → ≈ 4,000 reads/s average, peak ×3 ≈ 12,000/s. Storage: 100M × 12 months × 5 years = 6B records × ~500 B ≈ 3 TB. Key space: base62 with 7 chars = 62^7 ≈ 3.5 × 10^12 ≥ 6 × 10^9. Redo with your own assumptions.
4. (15m) API (`POST /links`, `GET /{code}` → 301/302 and why the choice matters for caching and analytics) and data model (`links(code PK, long_url, created_at, expires_at)`).
5. (25m) Architecture: how codes are generated (counter + base62 vs hash vs random with collision check), cache in front of the DB, read replicas. Draw the diagram and the redirect flow.
6. (15m) Failure modes: cache down, DB primary failover, hot link (viral), code collision. Trade-offs: 301 vs 302, counter coordination vs random codes.
7. (15m) Self-review with the rubric; fix the lowest items.
8. (20m) Notes: for each fallacy in S1, one sentence on how your design copes. Weekly retro (`retro.md`) 10 minutes: what worked, what blocked, what changes. (Take the retro from step 8's budget if you run over.)

**Tips and common problems**
- The doc becomes an essay → state the decision first, then the reason; cut anything the rubric doesn't ask for.
- Estimates feel arbitrary → state assumptions and round aggressively (86,400 ≈ 10^5 s/day); the point is order of magnitude.
- Designing sharding for 40 writes/s → check the estimates; one Postgres handles this easily. Say so in "Trade-offs".

**Done when**
- [ ] `docs/design/url-shortener.md` follows the template and scores ≥ 9/10 on the rubric.
- [ ] `docs/design/TEMPLATE.md` committed.
- [ ] `retro.md` has this week's three lines.

<details><summary>Self-check answers</summary>

1. *Why can't you tell a slow node from a dead one?* A timeout only says no reply arrived in time. The reply may be delayed, lost, or the node may have processed the request and its response got lost. You can only choose a timeout and live with false positives.
2. *Why are wall-clock timestamps unsafe for ordering?* Clocks drift and skew between machines, NTP can step backwards, and events on different nodes can't be compared reliably. Use logical clocks, version numbers or a single sequencer for ordering.
3. *Replication lag effect on users?* Users may not see their own writes, may see older data after newer (non-monotonic reads), or see effects before causes.
</details>

**If stuck:** use the 15-minute routine in `README.md`. For the toy KV, shrink to one leader and one follower first.
**Output:** toy KV store, Toxiproxy failure catalogue, URL shortener design doc.

---

## Week 40 — Distributed systems fundamentals II: consistency and consensus
**Outcome:** knows consistency models and what consensus provides.
**Before you start:** week 39 toy KV compiles. Create `labs/week-40/` (copy the toy KV or extend it in place).

### S1 Learn (3h): consistency models and consensus
1. (40m) DDIA ch. 9 "Consistency and Consensus": the sections on linearizability and ordering guarantees. After reading, define linearizable in two sentences and give 2 uses (lock/leader election, uniqueness constraints).
2. (20m) Revisit CAP and PACELC. Write one paragraph: where does ShopFlow's Postgres (primary + async replica) sit under PACELC, and what about Kafka and Redis?
3. (50m) Raft paper (Ongaro & Ousterhout), sections 1–5 only (skip the rest). Then open the Raft visualization (raft.github.io): start a cluster, kill the leader, watch an election, restart it. Answer in writing: what is a term, why a majority, what happens to uncommitted entries from a dead leader?
4. (30m) MIT 6.5840 Raft lecture(s): watch one at 1.5x and note one thing the paper did not make clear.
5. (20m) Pat Helland, "Life Beyond Distributed Transactions": read the introduction and the entity/activity ideas. Write why 2PC across services is avoided (blocking, availability, coupling) and what he proposes instead (small entities, messaging, at-least-once, idempotence).
6. (20m) Skim two Jepsen analyses at jepsen.io/analyses (read the summary and findings only). Note the pattern: claimed guarantee vs observed anomaly.

**Tips and common problems**
- Linearizable vs serializable get mixed up → linearizable = single-object recency; serializable = multi-object transaction isolation. Write both definitions side by side.
- Raft paper is dense → read the figure 2 summary and the visualization together; do not try to prove safety.
- "Eventual consistency" is vague → ask "eventual how, and what do clients see meanwhile?"

**Done when**
- [ ] A page `docs/design/consistency-notes.md` with definitions in your own words.
- [ ] You can narrate a Raft leader election in one minute.
- [ ] You can state why 2PC across services is avoided.

### S2 Build (3h): quorum reads and writes
1. (15m) Plan: N=3 replicas, a `Coordinator` that handles client reads/writes, parameters `W` and `R`. Each write carries a version from the coordinator (a counter is fine for this lab).
2. (35m) Tests first with the simulated network:
   - W=1,R=1 with one replica partitioned from the writer → a read from the stale replica returns the old value (assert stale read happens).
   - W=2,R=2 → a read always overlaps the last write (assert latest value across 200 random schedules with a seeded `Random`).
   - W=3 write fails when one replica is down (availability cost).
3. (50m) Implement the coordinator: write to all, wait for `W` acks (timeout → failure); read from `R` replicas, return the highest version.
4. (30m) Add read repair: after a quorum read, push the newest value to stale replicas. Test that a stale replica converges after a read.
5. (30m) Experiment table: for (W,R) in (1,1), (2,1), (1,3), (2,2), (3,1): record stale-read rate and write/read availability when 1 node is down. Add the inequality `W + R > N` and explain it.
6. (20m) Write `labs/week-40/README.md` with the table and your takeaway. Commit and open a PR.

**Tips and common problems**
- Quorum test passes but is nondeterministic → seed `Random` and print the seed on failure.
- Stale reads still appear at W=2,R=2 → coordinator assigns versions without seeing the latest → remember: a single counter on one coordinator is a simplification; document it.
- Overlap does not mean linearizable → quorums give freshness for sequential ops but concurrent operations and clock issues still matter; note this limitation.

**Done when**
- [ ] Tests demonstrate a stale read at (1,1) and none at (2,2).
- [ ] Table of five (W,R) settings is committed.
- [ ] Read repair test is green.

### S3 Lab (2h): extra — read a Jepsen report
**Exercise:** pick a Jepsen analysis of a system you use or will use (PostgreSQL, Kafka, Redis, MongoDB, etcd are good). Verify the report exists at jepsen.io/analyses.
1. (15m) Read the introduction and summary: version tested, workload, claimed guarantees.
2. (40m) Read the findings: for each anomaly note what was violated (lost write, stale read, split brain, duplicate), under what fault (partition, crash, clock skew) and the maintainers' response.
3. (25m) Fill this template in `docs/design/jepsen-<system>.md`: system and version; guarantee claimed; test workload and faults; anomalies found; root cause; fix or documented caveat; how it relates to ShopFlow.
4. (25m) Decide one concrete change to ShopFlow or its docs because of the report (e.g. "do not rely on X for locking"; "set Y config for durability").
5. (15m) Create ledger entry `X-40 Jepsen: <system>` with the one-minute summary, say it aloud.

**Tips and common problems**
- Reports are very long → read summary, findings and conclusions only.
- Jargon wall → look up terms in your S1 notes; skip the Clojure code.
- No relation to ShopFlow → choose a system you actually use.

**Done when**
- [ ] Filled template for one report.
- [ ] One ShopFlow decision derived from it.

### S4 Review + Write (2h): system design 2 — chat; consistency memo
**Short-form timebox (70m):** requirements 5, estimates 8, API+data 12, architecture 20, failure modes+trade-offs 15, self-review 10.
1. (5m) Re-read the template and rubric from week 39.
2. (5m) Requirements: 1:1 and group chat, message history, delivery/read receipts, online presence (decide in or out), ordering guarantees.
3. (8m) Estimates: e.g. 10M DAU × 40 messages/day = 400M/day → 400,000,000 / 86,400 ≈ 4,600/s average, peak ×3 ≈ 14,000/s; message ≈ 200 B → 80 GB/day raw before replication. Show your own numbers.
4. (12m) API (WebSocket vs long polling; send, ack, sync-since-cursor) and data model (messages keyed by conversation + sequence).
5. (20m) Architecture: connection gateways, message service, per-conversation ordering (sequence per conversation, partition by conversation ID), offline delivery, fan-out for groups.
6. (15m) Failure modes: gateway crash (reconnect + sync from last sequence), duplicate sends (client message ID), out-of-order delivery.
7. (10m) Self-review with the rubric (≥ 9/10).
8. (30m) Write the one-page memo `docs/design/consistency-shopflow.md`: table of ShopFlow operations (place order, reserve stock, charge payment, show catalog, show order status, send notification) with required consistency (strong / read-your-writes / eventual) and mechanism (DB transaction, idempotency key, cache TTL, etc.).
9. (15m) Retro.

**Tips and common problems**
- Designing presence and typing indicators first → put them out of scope unless asked; they dominate complexity.
- Ordering handled with timestamps → use per-conversation sequence numbers (week 39).
- Memo says "everything strong" → find at least three operations where eventual is fine and say why.

**Done when**
- [ ] `docs/design/chat.md` passes the rubric.
- [ ] Consistency memo lists ≥ 6 operations with a mechanism each.

<details><summary>Self-check answers</summary>

1. *Linearizable in plain words?* The system behaves as if there is one copy of the data and every operation takes effect atomically at one point between its start and end; once a write returns, all later reads see it.
2. *Why does a quorum overlap guarantee freshness?* If W + R > N, any read set shares at least one replica with the last write set, so the read sees the newest version (if versions are comparable).
3. *Why is 2PC rarely used across services?* It blocks while the coordinator is down, ties availability to all participants, needs locks held across network calls, and couples services tightly; sagas and idempotent messaging trade this for eventual consistency.
</details>

**If stuck:** simplify to N=3, W=2, R=2 with no concurrency; add complexity only after the happy path is green.
**Output:** quorum KV demo, chat design doc, consistency memo, Jepsen notes.

---

## Week 41 — Patterns: saga, CQRS, event sourcing, CDC
**Outcome:** can choose and implement cross-service workflows.
**Before you start:** Phase 6 outbox (SF-29) and idempotency (SF-28) work. Create `docs/adr/` entry draft for ADR-017.

### S1 Learn (3h): workflow and data patterns
1. (40m) microservices.io: read Saga and Transactional outbox. Draw checkout twice (orchestration and choreography) with participants Orders, Inventory, Payments.
2. (30m) Compensations: classify each checkout step as compensatable, pivot, or retriable. Table for ShopFlow: reserve stock (compensatable: release), charge payment (pivot or compensatable by refund?), confirm order (retriable), send notification (retriable). Write why compensation is a business action, not a rollback.
3. (35m) Fowler's bliki "CQRS" and "Event Sourcing". Write: costs of each, and three signs you should not use event sourcing.
4. (30m) Debezium docs "Architecture": CDC reads the DB log. Compare with your polling outbox from week 36 (latency, load, ordering, operational cost). Write a recommendation for ShopFlow.
5. (35m) Richardson, *Microservices Patterns*: the saga chapter. Compare his orchestrator state machine with your draft.
6. (10m) Recall questions (closed book): idempotent consumer, ordering key, what a saga log stores.

**Tips and common problems**
- Choreography looks simpler but is hard to follow → draw the event graph; if you can't answer "what happens if step 3 fails?" from the diagram, prefer orchestration.
- Treating compensation as undo → some effects can't be undone (email sent); design for semantic compensation and customer communication.
- CQRS everywhere → only where reads and writes have different shapes or scaling needs.

**Done when**
- [ ] Two checkout diagrams (orchestration and choreography) committed.
- [ ] Step classification table committed.
- [ ] Debezium vs outbox recommendation is written.

### S2 Build (3h): in-process checkout saga
1. (15m) Design: table `saga_instance(id uuid pk, order_id, state, current_step, payload jsonb, version, created_at, updated_at)` via Flyway. Steps: `ReserveStock` → `ChargePayment` → `ConfirmOrder`. Compensations: `ReleaseStock`, `RefundPayment`. Each step call carries an idempotency key `sagaId:stepName`.
2. (35m) Tests first (use the fake payment gateway from week 15/25):
   - happy path ends `COMPLETED`;
   - payment declined → stock released, state `COMPENSATED`;
   - confirm fails after charge → refund then release;
   - crash simulation: throw after `ChargePayment` completes, then run `SagaRecovery` and assert the saga resumes from persisted state without charging twice.
3. (45m) Implement `CheckoutSaga` as a state machine: persist state before each step ("intent") and after ("result"); a `SagaRecovery` job (scheduled, with ShedLock from week 37) picks sagas that are not terminal and older than a threshold.
4. (30m) Make steps idempotent: the payment gateway fake stores charges by idempotency key and returns the previous result on replay; stock reservation is keyed by `(sagaId)`.
5. (30m) Make compensations retryable: a compensation that fails stays in state `COMPENSATING` and is retried with backoff.
6. (20m) Wire the saga into checkout behind a feature flag `checkout.saga.enabled`; run an end-to-end test.
7. (5m) PR with a sequence diagram in the description.

**Tips and common problems**
- Saga state lives in memory → crash loses it → persist before calling the step; test with the crash simulation.
- Double charge on retry → idempotency key missing or generated per attempt → derive the key from `sagaId:stepName`, never from a random value per call.
- Two recovery workers act on the same saga → add optimistic `version` check or `SELECT … FOR UPDATE SKIP LOCKED` (week 37).
- Compensation fails and nobody notices → alert on sagas stuck in `COMPENSATING` for more than N minutes.

**Done when**
- [ ] All four failure-branch tests pass.
- [ ] Crash/recovery test shows exactly one charge in the fake ledger.
- [ ] `saga_instance` rows show a readable history of states.

### S3 Lab (2h): extra — failure after payment, before confirmation
**Setup:** the saga from S2 with a test hook that can throw inside `ConfirmOrder`.
1. (20m) Write a failing integration test: charge succeeds, `ConfirmOrder` throws. Expected: refund issued, stock released, final state `COMPENSATED`, customer-visible order status `FAILED`.
2. (25m) Implement or fix the saga until it passes. Observe the persisted states in `saga_instance` after each step (query it).
3. (25m) Replay: invoke recovery twice and replay the `ChargePayment` command manually with the same idempotency key. Assert the fake gateway ledger has exactly 1 charge and exactly 1 refund.
4. (25m) Concurrency: start two recovery workers at once against the same saga; assert only one performs compensations (optimistic lock or skip locked).
5. (15m) Write ledger entry `X-41 Saga: failure after pivot` with symptom, mechanism, fix, and your one-minute answer.
6. (10m) Add a "stuck saga" query to `docs/runbooks/saga.md`.

**Tips and common problems**
- Refund executes twice → refund idempotency key not derived from saga → use `sagaId:refund`.
- Test passes locally but flakes → recovery runs on a timer → trigger recovery explicitly in tests.
- Hard to see state → add a `saga_event` history table or log state transitions at INFO.

**Done when**
- [ ] Ledger shows 1 charge and 1 refund after replays.
- [ ] Concurrent recovery test is green 20 times in a row.
- [ ] Runbook has the stuck-saga query.

### S4 Review + Write (2h): system design 3 — notifications; ADR-017
**Short-form timebox (70m)** as in Week 40.
1. (5m) Re-read the rubric.
2. (5m) Requirements: email/push/SMS, templates, user preferences, rate limits per user, delivery status, retries.
3. (8m) Estimates (show arithmetic): e.g. 5M users × 3 notifications/day = 15M/day ≈ 175/s average, burst events (flash sale) ×50.
4. (12m) API (internal `POST /notifications`, preference API) and data model (notification, attempt, preference).
5. (20m) Architecture: ingest topic → router → per-channel queues → provider adapters; dedupe key; priority lanes.
6. (15m) Failure modes: provider down (circuit breaker, fallback channel), duplicate events, poison messages (DLQ), user spam, thundering herd on a campaign.
7. (10m) Self-review.
8. (30m) Write ADR-017 "Orchestrated saga for checkout": context, decision, consequences, alternatives rejected (choreography, 2PC, no saga). Include a link to the diagrams.
9. (15m) Retro.

**Tips and common problems**
- Treating "exactly once" as a requirement → state "at-least-once with dedupe key" in the doc.
- ADR with no rejected alternatives → add at least two.

**Done when**
- [ ] `docs/design/notifications.md` passes the rubric.
- [ ] ADR-017 includes ≥ 2 rejected alternatives.

<details><summary>Self-check answers</summary>

1. *Compensation vs rollback?* A rollback undoes uncommitted work atomically in one DB. A compensation is a new, committed business action that semantically reverses an earlier committed step (refund, release stock) and may itself fail or leave traces.
2. *When is event sourcing a bad idea?* When you need simple CRUD, ad-hoc queries are the norm, the team lacks experience, events need frequent schema change, or there's no real audit/temporal need; it adds complexity in projections, versioning and GDPR deletion.
3. *How does a saga survive a crashed orchestrator?* State is persisted before and after every step; a recovery process resumes non-terminal sagas, and every step and compensation is idempotent.
</details>

**If stuck:** reduce to the two-step saga (reserve, charge) first, then add confirm.
**Output:** in-process saga, ADR-017, notification design, saga runbook.

---

## Week 42 — Resilience patterns
**Outcome:** protects services from slow and failing dependencies.
**Before you start:** the payment adapter from SF-15 and the fake gateway exist. Toxiproxy from week 39 is running. Create `docs/resilience-policy.md`.

### S1 Learn (3h): stability patterns
1. (40m) RI Part II (Stability patterns): timeouts, circuit breaker, bulkheads, fail fast, handshaking. For each, note the failure it prevents.
2. (35m) Amazon Builders' Library: "Timeouts, retries and backoff with jitter". Write the formula for exponential backoff with full jitter, and why jitter matters.
3. (30m) Amazon Builders' Library: "Using load shedding to avoid overload". Write when a service should reject early and what status code to return.
4. (40m) Google SRE book ch. 21 (Handling Overload) and ch. 22 (Addressing Cascading Failures): list the causes and mitigations; link to what you can do in ShopFlow.
5. (25m) Resilience4j docs: Retry, CircuitBreaker, Bulkhead, TimeLimiter and the Spring Boot aspect order (Retry ( CircuitBreaker ( RateLimiter ( TimeLimiter ( Bulkhead ( function ) ) ) ) ); verify against current docs).
6. (10m) Closed-book quiz: list 5 ways a slow dependency takes down its callers.

**Tips and common problems**
- Retry everything → retry only idempotent operations and only transient errors (timeouts, 503), never 4xx.
- Timeouts too long to be useful → derive them from the caller's deadline and p99 of the dependency.
- Circuit breaker never opens → minimum calls or window too large → read `minimumNumberOfCalls` and `slidingWindowSize`.

**Done when**
- [ ] Notes on the 5 stability patterns each with a ShopFlow example.
- [ ] The full-jitter backoff formula is written and explained.

### S2 Build (3h): resilient payment adapter
1. (15m) Plan the policy table in `docs/resilience-policy.md`: dependency, timeout, retries (count, backoff, which errors), breaker thresholds, bulkhead size, fallback.
2. (30m) Tests first using a `ControllableGateway` with modes: `OK`, `SLOW(latency)`, `FLAKY(failureRate)`, `DOWN`. Cases: slow call times out; flaky call succeeds within retries; down trips the breaker; once open, calls fail fast without touching the gateway; half-open lets probes through and closes after success.
3. (40m) Add `resilience4j-spring-boot3` (or the current starter; verify against current docs) and configure instances in `application.yml`: `TimeLimiter` 2s, `Retry` max 3 with exponential backoff and jitter, `CircuitBreaker` (failure rate 50%, min calls 10, wait 10s), `Bulkhead` 10 concurrent.
4. (30m) Put the annotations on the adapter port implementation; ensure only idempotent payment operations are retried (use the idempotency key from week 35).
5. (25m) Expose metrics through Actuator/Micrometer (check metric names in `/actuator/metrics`, verify) and build one Grafana panel or a curl script showing breaker state.
6. (30m) Run the tests; add an integration test that proves the saga (week 41) compensates cleanly when the breaker is open.
7. (10m) PR; add the policy table to the description.

**Tips and common problems**
- Retries multiply latency (3 × 2s = 6s > the caller's timeout) → compute worst-case time: attempts × (timeout + backoff) and compare with the upstream deadline.
- `@CircuitBreaker` annotation seems ignored → self-invocation or missing AOP starter → call through the bean (same lesson as `@Transactional`).
- Fallback hides failures → a fallback that returns "success" for payments is a bug; use fallback only for safe degradation.
- Bulkhead rejects under normal load → size from Little's law (concurrency = rate × latency).

**Done when**
- [ ] Tests for slow/flaky/down/half-open all pass.
- [ ] Policy table committed.
- [ ] Breaker state visible via metrics.

### S3 Lab (2h): RL-83 retry storm, RL-84 timeout budget
**RL-83 setup:** Orders → Payments fake behind Toxiproxy. Use a client with naive retries (3 attempts, no delay) at both the HTTP client and the service layer.
1. (20m) Baseline: k6 open model (constant arrival rate 50 rps for 60s) with the dependency healthy. Count upstream requests (Payments access log or a counter) and client requests; amplification = upstream / client ≈ 1.
2. (20m) Add a `latency` toxic of 2s on the upstream and set client timeout 1s. Re-run. Record amplification, thread usage and error rate. Expect amplification up to 3 × 3 = 9 with nested retries.
3. (25m) Fix in steps: (a) one retry layer only; (b) exponential backoff with full jitter; (c) retry budget (e.g. retries ≤ 10% of requests); (d) breaker. Re-run after each and record the amplification.
4. (10m) Ledger entry `RL-83`.
**RL-84 setup:** client timeout 5s, server timeout 10s.
5. (20m) Make the server work take 8s. Observe: the client gave up at 5s, the server continues for 3 more seconds and holds a thread and DB connection (wasted work). Capture evidence (server log timestamps, thread dump).
6. (15m) Design a budget: gateway 3s ≥ service 2.5s ≥ DB 2s; propagate a deadline header and cancel work on timeout. Implement one cancellation (e.g. `Statement` timeout or `@Transactional(timeout=…)` and HTTP client deadline).
7. (10m) Ledger entry `RL-84` with the budget table.

**Tips and common problems**
- Closed-loop load hides the storm → use arrival-rate (open) scenarios in k6 (revisit week 31 RL-95).
- Toxic affects both directions → set stream deliberately (verify `toxiproxy-cli` flags).
- Counting requests wrong → count at the upstream (Payments), not only at the client.

**Done when**
- [ ] Amplification numbers recorded for each fix stage.
- [ ] Timeout budget table committed and one cancellation implemented.
- [ ] RL-83 and RL-84 entries explained aloud in < 1 minute.

### S4 Review + Write (2h): system design 4 — rate limiter / API gateway; resilience table
**Short-form timebox (70m).**
1. (5m) Re-read the rubric.
2. (5m) Requirements: limit per user/API key/IP, multiple tiers, distributed across gateway instances, response headers (429, `Retry-After`), fail open vs closed.
3. (8m) Estimates: e.g. 20k requests/s at peak, 1M active keys, counter size 32 B → 32 MB of counters; Redis ops/s needed.
4. (12m) API/headers and data model (key → tokens, timestamp).
5. (20m) Architecture: algorithms (token bucket, sliding window log, sliding window counter), Redis with Lua for atomicity, local pre-filter, where it sits in the gateway.
6. (15m) Failure modes: Redis down (fail open? closed?), clock skew between nodes, hot key (one noisy customer), race conditions.
7. (10m) Self-review.
8. (30m) Finalize `docs/resilience-policy.md` for every ShopFlow dependency (Payments, Inventory, Postgres, Redis, Kafka, Elasticsearch) with timeout, retry, breaker, bulkhead, fallback, and the reason. Review for retry-amplification risks across layers.
9. (15m) Retro.

**Tips and common problems**
- Blank policy cells → decide explicitly "no retry" and why.
- Fail-open vs fail-closed unspecified → for rate limiting say which and justify.

**Done when**
- [ ] `docs/design/rate-limiter.md` passes the rubric.
- [ ] Resilience policy table covers ≥ 6 dependencies.

<details><summary>Self-check answers</summary>

1. *Why does retrying at every layer multiply load?* Each layer retries N times, so attempts multiply (N^layers). A slow dependency receives the worst load exactly when it is weakest; this is a retry storm.
2. *What does an open circuit do to callers?* It rejects calls immediately (fail fast) without contacting the dependency, protecting threads and giving the dependency time to recover; after a wait it lets probe calls through (half-open).
3. *Why set client timeouts below server timeouts?* So the caller gives up (and can fall back or retry) before the callee's work expires, and budgets shrink down the call chain; otherwise the server keeps working for callers who have left.
</details>

**If stuck:** test one pattern at a time with the fake in `SLOW` mode before combining.
**Output:** resilient payment adapter, RL-83/84 entries, rate limiter design, resilience policy.

---

## Week 43 — Domain-driven design and modular monolith
**Outcome:** draws service boundaries from the domain, not from tables.
**Before you start:** ShopFlow monolith builds with hexagonal packages (SF-17). Create `docs/ddd/`.

### S1 Learn (3h): DDD and architecture styles
1. (100m) DDD-D (Vernon): read the whole book in two passes — pass one (60m) for ubiquitous language, bounded contexts, context maps, subdomains; pass two (40m) for aggregates, entities, value objects, domain events. After each chapter write the term and a ShopFlow example.
2. (30m) Fowler: "MonolithFirst", "Microservices" (the definition article) and "BoundedContext". List the costs of microservices and the triggers for moving to them.
3. (30m) BM ch. 1–3 (skim): what microservices are, how to model them, how to split a monolith.
4. (20m) Spring Modulith reference: application modules, verification, events, documentation generation. Note how it enforces boundaries.

**Tips and common problems**
- "Bounded context = microservice" → they are related, not identical; a context can live in a modular monolith first.
- Aggregates too big → an aggregate is a consistency boundary; keep it small and reference others by ID.
- Reading DDD-D passively → write one ShopFlow example per concept.

**Done when**
- [ ] Glossary of 10 DDD terms with ShopFlow examples.
- [ ] A list of "reasons to extract" and "reasons not to" from Fowler.

### S2 Build (3h): event storming and modular monolith
1. (20m) Setup: use a whiteboard tool or a markdown file. Colours: orange = domain event, blue = command, yellow = aggregate, lilac = policy/rule, pink = external system, red = hotspot/question.
2. (45m) Big-picture: write all ShopFlow domain events in time order (ProductListed, ItemAddedToCart, OrderPlaced, StockReserved, PaymentCaptured, OrderConfirmed, OrderShipped, ...). Alone, "ask the domain" questions out loud.
3. (30m) Add commands, actors, aggregates and policies ("when OrderPlaced then ReserveStock"). Mark hotspots.
4. (25m) Find boundaries: cluster events; note where vocabulary changes (the word "product" differs in Catalog and Inventory), and pivotal events. Name contexts: Catalog, Ordering, Inventory, Payments, Notifications.
5. (20m) Draw the context map with relationship types (customer-supplier, conformist, anti-corruption layer, shared kernel, open host service). Save `docs/ddd/context-map.md`.
6. (40m) Restructure the monolith into modules by context (`com.shopflow.catalog`, `...ordering`, `...inventory`, `...payments`, `...notifications`), each with an `api` package for what others may use and `internal` for the rest. Add a test: `ApplicationModules.of(ShopFlowApplication.class).verify()` (Spring Modulith; verify API against current docs) or an ArchUnit rule if you prefer. Replace direct cross-module calls with the module API or domain events.

**Tips and common problems**
- Event storming becomes a class diagram → stay with events and time, not data.
- `verify()` fails with cyclic dependencies → break the cycle with an event or an interface owned by the consumer.
- Package-private access trips tests → test through the module API or use Modulith's module integration tests.

**Done when**
- [ ] Context map committed with named relationships.
- [ ] `verify()` (or ArchUnit equivalent) passes with no cycles.
- [ ] No module imports another module's `internal` package.

### S3 Lab (2h): extra — find shared tables, separate ownership
**Setup:** the restructured modules and a running Postgres.
1. (20m) Find cross-context foreign keys with SQL on the catalog (verify the query on your Postgres version):
   `SELECT conrelid::regclass AS child, confrelid::regclass AS parent FROM pg_constraint WHERE contype = 'f';`
   Map each table to the context that owns it; list FKs that cross contexts.
2. (20m) Search the code for repositories or entities used by more than one module (`grep` imports of `*Repository` across module packages).
3. (30m) Pick two violations (e.g. Orders joining the `products` table for price). For each, define an interface in the owning module's `api` package (e.g. `ProductPricing.priceFor(sku)`), and replace the cross-module query with that call or with a copy of the data kept up to date by events (store only the needed fields).
4. (25m) Remove the cross-context foreign keys with a Flyway migration (keep the ID column, drop the FK). Add a test that fails if Orders reads a catalog table directly (ArchUnit or Modulith verification).
5. (15m) Run the full test suite and a manual checkout.
6. (10m) Ledger entry `X-43 Shared tables` with before/after.

**Tips and common problems**
- Dropping an FK loses integrity → compensate with application-level checks and eventual reconciliation; write the trade-off in ADR-018.
- Performance regression after replacing a JOIN → measure; cache or replicate the needed fields.
- Tests rely on cross-module fixtures → use each module's API to seed data.

**Done when**
- [ ] List of cross-context FKs before and after.
- [ ] At least two violations removed with tests.

### S4 Review + Write (2h): system design 5 — payments; ADR-018
**Short-form timebox (70m).**
1. (5m) Re-read the rubric.
2. (5m) Requirements: authorize/capture/refund, idempotency, no double charge, ledger of all money movements, reconciliation with the provider.
3. (8m) Estimates: e.g. 2M orders/day ≈ 23/s average, peak ×10 in a sale = 230/s; ledger rows 3 per order → 6M/day.
4. (12m) API (idempotency key header, states: `PENDING`, `AUTHORIZED`, `CAPTURED`, `FAILED`, `REFUNDED`) and data model (payment, ledger entry double-entry, provider event).
5. (20m) Architecture: payment service, provider adapter, webhook handler, reconciliation job, outbox to Orders.
6. (15m) Failure modes: timeout after charge (unknown outcome → query provider), duplicate webhook, out-of-order events, partial refunds, provider outage.
7. (10m) Self-review.
8. (30m) ADR-018 "Boundaries and the extraction criteria for each module": for each module list data owned, public API/events, and the criteria that would trigger extraction (independent scaling, different release cadence, team ownership, fault isolation). Mark Inventory and Payments as first extraction candidates with reasons; state "stay in the monolith" for the rest.
9. (15m) Retro.

**Tips and common problems**
- "Unknown outcome" on timeout is the key payments problem → the design must say how you resolve it (query by idempotency key, reconcile).
- ADR lists criteria that are vague → each criterion should be measurable.

**Done when**
- [ ] `docs/design/payments.md` passes the rubric.
- [ ] ADR-018 written with extraction criteria and rejected alternatives.

<details><summary>Self-check answers</summary>

1. *What is an aggregate's job?* To be the consistency boundary: it enforces invariants within one transaction, is changed only through its root, and references other aggregates by identity.
2. *When is a microservice the wrong choice?* When the team is small, the domain is not understood, deployment/ops maturity is low, or the boundaries are likely to change; a modular monolith gives most benefits with less cost.
3. *What would make you extract Inventory first?* Independent scaling (read-heavy, hot during sales), clear ownership of its data, a stable API, and the need to isolate its failures from checkout.
</details>

**If stuck:** do the event storming on paper; start with 10 events, not 40.
**Output:** context map, modular monolith, ADR-018, payment design.

---

## Week 44 — Extraction I: Inventory and Payments
**Outcome:** extracts a service with its own data and a stable contract.
**Before you start:** modules are verified (week 43), policies exist (week 42). Plan the work: SF-31 completes in S2; SF-32 finishes in the first 40 minutes of S3.

### S1 Learn (3h): strangler, ownership, contracts
1. (35m) Fowler: "StranglerFigApplication". Write a 5-step strangler plan for Inventory: (1) module API in monolith, (2) HTTP client behind a flag, (3) new service built, (4) traffic shifted, (5) old code removed.
2. (35m) BM chapters on integration and migration: database per service, patterns to split a database (move FK to API call, shared data → duplication).
3. (45m) Pact docs "Introduction" and the JVM consumer/provider guides: what a pact is, consumer test, provider verification, provider states. Write what contract tests catch that OpenAPI alone doesn't.
4. (30m) OpenAPI contract-first (revisit week 11) and versioning rules: additive changes only.
5. (35m) Spring Cloud docs (overview) vs Kubernetes-native: discovery (Services/DNS), config (ConfigMaps/Secrets), gateway (Ingress). Decision note: ShopFlow uses Kubernetes-native; write why.

**Tips and common problems**
- "We'll split the DB later" → a shared DB makes it a distributed monolith; plan data ownership first.
- Pact vs OpenAPI confusion → OpenAPI describes what the provider offers; Pact records what the consumer actually uses.

**Done when**
- [ ] Strangler plan written.
- [ ] Decision note (Kubernetes-native vs Spring Cloud) written.

### S2 Build (3h): SF-31 Inventory service (and SF-32 skeleton)
1. (15m) Branch and ADR note. Create module `inventory-service` (Spring Boot) with its own Postgres database/schema and Flyway baseline. Move inventory tables with a migration that copies data (`INSERT … SELECT` across schemas for local; document the production approach).
2. (20m) Write `inventory.openapi.yaml` first (reserve, release, getStock) and generate server interfaces or controllers.
3. (40m) Move the domain logic from the monolith module; keep behaviour (stock reservation from SF-18) and its concurrency test. Run it against the new service.
4. (35m) In Orders add `InventoryClient` (port + `RestClient` adapter) with the week 42 policy (timeouts, retries only where safe, breaker, bulkhead). Switch via flag `feature.inventory.remote` (local implementation vs remote).
5. (45m) Contract tests: Pact consumer test in Orders (reserve and release interactions) generating a pact file; provider verification in `inventory-service` with provider states (`stock exists`, `out of stock`). Use matching Pact JVM versions for consumer and provider (see tips).
6. (15m) Start SF-32: create `payments-service` skeleton, openapi stub, empty tests; commit.
7. (10m) PR for SF-31.

**Tips and common problems**
- Pact consumer and provider using different Pact versions or spec versions → verification fails with parsing errors → pin the same major version and spec (V3 or V4) on both sides (verify against current docs).
- Provider states not found → state name strings differ → copy them from the pact file.
- Data copied but IDs collide → sequences not reset → set sequences after the copy.
- Orders still joins inventory tables → check with the week 43 verification test.

**Done when**
- [ ] Inventory runs as its own app with its own DB and the concurrency test passes.
- [ ] Orders reaches it through `InventoryClient` with resilience configured.
- [ ] Pact consumer test generates a pact and provider verification passes.

### S3 Lab (2h): extra — finish SF-32, then kill a service mid-checkout
1. (40m) Finish SF-32: Payments extracted the same way (OpenAPI first, `PaymentsClient` with the idempotency key, Pact consumer and provider tests, policy from week 42).
2. (15m) Compose file: `orders`, `inventory`, `payments`, their databases, Kafka, Toxiproxy. Run a smoke checkout.
3. (25m) Experiment A: stop Inventory (`docker compose stop inventory`) during a k6 checkout loop. Observe breaker state, error responses, logs, and recovery after `start`.
4. (20m) Experiment B: stop Payments between "charge" and "confirm" in a single manual checkout (use a breakpoint or sleep). Check the saga state and what the customer sees.
5. (15m) Experiment C: restart Orders mid-saga; confirm recovery from `saga_instance`.
6. (5m) Write observations in `labs/week-44/README.md` and a ledger entry `X-44 Service outage behaviours`.

**Tips and common problems**
- Compose services start before DB is ready → add healthchecks and `depends_on: condition: service_healthy`.
- Breaker never opens in the experiment → traffic too low → increase k6 rate or lower `minimumNumberOfCalls` for the lab.
- Saga stuck forever after the outage → recovery job interval too long or not running → check the schedule and lock.

**Done when**
- [ ] Both services run in Compose with separate databases.
- [ ] Three outage experiments recorded with outcomes.
- [ ] Orders recovers without manual DB edits.

### S4 Review + Write (2h): system design 6 — search; extraction retrospective
**Short-form timebox (70m).**
1. (5m) Re-read the rubric.
2. (5m) Requirements: full-text product search, filters/facets, typo tolerance, relevance, near-real-time indexing.
3. (8m) Estimates: e.g. 10M products × 2 KB = 20 GB index (×1.5 overhead); 1,000 searches/s peak; indexing 50 updates/s.
4. (12m) API (query params, pagination via search-after) and data model (document mapping, analyzers).
5. (20m) Architecture: source of truth in Postgres, change events through outbox/CDC to indexer, Elasticsearch/OpenSearch cluster (shards, replicas), query service, reindex strategy with aliases.
6. (15m) Failure modes: index lag, reindex while serving, cluster yellow/red, bad analyzer change, DB-ES drift and reconciliation.
7. (10m) Self-review.
8. (30m) Retrospective `docs/retro-extraction-1.md`: what got harder (latency, debugging across logs, local setup, deployment count, data consistency), what got better, what you would do differently, whether Inventory should have been extracted at all. Include numbers (p95 before and after, lines of config added).
9. (15m) Retro.

**Tips and common problems**
- Retrospective is only positive → write at least three real costs.
- Search doc picks sharding numbers by guess → compute shard size targets from the index size.

**Done when**
- [ ] `docs/design/search.md` passes the rubric.
- [ ] Retrospective includes measured latency change.

<details><summary>Self-check answers</summary>

1. *Why can't two services share a database?* It couples their schemas and releases, hides dependencies, lets one service break another with a migration, and removes the ability to change or scale independently; it creates a distributed monolith.
2. *What does a consumer-driven contract catch?* Breaking changes to the parts of the API that consumers actually use (fields, status codes, formats), detected in the provider's build before release.
3. *What did you lose by extracting?* Local transactions and joins (replaced by sagas and APIs), simple debugging, low latency, easy local setup, and operational simplicity.
</details>

**If stuck:** do SF-31 in-process first (module API behind a flag), then lift it out; keep the flag until the Pact tests are green.
**Output:** SF-31, SF-32, contract tests, outage experiments, extraction retrospective, search design.

---

## Week 45 — Extraction II: Notifications and saga across services
**Outcome:** end-to-end checkout across three services survives faults.
**Before you start:** Orders, Inventory and Payments run in Compose. Kafka and the outbox exist. Add Jaeger or Tempo to Compose for traces (verify image names against current docs).

### S1 Learn (3h): tracing and distributed testing
1. (40m) OpenTelemetry docs "Concepts": traces, spans, context propagation, baggage. Write the lifecycle of one checkout trace.
2. (30m) W3C Trace Context (overview): the `traceparent` header format `00-<32 hex trace-id>-<16 hex span-id>-<2 hex flags>`. Practise reading a real header from a request.
3. (35m) How context moves over Kafka: it travels in record headers; Spring/Micrometer observation can inject and extract it (verify property names against current docs). Note what breaks when a consumer starts a new trace.
4. (35m) Idempotent consumers, DLQ and replay across services (revisit week 37): table `processed_message(message_id, consumer)` with unique constraint; replay tool.
5. (40m) Fowler "Testing Strategies in a Microservice Architecture": unit, integration, component, contract, end-to-end. Decide your test pyramid for ShopFlow (many unit/component, contract tests per integration, 2–3 end-to-end).

**Tips and common problems**
- Traces are broken at Kafka → consumer started a new span without extracting headers → enable observation on both `KafkaTemplate` and listener container.
- End-to-end tests multiply → they are slow and flaky; keep few and rely on contracts.

**Done when**
- [ ] You can read a `traceparent` header field by field.
- [ ] Test strategy table written in `docs/testing.md`.

### S2 Build (3h): SF-33 cross-service saga, SF-34 Notifications
1. (20m) Add tracing: dependencies (`micrometer-tracing-bridge-otel`, OTLP exporter; verify against current docs), config to export to the collector in Compose, `management.tracing.sampling.probability=1.0` for the lab.
2. (50m) SF-33: move the orchestrator calls to remote clients: Orders → Inventory `reserve`, Orders → Payments `charge` (idempotency key `sagaId:charge`), then confirm locally. Compensation calls use `release` and `refund`. Keep `saga_instance` in Orders. Write the integration test with Compose or Testcontainers (Inventory and Payments as containers or WireMock).
3. (45m) SF-34: `notifications-service` consumes `OrderConfirmed` and `OrderFailed` from Kafka; idempotent via `processed_message`; retry topic and DLQ with the policy from week 37; sends via a fake email adapter.
4. (25m) Outbox in Orders publishes the events in the same transaction as the order state change (reuse SF-29).
5. (25m) Verify one trace in Jaeger/Tempo that spans: HTTP into Orders → HTTP to Inventory → HTTP to Payments → Kafka publish → Notifications consume. Fix gaps.
6. (15m) Add a smoke test script and update the Compose README.

**Tips and common problems**
- Trace stops at Kafka → missing observation settings or custom serialisers dropping headers → check headers on the record with `kafka-console-consumer --property print.headers=true` (verify).
- Consumer processes the same event twice after restart → offset committed after processing but dedupe table missing → add the unique constraint and handle the violation as "already processed".
- Orders waits on a slow service holding a DB transaction → do remote calls outside transactions (week 25).

**Done when**
- [ ] Checkout works across three services through Compose.
- [ ] A single trace shows HTTP and Kafka spans.
- [ ] Notifications is idempotent (duplicate event sends one email).

### S3 Lab (2h): extra — partition during the saga; duplicate event
**Experiment 1 setup:** put Toxiproxy between Orders and Payments.
1. (15m) Add proxy `payments` (listen 18082 → payments:8082; ports illustrative). Point Orders' `PaymentsClient` at it.
2. (25m) Scenario "response lost after charge": add a `timeout` toxic downstream so that Payments processes the charge but Orders never gets the reply (verify toxic flags). Run checkout. Expected: Orders times out, retries with the same idempotency key, Payments returns the earlier result, Orders confirms; ledger shows one charge.
3. (20m) Scenario "full partition": `toxiproxy-cli toggle payments` before charge. Expected: breaker opens or retry exhausted, saga compensates (release stock), order `FAILED`, no charge.
4. (10m) Re-enable and check that stuck sagas, if any, resume.
**Experiment 2 setup:** duplicate the `PaymentSucceeded`/`OrderConfirmed` event.
5. (25m) Publish the same event twice using the console producer with the same `messageId` (verify command: `kafka-console-producer` with key/headers). Assert: one email, one ledger/consumer record, no error loop.
6. (15m) Write expected-vs-observed table in `labs/week-45/README.md`. Ledger entry `X-45 Saga under partition and duplicates`.
7. (10m) Capture one trace from each scenario and attach screenshots.

**Tips and common problems**
- Proxy ports collide with service ports → keep a table of proxy listen ports and update Compose.
- Retried charge produces a second charge → the idempotency key changes per retry → derive the key from `sagaId`, not a fresh UUID.
- Duplicate event reprocessed because dedupe key is the Kafka offset → use a business `messageId`, not partition/offset.

**Done when**
- [ ] Both scenarios behave as expected with evidence (ledger, saga table, traces).
- [ ] No double charge in any run.
- [ ] Duplicate event creates exactly one notification.

### S4 Review + Write (2h): system design 7 — ride-hailing or feed; saga diagrams
**Short-form timebox (70m).** Choose feed (needed for week 46) or ride-hailing.
1. (5m) Re-read the rubric.
2. (5m) Requirements (feed): follow graph, post, timeline reads, ranking (simple), freshness target.
3. (8m) Estimates: e.g. 50M DAU, 10 timeline reads/user/day = 500M/day ≈ 5,800/s average, peak ×3 ≈ 17,000/s; 1M posts/day ≈ 12/s; fan-out average 200 followers.
4. (12m) API and data model (post, follow edge, timeline cache).
5. (20m) Architecture: fan-out on write vs fan-out on read vs hybrid for celebrity accounts; timeline cache in Redis; async fan-out workers.
6. (15m) Failure modes: celebrity hot key, fan-out backlog, cache loss rebuild, stale timelines.
7. (10m) Self-review.
8. (30m) Mermaid sequence diagrams for the saga: happy path, payment declined, Payments timeout with retry, confirm failure with refund. Add to `docs/design/checkout-saga.md` with a table of states.
9. (15m) Retro.

**Tips and common problems**
- Diagram shows only happy path → each failure path is its own diagram; rubric item 8 depends on them.
- Fan-out choice stated without numbers → compare write amplification (posts × followers) with read cost using your estimates.

**Done when**
- [ ] Feed (or ride-hailing) doc passes the rubric.
- [ ] Four saga sequence diagrams committed.

<details><summary>Self-check answers</summary>

1. *How does a trace follow a Kafka message?* The producer injects the trace context (`traceparent`) into record headers; the consumer extracts it and starts a child (or linked) span, so the same trace ID continues across the async hop.
2. *What proves your saga is idempotent?* Replaying any step or event with the same idempotency key produces the same final state and side effects: one charge, one refund, one email; tests that replay and crash-recover show that.
3. *End-to-end test strategy and why few?* Mostly unit, component and contract tests; only 2–3 end-to-end tests for the critical journey, because they are slow, flaky and hard to debug.
</details>

**If stuck:** get a single HTTP trace working first (Orders → Inventory), then add Kafka.
**Output:** SF-33, SF-34, trace-linked flows, fault test results, feed/ride-hailing design, saga diagrams.

---

## Week 46 — Consolidation and Phase 7 gate
**Outcome:** passes the design gate.
**Before you start:** all earlier design docs exist in `docs/design/` (URL shortener, chat, notifications, rate limiter, payments, search, feed). Find a peer or mentor for reviews; if none, plan self-mocks with an AI agent as interviewer (allowed under README rules).

### S1 Learn (3h): system design method and capacity drill
1. (30m) Re-read the template and write your 8-step method card: requirements → estimates → API → data model → high-level design → deep dives → bottlenecks → trade-offs. Add the time split for a 45-minute interview (5, 5, 5, 5, 10, 10, 5).
2. (40m) Alex Xu vol. 1 ch. 2 (back-of-the-envelope). Memorise: 86,400 ≈ 10^5 s/day; 1 million requests/day ≈ 12 req/s; powers of two for KB/MB/GB/TB; latency magnitudes (memory ~100 ns, SSD ~100 µs, same-datacenter round trip ~0.5 ms, cross-region ~100 ms+).
3. (40m) Capacity drill, 6 problems, answers on paper before checking: (a) 300M MAU, 30% daily active, 5 reads/day → QPS; (b) 20M uploads/day × 2 MB for 3 years → storage; (c) 10k writes/s at 1 KB → MB/s; (d) cache hit ratio 90% of 20k reads/s → DB load; (e) 99.9% vs 99.99% availability → minutes of downtime per year; (f) number of servers if one does 500 rps at 50% target utilization. Show arithmetic.
4. (30m) System Design Primer (GitHub): read the scalability section and two worked problems; compare to yours.
5. (40m) DDIA ch. 1 (reliability, scalability, maintainability): reread; note the vocabulary for load parameters and percentiles.

**Tips and common problems**
- Arithmetic errors derail you → round to powers of ten, label units on every line.
- Reciting patterns instead of deriving → always connect a component to a number in your estimate.

**Done when**
- [ ] Method card written.
- [ ] Capacity drill answered with arithmetic and checked.

### S2 Build (3h): SF-35 polished design docs
1. (15m) Choose three docs for polish: feed, payments, search. Read each against the rubric; list the three weakest rubric items per doc.
2. (60m) Rewrite feed using the full-form timebox: tighten estimates (show peak vs average), add one deep dive (fan-out strategy), and failure modes.
3. (45m) Rewrite payments: add the reconciliation process, unknown-outcome handling, ledger invariants.
4. (45m) Rewrite search: sizing from the index estimate, reindex with aliases, drift detection.
5. (15m) Send each doc to the reviewer with the checklist: "Is every requirement testable? Which number looks wrong? Which failure mode did I miss? Which alternative would you pick?" Commit.

**Tips and common problems**
- Reviewer says "looks fine" → ask pointed questions (list above).
- Doc grows beyond two pages → move detail to appendices, keep the main doc to the template sections.

**Done when**
- [ ] Three docs revised and sent for review.
- [ ] Each doc scores ≥ 9/10 on your own rubric before sending.

### S3 Lab (2h): extra — timed mock interviews
**Setup:** a peer or mentor as interviewer, or an AI agent prompted: "Act as a senior interviewer for a system design interview. Ask one clarifying question at a time, push on trade-offs and failure modes, do not give the solution." Record screen and audio.
1. (5m) Prepare: pick two unseen problems (e.g. "design a file storage service", "design a metrics collection system"). Open only a blank document and a drawing tool.
2. (45m) Mock 1 following the time split from your method card.
3. (10m) Break; note three things that went badly while fresh.
4. (45m) Mock 2 on a different problem.
5. (15m) Replay at 1.5x and score yourself on the gate rubric: requirements, estimates, design, failure modes, explicit trade-offs. List fixes.

**Tips and common problems**
- Jumping to boxes and arrows → spend the first 5 minutes asking questions.
- Running out of time before failure modes → set a timer visible on screen; start failure modes at minute 30.
- Defensive about feedback → ask the interviewer "what would you have done differently?".

**Done when**
- [ ] Two recordings exist with a self-score for each rubric item.
- [ ] A list of three concrete improvements for the next mock.

### S4 Review + Write (2h): gate review
1. (30m) Apply the gate rubric with your reviewer or the recordings: mock coverage, docs reviewed, saga fault runs, ADR alternatives.
2. (25m) Re-run the saga fault scenarios from week 45 S3 plus a crash injection (kill Orders mid-saga) and record results.
3. (20m) Check ADR-017 and ADR-018 each list rejected alternatives; add any missing.
4. (20m) Address reviewer comments on the three docs; reply with what changed.
5. (15m) Update the progress tracker in `../java-backend-roadmap.md` (your copy), tag `phase-7`, and push.
6. (10m) Phase retro in `retro.md`: biggest gap, the topic to revisit, adjustments for Phase 8.

**Gate rubric (pass all):**
- 45-minute mock covers requirements, estimates, design, failure modes and explicit trade-offs.
- Three written design docs reviewed by a peer, with comments addressed.
- The ShopFlow saga survives partition, duplicate and crash injections.
- ADRs 017–018 have rejected alternatives recorded.

**Tips and common problems**
- "Passed" without evidence → keep the recordings, fault-run logs and review comments in `docs/phase-7-evidence/`.
- Gate failed → list failed items, repeat only those next week; do not delay Phase 8 more than one week.

**Done when**
- [ ] All four gate items have evidence in the repo.
- [ ] Tag `phase-7` pushed.

<details><summary>Self-check answers</summary>

1. *Order of the method?* Clarify requirements → estimate load and storage → define API → data model → high-level architecture → deep dives on the hard parts → bottlenecks, failure modes and trade-offs.
2. *Capacity drill answers (guide):* (a) 300M × 30% = 90M DAU × 5 = 450M/day ≈ 5,200/s. (b) 20M × 2 MB = 40 TB/day ≈ 44 PB over 3 years. (c) 10 MB/s. (d) 2,000 reads/s to the DB. (e) 99.9% ≈ 8.8 h/year; 99.99% ≈ 53 min/year. (f) 500 rps × 50% = 250 effective per server.
3. *Why record mocks?* You can't see your own habits (rambling, skipping failure modes, vague trade-offs) while performing; replay exposes them.
</details>

**If stuck:** if no peer is available, run the mocks with an AI agent as interviewer and score the transcript against the rubric.
**Output:** `phase-7` tag, design docs, mock recordings, evidence folder.
