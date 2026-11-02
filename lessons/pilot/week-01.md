# Week 1: Your Java toolchain: install, compile, run, Maven, first green test

**Mode:** A (build-along), with two small troubleshooting incidents (B-lite)  **Release:** v0.1 (toolchain)
**Outcome:** you have a working Java setup; you can compile and run Java programs from the command line; you can explain what the JDK, the compiler, the JVM and Maven each do; and you have turned your first red test green.

**Before you start**
- A laptop with admin rights, a GitHub account, and about 5 GB of free disk space.
- You will install a JDK and IntelliJ IDEA (Community is enough) this week. Nothing else.
- Create a folder for notes: `~/shopflow-notes/` with a file `ledger.md` (copy the template from `reality-lab/LEDGER.md`).
- Networking (DNS, TCP, TLS, `curl`), ports, Docker and the event loop are **not needed now**. They are in `lessons/appendix/appendix-a-web-ports-docker.md`. Read them later, when a lesson points there.

**Repo:** `shopflow-lab`, module `core`, package `shopflow.w00`, tag `@Tag("w00")`
Run: `scripts/run-week.sh w00`   Hints: `hints/w00/` (none this week)   Solutions: `solutions/w00/`

> Red tests are the starting point, not an error. A red test tells you exactly what the code must do.

**How to read the steps:** every step says what to do, shows an example where one helps, and tells you what you should see. The time in brackets is a budget. At twice the time, stop and use the "Stuck?" routine in `lessons/README.md`.

---

## S1 Try it (3h): install Java, compile, run, and see how it works (mode A)
*In this session you will install Java, write a few tiny programs, compile and run them by hand, and look inside the result. By the end you will understand the path "source code → compiler → bytecode → JVM", which is the base for everything else.*

1. (20m) **The big picture first.** Read this and copy the diagram into your notes:
   ```
   Hello.java --javac--> Hello.class --java--> the JVM runs it
   (source text)         (bytecode)            (a program that runs bytecode)
   ```
   - **JDK** (Java Development Kit): everything a developer needs: the compiler `javac`, the launcher `java`, tools such as `javap` and `jshell`, and the JVM.
   - **JRE** (runtime only): just enough to run programs. You always install the JDK.
   - **JVM** (Java Virtual Machine): the program that loads `.class` files and runs them. The same bytecode runs on any operating system, which is the old promise "write once, run anywhere".
   - Compare with Node: `node app.js` reads text and runs it. In Java, a compiler checks the code first and turns it into bytecode; many mistakes are caught before the program ever starts.
2. (25m) **Install the JDK.** Install JDK 25 (LTS); 21 also works. Option A, SDKMAN (macOS/Linux): `curl -s "https://get.sdkman.io" | bash`, open a **new** terminal, `sdk list java`, then `sdk install java <the Temurin 25 identifier shown, for example 25.0.1-tem>` (identifiers change; verify against current docs). Option B, Homebrew on macOS: `brew install openjdk@25`, then follow the message brew prints to link it. Windows: install a Temurin 25 or 21 MSI from the Adoptium site and tick "set JAVA_HOME". Check: `java -version` and `javac -version` must **both** print 25 (or 21). *Expected:* two lines each that contain the same major version. If `javac` is "not found", you installed a runtime only; install the JDK.
3. (25m) **Compile and run by hand.** Make a playground folder: `mkdir -p ~/java-playground && cd ~/java-playground`. Create `Hello.java`:
   ```java
   public class Hello {
       public static void main(String[] args) {
           System.out.println("Hello, Java!");
       }
   }
   ```
   *Predict:* what files exist after each command?
   - `java Hello.java` (runs the source file directly, no `.class` file is kept)
   - `javac Hello.java` then `ls` (you now have `Hello.class`)
   - `java Hello` (runs the compiled class; note: no `.class` and no `.java` in this command)
   Write in your notes the difference between the three commands.
4. (25m) **Look inside, and read error messages.** Run `javap -c Hello` and find the line with `invokevirtual ... println`: that is the bytecode for your print statement. Now break the program on purpose, one change at a time, run `javac Hello.java` and read the first line of each error:
   - delete a semicolon → `error: ';' expected`
   - rename the class to `Helo` but keep the file name → `class Helo is public, should be declared in a file named Helo.java`
   - change `println` to `printn` → `error: cannot find symbol`
   Rule: the **first** error is the one to fix; later errors are often a result of it. Restore the file.
5. (30m) **Three small samples.** Type (do not paste) and run each; write your prediction as a comment first.
   - `Greeter.java`: reads an argument.
     ```java
     public class Greeter {
         public static void main(String[] args) {
             String name = args.length > 0 ? args[0] : "world";
             for (int i = 1; i <= 3; i++) {
                 System.out.println(i + ": Hello, " + name + "!");
             }
         }
     }
     ```
     Run `java Greeter.java Sara` and `java Greeter.java`.
   - `Sum.java`: reads numbers from the keyboard until you type `0`, then prints the total (use `java.util.Scanner` on `System.in`; look up the class in the Javadoc via the dev.java site).
   - `Packages`: make folders `com/example/` with `App.java` that has `package com.example;` at the top and prints a line. Compile with `javac -d out com/example/App.java`, run with `java -cp out com.example.App`. Explain in your notes: `-d out` means "put the class files here"; `-cp out` (the *classpath*) means "look for classes here".
6. (25m) **`jshell` and a `jar`.** `jshell` is Java's playground: start it, type `1 + 2`, then `"abc".toUpperCase()`, then `int x = 10; x * 3`, then `/exit`. Then build a runnable archive: create `manifest.txt` containing `Main-Class: Hello` (end with an empty line), run `jar cfm hello.jar manifest.txt Hello.class`, then `java -jar hello.jar`. A `jar` is a zip file of classes; `-jar` is how services are usually started.
7. (30m) **Write it down.** In `~/shopflow-notes/week-01.md` answer in your own words: (a) What do `javac`, `java`, `javap`, `jshell`, `jar` each do? (b) What is bytecode and who runs it? (c) What is the classpath? (d) Why does Java report some mistakes at compile time that JavaScript only reports at run time? Then add a 5-row table "Node ↔ Java": `node file.js` ↔ `java File.java`; `npm` ↔ Maven (week 1, S2); `package.json` ↔ `pom.xml`; `node_modules` ↔ `~/.m2`; `console.log` ↔ `System.out.println`.

**Tips and common problems**
- `javac: command not found` → only a runtime is installed, or the JDK is not on your `PATH` → install the JDK, open a new terminal, check `which javac`.
- `error: class, interface, enum, or record expected` → a stray `}` or text outside the class → check the braces above the reported line.
- `Error: Could not find or load main class Hello` → you are in the wrong folder or you typed `java Hello.class` → run `java Hello` (no extension) in the folder that contains `Hello.class`.
- `java Greeter.java` works but `java Greeter` fails → no `Greeter.class` exists yet → run `javac Greeter.java` first.
- Windows paths → use `;` instead of `:` in classpaths, and `dir` instead of `ls`.

**Done when**
- [ ] `java -version` and `javac -version` both show your JDK
- [ ] `Hello`, `Greeter` and `Sum` run; the package sample runs with `-cp`
- [ ] You saw three different compiler errors and can say what each one means
- [ ] `week-01.md` has the answers to (a) to (d) and the small Node ↔ Java table

---

## S2 Build (3h): repo, Maven and your first green test
Today's ticket: **SF-00 "Make the project build and pass its smoke test."** The acceptance test is already in the repo and red.

*Doing it by hand with `javac` works for three files. A real project has hundreds of files and dozens of libraries, so we use a build tool. In Java that tool is **Maven**: it finds libraries, compiles, runs tests and builds the jar. Today you meet it step by step.*

1. (20m) **Create your repository.** Copy the whole `shopflow-lab` folder somewhere in your home directory, then `cd` into it and run `git init -b main`, `git add -A`, `git commit -m "Start from shopflow-lab"`. Create an empty private repository on GitHub and push following the instructions GitHub shows (`git remote add origin ...`, `git push -u origin main`).
2. (30m) **What Maven is, by reading.** Open the root `pom.xml` and `core/pom.xml` and find: the Java release (`maven.compiler.release`), the test libraries (JUnit, AssertJ) and the module list. Then read the Maven guide "Maven in 5 Minutes" (maven.apache.org). Write in your notes, in your own words:
   - **`pom.xml`** is the project description (the `package.json` of Java).
   - **Coordinates** `groupId:artifactId:version` name a library or your project.
   - **Dependencies** are downloaded to the local cache `~/.m2/repository` (like `node_modules`, but shared by all projects).
   - **Lifecycle phases** run in order: `validate → compile → test → package → verify → install`. Running a later phase runs all earlier ones.
   - **`mvnw`** is the *wrapper*: a small script in the repo that downloads the right Maven version, so everyone builds with the same one.
3. (20m) **Run the first red test.** `chmod +x mvnw scripts/*.sh` (macOS/Linux), then `./mvnw -v` (the first run downloads Maven; this can take a minute), then `scripts/run-week.sh w00`. You should see a failure: `expected: "Hello, Sara!" but was: "TODO"`. That is the goal: a precise error.
4. (20m) **Turn it green.** Open `core/src/main/java/shopflow/w00/Hello.java`. Make `greet("Sara")` return `Hello, Sara!`. Re-run `scripts/run-week.sh w00`. For the summary run `./mvnw -pl core test -Dgroups=w00` and look for `Tests run: 1, Failures: 0`. Commit: `git commit -am "Make HelloTest green"`.
5. (30m) **Maven by hand, one phase at a time.** Run each command, then look at what changed in `core/target/`:
   - `./mvnw -pl core clean` (deletes `target/`)
   - `./mvnw -pl core compile` (creates `target/classes` with your `.class` files; compare with what `javac` did in S1)
   - `./mvnw -pl core test` (also compiles tests and runs them; read the `Tests run:` line)
   - `./mvnw -pl core package` (creates a `.jar` under `target/`; list it with `jar tf core/target/*.jar | head`)
   - `./mvnw -pl core dependency:tree` (shows which libraries your project uses)
   Write one sentence per command saying what it did.
6. (20m) **Open it in IntelliJ.** *File → Open* the repo's root `pom.xml` → *Open as Project*. Wait for indexing and set the project SDK to your JDK (*File → Project Structure → Project*). Open `HelloTest`, click the green arrow next to the test, and run it. Then set a breakpoint on the `return` line in `Hello.greet`, right-click the arrow → *Debug*, and look at the variable `name` in the debugger panel.
7. (20m) **See the plan.** Run `scripts/run-week.sh w03`. You will see dozens of failures. That is the roadmap for the next weeks, not a problem. Skim the names of the failing tests: they are the specification of Week 3.
8. (20m) **Protect main and open your first PR.** On GitHub: *Settings → Branches → Add branch protection rule* for `main`: require a pull request before merging. Create `.github/pull_request_template.md` with sections *What*, *Why*, *How I tested it*, *Checklist (tests green, no TODOs left)*. Then: `git switch -c week-01-setup`, change something small (add a line to the README), push, open a PR, review your own diff as if it were someone else's, and merge.

**Tips and common problems**
- `./mvnw: Permission denied` → the execute bit was lost on copy → `chmod +x mvnw`.
- `Unable to find a valid JAVA_HOME` or Maven runs with the wrong Java → `JAVA_HOME` points to an old JDK → `echo $JAVA_HOME`, then unset it or point it to the new JDK (see incident INC-01-01 in S3).
- `release version 21 not supported` → the JDK that Maven uses is older than 21 → same cause as above.
- `COMPILATION ERROR` right after a clean checkout → you edited a file and left a syntax error; read the first error line: it names the file and line.
- Tests seem to run for all weeks → you ran `./mvnw test` (all tags). Use `scripts/run-week.sh w00` to filter by week.
- Maven downloads forever or fails → proxy or network → ask for the company proxy settings; `~/.m2/settings.xml` holds them (verify against current docs).

**Done when**
- [ ] `scripts/run-week.sh w00` passes after your change, and the change is committed.
- [ ] You ran every Maven command in step 5 and can say what each does.
- [ ] You ran and debugged `HelloTest` in IntelliJ.
- [ ] Your GitHub repo has branch protection, a PR template and one merged PR.

---

## S3 Incident (2h): two everyday setup problems (mode B-lite)
These are the two problems almost every beginner meets in week 1. You will cause them on purpose and learn to read them, so that they cost you five minutes later instead of an afternoon.

**INC-01-01 ticket (from you, to yourself):** *"The build says `JAVA_HOME is set to an invalid directory` and I have no idea which Java it's using."*
**INC-01-02 ticket:** *"My first Java file will not compile. The error mentions the file name and I have not touched the name."*

1. (10m) **PREDICT** (write answers first, no searching):
   - A) If `java -version` shows 25 but Maven fails with a Java error, what else could name a Java location? B) What is the difference between `java` and `javac`? C) What does the compiler print first when a class name and file name differ? D) Why is the *first* compiler error the one to fix first?
   <details><summary>Answers</summary>

   A) The `JAVA_HOME` environment variable, or a Maven toolchain setting. B) `java` runs programs (the JVM); `javac` compiles source to bytecode and exists only in a JDK. C) `class X is public, should be declared in a file named X.java`. D) Later errors are often consequences of the first one (one missing brace can cause twenty messages).
   </details>
2. (15m) **REPRODUCE INC-01-01.** Run `JAVA_HOME=/tmp/does-not-exist ./mvnw -v`. Expected: an error mentioning `JAVA_HOME`.
3. (20m) **INVESTIGATE.** Run `./mvnw -v` normally: the output lists `Java version: 25...` and `Java home: ...`. Run `echo $JAVA_HOME`, `which -a java`, and on macOS `/usr/libexec/java_home -V`; with SDKMAN also `sdk current java`. Write down: which `java` does a plain `java -version` use (the first on your `PATH`), and which one does Maven use (`JAVA_HOME` if set)? Fix: `unset JAVA_HOME` or set it to the JDK home that `./mvnw -v` reports.
4. (10m) **STUDY** (targeted): explain in three sentences what `PATH` and `JAVA_HOME` are. `PATH` is the list of folders the shell searches for commands, in order; `JAVA_HOME` is a variable that tools such as Maven read to find the JDK; the two can point to different Javas, which is the source of most "wrong Java" bugs.
5. (15m) **INC-01-02: reproduce and fix.** In `~/java-playground` create `Greeting.java` containing `public class Greeter { ... }` (class name differs from file name). Run `javac Greeting.java` and read the error. Fix it two ways: rename the file, or rename the class. Then add a typo in a method name and read `cannot find symbol` (it names the symbol and the line).
6. (15m) **GENERALIZE.** Add two ledger entries (INC-01-01, INC-01-02) in the template format: Symptom, Why, Reproduce, Observe with, Fix, Prevent, 1-minute answer. Say each answer aloud once and time it.
7. (15m) **Prevent: write an env-doctor script.** In your repo create `scripts/env-doctor.sh` that prints: `java -version`, `javac -version`, `JAVA_HOME`, `./mvnw -v | head -3`. Make it executable (`chmod +x`) and commit it.
8. (20m) **Troubleshooting page.** One page, `docs/troubleshooting.md`: "Wrong Java / JAVA_HOME", "Class name and file name differ", "`mvnw: Permission denied`", "Build can't download dependencies (proxy)". For each: symptom, command to check, fix. (Port busy and Docker problems are in appendix A.)

**Tips and common problems**
- Everything works in one terminal but not another → a different `PATH` or an old session → open a new terminal, check `which java`.
- You fixed `JAVA_HOME` in one terminal only → it was not saved → add the export line to `~/.zshrc` or `~/.bashrc`, or use SDKMAN's `sdk default java`.
- IntelliJ uses another Java than the terminal → the IDE has its own SDK setting → *File → Project Structure → SDK*.

**Done when**
- [ ] You can show which Java Maven uses (`./mvnw -v`) and which one the shell uses
- [ ] You can fix a class/file name error and explain the message
- [ ] Two ledger entries exist, each explained in under a minute
- [ ] `scripts/env-doctor.sh` and `docs/troubleshooting.md` are committed

---

## S4 Consolidate (2h)
1. (30m) **Command cheat sheet.** Create `docs/java-cheatsheet.md` with a table: command, what it does, example. Include `java`, `javac`, `javap -c`, `jshell`, `jar`, `./mvnw clean|compile|test|package|dependency:tree`, `scripts/run-week.sh`.
2. (25m) **Concepts in your own words** in `docs/how-java-works.md`: source → bytecode → JVM; JDK vs JRE vs JVM; what Maven adds; what a classpath is; the Node ↔ Java table from S1 with two more rows of your own.
3. (20m) **Flashcards.** Create 8 cards (question on the front, short answer on the back) from: `java` vs `javac`, what bytecode is, what the classpath is, the Maven phases in order, what `~/.m2` is, what the wrapper `mvnw` is for, what a red test gives you, `JAVA_HOME` vs `PATH`.
4. (20m) **Self-check.** Answer aloud, then open the answers.
   1) What happens when you run `javac Hello.java` and then `java Hello`? 2) Why do you need a JDK, not only a JRE? 3) What does `./mvnw test` do beyond running tests? 4) What is the difference between `PATH` and `JAVA_HOME`? 5) What does a red test give you that a failing app in the browser does not?
   <details><summary>Answers</summary>

   1) `javac` checks the source and writes `Hello.class` (bytecode); `java` starts the JVM, which loads that class and runs `main`. 2) The JDK includes the compiler and tools; the JRE only runs programs. 3) It runs all earlier lifecycle phases first (validate, compile, test-compile), then runs the tests. 4) `PATH` lists the folders searched for commands; `JAVA_HOME` is read by tools such as Maven to find the JDK. 5) A fast, repeatable, specific message about the exact expectation.
   </details>
5. (15m) **Ledger and commit.** Put your ledger entries into the `reality-lab/LEDGER.md` format in your repo; commit `docs/java-cheatsheet.md`, `docs/how-java-works.md`, `docs/troubleshooting.md` and the ledger.
6. (10m) **Retro** in `retro.md`: what worked, what blocked me (and for how long), what I will change. If installing tools took more than 90 minutes, note exactly where the time went.

**Output:** green `HelloTest`, repo with protected `main` and a merged PR, `week-01.md`, `how-java-works.md`, `java-cheatsheet.md`, `troubleshooting.md`, `env-doctor.sh`, two ledger entries, 8 flashcards.

**Optional (when curious):** `lessons/appendix/appendix-a-web-ports-docker.md`: how a request travels (DNS, TCP, TLS, HTTP), your first tiny server, threads vs the event loop, port conflicts, and a Docker check before Week 10.

**If stuck:** search the exact first line of the error message plus the tool name; if nothing helps in 15 minutes, write the question in your ledger as "open" and continue with the next step.
