# Pilot authoring spec (weeks 1–12, project-first, problem-first)

This spec makes every pilot week consistent. It applies to `lessons/pilot/week-NN.md` and to the matching code in `shopflow-lab/`.

## Learning model
Three modes, chosen per concept:
- **A. Build-along** (new concept, no mental model): run it → read it → modify one thing → predict → break it on purpose. At most 30–45 min of direct explanation. Feedback within minutes through small tests that go green one by one.
- **B. Break-it / Incident** (she knows normal behaviour, not failure): ticket → predict → reproduce (red test) → investigate (tool) → study (targeted reading, only now) → fix → prove (green + regression) → generalize (ledger entry).
- **C. Design-it**: not used in the pilot.

## Weekly file structure (`lessons/pilot/week-NN.md`)
```
# Week N: <title>
**Mode:** A | A→B | B      **Release:** v0.1 CLI | v0.2 Spring slice
**Outcome:** one sentence.
**Before you start:** setup/prereqs, exact commands.
**Repo:** shopflow-lab, module <core|api>, package shopflow.wNN, tag @Tag("wNN"),
          run: scripts/run-week.sh wNN [module]
          hints: hints/wNN/   solutions (peek only AFTER your tests are green): solutions/wNN/

## S1 Try it (3h): mode A
## S2 Build (3h): SF tickets with acceptance tests already in the repo
## S3 Incident (2h): mode B, one or two incidents
## S4 Consolidate (2h): ledger, flashcards, self-check, retro
**If stuck:** <one line>
```
Every session: numbered, time-boxed steps summing exactly to the session length; **Tips and common problems** (symptom → why → fix); **Done when** checkboxes (observable). Keep self-check answers in `<details><summary>Answers</summary>…</details>`.

### S1 Try it (mode A) rules
- Start with a 10–20 minute "worked example": a runnable class in the repo (already green) that the learner runs, reads line by line, and modifies.
- Then 3–6 small exercises (`EX-NN-xx`), each with a tiny test class that is red at the start. Order them easy → hard so greens appear every 15–30 minutes.
- Always include a "predict before you run" micro-step (write the expected output in a comment first).
- Compare with TypeScript where it helps (she knows JS/TS well): short side-by-side snippets, only for differences that cause mistakes.
- Introduce each term right before its first use; one sentence definition, then code.

### S2 Build rules
- 1–2 ShopFlow tickets (`SF-xx`, from `shopflow-backlog.md`) implemented against acceptance tests that are in the repo and red at the start.
- Steps: read the tests first (what behaviour do they demand?), implement one test at a time, refactor when green, commit per green group.

### S3 Incident rules (mode B)
Write the ticket in-world voice (support message or alert). Then use these labelled steps in this order:
1. **PREDICT** (5–10 min): 3–4 questions answered in writing BEFORE running anything; answers in `<details>`.
2. **REPRODUCE**: exact command, expected red output excerpt.
3. **INVESTIGATE**: the named tool and the exact signal to look for. Hint ladder at `hints/wNN/INC-NN-x.md` (5 levels in `<details>`: symptom only → where to look → tool → concept name to search → fix outline). Tell her to open one level at a time after 10 minutes of trying.
4. **STUDY** (targeted): the specific reading (EJ item, docs section) to read now that she has seen the failure, with 3 questions to answer from it.
5. **FIX**, 6. **PROVE** (red test green + regression test + guard test still green), 7. **GENERALIZE** (ledger entry in `reality-lab/LEDGER.md` format + the one-minute answer).
Each incident maps to the catalog IDs (RL-xx) given in the assignment.

### S4 Consolidate rules
Ledger entries, flashcards (list 5 Q/A pairs), weekly retro lines, ADR or note if the week calls for one, self-check questions with answers in `<details>`, and the week's output list.

## Repo rules (`shopflow-lab`)
- Layout: `core/` = plain Java (weeks 3–9, packages `shopflow.wNN`); `api/` = Spring Boot (weeks 10–12, packages `shopflow.api.wNN`). Each week is **standalone**: it re-declares any small types it needs inside its own package; it must not depend on another week's package. (This lets weeks be built in parallel.)
- Java release 21 (project compiles with `maven.compiler.release=21`); JUnit 6 + AssertJ 3.27. Do not use preview features.
- **Starter must compile and be red by assertion**, never by compile error. Stubs return wrong values or throw `UnsupportedOperationException("TODO EX-NN-xx")`. Use clear failure messages (`.as("...")`).
- Tag every test class `@Tag("wNN")`. Use `@DisplayName` and `@Nested` so test names read like requirements. Order tests easy → hard.
- Incident code: the starter contains the *buggy* production code with a plausible, innocent-looking implementation (no "BUG HERE" comments). Provide the failing repro test(s) plus at least one **guard test** (green at the start, must stay green after the fix).
- Solutions: `solutions/wNN/<module>/src/...` mirrors the repo path and contains only the files that change. The fix must be minimal and idiomatic.
- Hints: `hints/wNN/<id>.md` with the 5-level ladder for each incident and for the hardest exercise of S1/S2.
- Verify every week before reporting: `scripts/verify-solutions.sh wNN [module]` must print `OK red` and `OK green`. Run `scripts/run-week.sh wNN [module]` once to confirm the student view and failure messages are readable.
- Only edit `core/pom.xml` / `api/pom.xml` to add a dependency if truly needed (note it in your final reply). Do not touch other weeks' files.
- Never commit secrets. No network calls in tests. No sleeps longer than 1 s. Concurrency tests must be deterministic (use latches/barriers) and finish < 5 s.

## Content rules
- Keep all existing facts, references (books/items/docs by title; no invented URLs), SF tickets, ADR numbers and RL IDs from the old phase files, but reorganise them into this structure.
- Reading is **just-in-time**: reference the specific EJ item/doc section at the step where it explains something she just saw.
- Be concrete: exact commands, expected output, file/class names. Mark unsure commands "(verify against current docs)".
- Tone: direct, encouraging, no filler. Assume 10 h/week total.
