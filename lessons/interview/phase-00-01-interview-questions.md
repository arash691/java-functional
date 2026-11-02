# Interview Questions: Phase 0–1 (weeks 1–9)

A deeper question bank for each week of `lessons/phase-00-01-bridge-and-java.md`. The questions go from basic to the tricky ones that separate a junior from a strong candidate. The answers are written to **teach**, not only to give the "right line": read them even when you got the question right.

## How to use this file
1. **Pass 1, aloud.** Read only the question. Answer in 60 to 90 seconds as if in an interview. Record yourself for two or three of them.
2. **Pass 2, open the answer.** Mark each question green (complete), yellow (missed a part) or red (did not know). Write one flashcard for every yellow or red.
3. **Pass 3, prove it.** For every question marked **[Prove it]**, write the 10 to 20 lines of Java or the command that demonstrates the answer and run it. Understanding something you have seen fail sticks far longer.
4. Revisit the red questions one week later.

Levels: **[Core]** every candidate must know it; **[Intermediate]** expected for a one-to-two-year developer; **[Hard]** tricky details, asked to find the ceiling of your knowledge.

Answers come from the author's knowledge and the Java platform as of JDK 21 to 25. Verify details you plan to rely on against the current documentation.

---

## Week 1: JVM, toolchain, Maven and how a request travels

<details><summary><b>1. [Core] JDK, JRE, JVM: what is each, and where does <code>javac</code> fit?</b></summary>

The **JVM** loads and runs bytecode. The **JRE** was the JVM plus the standard class libraries. The **JDK** adds developer tools: the compiler `javac`, `java`, `javap`, `jshell`, `jar`, `jcmd` and more. Since Java 11 there is no separate JRE download for most vendors; you install a JDK, and `jlink` can build a small custom runtime for deployment. `javac` is a front end that turns `.java` text into platform-neutral `.class` bytecode; the JVM is the back end that runs it.
</details>

<details><summary><b>2. [Core] What exactly happens between <code>java Hello</code> and the first line of output?</b></summary>

The launcher creates a JVM. The **class loader** locates `Hello.class` on the classpath. The class is **linked**: *verification* (the bytecode is safe and well formed), *preparation* (static fields get default values) and *resolution* (symbolic references are resolved, often lazily). Then **initialization** runs static initializers. Finally the JVM looks up `public static void main(String[])` and calls it. Class loaders delegate to their parent first (bootstrap → platform → application), which is why you cannot replace `java.lang.String` with your own class.
</details>

<details><summary><b>3. [Intermediate] Is Java interpreted or compiled? Why is the first request often slower than the thousandth?</b></summary>

Both. `javac` compiles ahead of time to bytecode. At run time the JVM starts by **interpreting** bytecode, counts how often methods and loops run, and the **JIT compilers** compile the hot ones to machine code. HotSpot uses *tiered compilation*: C1 compiles quickly with light optimisation, C2 compiles hot code with heavy optimisation (inlining, escape analysis). So there is a **warm-up** period of slow code. Consequences: naive micro-benchmarks lie (use JMH), short-lived command-line tools start slowly, and some teams use **GraalVM native image** (ahead-of-time compilation: fast start, less peak throughput, no dynamic class loading without configuration) or CDS/AOT cache options.
</details>

<details><summary><b>4. [Intermediate] Where does the JVM keep data? What is the difference between <code>StackOverflowError</code> and <code>OutOfMemoryError</code>?</b></summary>

The **heap** holds all objects and arrays and is garbage collected, shared by all threads. Each thread has a **stack** of frames holding local variables and method calls; primitives live there, object references live there, but the objects live on the heap. **Metaspace** (native memory) holds class metadata. `StackOverflowError`: a thread's stack is exhausted, usually infinite or very deep recursion; fix the recursion or raise `-Xss` as a last resort. `OutOfMemoryError`: heap (`Java heap space`), metaspace, or native memory is exhausted; it means a leak or an undersized heap and needs a heap dump to diagnose. Both are `Error`s, not meant to be caught and "handled".
</details>

<details><summary><b>5. [Core] Maven: what are lifecycle phases, goals and plugins? What does <code>mvn test</code> actually run?</b></summary>

A **phase** is a named step in an ordered lifecycle (`validate, compile, test, package, verify, install, deploy`). A **goal** is a concrete task of a **plugin** (for example `compiler:compile`, `surefire:test`). The default lifecycle *binds* goals to phases by packaging type. `mvn test` runs every phase up to and including `test`: resources are copied, main code compiled, test code compiled, and the surefire plugin runs the tests. You can also call a goal directly (`mvn dependency:tree`), which does not run the lifecycle.
</details>

<details><summary><b>6. [Intermediate] Two of your dependencies need different versions of the same library. Which version does Maven use?</b></summary>

Maven's **dependency mediation** picks the version with the **shortest path** from your project in the dependency tree ("nearest wins"); on a tie, the first declared wins. This is not "the highest version", which surprises people. Check with `mvn dependency:tree` (add `-Dverbose` to see omitted conflicts). Typical symptom of a wrong pick: `NoSuchMethodError` or `ClassNotFoundException` at *run time*. Fix: pin the version in `dependencyManagement`, import a **BOM** (a pom that only manages versions, e.g. Spring Boot's), or exclude the transitive dependency.
</details>

<details><summary><b>7. [Hard] Explain "classpath hell". How do you find which jar a class was loaded from?</b></summary>

The same class name can exist in several jars (different versions, or a shaded copy). The JVM uses the **first one found** on the classpath, so order matters and behaviour can differ between your IDE, your tests and production. To investigate: run with `-verbose:class` (or `-Xlog:class+load`) to see the jar each class comes from; in code print `SomeClass.class.getProtectionDomain().getCodeSource()`; in Maven use `dependency:tree -Dverbose` and the *enforcer* plugin's `dependencyConvergence` rule. Prevention: one managed version per library, a BOM, and CI that builds from clean.
</details>

<details><summary><b>8. [Intermediate] <code>-DskipTests</code> or <code>-Dmaven.test.skip=true</code>: what is the difference? What is a SNAPSHOT version?</b></summary>

`-DskipTests` compiles the tests but does not run them. `-Dmaven.test.skip=true` does not even compile them (and test-jar dependencies break). A `-SNAPSHOT` version means "a moving development build": Maven may re-download it, so the same coordinates can give different bytes on different days. Releases are immutable. Never depend on SNAPSHOTs in a release, and use the Maven wrapper (`mvnw`) so every machine uses the same Maven version.
</details>

<details><summary><b>9. [Intermediate] What is a fat jar and what makes a jar runnable?</b></summary>

A jar is a zip of classes and resources with a `META-INF/MANIFEST.MF`. `java -jar app.jar` needs a `Main-Class` entry in the manifest, and the **classpath is only the jar** plus its `Class-Path` manifest entry (the `-cp` flag is ignored with `-jar`). A **fat (uber) jar** bundles all dependencies; Spring Boot builds a nested-jar layout with its own launcher; the *shade* plugin merges and can relocate packages. Trade-off: simple deployment versus large images and poor Docker layer caching (so layered jars are used later).
</details>

<details><summary><b>10. [Core] Walk through what happens from typing <code>https://shop.example/products</code> to the first byte, with the round trips.</b></summary>

(1) **DNS**: the resolver asks recursive and authoritative servers for the IP (cached by TTL). (2) **TCP handshake**: SYN, SYN-ACK, ACK = 1 round trip. (3) **TLS 1.3 handshake**: 1 round trip (TLS 1.2: 2) to agree on keys and authenticate the server with a certificate; session resumption can save it. (4) **HTTP request** and the server's work (maybe the database), then the first byte returns after 1 more round trip. A cold HTTPS request costs about 3 round trips before data. **Connection reuse (keep-alive)**, **HTTP/2** (many requests multiplexed over one connection) and **HTTP/3** (QUIC over UDP, which merges transport and TLS and avoids head-of-line blocking at the transport layer) cut that cost.
</details>

<details><summary><b>11. [Hard] A server gets slow under load. Compare thread-per-request with an event loop, and say where virtual threads fit.</b></summary>

*Thread-per-request* (classic servlet containers): each request holds a thread for its entire duration; while it waits for a database, the thread is idle but occupied, so concurrency is capped by the **pool size** (and memory, about 1 MB per platform thread stack). By **Little's law**, concurrent requests ≈ arrival rate × latency; if latency rises, the pool saturates and requests queue. *Event loop* (Node, Netty): a few threads handle many connections with non-blocking I/O; a CPU-heavy or blocking call stalls everyone. **Virtual threads** (JDK 21+) give you thread-per-request code with cheap threads that unmount while blocked, so blocking code scales without callbacks. They do not help CPU-bound work, and pinned threads (inside `synchronized` on older JDKs) can limit them.
</details>

---

## Week 2: Linux, processes, signals and Git

<details><summary><b>1. [Core] Process vs thread, and what does the OS do on a context switch?</b></summary>

A process has its own virtual address space, file descriptors and security context. Threads live inside a process and share its heap and file descriptors but each has its own stack and registers. Creating a process is heavier (`fork`/`exec`), and so is a process-level context switch (page tables, TLB flush). Threads switch faster but share memory, which creates data races. This difference drives the whole concurrency topic later: shared mutable state between threads needs synchronisation, between processes it needs explicit communication.
</details>

<details><summary><b>2. [Core] SIGTERM, SIGINT, SIGKILL, SIGHUP: what does each mean, and which can a JVM program react to?</b></summary>

SIGTERM (15) is a polite "please terminate"; SIGINT (2) is Ctrl+C; SIGHUP (1) historically "terminal closed" (often "reload config"); SIGKILL (9) and SIGSTOP cannot be caught or ignored. A JVM runs its **shutdown hooks** (`Runtime.addShutdownHook`) on SIGTERM and SIGINT and on normal exit; with SIGKILL it does not run anything. That is why deployments send SIGTERM, wait for a grace period (Kubernetes: 30 s by default) and only then SIGKILL, and why your service must stop accepting requests, finish in-flight ones and close resources in the hook.
</details>

<details><summary><b>3. [Intermediate] Your container is "killed with exit code 137" or "143". What do those numbers mean?</b></summary>

A process killed by signal *N* reports exit status `128 + N`. 143 = 128 + 15: it received SIGTERM (a normal stop or a rolling deploy). 137 = 128 + 9: SIGKILL, often the kernel **OOM killer** or Kubernetes enforcing a memory limit (`OOMKilled`), or a stop whose grace period expired. 137 with a Java app usually means the container memory limit is lower than heap plus off-heap memory (metaspace, thread stacks, direct buffers): the JVM did not throw `OutOfMemoryError`; the OS killed it.
</details>

<details><summary><b>4. [Hard] In a container, your Java app ignores <code>docker stop</code> and takes 10 seconds to die. Why?</b></summary>

The process with PID 1 in a container is treated specially: the kernel does not apply default signal actions to it, so a program that has no handler for SIGTERM ignores it. If the Dockerfile uses the **shell form** (`ENTRYPOINT java -jar app.jar`), PID 1 is `/bin/sh`, which does not forward the signal to Java; Docker waits for the timeout (10 s) and then sends SIGKILL. Fixes: use the **exec form** (`ENTRYPOINT ["java","-jar","app.jar"]`) so the JVM is PID 1 and receives SIGTERM, or use an init such as `tini` (`docker run --init`). Verify with `docker exec <c> ps`.
</details>

<details><summary><b>5. [Intermediate] Explain <code>cmd &gt; out.txt 2&gt;&amp;1</code> versus <code>cmd 2&gt;&amp;1 &gt; out.txt</code>.</b></summary>

Redirections are applied left to right on file descriptors (1 = stdout, 2 = stderr). In the first, stdout goes to the file, then stderr is pointed to *where stdout now goes* (the file): both land in the file. In the second, stderr is first pointed to where stdout currently goes (the terminal), then stdout is redirected to the file: only stdout reaches the file. Related: a pipe (`|`) connects stdout only, so `cmd 2>&1 | grep ERROR` is needed to search error output. [Prove it] with `ls /nope 2>&1 >/dev/null`.
</details>

<details><summary><b>6. [Intermediate] A port is "in use" after you stopped the server. How do you find the owner, and what is <code>TIME_WAIT</code>?</b></summary>

`lsof -nP -iTCP:8080 -sTCP:LISTEN` (or `ss -ltnp`) shows the PID listening. If nothing listens, connections you closed may be in **TIME_WAIT**: the side that closes first keeps the socket for about 2 × MSL (often 60 s) to absorb late packets and to make sure the last ACK arrives. Servers set `SO_REUSEADDR` so a restart can bind the port again; a very high number of TIME_WAIT sockets on a client means it opens a new connection per request instead of reusing one (connection pooling).
</details>

<details><summary><b>7. [Core] File permissions: what does <code>chmod 750 script.sh</code> mean? What is <code>umask</code>?</b></summary>

Three octal digits for owner, group, others; read = 4, write = 2, execute = 1. 750 = owner `rwx`, group `r-x`, others none. A shell script needs the execute bit (or you run `bash script.sh`); a directory needs `x` to be entered. `umask` is the mask of permission bits *removed* from newly created files (umask 022 gives files 644 and directories 755). Secrets such as private keys must be 600; ssh refuses keys that are group-readable.
</details>

<details><summary><b>8. [Intermediate] Merge, rebase, squash: how do they differ and what is the "golden rule of rebasing"?</b></summary>

A **merge** joins two histories with a merge commit and preserves what really happened. A **rebase** replays your commits onto a new base, producing *new commits* (new hashes) and a linear history. A **squash merge** collapses a branch into one commit on the target. The golden rule: never rebase commits that others have already pulled, because you rewrite shared history and everyone else's copy diverges. If you must force-push your own branch, use `--force-with-lease`, which refuses when the remote has commits you have not seen.
</details>

<details><summary><b>9. [Hard] What does a Git commit actually contain? What is a branch? What does <code>reset --hard</code> do to the three trees?</b></summary>

Git stores **objects** addressed by hash: *blobs* (file contents), *trees* (directories), *commits* (a tree, parent commit(s), author, message). A **branch** is just a movable pointer (a file with a commit hash); `HEAD` points at the current branch (or directly at a commit when "detached"). There are three trees: HEAD (last commit), the index (staging area), the working directory. `reset --soft` moves the branch only; `--mixed` (default) also resets the index; `--hard` also overwrites the working directory (uncommitted work is lost). Commits you "lost" stay reachable through the **reflog** for about 90 days by default: `git reflog`, then `git branch rescue <hash>`.
</details>

<details><summary><b>10. [Intermediate] <code>revert</code> or <code>reset</code> to undo a bad commit that is already on the shared main branch?</b></summary>

`git revert <hash>` creates a *new* commit that undoes the change: safe on shared branches because history is not rewritten. `reset` moves the branch backwards and would require a force-push that breaks everyone else's clone. Use `reset` only on local, unpublished commits. For reverting a merge commit you must also choose the parent with `-m 1`, and re-merging the same branch later needs a "revert the revert".
</details>

<details><summary><b>11. [Intermediate] How does <code>git bisect</code> work and what makes it efficient? When is trunk-based development better than gitflow?</b></summary>

Bisect does a **binary search** over the commit range between a known good and a known bad commit, so a range of 1,000 commits takes about 10 steps (log₂ n); `git bisect run ./test.sh` automates it if the script exits non-zero on failure. It only works well with small commits and a reliable test. **Trunk-based** development (short-lived branches merged to main at least daily, incomplete work hidden behind feature flags) keeps integration continuous, reduces merge pain and fits continuous deployment. **Gitflow** (long-lived `develop` and release branches) suits scheduled, versioned releases but causes large painful merges.
</details>

---

## Week 3: Java syntax, types and traps

<details><summary><b>1. [Core] Why does <code>Integer a = 127, b = 127; a == b</code> give true but 128 gives false?</b></summary>

Autoboxing calls `Integer.valueOf(int)`, which returns **cached** instances for -128 to 127 (the upper bound can be changed with `-XX:AutoBoxCacheMax`). Inside the range both variables reference the same object, so `==` (reference comparison) is true. Outside it, each boxing creates a new object. Lesson: never use `==` on boxed types or strings; use `equals`. Also `new Integer(127)` (deprecated) always creates a new object. [Prove it]
</details>

<details><summary><b>2. [Core] Is Java pass-by-value or pass-by-reference? Prove it with a <code>swap</code> method.</b></summary>

Always pass-by-value. For objects, the value copied is the **reference**. A method can mutate the object the reference points to, but it cannot change the caller's variable. A `swap(a, b)` method that exchanges its parameters changes nothing for the caller; the only way to "swap" is to mutate fields inside the objects or return a new pair. [Prove it]
</details>

<details><summary><b>3. [Intermediate] Predict: <code>Math.abs(Integer.MIN_VALUE)</code>, <code>-7 / 2</code>, <code>-7 % 3</code>, <code>(int) 3_000_000_000L</code>.</b></summary>

`Math.abs(Integer.MIN_VALUE)` is **-2147483648** (the positive value does not fit in `int`; two's complement overflow). `-7 / 2` is **-3** (integer division truncates toward zero, not floor). `-7 % 3` is **-1** (the remainder takes the sign of the dividend; use `Math.floorMod(-7, 3)` = 2 for a mathematical modulo, e.g. for array indexes). `(int) 3_000_000_000L` is **-1294967296**: the cast silently keeps the low 32 bits. Use `Math.toIntExact` to fail loudly. Hash-based indexing bugs often come from `Math.abs(hash) % n` for exactly this reason.
</details>

<details><summary><b>4. [Intermediate] Why is <code>0.1 + 0.2 == 0.3</code> false, and what are <code>NaN</code> and <code>-0.0</code> traps?</b></summary>

`double` is binary floating point (IEEE 754); 0.1 and 0.2 have no exact binary representation, so the sum is 0.30000000000000004. Never compare with `==`; compare within a tolerance, or use `BigDecimal` for money. `NaN != NaN` is true, and `NaN == NaN` is false, so test with `Double.isNaN`. `0.0 == -0.0` is true but `Double.compare(0.0, -0.0)` and `equals` differ, which matters in `HashSet<Double>` and sorting. `Double.compare` gives a total order; the `<` operators do not.
</details>

<details><summary><b>5. [Intermediate] What does this print? <code>String a = "hi"; String b = "h" + "i"; String c = new String("hi"); String h = "h"; String d = h + "i";</code> and the comparisons <code>a==b, a==c, a==d, a==d.intern()</code>.</b></summary>

`a == b` **true**: `"h" + "i"` is a compile-time constant expression, folded to `"hi"` and taken from the string pool. `a == c` **false**: `new String` creates a distinct object. `a == d` **false**: `h` is not a constant variable (it is not `final`), so the concatenation happens at run time and creates a new object. If you declare `final String h = "h";` then `d` is a constant and `a == d` becomes true. `a == d.intern()` **true**: `intern()` returns the pooled instance. Answer in the interview with "compile-time constant" and you stand out; then say you would use `equals` anyway.
</details>

<details><summary><b>6. [Hard] <code>byte b = 10; b = b + 5;</code> does not compile but <code>b += 5;</code> does. Why? What about <code>char c = 'a'; c++</code>?</b></summary>

`b + 5` is promoted to `int`, and assigning an `int` to a `byte` needs an explicit cast. A compound assignment `b += 5` has an **implicit narrowing cast**: it means `b = (byte)(b + 5)`. That hides overflow: `byte b = 127; b += 1;` gives -128. Likewise `c++` on a `char` is allowed and gives `'b'`. Related: `short s = 1; s = s + 1;` fails; `final byte` constants that fit are allowed. Compound assignment is a classic source of silent truncation in loops over small numeric types.
</details>

<details><summary><b>7. [Hard] The ternary operator and unboxing: what does <code>Integer x = null; int y = flag ? x : 0;</code> do? What type is <code>flag ? 1 : 2.0</code>?</b></summary>

If one operand of `?:` is a primitive (`int`) and the other is a boxed type (`Integer`), the result type is the primitive, so `x` is **unboxed** even when `flag` is true; if `x` is `null` this throws `NullPointerException` with no visible method call. And `flag ? 1 : 2.0` is a **double** (binary numeric promotion), so `flag ? 1 : 2.0` prints `1.0`. With `Object o = flag ? 1 : "x";` the result is `Object` and no promotion happens. Lesson: avoid mixing boxed, primitive and mixed numeric types in `?:`.
</details>

<details><summary><b>8. [Intermediate] Switch: statement vs expression, fall-through, <code>yield</code>, exhaustiveness.</b></summary>

The old `switch` statement falls through to the next case unless you `break`; a forgotten `break` is a classic bug. The arrow form `case A, B -> ...` does not fall through. A `switch` **expression** returns a value, must be exhaustive (every possible input covered; for enums and sealed types the compiler checks without `default`), and uses `yield` in block bodies. On `String` it uses `hashCode` then `equals`. A `null` selector throws `NullPointerException` unless you add `case null`. Prefer expressions without `default` for enums and sealed types, so that adding a constant breaks compilation where you need to handle it.
</details>

<details><summary><b>9. [Hard] What is the initialization order of static fields, instance fields, initializer blocks and constructors, and what bug does it cause with overridable methods?</b></summary>

When a class is first used: static fields and static blocks run once in textual order (superclass first). On each `new`: the superclass constructor runs first (after its field initializers), then this class's field initializers and instance blocks, then the constructor body. If the **superclass constructor calls an overridable method**, the overriding method runs *before the subclass fields are initialised*, so it sees `null`/0. Example: `Base() { init(); }` and `Sub.init()` uses a field `list` of `Sub` that is still `null` → `NullPointerException`. Rule: never call overridable methods from constructors (EJ Item 19). [Prove it]
</details>

<details><summary><b>10. [Intermediate] <code>final</code> vs immutable. Why do lambdas require "effectively final" variables?</b></summary>

`final` on a variable means the *reference* cannot be reassigned; the object it points to can still change (`final List<String> l` can still be modified). An immutable object has no way to change its state. Lambdas and anonymous classes **capture the value** of local variables; since the lambda may run later or on another thread, allowing the variable to change would give confusing or unsafe behaviour, so the compiler demands the variable is never reassigned (effectively final). To accumulate inside a lambda use an `AtomicInteger`, a one-element array (poor style), or restructure with a stream reduction.
</details>

<details><summary><b>11. [Core] What makes a good unit test? Why write the test first?</b></summary>

Fast, isolated, repeatable, self-checking, and focused on *behaviour* (one reason to fail). Structure: arrange, act, assert, with names that read as a requirement (`remove_belowZero_throws`). Writing it first (red → green → refactor) proves the test can fail (a test that has never failed proves nothing), shapes an API from the caller's point of view, and gives a safety net for refactoring. Test boundaries (0, 1, max, negative, empty, null) rather than only the happy path.
</details>

---

## Week 4: Classes, immutability, equals/hashCode, records, BigDecimal

<details><summary><b>1. [Core] State the <code>equals</code> contract and show how a subclass can break symmetry.</b></summary>

`equals` must be **reflexive** (`x.equals(x)`), **symmetric** (`x.equals(y)` iff `y.equals(x)`), **transitive**, **consistent** (same answer while nothing changes) and return false for `null`. Classic break: `Point(x,y)` with `equals` using `instanceof Point`, and `ColorPoint extends Point` adding `color` with its own `equals`. Then `point.equals(colorPoint)` is true (ignores color) but `colorPoint.equals(point)` is false: symmetry is broken. Using `getClass()` fixes symmetry but breaks Liskov (a subclass instance is never equal to a base instance, so e.g. an anonymous subclass behaves differently in a `Set`). Bloch's conclusion (EJ Item 10): there is no way to extend an instantiable class and add a value component while preserving the contract; **use composition**, or make the type final or a record. [Prove it]
</details>

<details><summary><b>2. [Core] What breaks if you override <code>equals</code> but not <code>hashCode</code>? What does the default <code>hashCode</code> return?</b></summary>

Two equal objects get different hash codes, so a `HashMap`/`HashSet` looks in the wrong bucket and `contains` returns false for an object you just added. The default `hashCode` is derived from the object **identity** (implementation-specific, not necessarily the memory address), so it differs per instance. Good implementations use the same fields as `equals`: `Objects.hash(a, b)` or a manual `31 * h + field` fold; records generate it. Do not use mutable fields: if the hash changes after insertion the entry is lost (see Week 5/6 labs).
</details>

<details><summary><b>3. [Intermediate] What is "consistent with equals" for <code>compareTo</code>, and how does <code>BigDecimal</code> violate it?</b></summary>

`compareTo` is consistent with `equals` if `a.compareTo(b) == 0` exactly when `a.equals(b)`. `BigDecimal` is not: `new BigDecimal("1.0").compareTo(new BigDecimal("1.00"))` is 0, but `equals` is false because the **scale** differs. Result: a `HashSet<BigDecimal>` treats them as two elements while a `TreeSet<BigDecimal>` keeps one. Solutions: normalise the scale in your `Money` constructor (`setScale(2, RoundingMode.HALF_EVEN)`), or use `stripTrailingZeros` carefully (note `new BigDecimal("100").stripTrailingZeros()` gives `1E+2`), and always compare money with `compareTo`.
</details>

<details><summary><b>4. [Core] How do you design an immutable class, and why bother?</b></summary>

Rules (EJ Item 17): no mutators; make the class non-extendable (`final` or private constructors with static factories); all fields `private final`; **defensive copies** of mutable inputs in the constructor and of mutable outputs in getters; do not let `this` escape from the constructor. Benefits: thread-safe without locking, safe to share and cache, usable as map keys, easy to reason about, failure atomicity. Costs: a new object for each change (allocation; mitigate with builders or "wither" methods). A final field set in the constructor is also safely published to other threads under the Java Memory Model.
</details>

<details><summary><b>5. [Hard] <code>List.copyOf</code>, <code>Collections.unmodifiableList</code>, <code>List.of</code>: what is the difference? Which one makes a class truly immutable?</b></summary>

`Collections.unmodifiableList(l)` is a **read-only view**: if someone still holds `l` and changes it, the "unmodifiable" list changes too. `List.copyOf(l)` makes an **independent immutable copy** (and returns the same instance if `l` is already an immutable list); it rejects `null` elements. `List.of(...)` creates an immutable list directly; it is null-hostile and its `contains(null)` throws. For immutability use `List.copyOf` in the constructor, and return the (immutable) list or a copy from getters. Remember elements themselves must be immutable, or the immutability is only **shallow**.
</details>

<details><summary><b>6. [Intermediate] What does a <code>record</code> generate, what can it not do, and what is the compact constructor?</b></summary>

For <code>record Money(BigDecimal amount, Currency currency)</code>: private final fields, a canonical constructor, accessors <code>amount()</code> (not <code>getAmount()</code>), <code>equals</code>, <code>hashCode</code>, <code>toString</code>. A record is implicitly final, cannot extend a class (it extends <code>java.lang.Record</code>), cannot declare extra instance fields, but can implement interfaces, have static members and methods. The **compact constructor** (<code>Money { ... }</code>) runs validation and normalisation before the fields are assigned. Traps: shallow immutability (a <code>List</code> component can still be mutated; copy it in the compact constructor); an <code>int[]</code> component is compared by <b>identity</b> in the generated <code>equals</code>; records are a poor fit for JPA entities (no no-arg constructor, final fields) and for types with mutable identity.
</details>

<details><summary><b>7. [Core] Why not <code>double</code> for money? What are the options?</b></summary>

Binary floating point cannot represent most decimal fractions exactly (0.1), so sums drift and `==` fails. Options: `BigDecimal` (exact decimal, explicit scale and `RoundingMode`; create from `String` or `BigDecimal.valueOf`, never `new BigDecimal(0.1)`), or `long` minor units (cents) with a currency (fast and simple, but you must handle currencies with 0 or 3 decimals, and division/allocation rounding yourself). Always pair the amount with a currency, round only at defined points, and use `HALF_EVEN` (banker's rounding) if the business does not specify, because it avoids a systematic upward bias.
</details>

<details><summary><b>8. [Hard] <code>new BigDecimal("10").divide(new BigDecimal("3"))</code> throws. Why, and how do you split 100.00 into 3 parts without losing a cent?</b></summary>

The exact quotient is a non-terminating decimal, so `divide` without a scale or `MathContext` throws `ArithmeticException: Non-terminating decimal expansion`. Give a scale and rounding: `a.divide(b, 2, RoundingMode.HALF_EVEN)`. Splitting 100.00 three ways with rounding gives 33.33 × 3 = 99.99: a cent is lost. **Allocation** fixes it: compute the base share in cents (3333), distribute the remainder cents (100.00 − 99.99 = 1 cent) one by one to the first parts → 33.34, 33.33, 33.33. Never round every part independently when the parts must sum to a total (invoices, tax, discounts). This is exactly what a `Money.allocate(ratios)` method does.
</details>

<details><summary><b>9. [Intermediate] Static factory methods vs constructors, and when to use a builder.</b></summary>

Static factories (EJ Item 1) have names (`Money.of("10.00","EUR")`, `Sku.parse(s)`), can return cached instances or subtypes, can validate and return `Optional`/error types, and do not have to create a new object. Drawbacks: harder to find in docs, classes with only private constructors cannot be subclassed. A **builder** (Item 2) helps when there are many optional parameters (avoids telescoping constructors and unclear `new X(true, false, null, 3)` calls) and lets you validate once in `build()`. For three or four required fields, a record plus a factory is simpler.
</details>

<details><summary><b>10. [Hard] Why is <code>Cloneable</code>/<code>clone()</code> considered broken, and what do you use instead?</b></summary>

`Cloneable` is a marker interface that changes the behaviour of `Object.clone()` (a protected method that does a shallow field copy and creates the object **without calling a constructor**), so invariants and `final` fields are bypassed; subclasses and mutable fields need careful manual deep copies; the contract is vague (EJ Item 13). Prefer a **copy constructor** or **copy factory** (`new Order(other)` / `Order.copyOf(other)`), or immutable types that need no copying. Arrays are the exception: `array.clone()` is fine.
</details>

<details><summary><b>11. [Intermediate] Value object or entity? Give an example in an order system.</b></summary>

A **value object** has no identity: it is defined by its attributes, is immutable and replaceable (`Money`, `Sku`, `Address`, `DateRange`); equality is by value; make it a record. An **entity** has an identity that persists while its attributes change (`Customer` id 42, `Order` id 7); equality is by id; it has a life cycle and usually mutable state. Mistakes: using a mutable class with value-based `equals` as a key in a map; giving an entity equality by all fields (so it changes when a field changes); modelling `Money` as a `double`. In an order system: `Order` is an entity, `OrderLine` is usually an entity inside the order, `Money`, `Quantity`, `Sku`, `ShippingAddress` are values.
</details>

---

## Week 5: Inheritance, interfaces, enums, sealed types

<details><summary><b>1. [Intermediate] What is dynamic dispatch? Which things are <em>not</em> polymorphic in Java?</b></summary>

Instance method calls are resolved at run time from the object's actual class (virtual dispatch). **Not polymorphic**: fields (field access uses the static type: `Parent p = new Child(); p.name` reads `Parent.name`), `static` methods (they are *hidden*, not overridden), and `private` methods. Overload selection is made at **compile time** from the static argument types, while which *overridden* version runs is decided at run time. Interviewers love: `void f(Object o)` and `void f(String s)` with `f(null)` calls the `String` overload (most specific), and `Object o = "hi"; f(o)` calls the `Object` one. [Prove it]
</details>

<details><summary><b>2. [Hard] Overload resolution with boxing and varargs: which method is picked for <code>f(1)</code> when you have <code>f(long)</code>, <code>f(Integer)</code> and <code>f(int...)</code>?</b></summary>

The compiler works in three phases and stops at the first that finds a match: (1) subtyping and primitive widening, no boxing, no varargs; (2) allow boxing/unboxing; (3) allow varargs. For `f(1)`: phase 1 matches `f(long)` by widening `int → long`, so it is chosen, even though `f(Integer)` looks "closer". Lesson: overloads that differ only by boxing or varargs are confusing; avoid them (EJ Item 52). The classic trap in the collections API: `list.remove(1)` removes **index 1**, while `list.remove(Integer.valueOf(1))` removes the **element** 1.
</details>

<details><summary><b>3. [Core] Composition vs inheritance: explain the fragile base class problem with an example.</b></summary>

A subclass depends on the superclass's internal behaviour. Example (EJ Item 18): `CountingSet extends HashSet` overrides `add` and `addAll` to count additions. But `HashSet.addAll` internally calls `add` for each element, so adding 3 elements with `addAll` counts 6. A new JDK release could change that internal call and break your class again. **Composition** with forwarding: `CountingSet implements Set` holds a `Set` field and delegates; it relies only on the public contract. Inherit only when the class is designed and documented for extension, or when there is a genuine is-a relationship; otherwise use an interface plus delegation (the Decorator pattern).
</details>

<details><summary><b>4. [Intermediate] Abstract class vs interface (with default methods). How does Java resolve a default-method conflict?</b></summary>

A class can implement many interfaces but extend one class. Interfaces declare a contract (and since Java 8 can provide `default` and `static` methods but no instance state); abstract classes can hold state and constructors. Prefer interfaces for types, and add an abstract skeleton (`AbstractList` style) to share code. Conflict rules: (1) a **class wins** over an interface (a concrete or inherited class method beats a default); (2) otherwise the **most specific interface** wins; (3) if still ambiguous, the class must override and may call `A.super.m()`. Since Java 9 interfaces can have private methods.
</details>

<details><summary><b>5. [Hard] What goes wrong when a constructor calls an overridable method? Show the output.</b></summary>

<pre>class Base { Base() { hello(); } void hello() {} }
class Sub extends Base {
  private String name = "Sara";
  @Override void hello() { System.out.println(name); }
}
new Sub();   // prints null</pre>
The superclass constructor runs before `Sub`'s field initialisers, so `hello()` executes on a half-built object and sees `name == null`. Final fields could even be read as their default values. Rule: constructors must call only `private`, `final` or `static` methods. This also bites frameworks that call overridable `init()` hooks.
</details>

<details><summary><b>6. [Hard] State the Liskov Substitution Principle in terms of contracts, and give a violation that is not Square/Rectangle.</b></summary>

A subtype must be usable wherever its supertype is expected **without the caller noticing**: it may not *strengthen preconditions*, may not *weaken postconditions* or invariants, and must not throw new checked or surprising exceptions. Violation example: `ImmutableList extends ArrayList` where `add` throws `UnsupportedOperationException`: code written for `List` breaks. Another: `ReadOnlyFile extends File` whose `write()` does nothing. Another: `equals` symmetry break (Week 4). Typical fixes: split the interface (`ReadableList` / `WritableList`), prefer composition, or make the base type not promise the capability.
</details>

<details><summary><b>7. [Intermediate] What can enums do beyond constants? What are <code>EnumMap</code>/<code>EnumSet</code> for, and why is <code>ordinal()</code> dangerous?</b></summary>

An enum is a class with a fixed set of singleton instances: it can have fields, a constructor, methods, implement interfaces and even give **each constant its own method body** (constant-specific behaviour), replacing `switch` on type. `EnumMap` and `EnumSet` are compact array/bit-set implementations keyed by `ordinal`, much faster than `HashMap`/`HashSet` for enum keys. `ordinal()` is the declaration position: reordering or inserting a constant changes it, silently corrupting stored data or logic; store an explicit code or the `name()` instead (EJ Item 35). Enums are serialisation-safe singletons, which is Bloch's recommended singleton idiom. `values()` returns a fresh array copy each time.
</details>

<details><summary><b>8. [Intermediate] Sealed interfaces and pattern-matching <code>switch</code>: what do you gain over enums and over the Visitor pattern?</b></summary>

An enum's constants all have the same shape. A `sealed interface PaymentResult permits Approved, Declined, Pending` with a record per case lets each case **carry different data**, and the compiler knows the full set, so a `switch` without `default` is checked for **exhaustiveness**: adding a case breaks compilation everywhere it must be handled. Compared with the Visitor pattern you write the operation as one `switch` instead of two interface hierarchies and double dispatch. This addresses the "expression problem": adding a new *operation* is easy with `switch`; adding a new *case* is easy with polymorphism; sealed types make the closed-set choice explicit. Do not use `default` on sealed switches; it hides new cases.
</details>

<details><summary><b>9. [Intermediate] How do pattern matching for <code>instanceof</code> and record patterns simplify <code>equals</code> and deconstruction?</b></summary>

`if (o instanceof Money m && m.currency().equals(currency))` declares and casts in one step; the variable `m` is in scope only where the test is definitely true (flow scoping). Record patterns deconstruct: `case Approved(var txId) -> ...` or nested `case Order(var id, Money(var amount, var cur))`. This removes casts and getters and combines type checks with guards (`case Circle c when c.r() > 10 -> ...`). The order of cases matters: more specific patterns must come first or the compiler reports "dominated" cases.
</details>

<details><summary><b>10. [Core] Access modifiers: why prefer <code>private</code> and package-private, and what does <code>protected</code> really allow?</b></summary>

`private` < package-private (no modifier) < `protected` < `public`. `protected` is accessible to subclasses **and to the whole package**, so it is wider than it looks. Minimise accessibility (EJ Item 15): public API is a promise you cannot change; hidden members can be changed freely. Public fields (except constants) break encapsulation. With JPMS (modules) you can also hide whole packages. Test code can live in the same package to test package-private classes without making them public.
</details>

---

## Week 6: Collections, generics and complexity

<details><summary><b>1. [Hard] Describe the internals of <code>HashMap</code> precisely: index, collisions, resize, treeification.</b></summary>

An array of buckets whose length is a power of two. The key's `hashCode()` is spread (`h ^ (h >>> 16)`) so high bits affect the index, and the index is `(n - 1) & hash`. Entries in the same bucket form a linked list; when a bucket reaches 8 entries and the table has at least 64 buckets it becomes a **red-black tree** (O(log n) worst case, using `Comparable` keys if available); below 6 it reverts. When `size > capacity × loadFactor` (0.75 by default) the table **doubles** and entries are redistributed (each stays in place or moves by exactly the old capacity). `HashMap` is **not thread-safe**: concurrent resizes could corrupt it (JDK 7 could even loop forever), so use `ConcurrentHashMap`. At most one `null` key is allowed.
</details>

<details><summary><b>2. [Intermediate] How would you pre-size a <code>HashMap</code> for 1,000 entries to avoid resizing?</b></summary>

Capacity must satisfy `n × 0.75 ≥ 1000`, so `n ≥ 1334`, which rounds up to 2048. Passing `new HashMap<>(1000)` is not enough because the capacity rounds to 1024, which would resize at 768 entries. Use `new HashMap<>((int) (1000 / 0.75f) + 1)` or, since JDK 19, `HashMap.newHashMap(1000)`. Resizing is O(n) and creates garbage, so it matters for big maps in hot paths; for small maps ignore it.
</details>

<details><summary><b>3. [Hard] A <code>TreeSet&lt;BigDecimal&gt;</code> and a <code>HashSet&lt;BigDecimal&gt;</code> both receive "1.0" and "1.00". What are their sizes and why?</b></summary>

`HashSet` has **2** elements (`equals` and `hashCode` consider scale). `TreeSet` has **1** (it uses `compareTo`, which returns 0). More generally, sorted collections use the comparison, not `equals`: a comparator that returns 0 for different objects silently drops one. A comparator that is not **consistent with `equals`** also makes `TreeMap.containsKey` and `remove` behave differently than `Map` documents. Always check the key type or comparator.
</details>

<details><summary><b>4. [Intermediate] Four <code>List</code> traps: <code>remove(int)</code> vs <code>remove(Object)</code>, <code>Arrays.asList</code>, <code>subList</code>, <code>List.of</code>.</b></summary>

(1) With <code>List&lt;Integer&gt; l</code>, <code>l.remove(1)</code> removes the element at **index 1**; <code>l.remove(Integer.valueOf(1))</code> removes the value 1. (2) <code>Arrays.asList(arr)</code> is a **fixed-size view** over the array: `set` writes through to the array, `add`/`remove` throw `UnsupportedOperationException`; with a primitive array (`int[]`) you get a list with one element, the array itself. (3) `list.subList(a, b)` is a **view**: structural changes to the parent afterwards invalidate it (`ConcurrentModificationException`); copy it with `new ArrayList<>(...)` if you keep it. (4) `List.of(...)` is immutable and rejects `null` elements; `List.of(...).contains(null)` throws.
</details>

<details><summary><b>5. [Hard] Explain why <code>ConcurrentModificationException</code> occurs, why removing the second-to-last element does <em>not</em> throw, and what fail-fast guarantees.</b></summary>

`ArrayList` has a `modCount` incremented on structural changes. Its iterator remembers the expected count and `next()` checks it. In a for-each loop, removing an element with `list.remove` changes `modCount` and the next call to `next()` throws. But `hasNext()` is `cursor != size`: if you remove the second-to-last element, `size` shrinks by one and equals `cursor`, so the loop ends *before* `next()` is called, and no exception is thrown (and the last element is skipped). Fail-fast iterators are **best effort**: they detect bugs, they do not guarantee anything under concurrency. Correct options: `Iterator.remove()`, `removeIf`, collecting to remove later, or a concurrent collection such as `CopyOnWriteArrayList` (snapshot iteration). [Prove it]
</details>

<details><summary><b>6. [Hard] <code>ConcurrentHashMap</code> vs <code>Collections.synchronizedMap</code> vs <code>Hashtable</code>. Why is <code>if (!map.containsKey(k)) map.put(k, v)</code> still a bug?</b></summary>

`Hashtable` and `synchronizedMap` lock the whole map on every operation (poor scalability, and iteration needs external locking). `ConcurrentHashMap` uses lock-free reads, CAS for empty bins and per-bin locking for writes; it does not allow `null` keys or values (so "absent" is unambiguous), its `size()` is an estimate under concurrency, and iterators are weakly consistent (never throw CME). A **check-then-act** sequence is not atomic even on a thread-safe map: two threads can both see the key missing and both put. Use the atomic operations: `putIfAbsent`, `computeIfAbsent`, `merge` (for counters), `compute`.
</details>

<details><summary><b>7. [Intermediate] Implement an LRU cache with the standard library.</b></summary>

<pre>class LruCache&lt;K,V&gt; extends LinkedHashMap&lt;K,V&gt; {
  private final int max;
  LruCache(int max) { super(16, 0.75f, true); this.max = max; } // true = access order
  @Override protected boolean removeEldestEntry(Map.Entry&lt;K,V&gt; e) { return size() &gt; max; }
}</pre>
`accessOrder = true` moves an entry to the end on every `get`; `removeEldestEntry` is called after each `put` and evicts the least recently used. It is not thread-safe (wrap with `Collections.synchronizedMap`, or use a library such as Caffeine in production). Be able to explain the alternative: a `HashMap` plus a doubly linked list (the design asked in interviews).
</details>

<details><summary><b>8. [Intermediate] <code>PriorityQueue</code>: complexity, ordering guarantees, and common mistakes.</b></summary>

A binary min-heap in an array: `offer` and `poll` are O(log n), `peek` O(1), `remove(Object)` and `contains` O(n). **Only the head is ordered**; iteration and `toString` show heap order, not sorted order. It is not stable (equal priorities come out in any order; add a sequence number to the comparator if you need FIFO among equals), not thread-safe (`PriorityBlockingQueue` is). Elements must be comparable or you pass a `Comparator`; mutating an element's priority after insertion corrupts the heap. A typical use: top-K with a min-heap of size K, O(n log k).
</details>

<details><summary><b>9. [Hard] What is type erasure? Why can't you write <code>new T[]</code> or <code>instanceof List&lt;String&gt;</code>? Why are arrays covariant but generics invariant?</b></summary>

The compiler checks generics and then **erases** type arguments (replacing them with their bounds, `Object` by default), inserting casts and **bridge methods** so that overriding still works. At run time `List<String>` and `List<Integer>` are both just `List`, so you cannot test `instanceof List<String>`, create `new T[]` or `new List<String>[]`, or overload on the type argument. Types that keep their information at run time are *reifiable* (`String`, `List<?>`, raw types, arrays of those). Arrays are **covariant** (`Object[] a = new String[1]` compiles) and checked at run time (`a[0] = 1` throws `ArrayStoreException`); generics are **invariant** (`List<Object> l = new ArrayList<String>()` does not compile) to push such errors to compile time. Mixing raw types and generics causes *heap pollution* (an unchecked warning that later becomes a `ClassCastException` somewhere else).
</details>

<details><summary><b>10. [Hard] Explain PECS with a method, and explain the error "Comparison method violates its general contract!".</b></summary>

PECS (Producer Extends, Consumer Super): <pre>static &lt;T&gt; void copy(List&lt;? super T&gt; dst, List&lt;? extends T&gt; src) { for (T t : src) dst.add(t); }</pre> `src` produces `T` values (read-only; you cannot add to it), `dst` consumes them. You can then copy a `List<Integer>` into a `List<Number>`. The `TimSort` error appears when a `Comparator` is not a valid total order: it must be antisymmetric, **transitive** and consistent. Typical causes: comparing doubles with a subtraction that overflows, using `a.price > b.price ? 1 : -1` (never returns 0 for equal), or comparing a field that changes during the sort. Use `Comparator.comparingInt(...)`, `Double.compare`, `thenComparing` and `nullsFirst`.
</details>

---

## Week 7: Exceptions, I/O and strings

<details><summary><b>1. [Core] Checked vs unchecked exceptions: when would you use each, and why do many modern libraries avoid checked ones?</b></summary>

Checked exceptions (subclasses of `Exception` but not `RuntimeException`) must be caught or declared; they suit conditions a caller can reasonably recover from (EJ Item 70). Unchecked exceptions (`RuntimeException`, `Error`) signal programming errors (`IllegalArgumentException`, `NullPointerException`) or failures the caller cannot fix. Criticisms of checked exceptions: they leak implementation details through signatures (a `SQLException` in a service interface), force boilerplate catches, and **do not work well with lambdas** (`Function` cannot throw them). A common strategy: unchecked domain exceptions inside, **translate** low-level exceptions at module boundaries (Item 73) while keeping the cause, and map them to HTTP statuses at the edge.
</details>

<details><summary><b>2. [Hard] What does this return? <code>int f() { try { return 1; } finally { return 2; } }</code> And what if <code>finally</code> throws?</b></summary>

It returns **2**: the `finally` block always runs before the method returns, and a `return` inside it overrides the earlier one (also discarding any exception that was in flight). If `finally` throws, the new exception **replaces** the original, which is lost: a diagnosis nightmare. Never `return` or throw from `finally`. The only ways `finally` does not run: `System.exit`, the JVM crashing or the thread being killed. This is why try-with-resources was introduced: it keeps the primary exception and records close failures as **suppressed** (`e.getSuppressed()`).
</details>

<details><summary><b>3. [Intermediate] try-with-resources in detail: order of closing, a null resource, and what a partially built resource means.</b></summary>

<pre>try (var in = open(a); var out = open(b)) { ... }</pre> Resources are closed in **reverse order** of declaration (`out`, then `in`) before any `catch`/`finally`. A `null` resource is allowed and simply not closed. If the body throws and `close()` also throws, the body exception is the primary and the close exception is added as suppressed. If opening `out` throws, `in` is still closed. A resource must implement `AutoCloseable` (`close()` may throw `Exception`) or `Closeable` (`IOException`). Since Java 9 you can use an effectively final existing variable: `try (in)`. Do not forget resources created in a constructor: if it throws midway, the earlier ones leak.
</details>

<details><summary><b>4. [Hard] How must you handle <code>InterruptedException</code>?</b></summary>

It signals that another thread asked this one to stop. Swallowing it (`catch (InterruptedException e) {}`) loses the request. Correct: either propagate it (declare `throws InterruptedException`), or if you cannot, **restore the flag** with `Thread.currentThread().interrupt()` and return/stop promptly, so callers higher in the stack can see it. Throwing a different exception without restoring the flag has the same effect as swallowing. Blocking methods (`sleep`, `wait`, `BlockingQueue.take`, `Future.get`) clear the flag when they throw, which is why restoring is necessary. This is a very common senior-level question.
</details>

<details><summary><b>5. [Intermediate] Are exceptions expensive? How should you use <code>catch</code>, multi-catch and <code>Throwable</code>/<code>Error</code>?</b></summary>

Creating an exception captures the **stack trace** (`fillInStackTrace`), which costs microseconds and grows with stack depth; throwing and catching itself is cheap. So do not use exceptions for ordinary control flow (EJ Item 69), but also do not fear them in error paths. Catch the narrowest type; order catches from specific to general (the compiler rejects a subclass after its superclass); multi-catch `catch (IOException | SQLException e)` avoids duplication (the variable is effectively final). Do not catch `Throwable` or `Error` (OutOfMemoryError, StackOverflowError) to "handle" them; at most log and exit at the top level. Custom exceptions for hot paths can disable the stack trace via the 4-argument constructor.
</details>

<details><summary><b>6. [Intermediate] Exception chaining and logging: what is wrong with <code>catch (E e) { log.error(e.getMessage()); throw new RuntimeException("failed"); }</code>?</b></summary>

Three faults: the stack trace is lost in the log (only the message), the new exception drops the **cause** (should be `new X("...", e)`), and logging *and* rethrowing means the same failure is reported twice at different layers. Rule: add context and keep the cause when you rethrow; log **once**, at the boundary that finally handles it, passing the exception as the last argument (`log.error("Cannot load {}", path, e)`). Also keep sensitive data (passwords, tokens, whole request bodies) out of exception messages.
</details>

<details><summary><b>7. [Intermediate] Strings: why immutable, what does <code>+</code> compile to, and when do you use <code>StringBuilder</code>?</b></summary>

Immutability makes strings safe as map keys (the hash can be cached), shareable between threads and usable in the string pool, and it protects security-sensitive values like file paths. Concatenation in a loop (`s += x`) creates a new string every iteration: O(n²). `StringBuilder` (not thread-safe) appends in a growing buffer; `StringBuffer` is the old synchronised variant, rarely needed. For a single expression, `a + b + c` is fine: since JDK 9 the compiler emits an `invokedynamic` call to `StringConcatFactory`, which is optimised at run time. Compact strings (JDK 9+) store Latin-1-only text as one byte per char internally. Use `String.join`, text blocks and `formatted` for readability.
</details>

<details><summary><b>8. [Hard] <code>"😀".length()</code> returns 2. Why? How should you count characters, and why do locale and charset matter?</b></summary>

A Java `char` is a UTF-16 code unit. Characters outside the Basic Multilingual Plane (most emoji) take two chars, a *surrogate pair*, so `length()` counts code units, not user-visible characters. Use `codePointCount(0, s.length())` for code points, and `java.text.BreakIterator` or ICU for grapheme clusters (an emoji with skin tone is several code points). `toUpperCase()`/`toLowerCase()` use the default locale: in Turkish `"TITLE".toLowerCase()` produces a dotless ı; use `toLowerCase(Locale.ROOT)` for identifiers and protocol text. Converting between bytes and text always needs an explicit charset (`StandardCharsets.UTF_8`); JDK 18+ defaults to UTF-8 for file APIs (JEP 400), but older runtimes and some tools do not. Also compare accented text after Unicode normalisation (NFC), because "é" can be one or two code points.
</details>

<details><summary><b>9. [Intermediate] <code>java.io.File</code> vs NIO <code>Path</code>/<code>Files</code>; what are the traps with <code>Files.walk</code>, <code>readAllLines</code> and "check then open"?</b></summary>

NIO.2 (`Path`, `Files`) gives better error types (`NoSuchFileException`), symbolic link handling, attributes, atomic moves and directory streams. `Files.walk`, `Files.lines` and `Files.list` return streams backed by open directory/file handles: **close them** (try-with-resources) or you leak descriptors. `Files.readAllLines` and `readString` load the whole file into memory; for big files stream with a `BufferedReader`. `if (Files.exists(p)) open(p)` is a **TOCTOU** race (time of check to time of use): the file may vanish in between; just try the operation and handle the exception. For crash-safe writes: write to a temp file in the same directory, flush (`FileChannel.force(true)` for durability) and `Files.move(tmp, target, ATOMIC_MOVE)`.
</details>

<details><summary><b>10. [Intermediate] Why is Java's built-in serialisation dangerous, and what do you use instead?</b></summary>

`ObjectInputStream` can instantiate arbitrary classes from the byte stream, which has been the root of many remote-code-execution vulnerabilities (deserialisation gadget chains); it also freezes your class structure into the format and bypasses constructors and invariants (EJ Items 85 to 90). Prefer a data format with a schema and explicit mapping: JSON (Jackson), Protocol Buffers or Avro. With Jackson mind: unknown properties, records and constructor binding, date formats (`Instant` as ISO-8601, not timestamps), and never deserialize untrusted input into polymorphic types without a type allow-list.
</details>

---

## Week 8: Time, thread-safety basics and the traps around them

<details><summary><b>1. [Core] <code>Instant</code>, <code>LocalDateTime</code>, <code>ZonedDateTime</code>, <code>OffsetDateTime</code>: when do you use which, and what do you store in the database?</b></summary>

`Instant`: an exact point on the UTC timeline, the right type for "when did it happen" (created at, paid at). `LocalDateTime`/`LocalDate`/`LocalTime`: wall-clock values **without** a zone; right for things defined by the calendar, not the moment (a birthday, "opening hours 09:00", "the invoice date"); wrong for events. `OffsetDateTime`: a moment plus a fixed offset (`+02:00`), good for exchange formats and `timestamptz`. `ZonedDateTime`: a moment plus a zone **with rules** (`Europe/Paris`), needed when you must compute "same time tomorrow". Store events as UTC (`Instant` ↔ PostgreSQL `timestamptz`), send ISO-8601 strings in APIs, and keep the user's `ZoneId` separately for display.
</details>

<details><summary><b>2. [Hard] What happens at daylight-saving boundaries, and what is the difference between <code>plusDays(1)</code> and <code>plus(Duration.ofHours(24))</code> on a <code>ZonedDateTime</code>?</b></summary>

In spring a **gap** removes local times (02:30 does not exist in New York on the change day); in autumn an **overlap** repeats an hour (01:30 happens twice). `ZonedDateTime.of(...)` resolves a gap by shifting forward and an overlap by choosing the earlier offset (`withLaterOffsetAtOverlap()` picks the other). `plusDays(1)` works on the **local timeline** ("same wall-clock time tomorrow"), so over a DST change the elapsed time is 23 or 25 hours; `plus(Duration.ofHours(24))` works on the **instant timeline** and the wall-clock time shifts by an hour. Use `Duration` for elapsed time and `Period`/`plusDays` for calendar rules ("delivery the next day at 09:00"). Test the edge dates explicitly.
</details>

<details><summary><b>3. [Intermediate] What is <code>LocalDate.of(2025,1,31).plusMonths(1)</code>? What are the problems of "end of day" and "between"?</b></summary>

`2025-02-28`: when the day does not exist in the target month Java clamps to the last valid day (and then `plusMonths(1)` again gives March 28, not 31, so repeated addition is not associative; compute from the original date: `start.plusMonths(n)`). "End of day" as `23:59:59` fails for events with fractional seconds or other precision; prefer **half-open intervals** `[start, nextDayStart)`. `ChronoUnit.DAYS.between(a, b)` counts whole units truncated; `Period.between` gives years/months/days. Subscription billing ("every month on the 31st") needs an explicit rule for short months.
</details>

<details><summary><b>4. [Hard] <code>System.currentTimeMillis</code>, <code>System.nanoTime</code> and <code>Instant.now()</code>: which one measures elapsed time? Why can wall-clock time go backwards?</b></summary>

`nanoTime` is a **monotonic** clock with an arbitrary origin: use it only to measure durations (`end - start`); it is meaningless as a date. `currentTimeMillis` and `Instant.now()` are **wall-clock** time: NTP adjustments, manual changes, leap-second smearing or VM pauses can make them jump or even go backwards, so elapsed times and timeouts based on them can be negative. In distributed systems different machines also have **clock skew**, so do not order events across machines by timestamps alone (that is why logical clocks, version numbers and database sequences exist). `Instant.now()` has microsecond or nanosecond precision depending on the platform, so tests comparing timestamps need a controlled `Clock`.
</details>

<details><summary><b>5. [Hard] Why is <code>SimpleDateFormat</code> not thread-safe, and what is the classic <code>yyyy</code> vs <code>YYYY</code> bug?</b></summary>

`SimpleDateFormat` stores the date being parsed or formatted in an internal `Calendar` field; two threads overwrite each other's state, giving wrong dates, `NumberFormatException` or an `ArrayIndexOutOfBoundsException`. Fixes: `DateTimeFormatter` (immutable), or a `ThreadLocal` per thread in legacy code. The pattern letter **`YYYY` means week-based year** (ISO week), not calendar year: `31 Dec 2025` formatted with `YYYY-MM-dd` can print `2026-12-31` because that date belongs to week 1 of 2026. Use lowercase `yyyy` (year of era) or `uuuu`. This bug has hit large production systems every year-end.
</details>

<details><summary><b>6. [Core] Define race condition, data race, atomicity, visibility. Why is <code>count++</code> not thread-safe?</b></summary>

A **race condition**: the result depends on the timing of threads (e.g. check-then-act: "if stock ≥ qty then reduce"). A **data race**: two threads access the same variable, at least one writes, without synchronisation: the Java Memory Model then gives no guarantee. **Atomicity**: an operation happens as one indivisible step. **Visibility**: a write by one thread becomes seen by another; without a happens-before relation (`synchronized`, `volatile`, `Lock`, `Thread.start/join`, final-field publication) a thread may never see an update, or see it out of order. `count++` is three steps (read, add, write), so two threads can lose an update. Fixes: `AtomicInteger`, `synchronized`/lock, confinement to one thread, or immutability.
</details>

<details><summary><b>7. [Intermediate] <code>synchronized</code>, <code>volatile</code>, <code>Atomic*</code> and <code>ReentrantLock</code>: what does each give you?</b></summary>

`synchronized`: mutual exclusion **and** visibility (entering sees what the previous holder wrote), re-entrant, tied to a block/method, released automatically. `volatile`: visibility and ordering for a single variable, but **no atomicity** for compound actions (`volatile int c; c++` is still racy). `Atomic*` classes (`AtomicInteger`, `AtomicReference`): lock-free atomic updates via compare-and-swap (CAS). `ReentrantLock`: like `synchronized` plus `tryLock`, timeouts, interruptible waits, fairness and multiple conditions, but you must unlock in `finally`. Deadlock needs four conditions (mutual exclusion, hold-and-wait, no preemption, circular wait); the practical cure is a **global lock ordering** or avoiding nested locks.
</details>

<details><summary><b>8. [Hard] Name "falsehoods" about time that cause real incidents.</b></summary>

A day is not always 24 hours (DST); a minute can have 61 seconds (leap seconds; `Instant` ignores them, cloud providers "smear" them); time zones and offsets **change by law** (update the tz database; old JDK versions are out of date); local time is not unique or even valid; the system clock can go backwards; the same instant has different dates in different zones; "midnight" may not exist in some zones on DST days; birthdays on 29 February; weeks start on Sunday or Monday depending on locale. Defensive habits: UTC at rest, `Clock` injection, half-open intervals, `Duration` vs `Period`, tests at DST and year boundaries, and never parse dates with a default locale.
</details>

<details><summary><b>9. [Intermediate] You inherit code using <code>java.util.Date</code>, <code>java.sql.Timestamp</code> and <code>Calendar</code>. What are their problems and how do you migrate?</b></summary>

`Date` is mutable, misnamed (it is an instant, not a date), has 1900-based years and 0-based months in deprecated methods, and `toString()` uses the default zone, which hides the real value. `Calendar` is mutable, verbose and not thread-safe. `java.sql.Timestamp extends Date` with nanoseconds, and `timestamp.equals(date)` is not symmetric with `date.equals(timestamp)` (a classic `equals` violation). Migration: keep legacy types at the **edge** (JDBC, old libraries) and convert at once: `date.toInstant()`, `Timestamp.toInstant()`, `Date.from(instant)`; use `java.time` everywhere inside, and let the JDBC driver map `Instant`/`OffsetDateTime` directly.
</details>

---

## Week 9: Logging, debugging, Maven, testing and the quality gate

<details><summary><b>1. [Core] SLF4J vs Logback: roles, levels, and the argument-evaluation trap in <code>log.debug("x={}", expensive())</code>.</b></summary>

SLF4J is a **facade** (API); Logback (or Log4j2) is the **implementation**; libraries log through SLF4J and the application chooses one backend. Levels: ERROR (needs attention), WARN (unexpected but handled), INFO (business milestones), DEBUG (diagnostics), TRACE (very detailed); production usually runs INFO. Parameterised messages (`{}`) avoid building the string when the level is disabled, **but the arguments are still evaluated**: `expensive()` runs even when DEBUG is off. Guard with `if (log.isDebugEnabled())` or use the fluent API with a lambda (`log.atDebug().addArgument(() -> expensive()).log("x={}")`). The last argument being a `Throwable` is logged as an exception with its stack trace.
</details>

<details><summary><b>2. [Hard] What is MDC, and why does it break with thread pools?</b></summary>

The Mapped Diagnostic Context stores key-value pairs (`requestId`, `userId`) in a **thread-local** map that the log pattern prints (`%X{requestId}`), so every line of a request is correlatable. Problems: pool threads are reused, so if you forget to clear the MDC at the end of a request the next request on that thread inherits the old values; and when you hand work to another thread (`executor.submit`, `CompletableFuture`) the MDC is **not** propagated automatically; you must copy it (`MDC.getCopyOfContextMap()`) into a wrapper task. Virtual threads and reactive code need the same care. Always `try { MDC.put(...); ... } finally { MDC.clear(); }`.
</details>

<details><summary><b>3. [Core] What must never be logged, and what is log injection?</b></summary>

Passwords, tokens, API keys, session ids, full card numbers, and unnecessary personal data (GDPR); also whole request/response bodies by default. Logs are copied to many systems and kept for months. **Log injection (CRLF injection)**: if you log user input verbatim, an attacker can include newlines and forge fake log lines (`\n2025... INFO admin logged in`), or inject markup that exploits a log viewer. Mitigations: sanitise or encode newlines, prefer structured (JSON) logging where each field is a separate value, redact at one central place, and review log statements in code review. (Log4Shell, 2021, was a lookup feature evaluating attacker-controlled text.)
</details>

<details><summary><b>4. [Intermediate] Debugging toolbox: conditional breakpoints, logpoints, remote debugging, thread and heap dumps. When do you use which?</b></summary>

A **conditional breakpoint** stops only when `sku.equals("SKU-9")` (a loop with 10,000 iterations); a **logpoint** (non-suspending breakpoint) prints an expression without editing code; **evaluate expression** and watches inspect state; **remote debugging** attaches to a JVM started with `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005` (never on production: it opens a code-execution port). Stopping a process changes timing, so for race conditions prefer logs and tests. For a hung process use a **thread dump** (`jcmd <pid> Thread.print` or `jstack`) to see what each thread waits for (deadlocks are reported); for memory use a **heap dump** (`jcmd <pid> GC.heap_dump file.hprof`) analysed in Eclipse MAT or VisualVM.
</details>

<details><summary><b>5. [Intermediate] Surefire vs Failsafe, test scopes, and the difference between <code>-DskipTests</code> and <code>-Dmaven.test.skip=true</code>.</b></summary>

**Surefire** runs unit tests in the `test` phase (names `*Test`, `*Tests`, `Test*`); **Failsafe** runs integration tests in `integration-test` and checks the result in `verify` (names `*IT`, `*ITCase`), so a failing integration test still lets `post-integration-test` (stopping containers) run. Dependency scopes: `compile` (default, everywhere), `provided` (needed to compile, supplied at run time by the container), `runtime` (not needed to compile), `test` (only for tests), `import` (BOMs). `-DskipTests` compiles tests but does not run them; `-Dmaven.test.skip=true` skips compiling them too. Skipping tests in CI is a smell; fix or quarantine the flaky test instead.
</details>

<details><summary><b>6. [Hard] <code>NoSuchMethodError</code>, <code>ClassNotFoundException</code>, <code>NoClassDefFoundError</code>, <code>LinkageError</code>: what causes each?</b></summary>

`ClassNotFoundException`: code asked to load a class **by name** (reflection, `Class.forName`, a driver) and it is not on the classpath. `NoClassDefFoundError`: the class was present at compile time but is **missing at run time** (or its static initialiser failed earlier: look for an `ExceptionInInitializerError` before it). `NoSuchMethodError`/`NoSuchFieldError`: a class exists but in a **different version** than the one compiled against, the signature changed: a dependency-version conflict (see Week 1 Q6). `LinkageError` family also arises when the same class is loaded by two class loaders (common in app servers and plugin systems). Tools: `mvn dependency:tree -Dverbose`, `-Xlog:class+load`, the enforcer plugin's convergence rules.
</details>

<details><summary><b>7. [Intermediate] Test doubles: dummy, stub, fake, mock, spy. When is a mock a bad idea?</b></summary>

A **dummy** fills a parameter. A **stub** returns canned answers. A **fake** is a working lightweight implementation (an in-memory repository). A **mock** also verifies interactions (`verify(repo).save(x)`). A **spy** wraps a real object. Over-mocking couples tests to implementation details (refactor internals, tests break) and gives false confidence when the mock does not behave like the real dependency. Prefer fakes and real objects for domain logic, use mocks at architecture boundaries (email sender, payment gateway), and complement them with a few real integration tests (Testcontainers) so you test the actual SQL, serialisation and wiring.
</details>

<details><summary><b>8. [Intermediate] Why are tests flaky, and how do you fix them?</b></summary>

Causes: real time (`now()`, sleeps), randomness, shared mutable static state, test order dependence, concurrency without synchronisation (assert before the other thread finished), external services, port collisions, unordered collections (`HashSet` iteration order), time zone/locale of the machine, and slow CI machines. Fixes: inject `Clock` and seeds, isolate state per test (`@TempDir`, fresh objects), wait with latches or a polling library (Awaitility) instead of `sleep`, assert on sorted/`containsExactlyInAnyOrder`, pin the zone/locale in the build, and treat a flaky test as a bug: quarantine it, never just re-run.
</details>

<details><summary><b>9. [Hard] What does 80% code coverage tell you and what does it hide? What are branch coverage and mutation testing?</b></summary>

Line coverage says which lines **executed**, not whether the assertions would catch a bug: a test with no assertions gives coverage. **Branch coverage** counts whether both outcomes of each condition ran. **Mutation testing** (PIT) changes the code (flips `<` to `<=`, removes calls) and checks that tests fail; surviving mutants reveal weak tests. Goodhart's law applies: a target becomes a bad measure. Use coverage to find *untested areas*, set a floor (like 80%) as a smoke alarm, review test quality in code review, and cover boundaries and failure paths. In JaCoCo remember the `argLine` gotcha (a custom surefire `argLine` overwrites the agent, giving 0%).
</details>

<details><summary><b>10. [Hard] During review of a TypeScript developer's first Java code, what "TypeScript in Java" smells do you look for?</b></summary>

`Map<String,Object>` or `String` for everything instead of types (stringly typed, `Object`); `double` for money and `LocalDateTime` for instants; `null` returned for "nothing" instead of `Optional` or an empty collection; getters and setters on everything (anaemic mutable beans); `==` on strings and boxed numbers; swallowed or over-broad catches and exceptions used for control flow; static mutable state and singletons; raw types and unchecked casts; huge methods with nested `if` chains instead of polymorphism or sealed `switch`; `Optional` as a field or parameter; mutating inputs; forgetting `equals`/`hashCode`; ignoring resources (no try-with-resources); one giant class instead of small cohesive ones; tests without assertions. The Phase 1 gate rubric checks these.
</details>

---
Add your own questions below as you meet them in real interviews or at work.
