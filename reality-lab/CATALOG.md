# Reality Lab Catalog

Columns: ID · Item · Phase · Observe with · Repro hint · Status (☐ / ✔)

## D. Java and JVM
| ID | Item | Ph | Observe with | Repro hint | Status |
|---|---|---|---|---|---|
| RL-01 | `Integer` cache and `==` | 1 | unit test | compare `Integer` 127 vs 128 with `==` | ☐ |
| RL-02 | Integer overflow | 1 | unit test | `Integer.MAX_VALUE + 1`; then `Math.addExact` | ☐ |
| RL-03 | Floating point money | 1 | unit test | sum 0.1 ten times; switch to `BigDecimal` | ☐ |
| RL-04 | `equals` without `hashCode` | 1 | unit test | put object in `HashSet`, lookup fails | ☐ |
| RL-05 | Mutable `HashMap` key | 1 | unit test | mutate key after insert, value lost | ☐ |
| RL-06 | Default charset/time zone differences | 1 | run with `-Duser.timezone`, `-Dfile.encoding` | same code, two JVM settings | ☐ |
| RL-07 | `SimpleDateFormat` thread safety | 1 | parallel test | shared formatter in 50 threads | ☐ |
| RL-08 | `ConcurrentModificationException` | 1 | unit test | remove in for-each | ☐ |
| RL-09 | Swallowed exception and lost cause | 1 | logs | `catch (Exception e) {}` then add cause chaining | ☐ |
| RL-10 | Check-then-act race | 5 | jcstress or stress test | `if (!map.containsKey) put` vs `computeIfAbsent` | ☐ |
| RL-11 | Visibility without `volatile` | 5 | stress test | busy-wait on non-volatile flag | ☐ |
| RL-12 | Deadlock | 5 | `jstack`, `jcmd Thread.print` | two locks, opposite order | ☐ |
| RL-13 | Thread-pool exhaustion | 5 | thread dump, metrics | blocking calls in a small pool | ☐ |
| RL-14 | `ThreadLocal` leak | 5 | heap dump + MAT | pool threads keep large value | ☐ |
| RL-15 | Static collection leak | 5 | heap dump + MAT | cache that never evicts | ☐ |
| RL-16 | Parallel stream misuse | 5 | JFR | blocking IO in `parallelStream` | ☐ |
| RL-17 | Virtual thread pinning | 5 | JFR `jdk.VirtualThreadPinned` | `synchronized` around blocking IO | ☐ |
| RL-18 | Ignoring interrupts | 5 | thread dump | task that cannot be cancelled | ☐ |
| RL-19 | GC pause and allocation rate | 5 | GC logs, JFR | allocate large short-lived arrays | ☐ |
| RL-20 | Heap in containers | 8 | `docker stats`, `-XX:+PrintFlagsFinal` | JVM in 512 MB container with/without `MaxRAMPercentage` | ☐ |
| RL-21 | Off-heap/direct buffer growth | 5 | NMT (`-XX:NativeMemoryTracking`) | allocate direct buffers in a loop | ☐ |
| RL-22 | Metaspace/classloader leak | 5 | heap dump, `jcmd GC.class_stats` | redeploy-like class generation | ☐ |
| RL-23 | MDC lost across threads | 6 | logs | `@Async` call without context propagation | ☐ |
| RL-24 | Expensive log string building | 5 | JMH/JFR | debug logs with string concat in hot path | ☐ |
| RL-25 | Secrets/PII in logs | 6 | log grep | log a request body with a token | ☐ |

## A. Spring and Spring Boot
| ID | Item | Ph | Observe with | Repro hint | Status |
|---|---|---|---|---|---|
| RL-26 | `@Transactional` self-invocation | 2 | SQL log, transaction log | call transactional method from same class | ☐ |
| RL-27 | `@Transactional` on private/final | 2 | debugger, bean class | check if proxy intercepts | ☐ |
| RL-28 | No rollback on checked exception | 2 | DB state | throw `Exception`, row persists | ☐ |
| RL-29 | `REQUIRES_NEW` surprise | 4 | SQL log | inner commit survives outer rollback | ☐ |
| RL-30 | `readOnly=true` meaning | 4 | SQL log, flush behaviour | write inside read-only tx | ☐ |
| RL-31 | Remote call inside transaction | 4 | `pg_stat_activity` | slow HTTP while holding a connection | ☐ |
| RL-32 | Open-Session-in-View | 2 | SQL log | lazy load during JSON serialization | ☐ |
| RL-33 | Singleton with mutable state | 2 | stress test | request data in a field | ☐ |
| RL-34 | Circular dependency | 2 | startup failure | A↔B constructor injection | ☐ |
| RL-35 | `@Async` default executor | 5 | thread names, metrics | burst of calls creates unbounded threads | ☐ |
| RL-36 | `@Cacheable` proxy limits | 6 | cache stats | self-invocation bypasses cache | ☐ |
| RL-37 | Auto-config debugging | 2 | `--debug`, Actuator `/conditions` | find why a bean is missing | ☐ |
| RL-38 | Property precedence and profiles | 2 | `/env` | env var vs yaml vs profile override | ☐ |
| RL-39 | Jackson date/time zone | 2 | response JSON | `LocalDateTime` vs `Instant` | ☐ |
| RL-40 | Jackson `BigDecimal` precision | 2 | JSON | number parsed as double on the client | ☐ |
| RL-41 | Jackson unknown/PATCH null vs absent | 2 | tests | `JsonNullable` or merge patch | ☐ |
| RL-42 | Serializing lazy proxies | 2 | exception | return entity directly | ☐ |
| RL-43 | Missing outbound HTTP timeouts | 6 | thread dump | remote that never answers | ☐ |
| RL-44 | Connection pool exhaustion (HTTP) | 6 | metrics | slow upstream exhausts pool | ☐ |
| RL-45 | Retrying non-idempotent calls | 6 | duplicate side effects | retry a POST payment | ☐ |
| RL-46 | Tomcat vs Hikari vs DB limits | 4 | metrics | 200 threads, pool of 10 | ☐ |
| RL-47 | Graceful shutdown | 8 | logs, `kill -TERM` | in-flight request during shutdown | ☐ |

## B. JPA / Hibernate
| ID | Item | Ph | Observe with | Repro hint | Status |
|---|---|---|---|---|---|
| RL-48 | N+1 select | 2 | datasource-proxy, Hibernate stats | list orders with items | ☐ |
| RL-49 | `LazyInitializationException` | 2 | exception | access lazy outside tx | ☐ |
| RL-50 | "EAGER fixes it" fallout | 2 | SQL log | EAGER on collections multiplies queries | ☐ |
| RL-51 | Fetch join with pagination | 4 | Hibernate warning HHH000104 | `join fetch` + `Pageable` | ☐ |
| RL-52 | Cartesian product with two collections | 4 | SQL log | fetch two bags | ☐ |
| RL-53 | Dirty checking and flush surprises | 4 | SQL log | mutate entity, update appears without `save` | ☐ |
| RL-54 | `persist` vs `merge`, detached entity | 4 | SQL log | save detached object | ☐ |
| RL-55 | Entity `equals/hashCode` with generated ID | 4 | tests | entity in `Set` before and after persist | ☐ |
| RL-56 | Cascade/`orphanRemoval` surprise | 4 | SQL log | delete child via parent collection | ☐ |
| RL-57 | `IDENTITY` blocks batching | 4 | SQL log, timing | 10k inserts vs sequence + batch | ☐ |
| RL-58 | Optimistic lock `@Version` | 4 | exception | two concurrent updates | ☐ |
| RL-59 | DTO projection vs entity reads | 4 | SQL log, timing | list view with projection | ☐ |

## C. PostgreSQL in production
| ID | Item | Ph | Observe with | Repro hint | Status |
|---|---|---|---|---|---|
| RL-60 | Index not used: type cast | 4 | `EXPLAIN` | compare varchar to int | ☐ |
| RL-61 | Index not used: function on column | 4 | `EXPLAIN` | `lower(email)` without expression index | ☐ |
| RL-62 | Composite index order | 4 | `EXPLAIN` | query on second column only | ☐ |
| RL-63 | Partial/covering index | 4 | `EXPLAIN` | index-only scan | ☐ |
| RL-64 | Stale stats / bad plan | 4 | `EXPLAIN`, `ANALYZE` | skewed data after bulk load | ☐ |
| RL-65 | `OFFSET` pagination cost | 4 | timing, `EXPLAIN` | page 100000 vs keyset | ☐ |
| RL-66 | Slow `count(*)` | 4 | `EXPLAIN` | 5M rows; estimate alternatives | ☐ |
| RL-67 | Idle in transaction | 4 | `pg_stat_activity` | open tx never closed; vacuum blocked | ☐ |
| RL-68 | Bloat and autovacuum | 4 | `pg_stat_user_tables`, `pgstattuple` | heavy updates | ☐ |
| RL-69 | `pg_stat_statements` hunt | 4 | extension | find top time consumers | ☐ |
| RL-70 | Lock queue behind `ALTER TABLE` | 4 | `pg_locks` | long tx + DDL + new queries blocked | ☐ |
| RL-71 | `CREATE INDEX CONCURRENTLY` | 4 | `pg_locks` | normal vs concurrent index build | ☐ |
| RL-72 | Safe NOT NULL / default migration | 4 | `pg_locks`, Flyway | multi-step migration | ☐ |
| RL-73 | Lost update under `READ COMMITTED` | 4 | tests | read-modify-write counter | ☐ |
| RL-74 | Write skew | 4 | tests | on-call doctors example | ☐ |
| RL-75 | Serialization failure and retry | 4 | exception | `SERIALIZABLE` conflict | ☐ |
| RL-76 | `FOR UPDATE SKIP LOCKED` queue | 6 | tests | multiple workers | ☐ |
| RL-77 | Deadlock from lock order | 4 | Postgres log | two transactions, opposite order | ☐ |
| RL-78 | `timestamptz` vs `timestamp` | 4 | queries | session time zone change | ☐ |
| RL-79 | UUID v4 vs v7 index locality | 4 | insert timing, index size | 5M inserts each | ☐ |
| RL-80 | `NOT IN` with NULL | 4 | query results | subquery returning NULL | ☐ |
| RL-81 | Replication lag, read-your-writes | 4 | replica lag query | write then read from replica | ☐ |
| RL-82 | Backup and restore test | 8 | `pg_dump`/PITR | restore into a new instance and verify | ☐ |

## E. Distributed systems and operations
| ID | Item | Ph | Observe with | Repro hint | Status |
|---|---|---|---|---|---|
| RL-83 | Retry storm | 7 | metrics, Toxiproxy | slow upstream plus naive retries | ☐ |
| RL-84 | Timeout budget across layers | 7 | traces | client 5s, server 10s | ☐ |
| RL-85 | Dual-write problem | 6 | crash test | write DB then Kafka, kill in between | ☐ |
| RL-86 | Duplicate delivery and idempotent consumer | 6 | consumer logs | restart before commit | ☐ |
| RL-87 | Kafka rebalance storm | 6 | consumer logs, lag | slow processing beyond `max.poll.interval` | ☐ |
| RL-88 | Consumer lag growth | 6 | lag metrics | slow consumer under load | ☐ |
| RL-89 | Poison message and DLQ | 6 | logs | malformed payload loops forever | ☐ |
| RL-90 | Cache stampede | 6 | Redis metrics | expire hot key under load | ☐ |
| RL-91 | Stale cache invalidation bug | 6 | tests | update without evict | ☐ |
| RL-92 | JVM DNS caching | 8 | `tcpdump`, logs | change DNS record while app runs | ☐ |
| RL-93 | Keep-alive mismatch (intermittent 502) | 8 | `curl -v`, LB logs | idle timeout shorter than server's | ☐ |
| RL-94 | TLS certificate expiry | 8 | `openssl s_client` | expired cert in test | ☐ |
| RL-95 | p99 vs average, coordinated omission | 5 | k6, histograms | closed-loop vs open-loop load | ☐ |
| RL-96 | Queueing and saturation | 5 | metrics | latency knee at 80%+ utilization | ☐ |
| RL-97 | CrashLoopBackOff debugging | 8 | `kubectl describe/logs --previous` | bad config | ☐ |
| RL-98 | OOMKilled vs OOM error | 8 | `kubectl describe` | memory limit below heap | ☐ |
| RL-99 | CPU throttling | 8 | cgroup metrics | low CPU limit, latency spikes | ☐ |
| RL-100 | Probe killing slow starter | 8 | events | liveness before startup completes | ☐ |
| RL-101 | Rolling update with incompatible migration | 8 | traffic errors | old pods vs new schema | ☐ |
| RL-102 | Graceful shutdown on K8s | 8 | access logs | missing `preStop` and readiness drain | ☐ |
| RL-103 | HPA flapping | 8 | HPA events | spiky CPU, short stabilization | ☐ |
| RL-104 | High-cardinality metrics | 8 | Prometheus | user ID as label | ☐ |
| RL-105 | Missing trace propagation | 8 | Tempo/Jaeger | async hop loses trace | ☐ |
| RL-106 | Secret rotation | 8 | rollout | rotate DB password without downtime | ☐ |

## F/G. Method and job reality (practice, not code repros)
| ID | Item | Ph | Observe with | Repro hint | Status |
|---|---|---|---|---|---|
| RL-107 | Blameless postmortem write-up | 8 | doc | from the game-day or a public incident | ☐ |
| RL-108 | Characterization tests on legacy | 9 | tests | untested legacy class | ☐ |
| RL-109 | Boot 2 → 3 upgrade | 9 | build, tests | `javax` → `jakarta` | ☐ |
| RL-110 | Review an AI-generated PR | 9 | checklist | find 5 issues in generated code | ☐ |
