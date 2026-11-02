# Appendix A: How the web works, ports and Docker (optional reading)

This appendix holds the material that used to be in Week 1. You do **not** need it to start the course. Read it when you are curious, when a later week points here, or when a port or Docker problem appears. It is safe to skip on a first pass.

| Part | What it is | When you need it |
|---|---|---|
| 1 | A request's life: DNS, TCP, TLS, HTTP, plus your first server and threads vs the event loop | Before Week 11 (REST), or whenever you want the big picture |
| 2 | Incident: "Address already in use" | The first time a port is busy |
| 3 | Docker sanity check | Before Week 10 (Postgres is needed from Week 10 and in modeling week M7) |

The shell, signals and Git rescue skills are in Week 2.

---

## Part 1: A request's life and your first server
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

## Part 2: Incident, "Address already in use"
**Ticket:** *"I started the server and it died immediately with `Address already in use`. I did not start anything else, did I?"*

1. (10m) **PREDICT** (write answers first, no searching):
   - A) Why can two programs not listen on the same port? B) Which command could tell you *which* program holds a port? C) If `java -version` shows 25 but Maven fails, what else could name a Java location? D) What is the difference between `java` and `javac`?
   <details><summary>Answers</summary>

   A) A port is owned by one listening socket; the OS refuses a second `bind`. B) `lsof -nP -iTCP:8080 -sTCP:LISTEN` (macOS/Linux), `ss -ltnp` (Linux), `netstat -ano | findstr :8080` (Windows). C) The `JAVA_HOME` environment variable, or a Maven toolchain setting. D) `java` runs programs (the JVM); `javac` compiles source to bytecode and is only in a JDK.
   </details>
2. (10m) **REPRODUCE INC-01-01.** Terminal 1: `java core/src/main/java/shopflow/w00/TinyServer.java 8080`. Terminal 2: start the same command again. Expected: `Exception in thread "main" java.net.BindException: Address already in use`.
3. (15m) **INVESTIGATE.** In terminal 3 run `lsof -nP -iTCP:8080 -sTCP:LISTEN`. Find the `PID` column. Cross-check with `ps -p <PID> -o pid,command`. Is it the process you started in terminal 1? (Hint ladder is not provided this week; if stuck, search the exact words `Address already in use` plus the command name above.)
4. (10m) **STUDY** (targeted). Julia Evans' *Networking! ACK!* zine, the pages on ports and sockets (about 10 minutes). Answer: (a) Which part of a connection is the port, which the IP address? (b) What does it mean for a socket to be in the LISTEN state?
5. (10m) **FIX and PROVE.** Stop the first server with `kill <PID>` (this sends SIGTERM, the polite request; avoid `kill -9` unless it ignores you). Start the second one again: it should now print `Listening on ...`. Alternative fix: start it on another port: `... TinyServer.java 9090`. Stop everything.

---

## Part 3: Docker sanity check
1. (20m) **Docker sanity check.** Install Docker Desktop, start it, and run `docker run --rm hello-world`. Then start a database once: `docker run -d --name pg-lab -e POSTGRES_PASSWORD=lab -p 5432:5432 postgres:17`, check with `docker ps`, and stop it with `docker rm -f pg-lab`. You will use Postgres from Week 10.

**Tips and common problems**
- Docker: `Cannot connect to the Docker daemon` → Docker Desktop is not started → start it and wait for the whale icon to settle.
- Port 5432 is already allocated → a local Postgres is running → stop it or map another host port (`-p 5433:5432`).
