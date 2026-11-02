# Phase 5: JVM and Concurrency (Weeks 28–32)

Goal: reason about memory, threads and latency; diagnose leaks, pool exhaustion and GC pauses from evidence.

Setup once: JDK Mission Control (JMC), async-profiler, Eclipse MAT, k6, VisualVM, and a ShopFlow build you can run with `-Xmx` and GC logging flags.

Conventions for this phase:
- Create a scratch Maven module `reality-lab/jvm-labs` for all lab snippets. Use `mvn -q exec:java -Dexec.mainClass=...` or run from IntelliJ with VM options.
- Find a JVM's PID with `jcmd` (no arguments lists Java processes) or `jps -l`.
- Benchmarks: never trust a number from a single short run. Warm up first and repeat (see tips in Week 29).

---

## Week 28 — JVM memory, JIT and garbage collection
**Outcome:** knows where memory goes and how GC behaves.
**Before you start:** have ShopFlow runnable as a fat JAR (`java -jar`) and k6 installed (`k6 version`).
**If stuck:** re-read the HotSpot GC Tuning Guide section on generations; ask for hypotheses, not answers (README "Stuck?" routine).

#### S1 Learn (3h): JVM runtime areas, JIT, GC, JMM
1. (25m) Read the introduction and "Generations" sections of the Oracle HotSpot GC Tuning Guide. Close it and write 5 bullets from memory: why generational, what is minor vs major collection, what "stop-the-world" means.
2. (30m) Read about runtime areas (heap young/old, metaspace, thread stacks, code cache, direct memory) in *Optimizing Cloud Native Java* (JVM chapters). Draw the memory map on paper with one flag per area (`-Xmx`, `-Xss`, `-XX:MaxMetaspaceSize`, `-XX:MaxDirectMemorySize`, `-XX:ReservedCodeCacheSize`).
3. (30m) Read the JIT sections of the same book: interpreter → C1 → C2, inlining, warmup, deoptimization. Write down why the first minute of a service is slower and what that means for benchmarks.
4. (35m) Read JCIP ch. 16 (Java Memory Model) and Shipilev's "Java Memory Model Pragmatics". Write the `happens-before` rules from memory (program order, monitor lock, volatile, thread start/join, final fields, transitivity). Check against the text.
5. (30m) Read about G1 vs ZGC (generational) in the GC Tuning Guide and Evans' book. Fill a 2-column table: pause target, throughput, heap size sweet spot, when to pick.
6. (20m) Learn to read a GC log. Run any Java program with `java -Xlog:gc*:file=gc.log:time,uptime,level,tags -Xmx256m ...` and read three lines: a young pause, a concurrent cycle, a full GC if any. Label each field (before→after heap, pause ms).
7. (10m) Write 8 flashcards (area, flag, what fills it, symptom when exhausted).

**Tips and common problems**
- Confusing "heap used" with "process memory" → the JVM also uses metaspace, stacks, code cache, direct memory and GC structures → always compare heap with RSS (`ps -o rss= -p <pid>`).
- GC log shows nothing → the `-Xlog` syntax changed in JDK 9+; the old `-XX:+PrintGCDetails` is gone → use `-Xlog:gc*`.
- Overlong reading on JIT → you only need the model (tiers, inlining, warmup), not assembly → stop at the timebox.

**Done when**
- [ ] A hand-drawn memory map with a flag next to each area exists in your notes.
- [ ] You can list the `happens-before` rules without looking.
- [ ] You can point at a GC log line and say what each number means.

#### S2 Build (3h): baseline performance report for ShopFlow
1. (20m) Add `scripts/run-perf.sh` that starts ShopFlow with:
   `java -Xms512m -Xmx512m -Xlog:gc*:file=logs/gc.log:time,uptime,level,tags -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/ -XX:StartFlightRecording=filename=logs/shopflow.jfr,settings=profile -jar target/shopflow.jar`
2. (20m) Write `perf/baseline.js` for k6: a 5-minute constant load on `GET /products` and `GET /products/{id}` (10 VUs). Run `k6 run perf/baseline.js` once against a warm app to check it works.
3. (40m) Start the app, wait 2 minutes for warmup, then run the 5-minute load. Do not touch the machine meanwhile (no builds, no browser video).
4. (30m) Stop the app cleanly (`SIGTERM`) so JFR writes the file. Open `logs/shopflow.jfr` in JMC. Note: allocation rate (Memory tab), GC pause max/avg (Garbage Collections), heap after GC, top 5 allocating classes.
5. (30m) Extract pauses from the log: `grep -E "Pause" logs/gc.log | tail -20`. Compute max and typical pause. Count collections per minute.
6. (30m) Write `docs/perf/baseline.md`: machine, JDK, flags, load profile, allocation rate, p95 latency from k6, GC pause stats, three observations, three questions to investigate later.
7. (10m) Commit scripts and report on a branch; open a PR; keep `logs/` out of git.

**Tips and common problems**
- JFR file empty or missing → app was killed with `kill -9` → stop with `SIGTERM` or run `jcmd <pid> JFR.dump filename=... ` before stopping.
- Allocation rate looks absurd → JFR sampling overhead or the load tool running on the same machine → note it in the report; compare relative changes, not absolutes.
- k6 numbers jump between runs → laptop thermal/power throttling and no warmup → plug in, warm up, run twice and compare.
- JMC can't open the file → version mismatch → use a current JMC build.

**Done when**
- [ ] `docs/perf/baseline.md` exists with numbers from at least two runs.
- [ ] GC log and JFR file are reproducible by one command.
- [ ] You can explain the allocation rate and a typical pause in plain words.

#### S3 Lab (2h): RL-19 GC pause, RL-21 off-heap, RL-22 metaspace
1. (10m) Create `reality-lab/jvm-labs`, add dependency `net.bytebuddy:byte-buddy` (for RL-22), and a package per lab.
2. (35m) **RL-19 GC pause and allocation rate.**
   - Write the failing test/script first: the target is "p99 pause under 50 ms with 512 MB heap"; start by measuring.
   ```java
   public class AllocStorm {
     static volatile Object sink;
     public static void main(String[] a) {
       long end = System.nanoTime() + 60_000_000_000L;
       while (System.nanoTime() < end) { sink = new byte[1_000_000]; }
     }
   }
   ```
   - Run twice: `java -Xmx512m -XX:+UseG1GC -Xlog:gc:file=g1.log AllocStorm` and `... -XX:+UseZGC -Xlog:gc:file=zgc.log AllocStorm`.
   - Observe: in `g1.log` look for `Pause Young (... G1 Humongous Allocation)` lines and their ms; in ZGC look for pause lines (should be sub-millisecond). Compare counts per minute.
   - Explain in 3 sentences: large arrays can be "humongous" in G1 (allocated directly in old regions), triggering frequent concurrent cycles; allocation rate drives GC frequency; ZGC trades CPU/footprint for tiny pauses.
   - Fix: reuse buffers (a small pool) or allocate smaller chunks. Regression test: measure allocated bytes per operation using `((com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean()).getThreadAllocatedBytes(Thread.currentThread().getId())` before/after a call and assert under a threshold.
3. (30m) **RL-21 off-heap growth.**
   ```java
   List<ByteBuffer> keep = new ArrayList<>();
   while (true) { keep.add(ByteBuffer.allocateDirect(10_000_000)); Thread.sleep(200); }
   ```
   - Run `java -Xmx128m -XX:NativeMemoryTracking=summary -XX:MaxDirectMemorySize=512m DirectLeak`.
   - Observe: `jcmd <pid> VM.native_memory summary` repeatedly (look at the `Other` category growing; verify category name against current docs). Compare with `ps -o rss= -p <pid>` rising while the heap stays small (`jcmd <pid> GC.heap_info`). End state: `OutOfMemoryError: Direct buffer memory`.
   - Explain: direct buffers live outside the heap and are freed only when their owner object is collected or released; `-Xmx` does not cap them.
   - Fix: bound and reuse buffers (pool), release deterministically. Regression: a test asserting your pool never exceeds N buffers.
4. (30m) **RL-22 metaspace/classloader growth.**
   ```java
   List<Class<?>> keep = new ArrayList<>();
   for (int i = 0; ; i++) {
     Class<?> c = new net.bytebuddy.ByteBuddy().subclass(Object.class).name("gen.C" + i)
         .make().load(new ClassLoader(null) {}, net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.WRAPPER).getLoaded();
     keep.add(c);
   }
   ```
   - Run with `-XX:MaxMetaspaceSize=64m -Xlog:gc*:file=meta.log`.
   - Observe: `OutOfMemoryError: Metaspace`; before it dies run `jcmd <pid> VM.metaspace` and `jcmd <pid> VM.classloader_stats` (verify subcommands against current docs) and note loader count rising.
   - Explain: class metadata is freed only when its classloader is unreachable; holding classes (or loaders) keeps metaspace pinned; frameworks that generate proxies per request can leak this way.
   - Fix: stop retaining classes/loaders (clear `keep`), cache generated classes. Regression: assert loader count stays flat after N iterations by watching `ManagementFactory.getClassLoadingMXBean().getLoadedClassCount()`.
5. (15m) Write ledger entries for RL-19, RL-21, RL-22 (symptom, mechanism, repro, evidence, fix, prevention, 1-minute answer).

**Tips and common problems**
- `jcmd` says "Unable to open socket file" or finds no process → run it as the same OS user as the JVM; in Docker use `docker exec` and run `jcmd` inside the container.
- G1 shows no humongous lines → region size may be larger than your arrays → use `-XX:G1HeapRegionSize=1m` or bigger arrays.
- NMT output is overwhelming → use `summary` and `jcmd <pid> VM.native_memory summary.diff` after a baseline (`baseline` first).
- ByteBuddy lab dies instantly with a different error → you may have run out of heap instead; set `-Xmx512m` so metaspace is the limit hit.

**Done when**
- [ ] Each lab has a failing run, an evidence file (log or jcmd output), and a fix with a regression check.
- [ ] Three ledger entries written with 1-minute answers.

#### S4 Review + Write (2h): consolidate JVM memory
1. (20m) Finalise the ledger entries; read each answer aloud once with a timer (≤ 1 minute).
2. (25m) Redraw the JVM memory map from memory; annotate each area with the flag that sizes it and the exception/symptom when it is exhausted.
3. (20m) Make flashcards for the three labs and for the `happens-before` rules.
4. (30m) Write a 1-page explanation of `happens-before` with a code example that fails without it (non-volatile flag, safe publication via `final`).
5. (15m) Open PR with lab code, `docs/perf/baseline.md` and ledger.
6. (10m) Retro (`retro.md`): worked, blocked, change.

**Done when**
- [ ] You can draw the memory map and name the flag per area from memory.
- [ ] PR merged; retro written.

**Self-check:** What is `happens-before`? Why is a high allocation rate bad even if there's no leak? What lives outside the heap?
<details><summary>Answers</summary>

- `happens-before` is the JMM guarantee that one action's effects are visible to another (e.g., unlock before later lock of the same monitor, volatile write before later read, `Thread.start` before the thread's actions).
- High allocation rate forces frequent GC, burning CPU and increasing pause/latency variance even when nothing leaks.
- Metaspace, thread stacks, code cache, direct/native buffers, GC data structures and JNI allocations.
</details>

**Output:** baseline report, RL-19/21/22 entries.

---

## Week 29 — Threads, executors and locks
**Outcome:** writes correct concurrent code with the standard tools and recognises races and deadlocks.
**Before you start:** add JMH to `jvm-labs` (JMH samples' Maven archetype or dependencies); confirm a hello benchmark runs.
**If stuck:** reduce the race to two threads and a shared counter; add `Thread.sleep` to widen the window.

#### S1 Learn (3h): concurrency foundations
1. (30m) JCIP ch. 1–2: what thread safety means, atomicity, race conditions, compound actions. Write the definition of "thread-safe class" in your own words.
2. (30m) JCIP ch. 3: visibility, safe publication, immutability, `volatile`. Write when `volatile` is enough and when it isn't.
3. (30m) JCIP ch. 5 (building blocks): `ConcurrentHashMap`, `BlockingQueue`, latches, semaphores. List which `ConcurrentHashMap` methods are atomic (`putIfAbsent`, `computeIfAbsent`, `merge`).
4. (30m) JCIP ch. 6–8 (tasks, executors, cancellation, pool sizing). Write the formula `N threads = cores × (1 + wait/compute)` and compute for one IO-bound and one CPU-bound case.
5. (25m) JCIP ch. 10, 13, 15 skim: deadlock conditions, lock ordering, `ReentrantLock` features (tryLock, fairness), atomics and CAS.
6. (25m) EJ Items 78–84 (synchronize shared mutable data, avoid excessive synchronization, prefer executors, concurrency utilities over wait/notify, document thread safety, lazy init). Write a one-line rule per item.
7. (10m) Flashcards for 4 deadlock conditions and the rules above.

**Tips and common problems**
- JCIP feels dense → read the bold rules and the listings first, then the prose → don't try to memorise every chapter.
- "Why not just `synchronized` everywhere?" → contention and deadlocks → note performance vs correctness trade-off in your cards.

**Done when**
- [ ] You can state the four deadlock conditions and a lock-ordering fix.
- [ ] You can size a pool for CPU-bound vs IO-bound work with the formula.

#### S2 Build (3h): SF-23 thread-safe token-bucket rate limiter
1. (20m) Create `ratelimit` package: `interface RateLimiter { boolean tryAcquire(); }`. Constructor takes capacity, refill per second, and a `LongSupplier nanoTime` so tests control time. Write the spec as tests first: starts full; denies when empty; refills over time; never exceeds capacity.
2. (30m) Implement `SyncTokenBucket` with a `synchronized` method:
   ```java
   public synchronized boolean tryAcquire() {
     long now = nanos.getAsLong();
     tokens = Math.min(capacity, tokens + (now - last) * refillPerNano);
     last = now;
     if (tokens >= 1) { tokens -= 1; return true; }
     return false;
   }
   ```
   Make the single-threaded tests pass.
3. (30m) Write the concurrency test: 32 threads started together via `CountDownLatch`, each calling `tryAcquire()` 10 000 times with a fake clock that never advances; assert granted count == capacity exactly.
4. (30m) Implement `AtomicTokenBucket`: pack state or use `AtomicReference<State>` with a CAS loop (immutable `State(tokens,last)`). Run the same tests.
5. (25m) Implement `LockTokenBucket` using `ReentrantLock` with `tryLock` variant if you like; run the same tests.
6. (35m) JMH benchmarks for the three versions: `@State(Scope.Benchmark)`, `@Threads(1)` and `@Threads(8)`, `@Warmup(iterations=3)`, `@Measurement(iterations=5)`, `@Fork(2)`. Record ops/s in a table. Add a conclusion with the trade-offs.
7. (10m) PR with the best implementation chosen by evidence and an ADR-style note.

**Tips and common problems**
- Concurrency test passes even with a broken implementation → threads rarely overlap on tiny loops → use a start gate (`CountDownLatch`), many iterations, and run the test repeatedly (`@RepeatedTest(50)`).
- JMH results are dominated by dead-code elimination → return the result or use a `Blackhole` → never ignore the returned boolean.
- JMH numbers are noisy → run with `-f 2 -wi 3 -i 5` and close other apps; compare shapes, not exact values.
- Using `System.nanoTime()` directly in tests → flaky timing → inject the `LongSupplier`.

**Done when**
- [ ] All three implementations pass the same stress test 50 times in a row.
- [ ] A benchmark table exists and the chosen implementation is justified.

#### S3 Lab (2h): RL-10 race, RL-11 visibility, RL-12 deadlock, RL-13 pool exhaustion
1. (10m) Create package per lab in `jvm-labs`. Prepare a helper `runInParallel(n, task)` that starts n threads on a latch.
2. (25m) **RL-10 check-then-act race.**
   ```java
   Map<String,Integer> m = new ConcurrentHashMap<>();
   AtomicInteger computes = new AtomicInteger();
   Runnable r = () -> { if (!m.containsKey("k")) { m.put("k", computes.incrementAndGet()); } };
   ```
   - Failing test: run `r` in 100 threads; assert `computes.get() == 1` (it will often be > 1).
   - Observe: print `computes`; repeat 100 times to see the distribution.
   - Explain: `containsKey` + `put` are individually atomic but the sequence is not; two threads both see "absent".
   - Fix: `m.computeIfAbsent("k", k -> computes.incrementAndGet())`. Regression: same test now always passes.
3. (20m) **RL-11 visibility.**
   ```java
   static boolean stop; // not volatile
   Thread t = new Thread(() -> { while (!stop) { } });
   t.start(); Thread.sleep(100); stop = true; t.join(2000);
   ```
   - Failing test: `assertTimeout(Duration.ofSeconds(3), ...)` should pass; it hangs on some JVMs/flags after JIT compiles the loop.
   - Observe: `top -H -p <pid>` shows one thread at 100% CPU; `jstack`/`jcmd <pid> Thread.print` shows it RUNNABLE in the lambda. If it doesn't reproduce, run longer or with `-Xcomp` (verify) and note that non-reproduction doesn't prove safety.
   - Explain: without `volatile` there is no `happens-before`, so the JIT may hoist the read out of the loop.
   - Fix: `volatile boolean stop`. Regression: the timeout test passes reliably.
4. (30m) **RL-12 deadlock.**
   ```java
   Object a = new Object(), b = new Object();
   new Thread(() -> { synchronized (a) { sleep(100); synchronized (b) { } } }, "T1").start();
   new Thread(() -> { synchronized (b) { sleep(100); synchronized (a) { } } }, "T2").start();
   ```
   - Failing test: use `ThreadMXBean.findDeadlockedThreads()` after 1 s and assert it is `null` (it won't be).
   - Observe: `jcmd <pid> Thread.print` and look for the section `Found one Java-level deadlock:` naming both threads and the locks they hold/wait for. Also `jstack <pid>`.
   - Explain: the four conditions (mutual exclusion, hold and wait, no preemption, circular wait) all hold.
   - Fix: acquire locks in a global order (e.g., by `System.identityHashCode` with a tie-breaker) or use `tryLock` with timeout. Regression: the same test now finds no deadlock.
5. (30m) **RL-13 thread-pool exhaustion.**
   ```java
   ExecutorService pool = Executors.newFixedThreadPool(4);
   for (int i = 0; i < 100; i++) pool.submit(() -> sleep(5_000)); // simulated blocking call
   Future<String> health = pool.submit(() -> "ok"); // waits behind the queue
   ```
   - Failing test: `health.get(1, SECONDS)` should succeed; it times out.
   - Observe: thread dump shows 4 threads `pool-1-thread-N` in `TIMED_WAITING` at `Thread.sleep`; the queue size (`((ThreadPoolExecutor) pool).getQueue().size()`) is ~96. With Spring: `executor.queued` / `tomcat.threads.busy` metrics.
   - Explain: all workers are blocked, so unrelated fast tasks starve (head-of-line blocking).
   - Fix: separate pools (bulkhead) for slow vs fast work, bounded queue with a rejection policy, timeouts on the blocking call, pool sized from the formula. Regression: health check on its own pool succeeds while the slow pool is saturated.
6. (5m) Ledger entries (draft) for all four.

**Tips and common problems**
- Thread dump is huge → search for `BLOCKED`, `waiting to lock`, `Found one Java-level deadlock` and your thread names; give threads names (`new Thread(r, "T1")`).
- `jstack` fails in containers → needs same user/namespace → exec into the container.
- Lab "works" because the machine is too fast → add a small `sleep` between check and act to widen the window.
- Don't "fix" races with `synchronized` on a different object per thread → the lock object must be shared.

**Done when**
- [ ] Each lab has a failing test that turns green with the fix.
- [ ] For RL-12 you have pasted the `Found one Java-level deadlock` section in the ledger.

#### S4 Review + Write (2h): consolidate concurrency
1. (40m) Complete four ledger entries and say each answer aloud in ≤ 1 minute.
2. (20m) Write "Deadlock debugging recipe": take dump (2–3 times, 5 s apart) → find BLOCKED threads → map lock owners → pick the cycle → decide fix (order / timeout / redesign).
3. (25m) Write "Pool sizing notes": Little's law `L = λ × W` with numbers (200 req/s × 0.05 s = 10 in flight), the CPU/IO formula, and why bounded queues matter.
4. (15m) Flashcards for the labs and EJ rules.
5. (10m) PR with labs, limiter and notes.
6. (10m) Retro.

**Done when**
- [ ] The two notes exist in `docs/notes/`.
- [ ] PR merged.

**Self-check:** Why is `volatile` not enough for `count++`? How do you size a thread pool for CPU-bound vs IO-bound work? What four conditions create a deadlock?
<details><summary>Answers</summary>

- `count++` is read-modify-write; `volatile` only gives visibility/ordering, not atomicity. Use `AtomicInteger`, a lock, or `LongAdder`.
- CPU-bound: about the number of cores (+1). IO-bound: cores × (1 + wait/compute), or use virtual threads; always bound the queue.
- Mutual exclusion, hold and wait, no preemption, circular wait.
</details>

**Output:** SF-23, benchmarks, RL-10/11/12/13 entries.

---

## Week 30 — Virtual threads, structured concurrency and reactive awareness
**Outcome:** knows when virtual threads, platform threads or reactive fit.
**Before you start:** have JDK 25 and JDK 21 both installed via SDKMAN (`sdk list java`) for the pinning lab.
**If stuck:** compare behaviour on JDK 21 vs 25 before assuming your code is wrong; the platform changed.

#### S1 Learn (3h): virtual threads, scoped data and reactive
1. (40m) Read JEP 444 (Virtual Threads). Write: what is mounted/unmounted, what a carrier thread is, why blocking IO becomes cheap, and the three "do not" rules (don't pool them, avoid long `synchronized`/native pinning on old JDKs, don't rely on `ThreadLocal` for big data).
2. (20m) Read JEP 491 (Synchronize Virtual Threads without Pinning, JDK 24). Note exactly what changed and what can still pin (native frames/foreign calls; verify against the JEP).
3. (25m) Read the structured concurrency JEP (preview in recent JDKs; check its status). Write a 6-line example with a scope that forks two subtasks and joins them.
4. (15m) Read about `ScopedValue` vs `ThreadLocal` (same JEP family). Write when you'd prefer each.
5. (15m) Spring Boot reference "Virtual threads": `spring.threads.virtual.enabled=true`; what it changes (Tomcat executor, `@Async`, etc.).
6. (40m) Project Reactor reference "Learn" section: `Mono`/`Flux`, cold vs hot, backpressure, `subscribeOn/publishOn`. Write why reactive helps with many slow connections and costs debugging/complexity.
7. (25m) Write a decision table: workload (CPU-bound / blocking IO / streaming with backpressure / high fan-out) → platform threads / virtual threads / WebFlux.

**Tips and common problems**
- Believing virtual threads are faster → they raise concurrency, not single-task speed → CPU-bound work gains nothing.
- Unbounded virtual-thread concurrency overwhelms the DB or downstream → limits move from threads to resources → use semaphores/pool limits.
- Reading too much Reactor → you only need awareness this week → stop after the decision table.

**Done when**
- [ ] The decision table exists.
- [ ] You can explain pinning and what changed in JDK 24.

#### S2 Build (3h): SF-24 bulk import of 1M rows
1. (15m) Generate a 1M-row CSV: `seq 1 1000000 | awk '{printf "SKU-%d,Product %d,%d.99\n",$1,$1,($1%100)+1}' > products.csv`.
2. (25m) Write a streaming reader with `BufferedReader` or `Files.lines` (never `readAllLines`). Parse into records and validate; count rejects.
3. (35m) Add a batch writer using JDBC `PreparedStatement.addBatch()` with batch size 1000 and `executeBatch()` inside one transaction per batch. Add `reWriteBatchedInserts=true` to the Postgres JDBC URL (verify).
4. (30m) Connect reader and writer through an `ArrayBlockingQueue<List<Row>>` of capacity 10 (backpressure). Producer blocks when the queue is full.
5. (30m) Run with `-Xmx256m` and watch heap (`jcmd <pid> GC.heap_info`, or JFR). It must finish without `OutOfMemoryError`. Record time.
6. (30m) Compare executors: fixed platform pool (4 writers) vs `Executors.newVirtualThreadPerTaskExecutor()` limited by a `Semaphore(4)`. Record time and CPU.
7. (15m) Compare against Postgres `COPY` (psql `\copy` or `CopyManager`) and note the difference.

**Tips and common problems**
- Import is slow → one commit per row or no batching → batch and commit per 1000.
- Memory still grows → the queue is unbounded or you hold all rows in a list → use bounded queue and flush per batch.
- Virtual version is not faster → the DB is the bottleneck → keep the concurrency limit at DB-sized values.

**Done when**
- [ ] 1M rows import with `-Xmx256m` without OOM.
- [ ] A table of timings for platform vs virtual vs `COPY` exists.

#### S3 Lab (2h): RL-16 parallel stream, RL-17 pinning, RL-18 interrupts
1. (10m) Add lab packages; check JDK versions with `java -version` per terminal (`sdk use java 21...`).
2. (35m) **RL-16 `parallelStream` with blocking IO.**
   ```java
   long t0 = System.nanoTime();
   IntStream.range(0, 100).parallel().forEach(i -> sleep(1000)); // "HTTP call"
   System.out.println((System.nanoTime() - t0) / 1_000_000 + " ms");
   ```
   - Failing test: assert that 100 one-second calls finish in under 10 s (they don't on a small CPU count).
   - Observe: elapsed ≈ 100 / (cores − 1) seconds; thread dump shows `ForkJoinPool.commonPool-worker-N` all sleeping; other code using the common pool (e.g., `CompletableFuture.supplyAsync`) stalls.
   - Explain: the common pool has `cores − 1` threads and is shared JVM-wide; blocking tasks starve everything using it.
   - Fix: use a dedicated executor (or virtual threads) for IO. Regression: the same workload completes in ≈ ceil(100 / poolSize) seconds, and unrelated common-pool work stays fast.
3. (35m) **RL-17 virtual-thread pinning.**
   ```java
   static final Object LOCK = new Object();
   try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
     for (int i = 0; i < 200; i++) ex.submit(() -> { synchronized (LOCK) { sleep(1000); } });
   }
   ```
   - On **JDK 21**: run with `-Djdk.tracePinnedThreads=full`; observe stack traces showing the pinned frame; with JFR look at `jdk.VirtualThreadPinned` events. The 200 tasks serialise anyway because of the single lock; to see carrier starvation, run more tasks than carriers each holding a different lock while blocking.
   - On **JDK 24+/25**: `synchronized` no longer pins; note that the same program shows no pinned events. Pinning can still happen with native frames (verify the JEP's remaining cases).
   - Explain: pinning keeps the carrier thread blocked while the virtual thread waits.
   - Fix (for JDK 21): replace `synchronized` around blocking calls with `ReentrantLock`. Regression: no `VirtualThreadPinned` events in JFR.
4. (30m) **RL-18 ignoring interrupts.**
   ```java
   ExecutorService ex = Executors.newSingleThreadExecutor();
   Future<?> f = ex.submit(() -> { while (true) { try { Thread.sleep(100); } catch (InterruptedException e) { /* swallowed */ } } });
   f.cancel(true);
   ex.shutdownNow();
   System.out.println(ex.awaitTermination(2, TimeUnit.SECONDS)); // false
   ```
   - Failing test: assert `awaitTermination` returns true.
   - Observe: `jcmd <pid> Thread.print` still shows `pool-1-thread-1` alive after cancel.
   - Explain: cancellation in Java is cooperative; swallowing `InterruptedException` clears the flag and the task never stops.
   - Fix: on `InterruptedException` call `Thread.currentThread().interrupt()` and return. Regression: test now passes.
5. (10m) Ledger drafts for all three.

**Tips and common problems**
- Can't reproduce pinning on JDK 25 → expected, `synchronized` no longer pins → state this in the ledger and keep the JDK 21 repro.
- `sleep` helper throws checked exceptions → write a tiny `Sleep.ms(long)` that restores the interrupt flag.
- `parallelStream` timing varies → machine core count → print `ForkJoinPool.commonPool().getParallelism()` in the output.

**Done when**
- [ ] Each lab has a failing test, an observation (timing, JFR event or thread dump) and a fix.
- [ ] RL-17 notes both JDK 21 and 25 behaviour.

#### S4 Review + Write (2h): ADR-013 and consolidation
1. (40m) Write ADR-013 "Virtual threads on, WebFlux off, and the conditions to revisit": context, decision, consequences, and the triggers to revisit (e.g., streaming backpressure needs, huge fan-out, CPU-bound pipelines).
2. (30m) Finish three ledger entries; rehearse aloud.
3. (15m) Flashcards: pinning, carrier thread, cooperative cancellation, common pool size.
4. (20m) Write the decision table into `docs/notes/threading-models.md` (from S1 step 7).
5. (10m) PR.
6. (5m) Retro.

**Done when**
- [ ] ADR-013 merged with a "revisit when" section.

**Self-check:** Why don't virtual threads make CPU-bound work faster? What is backpressure? What replaces `ThreadLocal` for scoped data?
<details><summary>Answers</summary>

- CPU-bound work is limited by cores; virtual threads only help when threads spend time waiting (blocking IO).
- Backpressure is a mechanism for a slow consumer to signal a fast producer to slow down, preventing unbounded buffering.
- `ScopedValue` (immutable, bounded-lifetime data bound to a scope).
</details>

**Output:** SF-24, ADR-013, RL-16/17/18 entries.

---

## Week 31 — Profiling, leaks and load testing
**Outcome:** finds leaks and hot paths with tools and measures latency correctly.
**Before you start:** install Eclipse MAT, async-profiler (check platform support), and confirm `k6 run` works. Make a `heap-dumps/` folder outside git.
**If stuck:** open the heap dump with the "Leak Suspects" report first, then the dominator tree sorted by retained heap.

#### S1 Learn (3h): profiling and latency measurement
1. (30m) Brendan Gregg: the USE method and flame graphs (his site). Write USE for CPU, memory, disk, network, and thread pools; and how to read a flame graph (width = time, top = leaf).
2. (50m) Watch Gil Tene, "How NOT to Measure Latency". Write 5 bullets: percentiles, coordinated omission, why averages lie, open vs closed load.
3. (30m) JFR and JMC docs: start a recording (`jcmd <pid> JFR.start name=r settings=profile duration=60s filename=r.jfr`), what the Hot Methods and Memory tabs show.
4. (25m) MAT introduction: heap dump (`jcmd <pid> GC.heap_dump /path/dump.hprof`), dominator tree, retained vs shallow size, Leak Suspects.
5. (25m) k6 docs "Scenarios" (executors: `constant-vus`, `constant-arrival-rate`, `ramping-arrival-rate`) and "Thresholds".
6. (15m) Google SRE book ch. 6 (Monitoring Distributed Systems): the four golden signals.
7. (5m) Write 6 flashcards.

**Tips and common problems**
- Heap dump is gigabytes → it's the size of the live heap → reproduce with `-Xmx256m` so dumps stay manageable.
- Profiling in a container fails → perf events restricted → for async-profiler try `wall` or `itimer` mode, or use JFR (verify options in the async-profiler README).
- Reading flame graphs by colour → colours are arbitrary → read by width and stack.

**Done when**
- [ ] You can explain coordinated omission in two sentences.
- [ ] You can name when to use JFR vs async-profiler vs heap dump.

#### S2 Build (3h): SF-25 load-test baseline and one hotspot fix
1. (25m) Write `perf/browse.js` with an open-model scenario:
   ```javascript
   export const options = {
     scenarios: { browse: { executor: 'constant-arrival-rate', rate: 50, timeUnit: '1s',
       duration: '3m', preAllocatedVUs: 50, maxVUs: 200 } },
     thresholds: { http_req_duration: ['p(95)<300', 'p(99)<800'], http_req_failed: ['rate<0.01'] },
   };
   ```
   Flow: list products → get product.
2. (25m) Write `perf/checkout.js`: login (or use a test token), add to cart, place order. Use unique idempotency keys.
3. (30m) Run both against a warmed app; export `k6 run --summary-export=perf/browse-summary.json perf/browse.js`.
4. (30m) Record JFR during a run; find the top 5 hot methods and top allocation sites.
5. (30m) Capture a flame graph with async-profiler (`asprof -d 30 -e cpu -f flame.html <pid>`; verify the command name/flags in the README). Identify one hotspot owned by ShopFlow (e.g., JSON serialisation, a loop, an extra query).
6. (30m) Fix the hotspot (cache, query fix, avoid repeated work), rerun the same scenario, compare p95/p99 and CPU.
7. (10m) Commit scripts and a short report `docs/perf/sf-25.md` with before/after and the evidence.

**Tips and common problems**
- k6 runs out of VUs ("dropped iterations") → arrival rate exceeds what `maxVUs` can serve → raise `maxVUs` or accept it as a signal of saturation.
- Results differ run to run → no warmup → add a 60 s warmup stage or discard the first minute.
- Load generator on the same laptop skews results → note it; where possible run k6 in a separate container with limits.

**Done when**
- [ ] k6 thresholds are encoded and the scripts run with one command.
- [ ] `docs/perf/sf-25.md` shows a before/after with a flame graph or JFR evidence.

#### S3 Lab (2h): RL-14, RL-15, RL-95, RL-96, RL-24
1. (5m) Prepare packages; run JVMs with `-Xmx256m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=heap-dumps/`.
2. (25m) **RL-14 `ThreadLocal` leak.**
   ```java
   static final ThreadLocal<byte[]> TL = new ThreadLocal<>();
   ExecutorService p = Executors.newFixedThreadPool(4);
   for (int i = 0; i < 1000; i++) p.submit(() -> TL.set(new byte[10_000_000]));
   ```
   - Failing test: after the tasks finish and a GC, assert used heap < 20 MB (it stays ~40 MB, or OOM with bigger values).
   - Observe: `jcmd <pid> GC.heap_dump heap-dumps/tl.hprof`; in MAT open the dominator tree: `Thread` objects → `threadLocals` → `ThreadLocalMap$Entry` retaining the byte arrays.
   - Explain: pooled threads live forever so their `ThreadLocalMap` entries stay reachable until `remove()`.
   - Fix: `try { TL.set(...); ... } finally { TL.remove(); }` or use scoped values. Regression: heap stays flat.
3. (25m) **RL-15 static cache leak.**
   ```java
   static final Map<String, byte[]> CACHE = new HashMap<>();
   for (int i = 0; ; i++) CACHE.put("k" + i, new byte[100_000]);
   ```
   - Observe: run until `OutOfMemoryError: Java heap space`; open the auto-dumped `.hprof` in MAT → Leak Suspects shows the `HashMap` retaining ~99%; the GC root is the class's static field.
   - Explain: GC can't free what's reachable from a GC root, even if you will never read it again.
   - Fix: bounded cache (Caffeine `maximumSize`/`expireAfterWrite`) or `LinkedHashMap` with `removeEldestEntry`. Regression: size never exceeds the bound in a test.
4. (25m) **RL-95 coordinated omission.**
   - Build a tiny HTTP handler that stalls for 5 s once per minute (`if (now % 60 == 0) sleep(5000)`).
   - Run k6 twice: closed model (`constant-vus`, 10 VUs) and open model (`constant-arrival-rate` 50/s).
   ```javascript
   // open model
   scenarios: { open: { executor: 'constant-arrival-rate', rate: 50, timeUnit: '1s', duration: '2m', preAllocatedVUs: 100 } }
   ```
   - Observe: closed model reports few slow requests (VUs waited); open model reports hundreds of slow ones, matching real users.
   - Explain: in a closed loop the tool stops sending while the server is stalled, hiding the queue that real arrivals would create.
   - Fix: use arrival-rate executors and report percentiles from them.
5. (20m) **RL-96 latency knee.**
   - Server with fixed capacity (e.g., 4 worker threads × 50 ms ≈ 80 req/s). Run `ramping-arrival-rate` from 10 to 120 req/s.
   - Observe: p95 stays flat until ~70–80% utilisation, then explodes; queue/thread-pool metrics climb.
   - Explain: queueing theory; waiting time grows non-linearly as utilisation → 100%.
   - Fix: capacity headroom, load shedding, autoscaling triggers before the knee.
6. (10m) **RL-24 logging cost.**
   ```java
   log.debug("order " + order.expensiveToString());          // always builds the string
   log.debug("order {}", order);                            // builds only if DEBUG is on
   ```
   - Measure with a small JMH benchmark at INFO level; the concatenation version is much slower and allocates.
   - Fix: parameterised logging, or guard with `isDebugEnabled()` for costly arguments.
7. (10m) Ledger drafts for all five.

**Tips and common problems**
- MAT shows "Unreachable objects" noise → a dump was taken before GC → MAT can discard unreachable objects (default); trust the dominator tree.
- `OutOfMemoryError` in the lab JVM happens before your measurement → lower allocations or raise heap → keep dumps small.
- Latency graph is noisy → fixed delays with `sleep` are fine for the lab; do not read absolute values.

**Done when**
- [ ] A `.hprof` analysis screenshot or notes exist for RL-14 and RL-15.
- [ ] Two k6 reports show the closed vs open difference.
- [ ] A latency-vs-rate table shows the knee.

#### S4 Review + Write (2h): investigation template and consolidation
1. (30m) Write `docs/templates/perf-investigation.md`: symptom → metric → hypothesis → profile → fix → verify (with an example filled from SF-25).
2. (40m) Finish five ledger entries; practise the answers aloud.
3. (15m) Flashcards: coordinated omission, retained size, GC root, USE method.
4. (20m) Write a 1-page note "Why latency explodes near saturation" with a worked example using utilisation `ρ` and `1/(1−ρ)`.
5. (10m) PR.
6. (5m) Retro.

**Done when**
- [ ] The template is used once on a real finding.
- [ ] Five entries added.

**Self-check:** Why is average latency misleading? What does the dominator tree show? Why does latency explode near 100% utilization?
<details><summary>Answers</summary>

- Averages hide the tail; a few very slow requests dominate user experience and are averaged away.
- It shows which objects keep the most memory alive (retained size) and the path to a GC root.
- Queues grow non-linearly as arrival rate approaches service capacity (waiting time ~ ρ/(1−ρ)).
</details>

**Output:** SF-25 report, flame graph, RL-14/15/24/95/96 entries.

---

## Week 32 — Consolidation and Phase 5 gate
**Outcome:** diagnoses unseen faults from evidence.
**Before you start:** find a peer to swap mystery apps with; if none, wait a few days and diagnose your own apps cold (`cat` nothing, use only tools).
**If stuck:** start with metrics and a thread dump before any heap dump.

#### S1 Learn (3h): Spring concurrency and review
1. (30m) Spring reference "Task Execution and Scheduling": `TaskExecutor`, `ThreadPoolTaskExecutor` properties (core, max, queue capacity, rejection policy).
2. (25m) `@Async` rules: needs `@EnableAsync`, proxy-based so self-invocation is not async, return `void`/`Future`/`CompletableFuture`, `AsyncUncaughtExceptionHandler`. Write a checklist.
3. (25m) `@Scheduled` pitfalls: default single-threaded scheduler, `fixedRate` vs `fixedDelay`, runs on every instance (link to ShedLock).
4. (30m) Graceful shutdown of executors: `setWaitForTasksToCompleteOnShutdown(true)`, `setAwaitTerminationSeconds`, Boot `server.shutdown=graceful`.
5. (50m) Review Phase 5: write a 1-page summary from memory (memory map, pool sizing, deadlock recipe, GC reading, latency rules). Compare with your notes; fix gaps.
6. (20m) List unfinished Phase 4 and Phase 5 lab items by priority (impact on interviews and incidents).

**Tips and common problems**
- `@Async` method runs synchronously → called from within the same class → move it to another bean.
- Scheduled job never runs in tests → `@EnableScheduling` missing or app context not loaded → add the annotation to config.

**Done when**
- [ ] A one-page review exists and a prioritised leftover-labs list is written.

#### S2 Build (3h): three mystery apps
1. (15m) Create `mystery/skeleton`: a small Spring Boot app with Actuator (`health,metrics,threaddump,heapdump` exposed in dev), a few endpoints, a `run.sh` with sensible JVM flags (`-Xmx256m`, GC log, HeapDump on OOM).
2. (50m) **App A (leak).** Hide a leak that is not a plain static map (e.g., a listener list that never unregisters, or a per-request `ThreadLocal`). Use neutral names. Add a load script that triggers it over ~10 minutes.
3. (50m) **App B (pool exhaustion).** A bounded executor used for both a fast endpoint and a slow downstream call (simulated with a delay). Under load the fast endpoint degrades.
4. (50m) **App C (GC pause).** A hot path that allocates large short-lived objects with a small heap, producing long pauses and latency spikes.
5. (15m) Write sealed `SOLUTION.md` for each (hidden fault, expected evidence, fix); put it in a separate folder to hand over only after the attempt.

**Tips and common problems**
- Fault too obvious → rename classes and methods to something neutral; do not leave comments.
- Fault not reproducible quickly → include the load script and the time needed.
- A mystery app crashes immediately → it must run for at least 10 minutes under load to be diagnosable.

**Done when**
- [ ] Each app runs with one command and shows a clear symptom within 10 minutes of load.
- [ ] Solutions are written but not visible to the diagnoser.

#### S3 Lab (2h): RL-35 and leftover items
1. (10m) Create a Spring Boot lab with Actuator and Micrometer enabled.
2. (35m) **RL-35 `@Async` default executor.**
   ```java
   @Async public void send() { sleep(5000); }   // called 10_000 times in a loop
   ```
   - Failing test/script: fire 10 000 calls and assert queue length/thread count stays bounded.
   - Observe: in Spring Boot the auto-configured `applicationTaskExecutor` is a `ThreadPoolTaskExecutor` with core size 8 and an **unbounded queue** (default; verify against current docs), so the backlog grows in memory (check `executor.queued`, `executor.active` metrics). Without Boot's auto-config, Spring falls back to `SimpleAsyncTaskExecutor`, which creates a **new thread per task**. Try both and note the difference.
   - Explain: defaults favour never rejecting work; under bursts this means unbounded memory or threads.
   - Fix: configure `spring.task.execution.pool.core-size`, `max-size`, `queue-capacity=100` and a rejection policy via a custom `ThreadPoolTaskExecutor` bean with `CallerRunsPolicy`. Regression: burst test shows bounded queue and either back-pressure or rejection.
3. (60m) Roll over leftover items from your prioritised list (Phase 4/5). For each: failing test → observe → fix → ledger line.
4. (15m) Complete ledger entries.

**Tips and common problems**
- Metrics don't show executor data → ensure the executor is a Spring bean and Micrometer is on the classpath → check `/actuator/metrics`.
- Rejection throws `TaskRejectedException` and you didn't handle it → decide: caller-runs, drop, or return 429/503.

**Done when**
- [ ] RL-35 shows bounded queue after the fix.
- [ ] At least two leftover items are closed with ledger entries.

#### S4 Review + Write (2h): Phase 5 gate session
1. (10m) Rules: you have 30 minutes per app; allowed tools are metrics, thread dumps, heap dumps, GC logs, JFR; no reading source before you have a hypothesis supported by evidence.
2. (30m) Diagnose app A (peer's or your own cold). Write: symptom, evidence, hypothesis, root cause, fix.
3. (30m) Diagnose app B the same way.
4. (30m) Diagnose app C the same way.
5. (20m) Compare with `SOLUTION.md`; write gaps; add alerts or regression tests for each fix; fill the tracker, tag `phase-5`, retro.

**Tips and common problems**
- Jumping to code → resist; first get a thread dump and one metric view.
- Heap dump too large → capture earlier or reduce `-Xmx` for the lab.

**Gate rubric (pass all):**
- Each fault diagnosed with evidence (thread dump, heap dump, GC log, metric), not by reading the code.
- Fix proposed with a regression test or alert.
- She can explain `happens-before`, pool sizing and the latency knee without notes.

**Done when**
- [ ] Three diagnoses written with evidence.
- [ ] `phase-5` tag created; tracker updated.

**Self-check:** Which evidence would you collect first for a service that is "slow but CPU idle"? When is `CallerRunsPolicy` a good choice? What do you do if a heap dump is too big to open?
<details><summary>Answers</summary>

- A thread dump (blocked/waiting threads), pool and connection-pool metrics, and downstream latency; CPU idle suggests waiting rather than computing.
- When you want natural backpressure: the submitting thread slows down instead of work being dropped or queued without limit.
- Reproduce with a smaller heap, increase MAT's memory (`MemoryAnalyzer.ini`), or use a class histogram (`jcmd <pid> GC.class_histogram`) to find suspects first.
</details>

**Output:** `phase-5` tag, ADR-013, ledger up to ~RL-35 plus extras.
