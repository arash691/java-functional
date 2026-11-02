# Week 1: Backend mental model and your first green test

**Mode:** A (build-along), with two small troubleshooting incidents (B-lite)  **Release:** v0.1 (toolchain)
**Outcome:** you can trace a request from the browser to a server and back, you have a working Java toolchain, and you have turned your first red test green.

**Before you start**
- A laptop with admin rights, a GitHub account, about 10 GB of free disk, and a stable connection (Docker images are large).
- You will be asked to install: a JDK, IntelliJ IDEA (Community is enough), Docker Desktop. Install them during S2, not before.
- Create a folder for notes: `~/shopflow-notes/` with a file `ledger.md` (copy the template from `reality-lab/LEDGER.md`).

**Repo:** `shopflow-lab`, module `core`, package `shopflow.w00`, tag `@Tag("w00")`
Run: `scripts/run-week.sh w00`   Hints: `hints/w00/` (none this week)   Solutions: `solutions/w00/`

> Red tests are the starting point, not an error. A red test tells you exactly what the code must do.

---

## S1 Try it (3h): a request's life, and your first server (mode A)
You already know HTTP from the **client** side (`fetch`). Today you will see the same request from the **server** side.

1. (20m) **Entry quiz.** Answer without searching, then check the answers. Score yourself honestly; anything you miss goes to the flashcard list in S4.
   1) What does DNS return? 2) What is the TCP three-way handshake and why does it exist? 3) Name two things TLS gives you. 4) What is the difference between `GET` and `POST` regarding idempotency? 5) What does status 404 mean vs 500? 6) What does `git rebase` do that `git merge` does not? 7) What is a process and how is it different from a thread? 8) What does `cd`, `ls`, `grep` do? 9) In JS, what does the event loop do when you `await` a `fetch`? 10) What is a port?
   <details><summary>Answers</summary>

   1) An IP address (or several) for a hostname. 2) SYN, SYN-ACK, ACK; both sides agree on initial sequence numbers so TCP can deliver bytes reliably and in order. 3) Encryption, server authentication (certificates), and integrity. 4) `GET` is safe and idempotent; `POST` is neither by default, so retrying it can duplicate effects. 5) 404: the resource was not found (client-side problem); 500: the server failed. 6) Rebase replays your commits on a new base for a linear history; merge creates a merge commit. 7) A process has its own memory; threads share the memory of their process. 8) Change directory, list files, search text. 9) It registers a continuation and runs other callbacks until the promise settles. 10) A 16-bit number that lets one machine run many network services.
   </details>
   Scoring: 8 or more correct, skim S1 steps 4 and 7 only. Fewer than 6, do everything slowly.
2. (10m) **Check your tools.** Open a terminal and run `curl --version`, `dig -v` (or `nslookup example.com` on Windows), `openssl version`, `git --version`. Write down anything that is missing and install it (macOS: `brew install curl bind openssl git`; Debian/Ubuntu: `sudo apt install curl dnsutils openssl git`).
3. (25m) **Trace one request.** Run these and copy the output into `~/shopflow-notes/week1-trace.md`:
   - `dig example.com +short`  (DNS: the IP address)
   - `curl -v https://example.com 2>&1 | head -60`  (look for the lines starting `*   Trying`, `* Connected`, `* TLSv1.3`, `> GET / HTTP/2`, `< HTTP/2 200`)
   - `openssl s_client -connect example.com:443 -servername example.com </dev/null 2>/dev/null | head -25`  (the certificate chain)
   Annotate each block of output with the step it belongs to: DNS, TCP connect, TLS handshake, HTTP request, HTTP response.
4. (30m) **Read, with questions.** *High Performance Browser Networking* (HPBN, free online): ch. 1 "Primer on Latency and Bandwidth", the three-way handshake part of ch. 2 "Building Blocks of TCP", and the TLS handshake part of ch. 4. Answer in your notes: (a) Why does opening a new HTTPS connection cost several round trips before the first byte of data? (b) Which of those round trips could connection reuse avoid? (c) Why does latency matter more than bandwidth for small requests?
5. (30m) **Run your first server.** Predict before you run: what will `curl localhost:8080/hello` print? Then, from the root of `shopflow-lab`:
   - `java core/src/main/java/shopflow/w00/TinyServer.java` (a Java file you can run directly, no build step)
   - In a second terminal: `curl -v localhost:8080/hello`. Compare the lines with the `example.com` trace: where are the DNS and TLS steps? (Answer: gone, because it's `localhost` over plain HTTP.)
   - Open `http://localhost:8080/hello` in the browser, open DevTools, Network tab, reload, click the request. Match the Request/Response headers with the `curl -v` output.
   - Read `TinyServer.java` top to bottom. Find: the port, the handler, and the reply. Change the message, restart (Ctrl+C then run again), reload.
6. (30m) **Threads vs the event loop.** Predict first: if one request takes 2 seconds, what happens to a second request that arrives right after? Run `TinyServer` with its default single dispatcher thread, then in two terminals: `curl localhost:8080/slow` and, 0.3 seconds later, `time curl localhost:8080/hello`. Write down how long `/hello` took. Restart with a pool: `java core/src/main/java/shopflow/w00/TinyServer.java 8080 4` and repeat. Note the thread name printed in each reply.
7. (25m) **Compare with Node.** Watch Philip Roberts, *What the heck is the event loop anyway?* (about 25 min) or read the Node.js docs page "The Node.js Event Loop". Write 6 lines: how Node would handle `/slow` (non-blocking I/O, the loop stays free) versus the JVM approach here (a thread blocks while sleeping, so you need more threads). Mention what happens in Node if you block the loop with a CPU-heavy loop.
8. (10m) **Draw the diagram.** On paper or in a drawing tool: browser → DNS → TCP/TLS → load balancer (imagine) → ShopFlow service → database → back. Label which parts you traced today.

**Tips and common problems**
- `curl: (60) SSL certificate problem` → a corporate proxy rewrites certificates → ask IT for the CA bundle or use `curl --cacert <file>`; never use `-k` for anything but a throwaway test.
- `dig: command not found` on Windows → `dig` is part of the BIND tools → use `nslookup example.com` instead.
- `java: command not found` when you run TinyServer → no JDK yet → do S2 step 1 first, then come back.
- `error: class found on application class path` or `class TinyServer is public, should be declared in a file named` when launching a file → you are in a different folder or typed the wrong path → run the command from the root of `shopflow-lab` exactly as written.
- `Address already in use` → another program owns the port → that is today's first incident (S3).
- The `/hello` reply always prints `HTTP-Dispatcher` → that is the single default thread; with the pool you will see `pool-1-thread-N`.

**Done when**
- [ ] `~/shopflow-notes/week1-trace.md` has the annotated DNS, TCP, TLS and HTTP blocks.
- [ ] You have run `TinyServer` and seen `Hello from ...` in both `curl` and the browser.
- [ ] You have two timing numbers: `/hello` while `/slow` runs on the single dispatcher, and the same with 4 threads.
- [ ] You have written the Node vs JVM paragraph.

---

## S2 Build (3h): toolchain, repo and your first green test
Today's ticket: **SF-00 "Make the project build and pass its smoke test."** The acceptance test is already in the repo and red.

1. (20m) **Install the JDK.** Install JDK 25 (LTS); 21 also works. Options: SDKMAN (`curl -s "https://get.sdkman.io" | bash`, restart the terminal, `sdk list java`, then `sdk install java <the 25 LTS identifier shown>`) or Homebrew on macOS (`brew install openjdk@25`, then follow the brew message to link it). Check: `java -version` and `javac -version` must **both** print 25 (or 21). If only `java` works, you installed a JRE, not a JDK. (verify against current docs for the exact SDKMAN identifier)
2. (15m) **Create your repository.** Copy the whole `shopflow-lab` folder somewhere in your home directory, then `cd` into it and run `git init -b main`, `git add -A`, `git commit -m "Start from shopflow-lab"`. Create an empty private repository on GitHub and push following the instructions GitHub shows (`git remote add origin ...`, `git push -u origin main`).
3. (20m) **Run the first red test.** `chmod +x mvnw scripts/*.sh` (macOS/Linux), then `./mvnw -v` (the first run downloads Maven; this can take a minute), then `scripts/run-week.sh w00`. You should see a failure: `expected: "Hello, Sara!" but was: "TODO"`. That is the goal: a precise error.
4. (20m) **Turn it green.** Open `core/src/main/java/shopflow/w00/Hello.java`. Make `greet("Sara")` return `Hello, Sara!`. Re-run `scripts/run-week.sh w00`. Silence means green with `-q`; to see the summary run `./mvnw -pl core test -Dgroups=w00` and look for `Tests run: 1, Failures: 0`. Commit: `git commit -am "Make HelloTest green"`.
5. (20m) **Open it in IntelliJ.** *File → Open* the repo's root `pom.xml` → *Open as Project*. Wait for indexing and set the project SDK to your JDK (*File → Project Structure → Project*). Open `HelloTest`, click the green arrow next to the test, and run it from the IDE. Then set a breakpoint on the `return` line in `Hello.greet`, right-click the arrow → *Debug*, and look at the variable `name` in the debugger panel.
6. (20m) **See the plan.** Run `scripts/run-week.sh w03`. You will see dozens of failures. That is the roadmap for the next weeks, not a problem. Skim the names of the failing tests: they are the specification of Week 3.
7. (25m) **Protect main and add a PR template.** On GitHub: *Settings → Branches → Add branch protection rule* for `main`: require a pull request before merging. Create `.github/pull_request_template.md` with sections *What*, *Why*, *How I tested it*, *Checklist (tests green, no TODOs left)*. Then: `git switch -c week-01-setup`, change something small (add a line to README), push, open a PR, review your own diff as if it were someone else's, and merge.
8. (20m) **Docker sanity check.** Install Docker Desktop, start it, and run `docker run --rm hello-world`. Then start a database once: `docker run -d --name pg-lab -e POSTGRES_PASSWORD=lab -p 5432:5432 postgres:17`, check with `docker ps`, and stop it with `docker rm -f pg-lab`. You will use Postgres from Week 10.
9. (20m) **Read the build.** Open the root `pom.xml` and `core/pom.xml`. Find: the Java release (`maven.compiler.release`), the test libraries (JUnit, AssertJ), and the module list. Read the Maven guide "Maven in 5 Minutes" (maven.apache.org) and write what `./mvnw test` does in three words per phase: validate, compile, test.

**Tips and common problems**
- `./mvnw: Permission denied` → the execute bit was lost on copy → `chmod +x mvnw`.
- `Unable to find a valid JAVA_HOME` or Maven runs with the wrong Java → `JAVA_HOME` points to an old JDK → `echo $JAVA_HOME`, then unset it or point it to the new JDK (see incident INC-01-02 in S3).
- `release version 21 not supported` → the JDK that Maven uses is older than 21 → same cause as above.
- `COMPILATION ERROR` right after a clean checkout → you edited a file and left a syntax error; read the first error line: it names the file and line.
- Tests seem to run for all weeks → you ran `./mvnw test` (all tags). Use `scripts/run-week.sh w00` to filter by week.
- Docker: `Cannot connect to the Docker daemon` → Docker Desktop is not started → start it and wait for the whale icon to settle.
- Port 5432 is already allocated → a local Postgres is running → stop it or map another host port (`-p 5433:5432`).

**Done when**
- [ ] `java -version` and `javac -version` both show your JDK.
- [ ] `scripts/run-week.sh w00` passes after your change, and the change is committed.
- [ ] You ran and debugged `HelloTest` in IntelliJ.
- [ ] Your GitHub repo has branch protection and a PR template, and one merged PR.
- [ ] `docker run --rm hello-world` works.

---

## S3 Incident (2h): two environment incidents (mode B-lite)
These are not in the Reality Lab catalog. They are the first two entries of your ledger because everybody meets them in week 1.

**INC-01-01 ticket (from you, to yourself):** *"I started the server and it died immediately with `Address already in use`. I did not start anything else, did I?"*
**INC-01-02 ticket:** *"The build says `JAVA_HOME is set to an invalid directory` and I have no idea which Java it's using."*

1. (10m) **PREDICT** (write answers first, no searching):
   - A) Why can two programs not listen on the same port? B) Which command could tell you *which* program holds a port? C) If `java -version` shows 25 but Maven fails, what else could name a Java location? D) What is the difference between `java` and `javac`?
   <details><summary>Answers</summary>

   A) A port is owned by one listening socket; the OS refuses a second `bind`. B) `lsof -nP -iTCP:8080 -sTCP:LISTEN` (macOS/Linux), `ss -ltnp` (Linux), `netstat -ano | findstr :8080` (Windows). C) The `JAVA_HOME` environment variable, or a Maven toolchain setting. D) `java` runs programs (the JVM); `javac` compiles source to bytecode and is only in a JDK.
   </details>
2. (10m) **REPRODUCE INC-01-01.** Terminal 1: `java core/src/main/java/shopflow/w00/TinyServer.java 8080`. Terminal 2: start the same command again. Expected: `Exception in thread "main" java.net.BindException: Address already in use`.
3. (15m) **INVESTIGATE.** In terminal 3 run `lsof -nP -iTCP:8080 -sTCP:LISTEN`. Find the `PID` column. Cross-check with `ps -p <PID> -o pid,command`. Is it the process you started in terminal 1? (Hint ladder is not provided this week; if stuck, search the exact words `Address already in use` plus the command name above.)
4. (10m) **STUDY** (targeted). Julia Evans' *Networking! ACK!* zine, the pages on ports and sockets (about 10 minutes). Answer: (a) Which part of a connection is the port, which the IP address? (b) What does it mean for a socket to be in the LISTEN state?
5. (10m) **FIX and PROVE.** Stop the first server with `kill <PID>` (this sends SIGTERM, the polite request; avoid `kill -9` unless it ignores you). Start the second one again: it should now print `Listening on ...`. Alternative fix: start it on another port: `... TinyServer.java 9090`. Stop everything.
6. (20m) **INC-01-02: reproduce and investigate.** Run `JAVA_HOME=/tmp/does-not-exist ./mvnw -v`. Expected: an error mentioning `JAVA_HOME`. Then run `./mvnw -v` normally: the output lists `Java version: 25...` and `Java home: ...`. Run `echo $JAVA_HOME`, `which -a java`, and on macOS `/usr/libexec/java_home -V`. Write down: which `java` would a plain `java -version` use, and which one does Maven use? Fix: `unset JAVA_HOME` or set it to the JDK home that `./mvnw -v` reports.
7. (15m) **GENERALIZE.** Add two ledger entries (INC-01-01, INC-01-02) in the template format: Symptom, Why, Reproduce, Observe with, Fix, Prevent, 1-minute answer. Say each answer aloud once and time it.
8. (15m) **Prevent: write an env-doctor script.** In your repo create `scripts/env-doctor.sh` that prints: `java -version`, `javac -version`, `JAVA_HOME`, `./mvnw -v | head -3`, `docker --version`, and whether ports 8080 and 5432 are busy (`lsof -nP -iTCP:8080 -sTCP:LISTEN || echo "8080 free"`). Make it executable and commit it.
9. (15m) **Write the troubleshooting checklist.** One page, `docs/troubleshooting.md`: "Port busy", "Wrong Java", "Build can't download dependencies (proxy)", "Docker not running". For each: symptom, command to check, fix.

**Tips and common problems**
- `lsof` prints nothing but the port is busy → the owner is another user or IPv6 only → run `sudo lsof ...` or `ss -ltnp` on Linux.
- You killed the process and the port is still busy for a few seconds → the OS keeps closing sockets in `TIME_WAIT` (for client connections) → wait or use another port.
- `kill` says `No such process` → wrong PID or already gone → re-run `lsof`.
- Docker also holds ports → `docker ps` shows containers publishing `0.0.0.0:8080` → stop that container.

**Done when**
- [ ] You can name the process holding a port using `lsof` (or `ss`) and stop it.
- [ ] You can show which Java Maven uses (`./mvnw -v`).
- [ ] Two ledger entries exist, each explained in under a minute.
- [ ] `scripts/env-doctor.sh` and `docs/troubleshooting.md` are committed.

---

## S4 Consolidate (2h)
1. (30m) **Finish the Node ↔ JVM mapping table** in `docs/node-vs-jvm.md`. Fill every row in your own words: `package.json` ↔ `pom.xml`; `npm install` ↔ `./mvnw dependency:resolve` (or just `./mvnw test`); `node_modules` ↔ `~/.m2/repository`; `npm test` ↔ `./mvnw test`; `async/await` ↔ blocking threads (or `CompletableFuture`, later); event loop ↔ thread pool; TypeScript types ↔ Java static types (checked at compile time, also kept at runtime for classes); `undefined`/`null` ↔ `null` only; `node file.js` ↔ `java File.java` / `java -jar app.jar`.
2. (20m) **Redraw the request diagram** from memory, then compare with S1 step 8 and fix mistakes. Add the failure points: DNS fails, connection refused, TLS certificate invalid, timeout, 5xx, slow database.
3. (20m) **Flashcards.** Create 8 cards (question on the front, short answer on the back) from: the quiz items you missed, the three-way handshake, "why does an HTTPS connection take several round trips", "what does `Address already in use` mean", "`java` vs `javac`", "SIGTERM vs SIGKILL (preview for next week)".
4. (20m) **Self-check.** Answer aloud, then open the answers.
   1) Name the five steps from typing a URL to seeing the first byte. 2) Why did `/hello` wait while `/slow` ran, and what would Node do differently? 3) What does a red test give you that a failing app in the browser does not? 4) Which command shows what is listening on a port? 5) Why do you need a JDK, not only a JRE?
   <details><summary>Answers</summary>

   1) DNS lookup, TCP handshake, TLS handshake, HTTP request, server work then response. 2) The single dispatcher thread was busy sleeping; the second request waited in the queue. Node would keep the loop free while waiting on I/O so it could answer `/hello` immediately (unless the code blocked the loop with CPU work). 3) A fast, repeatable, specific message about the exact expectation. 4) `lsof -nP -iTCP:<port> -sTCP:LISTEN` or `ss -ltnp`. 5) The JDK includes the compiler (`javac`) and tools; the JRE only runs programs.
   </details>
5. (20m) **Ledger and notes.** Copy your ledger entries into `reality-lab/LEDGER.md` format inside your repo; commit `docs/node-vs-jvm.md`, `docs/troubleshooting.md`, the ledger.
6. (10m) **Retro** in `retro.md`: what worked, what blocked me (and for how long), what I will change. If installing tools took more than 90 minutes, note exactly where the time went.

**Output:** green `HelloTest`, repo with protected `main` and a merged PR, `week1-trace.md`, `node-vs-jvm.md`, `troubleshooting.md`, `env-doctor.sh`, two ledger entries, 8 flashcards.

**If stuck:** search the exact first line of the error message plus the tool name; if nothing helps in 15 minutes, write the question in your ledger as "open" and continue with the next step.
