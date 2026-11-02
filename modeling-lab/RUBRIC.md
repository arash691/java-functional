# Modeling Rubric

Score every model you make (class model or data model) against this list. Be strict with yourself; there is no teacher. Write the score and the one thing to improve in your ledger.

Each row: 0 = missing, 1 = partly, 2 = good. Class model total is out of 16; data model total is out of 16.

## Class model
| # | Check | What "2" looks like |
|---|---|---|
| C1 | **Names** | Class names are nouns from the brief's own language; no `Manager`, `Helper`, `Data`, `Info`, `Util` |
| C2 | **Responsibility** | Each class can be described in one sentence without "and" |
| C3 | **Behaviour with data** | Rules live next to the data they protect (`account.withdraw(x)`, not `bankUtil.withdraw(account, x)`) |
| C4 | **Invariants** | Invalid states are impossible to create (constructor checks, no public setters for important fields) |
| C5 | **Relationships** | Each relationship has a direction and multiplicity (1, 0..1, 0..*), and you can say whether it is "has-a", "owns" or "knows" |
| C6 | **Inheritance used sparingly** | Inheritance only for a true "is-a" with the same behaviour; otherwise interface or composition |
| C7 | **Twist and concepts handled** | The domain's twist from `DOMAINS.md` has an explicit, explained decision, and the domain's building blocks (Money, Interval, Party/Role, history from M3 and M4) are used instead of raw `double`, two loose dates, or a `type` string |
| C8 | **Scenarios run** | At least 3 scenario tests (a happy path, a rule violation, an edge case) pass against your classes |

## Data model
| # | Check | What "2" looks like |
|---|---|---|
| D1 | **One fact, one place** | No fact stored twice (except deliberate snapshots, labelled as such) |
| D2 | **Keys** | Every table has a primary key; every relationship has a foreign key; natural vs surrogate key choice explained |
| D3 | **Cardinality** | Every relationship shows 1:1, 1:N or M:N; M:N has a join table, with its own attributes when needed |
| D4 | **Types and nulls** | Money is `NUMERIC` (never float), time is `TIMESTAMPTZ` or `DATE` on purpose, nullable columns only where "unknown" is truly valid |
| D5 | **Constraints** | `NOT NULL`, `UNIQUE`, `CHECK` and foreign keys enforce rules the brief states |
| D6 | **Normalization** | You can show the table is at least 3NF, or say which rule you broke on purpose and why |
| D7 | **History and time** | Anything that changes over time and matters later (price, status, address, role, salary) has effective dates, a history or a snapshot, and you can answer "what was true on date X" with a query |
| D8 | **Questions answerable** | The 5 questions in the exercise can each be answered by a SQL query that you wrote and ran |

## Reading your score
- **13–16**: ship it. Note what made it good.
- **9–12**: fix the lowest two rows, then move on.
- **8 or less**: redo the model after reading the matching lesson step again; do not start the next domain with the same mistake.
- A repeated low row across three domains is a **gap in understanding**, not bad luck. Write it in the retro and spend the next S4 on it.
