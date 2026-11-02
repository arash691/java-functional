# ShopFlow Lab

A hands-on practice repo for the Java backend roadmap. You learn by making red tests green: first by building small features, then by investigating bugs that look like real production tickets.

Study guides live in `../lessons/pilot/week-NN.md`. This repo holds the code, tests, hints and solutions those guides refer to.

## Quick start
```bash
java -version                        # JDK 21 or newer (the project targets release 21)
scripts/run-week.sh w00              # runs the smoke test; it should be RED the first time
```
You should see `expected: "Hello, Sara!" but was: "TODO"`. Open `core/src/main/java/shopflow/w00/Hello.java`, fix it, run again. That first red-to-green change is the loop you will repeat all course.

If `./mvnw` fails to download Maven, check your network/proxy and that `JAVA_HOME` points at a JDK (not a JRE).

## Layout
| Path | Purpose |
|---|---|
| `core/` | Plain Java exercises, weeks 3–9 (`shopflow.wNN` packages) |
| `api/` | Spring Boot exercises, weeks 10–12 (`shopflow.api.wNN` packages) |
| `hints/wNN/` | Five-level hint ladders for incidents and hard exercises |
| `solutions/wNN/` | Reference fixes. Open only after your tests are green |
| `scripts/` | `run-week.sh` (student), `verify-solutions.sh` (author tool) |

## How the loop works
1. **Run the week's tests** with `scripts/run-week.sh wNN` (for Spring weeks: `scripts/run-week.sh w10 api`). Red is expected.
2. **Exercises (`EX-NN-xx`)**: tests are ordered easy to hard. Make them green one at a time.
3. **Incidents (`INC-NN-x`)**: a ticket describes a symptom. A failing test reproduces it. Predict first, then investigate, then study the theory, then fix. Keep the *guard* tests green.
4. **Hints**: stuck for 10 minutes? Open `hints/wNN/<id>.md` and reveal one level at a time.
5. **Solutions**: compare only after green. Then write a ledger entry in your own words.

## Rules
- Do not edit tests to make them pass, except where a lesson tells you to write your own.
- Commit after every green group. Small commits make bisecting easy.
- Each week is independent; you don't need earlier weeks' code.

## For course authors
`scripts/verify-solutions.sh wNN [module]` checks that the starter compiles and is red, and that the solution overlay is green. Run it before publishing a week.
