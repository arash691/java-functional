# Handoff: context for a fresh session

Read this first, then `README.md`, `lessons/README.md` and `lessons/pilot/AUTHORING.md`. This replaces the chat history of the session that built the repo.

## Goal
A self-study roadmap that takes a strong frontend (JS/TS) developer with no Java to a senior Java backend engineer: Java, Spring Boot, PostgreSQL, concurrency, distributed systems, architecture, CI/CD, Kubernetes, cloud. About 60 weeks at about 10 h/week. The learner is a person (use they/them if unsure). The goal is to close the gap between tutorials and production or interview reality.

## Decisions already made
- **Learning method:** project-first and problem-first, in three modes chosen per concept.
  - **A. Build-along** for new concepts: run, read, modify, predict, break.
  - **B. Break-it / Incident:** ticket, predict, reproduce (red test), investigate, study just in time, fix, prove, generalize (ledger).
  - **C. Design-it:** build the naive version, pressure-test it, redesign, record an ADR.
- **Week shape:** four sessions: S1 Try it (3h), S2 Build (3h), S3 Incident (2h), S4 Consolidate (2h). Steps are numbered and time-boxed, with Tips and Done-when checklists. Self-check answers go in `<details>`.
- **Spine project:** ShopFlow (e-commerce). Tickets are in `shopflow-backlog.md`.
- **Reality Lab:** 110 production-style problems in `reality-lab/CATALOG.md`, recorded in `reality-lab/LEDGER.md`.
- **Versions:** JDK 25 LTS (release 21 compiled), Spring Boot 4.1.2 (Spring 7, Hibernate 7, Jackson 3), JUnit 6, Testcontainers 2.x, PostgreSQL 17+.

## Status
| Area | State |
|---|---|
| Weeks 1–12 | Done in the new format: `lessons/pilot/week-01.md` … `week-12.md` + `shopflow-lab/` (core and api modules, hints, solutions). Verified red then green for w00, w03–w12 with `scripts/verify-solutions.sh`. |
| Weeks 13–60 | Older format (`lessons/phase-*.md`): step-by-step but reading-first. **Not yet converted.** |
| `java-backend-roadmap.md`, `reality-lab/CATALOG.md`, `shopflow-backlog.md` | **Not yet synced** with the new method (old weekly rhythm, no pointers to repo tests). |
| Lesson text and references | **Not reviewed by a learner.** Commands (SDKMAN, Homebrew, Docker, IDE debugger, `dependency:tree`, JaCoCo) and book/doc references are unverified. |

## Known issues
- `verify-solutions.sh w07 core` failed once in a full run, then passed 6 of 6 re-runs. Cause unknown. If it recurs, capture the full output.
- Week 6 `ReportsTest` asserts under 150 ms and `LookupTest` compares timings. They could flake on a slow or busy machine.
- Week 12 needs Docker (Testcontainers).
- Spring Boot 4 changed package names, starter names and test annotations compared with Boot 3. Weeks 10–12 list these in their Tips; many online tutorials still show Boot 3.
- Maven is not installed. `./mvnw` downloads it. A bootstrap copy in `/tmp/mvn-bootstrap` was used once to generate the wrapper and can be ignored.

## Next steps (in order)
1. Sync the top-level docs (`java-backend-roadmap.md`, `reality-lab/CATALOG.md`, `shopflow-backlog.md`, `lessons/README.md` index) with the project-first method and the repo.
2. Have a person follow week 1 and week 4 literally and note every stumble. Fix the lessons before converting more weeks.
3. Convert weeks 13+ in waves, one phase at a time. Later phases need different repo support: SQL scripts plus a Postgres container (phase 4), a runnable app with dump tools (phases 5–6), a Compose/Kubernetes lab (phases 7–8). Phases 3 and 7 are mostly mode C.
4. When authoring weeks, follow `lessons/pilot/AUTHORING.md` and run `scripts/verify-solutions.sh wNN [module]` (must print `OK red` and `OK green`).

## Working agreements
- Keep roadmap work in this repo, not in `investigation-stuff`.
- Do not commit unless asked (files are currently staged, nothing committed).
- Do not invent URLs; reference books and docs by title. Mark unsure commands "(verify against current docs)".
- Report results faithfully: say what was verified and what was not.
