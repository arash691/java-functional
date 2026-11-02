> **Superseded for weeks 1–12 (pilot).** The project-first version of these weeks is in `pilot/week-01.md` … `pilot/week-12.md`, backed by the `shopflow-lab` repo. This file is kept for reference; the Phase 1 gate rubric and the full reference lists still apply.

# Phase 0–1: Bridge and Java Language (Weeks 1–9)

Goal: working environment, a backend mental model, and idiomatic, tested Java.

Session format and the "Stuck?" routine are defined in `README.md`. Commands are starting points: if one fails, read the error and the tool's current docs.

**How to use this file:** every session starts with a short italic note saying *why* you are doing it. Each numbered step says what to do, shows an example where one helps, and tells you what you should see. Time boxes are budgets: at twice the time, stop and use the "Stuck?" routine. Where a step says "predict first", write your guess before you run anything.

---

## Week 1 — Backend mental model and setup
**Outcome:** by the end of the week you can explain, step by step, what happens between typing a URL and seeing a page; you have a working Java toolchain; and you have merged your first pull request.

**Who this week is for:** you already know the *client* side of the web (`fetch`, the browser DevTools). This week you learn the *server* side and install the tools you will use for the next 60 weeks.

**Before you start**
- A laptop where you have admin rights, a GitHub account, and about 10 GB of free disk space.
- Docker Desktop (or another Docker engine) installed. If you do not have it yet, install it during S2 step 3, not before.
- A notes folder. Run `mkdir -p ~/shopflow-notes && cd ~/shopflow-notes` and keep every "write in your notes" instruction of this week in files inside it (for example `week-01.md`).

**How to read the steps:** every step has a time box (the number in brackets). The time box is a budget, not a target. If you are at twice the time, stop, write down where you are stuck and use the "Stuck?" routine in `README.md`.

**If stuck:** search "what happens when you type a URL" for the big picture; re-read HPBN ch. 2 for anything about handshakes.

---

#### S1 Learn (3h): the request's life and Node vs JVM
*In this session you build the mental picture that every later week hangs on: a request travels through several machines and protocols, and each hop can be slow or fail. You also compare how Node and Java handle many requests at once, because that difference explains many later topics (thread pools, blocking, async).*

1. (45m) **Entry quiz: find out what you already know.** Open `week-01.md` in your notes and write 12 one-line questions yourself, three for each area: HTTP, DNS/TCP/TLS, Git/shell, and async programming. For example: "What does a DNS server return?", "What is the difference between `GET` and `POST`?", "What does `git rebase` do?", "What happens in JavaScript when you `await` a `fetch`?". Now answer all 12 from memory, without searching. Mark each answer green (sure and correct), yellow (partly) or red (no idea). You will check the answers at the end of the session. *Why:* a green topic can be skimmed later; red topics get your attention. This is how you stop wasting time on things you know.
   - *Check:* you have 12 questions, 12 answers, and a colour for each.
2. (50m) **Read about latency and TCP.** Read *High Performance Browser Networking* (HPBN, free online) ch. 1 "Primer on Latency and Bandwidth" and ch. 2 "Building Blocks of TCP". Read in short blocks. After each section, close the page and write 2 or 3 lines from memory. Your notes must answer: (a) What is *round-trip time* (RTT)? It is the time for a message to go to the server and the answer to come back. (b) Why does opening a new TCP connection cost one round trip before any data is sent? (Hint: the *three-way handshake*, SYN, SYN-ACK, ACK.) (c) What is *slow start* and why does it make the first bytes of a connection slow?
   - *Example of a good answer to (a):* "RTT is like asking a question by letter and waiting for the reply. If a letter takes 50 ms each way, every question costs at least 100 ms, no matter how fast I write."
3. (30m) **Read about TLS.** Read HPBN ch. 4 "Transport Layer Security (TLS)", the handshake part. From memory, list (a) how many extra round trips TLS adds on top of TCP, (b) what the server proves to the client with its certificate, and (c) what *session resumption* saves.
   - *Why this matters:* when you later see "the first request is slow but the next ones are fast", this is one of the reasons.
4. (25m) **HTTP, server view.** Skim the MDN "HTTP" guide: Overview, Messages, Methods, Status codes. Write the five status codes a backend returns most often with one sentence each: 200 OK (it worked), 201 Created (a new thing exists now), 400 Bad Request (the client sent something invalid), 404 Not Found (no such thing), 500 Internal Server Error (the server failed, not the client). Add: which of `GET`, `PUT`, `DELETE`, `POST` can be safely *retried* if the response is lost? (Answer later in the week-1 self-check: `GET`, `PUT` and `DELETE` are idempotent; `POST` is not by default.)
5. (20m) **The event loop, in motion.** Watch Philip Roberts, *What the heck is the event loop anyway?* at 1.25x speed. Then write 5 lines in your own words: how Node handles 1,000 concurrent requests with one thread. Use these words: *callback*, *event loop*, *non-blocking I/O*. A model sentence: "Node starts the read, registers a callback, and moves on to the next request; when the data arrives, the event loop runs the callback."
6. (10m) **The JVM contrast.** Write 5 lines: a Java web server (a *servlet container* such as Tomcat) usually gives each request its own thread taken from a *pool*. If your code waits for the database, that thread waits too (it is *blocked*). So the size of the pool limits how many requests wait at the same time. You do not need to solve this now; you will meet it again in Phase 5. Just write the picture down: *1 thread per request, blocked threads cost memory*.
7. Finish by checking your quiz answers from step 1 with the web or your notes, and fix the colours.

**Tips and common problems**
- "I know HTTP from the browser" → you know the client side; the server side adds connection pooling, timeouts and keep-alive → focus on what the server must do when a client is slow or gone.
- Reading HPBN straight through takes hours → it is long → read only the listed chapters and stop at the time box.
- Mixing up latency and bandwidth → both are called "speed" in everyday talk → latency is the time for one bit to arrive; bandwidth is how many bits per second you can send. A satellite link has high bandwidth and high latency.
- Your notes are only copy-paste → you are not learning → close the source and write from memory; the "from memory" rule is the point.

**Done when**
- [ ] Quiz written, answered and colour-coded
- [ ] 3 short from-memory write-ups (TCP, TLS, event loop vs threads) saved in `week-01.md`
- [ ] The five status codes are written with one sentence each

---

#### S2 Build (3h): toolchain and first Maven project
*In this session you install everything and prove that it works by building and running a tiny Java program, starting a database, and merging a pull request. Nothing here is hard, but a broken setup costs hours later, so go slowly and check each step.*

1. (25m) **Install the JDK with SDKMAN.** SDKMAN is a tool that installs and switches Java versions. Run `curl -s "https://get.sdkman.io" | bash`, then open a **new** terminal and run `sdk version`. Then run `sdk list java` and pick a Temurin build of Java 25 (an LTS release; Java 21 also works). Install it with `sdk install java <identifier>` (the identifier is the text in the last column, for example `25.0.1-tem`; identifiers change, so verify against current docs). Check with `java -version` and `javac -version`.
   - *Expected:* both commands print a version starting with 25 (or 21).
2. (20m) **Install IntelliJ IDEA** (Community is enough). Open it, choose *New Project* later; for now open Settings → Project Structure → SDKs and make sure the list contains the SDKMAN JDK (path contains `.sdkman/candidates/java`), not an old system JDK.
3. (20m) **Start PostgreSQL in Docker.** Docker runs programs in isolated boxes called *containers*. Run:
   `docker run --name shopflow-pg -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:17`
   Meaning: start a container named `shopflow-pg`, set the admin password, make port 5432 of the container reachable at port 5432 of your laptop, run in the background (`-d`). Then verify: `docker exec -it shopflow-pg psql -U postgres -c "select version();"`.
   - *Expected:* one row that says `PostgreSQL 17...`.
4. (30m) **Create the GitHub repository** `shopflow`. Add three files: `.gitignore` (choose the Java template, then add `.idea/` and `target/`), `README.md` (one sentence), and `.github/pull_request_template.md` with the headings *What*, *Why*, *How tested*, *Risks*. In the repository settings, protect `main`: require a pull request before merging. (Status checks come later, when you have CI.)
5. (45m) **Generate your first Maven project.** Maven is Java's build tool (like npm plus a build script). Install it with `sdk install maven`, then run:
   `mvn archetype:generate -DgroupId=com.shopflow -DartifactId=hello -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false`
   An *archetype* is a project template. Open the generated `hello/pom.xml` (the "package.json" of Maven). Inside `<properties>` set `<maven.compiler.release>25</maven.compiler.release>` (or 21). Read the file top to bottom and write in your notes what `groupId`, `artifactId` and `version` mean (together they name your project like `@scope/name@1.0.0` in npm).
6. (25m) **Run it two ways.** From the command line: `cd hello && mvn package` then `java -cp target/classes com.shopflow.App`. *Expected:* it prints `Hello World!`. (`mvn exec:java` is not available by default, which is why we use `java -cp`.) Then open the folder in IntelliJ and run the same class with the green arrow next to `main`. Compare the two outputs.
7. (15m) **Practise the pull-request cycle.** In the `shopflow` repository: `git switch -c chore/readme-intro`, edit `README.md`, `git add . && git commit -m "Describe the project"`, `git push -u origin chore/readme-intro`, open a pull request on GitHub (the template should appear), review your own diff, and merge it.

**Tips and common problems**
- `mvn: command not found` → Maven isn't installed (SDKMAN doesn't install it by default) → `sdk install maven`, then open a new terminal.
- `invalid target release: 25` → Maven runs with a different JDK than you think → `mvn -v` must show the same Java version as `java -version`; if not, run `sdk use java <identifier>`, and upgrade `maven-compiler-plugin` if it is very old.
- `port is already allocated` on 5432 → a local Postgres already uses that port → map another port: `-p 5433:5432` (and remember 5433 later).
- `docker: command not found` or "Cannot connect to the Docker daemon" → Docker Desktop is not running → start it and wait until it says *running*.
- Push rejected on `main` → branch protection is working as designed → create a branch and open a PR.
- The first Maven command downloads for minutes → it fills `~/.m2` (Maven's local cache, like `node_modules` but shared) → wait; later runs are fast.

**Done when**
- [ ] `java -version`, `mvn -v` and `docker ps` show the expected output
- [ ] `psql` inside the container returns the Postgres version
- [ ] `hello` prints `Hello World!` from both the command line and IntelliJ
- [ ] First PR merged using the template

---

#### S3 Lab (2h): `reality-lab` repo and tracing a request
*In this session you look at a real request with your own eyes. You will see DNS, TCP, TLS and HTTP as separate steps with their own timings. This is the first entry in your Reality Lab ledger, where you will record every "production-style" problem you learn from during the course.*

1. (15m) **Create the `reality-lab` repository** on GitHub and clone it. Copy `LEDGER.md` and `CATALOG.md` from this roadmap's `reality-lab/` folder into it and commit.
2. (25m) **Trace with `curl`.** Run `curl -v https://example.com 2>&1 | head -60`. Copy the output into `notes/trace-curl.md` and annotate each part with a comment: lines starting `*   Trying` and `* Connected` are the TCP connection; lines containing `TLS` are the TLS handshake; lines starting with `>` are the request your computer sent; lines starting with `<` are the response headers.
3. (20m) **Trace DNS.** Run `dig example.com` and then `dig example.com +trace` (verify flags with `man dig`). Find: the A record (an IPv4 address), the TTL (how long the answer may be cached, in seconds), and which servers answered at each level (root, `.com`, the domain's own server).
4. (25m) **Look at the certificate.** Run `openssl s_client -connect example.com:443 -servername example.com </dev/null`. Find the certificate *subject* (who it is for), *issuer* (who signed it), expiry (`notAfter`) and the protocol (TLS 1.2 or 1.3). Note the expiry: expired certificates are a classic production outage, and you will use this for RL-94 in Phase 8.
5. (20m) **Time each phase.** Run
   `curl -o /dev/null -s -w "dns=%{time_namelookup} connect=%{time_connect} tls=%{time_appconnect} ttfb=%{time_starttransfer} total=%{time_total}\n" https://example.com`
   five times. Put the five lines in your notes. *Predict first:* which value drops after the first run? Then explain why `dns` is large the first time and almost zero afterwards (the answer is cached by your computer).
6. (15m) **Write the first ledger entry** "RL-00 Where does request time go" with the template from `LEDGER.md`: symptom ("my first request is slower"), cause (3 sentences: DNS caching, TCP then TLS before HTTP, TTFB includes server time), and how you measured it.

**Tips and common problems**
- `dig: command not found` → minimal installs lack the DNS tools → install `bind-utils` (Fedora) or `dnsutils` (Debian/Ubuntu), or use `nslookup example.com`; on macOS `dig` is included.
- `curl` shows HTTP/2 lines you don't expect → curl negotiates HTTP/2 automatically (ALPN) → add `--http1.1` to compare the two.
- Corporate proxy → `curl: (60) SSL certificate problem` → ask IT for the CA bundle; do not use `-k` except for a throwaway test.
- Output too long to read → pipe it to a file and open it in the editor, then annotate with comments.

**Done when**
- [ ] Annotated curl, dig and openssl outputs saved
- [ ] Timing breakdown run five times with a one-paragraph explanation
- [ ] Ledger entry RL-00 committed

---

#### S4 Review + Write (2h): Node↔JVM table and request-flow diagram
*In this session you turn what you saw into artifacts you can reuse: a diagram, a translation table from Node to Java, and flashcards.*

1. (30m) **Draw the request flow** on paper: browser → DNS → TCP/TLS → load balancer → application → database → back. Mark five places where it can hang or fail, and write the symptom next to each (for example "DNS failure → the browser says *site can't be reached*", "TLS certificate expired → *connection not private*", "slow query → page spinner").
2. (30m) **Node ↔ JVM table** in `notes/node-to-jvm.md`. Start with these rows and add one sentence of your own for each: npm ↔ Maven; `package.json` ↔ `pom.xml`; `node_modules` ↔ `~/.m2` (local cache); event loop ↔ thread pool; `async/await` ↔ `CompletableFuture` (later: virtual threads); TypeScript types ↔ Java types (generics are *erased* at runtime); `console.log` ↔ SLF4J logging.
3. (30m) **Redraw the diagram** in Mermaid (a text-based diagram language that GitHub renders) or draw.io, and commit it to `docs/request-flow`.
4. (20m) **Ten flashcards** (Anki or a text file): a question on the front, an answer on the back. Good question: "What does TLS 1.3 save compared with 1.2?". Bad question: "TLS" (too vague).
5. (10m) **Weekly retro** in `retro.md`: what worked, what blocked me (and for how long), what I will change.

**Tips and common problems**
- The table feels shallow → it is a first pass → add rows each week as new surprises appear.
- The diagram tool takes too long → perfectionism → hand-draw and photograph it.
- Flashcards are too long → you are copying paragraphs → one fact per card.

**Done when**
- [ ] Diagram, table, 10 flashcards and retro committed

**Self-check (answer aloud before opening the answers):**
1. What happens between typing a URL and the first byte of the response?
2. Why does TLS add round trips?
3. Where could a request hang?
4. Which HTTP methods are safe to retry, and why does it matter?
<details><summary>Answers</summary>

1) DNS lookup (cached or recursive), TCP handshake (1 round trip), TLS handshake (1 round trip on TLS 1.3, 2 on TLS 1.2), then the HTTP request travels through a load balancer to the application, maybe to the database, and back; the first byte arrives after the server has processed it. 2) TLS must agree on keys and authenticate the server before any application data can be encrypted, and that needs message exchanges. 3) In DNS, at connect, during TLS, while waiting for a free server thread, in a slow database query, or on a lock. 4) `GET`, `PUT`, `DELETE` are idempotent (repeating them has the same effect), so a client may retry after a timeout; `POST` is not by default, so a retry can create a duplicate (a reason to use idempotency keys later).
</details>

**Output:** repo with hello-world, `reality-lab` repo with RL-00, request-flow diagram, Node↔JVM table, 10 flashcards.

## Week 2 — Linux, shell and meeting ShopFlow
**Outcome:** you are comfortable in a shell, you understand how a process is asked to stop (and what happens when it is forced), you can rescue "lost" Git work, and ShopFlow is introduced with a one-page brief and ten tickets on a board. (Requirements, glossary and design come later, after the modeling block, because you cannot write them well before you know how to model.)

**Before you start:** Week 1 complete (shell, Docker, repo). Keep writing notes in `~/shopflow-notes/`.

**If stuck:** `man <command>` first, then Julia Evans' *Bite Size Linux*; for Git messes, `git reflog` is your undo.

---

#### S1 Learn (3h): shell, processes, signals, branching
*A backend developer lives in a terminal: starting and stopping servers, reading logs, finding what uses a port. This session gives you those basics, plus the idea of a *signal*, which is how every deployment tool asks your Java program to stop.*

1. (50m) **The shell.** Work through MIT Missing Semester "The Shell". Create a scratch directory (`mkdir -p /tmp/shell-play && cd /tmp/shell-play`) and type every example yourself. Goal: use `ls`, `cd`, `pwd`, pipes (`|`), redirects (`>`, `>>`) and `$?` without thinking. *Check:* explain in one sentence what `ls | wc -l` does (it counts the entries of the directory).
2. (40m) **Shell tools and scripting.** Work through "Shell Tools and Scripting". Then write `findport.sh` which takes a port number and prints the process using it. Start with this:
   ```bash
   #!/usr/bin/env bash
   # usage: ./findport.sh 8080
   lsof -i :"$1" -sTCP:LISTEN
   ```
   Make it executable (`chmod +x findport.sh`) and test it: start `python3 -m http.server 8080` in another terminal and run `./findport.sh 8080`. *Expected:* a line showing `Python` and its PID (process id). On Linux you can use `ss -ltnp | grep ":$1"` instead.
3. (30m) **Processes and signals.** Read Julia Evans, *Bite Size Linux*: the pages on processes and signals. A *signal* is a small message the operating system delivers to a process. From memory write the difference between `SIGTERM` (15: "please stop", the program can catch it and clean up), `SIGINT` (2: what Ctrl+C sends) and `SIGKILL` (9: "stop now", cannot be caught). Add: Kubernetes sends SIGTERM first, waits (30 seconds by default), then SIGKILL.
4. (30m) **Git as a graph.** Work through "Version Control (Git)". Then run `git log --graph --oneline --all` in your `shopflow` repo and explain each line in your notes. Use these words: *commit* (a snapshot with a parent), *branch* (a movable label on a commit), *HEAD* (where you are now).
5. (30m) **Choose a branching model.** Read the trunk-based development overview (trunkbaseddevelopment.com) and Atlassian's gitflow comparison. Write 5 lines: which would you choose for a small team that ships daily, and why? A model answer starts: "Trunk-based, because short-lived branches merge in hours, so conflicts stay small and every merge can be deployed."

**Tips and common problems**
- `Permission denied` on a script → the file has no execute bit → `chmod +x script.sh`.
- Exit codes confuse you → `$?` holds the exit code of the last command; 0 means success → try `true; echo $?` (0) and `false; echo $?` (1).
- macOS vs Linux flags differ (`ss` is missing on macOS) → BSD vs GNU tools → use `lsof -i :PORT` on macOS.
- You type but nothing seems to be learned → copy-pasting commands → type them, then change one thing and predict the result.

**Done when**
- [ ] `findport.sh` works for a running process
- [ ] SIGTERM vs SIGKILL explained in `notes/week-02.md`
- [ ] Branching choice written in 5 lines

---

#### S2 Build (3h): meet ShopFlow and set up your board
*Today there is no design work. You read what ShopFlow is, copy the first tickets to a board, and write down every word you do not understand yet. Most of those words are answered in weeks 3 to 12 and in the modeling block.*

1. (20m) **Read the brief.** Read `shopflow-brief.md` (one page). In `notes/week-02.md` write 3 sentences in your own words: what ShopFlow is, who uses it, what it will become.
2. (30m) **Skim the backlog.** Read `shopflow-backlog.md` from SF-01 to SF-12. For every word you do not understand (inventory? DTO? Flyway?) add a line to `notes/questions.md`. Do not look anything up now; tick the questions off as you meet them.
3. (50m) **Create the first ten issues.** In your GitHub repo create issues SF-01 to SF-10. Copy the title and the text from the backlog (keep the number in the title) and add the label `phase-1` or `phase-2`. Do not rewrite the criteria; writing good acceptance criteria is a skill you learn in Phase 3.
4. (30m) **Board and templates.** Create a GitHub Project (Kanban) with the columns Backlog, In progress, In review, Done and add the ten issues. Add `.github/ISSUE_TEMPLATE/ticket.md` with the headings Goal, Acceptance criteria, Out of scope, Notes.
5. (20m) **Baseline sketch.** Take SF-01 (inventory) and SF-03 (product catalog). On paper list the nouns you think the code needs and draw boxes and lines between them. It does not matter if you are wrong; keep the sheet to compare with what you can do after the modeling block (M8).
6. (30m) **Explain it aloud.** Record 2 minutes explaining ShopFlow to a friend who is not technical, without jargon. Listen back once and note where you hesitated.

**Tips and common problems**
- You feel you should design now → resist; designing before you know modeling produces documents you will throw away.
- Backlog words look odd (`DTO`, `Testcontainers`) → write them in `notes/questions.md` and move on.
- Copied tickets look long → leave them as in the backlog.

**Done when**
- [ ] `notes/week-02.md` has the 3-sentence summary; `notes/questions.md` has at least 8 terms
- [ ] Ten GitHub issues exist and are on the board
- [ ] The issue template is in the repo
- [ ] The baseline sketch is saved

---

#### S3 Lab (2h): shell and Git drills
*This session is practice by breaking things on purpose: stop a server politely and rudely, and rescue commits you think you lost.*

1. (20m) **Polite vs forced stop.** Start `python3 -m http.server 8080 &`. Find it with `./findport.sh 8080`, then `kill -TERM <pid>`; confirm it is gone with `./findport.sh 8080` (no output). Start it again and use `kill -KILL <pid>`. Write down: SIGKILL gives the program no chance to clean up (open files, half-written data).
2. (20m) **Catch SIGTERM.** Save and run this script, then send `kill -TERM <pid>` from another terminal:
   ```bash
   #!/usr/bin/env bash
   trap 'echo cleaning up; exit 0' TERM
   sleep 1000 &
   wait
   ```
   *Expected:* it prints `cleaning up` and exits. This is the *graceful shutdown* behaviour a Java app needs later (RL-47).
3. (25m) **Follow a log.** Generate lines: `while true; do echo "$(date) INFO ok"; sleep 1; done > app.log &`. Then `tail -f app.log | grep --line-buffered ERROR`. In a third terminal run `echo "$(date) ERROR boom" >> app.log` and see the line appear. Stop the loop with `kill %1`.
4. (30m) **Rebase drill.** In a scratch repo create `main` with one commit, branch off, make three messy commits (`wip`, `fix`, `fix2`). Run `git rebase -i main` and in the editor change the second and third `pick` to `squash` (or `s`) and reword the first. Then do a *wrong* thing on purpose (`git reset --hard HEAD~2`) and recover with `git reflog` and `git reset --hard <hash>`.
5. (20m) **Bisect.** In a throwaway repo make 10 commits where commit 6 breaks a script `test.sh` (it exits 1). Run `git bisect start`, `git bisect bad`, `git bisect good <first-commit-hash>`, then `git bisect run ./test.sh`. *Expected:* Git prints the first bad commit.
6. (5m) **Ledger entry** for "signals and graceful shutdown".

**Tips and common problems**
- `grep` shows nothing live → output buffering → use `--line-buffered`.
- Rebase conflicts → overlapping edits → fix the files, `git add`, `git rebase --continue`; abort with `git rebase --abort`.
- Commits "lost" after a rebase or reset → they are still in the reflog → `git reflog`, then `git reset --hard <hash>`.

**Done when**
- [ ] Trap script prints `cleaning up` on SIGTERM
- [ ] You recovered a "lost" commit with reflog
- [ ] Bisect found the bad commit automatically

---

#### S4 Review + Write (2h): ADR-001 and recall
1. (30m) **Write ADR-001** "Branching model and ShopFlow stack" with headings *Context* (solo learner, small team simulated), *Options* (trunk-based, gitflow, long-lived branches), *Decision*, *Consequences*. Use the template at adr.github.io. Include one rejected alternative and why.
2. (30m) **Say it aloud.** Record a one-minute answer to "SIGTERM vs SIGKILL and why it matters for deployments". Listen to it once.
3. (30m) **Ten flashcards** from the Missing Semester notes (one fact per card).
4. (20m) **Clean up the board.** Assign each Phase 1 ticket to a week (weeks 3 to 9).
5. (10m) **Retro** in `retro.md`.

**Tips and common problems**
- ADR is vague → the decision has no reason → add the rejected alternative and why you rejected it.

**Done when**
- [ ] ADR-001 merged; recording made; retro written

**Self-check (answer aloud first):** Which signal lets an app shut down gracefully? What is the difference between a process and a thread? When do you rebase and when do you merge?
<details><summary>Answers</summary>

SIGTERM (it can be handled; SIGKILL cannot). A process has its own memory; threads share the memory of their process. Rebase local, unpublished work to keep history linear; merge (or squash-merge) when integrating shared branches you must not rewrite.
</details>

**Output:** `notes/week-02.md`, `notes/questions.md`, baseline sketch, ten tickets on a board, ADR-001, 10 flashcards.

---

## Week 3 — Java syntax, types and testing basics
**Outcome:** you can write small Java programs and test them with JUnit, and you know the three or four ways Java surprises a TypeScript developer.

**Before you start:** JDK and Maven working (Week 1). Create a practice folder in IntelliJ: *New Project → Java → Maven*, name it `java-basics`.

**If stuck:** search the exact compiler message plus "java"; re-read the dev.java language basics.

---

#### S1 Learn (3h): language fundamentals
*You already program. This session is about the *differences*: Java is compiled, statically typed, and does less for you silently. Type every example; do not paste.*

1. (40m) **Variables, types, operators.** Follow the dev.java "Learn" path pages on variables, primitive types and operators. In IntelliJ create `Basics.java` and re-type each example. Add this to see the types in action, and *predict the output of each line before running*:
   ```java
   public class Basics {
       public static void main(String[] args) {
           int a = 7, b = 2;
           System.out.println(a / b);        // ?
           System.out.println(a / (double) b); // ?
           long big = 3_000_000_000L;
           int small = (int) big;             // what happens?
           System.out.println(small);
           char c = 'A'; c += 1;
           System.out.println(c);             // ?
       }
   }
   ```
   *Expected:* `3`, `3.5`, `-1294967296`, `B`. Integer division drops the decimals; casting a too-big `long` to `int` silently wraps. Write both facts in your notes.
2. (30m) **Control flow.** Read the Java Tutorials "Learning the Java Language" section on control flow (`if`, `switch` expressions, loops, enhanced `for`). Write one `switch` *expression* that returns a value:
   ```java
   String label = switch (day) {
       case SATURDAY, SUNDAY -> "weekend";
       default -> "weekday";
   };
   ```
   Then write the same with `if/else` and compare.
3. (30m) **Methods and pass-by-value.** Read about methods, parameters and overloading (same name, different parameters). Prove Java is pass-by-value with two small methods, and print before and after:
   ```java
   static void reassign(StringBuilder sb) { sb = new StringBuilder("new"); }
   static void mutate(StringBuilder sb)   { sb.append("!"); }
   ```
   *Expected:* `reassign` changes nothing outside; `mutate` does. The reference is *copied*, so the copy can point to the same object (mutation visible) but reassigning the copy does not affect the caller.
4. (25m) **Classes, packages and `main`.** Create two packages (`shop.model`, `shop.app`), a class in each, and import across them. Note: the folder structure must match the package name.
5. (25m) **javac vs the JVM.** In a scratch folder write `Hello.java`, run `javac Hello.java` (it produces `Hello.class`), `java Hello`, and `javap -c Hello`. In the `javap` output find the line with `invokevirtual` that calls `println`. This shows that Java compiles to *bytecode* that the JVM runs.
6. (30m) **Close all tabs and write from memory** a one-page summary "What is different from TypeScript". Cover: static types checked at compile time, no truthy/falsy (`if (x)` needs a `boolean`), `==` on objects compares identity, no `undefined` (there is `null`), `int` division, and every file/class naming rule you met.

**Tips and common problems**
- `class X is public, should be declared in a file named X.java` → the file name must equal the public class name → rename the file or the class.
- `incompatible types: possible lossy conversion from double to int` → narrowing needs an explicit cast → decide if you really mean it: `(int) x`.
- Using `==` on strings "works" sometimes → string pool (identical literals share an object) → always use `.equals` for content.
- `variable might not have been initialized` → Java insists on definite assignment → give the variable a value on every path.

**Done when**
- [ ] `Basics.java` compiles and runs; the pass-by-value demo prints what you predicted
- [ ] One-page TS-vs-Java summary is in your notes

---

#### S2 Build (3h): SF-01 skeleton with tests
*You build the first ShopFlow piece, a stock counter, **test-first**: write a failing test (red), make it pass (green), then tidy up (refactor). The test tells you exactly when you are done.*

1. (20m) **Create the module.** `mvn archetype:generate -DgroupId=com.shopflow -DartifactId=inventory-core -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false`. In `pom.xml` replace the old JUnit 4 dependency with JUnit Jupiter and AssertJ (copy the dependencies from the JUnit 5 and AssertJ docs; both with `<scope>test</scope>`), set `<maven.compiler.release>` to your JDK, and make sure `maven-surefire-plugin` is a recent version (3.x).
2. (15m) **First failing test.** Create `src/test/java/com/shopflow/InventoryTest.java`:
   ```java
   class InventoryTest {
       @Test
       void unknownSkuHasZeroStock() {
           assertThat(new Inventory().stockOf("SKU-1")).isZero();
       }
   }
   ```
3. (10m) Run `mvn test`. *Expected:* it fails to compile because `Inventory` does not exist. This is "red".
4. (30m) **Make it green.** Create `Inventory` with `add(String sku, int qty)`, `remove(String sku, int qty)` and `stockOf(String sku)`, backed by a `HashMap<String,Integer>`. Write only enough code to make the first test pass, then add methods for the next tests.
5. (45m) **More tests first.** Write each test *before* the code: adding twice accumulates (add 3 and 2 → 5); `remove` below zero throws `IllegalArgumentException`; adding a negative quantity throws; an unknown SKU returns 0. Run after each one.
6. (35m) **Parameterize.** Spend 20 minutes reading the JUnit 5 User Guide "Writing Tests" and "Parameterized Tests", then convert three similar tests into one:
   ```java
   @ParameterizedTest
   @CsvSource({"3,2,5", "0,4,4", "10,0,10"})
   void addAccumulates(int first, int second, int expected) { ... }
   ```
7. (25m) **Commit and PR.** Branch `sf-01-inventory`, commit the failing test first and the implementation after (two commits show red-then-green), open a PR and review your own diff.

**Tips and common problems**
- `Tests run: 0` → old surefire does not find JUnit 5 → use surefire/failsafe 3.x in the `pom`.
- `package org.junit.jupiter.api does not exist` → missing dependency or wrong scope → add `junit-jupiter` with `<scope>test</scope>`.
- AssertJ import clashes → static import only `org.assertj.core.api.Assertions.assertThat`.
- IntelliJ runs the tests but Maven does not (or the reverse) → different configuration → trust `mvn test` as the source of truth.

**Done when**
- [ ] `mvn test` is green with at least 6 tests
- [ ] SF-01 PR is open and the red-then-green commits are visible

---

#### S3 Lab (2h): RL-01, RL-02
*Two classic Java traps. For each you write a test that fails, find out why with a tool, then fix it and keep the test as protection.*

1. (15m) **RL-01 failing test.** In `reality-lab` add:
   ```java
   @Test void integerCache() {
       Integer a = 127, b = 127, c = 128, d = 128;
       assertThat(a == b).isTrue();
       assertThat(c == d).isTrue(); // expected to FAIL
   }
   ```
   *Predict first:* which assertion fails and why?
2. (10m) Run `mvn test`. *Expected:* the second assertion fails.
3. (20m) **Observe.** Run `javap -c` on the compiled test class and find `Integer.valueOf` on the boxing lines. Read the Javadoc of `Integer.valueOf(int)`: values from -128 to 127 are cached.
4. (10m) **Explain** in 3 sentences: boxing calls `valueOf`, `valueOf` returns cached objects for small values, so `==` (identity) is true for 127 and false for 128.
5. (10m) **Fix and regress.** Compare with `.equals` (or `intValue()`) and change the test to assert equality.
6. (20m) **RL-02 failing test.** Assert that `Integer.MAX_VALUE + 1` is positive. Run it; observe the wraparound to `-2147483648`.
7. (15m) **Fix.** Use `Math.addExact(a, b)` and expect `ArithmeticException`; add the regression test. Then decide where `Inventory.add` should use `addExact` and write it in your notes.
8. (20m) **Ledger entries** RL-01 and RL-02 with the `LEDGER.md` template, including the one-minute spoken answer.

**Tips and common problems**
- The `c == d` assertion passes → you used `new Integer(...)`, or someone raised the cache size with `-XX:AutoBoxCacheMax` → use plain autoboxing and no such flag.
- Overflow confusion between `int` and `long` → literal types → write `1L` where you need a `long`.
- Using `addExact` everywhere → it is slower and noisy → use it for domain quantities (stock, money in cents), not loop counters.

**Done when**
- [ ] Both labs have a red test, an explanation and a green regression test
- [ ] Two ledger entries committed

---

#### S4 Review + Write (2h)
1. (30m) **Ten flashcards** on Java vs TypeScript. Make them code-based: "What does `System.out.println(7 / 2)` print?"
2. (30m) **Clean the tests.** Re-read the `Inventory` tests; delete any test that only repeats another and add one boundary test (quantity 0).
3. (30m) **Say it aloud.** RL-01 and RL-02, under a minute each.
4. (20m) **Update `notes/node-to-jvm.md`** with three new rows (for example `number` ↔ `int`/`long`/`double`, `===` ↔ `.equals`, `undefined` ↔ `null`).
5. (10m) **Retro.**

**Tips and common problems**
- Flashcards feel boring → they are passive → write a question with a code snippet that must be predicted.

**Done when**
- [ ] 10 new flashcards, ledger answers rehearsed, retro written

**Self-check (answer aloud first):** Why does `Integer a=127,b=127; a==b` differ from the same with 128? Is Java pass-by-value or pass-by-reference? What does `javac` produce, and who runs it?
<details><summary>Answers</summary>

Boxing uses `Integer.valueOf`, which caches −128..127, so those are the same object; 128 creates new objects and `==` compares identities. Pass-by-value: an object reference is passed by value (a copy of the reference). `javac` produces bytecode (`.class` files), which the JVM interprets and JIT-compiles.
</details>

**Output:** SF-01 skeleton merged with tests; 2 ledger entries; 10 flashcards.

---

## Week 4 — Classes, immutability and value objects
**Outcome:** you can design small, correct, immutable value types (`Money`, `Sku`) and explain why they are written that way.

**Before you start:** SF-01 merged. Have *Effective Java* (EJ) 3rd edition at hand; this week cites items by number.

**If stuck:** re-read EJ Item 10 and Item 11; use `java.util.Objects.equals` and `Objects.hash`.

---

#### S1 Learn (3h): object basics and contracts
*A value object is a small class whose identity is its content: two `Money` objects of 10.00 EUR are "the same". Getting this right removes a whole family of bugs. Read in short blocks and write your own summary after each.*

1. (30m) **Static factories and builders.** Read EJ Item 1 (static factory methods) and Item 2 (builders). From memory write when a static factory beats a constructor. Example answer: `Money.of("10.00", "EUR")` can validate and return a cached or normalised value and has a name; `new Money(...)` cannot be named.
2. (40m) **`equals` and `hashCode`.** Read EJ Items 10 and 11. In your own words write the five properties of `equals` (reflexive, symmetric, transitive, consistent, false for `null`) and the `hashCode` rule: *if two objects are equal, they must have the same hash code.* Why? `HashMap` first uses the hash code to pick a bucket, then `equals` inside the bucket.
3. (20m) **`toString`, accessibility, mutability.** Read EJ Items 12, 15 and 17. Make a checklist of the five rules for an immutable class: no setters; `final` class (or private constructor); `final` fields; make defensive copies of mutable inputs and outputs; do not let `this` escape.
4. (20m) **Defensive copies.** Read EJ Item 50. Write a 10-line example where a caller changes a `List` after passing it to your class and breaks your internal state:
   ```java
   List<String> tags = new ArrayList<>(List.of("a"));
   var p = new Product(tags);   // stores the same list
   tags.add("hacked");          // p changed too!
   ```
   Then write the fix (`List.copyOf(tags)` in the constructor).
5. (30m) **Records.** Read the dev.java page on records. Convert a small class to a record and list what Java generates for you: constructor, accessors (`x()`, not `getX()`), `equals`, `hashCode`, `toString`.
6. (30m) **`BigDecimal` and `Currency`.** Read the Javadoc of `BigDecimal` (scale, precision, `RoundingMode`) and `Currency`. Try in `jshell`: `new BigDecimal("1.0").equals(new BigDecimal("1.00"))` (false!) and `.compareTo(...)` (0). Note: `equals` compares scale; `compareTo` does not.
7. (10m) **Summaries.** One sentence per item you read.

**Tips and common problems**
- `BigDecimal("1.0").equals(new BigDecimal("1.00"))` is false → scale is part of equality → use `compareTo` or normalise the scale in the constructor.
- Records can't extend classes → they already extend `java.lang.Record` → use composition or interfaces.
- A record with a `List` field is still mutable inside → immutability is shallow → copy with `List.copyOf` in the compact constructor.
- `jshell` not found → it ships with the JDK → check that `$JAVA_HOME/bin` is on your `PATH`.

**Done when**
- [ ] Notes contain the `equals`/`hashCode` rules and the immutability checklist
- [ ] You ran the `BigDecimal` equals vs compareTo experiment

---

#### S2 Build (3h): SF-02 `Money` and SF-03 catalog model
*You apply the reading. Write tests first, in this order, and keep each class small.*

1. (30m) **Failing tests for `Money`.** Cover: two `Money` with the same amount and currency are equal (even `10` and `10.00`); `plus` and `minus` work; mismatched currencies throw `IllegalArgumentException`; a `null` amount throws. Example:
   ```java
   @Test void sameValueIsEqual() {
       assertThat(Money.of("10", "EUR")).isEqualTo(Money.of("10.00", "EUR"));
   }
   ```
2. (40m) **Implement `Money`** as `record Money(BigDecimal amount, Currency currency)`. In the compact constructor reject `null` and normalise the scale:
   ```java
   Money {
       Objects.requireNonNull(amount); Objects.requireNonNull(currency);
       amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
   }
   ```
   Add `plus`, `minus` and `times(int)`. Why `HALF_EVEN`? It is "banker's rounding" and avoids a bias when many values are rounded.
3. (20m) **Static factory.** Add `Money.of(String amount, String currencyCode)` that builds the `BigDecimal` from the string (never from a `double`).
4. (30m) **SF-03 catalog.** Create `Sku` (record with a format check, e.g. three letters, a dash, four digits like `ABC-0001`), the `Category` enum, and `Product(Sku sku, String name, Money price, Category category)`.
5. (30m) **Tests for `Product` and `Sku`:** equality, invalid format throws, blank name throws.
6. (20m) **Mutation check.** Temporarily break the code (for example remove the scale normalisation) and confirm a test fails. If no test fails, a test is missing. Restore the code.
7. (10m) **PRs.** Open SF-02 and SF-03 as two separate PRs.

**Tips and common problems**
- `new BigDecimal(0.1)` in a test → the `double` is already inexact → use the string constructor `new BigDecimal("0.1")`.
- `times` with a division throws `ArithmeticException: Non-terminating decimal expansion` → pass a scale and a `RoundingMode` to `divide`.
- The PR is huge → two concerns in one → one ticket, one PR.
- Tests for `Money` fail on `equals` → scale mismatch → make sure the compact constructor normalises.

**Done when**
- [ ] `Money`, `Sku`, `Product` implemented with passing tests
- [ ] Two PRs open or merged

---

#### S3 Lab (2h): RL-03, RL-04
1. (15m) **RL-03 failing test.** *Predict first: what does the sum print?*
   ```java
   @Test void floatMoney() {
       double sum = 0;
       for (int i = 0; i < 10; i++) sum += 0.1;
       assertThat(sum).isEqualTo(1.0); // expected to FAIL: 0.9999999999999999
   }
   ```
2. (10m) Run it and copy the actual value into your notes.
3. (15m) **Explain.** Binary floating point cannot represent 0.1 exactly (like 1/3 in decimal), so errors accumulate. Print `new BigDecimal(0.1)` to see the exact value stored (`0.1000000000000000055511151231257827...`).
4. (10m) **Fix** with `Money` sums and add the regression test.
5. (20m) **RL-04.** Write a class `BadSku` with `equals` only. Test that a `HashSet` containing `a` also contains an equal `b`; expect it to **fail**.
6. (15m) **Observe.** Print `a.hashCode()` and `b.hashCode()`: they differ (identity hash codes), so the `HashSet` looks in the wrong bucket.
7. (15m) **Fix** by adding `hashCode` with `Objects.hash(...)`; add the regression test and a test showing that a `record` has both for free.
8. (20m) **Ledger entries** RL-03 and RL-04.

**Tips and common problems**
- The `contains` test passes by luck → both objects landed in the same bucket → insert more elements or run it repeatedly.
- IntelliJ can generate `equals/hashCode` (*Code → Generate*) → use it, but review whether it uses `getClass()` or `instanceof`.

**Done when**
- [ ] Both labs go red → explained → green
- [ ] Two ledger entries committed

---

#### S4 Review + Write (2h)
1. (30m) **Refactor.** Are any of your classes mutable without a reason? Make them immutable.
2. (30m) **Notes:** "the `equals`/`hashCode` contract" and "when a record is the wrong choice" (identity entities that change over time, many optional fields, JPA entities).
3. (30m) **Say aloud** the RL-03 and RL-04 answers.
4. (20m) **Ten flashcards** (BigDecimal pitfalls, record features, the contract).
5. (10m) **Retro.**

**Tips and common problems**
- You want to make every entity a record → records can't be proxied by JPA later → keep entities as normal classes.

**Done when**
- [ ] Notes and flashcards done; PRs merged

**Self-check (answer aloud first):** What does the `equals`/`hashCode` contract require? Why is `new BigDecimal(0.1)` different from `new BigDecimal("0.1")`? When is a record the wrong choice?
<details><summary>Answers</summary>

Equal objects must have equal hash codes; `equals` must be reflexive, symmetric, transitive, consistent and false for `null`. The double literal 0.1 is already inexact, so the first captures that error; the string constructor is exact. A record is wrong for mutable entities with identity, for JPA entities needing proxies, or when you need inheritance.
</details>

**Output:** SF-02, SF-03 PRs; RL-03, RL-04 entries; 10 flashcards.

---

## Week 5 — Inheritance, interfaces, enums, sealed types
**Outcome:** you know when to compose, when to inherit, and how to model a closed set of cases so that the compiler checks you.

**Before you start:** `Money` and `Product` merged. Keep your Week 4 value types; this week adds `OrderStatus` and `PaymentResult` next to them.

**If stuck:** re-read the JEP 409 examples; the compiler's "not exhaustive" errors are the guide.

---

#### S1 Learn (3h)
*The question of the week: how do I model "one of several things"? An order is NEW, PAID or SHIPPED. A payment is approved, declined or pending, and each case carries different data. Java gives you enums, interfaces, and sealed types; you learn which to pick.*

1. (40m) **Inheritance and interfaces.** Work through the Java Tutorials "Interfaces and Inheritance": overriding, `super`, abstract classes, default methods. Type the examples. Add a tiny `Animal`/`Dog` pair and predict what `super.speak()` prints before running.
2. (30m) **Composition over inheritance.** Read EJ Item 18. From memory, explain the *fragile base class* problem: a subclass depends on how the parent calls its own methods, so a harmless change in the parent breaks the subclass. Example: a `CountingSet extends HashSet` that counts `add` calls, and `addAll` calls `add` internally, so every element is counted twice.
3. (20m) **Interfaces first.** Read EJ Items 19 and 20. Note when an interface plus an abstract skeleton class is better than a plain abstract class (several unrelated classes can implement the interface; the skeleton shares code).
4. (30m) **Enums.** Read EJ Items 34, 35, 36. Write two behaviours you could give `OrderStatus` (for example `isFinal()` and the list of next states). Remember: an enum can have fields, constructors and methods.
5. (30m) **Sealed types and pattern switch.** Read JEP 409 (sealed classes) and JEP 441 (pattern matching for `switch`). Then write:
   ```java
   sealed interface Shape permits Circle, Square {}
   record Circle(double r) implements Shape {}
   record Square(double side) implements Shape {}

   static double area(Shape s) {
       return switch (s) {
           case Circle c -> Math.PI * c.r() * c.r();
           case Square q -> q.side() * q.side();
       };
   }
   ```
   Now add `record Triangle(...) implements Shape` and to `permits`. The compiler must refuse to compile `area` until you add the case. That error is the feature.
6. (30m) **Strategy in one page.** Read *Head First Design Patterns* (HFDP) ch. 1. Summarise Strategy in 5 lines: "put the part that varies behind an interface, and pass it in".

**Tips and common problems**
- `the switch expression does not cover all possible input values` → a permitted subtype is missing → add the case; avoid `default` so that a new subtype breaks compilation instead of hiding.
- Calling an overridable method in a constructor → the subclass is not yet initialised → avoid it (EJ Item 19).
- Two interfaces with the same default method → "diamond" conflict → override it and call `A.super.m()`.
- Records and sealed: a record implementing a sealed interface must be in the same package (or same file/nested) → keep them together.

**Done when**
- [ ] The sealed `Shape` demo compiles and the missing-case error was seen
- [ ] Notes on composition vs inheritance (with the `CountingSet` example)

---

#### S2 Build (3h): OrderStatus and PaymentResult
1. (30m) **Tests for `OrderStatus`.** Allowed: `NEW→PAID→SHIPPED`. Forbidden: `SHIPPED→NEW`, `NEW→SHIPPED`. Example:
   ```java
   assertThat(OrderStatus.NEW.canMoveTo(OrderStatus.PAID)).isTrue();
   assertThat(OrderStatus.SHIPPED.canMoveTo(OrderStatus.NEW)).isFalse();
   ```
2. (40m) **Implement the enum** with `EnumSet<OrderStatus> next()` and `canMoveTo(OrderStatus)`. Give each constant its allowed next states through the constructor or a `switch`. Do not use `ordinal()` for logic.
3. (30m) **Tests for `PaymentResult`:** you can create `Approved(txId)`, `Declined(reason)`, `Pending(retryAfter)` and read their data.
4. (40m) **Implement** `sealed interface PaymentResult permits Approved, Declined, Pending` with a record for each case. Why records? Each case is a small immutable bundle of data.
5. (30m) **Describe a result.** Write `String describe(PaymentResult r)` using a pattern `switch` with record deconstruction (verify the syntax for your JDK):
   ```java
   case Approved(var txId) -> "paid, tx " + txId;
   case Declined(var reason) -> "declined: " + reason;
   case Pending(var retryAfter) -> "retry after " + retryAfter;
   ```
6. (10m) **Prove the safety net.** Temporarily add a fourth subtype and watch compilation fail in `describe`. Revert.

**Tips and common problems**
- Using enum `ordinal()` in logic → it breaks when someone reorders constants → store an explicit field (EJ Item 35).
- Records in `permits` must be in the same package or module unless nested → put them as nested types or in the same file.
- Compile error "class is not allowed to extend sealed class" → the subtype is not listed in `permits` → add it.
- Sealed branches that are not records need `final`, `sealed` or `non-sealed` → pick one.

**Done when**
- [ ] Transition and pattern-match tests pass
- [ ] You saw the compile-time exhaustiveness error and reverted

---

#### S3 Lab (2h): RL-05 and an LSP extra
1. (15m) **RL-05 failing test.** A key that changes after it was put into a map. Use your own class:
   ```java
   class MutableKey {
       String id;
       MutableKey(String id) { this.id = id; }
       @Override public boolean equals(Object o) { return o instanceof MutableKey k && k.id.equals(id); }
       @Override public int hashCode() { return id.hashCode(); }
   }
   @Test void mutableKey() {
       var key = new MutableKey("a");
       Map<MutableKey,String> map = new HashMap<>();
       map.put(key, "v");
       key.id = "b";
       assertThat(map.get(key)).isEqualTo("v"); // FAILS: null
   }
   ```
   *Predict first:* will `get` find the entry?
2. (10m) Run; observe `null`.
3. (15m) **Observe.** Print `key.hashCode()` before and after the change. Optionally inspect the `HashMap`'s `table` field in the debugger.
4. (10m) **Explain:** the entry sits in the bucket of the *old* hash; the lookup uses the *new* hash and looks in another bucket.
5. (10m) **Fix:** make the key immutable (a record) and add the regression test.
6. (30m) **LSP extra.** Write `Rectangle` (with `setWidth`/`setHeight`) and `Square extends Rectangle` (setting one side sets the other). Test: after `setWidth(5); setHeight(4)` the area must be 20. Watch it fail for a `Square`. Fix by making separate immutable types behind a `Shape` interface. This is the Liskov substitution principle: a subclass must work wherever the parent is used.
7. (30m) **Ledger entry** RL-05 and a note for the LSP extra.

**Tips and common problems**
- The debugger cannot show `table` → JDK internals are hidden → use IntelliJ's *Evaluate Expression* or skip this optional step.
- The test passes by accident → both hashes landed in the same bucket → try several values.

**Done when**
- [ ] RL-05 red → green; the LSP extra written
- [ ] Ledger entry committed

---

#### S4 Review + Write (2h)
1. (40m) **Decision note** "enum vs sealed vs interface": for each, three examples from ShopFlow (enum: `OrderStatus`; sealed: `PaymentResult`; interface: `PriceRule`). Write the rule of thumb: enum = same shape, fixed values; sealed = fixed cases with different data; interface = open for extension.
2. (30m) **Refactor** any `if/else` on a type in your code into a `switch` pattern.
3. (20m) **Say aloud** the RL-05 answer.
4. (20m) **Ten flashcards** (sealed rules, Strategy, enum vs sealed).
5. (10m) **Retro.**

**Tips and common problems**
- Everything becomes a sealed hierarchy → over-engineering → use it only for closed sets of cases.

**Done when**
- [ ] Decision note, flashcards, retro committed

**Self-check (answer aloud first):** Why prefer composition? What does `sealed` give you that an enum does not? How does the compiler help with exhaustiveness?
<details><summary>Answers</summary>

Inheritance couples the subclass to the implementation details of the parent; composition delegates through a stable interface. Sealed types let each case carry different data, while enum constants share one shape. A pattern `switch` over a sealed type without `default` must cover every permitted subtype or compilation fails.
</details>

**Output:** `OrderStatus` and `PaymentResult` with tests; RL-05 entry; decision note.

---

## Week 6 — Collections and generics
**Outcome:** you can pick the right collection and explain the choice with complexity, and you use generics safely.

**Before you start:** bookmark the JMH page (openjdk.org/projects/code-tools/jmh); you use it in S3. Your `Inventory` and `Product` from earlier weeks are the base for S2.

**If stuck:** the Javadoc of each collection states its complexity; EJ Items 28–31 cover generics.

---

#### S1 Learn (3h)
*Almost every backend method returns or consumes a collection. Choosing the wrong one gives slow code that looks fine in a test. This session builds the map in your head: which interface, which implementation, what it costs.*

1. (50m) **The Collections trail.** Work through the Java Tutorials "Collections" trail (interfaces and implementations). Build a table in your notes with rows `List`, `Set`, `Map`, `Queue`, `Deque` and, for each, the common implementations and the cost (Big-O) of add / get / remove / contains. Start it like this:

   | Interface | Implementation | get | add | contains | Notes |
   |---|---|---|---|---|---|
   | List | ArrayList | O(1) | O(1) amortised | O(n) | fast by index |
   | List | LinkedList | O(n) | O(1) at ends | O(n) | rarely the right choice |
   | Map | HashMap | O(1) | O(1) | O(1) | no order |
   | Map | TreeMap | O(log n) | O(log n) | O(log n) | sorted by key |
2. (20m) **Recall test.** From memory write the Big-O of `ArrayList.get`, `LinkedList.get`, `HashMap.get`, `TreeMap.get`, `PriorityQueue.poll`. Check against your table. (Answers: O(1), O(n), O(1) average, O(log n), O(log n).)
3. (30m) **`Comparable` vs `Comparator`.** `Comparable` is the natural order *inside* a class (`compareTo`); a `Comparator` is an order supplied from *outside*. Implement both for `Product` (natural order by SKU; comparator by price then name):
   ```java
   Comparator<Product> byPriceThenName =
       Comparator.comparing((Product p) -> p.price().amount())
                 .thenComparing(Product::name);
   ```
4. (30m) **Generics.** Work through the Java Tutorials "Generics" (bounded types, wildcards). Read EJ Items 28 to 31. Learn PECS as a sentence: *Producer Extends, Consumer Super*: if a method only **reads** items from a collection use `? extends T`; if it only **writes** items into it use `? super T`.
5. (20m) **Return values and types.** Read EJ Item 54 (return empty collections, never `null`) and Item 64 (declare variables by interface: `List<String> x = new ArrayList<>()`).
6. (30m) **Type erasure.** Generic type arguments disappear after compilation. Write two overloads `void f(List<String> a)` and `void f(List<Integer> a)` in one class and read the compiler error ("name clash: same erasure"). Write one sentence explaining why.

**Tips and common problems**
- `List.of(...)` throws `UnsupportedOperationException` on `add` → it is immutable → copy it: `new ArrayList<>(List.of(...))`.
- `Arrays.asList(...)` cannot grow → it is a view of the array → same fix.
- `TreeMap`/`TreeSet` use `compareTo`, not `equals` → a comparator that returns 0 for different objects silently loses elements → keep the comparator consistent with equality.
- `PriorityQueue` printed or iterated is not sorted → only the head is guaranteed → call `poll()` repeatedly.

**Done when**
- [ ] The collections table and a PECS note are in your notes
- [ ] You saw the "same erasure" error and can explain it

---

#### S2 Build (3h)
*You extend `Inventory` and the catalog with ordering, and you make sure callers cannot damage your internals.*

1. (30m) **Tests first.** `lowStock(int threshold)` returns the SKUs whose stock is at or below the threshold, **lowest stock first**. Example: stocks A=5, B=1, C=9, threshold 5 → `[B, A]`.
2. (30m) **Implement.** Change the internal map to `Map<Sku,Integer>`. For the low-stock list collect entries into a `PriorityQueue<StockEntry>` with a comparator, or simply sort a list; explain in a comment why you picked one.
3. (30m) **Sorted views of products.** Add `List<Product> sortedByPrice()` and `sortedByNameThenPrice()` using comparators.
4. (30m) **Protect internals.** Return `List.copyOf(...)` (or an unmodifiable view) from public methods. Write a test: the caller does `result.add(...)` and gets `UnsupportedOperationException`, and the inventory is unchanged.
5. (20m) **Edge-case tests:** two products with equal price (stable tie-breaking), empty inventory returns an empty list (not `null`).
6. (20m) **Refactor to interfaces.** Public method signatures use `Map` and `List`, not `HashMap`/`ArrayList`.
7. (20m) **PR** with the updates.

**Tips and common problems**
- Returning the internal collection → the caller can change your state → return a copy or an unmodifiable view.
- Comparator written as `(a, b) -> a - b` → integer overflow for large values → use `Integer.compare(a, b)`.
- Sorting a list with `null` elements → `NullPointerException` → `Comparator.nullsFirst(...)`.

**Done when**
- [ ] Ordering and encapsulation tests are green
- [ ] The PR is open

---

#### S3 Lab (2h): RL-08 and a benchmark
1. (15m) **RL-08 failing test.** *Predict first: does it throw?*
   ```java
   @Test void cme() {
       var list = new ArrayList<>(List.of("a","b","c","d"));
       assertThatCode(() -> { for (String s : list) if (s.equals("b")) list.remove(s); })
           .doesNotThrowAnyException(); // FAILS: ConcurrentModificationException
   }
   ```
2. (10m) **Run it.** Read the stack trace and find the `modCount` check in `ArrayList.Itr`.
3. (10m) **Explain:** the iterator remembers the list's modification count and fails fast when the list changes underneath it.
4. (15m) **Fix** with `list.removeIf(s -> s.equals("b"))` and with an explicit `Iterator.remove()`; add tests for both.
5. (10m) **The quirk.** Removing the *second-to-last* element does **not** throw (the loop ends before the check). Write a test that shows it, so you remember that the bug can hide.
6. (40m) **Extra benchmark.** Create a JMH project (use the JMH Maven archetype; verify the current command) and compare `ArrayList` vs `LinkedList` for iteration and for `add(0, x)`. Run with 2 forks and 5 iterations. Save the numbers.
7. (20m) **Ledger entry** RL-08 and the benchmark results.

**Tips and common problems**
- JMH results vary a lot → the laptop throttles or other apps run → close apps, plug in power, add iterations.
- The benchmark is suspiciously fast → the JIT removed unused code (dead-code elimination) → return the result or pass it to a `Blackhole`.

**Done when**
- [ ] RL-08 red → green; benchmark results saved

---

#### S4 Review + Write (2h)
1. (40m) **One-page collections cheat sheet:** when to use which collection and its complexity. Add one sentence of "when I would NOT use it".
2. (30m) **Review your generics:** remove raw types (`List` without `<...>`) and unchecked warnings.
3. (20m) **Say aloud** the RL-08 answer.
4. (20m) **Ten flashcards.**
5. (10m) **Retro.**

**Done when**
- [ ] The cheat sheet is committed; flashcards created

**Self-check (answer aloud first):** Why does `HashMap` degrade with a bad `hashCode`? What is type erasure? When is `LinkedList` actually appropriate?
<details><summary>Answers</summary>

Many keys collide into the same bucket and lookups become linear (in newer JDKs a crowded bucket becomes a tree, O(log n), if the keys are comparable). Generic type arguments are erased at compile time, leaving only casts, so there are no type arguments at runtime. Rarely: mostly as a `Deque` where you remove from both ends with an iterator in place; `ArrayDeque` is usually better.
</details>

**Output:** inventory updated; cheat sheet; RL-08 entry; 10 flashcards.

---

## Week 7 — Exceptions, I/O and strings
**Outcome:** you can design an error-handling strategy and read and write files safely, without losing the cause of a failure.

**Before you start:** pick the Jackson dependency (check the version your Spring Boot target uses; Boot 4 uses Jackson 3, whose package names differ from Jackson 2, so verify against current docs).

**If stuck:** EJ Items 69–77; the JUnit `@TempDir` docs.

---

#### S1 Learn (3h)
*Production code spends half its lines on failure. Good error handling means three things: the failure is detected early, the cause is never lost, and the caller can decide what to do. This session gives you the rules.*

1. (50m) **Exceptions in EJ.** Read EJ Items 69 to 77 in order. After each item write a one-line rule in your own words. Examples: "Item 69: exceptions are for exceptional conditions, not for control flow (do not end a loop by catching `ArrayIndexOutOfBounds`)." "Item 73: throw exceptions that fit the abstraction: translate a low-level `SQLException` into a domain exception."
2. (20m) **try-with-resources.** Read EJ Item 9. Write an example with two resources:
   ```java
   try (var in = Files.newBufferedReader(src); var out = Files.newBufferedWriter(dst)) {
       // copy
   }
   ```
   Explain: resources close in **reverse** order (`out` first), and if both the body and `close()` throw, the `close()` exception is attached as *suppressed* to the main one.
3. (35m) **Exceptions tutorial.** Type the examples in the Java Tutorials "Essential Classes: Exceptions". Make sure you can say the difference between checked (the compiler forces you to handle them: `IOException`) and unchecked (`RuntimeException` and subclasses).
4. (35m) **Files with NIO.** Read the Java Tutorials "Basic I/O" and the `java.nio.file` pages: `Path`, `Files.readString`, `Files.writeString`, `Files.newBufferedReader`. Try each in a scratch class with a file in a temp folder.
5. (20m) **Strings.** Strings are immutable; building a string in a loop with `+=` is slow, so use `StringBuilder`. Try a text block (`"""..."""`) for multi-line JSON. **Always pass `StandardCharsets.UTF_8`** when converting between text and bytes; the platform default differs between machines.
6. (20m) **Draft your error strategy** in 10 lines: when to use checked vs unchecked; where exceptions are translated (at module boundaries); how errors will later reach an HTTP API (a domain exception becomes a 4xx or 5xx response). This becomes ADR-002 in S4.

**Tips and common problems**
- `FileNotFoundException` vs `NoSuchFileException` → the old `java.io` API and the NIO API throw different types → know both.
- Garbled characters (`Ã©`) → the wrong charset was assumed → always specify UTF-8 explicitly.
- `catch (Exception e)` hides bugs → it is too broad → catch the narrowest type you can handle.

**Done when**
- [ ] The rules from Items 69–77 are written (nine one-liners)
- [ ] The draft error strategy is saved

---

#### S2 Build (3h): SF-04 persistence to file
*Save the inventory to a JSON file and load it back, and make failure cases explicit.*

1. (20m) **Round-trip test first** using `@TempDir`:
   ```java
   @Test void saveThenLoadRoundTrips(@TempDir Path dir) {
       var repo = new InventoryRepository();
       var inv = new Inventory(); inv.add(Sku.of("ABC-0001"), 5);
       repo.save(dir.resolve("inv.json"), inv);
       assertThat(repo.load(dir.resolve("inv.json")).stockOf(Sku.of("ABC-0001"))).isEqualTo(5);
   }
   ```
2. (40m) **Jackson.** Add `jackson-databind` to the `pom.xml` and implement `InventoryRepository.save(Path, Inventory)` and `load(Path)` writing JSON. Keep the JSON shape simple (a map from SKU text to number).
3. (30m) **Missing file.** Test that `load` of a missing path throws `InventoryStorageException`, an unchecked domain exception that keeps the `cause` and the `path` and has a message such as `Cannot read inventory file /tmp/x.json`.
4. (30m) **Corrupt file.** Write `{ not json` into a file and test that `load` throws the same exception with a message that mentions the path.
5. (30m) **Atomic write.** Write to a temporary file next to the target, then `Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE)`. Why? If the program crashes mid-write, the old file must stay intact (verify filesystem support).
6. (20m) **Close everything** with try-with-resources.
7. (10m) **PR for SF-04.**

**Tips and common problems**
- `@TempDir` is not injected → you imported the JUnit 4 annotation → use `org.junit.jupiter.api.io.TempDir`.
- Jackson cannot read records or immutable classes → it needs constructor information or a module → verify the configuration for your Jackson version; or map to a simple `Map<String,Integer>` first.
- The root cause is missing from the stack trace → you did not pass the `cause` to the exception constructor → always pass it: `new InventoryStorageException(path, e)`.

**Done when**
- [ ] The round-trip, missing-file and corrupt-file tests are green
- [ ] The PR is open

---

#### S3 Lab (2h): RL-09 and a leak extra
1. (15m) **RL-09 failing test.** The production code swallows the cause:
   ```java
   try { Files.readString(Path.of("nope")); }
   catch (Exception e) { log.error("failed"); return null; }
   ```
   Write a test with a log-capturing appender (Logback `ListAppender`) asserting that the logged event carries the exception with its stack trace. It **fails**.
2. (10m) **Run it** and observe that the log line has no exception attached.
3. (10m) **Explain:** logging only a text, or swallowing the exception, destroys the cause and the stack trace, which are the only things you will have at 3 a.m.
4. (15m) **Fix:** throw `new InventoryStorageException(path, e)` and, where you log, pass the exception as the last argument: `log.error("Cannot read {}", path, e)`.
5. (20m) **Regression test** that checks the cause chain with `getCause()`.
6. (30m) **Extra: a resource leak.** Write a `TrackingInputStream` whose `close()` sets a flag. Show with a test that forgetting to close leaves the flag false, then fix with try-with-resources.
7. (20m) **Ledger entry** RL-09.

**Tips and common problems**
- Logging only `e.getMessage()` → the stack trace is lost → pass the exception object as the last argument.
- Logging **and** rethrowing → the same failure appears twice in the logs → log once, at the boundary where you handle it.

**Done when**
- [ ] RL-09 and the leak extra go red → green; the ledger entry is committed

---

#### S4 Review + Write (2h)
1. (50m) **Write ADR-002 "Error handling strategy"** with *Context*, *Options* (all checked / all unchecked / mixed), *Decision* (for example: checked only for recoverable failures at the API of a module, unchecked domain exceptions inside, translate at boundaries, one catch-and-log at the top), *Consequences* (including how errors will map to HTTP later).
2. (25m) **Review your SF-04 PR** for broad catches and swallowed causes.
3. (20m) **Say aloud** the RL-09 answer.
4. (15m) **Ten flashcards.**
5. (10m) **Retro.**

**Done when**
- [ ] ADR-002 merged

**Self-check (answer aloud first):** When should an exception be checked? What does try-with-resources guarantee? Why is `catch (Exception e) {}` dangerous?
<details><summary>Answers</summary>

When the caller can reasonably recover and you want to force them to consider it; most failures are better unchecked. Resources are closed in reverse order even when exceptions happen, and close failures are added as suppressed exceptions. It hides bugs and failures, removes the cause, and leaves the system in an unknown state.
</details>

**Output:** SF-04, ADR-002, RL-09 entry, 10 flashcards.

---

## Week 8 — Time, money and thread-safety basics
**Outcome:** you do not get bitten by dates, time zones or shared formatters, and you can explain why.

**Before you start:** `Money` and file persistence exist from earlier weeks.

**If stuck:** the Java Tutorials "Date Time"; search the title "falsehoods programmers believe about time".

---

#### S1 Learn (3h)
*Time is the source of more production bugs than almost anything else. The goal today is a small vocabulary and one habit: store an exact moment in UTC, and convert to a local time only for display.*

1. (50m) **The `java.time` types.** Work through the Java Tutorials "Date Time" and type the examples. Know these in your own words: `Instant` (a point on the global timeline, e.g. `2025-03-09T07:00:00Z`), `LocalDate` (a calendar date, no time or zone), `LocalDateTime` (date and time on a wall clock, **no zone**, so not a real moment), `ZonedDateTime` (a moment plus a zone like `Europe/Paris`), `Duration` (an amount of time in seconds) and `Period` (an amount of calendar days/months).
2. (30m) **Falsehoods.** Search "Falsehoods programmers believe about time", read it, and write five items you would have believed yesterday ("a day has 24 hours", "time never goes backwards").
3. (30m) **`Clock`.** Read about `Clock.systemUTC()` and `Clock.fixed(instant, zone)`. Write in 3 lines why a class should receive a `Clock` instead of calling `Instant.now()`: a test can then freeze time, so the result never changes between runs.
4. (30m) **Daylight saving time (DST).** Predict and then print:
   ```java
   var ny = ZoneId.of("America/New_York");
   System.out.println(ZonedDateTime.of(LocalDateTime.parse("2025-03-09T02:30"), ny)); // a gap: this time does not exist
   System.out.println(ZonedDateTime.of(LocalDateTime.parse("2025-11-02T01:30"), ny)); // an overlap: this time happens twice
   ```
   Write down what Java did: it moves the gap time forward (02:30 becomes 03:30), and for the overlap it picks the earlier offset by default.
5. (20m) **Immutability and threads.** Re-read EJ Item 17: an object that never changes can be shared by any number of threads without locks. `java.time` types are immutable; the old `SimpleDateFormat` is not.
6. (20m) **Formatting.** Try `DateTimeFormatter.ISO_INSTANT` and a locale-aware format. Rule: machines exchange ISO-8601 (`2025-03-09T07:00:00Z`); humans see a local format.

**Tips and common problems**
- `LocalDateTime` used for events (an order placed, a login) → it has no zone, so you cannot know which moment it was → store an `Instant` or `OffsetDateTime`.
- `Date` and `Calendar` appear in old code or libraries → legacy API → convert at the edge with `date.toInstant()`.
- A DST gap time is silently shifted → the zone rules resolve it → test those edges explicitly.

**Done when**
- [ ] The DST gap and overlap results are explained in your notes
- [ ] Five "falsehoods" are written

---

#### S2 Build (3h): SF-05 order timestamps
1. (20m) **Test first with a fixed clock.**
   ```java
   var clock = Clock.fixed(Instant.parse("2025-03-09T07:00:00Z"), ZoneOffset.UTC);
   var service = new OrderService(clock);
   assertThat(service.place(...).placedAt()).isEqualTo(Instant.parse("2025-03-09T07:00:00Z"));
   ```
2. (30m) **Implement.** Add `Instant placedAt` to `Order` and inject a `Clock` into `OrderService`; use `Instant.now(clock)`.
3. (40m) **Display in the user's zone.** Add `String displayFor(Order, ZoneId)` that formats `placedAt` in that zone.
4. (40m) **DST tests.** Orders placed at `2025-03-09T06:59:59Z` and `2025-03-09T07:00:00Z` shown in `America/New_York` must read `01:59:59` and `03:00:00` (the clocks jumped forward).
5. (20m) **A 24-hour rule.** A "delivered within 24 hours" check must use `Duration.between(placedAt, deliveredAt)`, not days. Write a test that spans a DST change.
6. (20m) **Persist** `placedAt` as ISO-8601 UTC text in the JSON file and read it back.
7. (10m) **PR for SF-05.**

**Tips and common problems**
- Tests depend on the real current time → they are flaky → inject the clock everywhere.
- `LocalDate.now()` or `Instant.now()` deep in the code → a hidden dependency → pass a `Clock`.

**Done when**
- [ ] The DST tests are green; there is no `now()` call without a clock

---

#### S3 Lab (2h): RL-06, RL-07
1. (15m) **RL-06.** Write a `main` that prints `ZoneId.systemDefault()`, `Charset.defaultCharset()` and a formatted `LocalDateTime.now()`. Run it twice, with VM options `-Duser.timezone=America/New_York` and then `-Duser.timezone=Asia/Tokyo` (in IntelliJ: *Run → Edit Configurations → VM options*).
2. (15m) **Observe the differences.** Also try `-Dfile.encoding=ISO-8859-1` and write a text with accents. JDK 18 and later default to UTF-8, so the old behaviour appears only if you set the flag (verify against current docs).
3. (10m) **Explain:** the default zone and charset come from the machine, so the same code behaves differently on your laptop and on the server.
4. (10m) **Fix:** never rely on defaults. Pass the `ZoneId` and charset explicitly, and write a test that fails under another `-Duser.timezone` before the fix and passes after.
5. (30m) **RL-07 failing test** with a shared `SimpleDateFormat`:
   ```java
   var f = new SimpleDateFormat("yyyy-MM-dd");
   var pool = Executors.newFixedThreadPool(16);
   // submit 10_000 tasks that call f.parse("2025-03-09"); collect exceptions and wrong results
   ```
6. (15m) **Run it.** Observe `NumberFormatException` or wrong dates, intermittently.
7. (10m) **Explain:** the formatter keeps mutable internal state (a `Calendar`) that all threads overwrite at once.
8. (15m) **Fix** with the immutable `DateTimeFormatter`; add the regression test; write ledger entries RL-06 and RL-07.

**Tips and common problems**
- You cannot reproduce the race → too few threads or tasks → increase both, and use a `CountDownLatch` so all threads start together.
- `-D` flags are ignored in IntelliJ → they went to the wrong place → add them under *VM options*, not *Program arguments*.

**Done when**
- [ ] Both labs go red → green; ledger entries are committed

---

#### S4 Review + Write (2h)
1. (50m) **Write `docs/time-rules.md`:** store UTC (`Instant`) at rest; use ISO-8601 in APIs; never use `LocalDateTime` for events; inject `Clock`; convert to a zone only at the edge for display. Add one example for each rule.
2. (25m) **Search your code** for `LocalDateTime`, `new Date()` and `now()`; fix each hit or write why it is fine.
3. (20m) **Say aloud** the RL-06 and RL-07 answers.
4. (15m) **Ten flashcards.**
5. (10m) **Retro.**

**Done when**
- [ ] The time-rules doc is merged and the search is clean

**Self-check (answer aloud first):** Which type represents an instant on the timeline? Why inject a `Clock`? What breaks at a DST boundary?
<details><summary>Answers</summary>

`Instant` (or `OffsetDateTime`/`ZonedDateTime`, which also carry an offset). It makes time deterministic in tests. Local times may not exist (a gap) or occur twice (an overlap), and "one day" is not always 24 hours.
</details>

**Output:** SF-05, the time-rules doc, RL-06 and RL-07 entries, 10 flashcards.

---

## Week 9 — Consolidation and Phase 1 gate
**Outcome:** you pass the Phase 1 gate review: your Java code is idiomatic, tested and free of the classic traps.

**Before you start:** all Phase 1 PRs are merged.

**If stuck:** the SLF4J manual "Getting started"; the IntelliJ debugger docs for breakpoints.

---

#### S1 Learn (3h): logging, debugging, Maven lifecycle
*Three everyday tools: logs to see what a running program did, a debugger to stop it and look inside, and Maven to know what happens when you press "build".*

1. (40m) **Logging.** Read the SLF4J manual (the API, parameterised messages, levels) and the Logback configuration manual. Create `src/main/resources/logback.xml` with a console appender and a pattern that includes `%X{requestId}` (an MDC value you will set later for web requests). Write `log.info("stock of {} is {}", sku, qty)` and see the output.
2. (30m) **Debugger.** In IntelliJ set a breakpoint, run with the *Debug* button, and practise *step over*, *step into*, *step out*, a **conditional breakpoint** (right-click the red dot, condition `sku.equals("SKU-9")`), *evaluate expression* and *watches*. Success: you stopped only on the one SKU you care about.
3. (40m) **Maven lifecycle.** Read "Introduction to the Build Lifecycle": the phases (`validate`, `compile`, `test`, `package`, `verify`, `install`, `deploy`) and that plugin goals are bound to phases. Run `mvn help:effective-pom` and read one section (for example how surefire is configured).
4. (30m) **Dependencies.** Run `mvn dependency:tree`. Find one transitive dependency (one you did not declare) and note which of yours pulled it in. Read about scopes (`compile`, `test`, `provided`) and `dependencyManagement`.
5. (20m) **Style.** Skim the Google Java Style Guide: naming and imports.
6. (20m) **Recall.** From memory write the Maven phases from `validate` to `install` in order, then check.

**Tips and common problems**
- `SLF4J: No SLF4J providers were found` → only the API is on the classpath → add `logback-classic`.
- A warning about multiple SLF4J bindings → two logging backends → exclude one in `dependency:tree`.
- Breakpoints are ignored → you started with *Run*, not *Debug* → use the bug icon.

**Done when**
- [ ] `logback.xml` works
- [ ] You used a conditional breakpoint once

---

#### S2 Build (3h): gate prep
1. (30m) **Coverage.** Add the JaCoCo plugin (`prepare-agent` and `report` goals), run `mvn verify` and open `target/site/jacoco/index.html`.
2. (40m) **Close the gaps.** Find uncovered lines and write tests for *behaviour* (what the method promises), not for trivial getters.
3. (30m) **Formatter.** Add Spotless (or Checkstyle) with the Google style and run `mvn spotless:apply`.
4. (30m) **Static analysis.** Add SpotBugs, run it, and triage each finding: fix it, or document why it is a false positive.
5. (30m) **Self-audit against the gate.** Search the code for `double` in money code, `LocalDateTime`, empty `catch` blocks and `System.out.println`.
6. (20m) **Logging.** Add parameterised log lines at meaningful points (a stock change, a save), with no secrets.

**Tips and common problems**
- JaCoCo shows 0% → the agent is not attached because a custom `argLine` overwrote it → use `@{argLine}` in surefire's `argLine`.
- Spotless fails the build → the formatting drifted → run `apply` before you commit.
- You chase 100% coverage → vanity → aim for meaningful tests; 80% is a floor, not a goal.

**Done when**
- [ ] Coverage is at least 80% with meaningful tests; the formatter and SpotBugs are clean

---

#### S3 Lab (2h): RL-25 and a debugger extra
1. (15m) **RL-25 failing test.** Log a request that contains `Authorization: Bearer abc123` and assert (with a `ListAppender`) that no log line contains `abc123`. It **fails**.
2. (10m) **Run it** and see the token in the output.
3. (10m) **Explain:** logs are plain text, are copied to many systems and kept for months, so secrets and personal data must never enter them.
4. (25m) **Fix:** write a `redact(String)` helper that masks tokens and sensitive keys (`password`, `authorization`, `token`), apply it at the logging call sites and add a regression test.
5. (10m) **Read** the OWASP Logging Cheat Sheet and list five things not to log.
6. (35m) **Extra: debugger only.** Introduce a deliberate off-by-one in `lowStock` (for example `<` instead of `<=`), then find it with a conditional breakpoint, **without** adding any print statement. Fix and revert.
7. (15m) **Ledger entry** RL-25.

**Tips and common problems**
- The redaction misses variants (`bearer`, `BEARER`) → patterns are incomplete → combine key-based and pattern-based redaction, and make it case-insensitive.
- Debugging changes the timing of concurrent code → the bug disappears (a "heisenbug") → use logs for those.

**Done when**
- [ ] RL-25 goes red → green; the debugger extra is done; the ledger entry is committed

---

#### S4 Review + Write (2h): gate review
1. (50m) **Gate review** with a mentor or peer. They read `inventory-core` against the EJ items and ask: "Is any of this TypeScript written in Java?". Without a reviewer: record yourself walking through the code, then watch the recording with the rubric below in hand.
2. (30m) **Fix** the findings.
3. (15m) **Update the progress tracker** in `java-backend-roadmap.md` and tag the repo `phase-1`.
4. (15m) **Retro** and a short plan for Phase 2.
5. (10m) **Count your ledger entries** and rehearse three aloud.

**Gate rubric (all must be true)**
- No `double` for money, no `LocalDateTime` for instants, no swallowed exceptions.
- Every public value type has a correct `equals`, `hashCode` and `toString`.
- Tests describe behaviour; they are not padding for coverage.
- You can explain every ledger entry in under one minute.

**Done when**
- [ ] Every rubric line is ticked; the `phase-1` tag is pushed

**Self-check (answer aloud first):** Which Maven phase runs unit tests? Why is `log.info("x=" + x)` worse than `log.info("x={}", x)`? What must never appear in logs?
<details><summary>Answers</summary>

`test` (via the surefire plugin). The parameterised form does not build the string when the level is disabled and keeps messages uniform. Secrets, tokens, passwords and unnecessary personal data.
</details>

**Output:** the `phase-1` tag in git, gate notes, 9 to 10 ledger entries.
