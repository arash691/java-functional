# M3: People, roles and organizations (the "who" of every business system)

**Mode:** A (build-along), with a naive-first refactor  **Block:** Modeling (M3 of M1–M8)
**Outcome:** you can tell apart *person, user, account, customer, employee, contractor, party, role, organization, organization unit, membership, assignment, labor, contract, tenant and actor*; you can explain why "Employee extends Person" breaks in real systems; and you have built a small tested Java library (Party, Role, OrgUnit, Assignment, TimePeriod) that you will reuse in M5 to M8.

**Before you start**
- M1 and M2 done (classes, invariants, composition over inheritance, entity vs value).
- Practice workspace: `~/shopflow-notes/modeling/concepts/` with `notes/m3.md` and a Maven project `concepts-lib/` (S2 creates it). Rubric: `modeling-lab/RUBRIC.md` (C rows).
- Why this week exists: almost every practical domain in `modeling-lab/DOMAINS.md` (HR, banking, access, insurance, booking) starts with *who is involved and in what capacity*. Getting this wrong early is expensive to fix once data exists.
- Reading (by title, just in time): *Analysis Patterns: Reusable Object Models* (Fowler), chapters on Party and Accountability (the same ideas are in the martinfowler.com articles "Party" and "Accountability", verify against current site); *The Data Model Resource Book* (Silverston), the Party chapter, skim only; search the titles "Falsehoods programmers believe about names" and "Falsehoods programmers believe about addresses"; *Effective Java* Items 17 and 50 (immutability, defensive copies); *Domain-Driven Design Distilled* chapter on ubiquitous language (light).
- No database yet. If you think "which table?", write it in `notes/questions.md`. M7 and M8 answer it.

---

## S1 Try it (3h): who is who

1. (10m) **Guess.** One line each, no search: person, user, customer, employee, role, organization. Keep this for the S4 comparison.
2. (20m) **Decide before you read.** For each sentence, say which *kind of thing* it talks about (a human, a login, a capacity a human acts in, a company, a part of a company, a period of time). Write the answers, then open the details.
   a) "Sara forgot her password." b) "Sara is a customer since 2021." c) "Sara joined Acme as a developer in March." d) "Acme's Payments team is in the Engineering department." e) "Sara left Acme in June and came back in October." f) "Sara can approve invoices for Acme but only read them for Beta Ltd." g) "Sara worked 7.5 hours on Tuesday." h) "Sara's contract says 40 hours a week."
   <details><summary>Answers</summary>

   a) a **user account** (credentials), belonging to a person. b) a **role** (customer) the person plays toward an organization, with a start date. c) an **employment**: a role of a person in an organization, plus a **position**. d) **organization units** in a tree (the team is a child of the department). e) the same person has **two employment periods**; the person did not change, the role relationship did. f) a **role in context**: the role belongs to the membership of a user in an organization, not to the user alone. g) **labor**: work actually performed (time spent). h) a **contract**: an agreement about future work.
   </details>
3. (25m) **The naive model.** Create `s1/naive/` and write exactly this, then add the scenarios below as small `main` or JUnit checks:
   ```java
   class Person { String name; String email; }
   class Employee extends Person { String department; double salary; }
   class Customer extends Person { String loyaltyCard; }
   ```
   **Predict** how each scenario fails, then try it: (1) Sara is an employee *and* a customer of her own company. (2) Sara leaves and returns later. (3) Sara moves from Engineering to Sales on 1 July; what was her department in May? (4) Sara's email changes; does the customer record change too? (5) A company (not a person) is also a customer. Write one sentence per failure. This is the classic "Employee extends Person" trap: inheritance models *what something is forever*, but employee, customer, user are things a human **is for a while**.
4. (30m) **Refactor: role as a relationship with a period.** Create `s1/better/`:
   ```java
   record TimePeriod(LocalDate from, LocalDate to) {           // to == null means open-ended
       TimePeriod { if (to != null && !to.isAfter(from)) throw new IllegalArgumentException("to must be after from"); }
       boolean contains(LocalDate d) { return !d.isBefore(from) && (to == null || d.isBefore(to)); }
   }
   record Person(String id, String givenName, String familyName) {}
   record PartyRole(Person person, String roleType, TimePeriod period) {}
   ```
   Make the five scenarios work. Note what changed: `Person` no longer knows if it is an employee; "is Sara an employee on 15 May?" is now a question you ask the *roles*. Draw it:
   ```mermaid
   classDiagram
     Person "1" --> "0..*" PartyRole : plays
     PartyRole --> TimePeriod : valid during
   ```
5. (25m) **The Party pattern.** Predict: a customer can be a person (Sara) or a company (Acme); an employer is a company; a supplier can be either. How would you avoid `PersonCustomer`, `CompanyCustomer`, `PersonSupplier`...? Read **Fowler, *Analysis Patterns*, Party** (or the article). Then sketch: `Party` as a common supertype of `Person` and `Organization`, with `PartyRole` pointing to a `Party`. Then spend 10 minutes on "Falsehoods programmers believe about names": write five assumptions you made in step 3 (`givenName` + `familyName` only? one name? one address?) and which you would change (for example, store a `fullName` and an optional `preferredName`, never split names by rule).
6. (25m) **Organization units and assignment.** A company has a tree: Acme → Engineering → Payments team. Predict how you would store "Sara works in Payments from 1 July, in Engineering before". Answer: not as a field on Sara but as an **Assignment**, an association *with its own data* (who, which unit, period, optionally a manager and a title). Draw `Person 1 — * Assignment * — 1 OrgUnit`, and `OrgUnit` with an optional `parent`. Questions to write down: can a unit have two parents (matrix organization)? Can a person have two assignments at once? What happens to child units when a unit is closed?
7. (25m) **User, account, role, permission.** Read this contrast and fix the model in your notes:
   - Wrong first: `User { String email; String password; String role; }`, which merges person, login and capacity, and has one global role.
   - Better: `Person` (a human) ←0..1 `UserAccount` (credentials, status, created) and `Membership` = (account, organization, role, period). Permissions belong to the **role**, and the role applies **in the context of an organization** ("role in context"). This is RBAC (role-based access control) in model terms: *who* (account) *may do what* (permission) *where* (organization).
   - Decide: why can one human have two accounts (work and private) and why might an invitation exist before the account does?
8. (20m) **Labor, contract, position, assignment.** Without looking, order from "agreement" to "reality": position (a seat in the structure: "Senior Developer, Payments"), contract (agreed terms with a person), assignment (the person sits in the unit now), timesheet entry (labor: hours actually worked on a day). Write one sentence each and one thing that can change without the others changing (for example, the position remains when the person leaves). Add the **actor**: the party that *did* an action, kept for audit ("who changed this price?"). Actor is not always a person: it may be a system job.

**Tips and common problems**
- You keep adding `isEmployee` booleans → that is a role in disguise; replace with a role relationship and a period.
- Everything becomes a `Party` subclass → use inheritance only for Person vs Organization; roles are **data**, not subclasses.
- The tree "looks recursive and scary" → an `OrgUnit` with a `parent` reference (or `null` for the root) is the whole idea; navigation code comes later.
- Names and addresses feel boring → they cause real production bugs; keep fields few and optional.

**Done when**
- [ ] `notes/m3.md` has your guesses, the five naive failures and one sentence each on person, user, role, assignment, labor, actor.
- [ ] `s1/better` runs the five scenarios; the Mermaid diagrams for roles and for the org tree are saved.

---

## S2 Build (3h): the concepts library

Today you write a small library you will reuse. You decide the shape; the scenario list tells you what must work. Write each test **before** the code.

1. (15m) **Project.** Create `concepts-lib/` (Maven, Java 21, JUnit 6 and AssertJ, same as `shopflow-lab/core/pom.xml`; copy its dependencies section). Package `concepts.party`. Commit the empty skeleton and tag it `m3-start`.
2. (20m) **TimePeriod.** Records are immutable value objects. Scenarios: `from` is before `to`; `to` may be open (null); `contains(date)` includes `from` and excludes `to`; `overlaps(other)` is true for [1 Jan, 1 Mar) and [1 Feb, null), false for [1 Jan, 1 Feb) and [1 Feb, 1 Mar) (touching is not overlapping); `isOpenEnded()`. (M4 goes deeper into intervals.)
3. (25m) **Party.** Create `sealed interface Party permits Person, Organization` with `id()` and `displayName()`. `Person(id, givenName, familyName)` and `Organization(id, legalName)`. Scenarios: blank names rejected; two persons with the same id are equal (decide: entity or value? write why in a comment; EJ Item 10/11 apply).
4. (30m) **PartyRole.** `PartyRole(Party party, RoleType type, TimePeriod period)` with `RoleType` an enum or sealed interface (`CUSTOMER`, `EMPLOYEE`, `SUPPLIER`...). Add a `Roles` collection class: `roleTypesOn(Party, LocalDate)`, `isInRole(party, type, date)`. Scenarios: Sara is customer and employee at the same time; Sara leaves 1 June and returns 1 October (two periods, not one); a role may not overlap with itself for the same party and type (reject).
5. (30m) **OrgUnit and Assignment.** `OrgUnit(id, name, parent)` (parent may be null). `Assignment(Person person, OrgUnit unit, TimePeriod period, String title, Person manager)`. Scenarios: `pathOf(unit)` returns "Acme / Engineering / Payments"; `unitOf(person, date)` returns the unit valid on that date (May vs July); two overlapping assignments of one person are allowed only when you decide so (write the rule down; recommended: allowed, with a `primary` flag, or rejected, but state which); the manager of the unit on a date.
6. (25m) **Membership and permissions.** `UserAccount(id, person, email, status)`, `Role(name, Set<Permission>)`, `Membership(account, Organization org, Role role, TimePeriod period)`. Scenarios: Sara is `ADMIN` in Acme and `READER` in Beta; `can(account, permission, org, date)` is true in Acme and false in Beta; suspended account can do nothing; an invited email with no account yet produces a `PendingInvitation`, not a fake account.
7. (20m) **Actor.** `sealed interface Actor permits PersonActor, SystemActor`. `AuditRecord(Actor actor, String action, Instant at)`. Scenario: a nightly job records its changes as `SystemActor("price-sync")`.
8. (15m) **Review and commit.** Score the library with RUBRIC C1 to C8 (C7: did you use roles and periods instead of booleans?). Fix the lowest row. Commit, tag `m3-lib`.

**Tips and common problems**
- Tests need a lot of setup → write small helper factories (`aPerson("Sara")`); that is normal, and it shows the model is usable.
- Overlap logic gets wrong at the boundary → draw the two periods on paper with half-open ends: [from, to).
- `Role` vs `RoleType` confusion → `RoleType` = capacity of a party toward the business (customer, employee); `Role` = bundle of permissions in the access system. Same word, different concepts; note both in the glossary (S4).
- Equality surprises with records → a record compares all fields; for entities with identity decide `equals` on `id` only (EJ Item 10).

**Done when**
- [ ] All scenarios above are tests, green, in `concepts-lib/`.
- [ ] No `isEmployee`-style boolean exists anywhere; roles have periods.
- [ ] Tags `m3-start` and `m3-lib` exist and the rubric score is in `notes/m3.md`.

---

## S3 Practice (2h): three mini cases (class diagrams only)

For each case use this loop: nouns and verbs → which of **party, role, organization, org unit, assignment, membership, labor, actor** appear → Mermaid diagram → name one question the model must answer ("who taught class 5A on 3 March?").

1. (10m) Re-read your glossary guesses from S1 step 1; note which words changed meaning for you.
2. (30m) **Clinic staff.** A clinic has doctors, nurses and receptionists. Some doctors also work at a second clinic. Patients are people; a patient may also be an employee. Doctors have specialties and weekly shifts. Decide: is "Doctor" a class or a role? Where do shifts (labor) live?
3. (30m) **School.** A school has students, teachers, parents, classes per school year, and subjects. A parent may be a teacher at the same school. Teachers teach classes in a year. Decide: person or role? What is the period of the class assignment?
4. (30m) **Marketplace seller and buyer.** A marketplace lets any person or company be a buyer, a seller, or both. Sellers have shop pages; staff members of a seller company manage the shop with different permissions. Decide: party, role, membership, tenant (the seller company owns its data).
5. (20m) **Review.** For each diagram check: no booleans for roles, every role/assignment has a period, no inheritance from Person except where truly necessary, one question answered. Score C5, C6, C7. Save the diagrams.

<details><summary>Reference thoughts, open after your diagrams</summary>

Clinic: `Person` + `PartyRole(DOCTOR | NURSE | PATIENT)` with periods; `Organization` clinic; `Assignment(person, clinic, period)` handles two clinics; `Shift` is planned labor, `WorkedShift` actual labor. School: `Enrollment(student, class)` and `TeachingAssignment(teacher, class, subject)` are association classes with their own data; a `SchoolYear` is a `TimePeriod`. Marketplace: `Organization` seller is a tenant; `Membership(account, org, role)` gives staff permissions; buyer and seller are `PartyRole` types on the same `Party`.
</details>

**Tips and common problems**
- You model "Doctor extends Person" again → ask: can a doctor also be a patient next year? Then role.
- Everything becomes one huge diagram → split by question; each diagram answers one question.

**Done when**
- [ ] Three diagrams, each with the one question it answers, scores written.

---

## S4 Consolidate (2h)

1. (35m) **Domain glossary v1.** In `notes/glossary-m3.md` write one line each, with "not to be confused with": user, person, party, role (business capacity), role (access bundle), organization, org unit, membership, assignment, labor, contract, tenant, actor. Cover all 13 terms. Rule: one term, one meaning; if two things share a word, rename one. Compare with your S1 guesses.
2. (20m) **Ledger entries.** Add two entries in `~/shopflow-notes/ledger.md` (template `reality-lab/LEDGER.md`): "Employee extends Person fails because...", "Role in context: the role lives on the membership because...". Say each aloud in under a minute.
3. (20m) **Flashcards.** Five cards: (1) person vs user vs employee? (2) why not `Employee extends Person`? (3) what is an assignment and why does it have a period? (4) role in context? (5) labor vs contract?
4. (25m) **Self-check.** Answer aloud first. 1) Name three facts about Sara that change at different times. 2) How do you answer "which department was Sara in on 15 May?" 3) Why does a user's role live on the membership? 4) Why might an audit actor be a system?
   <details><summary>Answers</summary>

   1) Email (contact), department (assignment), salary (contract); also employment periods. 2) Find the assignment whose period contains 15 May. 3) The same account has different rights in different organizations. 4) Scheduled jobs and imports change data too; you need to say *who*.
   </details>
5. (10m) **Retro** in `retro.md`: what worked, what blocked me, what I change. Did "roles as data" feel natural?
6. (10m) **Preview M4.** Read the first scenario list of M4 S2 (Money and Period). Write down how you would currently add 0.1 + 0.2 in Java.

**Forward note (M7/M8):** the same ideas become tables: `party`, `person`, `organization`, `party_role(valid_from, valid_to)`, `org_unit(parent_id)`, `assignment`, `membership`. Preview only: `party_role(party_id, role_type, valid_from, valid_to)`.

**Output:** `concepts-lib/` (tags `m3-start`, `m3-lib`), three class diagrams, glossary v1, 2 ledger entries, 5 flashcards, retro.

**If stuck:** reduce to Person, Role, TimePeriod and make three scenarios pass; add the rest later. Use the 15-minute routine in `lessons/README.md`.
