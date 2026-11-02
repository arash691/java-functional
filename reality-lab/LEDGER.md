# Reality Lab Ledger

One entry per catalog item. An item counts only when it was reproduced, observed with a tool, and explained out loud.

## Template (copy per entry)

### RL-XX — Title
- **Date / phase:**
- **Symptom:** what you saw (error, slow, wrong data)
- **Why (theory):** the mechanism, in 3–5 sentences
- **Reproduce:** repo path / commit with a failing test or script
- **Observe with:** tool and the exact evidence (log line, plan node, dump view, metric)
- **Fix:** the change
- **Prevent:** test, lint rule, ArchUnit rule, config default, review checklist item
- **1-minute answer:** what you would say in an interview
- **Confidence (1–5):**

## Weekly routine
1. Pick 2 items from `CATALOG.md` tagged for the current phase.
2. Write a failing test or script that shows the problem.
3. Observe it with the listed tool. Capture the evidence.
4. Fix it and add a regression test.
5. Record the ledger entry and say the one-minute answer aloud.

## Using an AI agent
Reproduce first. Then ask the agent for hypotheses and compare them with your evidence. You must be able to explain the root cause without the agent. Never paste production data or secrets.

## Entries
(Add entries below.)
